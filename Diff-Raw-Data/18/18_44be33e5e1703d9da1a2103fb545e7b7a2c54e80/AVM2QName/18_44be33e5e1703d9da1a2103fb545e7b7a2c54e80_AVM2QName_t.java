 package com.anotherbigidea.flash.avm2.model;
 
 import com.anotherbigidea.flash.avm2.MultiNameKind;
import com.anotherbigidea.flash.avm2.NamespaceKind;
 
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
    
    /**
     * @param qualifiedName a fully qualified name
     */
    public AVM2QName( String qualifiedName ) {
        this( new AVM2Namespace( NamespaceKind.PackageNamespace, 
                                 packagePrefix( qualifiedName )),
              simpleName( qualifiedName ) );
    }    
    
    /**
     * Get the as a fully qualified name string
     */
    public String toQualString() {
        return namespace.name + "." + name;
    }
 }
