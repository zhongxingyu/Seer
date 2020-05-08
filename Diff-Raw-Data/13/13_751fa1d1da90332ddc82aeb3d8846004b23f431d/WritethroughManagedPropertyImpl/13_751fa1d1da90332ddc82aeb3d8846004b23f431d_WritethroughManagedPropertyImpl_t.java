 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.managed.plugins;
 
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 
 import org.jboss.beans.info.spi.BeanInfo;
 import org.jboss.beans.info.spi.PropertyInfo;
 import org.jboss.managed.api.Fields;
 import org.jboss.managed.api.ManagedObject;
 import org.jboss.managed.api.factory.ManagedObjectFactory;
 import org.jboss.managed.plugins.factory.ManagedObjectFactoryBuilder;
 import org.jboss.managed.spi.factory.InstanceClassFactory;
 import org.jboss.metatype.api.values.MetaValue;
 import org.jboss.metatype.api.values.MetaValueFactory;
 import org.jboss.metatype.api.types.MetaType;
 import org.jboss.metatype.plugins.values.MetaValueFactoryBuilder;
 
 /**
  * An extension of ManagedPropertyImpl.
  *
  * @author Scott.Stark@jboss.org
  * @author Ales.Justin@jboss.org
  * @version $Revision$
  */
 public class WritethroughManagedPropertyImpl extends ManagedPropertyImpl
 {
    private static final long serialVersionUID = 1;
 
    /** The meta value factory */
    private transient MetaValueFactory valueFactory;
    /** The managed object factory */
    private transient ManagedObjectFactory objectFactory;
 
    public WritethroughManagedPropertyImpl(Fields fields)
    {
       super(fields);
    }
 
    public WritethroughManagedPropertyImpl(ManagedObject managedObject, Fields fields)
    {
       super(managedObject, fields);
    }
 
    public WritethroughManagedPropertyImpl(Fields fields, MetaValueFactory valueFactory, ManagedObjectFactory objectFactory)
    {
       super(fields);
       this.valueFactory = valueFactory;
       this.objectFactory = objectFactory;
    }
 
    protected ManagedObjectFactory getObjectFactory()
    {
       if (objectFactory == null)
          objectFactory = ManagedObjectFactoryBuilder.create();
       return objectFactory;
    }
 
    protected MetaValueFactory getValueFactory()
    {
       if (valueFactory == null)
          valueFactory = MetaValueFactoryBuilder.create();
       return valueFactory;
    }
 
    /**
     * Write the value back to the attachment if there is a PropertyInfo
     * in the Fields.PROPERTY_INFO field.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Serializable value)
    {
       super.setValue(value);
 
       PropertyInfo propertyInfo = getField(Fields.PROPERTY_INFO, PropertyInfo.class);
       if (propertyInfo != null)
       {
          Serializable attachment = getManagedObject().getAttachment();
          if (attachment != null)
          {
             MetaValue metaValue;
             if (value instanceof MetaValue == false)
                metaValue = getValueFactory().create(value, propertyInfo.getType());
             else
                metaValue = (MetaValue)value;
 
            boolean setValue;
            if (metaValue == null)
            {
               setValue = true;
            }
            else
            {
               MetaType metaType = metaValue.getMetaType();
               setValue = (metaType.isTable() == false && metaType.isComposite() == false);
            }
            if (setValue)
             {
                InstanceClassFactory icf = getObjectFactory().getInstanceClassFactory(attachment.getClass());
                BeanInfo beanInfo = propertyInfo.getBeanInfo();
                icf.setValue(beanInfo, this, attachment, metaValue);
             }
          }
       }
    }
 
    /**
     * Expose only plain ManangedPropertyImpl.
     *
     * @return simpler ManagedPropertyImpl
     * @throws ObjectStreamException for any error
     */
    private Object writeReplace() throws ObjectStreamException
    {
       ManagedPropertyImpl managedProperty = new ManagedPropertyImpl(getManagedObject(), getFields());
       managedProperty.setTargetManagedObject(getTargetManagedObject());
       return managedProperty;
    }
 }
