 package org.mosaic.shell.impl.auth;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import org.apache.sshd.server.session.ServerSession;
 import org.mosaic.lifecycle.annotation.Bean;
import org.mosaic.lifecycle.annotation.BeanRef;
 import org.mosaic.security.User;
 import org.mosaic.security.UserManager;
 import org.mosaic.security.credentials.Password;
 
 /**
  * @author arik
  */
 @Bean
 public class PasswordAuthenticator implements org.apache.sshd.server.PasswordAuthenticator
 {
     @Nonnull
     private UserManager userManager;
 
    @BeanRef
     public void setUserManager( @Nonnull UserManager userManager )
     {
         this.userManager = userManager;
     }
 
     @Override
     public boolean authenticate( @Nonnull String username, @Nullable String password, @Nonnull ServerSession session )
     {
         if( password == null || password.trim().isEmpty() )
         {
             return false;
         }
 
         User user = this.userManager.loadUser( "local", username );
         if( user == null )
         {
             return false;
         }
 
         Password passwordCredential = user.getCredential( Password.class );
         return passwordCredential != null && passwordCredential.check( password.toCharArray() );
     }
 }
