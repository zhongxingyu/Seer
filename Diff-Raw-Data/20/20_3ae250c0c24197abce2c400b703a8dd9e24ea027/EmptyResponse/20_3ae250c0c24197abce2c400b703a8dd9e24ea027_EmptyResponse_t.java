 
 package me.heldplayer.web.server.internal;
 
 import java.io.IOException;
 
 import me.heldplayer.web.server.RequestSource;
 
 public class EmptyResponse extends WebResponse {
 
     public EmptyResponse() throws IOException {
         super();
     }
 
     @Override
     public WebResponse writeResponse(RequestSource source) throws IOException {
         this.header.writeBytes("HTTP/1.0 200 OK\r\n");
         this.header.writeBytes("Connection: close\r\n");
        this.header.writeBytes("Server: HeldBot\r\n");
         this.header.writeBytes("Content-Type: text/plain\r\n");
 
         return this;
     }
 
 }
