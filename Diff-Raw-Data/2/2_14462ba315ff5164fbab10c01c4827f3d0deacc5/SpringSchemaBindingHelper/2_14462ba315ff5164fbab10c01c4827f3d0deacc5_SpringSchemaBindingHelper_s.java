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
 package org.jboss.spring.deployment.xml;
 
 import org.jboss.xb.binding.sunday.unmarshalling.TypeBinding;
 import org.jboss.kernel.plugins.deployment.xml.*;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public class SpringSchemaBindingHelper
 {
 
    public static void initBeansHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(SpringBeansHandler.HANDLER);
       // handle beans
       typeBinding.pushInterceptor(SpringSchemaBinding.beansQName, SpringBeansInterceptor.INTERCEPTOR);
       // todo alias
       // todo import
    }
 
    public static void initBeanHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(SpringBeanHandler.HANDLER);
       // handle constructor-arg
       typeBinding.pushInterceptor(SpringSchemaBinding.constructorQName, ConstructorArgInterceptor.INTERCEPTOR);
       // handle properties
       typeBinding.pushInterceptor(SpringSchemaBinding.propertyQName, BeanPropertyInterceptor.INTERCEPTOR);
       // todo lookup-method
       // todo replaced method
    }
 
    public static void initRefHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(RefHandler.HANDLER);
    }
 
    public static void initConstructorArgHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(ConstructorArgHandler.HANDLER);
    }
 
    public static void initPropertyHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(PropertyHandler.HANDLER);
       // property can take characters
       typeBinding.setSimpleType(PropertyCharactersHandler.HANDLER);
       // configure
       configureValueBindings(typeBinding);
    }
 
    public static void initValueHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(ValueHandler.HANDLER);
       // value can take characters
       typeBinding.setSimpleType(ValueCharactersHandler.HANDLER);
       // configure
       configureValueBindings(typeBinding);
    }
 
    public static void initCollectionHandler(TypeBinding typeBinding)
    {
      typeBinding.setHandler(CollectionHandler.HANDLER);
       // configure
       configureValueBindings(typeBinding);
    }
 
    public static void initMapHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(MapHandler.HANDLER);
       // entry has an entry
       typeBinding.pushInterceptor(SpringSchemaBinding.entryQName, MapEntryInterceptor.INTERCEPTOR);
    }
 
    public static void initEntryHandler(TypeBinding typeBinding)
    {
       typeBinding.setHandler(EntryHandler.HANDLER);
       // entry has a key
       typeBinding.pushInterceptor(SpringSchemaBinding.keyQName, EntryKeyInterceptor.INTERCEPTOR);
       // entry has a value
       typeBinding.pushInterceptor(SpringSchemaBinding.valueQName, EntryValueInterceptor.INTERCEPTOR);
    }
 
    public static void configureValueBindings(TypeBinding typeBinding)
    {
       // type has values
       typeBinding.pushInterceptor(SpringSchemaBinding.valueQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take a list
       typeBinding.pushInterceptor(SpringSchemaBinding.listQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take a set
       typeBinding.pushInterceptor(SpringSchemaBinding.setQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type can take a map
       typeBinding.pushInterceptor(SpringSchemaBinding.mapQName, ValueMetaDataElementInterceptor.VALUES);
 
       // type has a null
       typeBinding.pushInterceptor(SpringSchemaBinding.nullQName, NullValueElementInterceptor.NULLVALUES);
    }
 
 }
