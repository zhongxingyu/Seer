 package com.example.payback;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 class AccessNet{
 	public static boolean AccountLogin(String Email, String Password) throws InterruptedException{
 		String status  ="fail";
 		boolean retval = false;
 		Logger CONLOG = Logger.getLogger(AccessNet.class .getName());
 
 		String params = "userEmail="+Email+"&password="+Password;
 		String urlstub = "db_verify_login.php";
 		//if you go around renaming these urlstubs the app will not work and Hohyun will hate you.
 		CONLOG.info("Attempting to call server at: "+urlstub+", "+params);
 		status = simpleServerCall(urlstub, params);
 		//CONLOG.info("Server Returned "+status);
 		if(status.equalsIgnoreCase("success")||status.equalsIgnoreCase("1")||status.equalsIgnoreCase("true"))
 			retval = true;
 		return retval;
 	}
 	
 	public static boolean AccountCreation(String Email, String Password, String FName, String LName) throws InterruptedException{
 		boolean retval = false;
 		String params = "fname="+FName+"&lname="+LName+"&email="+Email+"&password="+Password;
 		String status = "fail";
 		String urlstub = "db_account_create.php";
 		//calling server
 		status = simpleServerCall(urlstub, params);
 		if(status.equalsIgnoreCase("success")||status.equalsIgnoreCase("1")||status.equalsIgnoreCase("true"))
 			retval=true;
 		return retval;
 	}
 	
 	public static boolean AddFriend(String femail, String uemail, String password) throws InterruptedException{
 		boolean retval = false;
 		String params = "friendEmail="+femail+"&userEmail="+uemail+"&password="+password;
 		String status = "fail";
 		String urlstub = "db_friendof_create.php";
 		//calling server
 		status = simpleServerCall(urlstub, params);
 		if(status.equalsIgnoreCase("success")||status.equalsIgnoreCase("1")||status.equalsIgnoreCase("true"))
 			retval=true;
 		return retval;
 	}
 	
 	public static boolean DeleteFriend(String femail, String uemail, String password) throws InterruptedException, JSONException{
 		boolean retval = false;
 		String params = "userEmail="+uemail+"&password="+password+"&friendEmail="+femail;
 		String urlstub = "db_friendof_deleteOne.php";
 		//calling server
 		String status = simpleServerCall(urlstub, params);
 		if(status.equalsIgnoreCase("success")||status.equalsIgnoreCase("1")||status.equalsIgnoreCase("true"))
 			retval=true;
 		return retval;
 	}
 	
 	public static boolean DeleteAccount(String uemail, String password) throws InterruptedException, JSONException{
 		//$attribute decides the function taken. Currently there are three functions:
         //trans:                Deletes tuples that match LenderID and user $email where Borrower = $value
         //note:                        Deletes tuples that match ReceiveID and user $email where Send == $value
         //account:                Deletes tuple for account. Account tuple will be gone permanently after action.
 		String value = "null" , attribute = "account";
 		boolean retval = false;
 		String params = "email="+uemail+"&password="+password+"&attribute="+attribute+"&value"+value;
 		
 		String urlstub = "DeleteTuple.php";
 		//calling server
 		JSONObject status = jsonServerCall(urlstub, params);
 		if(status.has("1")||status.has("true")||status.has("success"))
 			retval=true;
 		return retval;
 	}
 	
 	public static boolean DeleteTrans(String femail, String uemail, String password) throws InterruptedException, JSONException{
 		//$attribute decides the function taken. Currently there are three functions:
         //trans:                Deletes tuples that match LenderID and user $email where Borrower = $value
         //note:                        Deletes tuples that match ReceiveID and user $email where Send == $value
         //account:                Deletes tuple for account. Account tuple will be gone permanently after action.
 		String attribute = "trans";
 		boolean retval = false;
 		String params = "email="+uemail+"&password="+password+"&attribute="+attribute+"&value"+femail;
 		//String status = "fail";
 		String urlstub = "DeleteTuple.php";
 		//calling server
 		JSONObject status = jsonServerCall(urlstub, params);
 		if(status.has("1")||status.has("true")||status.has("success"))
 			retval=true;
 		return retval;
 	}
 
 	public static boolean AddTrans(String uemail, String password, int amount, String description, String lemail, String bemail) throws InterruptedException, JSONException{
 		Logger TRANLOG = Logger.getLogger(AccessNet.class .getName());
 		TRANLOG.setLevel(Level.INFO);
 		boolean retval = false;
 		String params = "email="+uemail+"&password="+password+"&amount="+amount+"&description="+description+"&lenderEmail="+lemail+"&borrowerEmail="+bemail;
 		String urlstub = "db_transaction_create.php";
 		String status = "fail";
 		//calling server
 		status = jsonServerCall(urlstub, params).getString("result");
 		if(status.equalsIgnoreCase("success")||status.equalsIgnoreCase("1")||status.equalsIgnoreCase("true"))
 			retval=true;
 		TRANLOG.info("AddTrans was " + retval);
 		return retval;
 	}
 	
 	public static boolean AddNotif(String uemail, String password, String description, String targetEmail) throws InterruptedException, JSONException{
 		boolean retval = false;
 		String params = "userEmail="+uemail+"&password="+password+"&sendInfo="+description+"&receiveEmail="+targetEmail;
 		String urlstub = "db_notif_create.php";
 		//calling server
 		JSONObject obj = jsonServerCall(urlstub, params);
 		if(obj.get("result").toString().equalsIgnoreCase("1"))
 			retval=true;
 		return retval;
 	}
 	
 	public static boolean DeleteAllNotif(String uemail, String password) throws InterruptedException, JSONException{
 		boolean retval = false;
 		String params = "userEmail="+uemail+"&password="+password;
 		String urlstub = "db_notif_deleteAll.php";
 		//calling server
 		JSONObject obj = jsonServerCall(urlstub, params);
 		if(obj.get("result").toString().equalsIgnoreCase("1"))
 			retval=true;
 		return retval;
 	}
 	
 	public static boolean DeleteOneNotif(String uemail, String password, String nID) throws InterruptedException, JSONException{
 		boolean retval = false;
 		String params = "userEmail="+uemail+"&password="+password+"&noteID="+nID;
 		String urlstub = "db_notif_deleteOne.php";
 		//calling server
 		JSONObject obj = jsonServerCall(urlstub, params);
 		if(obj.get("result").toString().equalsIgnoreCase("1"))
 			retval=true;
 		return retval;
 	}
 	
 	public static JSONObject lookupFriends(String uemail, String password) throws InterruptedException, JSONException{
 		String params = "userEmail="+uemail+"&password="+password;
 		String urlstub = "db_friendof_selectAll.php";
 		return jsonServerCall(urlstub, params); 
 	}
 	
 	public static JSONObject lookupTransLender(String uemail, String password) throws InterruptedException, JSONException{
 		return lookupTrans(uemail,password,"userAsLender");
 	}
 	public static JSONObject lookupTransBorrower(String uemail, String password) throws InterruptedException, JSONException{
 		return lookupTrans(uemail,password,"userAsBorrower");
 	}
 	public static JSONObject lookupTrans(String uemail, String password, String attribute) throws InterruptedException, JSONException{
 		String params = "userEmail="+uemail+"&password="+password+"&lookupWith="+attribute;
 		String urlstub = "db_transaction_lookup.php";
 		return jsonServerCall(urlstub, params); 
 	}
 	
 	public static JSONObject lookupNotifsDate(String uemail, String password) throws InterruptedException, JSONException{
 		return lookupNotifs(uemail, password, "date");
 	}
 	public static JSONObject lookupNotifsEmailSent(String uemail, String password) throws InterruptedException, JSONException{
 		return lookupNotifs(uemail, password, "emailsent");	
 	}
	public static JSONObject lookupNotifsEmailIn(String uemail, String password) throws InterruptedException, JSONException{
 		return lookupNotifs(uemail, password, "emailin");	
 	}
 	public static JSONObject lookupNotifs(String uemail, String password, String attribute) throws InterruptedException, JSONException{
 		String params = "userEmail="+uemail+"&password="+password+"&attribute="+attribute;
 		String urlstub = "db_notif_lookup.php";	
 		return jsonServerCall(urlstub, params);
 	}
 	
 	public static boolean modifyUserEmail(String uemail, String password, String value) throws InterruptedException{
 		return modifyUser(uemail,password,value,"email");
 	}
 	public static boolean modifyUserPassword(String uemail, String password, String value) throws InterruptedException{
 		return modifyUser(uemail,password,value,"password");
 	}
 	public static boolean modifyUserFirstName(String uemail, String password, String value) throws InterruptedException{
 		return modifyUser(uemail,password,value,"fname");
 	}
 	public static boolean modifyUserLastName(String uemail, String password, String value) throws InterruptedException{
 		return modifyUser(uemail,password,value,"lname");
 	}
 	public static boolean modifyUser(String uemail, String password, String value, String attribute) throws InterruptedException{
 		boolean retval = false;
 		String params = "userEmail="+uemail+"&password="+password+"&attribute="+attribute+"&value="+value;
 		String urlstub = "db_account_userModify.php";
 		String status = "fail";
 		//calling server
 		status = simpleServerCall(urlstub, params);
 		if(status.equalsIgnoreCase("success")||status.equalsIgnoreCase("1")||status.equalsIgnoreCase("true")) {
 			retval=true;
 		}
 		return retval;
 	}
 	
 	public static String simpleServerCall(String urlstub, String params) throws InterruptedException{
 		Logger AXNLOG = Logger.getLogger(AccessNet.class .getName());
 		AXNLOG.setLevel(Level.INFO);
 		String url = "http://chase.mamatey.com/PayBack/"+urlstub;
 		//String retval = "default";
 		final String[] items = new String[2];
 		items[0] = "400 Bad Request";
 		items[1] = params;
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		try {
 			final URL addr = new URL(url);
 			new Thread (new Runnable() {
 				@Override
 				public synchronized void run(){
 					//view.invalidate();//uncommenting this will render the button into an app crasher
 					Logger CANLOG = Logger.getLogger(AccessNet.class .getName());
 					CANLOG.setLevel(Level.INFO);
 					HttpURLConnection connection;
 					try {
 						connection = (HttpURLConnection) addr.openConnection();
 						connection.setDoOutput(true);
 						connection.setDoInput(true);
 						connection.setInstanceFollowRedirects(false); 
 						connection.setRequestMethod("POST"); 
 						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
 						connection.setRequestProperty("charset", "utf-8");
 						connection.setRequestProperty("Content-Length", "" + Integer.toString(items[1].getBytes().length));
 						connection.setUseCaches (false);
 						CANLOG.info("Able to set HTTP: "+connection);
 
 						try{
 							DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
 							wr.writeBytes(items[1]);
 							wr.flush();
 							wr.close();
 							CANLOG.info("Data send success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection send to server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 
 						try{
 							String line;
 							BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
 							line = reader.readLine();
 							if(line.equalsIgnoreCase("1")||line.equalsIgnoreCase("true")||line.equalsIgnoreCase("success")||line.equalsIgnoreCase("Login and query success")||line.equalsIgnoreCase("{\"result\":1,\"message\":\"Sign-in successful\"}")||line.equalsIgnoreCase("{\"result\":1,\"message\":\"Contact added successfully\"}"))
 								items[0]="success";
 							CANLOG.info("Data in: "+line);
 							
 							reader.close();
 							CANLOG.info("Data receive success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection receive from server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 
 						connection.disconnect();
 						CANLOG.info("Performed action and server returned: "+items[0]);
 						//Toast.makeText(getApplicationContext(), "Account Created!", Toast.LENGTH_SHORT).show();
 
 					} catch (IOException e) {
 						CANLOG.warning("IOException on HTTP connection creation");
 						e.printStackTrace();
 					}           
 					latch.countDown();
 				}
 			}).start();
 		} catch (MalformedURLException e) {
 			AXNLOG.warning("URL: \""+url+"\" could not be created.");
 			e.printStackTrace();
 		}
 		
 		latch.await();
 		AXNLOG.info("return value is: \""+items[0]+"\"");
 		return items[0];
 	}
 	
 	public static JSONObject jsonServerCall(String urlstub, String params) throws InterruptedException, JSONException{
 		Logger AXNLOG = Logger.getLogger(AccessNet.class .getName());
 		AXNLOG.setLevel(Level.INFO);
 		String url = "http://chase.mamatey.com/PayBack/"+urlstub;
 		
 		final String[] items = new String[2];
 		items[0] = "400 Bad Request";
 		items[1] = params;
 		final JSONObject[] retval = new JSONObject[1];
 		retval[0] = new JSONObject();
 		retval[0].put("ServerResponse", 400);
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		try {
 			final URL addr = new URL(url);
 			new Thread (new Runnable() {
 				@Override
 				public synchronized void run(){
 					Logger CANLOG = Logger.getLogger(AccessNet.class .getName());
 					CANLOG.setLevel(Level.INFO);
 					HttpURLConnection connection;
 					try {
 						connection = (HttpURLConnection) addr.openConnection();
 						connection.setDoOutput(true);
 						connection.setDoInput(true);
 						connection.setInstanceFollowRedirects(false); 
 						connection.setRequestMethod("POST"); 
 						connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
 						connection.setRequestProperty("charset", "utf-8");
 						connection.setRequestProperty("Content-Length", "" + Integer.toString(items[1].getBytes().length));
 						connection.setUseCaches (false);
 						CANLOG.info("Able to set HTTP: "+connection);
 
 						try{
 							DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
 							wr.writeBytes(items[1]);
 							wr.flush();
 							wr.close();
 							CANLOG.info("Data send success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection send to server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 						
 						try{
 							String line;
 							BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 							StringBuilder sb = new StringBuilder();
 						    int cp;
 						    while ((cp = reader.read()) != -1) {
 						      sb.append((char) cp);
 						    }
 						    line = sb.toString();
 						    
 						    try {
 								retval[0] = new JSONObject(line);
 								items[0] = "success data received";
 								CANLOG.info("Data in: "+line);
 							} catch (JSONException e) {
 								CANLOG.warning("JSONException on receive from server: "+connection.getResponseMessage());
 								e.printStackTrace();
 							}
 							/*
 							if(line.equalsIgnoreCase("1"))
 								items[0]="success";
 							CANLOG.info("Data in: "+line);*/
 							
 							reader.close();
 							CANLOG.info("Data receive success!");
 						} catch (IOException e){
 							CANLOG.warning("IOException on HTTP connection receive from server: "+connection.getResponseMessage());
 							e.printStackTrace();
 						}
 
 						connection.disconnect();
 						CANLOG.info("Executed process and server returned: "+items[0]);
 						//Toast.makeText(getApplicationContext(), "Account Created!", Toast.LENGTH_SHORT).show();
 
 					} catch (IOException e) {
 						CANLOG.warning("IOException on HTTP connection creation");
 						e.printStackTrace();
 					}           
 					latch.countDown();
 				}
 			}).start();
 		} catch (MalformedURLException e) {
 			AXNLOG.warning("URL: \""+url+"\" could not be created.");
 			e.printStackTrace();
 		}
 		
 		latch.await();
 		AXNLOG.info("The process was: \""+items[0]+"\"");
 		return retval[0];
 	}
 }
