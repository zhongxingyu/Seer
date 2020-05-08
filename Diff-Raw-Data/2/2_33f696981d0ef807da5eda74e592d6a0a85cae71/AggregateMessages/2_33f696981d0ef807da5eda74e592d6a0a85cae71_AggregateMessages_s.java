 package com.example.tracker;
 
 import Client.TestClient;
 
 public class AggregateMessages {
 	
 	public static StringBuffer messages = null;
 	public static int count = 0;
 	public static int packageCount = 0;
 	public static final int max = 200;
 	public static final int maxPerPackage = 20;
 	
 	public static synchronized void addMessages(String message, boolean isEnd, boolean isOnline) {
 		/**Check corner case */
 		if(messages == null) {
 			messages = new StringBuffer();
 		}
 		
 		if(isEnd && message == null) {
			if(isOnline && messages.length() != 0) {
 				messages.append("END" + '\n');
 				String[] messages = new String[2];
 				messages[0] = TrackerService.deviceID;
 				messages[1] = AggregateMessages.getMessages();
 				TestClient client = new TestClient("209.129.244.6", messages);
 			}
 			cleanMessages();
 			return;		
 		}
 		
 		if(message == null || message.length() == 0) {
 			return;
 		}
 		
 		if(count < max) {
 			messages.append(message + '\n');
 			count++;
 			packageCount++;
 		}
 		
 		if(packageCount > maxPerPackage) {
 			if(isOnline) {
 				String[] messages = new String[2];
 				messages[0] = TrackerService.deviceID;
 				messages[1] = AggregateMessages.getMessages();
 				TestClient client = new TestClient("209.129.244.6", messages);
 			}
 			cleanMessagesNoTouchCount();
 			return;			
 		}
 		
 		return;
 	}
 	
 	public static synchronized void setMessages(String newMessages) {
 		messages = new StringBuffer(newMessages);
 	}
 	
 	public static synchronized String getMessages() {
 		if(messages != null)
 			return messages.toString();
 		else 
 			return new String();
 	}
 	
 	public static synchronized void cleanMessages() {
 		messages = null;
 		count = 0;
 		packageCount = 0;
 	}
 	
 	public static synchronized void cleanMessagesNoTouchCount() {
 		messages = null;
 		packageCount = 0;
 	}
 	
 	public static synchronized void clearPackageCount() {
 		packageCount = 0;
 	}
 }
