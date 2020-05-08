 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.llvm;
 import java.util.List;
 import java.util.ArrayList;
 import me.pavlina.alco.llvm.FHead.*;
 
 /**
  * call */
 public class call
 {
     private Counter counter;
     private Function function;
     private String result;
     private String type;
     private String ftype;
     private String pointer;
     private boolean tail;
     private CallingConvention cconv;
     private List<ParamAttribute> retattrs;
     private List<FunctionAttribute> fattrs;
     private List<ParamAttribute[]> argAttrs;
     private List<String> argTypes;
     private List<String> argValues;
 
     public call (Counter counter, Function function) {
         this.counter = counter;
         this.function = function;
         tail = false;
         cconv = CallingConvention.CCC;
         retattrs = new ArrayList<ParamAttribute> ();
         fattrs = new ArrayList<FunctionAttribute> ();
         argAttrs = new ArrayList<ParamAttribute[]> ();
         argTypes = new ArrayList<String> ();
         argValues = new ArrayList<String> ();
     }
 
     /**
      * Required: Set return type */
     public call type (String type) {
         this.type = type;
         return this;
     }
 
     /**
      * Set function type */
     public call ftype (String ftype) {
         this.ftype = ftype;
         return this;
     }
 
     /**
      * Required: Set function pointer */
     public call pointer (String pointer) {
         this.pointer = pointer;
         return this;
     }
 
     /**
      * Set whether this is a tail call */
     public call tail (boolean tail) {
         this.tail = tail;
         return this;
     }
 
     /**
      * Set the calling convention */
     public call cconv (CallingConvention cconv) {
         this.cconv = cconv;
         return this;
     }
 
     /**
      * Add a return attribute */
     public call retattr (ParamAttribute retattr) {
         this.retattrs.add (retattr);
         return this;
     }
 
     /**
      * Add a function attribute */
     public call fattr (FunctionAttribute fattr) {
         this.fattrs.add (fattr);
         return this;
     }
 
     /**
      * Add an argument */
     public call arg (String type, String value, ParamAttribute... attrs) {
         this.argTypes.add (type);
         this.argValues.add (value);
         this.argAttrs.add (attrs);
         return this;
     }
 
     /**
      * Set the result register */
     public call result (String result) {
         this.result = result;
         return this;
     }
 
     public String build () {
         StringBuilder sb = new StringBuilder ();
         
         if (!type.equals ("void")) {
             if (result == null) {
                 result = "%" + counter.getTemporary ("%");
             }
             sb.append (result).append (" = ");
         }
         
         sb.append (String.format ("%scall %s",
                                   tail ? "tail " : "", cconv));
         for (ParamAttribute i: retattrs)
             sb.append (' ').append (i);
 
         sb.append (' ').append (type);
         if (ftype != null)
             sb.append (' ').append (ftype);
 
         sb.append (' ').append (pointer).append (" (");
         
         for (int i = 0; i < argAttrs.size (); ++i) {
             ParamAttribute[] attrs = argAttrs.get (i);
             String type = argTypes.get (i);
             String value = argValues.get (i);
 
             for (ParamAttribute j: attrs) {
                 sb.append (j).append (' ');
             }
             sb.append (type).append (' ').append (value);
         }
         sb.append (") ");
         for (FunctionAttribute i: fattrs) {
             sb.append (i).append (' ');
         }
         sb.append ('\n');
 
         function.add (sb.toString ());
 
         if (type.equals ("void")) {
             return null;
         } else {
             return result;
         }
     }
 }
