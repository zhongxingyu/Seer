 package com.smarthome;
 
 import java.io.IOException;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
public class sendMessageToProxy extends AsyncTask {
 	// params [0] Server; [1] Port; [2] TopicName; [3] topic/queue;
 	// [4] message;
 	
 	@Override
	protected Object doInBackground(Object... params) {
 		try {
			AndroidPublisher publisher = new AndroidPublisher((String)params[0],
					Integer.valueOf((String)params[1]), (String)params[2]);
			publisher.setMessage((String)params[4]);
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
