 package org.cbase.smartahoy;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Html;
 import android.widget.TextView;
 
 public class AboutDialog extends Activity {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.about);
 
         TextView aboutText=(TextView)findViewById(R.id.aboutText);
 
        aboutText.setText(Html.fromHtml("SmarAhoy! v0.1 is the app to show the notifications from <a href='https://github.com/nhcham/Ahoy'>Ahoy!</a> on a Sony SmartWatch\n by <a href='http://github.com/cketti'>cketti</a> and <a href='http://ligi.de'>ligi</a> the Icon was designed by Jana Schramm"));
     }
 }
