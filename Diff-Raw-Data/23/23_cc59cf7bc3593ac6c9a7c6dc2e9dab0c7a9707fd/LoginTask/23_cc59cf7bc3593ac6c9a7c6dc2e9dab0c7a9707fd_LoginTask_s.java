 package ru.allgage.geofriend;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.widget.TextView;
 
 
 public class LoginTask extends AsyncTask<String, String, String> {
 	TaskSocket sock;
 	DataOutputStream dout;
     DataInputStream din;
     String msg = "";
     boolean isLogged = false;
     TextView view;
     Activity parent;
 
     LoginTask(TextView v, Activity cont) {
     	view = v;
     	parent = cont;
     }
     
 	
 	@Override
 	public String doInBackground(String... str) {
 		// TODO Auto-generated method stub
 		try {
 			sock = new TaskSocket();
 			sock.log = str[0];
 			sock.pass = str[1];
 			sock.writeAuth();
			//sock = new Socket("192.168.1.102", 7777);
 			din = sock.in;
 			publishProgress("sending data to server");
 			if(str.length == 2) {
 				sock.writeMessages("login");
 			}
 			else {
 				sock.writeMessages("register", str[2], str[3]);
 			}
 			String res = din.readUTF();
 			return res;
 
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	
 	protected void onPostExecute(String result) {
         view.setText(result);
         if(result.equals("logged in")) {
         	Intent intent = new Intent(parent, MapActivity.class);
 			parent.startActivity(intent);
 			parent.finish();
 			
         }
         sock.close();
 
     }
 	
 }
