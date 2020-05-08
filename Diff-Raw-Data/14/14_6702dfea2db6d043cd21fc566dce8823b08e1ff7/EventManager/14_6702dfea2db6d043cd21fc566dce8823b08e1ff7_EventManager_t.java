 /*
  * Copyright (C) 2013 mewin<mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.mewin.jhttp.event;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class EventManager
 {
     private HashMap<Class<? extends Event>, ArrayList<Handler>> registeredEvents;
     
     public EventManager()
     {
         registeredEvents = new HashMap<Class<? extends Event>, ArrayList<Handler>>();
     }
     
     public void registerEvents(Listener listener)
     {
         for (Method m : listener.getClass().getDeclaredMethods())
         {
            EventHandler eh = m.getAnnotation(EventHandler.class);

            if (eh != null)
             {
                registerHandler(m, eh.priority(), listener);
             }
         }
     }
     
     private void registerHandler(Method method, int priority, Listener listener)
     {
         Handler handler = new Handler(method, priority, listener);
         Class[] para = method.getParameterTypes();
         
         if (para.length < 1 || !Event.class.isAssignableFrom(para[0]))
         {
             throw new RuntimeException("Method " + method.getName() + " is not a valid EventHandler method.");
         }
         else
         {
             ArrayList<Handler> list;
             if (registeredEvents.containsKey(para[0]))
             {
                 list = registeredEvents.get(para[0]);
             }
             else
             {
                 list = new ArrayList<Handler>();
                 registeredEvents.put(para[0], list);
             }
             
             insertHandler(list, handler);
         }
     }
     
     private void insertHandler(ArrayList<Handler> list, Handler handler) // sortieren durch Einfügen o.0
     {
         for (int i = 0; i < list.size(); i++)
         {
             if (list.get(i).getPriority() > handler.getPriority())
             {
                 list.add(i, handler);
                 return;
             }
         }
         list.add(handler);
     }
     
     public void handleEvent(Event e)
     {
         if (registeredEvents.containsKey(e.getClass()))
         {
             ArrayList<Handler> handlers = registeredEvents.get(e.getClass());
             
             for (Handler handler : handlers)
             {
                 try
                 {
                     handler.getMethod().invoke(handler.getListener(), e);
                 }
                 catch(Exception ex)
                 {
                     throw new RuntimeException("Could not call event method.", ex);
                 }
             }
         }
     }
     
     private class Handler
     {
         private Method method;
         private int priority;
         private Listener listener;
         
         public Handler(Method method, int priority, Listener listener)
         {
             this.method = method;
             this.priority = priority;
             this.listener = listener;
         }
         
         public Method getMethod()
         {
             return this.method;
         }
         
         public int getPriority()
         {
             return this.priority;
         }
         
         public Listener getListener()
         {
             return this.listener;
         }
     }
 }
