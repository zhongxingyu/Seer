 package net.premereur.mvp.core.guice;
 
 import net.premereur.mvp.core.EventBus;
 import net.premereur.mvp.core.Presenter;
 import net.premereur.mvp.core.View;
 
 /**
  * An abstract base implementation for {@link Presenter}s that are dependency-managed by Guice.
  * 
  * @author gpremer
  * 
  * @param <V> The type of View the Presenter manages
  * @param <E> The type of EventBus (segment) most events are sent to
  */
 public abstract class BasePresenter<V extends View, E extends EventBus> implements Presenter<V, E> {
 
     private final E eventBus;
 
     private final V view;
 
     /**
      * Creates a base presenter for the given event bus segment and view.
      * @param eventBus an event bus segment
      * @param view the managed view
      */
     public BasePresenter(final E eventBus, final V view) {
         this.eventBus = eventBus;
         this.view = view;
     }
 
     @Override
     public void setEventBus(final E eventBus) {
         // To respect interface
     }
 
     /**
      * Returns the default event bus segment.
      * 
      * @return an event bus
      */
     protected E getEventBus() {
         return eventBus;
     }
 
     /**
      * Returns the event bus associated with the presenter cast to the type given as an argument. This is meant to be used for composite busses that actually
      * use the requested bus segment.
      * 
      * @param <Bus> The type of the bus segment
      * @param eventBusClass The class of the type of the bus segment
      * @return the event bus
      */
     @SuppressWarnings("unchecked")
    protected <Bus extends EventBus> Bus getEventBus(Class<Bus> eventBusClass) {
         return (Bus) getEventBus();
     }
 
     @Override
     public void setView(final V view) {
         // To respect interface
     }
 
     protected V getView() {
         return view;
     }
 
 }
