 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, JBoss Inc., and individual contributors as indicated
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
 package org.jboss.util.collection.temp;
 
 import java.lang.reflect.GenericArrayType;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 /**
  * A weak class cache that instantiates does not a hold a
  * strong reference to either the classloader or class.<p>
  * 
  * It creates the class specific data in two stages
  * to avoid recursion.<p>
  * 
  * instantiate - creates the data<br>
  * generate - fills in the details
  *
  * @param <T> the cached type
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  */
 public abstract class WeakTypeCache<T>
 {
    /** The cache */
    private Map<ClassLoader, Map<String, T>> cache = new WeakHashMap<ClassLoader, Map<String, T>>(); 
    
    /**
     * Get the information for a type
     * 
     * @param type the type
     * @return the info
     */
    public T get(Type type)
    {
       if (type == null)
          throw new IllegalArgumentException("Null type");
 
       if (type instanceof ParameterizedType)
          return getParameterizedType((ParameterizedType) type);
       else if (type instanceof Class)
          return getClass((Class<?>) type);
       else if (type instanceof TypeVariable)
          return getTypeVariable((TypeVariable) type);
       else if (type instanceof GenericArrayType)
          return getGenericArrayType((GenericArrayType) type);
       else
          throw new UnsupportedOperationException("Unknown type: " + type + " class=" + type.getClass());
    }
    
    /**
     * Get the information for a class
     * 
     * @param name the name
     * @param cl the classloader
     * @return the info
     * @throws ClassNotFoundException when the class cannot be found
     */
    public T get(String name, ClassLoader cl) throws ClassNotFoundException
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
       if (cl == null)
          throw new IllegalArgumentException("Null classloader");
 
       Class<?> clazz = cl.loadClass(name);
       return get(clazz);
    }
    
    /**
     * Instantiate for a class
     * 
     * @param clazz the class
     * @return the result
     */
    protected abstract T instantiate(Class<?> clazz);
    
    /**
     * Fill in the result
     * 
     * @param clazz the class
     * @param result the result
     */
    protected abstract void generate(Class<?> clazz, T result);
    
    /**
     * Instantiate for a parameterized type
     * 
     * @param type the parameterized type
     * @return the result
     */
    protected abstract T instantiate(ParameterizedType type);
    
    /**
     * Fill in the result
     * 
     * @param type the parameterized type
     * @param result the result
     */
    protected abstract void generate(ParameterizedType type, T result);
 
    /**
     * Get the information for a parameterized type
     * 
     * @param type the paremeterized type
     * @return the info
     */
    private T getParameterizedType(ParameterizedType type)
    {
       // First check if we already have it
       T result = peek(type);
       if (result != null)
          return result;
       
       // Instantiate
       result = instantiate(type);
 
       // Put the perlimanary result into the cache
       put(type, result);
 
       // Generate the details
       generate(type, result);
       
       return result;
    }
 
    /**
     * Get the information for a type variable
     * 
     * @param type the type variable
     * @return the info
     */
    private T getTypeVariable(TypeVariable type)
    {
       // TODO JBMICROCONT-131 improve this
       return get(type.getBounds()[0]);
    }
 
    /**
     * Get the information for an array type
     * 
     * @param type the array type
     * @return the info
     */
    private T getGenericArrayType(GenericArrayType type)
    {
       // TODO JBMICROCONT-131 this needs implementing properly
       return get(Object[].class);
    }
 
    /**
     * Peek into the cache
     * 
     * @param type the type
     * @return the value
     */
    private T peek(ParameterizedType type)
    {
       Class<?> rawType = (Class<?>) type.getRawType();
       Map<String, T> classLoaderCache = getClassLoaderCache(rawType.getClassLoader());
       
       synchronized (classLoaderCache)
       {
          return classLoaderCache.get(type.toString());
       }
    }
 
    /**
     * Put a result into the cache
     * 
     * @param type the type
     * @param result the value
     */
    private void put(ParameterizedType type, T result)
    {
       Class<?> rawType = (Class<?>) type.getRawType();
       Map<String, T> classLoaderCache = getClassLoaderCache(rawType.getClassLoader());
 
       synchronized (classLoaderCache)
       {
          // TODO JBMICROCONT-131 something better than toString()?
          classLoaderCache.put(type.toString(), result);
       }
    }
 
    /**
     * Get the information for a class
     * 
     * @param clazz the class
     * @return the info
     */
    private T getClass(Class<?> clazz)
    {
       // First check if we already have it
       T result = peek(clazz);
       if (result != null)
          return result;
 
       // Instantiate
       result = instantiate(clazz);
 
       // Put the perlimanary result into the cache
       put(clazz, result);
 
       // Generate the details
       generate(clazz, result);
       
       return result;
    }
 
    /**
     * Peek into the cache
     * 
     * @param clazz the class
     * @return the value
     */
    private T peek(Class<?> clazz)
    {
       Map<String, T> classLoaderCache = getClassLoaderCache(clazz.getClassLoader());
 
       synchronized (classLoaderCache)
       {
          return classLoaderCache.get(clazz.getName());
       }
    }
 
    /**
     * Put a result into the cache
     * 
     * @param clazz the class
     * @param result the value
     */
    private void put(Class<?> clazz, T result)
    {
       Map<String, T> classLoaderCache = getClassLoaderCache(clazz.getClassLoader());
 
       synchronized (classLoaderCache)
       {
          classLoaderCache.put(clazz.getName(), result);
       }
    }
    
    /**
     * Get the cache for the classloader
     * 
     * @param cl the classloader
     * @return the map
     */
    private Map<String, T> getClassLoaderCache(ClassLoader cl)
    {
       synchronized (cache)
       {
          Map<String, T> result = cache.get(cl);
          if (result == null)
          {
             result = new WeakValueHashMap<String, T>();
             cache.put(cl, result);
          }
          return result;
       }
    }
 }
