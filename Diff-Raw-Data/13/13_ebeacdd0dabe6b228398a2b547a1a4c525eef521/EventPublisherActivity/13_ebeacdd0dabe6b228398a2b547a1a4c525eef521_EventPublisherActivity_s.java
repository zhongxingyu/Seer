 package com.adhi.webclient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLConnection;
 
 import com.adhi.webclient.util.ClientUtil;
 
 
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class EventPublisherActivity extends Activity {
 	private EditText messageTxtBx;
 	private Button publish;
 	private EditText topicTxtBx;
 	private String topic ="";
 	private String message = "";
 	private String TAG = ClientUtil.TAG;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.publisher);
 		
 		messageTxtBx = (EditText) findViewById(R.id.pub_msg);
 		
 		topicTxtBx = (EditText) findViewById(R.id.topic);
 		
 		publish = (Button) findViewById(R.id.btn_publish);
 		publish.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				publish .setEnabled(false);
 				topic = topicTxtBx.getText().toString();
 				message = messageTxtBx.getText().toString();
 				Log.i(TAG, "topic " + topic + ", message " +message );
 				new ConnectBackground().execute();
 				
 			}
 		});
 	}
 	
 	private class ConnectBackground extends AsyncTask<Void, Void, String>{
 
 		@Override
 		protected String doInBackground(Void... arg0) {
 			// TODO Auto-generated method stub
 			String data = null;
 			
 			try {
 				  URL url = new URL(ClientUtil.getMessagingServiceUrl());
 				
 				 /*
 				  HttpURLConnection con = (HttpURLConnection) url
 				    .openConnection();
 				  data = readStream(con.getInputStream());
 				  */
 				  data = sendMessage(url, getMessageBody());
 				  } catch (Exception e) {
 				  e.printStackTrace();
 				}
 			return data;
 		}
 		
 		@Override
 		protected void onPostExecute(String result) {
 			// TODO Auto-generated method stub
 			
			Log.i(TAG, result);
 			//txt.setText(result);
			publish .setEnabled(true);
			Toast.makeText(getApplicationContext(), "Messaged Published", Toast.LENGTH_SHORT).show();
 			
 		}
 		private String readStream(InputStream in) {
 			  BufferedReader reader = null;
 			  String data = "";
 			  try {
 			    reader = new BufferedReader(new InputStreamReader(in));
 			    String line = "";
 			    while ((line = reader.readLine()) != null) {
 			     // System.out.println(line);
 			    	data = data + line;
 			    }
 			  } catch (IOException e) {
 			    e.printStackTrace();
 			  } finally {
 			    if (reader != null) {
 			      try {
 			        reader.close();
 			      } catch (IOException e) {
 			        e.printStackTrace();
 			        }
 			    }
 			  }
 			return data;
 		}
 		
 		private String sendMessage(URL url, String message) throws IOException{
 			URLConnection connection = url.openConnection();
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 
 			connection.connect();
 
 			OutputStream os = connection.getOutputStream();
 			PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
 			String payload = message;
 			pw.write(payload);
 			pw.close();
 
 			InputStream is = connection.getInputStream();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 			String line = null;
 			StringBuffer sb = new StringBuffer();
 			while ((line = reader.readLine()) != null) {
 			    sb.append(line);
 			}
 			is.close();
 			String response = sb.toString();
 			return response;
 		}
 
 		private String getMessageBody() {
 			// TODO Auto-generated method stub
 			/*
 			String body = "<?xml version=\"1.0\"?>" +
 						  "<root>" +
 							"<title>CSE message</title>" +
 							"<message>" + message + "</message>" +
 					   	   "</root>";
 			 */
 			String body = "Notification," + message; //message body in text/csv format
 													//format is title,body
 			return body;
 		}
 	}
 
 }
