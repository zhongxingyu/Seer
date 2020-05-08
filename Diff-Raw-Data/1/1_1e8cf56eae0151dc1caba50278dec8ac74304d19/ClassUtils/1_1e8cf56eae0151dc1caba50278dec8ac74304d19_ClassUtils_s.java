 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.core.util;
 
 
 import org.apache.bcel.util.SyntheticRepository;
 import org.apache.bcel.util.ClassPath;
 import org.apache.bcel.classfile.JavaClass;
 import org.apache.bcel.generic.ClassGen;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Constructor;
 import java.io.IOException;
 import java.io.File;
 
 /**
  * @author werpu
  *         <p/>
  *         A generic utils class dealing with different aspects
  *         (naming and reflection) of java classes
  */
 public class ClassUtils {
 
 
     public static Class forName(String name) {
         try {
             return Class.forName(name);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static Object instantiate(String clazz, Object... varargs) {
         return instantiate(forName(clazz), varargs);
     }
 
     public static Object instantiate(Class clazz, Object... varargs) {
         Class[] classes = new Class[varargs.length];
         for (int cnt = 0; cnt < varargs.length; cnt++) {
 
             if (varargs[cnt] instanceof Cast) {
                 classes[cnt] = ((Cast) varargs[cnt]).getClazz();
                 varargs[cnt] = ((Cast) varargs[cnt]).getValue();
             } else {
                 classes[cnt] = varargs[cnt].getClass();
             }
         }
 
         Constructor constr = null;
         try {
             constr = clazz.getConstructor(classes);
             return (Object) constr.newInstance(varargs);
         } catch (NoSuchMethodException e) {
             throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     /*this is mostly just a helper to bypass a groovy bug in a more
    * complex delegation environemt. Groovy throws a classcast
    * exeption wrongly, delegating the instantiation code to java
    * fixes that
    * */
     public static Object newObject(Class clazz) throws IllegalAccessException, InstantiationException {
         return clazz.newInstance();
     }
 
     /**
      * executes a method
      *
      * @param obj        the target object
      * @param methodName the method name
      * @param varargs    a list of objects casts or nulls defining the parameter classes and its values
      *                   if something occurs on introspection level an unmanaged exception is throw, just like
      *                   it would happen in a scripting class
      */
     public static void executeMethod(Object obj, String methodName, Object... varargs) {
 
         Class[] classes = new Class[varargs.length];
         for (int cnt = 0; cnt < varargs.length; cnt++) {
 
             if (varargs[cnt] instanceof Cast) {
                 classes[cnt] = ((Cast) varargs[cnt]).getClazz();
                 varargs[cnt] = ((Cast) varargs[cnt]).getValue();
             } else {
                 classes[cnt] = varargs[cnt].getClass();
             }
         }
 
         try {
             Method m = getMethod(obj, methodName, classes);
             m.invoke(obj, varargs);
         } catch (NoSuchMethodException e) {
             throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * executes a function method on a target object
      *
      * @param obj        the target object
      * @param methodName the method name
      * @param varargs    a list of objects casts or nulls defining the parameter classes and its values
      *                   if something occurs on introspection level an unmanaged exception is throw, just like
      *                   it would happen in a scripting class
      * @return the result object for the function(method) call
      * @throws RuntimeException an unmanaged runtime exception in case of an introspection error
      */
     public static Object executeFunction(Object obj, String methodName, Object... varargs) {
         Class[] classes = new Class[varargs.length];
         for (int cnt = 0; cnt < varargs.length; cnt++) {
 
             if (varargs[cnt] instanceof Cast) {
                 classes[cnt] = ((Cast) varargs[cnt]).getClazz();
                 varargs[cnt] = ((Cast) varargs[cnt]).getValue();
             } else {
                 classes[cnt] = varargs[cnt].getClass();
             }
         }
 
         try {
             Method m = getMethod(obj, methodName, classes);
             return m.invoke(obj, varargs);
         } catch (NoSuchMethodException e) {
             throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     private static Method getMethod(Object obj, String methodName, Class[] classes) throws NoSuchMethodException {
         Method m = null;
         try {
             m = obj.getClass().getDeclaredMethod(methodName, classes);
         } catch (NoSuchMethodException e) {
             m = obj.getClass().getMethod(methodName, classes);
         }
         return m;
     }
 
 
     /**
      * executes a function method on a target object
      *
      * @param obj        the target object
      * @param methodName the method name
      * @param varargs    a list of objects casts or nulls defining the parameter classes and its values
      *                   if something occurs on introspection level an unmanaged exception is throw, just like
      *                   it would happen in a scripting class
      * @return the result object for the function(method) call
      * @throws RuntimeException an unmanaged runtime exception in case of an introspection error
      */
     public static Object executeStaticFunction(Class obj, String methodName, Object... varargs) {
         Class[] classes = new Class[varargs.length];
         for (int cnt = 0; cnt < varargs.length; cnt++) {
 
             if (varargs[cnt] instanceof Cast) {
                 classes[cnt] = ((Cast) varargs[cnt]).getClazz();
                 varargs[cnt] = ((Cast) varargs[cnt]).getValue();
             } else {
                 classes[cnt] = varargs[cnt].getClass();
             }
         }
 
         try {
             Method m = getStaticMethod(obj, methodName, classes);
             return m.invoke(obj, varargs);
         } catch (NoSuchMethodException e) {
             throw new RuntimeException(e);
         } catch (InvocationTargetException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     private static Method getStaticMethod(Class obj, String methodName, Class[] classes) throws NoSuchMethodException {
         Method m = null;
         try {
             m = obj.getDeclaredMethod(methodName, classes);
         } catch (NoSuchMethodException e) {
             m = obj.getMethod(methodName, classes);
         }
         return m;
     }
 
     /**
      * convenience method which makes the code a little bit more readable
      * use it in conjunction with static imports
      *
      * @param clazz the cast target for the method call
      * @param value the value object to be used as param
      * @return a Cast object of the parameters
      */
     public static Cast cast(Class clazz, Object value) {
         return new Cast(clazz, value);
     }
 
     /**
      * convenience method which makes the code a little bit more readable
      * use it in conjunction with static imports
      *
      * @param clazz the cast target for the method call
      * @return a null value Cast object of the parameters
      */
     public static Null nullCast(Class clazz) {
         return new Null(clazz);
     }
 
 
     /**
      * we use the BCEL here to add a marker interface dynamically on the compiled java class
      * so that later we can identify the marked class as being of dynamic origin
      * that way we dont have to hammer any data structure but can work over introspection
      * to check for an implemented marker interface
      *
      * I cannot use the planned annotation for now
      * because the BCEL has annotation support only
      * in the trunk but in no official release,
      * the annotation support will be added as soon as it is possible to use it
      *
      * @param classPath the root classPath which hosts our class
      * @param className the className from the class which has to be rewritten
      * @throws ClassNotFoundException
      */
     public static void markAsDynamicJava(String classPath, String className) throws ClassNotFoundException {
         SyntheticRepository repo = SyntheticRepository.getInstance(new ClassPath(classPath));
         JavaClass javaClass = repo.loadClass(className);
         ClassGen classGen = new ClassGen(javaClass);
         
         classGen.addInterface("org.apache.myfaces.scripting.loaders.java._ScriptingClass");
         classGen.update();
 
         File target = classNameToFile(classPath, className);
 
         try {
             classGen.getJavaClass().dump(target);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
   
 
     public static File classNameToFile(String classPath, String className) {
         String classFileName = classNameToRelativeFileName(className);
         File target = new File(classPath + File.separator + classFileName);
         return target;
     }
 
     private static String classNameToRelativeFileName(String className) {
         return className.replaceAll("\\.", File.separator) + ".class";
     }
 
     public static String relativeFileToClassName(String relativeFileName) {
         String className = relativeFileName.replaceAll("\\\\", ".").replaceAll("\\/", ".");
         className = className.substring(0, className.lastIndexOf("."));
         return className;
     }
 }
