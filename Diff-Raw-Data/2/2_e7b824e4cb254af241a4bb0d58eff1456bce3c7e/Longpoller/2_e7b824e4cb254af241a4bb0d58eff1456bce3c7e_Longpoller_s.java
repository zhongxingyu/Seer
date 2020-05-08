 package com.emergency.codeblue;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.os.AsyncTask;
 import android.os.Message;
 
 public class Longpoller extends AsyncTask<Integer, Integer, Long> {
 	// This Task will handle long polling whether it is during a Code Blue or
 	// not.
 	protected Long doInBackground(Integer... code_id) {
 		// Long Polling keeps re-triggering after timeouts or updates being
 		// detected
 		while (true) {
 			String curcode = PhysicianActivity.getCodeId();
 			// DEBUG, comment out:
 			// System.out.println("Longpolling iteration started");
 			// System.out.println("The current code id is: " + curcode);
 
 			// If there is no active Code Blue, will wait for a new one to start
 			if (curcode == "-1") {
 
 				// DEBUG, comment out:
 				System.out.println("curcode detected as '-1'");
 
 				// Attempts to do a Get request to the server.
 				try {
 					// DEBUG, comment out:
 					System.out
 							.println("attempting to request /get_new_codeblue/");
 
 					HttpClient httpClient = new DefaultHttpClient();
 					HttpGet get = new HttpGet(
 							"http://dabix.no-ip.org/get_new_codeblue/");
 
 					HttpResponse response = httpClient.execute(get);
 					// DEBUG, comment out:
 					System.out
 							.println("Response received from /get_new_codeblue/");
 
 					BufferedReader reader = new BufferedReader(
 							new InputStreamReader(response.getEntity()
 									.getContent()));
 
 					StringBuilder builder = new StringBuilder();
 					for (String line = null; (line = reader.readLine()) != null;) {
 						builder.append(line).append("\n");
 					}
 
 					// Builds a JSONObject with the response of the request
 					JSONTokener tokener = new JSONTokener(builder.toString());
 					JSONObject newCodeInfo = new JSONObject(tokener);
 
 					System.out.println(newCodeInfo.toString());
 
 					String type = newCodeInfo.get("type").toString().trim();
 
 					// Handle new active Code Blue being detected server-side
 					if (type.equals("timeout") == false) {
 						// DEBUG, comment out:
 						System.out.println("type not recognized as 'timeout'");
 
 						String newcode = newCodeInfo.get("id").toString()
 								.trim();
 						String time = newCodeInfo.get("time").toString().trim();
 
 						// DEBUG, comment out:
 						System.out
 								.println("Got " + newcode + " as new code id");
 
 						// DEBUG, comment out:
 						// System.out.println("Current Code id is now set to "
 						// + PhysicianActivity.getCodeId()
 						// + " and starting timer.");
 
 						newCodeHandler(time, newcode);
 
 					} else {
 						// DEBUG, comment out:
 						System.out.println("Timeout detected.");
 					}
 
 				} catch (Exception e) {
 					System.out
 							.println("Got error while looking for new Code Blue: "
 									+ e.toString());
 					return (long) 1;
 				}
 			}
 			// If there is an active Code Blue, will poll for Orders or End
 			else {
 				try {
 					System.out
 							.println("attempting to request /codeblue_orders/");
 
 					HttpClient httpClient = new DefaultHttpClient();
 					HttpPost post = new HttpPost(
 							"http://dabix.no-ip.org/codeblue_orders/");
 
 					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
 							1);
 					nameValuePairs.add(new BasicNameValuePair("codeblue_id",
 							curcode));
 
 					ArrayList<String> orderIds = PhysicianActivity
 							.getOrderIds();
 
 					String send = "";
 					for (String s : orderIds) {
 						send = send + s + ',';
 					}
 
 					nameValuePairs
 							.add(new BasicNameValuePair("order_ids", send));
 
					// System.out.println("Current orders: " + send);
 
 					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
 					HttpResponse response = httpClient.execute(post);
 
 					BufferedReader reader = new BufferedReader(
 							new InputStreamReader(response.getEntity()
 									.getContent()));
 					StringBuilder builder = new StringBuilder();
 					for (String line = null; (line = reader.readLine()) != null;) {
 						builder.append(line).append("\n");
 					}
 
 					System.out.println("RAW: " + builder.toString());
 
 					// Builds a JSONObject with the response of the request
 					JSONTokener tokener = new JSONTokener(builder.toString());
 					JSONObject newCodeInfo = new JSONObject(tokener);
 
 					System.out.println(newCodeInfo.toString());
 
 					// Handle end of active Code Blue being detected server-side
 					if (newCodeInfo.get("type").toString().trim().equals("end")) {
 						stopCodeHandler();
 
 					}
 
 					// new orders
 					if (newCodeInfo.get("type").toString().trim()
 							.equals("new_orders")) {
 
 						System.out.println("NEW ORDER!!!");
 						System.out.println(newCodeInfo.get("orders").toString()
 								.trim());
 					}
 
 					else { // DEBUG, comment
 						System.out.println("Type not recognized as 'end'");
 					}
 				} catch (Exception e) {
 					System.out
 							.println("Got error while looking for end of Code Blue: "
 									+ e.toString());
 					return (long) 1;
 				}
 
 			}
 		}
 	}
 
 	public static void stopCodeHandler() {
 		Message msg = new Message();
 		// do whatever code to end codeblue
 		System.out.println("ENDING CODE");
 		msg.obj = "-1";
 		PhysicianActivity.mHandlerEndCode.sendMessage(msg);
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// Handler to deal with new Codes.
 	public static void newCodeHandler(String startTime, String codeId) {
 		System.out.println("startTime: " + startTime);
 		try {
 			// Set codeID
 			Message msg = new Message();
 			msg.obj = codeId;
 			PhysicianActivity.mHandlerSetCodeId.sendMessage(msg);
 
 			// start code with given time since epoch for code start...
 			Message msg2 = new Message();
 			msg2.obj = startTime;
 			PhysicianActivity.mHandlerStartCode.sendMessage(msg2);
 
 		} catch (Exception e) {
 			System.out.println("ERROR");
 			System.out.println(e);
 		}
 
 		// boolean timerStarted = PhysicianActivity.getTimerState();
 		// if (timerStarted == false) {
 		// PhysicianActivity.gotNewCode(startTime);
 		// }
 		//
 
 	}
 }
