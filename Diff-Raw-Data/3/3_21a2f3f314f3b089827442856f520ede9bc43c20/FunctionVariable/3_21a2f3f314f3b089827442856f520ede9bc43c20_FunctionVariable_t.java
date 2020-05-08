 /*
  * FunctionVariable.java
  *
  * Created on March 21, 2003, 12:15 AM
  */
 
 package org.webmacro.engine;
 
 import org.webmacro.Context;
 import org.webmacro.PropertyException;
 
 /**
  *
  * @author  Keats
  */
 public class FunctionVariable extends Variable
 {
     final public static Object TYPE = new Object();
 
     /** Creates a new instance of FunctionVariable */
     public FunctionVariable (Object names[])
     {
         super(names);
     }
 
     /** The code to get the value represented by the variable from the
      * supplied context.
      *
      */
     public Object getValue (Context context) throws PropertyException
     {
       return context.getProperty(_names);
       //return context.getProperty(_names[0]);
     }
 
     /** The code to set the value represented by the variable in the
      * supplied context.
      *
      */
     public void setValue (Context c, Object v) throws PropertyException
     {
         throw new PropertyException("Cannot set the value of a function: " + _vname);
     }
 
     /**
      * Return the String name of the variable prefixed with a string
      * representing its type, in this case "function:".
      */
     public String toString ()
     {
         return "function:" + _vname;
     }
 
     public boolean isSimpleName ()
     {
         return false;
     }
 
 }
