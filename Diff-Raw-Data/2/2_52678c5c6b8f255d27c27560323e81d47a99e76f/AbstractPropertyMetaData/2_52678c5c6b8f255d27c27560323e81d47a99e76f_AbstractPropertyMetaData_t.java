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
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.xml.bind.annotation.XmlType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlAnyElement;
 import javax.xml.bind.annotation.XmlValue;
 
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.beans.metadata.spi.MetaDataVisitor;
 import org.jboss.beans.metadata.spi.MetaDataVisitorNode;
 import org.jboss.beans.metadata.spi.PropertyMetaData;
 import org.jboss.beans.metadata.spi.ValueMetaData;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.kernel.spi.config.KernelConfigurator;
 import org.jboss.kernel.spi.dependency.KernelControllerContext;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossStringBuilder;
 import org.jboss.managed.api.annotation.ManagementProperty;
 
 /**
  * Metadata for a property.
  * 
  * @author <a href="ales.justin@jboss.com">Ales Justin</a>
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 @XmlType(name="propertyType", propOrder={"annotations", "value"})
 public class AbstractPropertyMetaData extends AbstractFeatureMetaData
    implements PropertyMetaData, ValueMetaDataAware, TypeProvider, Serializable
 {
    private static final long serialVersionUID = 3L;
 
    /** The property name */
    protected String name;
 
    /** The preinstantiate */
    protected boolean preInstantiate = true;
 
    /** The property value */
    protected ValueMetaData value;
 
    /**
     * Create a new property meta data
     */
    public AbstractPropertyMetaData()
    {
    }
 
    /**
     * Create a new property meta data
     * 
     * @param name the name
     * @param value the value
     */
    public AbstractPropertyMetaData(String name, Object value)
    {
       this.name = name;
       this.value = new AbstractValueMetaData(value);
    }
 
    /**
     * Create a new property meta data
     * 
     * @param name the name
     * @param value the string value
     */
    public AbstractPropertyMetaData(String name, String value)
    {
       this.name = name;
       this.value = new StringValueMetaData(value);
    }
 
    /**
     * Create a new attribute meta data
     * 
     * @param name the name
     * @param value the value meta data
     */
    public AbstractPropertyMetaData(String name, ValueMetaData value)
    {
       this.name = name;
       this.value = value;
    }
 
    /**
     * Create a new property meta data
     * 
     * @param name the name
     * @param value the string value
     * @param type the type
     */
    public AbstractPropertyMetaData(String name, String value, String type)
    {
       this.name = name;
       StringValueMetaData svmd = new StringValueMetaData(value);
       svmd.setType(type);
       this.value = svmd;
    }
 
    public String getName()
    {
       return name;
    }
 
    /**
     * Set the name
     * 
     * @param name the name
     */
    @XmlAttribute
    public void setName(String name)
    {
       this.name = name;
       flushJBossObjectCache();
    }
 
    public String getType()
    {
       if (value instanceof AbstractTypeMetaData)
       {
          return ((AbstractTypeMetaData)value).getType();
       }
       return null;
    }
 
    public boolean isPreInstantiate()
    {
       return preInstantiate;
    }
 
   @XmlAttribute(name="preinstantiate")
    public void setPreInstantiate(boolean preInstantiate)
    {
       this.preInstantiate = preInstantiate;
    }
 
    public ValueMetaData getValue()
    {
       return value;
    }
 
    /**
     * Set the value
     * 
     * @param value the value
     */
    @XmlElements
    ({
       @XmlElement(name="array", type=AbstractArrayMetaData.class),
       @XmlElement(name="collection", type=AbstractCollectionMetaData.class),
       @XmlElement(name="inject", type=AbstractInjectionValueMetaData.class),
       @XmlElement(name="list", type=AbstractListMetaData.class),
       @XmlElement(name="map", type=AbstractMapMetaData.class),
       @XmlElement(name="set", type=AbstractSetMetaData.class),
       @XmlElement(name="null", type=AbstractValueMetaData.class),
       @XmlElement(name="this", type=ThisValueMetaData.class),
       @XmlElement(name="value", type=StringValueMetaData.class),
       @XmlElement(name="value-factory", type=AbstractValueFactoryMetaData.class)
    })
    public void setValue(ValueMetaData value)
    {
       this.value = value;
       flushJBossObjectCache();
    }
 
    @XmlAnyElement
    @ManagementProperty(ignored = true)
    public void setValueObject(Object value)
    {
       if (value == null)
          setValue(null);
       else if (value instanceof ValueMetaData)
          setValue((ValueMetaData) value);
       else
          setValue(new AbstractValueMetaData(value));
    }
 
    @XmlValue
    @ManagementProperty(ignored = true)
    public void setValueString(String value)
    {
       if (value == null)
          setValue(null);
       else
       {
          ValueMetaData valueMetaData = getValue();
          if (valueMetaData instanceof StringValueMetaData)
          {
             ((StringValueMetaData) valueMetaData).setValue(value);
          }
          else
          {
             StringValueMetaData stringValue = new StringValueMetaData(value);
             stringValue.setType(getType());
             setValue(stringValue);
          }
       }
    }
 
    @XmlAttribute(name="class")
    @ManagementProperty(ignored = true)
    public void setPropertyType(String type)
    {
       ValueMetaData valueMetaData = getValue();
       if (valueMetaData != null && valueMetaData instanceof StringValueMetaData == false)
          throw new IllegalArgumentException("Property is not a string");
       if (valueMetaData == null)
       {
          valueMetaData = new StringValueMetaData();
          setValue(valueMetaData);
       }
       ((StringValueMetaData) valueMetaData).setType(type);
    }
 
    @XmlAttribute(name="replace")
    @ManagementProperty(ignored = true)
    public void setPropertyReplace(boolean replace)
    {
       ValueMetaData valueMetaData = getValue();
       if (valueMetaData != null && valueMetaData instanceof StringValueMetaData == false)
          throw new IllegalArgumentException("Property is not a string");
       if (valueMetaData == null)
       {
          valueMetaData = new StringValueMetaData();
          setValue(valueMetaData);
       }
       ((StringValueMetaData) valueMetaData).setReplace(replace);
    }
 
    @XmlAttribute(name="trim")
    @ManagementProperty(ignored = true)
    public void setPropertyTrim(boolean trim)
    {
       ValueMetaData valueMetaData = getValue();
       if (valueMetaData != null && valueMetaData instanceof StringValueMetaData == false)
          throw new IllegalArgumentException("Property is not a string");
       if (valueMetaData == null)
       {
          valueMetaData = new StringValueMetaData();
          setValue(valueMetaData);
       }
       ((StringValueMetaData) valueMetaData).setTrim(trim);
    }
 
    public void initialVisit(MetaDataVisitor visitor)
    {
       visitor.setContextState(ControllerState.CONFIGURED);
       super.initialVisit(visitor);
    }
 
    public void addChildren(Set<MetaDataVisitorNode> children)
    {
       if (value != null)
          children.add(value);
    }
 
    public TypeInfo getType(MetaDataVisitor visitor, MetaDataVisitorNode previous) throws Throwable
    {
       String type = getType();
       if (type != null)
       {
          KernelControllerContext context = visitor.getControllerContext();
          ClassLoader cl = Configurator.getClassLoader(context.getBeanMetaData());
          KernelConfigurator configurator = context.getKernel().getConfigurator();
          return configurator.getClassInfo(type, cl);
       }
       // check properties
       KernelControllerContext context = visitor.getControllerContext();
       Set<PropertyInfo> propertyInfos = context.getBeanInfo().getProperties();
       if (propertyInfos != null && propertyInfos.isEmpty() == false)
       {
          for (PropertyInfo pi : propertyInfos)
          {
             if (getName().equals(pi.getName()))
             {
                return applyCollectionOrMapCheck(pi.getType());
             }
          }
       }
       throw new IllegalArgumentException("Should not be here - no matching propertyInfo: " + this);
    }
 
    public void toString(JBossStringBuilder buffer)
    {
       buffer.append("name=").append(name);
       if (value != null)
          buffer.append(" value=").append(value);
       super.toString(buffer);
    }
 
    public void toShortString(JBossStringBuilder buffer)
    {
       buffer.append(name);
    }
 }
