 /*
  * Copyright 2007-2009 Georgina Stegmayer, Milagros Gutiérrez, Jorge Roa
  * y Milton Pividori.
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
 package frsf.cidisi.faia.simulator.events;
 
 import java.util.Hashtable;
 import java.util.Vector;
 
 public class SimulatorEventNotifier {
 
     private static Hashtable<EventType, Vector<EventHandler>> eventHandlers =
             new Hashtable<EventType, Vector<EventHandler>>();
 
     public static void runEventHandlers(EventType eventType, Object[] params) {
 
        if (!eventHandlers.contains(eventType)) {
             return;
         }
 
         for (EventHandler eventHandler : eventHandlers.get(eventType)) {
             eventHandler.runEventHandler(params);
         }
     }
 
     public static void SubscribeEventHandler(EventType eventType, EventHandler eventHandler) {
         if (!eventHandlers.contains(eventType)) {
             eventHandlers.put(eventType, new Vector<EventHandler>());
         }
 
         Vector<EventHandler> eventHandlerList =
                 eventHandlers.get(eventType);
 
         eventHandlerList.add(eventHandler);
     }
 
     public static void UnsubscribeEventHandler(EventType eventType, EventHandler eventHandler) {
         if (!eventHandlers.contains(eventType)) {
             return;
         }
 
         Vector<EventHandler> eventHandlerList =
                 eventHandlers.get(eventType);
 
         eventHandlerList.remove(eventHandler);
     }
 
     public static void ClearEventHandlers() {
         eventHandlers.clear();
     }
 }
