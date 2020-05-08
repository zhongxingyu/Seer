 package ru.allgage.geofriend;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 
 public class SendTask extends AsyncTask<Object, String, String> {
 
 	@Override
 	protected String doInBackground(Object... arg) {
 		synchronized(TaskSocket.socket) {
 			try {
 				TaskSocket.out.writeUTF("updateStatus");
				double t = (Double)arg[0];
 				TaskSocket.out.writeDouble((Double)arg[0]);
 				TaskSocket.out.writeDouble((Double)arg[1]);
 				TaskSocket.out.writeUTF((String)arg[2]);
				TaskSocket.out.flush();
 				String res = TaskSocket.in.readUTF();
 				return res;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return "error";
 	}
 	
 	protected void onPostExecute(String result) {
         // TODO: add pop-up
     }
 
 }
