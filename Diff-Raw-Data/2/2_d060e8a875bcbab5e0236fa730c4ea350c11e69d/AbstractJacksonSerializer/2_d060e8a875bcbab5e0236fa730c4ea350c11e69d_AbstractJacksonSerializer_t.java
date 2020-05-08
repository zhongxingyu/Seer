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
 
 package com.cedarsoft.serialization.jackson;
 
 import com.cedarsoft.version.Version;
 import com.cedarsoft.version.VersionException;
 import com.cedarsoft.version.VersionRange;
 import com.cedarsoft.serialization.AbstractSerializer;
 import com.cedarsoft.serialization.jackson.test.compatible.JacksonParserWrapper;
 import org.codehaus.jackson.JsonEncoding;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.JsonToken;
 
 import javax.annotation.Nullable;
 
 import javax.annotation.Nonnull;
 import javax.annotation.WillNotClose;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @param <T> the type
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public abstract class AbstractJacksonSerializer<T> extends AbstractSerializer<T, JsonGenerator, JsonParser, JsonProcessingException> implements JacksonSerializer<T> {
   public static final String FIELD_NAME_DEFAULT_TEXT = "$";
   public static final String PROPERTY_TYPE = "@type";
   public static final String PROPERTY_VERSION = "@version";
   public static final String PROPERTY_SUB_TYPE = "@subtype";
 
   @Nonnull
   private final String type; //$NON-NLS-1$
 
   protected AbstractJacksonSerializer( @Nonnull String type, @Nonnull VersionRange formatVersionRange ) {
     super( formatVersionRange );
     this.type = type;
   }
 
   @Nonnull
   @Override
   public String getType() {
     return type;
   }
 
   @Override
   public void verifyType( @Nullable String type ) throws InvalidTypeException {
     if ( !this.type.equals( type ) ) {//$NON-NLS-1$
       throw new InvalidTypeException( type, this.type );
     }
   }
 
   @Override
   public void serialize( @Nonnull T object, @WillNotClose @Nonnull OutputStream out ) throws IOException {
     JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
     JsonGenerator generator = jsonFactory.createJsonGenerator( out, JsonEncoding.UTF8 );
 
     serialize( object, generator );
     generator.flush();
   }
 
   /**
    * Serializes the object to the given serializeTo.
    * <p/>
    * The serializer is responsible for writing start/close object/array brackets if necessary.
    * This method also writes the @type property.
    *
    * @param object    the object that is serialized
    * @param generator the serialize to object
    * @throws IOException
    */
   @Override
   public void serialize( @Nonnull T object, @Nonnull JsonGenerator generator ) throws IOException {
     if ( isObjectType() ) {
       generator.writeStartObject();
 
       beforeTypeAndVersion( object, generator );
       
       generator.writeStringField( PROPERTY_TYPE, type );
       generator.writeStringField( PROPERTY_VERSION, getFormatVersion().format() );
     }
 
     serialize( generator, object, getFormatVersion() );
 
     if ( isObjectType() ) {
       generator.writeEndObject();
     }
   }
 
   protected void beforeTypeAndVersion( @Nonnull T object, @Nonnull JsonGenerator serializeTo ) throws IOException {
   }
 
   @Nonnull
   @Override
   public T deserialize( @Nonnull InputStream in ) throws IOException, VersionException {
     return deserialize( in, null );
   }
 
   @Override
   @Nonnull
   public T deserialize( @Nonnull JsonParser parser ) throws IOException, JsonProcessingException, InvalidTypeException {
    return deserializeInternal( parser, null );
   }
 
   @Nonnull
   public T deserialize( @Nonnull InputStream in, @Nullable Version version ) throws IOException, VersionException {
     try {
       JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
       JsonParser parser = jsonFactory.createJsonParser( in );
 
       T deserialized = deserializeInternal( parser, version );
 
       ensureParserClosed( parser );
       return deserialized;
     } catch ( InvalidTypeException e ) {
       throw new IOException( "Could not parse due to " + e.getMessage(), e );
     }
   }
 
   /**
    * If the format version override is not null, the type and version field are skipped
    * @param parser the parser
    * @param formatVersionOverride the format version override (usually "null")
    * @return the deserialized object
    * @throws IOException
    * @throws JsonProcessingException
    * @throws InvalidTypeException
    */
   @Nonnull
   protected T deserializeInternal( @Nonnull JsonParser parser, @Nullable Version formatVersionOverride ) throws IOException, JsonProcessingException, InvalidTypeException {
     JacksonParserWrapper wrapper = new JacksonParserWrapper( parser );
 
     Version version = prepareDeserialization( wrapper, formatVersionOverride );
 
     T deserialized = deserialize( parser, version );
 
     if ( isObjectType() ) {
       ensureObjectClosed( parser );
     }
 
     return deserialized;
   }
 
   /**
    * Prepares the deserialization.
    *
    * If the format version is set - the type and version properties are *not* read!
    * This can be useful for cases where this information is not available...
    *
    * @param wrapper the wrapper
    * @param formatVersionOverride the format version
    * @return the format version
    * @throws IOException
    * @throws InvalidTypeException
    */
   @Nonnull
   private Version prepareDeserialization( @Nonnull JacksonParserWrapper wrapper, @Nullable Version formatVersionOverride ) throws IOException, InvalidTypeException {
     if ( isObjectType() ) {
       wrapper.nextToken( JsonToken.START_OBJECT );
 
       beforeTypeAndVersion( wrapper );
 
       if ( formatVersionOverride == null ) {
         wrapper.nextFieldValue( PROPERTY_TYPE );
         String readType = wrapper.getText();
         verifyType( readType );
         wrapper.nextFieldValue( PROPERTY_VERSION );
         Version version = Version.parse( wrapper.getText() );
         verifyVersionReadable( version );
         return version;
       } else {
         verifyVersionReadable( formatVersionOverride );
         return formatVersionOverride;
       }
     } else {
       wrapper.nextToken();
       return getFormatVersion();
     }
   }
 
   /**
    * Callback method that is called before the type and version are parsed
    *
    * @param wrapper the wrapper
    */
   protected void beforeTypeAndVersion( @Nonnull JacksonParserWrapper wrapper ) throws IOException, JsonProcessingException, InvalidTypeException {
   }
 
   @Deprecated
   public static void ensureParserClosedObject( @Nonnull JsonParser parser ) throws IOException {
     ensureObjectClosed( parser );
     ensureParserClosed( parser );
   }
 
   @Deprecated
   public static void ensureObjectClosed( @Nonnull JsonParser parser ) throws JsonParseException {
     if ( parser.getCurrentToken() != JsonToken.END_OBJECT ) {
       throw new JsonParseException( "No consumed everything " + parser.getCurrentToken(), parser.getCurrentLocation() );
     }
   }
 
   @Deprecated
   public static void ensureParserClosed( @Nonnull JsonParser parser ) throws IOException {
     if ( parser.nextToken() != null ) {
       throw new JsonParseException( "No consumed everything " + parser.getCurrentToken(), parser.getCurrentLocation() );
     }
 
     parser.close();
   }
 
   /**
    * Verifies the next field has the given name and prepares for read (by calling parser.nextToken).
    *
    * @param parser    the parser
    * @param fieldName the field name
    * @throws IOException
    */
   @Deprecated
   public static void nextFieldValue( @Nonnull JsonParser parser, @Nonnull String fieldName ) throws IOException {
     nextField( parser, fieldName );
     parser.nextToken();
   }
 
   /**
    * Verifies that the next field starts.
    * When the content of the field shall be accessed, it is necessary to call parser.nextToken() afterwards.
    *
    * @param parser    the parser
    * @param fieldName the field name
    * @throws IOException
    */
   @Deprecated
   public static void nextField( @Nonnull JsonParser parser, @Nonnull String fieldName ) throws IOException {
     nextToken( parser, JsonToken.FIELD_NAME );
     String currentName = parser.getCurrentName();
 
     if ( !fieldName.equals( currentName ) ) {
       throw new JsonParseException( "Invalid field. Expected <" + fieldName + "> but was <" + currentName + ">", parser.getCurrentLocation() );
     }
   }
 
   @Deprecated
   public static void nextToken( @Nonnull JsonParser parser, @Nonnull JsonToken expected ) throws IOException {
     parser.nextToken();
     verifyCurrentToken( parser, expected );
   }
 
   @Deprecated
   public static void verifyCurrentToken( @Nonnull JsonParser parser, @Nonnull JsonToken expected ) throws JsonParseException {
     JsonToken current = parser.getCurrentToken();
     if ( current != expected ) {
       throw new JsonParseException( "Invalid token. Expected <" + expected + "> but got <" + current + ">", parser.getCurrentLocation() );
     }
   }
 
   @Deprecated
   public static void closeObject( @Nonnull JsonParser deserializeFrom ) throws IOException {
     nextToken( deserializeFrom, JsonToken.END_OBJECT );
   }
 
   protected <T> void serializeArray( @Nonnull Iterable<? extends T> elements, @Nonnull Class<T> type, @Nonnull JsonGenerator serializeTo, @Nonnull Version formatVersion ) throws IOException {
     serializeArray( elements, type, null, serializeTo, formatVersion );
   }
 
   protected <T> void serializeArray( @Nonnull Iterable<? extends T> elements, @Nonnull Class<T> type, @Nullable String propertyName, @Nonnull JsonGenerator serializeTo, @Nonnull Version formatVersion ) throws IOException {
     JacksonSerializer<? super T> serializer = getSerializer( type );
     Version delegateVersion = delegatesMappings.getVersionMappings().resolveVersion( type, formatVersion );
 
     if ( propertyName == null ) {
       serializeTo.writeStartArray();
     } else {
       serializeTo.writeArrayFieldStart( propertyName );
     }
     for ( T element : elements ) {
       if ( serializer.isObjectType() ) {
         serializeTo.writeStartObject();
       }
 
       serializer.serialize( serializeTo, element, delegateVersion );
 
       if ( serializer.isObjectType() ) {
         serializeTo.writeEndObject();
       }
     }
     serializeTo.writeEndArray();
   }
 
   @Nonnull
   protected <T> List<? extends T> deserializeArray( @Nonnull Class<T> type, @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException {
     return deserializeArray( type, null, deserializeFrom, formatVersion );
   }
 
   @Nonnull
   protected <T> List<? extends T> deserializeArray( @Nonnull Class<T> type, @Nullable String propertyName, @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException {
     if ( propertyName == null ) {
       assert deserializeFrom.getCurrentToken() == JsonToken.START_ARRAY;
     } else {
       nextFieldValue( deserializeFrom, propertyName );
     }
 
     List<T> deserialized = new ArrayList<T>();
     while ( deserializeFrom.nextToken() != JsonToken.END_ARRAY ) {
       deserialized.add( deserialize( type, formatVersion, deserializeFrom ) );
     }
     return deserialized;
   }
 
   public <T> void serialize( @Nullable T object, @Nonnull Class<T> type, @Nonnull String propertyName, @Nonnull JsonGenerator serializeTo, @Nonnull Version formatVersion ) throws JsonProcessingException, IOException {
     serializeTo.writeFieldName( propertyName );
 
     //Fast exit if the value is null
     if ( object == null ) {
       serializeTo.writeNull();
       return;
     }
 
     JacksonSerializer<? super T> serializer = getSerializer( type );
     Version delegateVersion = delegatesMappings.getVersionMappings().resolveVersion( type, formatVersion );
     if ( serializer.isObjectType() ) {
       serializeTo.writeStartObject();
     }
 
     serializer.serialize( serializeTo, object, delegateVersion );
 
     if ( serializer.isObjectType() ) {
       serializeTo.writeEndObject();
     }
   }
 
   @Nullable
   protected <T> T deserializeNullable( @Nonnull Class<T> type, @Nonnull String propertyName, @Nonnull Version formatVersion, @Nonnull JsonParser deserializeFrom ) throws IOException, JsonProcessingException {
     nextFieldValue( deserializeFrom, propertyName );
 
     if ( deserializeFrom.getCurrentToken() == JsonToken.VALUE_NULL ) {
       return null;
     }
 
     return deserialize( type, formatVersion, deserializeFrom );
   }
 
   @Nonnull
   protected <T> T deserialize( @Nonnull Class<T> type, @Nonnull String propertyName, @Nonnull Version formatVersion, @Nonnull JsonParser deserializeFrom ) throws IOException, JsonProcessingException {
     nextFieldValue( deserializeFrom, propertyName );
     return deserialize( type, formatVersion, deserializeFrom );
   }
 
   @Nonnull
   @Override
   public <T> JacksonSerializer<? super T> getSerializer( @Nonnull Class<T> type ) {
     return ( JacksonSerializer<? super T> ) super.getSerializer( type );
   }
 
   @Override
   public boolean isObjectType() {
     return true;
   }
 
   public void serializeEnum( @Nonnull Enum<?> enumValue, @Nonnull String propertyName, @Nonnull JsonGenerator serializeTo ) throws IOException {
     serializeTo.writeStringField( propertyName, enumValue.name() );
   }
 
   @Nonnull
   public <T extends Enum<T>> T deserializeEnum( @Nonnull Class<T> enumClass, @Nonnull String propertyName, @Nonnull JacksonParserWrapper parser ) throws IOException {
     parser.nextFieldValue( propertyName );
     return Enum.valueOf( enumClass, parser.getText() );
   }
 }
