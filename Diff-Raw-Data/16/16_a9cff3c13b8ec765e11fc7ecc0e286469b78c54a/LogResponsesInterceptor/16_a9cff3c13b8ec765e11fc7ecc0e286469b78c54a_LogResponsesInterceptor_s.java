 /*
  * $Id$
  * -------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.ibeans.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.ibeans.api.channel.MimeType;
 
 import org.ibeans.api.AbstractCallInterceptor;
 import org.ibeans.api.InvocationContext;
 import org.ibeans.api.Response;
 import org.ibeans.api.channel.MimeTypes;
 
 /**
  * Logs responses from iBean invocations to a directory.  Files are stored using the pattern -
  * [Test class Name]-[Test method name]-response[count].[json/xml/rss/atom/txt/html/data]
  *
  * This is useful for tracking responses for different methods which can be used to create mock test cases
  *
  * This interceptor can be enabled by setting the VM property 'ibeans.log.responses' to the directory path where the files
  * should be stored.
  */
 public class LogResponsesInterceptor extends AbstractCallInterceptor
 {
     private File logDirectory;
 
     public LogResponsesInterceptor(String logDirectory)
     {
         this(new File(logDirectory));
     }
 
     public LogResponsesInterceptor(File logDirectory)
     {
         if(!logDirectory.exists())
         {
             throw new IllegalArgumentException("Log directory does not exist: " + logDirectory);
         }
 
         if(!logDirectory.isDirectory())
         {
             throw new IllegalArgumentException("Log directory is not a directory: " + logDirectory);
         }
 
         this.logDirectory = logDirectory;
     }
 
     @Override
     public void afterCall(InvocationContext invocationContext) throws Throwable
     {
         Response msg = invocationContext.getResponse();
         boolean isStream = InputStream.class.isAssignableFrom(msg.getPayload().getClass());
         String response = msg.getPayload().toString();
 
         //Set the result on the invocationContext in case we got back a stream, this avoids StreamAlreadyClosed exceptions
         if(isStream)
         {
             //wrap it back up as a ByteArray so that transformers do no need to handle the new String payload
             invocationContext.setResult(new ByteArrayInputStream(response.getBytes()));
         }
         else
         {
             invocationContext.setResult(response);
         }
         
         String type = (String)msg.getHeader("Content-Type");
         String testMethod = getTestMethod(invocationContext.getMethod().getName());
         String ext = getFileExtension(new MimeType(type));
         int i = 1;
         String filename = testMethod + "-response" + (i++) + ext;
         File responseFile = new File(logDirectory, filename);
         //make sure we have a unique file name since the same ibeans method may be called more than once from a test method
         while(responseFile.exists())
         {
             filename = testMethod + "-response" + i++ + ext;
             responseFile = new File(logDirectory, filename);
         }
 
         FileOutputStream fos = openOutputStream(responseFile);
         try
         {
            fos.write(response.getBytes());
         }
         finally
         {
             try
             {
                 fos.close();
             }
             catch (IOException e)
             {
                 //ignore
             }
         }
     }
 
     protected String getFileExtension(MimeType mimeType)
     {
         if(mimeType==null){
             return ".data";
         }
         
 
         if(mimeType.equals(MimeTypes.ATOM))
         {
             return ".atom";
         }
         else if(mimeType.equals(MimeTypes.RSS))
         {
             return ".rss";
         }
         else if(mimeType.equals(MimeTypes.XML) || mimeType.equals(MimeTypes.APPLICATION_XML))
         {
             return ".xml";
         }
         else if(mimeType.equals(MimeTypes.JSON))
         {
             return ".json";
         }
         else if(mimeType.equals(MimeTypes.TEXT))
         {
             return ".txt";
         }
         else if(mimeType.equals(MimeTypes.HTML))
         {
             return ".html";
         }
         else
         {
             return ".data";
         }
     }
 
     private String getTestMethod(String name)
     {
         StackTraceElement[] stack = Thread.currentThread().getStackTrace();
         for (int i = 0; i < stack.length; i++)
         {
             StackTraceElement element = stack[i];
             if(element.getMethodName().equals(name))
             {
                 //This assume that the ibean was called from the current method, not a method called by the test method
                 //This should be fine for almot all if not all ibeans test cases
                 String className = stack[i+1].getClassName();
                 int x = className.lastIndexOf(".");
                 if(x > -1)
                 {
                     className = className.substring(x+1);
                 }
                 return className + "-" + stack[i+1].getMethodName();
             }
         }
         throw new IllegalStateException("Method not found in call stack");
     }
 
     /**
      * Opens a {@link java.io.FileOutputStream} for the specified file, checking and
      * creating the parent directory if it does not exist.
      * <p>
      * At the end of the method either the stream will be successfully opened,
      * or an exception will have been thrown.
      * <p>
      * The parent directory will be created if it does not exist.
      * The file will be created if it does not exist.
      * An exception is thrown if the file object exists but is a directory.
      * An exception is thrown if the file exists but cannot be written to.
      * An exception is thrown if the parent directory cannot be created.
      *
      * @param file  the file to open for output, must not be <code>null</code>
      * @return a new {@link java.io.FileOutputStream} for the specified file
      * @throws java.io.IOException if the file object is a directory, if the file cannot be written to,
      * if a parent directory needs creating but that fails
      */
     public static FileOutputStream openOutputStream(File file) throws IOException
     {
         if (file.exists()) {
             if (file.isDirectory()) {
                 throw new IOException("File '" + file + "' exists but is a directory");
             }
             if (!file.canWrite()) {
                 throw new IOException("File '" + file + "' cannot be written to");
             }
         } else {
             File parent = file.getParentFile();
             if (parent != null && !parent.exists()) {
                 if (!parent.mkdirs()) {
                     throw new IOException("File '" + file + "' could not be created");
                 }
             }
         }
         return new FileOutputStream(file);
     }
 }
