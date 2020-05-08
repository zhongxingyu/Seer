 
 /*
  * Copyright (c) 1998, 1999 Semiotek Inc. All Rights Reserved.
  *
  * This software is the confidential intellectual property of
  * of Semiotek Inc.; it is copyrighted and licensed, not sold.
  * You may use it under the terms of the GNU General Public License,
  * version 2, as published by the Free Software Foundation. If you 
  * do not want to use the GPL, you may still use the software after
  * purchasing a proprietary developers license from Semiotek Inc.
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See the attached License.html file for details, or contact us
  * by e-mail at info@semiotek.com to get a copy.
  */
 
 
 package org.webmacro.broker;
 import org.webmacro.util.*;
 import org.webmacro.*;
 import java.util.*;
 
 public final class ResourceManager implements ResourceMap
 {
 
    // STATE
 
    /**
      * Are we shut down?
      */
    private boolean _shutdown = false;
 
    /**
      * What type do we serve?
      */
   final private String _type;
 
    /**
      * How many milliseconds do we cache stuff? less than 1 means forever
      */
    private int _expireTime = ResourceProvider.NEVER_CACHE;
 
    /**
      * Max threads we might run
      */
    int _threadPool = 0;
 
    /**
      * Our reaper thread
      */
    private Thread _reaper;
 
    /**
      * The broker that owns us
      */
   final private ResourceBroker _broker;
  
    /**
      * A list of all our providers
      */
   final private Vector _providers = new Vector();
 
    /**
      * A hashtable containing all our resources. Lots of code 
      * synchronizes on _resources--the policy, universally, is 
      * that no further locks must be acquired within those 
      * synchronized blocks.
      */
   final private Hashtable _resources = new Hashtable();
 
    /**
      * A hashtable containing all our worker threads
      */
   final private Hashtable _workers = new Hashtable();
 
 
    // CONSTANTS
 
    /**
      * At what priority a worker thread whose work is irrelevant?
      */
   final private static int _REVOKED_WORKER; // not quite the lowest
 
    /**
      * At what priority a worker thread whose work may be important?
      */
   final private static int _BACKGROUND_WORKER;  // default priority
 
    /**
      * At what priority a worker thread whose work is important?
      */
   final private static int _NORMAL_WORKER;  // default priority
 
    /**
      * At what priority a worker whose thread performs a critical task?
      */
   final private static int _CRITICAL_WORKER; // same as everyone else
 
    static {
       int curPriority = Thread.currentThread().getPriority();
       _REVOKED_WORKER     = curPriority - 2;
       _BACKGROUND_WORKER  = curPriority -1;
       _NORMAL_WORKER      = curPriority;
       _CRITICAL_WORKER    = curPriority + 1;
       ThreadScheduler.start(1000); // switch threads every second
    }
 
    /**
      * Maximum amount of time the reaper should sleep, even when the
      * cache is empty. Note that the reaper will be woken up whenever 
      * something is added to the cache, so this is a failsafe measure.
      */
    final private static long _REAPER_LONG_SLEEP = 1000 * 60 * 30; // 30 minutes
 
 
    // INITIALIZATION & CONFIGURATION
 
    /**
      * Create a new ResourceManager relating to a specific type.
      * @param type The type of message handled by this queue
      * @param expireTime how long resources are cached by this queue
      */
    ResourceManager(
          final ResourceBroker owner, 
          final String type) {
 
       // thread issues: acquires no external accessible locks.
 
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug(type + " manager initializing");
       }
 
       _broker = owner;
       _type = type.intern();
 
       _reaper = new Thread() {
          public void run() {
             if (ResourceBroker.debug_) {
                ResourceBroker._log.debug(type + " reaper started");
             }
             try {
                while (!isInterrupted()) {
                   int expireTime = _expireTime;
                   if (expireTime > 0) {
                      if (ResourceBroker.debug_) {
                         ResourceBroker._log.debug(
                               type + " reaper--sleeping (" 
                               + _expireTime + ")");
                      }
                      sleep(expireTime);
                      if (! reap()) {      
                         if (ResourceBroker.debug_) {
                            ResourceBroker._log.debug(
                                  type + " reaper--waiting!");
                         }
                         synchronized(_resources) {
                            _resources.wait(_REAPER_LONG_SLEEP);
                         }
                         if (ResourceBroker.debug_) {
                            ResourceBroker._log.debug(
                                  type + " reaper--i wake up!");
                         }
                      }
                   } else {
                      if (ResourceBroker.debug_) {
                         ResourceBroker._log.debug(
                               type + " reaper--inactive sleep");
                      }
                      // double check while synched in case of change
                      synchronized(_resources) {
                         if (_expireTime <= 0) {
                            _resources.wait(_REAPER_LONG_SLEEP); 
                         }
                      }
                   }
                }
             } catch (InterruptedException ie) {
                if (ResourceBroker.debug_) {
                   ResourceBroker._log.debug(type + "reaper--interrupted");
                }
                // just quit
             } catch (Exception e) {
                ResourceBroker._log.exception(e);
                ResourceBroker._log.warning(
                      "Reaper caught exception (continuing anyway)");
             }
             if (ResourceBroker.debug_) {
                ResourceBroker._log.debug(type + " reaper stopped");
             }
          }
       };
       _reaper.setName("reaper:" + type);
       _reaper.setDaemon(true);
       _reaper.setPriority(_NORMAL_WORKER);
       _reaper.start();
    }
 
    /**
      * Get a string representing this ResourceManager, suitable for 
      * debugging purposes.
      */
    final public String toString() {
       return "ResourceManager(" + _type + "," + _expireTime + ")";
    }
 
 
    // REGISTRATION
 
    /**
      * Add a new listener to this manager. The handler must support the 
      * type that this queue handles. Use this method to register 
      * providers 
      * @param newProvider the new provider to be added to the queue
      * @exception InvalidTypeException provider does not support our type
      */
    final void addListener(final ResourceProvider newListener)
    {
 
       // synch: delegate to addListener
 
       if (newListener instanceof ResourceProvider) {
          ResourceProvider rp = (ResourceProvider) newListener;
          if (ResourceBroker.debug_) {
             ResourceBroker._log.debug("Manager(" + _type 
                   + ") adding provider " + rp);
          }
          addListener(_providers,rp);
          if (_expireTime < rp.resourceExpireTime()) {
             setExpireTime(rp.resourceExpireTime());
          }
       }
 
    }
 
    /**
      * What expire time are we currently using?
      */
    public long getExpireTime() {
       // synch: ok to get totally out of date value here
       // we need this method for the reaper thread: _expireTime not final
       return (long) _expireTime; // expireTime not a long, no synch issue
    }
 
    /**
      * Set the expire time for the cache. Valid values include any positive
      * integer (number of milliseconds), or the cache control constants 
      * defined in ResourceProvider: ResourceProvider.NEVER_CACHE (never 
      * cache a resource) or ResourceProvider.INFINITE_CACHE (cache resources
      * forever).
      */
    public void setExpireTime(int expireTime) 
    {
       // must synch. with reaper so it doesn't go for a long sleep just
       // after we set the expire time from <1 to >0 
       synchronized(_resources) {
          _expireTime = expireTime;
          _resources.notify();
       }
    }
 
    /**
      * Ensure that the named listener no longer appears in our type queue.
      * Use this method to remove providers 
      * @param rmListener ResourceProvider to be removed from the queue
      */
    final void removeListener(final ResourceProvider rmListener)
    {
 
       // synch: delete to removeListener
 
       removeListener(_providers,rmListener);
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") removing provider " + rmListener);
       }
       
    }
 
 
    // RESOURCE MAP API
 
 
    /**
      * Get the resource matching te supplied name
      */
    final public ResourceEvent get(final String name) 
       throws InvalidTypeException, NotFoundException
    {
       // synch: delegate to locate
       return locate(name,null,
             ResourceBroker.REQUEST_,ResourceBroker.ASYNCHRONOUS);
    }
 
    /**
      * Remove the resource matching the supplied name
      */
    final public void remove(final String name)
       throws InvalidTypeException, NotFoundException
    {
      locate(name,null,ResourceBroker.DELETE_,ResourceBroker.ASYNCHRONOUS);
    }
 
    /**
      * Add the resource matching the supplied name
      */
    final public ResourceEvent put(
          final String name, 
          final Object argument)
       throws InvalidTypeException, NotFoundException
    {
       // synch: delegate to locate
       return locate(name,argument,
             ResourceBroker.CREATE_,ResourceBroker.ASYNCHRONOUS);
    }
 
 
    // CORE METHODS 
 
    /**
      * Return true if this manager contains any providers
      */
    final boolean isProvider()
    {
       // synch: OK to be slightly out of date on rare occasions
       return (! _providers.isEmpty());
    }
 
 
    /**
      * Common code for all attempts to locate a provider and access it
      * to perform an action.
      * @param name the string name identifying the individual resource
      * @param arg  command (eg: CREATE) may take an optional argument
      * @param command one of REQUEST, CREATE, or DELETE
      * @param worker true if we should start a new thread for this
      * @exception InvalidTypeException on unknown type / no provider
      * @exception NotFoundException  on invalid or null value
      */
    ResourceEvent locate(
          final String name, 
          final Object arg, 
          final int command,
          final boolean worker)
       throws InvalidTypeException, NotFoundException
    {
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") locating " + name + "[" + command + "] worker=" + worker);
       }
 
       final ResourceEvent evt;
 
       // thread issues: synch on _resources for duration of cache operations,
       // but does not acquire any further locks.
 
       synchronized(_resources) {
          if (_shutdown) {
             throw new InvalidTypeException("Request follows shutdown");
          }        
 
          final ResourceEvent cachedEvt = (ResourceEvent) _resources.get(name);
 
          if (cachedEvt == null) {  
             switch(command) {
                case ResourceBroker.REQUEST_:  
                   evt = new RequestResourceEvent(_broker,_type,name);
                   break;
                case ResourceBroker.CREATE_:   
                   evt = new CreateResourceEvent(_broker,_type,name,arg);
                   break;
                case ResourceBroker.DELETE_:   
                   evt = new RequestResourceEvent(_broker,_type,name);
                   evt.deleted = true; // changes revoke's behavior
                   break;
                default:
                   throw new InvalidTypeException("Unsupported method!");
             }
             if (_expireTime != ResourceProvider.NEVER_CACHE) {
                // we are a caching provider, -1 = FOREVER, >0=EXPIRE
                _resources.put(name,evt);
                _resources.notify(); // wake up the reaper
             }
          } else {
             evt = cachedEvt;
             switch(command) {
                case ResourceBroker.REQUEST_: 
                   return evt;
                case ResourceBroker.CREATE_:
                   throw new NotFoundException("Resource exists.");
                case ResourceBroker.DELETE_:
                   evt.deleted = true; // changes revoke's behavior
                   break;
                default:
                   throw new InvalidTypeException("Unsupported method!");
             }
          } 
       }
 
       // thread issues: at this point we're working with either a new
       // event which has been put into _resources or a cached event that is
       // now deleted every other case returned or threw an exception--now
       // we resolve--so thread issues are delegated to resolve().
 
       if (worker && (_threadPool > 0)) {
          final Thread t = new Thread() {
             public void run()
             {
                try {
                   if (ResourceBroker.debug_) {
                      ResourceBroker._log.debug(this 
                            + " starting worker to resolve " + evt);
                   }
                   _workers.put(evt,this);
                   setName("worker:" + evt);
                   setPriority(_BACKGROUND_WORKER);
                   yield(); // we are not important yet, let someone else run
                   resolve(evt,true);
                } catch (Exception e) {
                   ResourceBroker._log.exception(e);
                } finally {
                   _workers.remove(evt); // was set before thread start
                }
             }
          };
          
          t.start();
       } else {
          resolve(evt,false);
       }
       return evt;
    }
 
 
    /**
      * Find a provider capable of filling this request, and obtain
      * the resource.
      */
    final private void resolve(ResourceEvent evt, boolean worker)
    {
 
       // thread issues: optimistically assumes it can read through 
       // the _providers array, catching exceptions that
       // indicate failure to do that. acquires no locks, and delegates
       // thread issues to ResourceProvider methods. following resolve,
       // resets thread priority if worker==true (our priority may have
       // been upgraded previously if someone blocked waiting for us).
 
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") resolving " + evt + " worker=" + worker);
       }
 
 
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("resolve(" + evt + "," 
                + _providers + "," + worker + ")");
       }
 
       // try and resolve it
       ResourceProvider rp;
       final int size = _providers.size();
 
       // if the event is revoked, it will not be settable
       // if the event has been set now, it will not be settable
       try {
          for (int i = 0; i < size && evt.isSettable(); i++) {
             evt.providerSet((ResourceProvider) _providers.elementAt(i));
          }
       } catch (ArrayIndexOutOfBoundsException e) {
          // got shorter on us, ignore
       } catch (NotFoundException rue) {
          // stop looking for it, it isn't there
       } catch (InterruptedException ie) {
          // someone asked us to quit doing this
          Thread.interrupted(); // we've handled it
       } catch (Exception e) {
          ResourceBroker._log.exception(e);
          ResourceBroker._log.warning(
                "Provider " + _type + " threw an exception");
       }
 
       if (!evt.deleted && evt.isAvailable()) { 
          // a new event is now available 
          if (worker) {
             // we are not critical anymore
             Thread.currentThread().setPriority(_NORMAL_WORKER);
             Thread.yield(); // if we blocked someone, let them run
          }
       } else if (evt.deleted || evt.isSettable()) {
          // being deleted, or we failed to set it
          revoke(evt, "Unable to resolve requested resource"); 
       } else if (evt.isRevoked()) {
          // this should be the only other logical possibility
          // do nothing.
       } else {
          // should never get here: this event matches the conditions:
          // not-deleted, not-available, not-settable, not-revoked
          ResourceBroker._log.warning( 
             this + " reached an unexpected state resolving " + evt); 
       }
    }
 
 
    /**
      * Commit (save/delete) revokeMe and mark it as being revoked. Also 
      * stop/slow any ongoing work to resolve it.
      */
    void revoke(ResourceEvent revokeMe, String reason) 
    {
 
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") revoking " + revokeMe);
       }
 
       // thread issues: locks revokeMe, delegates to save
       // must prevent people from getting a fresh copy of an unsaved event
       // ok to get a revoked event, or a fresh version of saved event
       synchronized(revokeMe)
       {
          // it's ok to call us more than once, we'll ignore
          if (revokeMe.isRevoked()) {
             return;
          }
 
          commit(revokeMe);
          revokeMe.revoke(reason); 
          
          // stop unnecessary work: but beware of self!
          Thread t = (Thread) _workers.remove(revokeMe);
          if ((t != null) && t != Thread.currentThread()) {
             t.setPriority(_REVOKED_WORKER);
             t.interrupt();
          }
       }
 
       // ok... we have given up our lock... we didn't want to hold a lock
       // on revoke me AND _resources at the same time. but now someone
       // could come along and remove revokeMe from the cache before we 
       // get around to it. however, we are "optomistic" that this will
       // not happen--so we try and remove it, and then check whether the 
       // thing we removed is the thing we thought it was (just in case).
 
       synchronized(_resources) {
          ResourceEvent removed = 
             (ResourceEvent) _resources.remove(revokeMe.getName());
          if ((removed != null) && !removed.isRevoked()) {
             _resources.put(removed.getName(),removed); // not ours!
             _resources.notify(); // wake reaper
          }
       }
    }
 
    /**
      * This method is called when a thread blocks waiting for a resource
      * event. We check whether the thread is waiting for itself, and 
      * upgrade the priority of the thread it is waiting for.
      * <p>
      * @exception NotFoundException violated thread policy
      */
    void blocked(ResourceEvent evt)
       throws NotFoundException
    {
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") blocked " + evt);
       }
       // thread: called from ResourceEvent.getResource()
       // so from a thread viewpoint, part of ResourceEvent's thread
       // policy. therefore it must not acquire any more locks: 
       // it must be "inner-most" locking: MAY lock on evt if needed.
 
       Thread slow = (Thread) _workers.get(evt);
       Thread me = Thread.currentThread();
       if (slow == me) {
          // we are trying to block waiting for ourself!
          ResourceBroker._log.warning("Attempt to block self for: " + evt);
          throw new NotFoundException("Attempt to block self!");
       } else if (slow != null) {
          // raise the thread we blocked on to at least our priority
          int blockedPriority = me.getPriority();
          if (slow.getPriority() < blockedPriority) {
             slow.setPriority(blockedPriority);
          }
       }
    }
 
    /**
      * Save the supplied resource. 
      */
    private void commit(ResourceEvent toBeCommitted)
    {
 
       // THREAD ISSUE: Caller already holds lock on toBeCommitted!!!
       // synchronized(toBeCommitted) 
       // thread policy: acquire no further locks, do not use _resources
 
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") committing " + toBeCommitted);
       }
 
       ResourceProvider rp;
       final int size = _providers.size();
       boolean saved = false;
 
       // we want to commit allocated or deleted events only
       if (toBeCommitted.isAvailable() || toBeCommitted.deleted) {
          try {
             for (int i = 0; i < size && !saved; i++) {
                rp = (ResourceProvider) _providers.elementAt(i);
                if (toBeCommitted.deleted) {
                   saved = rp.resourceDelete(toBeCommitted); 
                } else {
                   saved = rp.resourceSave(toBeCommitted); 
                }
             }
          } catch (ArrayIndexOutOfBoundsException e) {
             // got shorter on us, ignore
          }
          if (!saved) {
             ResourceBroker._log.warning(
                   "Resource unsaved: " + toBeCommitted);
          }
       }
    }
 
    // STOPPING
 
    /**
      * Shut down the type queue, phase one: revoke all resources
      */
    void shutdownResources()
    {
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") shutting down resources");
       }
       // acquires _resources lock FIRST, then event locks
       synchronized(_resources) {
          _shutdown = true;
       }
 
       // stop the reaper
       _reaper.interrupt();
 
       /**
         * revoke all outstanding resources
         */
       Enumeration enum = _resources.elements();
       ResourceEvent re;
       while (enum.hasMoreElements()) {
          re = (ResourceEvent) enum.nextElement();
          revoke(re, "shutting down"); // acquires locks
       }
    }
 
    /**
      * Shut down the type queue, phase two: shut down the listeners
      */
    void shutdownListeners(Hashtable alreadyShutdown)
    {
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug("Manager(" + _type + 
                ") shutting down listeners");
       }
 
       // thread issues: delegated. shutdown assumed set already
 
       destroy(_providers, alreadyShutdown);
    }
 
    /**
      * Stop all the listeners of this type, if they haven't been already
      */
    private void destroy(Vector v, Hashtable stopped)
    {
       // thread issues: none. shutdown set prior to this call
 
       ResourceProvider rl;
       Enumeration rlenum;
 
       rlenum = v.elements();
       while (rlenum.hasMoreElements()) {
         rl = (ResourceProvider) rlenum.nextElement();
         if (stopped.get(rl) == null) {
             stopped.put(rl,rl);
             rl.destroy();
          }
       }
    }
 
 
    // PRIVATE METHODS
 
    /**
      * Called by the reaper thread, attempt to free old resources
      * @return whether or not anything interesting happened
      */
    private boolean reap() throws InterruptedException
    {
 
       boolean sawSomething = false;
 
       if (ResourceBroker.debug_) {
          ResourceBroker._log.debug(this + ": reaping");
       }
       try {
          Enumeration events = _resources.elements();
          ResourceEvent evt;
          while (events.hasMoreElements()) {
             evt = (ResourceEvent) events.nextElement();
             if (ResourceBroker.debug_) {
                ResourceBroker._log.debug(this + " reaper checking: " + evt);
             }
             if (! evt.isSettable()) {
                if (ResourceBroker.debug_) {
                   ResourceBroker._log.debug(this + " reaper--is settable: " + evt);
                }
                if (evt.accessed) {
                   if (ResourceBroker.debug_) {
                      ResourceBroker._log.debug(this + " reaper--is accessed: " + evt);
                   }
                   evt.accessed = false;
                } else {
                   if (ResourceBroker.debug_) {
                      ResourceBroker._log.debug(this + " reaper--revoking: " + evt);
                   }
                   try {
                      _reaper.setPriority(_CRITICAL_WORKER);
                      // this grabs important locks, do it quick
                      revoke(evt, "Expired from cache"); 
                   } finally {
                      _reaper.setPriority(_NORMAL_WORKER);  
                   }
                }
             }
             sawSomething = true;
          }
       } catch (Exception e) {
          ResourceBroker._log.exception(e);
          ResourceBroker._log.warning("Reaper caught exception in " + this
                + " (continuing anyway)");
       }
 
       // return whether anything interesting happened
       return sawSomething;
       
    }
 
    /**
      * Add a new listener to the supplied vector, after making sure
      * that it is not already in the vector.
      */
    private void addListener(final Vector v, final ResourceProvider rl) 
    {
       synchronized(_resources) {
          if (_shutdown) {
             return;
          }
          if (!v.contains(rl)) {
             v.addElement(rl);
          }
       }
    }
 
    /**
      * Remove the supplied listener from the supplied vector, making
      * sure that every reference is removed
      */
    private void removeListener(final Vector v, final ResourceProvider r)
    {
       synchronized(_resources) {
          while (v.removeElement(r)) {
             // keep going
          }
       }
    }
 }
 
 
