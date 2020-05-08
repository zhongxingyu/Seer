 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 package net.rptools.lib.datavalue;
 
 import net.rptools.lib.result.Result;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Represents the NULL pseudo value.
  */
 public class NullDataValue implements DataValue {
 
 
     /** The singleton instance. */
     private static final NullDataValue INSTANCE = new NullDataValue();
 
     private NullDataValue() {
         // Stop instantiation
     }
 
     /**
      * Returns a NullDataValue instance.
      *
      * @return the NullDataValue instance.
      */
     public static NullDataValue getInstance() {
         return INSTANCE;
     }
 
     @Override
     public DataType dataType() {
         return DataType.NULL;
     }
 
     @Override
     public String asString() {
         throw new UnsupportedOperationException("Can not convert null to a string.");
     }
 
     @Override
     public long asLong() {
         throw new UnsupportedOperationException("Can not convert null to a number.");
     }
 
     @Override
     public double asDouble() {
         throw new UnsupportedOperationException("Can not convert null to a number.");
     }
 
     @Override
     public List<DataValue> asList() {
         throw new UnsupportedOperationException("Can not convert null to a list.");
     }
 
     @Override
     public Map<String, DataValue> asDictionary() {
         throw new UnsupportedOperationException("Can not convert null to a dictionary.");
     }
 
     @Override
     public Result asResult() {
         throw new UnsupportedOperationException("Can not convert null to a result.");
     }
 
     @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
     public DataValue add(DataValue val) {
         return DataValueOperations.add(this, val);
     }
 
     @Override
     public DataValue subtract(DataValue val) {
         return DataValueOperations.subtract(this, val);
     }
 
     @Override
     public DataValue multiply(DataValue val) {
         return DataValueOperations.multiply(this, val);
     }
 
     @Override
     public DataValue divide(DataValue val) {
         return DataValueOperations.divide(this, val);
     }
 
     @Override
     public DataValue remainder(DataValue val) {
         return DataValueOperations.remainder(this, val);
     }
 
     @Override
     public DataValue power(DataValue exp) {
         return DataValueOperations.power(this, exp);
     }
 
     @Override
     public DataValue negate() {
         throw new UnsupportedOperationException("Can not negatea null.");
     }
 
     @Override
     public boolean hasLabel() {
         return false;
     }
 
     @Override
     public DataLabel getLabel() {
         return DataLabel.NO_LABEL;
     }
 
     @Override
     public DataValue asLongValue() {
        return this;
     }
 
     @Override
     public DataValue asDoubleValue() {
        return this;
     }
 
     @Override
     public DataValue asStringValue() {
        return this;
     }
 
     @Override
     public DataValue asListValue() {
        return this;
     }
 
     @Override
     public DataValue asDictionaryValue() {
        return this;
     }
 
     @Override
     public DataValue asResultValue() {
        return this;
    }

    @Override
    public DataValue asBooleanValue() {
        return this;
     }
 }
