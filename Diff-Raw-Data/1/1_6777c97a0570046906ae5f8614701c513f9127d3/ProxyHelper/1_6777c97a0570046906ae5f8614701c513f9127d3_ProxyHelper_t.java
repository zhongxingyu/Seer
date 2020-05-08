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
 
 package cx.ath.mancel01.dependencyshot.aop;
 
 import javassist.util.proxy.ProxyFactory;
 import javassist.util.proxy.ProxyObject;
 
 /**
  * Helper for proxy creation.
  *
  * @author Mathieu ANCELIN
  */
 public class ProxyHelper {
 
     public static <T> T createProxy(
             Object bean, MethodInvocationHandler handler) {
         ProxyFactory fact = new ProxyFactory();
         Class<T> from = handler.getFrom();
         Class<? extends T> to = handler.getTo();
         if (from.isInterface()) {
             fact.setInterfaces(new Class[] {from});
         } 
         fact.setSuperclass(to);
         fact.setFilter(handler);
         Class newBeanClass = fact.createClass();
         T scopedObject = null;
         try {
             scopedObject = (T) newBeanClass.cast(newBeanClass.newInstance());
         } catch (Exception ex) {
             throw new IllegalStateException("Unable to create proxy for object " + from.getSimpleName(), ex);
         }
         ((ProxyObject) scopedObject).setHandler(handler);
         return scopedObject;
     }
 }
