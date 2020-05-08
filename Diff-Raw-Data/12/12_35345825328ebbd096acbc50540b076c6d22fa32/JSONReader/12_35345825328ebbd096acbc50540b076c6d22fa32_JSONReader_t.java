 // Written by William Cook, Ben Wiedermann, Ali Ibrahim
 // The University of Texas at Austin, Department of Computer Science
 // See LICENSE.txt for license information
 package batch.json;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.sql.Date;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.Iterator;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 
 import batch.DataType;
 import batch.util.ForestReader;
 import batch.util.ForestWriter;
 import batch.util.TransportHelper;
 
 /*
 
 
  */
 public class JSONReader extends TransportHelper implements ForestReader {
 
   JsonParser jp;
 
   public JSONReader(Reader in) throws IOException {
     BufferedReader buf = new BufferedReader(in);
     String header = buf.readLine();
 
     if (!header.toLowerCase().equals("batch 1.0 json 1.0"))
       throw new Error("BAD FORMAT");
     JsonFactory jsonFactory;
     jsonFactory = new JsonFactory();
     jp = jsonFactory.createJsonParser(buf);
     check(JsonToken.START_OBJECT);
   }
 
   private void check(JsonToken expected) {
     JsonToken next;
     try {
       next = jp.nextToken();
     } catch (Exception e) {
       throw new Error("JSON Parser Error");
     }
     if (next != expected)
       throw new Error("EXPECTED " + expected.asString() + " FOUND "
           + next.asString());
   }
 
   public Object get(String request) {
     check(JsonToken.FIELD_NAME); // FIELD_NAME is returned when a String token is
     // encountered as a field name (same lexical
     // value, different function)
     try {
       String field = jp.getCurrentName();
       if (!field.equals(request))
         throw new Error("Mismatched field: " + request + " expected but found "
             + field);
       JsonToken token = jp.nextToken();
       switch (token) {
       case VALUE_NULL: // VALUE_TRUE is returned when encountering literal
         // "true" in value context
         return null;
       case VALUE_TRUE: // VALUE_TRUE is returned when encountering literal
         // "true" in value context
         return true;
       case VALUE_FALSE: // VALUE_FALSE is returned when encountering
         // literal "false" in value context
         return false;
       case VALUE_NUMBER_FLOAT: // VALUE_NUMBER_INT is returned when a
         // numeric token other that is not an
         // integer is encountered: that is, a
         // number that does have floating point
         // or exponent marker in it, in addition
         // to one or more digits.
         return jp.getDoubleValue();
       case VALUE_NUMBER_INT: // VALUE_NUMBER_INT is returned when an
         // integer numeric token is encountered in
         // value context: that is, a number that
         // does not have floating point or exponent
         // marker in it (consists only of an
         // optional sign, followed by one or more
         // digits)
         return jp.getLongValue();
       case VALUE_STRING: // VALUE_STRING is returned when a String token
         // is encountered in value context (array
         // element, field value, or root-level
         // stand-alone value)
         return jp.getText();
       case START_OBJECT:
         check(JsonToken.FIELD_NAME); // FIELD_NAME is returned when a String token is
         // encountered as a field name (same lexical
         // value, different function)
         field = jp.getCurrentName();
         if (field.charAt(0) == '*') {
           DataType type = DataType.fromString(field.substring(1));
           if (type != null) {
             check(JsonToken.VALUE_STRING);
             String data = jp.getText();
             check(JsonToken.END_OBJECT);
             return loadData(type, data);
           } else
             throw new Error("Unkonwn type: " + field);
         }
 
       default:
         throw new Error("JSON Parser Error");
       }
     } catch (JsonParseException e) {
       throw new Error("JSON Parser Error");
     } catch (IOException e) {
       throw new Error("JSON Parser Error");
     }
   }
 
   @Override
   public int getByte(String field) {
     return ((Number) get(field)).byteValue();
   }
 
   @Override
   public short getShort(String field) {
     return ((Number) get(field)).shortValue();
   }
 
   @Override
   public int getInteger(String field) {
     return ((Number) get(field)).intValue();
   }
 
   @Override
   public long getLong(String field) {
     return ((Number) get(field)).longValue();
   }
 
   @Override
   public double getDouble(String field) {
     return ((Number) get(field)).doubleValue();
   }
 
   @Override
   public float getFloat(String field) {
     return ((Number) get(field)).floatValue();
   }
 
   @Override
   public BigDecimal getBigDecimal(String field) {
     return (BigDecimal) get(field);
   }
 
   @Override
   public String getString(String field) {
     return get(field).toString();
   }
 
   @Override
   public char getCharacter(String field) {
     return (Character) get(field);
   }
 
   @Override
   public boolean getBoolean(String field) {
     return (Boolean) get(field);
   }
 
   @Override
   public Date getDate(String field) {
     return (Date) get(field);
   }
 
   @Override
   public java.util.Date getUtilDate(String field) {
     return (java.util.Date) get(field);
   }
 
   @Override
   public Time getTime(String field) {
     return (Time) get(field);
   }
 
   @Override
   public Timestamp getTimestamp(String field) {
     return (Timestamp) get(field);
   }
 
   @Override
   public byte[] getRawData(String field) {
     return (byte[]) get(field);
   }
 
   class ListReader implements Iterable<ForestReader> {
     JsonParser jp;
     JSONReader reader;
 
     public ListReader(JsonParser jp, JSONReader reader) {
       super();
       this.jp = jp;
       this.reader = reader;
     }
 
     @Override
     public Iterator<ForestReader> iterator() {
       return new ReadIterator(jp, reader);
     }
 
     class ReadIterator implements Iterator<ForestReader> {
       JsonParser jp;
       private boolean hasNext;
       JSONReader reader;
      boolean peeked;
     
       public ReadIterator(JsonParser jp, JSONReader reader) {
         super();
         this.jp = jp;
         this.reader = reader;
       }
 
       private void peek() {
        peeked = true;
         while (true)
           try {
             switch (jp.nextToken()) {
             case END_OBJECT:
               break; // continue
             case START_OBJECT:
               hasNext = true;
               return;
             case END_ARRAY:
               hasNext = false;
               return;
             default:
               throw new Error("JSON Reader error");
             }
           } catch (JsonParseException e) {
             throw new Error("JSON Reader error");
           } catch (IOException e) {
             throw new Error("JSON Reader error");
           }
       }
 
       @Override
       public boolean hasNext() {
        if (!peeked)
          peek();
         return hasNext;
       }
 
       @Override
       public ForestReader next() {
        peeked = false;
         return reader;
       }
 
       @Override
       public void remove() {
         throw new Error("CAN'T REMOVE from JSON Reader");
       }
     }
   }
 
   @Override
   public Iterable<ForestReader> getTable(String request) {
     try {
       check(JsonToken.FIELD_NAME); // FIELD_NAME is returned when a String token is
       // encountered as a field name (same lexical
       // value, different function)
       String field;
       field = jp.getCurrentName();
       if (!field.equals(request))
         throw new Error("Mismatched field: " + request + " expected but found "
             + field);
       check(JsonToken.START_ARRAY);
       return new ListReader(jp, this);
     } catch (JsonParseException e) {
       throw new Error("JSON Reader error");
     } catch (IOException e) {
       throw new Error("JSON Reader error");
     }
   }
 
   @Override
   public void complete() {
     try {
       jp.close();
     } catch (IOException e) {
       throw new Error("JSON Reader error");
     }
   }
 
   @Override
   public void copyTo(ForestWriter out) {
     throw new Error("Not yet implemented");
   }
 }
