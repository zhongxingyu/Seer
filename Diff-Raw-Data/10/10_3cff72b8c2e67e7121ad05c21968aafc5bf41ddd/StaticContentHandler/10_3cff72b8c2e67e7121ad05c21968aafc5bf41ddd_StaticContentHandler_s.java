 /*
  * Copyright (c) 2011 Sergey Prilukin
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package jstreamserver.http;
 
 import anhttpserver.HttpRequestContext;
 import jstreamserver.utils.HttpUtils;
 import org.apache.commons.io.FilenameUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * This handler serves static content like CSS, JavaScript and images
  *
  * @author Sergey Prilukin
  */
 public final class StaticContentHandler extends BaseHandler {
 
     public static final List<String> mimeTypesToCompress = new ArrayList<String>();
     static {
         mimeTypesToCompress.add("text/css");
         mimeTypesToCompress.add("application/x-javascript");
     }
 
     public StaticContentHandler() {
         super();
     }
 
     private long getResourceLastModifiedTime(String path) {
         File file = new File(getConfig().getResourcesFolder() + "/" + path);
         return file.exists() ? (file.lastModified() / 1000) * 1000 : Long.MAX_VALUE;
     }
 
     private boolean isResourceModified(long resourceLastModifiedDate, HttpRequestContext httpRequestContext) {
        if (httpRequestContext.getRequestHeaders().containsKey("If-Modified-Since")) {
             try {
                Date browserCacheModifiedDate = HTTP_HEADER_DATE_FORMAT.parse(httpRequestContext.getRequestHeaders().get("If-Modified-Since").get(0));

                 return resourceLastModifiedDate - browserCacheModifiedDate.getTime() > 0;
             } catch (ParseException e) {
                throw new RuntimeException(e);
             }
         } else {
             return true;
         }
     }
 
     @Override
     public InputStream getResponseInternal(HttpRequestContext httpRequestContext) throws IOException {
         String path = httpRequestContext.getRequestURI().getPath().replaceFirst("/", "");
 
         long resourceLastModifiedTime = getResourceLastModifiedTime(path);
         if (isResourceModified(resourceLastModifiedTime, httpRequestContext)) {
             String extension = FilenameUtils.getExtension(path);
             String type = getMimeProperties().getProperty(extension);
 
             InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
             if (resourceAsStream == null) {
                 return rendeResourceNotFound(path, httpRequestContext);
             }
 
             if (mimeTypesToCompress.contains(type)) {
                 setResponseHeader(HttpUtils.CONTENT_ENCODING_HEADER, HttpUtils.GZIP_ENCODING, httpRequestContext);
                 resourceAsStream = compressInputStream(resourceAsStream);
             }
 
             setContentType(type, httpRequestContext);
             setResponseHeader("Last-Modified", HTTP_HEADER_DATE_FORMAT.format(new Date(resourceLastModifiedTime)), httpRequestContext);
             setResponseHeader("Expires", HTTP_HEADER_DATE_FORMAT.format(new Date(resourceLastModifiedTime)), httpRequestContext);
             setResponseHeader("Cache-Control", "private, max-age=31536000", httpRequestContext);
 
             return resourceAsStream;
         } else {
             setResponseCode(HttpURLConnection.HTTP_NOT_MODIFIED, httpRequestContext);
             setResponseSize(0, httpRequestContext);
             return null;
         }
     }
 }
