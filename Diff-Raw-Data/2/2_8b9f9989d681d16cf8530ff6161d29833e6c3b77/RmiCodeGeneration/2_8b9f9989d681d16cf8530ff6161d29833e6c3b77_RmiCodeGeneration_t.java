 package com.flaptor.util.remote;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.ServerException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtMethod;
 
 import org.apache.log4j.Logger;
 
 import com.flaptor.util.Execute;
 
 /**
  * Class for automatically generating Rmi boilerplate code.
  * 
  * @author Martin Massera
  *
  */
 public class RmiCodeGeneration {
     private static Logger logger = Logger.getLogger(Execute.whoAmI());
     private static final Map<String, Class> classMap = new HashMap<String, Class>(); 
     
     private static final Set<Method> objectMethods = new HashSet<Method>(Arrays.asList(Object.class.getMethods()));
     /**
      * it gives you an object with the same methods as the original one,
      * but implementing Remote and throwing RemoteException in all its methods. 
      * It can handle exceptions so they are sent all the way to the client (calling) code.
      * 
      * @param remoteClassName the fully qualified name of the generated remote interface. The returned handler will 
      * be of this class name. This is important because on the client side, RMI loads this interface (you can generate it
      * using getRemoteInterface or using reconnectableStub)
      * @param interfaces the interfaces that the object will expose
      * @param originalHandler
      * @return  
      */
     public static Remote remoteHandler( String remoteClassName, Class[] interfaces, final Object originalHandler) {
         try {
             logger.debug("creating remote server for  " + originalHandler);
             Class handlerInterface = getRemoteInterface(remoteClassName, interfaces);
 
             logger.debug("constructing method map");
             final Map<Method, Method> methodMap = getMethodMap(new Class[]{handlerInterface}, originalHandler.getClass());
             
             InvocationHandler handler = new InvocationHandler() {
                 public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                     if (objectMethods.contains(method)) { //execute Object methods (toString, etc.)
                         return method.invoke(originalHandler, args);
                     } 
                     Method originalMethod = methodMap.get(method);;
                     logger.debug("invoking original method " + originalMethod + " for " + method);
                     try {
                         return originalMethod.invoke(originalHandler, args);
                     } catch (InvocationTargetException e) {
                         logger.debug("wrapping throwable in RemoteException");
                         throw new RemoteException(RemoteHostCodeException.THROWABLE_IN_REMOTE_HOST_CODE, e.getCause());
                     } catch (Throwable t) {
                         throw new RemoteException(RemoteHostCodeException.THROWABLE_IN_REMOTE_HOST_CODE, t);
                     }
                 }
             };
             return (Remote)Proxy.newProxyInstance(handlerInterface.getClassLoader(), new Class[]{handlerInterface}, handler);
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
 
     public static Class getRemoteInterface(String remoteClassName, Class[] interfaces) {
         try{ 
             ClassPool cp = ClassPool.getDefault();
             logger.debug("interface name " + remoteClassName);
             Class handlerInterface = classMap.get(remoteClassName);
             if (handlerInterface == null) {
                 CtClass generatedInterf = cp.makeInterface(remoteClassName);
                 generatedInterf.setSuperclass(cp.get(Remote.class.getCanonicalName()));
                 for (Class interf : interfaces) {
                     // adding super interfaces
                     Class enclosingClass = interf.getDeclaringClass();
                     if ( null == enclosingClass)  {
                         generatedInterf.setSuperclass(cp.get(interf.getCanonicalName()));
                     } else { // special case for inner interfaces
                         String clazzName = interf.getCanonicalName();
                         int lastIndexOf = clazzName.lastIndexOf(".");
                         if ( lastIndexOf > 0) 
                             clazzName = clazzName.substring(0,lastIndexOf) + "$" + clazzName.substring(lastIndexOf + 1 );
                         generatedInterf.setSuperclass(cp.get(clazzName));
                     }
 
                     for (Method method : interf.getMethods()) {
                         if (objectMethods.contains(method)) {
                             logger.debug("skipping method from object: " + method);
                             continue;
                         }
                         logger.debug("processing method " + method.getName());
                         
                         Class retType = method.getReturnType();
                         CtClass newRetType = cp.get(retType.getCanonicalName());
                         Class[] paramTypes = method.getParameterTypes();
                         CtClass[] newParamTypes = new CtClass[paramTypes.length];
                         for (int i = 0; i < paramTypes.length; ++i) newParamTypes[i] = cp.get(paramTypes[i].getCanonicalName());
         
                         CtMethod newMethod = new CtMethod(newRetType, method.getName(),newParamTypes, generatedInterf);
                         Class[] declaredExceptions = method.getExceptionTypes();
                         CtClass[] knownExceptions = new CtClass[declaredExceptions.length + 1];
                         for (int i = 0; i < declaredExceptions.length; i++) {
                             knownExceptions[i] = cp.get(declaredExceptions[i].getCanonicalName());
                         }
                         knownExceptions[declaredExceptions.length] = cp.get(RemoteException.class.getCanonicalName());
                         newMethod.setExceptionTypes(knownExceptions);
                         generatedInterf.addMethod(newMethod);
                     }
                 }
                handlerInterface = generatedInterf.toClass(ClassLoader.getSystemClassLoader());
                 classMap.put(remoteClassName, handlerInterface);
             }
             return handlerInterface;
         } catch (Exception e) {
             e.printStackTrace();
             throw new RuntimeException(e);
         }
     }
     /**
      * Given a remote, creates a proxy of the provided interface that calls methods in that remote
      * If exceptions are thrown in the remote server code, they are thrown through the original interface
      * as the original exception. Other exceptions should be caught as RemoteException
      * 
      * @param remote
      * @param originalInterface
      * @return 
      */
     public static Object proxy(final Remote remote, Class[] originalInterfaces) {
         logger.debug("constructing reverse method map");
         final Map<Method, Method> methodMap = getMethodMap(originalInterfaces, remote.getClass());
         InvocationHandler handler = new InvocationHandler() {
             
             @SuppressWarnings("unchecked")
             public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                 Method originalMethod = methodMap.get(method);
                 logger.debug("invoking original method " + originalMethod + " for " + method);
                 try {
                     return originalMethod.invoke(remote, args);
                 } catch (InvocationTargetException e) {
                     Throwable t = e.getCause();
                     if (t instanceof ServerException) {
                         if (t.getCause() instanceof RemoteException && t.getCause().getMessage().contains(RemoteHostCodeException.THROWABLE_IN_REMOTE_HOST_CODE)) {
                             Throwable remoteCodeException = t.getCause().getCause();
                             if (RuntimeException.class.isAssignableFrom(remoteCodeException.getClass())) throw remoteCodeException;
                             for (Class exceptionClass: method.getExceptionTypes()) {
                                 //if we can throw it as is, throw it
                                 if (exceptionClass.isAssignableFrom(remoteCodeException.getClass())) throw remoteCodeException;
                             }
                             //otherwise throw it as a runtime exception
                             throw new RemoteHostCodeException(remoteCodeException);
                         }
                     }
                     logger.warn("unexpected exception", t);
                     throw t;
                 }
             }
         };
         return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), originalInterfaces, handler);
     }
     
     private static Map<Method, Method> getMethodMap(Class[] calledClasses, Class originalClass) {
         final Map<Method, Method> methodMap = new HashMap<Method, Method>();
         for (Class calledClass : calledClasses) {
             for (Method calledMethod : calledClass.getMethods()) {
                 for (Method originalMethod : originalClass.getMethods()) {
                     if (calledMethod.getName().equals(originalMethod.getName())) {
                         Class[] params1 = calledMethod.getParameterTypes();
                         Class[] params2 = originalMethod.getParameterTypes();
                         if (params1.length != params2.length) continue;
                                                 
                         boolean equals = true;
                         for (int i = 0; i < params1.length; ++i) {
                             if (!params1[i].getCanonicalName().equals(params2[i].getCanonicalName())) {
                                 equals = false;
                                 break;
                             }
                         }
                         if (equals) {
                             logger.debug(calledMethod + " ->" + originalMethod);
                             methodMap.put(calledMethod, originalMethod);
                         }
                     }
                 }
             }
         }
         return methodMap;
     }
     
     /**
      * generates a reconnectable stub
      * 
      * @param objectInterface
      * @param context context of the rmi handler, if null uses default
      * @param host rmi server host
      * @param port rmi server port
      * @param policy
      * @return an object that implements objectInterface
      */
     public static Object reconnectableStub(String remoteClassName, final Class[] interfaces, String context, String host, int port, IRetryPolicy policy) {
         //need to generate the remote interface because RMI will instantiate it
         final Class[] remoteInterfaces = new Class[]{getRemoteInterface(remoteClassName, interfaces)};
         final StubbingInvocationHandler stubbingInvocationHandler = new StubbingInvocationHandler();
         final ARmiClientStub myStub = new ARmiClientStub(port, host, context, policy) {
             protected void setRemote(Remote stub) {
                 stubbingInvocationHandler.setObjectProxy(proxy(stub, remoteInterfaces));
             }
         };
         stubbingInvocationHandler.setRmiStub(myStub);
         return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), remoteInterfaces, stubbingInvocationHandler);
     }
 
     private static class StubbingInvocationHandler implements InvocationHandler {
         private ARmiClientStub rmiStub;
         private Object objectProxy;
         public void setRmiStub(ARmiClientStub rmiStub) {
             this.rmiStub = rmiStub;
         }
         public void setObjectProxy(Object objectProxy) {
             this.objectProxy = objectProxy;
         }
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         
             
             if (objectMethods.contains(method)) {
                 if (logger.isDebugEnabled()) logger.debug("running Object method " + method);
                 return method.invoke(this,args);
             }
             
             
             try { 
                 rmiStub.checkConnection();
                 Object ret = method.invoke(objectProxy, args);
                 rmiStub.connectionSuccess();
                 return ret;
             } catch (RemoteException re) { // this happens due to connection failure on checkConnection
                 logger.error(re,re);
                 rmiStub.connectionFailure();
                 throw new RpcException(re);
             } catch (InvocationTargetException e) { // this happens due to exceptions on method.invoke
                 if (e.getCause() instanceof RemoteException) { 
                     logger.error(e,e);
                     rmiStub.connectionFailure();
                     throw new RpcException(e);
                 } else {
                     //other exceptions were thrown in the server code, we must throw them
                     rmiStub.connectionSuccess();
                     throw e.getCause();
                 }
             }
         }
 
         
         public boolean equals(Object other) {
             if (null == other) return false;
             if ( !(other instanceof Proxy) && !(other instanceof StubbingInvocationHandler)) return false;
             if ( other instanceof Proxy) {
                 InvocationHandler ih = Proxy.getInvocationHandler(other);
                 if (ih instanceof StubbingInvocationHandler) return rmiStub.equals(((StubbingInvocationHandler)ih).rmiStub);
                 // else
                 return false;
             }
             // else it is a StubbingInvocationHandler
 
             return rmiStub.equals(((StubbingInvocationHandler)other).rmiStub);
         }
         public int hashCode(){
             if (null == rmiStub) return 0;
             return rmiStub.hashCode();
         }
         
     };
 }
