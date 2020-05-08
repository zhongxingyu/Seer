 package com.logicalpractice.collections.support;
 
 import java.lang.reflect.Modifier;
 
 import net.sf.cglib.proxy.Enhancer;
 
 /**
  * Supplies and holds a CapturingProxy bound to the current Thread.
  * {@link #capture(Class)} uses CGLIB
 * {@link net.sf.cglib.proxy.Enhancer#create(Class,net.sf.cglib.proxy.Callback) Enchancer.create(Class,Callback)}
  * to provide the instance.
  * <br/>Class must:
  * <ul>
  * <li>not be null</li>
  * <li>not marked as final</li>
  * <li>not be a primitive</li>
  * <li>must have a visable no args constructor</li>
  * </ul>
  * <p>
  *    The contract of use is that once finished with the {@link #capture(Class)} method the caller must call
  *    {@link #clearAndReturn()} in order to release the reference that is maintained via a thread local.
  *    Failing to do this will result the reference being maintained and a potiental memory leak will exist.
  * </p>
  * <p>
  *    it is worth noting that CGLIB will create an instance of class in order to provide the proxy, the CapturingProxy
  *    will prevent any of it's methods from actually called, but would be a good idea to avoid classes that do
  *    anything in there default constructor.
  * </p>
  * @author gareth
  */
 public class MethodCapture {
 
    /**
     * Holder for the current CapturingProxy.
     */
    private final static ThreadLocal<CapturingProxy<?,?>> context = new ThreadLocal<CapturingProxy<?,?>>();
 
    /**
     * Returns and starts the capture proxy.
     * See the class description above for restrictions of use.
     * The CapturingProxy that is started can be obtained via {@link #clearAndReturn()}
     * @param <T> Type of the proxy
     * @param cls Class of type
     * @return instance of type T
     * @throws IllegalArgumentException if the class is unsuitable for proxying
     * @throws IllegalStateException if there is an existing CapturingProxy bound to this thread
     */
    @SuppressWarnings("unchecked")
    public final static <T> T capture(Class<T> cls) {
       checkClass(cls);
       
       if( context.get() != null ){
          throw new IllegalStateException("An existing CapturingProxy() exists for this thread, calls to capture cannot be nested, or clearAndReturn has not been called correctly");
       }
       context.set(new CapturingProxy<Object,Object>());
 
       return (T) Enhancer.create(cls, context.get());
    }
 
    public final static CapturingProxy<?,?> clearAndReturn() {
       CapturingProxy<?,?> capture = context.get();
       context.set(null);
       return capture;
    }
 
    private static <T> void checkClass(Class<T> cls) {
       if (cls == null) {
          throw new IllegalArgumentException("cls is null");
       }
       if (cls.isPrimitive()) {
          throw new IllegalArgumentException("cls {" + cls.getName() + "} is primitive and cannot be captured");
       }
       if (Modifier.isFinal(cls.getModifiers())) {
          throw new IllegalArgumentException("cls {" + cls.getName() + "} is final and therefore cannot be captured");
       }
    }
 }
