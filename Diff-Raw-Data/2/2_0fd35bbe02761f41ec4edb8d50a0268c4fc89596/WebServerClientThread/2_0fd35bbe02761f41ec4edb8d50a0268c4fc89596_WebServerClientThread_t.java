 package com.nexus.webserver;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class WebServerClientThread implements Runnable{
 
 	private final WebServerSession Session;
 	private final Thread ClientThread;
 	
 	private WebServerClientThread(WebServerSession Session){
 		this.Session = Session;
 		this.ClientThread = new Thread(this);
 		this.ClientThread.setName("WebServerClientThread");
 		this.ClientThread.setDaemon(true);
 		this.ClientThread.start();
 	}
 	
 	public static void LaunchNewThread(WebServerSession Session){
 		new WebServerClientThread(Session);
 	}
 
 	@Override
 	public void run(){
 		try{
 			String ClientIP = Session.Channel.socket().getInetAddress().toString().split("/")[1];
 		    if(ClientIP.contains("0:0:0:0:0:0:1")){ClientIP = "127.0.0.1";}
 		    
 		    String HTTPHeader = Session.readLine();
 		    String HTTPHeaderData[] = HTTPHeader.split(" ");
 		    if(HTTPHeaderData.length != 3){
 		    	return;
 		    }
 		    
 		    WebServerMethod HTTPMethod = WebServerMethod.valueOf(HTTPHeaderData[0]);
 
 		    String Path = HTTPHeaderData[1].split("\\?")[0];
 
 		    Map<String, String> Parameters = new HashMap<String, String>();
 		    String ParameterArray[] = {};
 		    try{
 		    	ParameterArray = HTTPHeaderData[1].split("\\?")[1].split("&");
 		    }catch(Exception e){}
 
 		    for(String Parameter : ParameterArray){
 		    	String Value = "";
 		    	try{
 		    		Value = Parameter.split("=")[1];
 		    	}catch(Exception e){}
 		    	Parameters.put(Parameter.split("=")[0], Value);
 		    }
 
 		    Map<String, String> Headers = new HashMap<String, String>();
 		    String ReadingLine = "";
 		    String RequestData = "";
 		    boolean ReadingHeaders = true;
 		    while((ReadingLine = Session.readLine()) != null){
 		    	if(ReadingLine.equals("")) ReadingHeaders = false;
 		    	if(ReadingHeaders){
		    		Headers.put(ReadingLine.split(":")[0].trim(), ReadingLine.split(":", 2)[1].trim());
 		    	}else{
 		    		RequestData += ReadingLine + "/";
 		    	}
 		    }
 		    
 		    if(Headers.containsKey("Accept-Encoding")){
 		    	if(Headers.get("Accept-Encoding").toLowerCase().contains("gzip")){
 		    		Session.SetSupportsGZIP(true);
 		    	}
 		    }
 		    
 		    WebServerRequest Request = new WebServerRequest();
 		    Request.HTTPMethod = HTTPMethod;
 		    Request.Path = Path;
 		    Request.Data = RequestData;
 		    Request.Parameters = Parameters;
 		    Request.Headers = Headers;
 		    Request.Address = ClientIP;
 		    
 		    WebServerHandlerFactory.HandleRequest(Request, Session);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 }
