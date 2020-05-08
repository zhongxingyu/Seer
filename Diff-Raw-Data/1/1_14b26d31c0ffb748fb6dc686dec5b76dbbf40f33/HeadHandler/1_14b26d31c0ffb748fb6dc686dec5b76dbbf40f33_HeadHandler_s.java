 package com.janx57.aweb.server.handler;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import com.google.common.io.ByteStreams;
 import com.janx57.aweb.server.Server;
 import com.janx57.aweb.server.config.ServerPaths;
 import com.janx57.aweb.server.http.HttpRequest;
 import com.janx57.aweb.server.http.HttpResponse;
 import com.janx57.aweb.server.http.Status;
 
 public class HeadHandler implements Handler {
   ServerPaths paths;
   byte[] body;
 
   public HeadHandler(ServerPaths paths) {
     this.paths = paths;
   }
 
   @Override
   public void handle(HttpRequest request, HttpResponse response) {
     Status status = null;
     try {
       body = getContent(request.getUri());
       status = Status.OK;
     } catch (IOException e) {
       body = new byte[] {};
       status = Status.NOT_FOUND;
     }
 
     response.setReasonPhrase(status.phrase);
     response.setStatusCode(status.code);
     response.setVersion(Server.protocolVersion);
     response.setHeader("Date", getServerTime());
     response.setHeader("Content-Type", "text/html");
     response.setHeader("Content-Length", body.length + "");
   }
 
   private byte[] getContent(String path) throws IOException {
     FileInputStream fis = new FileInputStream(new File(paths.getWwwDir(), path));
     try {
       return ByteStreams.toByteArray(fis);
     } finally {
       fis.close();
     }
   }
 
   private String getServerTime() {
     Calendar calendar = Calendar.getInstance();
     SimpleDateFormat rfc1123Format =
         new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
     rfc1123Format.setTimeZone(TimeZone.getTimeZone("GMT"));
     return rfc1123Format.format(calendar.getTime());
   }
 }
