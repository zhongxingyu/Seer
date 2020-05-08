 package com.github.legendsdev.legends.library.event;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author YoshiGenius
  */
 //TODO Finish making this thread safe
 public class EventManager {
     private static HashMap<Method, EventHandler> handlers = new HashMap<>();
    private static HashMap<LegendsEvent, ArrayList<Method>> methods = new HashMap<>();
     private static HashMap<LegendsEvent, ArrayList<EventListener>> listeners = new HashMap<>();
 
     private static EventManager instance = new EventManager();
 
     public static EventManager getInstance() {
         return instance;
     }
 
     private EventManager() {
     }
 
     public static synchronized void registerEvents(EventListener listener) {
         for(Method method : listener.getClass().getMethods()) {
             if(method.isAnnotationPresent(EventHandler.class)) {
                 Class[] paramType = method.getParameterTypes();
                 if(paramType.length == 1 &&
                         LegendsEvent.class.isAssignableFrom(paramType[0]) &&
                         method.isAccessible()) {
                     handlers.put(method, method.getAnnotation(EventHandler.class));
                 }
             }
         }
     }
 
     // TODO this can be optimized by mapping the priorities before the event needs to be called.
     public static synchronized void callEvent(LegendsEvent event) {
         try {
            if(methods.containsKey(event)) {
                 ArrayList<Method> lowestPriority = new ArrayList<>();
                 ArrayList<Method> lowPriority = new ArrayList<>();
                 ArrayList<Method> normalPriority = new ArrayList<>();
                 ArrayList<Method> highPriority = new ArrayList<>();
                 ArrayList<Method> highestPriority = new ArrayList<>();
 
                for(Method method : methods.get(event)) {
                     EventHandler handler = handlers.get(method);
                     switch(handler.priority()) {
                         case LOWEST:
                             lowestPriority.add(method);
                         break;
                         case LOW:
                             lowPriority.add(method);
                         break;
                         case NORMAL:
                             normalPriority.add(method);
                         break;
                         case HIGH:
                             highPriority.add(method);
                         break;
                         case HIGHEST:
                             highestPriority.add(method);
                         break;
                     }
                 }
 
                 for(Method method : lowestPriority) method.invoke(event);
                 for(Method method : lowPriority) method.invoke(event);
                 for(Method method : normalPriority) method.invoke(event);
                 for(Method method : highPriority) method.invoke(event);
                 for(Method method : highestPriority) method.invoke(event);
             }
         } catch (IllegalAccessException | InvocationTargetException ignored){
         }
     }
 
 }
