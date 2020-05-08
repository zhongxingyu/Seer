 package org.mosaic.security.impl;
 
 import javax.annotation.Nonnull;
 import org.mosaic.security.Permission;
 import org.mosaic.security.Subject;
 import org.mosaic.util.expression.Expression;
 
 /**
  * @author arik
  */
 public class GrantRule
 {
     @Nonnull
     private final Expression<Boolean> test;
 
     @Nonnull
     private final Permission permission;
 
     public GrantRule( @Nonnull Expression<Boolean> test, @Nonnull Permission permission )
     {
         this.test = test;
         this.permission = permission;
     }
 
     public boolean implies( @Nonnull Subject user, @Nonnull Permission permission )
     {
        if( this.permission.implies( permission ) )
        {
            Boolean result = this.test.createInvocation( user ).invoke();
            return result != null && result;
        }
        else
        {
            return false;
        }
     }
 }
