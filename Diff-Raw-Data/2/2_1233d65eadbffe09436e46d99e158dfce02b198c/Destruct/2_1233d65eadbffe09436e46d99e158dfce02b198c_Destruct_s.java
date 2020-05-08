 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import java.io.Serializable;
 
 import org.joe_e.Struct;
 
 /**
  * An exportable reference to the destruct operation.
  */
final class
 Destruct extends Struct implements Runnable, Serializable {
     static private final long serialVersionUID = 1L;
 
     /**
      * {@link Root#destruct}
      */
     private final Runnable destruct;
     
     Destruct(final Runnable destruct) {
         this.destruct = destruct;
     }
     
     // java.lang.Runnable interface
 
     public void
     run() { destruct.run(); }
 }
