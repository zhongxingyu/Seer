 /**
  * Operand.java
  *
  * Represents an operand lying on the operand stack in the While Virtual
  * Machine.
  */
 
 package wam.vm;
 
 public abstract class Operand {
     /**
      * The different types of an operand. Currently only boolean and integral
      * operands are supported.
      */
     public enum Type {
         Integer,
        Boolean,
     };
 
     /**
      * The type of the operand.
      */
     private final Type type;
 
     /**
      * Create a new operand of the given type.
      *
      * @param type The type of the operand.
      */
    public Operand(Type type) {
         this.type = type;
     }
 
     /**
      * @return the type of the Operand.
      */
     public Type getType() {
         return type;
     }
 }
