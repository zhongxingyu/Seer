 package de.tuberlin.dima.presslufthammer.data.json;
 
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import de.tuberlin.dima.presslufthammer.data.Field;
 import de.tuberlin.dima.presslufthammer.data.PrimitiveType;
 import de.tuberlin.dima.presslufthammer.data.RecordDecoder;
 import de.tuberlin.dima.presslufthammer.data.SchemaNode;
 import de.tuberlin.dima.presslufthammer.data.fields.BooleanField;
 import de.tuberlin.dima.presslufthammer.data.fields.DoubleField;
 import de.tuberlin.dima.presslufthammer.data.fields.FloatField;
 import de.tuberlin.dima.presslufthammer.data.fields.IntField;
 import de.tuberlin.dima.presslufthammer.data.fields.LongField;
 import de.tuberlin.dima.presslufthammer.data.fields.RecordField;
 import de.tuberlin.dima.presslufthammer.data.fields.StringField;
 
 class JSONDecoder implements RecordDecoder {
     private final SchemaNode schema;
 
     private Iterator<Entry<String, Object>> iterator;
     private Iterator<Object> arrayIterator = null;
     private String arrayKey = null;
 
     @SuppressWarnings("unchecked")
     public JSONDecoder(SchemaNode schema, JSONObject job) {
         this.schema = schema;
         iterator = job.entrySet().iterator();
     }
 
     public RecordDecoder newDecoder(SchemaNode schema, Object data) {
         JSONObject job = (JSONObject) data;
         return new JSONDecoder(schema, job);
     }
 
     private Field wrapPrimitive(String key, Object value, SchemaNode childSchema) {
         if (childSchema.getPrimitiveType() == PrimitiveType.INT
                 && value instanceof Integer) {
             return new IntField(childSchema, (Integer) value);
         } else if (childSchema.getPrimitiveType() == PrimitiveType.BOOLEAN
                 && value instanceof Boolean) {
             return new BooleanField(childSchema, (Boolean) value);
         } else if (childSchema.getPrimitiveType() == PrimitiveType.LONG
                 && value instanceof Long) {
             return new LongField(childSchema, (Long) value);
         } else if (childSchema.getPrimitiveType() == PrimitiveType.FLOAT
                 && value instanceof Float) {
             return new FloatField(childSchema, (Float) value);
         } else if (childSchema.getPrimitiveType() == PrimitiveType.DOUBLE
                 && value instanceof Double) {
             return new DoubleField(childSchema, (Double) value);
         } else if (childSchema.getPrimitiveType() == PrimitiveType.STRING
                 && value instanceof String) {
             return new StringField(childSchema, (String) value);
         }
         return null;
     }
 
     private Field wrapValue(String key, Object value, SchemaNode childSchema) {
         if (childSchema.isPrimitive()) {
             return wrapPrimitive(key, value, childSchema);
         } else {
             if (value instanceof JSONObject) {
                 JSONObject jsonValue = (JSONObject) value;
                 RecordField field = new RecordField(childSchema, jsonValue);
                 return field;
             } else {
                 return null;
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public Field next() {
         Field field = null;
         while (arrayIterator != null && field == null) {
             if (arrayIterator.hasNext()) {
                 Object value = arrayIterator.next();
                 SchemaNode childSchema = schema.getField(arrayKey);
                 field = wrapValue(arrayKey, value, childSchema);
             } else {
                 arrayIterator = null;
                 arrayKey = null;
             }
         }
         if (field != null) {
             return field;
         }
 
         while (iterator.hasNext() && field == null) {
             Entry<String, Object> entry = iterator.next();
             String key = entry.getKey();
             Object value = entry.getValue();
 
             if (schema.hasField(key)) {
                 SchemaNode childSchema = schema.getField(key);
                 if (childSchema.isRepeated()) {
                    JSONArray array = (JSONArray) value;
                    arrayIterator = array.iterator();
                    arrayKey = key;
                    field = next();
                 } else {
                     field = wrapValue(key, value, childSchema);
                 }
             }
         }
         return field;
     }
 }
