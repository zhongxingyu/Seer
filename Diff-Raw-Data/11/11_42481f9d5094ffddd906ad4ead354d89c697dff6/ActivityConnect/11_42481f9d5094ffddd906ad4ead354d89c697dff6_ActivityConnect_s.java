 package com.RoboMobo;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: loredan
  * Date: 08.08.13
  * Time: 11:59
  * To change this template use File | Settings | File Templates.
  */
 public class ActivityConnect extends Activity
 {
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.url);
     }
 
     public void connect(View view)
     {
         String ip = ((EditText) findViewById(R.id.et_url)).getText().toString();
         Networking.init(ip, ((ToggleButton) findViewById(R.id.tb_server)).isChecked());
     }
 }
