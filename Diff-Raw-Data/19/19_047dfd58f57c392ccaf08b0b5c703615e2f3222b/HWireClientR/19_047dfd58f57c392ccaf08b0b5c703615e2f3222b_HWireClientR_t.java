 package byps.http;
 /* USE THIS FILE ACCORDING TO THE COPYRIGHT RULES IN LICENSE.TXT WHICH IS PART OF THE SOURCE CODE PACKAGE */
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import byps.BAsyncResult;
 import byps.BContentStream;
 import byps.BException;
 import byps.BExceptionC;
 import byps.BMessage;
 import byps.BMessageHeader;
 import byps.BStreamRequest;
 import byps.BWire;
 
 public class HWireClientR extends BWire {
   
   public HWireClientR(BWire wireServer) {
     super(wireServer.getFlags());
     this.wireServer = wireServer;
   }
 
   @Override
   public synchronized void cancelAllRequests() {
     if (log.isDebugEnabled()) log.debug("cancelAllRequests(");
 
     canceled = true;
 
     BException bex = new BException(BExceptionC.CANCELLED, "Longpoll canceled");
 
     // Notify the threads inside the server waiting for results that the
     // their calls are canceled.
     for (BAsyncResult<BMessage> asyncResult : mapAsyncResults.values()) {
       // Gib iBuf an den Thread weiter, der auf das Resultat wartet.
       synchronized (asyncResult) {
         if (log.isDebugEnabled()) log.debug("pass cancel message to asyncResult, notify thread waiting in send()");
        try { 
          asyncResult.setAsyncResult(null, bex); 
        } catch (Throwable ex) {
          // just make sure, that all results are handled.
        }
         asyncResult.notifyAll();
       }
     }
 
     // Notify the client that the long-poll has finished.
     for (AsyncRequestFromLongpoll lrequest : lonpollRequests) {
       try {
         lrequest.request.setAsyncResult(null, bex);
       } catch (Throwable ignored) {
         // happens, if the HTTP session has already been invalidated
       }
     }
     lonpollRequests.clear();
 
     if (log.isDebugEnabled()) log.debug(")cancelAllRequests");
   }
 
   /**
    * Receive a long poll (reverse HTTP) request.
    * 
    * The HTTP request body (stored in ibuf) contains the response from the
    * client. The send() function is waiting for this response. As send() has
    * started the request, it has put an BAsyncResult object into the map
    * asyncResults_access_sync associated with the key messageId. This object is
    * found by the messageId and ibuf is passed to it by a call to
    * setAsyncResult.
    * 
    * The HTTP response has been encapsulated in parameter asyncRequest by the
    * caller. It will receive the next call when the server has to invoke the
    * client. Until the server needs the asyncRequest, it is stored in the map
    * asyncRequests_access_sync. The send() function takes the asyncRequest from
    * there and writes the request bytes into it.
    * 
    * @param messageId
    * @param ibuf
    * @param asyncRequest
    * @throws IOException
    */
   public void recvLongPoll(BMessage ibuf, final BAsyncResult<BMessage> nextRequest) throws IOException {
     if (log.isDebugEnabled()) log.debug("recvLongPoll(messageId=" + ibuf.header.messageId);
 
     if (log.isDebugEnabled()) log.debug("message buffer=" + ibuf);
 
     // Function send() has stored its parameter BAsyncResult mapped under the messageId.  
     BAsyncResult<BMessage> asyncResult = mapAsyncResults.remove(ibuf.header.messageId);
     if (log.isDebugEnabled()) log.debug("asyncResult for messageId: " + asyncResult);
 
     // If a BAsyncResult object is found ...
     if (asyncResult != null) {
 
       // ... pass ibuf as result.
       if (log.isDebugEnabled()) log.debug("pass message buffer to asyncResult");
       asyncResult.setAsyncResult(ibuf, null);
 
     }
     else {
       // Function send() has not been called before. 
     }
 
     // If function send() could not find a pending long-poll, it pushed 
     // the message into the queue of pending messages.
     PendingMessage pendingMessage = pendingMessages.poll(); 
         
     // Pending message available? ...
     if (pendingMessage != null) {
       
       // ... send the pending message immediately to the client.
       nextRequest.setAsyncResult(pendingMessage.msg, null);
     }
     else {
       
       // ... push the long-poll into the queue.
       lonpollRequests.add(new AsyncRequestFromLongpoll(nextRequest, ibuf.header));
       if (log.isDebugEnabled()) log.debug("add longpoll to list, #polls=" + lonpollRequests.size());
     }
     
     if (log.isDebugEnabled()) log.debug(")recvLongPoll");
   }
 
   @Override
   public void send(BMessage msg, BAsyncResult<BMessage> asyncResult) {
     if (log.isDebugEnabled()) log.debug("send(" + msg + ", asyncResult=" + asyncResult);
 
     try {
       
       final long messageId = msg.header.messageId;
       
       // Save result object for next long-poll
       if (log.isDebugEnabled()) log.debug("map messageId=" + messageId + " to asyncResult=" + asyncResult);
       mapAsyncResults.put(messageId, asyncResult);
       
       for (int i = 0; i < 2; i++) {
         
         // If canceled, set BExceptionC.CLIENT_DIED to asyncResult 
         if (canceled) {
           terminateMessage(messageId, null);
           break;
         }
         
         // Find valid long-poll request object
         BAsyncResult<BMessage> asyncRequest = getNextLongpollRequestOrPushMessage(msg);
         
         // No long-poll found?
         if (asyncRequest == null) {
           
           // The message was pushed into pendingMessages_access_sync.
           // It will be sent in the next call to recvLongPoll. 
           break;
         }
 
         // Send the message
         try {
           
           asyncRequest.setAsyncResult(msg, null);
           break;
           
          } catch (Throwable e) {
            
           if (e.toString().indexOf("ClientAbortException") >= 0) {
             terminateMessage(messageId, e);
             break;
           }
           else {
             // The underlying long-poll request has already timed out.
             // Maybe there is another connection available.
             // -> retry
           }
           
         }
 
       }
 
     } catch (Throwable e) {
       if (log.isWarnEnabled()) log.warn("Failed to send reverse message.", e);
       asyncResult.setAsyncResult(null, e);
     }
 
     if (log.isDebugEnabled()) log.debug(")send");
   }
   
   protected BAsyncResult<BMessage> getNextLongpollRequestOrPushMessage(BMessage msg) {
     
     // Get next long-poll request from queue.
     AsyncRequestFromLongpoll longpollRequest = lonpollRequests.poll();
     
     // long-poll active? ...
     if (longpollRequest != null) return longpollRequest.request;
     
     // Push message into the queue of pending messages.
     // This message will be immediately sent at the next time recvLongPoll is called.
     if (!pendingMessages.offer(new PendingMessage(msg))) {
       
       // Queue is full, terminate message with error.
       if (log.isDebugEnabled()) log.debug("Failed to add pending msg=" + msg);
       BException ex = new BException(BExceptionC.TOO_MANY_REQUESTS, "Failed to add pending message.");
       terminateMessage(msg.header.messageId, ex);
     }
     
     return null;
   }
 
   @Override
   public void putStreams(List<BStreamRequest> streamRequests, BAsyncResult<BMessage> asyncResult) {
     wireServer.putStreams(streamRequests, asyncResult);
   };
 
   @Override
   public BContentStream getStream(long messageId, long strmId) throws IOException {
     return wireServer.getStream(messageId, strmId);
   }
 
   /**
    * Terminate the message to be sent to the client.
    * The caller receives a {@link BExceptionC#CLIENT_DIED}.
    * @param messageId
    * @param e Exception used for BException detail.
    */
   protected void terminateMessage(final long messageId, Throwable e) {
     
     BException bex = null;
     
     // Get the asyncResult pushed from the send() method into the
     // asyncResults_access_sync map.
     BAsyncResult<BMessage> asyncResult = mapAsyncResults.remove(messageId);
     if (asyncResult != null) {
   
       if (e instanceof BException) {
         bex = (BException)e;
       }
       else {
         // Find the innermost exception cause
         Throwable innerException = e;
         while (e != null) {
           innerException = e;
           e = e.getCause();
         }
       
         // Notify the caller of send() with an exception.
         bex = new BException(BExceptionC.CLIENT_DIED, "", innerException);
       }
     }
     
     asyncResult.setAsyncResult(null, bex);
   }
   
   /**
    * This function releases expired long-polls and messages.
    * The client application receives a status code 204 for a long-poll. 
    */
   public void cleanup() {
     
     ArrayList<AsyncRequestFromLongpoll> lpolls = removeExpiredLongpolls();
     ArrayList<PendingMessage> msgs = getExpiredMessages();
     
     if (lpolls.size() != 0) {
 
       // The client should send a new long-poll.
       // HWriteResponseHelper will return HTTP status cod 204
 
       BException ex = new BException(BExceptionC.RESEND_LONG_POLL, "");
       for (AsyncRequestFromLongpoll lrequest : lpolls) {
         try {
           lrequest.request.setAsyncResult(null, ex);
         }
         catch (Throwable e) {
           // catch "Response already written"
           if (log.isDebugEnabled()) log.debug("Failed to respond to longpoll=" + lrequest, e);
         }
       }
     }
     
     if (msgs.size() != 0) {
       
       BException ex = new BException(BExceptionC.TIMEOUT, "Timeout while waiting for reverse request.");
       for (PendingMessage msg : msgs) {
         try {
           terminateMessage(msg.msg.header.messageId, ex);
         }
         catch (Throwable e) {
           // just make sure that cleanup runs over all messages.
           if (log.isDebugEnabled()) log.debug("Failed to terminate pending message=" + msg, e);
         }
       }
     }
 
   }
   
   /**
    * Remove expired long-poll requests from internal list.
    * @return Removed long-poll requests.
    */
   protected ArrayList<AsyncRequestFromLongpoll> removeExpiredLongpolls() {
     ArrayList<AsyncRequestFromLongpoll> ret = new ArrayList<AsyncRequestFromLongpoll>();
     
     boolean found = true;
     while (found) {
       found = false;
       for (Iterator<AsyncRequestFromLongpoll> it = lonpollRequests.iterator(); it.hasNext(); ) {
         AsyncRequestFromLongpoll lrequest = it.next();
         if (lrequest.isExpired()) {
           ret.add(lrequest);
           it.remove();
           found = true;
           break;
         }
       }
     }
     
     return ret;
   }
 
   /**
    * Remove expired messages.
    * A message is terminated, if it has been waiting more than {@link HConstants#MAX_WAIT_FOR_LONGPOLL_MILLIS}
    * for a long-poll.
    */
   protected ArrayList<PendingMessage> getExpiredMessages() {
     ArrayList<PendingMessage> ret = new ArrayList<PendingMessage>();
 
     boolean foundOne = true;
     while (foundOne) {
       foundOne = false;
       for (Iterator<PendingMessage> it = pendingMessages.iterator(); it.hasNext(); ) {
         PendingMessage pendingMessage = it.next();
         if (pendingMessage.isExpired()) {
           it.remove();
           ret.add(pendingMessage);
           foundOne = true;
           break;
         }
       }    
     }
     
     return ret;
   }
   
   static class AsyncRequestFromLongpoll {
     final long bestBefore;
     //Date dt;
     final BAsyncResult<BMessage> request;
     final BMessageHeader header;
     
     AsyncRequestFromLongpoll(BAsyncResult<BMessage> request, BMessageHeader header) {
       this.request = request;
       this.header = header;
       long timeout = header.timeoutSeconds != 0 ? (header.timeoutSeconds*1000) : HConstants.TIMEOUT_LONGPOLL_MILLIS;
       this.bestBefore = System.currentTimeMillis() + timeout;
       //this.dt = new Date(bestBefore);
     }
     
     boolean isExpired() {
       return bestBefore < System.currentTimeMillis();
     }
     
     public String toString() {
       return "[" + header + ", expired=" + isExpired() + ", bestBefore=" + new Date(bestBefore) + "]";
     }
   }
 
   static class PendingMessage {
     long bestBefore;
     BMessage msg;
     
     PendingMessage(BMessage msg) {
       this.msg = msg;
       this.bestBefore = System.currentTimeMillis() + HConstants.MAX_WAIT_FOR_LONGPOLL_MILLIS;
     }
     
     boolean isExpired() {
       return bestBefore < System.currentTimeMillis();
     }
 
     public String toString() {
       return "[" + msg + ", expired=" + isExpired() + ", bestBefore=" + new Date(bestBefore) + "]";
     }
   }
 
   private final BlockingQueue<AsyncRequestFromLongpoll> lonpollRequests = new LinkedBlockingQueue<AsyncRequestFromLongpoll>();
   private final ConcurrentHashMap<Long, BAsyncResult<BMessage>> mapAsyncResults = new ConcurrentHashMap<Long, BAsyncResult<BMessage>>();
   private final BlockingQueue<PendingMessage> pendingMessages = new LinkedBlockingQueue<PendingMessage>();
   private final BWire wireServer;
   private final Log log = LogFactory.getLog(HWireClientR.class);
   private volatile boolean canceled;
 
 }
