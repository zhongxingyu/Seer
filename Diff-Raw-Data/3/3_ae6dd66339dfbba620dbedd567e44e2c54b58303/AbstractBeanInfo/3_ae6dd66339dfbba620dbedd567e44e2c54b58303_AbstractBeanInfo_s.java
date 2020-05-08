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
 package org.jboss.beans.info.plugins;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.BeanInfoFactory;
 import org.jboss.beans.info.spi.EventInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.classadapter.spi.ClassAdapter;
 import org.jboss.joinpoint.plugins.Config;
 import org.jboss.joinpoint.spi.ConstructorJoinpoint;
 import org.jboss.joinpoint.spi.JoinpointFactory;
 import org.jboss.joinpoint.spi.MethodJoinpoint;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.ConstructorInfo;
 import org.jboss.reflect.spi.MethodInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossObject;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * BeanInfo.
  * 
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractBeanInfo extends JBossObject implements BeanInfo
 {
    /** The class name */
    private String name;
 
    /** The class adapter */
    protected ClassAdapter classAdapter;
 
    /** The properties */
    private Set<PropertyInfo> properties;
 
    /** The properties by name */
    private transient Map<String, PropertyInfo> propertiesByName = Collections.emptyMap();
 
    /** The constructors */
    private Set<ConstructorInfo> constructors;
 
    /** The methods */
    private Set<MethodInfo> methods;
 
    /** The events */
    private Set<EventInfo> events;
 
    /** The BeanInfoFactory */
    private BeanInfoFactory beanInfoFactory;
 
    /**
     * Create a new bean info
     *
     * @param beanInfoFactory the bean info factory
     * @param classAdapter the class adapter
     * @param properties the properties
     * @param constructors the constructors
     * @param methods the methods
     * @param events the events
     */
    public AbstractBeanInfo(
          BeanInfoFactory beanInfoFactory,
          ClassAdapter classAdapter,
          Set<PropertyInfo> properties,
          Set<ConstructorInfo> constructors,
          Set<MethodInfo> methods,
          Set<EventInfo> events)
    {
       this.beanInfoFactory = beanInfoFactory;
       if (classAdapter == null)
          throw new IllegalArgumentException("Null class adapter.");
       this.name = classAdapter.getClassInfo().getName();
       this.classAdapter = classAdapter;
       setProperties(properties);
       this.constructors = constructors;
       this.methods = methods;
       this.events = events;
    }
 
    public String getName()
    {
       return name;
    }
 
    public Set<PropertyInfo> getProperties()
    {
       return properties;
    }
 
    public void setProperties(Set<PropertyInfo> properties)
    {
       if (properties != null && properties.isEmpty() == false)
       {
          this.properties = new HashSet<PropertyInfo>(properties.size());
          this.propertiesByName = new HashMap<String, PropertyInfo>(properties.size());
 
          for (PropertyInfo property : properties)
          {
             replaceAndAddProperty(property);
          }
       }
    }
 
    /**
     * Get a property
     *
     * @param name the property name
     * @return the property
     * @throws IllegalArgumentException if there is no such property
     */
    public PropertyInfo getProperty(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
 
       PropertyInfo property = findPropertyInfo(name);
       if (property == null)
          throw new IllegalArgumentException("No such property " + name + " for bean " + getName() + " available " + propertiesByName.keySet());
       return property;
    }
 
    /**
     * Find property
     *
     * @param name the property name
     * @return the property or null if no such property
     */
    protected PropertyInfo findPropertyInfo(String name)
    {
       return propertiesByName.get(name);
    }
 
    /**
     * Replace and add property.
     *
     * @param property the property to add
    * @return previous property or null if it doesn't exist
     */
    protected void replaceAndAddProperty(PropertyInfo property)
    {
       property = replaceProperty(property);
       addProperty(property);
    }
 
    /**
     * Add property.
     *
     * @param property the property to add
    * @return previous property or null if it doesn't exist
     */
    protected void addProperty(PropertyInfo property)
    {
       properties.add(property);
       PropertyInfo previous = propertiesByName.put(property.getName(), property);
       if (previous != null)
       {
          NestedPropertyInfo nestedPropertyInfo;
          if (previous instanceof NestedPropertyInfo)
          {
             nestedPropertyInfo = (NestedPropertyInfo)previous;
          }
          else
          {
             nestedPropertyInfo = new NestedPropertyInfo(previous.getName(), this);
             nestedPropertyInfo.addPropertyInfo(previous);
             propertiesByName.put(previous.getName(), nestedPropertyInfo);
          }
          nestedPropertyInfo.addPropertyInfo(property);
       }
       if (property instanceof AbstractPropertyInfo)
       {
          AbstractPropertyInfo ainfo = (AbstractPropertyInfo) property;
          ainfo.setBeanInfo(this);
       }
    }
 
    /**
     * Do we need to replace property due to access mode.
     * By default we don't do anything, returning original.
     *
     * @param original the original property
     * @return replaced property or original if no replacement neccessary
     */
    protected PropertyInfo replaceProperty(PropertyInfo original)
    {
       return original;
    }
 
    public ClassInfo getClassInfo()
    {
       return classAdapter.getClassInfo();
    }
 
    public JoinpointFactory getJoinpointFactory()
    {
       return classAdapter.getJoinpointFactory();
    }
 
    public Set<ConstructorInfo> getConstructors()
    {
       return constructors;
    }
 
    public void setConstructors(Set<ConstructorInfo> constructors)
    {
       this.constructors = constructors;
    }
 
    public Set<EventInfo> getEvents()
    {
       return events;
    }
 
    public void setEvents(Set<EventInfo> events)
    {
       this.events = events;
    }
 
    public Set<MethodInfo> getMethods()
    {
       return methods;
    }
 
    public void setMethods(Set<MethodInfo> methods)
    {
       this.methods = methods;
    }
 
    public BeanInfoFactory getBeanInfoFactory()
    {
       return beanInfoFactory;
    }
 
    public Object newInstance() throws Throwable
    {
       return newInstance((String[]) null, null);
    }
 
    public Object newInstance(String[] paramTypes, Object[] params) throws Throwable
    {
       ConstructorJoinpoint joinpoint = Config.getConstructorJoinpoint(getJoinpointFactory(), paramTypes, params);
       return joinpoint.dispatch();
    }
 
    public Object newInstance(Class<?>[] paramTypes, Object[] params) throws Throwable
    {
       return newInstance(classesToStrings(paramTypes), params);
    }
 
    public Object newInstance(TypeInfo[] paramTypes, Object[] params) throws Throwable
    {
       return newInstance(typeInfosToStrings(paramTypes), params);
    }
 
    public Object getProperty(Object bean, String name) throws Throwable
    {
       return BeanInfoUtil.get(this, bean, name);
    }
 
    public void setProperty(Object bean, String name, Object value) throws Throwable
    {
       BeanInfoUtil.set(this, bean, name, value);
    }
 
    public Object invoke(Object bean, String name) throws Throwable
    {
       return invoke(bean, name, (String[]) null, null);
    }
 
    public Object invoke(Object bean, String name, String[] paramTypes, Object[] params) throws Throwable
    {
       MethodJoinpoint joinpoint = Config.getMethodJoinpoint(bean, getJoinpointFactory(), name, paramTypes, params);
       return joinpoint.dispatch();
    }
 
    public Object invoke(Object bean, String name, Class<?>[] paramTypes, Object[] params) throws Throwable
    {
       return invoke(bean, name, classesToStrings(paramTypes), params);
    }
 
    public Object invoke(Object bean, String name, TypeInfo[] paramTypes, Object[] params) throws Throwable
    {
       return invoke(bean, name, typeInfosToStrings(paramTypes), params);
    }
 
    public Object invokeStatic(String name) throws Throwable
    {
       return invokeStatic(name, (String[]) null, null);
    }
 
    public Object invokeStatic(String name, String[] paramTypes, Object[] params) throws Throwable
    {
       MethodJoinpoint joinpoint = Config.getStaticMethodJoinpoint(getJoinpointFactory(), name, paramTypes, params);
       return joinpoint.dispatch();
    }
 
    public Object invokeStatic(String name, Class<?>[] paramTypes, Object[] params) throws Throwable
    {
       return invokeStatic(name, classesToStrings(paramTypes), params);
    }
 
    public Object invokeStatic(String name, TypeInfo[] paramTypes, Object[] params) throws Throwable
    {
       return invokeStatic(name, typeInfosToStrings(paramTypes), params);
    }
 
    public boolean equals(Object object)
    {
       if (object == null || object instanceof AbstractBeanInfo == false)
          return false;
       
       AbstractBeanInfo other = (AbstractBeanInfo) object;
       if (notEqual(name, other.name))
          return false;
       else if (notEqual(classAdapter, other.classAdapter))
          return false;
       else if (notEqual(properties, other.properties))
          return false;
       else if (notEqual(methods, other.methods))
          return false;
       else if (notEqual(constructors, other.constructors))
          return false;
       else if (notEqual(events, other.events))
          return false;
       return true;
    }
    
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       buffer.append(" classInfo=");
       classAdapter.toShortString(buffer);
       buffer.append(" properties=");
       list(buffer, properties);
       buffer.append(" methods=");
       list(buffer, methods);
       buffer.append(" constructors=");
       list(buffer, constructors);
       buffer.append(" events=");
       list(buffer, events);
    }
    
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(name);
    }
    
    public int getHashCode()
    {
       return name.hashCode();
    }
    
    /**
     * Convert classes to strings
     * 
     * @param classes the classes
     * @return the strings
     */
    private static String[] classesToStrings(Class<?>[] classes)
    {
       if (classes == null || classes.length == 0)
          return null;
       
       String[] result = new String[classes.length];
       for (int i = 0; i < classes.length; ++i)
       {
          if (classes[i] == null)
             throw new IllegalArgumentException("Null class in parameter types: " + Arrays.asList(classes));
          result[i] = classes[i].getName();
       }
       return result;
    }
    
    /**
     * Convert typeInfos to strings
     * 
     * @param typeInfos the typeInfos
     * @return the strings
     */
    private static String[] typeInfosToStrings(TypeInfo[] typeInfos)
    {
       if (typeInfos == null || typeInfos.length == 0)
          return null;
       
       String[] result = new String[typeInfos.length];
       for (int i = 0; i < typeInfos.length; ++i)
       {
          if (typeInfos[i] == null)
             throw new IllegalArgumentException("Null class in parameter types: " + Arrays.asList(typeInfos));
          result[i] = typeInfos[i].getName();
       }
       return result;
    }
 }
