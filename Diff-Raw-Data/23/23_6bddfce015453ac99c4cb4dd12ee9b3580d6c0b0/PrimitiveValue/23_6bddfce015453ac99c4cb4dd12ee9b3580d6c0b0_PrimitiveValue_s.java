 package deeva.processor;
 
 //import com.google.gson.Gson;
 //import com.google.gson.GsonBuilder;
 import com.sun.jdi.*;
 
 /**
  * Plain old Java Object containing data we want to eventually serialize.
  */
 public class PrimitiveValue extends JVMValue {
     private final Object value;
 
     public PrimitiveValue(String type, String value) {
         super(type);
         this.value = value;
     }
 
     public PrimitiveValue(String type, Value variableValue) {
         super(type);
        if (variableValue instanceof IntegerType) {
             this.value = ((IntegerValue) variableValue).value();
        } else if (variableValue instanceof BooleanType) {
             this.value = ((BooleanValue) variableValue).value();
        } else if (variableValue instanceof ByteType) {
             this.value = ((ByteValue) variableValue).value();
        } else if (variableValue instanceof CharType) {
             this.value = ((CharValue) variableValue).value();
        } else if (variableValue instanceof DoubleType) {
             this.value = ((DoubleValue) variableValue).value();
        } else if (variableValue instanceof FloatType) {
             this.value = ((FloatValue) variableValue).value();
        } else if (variableValue instanceof LongType) {
             this.value = ((LongValue) variableValue).value();
        } else if (variableValue instanceof ShortType) {
             this.value = ((ShortValue) variableValue).value();
         } else {
             this.value = "void";
         }
     }
 
 }
