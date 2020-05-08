 package com.nexus.webserver.handlers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import com.nexus.webserver.IWebServerHandler;
 import com.nexus.webserver.WebServerRequest;
 import com.nexus.webserver.WebServerResponse;
 import com.nexus.webserver.WebServerStatus;
 
 public class WebServerHandlerHtml implements IWebServerHandler{
 	
 	private static final SimpleDateFormat DateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
 	
 	@Override
 	public void Handle(WebServerRequest Request, WebServerResponse Response){
 		try{
 			String Path = System.getProperty("nexus.webserver.htdocslocation", "htdocs/") + Request.Path;
 			
 			if(Path.endsWith("/")){
 				Path += "index.html";
 			}
 
 			File f = new File(Path);
 			String LastFileModification = GetFileLastModifiedString(f);
 			
 			if(Request.Headers.containsKey("If-Modified-Since")){
 				try{
 					Date RemoteVersion = DateFormat.parse(Request.Headers.get("If-Modified-Since"));
 					if(RemoteVersion.equals(new Date(f.lastModified()))){
						Response.SendHeaders(WebServerStatus.NotModified);
 						Response.Close();
 						return;
 					}
 				}catch(Exception e){}
 			}
 			
 			FileInputStream fs = new FileInputStream(f);
 			FileChannel file = fs.getChannel();
 			ByteBuffer buffer = ByteBuffer.allocate((int) file.size());
 			
 			file.read(buffer);
 			buffer.rewind();
 			
 			String type = "";
 			if(Path.endsWith(".html")) type = "text/html";
 			if(Path.endsWith(".css")) type = "text/css";
 			if(Path.endsWith(".js")) type = "text/javascript";
 			if(Path.endsWith(".png")) type = "image/png";
 			
 			Response.SetHeader("Content-Type", type);
 			Response.SetHeader("Content-Length", Long.toString(file.size()));
 			Response.SetHeader("Last-Modified", LastFileModification);
 			
 			Response.ForceSendHeaders(WebServerStatus.OK);
 			
 			Response.GetSession().Channel.write(buffer);
 			
 			Response.Close();
 			buffer.clear();
 			fs.close();
 			file.close();
 		}catch(FileNotFoundException e){
 			Response.SendHeaders(WebServerStatus.NotFound);
 			Response.SendError("Not found");
 		}catch(IOException e){
 			System.out.println("IOException in WebServerHandlerHTML");
 			e.printStackTrace();
 			Response.SendHeaders(WebServerStatus.InternalServerError);
 			Response.SendError("Internal server error");
 		}
 		Response.Close();
 	}
 	
 	private static String GetFileLastModifiedString(File f){
 		return DateFormat.format(new Date(f.lastModified()));
 	}
 	
 	public static String getHtdocsLocation(){
 		return System.getProperty("nexus.webserver.htdocslocation", "htdocs/");
 	}
 }
