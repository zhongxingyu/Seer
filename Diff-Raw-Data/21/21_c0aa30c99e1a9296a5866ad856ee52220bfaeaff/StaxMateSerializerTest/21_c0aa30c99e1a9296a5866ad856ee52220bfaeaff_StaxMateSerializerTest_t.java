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
 
 package com.cedarsoft.serialization.stax;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.VersionException;
 import com.cedarsoft.VersionMismatchException;
 import com.cedarsoft.VersionRange;
 import com.cedarsoft.serialization.AbstractXmlSerializerTest;
 import com.cedarsoft.xml.XmlCommons;
 import org.codehaus.staxmate.out.SMOutputElement;
 import org.jetbrains.annotations.NotNull;
 import org.junit.*;
 import org.junit.rules.*;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class StaxMateSerializerTest extends AbstractXmlSerializerTest<String> {
   @NotNull
   @Rule
   public final ExpectedException expectedException = ExpectedException.none();
 
   @NotNull
   @Override
   protected AbstractStaxMateSerializer<String> getSerializer() {
     return new AbstractStaxMateSerializer<String>( "aString", "http://www.lang.java/String", new VersionRange( new Version( 1, 5, 3 ), new Version( 1, 5, 3 ) ) ) {
       @Override
       public void serialize( @NotNull SMOutputElement serializeTo, @NotNull String object, @NotNull Version formatVersion ) throws XMLStreamException {
         assert isVersionWritable( formatVersion );
         serializeTo.addCharacters( object );
       }
 
       @Override
       @NotNull
       public String deserialize( @NotNull XMLStreamReader deserializeFrom, @NotNull Version formatVersion ) throws XMLStreamException {
         assert isVersionReadable( formatVersion );
         deserializeFrom.next();
         String text = deserializeFrom.getText();
         closeTag( deserializeFrom );
         return text;
       }
     };
   }
 
   @Override
   protected void verifySerialized( @NotNull byte[] serialized ) throws Exception {
     super.verifySerialized( serialized );
     assertTrue( XmlCommons.format( new String( serialized ) ), new String( serialized ).contains( "xmlns=\"http://www.lang.java/String/1.5.3\"" ) );
   }
 
   @NotNull
   @Override
   protected String createObjectToSerialize() {
     return "asdf";
   }
 
   @NotNull
   @Override
   protected String getExpectedSerialized() {
     return "<aString xmlns=\"http://www.lang.java/String/1.5.3\">asdf</aString>";
   }
 
   @Override
   protected void verifyDeserialized( @NotNull String deserialized ) {
     assertEquals( "asdf", deserialized );
   }
 
   @Test
   public void testJson() throws Exception {
     try {
      StaxMateSupport.enableJson();
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       getSerializer().serialize( "daObject", out );
       assertEquals( "{\"aString\":{\"@xmlns\":{\"$\":\"http:\\/\\/www.lang.java\\/String\\/1.5.3\"},\"$\":\"daObject\"}}", out.toString() );
     } finally {
       StaxMateSupport.clear();
     }
   }
 
   @Test
   public void testNoVersion() throws IOException {
     expectedException.expect( VersionException.class );
     getSerializer().deserialize( new ByteArrayInputStream( "<aString>asdf</aString>".getBytes() ) );
   }
 
   @Test
   public void testWrongVersion() throws IOException {
     expectedException.expect( VersionMismatchException.class );
     getSerializer().deserialize( new ByteArrayInputStream( "<aString xmlns=\"http://www.lang.java/String/0.9.9\">asdf</aString>".getBytes() ) );
   }
 
   @Test
   public void testWrongNamespaceVersion() throws IOException {
     expectedException.expect( IOException.class );
     expectedException.expectMessage( "Invalid namespace. Was <http://www.lang.invalid.java/String/1.5.3> but expected <http://www.lang.java/String/$VERSION>" );
 
     getSerializer().deserialize( new ByteArrayInputStream( "<aString xmlns=\"http://www.lang.invalid.java/String/1.5.3\">asdf</aString>".getBytes() ) );
   }
 }
