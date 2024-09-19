package com.example.diabetesdetect

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // กำหนดตัวแปรด้วย findViewById
        val detectBTN = findViewById<Button>(R.id.detectBTN)
        val pregnanciestxt = findViewById<EditText>(R.id.pregnanciestxt)
        val glucosetxt = findViewById<EditText>(R.id.glucosetxt)
        val bloodPressuretxt = findViewById<EditText>(R.id.blood_pressuretxt)
        val skinThicknesstxt = findViewById<EditText>(R.id.skin_thicknesstxt)
        val insulintxt = findViewById<EditText>(R.id.insulintxt)
        val bmitxt = findViewById<EditText>(R.id.bmitxt)
        val agetxt = findViewById<EditText>(R.id.agetxt)

        detectBTN.setOnClickListener {
            // รับข้อมูลจาก EditText และแปลงเป็นตัวเลข
            val pregnancies = pregnanciestxt.text.toString()
            val glucose = glucosetxt.text.toString()
            val bloodPressure = bloodPressuretxt.text.toString()
            val skinThickness = skinThicknesstxt.text.toString()
            val insulin = insulintxt.text.toString()
            val bmi = bmitxt.text.toString()
            val age = agetxt.text.toString()

            // ตรวจสอบว่าผู้ใช้ได้ป้อนข้อมูลครบถ้วนหรือไม่
            if (pregnancies.isNotEmpty() && glucose.isNotEmpty() && bloodPressure.isNotEmpty() && skinThickness.isNotEmpty()
                && insulin.isNotEmpty() && bmi.isNotEmpty() && age.isNotEmpty()) {
                // ส่งข้อมูลไปยัง API ด้วย OkHttp
                sendRequest(pregnancies, glucose, bloodPressure, skinThickness, insulin, bmi, age)
            } else {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendRequest(pregnancies: String, glucose: String, bloodPressure: String, skinThickness: String,
                            insulin: String, bmi: String, age: String) {
        // สร้าง FormBody สำหรับส่งข้อมูล
        val formBody = FormBody.Builder()
            .add("pregnancies", pregnancies)
            .add("glucose", glucose)
            .add("blood_pressure", bloodPressure)
            .add("skin_thickness", skinThickness)
            .add("insulin", insulin)
            .add("bmi", bmi)
            .add("age", age)
            .build()

        // สร้าง Request
        val request = Request.Builder()
            .url("http://10.0.2.2:3000/api/diabetes") // เปลี่ยนเป็น URL ที่ถูกต้อง
            .post(formBody)
            .build()

        // ส่งคำขอด้วย OkHttp
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failure: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val result = response.body?.string()
                    val jsonObject = JSONObject(result)
                    val prediction = jsonObject.getString("ผลการคาดการ")
                    runOnUiThread {
                        // แสดงผลในรูปแบบของ AlertDialog
                        showResultDialog(prediction)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Error: ${response.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun showResultDialog(prediction: String) {
        // สร้างและแสดง AlertDialog
        AlertDialog.Builder(this)
            .setTitle("ผลการวินิจฉัย")
            .setMessage(prediction)
            .setPositiveButton("ตกลง") { dialog, _ ->
                dialog.dismiss() // ปิด Dialog เมื่อกดปุ่มตกลง
            }
            .show()
    }
}
