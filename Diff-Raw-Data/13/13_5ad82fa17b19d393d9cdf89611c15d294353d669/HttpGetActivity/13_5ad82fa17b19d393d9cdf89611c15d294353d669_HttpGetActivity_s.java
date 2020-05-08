 package com.ozgur.yazilim;
  
 import java.io.BufferedReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.media.Ringtone;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.project.scadamobile.R;
 
 
 public class HttpGetActivity extends Activity implements OnClickListener {
 	/** Called when the activity is first created. */
 	
 	
 	private ImageView im;
 	private EditText t1;
 	private EditText t2;
 	private EditText t3;
 	private EditText t4;
 	private RelativeLayout mylayout;
 	
 	private FileWriter fWriter;
 	private final String Fname="/sdcard/scadadatas.txt";
 	
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		mylayout = (RelativeLayout)findViewById(R.id.rellayout);
 		im = (ImageView)findViewById(R.id.imageView1);
 		im.setVisibility(View.INVISIBLE);
 		t1 = (EditText) findViewById(R.id.editText1);
 		t1.setInputType(InputType.TYPE_NULL);
 		t2 = (EditText) findViewById(R.id.editText2);
 		t2.setInputType(InputType.TYPE_NULL);
 		t3 = (EditText) findViewById(R.id.editText3);
 		t3.setInputType(InputType.TYPE_NULL);
 		t4 = (EditText) findViewById(R.id.editText4);
 		t4.setInputType(InputType.TYPE_NULL);
 		
 		Button b1 = (Button) findViewById(R.id.button1);
 		b1.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.button1) {
 			String str = "";
 			String inStr = "";
 			URL myUrl;
 			try {
 				myUrl = new URL("http://www.alimertozdemir.com/csvtojson.php");
 				URLConnection myConn = myUrl.openConnection();
 				BufferedReader in = new BufferedReader(new InputStreamReader(
 						myConn.getInputStream()));
 				while ((inStr = in.readLine()) != null) {
 					str = str + inStr;
 				}
 				writeToFile(str);
 				in.close();
 				
 			} catch (Exception e) {
 				Toast msg = Toast.makeText(getApplicationContext(), "Can't access to server", Toast.LENGTH_LONG);
             	msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
 				e.printStackTrace();
 			}
 			
 		
 			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
         	Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
         	
 			
 			try{
 				JSONArray ja = new JSONArray(str);
 				 int n = ja.length();
 		            for (int i = 0; i < n; i++) {
 		                JSONObject obj = ja.getJSONObject(i);
 		                 
 		                String date = obj.getString("StartDate");
 		                t1.setText(date);
 		                
 		                String time = obj.getString("CurrentTime");
 		                t2.setText(time);
 		                
 		                String temp = obj.getString("MaxTemp");
 		                t3.setText(temp);
 		                
 		                String runtime = obj.getString("Runtime");
 		                t4.setText(runtime);
 		                
 		                int  sic = obj.getInt("MaxTemp");
 		                if(sic > 239)
 		                {
 		                	mylayout.setBackgroundColor(Color.parseColor("#B40404"));
 		                	Toast msg = Toast.makeText(getApplicationContext(), "SYSTEM OVERHEATED!", Toast.LENGTH_LONG);
 		                	msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
 		                	im.setImageResource(R.drawable.hotim);
 		                	im.setVisibility(View.VISIBLE);
 		                	msg.show();
 		                	r.play();
 		                	r.wait(1000000000);	
 		                } 
 		                else
 		                {	
 		                	mylayout.setBackgroundColor(Color.parseColor("#04B4AE"));
 				            im.setImageResource(R.drawable.coldim);
 			               	im.setVisibility(View.VISIBLE);	
 			               	r.wait(1000000000);	
 		                }
 		            }
 		           
 			}
 			catch (Exception e) {
 				Toast msg = Toast.makeText(getApplicationContext(), "Veri Alnamad!", Toast.LENGTH_LONG);
 				msg.setGravity(Gravity.CENTER, msg.getXOffset() / 2, msg.getYOffset() / 2);
 			}
 			
 		}
 
 	}
 
 	public static String executeHttpGet(String url) throws Exception {
 		BufferedReader in = null;
 		String page = "";
 		try {
 			HttpClient client = new DefaultHttpClient();
 			client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
 					"android");
 			HttpGet request = new HttpGet();
 			request.setURI(new URI(url));
 			HttpResponse response = client.execute(request);
 
 			in = new BufferedReader(new InputStreamReader(response.getEntity()
 					.getContent()));
 			StringBuffer sb = new StringBuffer("");
 			String line = "";
 			String NL = System.getProperty("line.separator");
 			while ((line = in.readLine()) != null) {
 				sb.append(line + NL);
 			}
 			in.close();
 			page = sb.toString();
 
 		} catch (Exception e) {
 			page = "false";
 
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return page;
 	}
 	
 	public void writeToFile(String str)
 	{
 		try{
 			fWriter=new FileWriter(Fname);
 			fWriter.write(str);
 			fWriter.flush();
 			fWriter.close();
 			
 		}
 		catch (Exception e){
 			
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.app_menu, menu);
 	    return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent callIntent = new Intent(Intent.ACTION_CALL);
 		callIntent.setData(Uri.parse("tel:05554641879"));
 		switch (item.getItemId()) {
 	    case R.id.call:
 	    startActivity(callIntent);
 	    return true;
 	    case R.id.exit:
 	    finish();
 	    break;
 		}
 		return super.onOptionsItemSelected(item); 
 	} 
 }
 
