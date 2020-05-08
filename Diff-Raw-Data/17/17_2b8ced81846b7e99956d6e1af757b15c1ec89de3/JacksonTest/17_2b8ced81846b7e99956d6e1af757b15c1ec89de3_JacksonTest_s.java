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
 
 package com.cedarsoft.serialization.bench;
 
 import org.codehaus.jackson.JsonEncoding;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.jetbrains.annotations.NonNls;
 import org.junit.*;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class JacksonTest {
   @NonNls
   public static final String JSON = "{\"id\":\"Canon Raw\",\"dependent\":false,\"extension\":{\"extension\":\"cr2\",\"default\":true,\"delimiter\":\".\"}}";
   private JsonFactory jsonFactory;
 
   @Before
   public void setUp() throws Exception {
     jsonFactory = new JsonFactory();
   }
 
   @Test
   public void testMapper() throws Exception {
     ObjectMapper mapper = new ObjectMapper();
 
     com.cedarsoft.serialization.bench.jaxb.FileType fileType = new com.cedarsoft.serialization.bench.jaxb.FileType( "Canon Raw", new com.cedarsoft.serialization.bench.jaxb.Extension( ".", "cr2", true ), false );
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     mapper.writeValue( out, fileType );
 
    assertEquals( JSON, out.toString() );
   }
 
   @Test
   public void testIt() throws IOException {
     FileType fileType = new FileType( "Canon Raw", new Extension( ".", "cr2", true ), false );
 
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );
 
     generator.writeStartObject();
     generator.writeStringField( "id", fileType.getId() );
     generator.writeBooleanField( "dependent", fileType.isDependent() );
 
     generator.writeFieldName( "extension" );
     generator.writeStartObject();
 
     generator.writeStringField( "extension", fileType.getExtension().getExtension() );
     generator.writeBooleanField( "default", fileType.getExtension().isDefault() );
     generator.writeStringField( "delimiter", fileType.getExtension().getDelimiter() );
 
     generator.writeEndObject();
 
     //    generator.writeFieldName( "id" );
     //    generator.writeString( fileType.getId() );
 
     generator.writeEndObject();
 
     generator.close();
     assertEquals( JSON, out.toString() );
   }
 
   @Test
   public void testParse() throws Exception {
     JsonParser parser = jsonFactory.createJsonParser( JSON );
 
     assertEquals( JsonToken.START_OBJECT, parser.nextToken() );
 
     assertEquals( JsonToken.FIELD_NAME, parser.nextToken() );
     assertEquals( "id", parser.getCurrentName() );
     assertEquals( JsonToken.VALUE_STRING, parser.nextToken() );
     assertEquals( "Canon Raw", parser.getText() );
 
     assertEquals( JsonToken.FIELD_NAME, parser.nextToken() );
     assertEquals( "dependent", parser.getCurrentName() );
     assertEquals( JsonToken.VALUE_FALSE, parser.nextToken() );
     assertFalse( parser.getBooleanValue() );
 
     assertEquals( JsonToken.FIELD_NAME, parser.nextToken() );
     assertEquals( "extension", parser.getCurrentName() );
     assertEquals( JsonToken.START_OBJECT, parser.nextToken() );
 
     assertEquals( JsonToken.FIELD_NAME, parser.nextToken() );
     assertEquals( "extension", parser.getCurrentName() );
     assertEquals( JsonToken.VALUE_STRING, parser.nextToken() );
     assertEquals( "cr2", parser.getText() );
 
     assertEquals( JsonToken.FIELD_NAME, parser.nextToken() );
     assertEquals( "default", parser.getCurrentName() );
     assertEquals( JsonToken.VALUE_TRUE, parser.nextToken() );
     assertTrue( parser.getBooleanValue() );
 
     assertEquals( JsonToken.FIELD_NAME, parser.nextToken() );
     assertEquals( "delimiter", parser.getCurrentName() );
     assertEquals( JsonToken.VALUE_STRING, parser.nextToken() );
     assertEquals( ".", parser.getText() );
 
     assertEquals( JsonToken.END_OBJECT, parser.nextToken() );
     assertEquals( JsonToken.END_OBJECT, parser.nextToken() );
     assertNull( parser.nextToken() );
   }
 }
