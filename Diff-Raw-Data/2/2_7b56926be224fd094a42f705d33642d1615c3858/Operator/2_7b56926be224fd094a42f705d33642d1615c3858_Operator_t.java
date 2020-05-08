 /**
  * Operator.java
  *
  * An enum for representing the different operations available in the While
  * language.
  */
 
 package wam.util;
 
 public enum Operator {
     ADD("+"),
     SUB("-"),
     MUL("*"),
     EQ ("="),
    LE ("≤"),
     AND("∧"),
     NOT("¬");
 
     /**
      * The textual representation of the operator.
      */
     private final String alias;
 
     /**
      * Create a new Operator with the given alias.
      *
      * @param alias The textual representation of the operator.
      */
     private Operator(String alias) {
         this.alias = alias;
     }
 
     @Override
     public String toString() {
         return alias;
     }
 }
