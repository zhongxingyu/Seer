 package ddth.dasp.servlet.thrift.api;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.thrift.TProcessor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.servlet.thrift.ThriftUtils;
 import ddth.dasp.servlet.thrift.serverfactory.IServerFactory;
 import ddth.dasp.servlet.thrift.serverfactory.ThreadedSelectorServerFactory;
 import ddth.dasp.servlet.thrift.serverfactory.ThreadedServerFactory;
 import ddth.dasp.servlet.utils.NetUtils;
 
 public class DaspThriftJsonApiServer {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DaspThriftJsonApiServer.class);
 
     private String portStr = "9092";
     private boolean nonBlockingServer = true;
     private int clientTimeoutMillisecs = 1000; // 1 second
     private int maxFrameSize = 1 * 1024 * 1024; // 1 Mb
    private long maxReadBufferSize = 16 * 1024 * 1024; // 16 Mb
 
     public String getPort() {
         return portStr;
     }
 
     public DaspThriftJsonApiServer setPort(String portStr) {
         this.portStr = portStr;
         return this;
     }
 
     public int getMaxFrameSize() {
         return maxFrameSize;
     }
 
     public DaspThriftJsonApiServer setMaxFrameSize(int maxFrameSize) {
         this.maxFrameSize = maxFrameSize;
         return this;
     }
 
     public long getMaxReadBufferSize() {
         return maxReadBufferSize;
     }
 
     public DaspThriftJsonApiServer setMaxReadBufferSize(long maxReadBufferSize) {
         this.maxReadBufferSize = maxReadBufferSize;
         return this;
     }
 
     public int getClientTimeoutMillisecs() {
         return clientTimeoutMillisecs;
     }
 
     public DaspThriftJsonApiServer setClientTimeoutMillisecs(int clientTimeoutMillisecs) {
         this.clientTimeoutMillisecs = clientTimeoutMillisecs;
         return this;
     }
 
     public boolean isNonBlockingServer() {
         return nonBlockingServer;
     }
 
     public DaspThriftJsonApiServer setNonBlockingServer(boolean nonBlockingServer) {
         this.nonBlockingServer = nonBlockingServer;
         return this;
     }
 
     /**
      * {@inheritDoc}
      */
     public void start() {
         Integer port = 9092;
         if (!StringUtils.isBlank(portStr)) {
             // find free port
             String[] tokens = portStr.split("[\\s,]+");
             int[] ports = new int[tokens.length];
             for (int i = 0; i < tokens.length; i++) {
                 ports[i] = Integer.parseInt(tokens[i]);
             }
             port = NetUtils.getFreePort(ports);
         }
 
         TProcessor processor = new DaspJsonApi.Processor<DaspJsonApi.Iface>(new JsonApiHandler());
         IServerFactory serverFactory = nonBlockingServer ? new ThreadedSelectorServerFactory(port,
                 processor, clientTimeoutMillisecs, maxFrameSize, maxReadBufferSize)
                 : new ThreadedServerFactory(port, processor, clientTimeoutMillisecs, maxFrameSize);
         ThriftUtils.startThriftServer(serverFactory);
         LOGGER.info("Thrift interface " + serverFactory + " is listening on port " + port);
     }
 
     /**
      * {@inheritDoc}
      */
     public void destroy() {
     }
 }
