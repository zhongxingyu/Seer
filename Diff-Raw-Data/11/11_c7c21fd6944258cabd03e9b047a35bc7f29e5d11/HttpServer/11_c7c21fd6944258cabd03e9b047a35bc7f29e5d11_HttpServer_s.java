 package org.smartflow.server;
 
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URLDecoder;
 import java.net.UnknownHostException;
 import java.util.StringTokenizer;
 
 import org.smartflow.MessageHandler;
 import org.smartflow.MessageSender;
 import org.smartflow.Resources;
 import org.smartflow.Settings;
 import org.smartflow.WorkflowEngine;
 
 public class HttpServer implements MessageSender,Runnable{
 
 private boolean serverIsRunning = true;
 private Socket clientSocket;
 private ServerSocket serverSocket;
 private BufferedReader socketReader;
 private DataOutputStream outputStream;
 private String buttonLayout = Resources.HTML_WORKFLOW_BUTTONS_START + Resources.HTML_START_BUTTON + Resources.HTML_WORKFLOW_BUTTONS_END;
 private Thread serverThread;
 private String previousMessage;
 
 
 public HttpServer() {
 	
 	this.serverThread = new Thread(this);
 	MessageHandler.getInstance().registerSender(this);
 	this.initialize();
 	serverThread.start();
 }
 
 public void initialize() {
 	
 	try {
 		this.serverSocket = new ServerSocket (Settings.SERVER_PORT, 10, InetAddress.getByName(Settings.LOCAL_HOST));
 		
 	} catch (UnknownHostException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 
 	
 }
 
 public boolean isServerRunning() {
 	return this.isServerRunning();
 }
 
 private void registerClient(Socket client) {
 	this.clientSocket = client;
 	System.out.println( "The Client "+ clientSocket.getInetAddress() + ":" + clientSocket.getPort() + " is connected");
 }
 
 public Socket getClient() {
 	return this.clientSocket;
 }
 
 public void run() {
 
 	while(serverIsRunning) {
 		
 		try {
 			
 			registerClient(serverSocket.accept());
 			this.socketReader = new BufferedReader(new InputStreamReader (clientSocket.getInputStream()));
 			this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
 			
 			
 			String clientRequest = socketReader.readLine();
 
 			StringTokenizer tokenizer = new StringTokenizer(clientRequest);
 			String httpMethod = tokenizer.nextToken();
 			String httpQueryString = tokenizer.nextToken();
 
 			StringBuffer serverResponse = new StringBuffer();
 			serverResponse.append(Resources.RESPONSE_WELCOME_MESSAGE);
 			serverResponse.append(Resources.RESPONSE_CLIENT_REQUEST_MESSAGE);
 			System.out.println(clientRequest);
 
 			//System.out.println("The HTTP request string is ....");
 			
 //			while (socketReader.ready())	{ //is true while buffer is not empty
 //
 //				// Read the HTTP complete HTTP Query
 //				serverResponse.append(clientRequest + "<BR>");
 //				
 //				clientRequest = socketReader.readLine();
 //				System.out.println(clientRequest);
 //	
 //			}
 			
 			if (httpMethod.equals("GET")) {
 				
 				if (httpQueryString.equals("/")) {
 					// The default home page
 					sendResponse(200, serverResponse.toString(), false);
 					
 					
 				} else {
 					//This is interpreted as a file name
 					String fileName = httpQueryString.replaceFirst("/", "");
 					fileName = URLDecoder.decode(fileName);
 
 					if (new File(fileName).isFile()){ //this is relative path
 						sendResponse(200, fileName, true);
 						
 					}
 					else {
 						sendResponse(404, Resources.ERROR_404_MESSAGE, false);
 					}
 				}
 				
 			} else if (httpMethod.equals("POST")) { 
 				
 				String action = httpQueryString.replaceFirst("/", "");
 				 
 				if(action.equals("PREVIOUS")) {
 					MessageHandler.getInstance().messageReceived("Previous");
 				}if(action.equals("NEXT")) {
 					MessageHandler.getInstance().messageReceived("Next");
 				}if(action.equals("START")) {
 					this.buttonLayout = Resources.HTML_WORKFLOW_BUTTONS_START + Resources.HTML_STOP_BUTTON + Resources.HTML_WORKFLOW_BUTTONS_END;
					MessageHandler.getInstance().messageReceived("Next");
 				}if(action.equals("STOP")) {
 					this.buttonLayout = Resources.HTML_WORKFLOW_BUTTONS_START + Resources.HTML_START_BUTTON + Resources.HTML_WORKFLOW_BUTTONS_END;
 					this.sendMessage(this.previousMessage);
 				}
 				
 				
 			} else {
 				System.out.println(httpMethod.toString());
 			}
 			
 			//this.clientSocket.close();
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 }
 
 public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {
 
 	String statusLine = null;
 	String serverdetails = Settings.SERVER_DETAILS;
 	String contentLengthLine = null;
 	String fileName = null;
 	String contentTypeLine = "Content-Type: text/html" + "\r\n";
 	FileInputStream fin = null;
 
 	if (statusCode == 200)
 		statusLine = "HTTP/1.1 200 OK" + "\r\n";
 	else
 		statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
 
 	if (isFile) {
 		fileName = responseString;
 		fin = new FileInputStream(fileName);
 		contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
 		if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
 			contentTypeLine = "Content-Type: \r\n";
 	} else {
 		responseString = Resources.HTML_START + responseString  + this.buttonLayout + Resources.HTML_END;
 		contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
 	}
 
 	outputStream.writeBytes(statusLine);
 	outputStream.writeBytes(serverdetails);
 	outputStream.writeBytes(contentTypeLine);
 	outputStream.writeBytes(contentLengthLine);
 	outputStream.writeBytes("Connection: close\r\n");
 	outputStream.writeBytes("\r\n");
 
 	if (isFile) {
 		sendFile(fin, outputStream);
 	}
 	else {
 		outputStream.writeBytes(responseString);
 	}
 	
 	//outputStream.close();
 }
 
 public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
 	
 	byte[] buffer = new byte[1024] ;
 	int bytesRead;
 
 	while ((bytesRead = fin.read(buffer)) != -1 ) {
 		out.write(buffer, 0, bytesRead);
 	}
 
 	//fin.close();
 }
 
 
 
 @Override
 public void sendMessage(String msg) {
 	try {
 		this.previousMessage = msg;
 		sendResponse(200, msg, false);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 
 	
 }
 
 
 
 
 }
 
