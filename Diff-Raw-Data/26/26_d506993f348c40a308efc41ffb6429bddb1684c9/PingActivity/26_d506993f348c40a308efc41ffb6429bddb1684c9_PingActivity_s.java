 package com.networkbenchmark;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.Editable;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class PingActivity extends Activity {
 	EditText edit;
 	TextView text;
 	
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.ping_view);
 		
 		edit = (EditText) findViewById(R.id.editText1);
		edit.setText("127.0.0.1");
 		text = (TextView) findViewById(R.id.textView1);
 		Button button = (Button) findViewById(R.id.button1);
 		button.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Editable host = edit.getText();
 				try {
					String pingCmd = "/system/bin/ping";
					
 					// The first way to create process
 //					Runtime r = Runtime.getRuntime();
 //					Process p = r.exec(pingCmd+host.toString());
 					
 					// The second way to create process, ping the android.com 
					Process p = new ProcessBuilder()
				       .command(pingCmd, "android.com")
				       .redirectErrorStream(true)
				       .start();
 
 					p.waitFor();
 					
 					String pingResult = pingCmd.toString();
 					// 0=success, 1=fail, 2=error
 					int exit = p.exitValue();
 					if (exit == 0) {
 						BufferedReader in = new BufferedReader(
 								new InputStreamReader(p.getInputStream()));
 						BufferedReader stdError = new BufferedReader(
 								new InputStreamReader(p.getErrorStream()));
 						String inputLine;
 						while ((inputLine = in.readLine()) != null) {
 							System.out.println(inputLine);
 							text.setText(inputLine + "\n\n");
 							pingResult += inputLine;
 							text.setText(pingResult);
 						}
 						while ((inputLine = stdError.readLine()) != null) {
 							System.out.println(inputLine);
 							text.setText(inputLine + "\n\n");
 							pingResult += inputLine;
 							text.setText(pingResult);
 						}
 						Log.v("Ping", pingResult);
 
 						in.close();
 						stdError.close();
 					} else if (exit == 1) {
 						pingResult += "\nFailed, exit = 1";
 						text.setText(pingResult);
 					} else {
 						pingResult += "\nError, exit = 2";
 						text.setText(pingResult);
 					}
 					
 				} catch (IOException e) {
 					Log.e("Ping", e.toString());
 //					text.setText("IOException: "+e.toString());
 				} catch (InterruptedException e) {
 					Log.e("Ping", e.toString());
 //					text.setText("InterruptedException: "+e.toString());
 				}
 			}
 		});
 		
 	    // Make sure we're running on Honeycomb or higher to use ActionBar APIs
 	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 	    // Show the Up button in the action bar.
 	        getActionBar().setDisplayHomeAsUpEnabled(true);
 	    }
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    case android.R.id.home:
 	        NavUtils.navigateUpFromSameTask(this);
 	        return true;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 }
