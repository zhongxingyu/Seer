 package com.nexus.webserver;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import com.nexus.NexusServer;
 
 public class WebServerHandlerHtml extends WebServerHandler {
 	
 	public WebServerHandlerHtml(WebServerRequest request, WebServerSession session) {
 		super(request, session);
 	}
 
 	public void handle() {
 		response = new WebServerResponse();
 
 		try {
 			String path;
 			if(NexusServer.Instance.IsDebug){
 				path = "../nexus-webclient/" + request.path();
 			}else{
 				path = "htdocs/" + request.path();
 			}
 			if (path.endsWith("/"))
 				path += "index.html";
 
			FileInputStream fs = new FileInputStream(path);
			
			FileChannel file = fs.getChannel();
 
 			ByteBuffer buffer = ByteBuffer.allocate((int) file.size());
 			
 			file.read(buffer);
 
 			buffer.rewind();
 
 			String type = "";
 			if(path.endsWith(".html")) type = "text/html";
 			if(path.endsWith(".css")) type = "text/css";
 			if(path.endsWith(".js")) type = "text/javascript";
 			if(path.endsWith(".png")) type = "image/png";
 			
 			this.response.setHeader("Content-Type", type);
 			this.response.setHeader("Content-Length", Long.toString(file.size()));
 
 			this.sendHeaders(WebServerStatus.OK);
 
 			this.Session.Channel.write(buffer);
			
 			buffer.clear();
			fs.close();
			file.close();
 		} catch (FileNotFoundException e) {
 			this.sendHeaders(WebServerStatus.NotFound);
 		} catch (IOException e) {
 			this.sendHeaders(WebServerStatus.InternalServerError);
 		}
 
 		this.close();
 	}
 }
