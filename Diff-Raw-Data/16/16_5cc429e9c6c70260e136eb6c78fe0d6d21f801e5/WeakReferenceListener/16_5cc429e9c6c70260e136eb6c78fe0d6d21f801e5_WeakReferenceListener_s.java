 package com.od.jtimeseries.ui.util;
 
 import javax.swing.*;
 import java.lang.ref.WeakReference;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.*;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Nick Ebbutt (Object Definitions Ltd.)
  * <p/>
  * Create a dynamic proxy for a listener, and add the dynamic proxy to one or more observables in place of the real listener instance
  * The proxy delegates all events to the real listener, but only retains a WeakReference to the real listener instance.
  * This allows the real listener (and any associated resources) to be garbage collected without explicitly removing
  * its listeners from any observables.
  *
  * In this implementation, a background thread is used to keep track of all WeakReferenceListener instances currently in use.
  * Once each real listener wrapped by a WeakReferenceListener has been garbage collected, a background task will perform cleanup -
  * this will removing the dynamic proxy listener instance from any targetObservables to which it was added, and allow the
  * WeakReferenceListener itself to be garbage collected.
  *
  * This implementation of WeakReferenceListener is intended for use in Swing UIs
  * Only the Swing event thread should create WeakReferenceListeners and call addListenerTo() and removeListenerFrom()
  * An alternative synchronization strategy would be required to make this class thread safe for other usages.
  *
  * This implementation uses reflection to find and to call the methods on target observables which allow listeners to be
  * added and removed. This allows WeakReferenceListener to work for a wide range of swing listener interfaces 
  *
  * Usage:
  * <pre>
  * private PropertyChangeListener realListener = new PropertyChangeListener() {
  *      public void propertyChange(PropertyChangeEvent evt) {
  *          doSomeWork(evt);
  *      }
  * };
  *
  * public MyClass(List<MyBean> beans) {
  *      for ( MyBean b : beans ) {
  *          new WeakReferenceListener(realListener).addListenerTo(b);
  *
  *          //alternatively you can pass a parameter when the listener is added and removed:
  *          //new WeakReferenceListener("myPropertyName", realListener).addListenerTo(b);
  *      }
  * }
  * </pre>
  *
  */
 public class WeakReferenceListener {
 
     private static final boolean DEBUG_LOGGING = false;
     private static volatile int CLEANUP_PERIOD_SECONDS = 30;
     
     private static final Object cleanupLock = new Object();
     private static final ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
     private static boolean cleanupRunning;
 
     //all the weak reference listeners, we check these periodically to see if we can
     //the real listener is collected, and we can remove and garbage collect the weak listener
     private static final Set<WeakReferenceListener> listeners = new HashSet<WeakReferenceListener>();
 
     private volatile WeakReference delegateListener;
     private List<Class> listenerClassInterfaces;
     private Class listenerClass;
 
     //proxy listener added in place of delegateListener
     private Object proxyListener;
 
     //list of observables to which the proxy listener has been added
     //these need to be weak reference otherwise the WeakReferenceListener itself may hold on to them
     private Set<WeakReference<Object>> targetObservables = new HashSet<WeakReference<Object>>();
 
     //if set, the first argument to use for methods which add or remove the listener on target observables
     private Object firstAddAndRemoveArgument;
 
     public WeakReferenceListener(Object delegateListener) {
         this(null, delegateListener);
     }
 
     /**
      * @param firstAddAndRemoveArgument - not null only if the add and remove listener methods on target observables take an additional first argument (e.g. we want to add a PropertyChangeListener for a specific named property)
      * @param delegateListener, the listener we are proxying for
      */
     public WeakReferenceListener(Object firstAddAndRemoveArgument, Object delegateListener) {
         this.firstAddAndRemoveArgument = firstAddAndRemoveArgument;
         this.listenerClass = delegateListener.getClass();
         this.delegateListener = new WeakReference(delegateListener);
         this.listenerClassInterfaces = getAllInterfaces(delegateListener.getClass());
     }
 
     //set how often the background thread will attempt to deregister and cleanup old WeakReferenceListener
     public static void setCleanupPeriod(int seconds) {
         CLEANUP_PERIOD_SECONDS = seconds;
     }
 
     private LinkedList<Class> getAllInterfaces(Class c) {
         HashSet<Class> interfaces = new HashSet<Class>();
         addAllInterfaces(c, interfaces);
         return new LinkedList<Class>(interfaces);
     }
 
     private boolean isUseExtraParameterForListenerMethods() {
         return firstAddAndRemoveArgument != null;
     }
 
     private void addAllInterfaces(Class startClass, HashSet<Class> interfaces) {
         for (Class c : startClass.getInterfaces()) {
             interfaces.add(c);
         }
 
         //recursively add up the class heirarchy
         Class superClass = startClass.getSuperclass();
         if (superClass != null) {
             addAllInterfaces(superClass, interfaces);
         }
     }
 
     private boolean isDelegateCollected() {
         return delegateListener.get() == null;
     }
 
     public boolean addListenerTo(Object targetObservable) {
 
         Object proxyListener = getOrCreateProxyListener();
 
         //now find a method to add proxyListener to the observable
         boolean success = false;
         Method[] methods = targetObservable.getClass().getMethods();
         for ( Method m : methods ) {
             if (m.getName().startsWith("add") && hasRequiredParameters(m)) {
                 try {
                     Object[] args = getParameters(proxyListener);
                     if (DEBUG_LOGGING) logDebug("Will use add method " + m);
                     m.invoke(targetObservable, args);
                     success = true;
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 break;
             }
         }
 
         if ( success ) {
             addForCleanup(targetObservable);
         }
 
         return success;
     }
 
     private void addForCleanup(Object targetObservable) {
         synchronized(cleanupLock) {
             //keep a reference to the observable, so that we can remove the proxy listener from it
             targetObservables.add(new WeakReference<Object>(targetObservable));
 
              //we are going to add the proxy listener to target observables,
             //so add this weak reference listener to the queue so that we can remove
             //proxyListener from target observables if the delegateListener is collected
             listeners.add(this);
         }
 
         checkCleanupThreadStarted();
     }
 
     public boolean removeListenerFrom(Object targetObservable) {
 
         boolean success = false;
 
         //now find a method to remove proxyListener from the observable
         if ( proxyListener != null ) {
             Method[] methods = targetObservable.getClass().getMethods();
             for ( Method m : methods ) {
                 if ( m.getName().startsWith("remove") && hasRequiredParameters(m)) {
                     try {
                         Object[] args = getParameters(proxyListener);
                         if (DEBUG_LOGGING) logDebug("Will use remove method " + m);
                         m.invoke(targetObservable, args);
                         success = true;
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                     break;
                 }
             }
 
             if ( success ) {
                 removeFromCleanup(targetObservable);
             }
         }
 
         return success;
     }
 
     private void removeFromCleanup(Object targetObservable) {
         synchronized (cleanupLock) {
             Iterator<WeakReference<Object>> i = targetObservables.iterator();
             while( i.hasNext() ) {
                 WeakReference<Object> o = i.next();
                 if (o.get() == targetObservable) {
                     i.remove();
                     break;
                 }
             }
 
             removeFromCleanupIfNoTargetObservables();
         }
     }
 
     private void removeFromCleanupIfNoTargetObservables() {
         if ( targetObservables.size() == 0 ) {
             if (DEBUG_LOGGING) logDebug("No more targetObservables, remove weak reference listener " + this);
             listeners.remove(this);
         }
     }
 
     //check an add or remove listener method has the right parameters
     //if we are not using an extra argument, this means the only argument is a listener interface which is implemented by proxylistener
     //if we are using an extra first argument, check the class for that is also valid
     private boolean hasRequiredParameters(Method m) {
         boolean result = false;
         int expectedParameterCount = isUseExtraParameterForListenerMethods() ? 2 : 1;
         Class[] parameterClasses = m.getParameterTypes();
         if ( parameterClasses.length == expectedParameterCount ) {
             result = checkParameters(parameterClasses[expectedParameterCount - 1], parameterClasses);
         }
         return result;
     }
 
     private boolean checkParameters(Class parameterClass, Class[] parameterClasses) {
         boolean result = true;
         if ( isUseExtraParameterForListenerMethods() ) {
             result = parameterClasses[0].isAssignableFrom(firstAddAndRemoveArgument.getClass());
         }
         result &= listenerClassInterfaces.contains(parameterClass);
         return result;
     }
 
     //if we are supporting an extra parameter to listener add and remove, return this in parameter array
     private Object[] getParameters(Object proxyListener) {
         if ( firstAddAndRemoveArgument == null) {
             return new Object[] { proxyListener };
         } else {
             return new Object[] { firstAddAndRemoveArgument, proxyListener };
         }
     }
 
 
     private void checkCleanupThreadStarted() {
         if ( ! cleanupRunning ) {
             s.scheduleWithFixedDelay(new Runnable() {
                 public void run() {
                     List<WeakReferenceListener> currentListeners;
                     synchronized (cleanupLock) {
                         currentListeners = new LinkedList<WeakReferenceListener>(listeners);
                     }
 
                     List<WeakReferenceListener> toDispose = new LinkedList<WeakReferenceListener>();
 
                     for ( WeakReferenceListener l : currentListeners) {
                         if (DEBUG_LOGGING) logDebug("Checking weak ref listener " + l);
                         if ( l.isDelegateCollected() ) {
                             if (DEBUG_LOGGING) logDebug("Delegate listener collected, adding for disposal " + l);
                             toDispose.add(l);
                         }
                     }
 
                     for ( final WeakReferenceListener l : toDispose) {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 l.dispose();
                             }
                         });
                     }
                 }
             }, CLEANUP_PERIOD_SECONDS, CLEANUP_PERIOD_SECONDS, TimeUnit.SECONDS);
             cleanupRunning = true;
         }
     }
 
     private void dispose() {
         //targetObservables will change as we remove the listeners from it so take a snapshot to iterate
         List<WeakReference> snapshotObservables;
         synchronized (cleanupLock) {
             snapshotObservables = new LinkedList<WeakReference>(targetObservables);
         }
 
         //the delegate listener has been collected, good news! this is why we created a weak reference listener in the first place!
         //now we can remove this WeakReferenceListner from each of the observables it was added to and allow weak ref to be garbage collected
         for ( WeakReference<Object> observable : snapshotObservables) {
             Object o = observable.get();
             if ( o != null) {
                 if (DEBUG_LOGGING) logDebug("removing  " + this + " from target disposable " + o);
                 removeListenerFrom(o);
             } else {
                targetObservables.remove(observable);
             }
         }
 
        //perhaps all the target observables were collected
        //in that case, we don't need to keep this weak ref listener
        removeFromCleanupIfNoTargetObservables();       
     }
 
     private Object getOrCreateProxyListener() {
         if ( proxyListener == null ) {
             InvocationHandler handler = new InvocationHandler() {
 
                 public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                     Class<?>[] paramTypes = method.getParameterTypes();
                     if (method.getName().equals("equals") && paramTypes.length == 1 && paramTypes[0] == Object.class) {
                         //this is the equals method being called on the proxy, we need to handle it locally
                         return args[0] == proxy;
                     } else {
                         Object listener = delegateListener.get();
                         if (listener == null) {
                             return null;
                         } else {
                             return method.invoke(listener, args);
                         }
                     }
                 }
             };
 
             proxyListener = Proxy.newProxyInstance(listenerClass.getClassLoader(),
                     listenerClassInterfaces.toArray(new Class[listenerClassInterfaces.size()]),
                     handler);
             }
         return proxyListener;
     }
 
     private void logDebug(String s) {
         System.out.println("WeakReferenceListener: " + s);
     }
 
 }
