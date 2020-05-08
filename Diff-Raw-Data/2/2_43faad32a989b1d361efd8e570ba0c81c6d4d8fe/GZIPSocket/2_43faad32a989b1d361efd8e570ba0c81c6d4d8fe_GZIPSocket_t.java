 package com.github.rmifs.rmi.sock;
 
 import java.io.*;
 import java.net.*;
 import java.util.zip.*;
 
 public class GZIPSocket extends Socket
 {
     private InputStream in = null;
     private OutputStream out = null;
 
     public GZIPSocket() throws IOException
     {
         super();
     }
 
     public GZIPSocket(String host, int port) throws IOException
     {
         super(host, port);
     }
 
     public synchronized InputStream getInputStream() throws IOException
     {
         if (in == null) {
         	in = new GZIPInputStream(super.getInputStream());
         }
         return in;
     }
 
     public synchronized OutputStream getOutputStream() throws IOException
     {
         if (out == null) {
        	out = new GZIPOutputStream(super.getOutputStream(), true);
         }
         return out;
     }
 }
