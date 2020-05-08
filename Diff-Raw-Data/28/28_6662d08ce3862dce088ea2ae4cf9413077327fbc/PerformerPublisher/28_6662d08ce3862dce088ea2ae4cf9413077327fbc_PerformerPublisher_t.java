 package br.usp.ime.arranger.tests.functional;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.xml.ws.Endpoint;
 
 import br.usp.ime.arranger.service.Performer;
 import br.usp.ime.arranger.service.PerformerImpl;
 
 public class PerformerPublisher {
 
     private static final int PORT = 8081;
     private static final AtomicInteger INSTANCES = new AtomicInteger(0);
     private List<Endpoint> endpoints = new ArrayList<>();
     private List<String> wsdls = new ArrayList<>();
 
     public static void main(final String[] args) {
         final PerformerPublisher publisher = new PerformerPublisher();
        publisher.publish();
        publisher.publish();
     }
 
     public String publish() {
         return publish(new PerformerImpl());
     }
 
     public String publish(final Performer performer) {
         final String address = getAddress(INSTANCES.getAndIncrement());
        return publish(performer, address);
     }
 
    private String publish(final Performer performer, final String address) {
         Endpoint endpoint = Endpoint.publish(address, performer);
         endpoints.add(endpoint);

        final String wsdl = address + "?wsdl";
        wsdls.add(wsdl);
        return wsdl;
     }
 
     public void stopAll() {
         for (Endpoint endpoint : endpoints) {
             endpoint.stop();
         }
         endpoints.clear();
         wsdls.clear();
         INSTANCES.set(0);
     }
 
     private String getAddress(final int instance) {
         return "http://127.0.0.1:" + PORT + "/performer" + instance
                 + "/endpoint";
     }
 }
