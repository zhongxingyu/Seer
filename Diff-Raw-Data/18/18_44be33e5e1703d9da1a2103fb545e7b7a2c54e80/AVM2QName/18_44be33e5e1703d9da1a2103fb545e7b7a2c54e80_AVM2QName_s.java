 package com.anotherbigidea.flash.avm2.model;
 
 import com.anotherbigidea.flash.avm2.MultiNameKind;
 
 /**
  * A name qualified by a namespace
  *
  * @author nickmain
  */
 public class AVM2QName extends AVM2Name {
 
     /**
      * @param namespace the namespace
      * @param name the simple name
      */
     public AVM2QName( AVM2Namespace namespace, String name ) {
         super( MultiNameKind.Qname, namespace, name, null );
     }    
 }
