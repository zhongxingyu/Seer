 package com.timgroup.tucker.info.httpserver;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.UnknownHostException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.sun.net.httpserver.HttpServer;
 import com.timgroup.tucker.info.ApplicationInformationHandler;
 import com.timgroup.tucker.info.Health;
 import com.timgroup.tucker.info.Stoppable;
 import com.timgroup.tucker.info.status.StatusPageGenerator;
 
 import static java.util.concurrent.Executors.newFixedThreadPool;
 
 public class ApplicationInformationServer {
 
     public static ApplicationInformationServer create(int port, ApplicationInformationHandler handler) throws IOException {
         return new ApplicationInformationServer(port, handler);
     }
 
     public static ApplicationInformationServer create(int port, StatusPageGenerator statusPage, Stoppable stoppable, Health health) throws IOException {
         return create(port, new ApplicationInformationHandler(statusPage, stoppable, health));
     }
 
     public static ApplicationInformationServer create(int port, StatusPageGenerator statusPage, Health health) throws IOException {
         return ApplicationInformationServer.create(port, statusPage, Stoppable.ALWAYS_STOPPABLE, health);
     }
 
     private final URI base;
     private final HttpServer server;
 
     private ApplicationInformationServer(int port, ApplicationInformationHandler handler) throws IOException {
         base = constructBaseUri(port);
         server = HttpServer.create(new InetSocketAddress(port), 0);
         server.createContext(base.getPath(), new ApplicationInformationHttpHandler(handler, base));
         server.setExecutor(newFixedThreadPool(5, new TuckerThreadFactory()));
     }
 
     private URI constructBaseUri(int port) throws UnknownHostException, IOException {
         String hostName = InetAddress.getLocalHost().getHostName();
         try {
             return new URI("http", null, hostName, port, "/info", null, null);
         } catch (URISyntaxException e) {
             throw new IOException("disappointing error constructing base URI for " + hostName, e);
         }
     }
 
     public URI getBase() {
         return base;
     }
 
     public void start() {
         server.start();
     }
 
     public void stop() {
         server.stop(0);
     }
 
 
     private static class TuckerThreadFactory implements ThreadFactory {
         final AtomicInteger threadNumber = new AtomicInteger(1);
 
         @Override
         public Thread newThread(Runnable r) {
            Thread thread = new Thread("Tucker-" + threadNumber.getAndIncrement());
             thread.setDaemon(false);
             return thread;
         }
     }
 }
