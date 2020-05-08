 package gx.realtime;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * An event target which can dispatch events to interested listeners.
  * Listeners subscribe via addEventListener.
  */
 public abstract class EventTarget
 {
     /**
      * All event handlers, indexed by event type.
      */
     protected Map<EventType, Set<EventHandler>> eventHandlers = new HashMap<>();
 
     /**
      * The parents of this eventTarget.
      */
     protected Set<EventTarget> parents = new HashSet();
 
     /**
      * Adds an event listener to the event target. The same handler can only
      * be added once per the type. Even if you add the same handler multiple
      * times using the same type then it will only be called once when the
      * event is dispatched.
      * @param type The type of the event to listen for.
      * @param handler The function to handle the event. The handler can also
      *     be an object that implements the handleEvent method which takes
      *     the event object as argument.
      */
     public <T extends Event> void addEventListener(EventType type, EventHandler<T> handler)
     {
         if (!eventHandlers.containsKey(type)){
             eventHandlers.put(type, new HashSet<>());
         }
         
         eventHandlers.get(type).add(handler);
     }
 
     /**
      * Removes an event listener from the event target. The handler must be
      * the same object as the one added. If the handler has not been added
      * then nothing is done.
      * @param type The type of the event to listen for.
      * @param handler The function to handle the event. The handler can
      *     also be an object that implements the handleEvent method which
      *     takes the event object as argument.
      */
     public <T extends Event> void removeEventListener(EventType type, EventHandler<T> handler)
     {
         Set<EventHandler> handlers = eventHandlers.get(type);
         if (handlers != null){
             handlers.remove(handler);
         }
     }
     
     /**
      * Dispatches the given event to this object.
      * The corresponding EventHandlers are executed.
      * If the event is a BaseModelEvent that is bubbling, the event is passed to the parents of this EventTarget afterwards.
      * @param event The event object, containing any necessary information.
      */
     protected void fireEvent(Event event)
     {
         System.out.println("Firing event");
         if(event instanceof BaseModelEvent){
             System.out.println("This is a basemodelEvent");
             fireEvent((BaseModelEvent) event);
        } else if(this.equals(event.getTarget())) {
             System.out.println("This is ont a BaseModelEvent");
             Set<EventHandler> handlers = eventHandlers.get(event.getType());
             if (handlers != null){
                 for (EventHandler handler : handlers){
                     handler.handleEvent(event);
                 }
             }
         }
     }
 
     /**
      * Dispatches the given BaseModelEvent to this object. The corresponding EventHandlers are executed. If the event is bubbling,
      * the event is passed to the parents of this EventTarget afterwards.
      * @param event The BaseModelEvent, containing any necessary information.
      */
     private void fireEvent(BaseModelEvent event){
         System.out.println("firing event on basemodelEvent" + event.getTarget());
         if(event.isFirstVisit(this)){
             System.out.println("This is the first visit");
             event.addBubbledNode(this);
 
             //execute eventhandlers of this EventTarget if needed.
             Set<EventHandler> handlers = eventHandlers.get(event.getType());
             if (this.equals(event.getTarget()) && handlers != null){
                 for (EventHandler handler : handlers){
                     handler.handleEvent(event);
                 }
             }
 
             //Bubble if needed.
             if(event.bubbles()){
                 for(EventTarget parent : parents){
                     parent.fireEvent(event);
                 }
             }
         }
     }
 
     /**
      * Add a parent of this EventTarget.
      * @param parent The parent of this EventTarget.
      */
     public void addParent(EventTarget parent){
         parents.add(parent);
     }
 
     /**
      * Remove the given EventTarget from the set of parents.
      * @param parent The parent that should be removed.
      */
     public void removeParent(EventTarget parent){
         parents.remove(parent);
     }
 }
