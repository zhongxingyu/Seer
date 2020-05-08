 package com.sfdc.http.smc;
 
 import com.ning.http.client.Cookie;
 import com.ning.http.client.Response;
 import com.sfdc.http.client.handler.StatefulHandler;
 import com.sfdc.http.queue.ProducerInterface;
 import com.sfdc.http.queue.StreamingWorkItem;
 import com.sfdc.http.queue.WorkItemInterface;
 import com.sfdc.stats.StatsManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 /**
  * @author psrinivasan
  *         Date: 9/13/12
  *         Time: 12:07 PM
  *         <p/>
  *         This client, similar to StreamingClientImpl, encapsulates FSM assisted transitions into
  *         handshakes, subscribes, and connects. Only difference is that it pushes requests into a
  *         queue rather than working directly with the http client.
  *         There is one instance of this class per client.
  */
 public class QueueingStreamingClientImpl implements StreamingClient {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(QueueingStreamingClientImpl.class);
     private String sessionId;
     private String instance;
     private String clientId;
     protected final StreamingClientFSMContext _fsm;
     //protected final StreamingClientHandshakeFSMContext _fsm; //used for debugging purposes
     //protected final StreamingClientSubscribeFSMContext _fsm; //used for debugging purposes
     private final ProducerInterface handshakeProducer;
     private final ProducerInterface defaultProducer;
     private List<Cookie> cookies;
     private String[] channels;
     private final Semaphore handshakeConcurrencyPermit;
     private final Date endTime;
     private final Semaphore concurrencyPermit;
 
     public String getSessionId() {
         return sessionId;
     }
 
     public void setSessionId(String sessionId) {
         this.sessionId = sessionId;
     }
 
     public String getInstance() {
         return instance;
     }
 
     public void setInstance(String instance) {
         this.instance = instance;
     }
 
     public String getClientId() {
         return clientId;
     }
 
     public void setClientId(String clientId) {
         this.clientId = clientId;
     }
 
     public QueueingStreamingClientImpl(String sessionId, String instance, ProducerInterface handshakeProducer,
                                        ProducerInterface defaultProducer, String[] channels,
                                        Semaphore handshakeConcurrencyPermit,
                                        Date endTime,
                                        Semaphore concurrencyPermit
     ) {
         this.concurrencyPermit = concurrencyPermit;
         this.endTime = endTime;
         this.sessionId = sessionId;
         this.instance = instance;
         this.clientId = clientId;
         this.handshakeProducer = handshakeProducer;
         this.defaultProducer = defaultProducer;
         this.channels = channels;
         this.handshakeConcurrencyPermit = handshakeConcurrencyPermit;
         _fsm = new StreamingClientFSMContext(this);
         //_fsm = new StreamingClientHandshakeFSMContext(this);
         //_fsm = new StreamingClientSubscribeFSMContext(this);
     }
 
     public void start() {
         //todo: need a parameterized way to enable/disable fsm debugging.
         //_fsm.setDebugFlag(true);
         //_fsm.setDebugStream(System.out);
         _fsm.enterStartState();
     }
 
 
     @Override
     public String getState() {
         return _fsm.getState().getName();
     }
 
     @Override
     public void setCookies(List<Cookie> cookies) {
         this.cookies = cookies;
     }
 
     public String[] getChannels() {
         return channels;
     }
 
     public void setChannels(String[] channels) {
         this.channels = channels;
     }
 
     public WorkItemInterface createWorkItem(String operation) {
         WorkItemInterface w = new StreamingWorkItem();
         w.setOperation(operation);
         w.setInstance(instance);
         w.setSessionId(sessionId);
         w.setCookies(cookies);
         w.setClientId(clientId);
         w.setHandler(new StatefulHandler(this, StatsManager.getInstance(), concurrencyPermit));
         return w;
     }
 
     @Override
     public void startHandshake() {
         WorkItemInterface work = createWorkItem(StreamingWorkItem.HANDSHAKE);
         ProducerInterface p = (handshakeProducer == null) ? defaultProducer : handshakeProducer;
         try {
             handshakeConcurrencyPermit.acquire();
             LOGGER.debug("Acquired Hansdshake Permit! - remaining permits " + handshakeConcurrencyPermit.availablePermits());
         } catch (InterruptedException e) {
             LOGGER.warn("exception thrown while waiting for handshake concurrency semaphore.  Actual handshake concurrency may not be what you expect.");
             e.printStackTrace();
         }
         p.publish(work);
         _fsm.onStartingHandshake(null);
     }
 
     @Override
     public void startSubscribe() {
         WorkItemInterface work = createWorkItem(StreamingWorkItem.SUBSCRIBE);
         work.setChannel(channels[0]);//todo:  make the subscribes happen for multiple channels.  this means changing the fsm.
         defaultProducer.publish(work);
         _fsm.onStartingSubscribe(null);
     }
 
     @Override
     public void startConnect() {
         WorkItemInterface work = createWorkItem(StreamingWorkItem.CONNECT);
         work.setChannel("/topic/accountTopic");
         defaultProducer.publish(work);
         _fsm.onStartingConnect(null);
     }
 
     @Override
     public void shouldWeReconnect() {
         if ((System.currentTimeMillis() >= endTime.getTime())) {
             LOGGER.info(clientId + ":willnotreconnect");
             _fsm.onFinishedScenario();
         } else {
             LOGGER.info(clientId + ":reconnecting");
             _fsm.onReconnectRequest();
         }
     }
 
     @Override
     public void clientDone() {
         LOGGER.info("Client Done.");
     }
 
     @Override
     public void clientAborted() {
         LOGGER.info("Client Aborted");
     }
 
     @Override
     public void abortClientDueToBadCredentials(Response response) {
         try {
             LOGGER.error("Client Aborted due to bad credentials.  Response: " + response.getResponseBody());
         } catch (IOException e) {
             LOGGER.error("Client Aborted due to bad credentials.");
             e.printStackTrace();
         }
     }
 
     @Override
     public void abortClientDueTo500(Response response) {
         try {
             LOGGER.error("Client Aborted due to 500 Internal Server Error.  HTTP Status code: " + response.getStatusCode() + " Body: " + response.getResponseBody());
         } catch (IOException e) {
             LOGGER.error("Client Aborted due to 500 Internal Server Error.");
             e.printStackTrace();
         }
 
     }
 
     @Override
     public void abortClientDueToUnknownClientId(Response response) {
         try {
             LOGGER.error("Client Aborted due to Unknown Client ID Response.  HTTP Status code: " + response.getStatusCode() + " Body: " + response.getResponseBody());
         } catch (IOException e) {
             LOGGER.error("Client Aborted due to Unknown Client ID Response");
             e.printStackTrace();
         }
 
     }
 
     @Override
     public void abortClientDueToOtherHttpErrorCode(Response response) {
         try {
             LOGGER.error("Client Aborted due to Unknown HTTP Error Response.  HTTP Status code: " + response.getStatusCode() + " Body: " + response.getResponseBody());
         } catch (IOException e) {
             LOGGER.error("Client Aborted due to Unknown HTTP Error Response");
             e.printStackTrace();
         }
     }
 
 
     @Override
     public void onHandshakeComplete(List<Cookie> cookies, String clientId) {
         handshakeConcurrencyPermit.release();
         setCookies(cookies);
         setClientId(clientId);
         _fsm.onHandshakeComplete(cookies, clientId);
     }
 
     @Override
     public void onSubscribeComplete() {
         _fsm.onSubscribeComplete();
     }
 
     @Override
     public void onConnectComplete() {
         _fsm.onConnectComplete();
     }
 
     @Override
     public void onFinishedScenario() {
         _fsm.onFinishedScenario();
     }
 
     @Override
     public void onReconnectRequest() {
         _fsm.onReconnectRequest();
     }
 
     @Override
     public void onInvalidAuthCredentials(Response response) {
        LOGGER.error("Invalid Auth Credentials " + getSessionId());
         _fsm.onInvalidAuthCredentials(response);
     }
 
     @Override
     public void on500Error(Response response) {
         _fsm.on500Error(response);
     }
 
     @Override
     public void onUnknownClientId(Response response) {
         _fsm.onUnknownClientId(response);
     }
 
     @Override
     public void onOtherHttpErrorCode(Response response) {
         _fsm.onOtherHttpErrorCode(response);
     }
 }
