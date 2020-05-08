 package com.smarthome;
 
 import java.io.IOException;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
public class sendMessageToProxy extends AsyncTask<String, Void, String> {
 	// params [0] Server; [1] Port; [2] TopicName; [3] topic/queue;
 	// [4] message;
 	
 	@Override
	protected String doInBackground(String... params) {
 		try {
			AndroidPublisher publisher = new AndroidPublisher(params[0],
					Integer.valueOf(params[1]), params[2]);
			publisher.setMessage(params[4]);
 			if (params[3].equals("topic")) {
 				publisher.publishToTopic();
 			} else {
 				publisher.publishToQueue();
 			}
 		} catch (IOException e) {
 			Log.e("Publisher", "Can't publish the message");
 		}
 		return null;
 	}
 

 }
