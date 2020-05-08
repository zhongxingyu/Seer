 package reporting;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 /*
  * Created on Oct 5, 2005
  * 
  * $Id: PostTest.java Feb 8, 2004 izpack-frontend
  * Copyright (C) 2001-2003 IzPack Development Group
  * 
  * File : PostTest.java 
  * Description : TODO Add description
  * Author's email : gumbo@users.berlios.de
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 public class ErrorSubmitter
 {
     public static void sendErrorReport(Throwable e, String message)
     {
         try
         {
             HttpURLConnection con = (HttpURLConnection) new URL("http://izpack.berlios.de/process.php").openConnection();
             
             con.setDoOutput(true);
             
             con.setRequestMethod("POST");        
             
             OutputStream os = con.getOutputStream();
             
             
             StringBuffer properties = new StringBuffer();            
             
             Properties props = System.getProperties();
             Set<Object> keys = props.keySet();
             
             os.write("exceptionList=".getBytes());            
                        
             Throwable t = e;
             writeException(t, os);
             
             //Write out any linked exceptions too
             while ( (t = t.getCause()) != null)
                 writeException(t, os);
             
             os.write("&threadDump=".getBytes());
             
             Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
             
             Set<Thread> threads = stackTraces.keySet();
             
             for (Thread thread : threads)
             {
                 os.write( ("Thread: " + thread.getName() + "\r\n" ).getBytes() );
                 StackTraceElement[] stackTrace = stackTraces.get(thread);
                 
                 for (int i = 0; i < stackTrace.length; i++)
                 {
                     os.write( ("\t" + stackTrace[i] + "\r\n").getBytes());
                 }
             }
             
             for (Object object : keys)
             {
                 properties.append(object + " = " + props.getProperty((String) object) + "\r\n");
             }                   
             
             os.write("&properties=".getBytes());
             os.write(properties.toString().getBytes());
             
             os.write("&message=".getBytes());
             os.write(message.getBytes());
             
             if (con.getResponseCode() == 200)
                 System.err.println("Submit succeeded");
             
             con.disconnect();
         }
         catch (Exception e1)
         {
             //Do nothing, we're doing this because of an error :)
            //Not really critical anyway
            System.err.println("Error filing report: " + e1.getMessage());
         }
     }
 
     private static void writeException(Throwable e, OutputStream os) throws IOException
     {
         StackTraceElement[] trace = e.getStackTrace();
         
         os.write(( e.toString() + "\r\n" ).getBytes());
         
         for (int i = 0; i < trace.length; i++)
         {
             os.write( ("\t" + trace[i] + "\r\n").getBytes());
         }
     }
 
 }
