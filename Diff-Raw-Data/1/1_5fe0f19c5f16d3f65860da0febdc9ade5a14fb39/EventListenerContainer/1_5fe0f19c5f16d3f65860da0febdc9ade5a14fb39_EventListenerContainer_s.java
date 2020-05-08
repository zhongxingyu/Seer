 package com.dafttech.eventmanager;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 public class EventListenerContainer {
     volatile protected boolean isStatic;
     volatile protected Object eventListener;
     volatile protected Method method;
     volatile protected int priority;
 
     volatile private Object filter;
 
     protected EventListenerContainer(boolean isStatic, Object eventListener, Method method, EventListener annotation) {
         this.isStatic = isStatic;
         this.eventListener = eventListener;
         this.method = method;
         this.priority = annotation.priority();
         this.filter = getFilterContainer(isStatic, isStatic ? (Class<?>) this.eventListener : this.eventListener.getClass(), annotation.filter());
     }
 
     protected Object[] getFilter() {
         try {
             if (filter != null) {
                 if (filter instanceof Field) return (Object[]) ((Field) filter).get(isStatic ? null : eventListener);
                 if (filter instanceof Method) return (Object[]) ((Method) filter).invoke(isStatic ? null : eventListener);
             }
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         }
         return new Object[0];
     }
 
     @Override
     public boolean equals(Object paramObject) {
         if (paramObject instanceof EventListenerContainer) {
             return paramObject == this;
         } else {
             return paramObject == eventListener;
         }
     }
 
     private static final Object getFilterContainer(boolean isStatic, Class<?> filterClass, String filterName) {
         if (!filterName.equals("")) {
             if (filterName.contains(".")) {
                 try {
                     filterClass = Class.forName(filterName.substring(0, filterName.lastIndexOf('.')));
                     isStatic = true;
                 } catch (ClassNotFoundException e) {
                     e.printStackTrace();
                 }
             }
             for (Field field : EventManager.getAnnotatedFields(filterClass, EventFilter.class, Object[].class)) {
                 if ((!isStatic || Modifier.isStatic(field.getModifiers())) && field.getAnnotation(EventFilter.class).name().equals(filterName))
                     return field;
             }
             for (Method method : EventManager.getAnnotatedMethods(filterClass, EventFilter.class, Object[].class)) {
                 if ((!isStatic || Modifier.isStatic(method.getModifiers())) && method.getAnnotation(EventFilter.class).name().equals(filterName))
                     return method;
             }
         }
         return null;
     }
 }
