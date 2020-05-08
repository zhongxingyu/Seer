 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 
 package cx.ath.mancel01.dependencyshot.aop.v2;
 
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import org.aopalliance.intercept.MethodInvocation;
 
 /**
  *
  * @author Mathieu ANCELIN
  */
 public class MethodJoinpoint implements MethodInvocation {
 
     private final Method method;
 
     private final Object[] arguments;
 
     private final Object interceptedBean;
 
     private final Iterator<MethodInterceptorWrapper> interceptor;
 
     private final MethodInvocationHandler staticPart;
 
     public MethodJoinpoint(Method method, Object[] arguments,
             Iterator<MethodInterceptorWrapper> interceptor,
             Object interceptedBean, 
             MethodInvocationHandler staticPart) {
         this.method = method;
         this.arguments = arguments;
         this.interceptor = interceptor;
         this.interceptedBean = interceptedBean;
         this.staticPart = staticPart;
     }
 
     @Override
     public Method getMethod() {
         return method;
     }
 
     @Override
     public Object[] getArguments() {
         return arguments;
     }
 
     @Override
     public Object proceed() throws Throwable {
         if (interceptor.hasNext()) {
             MethodInterceptorWrapper wrap = interceptor.next();
             if (!wrap.canBeAppliedOn(method)) {
                 if (interceptor.hasNext()) {
                     wrap = interceptor.next();
                 } else {
                     return method.invoke(interceptedBean, arguments);
                 }
             }
             return wrap.invoke(this);
         } else {
             return method.invoke(interceptedBean, arguments);
         }
     }
 
     @Override
     public Object getThis() {
         return interceptedBean;
     }
 
     @Override
     public AccessibleObject getStaticPart() {
        return method;
     }
 }
