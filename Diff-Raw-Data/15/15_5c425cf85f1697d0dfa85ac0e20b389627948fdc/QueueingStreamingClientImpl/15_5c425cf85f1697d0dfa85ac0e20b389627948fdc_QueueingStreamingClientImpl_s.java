 package com.sfdc.http.smc;
 
 import com.ning.http.client.Cookie;
 import com.ning.http.client.Response;
 import com.sfdc.http.client.handler.StatefulHandler;
 import com.sfdc.http.queue.Producer;
 import com.sfdc.http.queue.WorkItem;
 import com.sfdc.stats.StatsManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * @author psrinivasan
  *         Date: 9/13/12
  *         Time: 12:07 PM
  *         <p/>
  *         This client, similar to StreamingClientImpl, encapsulates FSM assisted transitions into
  *         handshakes, subscribes, and connects. Only difference is that it pushes requests into a
  *         queue rather than working directly with the http client.
  */
 public class QueueingStreamingClientImpl implements StreamingClient {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(QueueingStreamingClientImpl.class);
     private String sessionId;
     private String instance;
     private String clientId;
     protected final StreamingClientFSMContext _fsm;
     private final Producer handshakeProducer;
     private final Producer defaultProducer;
     private List<Cookie> cookies;
     private String[] channels;
 
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
 
     public QueueingStreamingClientImpl(String sessionId, String instance, Producer handshakeProducer, Producer defaultProducer, String[] channels) {
         this.sessionId = sessionId;
         this.instance = instance;
         this.clientId = clientId;
         this.handshakeProducer = handshakeProducer;
         this.defaultProducer = defaultProducer;
         this.channels = channels;
         _fsm = new StreamingClientFSMContext(this);
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
 
     public WorkItem createWorkItem(WorkItem.Operation operation) {
         WorkItem w = new WorkItem();
         w.setOperation(operation);
         w.setInstance(instance);
         w.setSessionId(sessionId);
         w.setCookies(cookies);
         w.setClientId(clientId);
         w.setHandler(new StatefulHandler(this, StatsManager.getInstance()));
         return w;
     }
 
     @Override
     public void startHandshake() {
         WorkItem work = createWorkItem(WorkItem.Operation.HANDSHAKE);
         Producer p = (handshakeProducer == null) ? defaultProducer : handshakeProducer;
         p.publish(work);
         _fsm.onStartingHandshake(null);
     }
 
     @Override
     public void startSubscribe() {
         WorkItem work = createWorkItem(WorkItem.Operation.SUBSCRIBE);
         work.setChannel(channels[0]);//todo:  make the subscribes happen for multiple channels.  this means changing the fsm.
         defaultProducer.publish(work);
         _fsm.onStartingSubscribe(null);
     }
 
     @Override
     public void startConnect() {
         WorkItem work = createWorkItem(WorkItem.Operation.CONNECT);
         work.setChannel("/topic/accountTopic");
         defaultProducer.publish(work);
         _fsm.onStartingConnect(null);
     }
 
     @Override
     public void shouldWeReconnect() {
         //No.
         _fsm.onFinishedScenario();
         //Other option was
         //_fsm.onReconnectRequest();
 
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
            LOGGER.error("Client Aborted due to 500 Internal Server Error.  HTTP Status code: " + response.getResponseBody());
         } catch (IOException e) {
             LOGGER.error("Client Aborted due to 500 Internal Server Error.");
             e.printStackTrace();
         }
 
     }
 
     @Override
     public void abortClientDueToUnknownClientId(Response response) {
         try {
            LOGGER.error("Client Aborted due to Unknown Client ID Response.  HTTP Status code: " + response.getResponseBody());
         } catch (IOException e) {
             LOGGER.error("Client Aborted due to Unknown Client ID Response");
             e.printStackTrace();
         }
 
     }
 
 
     @Override
     public void onHandshakeComplete(List<Cookie> cookies, String clientId) {
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
 }
