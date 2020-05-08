 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise;
 
 import java.io.Serializable;
 
 import org.joe_e.Struct;
 import org.ref_send.Record;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 
 /**
  * A return from a {@linkplain Eventual#spawn vat creation}.
  */
 public class
 Vat<T> extends Struct implements Record, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * object created by the vat's maker
      */
     public final T top;
     
     /**
      * destruct the vat
      * <p>
     * call like: <code>destruct.apply(null)</code>
      * </p>
      */
     public final Receiver<?> destruct;
 
     /**
      * Constructs an instance.
      * @param top       {@link #top}
      * @param destruct  {@link #destruct}
      */
     public @deserializer
     Vat(@name("top") final T top,
         @name("destruct") final Receiver<?> destruct) {
         this.top = top;
         this.destruct = destruct;
     }
 }
