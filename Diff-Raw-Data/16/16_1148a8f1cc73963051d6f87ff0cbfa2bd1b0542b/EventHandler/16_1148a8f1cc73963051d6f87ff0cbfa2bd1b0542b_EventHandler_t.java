 package com.djdch.simpleeventsystem.event;
 
 import com.djdch.simpleeventsystem.listener.ConsoleOutputListener;
 import com.djdch.simpleeventsystem.listener.ExceptionListener;
 import com.djdch.simpleeventsystem.listener.Listener;
 import com.djdch.simpleeventsystem.util.Validate;
 
 public class EventHandler {
     public static synchronized void registerListener(Listener listener) {
         Validate.notNull(listener, "Listener cannot be null");
 
        boolean found = false;

         if (listener instanceof ConsoleOutputListener) {
             ConsoleOutputEvent.getHandlerList().register(listener);

            found = true;
        }

        if (listener instanceof ExceptionListener) {
             ExceptionEvent.getHandlerList().register(listener);

            found = true;
        }

        if (!found) {
             throw new IllegalArgumentException("Unknown listener");
         }
     }
 
     public static synchronized void dispatch(Event event) {
         HandlerList handlers = event.getHandlers();
         Listener[] listeners = handlers.getListeners();
 
         for (Listener listener : listeners) {
             try {
                 callEvent(listener, event);
             } catch (Throwable ex) {
                 // TODO: Handle this
             }
         }
     }
 
     private static void callEvent(Listener listener, Event event) {
         switch (event.getType()) {
             case CONSOLE_OUTPUT:
                 ((ConsoleOutputListener) listener).onConsoleOutput((ConsoleOutputEvent) event);
                 break;
 
             case EXCEPTION:
                 ((ExceptionListener) listener).onException((ExceptionEvent) event);
                 break;
 
             default:
                 throw new IllegalArgumentException("Unknown event type");
         }
     }
 }
