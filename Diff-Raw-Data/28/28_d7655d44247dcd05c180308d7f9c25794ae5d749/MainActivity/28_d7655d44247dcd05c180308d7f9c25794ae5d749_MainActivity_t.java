 package com.wt.srichards.checkin;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.os.Build;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         Button btn = (Button) findViewById(R.id.button1);
         btn.setOnClickListener(new OnClickListener() {
 
             public void onClick(View v) {

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost("http://peer.to/checkin/get.php");
            	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            	EditText note = (EditText)findViewById(R.id.editText2);
                EditText name = (EditText)findViewById(R.id.editText1);
                Calendar c = Calendar.getInstance(); 
            	int seconds = c.get(Calendar.SECOND);
                pairs.add(new BasicNameValuePair("users_name", name.getText().toString()));
                pairs.add(new BasicNameValuePair("note", note.getText().toString()));
                pairs.add(new BasicNameValuePair("device_version", Build.VERSION.RELEASE));
                pairs.add(new BasicNameValuePair("device", Build.MODEL));
                pairs.add(new BasicNameValuePair("manufacturer", Build.MANUFACTURER));
                pairs.add(new BasicNameValuePair("time", String.valueOf(seconds)));
             	try {
         			post.setEntity(new UrlEncodedFormEntity(pairs));
         		} catch (UnsupportedEncodingException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         			Log.d("UnsupportedEncodingException", null);
         		}
                 try {
         			HttpResponse response = client.execute(post);
         		} catch (ClientProtocolException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         			Log.d("ClientProtocolException", null);
         		} catch (IOException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         			Log.d("IOException", null);
         		}
             }
         });
         
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
