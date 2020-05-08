 package ch.rollis.emma.contenthandler;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Date;
 
 import ch.rollis.emma.Emma;
 import ch.rollis.emma.context.ServerContext;
 import ch.rollis.emma.request.Request;
 import ch.rollis.emma.response.Response;
 import ch.rollis.emma.response.ResponseFactory;
 import ch.rollis.emma.response.ResponseStatus;
 import ch.rollis.emma.util.DateConverter;
import ch.rollis.emma.util.DateConverterException;
 import ch.rollis.emma.util.MimeTypes;
 
 /**
  * This content handler works on resources in the filesystem.
  * <p>
  * 
  * @author mrolli
  */
 public class FileHandler implements ContentHandler {
     @Override
     public Response process(final Request request, final ServerContext context) {
         File docRoot, file;
 
         ResponseFactory responseFacotry = new ResponseFactory();
 
         try {
             if (!(request.isGet() || request.isHead())) {
                 return responseFacotry.getResponse(request, ResponseStatus.NOT_IMPLEMENTED);
             }
 
             docRoot = context.getDocumentRoot().getCanonicalFile();
 
             String uri = request.getRequestURI().getPath();
 
             file = new File(docRoot, uri);
             if (!file.exists()) {
                 return responseFacotry.getResponse(request, ResponseStatus.NOT_FOUND);
             }
 
             if (file.isDirectory()) {
                 if (!uri.endsWith("/")) {
                     StringBuilder location = new StringBuilder();
                     if (request.isSslSecured()) {
                         location.append("https://");
                     } else {
                         location.append("http://");
                     }
                     location.append(context.getServerName());
                     location.append(":" + request.getPort());
                     location.append(uri + "/");
                     Response response = responseFacotry.getResponse(request,
                             ResponseStatus.MOVED_PERMANENTLY);
                     response.setHeader("Location", location.toString());
                     return response;
                 }
             }
 
             // check default index files
             for (String defaultFile : Emma.DEFAULT_FILES) {
                 if (new File(file, defaultFile).exists()) {
                     file = new File(file, "/" + defaultFile);
                     break;
                 }
             }
 
             if (!file.canRead()) {
                 return responseFacotry.getResponse(request, ResponseStatus.FORBIDDEN);
             }
 
             if (file.isDirectory()) {
                 // still a directory
                 if (context.allowsIndexes()) {
                     String listing = getDirectoryListing(uri, file);
                     Response response = responseFacotry.getResponse(request, ResponseStatus.OK);
                     response.setEntity(listing);
                     return response;
                 } else {
                     return responseFacotry.getResponse(request, ResponseStatus.FORBIDDEN);
                 }
             }
 
            // Do we have a conditional GET
            String lastModifiedString = request.getHeader("If-Modified-Since");
            if (lastModifiedString != null) {
                try {
                    Date lastModified = DateConverter.dateFromString(lastModifiedString);
                    if (file.lastModified() <= lastModified.getTime()) {
                        return responseFacotry.getResponse(request, ResponseStatus.NOT_MODIFIED);
                    }
                } catch (DateConverterException e) {
                    // do nothing, serve file anyway
                }
            }

            // Serve the file's content
             String contentType = MimeTypes.evaluate(MimeTypes.getExtension(file));
             if (contentType == null) {
                 contentType = "application/octet-stream";
             }
 
             Response response = responseFacotry.getResponse(request);
             response.setStatus(ResponseStatus.OK);
             response.setContentType(contentType);
             response.setContentLength(String.valueOf(file.length()));
             response.setLastModified(DateConverter.formatRfc1123(new Date(file.lastModified())));
 
             byte[] fileContent = new byte[(int) file.length()];
             new FileInputStream(file).read(fileContent);
             response.setEntity(fileContent);
 
             return response;
 
         } catch (IOException e) {
             return responseFacotry.getResponse(request, ResponseStatus.INTERNAL_SERVER_ERROR);
         }
     }
 
     /**
      * Generates a html page with a table displaying the contents of a folder.
      * 
      * @param uri
      *            Uri for which directory is displayed
      * @param targetDir
      *            Directory to display as a table
      * @return HTML message body to be used as response
      */
     public String getDirectoryListing(final String uri, final File targetDir) {
         String[] files = targetDir.list();
         String msg = "<html><head><title>Index of " + uri + "</title></head><body><h1>Index of "
                 + uri + "</h1><pre><hr />";
 
         if (uri.length() > 1) {
             String u = uri.substring(0, uri.length() - 1);
             int slash = u.lastIndexOf('/');
             if (slash >= 0 && slash < u.length()) {
                 msg += "<img src=\"/images/icons/back.gif\" alt=\"[DIR]\" width=\"20\""
                         + " height=\"22\"> <a href=\"" + uri.substring(0, slash + 1)
                         + "\">Parent Directory</a>\r\n";
             }
         }
 
         if (files != null) {
             for (int i = 0; i < files.length; ++i) {
                 File curFile = new File(targetDir, files[i]);
 
                 if (curFile.isDirectory()) {
                     files[i] += "/";
                     msg += "<img src=\"/images/icons/folder.gif\" alt=\"[DIR]\" width=\"20\""
                             + " height=\"22\"> ";
                 } else {
                     String mimetype = MimeTypes.evaluate(MimeTypes.getExtension(files[i]));
                     if (mimetype.startsWith("image")) {
                         msg += "<img src=\"/images/icons/image.gif\" alt=\"[DIR]\" width=\"20\""
                                 + " height=\"22\"> ";
                     } else {
                         msg += "<img src=\"/images/icons/text.gif\" alt=\"[DIR]\" width=\"20\""
                                 + " height=\"22\"> ";
                     }
                 }
                 msg += "<a href=\"" + files[i] + "\">" + files[i] + "</a>";
 
                 if (curFile.isFile()) {
                     long len = curFile.length();
                     msg += " &nbsp;";
                     if (len < 1024) {
                         msg += len;
                     } else if (len < 1024 * 1024) {
                         msg += len / 1024 + "." + (len % 1024 / 10 % 100) + "K";
                     } else {
                         msg += len / (1024 * 1024) + "." + len % (1024 * 1024) / 10 % 100 + "M";
                     }
                 }
                 msg += "\r\n";
             }
         }
         msg += "</pre><hr />" + Emma.VERSION;
         msg += "</body></html>";
 
         return msg;
     }
 }
 
