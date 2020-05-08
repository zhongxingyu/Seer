 package byps.http;
 
 /* USE THIS FILE ACCORDING TO THE COPYRIGHT RULES IN LICENSE.TXT WHICH IS PART OF THE SOURCE CODE PACKAGE */
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import byps.BAsyncResult;
 import byps.BBufferJson;
 import byps.BContentStream;
 import byps.BContentStreamAsyncCallback;
 import byps.BException;
 import byps.BExceptionC;
 import byps.BMessage;
 import byps.BMessageHeader;
 import byps.BNegotiate;
 import byps.BStreamRequest;
 import byps.BSyncResult;
 import byps.BTestAdapter;
 import byps.BWire;
 import byps.http.client.HHttpClient;
 import byps.http.client.HHttpClientFactory;
 import byps.http.client.HHttpRequest;
 import byps.http.client.jcnn.JcnnClientFactory;
 
 /**
  * BWire implementation for HTTP.
  * By default, this class uses HttpURLConnection to communicate with the server.  
  * Set 
  * <code>System.setProperty(HWireClient.SYSTEM_PROPERTY_HTTP_CLIENT_FACTORY, "byps.http.client.asf.AsfClientFactory");</code>
  * in order to send messages by Apache HTTP Components 4.3.2.
  */
 public class HWireClient extends BWire {
 
   /**
    * Full qualified name of the HTTP client factory class. The constructor reads
    * this value by System.getProperty() and instantiates an object in order to
    * create a HTTP client object and other necessary objects. If this system
    * property is not set, the factory
    * {@link byps.http.client.jcnn.JcnnClientFactory} is used.
    * The value of this property is "byps.http.client.factory".
    */
   public final static String SYSTEM_PROPERTY_HTTP_CLIENT_FACTORY = "byps.http.client.factory";
 
   protected final static long MESSAGEID_CANCEL_ALL_REQUESTS = -1;
   protected final static long MESSAGEID_DISCONNECT = -2;
 
   protected final String surl;
   protected final static int CHUNK_SIZE = 10 * 1000;
   protected final static int MAX_STREAM_PART_SIZE = 1000 * CHUNK_SIZE; // should
                                                                        // be a
                                                                        // multiple
                                                                        // of
                                                                        // CHUNK_SIZE
   protected final Log log = LogFactory.getLog(HWireClient.class);
   protected final ConcurrentHashMap<RequestToCancel, Boolean> openRequestsToCancel = new ConcurrentHashMap<RequestToCancel, Boolean>();
   // protected final Map<RequestToCancel, Boolean> openRequestsToCancel =
   // Collections.synchronizedMap(new HashMap<RequestToCancel, Boolean>());
   protected final Executor threadPool;
   protected final boolean isMyThreadPool;
   protected final HTestAdapter testAdapter;
   protected volatile boolean isDone;
   protected final HHttpClient httpClient;
 
   protected Statistics stats = null;
 
   protected int timeoutSecondsClient;
 
   private enum ERequestDirection {
     FORWARD, REVERSE
   };
 
   /**
    * Initializes a new client-side HTTP communication.
    * 
    * @param url
    *          URL to server.
    * @param flags
    *          A combination of BWire.FLAG_* values.
    * @param timeoutSeconds
    *          Read timeout in seconds. A timeout of zero is interpreted as an
    *          infinite timeout.
    * @param threadPool
    *          Optional: A thread pool. If null, a thread pool is internally
    *          created.
    * @see BWire#FLAG_GZIP
    */
   public HWireClient(String url, int flags, int timeoutSeconds, Executor threadPool) {
     super(flags);
     
     if (log.isDebugEnabled()) log.debug("HWireClient(" + url + ", flags=" + flags + ", timeoutSeconds=" + timeoutSeconds + ", threadPool=" + threadPool);
 
     if (url == null || url.length() == 0) throw new IllegalStateException("Missing URL");
 
     this.surl = url;
     this.timeoutSecondsClient = timeoutSeconds;
 
     this.isMyThreadPool = threadPool == null;
     if (threadPool == null) {
       threadPool = Executors.newCachedThreadPool();
     }
     this.threadPool = threadPool;
 
     this.testAdapter = new HTestAdapter(this);
 
     // Create HTTP client object by a HHttpClientFactory. ----------------
 
     // The class name of the HHttpClientFactory is taken from the
     // System.properties.
     String factName = System.getProperty(SYSTEM_PROPERTY_HTTP_CLIENT_FACTORY);
     if (log.isDebugEnabled()) log.debug("factory from system.properties[" + SYSTEM_PROPERTY_HTTP_CLIENT_FACTORY + "]=" + factName);
     if (factName == null || factName.length() == 0) factName = JcnnClientFactory.class.getName();
 
     // Create a client factory object
     HHttpClientFactory fact = null;
     try {
       if (log.isDebugEnabled()) log.debug("instantiate factName=" + factName);
       Class<?> clazz = Class.forName(factName);
       fact = (HHttpClientFactory) clazz.newInstance();
       if (log.isDebugEnabled()) log.debug("fact=" + fact);
     }
     catch (Exception e) {
       if (log.isDebugEnabled()) log.debug("failed to create factory, factName=" + factName, e);
       throw new IllegalStateException(e);
     }
 
     if (log.isDebugEnabled()) log.debug("createHttpClient...");
     this.httpClient = fact.createHttpClient(url);
     if (log.isDebugEnabled()) log.debug("createHttpClient OK, " + this.httpClient);
 
     if (log.isDebugEnabled()) log.debug(")HWireClient");
   }
 
   protected class AsyncResultAfterAllRequests implements BAsyncResult<BMessage> {
     final BAsyncResult<BMessage> innerResult;
     final long messageId;
     int nbOfOutstandingResults;
     BMessage result;
     Throwable ex;
 
     public AsyncResultAfterAllRequests(long messageId, BAsyncResult<BMessage> asyncResult, final int nbOfRequests) {
       this.innerResult = asyncResult;
       this.messageId = messageId;
       this.nbOfOutstandingResults = nbOfRequests;
     }
 
     @Override
     public void setAsyncResult(BMessage msg, Throwable ex) {
       if (log.isDebugEnabled()) log.debug("setAsyncResult(msg=" + msg + ", ex=" + ex);
 
       boolean cancelMessage = false;
       boolean isLastResult = false;
       BMessage innerMsg = null;
       Throwable innerEx = null;
 
       synchronized (this) {
 
         isLastResult = --nbOfOutstandingResults == 0;
         if (log.isDebugEnabled()) log.debug("isLastResult=" + isLastResult);
 
         if (ex != null) {
           cancelMessage = this.ex == null;
           if (cancelMessage) this.ex = ex;
           if (log.isDebugEnabled()) log.debug("cancelMessage=" + cancelMessage);
         }
 
         if (msg != null && msg.buf != null) {
           if (log.isDebugEnabled()) log.debug("set result=" + msg);
           this.result = msg;
         }
         else {
           // Stream result OK
         }
 
         if (isLastResult) {
           innerMsg = this.result;
           innerEx = this.ex;
         }
 
       }
 
       if (isLastResult) {
         if (log.isDebugEnabled()) log.debug("innerResult.setAsyncResult(result=" + innerMsg + ", ex=" + innerEx);
         innerResult.setAsyncResult(innerMsg, innerEx);
       }
 
       if (cancelMessage && !isLastResult) {
         sendCancelMessage(messageId);
       }
 
       if (log.isDebugEnabled()) log.debug(")setAsyncResult");
     }
 
   }
 
   @Override
   public synchronized void send(final BMessage msg, final BAsyncResult<BMessage> asyncResult) {
     // internalSendMessageAndStreams(msg, asyncResult,
     // this.timeoutSecondsClient);
     internalSendMessageAndStreams(msg, asyncResult);
   }
 
   @Override
   public void sendR(BMessage msg, BAsyncResult<BMessage> asyncResult) {
     // internalSendMessageAndStreams(msg, asyncResult, 0);
     internalSendMessageAndStreams(msg, asyncResult);
   }
 
   protected void executeRequest(RequestToCancel r) throws BException {
     if (log.isDebugEnabled()) log.debug("executeRequest(" + r);
 
     if (isDone) {
       BException e = new BException(BExceptionC.CANCELLED, "HTTP Client already disconnected.");
       throw e;
     }
 
     for (int retry = 0; retry < 10 && !isDone; retry++) {
       try {
         threadPool.execute(r);
         if (log.isDebugEnabled()) log.debug(")executeRequest");
         return;
       }
       catch (RejectedExecutionException e) {
         if (log.isDebugEnabled()) log.debug("rejected execution, thread pool exausted? retry=" + retry);
         int retryAfterMillis = rand.nextInt(100);
         try {
           Thread.sleep((long) retryAfterMillis);
         }
         catch (InterruptedException e1) {
           break;
         }
       }
     }
 
    BException e = new BException(BExceptionC.TOO_MANY_REQUESTS, "Failed to send method or stream because too many requests are currently active.");
     throw e;
   }
 
   @Override
   public void putStreams(List<BStreamRequest> streamRequests, BAsyncResult<BMessage> asyncResult) {
     try {
       for (BStreamRequest stream : streamRequests) {
         RequestToCancel req = createRequestForPutStream(stream, asyncResult);
         executeRequest(req);
       }
     }
     catch (BException e) {
       asyncResult.setAsyncResult(null, e);
     }
   }
 
   private synchronized void internalSendMessageAndStreams(final BMessage msg, final BAsyncResult<BMessage> asyncResult) {
     if (log.isDebugEnabled()) log.debug("send(" + msg + ", asyncResult=" + asyncResult);
 
     try {
 
       // If the BMessage contains streams, the given asyncResult is wrapped into a
       // BAsyncRequest object that sets the asynchronous result after the
       // message and all streams
       // have been sent.
       BAsyncResult<BMessage> outerResult = asyncResult;
       if (msg.streams != null && msg.streams.size() != 0) {
         if (log.isDebugEnabled()) log.debug("wrap asyncResult");
         outerResult = new AsyncResultAfterAllRequests(msg.header.messageId, asyncResult, msg.streams.size() + 1);
       }
       
       // Eval timeout for message.
       // If the message is sent with streams, it does not return before all streams are sent.
       // If large streams are uploaded, the message could cause a timeout error on the client side. 
       // In order to prevent this situation, set an infinite timeout (=0).
       boolean hasStreams = msg.streams != null && msg.streams.size() != 0;
       int timeoutSecondsRequest = hasStreams ? 0 : this.timeoutSecondsClient; 
 
       // Create RequestToCancel for message
       RequestToCancel req = createRequestForMessage(msg, outerResult, timeoutSecondsRequest);
       executeRequest(req);
 
       // Create RequestToCancel objects for each stream.
       if (hasStreams) {
         putStreams(msg.streams, outerResult);
       }
 
     }
     catch (BException e) {
       asyncResult.setAsyncResult(null, e);
     }
 
     if (log.isDebugEnabled()) log.debug(")send");
   }
 
   @SuppressWarnings("unused")
   private synchronized void internalSendStreamsThenMessage(final BMessage msg, final BAsyncResult<BMessage> asyncResult) {
     if (log.isDebugEnabled()) log.debug("internalSendStreamsThenMessage(" + msg + ", asyncResult=" + asyncResult);
 
     // Convert the BMessage into RequestToCancel objects.
     // One RequestToCancel is created for msg.buf.
     // For each stream in msg.streams further RequestToCancel objects are
     // created.
 
     int nbOfStreams = msg.streams != null ? msg.streams.size() : 0;
     final ArrayList<RequestToCancel> requests = new ArrayList<RequestToCancel>(nbOfStreams != 0 ? nbOfStreams : 1);
 
     // Does the message contains streams?
     if (nbOfStreams != 0) {
 
       // Send the streams first and then send the message.
       // If the message is sent before the streams,
       // a timeout happens for the message if it takes
       // more than timeoutMillisRequest to send the streams.
 
       // Create an BAsyncResult that sends the message on setAsyncResult
       BAsyncResult<BMessage> asyncSendMessage = new BAsyncResult<BMessage>() {
         @Override
         public void setAsyncResult(BMessage result, Throwable exception) {
           if (exception != null) {
             asyncResult.setAsyncResult(null, exception);
           }
           else {
             // Send the message
             try {
               RequestToCancel messageRequest = createRequestForMessage(msg, asyncResult, timeoutSecondsClient);
               executeRequest(messageRequest);
             }
             catch (BException e) {
               asyncResult.setAsyncResult(null, e);
             }
           }
         }
       };
 
       // Create requests for each stream
       BAsyncResult<BMessage> outerResult = new AsyncResultAfterAllRequests(msg.header.messageId, asyncSendMessage, nbOfStreams);
       for (BStreamRequest stream : msg.streams) {
         RequestToCancel streamRequest = createRequestForPutStream(stream, outerResult);
         requests.add(streamRequest);
       }
 
     }
     else {
       // Create RequestToCancel for msg.buf
       RequestToCancel req = createRequestForMessage(msg, asyncResult, timeoutSecondsClient);
       requests.add(req);
     }
 
     // Execute the RequestToCancel objects in the thread pool
     if (log.isDebugEnabled()) log.debug("put requests into thread pool");
     try {
       for (RequestToCancel r : requests) {
         executeRequest(r);
       }
     }
     catch (BException e) {
       asyncResult.setAsyncResult(null, e);
     }
 
     if (log.isDebugEnabled()) log.debug(")internalSendStreamsThenMessage");
   }
 
   protected RequestToCancel createRequestForMessage(BMessage msg, BAsyncResult<BMessage> asyncResult, int timeoutSecondsRequest) {
     if (log.isDebugEnabled()) log.debug("createRequestForMessage(" + msg);
     ByteBuffer requestDataBuffer = msg.buf;
 
     if (log.isDebugEnabled()) {
       requestDataBuffer.mark();
       BBufferJson bbuf = new BBufferJson(requestDataBuffer);
       log.debug(bbuf.toDetailString());
       requestDataBuffer.reset();
     }
 
     final RequestToCancel requestToCancel = new RequestToCancel(msg.header.messageId, 0L, 0L, asyncResult);
 
     final boolean isNegotiate = BNegotiate.isNegotiateMessage(requestDataBuffer);
     final boolean isJson = isNegotiate || BMessageHeader.detectProtocol(requestDataBuffer) == BMessageHeader.MAGIC_JSON;
     if (log.isDebugEnabled()) log.debug("isJson=" + isJson);
 
     try {
       String destUrl = surl;
 
       // Negotiate?
       if (isNegotiate) {
 
         // Send a GET request and pass the negotiate string as parameter
 
         String negoStr = new String(requestDataBuffer.array(), requestDataBuffer.position(), requestDataBuffer.limit(), "UTF-8");
         negoStr = URLEncoder.encode(negoStr, "UTF-8");
 
         destUrl = makeUrl(getServletPathForNegotiationAndAuthentication(), new String[] { "negotiate", negoStr });
       }
 
       // Reverse request (long-poll) ?
       else if ((msg.header.flags & BMessageHeader.FLAG_RESPONSE) != 0) {
         destUrl = makeUrl(getServletPathForReverseRequest(), null);
         timeoutSecondsRequest = 0; // timeout controlled by server, 10min by
                                    // default.
       }
 
       if (log.isDebugEnabled()) log.debug("open connection, url=" + destUrl);
       final HHttpRequest httpRequest = isNegotiate ? httpClient.get(destUrl, requestToCancel) : httpClient.post(destUrl, requestDataBuffer, requestToCancel);
 
       httpRequest.setTimeouts(timeoutSecondsClient, timeoutSecondsRequest);
       
       requestToCancel.setHttpRequest(httpRequest);
 
       addRequest(requestToCancel);
     }
     catch (Throwable e) {
       if (log.isDebugEnabled()) log.debug("received Throwable: " + e);
       BException bex = new BException(BExceptionC.IOERROR, "IO error", e);
       asyncResult.setAsyncResult(null, bex);
     }
 
     if (log.isDebugEnabled()) log.debug(")createRequestForMessage=" + requestToCancel);
     return requestToCancel;
   }
 
   protected RequestToCancel createRequestForPutStream(BStreamRequest streamRequest, BAsyncResult<BMessage> asyncResult) {
     if (log.isDebugEnabled()) log.debug("createRequestForPutStream(" + streamRequest);
 
     final long messageId = streamRequest.messageId;
     final long streamId = streamRequest.streamId;
     StringBuilder destUrl = new StringBuilder(surl);
     destUrl.append("?messageid=").append(messageId).append("&streamid=").append(streamId);
 
     final RequestToCancel requestToCancel = new RequestToCancel(messageId, streamId, 0L, asyncResult);
     final HHttpRequest httpRequest = httpClient.putStream(destUrl.toString(), streamRequest.strm, requestToCancel);
 
     requestToCancel.setHttpRequest(httpRequest);
     addRequest(requestToCancel);
 
     if (log.isDebugEnabled()) log.debug(")createRequestForPutStream=" + requestToCancel);
     return requestToCancel;
   }
 
   protected RequestToCancel createRequestForGetStream(long messageId, long streamId, final BAsyncResult<BContentStream> asyncResult) {
     if (log.isDebugEnabled()) log.debug("createRequestForPutStream(messageId=" + messageId + ", streamId=" + streamId);
 
     StringBuilder destUrl = new StringBuilder(surl);
     destUrl.append("?messageid=").append(messageId).append("&streamid=").append(streamId);
 
     final RequestToCancel requestToCancel = new RequestToCancel(messageId, streamId, 0L, null);
     final HHttpRequest httpRequest = httpClient.getStream(destUrl.toString(), new BAsyncResult<BContentStream>() {
       public void setAsyncResult(BContentStream stream, Throwable ex) {
         asyncResult.setAsyncResult(stream, ex);
         requestToCancel.setAsyncResult(null, ex);
       }
     });
 
     requestToCancel.setHttpRequest(httpRequest);
     addRequest(requestToCancel);
 
     if (log.isDebugEnabled()) log.debug(")createRequestForPutStream=" + requestToCancel);
     return requestToCancel;
   }
 
   protected RequestToCancel createRequestForCancelMessage(long messageId) {
     final RequestToCancel requestToCancel = new RequestToCancel(0L, 0L, messageId, new BAsyncResult<BMessage>() {
       public void setAsyncResult(BMessage msg, Throwable ex) {
       }
     });
 
     String destUrl = surl + "?messageid=" + messageId + "&cancel=1";
     final HHttpRequest httpRequest = httpClient.get(destUrl, requestToCancel);
 
     requestToCancel.setHttpRequest(httpRequest);
 
     addRequest(requestToCancel);
     return requestToCancel;
   }
 
   protected class RequestToCancel implements Runnable, BAsyncResult<ByteBuffer>, Comparable<RequestToCancel> {
 
     final ERequestDirection requestDirection;
     final long messageId;
     final long streamId;
     final ByteBuffer buf;
     final BStreamRequest streamRequest;
     final long cancelMessageId;
     final BAsyncResult<BMessage> asyncResult;
     final AtomicBoolean isOpen = new AtomicBoolean(true);
     final int timeoutSecondsRequest;
     HHttpRequest httpRequest;
 
     protected RequestToCancel(ERequestDirection requestDirection, long messageId, ByteBuffer buf, BStreamRequest streamRequest, long streamId, long cancelMessageId, int timeoutSeconds,
         BAsyncResult<BMessage> asyncResult) {
       this.requestDirection = requestDirection;
       this.messageId = messageId;
       this.streamId = streamId;
       this.buf = buf;
       this.streamRequest = streamRequest;
       this.cancelMessageId = cancelMessageId;
       this.timeoutSecondsRequest = timeoutSeconds;
       this.asyncResult = asyncResult;
       this.httpRequest = null;
     }
 
     protected RequestToCancel(long messageId, long streamId, long cancelMessageId, BAsyncResult<BMessage> asyncResult) {
       this.requestDirection = ERequestDirection.FORWARD;
       this.messageId = messageId;
       this.streamId = streamId;
       this.buf = null;
       this.streamRequest = null;
       this.cancelMessageId = cancelMessageId;
       this.timeoutSecondsRequest = 0;
       this.asyncResult = asyncResult;
     }
 
     public void setHttpRequest(HHttpRequest httpRequest) {
       this.httpRequest = httpRequest;
     }
 
     @Override
     public void run() {
       httpRequest.run();
     }
 
     public void setAsyncResult(ByteBuffer buf, Throwable e) {
       if (log.isDebugEnabled()) log.debug("setAsyncResult" + this + "(");
 
       if (isOpen.getAndSet(false) && asyncResult != null) {
         if (e == null && buf != null && buf.remaining() != 0) {
           BMessageHeader header = new BMessageHeader();
           try {
             if (BNegotiate.isNegotiateMessage(buf)) {
               header.messageId = messageId;
             }
             else {
               header.read(buf);
             }
             BMessage msg = new BMessage(header, buf, null);
             asyncResult.setAsyncResult(msg, null);
           }
           catch (BException ex) {
             asyncResult.setAsyncResult(null, ex);
           }
         }
         else {
           asyncResult.setAsyncResult(null, e);
         }
       }
 
       // Remove request from HWireClient's internal map of requests.
       HWireClient.this.removeRequest(this);
 
       if (log.isDebugEnabled()) log.debug(")setAsyncResult");
     }
 
     public void cancel() {
       if (log.isDebugEnabled()) log.debug("cancel" + this + "(");
       httpRequest.cancel();
       if (log.isDebugEnabled()) log.debug(")cancel");
     }
 
     @Override
     public int compareTo(RequestToCancel o) {
       if (messageId < o.messageId) return -1;
       if (messageId > o.messageId) return 1;
       if (streamId < o.streamId) return -1;
       if (streamId > o.streamId) return 1;
       if (cancelMessageId < o.cancelMessageId) return -1;
       if (cancelMessageId > o.cancelMessageId) return 1;
       return 0;
     }
 
     @Override
     public String toString() {
       StringBuilder sbuf = new StringBuilder();
       sbuf.append("[").append(messageId);
       if (buf != null) sbuf.append(",buf=").append(buf);
       if (streamRequest != null) sbuf.append(",streamId=").append(streamRequest.streamId);
       if (cancelMessageId != 0) sbuf.append(",cancelMessageId=").append(cancelMessageId);
       sbuf.append(",httpRequest=").append(System.identityHashCode(httpRequest));
       sbuf.append("]");
       return sbuf.toString();
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + getOuterType().hashCode();
       result = prime * result + (int) (cancelMessageId ^ (cancelMessageId >>> 32));
       result = prime * result + (int) (messageId ^ (messageId >>> 32));
       result = prime * result + (int) (streamId ^ (streamId >>> 32));
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj) return true;
       if (obj == null) return false;
       if (getClass() != obj.getClass()) return false;
       RequestToCancel other = (RequestToCancel) obj;
       if (!getOuterType().equals(other.getOuterType())) return false;
       if (cancelMessageId != other.cancelMessageId) return false;
       if (messageId != other.messageId) return false;
       if (streamId != other.streamId) return false;
       return true;
     }
 
     private HWireClient getOuterType() {
       return HWireClient.this;
     }
   }
 
   protected class MyInputStream extends InputStreamWrapper implements BAsyncResult<BContentStream> {
 
     // Implementierung ist so kompliziert, damit die HTTP-Verbindung abgebrochen
     // werden kann.
     // Die Exception durch RequestToCancel.cancle() wird nur in openStream
     // ausgewertet. Aber da
     // msste das cancel schon whrend des openStream kommen.
 
     volatile RequestToCancel request;
     volatile HttpURLConnection conn;
     volatile Throwable ex;
     protected final Log log = LogFactory.getLog(MyInputStream.class);
 
     public MyInputStream(long messageId, long streamId) {
       super(messageId, streamId);
     }
 
     @Override
     public void setAsyncResult(BContentStream stream, Throwable e) {
       this.ex = e;
       if (e == null) {
         try {
           this.innerStream = stream;
           this.contentType = stream.getContentType();
           this.contentLength = stream.getContentLength();
         }
         catch (IOException e1) {
           this.ex = e1;
         }
       }
     }
 
     void throwExceptionIf() throws IOException {
       Throwable e = ex;
       if (e == null) return;
       if (e instanceof IOException) {
         throw (IOException) e;
       }
       else {
         throw new BException(BExceptionC.CANCELLED, "", e);
       }
     }
 
     @Override
     public InputStream ensureStream() throws IOException {
       throwExceptionIf();
       return super.ensureStream();
     }
 
     @Override
     protected InputStream openStream() throws IOException {
       if (log.isDebugEnabled()) log.debug("openStream(");
       InputStream is = null;
 
       final BContentStreamAsyncCallback streamCallback = getAsyncCallback();
       if (streamCallback != null) {
 
         BAsyncResult<BContentStream> asyncResult = new BAsyncResult<BContentStream>() {
 
           public void setAsyncResult(BContentStream stream, Throwable ex) {
             if (ex == null) {
               try {
                 streamCallback.onReceivedContentType(stream.getContentType());
                 streamCallback.onReceivedContentLength(stream.getContentLength());
 
                 byte[] buf = new byte[10 * 1000];
                 int len = 0;
                 boolean succ = true;
                 while ((len = stream.read(buf)) != -1) {
                   if (succ) {
                     succ = streamCallback.onReceivedData(buf, len);
                   }
                 }
               }
               catch (Throwable e) {
                 streamCallback.onReceivedException(e);
               }
 
             }
             else {
               streamCallback.onReceivedException(ex);
             }
 
             streamCallback.onFinished();
           }
         };
 
         internalOpenStream(asyncResult);
 
         // Return empty stream to prevent NPE
         is = new InputStream() {
           public int read() throws IOException {
             return -1;
           }
         };
 
       }
       else {
 
         BSyncResult<BContentStream> syncResult = new BSyncResult<BContentStream>() {
           public void setAsyncResult(BContentStream stream, Throwable ex) {
 
             // Set members this.innerStream, this.contentType,
             // this.contentLength
             MyInputStream.this.setAsyncResult(stream, ex);
 
             // Release thread which is waiting in syncResult.getResult()
             super.setAsyncResult(stream, ex);
           }
         };
 
         internalOpenStream(syncResult);
 
         is = syncResult.getResult();
       }
 
       if (log.isDebugEnabled()) log.debug(")openStream=" + is);
       return is;
     }
 
     protected void internalOpenStream(BAsyncResult<BContentStream> asyncResult) throws IOException {
       if (log.isDebugEnabled()) log.debug("internalOpenStream(");
       RequestToCancel requestToCancel = createRequestForGetStream(messageId, streamId, asyncResult);
       executeRequest(requestToCancel);
       if (log.isDebugEnabled()) log.debug(")internalOpenStream");
     }
 
     @Override
     public void close() throws IOException {
       super.close();
     }
 
     @Override
     public long getContentLength() throws IOException {
       ensureStream();
       return contentLength;
     }
 
     @Override
     public String getContentType() throws IOException {
       ensureStream();
       return contentType;
     }
   }
 
   @Override
   public BContentStream getStream(long messageId, long strmId) throws IOException {
     if (log.isDebugEnabled()) log.debug("getStream(messageId=" + messageId + ", streamId=" + strmId);
     BContentStream is = new MyInputStream(messageId, strmId);
     if (log.isDebugEnabled()) log.debug(")getStream=" + is);
     return is;
   }
 
   protected void internalCancelAllRequests(long cancelMessageId) {
     if (log.isDebugEnabled()) log.debug("internalCancelAllRequests(");
 
     ArrayList<RequestToCancel> arr = new ArrayList<RequestToCancel>(openRequestsToCancel.keySet());
     openRequestsToCancel.clear();
 
     if (log.isDebugEnabled()) log.debug("cancel requests on client, #requests=" + arr.size());
     for (RequestToCancel robj : arr) {
       robj.cancel();
     }
 
     // Notify the server about the canceled messages
     if (cancelMessageId != 0) {
       sendCancelMessage(cancelMessageId);
     }
 
     if (log.isDebugEnabled()) log.debug(")internalCancelAllRequests");
   }
 
   @Override
   public void cancelAllRequests() {
     internalCancelAllRequests(MESSAGEID_CANCEL_ALL_REQUESTS);
   }
 
   @Override
   public void done() {
 
     internalCancelAllRequests(MESSAGEID_DISCONNECT);
 
     isDone = true;
 
     if (isMyThreadPool) {
       if (threadPool instanceof ExecutorService) {
         ExecutorService tp = (ExecutorService) threadPool;
         tp.shutdown();
       }
     }
   }
 
   protected void addRequest(RequestToCancel robj) {
     if (log.isDebugEnabled()) log.debug("addRequest(robj=" + robj);
     openRequestsToCancel.put(robj, Boolean.TRUE);
     if (log.isDebugEnabled()) log.debug(")addRequest=" + robj);
   }
 
   protected void removeRequest(RequestToCancel robj) {
     if (log.isDebugEnabled()) log.debug("removeRequest(" + robj);
     if (robj == null) return;
     openRequestsToCancel.remove(robj);
     if (log.isDebugEnabled()) log.debug(")removeRequest");
   }
 
   /**
    * Cancel a message on the server side.
    * 
    * @param messageId
    *          Either a message ID or -1, if all messages of the session should
    *          be canceled
    */
   protected void sendCancelMessage(final long messageId) {
     if (log.isDebugEnabled()) log.debug("sendCancelMessage(messageId=" + messageId);
     try {
       RequestToCancel r = createRequestForCancelMessage(messageId);
       executeRequest(r);
     }
     catch (BException e) {
       log.warn("Exception", e);
     }
     if (log.isDebugEnabled()) log.debug(")sendCancelMessage");
   }
 
   String testAdapter(String fnct, String[] args) {
     if (log.isDebugEnabled()) log.debug("testAdapter(fnct=" + fnct + ", " + Arrays.toString(args));
 
     String ret = "";
 
     if (fnct.equals(HTestAdapter.KILL_CONNECTIONS)) {
       internalCancelAllRequests(0);
       return ret;
     }
 
     try {
       StringBuilder uparams = new StringBuilder();
       if (args != null) {
         for (String arg : args) {
           uparams.append("&").append(arg);
         }
       }
 
       final String destUrl = surl + "?testAdapter=" + fnct + uparams;
 
       final BSyncResult<ByteBuffer> syncResult = new BSyncResult<ByteBuffer>();
       final HHttpRequest httpRequest = httpClient.get(destUrl, syncResult);
 
       httpRequest.run();
 
       ByteBuffer buf = syncResult.getResult();
       if (buf != null) {
         ret = new String(buf.array(), buf.position(), buf.remaining(), "UTF-8");
       }
     }
     catch (IOException ignored) {
     }
 
     if (log.isDebugEnabled()) log.debug(")testAdapter=" + ret);
     return ret;
   }
 
   @Override
   public synchronized Statistics getStatistics() {
     return new Statistics(stats);
   }
 
   @Override
   public synchronized void clearStatistics() {
     stats = new Statistics();
   }
 
   @Override
   public BTestAdapter getTestAdapter() {
     return testAdapter;
   }
 
   /**
    * Gets the servlet path for negotiation and authentication. A request for
    * HTTP authentication should be sent to a different sub directory of the web
    * application. E.g. while http://server/app/byps is the URL for BYPS
    * communication, authentication requests are sent to
    * http://server/app/bypsauth/auth. The reason for this is that some browsers
    * initiate the authentication handshake in each request if the first one has
    * had to be authenticated.
    * 
    * @return servlet path of the URL that was passed in the constructor, e.g.
    *         /byps
    */
   public String getServletPathForNegotiationAndAuthentication() {
     String authUrl = surl;
     int p = authUrl.lastIndexOf('/');
     if (p >= 0) {
       authUrl = authUrl.substring(p);
     }
     return authUrl;
   }
 
   /**
    * Gets the servlet path for reverse HTTP requests. Reverse requests can be
    * sent to a different sub directory of the web application. This is useful if
    * a load balancer or proxy redirects the requests, because it allows to
    * specify a special timeout for long polls.
    * 
    * @return servlet path of the URL that was passed in the constructor, e.g.
    *         /byps
    */
   public String getServletPathForReverseRequest() {
     String longUrl = surl;
     int p = longUrl.lastIndexOf('/');
     if (p >= 0) {
       longUrl = longUrl.substring(p);
     }
     return longUrl;
   }
 
   private String makeUrl(String servletPath, String[] params) {
     StringBuilder sbuf = new StringBuilder();
     int p = surl.lastIndexOf("/");
     if (p < 0) p = surl.length();
     sbuf.append(surl.substring(0, p));
     sbuf.append(servletPath);
     if (params != null) {
       for (int i = 0; i < params.length; i += 2) {
         sbuf.append(i == 0 ? "?" : "&");
         sbuf.append(params[i]);
         sbuf.append("=");
         sbuf.append(params[i + 1]);
       }
     }
     return sbuf.toString();
   }
 
   public String toString() {
     return "[url=" + surl + "]";
   }
 
   public HHttpClient getHttpClient() {
     return this.httpClient;
   }
 }
