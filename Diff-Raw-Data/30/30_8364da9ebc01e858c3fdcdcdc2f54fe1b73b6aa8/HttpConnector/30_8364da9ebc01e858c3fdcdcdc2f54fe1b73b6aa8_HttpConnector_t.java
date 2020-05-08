 package eu.hack4europe.europeana4j;
 
 import android.util.Log;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 
 public final class HttpConnector {
 
     public String getURLContent(String url) throws IOException {
         Log.i("http", url);
 
        HttpClient client = getHttpClient();
         HttpGet httpGet = new HttpGet(url);
 
         try {
             HttpResponse response = client.execute(httpGet);
             HttpEntity entity = response.getEntity();
             StatusLine statusLine = response.getStatusLine();
 
             if (entity != null) {
                 try {
                     if (isOk(statusLine)) {
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                         try {
                             entity.writeTo(outputStream);
                             return outputStream.toString();
                         } finally {
                             outputStream.close();
                         }
                     } else {
                         return "";
                     }
                 } catch (RuntimeException e) {
                     Log.e("http", "failed request", e);
                     // Aborting request in case of exception
                     httpGet.abort();
                     throw e;
                 }
             }
         } finally {
             client.getConnectionManager().shutdown();
         }
 
         return "";
     }
 
     private boolean isOk(StatusLine statusLine) {
         return statusLine.getStatusCode() >= HttpStatus.SC_OK
                 && statusLine.getStatusCode() <= HttpStatus.SC_MULTI_STATUS;
     }
 
     public boolean writeURLContent(String url, OutputStream out, String requiredMime)
             throws IOException {
         Log.i("http", url + ":" + requiredMime);
 
         HttpClient client = this.getHttpClient();
         HttpGet httpGet = new HttpGet(url);
 
         try {
             HttpResponse response = client.execute(httpGet);
             HttpEntity entity = response.getEntity();
             StatusLine statusLine = response.getStatusLine();
 
             Header[] headers = response.getHeaders("Content-Type");
             String contentType = headers[0].getValue();
 
             if (isOk(statusLine) && contentType.contains(requiredMime)) {
                 InputStream content = entity.getContent();
 
                 // Copy input stream to output stream
                 byte[] b = new byte[4 * 1024];
                 int read;
                 while ((read = content.read(b)) != -1) {
                     out.write(b, 0, read);
                 }
 
                 content.close();
                 return true;
             } else {
                 return false;
             }
         } finally {
             client.getConnectionManager().shutdown();
         }
     }
 
 
     public boolean silentWriteURLContent(String url, OutputStream out, String mimeType) {
         try {
             return this.writeURLContent(url, out, mimeType);
         } catch (Exception e) {
             return false;
         }
     }
 
 
     public boolean checkURLContent(String url, String requiredMime) {
         OutputStream out = new OutputStream() {
             @Override
             public void write(int b) throws IOException {
             }
         };
 
         return this.silentWriteURLContent(url, out, requiredMime);
     }
 
     private HttpClient getHttpClient() {
        return new DefaultHttpClient();
     }
 
 }
