 package net.joala.dns;
 
 import com.google.common.base.Objects;
 import sun.net.spi.nameservice.NameService;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import static java.lang.String.format;
 
 /**
  * <p>
  * Fallback DNS resolution strategy using Java Default Implementation. As noted in the
  * {@link net.joala.dns package description} this is actually a hack using Java Reflection
  * to access private methods and fields. Thus it will most likely fail with some JVM versions.
  * Since Java 7 it is recommended to provide your own order of DNS providers using an ordered
  * list of {@code sun.net.spi.nameservice.provider.#}.
  * </p>
  *
  * @since 10/6/12
  * @see net.joala.dns package documentation
  */
class FallbackInetAddressNameService implements NameService {
   /**
    * A logger using System-PrintStreams.
    */
   private static final SystemLogger LOG = SystemLogger.getLogger(FallbackInetAddressNameService.class);
   /**
    * Class (verified to exist in Java 6 & 7) that is responsible for creating instances of
    * {@code InetAddressImpl}.
    */
   private static final String JAVA_NET_INET_ADDRESS_IMPL_FACTORY = "java.net.InetAddressImplFactory";
   /**
    * Method hidden in {@code InetAddressImpl}.
    */
   private static final String LOOKUP_ALL_HOST_ADDR = "lookupAllHostAddr";
   /**
    * Method hidden in {@code InetAddressImpl}.
    */
   private static final String GET_HOST_BY_ADDR = "getHostByAddr";
 
   /**
    * Threadsafe lazy initialization pattern.
    */
   @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
   private static class NameServiceHolder {
     private static final FallbackInetAddressNameService ourInstance = new FallbackInetAddressNameService();
   }
 
   /**
    * <p>
    * Retrieve singleton instance. On failure it is expected that either the JVM does not provide the classes/methods
    * accessed in here or that the installed SecurityManager prohibits any access.
    * </p>
    *
    * @return instance of the fallback service
    */
   public static FallbackInetAddressNameService fallbackNameService() {
     return NameServiceHolder.ourInstance;
   }
 
   /**
    * Reference to the wrapped {@code InetAddressImpl}.
    */
   private final Object inetAddressImpl;
   /**
    * Method in {@code InetAddressImpl} to lookup all hosts for a given hostname.
    */
   private final Method lookupAllHostAddrMethod;
   /**
    * Method in {@code InetAddressImpl} to lookup a host by its address.
    */
   private final Method getHostByAddrMethod;
 
   /**
    * <p>
    * Constructor. On failure it is expected that either the JVM does not provide the classes/methods
    * accessed in here or that the installed SecurityManager prohibits any access.
    * </p>
    */
   private FallbackInetAddressNameService() {
     try {
       inetAddressImpl = getInetAddressImpl();
       lookupAllHostAddrMethod = getLookupAllHostAddrMethod();
       getHostByAddrMethod = getGetHostByAddrMethod();
     } catch (Exception e) {
       throw new RuntimeException("Unable to access JVM private API as it seems.", e);
     }
   }
 
   /**
    * Retrieve accessible method.
    *
    * @return method, which is accessible
    * @throws NoSuchMethodException if the method does not exist; unsupported JVM is assumed
    */
   private Method getGetHostByAddrMethod() throws NoSuchMethodException {
     final Class<?> wrappedClass = inetAddressImpl.getClass();
     final Method method = wrappedClass.getMethod(GET_HOST_BY_ADDR, byte[].class);
     method.setAccessible(true);
     return method;
   }
 
   /**
    * <p>
    * Retrieve accessible method.
    * </p>
    *
    * @return method, which is accessible
    * @throws NoSuchMethodException if the method does not exist; unsupported JVM is assumed
    */
   private Method getLookupAllHostAddrMethod() throws NoSuchMethodException {
     final Class<?> wrappedClass = inetAddressImpl.getClass();
     final Method method = wrappedClass.getMethod(LOOKUP_ALL_HOST_ADDR, String.class);
     method.setAccessible(true);
     return method;
   }
 
   /**
    * <p>
    * Retrieve instance of {@code InetAddressImpl}.
    * </p>
    *
    * @return instance
    * @throws NoSuchMethodException     if the factory for {@code InetAddressImpl} does not provide the expected method;
    *                                   unsupported JVM is assumed
    * @throws ClassNotFoundException    if the factory for {@code InetAddressImpl}  is unknown; unsupported JVM is assumed
    * @throws IllegalAccessException    if for example a security manager prohibits access
    * @throws InvocationTargetException if it failed to call the factory method
    */
   private Object getInetAddressImpl() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
     final Class<?> factory = Class.forName(JAVA_NET_INET_ADDRESS_IMPL_FACTORY, true, InetAddress.class.getClassLoader());
     final Method method = factory.getDeclaredMethod("create");
     method.setAccessible(true);
     return method.invoke(null);
   }
 
   @Override
   public InetAddress[] lookupAllHostAddr(final String host) throws UnknownHostException {
     try {
       LOG.info("Redirecting lookupAllHostAddr() to " + inetAddressImpl.getClass().getName());
       return (InetAddress[]) lookupAllHostAddrMethod.invoke(inetAddressImpl, host);
     } catch (IllegalAccessException e) {
      throw createForwardException("lookupAllHostAddr", e);
     } catch (InvocationTargetException e) {
      throw createForwardException("lookupAllHostAddr", e);
     }
   }
 
   @Override
   public String getHostByAddr(final byte[] addr) throws UnknownHostException {
     try {
       LOG.info("Redirecting getHostByAddr() to " + inetAddressImpl.getClass().getName());
       return (String) getHostByAddrMethod.invoke(inetAddressImpl, new Object[]{addr});
     } catch (IllegalAccessException e) {
      throw createForwardException("lookupAllHostAddr", e);
     } catch (InvocationTargetException e) {
      throw createForwardException("lookupAllHostAddr", e);
     }
   }
 
   /**
    * <p>
    * Adds especially a cause to the {@link UnknownHostException}.
    * </p>
    *
    * @param methodName method which failed
    * @param cause      cause
    * @return exception with a cause
    */
   private UnknownHostException createForwardException(final String methodName, final Throwable cause) {
     final UnknownHostException exception = new UnknownHostException(format("Unable to forward request to %s", methodName));
     exception.initCause(cause);
     return exception;
   }
 
   @Override
   public String toString() {
     return Objects.toStringHelper(this)
             .add("inetAddressImpl", inetAddressImpl)
             .add("getHostByAddrMethod", getHostByAddrMethod)
             .add("lookupAllHostAddrMethod", lookupAllHostAddrMethod)
             .toString();
   }
 }
