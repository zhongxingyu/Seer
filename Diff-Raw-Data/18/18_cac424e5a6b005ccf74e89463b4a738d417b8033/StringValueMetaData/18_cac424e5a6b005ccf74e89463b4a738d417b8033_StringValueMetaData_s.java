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
 
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.bind.annotation.XmlValue;
 
 import org.jboss.beans.metadata.spi.MetaDataVisitor;
 import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
 import org.jboss.logging.Logger;
 import org.jboss.reflect.spi.TypeInfo;
 
 /**
  * String value.
  * 
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 @XmlType(name="plainValueType")
 public class StringValueMetaData extends AbstractTypeMetaData
 {
    private static final long serialVersionUID = 2L;
 
    /** The log */
    private static final Logger log = Logger.getLogger(StringValueMetaData.class);
 
    /**
     * Do we replace String with System property,
     * by default it is true.
     */
    private boolean replace = true;
 
    /**
     * Do we trim string value before usage,
     * by default is true.
     */
    private boolean trim = true;
 
    /**
     * Create a new string value
     */
    public StringValueMetaData()
    {
    }
 
    /**
     * Create a new string value
     * 
     * @param value the value
     */
    public StringValueMetaData(String value)
    {
       super(value);
    }
 
    @Override
    public String getValue()
    {
       return (String) super.getValue();
    }
    
    /**
     * Set the value
     * 
     * @param value the value
     */
    @XmlValue
    public void setValue(String value)
    {
       super.setValue(value);
    }
 
    public void setValue(Object value)
    {
       if (value != null && value instanceof String == false)
          throw new ClassCastException("value is not a String: " + value);
       super.setValue(value);
    }
 
    public Object getValue(TypeInfo info, ClassLoader cl) throws Throwable
    {
       boolean trace = log.isTraceEnabled();
       if (trace)
          log.trace("getValue value=" + getUnderlyingValue() + " type=" + type + " info=" + info);
 
       TypeInfo typeInfo = getClassInfo(cl);
       if (typeInfo == null)
          typeInfo = info;
       if (typeInfo == null)
          throw new IllegalArgumentException("Unable to determine type for value: " + getUnderlyingValue());
 
       // we convert it with more precise type
       // and then check for progression, ...
       if (typeInfo != info && info != null)
       {
          Object typeValue = typeInfo.convertValue(getUnderlyingValue());
          return info.convertValue(typeValue, replace, trim);
       }
       return typeInfo.convertValue(getUnderlyingValue(), replace, trim);
    }
 
    protected Object getDefaultInstance()
    {
       return null;
    }
 
    public TypeInfo getType(MetaDataVisitor visitor, MetaDataVisitorNode previous) throws Throwable
    {
       if (getType() != null)
       {
          return getClass(visitor, getType());
       }
       return super.getType(visitor, previous);
    }
 
    public boolean isReplace()
    {
       return replace;
    }
 
    public void setReplace(boolean replace)
    {
       this.replace = replace;
    }
 
    public boolean isTrim()
    {
       return trim;
    }
 
    public void setTrim(boolean trim)
    {
       this.trim = trim;
    }
 }
