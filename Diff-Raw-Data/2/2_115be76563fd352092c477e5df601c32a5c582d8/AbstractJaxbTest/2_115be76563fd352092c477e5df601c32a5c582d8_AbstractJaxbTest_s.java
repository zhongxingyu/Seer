 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.rest;
 
 import com.cedarsoft.AssertUtils;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
import org.testng.*;
 import org.junit.*;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 
 /**
  * @param <J> the object to serialize
  */
 public abstract class AbstractJaxbTest<J> {
   protected JAXBContext context;
 
   @Before
   public void setup() throws JAXBException {
     context = createContext();
     assertNotNull( context );
   }
 
   @NotNull
   protected abstract Class<J> getJaxbType();
 
   @NotNull
   public JAXBContext getContext() {
     return context;
   }
 
   @NotNull
   protected JAXBContext createContext() throws JAXBException {
     List<? extends Class<?>> classesToBeBound = getClassesToBeBound();
     return JAXBContext.newInstance( classesToBeBound.toArray( new Class[new ArrayList<Class<?>>( classesToBeBound ).size()] ) );
   }
 
   @NotNull
   protected List<? extends Class<?>> getClassesToBeBound() {
     List<Class<?>> list = new ArrayList<Class<?>>();
     list.add( getJaxbType() );
     Collection<? extends Class<?>> additionalClassesToBeBound = getAdditionalClassesToBeBound();
     if ( additionalClassesToBeBound != null ) {
       list.addAll( additionalClassesToBeBound );
     }
     return list;
   }
 
   @Nullable
   protected Collection<? extends Class<?>> getAdditionalClassesToBeBound() {
     return null;
   }
 
   @NotNull
   @NonNls
   protected abstract String expectedXml();
 
   @NotNull
   protected abstract J createObjectToSerialize() throws Exception;
 
   @Test
   public void testRound() throws Exception {
     Marshaller marshaller = createMarshaller();
     marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
 
     J jaxbObject = createObjectToSerialize();
 
     StringWriter out = new StringWriter();
     marshaller.marshal( jaxbObject, out );
 
     AssertUtils.assertXMLEqual( out.toString(), expectedXml() );
 
     J deserialized = getJaxbType().cast( createUnmarshaller().unmarshal( new StringReader( out.toString() ) ) );
     assertNotNull( deserialized );
 
     verifyDeserialized( deserialized, jaxbObject );
   }
 
   protected void verifyDeserialized( @NotNull J deserialized, @NotNull J originalJaxbObject ) throws IllegalAccessException {
     Assert.assertEquals( deserialized.getClass(), originalJaxbObject.getClass() );
     for ( Field field : originalJaxbObject.getClass().getDeclaredFields() ) {
       field.setAccessible( true );
 
       Object originalValue = field.get( originalJaxbObject );
       Object deserializedValue = field.get( deserialized );
 
       Assert.assertEquals( deserializedValue, originalValue, "Failed comparing field <" + field.getName() + ">" );
     }
   }
 
   @NotNull
   protected Marshaller createMarshaller() throws JAXBException {
     assertNotNull( context );
     return context.createMarshaller();
   }
 
   @NotNull
   protected Unmarshaller createUnmarshaller() throws JAXBException {
     return context.createUnmarshaller();
   }
 
   @Test
   public void testNameSpace() throws Exception {
     String namespace = getJaxbType().getAnnotation( XmlRootElement.class ).namespace();
     assertNotNull( namespace );
     Assert.assertFalse( namespace.equals( "##default" ), "Missing namespace for <" + getJaxbType().getName() + ">" );
     Assert.assertTrue( namespace.length() > 0 );
   }
 }
