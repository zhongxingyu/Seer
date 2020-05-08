 package org.imaginea.botbot;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.net.HttpURLConnection;
import java.net.MalformedURLException;
 import java.net.Socket;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 
 
 public class CommandTransmitter {
 	Socket soc = null;
 	PrintStream ps = null;
 	static ServerProperties sp = new ServerProperties();
 	static String sessionID = null;
 	static String tempSession=null;
 	String serverIP = sp.serverIP;
 	String port=sp.port;
 	String sessionName=sp.sessionName;
 	String serverName=sp.serverName;
 	BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
 	static String sUrl="";
 	
 	public CommandTransmitter() {
 		if(!serverName.equalsIgnoreCase("")){
 			sUrl="http://" + serverIP +":"+port+"/"+serverName;
 		}else{
 			sUrl="http://" + serverIP +":"+port;
 		}
 		System.out.println("Creating session....");
 		new CreateSessionTask().execute();
 
 	}
 
 	public void publish(Command command) {
 		queue.offer(command.getData());
 		
 	}
 
 	public boolean checkSession(String id) {
 		try {
 			URL url = null;
 			url = new URL(sUrl+"/api/recordsessions/" + id);
 			HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			connection.setDoInput(true);
 			connection.setRequestProperty("Accept", "application/json");
 			connection.setRequestProperty("Content-Type",
 					"application/json; charset=UTF-8");
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					connection.getInputStream()));
 			String inputLine;
 			while ((inputLine = in.readLine()) != null)
 				System.out.println(inputLine);
 			in.close();
 			return true;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	
 	/**
 	 * @author moiz
 	 * 
 	 * CreateSessionTask is a sub class to create an AsyncTask
 	 * AsyncTask is a thread which runs  parallel to the main UI thread.
 	 *  
 	 *
 	 */
 	
 
 	class CreateSessionTask extends AsyncTask<Void, Void, Void> {
 		int recordID=1;
 		int prevRecord=1;
 		/**
 		 * This method is called when CreateSessionTask.execute() is invoked.
 		 * This method performs two actions, first it creates a session and then it waits for population of
 		 * queue in a while loop, when queue is not empty it removes the head and writes the data to the server.
 		 */
 		@Override
 		protected Void doInBackground(Void... nothing) {
 
 			// creating session
 				System.out.println("In background");
 				recordID=1;
 				prevRecord=0;
 				
 			while (true) {
 
 				try {
 					String data=queue.take();
 					String session=getSession();
 					//it waits until queue is populated
 					postRecord(data,session);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		}
 		//Performs writing data to the server
 
 		void postRecord(String data,String session) {
 			// TODO Auto-generated method stub
 			if (!session.equalsIgnoreCase("")) {
 				try {
 					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 					String currentDateTime = df.format(new Date());
 
 					Log.i("bot-bot", "In create record Async task:- " + session
 							+ data);
 					String postData = "{\"entryNo\":\"" + recordID + "\","
 							+ "\"prevEntryNo\":\"" + prevRecord
 							+ "\",\"recordSession\":{\"id\":\"" + session
 							+ "\"},\"entryTime\":\"" + currentDateTime
 							+ "\",\"payload\":\"" + data + "\"}";
 					Log.i("bot-bot", postData);
 					URL url = null;
 					url = new URL(sUrl + "/api/recordentries");
 					HttpURLConnection connection = (HttpURLConnection) url
 							.openConnection();
 					connection.setDoOutput(true);
 					connection.setRequestProperty("Accept", "application/json");
 					connection.setRequestProperty("Content-Type",
 							"application/json; charset=UTF-8");
 					OutputStreamWriter out = new OutputStreamWriter(
 							connection.getOutputStream());
 
 					out.write(postData);
 					out.close();
 					prevRecord = recordID;
 					recordID++;
 					Map<String, List<String>> respHeaders = connection
 							.getHeaderFields();
 
 					if (connection.getResponseCode() == 201) {
						Log.i("bot-bot", respHeaders.get("Location").toString());
 					} else {
 						Log.i("bot-bot", "Invalid Response");
 					}
 					BufferedReader in = new BufferedReader(
 							new InputStreamReader(connection.getInputStream()));
 					String inputLine;
 					while ((inputLine = in.readLine()) != null)
 						System.out.println(inputLine);
 					in.close();
 				} catch (Exception e) {
 					Log.i("bot-bot", "Exception occured in postMethod: "+e.getMessage());
 				}
 			} else {
 				Log.i("bot-bot",
 						"Inside post record. Data not posted as session was empty.");
 			}
 
 		}
 		
 		String getSession(){
 			String session = "";
 			if (CommandTransmitter.sessionID == null
 					|| CommandTransmitter.sessionID.contentEquals("")) {
 				CommandTransmitter.sessionID=createNewSession();
 				session = CommandTransmitter.sessionID;
 			} else {
 				try {
 					URL url = new URL(sUrl + "/api/recordsessions/"+CommandTransmitter.sessionID);
 					HttpURLConnection connection = (HttpURLConnection) url
 							.openConnection();
 					BufferedReader rd = new BufferedReader(
 							new InputStreamReader(connection.getInputStream()));
 					StringBuffer sb = new StringBuffer();
 					String line;
 					while ((line = rd.readLine()) != null) {
 						sb.append(line);
 					}
 					rd.close();
 					String result = sb.toString();
 					String status = result.substring(
 							result.indexOf("<status>") + 8,
 							result.indexOf("</status>"));
 					if (status.contentEquals("stopped")) {
 						recordID=1;
 						prevRecord=0;
 						CommandTransmitter.sessionID = createNewSession();
 						session = CommandTransmitter.sessionID;
 					} else if (status.contentEquals("started")) {
 						session = CommandTransmitter.sessionID;
 					}
 				} catch (IOException e) {
 					Log.i("bot-bot",
 							"Unable to get status of the record. Application will continue without recording");
 				}
 			}
 			return session;
 		}
 		
 		String createNewSession() {
 			String genSessionId = "";
 			try{
 				URL url = null;
 				url = new URL(sUrl + "/api/recordsessions");
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				connection.setDoOutput(true);
 				connection.setRequestProperty("Accept", "application/json");
 				connection.setRequestProperty("Content-Type",
 						"application/json; charset=UTF-8");
 				OutputStreamWriter out = new OutputStreamWriter(
 						connection.getOutputStream());
 				out.write("{\n\"name\":\"" + sessionName + "\",\"status\":\"started\"\n}");
 				out.close();
 
 				Map<String, List<String>> respHeaders = connection
 						.getHeaderFields();
 
 				if (connection.getResponseCode() == 201) {
					String temp = respHeaders.get("Location").get(0);
 					genSessionId = temp.substring(temp
 							.lastIndexOf("/") + 1);
 					Log.i("TASK", "Session ID received: "
 							+ genSessionId);
 				} else {
 					throw new Exception("Invalid responce");
 				}
 			}catch(Exception e){
 				Log.i("bot-bot","Unable to create record because of :" + e);
 				Log.i("bot-bot","System will continue to work without recording.");
 			}
 
 			return genSessionId;
 		}
 	}
 
 }
 
 
 
