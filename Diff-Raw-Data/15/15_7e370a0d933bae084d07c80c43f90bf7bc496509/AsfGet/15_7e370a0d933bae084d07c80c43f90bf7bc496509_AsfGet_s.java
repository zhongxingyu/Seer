 package byps.http.client.asf;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.CloseableHttpClient;
 
 import byps.BAsyncResult;
 import byps.BBufferJson;
 import byps.BException;
 import byps.BExceptionC;
 import byps.BWire;
 
 public class AsfGet extends AsfRequest {
 
   private Log log = LogFactory.getLog(AsfGet.class);
   private final BAsyncResult<ByteBuffer> asyncResult;
   
   protected AsfGet(String url, BAsyncResult<ByteBuffer> asyncResult, CloseableHttpClient httpClient) {
     super(url, httpClient);
     this.asyncResult = asyncResult;
   }
 
   @Override
   public void run() {
     
     request = new HttpGet(url);
     applyTimeout();
     
     CloseableHttpResponse response = null; 
     InputStream is = null;
     ByteBuffer returnBuffer = null;
     Throwable returnException = null;
     int statusCode = BExceptionC.CONNECTION_TO_SERVER_FAILED;
     
     try {
 
       request.setHeader("Accept", "application/json, application/byps, text/plain, text/html");
       request.setHeader("Accept-Encoding", "gzip");
 
       response = httpClient.execute(request);
 
       statusCode = response.getStatusLine().getStatusCode();
       if (statusCode != HttpURLConnection.HTTP_OK) {
         throw new IOException("HTTP status " + statusCode);
       }
 
       HttpEntity entity = response.getEntity();
 
       boolean gzip = false;
       {
         Header header = entity.getContentEncoding();
         if (header != null) {
           String enc = header.getValue();
           gzip = enc != null && enc.equals("gzip");
         }
       }
 
       is = response.getEntity().getContent();
 
       if (log.isDebugEnabled()) log.debug("read stream");
       ByteBuffer obuf = BWire.bufferFromStream(is, gzip);
       if (log.isDebugEnabled()) {
         log.debug("received #bytes=" + obuf.remaining());
         obuf.mark();
         BBufferJson bbuf = new BBufferJson(obuf);
         log.debug(bbuf.toDetailString());
         obuf.reset();
       }
 
       is = null;
       returnBuffer = obuf;
     }
     catch (SocketException e) {
       if (log.isDebugEnabled()) log.debug("received exception=" + e);
      returnException = new BException(BExceptionC.CONNECTION_TO_SERVER_FAILED, "Socket error", e);
     }
     catch (Throwable e) {
       if (log.isDebugEnabled()) log.debug("received exception=" + e);
      returnException = new BException(statusCode, "Send message failed", e);
     }
     finally {
       if (is != null) {
         try {
           is.close();
         }
         catch (IOException ignored) {
         }
       }
       
       if (response != null) {
         try {
           response.close();
         }
         catch (IOException e) {
         }
       }
       
       asyncResult.setAsyncResult(returnBuffer, returnException);
       done();
     }
 
     
   }
 
 
 }
