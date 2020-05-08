 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.fileupload;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * A simple file upload utility . The class uploads the file from a multipart
  * encoded form to a given directory.
  */
 public class FileUpload
 {
     private Log log = LogFactory.getLog(FileUpload.class);
     // The multipart request
     private HttpServletRequest req;
 
     // Upload dir
     private String dir;
 
     // Name of the file being uploaded currently
     private String fileName;
 
     // Table of form fields v/s values
     private Hashtable map = new Hashtable();
 
     // The boundary
     private String boundary = "--";
 
     private byte[] buff = new byte[100 * 1024];
 
     // Input stream from the request
     private ServletInputStream in;
 
     // Name of the form field
     private String paramName;
 
     // Content disposition name
     private String contentDisp;
 
     // tempFIle prefix
     private String prefix;
 
     // request variable for file
     private String uploadFileArg;
 
     /**
      * The constructor takes the request and upload dir path as paramters and
      * uploads all the files in the request to the dir.
      *
      * @param r         - multipart encoded HttpServletRequest
      * @param uploadDir - The directory to which the files should be uploaded
      *
      * @throws IOException - If there was a problem reading the stream or
      *                     writing to the file.
      */
     public FileUpload(HttpServletRequest r, String uploadDir, String prefix, String uploadFileArg) throws IOException
     {
         req = r;
         dir = uploadDir;
         this.prefix = prefix;
         this.uploadFileArg = uploadFileArg;
         upload();
     }
 
     /**
      * The method reads the next line from the servlet input stream into a byte
      * array and returns a String form of the array.Returns null if it reaches the
      * end of the stream
      *
      * @return String The next line in the stream
      *
      * @throws IOException - If there was a problem reading the stream or
      */
     private String readLine() throws IOException
     {
         int len = 0;
         String line = null;
         len = in.readLine(buff, 0, buff.length);
         if(len < 0) return null;
         line = new String(buff, 0, len, "ISO-8859-1");
 
         return line;
     }
 
     /**
      * The method loops through the lines in the input stream and calls
      * writeToFile() if it encounters a file and readParam() if it encounters a
      * form field
      *
      * @throws IOException - If there was a problem reading the stream or
      */
     public void upload() throws IOException
     {
         log.debug("upload() - processing request");
         // Set the boundary string
         setBoundary();
 
         // The request stream
         in = req.getInputStream();
         int len = 0;
 
         // the first line is the boundary , ignore it
         String line = readLine();
         while((line = readLine()) != null)
         {
             log.debug("from request: " + line);
             // set dispostion, param name and filename
             setHeaders(line);
 
             // skip next line
             line = readLine();
 
             // if there is a content-type specified, skip next line too,
             // and this is a file, so upload it to the file system
             if(line.toLowerCase().startsWith("content-type"))
             {
                 line = readLine();
                 writeToFile();
                 continue;
             }
             else
             {
                 // its  a form field, read it.
                 readParam();
             }
         }
     }
 
     /**
      * Sets the boundary string for this request
      */
     private void setBoundary() throws IOException
     {
         String temp = req.getContentType();
         int index = temp.indexOf("boundary=");
         boundary += temp.substring(index + 9, temp.length());
     }
 
 
     /**
      * Reads the form field and puts it in to a table
      */
     private void readParam() throws IOException
     {
         String line = null;
         StringBuffer buf = new StringBuffer();
         while(!(line = readLine()).startsWith(boundary))
         {
             buf.append(line);
         }
         line = buf.substring(0, buf.length() - 2);
         if(map.containsKey(paramName))
         {
             Object existingValue = map.get(paramName);
             List valueList = null;
             if(existingValue instanceof List)
             {
                 valueList = (List) existingValue;
             }
             else
             {
                 valueList = new ArrayList();
                 valueList.add(existingValue);
             }
             valueList.add(line);
             map.put(paramName, valueList);
         }
         map.put(paramName, line);
     }
 
     /**
      * Sets the content disposition, param name and file name fields
      *
      * @param line the content-disposition line
      */
     public void setHeaders(String line)
     {
         StringTokenizer tokens = new StringTokenizer(line, ";", false);
         String token = tokens.nextToken();
         String temp = token.toLowerCase();
         int index = temp.indexOf("content-disposition=");
 
         contentDisp = token.substring(index + 21, token.length());
         token = tokens.nextToken();
         temp = token.toLowerCase();
         index = token.indexOf("name=");
         paramName = token.substring(index + 6, token.lastIndexOf('"'));
         fileName = null;
 
         if(tokens.hasMoreTokens())
         {
             token = tokens.nextToken();
             temp = token.toLowerCase();
             index = token.indexOf("filename=");
             fileName = token.substring(index + 10, token.length());
             index = fileName.lastIndexOf('/');
             if(index < 0)
             {
                 index = fileName.lastIndexOf('\\');
             }
             if(index < 0)
             {
                 fileName = fileName.substring(0, fileName.lastIndexOf('"'));
             }
             else
             {
                 fileName = fileName.substring(index + 1, fileName.lastIndexOf('"'));
             }
         }
     }
 
     /**
      * Reads the file content from the stream and writes it to the local file system
      *
      * @throws IOException - If there was a problem reading the stream
      */
     private void writeToFile() throws IOException
     {
         // Open an o/p stream
         File tmpFile = File.createTempFile(prefix, "upload", new File(dir));
         tmpFile.deleteOnExit();
         log.debug("Setting: " + uploadFileArg + " to: " + tmpFile.getPath());
         map.put(uploadFileArg, tmpFile.getPath());
         //FileOutputStream out = new FileOutputStream (dir+File.separator+fileName);
         FileOutputStream out = new FileOutputStream(tmpFile);
 
         // this flag checks if \r\n needs to be written to the file
         // the servlet output stream appends these characters at the end of the
         // last line of the content, which should be skipped
         // so in the loop, all \r\n but for the last line are written to the file
         boolean writeCR = false;
         int len = 0;
         String line = null;
         map.put(paramName, fileName);
 
         // for each line
         while((len = in.readLine(buff, 0, buff.length)) > -1)
         {
             line = new String(buff, 0, len);
 
             // if end of content, break
             if(line.startsWith(boundary)) break;
             if(writeCR)
             {
                 writeCR = false;
                 out.write('\r');
                 out.write('\n');
             }
             if(len > 2 && buff[len - 2] == '\r' && buff[len - 1] == '\n')
             {
                 writeCR = true;
                 out.write(buff, 0, len - 2);
             }
             else
             {
                 out.write(buff, 0, len);
             }
         }
         out.close();
     }
 
     /**
      * Returns the value of a request parameter as a String, or null if
      * the parameter does not exist.The method overrides parent method.
      *
      * @param name The name of the parameter
      *
      * @return String  The value of the paramter
      */
     public String getParameter(String name)
     {
         log.debug("calling getParameter for :" + name);
         Object val = map.get(name);
         if(val == null)
             return null;
         log.debug("val is a: " + val.getClass().getName());
         if(val instanceof String)
         {
             return (String) val;
         }
         else
         {
             List vals = (List) val;
             return (String) vals.get(0);
         }
     }
 
     /**
      * Returns an Enumeration of String objects containing the names of the
      * parameters contained in this request. If the  request has no parameters,
      * the method returns an empty Enumeration.The method overrides parent method.
      *
      * @return Enumeration of String objects, each String containing the
      *         name of a request parameter; or an empty Enumeration if the request has
      *         no parameters
      */
     public java.util.Enumeration getParameterNames()
     {
         return map.keys();
     }
 
     /**
      * Returns an array of String objects containing all of the values the given
      * request parameter has, or null if the parameter does not exist.
      * The method overrides parent method.
      *
      * @param name the name of the parameter whose value is requested
      *
      * @return <String[] an array of String objects containing the
      *         parameter's values
      */
     public String[] getParameterValues(String name)
     {
         Object val = map.get(name);
        if(val == null)
            return null;
         if(val instanceof String)
         {
             return new String[]{(String) val};
         }
         else
         {
             List vals = (List) val;
             return (String[]) vals.toArray(new String[vals.size()]);
         }
     }
 }
