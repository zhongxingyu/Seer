 /*
  * Modified android.util.Pool by nchiring
  * Copyright (C) 2013 nchiring
  *
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package org.boomerang;
 
 import org.boomerang.exception.TimeOutException;
 
 import java.lang.ref.WeakReference;
 import java.lang.reflect.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 
 /**
  *  An interface for pooling objects which implement the <code>Poolable</code> interface
  */
 public interface Pool<T extends Poolable<T> > {
 
     T acquire() throws TimeOutException;
 
     void release(T item );
 
     void shutdown();
 
     /**
      *  <code>Pool</code> builder.
      *
      *  All time figures are in <code>SECONDS</code>
      *
      * @param <T>
      */
     public static class  Builder<T extends Poolable<T> >  {
 
         private static <T> T checkNotNull(T object, String paramName){
             if(object ==null) {
                 String errorMesg = String.format("Argument %s is null", paramName );
                 throw new IllegalArgumentException( errorMesg );
             }
             return object;
         }
 
 
         private PoolableManager<T> manager;
 
         private boolean harvestable= false;
         private int initialSize=0;
         private int maxSize=Integer.MAX_VALUE;
 
         private int minSize=5;
 
         private int timeout=2;
 
         private int decay=5;
         private int delay=5;
 
         public Builder( PoolableManager<T> manager) {
             this.manager = checkNotNull(manager, "manager") ;
 
         }
 
         public Builder  initialSize(  int initialSize )  {
                 this.initialSize= Math.max(5, initialSize);
                 return this;
         }
 
        /**
         * will have no effect if the corresponding <code>T</code> type
         * doesn't implements {@link Harvestable } interface
         *
         * @param harvestable
         * @return
         */
         public Builder<T> harvestable(boolean harvestable){
             this.harvestable = harvestable;
             return this;
         }
 
 
         public Builder<T> maxSize(int size){
             this.maxSize = Math.min(Integer.MAX_VALUE, size);;
             return this;
         }
 
         public Builder<T> minSize(int size){
             this.minSize= Math.max(5, size);
             return this;
         }
 
         public Builder<T> timeout(int timeout){
             if( timeout <= 0 )  {
                 throw new IllegalArgumentException( "Argument timeout must be positive" );
             }
             this.timeout= timeout;
             return this;
         }
 
         public Builder<T> decay(int period, int delay){
             this.decay=Math.max(5, period);;
             this.delay=Math.max(5, delay);;
             return this;
         }
 
         public Pool<T> build(){
                return new PoolImpl(manager,  harvestable,  initialSize, maxSize, minSize, timeout, decay,delay) ;
         }
 
     }
 
     /*TODO -- revisit if a priority queue  can be used instead of TreeSet
     static class Base<K extends Poolable<K> > implements  Poolable<K>, Harvestable, Comparable<Base<K> > {
 
         private K item ;
 
         boolean prioritySet=false;   //TODO-- use atomic boolean
 
         private List <HarvestListener> listeners = new ArrayList<HarvestListener>()   ;
         private  int priority= Integer.MAX_VALUE;
 
         Base(K item) {
 
             this.item = item;
         }
 
         @Override
         public void setPriority(int priority) {
             if(prioritySet){
                 throw new IllegalStateException("priority already set")  ;
             }
             this.priority = priority;
             prioritySet=true;
         }
 
         public void addHarvestListener(HarvestListener listener) {
             listeners.add(listener);
         }
 
         K getItem( ){
             return item;
         }
 
         public List<HarvestListener> getListeners() {
             return Collections.unmodifiableList(listeners);
         }
 
         public int getPriority() {
             return priority;
         }
 
         void reset( ){
             prioritySet=false;
             priority= Integer.MAX_VALUE;
             listeners.clear();
         }
 
         @Override
         public int compareTo(Base<K> o) {    */
 
             /*
             if( this.priority < o.priority ){
                 return 1;
             }else if( this.priority > o.priority ){
                 return -1;
             }else {
                 return 0;
             }
 
             return   this.priority - o.priority  ;
         }
     }       */
 
      static class MaintenanceRunner<T extends Poolable<T> > implements Runnable {
 
          private static final Logger LOG = Logger.getLogger(MaintenanceRunner.class.getName());
 
          WeakReference<PoolImpl<T> > _weakRef;
 
          public MaintenanceRunner(PoolImpl<T> pool)
          {
              _weakRef = new WeakReference<PoolImpl<T>>(pool);
          }
          @Override
          public void run() {
              try {
                  PoolImpl<T> pool = _weakRef.get();
                  if (pool != null ){
                       LOG.info("Maintenance work in progress...");
                       synchronized (pool){
                           int count=0;
                           //we prune the pool size 5 items per cycle
                           while( pool.getAvailableSize() > pool.minSize && count < 5 ){
                                      T item =  pool.availableItems.iterator().next();
                                      pool.availableItems.remove(item);
                                      LOG.info("removing -->" + item + " from pool ");
                                      count++;
                           }
                       }
 
                  } else {
                     // System.out.println("object not there, but task is running. ");
                      LOG.info("No Maintenance work required.");
                  }
 
              } catch (Exception ex) {
                  LOG.log(Level.SEVERE, "Maintenance action failed, quitting.", ex);
              }
          }
      }
 
     static class DiagnosticRunner<T extends Poolable<T> > implements Runnable {
 
         private static final Logger LOG = Logger.getLogger(DiagnosticRunner.class.getName());
 
         WeakReference<PoolImpl<T> > _weakRef;
 
         public DiagnosticRunner(PoolImpl<T> pool)
         {
             _weakRef = new WeakReference<PoolImpl<T>>(pool);
         }
         @Override
         public void run() {
             try {
                 PoolImpl<T> pool = _weakRef.get();
                 if (pool != null) {
                      LOG.info("Diagnostic work done!!!");
                     //TODO--
                 } else {
                     LOG.info("No Diagnostic work required.");
                 }
             } catch (Exception ex) {
                 LOG.log(Level.SEVERE, "Diagnostic action failed, quitting.", ex);
             }
         }
     }
 
 
     static class PoolImpl<T extends Poolable<T> > implements Pool<T> {
 
 
          private static final Logger LOG = Logger.getLogger(PoolImpl.class.getName());
 
         /** Copied from Guava's {@code MoreExecutors} class. Copyright (C) 2007 Google Inc.
          *
          * Add a shutdown hook to wait for thread completion in the given
          * {@link ExecutorService service}.  This is useful if the given service uses
          * daemon threads, and we want to keep the JVM from exiting immediately on
          * shutdown, instead giving these daemon threads a chance to terminate
          * normally.
          * @param service ExecutorService which uses daemon threads
          * @param terminationTimeout how long to wait for the executor to finish
          *        before terminating the JVM
          * @param timeUnit unit of time for the time parameter
          */
         private static void addDelayedShutdownHook(
                 final ExecutorService service, final long terminationTimeout,
                 final TimeUnit timeUnit) {
             Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                 public void run() {
                     try {
                         // We'd like to log progress and failures that may arise in the
                         // following code, but unfortunately the behavior of logging
                         // is undefined in shutdown hooks.
                         // This is because the logging code installs a shutdown hook of its
                         // own. See Cleaner class inside {@link LogManager}.
                         service.shutdown();
                         service.awaitTermination(terminationTimeout, timeUnit);
                     } catch (InterruptedException ignored) {
                         // We're shutting down anyway, so just ignore.
                     }
                 }
             }));
         }
 
         private PoolableManager<T> manager;
 
         private boolean harvestable= false;
 
         private int initialSize=0;
         private int maxSize=500;
 
         private int minSize=0;
 
         private int timeout=5;
 
         private int decay=5;
 
         private int delay;
         //monitor the access to the connection pool
         private Semaphore semaphore = null;
 
         Set<T> availableItems = new HashSet<T>(); //list of available poolables
         Set<T> borrowedItems = new HashSet<T>(); //list of in use(active) poolables,
                                                          //borrowed by the client codes
 
         //tracks what Thread has borrowed what poolable(s)
         private Map<Thread, Set<T> > borrowers = new WeakHashMap<Thread,Set<T >>();
 
         //housekeeping threads -> 1 for the pool size, 1 for poolable leak treatment
         private ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool( 2,
                                                                                         new ThreadFactory() {
                                                                                             public Thread newThread(Runnable r) {
                                                                                                   // System.out.println( " creating thread ....");
                                                                                                    Thread t = Executors.defaultThreadFactory().newThread(r);
                                                                                                    t.setDaemon(true);
                                                                                                    return t;
                                                                                              } }
                                                                                         );
         private ScheduledFuture<?> diagnosticTaskHandler =null;
         private ScheduledFuture<?> maintenanceTaskHandler =null;
 
         private Runnable maintenanceTask = null;
 
         private Runnable diagnosticTask = null;
 
         private Object shutter = new Object(){
             @Override
             protected void finalize() throws Throwable {
                 try {
                     LOG.info("shuting down the pool in GC phase... ");
                     if( !shutdownDone){
                         shutdown() ;
                     }
 
                 } finally {
                     super.finalize();
                 }
             }
         } ;
 
         private Class<T> itemType = null;
 
         private PoolImpl(PoolableManager<T> manager, boolean harvestable, int initialSize, int maxSize, int minSize,
                          int timeout,
                          int decay, int delay) {
             this.manager = manager;
             this.harvestable = harvestable  ;
             this.initialSize = initialSize;
             this.maxSize = maxSize;
             this.minSize = minSize;
             this.timeout = timeout;
             this.decay = decay;
             this.delay=delay;
 
             Type type =  this.manager.getClass().getGenericInterfaces()[0];
             if (type instanceof ParameterizedType) {
                 //System.out.println(((ParameterizedType)type).getRawType()); // prints; class Foo
                 Type[] actualTypeArguments = ((ParameterizedType)type).getActualTypeArguments();
                 itemType = (Class<T>) actualTypeArguments[0];
             }
 
 
            // System.out.println(" type is ---" + itemType );
 
             this.setup();
         }
 
         private void setup(){
             semaphore = new Semaphore( this.maxSize, true);
 
             maintenanceTask =   new MaintenanceRunner<T>(this);
 
             diagnosticTask =   new DiagnosticRunner<T>(this);
 
             //schedule the housekeeping tasks
             diagnosticTaskHandler = scheduler.scheduleAtFixedRate(diagnosticTask, delay /*initialDelay */, decay /* period */, SECONDS);
             maintenanceTaskHandler = scheduler.scheduleAtFixedRate(maintenanceTask, delay /*initialDelay */, decay /* period */, SECONDS);
 
             addDelayedShutdownHook(  this.scheduler, this.timeout, SECONDS);
 
         }
 
         private volatile boolean shutdownDone = false;
 
         @Override
         public synchronized void shutdown() {
                  LOG.info( " pool shutting down.. ");
                  if(shutdownDone)   {
                      LOG.info( " Already down, returning... ");
                      return;
                  }
                  scheduler.shutdown();
 
 
                 for( T item : borrowedItems )  {
                     this.release(item);
                 }
 
                 availableItems.clear();
                 borrowedItems.clear() ;
                 borrowers.clear();
 
                 semaphore = null;
 
                 shutdownDone = true;
         }
 
         @Override
         public T acquire() throws TimeOutException {
             if(shutdownDone){
                 throw new IllegalStateException("pool has already been shutdown");
             }
 
             T item=null;
            if(this.harvestable && getPoolSize() == this.maxSize && Harvestable.class.isAssignableFrom(itemType) ){
                    //max threshhold reached, so we try to reclaim harvestable item from borrowers if possible
                     synchronized (this){
                         LOG.info(" *************** Max size hit for  harvestable poolable type ***********************");
                         //LOG.info(" *************** dealing with a harvestable type .... ***********************");
                         Set<Harvestable> harvestables = (Set<Harvestable>)getHarvestEligibles( ) ;
                         if( ! harvestables.isEmpty()  ) {
                             Harvestable item2= harvestables.iterator().next(); //reclaim the item with the lowest priority
                             LOG.info(" ####### reclaiming item with priority= " + item2.getPriority() + " ..details =" + item2 );
                             release((T)item2,true);
 
                         }
                     }
 
             }
 
             item=null;
 
             try {
                 //get a permit first
                 if(!semaphore.tryAcquire(timeout, SECONDS))
                     throw new TimeOutException();
             } catch (InterruptedException e) {
                 throw new RuntimeException("Thread interrupted while waiting to get a poolable");
             }
 
             boolean ok = false;
             try {
                 item = this.getItem();
                 ok=true;
 
                 return item ;
             }
             finally {
                 if (!ok){
                     LOG.info( " didn't success; releasing the permit ");
                     semaphore.release();
                 }
             }
         }
 
         private int getPoolSize(){
             return this.availableItems.size() +  this.borrowedItems.size();
         }
 
         private int getAvailableSize(){
             return this.availableItems.size() ;
         }
 
         private int geBorrowedSize(){
             return this.borrowedItems.size();
         }
 
 
         private Set<Harvestable > getHarvestEligibles( ){
             Set<Harvestable > set = new TreeSet<Harvestable >();
 
 
             if(  ! this.borrowedItems.isEmpty() ){
                   for( T item: this.borrowedItems){
                       Harvestable hvt = (Harvestable) item ;
                       if( hvt.getPriority() < Integer.MAX_VALUE ){
                           set.add(hvt);
                       }
                   }
             }
             return Collections.unmodifiableSet(set);
         }
 
         private synchronized T getItem() {
             T item=null;
             if (!availableItems.isEmpty()) {
                 item = availableItems.iterator().next(); //apply your selection logic here
                 availableItems.remove(item);
             } else {
 
                 if(getPoolSize() < this.maxSize ){        //there is room for creating more instances, so we create
                     item= this.manager.newInstance();
                     LOG.info(" still can grow; creating new item....");
                  /*   if(Harvestable.class.isAssignableFrom(itemType) ) {
                         LOG.info(" dealing with a harvestable type ....");
                         item = (T) new Base<T>(item);
                     }  */
                 }
             }
 
 
             borrowedItems.add(item);
 
             Set<T> borroweds = borrowers.get(Thread.currentThread() );
             if( borroweds ==null){
                 borroweds = new HashSet<T>();
             }
             borroweds.add(item) ;
             borrowers.put(Thread.currentThread(), borroweds);
 
             this.manager.onAcquired(item);
 
             return item;
 
         }
 
 
 
         @Override
         public void release(T item) {
             if(shutdownDone){
                 throw new IllegalStateException("pool has already been shutdown");
             }
            release(item,false);
         }
 
         private void release(T item, boolean notifyListeners) {
             semaphore.release();
             borrowedItems.remove(item);
             if( Harvestable.class.isAssignableFrom(itemType) ) {
                 Harvestable itemX=  (Harvestable)item ;
                 if(notifyListeners){
                     //notify the listeners
                     LOG.info( " notifying the harvest listeners ");
                     for( HarvestListener listener: itemX.getListeners()) {
                         listener.onHarvested(itemX);
                     }
                 }
                 itemX.reset() ;
             }
 
             availableItems.add(item);
 
 
             // remove from the borrowing records
             Set<T> borroweds = borrowers.get(Thread.currentThread() );
             if( borroweds !=null){
                 borroweds.remove(item);
                 if(borroweds.isEmpty() )
                     borrowers.remove(Thread.currentThread());
             }
 
             this.manager.onReleased(item);
         }
 
 
     }
 }
