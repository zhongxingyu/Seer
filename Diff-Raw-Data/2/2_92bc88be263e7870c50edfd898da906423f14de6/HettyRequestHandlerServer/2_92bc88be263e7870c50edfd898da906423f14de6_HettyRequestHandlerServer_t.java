 package ddth.dasp.hetty.back;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.common.DaspGlobal;
 import ddth.dasp.common.osgi.IOsgiBootstrap;
 import ddth.dasp.hetty.IRequestActionHandler;
 import ddth.dasp.hetty.message.protobuf.HettyProtoBuf;
 import ddth.dasp.hetty.message.protobuf.IRequestParser;
 import ddth.dasp.hetty.message.protobuf.ResponseUtils;
 import ddth.dasp.hetty.qnt.IQueueReader;
 import ddth.dasp.hetty.qnt.ITopicPublisher;
 
 public class HettyRequestHandlerServer {
 
     private final Logger LOGGER = LoggerFactory.getLogger(HettyRequestHandlerServer.class);
 
     private IQueueReader queueReader;
     private ITopicPublisher topicPublisher;
     private long readTimeoutMillisecs = 5000, writeTimeoutMillisecs = 5000;
     private IRequestParser requestParser;
     private int numWorkers = Runtime.getRuntime().availableProcessors();
     private Thread[] workerThreads;
 
     public HettyRequestHandlerServer() {
     }
 
     public IQueueReader getQueueReader() {
         return queueReader;
     }
 
     public HettyRequestHandlerServer setQueueReader(IQueueReader queueReader) {
         this.queueReader = queueReader;
         return this;
     }
 
     public ITopicPublisher getTopicPublisher() {
         return topicPublisher;
     }
 
     public HettyRequestHandlerServer setTopicPublisher(ITopicPublisher topicPublisher) {
         this.topicPublisher = topicPublisher;
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
 
     protected void handleRequest(HettyProtoBuf.Request requestProtobuf) throws Exception {
         String module = requestParser.getModule(requestProtobuf);
         String action = requestParser.getAction(requestProtobuf);
         Map<String, String> filter = new HashMap<String, String>();
         filter.put(IRequestActionHandler.FILTER_KEY_MODULE, module != null ? module : "");
         filter.put(IRequestActionHandler.FILTER_KEY_ACTION, action != null ? action : "");
         IOsgiBootstrap osgiBootstrap = DaspGlobal.getOsgiBootstrap();
         IRequestActionHandler handler = osgiBootstrap.getService(IRequestActionHandler.class,
                 filter);
         if (handler == null) {
             // fallback 1: lookup for wildcard handler
            filter.put(IRequestActionHandler.FILTER_KEY_ACTION, "_");
             handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
         }
         if (handler == null) {
             // fallback 2: lookup for non-action handler
             filter.remove(IRequestActionHandler.FILTER_KEY_ACTION);
             handler = osgiBootstrap.getService(IRequestActionHandler.class, filter);
         }
         if (handler != null) {
             handler.handleRequest(requestProtobuf, topicPublisher);
         } else {
             HettyProtoBuf.Response responseProtobuf = ResponseUtils.response404(requestProtobuf)
                     .build();
             topicPublisher.publishToTopic(responseProtobuf, writeTimeoutMillisecs,
                     TimeUnit.MILLISECONDS);
         }
     }
 
     public void destroy() {
         for (Thread workerThread : workerThreads) {
             try {
                 workerThread.interrupt();
             } catch (Exception e) {
             }
         }
     }
 
     public void start() {
         workerThreads = new Thread[numWorkers];
         for (int i = 1; i <= numWorkers; i++) {
             Thread t = new Thread(HettyRequestHandlerServer.class.getName() + " - " + i) {
                 public void run() {
                     while (!isInterrupted()) {
                         Object obj = queueReader.readFromQueue(readTimeoutMillisecs,
                                 TimeUnit.MILLISECONDS);
                         if (obj != null && obj instanceof byte[]) {
                             HettyProtoBuf.Request requestProtobuf = null;
                             try {
                                 requestProtobuf = HettyProtoBuf.Request.parseFrom((byte[]) obj);
                                 handleRequest(requestProtobuf);
                             } catch (Exception e) {
                                 LOGGER.error(e.getMessage(), e);
                                 HettyProtoBuf.Response response = ResponseUtils.response500(
                                         requestProtobuf, e.getMessage(), e).build();
                                 topicPublisher.publishToTopic(response);
                             }
                         }
                     }
                 }
             };
             t.setDaemon(true);
             t.start();
             workerThreads[i - 1] = t;
         }
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Hetty request handler workers: " + numWorkers + " / Read timeout: "
                     + readTimeoutMillisecs + " / Write timeout: " + writeTimeoutMillisecs);
         }
     }
 }
