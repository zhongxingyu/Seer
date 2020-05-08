 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.joinpoint.plugins;
 
 import java.util.Arrays;
 
 import org.jboss.joinpoint.spi.ConstructorJoinpoint;
 import org.jboss.joinpoint.spi.FieldGetJoinpoint;
 import org.jboss.joinpoint.spi.FieldSetJoinpoint;
 import org.jboss.joinpoint.spi.JoinpointException;
 import org.jboss.joinpoint.spi.JoinpointFactory;
 import org.jboss.joinpoint.spi.MethodJoinpoint;
 import org.jboss.logging.Logger;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.FieldInfo;
 import org.jboss.reflect.spi.MethodInfo;
 import org.jboss.reflect.spi.TypeInfo;
 
 /**
  * Config utilities.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class Config
 {
    /** The log */
    protected static final Logger log = Logger.getLogger(Config.class);
 
    /** No parameter types */
    private static final String[] NO_PARAMS_TYPES = new String[0];
 
    /** No parameters */
    private static final Object[] NO_PARAMS = new Object[0];
 
    /**
     * Instantiate an object
     *
     * @param jpf the join point factory
     * @param paramTypes the parameter types
     * @param params the parameters
     * @return the instantiated object
     * @throws Throwable for any error
     */
    public static Object instantiate(JoinpointFactory jpf, String[] paramTypes, Object[] params) throws Throwable
    {
       ConstructorJoinpoint joinpoint = getConstructorJoinpoint(jpf, paramTypes, params);
       return joinpoint.dispatch();
    }
 
    /**
     * Configure a field
     *
     * @param object the object to configure
     * @param jpf the join point factory
     * @param name the name of the field
     * @param value the value
     * @throws Throwable for any error
     */
    public static void configure(Object object, JoinpointFactory jpf, String name, Object value) throws Throwable
    {
       FieldSetJoinpoint joinpoint = getFieldSetJoinpoint(object, jpf, name, value);
       joinpoint.dispatch();
    }
 
    /**
     * Unconfigure a field
     *
     * @param object the object to unconfigure
     * @param jpf the join point factory
     * @param name the name of the field
     * @throws Throwable for any error
     */
    public static void unconfigure(Object object, JoinpointFactory jpf, String name) throws Throwable
    {
       FieldSetJoinpoint joinpoint = getFieldSetJoinpoint(object, jpf, name, null);
       joinpoint.dispatch();
    }
 
    /**
     * Invoke a method
     *
     * @param object the object to invoke
     * @param jpf the join point factory
     * @param name the name of the method
     * @param paramTypes the parameter types
     * @param params the parameters
     * @return the result of the invocation
     * @throws Throwable for any error
     */
    public static Object invoke(Object object, JoinpointFactory jpf, String name, String[] paramTypes, Object[] params) throws Throwable
    {
       MethodJoinpoint joinpoint = getMethodJoinpoint(object, jpf, name, paramTypes, params);
       return joinpoint.dispatch();
    }
 
    /**
     * Get a constructor Joinpoint
     *
     * @param jpf the join point factory
     * @return the Joinpoint
     * @throws Throwable for any error
     */
    public static ConstructorJoinpoint getConstructorJoinpoint(JoinpointFactory jpf) throws Throwable
    {
       return getConstructorJoinpoint(jpf, null, null);
    }
 
    /**
     * Get a constructor Joinpoint
     *
     * @param jpf the join point factory
     * @param paramTypes the parameter types
     * @param params the parameters
     * @return the Joinpoint
     * @throws Throwable for any error
     */
    public static ConstructorJoinpoint getConstructorJoinpoint(JoinpointFactory jpf, String[] paramTypes, Object[] params) throws Throwable
    {
       if (paramTypes == null)
          paramTypes = NO_PARAMS_TYPES;
 
       if (params == null)
          params = NO_PARAMS;
 
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace("Get constructor Joinpoint jpf=" + jpf + " paramTypes=" + Arrays.asList(paramTypes) + " params=" + Arrays.asList(params));
 
       ConstructorInfo constructorInfo = findConstructorInfo(jpf.getClassInfo(), paramTypes);
       ConstructorJoinpoint joinpoint = jpf.getConstructorJoinpoint(constructorInfo);
       joinpoint.setArguments(params);
       return joinpoint;
    }
 
    /**
     * Get a field get joinpoint
     *
     * @param object the object to configure
     * @param jpf the join point factory
     * @param name the name of the field
     * @return the Joinpoint
     * @throws Throwable for any error
     */
    public static FieldGetJoinpoint getFieldGetJoinpoint(Object object, JoinpointFactory jpf, String name) throws Throwable
    {
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace("Get field get Joinpoint jpf=" + jpf + " target=" + object + " name=" + name);
 
       FieldInfo fieldInfo = findFieldInfo(jpf.getClassInfo(), name);
       FieldGetJoinpoint joinpoint = jpf.getFieldGetJoinpoint(fieldInfo);
       joinpoint.setTarget(object);
       return joinpoint;
    }
 
    /**
     * Get a field set joinpoint
     *
     * @param object the object to configure
     * @param jpf the join point factory
     * @param name the name of the field
     * @param value the value
     * @return the Joinpoint
     * @throws Throwable for any error
     */
    public static FieldSetJoinpoint getFieldSetJoinpoint(Object object, JoinpointFactory jpf, String name, Object value) throws Throwable
    {
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace("Get field set Joinpoint jpf=" + jpf + " target=" + object + " name=" + name + " value=" + value);
 
       FieldInfo fieldInfo = findFieldInfo(jpf.getClassInfo(), name);
       FieldSetJoinpoint joinpoint = jpf.getFieldSetJoinpoint(fieldInfo);
       joinpoint.setTarget(object);
       joinpoint.setValue(value);
       return joinpoint;
    }
 
    /**
     * Get a method joinpoint
     *
     * @param object the object to invoke
     * @param jpf the join point factory
     * @param name the name of the method
     * @param paramTypes the parameter types
     * @param params the parameters
     * @return the join point
     * @throws Throwable for any error
     */
    public static MethodJoinpoint getMethodJoinpoint(Object object, JoinpointFactory jpf, String name, String[] paramTypes, Object[] params) throws Throwable
    {
       boolean trace = log.isTraceEnabled();
       if (trace)
       {
          if (paramTypes != null)
             log.trace("Get method Joinpoint jpf=" + jpf + " target=" + object + " name=" + name + " paramTypes=" + Arrays.asList(paramTypes));
          else
             log.trace("Get method Joinpoint jpf=" + jpf + " target=" + object + " name=" + name + " paramTypes=()");
       }
 
       MethodInfo methodInfo = findMethodInfo(jpf.getClassInfo(), name, paramTypes);
       MethodJoinpoint joinpoint = jpf.getMethodJoinpoint(methodInfo);
       joinpoint.setTarget(object);
       joinpoint.setArguments(params);
       return joinpoint;
    }
 
    /**
     * Get a static method joinpoint
     *
     * @param jpf the join point factory
     * @param name the name of the method
     * @param paramTypes the parameter types
     * @param params the parameters
     * @return the join point
     * @throws Throwable for any error
     */
    public static MethodJoinpoint getStaticMethodJoinpoint(JoinpointFactory jpf, String name, String[] paramTypes, Object[] params) throws Throwable
    {
       boolean trace = log.isTraceEnabled();
       if (trace)
       {
          if (paramTypes != null)
             log.trace("Get method Joinpoint jpf=" + jpf + " name=" + name + " paramTypes=" + Arrays.asList(paramTypes));
          else
             log.trace("Get method Joinpoint jpf=" + jpf + " name=" + name + " paramTypes=()");
       }
 
       MethodInfo methodInfo = findMethodInfo(jpf.getClassInfo(), name, paramTypes, true, true);
       MethodJoinpoint joinpoint = jpf.getMethodJoinpoint(methodInfo);
       joinpoint.setArguments(params);
       return joinpoint;
    }
 
    /**
     * Find constructor info
     *
     * @param classInfo the class info
     * @param paramTypes the parameter types
     * @return the constructor info
     * @throws JoinpointException when no such constructor
     */
    public static ConstructorInfo findConstructorInfo(ClassInfo classInfo, String[] paramTypes) throws JoinpointException
    {
       ConstructorInfo[] constructors = classInfo.getDeclaredConstructors();
       if (constructors != null)
       {
          for (int i = 0; i < constructors.length; ++i)
          {
             if (equals(paramTypes, constructors[i].getParameterTypes()))
                return constructors[i];
          }
          throw new JoinpointException("Constructor not found " + classInfo.getName() + Arrays.asList(paramTypes) + " in " + Arrays.asList(constructors));
       }
       throw new JoinpointException("Constructor not found " + classInfo.getName() + Arrays.asList(paramTypes) + " no constructors");
    }
 
    /**
     * Find field info
     *
     * @param classInfo the class info
     * @param name the field name
     * @return the field info
     * @throws JoinpointException when no such field
     */
    public static FieldInfo findFieldInfo(ClassInfo classInfo, String name) throws JoinpointException
    {
       if (classInfo == null)
          throw new IllegalArgumentException("ClassInfo cannot be null!");
       ClassInfo current = classInfo;
       while (current != null)
       {
          FieldInfo result = locateFieldInfo(current, name);
          if (result != null)
             return result;
          current = current.getSuperclass();
       }
       throw new JoinpointException("Field not found '" + name + "' for class " + classInfo.getName());
    }
 
    /**
     * Find field info
     *
     * @param classInfo the class info
     * @param name the field name
     * @return the field info or null if not found
     */
    private static FieldInfo locateFieldInfo(ClassInfo classInfo, String name)
    {
       FieldInfo[] fields = classInfo.getDeclaredFields();
       if (fields != null)
       {
          for (int i = 0; i < fields.length; ++i)
          {
             if (name.equals(fields[i].getName()))
                return fields[i];
          }
       }
       return null;
    }
 
    /**
     * Find method info
     *
     * @param classInfo the class info
     * @param name the method name
     * @param paramTypes the parameter types
     * @return the method info
     * @throws JoinpointException when no such method
     */
    public static MethodInfo findMethodInfo(ClassInfo classInfo, String name, String[] paramTypes) throws JoinpointException
    {
       return findMethodInfo(classInfo, name, paramTypes, false, true);
    }
 
    /**
     * Find method info
     *
     * @param classInfo the class info
     * @param name the method name
     * @param paramTypes the parameter types
     * @param strict is strict about method modifiers
     * @return the method info
     * @throws JoinpointException when no such method
     */
    public static MethodInfo findMethodInfo(ClassInfo classInfo, String name, String[] paramTypes, boolean strict) throws JoinpointException
    {
       return findMethodInfo(classInfo, name, paramTypes, false, true, strict);
    }
 
    /**
     * Find method info
     *
     * @param classInfo the class info
     * @param name the method name
     * @param paramTypes the parameter types
     * @param isStatic must the method be static
     * @param isPublic must the method be public
     * @return the method info
     * @throws JoinpointException when no such method
     */
    public static MethodInfo findMethodInfo(ClassInfo classInfo, String name, String[] paramTypes, boolean isStatic, boolean isPublic) throws JoinpointException
    {
       return findMethodInfo(classInfo, name, paramTypes, isStatic, isPublic, true);
    }
 
    /**
     * Find method info
     *
     * @param classInfo the class info
     * @param name the method name
     * @param paramTypes the parameter types
     * @param isStatic must the method be static
     * @param isPublic must the method be public
     * @param strict is strict about method modifiers
     * @return the method info
     * @throws JoinpointException when no such method
     */
    public static MethodInfo findMethodInfo(ClassInfo classInfo, String name, String[] paramTypes, boolean isStatic, boolean isPublic, boolean strict) throws JoinpointException
    {
       if (classInfo == null)
          throw new IllegalArgumentException("ClassInfo cannot be null!");
 
       if (paramTypes == null)
          paramTypes = NO_PARAMS_TYPES;
 
       ClassInfo current = classInfo;
       while (current != null)
       {
          MethodInfo result = locateMethodInfo(current, name, paramTypes, isStatic, isPublic, strict);
          if (result != null)
             return result;
          current = current.getSuperclass();
       }
       throw new JoinpointException("Method not found " + name + Arrays.asList(paramTypes) + " for class " + classInfo.getName());
    }
 
    /**
     * Find method info
     *
     * @param classInfo the class info
     * @param name the method name
     * @param paramTypes the parameter types
     * @param isStatic must the method be static
     * @param isPublic must the method be public
     * @param strict is strict about method modifiers
     * @return the method info or null if not found
     */
    private static MethodInfo locateMethodInfo(ClassInfo classInfo, String name, String[] paramTypes, boolean isStatic, boolean isPublic, boolean strict)
    {
       MethodInfo[] methods = classInfo.getDeclaredMethods();
       if (methods != null)
       {
          for (int i = 0; i < methods.length; ++i)
          {
             if (name.equals(methods[i].getName()) &&
                 equals(paramTypes, methods[i].getParameterTypes()) &&
                 (strict == false || (methods[i].isStatic() == isStatic && methods[i].isPublic() == isPublic)))
                return methods[i];
          }
       }
       return null;
    }
 
    /**
     * Test whether type names are equal to type infos
     *
     * @param typeNames the type names
     * @param typeInfos the type infos
     * @return true when they are equal
     */
    public static boolean equals(String[] typeNames, TypeInfo[] typeInfos)
    {
       if (simpleCheck(typeNames, typeInfos) == false)
          return false;
 
       for (int i = 0; i < typeNames.length; ++i)
       {
          if (typeNames[i] != null && typeNames[i].equals(typeInfos[i].getName()) == false)
             return false;
       }
       return true;
    }
 
    /**
     * A simple null and length check.
     *
     * @param typeNames
     * @param typeInfos
     * @return false if either argument is null or lengths differ, else true
     */
    protected static boolean simpleCheck(String[] typeNames, TypeInfo[] typeInfos)
    {
       if (typeNames == null || typeInfos == null)
       {
          return false;
       }
       return typeNames.length == typeInfos.length;
    }
 
 }
