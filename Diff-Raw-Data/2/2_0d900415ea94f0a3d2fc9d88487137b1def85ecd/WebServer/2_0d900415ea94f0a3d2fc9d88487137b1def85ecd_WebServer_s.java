 package org.polyglotted.webapp.launcher;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.NCSARequestLog;
 import org.eclipse.jetty.server.RequestLog;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.HandlerCollection;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.RequestLogHandler;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 import org.eclipse.jetty.util.thread.ThreadPool;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 public class WebServer {
     private static final String LOG_PATH = "target/accesslogs/yyyy_mm_dd.request.log";
     private static final String WEB_XML = "webapp/WEB-INF/web.xml";
     private static final String PROJECT_RELATIVE_PATH_TO_WEBAPP = "src/main/webapp";
 
     public static interface WebContext {
 
         public File getWarPath();
 
         public String getContextPath();
     }
 
     private Server server;
 
     public void start() throws Exception {
         server = new Server();
         server.setThreadPool(createThreadPool());
         server.addConnector(createConnector());
         server.setHandler(createHandlers());
         server.setStopAtShutdown(true);
         server.start();
     }
 
     public void join() throws InterruptedException {
         server.join();
     }
 
     public void stop() throws Exception {
         server.stop();
     }
 
     private ThreadPool createThreadPool() {
         QueuedThreadPool _threadPool = new QueuedThreadPool();
         _threadPool.setMinThreads(intSystemProperty("jetty.http.minThreads", "10"));
         _threadPool.setMaxThreads(intSystemProperty("jetty.http.maxThreads", "100"));
         return _threadPool;
     }
 
     private SelectChannelConnector createConnector() {
         SelectChannelConnector _connector = new SelectChannelConnector();
         _connector.setPort(intSystemProperty("jetty.http.port", "8080"));
         _connector.setHost(System.getProperty("jetty.http.bindInterface"));
         return _connector;
     }
 
     private int intSystemProperty(String prop, String def) {
         return Integer.parseInt(System.getProperty(prop, def));
     }
 
     private HandlerCollection createHandlers() {
         WebAppContext _ctx = new WebAppContext();
        _ctx.setContextPath("/");
 
         if (isRunningInIde()) {
             _ctx.setWar(PROJECT_RELATIVE_PATH_TO_WEBAPP);
         } else {
             _ctx.setWar(getShadedWarUrl());
         }
 
         List<Handler> _handlers = new ArrayList<Handler>();
         _handlers.add(_ctx);
 
         HandlerList _contexts = new HandlerList();
         _contexts.setHandlers(_handlers.toArray(new Handler[0]));
 
         RequestLogHandler _log = new RequestLogHandler();
         _log.setRequestLog(createRequestLog());
 
         HandlerCollection _result = new HandlerCollection();
         _result.setHandlers(new Handler[] { _contexts, _log });
 
         return _result;
     }
 
     private RequestLog createRequestLog() {
         NCSARequestLog _log = new NCSARequestLog();
         File _logPath = new File(System.getProperty("webapp.log.path", LOG_PATH));
         _logPath.getParentFile().mkdirs();
         _log.setFilename(_logPath.getPath());
         _log.setRetainDays(90);
         _log.setExtended(false);
         _log.setAppend(true);
         _log.setLogTimeZone("GMT");
         _log.setLogLatency(true);
         return _log;
     }
 
     private boolean isRunningInIde() {
         return "true".equalsIgnoreCase(System.getProperty("webapp.in.ide", "false"));
     }
 
     private String getShadedWarUrl() {
         String _urlStr = getResource(WEB_XML).toString();
         return _urlStr.substring(0, _urlStr.length() - 15);
     }
 
     private URL getResource(String aResource) {
         return Thread.currentThread().getContextClassLoader().getResource(aResource);
     }
 }
