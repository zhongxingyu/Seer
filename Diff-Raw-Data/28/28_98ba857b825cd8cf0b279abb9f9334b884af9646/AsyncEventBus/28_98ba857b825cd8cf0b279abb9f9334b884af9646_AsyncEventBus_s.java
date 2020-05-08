 package com.clouway.asynctaskscheduler.spi;
 
 /**
  * @author Mihail Lesikov (mlesikov@gmail.com)
  */
 public interface AsyncEventBus {
 
   /**
    * Fires the given async event to the handlers listening to the event's type.
    * <p/>
    * Any exceptions thrown by handlers will be bundled into a
    * completed. An exception thrown by a handler will not prevent other handlers
    * from executing.
    *
    * @param event the event
    */
   void fireEvent(AsyncEvent<?> event);
 
   /**
    * Fires the given async event to the handlers listening to the event's type after specified delay in mills
    * <p/>
    * Any exceptions thrown by handlers will be bundled into a
    * completed. An exception thrown by a handler will not prevent other handlers
    * from executing.
    *
    * @param event the event
    */
   void fireEvent(AsyncEvent<?> event, long delayMills);
 
   /**
    * Fires the given async event transactionless (no matter of the transaction success)
    * to the handlers listening to the event's type.
    * <p/>
    * Any exceptions thrown by handlers will be bundled into a
    * completed. An exception thrown by a handler will not prevent other handlers
    * from executing.
    *
    * @param event the event
    */
  void fireTrasnactionlessEvent(AsyncEvent<?> event);
 
   /**
     * Fires the given async event transactionless (no matter of the transaction success)
     * to the handlers listening to the event's type after specified delay in mills
     * <p/>
     * Any exceptions thrown by handlers will be bundled into a
     * completed. An exception thrown by a handler will not prevent other handlers
     * from executing.
     *
     * @param event the event
     */
  void fireTrasnactionlessEvent(AsyncEvent<?> event, long delayMills);
 
 }
