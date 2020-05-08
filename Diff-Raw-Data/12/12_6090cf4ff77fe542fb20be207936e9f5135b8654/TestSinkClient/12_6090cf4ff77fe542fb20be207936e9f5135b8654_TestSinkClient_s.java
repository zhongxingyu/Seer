 package edu.monash.io.socketio.test;
 
 import edu.monash.io.socketio.connection.*;
 import edu.monash.io.socketio.exceptions.*;
 import com.google.gson.*;
 import java.io.*;
 
 public class TestSinkClient{
 	//sink
 	final SinkConnection sink;
 
 	public TestSinkClient(){
 		System.out.println("TEtSinkClient constructor");
 		//default one
 		sink = new SinkConnection();
 		JsonObject authInfo = new JsonObject();
		authInfo.addProperty(ConnectionConsts.CONNECTION_C_USERNAME, "asource");
 		authInfo.addProperty(ConnectionConsts.CONNECTION_C_PASSWORD , "asink");
		authInfo.addProperty("appname" , this.getClass().getName());
 		authInfo.addProperty("comment" , "a test for sink client");
 		sink.setAuthInfo(authInfo);
 		try{
 			sink.addListener(new SinkListener(){
 				public void onConnectionEstablished(){
 					System.out.println("TestSinkClient:: socket established");
 				}
 				public void onDisconnect(){
 					System.out.println("TestSinkClient:: disconnect");
 				}
 				public void onAuthResponse(JsonObject authResponse){
 					System.out.println("TestSinkClient:: auth response:" + authResponse.toString());
 					System.out.println("TestSinkClient:: new source connected:" + sink.getSourceList().toString());
 				}
 				public void onMessage(JsonObject aMessage) throws InvalidMessageException{
 					System.out.println("TestSinkClient:: new message:" + aMessage.toString());
 				}	
 				public void onSourceDisconnect(){
 					System.out.println("TestSinkClient:: a source disconnected: is still connected:" + sink.isConnected());
 				}
 				public void onSourceConnect(JsonObject sourceList){
 					System.out.println("TestSinkClient:: new source connected:" + sourceList.toString());
 				}
 				public void onPermissionChanged(String newPermission){
 					System.out.println("TestSinkClient:: new permission:" + newPermission);
 				}
 				public void onLinkEstablished(String allowedOperations){
 					System.out.println("TestSinkClient:: link established:" + allowedOperations);
 				}
 			});
 			//connect
 			sink.connect();
 
 			System.out.println("-------------");
 
 			while(!sink.authcated()){
 			try{
 				Thread.sleep(600);
 			}
 			catch(Exception e){}}	
 
 			if(!sink.doneSelection()){
 				try {
 			    	String source = null;
 			    	System.out.print("source: ");
 			    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 					source = br.readLine();
 					//sink.getSocket().emit(ConnectionConsts.CLIENT_C_SELECT, source);
 					sink.selectSource(source);
 				} catch (IOException ioe) {
 			        System.out.println("IO error trying to read your name!");
 			        System.exit(1);
 			    }
 			    catch(UnauthcatedClientException e){
     				System.out.println(e.getMessage());
 			        System.exit(1);
 			    
 			    }
 			    catch(InvalidSourceException e){
     				System.out.println(e.getMessage());
 			        System.exit(1);
 			    }
 			}	
 
 			//now read message
 			try {
 		    	String msg = "";
 				while(!msg.equals("q")){
 					System.out.print("Enter smth to send to source: ");
 		    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 					msg = br.readLine();
 					JsonObject jObject = new JsonObject();
 					jObject.addProperty("messagetype", ConnectionConsts.CLIENT_C_MESSAGE);
 					jObject.addProperty(ConnectionConsts.CLIENT_C_MESSAGE , msg);		
 					sink.send(jObject);	
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
 			System.out.println("There is smth wrong:" + e.getMessage());
 		}
 	}
 
 
 	//main
 	public static void main(String[] args){
 		TestSinkClient sinkClient = new TestSinkClient();
 		System.out.println("creat sinkclient");
 	}
 }
