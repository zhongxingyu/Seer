 package ru.spbau.cheque;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 import java.io.*;
 import java.net.Socket;
 
 public class Main extends Activity
 {
     static final int GET_PHOTO_FROM_CAMERA_REQUEST = 1;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Button takePhotoBtn = (Button) findViewById(R.id.takePhoto);
         Button spendingsSumBtn = (Button) findViewById(R.id.spendingsSum);
         takePhotoBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent pictureIntent = new Intent(
                         android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                 startActivityForResult(pictureIntent, GET_PHOTO_FROM_CAMERA_REQUEST);
             }
         });
         spendingsSumBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                Intent intent = new Intent(Main.this, spendingsSumActivity.class);
                 startActivity(intent);
             }
         });
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == Activity.RESULT_OK && requestCode == GET_PHOTO_FROM_CAMERA_REQUEST) {
             Socket sock = null;
             try {
                 Bitmap pic = (Bitmap) data.getExtras().get("data");
                 sock = new Socket("192.168.222.169", 3843);
                 OutputStream os = sock.getOutputStream();
                 pic.compress(Bitmap.CompressFormat.JPEG, 100, os);
                 os.flush();
             } catch (IOException e) {
                 e.printStackTrace();
             }
             finally {
                 if (sock != null)
                     try {
                         sock.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
             }
         }
     }
 }
