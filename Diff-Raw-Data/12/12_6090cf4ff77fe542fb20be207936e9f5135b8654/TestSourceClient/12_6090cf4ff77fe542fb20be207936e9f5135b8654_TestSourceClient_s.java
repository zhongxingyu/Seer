 package edu.monash.io.socketio.test;
 
 import edu.monash.io.socketio.connection.*;
 import edu.monash.io.socketio.exceptions.*;
 import com.google.gson.*;
 import java.io.*;
 
 public class TestSourceClient{
 	//sink
 	SourceConnection source;
 
 	public TestSourceClient(){
 		//default one
 		source = new SourceConnection();
 		//add auth
 		JsonObject authInfo = new JsonObject();
 		authInfo.addProperty(ConnectionConsts.CONNECTION_C_USERNAME, "asource");
 		authInfo.addProperty(ConnectionConsts.CONNECTION_C_PASSWORD , "asource");
		authInfo.addProperty("appname" , this.getClass().getName());
 		authInfo.addProperty("comment" , "a test for source client");
 		source.setAuthInfo(authInfo);
 		try{
 			source.addListener(new SourceListener(){
 				public void onConnectionEstablished(){
 					System.out.println("TestSourceClient:: socket established");
 				}
 				public void onDisconnect(){
 					System.out.println("TestSourceClient:: disconnect");
 				}
 				public void onAuthResponse(JsonObject authResponse){
 					System.out.println("TestSourceClient:: auth response:" + authResponse.toString());
 				}
 				public void onMessage(JsonObject aMessage) throws InvalidMessageException{
 					System.out.println("TestSourceClient:: new message: " + aMessage.toString());
 				}	
 				public void onSinkDisconnect(){
 					System.out.println("TestSourceClient:: sink disconnect, no more sink");	
 				}
 				public void onSinkConnect(JsonArray sinkList){
 					System.out.println("TestSourceClient:: new sink connect, sinklist:" + sinkList.toString());	
 				}
 			});
 			//connect
 			source.connect();
 			while(!source.authcated()){
 				try{
 				Thread.sleep(600);
 			}
 			catch(Exception e){}}	
 			//now read message
 			try {
 		    	String msg = "";
 				while(!msg.equals("q")){
 					System.out.print("Enter smth to send to sink: ");
 		    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 					msg = br.readLine();
 					JsonObject jObject = new JsonObject();
 					jObject.addProperty("messagetype", ConnectionConsts.CLIENT_C_MESSAGE);
 					jObject.addProperty(ConnectionConsts.CLIENT_C_MESSAGE , msg);		
 					source.send(jObject);	
 				}	
 		    } 
 		    catch(UnauthcatedClientException e){
 				System.out.println("UnauthcatedClientException:" + e.getMessage());		    	
 		    }
 		    catch (IOException ioe) {
 		        System.out.println("IO error trying to read your name!");
 		        System.exit(1);
 		    }
 
 		}
 		catch(ConnectionFailException e){
 
 		}	
 
 	}
 
 
 
 	//main
 	public static void main(String[] args){
 		TestSourceClient sClient = new TestSourceClient();
 		System.out.println("create source client");
 	}
 }
