 package com.github.multicall.grabber.impl;
 
 import com.github.multicall.grabber.MethodCall;
 import net.sf.cglib.proxy.MethodProxy;
 
 import java.lang.reflect.Method;
 import java.text.MessageFormat;
import java.util.Arrays;
 
 /**
  * Method call intercepted by cglib.
  */
 class InterceptedCall<T, U> implements MethodCall<T, U> {
     private Method method;
     private MethodProxy proxy;
     private Object[] args;
 
     InterceptedCall(Method method, MethodProxy proxy, Object[] args) {
         this.method = method;
         this.proxy = proxy;
         this.args = args;
     }
 
     @Override
     public U replayUnsafe(T obj) {
         try {
             return replay(obj);
         } catch (Throwable t) {
             if (t instanceof RuntimeException) {
                 throw (RuntimeException) t;
             } else if (t instanceof Error) {
                 throw (Error) t;
             } else {
                 throw new RuntimeException(t.getMessage(), t); // Indicates programmer's error
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public U replay(T obj) throws Throwable {
         return (U) proxy.invoke(obj, args);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public <V extends U> MethodCall<T, V> returns(Class<V> clazz) {
         if (clazz.isAssignableFrom(method.getReturnType())) {
             return (MethodCall<T, V>) this;
         } else {
             throw new ClassCastException(MessageFormat.format(
                    "Return type [{2}] of method [{0}.{1}] is not compatible with [{3}]",
                    method.getDeclaringClass().getName(), method.getName(), clazz.getName(), method.getReturnType())
             );
         }
     }

    @Override
    public String toString() {
        return "InterceptedCall{" +
                "method=" + method +
                ", proxy=" + proxy +
                ", args=" + (args == null ? null : Arrays.asList(args)) +
                '}';
    }
 }
