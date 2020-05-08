 /*
  * Copyright (c) 2008, 2009 by Xuggle Incorporated. All rights reserved.
  * 
  * This file is part of Xuggler.
  * 
  * You can redistribute Xuggler and/or modify it under the terms of the GNU
  * Affero General Public License as published by the Free Software Foundation,
  * either version 3 of the License, or (at your option) any later version.
  * 
  * Xuggler is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with Xuggler. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.xuggle.utils.event;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import com.xuggle.utils.queue.ArrayQueue;
 
 /**
  * A synchronous implementation of {@link IEventDispatcher}.
  * <p>
  * Callers must ensure that all calls to {@link #dispatchEvent(IEvent)} are
  * synchronized.
  * </p>
  * <p>
  * This object that any {@link IEvent} dispatched to this
  * {@link IEventDispatcher} by an {@link IEventHandler} currently being executed
  * by this {@link IEventDispatcher} will not actually be handled until the
  * current {@link IEventHandler} has returned.
  * </p>
  * <p>
  * That means, even if a handler caused a new {@link IEvent} to be dispatched,
  * the new {@link IEvent} will be queues until the handler completely unwinds.
  * </p>
  * 
  * @author aclarke
  * 
  */
 public class SynchronousEventDispatcher implements IEventDispatcher
 {
   private static class ClassHandler
   {
     private InternalKey[] mKeys = null;
     private Object[] mHandlers = null;
     private final SortedMap<Integer, List<InternalKey>> mPriorities = new TreeMap<Integer, List<InternalKey>>(
         new Comparator<Integer>()
         {
           public int compare(Integer o1, Integer o2)
           {
             // reverse the sort order
             return o2.compareTo(o1);
           }
         });
 
     private void refreshHandlers()
     {
       final ArrayList<Object> handlers = new ArrayList<Object>();
       final ArrayList<InternalKey> keys = new ArrayList<InternalKey>();
       final Set<Integer> priorityKeys = mPriorities.keySet();
       final Iterator<Integer> orderedKeys = priorityKeys.iterator();
       while (orderedKeys.hasNext())
       {
         final Integer priority = orderedKeys.next();
         final List<InternalKey> priHandlers = mPriorities.get(priority);
         if (priHandlers != null)
         {
           for (InternalKey reference : priHandlers)
           {
             handlers.add(reference.getHandler());
             keys.add(reference);
           }
         }
       }
       mHandlers = new Object[keys.size()];
       mHandlers = handlers.toArray(mHandlers);
       mKeys = new InternalKey[keys.size()];
       mKeys = keys.toArray(mKeys);
     }
 
     public void addEventHandler(InternalKey key)
     {
       final int priority = key.getPriority();
       List<InternalKey> handlers = mPriorities.get(priority);
       if (handlers == null)
       {
         handlers = new LinkedList<InternalKey>();
         mPriorities.put(priority, handlers);
       }
       handlers.add(key);
       refreshHandlers();
     }
 
     /**
      * Remove an event handler
      * 
      * @param key the key as returned from
      *        {@link IEventDispatcher#addEventHandler(int, Class, IEventHandler, boolean)}
      *        .
      * 
      * @throws IndexOutOfBoundsException if handler wasn't found for removal
      */
     public void removeEventHandler(InternalKey key)
     {
       final List<InternalKey> handlers = mPriorities.get(key.getPriority());
       if (handlers == null)
       {
         throw new IndexOutOfBoundsException();
       }
       Iterator<InternalKey> iter = handlers.iterator();
       int numRemoved = 0;
       while (iter.hasNext() && numRemoved == 0)
       {
         InternalKey ref = iter.next();
         if (key.equals(ref))
         {
           iter.remove();
           numRemoved++;
           break;
         }
       }
       if (handlers.size() == 0)
         // remove the list if empty
         mPriorities.remove(key.getPriority());
       refreshHandlers();
       if (numRemoved != 1)
         // we asked for a handler to be removed, but none was
         throw new IndexOutOfBoundsException();
     }
 
     public int getNumHandlers()
     {
       return mKeys == null ? 0 : mKeys.length;
     }
 
     public InternalKey[] getSortedKeys()
     {
       return mKeys;
     }
 
     public Object[] getSortedHandlers()
     {
       return mHandlers;
     }
   }
 
   private static class InternalKey implements Key
   {
     private final int mPriority;
     private final Class<? extends IEvent> mEventClass;
     private final Object mHandler;
 
     public InternalKey(int priority, Class<? extends IEvent> eventClass,
         Object handler)
     {
       mPriority = priority;
       mEventClass = eventClass;
       mHandler = handler;
     }
 
     /**
      * @return the priority
      */
     public int getPriority()
     {
       return mPriority;
     }
 
     /**
      * @return the eventClass
      */
     public Class<? extends IEvent> getEventClass()
     {
       return mEventClass;
     }
 
     public Object getHandler()
     {
       return mHandler;
     }
   }
 
   private int mNumNestedEventDispatches = 0;
 
   private final Map<String, ClassHandler> mHandlers = new HashMap<String, ClassHandler>();
 
   private final Queue<IEvent> mPendingEventDispatches = new ArrayQueue<IEvent>();
 
   /**
   * {@inheritDoc}
   *  This method is CRAZY hot and has been extensively optimized;
    *              don't make any changes here without reviewing them with
    *              another person. If you can, pre-compute elements during
    *              {@link #addEventHandler(int, Class, IEventHandler, boolean)}
    *              or
    *              {@link #removeEventHandler(com.xuggle.utils.event.IEventHandlerRegistrable.Key)}
    *              calls.
    * 
    */
   @SuppressWarnings("unchecked")
   public void dispatchEvent(IEvent event)
   {
     final long dispatcherNum = ++mNumNestedEventDispatches;
     final IEvent origEvent = event;
     try
     {
       // log.debug("dispatching event: {}", event);
       // do one acquire for adding to the queue
       origEvent.acquire();
       mPendingEventDispatches.offer(origEvent);
       if (dispatcherNum != 1)
         return;
       // don't process a dispatch if nested within a dispatchEvent() call;
       // wait for the stack to unwind, and then process it.
       while ((event = mPendingEventDispatches.poll()) != null)
       {
         try
         {
           // self handlers ARE ALWAYS called first
           if (event instanceof ISelfHandlingEvent<?>)
           {
             ISelfHandlingEvent<IEvent> handler = (ISelfHandlingEvent<IEvent>) event;
             try
             {
               if (handler.handleEvent(this, event))
                 // done handling this event
                 continue;
             }
             catch (AssertionError t)
             {
               // to enable tests to continue working, we dispatch an event but
               // rethrow.
               dispatchEvent(new ErrorEvent(event.getSource(), t,
                   "uncaught exception", event, handler));
               throw t;
             }
             catch (Throwable t)
             {
               dispatchEvent(new ErrorEvent(event.getSource(), t,
                   "uncaught exception", event, handler));
             }
           }
           final InternalKey[] keys;
           final Object[] handlers;
           // hold for minimum amount of time
           // find our registered handlers.
           final String className = event.getClass().getName();
 
           synchronized (mHandlers)
           {
             final ClassHandler classHandler = mHandlers.get(className);
             if (classHandler != null)
             {
               keys = classHandler.getSortedKeys();
               handlers = classHandler.getSortedHandlers();
             }
             else
             {
               keys = null;
               handlers = null;
             }
           }
           final int numHandlers = keys != null ? keys.length : 0;
           // log.trace("Handling event: {} with {} handlers", event,
           // numHandlers);
           boolean eventHandled = false;
           for (int i = 0; i < numHandlers && !eventHandled; ++i)
           {
             final Object handler = handlers[i];
             // log.debug("Handling event: {} with handler: {}", event, handler);
             final IEventHandler<IEvent> actualHandler;
             if (handler instanceof WeakReference<?>)
             {
               actualHandler = ((WeakReference<IEventHandler<IEvent>>) handler)
                   .get();
               if (actualHandler == null)
               {
                 // This has expired; remove it.
                 final InternalKey removeKey = keys[i];
                 dispatchEvent(new SelfHandlingEvent<IEvent>(this)
                 {
                   public boolean handleEvent(IEventDispatcher aDispatcher,
                       IEvent aEvent)
                   {
                     try
                     {
                       SynchronousEventDispatcher.this
                           .removeEventHandler(removeKey);
                     }
                     catch (IndexOutOfBoundsException e)
                     {
                       // ignore if already removed
                     }
                     return false;
                   }
                 });
                 continue;
               }
             }
             else
             {
               actualHandler = (IEventHandler<IEvent>) handler;
             }
             try
             {
               eventHandled = actualHandler.handleEvent(this, event);
             }
             catch (AssertionError t)
             {
               // to enable tests to continue working, we dispatch an event but
               // rethrow.
               dispatchEvent(new ErrorEvent(event.getSource(), t,
                   "uncaught exception", event, actualHandler));
               throw t;
             }
             catch (Throwable t)
             {
               dispatchEvent(new ErrorEvent(event.getSource(), t,
                   "uncaught exception", event, actualHandler));
             }
           }
         }
         finally
         {
           // do one release for finishing the handle
           event.release();
           // log.debug("Handling event: {} done", event);
         }
       }
     }
     finally
     {
       mNumNestedEventDispatches--;
     }
   }
 
   public Key addEventHandler(int priority, Class<? extends IEvent> eventClass,
       IEventHandler<? extends IEvent> handler)
   {
     return addEventHandler(priority, eventClass, handler, false);
   }
 
   public Key addEventHandler(int priority, Class<? extends IEvent> eventClass,
       IEventHandler<? extends IEvent> handler, boolean useWeakReferences)
   {
     if (eventClass == null)
       throw new IllegalArgumentException();
     if (handler == null)
       throw new IllegalArgumentException();
     final String eventName = eventClass.getName();
     final InternalKey key = new InternalKey(priority, eventClass,
         useWeakReferences ? new WeakReference<IEventHandler<? extends IEvent>>(
             handler) : handler);
     synchronized (mHandlers)
     {
       ClassHandler classHandler = mHandlers.get(eventName);
       if (classHandler == null)
       {
         classHandler = new ClassHandler();
         mHandlers.put(eventName, classHandler);
       }
       classHandler.addEventHandler(key);
     }
     // dispatch event outside of the lock
     dispatchEvent(new EventHandlerAddedEvent(this, key, priority, eventClass,
         handler, useWeakReferences));
     return key;
   }
 
   @SuppressWarnings("unchecked")
   public void removeEventHandler(final Key key)
       throws IndexOutOfBoundsException
   {
     if (key == null)
       throw new IndexOutOfBoundsException();
     if (!(key instanceof InternalKey))
       throw new IndexOutOfBoundsException("Key not generated by this class");
     final InternalKey iKey = (InternalKey) key;
     final String eventName = iKey.getEventClass().getName();
     final int priority = iKey.getPriority();
     Object handler = iKey.getHandler();
     synchronized (mHandlers)
     {
       ClassHandler classHandler = mHandlers.get(eventName);
       if (classHandler == null)
         throw new IndexOutOfBoundsException();
       classHandler.removeEventHandler(iKey);
       if (classHandler.getNumHandlers() <= 0)
         mHandlers.remove(eventName);
     }
     if (handler instanceof WeakReference<?>)
     {
       handler = ((WeakReference<IEventHandler<? extends IEvent>>) handler)
           .get();
     }
     dispatchEvent(new EventHandlerRemovedEvent(this, key, priority, iKey
         .getEventClass(), (IEventHandler<? extends IEvent>) handler));
   }
 
 }
