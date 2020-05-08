 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rightscale.util;
 
 import java.io.*;
 import java.util.*;
 
 /**
  *
  * @author tony
  */
 public class FileUtils {
     public static String readText(File location)
             throws IOException
     {
         String newline = System.getProperty("line.separator");
         
         FileInputStream fis = new FileInputStream(location);
         BufferedReader  br = new BufferedReader(new InputStreamReader(fis));
 
         StringBuffer contents = new StringBuffer();
         String       line     = null;
 
         do {
             line = br.readLine();
             if(line != null) {
                 contents.append(line);
                 contents.append(newline);
             }
         } while(line != null);
 
         return contents.toString();
     }
 
     public static void writeText(String contents, File location)
             throws IOException
     {
         FileOutputStream fos = new FileOutputStream(location);
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
         bw.write(contents);
         bw.close();
     }
 
     public static void writeResource(Class klass, String resName, File location)
             throws IOException
     {
         FileOutputStream fos = new FileOutputStream(location);
         BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
 
         InputStream in = klass.getResourceAsStream(resName);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
         String crlf = System.getProperty("line.separator");
         String line = null;
 
         do {
             line = br.readLine();
             if(line != null) {
                 bw.write(line); bw.write(crlf);
             }
         } while(line != null);
 
         br.close();
         bw.close();
     }
     
     public static void scheduleDelete(File f, int timeout)
             throws IOException
     {
         new FileDeleter(f, timeout);
     }
 }
 
 class FileDeleter implements Runnable {
     public static Map _deleters = new HashMap();
 
     Thread    _thread = null;
     File      _file   = null;
     int       _delay  = 0;
     boolean   _abort  = false;
 
     public FileDeleter(File file, int delay)
         throws IOException
     {
         String cp = file.getCanonicalPath();
 
         synchronized(_deleters) {
             FileDeleter fd = (FileDeleter)_deleters.get(cp);
             if(fd != null) {
                 fd.abort();
             }
         }
 
         _file = file;
         _delay = delay;
         _thread = new Thread(this);
         _thread.start();
 
         synchronized(_deleters) {
             _deleters.put(cp, this);
         }
     }
 
     public void run() {
         try {
             Thread.currentThread().sleep(_delay * 1000);
         }
         catch (InterruptedException e) {}
 
         if(!_abort) {
             _file.delete();
             System.err.println("Deleted " + _file.getName());
         }
     }
 
     protected void abort() {
         _abort = true;
         _thread.interrupt();
     }
 }
