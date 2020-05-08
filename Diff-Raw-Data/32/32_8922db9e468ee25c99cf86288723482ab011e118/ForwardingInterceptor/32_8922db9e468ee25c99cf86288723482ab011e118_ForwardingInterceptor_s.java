 /*
  * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *   - Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *   - Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *
  *   - Neither the name of Sun Microsystems nor the names of its
  *     contributors may be used to endorse or promote products derived
  *     from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package com.sudothought.jmx;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.management.remote.MBeanServerForwarder;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * An invocation handler that implement an intercepting MBeanServerForwarder.
  * The MBeanServerForwarder can be configured with a set of {@link
  * InvokeOperationInterceptor} that can intercept a specific method call
  * invoke on an MBean (or a set of MBeans defined by a pattern).
  * <p/>
  * See {@link Stopper} for a concrete example.
  *
  * @author dfuchs
  */
 public class ForwardingInterceptor implements InvocationHandler {
 
     private final static class Methods {
         final static Method invokeMethod;
         final static Method getClassLoaderForMethod;
         final static Method setMBeanServerMethod;
         final static Method getMBeanServerMethod;
 
         static {
             try {
                 invokeMethod = MBeanServer.class.getMethod("invoke",
                                                            new Class[]{
                                                                    ObjectName.class, // MBean
                                                                    String.class, // Method name
                                                                    Object[].class, // Parameters
                                                                    String[].class    // Signature
                                                            });
                 getClassLoaderForMethod = MBeanServer.class.getMethod(
                         "getClassLoaderFor", new Class[]{
                                 ObjectName.class
                         });
                 setMBeanServerMethod = MBeanServerForwarder.class.getMethod(
                         "setMBeanServer", new Class[]{
                                 MBeanServer.class
                         });
                 getMBeanServerMethod = MBeanServerForwarder.class.getMethod(
                         "getMBeanServer", new Class[]{});
             }
             catch (NoSuchMethodException x) {
                 throw new ExceptionInInitializerError(x);
             }
         }
     }
 
     private volatile MBeanServer server = null;
     private final List<InvokeOperationInterceptor> interceptedCalls;
 
    public ForwardingInterceptor(MBeanServer server,
                                 InvokeOperationInterceptor... intercepted) {
         this.server = server;
        this.interceptedCalls =
                new CopyOnWriteArrayList<InvokeOperationInterceptor>();
         this.interceptedCalls.addAll(Arrays.asList(intercepted));
     }
 
     public void addInterceptedCall(InvokeOperationInterceptor call) {
         interceptedCalls.add(call);
     }
 
     public boolean removeInterceptedCall(InvokeOperationInterceptor call) {
         return interceptedCalls.remove(call);
     }
 
     public Object invoke(Object proxy, Method method, Object[] args)
             throws Throwable {
         final Class declaring = method.getDeclaringClass();
         if (MBeanServer.class.equals(declaring)) {
             return invokeOnServer(proxy, method, args);
         }
         else if (Object.class.equals(declaring)) {
             return invokeOnThis(proxy, method, args);
         }
         else if (MBeanServerForwarder.class.equals(declaring)) {
             return invokeOnThis(proxy, method, args);
         }
         throw new IllegalArgumentException(method.getName());
     }
 
    private Object intercept(InvokeOperationInterceptor c,
                             Method method, Object on, Object[] args) throws Throwable {
        return c.intercept(server, (ObjectName)args[0], (String)args[1],
                           (Object[])args[2], (String[])args[3]);
     }
 
    private Object invokeOnServer(Object proxy, Method method,
                                  Object[] args) throws Throwable {
         try {
             if (method.equals(Methods.invokeMethod)) {
                 for (InvokeOperationInterceptor c : interceptedCalls) {
                     if (isIntercepting(c, method, args))
                         return intercept(c, method, server, args);
                 }
             }
             else if (method.equals(Methods.getClassLoaderForMethod)) {
                 final ObjectName mbean = (ObjectName)args[0];
                 for (InvokeOperationInterceptor c : interceptedCalls) {
                     if (c.matches(mbean))
                         return c.getClassLoaderFor(server, mbean);
                 }
             }
             return method.invoke(server, args);
         }
         catch (InvocationTargetException ex) {
             throw ex.getTargetException();
         }
     }
 
    private Object invokeOnThis(Object proxy, Method method,
                                Object[] args) throws Throwable {
         if (Methods.setMBeanServerMethod.equals(method)) {
             setMBeanServer((MBeanServer)args[0]);
             return null;
         }
         if (Methods.getMBeanServerMethod.equals(method)) {
             return getMBeanServer();
         }
 
         try {
             return method.invoke(this, args);
         }
         catch (InvocationTargetException ex) {
             throw ex.getTargetException();
         }
     }
 
     public void setMBeanServer(MBeanServer mbs) {
         this.server = mbs;
     }
 
     public MBeanServer getMBeanServer() {
         return server;
     }
 
    private boolean isIntercepting(InvokeOperationInterceptor c, Method method,
                                   Object[] args) {
         if (!method.equals(Methods.invokeMethod)) return false;
         if (!c.matches((ObjectName)args[0])) return false;
         if (!c.matches((String)args[1])) return false;
         return true;
     }
 
 
     public static MBeanServerForwarder newForwardingInterceptor() {
         return newForwardingInterceptor(null, new InvokeOperationInterceptor[0]);
     }
 
     public static MBeanServerForwarder newForwardingInterceptor(InvokeOperationInterceptor... interceptors) {
         return newForwardingInterceptor(null, interceptors);
     }
 
     public static MBeanServerForwarder newForwardingInterceptor(MBeanServer server) {
         return newForwardingInterceptor(server, new InvokeOperationInterceptor[0]);
     }
 
     public static MBeanServerForwarder newForwardingInterceptor(MBeanServer server,
                                                                 InvokeOperationInterceptor... interceptors) {
         return newForwardingInterceptor(new ForwardingInterceptor(server, interceptors));
     }
 
     public static MBeanServerForwarder newForwardingInterceptor(ForwardingInterceptor interceptor) {
         return (MBeanServerForwarder)Proxy.newProxyInstance(null, new Class[]{MBeanServerForwarder.class}, interceptor);
     }
 
     public static void addInterceptor(Object proxy, InvokeOperationInterceptor interceptor) {
         final InvocationHandler h = Proxy.getInvocationHandler(proxy);
         if (!(h instanceof ForwardingInterceptor))
             throw new IllegalArgumentException("Proxy not handled by a " + ForwardingInterceptor.class.getName());
         ForwardingInterceptor i = (ForwardingInterceptor)h;
         i.addInterceptedCall(interceptor);
     }
 
     public static boolean removeInterceptor(Object proxy, InvokeOperationInterceptor interceptor) {
         final InvocationHandler h = Proxy.getInvocationHandler(proxy);
         if (!(h instanceof ForwardingInterceptor))
             throw new IllegalArgumentException("Proxy not handled by a " + ForwardingInterceptor.class.getName());
         ForwardingInterceptor i = (ForwardingInterceptor)h;
         return i.removeInterceptedCall(interceptor);
     }
 }
