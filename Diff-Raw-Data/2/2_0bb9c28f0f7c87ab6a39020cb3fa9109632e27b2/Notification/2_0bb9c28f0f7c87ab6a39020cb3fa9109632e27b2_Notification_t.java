 package com.kdcloud.server.gcm;
 
 import java.io.IOException;
 
 import com.google.android.gcm.server.Message;
 import com.google.android.gcm.server.Sender;
 import com.kdcloud.server.entity.Task;
 
 public abstract class Notification {
 	
 	private static final String GOOGLE_API_KEY = "AIzaSyCdog7MGmFI9XdMUR2OKDhWsioqkiiFzB4";
 	
 	public static final void notify(Task task) {
 		gcmNotify(task);
 	}
 	
 	
 	private static final void gcmNotify(Task task) {
 		Sender sender = new Sender(GOOGLE_API_KEY);
 		String id = Long.toString(task.getId());
 		Message message = new Message.Builder().addData("id", id).build();
 		try {
			sender.sendNoRetry(message, task.getRegId());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
