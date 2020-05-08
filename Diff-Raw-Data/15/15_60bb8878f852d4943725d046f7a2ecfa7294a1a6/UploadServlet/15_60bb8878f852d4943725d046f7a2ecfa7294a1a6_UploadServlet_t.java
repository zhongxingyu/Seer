 /*
  * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
  * http://code.google.com/p/gwtupload
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package gwtupload.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.FileUploadBase.IOFileUploadException;
 import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 /** Upload servlet for the GwtUpload library. */
 public abstract class UploadServlet extends HttpServlet {
 
   private static final long serialVersionUID = 1L;
   private static final Logger logger = Logger.getLogger(UploadServlet.class);
   private static final String XML_TPL = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<response>%%MESSAGE%%</response>\n";
   protected long maxSize = 5000; // 5000 bytes, 5kB
   protected int uploadDelay = 0;
   protected FileRepository repo;
 
   /** Read configurable parameters during the servlet initialization. */
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
 
     repo = newFileRepository();
 
     String size = config.getServletContext().getInitParameter("maxSize");
     if (size != null) {
       maxSize = Long.parseLong(size);
     }
 
     String delay = config.getServletContext().getInitParameter("uploadDelay");
     if (delay != null) {
       uploadDelay = Integer.valueOf(delay);
     }
 
     logger.info("init: maxSize=" + maxSize + ", uploadDelay=" + uploadDelay + ")");
   }
 
   /** The get method is used to monitor the uploading process or to get the content of the uploaded files. */
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
     if (request.getParameter("show") != null) {
       final Integer fileToken = getToken(request, "show");
       if (fileToken != null) {
         repo.sendData(fileToken, response);
       }
 
     } else if (request.getParameter("cancel") != null) {
       final Integer fileToken = getToken(request, "cancel");
       if (fileToken != null) {
         repo.saveError(fileToken, "cancelled");
         renderXmlResponse(request, response, wrapXml("cancelled", "true"));
       } else {
         renderXmlResponse(request, response, errorXml("Missing token"));
       }
 
     } else if (request.getParameter("remove") != null) {
       // noop for now
       renderXmlResponse(request, response, finishedXml("OK"));
 
     } else if (request.getParameter("clean") != null) {
       // noop for now
       renderXmlResponse(request, response, finishedXml("OK"));
 
     } else if (request.getParameter("status") != null) {
       final Integer fileToken = getToken(request, "status");
       if (fileToken != null) {
         final String statusXml = getUploadStatus(fileToken);
         renderXmlResponse(request, response, statusXml);
       } else {
         renderXmlResponse(request, response, errorXml("Missing token"));
       }
     }
 
   }
 
   /**
    * The post method is used to receive the file and save it in the user
    * session. It returns a very XML page that the client receives in an
    * iframe.
    * 
    * The content of this xml document has a tag error in the case of error in
    * the upload process or the string OK in the case of success.
    * 
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
     try {
       parsePostRequest(request, response);
       renderXmlResponse(request, response, finishedXml("OK"));
     } catch (UploadCancelledException e) {
       // expected cancelled error
       renderXmlResponse(request, response, wrapXml("cancelled", "true") + errorXml(e.getMessage()));
     } catch (IOFileUploadException io) {
       // expected io error and upwrap the IOException
       throw (IOException) io.getCause();
     } catch (SizeLimitExceededException e) {
       // expected size error
       renderXmlResponse(request, response, errorXml(e.getMessage()));
     } catch (Exception e) {
       // not expected
       logger.error("Exception -> " + e.getMessage(), e);
       renderXmlResponse(request, response, errorXml(e.getMessage()));
     }
   }
 
   protected void parsePostRequest(HttpServletRequest request, HttpServletResponse response) throws FileUploadException, IOException {
     try {
       String delay = request.getParameter("delay");
       uploadDelay = Integer.parseInt(delay);
     } catch (Exception e) {}
 
     // set file upload progress listener to store status in the db
     final Integer fileToken = getToken(request, "fileToken");
     if (fileToken == null) { throw new UploadCancelledException("Missing token"); }
 
    // reset any old error in case they are trying again
    repo.saveError(fileToken, null);

     final UploadListener listener = new UploadListener(repo, fileToken, uploadDelay);
     logger.debug("(" + fileToken + ") new upload request received.");
 
     try {
       // Call to a method which the user can override
       checkRequest(request);
 
       // Create the factory used for uploading files,
       ServletFileUpload uploader = new ServletFileUpload();
       uploader.setSizeMax(maxSize);
       uploader.setProgressListener(listener);
 
       // Receive the files--well, file, I hacked this to only support 1 file
       logger.debug("(" + fileToken + ") parsing HTTP POST request");
       boolean found = false;
       for (final FileItemIterator i = uploader.getItemIterator(request); i.hasNext() && !found;) {
         FileItemStream s = i.next();
         if (!s.isFormField()) {
           InputStream in = s.openStream();
           try {
            repo.saveData(fileToken, s.getContentType(), in, request);
           } finally {
             IOUtils.closeQuietly(in);
           }
           found = true;
         }
       }
       logger.debug("(" + fileToken + ") parsed request, item received.");
 
       if (!found) {
         // Exception so that it gets called into repo.saveError
         throw new UploadCancelledException("File was empty");
       }
     } catch (IOException io) {
       repo.saveError(fileToken, io.getMessage());
       throw io;
     } catch (FileUploadException fue) {
       repo.saveError(fileToken, fue.getMessage());
       throw fue;
     } catch (RuntimeException e) {
       repo.saveError(fileToken, e.getMessage());
       throw e;
     }
   }
 
   /** Override this method if you want to check the request before it is passed to commons-fileupload parser. */
   protected void checkRequest(HttpServletRequest request) throws SizeLimitExceededException {
     logger.debug("procesing a request with size: " + request.getContentLength() + " bytes.");
     if (request.getContentLength() > maxSize)
       throw new SizeLimitExceededException("File", maxSize, request.getContentLength());
   }
 
   protected String getUploadStatus(Integer fileToken) {
     final StringBuilder sb = new StringBuilder();
     final FileProgress progress = repo.loadProgress(fileToken);
 
     if (progress.getMessage() != null) {
       logger.debug("(" + fileToken + ") status message: " + progress.getMessage());
       if ("cancelled".equals(progress.getMessage())) {
         sb.append(wrapXml("cancelled", "true"));
         sb.append(finishedXml("cancelled"));
       } else {
         sb.append(wrapXml("error", progress.getMessage()));
         sb.append(finishedXml("error"));
       }
 
     } else {
       logger.debug("(" + fileToken + ") status percent: " + progress.getPercent() + "%");
       sb.append(wrapXml("percent", String.valueOf(progress.getPercent())));
       sb.append(wrapXml("currentBytes", String.valueOf(progress.getCurrentBytes())));
       sb.append(wrapXml("totalBytes", String.valueOf(progress.getTotalBytes())));
       if (progress.getPercent() >= 100) {
         sb.append(finishedXml("OK"));
       }
     }
 
     return sb.toString();
   }
 
   protected void renderXmlResponse(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
     response.setContentType("text/html");
     PrintWriter out = response.getWriter();
     try {
       out.print(XML_TPL.replace("%%MESSAGE%%", message != null ? message : ""));
       out.flush();
     } finally {
       IOUtils.closeQuietly(out);
     }
   }
 
   protected Integer getToken(HttpServletRequest request, String paramName) {
     try {
       return new Integer(request.getParameter(paramName));
     } catch (RuntimeException e) {
       return null;
     }
   }
 
   protected String errorXml(String message) {
     return "<error>" + message + "</error>";
   }
 
   protected String finishedXml(String message) {
     return "<finished>" + message + "</finished>";
   }
 
   protected String wrapXml(String elem, String message) {
     return "<" + elem + ">" + message + "</" + elem + ">";
   }
 
   protected abstract FileRepository newFileRepository();
 }
