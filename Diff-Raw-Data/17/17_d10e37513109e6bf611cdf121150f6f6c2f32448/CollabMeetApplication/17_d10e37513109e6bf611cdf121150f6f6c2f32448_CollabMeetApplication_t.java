 package com.collabmeet.mobile;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import android.app.Application;
 import android.location.Location;
 import android.os.AsyncTask;
 
 public class CollabMeetApplication extends Application {
 	public String userName = "";
 	public String password = "";
 	public String meetingID = "";
 	public String members = "";
 	public String master = "";
 	public String status = "";
 	public int locUpdateCount = 0;
 	public final int UDP_RECEIVE_PORT = 5005;
 	public boolean isBroadcastEnabled = true;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 
 	public void broadcastLocation(Location loc) {
 		if(!isBroadcastEnabled || members.length() == 0 || loc == null) {
 			return;
 		}
 
 		String msg = userName + ":node:mobilelocation:" + loc.getLatitude() + ":" + loc.getLongitude() + ":" + locUpdateCount;
 		locUpdateCount++;
 
 		new BroadcastInBackground().execute(msg, members);
 	}
 
 	private class BroadcastInBackground extends AsyncTask<String, Void, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 			String msg = params[0];
 			String ipAddresses = params[1];
 
 			for (String str: ipAddresses.split(",")) {
 				try {
 					String sockAddr[] = str.split(":");
					String ip = sockAddr[1];
					int port = Integer.parseInt(sockAddr[2]);
 					
 					if(port == 0) {
 						continue;
 					}
 
 					System.out.println("Sending location to " + str);
 					Socket s = new Socket(ip, port);					
 					OutputStream out = s.getOutputStream();
 					PrintWriter output = new PrintWriter(out);
 					output.print(msg);
 					output.flush();
 					s.close();
 				} catch (Exception e) {
 					continue;
 				}
 			}
 
 			return true;
 		}
 
 		protected void onPostExecute(Boolean flag) {
 		}
 	}
 }
