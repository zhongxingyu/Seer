 package com.clouway.asynceventbus.spi;
 
 /**
  * @author Mihail Lesikov (mlesikov@gmail.com)
  */
 public interface AsyncEventBus {
 
   /**
    * Fires the given async event to the handlers listening to the event's type.
    * <p>
    * Any exceptions thrown by handlers will be bundled into a
    * completed. An exception thrown by a handler will not prevent other handlers
    * from executing.
    *
    * @param event the event
    */
   void fireEvent(AsyncEvent<?> event);
 }
