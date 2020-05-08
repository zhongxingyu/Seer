 package com.esiea.qrcode;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class MainActivity extends Activity
 {
     QRCodeView qrcvodeView = new QRCodeView(this);
     QRCodeController qrCodeController = null;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         qrCodeController = QRCodeController.getController(qrcvodeView);
         this.qrCodeController.setActiveView(this.qrcvodeView);
         setContentView(R.layout.main);
         Button bouton = (Button)findViewById(R.id.button1);
         bouton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
 
                 qrCodeController.generateAndDisplayQRcode();
             }
         });
         this.qrCodeController.askForUpdate();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu,menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         { 
         case R.id.quit:
            this.qrCodeController.destroyController();
             this.finish();
             break;
         case R.id.save:
             this.qrCodeController.savePicture();
             break;
         case R.id.load:
             this.load();
             break;
         }
         return true;
     }
 
     public void Update(BitmapDrawable bitdraw)
     {
         ImageView imageView = (ImageView)this.findViewById(R.id.imageView1);
         imageView.setBackgroundDrawable(bitdraw);
     }
 
     public String getData()
     {
         EditText findViewById = (EditText) this.findViewById(R.id.editText1);
         String getDataString = findViewById.getText().toString();
         return getDataString;
     }
 
     public String getHiddenData()
     {
 
         EditText findViewById = (EditText) this.findViewById(R.id.EditText02);
         String getDataString = findViewById.getText().toString();
         return getDataString;
     }
 
     private void load()
     {
         Intent i = new Intent(Intent.ACTION_GET_CONTENT);
         i.setType("image/*");
         startActivityForResult(i, 1);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode,Intent imageReturnedIntent)
     {
         super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
         switch (requestCode)
         {
         case 1:
             if (resultCode == RESULT_OK)
             {
                 Uri selectedImage = imageReturnedIntent.getData();
                 String[] filePathColumn = {MediaStore.Images.Media.DATA};
                 Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                 cursor.moveToFirst();
                 int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                 String filePath = cursor.getString(columnIndex);
                 cursor.close();
                 Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);
                 this.qrCodeController.analyseImage(yourSelectedImage);
             }
         }
     }
 
     public void showMessage(String message)
     {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(message)
         .setCancelable(false)
         .setNegativeButton("Ok", new DialogInterface.OnClickListener()
         {
             public void onClick(DialogInterface dialog, int id)
             {
                 dialog.cancel();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
     }
 }
 
