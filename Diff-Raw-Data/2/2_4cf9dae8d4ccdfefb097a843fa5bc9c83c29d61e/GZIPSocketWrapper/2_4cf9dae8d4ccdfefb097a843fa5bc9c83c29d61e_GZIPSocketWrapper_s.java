 package com.github.rmifs.rmi.sock;
 
 import java.io.*;
 import java.net.*;
 import java.util.zip.*;
 
 public class GZIPSocketWrapper extends Socket
 {
 	private Socket sock = null;
     private InputStream in = null;
     private OutputStream out = null;
 
     public GZIPSocketWrapper(Socket sock) throws IOException
     {
         this.sock = sock;
     }
 
     public synchronized InputStream getInputStream() throws IOException
     {
         if (in == null) {
             in = new GZIPInputStream(sock.getInputStream());
         }
         return in;
     }
 
     public synchronized OutputStream getOutputStream() throws IOException
     {
         if (out == null) {
            out = new GZIPOutputStream(sock.getOutputStream());
         }
         return out;
     }
 }
