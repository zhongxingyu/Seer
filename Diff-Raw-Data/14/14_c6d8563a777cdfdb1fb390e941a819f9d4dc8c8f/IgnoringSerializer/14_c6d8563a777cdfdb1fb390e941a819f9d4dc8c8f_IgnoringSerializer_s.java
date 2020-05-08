 package com.cedarsoft.serialization.jackson;
 
 import com.cedarsoft.version.Version;
 import com.cedarsoft.version.VersionException;
 import com.cedarsoft.version.VersionRange;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.JsonToken;
 
 import javax.annotation.Nonnull;
 import java.io.IOException;
 
 /**
  * Simply ignores the object/array
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class IgnoringSerializer extends AbstractJacksonSerializer<Void> {
   public IgnoringSerializer() {
     super( "ignoring", VersionRange.single( 0, 0, 0 ) );
   }
 
   @Override
   public void serialize( @Nonnull JsonGenerator serializeTo, @Nonnull Void object, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
     throw new UnsupportedOperationException();
   }
 
   @Nonnull
   @Override
   public Void deserialize( @Nonnull JsonParser deserializeFrom, @Nonnull Version formatVersion ) throws IOException, VersionException, JsonProcessingException {
     JsonToken inToken = deserializeFrom.nextToken();
 
     if ( isValueToken( inToken ) ) {
       return null;
     }
     
     
     JsonToken outToken = findOutToken( inToken );
 
     int depth = 1;
 
     while ( depth > 0 ) {
       JsonToken next = deserializeFrom.nextToken();
       if ( next == inToken ) {
         depth++;
       }
       if ( next == outToken ) {
         depth--;
       }
     }
 
     //noinspection ConstantConditions
     return null;
   }
 
   private static boolean isValueToken( @Nonnull JsonToken inToken ) {
     return inToken.name().startsWith( "VALUE_" );
   }
 
   @Nonnull
   private static JsonToken findOutToken( @Nonnull JsonToken inToken ) {
     switch ( inToken ) {
       case START_OBJECT:
         return JsonToken.END_OBJECT;
       case START_ARRAY:
         return JsonToken.END_ARRAY;
     }
 
     throw new IllegalArgumentException( "No end token found for <" + inToken + ">" );
   }
 }
