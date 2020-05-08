 package io.metacake.core.common;
 
 /**
  * This class is meant to be used as extensible enumerations.
  * <p>
  * All symbols are 'unique', in that they can only be compared through
  * referential equality.
  * For example :
  * public class MovementAction extends Symbol {
  *   public static final GO_UP = new MovementAction();
  *   public static final GO_DOWN = new MovementAction();
  *   public static final GO_LEFT = new MovementAction();
  *   public static final GO_RIGHT = new MovementAction();
  * }
  *
  * In this example, none of these fields can ever be equal to any other field.
  * In addition, the example type that extends Symbol could later be extended to add other
  * types to the enumeration.
  *
  * @author florence
  * @author rpless
  */
 public class Symbol {
     static final String PREFIX = "Symbol:";
 
     public static Symbol genSym() {
         return Symbol.genSym("");
     }
 
     public static Symbol genSym(String name) {
         return new Symbol(name);
     }
 
     private String name;
 
     public Symbol() {
         this("");
     }
 
     public Symbol(String name) {
         this.name = name;
     }
 
     @Override
     public final boolean equals(Object that) {
         return this == that;
     }
     @Override
     public final int hashCode() {
         return super.hashCode();
     }
 
     @Override
     public final String toString() {
         if(name.isEmpty()) {
             return super.toString();
         } else {
             return PREFIX + name;
         }
     }
 }
