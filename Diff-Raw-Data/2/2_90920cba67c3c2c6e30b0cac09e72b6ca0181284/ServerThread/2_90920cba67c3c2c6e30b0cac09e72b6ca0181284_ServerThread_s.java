 package edu.cmu.cs214.hw9.server;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 
 import json.JSONObject;
 import json.JSONTokener;
 
 import edu.cmu.cs214.hw9.db.FriendsDAO;
 import edu.cmu.cs214.hw9.db.SubscriptionsDAO;
 import edu.cmu.cs214.hw9.db.User;
 import edu.cmu.cs214.hw9.db.UserDAO;
 
 public class ServerThread extends Thread {
 	private Socket mySocket;
 	private UserDAO u;
 	private FriendsDAO f;
 	private SubscriptionsDAO s;
 	
 	public ServerThread(Socket mySocket, UserDAO u,
 					FriendsDAO f, SubscriptionsDAO s) throws Exception {
 		if (s == null) {
 			throw new NullPointerException();
 		}
 		this.mySocket = mySocket;
 		this.mySocket.setSoTimeout(600000);
 		this.u = u;
 		this.f = f;
 		this.s = s;
 	}
 	
 	public void run(){
 		try{
 			PrintWriter out = new PrintWriter(mySocket.getOutputStream(),
 					true);
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					mySocket.getInputStream()));
 			while(true){//keep accepting messages until END command is given.
 				String msg = in.readLine();
 				JSONObject o;
 				
 				//on null input, do not do anything
 				if(msg == null) {
 					out.println("exit");
 				}
 				
 				/***********************************/
 				/***********************************/
 				/***** LOGIN AND REGISTRATION ******/
 				/***********************************/
 				/***********************************/
 				//LOGIN
 				else if(msg.indexOf("LOGIN") == 0){
 					o = new JSONObject(new JSONTokener(msg.substring(6)));
 					boolean t = u.authenticateUser(o.getString("email"), o.getString("password"));
 					if(t) {
 						out.println("LOGIN SUCCESSFUL");
 					}
 					
 					else {
 						out.println("LOGIN FAILED");
 					}
 				}
 				//REGISTER
 				else if(msg.indexOf("REGISTER") == 0){
 					o = new JSONObject(new JSONTokener(msg.substring(9)));
 					boolean t = u.createUser(o.getString("email"), o.getString("password"), o.getString("name"));
 					if(t) {
 						out.println("REGISTRATION SUCCESSFUL");
 					}
 					
 					else {
 						out.println("REGISTRATION FAILED");
 					}
 				}
 				
 				/***********************************/
 				/***********************************/
 				/********* FRIEND ACTIONS **********/
 				/***********************************/
 				/***********************************/
 				else if(msg.indexOf("MODIFYFRIEND") == 0){
					o = new JSONObject(new JSONTokener(msg.substring(10)));
 					boolean t = f.modifyFriend(o.getString("emailModifying"), o.getString("emailModified"));
 					if(t) {
 						out.println("MODIFY SUCCESSFUL");
 					}
 					
 					else {
 						out.println("MODIFY FAILED");
 					}
 				}
 				
 				else if(msg.indexOf("LISTFRIENDS") == 0){
 					String list = f.listFriends(msg.substring(12));
 					out.println(list);
 				}
 				
 				/***********************************/
 				/***********************************/
 				/****** SUBSCRIPTION ACTIONS *******/
 				/***********************************/
 				/***********************************/
 				else if(msg.indexOf("MODIFYSUBSCRIPTION") == 0){
 					o = new JSONObject(new JSONTokener(msg.substring(19)));
 					boolean t = s.modifySubscription(o.getString("emailSubscriber"), o.getString("emailSubscribed"));
 
 					if(t) {
 						out.println("MODIFY SUCCESSFUL");
 					}
 					
 					else {
 						out.println("MODIFY FAILED");
 					}
 				}
 				
 				else if(msg.indexOf("LISTSUBSCRIPTIONS") == 0){
 					String list = s.listSubscriptions(msg.substring(18));
 					out.println(list);
 				}
 				
 				
 				/***********************************/
 				/***********************************/
 				/**** MAKING AND VIEWING POSTS *****/
 				/***********************************/
 				/***********************************/
 				
 				/***********************************/
 				/***********************************/
 				/********** USER ACTIONS ***********/
 				/***********************************/
 				/***********************************/				
 				//retrieve username given e-mail address
 				else if(msg.indexOf("GETUSERNAME") == 0){
 					User thisUser = u.findUser(msg.substring(12));
 					if (thisUser == null){
 						out.println("");
 					}
 					else{
 						out.println(thisUser.getName());
 					}
 				}
 				
 				else if(msg.indexOf("END") == 0){//Break out of the loop, no longer accept messages
 					out.println("closing connection");
 					break;
 				}
 				else{
 					out.println("Unrecognized command");
 				}
 			}
 			out.close();
 			in.close();
 			mySocket.close();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 
 	}
 }
