 /**
  *   Copyright 2011 Karl Martens
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *       
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  *   net.karlmartens.platform, is a library of shared basic utility classes
  */
 package net.karlmartens.platform.util;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 public final class ReflectSupport {
 
   public static Object invoke(String methodName, Object instance, Class<?>[] argTypes, Object[] args) {
     try {
      final Method method = instance.getClass().getDeclaredMethod(methodName, argTypes);
       method.setAccessible(true);
       return method.invoke(instance, args);
     } catch (NoSuchMethodException e) {
       throw new RuntimeException(e);
     } catch (InvocationTargetException e) {
       throw new RuntimeException(e);
     } catch (IllegalAccessException e) {
       throw new RuntimeException(e);
     }
   }
 
   public static Object invoke(String methodName, Object instance, Class<?> argType, Object arg) {
     return invoke(methodName, instance, new Class[] { argType }, new Object[] { arg });
   }
 }
