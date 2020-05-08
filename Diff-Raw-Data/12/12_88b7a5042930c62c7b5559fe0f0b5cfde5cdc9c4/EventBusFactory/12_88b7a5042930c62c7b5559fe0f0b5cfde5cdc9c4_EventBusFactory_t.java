 package net.premereur.mvp.core;
 
 /**
  * 
  * EventBusFactories create event bus implementations. These implementations are synthetic classes that are typically generated on the fly.
  * 
  * @author gpremer
  * 
  */
 public interface EventBusFactory {
 
     /**
     * Creates an event bus implementation. Requires at least one event bus segment and potentially more. The returned object will implement <em>all</em> of
     * the event bus interfaces.
      * 
     * @param <E> The type of the initial event bus segment
     * @param initialEventBusIntf The first event bus segment
     * @param segmentIntfs All other event bus segments. Due to restriction in the Java 5 & 6 generics implementation, it is not possible to define a tighter
     *            type for this parameter. However, all interfaces have to extend {@link EventBus}.
      * @return an event bus implementing all supplied interfaces and all event methods contained therein.
      */
     <E extends EventBus> E createEventBus(Class<E> initialEventBusIntf, Class<?>... segmentIntfs);
 
 }
