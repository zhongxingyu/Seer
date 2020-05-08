 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.llvm;
 
 /**
  * ret */
 public class RET implements Terminator {
 
     String type, value;
     Instruction iValue;
 
     public RET () {}
 
     /**
      * Type */
     public RET type (String t) { type = t; return this; }
 
     /**
      * Value */
     public RET value (String v) { value = v; return this; }
 
     /**
      * Type and value */
     public RET value (Instruction i) { iValue = i; return this; }
 
     public String toString () {
         if (iValue != null) {
             type = iValue.getType ();
             value = iValue.getId ();
         }
        if (value == null)
             return "ret void\n";
         else
             return "ret " + type + " " + value + "\n";
     }
 
     public boolean needsId () { return false; }
     public void setId (String id) {}
     public String getId () { throw new RuntimeException (); }
     public String getType () { return null; }
 
 }
