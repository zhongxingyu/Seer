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
 package org.jboss.beans.metadata.spi;
 
 import org.jboss.beans.metadata.plugins.AbstractArrayMetaData;
 import org.jboss.beans.metadata.plugins.AbstractCollectionMetaData;
import org.jboss.beans.metadata.plugins.AbstractInjectionValueMetaData;
 import org.jboss.beans.metadata.plugins.AbstractListMetaData;
 import org.jboss.beans.metadata.plugins.AbstractMapMetaData;
 import org.jboss.beans.metadata.plugins.AbstractSetMetaData;
 import org.jboss.beans.metadata.plugins.AbstractValueFactoryMetaData;
 import org.jboss.beans.metadata.plugins.AbstractValueMetaData;
 import org.jboss.beans.metadata.plugins.StringValueMetaData;
 import org.jboss.beans.metadata.plugins.ThisValueMetaData;
 import org.jboss.reflect.spi.TypeInfo;
 import org.jboss.util.JBossInterface;
 import org.jboss.xb.annotations.JBossXmlChild;
 import org.jboss.xb.annotations.JBossXmlGroup;
 import org.jboss.xb.annotations.JBossXmlGroupText;
 import org.jboss.xb.annotations.JBossXmlGroupWildcard;
 
 /**
  * Metadata about a value.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision$
  */
 @JBossXmlGroup
 ({
    @JBossXmlChild(name="array", type=AbstractArrayMetaData.class),
    @JBossXmlChild(name="collection", type=AbstractCollectionMetaData.class),
   @JBossXmlChild(name="inject", type=AbstractInjectionValueMetaData.class),
    @JBossXmlChild(name="list", type=AbstractListMetaData.class),
    @JBossXmlChild(name="map", type=AbstractMapMetaData.class),
    @JBossXmlChild(name="null", type=AbstractValueMetaData.class),
    @JBossXmlChild(name="set", type=AbstractSetMetaData.class),
    @JBossXmlChild(name="this", type=ThisValueMetaData.class),
    @JBossXmlChild(name="value", type=StringValueMetaData.class),
    @JBossXmlChild(name="value-factory", type= AbstractValueFactoryMetaData.class)
 })
 @JBossXmlGroupText(wrapper= StringValueMetaData.class, property="value")
 @JBossXmlGroupWildcard(wrapper= AbstractValueMetaData.class, property="value")
 public interface ValueMetaData extends JBossInterface, MetaDataVisitorNode
 {
    /**
     * Get the underlying value
     * 
     * @return the underlying value
     */
    public Object getUnderlyingValue();
    
    /**
     * Get the value.
     * 
     * @param info the type info
     * @param cl the classloader
     * @return the value.
     * @throws Throwable for any error
     */
    Object getValue(TypeInfo info, ClassLoader cl) throws Throwable;
 }
