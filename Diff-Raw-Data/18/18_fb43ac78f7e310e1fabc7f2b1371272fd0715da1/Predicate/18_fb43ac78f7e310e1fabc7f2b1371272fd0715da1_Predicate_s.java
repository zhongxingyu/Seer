 package com.os.sp.search;
 
 public class Predicate extends SearchCondition {
     
     private Field field;
     private Operator op;
     private Value value;
     
     public Field getField() {
         return field;
     }
     public void setField(Field field) {
         this.field = field;
     }
     
     public Operator getOp() {
         return op;
     }
     public void setOp(Operator op) {
         this.op = op;
     }
     
     public Value getValue() {
         return value;
     }
     public void setValue(Value value) {
         this.value = value;
     }
     
     public Predicate(Field field, Operator op, Value value) {
         super();
         this.field = field;
         this.op = op;
         this.value = value;
     }
    
    @Override
    public String toString() {
         
    }
 }
