 
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
 import org.webmacro.*;
 import org.webmacro.util.*;
 import java.util.*;
 
 /**
   * Resource Broker--connect up requests for a particular type of 
   * resource with a provider capable of obtaining it.
   * It also manages a cache of resources, frees them over time, 
   * and ensures that their Providers have an opportunity to save them.
   * <p>
   * A "resource" is an object which can be uniquely identified by a 
   * string type and a string name.
   * <p>
   * The ResourceBroker is strongly related to the Java InfoBus. The
   * InfoBus was intended to be used in applets, and has many Applet
   * specific properties. Like the InfoBus a ResourceBroker is intended
   * to allow you to exchange data by name, between providers and 
   * client programs--enabling you to change providers/client at runtime,
   * have multiple providers for the same kind of information, and allow
   * multiple clients of information to share the same copy of the data.
   * Though the ResourceBroker and InfoBus differ in many other ways, one
   * intent of the ResourceBroker design is that it could make use of an
   * InfoBus internally, proxying requests so transparently that clients
   * of the ResourceBroker would not know they were using an InfoBus. This
   * is important because many useful classes may be written for the InfoBus
   * in the near future.
   * <p>
   * Unlike the InfoBus, the ResourceBroker handles requests asynchronously,
   * starting a new thread for each request. It also manages a simple cache,
   * and automatically revokes objects that have not been accessed (according
   * to a strict definition of "accessed") within a fixed period of time.
   * <p>
   * The ResourceBroker attempts some simple thread scheduling. The thread 
   * spawned off to do the work happens at less than the default 
   * thread priority. If a thread blocks waiting for it to 
   * complete in the ResourceEvent.getValue() method, the priority of the 
   * worker thread trying to acquire the resource is raised to be
   * greater than the default priority.
   * <p>
   * The broker also manages a resource cache. If two clients request
   * the same resource (identical name and type) then they get back the
   * same resource. To accomplish this, the broker maintains a cache of
   * outstanding objects. 
   * <p>
   * The ResourceBroker runs a reaper thread in the background for each
   * resource type, to revoke resources which have not been used recently.  
   * You can specify what "recently" means as an argument to the constructor. 
   * You can override that choice for each specific type when you register 
   * the FIRST provider of that type. 
   * <p>
   * Please note that revoking does NOT mean the underlying objects are 
   * freed--only that the ResourceBroker will not permit any new accesses 
   * to them, ask their provider to save them, and cause their corresponding 
   * ResourceEvents to throw a ResourceRevokedException whenever an attempt 
   * is made to access its value. You could still go and get a NEW reference
   * to it by calling request() and have a fresh working copy. The object 
   * will not be freed by the JVM until the last reference to it is lost by 
   * its client--but this will be convenient in many instances, such as 
   * threaded servlets, * where references are lost quite quickly.
   * <p>
   * An object is revoked if it has not been touched since the last time 
   * the reaper looked at it. A resource is "touched" when you call a 
   * method on its ResourceEvent (eg: getValue, isAvailable, etc.). This
   * policy can be extended by the ResourceProvider, since a ResourceEvent
   * supports both the Observer and PropertyChangeListener interfaces: A 
   * provider could supply resource objects that support the complementary
   * interfaces and register them with the ResourceEvent when setting them.
   * <p>
   * Some segments of code may find it inconvenient that a resource can 
   * be revoked at any time. To make life easier for these applications, 
   * there is a getValue(type,name) method available which blocks until
   * the object becomes available, and attempts to deal with the fact that
   * it has been revoked.
   * <p>
   * <b>Thread policy:</b> NEVER lock this object.
   * <p>
   * You MAY lock on ResourceEvents under limited conditions--in
   * particular if you hold a lock on an event, you MUST NOT use the
   * broker at all while you have that lock, nor may you call any
   * method which will use the broker. The broker will lock ResourceEvents
   * under a variety of conditions; this policy ensures that locks
   * are always acquired in the same order: Broker lock first,
   * lock ResourceEVent second.  
   * <p>
   * <b>NOTE</b>: While the ResourceBroker takes steps to ensure that 
   * a ResourceEvent is saved before it is revoked, it has no way of 
   * detecting activity on the underlying object, nor can it prevent 
   * clients from extracting the underlying resource from a 
   * ResourceEvent and holding copies of it after it has been revoked.
   * <p>
   * @see ResourceEvent
   * @see ResourceProvider
   * @see ResourceTracker
   */
 final public class ResourceBroker implements Broker
 {
 
    // CONSTANTS
 
    /**
      * This indicates that the resource is being retreived
      */
    static final int REQUEST_ = 0;
 
    /**
      * This indicates that the resoruce is being created
      */
    static final int CREATE_ = 1;
 
    /**
      * This indicates that the resource is being deleted
      */
    static final int DELETE_ = 2;
 
    /**
      * Used to get debug code optimized out of the binary
      */
    static final boolean debug_ = false;
 
    /**
      * Our local log
      */
    static final Log _log = new Log("broker","resource broker package");
 
    /**
      * In case someone forgets to specify it--how long should a resource
      * live in the cache, if not otherwise specified?
      */
    final private static long _DEFAULT_EXPIRE = 1000 * 30; // 30 seconds
 
    // INSTANCE INIT
 
    /**
      * contains the information providers indexed by type
      */
   final private Hashtable _managers = new Hashtable();
 
    /**
      * prevents activity from happening during/after a shutdown
      */
    private boolean _shutdown = false; // latch
 
    /**
      * Construct a new resource broker with default expire time.
      */
    public ResourceBroker() throws BrokerInitException
    {
       this(new Config());
    }
 
    /**
      * Construct a new resource broker with the specified config file
      */
    public ResourceBroker(String configFile) throws BrokerInitException
    {
       this(new Config(configFile));
    }
   
    /**
      * Create a ResourceBroker based on the specified configuration
      */
    public ResourceBroker(Config conf) throws BrokerInitException
    { 
       join(conf);
       String providers;
       try {
          providers = (String) getValue(Config.TYPE, Config.PROVIDERS);
       } catch (NotFoundException re) {
          throw new BrokerInitException("Could not read list of providers out of the configuration file; the configuration file appears to have incomplete data.");
       } catch (InvalidTypeException re) {
          throw new BrokerInitException("Could not read configuration data at all; the broker failed to initialize correctly.");
       }
 
       Enumeration provEnum = new StringTokenizer(providers);
       String className;
       Class resClass;
       ResourceProvider instance;
       while (provEnum.hasMoreElements()) {
          className = (String) provEnum.nextElement();
          try {
             resClass = Class.forName(className);
             instance = (ResourceProvider) resClass.newInstance();
             join(instance);
             _log.info("Loaded provider: " + className);
          } catch (Exception e) {
             _log.exception(e);
             _log.error("Could not load resource provider \"" +
                    className + "\":" + e);
          }
       }
    }
 
 
 
    // REGISTRATION METHODS
 
    /**
      * Adds a new resource provider to the Broker's 
      * internal lists of service providers. Once a provider has joined
      * the broker, it will be used to satisfy requests matching its type.
      */
    final synchronized public void join(final ResourceProvider newListener)
    {
       // thead issues: adding/removing resource managers must be synch.,
       // but threads are allows unsyncronized reads to get managers
       // also check/set of shutdown must synchronized
 
       if (_shutdown) {
           return;
       }
 
       try {
          newListener.init(this);
       } catch (Exception e) {
          _log.exception(e);
          _log.warning("Could not initialize listener: " + newListener);
          try { newListener.destroy(); } catch (Exception ignore) { }
          return;
       }
 
       final String types[] = newListener.getTypes();
       ResourceManager man;
       for (int i = 0; i < types.length; i++) {
          man = (ResourceManager) _managers.get(types[i]);
          if (null == man) {
             man = new ResourceManager(this,types[i]);
             _managers.put(types[i],man);
          } 
          man.addListener(newListener);
       }
    }
 
    /**
      * Remove a resource producer from the Broker's 
      * internal lists of service providers. After leaving
      * this broker rmListener will no longer be notified of resources
      * that become available, nor will it be used to allocate or find
      * new resources.
      */
    final synchronized public void leave(final ResourceProvider rmListener)
    {
       // thead ussues: adding/removing resource managers must be synch.,
       // but threads are allows unsyncronized reads to get managers
       // also check/set of shutdown must synchronized
 
       final String types[] = rmListener.getTypes();
       ResourceManager man;
       for (int i = 0; i < types.length; i++) {
          man = (ResourceManager) _managers.get(types[i]);
          if (null != man) {
             man.removeListener(rmListener);
          }
       }
    }
 
 
    // CORE METHODS
 
    /**
      * Return the value of the contained resource. Throw an exception
      * only if the resource is absolutely unavailable, and cannot be 
      * retrieved again. This method will block until the resource 
      * becomes available.
      * <p>
      * If the resource is marked as revoked, getValue() will wait for 
      * it to disappear and request it again. This is not guaranteed to
      * succeed, but it would be a pretty pathalogical situation where 
      * it failed--so pathalogical that any other method of retreiving 
      * the value would be unlikely to succeed either.
      * <p>
      * If the resource cannot be retrieved, despite a really good try, 
      * then a NotFoundException will be thrown. Though it's 
      * possible a second request may succeed in retrieving the value, 
      * trying again is not recommended unless you have specific 
      * knowledge that circumstances have changed in a way that makes 
      * this likely.
      * <p>
      * @param type the type of the resource being sought
      * @param name the name of the resource beig sought
      * @exception NotFoundException invalid type/request
      */
    public Object getValue(final String type, final String name)
       throws NotFoundException, InvalidTypeException
    {
       // optomistic busy-wait, try up to fifty times to get it: the
       // reason for the timeout is that if someone is trying really 
       // hard to revoke this event we don't want to fight with them 
       // forever. revoke() temporarily leaves the object unlocked 
       // between marking it as revoked, and removing it from the cache,
       // so we are trying to wait until it is deleted from the cache
       // here. in order to give the revoker a chance to do that, we 
       // yield and temporarily sleep. 100 * 10 == 1 second max 
       // until we time out and quit.
       BrokerRequest evt;
       for(int i = 0; i < 100; i++) {
          try {   
             evt = request(type,name);
             return evt.getValue();
          } catch (ResourceRevokedException e) {
             // give the revoker a chance to complete, and be a nice busy wait
             Thread.yield();
             try { Thread.sleep(10); }
             catch (InterruptedException ie) {
                throw new NotFoundException("Thread interrupted.");
             }
          } 
       }
       throw new NotFoundException("Resource locked by revoker.");
    }
 
    /**
      * Get a resource map-like interface for the supplied type,
      * meaning you can use get/put/remove as you would with a 
      * dictionary. (It's not a dictionary, but it's pretty similar.)
      * <p>
      * This is recommended if you are going to repeatedly access the 
      * same type of information, as it is slightly more efficient 
      * than calling the equilvalent methods on the ResourceBroker 
      * (it saves resolving the type name for each request).
      * @see ResourceMap
      * @param type the resource type this map should relate to
      * @return a map that has a fixed type, where you supply the names
      * @exception InvalidTypeException no provider for type
      */
    final public ResourceMap get(final String type) 
       throws InvalidTypeException
    {
       final ResourceManager man = (ResourceManager) _managers.get(type);
       if ((man == null) || (! man.isProvider())) {
          throw new InvalidTypeException("No provider for " + type);
       }
       return man;
    }
 
    /**
      * Equivalent to request(type,name,ASYNCHRONOUS)
      * @param type Tye type of service requested
      * @param name A name/description of the desired item
      * @return an event representing the submitted request
      * @exception InvalidTypeException on null value or unknown type
      * @exception NotFoundException refused: invalid type/service
      */
    final public BrokerRequest request(
          final String type, final String name)
       throws InvalidTypeException, NotFoundException
    {
       return locate(type,name,null,REQUEST_,ASYNCHRONOUS);
    }
 
    /**
      * Request a resource from the Broker. This will result in the 
      * creation of a ResourceEvent, which will be returned. The 
      * ResourceEvent will asynchronously be passed to one or 
      * more Provider, if available.
      * <p>
      * Note that this method operates asynchronously and may spawn 
      * multiple threads. This behavior should be transparent to 
      * the Provider, but it is worth remembering. 
      * Although this method will return immediately, calls to toString()
      * and getValue() will block until the Resource is actually 
      * made available by a Provider.
      * <p>
      * You can specify whether you want the resource to be resolved in
      * its own thread (ASYNCHRONOUS) or in this thread (SYNCHRONOUS).
      * <p>
      * @param type Tye type of service requested
      * @param name A name/description of the desired item
      * @param asynchronous either ASYNCHRONOUS (true) or SYNCHRONOUS (false)
      * @return an event representing the submitted request
      * @exception InvalidTypeException on null value or unknown type
      * @exception NotFoundException refused: invalid type/service
      */
    final public BrokerRequest request(
          final String type, 
          final String name,
          final boolean asynchronous)
       throws InvalidTypeException, NotFoundException
    {
       return locate(type,name,null,REQUEST_,ASYNCHRONOUS);
    }
 
    /**
      * Request that a resource be deleted, returning the instance of 
      * it if it existed. If it does not exist then the returned 
      * ResourceEvent will be revoked before a value becomes available.
      * Any outstanding copy of the resource will be revoked before the
      * deletion occurs.
      * <p>
      * ResourceProviders are not required to support the delete operation,
      * and may simply return a revoked item.
      * <p>
      * Note that this method operates asynchronously and is subject to
      * exactly the same conditions as request().
      * <p>
      * @exception InvalidTypeException on null value or unknown type
      * @exception NotFoundException refused: invalid type/service
      */
    final public BrokerRequest delete(
          final String type,
          final String name)
       throws InvalidTypeException, NotFoundException
    {
       return locate(type,name,null,DELETE_,ASYNCHRONOUS);
    }
 
    /**
      * Request that a resource be created. This method will be fail if 
      * the resource already exists. 
      * <p>
      * ResourceProviders are not required to suppor the create operation,
      * and may simply return a revoked item.
      * <p>
      * Note that this method operates asynchronously and is subject to 
      * exactly the same conditions as request().
      * <p>
      * The returned value will be saved once it expires or is revoked.
      * @exception InvalidTypeException on null value or unknown type
      * @exception NotFoundException refused: invalid type/service
      */
    final public BrokerRequest create(
          final String type,
          final String name,
          final Object argument)
       throws InvalidTypeException, NotFoundException
    {
       return locate(type,name,argument,CREATE_,ASYNCHRONOUS);
    }
 
    /**
      * Same as calling create(type,name,null)
      * @exception InvalidTypeException on null value or unknown type
      * @exception NotFoundException refused: invalid type/service
      * @return a resource event representing an attempt to create the user
      * @param type the type of resource this operation relates to
      * @param name the name of the resource we wish to create
      */
    final public BrokerRequest create(
          final String type,
          final String name)
       throws InvalidTypeException, NotFoundException
    {
       return locate(type,name,null,CREATE_,ASYNCHRONOUS);
    }
 
    /**
      * Revoke a resource. Provider can call this to revoke an item 
      * they previously returned. 
      * <p>
      * Revoking a resource does not delete it, it simply invalidates the
      * current copy. Whether that deletes it depends on whether the 
      * provider maintains a persistent copy or not--most providers 
      * probably do.
      * <p>
      * Resources are automatically saved when they are revoked, unless
      * they have been deleted.
      */
    final public void revoke(final ResourceEvent revokeMe)
    {
 
       // thread issues: delegate to ResourceManager.revoke()
 
       if (debug_) {
          _log.debug("revoke(" + revokeMe + ")");
       }
       final ResourceManager rm = 
          (ResourceManager) _managers.get(revokeMe.getType());
       if (rm == null) {
          _log.error("Attempt to revoke resource with no manager: " + revokeMe);
          return;
       }
       rm.revoke(revokeMe, "Resource revoked by request");
    }
 
 
    /**
      * Return whether there is currently a provider for the supplied type.
      */
    final public boolean isProvider(final String type) 
    {
       final ResourceManager man = 
          (ResourceManager) _managers.get(type);
       if (man == null) {
          return false;
       }
       return man.isProvider();
    }
 
 
    /**
      * Shuts down the broker, removing all of its providers 
      */
    final public void shutdown()
    {
       // acquires resourceCache lock FIRST, then event locks
       synchronized(this) {
          _shutdown = true;
       }
 
       /**
         * revoke all outstanding resources
         */
       Enumeration mgrs = _managers.elements();
       ResourceManager man;
       while (mgrs.hasMoreElements()) {
          man = (ResourceManager) mgrs.nextElement();
          man.shutdownResources();
       }
 
       /**
         * shutdown all providers 
         */
       Hashtable stopped = new Hashtable(); // so we don't shutdown twice
       mgrs = _managers.elements();
       while (mgrs.hasMoreElements()) {
          man = (ResourceManager) mgrs.nextElement();
          man.shutdownListeners(stopped);
       }
    }
 
 
    // NON-PUBLIC METHODS
 
 
    private ResourceEvent locate(
          final String type,
          final String name, 
          final Object arg,
          int   command,
          boolean isWorker)
       throws InvalidTypeException, NotFoundException
    {
       try {
          return ((ResourceManager) 
            _managers.get(type)).locate( name,arg,command,isWorker);
       } catch (NullPointerException e) {
          throw new InvalidTypeException("No such type: " + type);
       }
    }
 
    /**
      * This method is called when a thread blocks waiting for a resource
      * event. We check whether the thread is waiting for itself, and 
      * upgrade the priority of the thread it is waiting for.
      * <p>
      * @exception NotFoundException violated thread policy
      */
    final void blocked(final ResourceEvent evt)
       throws NotFoundException
    {
       // delegates thread issues to locate
       try {
          ((ResourceManager) _managers.get(evt.getType())).blocked(evt);
       } catch (NullPointerException e) {
          throw new NotFoundException("No provider for " + evt);
       }
    }
 
 
    // TEST HARNESS
 
    /**
      * Test harness
      */
    public static void main(String arg[]) {
 
       try {
          String[] type1 = { "type one" };
          String[] type2 = { "type two" };
          String[] type13 = { "type three", "type two" };
 
          System.out.println("MAIN: Creating new broker with 5sec expire time");
          ResourceBroker b = new ResourceBroker();
         
          ResourceProvider p1 = 
             new TestProvider(type1, "provider 1", "A");
 
          ResourceProvider p2 = 
             new TestProvider(type2, "provider 2", "B");
          ResourceProvider p3 = 
             new TestProvider( type13, "provider 3", "C");
 
          b.join(p1);
          b.join(p2);
          b.join(p3);
 
          System.out.println("MAIN: launching a lot of threads");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
          b.request("type one","A");
          b.request("type two","C");
  
          System.out.println("MAIN1 Requesting type one/A:");  
          ResourceEvent evt1 = (ResourceEvent) b.request("type one","A");
          System.out.println("MAIN1 request gave: " + evt1);  
 
          System.out.println("MAIN2 Requesting type two/B:");  
          ResourceEvent evt2 = (ResourceEvent) b.request("type two","B");
          System.out.println("MAIN2 request gave: " + evt2);  
 
          ResourceEvent evt3 = null;
          try {
            System.out.println("MAIN3: Requesting type four/B:");  
            evt3 =(ResourceEvent)  b.request("type four","B");
            System.out.println("ERROR: should not have got here");
          } catch (InvalidTypeException e) {
            System.out.println("Got expected exception (no type four provider)");
          }
 
          System.out.println("MAIN4: Requesting type two/D:");  
          ResourceEvent evt4 = (ResourceEvent) b.request("type two","D");
          System.out.println("MAIN4 request gave: " + evt4);  
          
          System.out.println("MAIN1: " + evt1 + " = " + evt1.getValue());
          System.out.println("MAIN2: " + evt2 + " = " + evt2.getValue());
 
          try {
             System.out.println("MAIN4: " + evt4 + " = " + evt4.getValue());
          } catch (NotFoundException rre) {
             System.out.println("Got expected excption: " + evt4 
                   + " has no provider");
          }
       
          Thread.sleep(2500);
          System.out.println("Touching " + evt1);
          evt1.update();
          Thread.sleep(2500);
          System.out.println("Touching " + evt1);
          evt1.update();
          Thread.sleep(2500);
 
          System.out.println("---SHUTDOWN---");
          b.shutdown();
 
          System.out.println("---DONE---");
 
       } catch (Exception e) {
          System.out.println("---UNEXPECTED EXCEPTION---");
          e.printStackTrace();
       }
 
    }
 }
 
 class TestProvider implements ResourceProvider
 {
 
    String types[];
    String value;
    String name;
 
    TestProvider(String types[], String value, String name) {
       this.value = value;
       this.types = types;
       this.name = name;
       for (int i = 0; i < types.length; i++) {
          System.out.println("CREATE:" + this);
       }
    }
 
    public int resourceExpireTime() {
       return 1500;
    }
 
    public int resourceThreads() {
       return 10;
    }
 
    public void resourceRequest(RequestResourceEvent req)
    {
       try {
          System.out.println(this + " got request " + req);
          if (name.equals(req.getName())) {
             req.set("hello from " + value);
             System.out.println(value + " setting it.");
          } else {
             System.out.println(value + " not for me.");
          }
       } catch (Exception e) {
          e.printStackTrace();
       }
    }
 
    public void resourceCreate(CreateResourceEvent req)
    {
       System.out.println(this + " got create for " + req);
    }
 
    public String toString() {
       return "TestProvider(" + listTypes() + ", " + value + ", " + name + ")";
    }
 
    public String listTypes() {
       StringBuffer b = new StringBuffer();
       b.append("{");
       for (int i = 0; i < types.length; i++) {
          if (i != 0) {
             b.append(",");
          }
          b.append(types[i]);
       }
       b.append("}");
       return b.toString();
    }
 
    public boolean resourceDelete(ResourceEvent evt)
    {
       System.out.println(this + " DELETING: " + evt);
       return true;
    }
 
    public boolean resourceSave(ResourceEvent evt)
    {
       System.out.println(this + " SAVING: " + evt);
       return true;
    }
 
    public String[] getTypes() {
       return types;
    }
 
    public void init(ResourceBroker broker)
    {
       System.out.println(this + " initialized by " + broker);
    }
 
    public void destroy()
    {
       System.out.println(this + " shutting down");
    }
 }
 
 
