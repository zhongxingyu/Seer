 /*
  * Copyright 2005 Joe Walker
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.directwebremoting.dwrp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.ProgressListener;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.directwebremoting.extend.FormField;
 import org.directwebremoting.extend.ServerException;
 import org.directwebremoting.util.LocalUtil;
 import org.directwebremoting.util.Messages;
 
 /**
  * Utilities to parse GET and POST requests from the DWR javascript section.
  * @author Joe Walker [joe at getahead dot ltd dot uk]
  */
 public class ParseUtil
 {
     /**
      * The javascript outbound marshaller prefixes the toString value with a
      * colon and the original type information. This undoes that.
      * @param data The string to be split up
      * @return A string array containing the split data
      */
     public static String[] splitInbound(String data)
     {
         String[] reply = new String[2];
 
         int colon = data.indexOf(ProtocolConstants.INBOUND_TYPE_SEPARATOR);
         if (colon == -1)
         {
             log.error("Missing : in conversion data (" + data + ')');
             reply[LocalUtil.INBOUND_INDEX_TYPE] = ProtocolConstants.TYPE_STRING;
             reply[LocalUtil.INBOUND_INDEX_VALUE] = data;
         }
         else
         {
             reply[LocalUtil.INBOUND_INDEX_TYPE] = data.substring(0, colon);
             reply[LocalUtil.INBOUND_INDEX_VALUE] = data.substring(colon + 1);
         }
 
         return reply;
     }
     
     /**
      * Parse an inbound request into a set of fields
      * @param request The original browser's request
      * @return The set of fields parsed from the request
      * @throws ServerException
      */
     public Map<String, FormField> parseRequest(HttpServletRequest request) throws ServerException
     {
         boolean get = "GET".equals(request.getMethod());
         if (get)
         {
             return parseGet(request);
         }
         else
         {
             return parsePost(request);
         }
     }
 
     /**
      * Parse an HTTP POST request to fill out the scriptName, methodName and
      * paramList properties. This method should not fail unless it will not
      * be possible to return any sort of error to the user. Failure cases should
      * be handled by the <code>checkParams()</code> method.
      * @param req The original browser's request
      * @return The equivalent of HttpServletRequest.getParameterMap() for now
      * @throws ServerException If reading from the request body stream fails
      */
     private Map<String, FormField> parsePost(HttpServletRequest req) throws ServerException
     {
         Map<String, FormField> paramMap;
 
         if (ServletFileUpload.isMultipartContent(req))
         {
             paramMap = parseMultipartPost(req);
         }
         else
         {
             paramMap = parseBasicPost(req);
         }
 
         // If there is only 1 param then this must be a broken Safari.
         if (paramMap.size() == 1)
         {
             parseBrokenMacPost(paramMap);
         }
         
         return paramMap;
     }
 
     /**
      * The default parse case for a normal form submit
      * @param req The http request
      * @return a map of parsed parameters
      * @throws ServerException
      */
     private Map<String, FormField> parseBasicPost(HttpServletRequest req) throws ServerException
     {
         Map<String, FormField> paramMap;
         paramMap = new HashMap<String, FormField>();
         
         BufferedReader in = null;
         try
         {
             // I've had reports of data loss in Tomcat 5.0 that relate to this bug
             //   http://issues.apache.org/bugzilla/show_bug.cgi?id=27447
             // See mails to users@dwr.dev.java.net:
             //   Subject: "Tomcat 5.x read-ahead problem"
             //   From: CAKALIC, JAMES P [AG-Contractor/1000]
             // It would be more normal to do the following:
             // BufferedReader in = req.getReader();
             in = new BufferedReader(new InputStreamReader(req.getInputStream()));
    
             while (true)
             {
                 String line = in.readLine();
    
                 if (line == null)
                 {
                     break;
                 }
    
                 if (line.indexOf('&') != -1)
                 {
                     // If there are any &'s then this must be iframe post and all the
                     // parameters have got dumped on one line, split with &
                     log.debug("Using iframe POST mode");
                     StringTokenizer st = new StringTokenizer(line, "&");
                     while (st.hasMoreTokens())
                     {
                         String part = st.nextToken();
                         part = LocalUtil.decode(part);
    
                         parsePostLine(part, paramMap);
                     }
                 }
                 else
                 {
                     // Hooray, this is a normal one!
                     parsePostLine(line, paramMap);
                 }
             }
         }
         catch (Exception ex)
         {
             throw new ServerException(Messages.getString("ParseUtil.InputReadFailed"), ex);
         }
         finally
         {
             if (in != null)
             {
                 try
                 {
                     in.close();
                 }
                 catch (IOException ex)
                 {
                     // Ignore
                 }
             }
         }
         return paramMap;
     }
 
     /**
      * Parse a multipart request using commons file-upload.
      * @param req
      * @return A map of FileItems. Strings and files can be determined by isFormField()
      * @throws ServerException
      */
     @SuppressWarnings("unchecked")
     private Map<String, FormField> parseMultipartPost(HttpServletRequest req) throws ServerException
     {
         try
         {
             Map<String, FormField> map = new HashMap<String, FormField>();
             File location = new File(System.getProperty("java.io.tmpdir"));
             DiskFileItemFactory itemFactory = new DiskFileItemFactory(DEFAULT_SIZE_THRESHOLD, location);
 
             ServletFileUpload fileUploader = new ServletFileUpload(itemFactory);
             fileUploader.setProgressListener(new ProgressListener()
             {
                 public void update(long bytesRead, long contentLength, int items)
                 {
                 }
             });
 
             List<FileItem> fileItems = fileUploader.parseRequest(req);
             for (FileItem fileItem : fileItems)
             {
                 FormField formField;
                 if (fileItem.isFormField())
                 {
                     formField = new FormField(fileItem.getString());
                 }
                 else
                 {
                     formField = new FormField(fileItem.getName(), fileItem.getContentType(), fileItem.get());
                 }
                 map.put(fileItem.getFieldName(), formField);
             }
             return map;
         }
         catch (FileUploadException e)
         {
             throw new ServerException(Messages.getString("ParseUtil.InputReadFailed"), e);
         }
     }
 
     /**
      * All the parameters have got dumped on one line split with \n
      * See: http://bugzilla.opendarwin.org/show_bug.cgi?id=3565
      *      https://dwr.dev.java.net/issues/show_bug.cgi?id=93
      *      http://jira.atlassian.com/browse/JRA-8354
      *      http://developer.apple.com/internet/safari/uamatrix.html
      * @param paramMap The broken parsed parameter
      */
     private static void parseBrokenMacPost(Map<String, FormField> paramMap)
     {
         // This looks like a broken Mac where the line endings are confused
         log.debug("Using Broken Safari POST mode");
 
         // Iterators insist that we call hasNext() before we start
         Iterator<String> it = paramMap.keySet().iterator();
         if (!it.hasNext())
         {
             throw new IllegalStateException("No entries in non empty map!");
         }
 
         // So get the first
         String key = it.next();
         String value = paramMap.get(key).getString();
         String line = key + ProtocolConstants.INBOUND_DECL_SEPARATOR + value;
 
         StringTokenizer st = new StringTokenizer(line, "\n");
         while (st.hasMoreTokens())
         {
             String part = st.nextToken();
             part = LocalUtil.decode(part);
 
             parsePostLine(part, paramMap);
         }
     }
 
     /**
      * Sort out a single line in a POST request
      * @param line The line to parse
      * @param paramMap The map to add parsed parameters to
      */
     private static void parsePostLine(String line, Map<String, FormField> paramMap)
     {
         if (line.length() == 0)
         {
             return;
         }
 
         int sep = line.indexOf(ProtocolConstants.INBOUND_DECL_SEPARATOR);
         if (sep == -1)
         {
             paramMap.put(line, null);
         }
         else
         {
             String key = line.substring(0, sep);
             String value = line.substring(sep  + ProtocolConstants.INBOUND_DECL_SEPARATOR.length());
 
             paramMap.put(key, new FormField(value));
         }
     }
 
     /**
      * Parse an HTTP GET request to fill out the scriptName, methodName and
      * paramList properties. This method should not fail unless it will not
      * be possible to return any sort of error to the user. Failure cases should
      * be handled by the <code>checkParams()</code> method.
      * @param req The original browser's request
      * @return Simply HttpRequest.getParameterMap() for now
      * @throws ServerException If the parsing fails
      */
     @SuppressWarnings("unchecked")
     private Map<String, FormField> parseGet(HttpServletRequest req) throws ServerException
     {
         Map<String, FormField> convertedMap = new HashMap<String, FormField>();
         Map<String, String[]> paramMap = req.getParameterMap();
 
         for (Map.Entry<String, String[]> entry : paramMap.entrySet())
         {
             String key = entry.getKey();
             String[] array = entry.getValue();
 
             if (array.length == 1)
             {
                 convertedMap.put(key, new FormField(array[0]));
             }
             else
             {
                 throw new ServerException(Messages.getString("ParseUtil.MultiValues", key));
             }
         }
 
         return convertedMap;
     }
 
     /**
      * The log stream
      */
     private static final Log log = LogFactory.getLog(ParseUtil.class);
     
     /**
      * The threshold, in bytes, below which items will be retained in memory and above which they will be stored as a file
      */
     private static final int DEFAULT_SIZE_THRESHOLD = 256 * 1024;
 }
