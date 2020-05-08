 // (c) Copyright 3ES Innovation Inc. 2013.  All rights reserved.
 package pl.aleskiewicz.jaxrs;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.ws.rs.container.AsyncResponse;
 import javax.ws.rs.core.Response;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public enum SimpleService {
     INSTANCE;
     private final Logger _logger = LoggerFactory.getLogger(SimpleService.class);
     private final BlockingQueue<AsyncResponse> _suspended = new LinkedBlockingQueue<>();
     private final LinkedList<SimpleEntry> dao = new LinkedList<>();
 
     public Collection<SimpleEntry> list(int limit, int offset) {
 
         if (limit < 0 || offset > dao.size()) {
             return Collections.emptyList();
         } else {
             int endIdx = limit + offset;
             if (endIdx > dao.size()) {
                 endIdx = dao.size();
             }
             if (offset < dao.size()) {
                 offset = 0;
             }
             return dao.subList(offset, endIdx);
         }
     }
     public void addResponseListener(AsyncResponse ar) {
         _logger.debug("Received new Listener: {} adding to {} watchers", ar, _suspended.size());
         _suspended.add(ar);
     }
 
     public void addResponseListener(final AsyncResponse ar, int offset) {
         final Collection<SimpleEntry> recentActivities = list(5, offset);
         if (!recentActivities.isEmpty()) {
             Response simpleResponse = Response.ok(recentActivities).build();
             // try {
             // OutboundJaxrsResponse validResponse = OutboundJaxrsResponse.from(simpleResponse);
             // validResponse.getContext().setEntityAnnotations(
            // SimpleResource.class.getMethod("subscribeForActivity", AsyncResponse.class, String.class)
             // .getAnnotations());
             // ar.resume(validResponse);
             // } catch (NoSuchMethodException | SecurityException e) {
             // _logger.warn("Failed to get annotations for async response: ", e);
             // }
             ar.resume(simpleResponse);
 
         } else {
             addResponseListener(ar);
         }
     }
 
     void notifyListeners(SimpleEntry entry) {
         _logger.debug("Received new activity log: {} sending to {} watchers", entry, _suspended.size());
         dao.add(entry);
         AsyncResponse asyncResponse = null;
         while ((asyncResponse = _suspended.poll()) != null) {
             _logger.debug("sending to watcher {}", _suspended.size());
             asyncResponse.resume(Response.ok(entry).build());
         }
     }
 
     public void notifyListeners(String standardInfo, String detailedInfo) {
         SimpleEntry entry = new SimpleEntry(standardInfo, detailedInfo);
         notifyListeners(entry);
     }
 }
