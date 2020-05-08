 //    Data SQL is a light JDBC wrapper.
 //    Copyright (C) 2014 Adrián Romero Corchado.
 //
 //    This file is part of Data SQL
 //
 //     Licensed under the Apache License, Version 2.0 (the "License");
 //     you may not use this file except in compliance with the License.
 //     You may obtain a copy of the License at
 //     
 //         http://www.apache.org/licenses/LICENSE-2.0
 //     
 //     Unless required by applicable law or agreed to in writing, software
 //     distributed under the License is distributed on an "AS IS" BASIS,
 //     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //     See the License for the specific language governing permissions and
 //     limitations under the License.
 
 package com.adr.datasql.orm;
 
 import java.sql.SQLException;
 import javax.json.Json;
import javax.json.JsonNumber;
 import javax.json.JsonObject;
import javax.json.JsonString;
 import javax.json.JsonValue;
 
 /**
  *
  * @author adrian
  */
 public class DataJson extends Data<JsonObject> {
     
     public DataJson(Definition definition) {
         super(definition);
     }
 
     @Override
     protected Object getValue(Field f, JsonObject param) throws SQLException {
         
         JsonValue value = param.get(f.getName());
         if (value == null) {
             return null;
         }
         
         if (value.getValueType() == JsonValue.ValueType.NULL) {
             return null;
         } else if (value.getValueType() == JsonValue.ValueType.FALSE) {
             return false;
         } else if (value.getValueType() == JsonValue.ValueType.TRUE) {
             return true;
         } else if (value.getValueType() == JsonValue.ValueType.STRING) {
            return ((JsonString)value).getString();
         } else if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            JsonNumber num = (JsonNumber) value;
            return num.isIntegral() ? num.intValue(): num.doubleValue();
         } else {
             throw new SQLException("Not valid Json value.");
         }  
     }        
 
     @Override
     protected void setValue(Field f, JsonObject param, Object value) throws SQLException {
         
         if (value == null) {
             param.put(f.getName(), JsonValue.NULL);            
         } else if (value instanceof Boolean) {
             param.put(f.getName(), ((Boolean) value) ? JsonValue.TRUE : JsonValue.FALSE);                        
         } else if (value instanceof Number) {
             param.put(f.getName(), new JsonValueImpl(value, JsonValue.ValueType.NUMBER));
         } else {
             param.put(f.getName(), new JsonValueImpl(value, JsonValue.ValueType.STRING));
         }
     }
 
     @Override
     protected JsonObject create() throws SQLException {
         return Json.createObjectBuilder().build();
     }
     
     private static final class JsonValueImpl implements JsonValue {
         
         private final String value;
         private final JsonValue.ValueType valuetype;
         
         public JsonValueImpl(Object value, JsonValue.ValueType valuetype) {
             this.value = value.toString();
             this.valuetype = valuetype;
         }
 
         @Override
         public JsonValue.ValueType getValueType() {
             return valuetype;
         }
         @Override
         public String toString() {
             return value;
         }
     }
 }
