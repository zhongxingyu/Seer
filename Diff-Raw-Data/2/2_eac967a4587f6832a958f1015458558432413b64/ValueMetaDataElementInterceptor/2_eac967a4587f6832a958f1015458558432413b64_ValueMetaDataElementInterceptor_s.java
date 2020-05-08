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
 package org.jboss.kernel.plugins.deployment.xml;
 
 import javax.xml.namespace.QName;
 
 import org.jboss.beans.metadata.plugins.AbstractClassLoaderMetaData;
 import org.jboss.beans.metadata.plugins.AbstractCollectionMetaData;
 import org.jboss.beans.metadata.plugins.AbstractConstructorMetaData;
 import org.jboss.beans.metadata.plugins.AbstractParameterMetaData;
 import org.jboss.beans.metadata.plugins.AbstractPropertyMetaData;
 import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
 import org.jboss.beans.metadata.plugins.policy.AbstractBindingMetaData;
 import org.jboss.beans.metadata.spi.ValueMetaData;
 import org.jboss.xb.binding.sunday.unmarshalling.DefaultElementInterceptor;
 
 /**
  * ValueMetaDataElementInterceptor.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 68656 $
  */
 public class ValueMetaDataElementInterceptor extends DefaultElementInterceptor
 {
    /** The value handler */
   public static ValueMetaDataElementInterceptor VALUES = new ValueMetaDataElementInterceptor();
 
    public void add(Object parent, Object child, QName name)
    {
       if (parent instanceof AbstractCollectionMetaData)
       {
          AbstractCollectionMetaData collection = (AbstractCollectionMetaData) parent;
          ValueMetaData value = (ValueMetaData) child;
          collection.add(value);
       }
       else if (parent instanceof AbstractParameterMetaData)
       {
          AbstractParameterMetaData valueMetaData = (AbstractParameterMetaData) parent;
          ValueMetaData value = (ValueMetaData) child;
          valueMetaData.setValue(value);
       }
       else if (parent instanceof AbstractPropertyMetaData)
       {
          AbstractPropertyMetaData valueMetaData = (AbstractPropertyMetaData) parent;
          ValueMetaData value = (ValueMetaData) child;
          valueMetaData.setValue(value);
       }
       else if (parent instanceof AbstractClassLoaderMetaData)
       {
          AbstractClassLoaderMetaData valueMetaData = (AbstractClassLoaderMetaData) parent;
          ValueMetaData value = (ValueMetaData) child;
          valueMetaData.setClassLoader(value);
       }
       else if (parent instanceof AbstractConstructorMetaData)
       {
          AbstractConstructorMetaData valueMetaData = (AbstractConstructorMetaData) parent;
          ValueMetaData value = (ValueMetaData) child;
          valueMetaData.setValue(value);
       }
       else if (parent instanceof AbstractBindingMetaData)
       {
          AbstractBindingMetaData valueMetaData = (AbstractBindingMetaData) parent;
          ValueMetaData value = (ValueMetaData) child;
          valueMetaData.setValue(value);
       }
       else
       {
          AbstractValueMetaData valueMetaData = (AbstractValueMetaData) parent;
          ValueMetaData value = (ValueMetaData) child;
          valueMetaData.setValue(value);
       }
    }
 }
