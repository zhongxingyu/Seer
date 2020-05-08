 package com.finfrock.moneycheck.connection;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 public class HttpRetiever
 {
    private String baseUrl = "http://yoururl.com";
     
     public String sendPostMessage(String urlPath, HttpPart[] httpParts)
             throws IOException
     {
         URL siteUrl = new URL(baseUrl + urlPath);
         HttpURLConnection conn = (HttpURLConnection) siteUrl.openConnection();
 
         // conn.setConnectTimeout(5000);
         conn.setRequestMethod("POST");
         conn.setDoOutput(true);
         conn.setDoInput(true);
 
         DataOutputStream out = new DataOutputStream(conn.getOutputStream());
 
         try
         {
             String content = "";
             for (HttpPart httpPart : httpParts)
             {
                 if (content.length() != 0)
                 {
                     content += "&";
                 }
                 content += httpPart.getName() + "="
                         + URLEncoder.encode(httpPart.getValue(), "UTF-8");
             }
             out.writeBytes(content);
             out.flush();
         } finally
         {
             out.close();
         }
 
         InputStream inputStream = conn.getInputStream();
         try
         {
             return getStringResult(inputStream);
         } finally
         {
             inputStream.close();
         }
     }
 
     public String sendGetMessage(String urlPath)
     {
         try
         {
             HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl + urlPath)
                     .openConnection();
             // connection.setConnectTimeout(5000)
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     connection.getInputStream()));
             try
             {
                 String data = "";
                 String line = reader.readLine();
                 while (line != null)
                 {
                     data += line;
                     line = reader.readLine();
                 }
                 return data;
             } finally
             {
                 reader.close();
             }
         } catch (MalformedURLException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return "";
     }
 
     private String getStringResult(InputStream incomingStream)
             throws IOException
     {
         int bufferSize = 128000;
         byte[] buffer = new byte[bufferSize];
 
         byte byteRead = 0;
         int numOfBytesRead = 0;
 
         byteRead = (byte) incomingStream.read();
         while (byteRead != -1)
         {
 
             if (numOfBytesRead >= buffer.length)
             {
 
                 byte[] newBuffer = new byte[buffer.length + bufferSize];
 
                 // copy elements from the old "buffer" to the new "newBuffer"
                 System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
 
                 buffer = newBuffer;
             }
 
             buffer[numOfBytesRead] = byteRead;
 
             numOfBytesRead += 1;
 
             byteRead = (byte) incomingStream.read();
         }
 
         byte[] incomingResult = new byte[numOfBytesRead];
         System.arraycopy(buffer, 0, incomingResult, 0, numOfBytesRead);
 
         return new String(incomingResult);
     }
 }
