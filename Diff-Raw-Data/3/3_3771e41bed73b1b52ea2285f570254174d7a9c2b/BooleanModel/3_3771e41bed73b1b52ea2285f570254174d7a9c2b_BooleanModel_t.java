 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.types;
 
 import java.io.Serializable;
 import org.modelcc.*;
 
 /**
 * Boolean Model.
  * @author elezeta
  * @serial
  */
 public class BooleanModel implements IModel,Serializable {
     
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Value.
      */
     @Value
     Boolean val;
 
     public boolean booleanValue() {
         return val.booleanValue();
     }
 
 
 }
