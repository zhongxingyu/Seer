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
 package org.jboss.beans.metadata.plugins;
 
 import java.util.*;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.metadata.spi.MetaDataVisitor;
 import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
 import org.jboss.beans.metadata.spi.ValueMetaData;
 import org.jboss.joinpoint.spi.Joinpoint;
 import org.jboss.reflect.spi.ClassInfo;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossStringBuilder;
 
 /**
  * Map metadata.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 public class AbstractMapMetaData extends AbstractTypeMetaData implements Map<MetaDataVisitorNode, MetaDataVisitorNode>
 {
    /** The map */
    private HashMap<MetaDataVisitorNode, MetaDataVisitorNode> map = new HashMap<MetaDataVisitorNode, MetaDataVisitorNode>();
 
    /** The key type */
    protected String keyType;
 
    /** The value type */
    protected String valueType;
 
    /**
     * Create a new map value
     */
    public AbstractMapMetaData()
    {
    }
 
    /**
     * Get the key type
     * 
     * @return the key type
     */
    public String getKeyType()
    {
       return keyType;
    }
 
    /**
     * Set the key type
     * 
     * @param keyType the key type
     */
    public void setKeyType(String keyType)
    {
       this.keyType = keyType;
    }
 
    /**
     * Get the value type
     * 
     * @return the value type
     */
    public String getValueType()
    {
       return valueType;
    }
 
    /**
     * Set the value type
     * 
     * @param valueType the value type
     */
    public void setValueType(String valueType)
    {
       this.valueType = valueType;
    }
 
    public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable
    {
       Map<Object, Object> result = getMapInstance(info, cl, Map.class);
       if (result == null)
          result = getDefaultMapInstance();
 
       TypeInfo keyTypeInfo = getKeyClassInfo(cl);
       TypeInfo valueTypeInfo = getValueClassInfo(cl);
 
       if (map.size() > 0)
       {
          for (Iterator i = map.entrySet().iterator(); i.hasNext();)
          {
             Map.Entry entry = (Map.Entry) i.next();
             ValueMetaData key = (ValueMetaData) entry.getKey();
             ValueMetaData value = (ValueMetaData) entry.getValue();
             Object keyValue = key.getValue(keyTypeInfo, cl);
             Object valueValue = value.getValue(valueTypeInfo, cl);
             result.put(keyValue, valueValue);
          }
       }
       return result;
    }
 
    public void clear()
    {
       map.clear();
    }
 
    public boolean containsKey(Object key)
    {
       return map.containsKey(key);
    }
 
    public boolean containsValue(Object value)
    {
       return map.containsValue(value);
    }
 
    public Set<Entry<MetaDataVisitorNode, MetaDataVisitorNode>> entrySet()
    {
       return map.entrySet();
    }
 
    public MetaDataVisitorNode get(Object key)
    {
       return map.get(key);
    }
 
    public boolean isEmpty()
    {
       return map.isEmpty();
    }
 
    public Set<MetaDataVisitorNode> keySet()
    {
       return map.keySet();
    }
 
    public MetaDataVisitorNode put(MetaDataVisitorNode key, MetaDataVisitorNode value)
    {
       return map.put(key, value);
    }
 
    public void putAll(Map<? extends MetaDataVisitorNode, ? extends MetaDataVisitorNode> t)
    {
       putAll(t);
 
    }
 
    public MetaDataVisitorNode remove(Object key)
    {
       return map.remove(key);
    }
 
    public int size()
    {
       return map.size();
    }
 
    public Collection<MetaDataVisitorNode> values()
    {
       return map.values();
    }
 
    public Iterator<? extends MetaDataVisitorNode> getChildren()
    {
       ArrayList<MetaDataVisitorNode> children = new ArrayList<MetaDataVisitorNode>(keySet());
       children.addAll(values());
       return children.iterator();
    }
 
    public Class getType(MetaDataVisitor visitor, MetaDataVisitorNode previous) throws Throwable
    {
      // todo equality on cloned nodes
       if (keyType != null)
       {
          for(MetaDataVisitorNode key : keySet())
          {
             if (previous.equals(key))
             {
                return getClass(visitor, keyType);
             }
          }
       }
       if (valueType != null)
       {
          for(MetaDataVisitorNode v : values())
          {
             if (previous.equals(v))
             {
                return getClass(visitor, valueType);
             }
          }
       }
       return super.getType(visitor, previous);
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       super.toString(buffer);
    }
 
    /**
     * Create the default map instance
     * 
     * @return the class instance
     * @throws Throwable for any error
     */
    protected Map<Object, Object> getDefaultMapInstance() throws Throwable
    {
       return new HashMap<Object, Object>();
    }
 
    /**
     * Create the map instance
     * 
     * @param info the request type
     * @param cl the classloader
     * @param expected the expected class
     * @return the class instance
     * @throws Throwable for any error
     */
    @SuppressWarnings("unchecked")
    protected Map<Object, Object> getMapInstance(TypeInfo info, ClassLoader cl, Class<?> expected) throws Throwable
    {
       TypeInfo typeInfo = getClassInfo(cl);
 
       if (typeInfo != null && typeInfo instanceof ClassInfo == false)
          throw new IllegalArgumentException(typeInfo.getName() + " is not a class");
 
       if (typeInfo != null && ((ClassInfo) typeInfo).isInterface())
          throw new IllegalArgumentException(typeInfo.getName() + " is an interface");
 
       if (typeInfo == null)
       {
          // No type specified
          if (info == null)
             return null;
          // Not a class 
          if (info instanceof ClassInfo == false)
             return null;
          // Not an interface
          if (((ClassInfo) info).isInterface())
             return null;
          // Type is too general
          if (Object.class.getName().equals(info.getName()))
             return null;
          // Try to use the passed type
          typeInfo = info;
       }
 
       BeanInfo beanInfo = configurator.getBeanInfo(typeInfo);
       Joinpoint constructor = configurator.getConstructorJoinPoint(beanInfo);
       Object result = constructor.dispatch();
       if (expected.isAssignableFrom(result.getClass()) == false)
          throw new ClassCastException(result.getClass() + " is not a " + expected.getName());
       return (Map<Object, Object>) result;
    }
 
    /**
     * Get the class info for the key type
     * 
     * @param cl the classloader
     * @return the class info
     * @throws Throwable for any error
     */
    protected ClassInfo getKeyClassInfo(ClassLoader cl) throws Throwable
    {
       if (keyType == null)
          return null;
 
       return configurator.getClassInfo(keyType, cl);
    }
 
    /**
     * Get the class info for the value type
     * 
     * @param cl the classloader
     * @return the class info
     * @throws Throwable for any error
     */
    protected ClassInfo getValueClassInfo(ClassLoader cl) throws Throwable
    {
       if (valueType == null)
          return null;
 
       return configurator.getClassInfo(valueType, cl);
    }
 }
