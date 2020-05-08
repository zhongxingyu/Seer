 package ddth.dasp.hetty.back;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.common.RequestLocal;
 import ddth.dasp.common.logging.ProfileLogger;
 import ddth.dasp.common.osgi.IOsgiBootstrap;
 import ddth.dasp.hetty.HettyConstants;
 import ddth.dasp.hetty.IRequestActionHandler;
 import ddth.dasp.hetty.message.IMessageFactory;
 import ddth.dasp.hetty.message.IRequest;
 import ddth.dasp.hetty.message.IRequestParser;
 import ddth.dasp.hetty.message.IResponse;
 import ddth.dasp.hetty.message.protobuf.ResponseUtils;
 import ddth.dasp.hetty.qnt.IQueueReader;
 import ddth.dasp.hetty.qnt.ITopicPublisher;
 
 public class HettyRequestHandlerServer {
 
     private final Logger LOGGER = LoggerFactory.getLogger(HettyRequestHandlerServer.class);
 
     private IQueueReader queueReader;
     private String queueName = HettyConstants.DEFAULT_HETTY_QUEUE;
 
     private ITopicPublisher topicPublisher;
     private String topicName = HettyConstants.DEFAULT_HETTY_TOPIC;
 
     private long readTimeoutMillisecs = 5000, writeTimeoutMillisecs = 5000;
 
     private IRequestParser requestParser;
     private IMessageFactory messageFactory;
 
     private int numWorkers = Runtime.getRuntime().availableProcessors();
     private WorkerThread[] workerThreads;
 
     public HettyRequestHandlerServer() {
     }
 
     protected IMessageFactory getMessageFactory() {
         return messageFactory;
     }
 
     public HettyRequestHandlerServer setMessageFactory(IMessageFactory messageFactory) {
         this.messageFactory = messageFactory;
         return this;
     }
 
     protected IQueueReader getQueueReader() {
         return queueReader;
     }
 
     public HettyRequestHandlerServer setQueueReader(IQueueReader queueReader) {
         this.queueReader = queueReader;
         return this;
     }
 
     protected ITopicPublisher getTopicPublisher() {
         return topicPublisher;
     }
 
     public HettyRequestHandlerServer setTopicPublisher(ITopicPublisher topicPublisher) {
         this.topicPublisher = topicPublisher;
         return this;
     }
 
     protected String getQueueName() {
         return queueName;
     }
 
     public HettyRequestHandlerServer setQueueName(String queueName) {
         this.queueName = queueName;
         return this;
     }
 
     protected String getTopicName() {
         return topicName;
     }
 
     public HettyRequestHandlerServer setTopicName(String topicName) {
         this.topicName = topicName;
         return this;
     }
 
     public long getReadTimeoutMillisecs() {
         return readTimeoutMillisecs;
     }
 
     public HettyRequestHandlerServer setReadTimeoutMillisecs(long readTimeoutMillisecs) {
         this.readTimeoutMillisecs = readTimeoutMillisecs;
         return this;
     }
 
     public long getWriteTimeoutMillisecs() {
         return writeTimeoutMillisecs;
     }
 
     public HettyRequestHandlerServer setWriteTimeoutMillisecs(long writeTimeoutMillisecs) {
         this.writeTimeoutMillisecs = writeTimeoutMillisecs;
         return this;
     }
 
     public IRequestParser getRequestParser() {
         return requestParser;
     }
 
     public HettyRequestHandlerServer setRequestParser(IRequestParser requestParser) {
         this.requestParser = requestParser;
         return this;
     }
 
     public int getNumWorkers() {
         return numWorkers;
     }
 
     public HettyRequestHandlerServer setNumWorkers(int numWorkers) {
         this.numWorkers = numWorkers;
         return this;
     }
 
     protected void handleRequest(IRequest request) throws Exception {
         long queueTimeMs = (long) ((System.nanoTime() - request.getTimestampNano()) / 1E6);
         if (queueTimeMs > 10000) {
             // in queue more than 10 secs
             String msg = "Request [" + request.getId() + ":" + request.getUri()
                     + "] has stayed in queue too long [" + queueTimeMs + " ms]!";
             IResponse response = ResponseUtils.newResponse(request).setStatus(408)
                     .addHeader("Content-Type", "text/html; charset=UTF-8").setContent(msg);
             topicPublisher.publish(topicName, response, writeTimeoutMillisecs,
                     TimeUnit.MILLISECONDS);
             LOGGER.warn(msg);
         } else {
             internalHandleRequest(request);
         }
     }
 
     /**
      * Actually handles the request. Called by {@link #handleRequest(IRequest)},
      * sub-class may override this method to implement its own logic.
      * 
      * @param request
      * @throws Exception
      */
     protected void internalHandleRequest(IRequest request) throws Exception {
         String module = requestParser.getModule(request);
         String action = requestParser.getAction(request);
         Map<String, String> filter = new HashMap<String, String>();
         filter.put(IRequestActionHandler.FILTER_KEY_MODULE, !StringUtils.isBlank(module) ? module
                 : "_");
         filter.put(IRequestActionHandler.FILTER_KEY_ACTION, !StringUtils.isBlank(action) ? action
                 : "_");
         IOsgiBootstrap osgiBootstrap = DaspGlobal.getOsgiBootstrap();
         IRequestActionHandler handler = osgiBootstrap.getService(IRequestActionHandler.class,
                 filter);
         if (handler == null) {
             // fallback 1: lookup for wildcard handler
             // note: do not use "*" for filtering as OSGi will match "any"
             // service, which is not what we want!
             filter.put(IRequestActionHandler.FILTER_KEY_ACTION, "_");
             handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
         }
         if (handler == null) {
             // fallback 2: lookup for non-action handler
             filter.remove(IRequestActionHandler.FILTER_KEY_ACTION);
             handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
         }
         if (handler != null) {
             handler.handleRequest(request, topicPublisher, topicName);
         } else {
             IResponse response = ResponseUtils.response404(request);
             topicPublisher.publish(topicName, response, writeTimeoutMillisecs,
                     TimeUnit.MILLISECONDS);
         }
     }
 
     public void destroy() {
         for (WorkerThread workerThread : workerThreads) {
             try {
                 workerThread.finish();
             } catch (Exception e) {
             }
         }
     }
 
     public void start() {
         workerThreads = new WorkerThread[numWorkers];
         for (int i = 1; i <= numWorkers; i++) {
             WorkerThread t = new WorkerThread(this, queueReader, queueName, messageFactory,
                     topicPublisher, topicName, readTimeoutMillisecs);
             workerThreads[i - 1] = t;
             t.start();
         }
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Hetty request handler workers: " + numWorkers + " / Read timeout: "
                     + readTimeoutMillisecs + " / Write timeout: " + writeTimeoutMillisecs);
         }
     }
 
     private static class WorkerThread extends Thread {
         private Logger LOGGER = LoggerFactory.getLogger(WorkerThread.class);
         private static int COUNTER = 1;
 
         private HettyRequestHandlerServer requestHandler;
         private IQueueReader queueReader;
         private String queueName;
         private IMessageFactory messageFactory;
         private ITopicPublisher topicPublisher;
         private String topicName;
         private long readTimeoutMillisecs;
         private boolean done = false;
 
         public WorkerThread(HettyRequestHandlerServer requestHandler, IQueueReader queueReader,
                 String queueName, IMessageFactory messageFactory, ITopicPublisher topicPublisher,
                 String topicName, long readTimeoutMillisecs) {
            setName("HRH-Worker-" + (COUNTER++));
             setDaemon(true);
 
             this.requestHandler = requestHandler;
             this.queueReader = queueReader;
             this.queueName = queueName;
             this.messageFactory = messageFactory;
             this.topicPublisher = topicPublisher;
             this.topicName = topicName;
             this.readTimeoutMillisecs = readTimeoutMillisecs;
         }
 
         private Object poll() {
             Object obj = null;
             try {
                 obj = queueReader.queueRead(queueName, readTimeoutMillisecs, TimeUnit.MILLISECONDS);
             } catch (Exception e) {
                 obj = null;
             }
             if (obj == null) {
                 try {
                     Thread.sleep(1);
                     // Thread.sleep(System.currentTimeMillis() & 0xF);
                     Thread.yield();
                 } catch (Exception e) {
                 }
             }
             return obj;
         }
 
         private RequestLocal initRequestLocal() {
             RequestLocal requestLocal = RequestLocal.get();
             if (requestLocal == null) {
                 requestLocal = new RequestLocal();
                 RequestLocal.set(requestLocal);
             }
             return requestLocal;
         }
 
         private void doneRequestLocal() {
             try {
                 RequestLocal.remove();
             } catch (Exception e) {
             }
         }
 
         private Object deserialize(Object obj) {
             try {
                 if (obj instanceof byte[]) {
                     ProfileLogger.push("deserialize_request");
                     try {
                         obj = messageFactory.deserializeRequest((byte[]) obj);
                     } finally {
                         ProfileLogger.pop();
                     }
                 }
                 return obj;
             } catch (Exception e) {
                 LOGGER.warn(((byte[]) obj).length + ": " + e.getMessage());
                 return null;
             }
         }
 
         public void finish() {
             done = true;
             interrupt();
         }
 
         public void run() {
             while (!done && !interrupted()) {
                 Object obj = poll();
                 initRequestLocal();
                 try {
                     ProfileLogger.push("start_request_handler_worker");
                     try {
                         obj = deserialize(obj);
                         if (obj instanceof IRequest) {
                             requestHandler.handleRequest((IRequest) obj);
                         }
                     } catch (Exception e) {
                         LOGGER.error(e.getMessage(), e);
                         if (obj instanceof IRequest) {
                             try {
                                 IResponse response = ResponseUtils.response500((IRequest) obj,
                                         e.getMessage(), e);
                                 topicPublisher.publish(topicName, response);
                             } catch (Exception ex) {
                                 LOGGER.error(ex.getMessage(), ex);
                             }
                         }
                     } finally {
                         ProfileLogger.pop();
                     }
                 } finally {
                     doneRequestLocal();
                 }
             }
         }
     }
 }
