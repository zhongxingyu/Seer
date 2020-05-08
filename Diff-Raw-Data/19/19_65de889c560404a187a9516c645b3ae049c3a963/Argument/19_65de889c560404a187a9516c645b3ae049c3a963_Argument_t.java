 package org.p000ison.dev.commandapi;
 
 /**
  * Represents a Argument
  */
 public final class Argument {
 
     private final String name;
     private final int position;
     private final boolean optional;
     private final boolean decimal, integer;
 
     public Argument(final String name, final int position, final boolean optional, final boolean decimal, final boolean integer) {
         this.name = name;
         this.position = position;
         this.optional = optional;
         this.decimal = decimal;
         this.integer = integer;
     }
 
     public final String getName() {
         return name;
     }
 
     public final int getPosition() {
         return position;
     }
 
    public final boolean isRequired() {
         return optional;
     }
 
     public final boolean isDecimal() {
         return decimal;
     }
 
     public final boolean isInteger() {
         return integer;
     }
 }
