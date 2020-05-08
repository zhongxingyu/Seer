 package org.codehaus.xfire.service.invoker;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.util.ServiceUtils;
 
 /**
  * Abstract implementation of Invoker.
  * <p>
  * @author Ben Yu
  * Feb 10, 2006 10:57:23 PM
  */
 public abstract class AbstractInvoker implements Invoker {
   public Object invoke(final Method method, final Object[] params, final MessageContext context)
           throws XFireFault
   {
       Method m = null;
       try
       {
           final Object serviceObject = getServiceObject(context);
 
           Object[] newParams = params;
           for (int i = 0; i < method.getParameterTypes().length; i++)
           {
               if (method.getParameterTypes()[i].equals(MessageContext.class))
               {
                   newParams = new Object[params.length+1];
                   
                   for (int j = 0; j < newParams.length; j++)
                   {
                       if (j == i)
                       {
                           newParams[j] = context;
                       }
                       else if (j > i)
                       {
                           newParams[j] = params[j-1];
                       }
                       else
                       {
                           newParams[j] = params[j];
                       }
                   }
               }
           }
 
           m = matchMethod(method, serviceObject);
           
           return m.invoke(serviceObject, newParams);
       }
       catch (IllegalArgumentException e)
       {
           throw new XFireFault("Illegal argument invoking '" + ServiceUtils.getMethodName(method) + "': " + e.getMessage(), e, XFireFault.SENDER);
       }
       catch (InvocationTargetException e)
       {
           final Throwable t = e.getTargetException();
 
           if (t instanceof XFireFault)
           {
               throw (XFireFault) t;
           }
           else if (t instanceof Exception)
           {
               
               Class[] exceptions = m.getExceptionTypes();
               for( int i=0;i<exceptions.length;i++){
                  if(  exceptions[i].isAssignableFrom(t.getClass())){
                       throw new XFireFault(t, XFireFault.RECEIVER);
                   }
               }
               
               throw new XFireFault(t, XFireFault.SENDER);
           }
           else
           {
               throw new XFireRuntimeException("Error invoking '" + ServiceUtils.getMethodName(method) + '\'', e);
           }
       }
       catch (IllegalAccessException e)
       {
           throw new XFireFault("Couldn't access service object to invoke '" + ServiceUtils.getMethodName(method) + "': " + e.getMessage(), e, XFireFault.RECEIVER);
       }
   }
 
   /**
    * Creates and returns a service object depending on the scope.
    */
   public abstract Object getServiceObject(final MessageContext context)
           throws XFireFault;
   /**
    * Returns a Method that has the same declaring class as the
    * class of targetObject to avoid the IllegalArgumentException
    * when invoking the method on the target object. The methodToMatch
    * will be returned if the targetObject doesn't have a similar method.
    * 
    * @param methodToMatch The method to be used when finding a matching
    *                      method in targetObject
    * @param targetObject  The object to search in for the method. 
    * @return The methodToMatch if no such method exist in the class of
    *         targetObject; otherwise, a method from the class of
    *         targetObject matching the matchToMethod method.
    */
   private static Method matchMethod(Method methodToMatch, Object targetObject) {
       if (isJdkDynamicProxy(targetObject)) {
           Class[] interfaces = targetObject.getClass().getInterfaces();
           for (int i = 0; i < interfaces.length; i++) {
               Method m = getMostSpecificMethod(methodToMatch, interfaces[i]);
               if (!methodToMatch.equals(m)) {
                   return m;
               }
           }
       }
       return methodToMatch;
   }
 
   /**
    * Return whether the given object is a J2SE dynamic proxy.
    * 
    * @param object the object to check
    * @see java.lang.reflect.Proxy#isProxyClass
    */
   public static boolean isJdkDynamicProxy(Object object) {
       return (object != null && Proxy.isProxyClass(object.getClass()));
   }
 
   /**
    * Given a method, which may come from an interface, and a targetClass
    * used in the current AOP invocation, find the most specific method
    * if there is one. E.g. the method may be IFoo.bar() and the target
    * class may be DefaultFoo. In this case, the method may be
    * DefaultFoo.bar(). This enables attributes on that method to be found.
    * 
    * @param method method to be invoked, which may come from an interface
    * @param targetClass target class for the curren invocation. May
    *        be <code>null</code> or may not even implement the method.
    * @return the more specific method, or the original method if the
    *         targetClass doesn't specialize it or implement it or is null
    */
   public static Method getMostSpecificMethod(Method method, Class targetClass) {
       if (method != null && targetClass != null) {
           try {
               method = targetClass.getMethod(method.getName(), method.getParameterTypes());
           }
           catch (NoSuchMethodException ex) {
               // Perhaps the target class doesn't implement this method:
               // that's fine, just use the original method
           }
       }
       return method;
   }
 }
