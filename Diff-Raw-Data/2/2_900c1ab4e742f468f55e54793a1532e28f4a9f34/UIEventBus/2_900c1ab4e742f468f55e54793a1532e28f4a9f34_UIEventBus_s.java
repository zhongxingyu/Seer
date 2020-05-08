 package com.od.swing.eventbus;
 
 import com.od.swing.util.UIUtilities;
 
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick
  * Date: 12-Dec-2010
  * Time: 10:59:02
  */
 public class UIEventBus {
 
     private static UIEventBus singleton;
 
     private Map<Class, List> listenerClassToListeners = new HashMap<Class, List>();
 
     private UIEventBus() {}
 
     public <E> boolean addEventListener(Class<E> listenerInterface, E listener) {
         List listeners = getListenerList(listenerInterface);
 
         boolean result = false;
         if (! listeners.contains(listener)) {
             listeners.add(listener);
             result = true;
         }
         return result;
     }
 
     public <E> boolean removeEventListener(Class<E> listenerInterface, E listener) {
         List listeners = getListenerList(listenerInterface);
         boolean result = false;
         //use a set to search, faster than list even with creation overhead
         return listeners.remove(listener);
     }
 
     public <E> void fireEvent(final Class<E> listenerClass, final EventSender<E> eventSender) {
         //run in event thread, if this is not already the event thread
         UIUtilities.runInDispatchThread(
             new Runnable() {
                 public void run() {
                    List listeners = listenerClassToListeners.get(listenerClass);
                     LinkedList snapshot = new LinkedList(listeners);
                     for (Object o : snapshot) {
                         try {
                             eventSender.sendEvent((E) o);
                         } catch (Throwable t) {
                             t.printStackTrace();
                         }
                     }
                 }
             }
         );
     }
 
     private <E> List getListenerList(Class<E> listenerInterface) {
         List listeners = listenerClassToListeners.get(listenerInterface);
         if ( listeners == null ) {
             listeners = new LinkedList<E>();
             listenerClassToListeners.put(listenerInterface, listeners);
         }
         return listeners;
     }
 
     public synchronized static UIEventBus getInstance() {
         if ( singleton == null) {
             singleton = new UIEventBus();
         }
         return singleton;
     }
 
 }
