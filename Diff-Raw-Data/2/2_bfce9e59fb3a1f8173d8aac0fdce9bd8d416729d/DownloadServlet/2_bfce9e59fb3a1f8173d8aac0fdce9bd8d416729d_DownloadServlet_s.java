 package uk.ac.ebi.arrayexpress.servlets;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
 import uk.ac.ebi.arrayexpress.components.Experiments;
 import uk.ac.ebi.arrayexpress.components.Files;
 import uk.ac.ebi.arrayexpress.components.Users;
 import uk.ac.ebi.arrayexpress.utils.CookieMap;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class DownloadServlet extends ApplicationServlet
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     // buffer size (in bytes)
     private final int TRANSFER_BUFFER_SIZE = 10 * 1024 * 1024;
 
     // multipart boundary constant
     private final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
 
     public class DownloadServletException extends Exception
     {
         public DownloadServletException( String message )
         {
             super(message);
         }
     }
 
     protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
     {
         return true; // all requests are supported
     }
 
     // Respond to HTTP requests from browsers.
     protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType )
             throws ServletException, IOException
     {
         logRequest(logger, request, requestType);
 
         // 1. validate arguments: if file exists and available
         try {
             File requestedFile = validateRequest(request, response);
             if (null != requestedFile) { // so we can proceed
                 sendFile(requestedFile, request, response, requestType );
             }
         } catch (DownloadServletException x) {
             logger.error(x.getMessage());
         } catch (Exception x) {
             if (x.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                 // generate log entry for client abortion
                 logger.warn("Download aborted");
             } else {
                 throw new ServletException(x);
             }
         }
     }
 
     private File validateRequest( HttpServletRequest request, HttpServletResponse response )
             throws Exception
     {
         String accession = null;
         String name = null;
         File file;
 
         String[] requestArgs = new RegexHelper("servlets/download/([^/]+)/?([^/]*)", "i")
                 .match(request.getRequestURL().toString());
         if (null != requestArgs) {
             if (requestArgs[1].equals("")) {
                 name = requestArgs[0]; // old-style
             } else {
                 accession = requestArgs[0];
                 name = requestArgs[1];
             }
         }
         logger.info("Requested download of [{}], accession [{}]", name, accession);
         Files files = (Files) getComponent("Files");
         Experiments experiments = (Experiments) getComponent("Experiments");
         Users users = (Users) getComponent("Users");
 
         List<String> userIds = new ArrayList<String>();
         userIds.add("1");   // add guest
         CookieMap cookies = new CookieMap(request.getCookies());
         if (cookies.containsKey("AeLoggedUser") && cookies.containsKey("AeLoginToken")) {
             String user = cookies.get("AeLoggedUser").getValue();
             String passwordHash = cookies.get("AeLoginToken").getValue();
             if (users.verifyLogin(user, passwordHash, request.getRemoteAddr().concat(request.getHeader("User-Agent")))) {
                 userIds = users.getUserIDs(user);
             } else {
                 logger.warn("Removing invalid session cookie for user [{}]", user);
                 // resetting cookies
                 Cookie userCookie = new Cookie("AeLoggedUser", "");
                 userCookie.setPath("/");
                 userCookie.setMaxAge(0);
 
                 response.addCookie(userCookie);
             }
         }
 
 
         if (!files.doesExist(accession, name)) {
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             throw new DownloadServletException("File with name [" + name + "], accession [" + accession + "] is not in files.xml");
         } else {
             String fileLocation = files.getLocation(accession, name);
 
             if (null != fileLocation && null == accession) {
                 // attempt to resolve accession for file by its location
                 accession = files.getAccession(fileLocation);
             }
 
             // finally if there is no accession or location determined at the stage - panie
             if (null == fileLocation || null == accession) {
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                 throw new DownloadServletException("Either accession [" + String.valueOf(accession) + "] or location [" + String.valueOf(fileLocation) + "] were not determined");
             }
 
             if (!experiments.isAccessible(accession, userIds)) {
                 response.sendError(HttpServletResponse.SC_FORBIDDEN);
                throw new DownloadServletException("The experiment [" + accession + "] is not accessible for user id(s) [" + StringTools.arrayToString((String[])userIds.toArray(), ", ") + "]");
             }
 
             logger.debug("Will be serving file [{}]", fileLocation);
             file = new File(files.getRootFolder(), fileLocation);
         }
         return file;
     }
 
     private void sendFile( File requestedFile, HttpServletRequest request, HttpServletResponse response, RequestType requestType )
             throws IOException, DownloadServletException
     {
         // Check if file is actually supplied to the request URL.
         if (null == requestedFile) {
             // Do your thing if the file is not supplied to the request URL.
             // Throw an exception, or send 404, or show default/warning page, or just ignore it.
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             throw new DownloadServletException("Specified [null] file to sendFile");
         }
 
         // Check if file actually exists in filesystem.
         if (!requestedFile.exists() || !requestedFile.isFile()) {
             // Do your thing if the file appears to be non-existing.
             // Throw an exception, or send 404, or show default/warning page, or just ignore it.
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             throw new DownloadServletException("Specified file [" + requestedFile.getPath() + "] does not exist in file system or is not a file");
         }
 
         // Prepare some variables. The ETag is an unique identifier of the file.
         String fileName = requestedFile.getName();
         long length = requestedFile.length();
         long lastModified = requestedFile.lastModified();
         String eTag = fileName + "_" + length + "_" + lastModified;
 
 
         // Validate request headers for caching ---------------------------------------------------
 
         // If-None-Match header should contain "*" or ETag. If so, then return 304.
         String ifNoneMatch = request.getHeader("If-None-Match");
         if (ifNoneMatch != null && (ifNoneMatch.contains("*") || matches(ifNoneMatch, eTag))) {
             response.setHeader("ETag", eTag); // Required in 304.
             response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
             return;
         }
 
         // If-Modified-Since header should be greater than LastModified. If so, then return 304.
         // This header is ignored if any If-None-Match header is specified.
         long ifModifiedSince = request.getDateHeader("If-Modified-Since");
         if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
             response.setHeader("ETag", eTag); // Required in 304.
             response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
             return;
         }
 
 
         // Validate request headers for resume ----------------------------------------------------
 
         // If-Match header should contain "*" or ETag. If not, then return 412.
         String ifMatch = request.getHeader("If-Match");
         if (ifMatch != null && !ifMatch.contains("*") && !matches(ifMatch, eTag)) {
             response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
             return;
         }
 
         // If-Unmodified-Since header should be greater than LastModified. If not, then return 412.
         long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
         if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
             response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
             return;
         }
 
 
         // Validate and process range -------------------------------------------------------------
 
         // Prepare some variables. The full Range represents the complete file.
         Range full = new Range(0, length - 1, length);
         List<Range> ranges = new ArrayList<Range>();
 
         // Validate and process Range and If-Range headers.
         String range = request.getHeader("Range");
         if (range != null) {
 
             // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
             if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                 response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                 response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                 return;
             }
 
             // If-Range header should either match ETag or be greater then LastModified. If not,
             // then return full file.
             String ifRange = request.getHeader("If-Range");
             if (ifRange != null && !ifRange.equals(eTag)) {
                 try {
                     long ifRangeTime = request.getDateHeader("If-Range"); // Throws IAE if invalid.
                     if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                         ranges.add(full);
                     }
                 } catch (IllegalArgumentException ignore) {
                     ranges.add(full);
                 }
             }
 
             // If any valid If-Range header, then process each part of byte range.
             if (ranges.isEmpty()) {
                 for (String part : range.substring(6).split(",")) {
                     // Assuming a file with length of 100, the following examples returns bytes at:
                     // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                     long start = sublong(part, 0, part.indexOf("-"));
                     long end = sublong(part, part.indexOf("-") + 1, part.length());
 
                     if (start == -1) {
                         start = length - end;
                         end = length - 1;
                     } else if (end == -1 || end > length - 1) {
                         end = length - 1;
                     }
 
                     // Check if Range is syntactically valid. If not, then return 416.
                     if (start > end) {
                         response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                         response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                         return;
                     }
 
                     // Add range.
                     ranges.add(new Range(start, end, length));
                 }
             }
         }
 
 
         // Prepare and initialize response --------------------------------------------------------
 
         // Get content type by file name.
         String contentType = getServletContext().getMimeType(fileName);
 
         // If content type is unknown, then set the default value.
         // For all content types, see: http://www.w3schools.com/media/media_mimeref.asp
         // To add new content types, add new mime-mapping entry in web.xml.
         if (contentType == null) {
             contentType = "application/octet-stream";
         }
 
         // Determine content disposition. If content type is supported by the browser or an image
         // then it is set to inline, else attachment which will pop up a 'save as' dialogue.
         String accept = request.getHeader("Accept");
         boolean inline = (accept != null && accepts(accept, contentType));
         String disposition = (inline || contentType.startsWith("image")) ? "inline" : "attachment";
 
         // Initialize response.
         response.reset();
         response.setBufferSize(TRANSFER_BUFFER_SIZE);
         response.setHeader("Content-Disposition", disposition + ";filename=\"" + fileName + "\"");
         response.setHeader("Accept-Ranges", "bytes");
         response.setHeader("ETag", eTag);
         response.setDateHeader("Last-Modified", lastModified);
 
 
         // Send requested file (part(s)) to client ------------------------------------------------
 
         // Prepare streams.
         RandomAccessFile input = null;
         ServletOutputStream output = null;
 
         try {
             // Open streams.
             input = new RandomAccessFile(requestedFile, "r");
             output = response.getOutputStream();
 
             if (ranges.isEmpty() || ranges.get(0) == full) {
 
                 // Return full file.
                 response.setContentType(contentType);
                 response.setHeader("Content-Range", "bytes " + full.start + "-" + full.end + "/" + full.total);
                 response.setHeader("Content-Length", String.valueOf(full.length));
 
                 if (RequestType.GET == requestType || RequestType.POST == requestType) {
                     // Copy full range.
                     copy(input, output, full.start, full.length);
                     logger.info("Full download of [{}] completed, sent [{}] bytes", fileName, full.length);
                 }
 
             } else if (ranges.size() == 1) {
 
                 // Return single part of file.
                 Range r = ranges.get(0);
                 response.setContentType(contentType);
                 response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total);
                 response.setHeader("Content-Length", String.valueOf(r.length));
                 response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
 
                 if (RequestType.GET == requestType || RequestType.POST == requestType) {
                     // Copy single part range.
                     copy(input, output, r.start, r.length);
                     logger.info("Single range download of [{}] completed, sent [{}] bytes", fileName, r.length);
 
                 }
 
             } else {
 
                 // Return multiple parts of file.
                 response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                 response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
 
                 if (RequestType.GET == requestType || RequestType.POST == requestType) {
                     // Copy multi part range.
                     for (Range r : ranges) {
                         // Add multipart boundary and header fields for every range.
                         output.println();
                         output.println("--" + MULTIPART_BOUNDARY);
                         output.println("Content-Type: " + contentType);
                         output.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);
 
                         // Copy single part range of multi part range.
                         copy(input, output, r.start, r.length);
                         logger.info("A range of multiple-part download of [{}] completed, sent [{}] bytes", fileName, r.length);
                     }
 
                     // End with multipart boundary.
                     output.println();
                     output.println("--" + MULTIPART_BOUNDARY + "--");
                 }
             }
 
             // Finalize task.
             output.flush();
         } finally {
             // Gently close streams.
             close(output);
             close(input);
         }
     }
     
     /**
      * Returns true if the given accept header accepts the given value.
      * @param acceptHeader The accept header.
      * @param toAccept The value to be accepted.
      * @return True if the given accept header accepts the given value.
      */
     private boolean accepts(String acceptHeader, String toAccept) {
         String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
         Arrays.sort(acceptValues);
         return Arrays.binarySearch(acceptValues, toAccept) > -1
             || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
             || Arrays.binarySearch(acceptValues, "*/*") > -1;
     }
 
     /**
      * Returns true if the given match header matches the given value.
      * @param matchHeader The match header.
      * @param toMatch The value to be matched.
      * @return True if the given match header matches the given value.
      */
     private static boolean matches(String matchHeader, String toMatch) {
         String[] matchValues = matchHeader.split("\\s*,\\s*");
         Arrays.sort(matchValues);
         return Arrays.binarySearch(matchValues, toMatch) > -1
             || Arrays.binarySearch(matchValues, "*") > -1;
     }
 
     /**
      * Returns a substring of the given string value from the given begin index to the given end
      * index as a long. If the substring is empty, then -1 will be returned
      * @param value The string value to return a substring as long for.
      * @param beginIndex The begin index of the substring to be returned as long.
      * @param endIndex The end index of the substring to be returned as long.
      * @return A substring of the given string value as long or -1 if substring is empty.
      */
     private long sublong(String value, int beginIndex, int endIndex) {
         String substring = value.substring(beginIndex, endIndex);
         return (substring.length() > 0) ? Long.parseLong(substring) : -1;
     }
 
     /**
      * Copy the given byte range of the given input to the given output.
      * @param input The input to copy the given range to the given output for.
      * @param output The output to copy the given range from the given input for.
      * @param start Start of the byte range.
      * @param length Length of the byte range.
      * @throws IOException If something fails at I/O level.
      */
     private void copy(RandomAccessFile input, OutputStream output, long start, long length)
         throws IOException
     {
         byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
         int read;
 
         if (input.length() == length) {
             // Write full range.
             while ((read = input.read(buffer)) > 0) {
                 output.write(buffer, 0, read);
             }
         } else {
             // Write partial range.
             input.seek(start);
             long toRead = length;
 
             while ((read = input.read(buffer)) > 0) {
                 if ((toRead -= read) > 0) {
                     output.write(buffer, 0, read);
                 } else {
                     output.write(buffer, 0, (int) toRead + read);
                     break;
                 }
             }
         }
     }
 
     /**
      * Close the given resource.
      * @param resource The resource to be closed.
      */
     private void close(Closeable resource) {
         if (resource != null) {
             try {
                 resource.close();
             } catch (IOException ignore) {
                 // Ignore IOException. If you want to handle this anyway, it might be useful to know
                 // that this will generally only be thrown when the client aborted the request.
             }
         }
     }
 
     // Inner classes ------------------------------------------------------------------------------
 
     /**
      * This class represents a byte range.
      */
     protected class Range {
         long start;
         long end;
         long length;
         long total;
 
         /**
          * Construct a byte range.
          * @param start Start of the byte range.
          * @param end End of the byte range.
          * @param total Total length of the byte source.
          */
         public Range(long start, long end, long total) {
             this.start = start;
             this.end = end;
             this.length = end - start + 1;
             this.total = total;
         }
     }
 }
