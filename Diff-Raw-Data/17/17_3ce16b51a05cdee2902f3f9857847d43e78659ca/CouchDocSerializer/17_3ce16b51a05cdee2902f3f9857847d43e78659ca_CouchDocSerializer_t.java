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
 
 package com.cedarsoft.couchdb.io;
 
 import com.cedarsoft.version.Version;
 import com.cedarsoft.couchdb.AttachmentId;
 import com.cedarsoft.couchdb.CouchDoc;
 import com.cedarsoft.couchdb.DocId;
 import com.cedarsoft.couchdb.Revision;
 import com.cedarsoft.serialization.jackson.AbstractJacksonSerializer;
 import com.cedarsoft.serialization.jackson.InvalidTypeException;
 import com.cedarsoft.serialization.jackson.JacksonSerializer;
 import com.sun.jersey.core.util.Base64;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 
 import javax.annotation.Nonnull;
 import javax.ws.rs.core.MediaType;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.cedarsoft.serialization.jackson.AbstractJacksonSerializer.nextFieldValue;
 import static com.cedarsoft.serialization.jackson.AbstractJacksonSerializer.nextToken;
 
 public class CouchDocSerializer {
 
   public static final String PROPERTY_ID = RawCouchDocSerializer.PROPERTY_ID;
 
   public static final String PROPERTY_REV = RawCouchDocSerializer.PROPERTY_REV;
 
   @Nonnull
   public <T> byte[] serialize( @Nonnull CouchDoc<T> doc, @Nonnull JacksonSerializer<? super T> wrappedSerializer ) throws IOException {
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     serialize( doc, wrappedSerializer, out );
     return out.toByteArray();
   }
 
   public <T> void serialize( @Nonnull CouchDoc<T> doc, @Nonnull JacksonSerializer<? super T> wrappedSerializer, @Nonnull OutputStream out ) throws IOException {
     JsonGenerator generator = RawCouchDocSerializer.createJsonGenerator( out );
 
     serialize( doc, wrappedSerializer, generator );
     generator.close();
   }
 
   public <T> void serialize( @Nonnull CouchDoc<T> doc, @Nonnull JacksonSerializer<? super T> wrappedSerializer, @Nonnull JsonGenerator generator ) throws IOException {
     generator.writeStartObject();
     RawCouchDocSerializer.serializeIdAndRev( generator, doc );
 
     //Type
     generator.writeStringField( AbstractJacksonSerializer.PROPERTY_TYPE, wrappedSerializer.getType() );
     //Version
     generator.writeStringField( AbstractJacksonSerializer.PROPERTY_VERSION, wrappedSerializer.getFormatVersion().format() );
 
     //The wrapped object
     wrappedSerializer.serialize( generator, doc.getObject(), wrappedSerializer.getFormatVersion() );
 
     //The attachments - placed at the end
     serializeInlineAttachments( doc, generator );
 
     generator.writeEndObject();
   }
 
   private <T> void serializeInlineAttachments( @Nonnull CouchDoc<T> doc, @Nonnull JsonGenerator generator ) throws IOException {
     if ( !doc.hasInlineAttachments() ) {
       return;
     }
 
     generator.writeObjectFieldStart( "_attachments" );
 
     for ( CouchDoc.Attachment attachment : doc.getAttachments() ) {
       if ( !attachment.isInline() ) {
         throw new IllegalStateException( "Cannot serialize non-inline attachments: " + attachment );
       }
       generator.writeObjectFieldStart( attachment.getId().asString() );
 
       generator.writeStringField( "content_type", attachment.getContentType().toString() );
       generator.writeStringField( "data", new String( Base64.encode( attachment.getData() ) ) );
 
       generator.writeEndObject();
     }
 
     generator.writeEndObject();
   }
 
   @Nonnull
   public <T> CouchDoc<T> deserialize( @Nonnull JacksonSerializer<T> wrappedSerializer, @Nonnull InputStream in ) {
     try {
       JsonParser parser = RawCouchDocSerializer.createJsonParser( in );
       CouchDoc<T> doc = deserialize( wrappedSerializer, parser );
 
       AbstractJacksonSerializer.ensureParserClosed( parser );
       return doc;
     } catch ( InvalidTypeException e ) {
       throw new RuntimeException( "Could not parse due to " + e.getMessage(), e );
     } catch ( IOException e ) {
       throw new RuntimeException( "Could not parse due to " + e.getMessage(), e );
     }
   }
 
   @Nonnull
   public <T> CouchDoc<T> deserialize( @Nonnull JacksonSerializer<T> wrappedSerializer, @Nonnull JsonParser parser ) throws InvalidTypeException, IOException {
     nextToken( parser, JsonToken.START_OBJECT );
 
     nextFieldValue( parser, PROPERTY_ID );
     String id = parser.getText();
 
     nextFieldValue( parser, PROPERTY_REV );
     String rev = parser.getText();
 
     //Type and Version
     nextFieldValue( parser, AbstractJacksonSerializer.PROPERTY_TYPE );
     wrappedSerializer.verifyType( parser.getText() );
     nextFieldValue( parser, AbstractJacksonSerializer.PROPERTY_VERSION );
     Version version = Version.parse( parser.getText() );
 
     //The wrapped object
     T wrapped = wrappedSerializer.deserialize( parser, version );
 
     //the attachments - if there are any....
     List<? extends CouchDoc.Attachment> attachments = deserializeAttachments( parser );
 
     AbstractJacksonSerializer.ensureObjectClosed( parser );
     CouchDoc<T> doc = new CouchDoc<T>( new DocId( id ), rev == null ? null : new Revision( rev ), wrapped );
     doc.addAttachments( attachments );
     return doc;
   }
 
   @Nonnull
   private List<? extends CouchDoc.Attachment> deserializeAttachments( JsonParser parser ) throws IOException {
     List<CouchDoc.Attachment> attachments = new ArrayList<CouchDoc.Attachment>();
 
     //check for attachments
     if ( parser.getCurrentToken() == JsonToken.FIELD_NAME && parser.getCurrentName().equals( "_attachments" ) ) {
       nextToken( parser, JsonToken.START_OBJECT );
 
       while ( parser.nextToken() != JsonToken.END_OBJECT ) {
         String attachmentId = parser.getCurrentName();
 
         nextToken( parser, JsonToken.START_OBJECT );
         nextFieldValue( parser, "content_type" );
         String contentType = parser.getText();
         nextFieldValue( parser, "revpos" );
        nextFieldValue( parser, "digest" );
         nextFieldValue( parser, "length" );
         long length = parser.getNumberValue().longValue();
         nextFieldValue( parser, "stub" );
 
         attachments.add( new CouchDoc.StubbedAttachment( new AttachmentId( attachmentId ), MediaType.valueOf( contentType ), length ) );
 
         nextToken( parser, JsonToken.END_OBJECT );
       }
       nextToken( parser, JsonToken.END_OBJECT );
     }
 
     return attachments;
   }
 }
