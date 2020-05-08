 package edu.uw.zookeeper.jackson;
 
 import static com.google.common.base.Preconditions.*;
 
 import java.io.IOException;
 
 import org.apache.jute.Index;
 import org.apache.jute.InputArchive;
 import org.apache.jute.Record;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonStreamContext;
 import com.fasterxml.jackson.core.JsonToken;
 import com.google.common.base.Throwables;
 
 public class JacksonInputArchive implements InputArchive {
 
     protected final JsonParser json;
     
     public JacksonInputArchive(JsonParser json) {
         this.json = json;
     }
 
     @Override
     public byte readByte(String tag) throws IOException {
         return readBuffer(tag)[0];
     }
 
     @Override
     public boolean readBool(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         boolean value;
         if (token == JsonToken.VALUE_TRUE) {
             value = true;
         } else if (token == JsonToken.VALUE_FALSE) {
             value = false;
         } else {
             throw new JsonParseException(String.valueOf(token), json.getCurrentLocation());
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public int readInt(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         int value;
         if (token == JsonToken.VALUE_NUMBER_INT) {
             value = json.getIntValue();
         } else {
             throw new JsonParseException(String.valueOf(token), json.getCurrentLocation());
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public long readLong(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         long value;
         if (token == JsonToken.VALUE_NUMBER_INT) {
             value = json.getLongValue();
         } else {
             throw new JsonParseException(String.valueOf(token), json.getCurrentLocation());
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public float readFloat(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         float value;
         if (token == JsonToken.VALUE_NUMBER_FLOAT) {
             value = json.getFloatValue();
         } else {
             throw new JsonParseException(String.valueOf(token), json.getCurrentLocation());
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public double readDouble(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         double value;
         if (token == JsonToken.VALUE_NUMBER_FLOAT) {
             value = json.getDoubleValue();
         } else {
             throw new JsonParseException(String.valueOf(token), json.getCurrentLocation());
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public String readString(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         String value;
         if (token == JsonToken.VALUE_NULL) {
             value = null;
         } else if (token == JsonToken.VALUE_STRING) {
             value = json.getValueAsString();
         } else {
             throw new JsonParseException(String.valueOf(token), json.getCurrentLocation());
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public byte[] readBuffer(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         byte[] value;
         if (token == JsonToken.VALUE_NULL) {
             value = null;
         } else if (token == JsonToken.VALUE_STRING) {
             value = json.getBinaryValue();
         } else {
             throw new JsonParseException(String.valueOf(token), json.getCurrentLocation());
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public void readRecord(Record r, String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         if (json.isExpectedStartArrayToken()) {
             r.deserialize(this, tag);
         } else if (token == JsonToken.VALUE_NULL) {
             json.clearCurrentToken();
         } else {
            // must be an EmptyRecord
         }
     }
 
     @Override
     public void startRecord(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         if (! json.isExpectedStartArrayToken()) {
             throw new JsonParseException(String.valueOf(json.getCurrentToken()), json.getCurrentLocation());
         }
         json.clearCurrentToken();
     }
 
     @Override
     public void endRecord(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         if (token != JsonToken.END_ARRAY) {
             throw new JsonParseException(String.valueOf(json.getCurrentToken()), json.getCurrentLocation());
         }
         json.clearCurrentToken();
     }
 
     @Override
     public Index startVector(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         Index value;
         if (token == JsonToken.VALUE_NULL) {
             value = null;
         } else if (json.isExpectedStartArrayToken()) {
             value = new ArrayIndex();
         } else {
             throw new JsonParseException(String.valueOf(json.getCurrentToken()), json.getCurrentLocation());            
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public void endVector(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         if (token == JsonToken.END_ARRAY) {
             json.clearCurrentToken();
         }
     }
 
     @Override
     public Index startMap(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         Index value;
         if (token == JsonToken.VALUE_NULL) {
             value = null;
         } else if (json.isExpectedStartArrayToken()) {
             value = new ArrayIndex();
         } else {
             throw new JsonParseException(String.valueOf(json.getCurrentToken()), json.getCurrentLocation());            
         }
         json.clearCurrentToken();
         return value;
     }
 
     @Override
     public void endMap(String tag) throws IOException {
         JsonToken token = json.getCurrentToken();
         if (token == null) {
             token = json.nextToken();
         }
         if (token == JsonToken.END_ARRAY) {
             json.clearCurrentToken();
         }
     }
 
     protected class ArrayIndex implements Index {
         
         private final JsonStreamContext context;
         
         public ArrayIndex() {
             this.context = json.getParsingContext();
             checkState(context.inArray());
         }
         
         @Override
         public boolean done() {
             JsonStreamContext c = json.getParsingContext();
             while (! context.equals(c) && (c != null)) {
                 c = c.getParent();
             }
             if (! context.equals(c)) {
                 return true;
             }
             JsonToken token = json.getCurrentToken();
             if (token == null) {
                 try {
                     token = json.nextToken();
                 } catch (IOException e) {
                     throw Throwables.propagate(e);
                 }
             }
             if (token == JsonToken.END_ARRAY) {
                 return true;
             } else if (token == null) {
                 throw new IllegalStateException();
             }
             return false;
         }
 
         @Override
         public void incr() {
             JsonToken token = json.getCurrentToken();
             if (token == null) {
                 try {
                     token = json.nextToken();
                 } catch (IOException e) {
                     throw Throwables.propagate(e);
                 }
             }
             if (token == null) {
                 throw new IllegalStateException();
             }
         }
     }
 }
