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
 
 package com.cedarsoft.serialization.demo2;
 
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.converters.ConversionException;
 import org.junit.*;
 import org.xml.sax.SAXException;
 
 import javax.annotation.Nonnull;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.IOException;
 import java.io.InputStream;
 
 import static com.cedarsoft.test.utils.AssertUtils.assertXMLEquals;
 import static org.junit.Assert.*;
 
 /**
  *
  */
 public class MoneyXstreamLegacyTest {
   private XStream xStream;
 
   @Before
   public void setUp() throws Exception {
     xStream = new XStream();
     xStream.alias( "money", Money.class );
   }
 
   @Test
   public void testFromXml() throws IOException {
     assertEquals( 701, ( ( Money ) xStream.fromXML( MoneyTest.EXPECTED.openStream() ) ).getCents() );
     assertEquals( 7.01, ( ( Money ) xStream.fromXML( MoneyTest.EXPECTED.openStream() ) ).getAmount(), 0 );
   }
 
   @Test
   public void testFromXmlLegacyFormat() throws IOException {
     try {
       xStream.fromXML( com.cedarsoft.serialization.demo1.MoneyTest.EXPECTED.openStream() );
       fail( "Where is the Exception" );
     } catch ( ConversionException e ) {
      assertEquals( "No such field com.cedarsoft.serialization.demo2.Money.amount\n" +
                      "---- Debugging information ----\n" +
                      "field               : amount\n" +
                      "class               : com.cedarsoft.serialization.demo2.Money\n" +
                      "required-type       : com.cedarsoft.serialization.demo2.Money\n" +
                      "converter-type      : com.thoughtworks.xstream.converters.reflection.ReflectionConverter\n" +
                      "path                : /money/amount\n" +
                      "line number         : 3\n" +
                      "version             : null\n" +
                      "-------------------------------", e.getMessage() );
     }
   }
 
   @Test
   public void testCustomConverter() throws IOException, SAXException {
     xStream.registerConverter( new MoneyConverter() );
     //writing
     assertXMLEquals( com.cedarsoft.serialization.demo2.MoneyTest.EXPECTED, xStream.toXML( new Money( 701 ) ) );
     //current format
     assertEquals( 701, ( ( Money ) xStream.fromXML( MoneyTest.EXPECTED.openStream() ) ).getCents() );
     //old format
     assertEquals( 701, ( ( Money ) xStream.fromXML( com.cedarsoft.serialization.demo1.MoneyTest.EXPECTED.openStream() ) ).getCents() );
   }
 
   @Test
   public void testManual() throws XMLStreamException, IOException, SAXException {
     assertEquals( 701, deserialize( MoneyTest.EXPECTED.openStream(), Version.CURRENT ).getCents() );
     assertEquals( 701, deserialize( com.cedarsoft.serialization.demo1.MoneyTest.EXPECTED.openStream(), Version.LEGACY ).getCents() );
   }
 
   private static Money deserialize( @Nonnull InputStream serialized, Version version ) throws XMLStreamException {
     //Boilerplate
     XMLInputFactory factory = XMLInputFactory.newInstance();
     XMLStreamReader reader = factory.createXMLStreamReader( serialized );
     //    writer.writeStartDocument();
 
     //That is the actual code
     reader.nextTag();//money
     reader.nextTag();//cents or amount
     reader.next();//the content
 
     long cents;
     if ( version == Version.LEGACY ) {
       cents = Money.convertValueToCents( Double.parseDouble( reader.getText() ) );
     } else {
       cents = Long.parseLong( reader.getText() );
     }
 
     return new Money( cents );
   }
 
   public enum Version {
     LEGACY,
     CURRENT
   }
 }
