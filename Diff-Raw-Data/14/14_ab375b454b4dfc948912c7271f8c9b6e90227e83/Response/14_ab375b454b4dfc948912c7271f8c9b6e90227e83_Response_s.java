 package com.server.Responses;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 public class Response {
     Hashtable req;
     OutputStream out;
     String CRLF = "\r\n";
 
     public void writeTo(Hashtable<String, Object> req, OutputStream out) throws IOException {
         this.req = req;
         this.out = out;
 
         writeStatusLine();
         writeHeader();
         writeBody();
     }
 
     private byte[] convertToBytes(String output) {
         return output.getBytes(Charset.forName("utf-8"));
     }
 
     private void writeStatusLine() throws IOException {
         out.write((req.get("status-line") + "").getBytes(Charset.forName("utf-8")));
         out.write(convertToBytes(CRLF));
     }
 
     private void writeHeader() throws IOException {
         Hashtable<String, String> headers = (Hashtable<String, String>) req.get("message-header");
 
         for (String key: headers.keySet()) {
             if(key.equals("Set-Cookie")){
                 printCookies(headers);
             } else {
                 String line = String.format("%s: %s", key, headers.get(key));
                 out.write(convertToBytes(line));
                 out.write(convertToBytes(CRLF));
             }
         }
     }
 
     private void printCookies(Hashtable header) throws IOException {
         for(Object cookie: (ArrayList) header.get("Set-Cookie")) {
            out.write(convertToBytes("Set-Cookie: " + cookie));
         }
     }
 
     private void writeBody() throws IOException {
         out.write(convertToBytes(CRLF));
         out.write((byte[]) req.get("message-body"));
         out.close();
     }
 }
