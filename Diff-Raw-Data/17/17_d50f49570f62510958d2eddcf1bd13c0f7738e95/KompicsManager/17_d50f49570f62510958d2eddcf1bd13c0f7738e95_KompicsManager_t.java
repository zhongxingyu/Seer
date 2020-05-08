 package se.sics.kompics.storm.bolt;
 
 
 import se.sics.kompics.*;
 
 /**
  * Entry point to Kompics execution for somewhat flexible configurations.
  * Mind that KompicsManager is a (thread-safe) singleton and as a result only one instance will be shared
  * in each JVM process (aka Storm worker). This means that multiple independent tasks inside the same worker process
  * will be using the same instance to access the Kompics api.
  */
 public class KompicsManager extends ComponentDefinition {
 
    private final static KompicsManager instance = new KompicsManager();
 
    public KompicsManager() {
         if (Kompics.getScheduler() == null) {
             Kompics.createAndStart(KompicsManager.class, Runtime.getRuntime().availableProcessors());
         }
     }
 
     public static KompicsManager getInstance() {
         return instance;
     }
 
     public synchronized Component _create(
             Class<? extends ComponentDefinition> definition) {
         return create(definition);
     }
 
 
     public synchronized <E extends Event, P extends PortType> void _subscribe(
             Handler<E> handler, Port<P> port) {
         subscribe(handler, port);
     }
 
     public synchronized <P extends PortType> void _trigger(Event event, Port<P> port) {
         trigger(event, port);
     }
 
     public synchronized <P extends PortType> Channel<P> _connect(
             Positive<P> positive, Negative<P> negative) {
         return connect(positive, negative);
     }
 
 }
