 package com.github.legendsdev.legends.library.event;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * @author YoshiGenius
  */
 //TODO Finish making this thread safe
 @SuppressWarnings("unchecked")
 public class EventManager {
     private static int MAX_UPSTREAM = 10;
     private static HashMap<Method, LEventHandler> handlers = new HashMap<>();
     private static HashMap<Class<? extends LegendsEvent>, ArrayList<Method>> methods = new HashMap<>();
     private static HashMap<Method, EventListener> listeners = new HashMap<>();
 
     private static EventManager instance = new EventManager();
 
     public static EventManager getInstance() {
         return instance;
     }
 
     private EventManager() {
     }
 
     public static synchronized void registerEvents(EventListener listener) {
         for(Method method : listener.getClass().getMethods()) {
             if(method.isAnnotationPresent(LEventHandler.class)) {
                 Class[] paramType = method.getParameterTypes();
                 if(paramType.length == 1 &&
                         LegendsEvent.class.isAssignableFrom(paramType[0])) {
                     handlers.put(method, method.getAnnotation(LEventHandler.class));
 
                     // Register upstream events to method
                     Class clazz = paramType[0];
                     for(int i = 0; i < MAX_UPSTREAM; i++) {
                         ArrayList<Method> methodList = methods.get(clazz);
                         if(methodList == null) methodList = new ArrayList<>();
                         methodList.add(method);
                         methods.put(clazz, methodList);
 
                         listeners.put(method, listener);
                         clazz = clazz.getSuperclass();
                         if(clazz == Object.class) break;
                     }
                 }
             }
         }
     }
 
     // TODO this can be optimized by mapping the priorities before the event needs to be called.
     public static synchronized void callEvent(LegendsEvent event) {
         try {
             Class clazz = event.getClass();
             if(methods.containsKey(clazz)) {
                 ArrayList<Method> lowestPriority = new ArrayList<>();
                 ArrayList<Method> lowPriority = new ArrayList<>();
                 ArrayList<Method> normalPriority = new ArrayList<>();
                 ArrayList<Method> highPriority = new ArrayList<>();
                 ArrayList<Method> highestPriority = new ArrayList<>();
 
                // Add upstream methods
                 for(int i = 0; i < MAX_UPSTREAM; i++) {
                     for(Method method : methods.get(clazz)) {
                         LEventHandler handler = handlers.get(method);
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
                     clazz = clazz.getSuperclass();
                     if(clazz == Object.class) break;
                 }
 
                 for(Method method : lowestPriority) {
                     if (method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                         method.invoke(listeners.get(method), event);
                     }
                 }
                 for(Method method : lowPriority) {
                     if (method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                         method.invoke(listeners.get(method), event);
                     }
                 }
                 for(Method method : normalPriority) {
                     if (method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                         method.invoke(listeners.get(method), event);
                     }
                 }
                 for(Method method : highPriority) {
                     if (method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                         method.invoke(listeners.get(method), event);
                     }
                 }
                 for(Method method : highestPriority) {
                     if (method.getParameterTypes()[0].isAssignableFrom(event.getClass())) {
                         method.invoke(listeners.get(method), event);
                     }
                 }
             }
         } catch (IllegalAccessException | InvocationTargetException ignored){
         }
     }
 
 }
