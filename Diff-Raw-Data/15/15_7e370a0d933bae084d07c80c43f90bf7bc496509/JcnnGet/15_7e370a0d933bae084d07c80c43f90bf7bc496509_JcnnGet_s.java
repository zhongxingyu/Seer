 package byps.http.client.jcnn;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.CookieManager;
 import java.net.HttpURLConnection;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import byps.BAsyncResult;
 import byps.BBufferJson;
 import byps.BException;
 import byps.BExceptionC;
 import byps.BWire;
 
 public class JcnnGet extends JcnnRequest {
 
   private Log log = LogFactory.getLog(JcnnGet.class);
   private final BAsyncResult<ByteBuffer> asyncResult;
 
   protected JcnnGet(String url, BAsyncResult<ByteBuffer> asyncResult, CookieManager cookieManager) {
     super(url, cookieManager);
     this.asyncResult = asyncResult;
   }
 
   @Override
   public void run() {
     HttpURLConnection c = null;
     InputStream is = null;
     ByteBuffer returnBuffer = null;
     Throwable returnException = null;
     int statusCode = BExceptionC.CONNECTION_TO_SERVER_FAILED;
 
     try {
       c = createConnection(url);
 
       c.setDoInput(true);
       c.setDoOutput(false);
 
       c.setRequestMethod("GET");
       c.setRequestProperty("Accept", "application/json, application/byps, text/plain, text/html");
       c.setRequestProperty("Accept-Encoding", "gzip");
 
       statusCode = getResponseCode(c);
 
       if (statusCode != HttpURLConnection.HTTP_OK) {
         throw new IOException("HTTP status " + statusCode);
       }
 
       saveSession(this);
 
       is = c.getInputStream();
 
       String enc = c.getHeaderField("Content-Encoding");
       boolean gzip = enc != null && enc.equals("gzip");
 
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
 
       try {
         if (c != null) {
           is = c.getErrorStream();
           BWire.bufferFromStream(is, false);
           is = null;
         }
       }
       catch (IOException ignored) {
       }
 
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
       
       asyncResult.setAsyncResult(returnBuffer, returnException);
       done();
     }
 
     
   }
 
 }
