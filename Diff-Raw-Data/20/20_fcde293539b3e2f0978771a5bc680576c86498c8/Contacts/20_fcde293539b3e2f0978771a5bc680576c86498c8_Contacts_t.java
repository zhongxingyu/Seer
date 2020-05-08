 package com.newsrss.Feed;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Created with IntelliJ IDEA.
  * User: AndrewTivodar
  * Date: 18.04.13
  * Time: 15:47
  * To change this template use File | Settings | File Templates.
  */
 public class Contacts extends Activity {
     TextView tel1Text,tel2Text;
     ImageView cal_btnIR,cal_btnNI;
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         setContentView(R.layout.contacts);
 
         tel1Text=(TextView) findViewById(R.id.contacts_Ireland_tel_text);
         tel1Text.setText(Html.fromHtml("<b>Tel:</b> 00353 1 637 7200"));
         tel2Text=(TextView) findViewById(R.id.contacts_NI_tel_text);
         tel2Text.setText(Html.fromHtml("<b>Tel:</b> 00442 8 904 3584"));
       // cal_btnIR=(ImageView) findViewById(R.id.contacts_callIR);
        //cal_btnNI=(ImageView) findViewById(R.id.contacts_callNI);
 
         ImageView.OnClickListener ocCall=new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent callIntent = new Intent(Intent.ACTION_CALL);
                 callIntent.setData(Uri.parse("tel:0035316377200"));
                 startActivity(callIntent);
             }
         };
 
         ImageView.OnClickListener ocCallNI=new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent callIntent = new Intent(Intent.ACTION_CALL);
                 callIntent.setData(Uri.parse("tel:0044289043584"));
                 startActivity(callIntent);
             }
         };
 
         cal_btnIR.setOnClickListener(ocCall);
         cal_btnNI.setOnClickListener(ocCallNI);
     }
 }
