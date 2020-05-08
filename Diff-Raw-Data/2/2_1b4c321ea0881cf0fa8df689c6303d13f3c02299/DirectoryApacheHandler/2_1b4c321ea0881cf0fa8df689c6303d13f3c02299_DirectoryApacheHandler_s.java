 package com.purplefrog.apachehttpcliches;
 
 import java.io.*;
 import java.net.*;
 
 import com.purplefrog.httpcliches.*;
 import org.apache.http.*;
 import org.apache.http.entity.*;
 import org.apache.http.message.*;
 import org.apache.http.protocol.*;
 import org.apache.log4j.*;
 
 /**
 * <p>Copyright (C) 2012 Robert Forsman, Purple Frog Software
 * $Author thoth $
 * $Date 3/6/12 $
 */
 public class DirectoryApacheHandler
     implements HttpRequestHandler
 {
     private static final Logger logger = Logger.getLogger(DirectoryApacheHandler.class);
 
     public final static BasicHeader ACCEPT_RANGES_BYTES = new BasicHeader("Accept-Ranges", "bytes");
 
 
     public final String prefix;
     public final File contentDir;
 
     public DirectoryApacheHandler(File contentDir, String webPrefix)
     {
         this.contentDir = contentDir;
         prefix = webPrefix;
     }
 
     public void handle(HttpRequest request, HttpResponse response, HttpContext context)
         throws HttpException, IOException
     {
         handle(request, response, context, null);
     }
 
     public void handle(HttpRequest request, HttpResponse response, HttpContext context, TransferCallback callback)
         throws HttpException, IOException
     {
         final RequestLine rl = request.getRequestLine();
         final String method = rl.getMethod();
 
         logger.debug(method+" "+rl.getUri());
 
         EntityAndHeaders rval;
         try {
            URI uri = new URI(null, null, rl.getUri(), null);
             if (uri.getPath().startsWith(prefix)) {
                 URI suffix = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath().substring(prefix.length()), uri.getQuery(), uri.getFragment());
 
                 if ("GET".equals(method)) {
                     rval = handleGET(context, suffix, ApacheHTTPCliches.getRangeHeader(request), callback);
                 } else {
                     rval = EntityAndHeaders.plainTextPayload(501, "501 Not Implemented");
                 }
             } else {
                 rval = EntityAndHeaders.plainTextPayload(404, "404 not found");
             }
 
         } catch (Throwable e) {
             logger.warn("handler malfunctioned", e);
             rval = EntityAndHeaders.plainTextPayload(500, "I am full of explosions!\n" + Util2.stringStackTrace(e));
         }
         rval.apply(response);
     }
 
     private EntityAndHeaders handleGET(HttpContext context, URI suffix_, String range, TransferCallback callback)
     {
         String suffix = suffix_.getPath();
         if ("".equals(suffix)) {
             String u = ApacheHTTPCliches.redirectPath(context, prefix+"/");
             return new EntityAndHeaders.Redirect(u, "redirect to \n"+u);
         }
 
         suffix = ApacheHTTPCliches.crushDotDot(suffix);
         if (suffix==null) {
             return EntityAndHeaders.plainTextPayload(404, "Not Found");
         }
 
         File target = new File(contentDir, suffix);
 
         logger.debug("mapped to "+target);
 
         if (target.isDirectory()) {
             String u = ApacheHTTPCliches.redirectPath(context, (prefix+"/" +suffix+"/").replaceAll("/+","/")
                 + indexThingyFor(target));
             return new EntityAndHeaders.Redirect(u, "redirect to \n"+u);
         }
 
         if (target.exists()) {
 
             ContentType mime = ApacheHTTPCliches.mimeTypeFor(target);
             if (range==null) {
                 PartialFileEntity entity = new PartialFileEntity(target, new ByteRangeSpec(0, target.length()), mime, callback);
                 return new EntityAndHeaders(200, entity, ACCEPT_RANGES_BYTES);
             } else
                 return handleSubset2(target, mime, range, callback);
 
 
         } else {
             return EntityAndHeaders.plainTextPayload(404, "Not Found");
         }
     }
 
     public static EntityAndHeaders handleSubset2(File f, ContentType mime, String rangeHeader, TransferCallback callback)
     {
         long totalFileLength = f.length();
 
         ByteRangeSpec brs = ByteRangeSpec.parseRange(rangeHeader, totalFileLength);
 
         if (brs.end == null)
             brs.end = totalFileLength-1;
 
         PartialFileEntity en = new PartialFileEntity(f, brs, mime, callback);
         BasicHeader contentRange = new BasicHeader("Content-Range", "bytes " + brs.start + "-" + brs.end + "/" + totalFileLength);
         return new EntityAndHeaders(206, en, contentRange, ACCEPT_RANGES_BYTES);
     }
 
     public static EntityAndHeaders handleSubset(File f, String contentType, String rangeHeader, TransferCallback callback)
     {
         long totalFileLength = f.length();
 
         ByteRangeSpec brs = ByteRangeSpec.parseRange(rangeHeader, totalFileLength);
 
         if (brs.end == null)
             brs.end = totalFileLength-1;
 
         PartialFileEntity en = new PartialFileEntity(f, brs, contentType, callback);
         BasicHeader contentRange = new BasicHeader("Content-Range", "bytes " + brs.start + "-" + brs.end + "/" + totalFileLength);
         return new EntityAndHeaders(206, en, contentRange, ACCEPT_RANGES_BYTES);
     }
 
     public String indexThingyFor(File target)
     {
         return "index.html";
     }
 
 }
