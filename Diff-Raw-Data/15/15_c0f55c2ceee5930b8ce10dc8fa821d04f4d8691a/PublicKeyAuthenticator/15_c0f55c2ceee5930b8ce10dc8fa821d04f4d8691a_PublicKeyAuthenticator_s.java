 package org.mosaic.shell.impl.auth;
 
 import java.io.IOException;
 import java.security.PublicKey;
 import javax.annotation.Nonnull;
 import org.apache.sshd.server.PublickeyAuthenticator;
 import org.apache.sshd.server.session.ServerSession;
 import org.mosaic.lifecycle.annotation.Bean;
import org.mosaic.lifecycle.annotation.ServiceRef;
 import org.mosaic.security.User;
 import org.mosaic.security.UserManager;
 import org.mosaic.security.credentials.PublicKeys;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author arik
  */
 @Bean
 public class PublicKeyAuthenticator implements PublickeyAuthenticator
 {
     private static final Logger LOG = LoggerFactory.getLogger( PublicKeyAuthenticator.class );
 
     @Nonnull
     private UserManager userManager;
 
    @ServiceRef
     public void setUserManager( @Nonnull UserManager userManager )
     {
         this.userManager = userManager;
     }
 
     @Override
     public boolean authenticate( String username, PublicKey key, ServerSession session )
     {
         User user = this.userManager.loadUser( "local", username );
         if( user == null )
         {
             return false;
         }
 
         PublicKeys publicKeys = user.getCredential( PublicKeys.class );
         if( publicKeys == null )
         {
             return false;
         }
 
         try
         {
             return publicKeys.authorizedFor( key );
         }
         catch( IOException e )
         {
             LOG.warn( "Error while checking public keys for user '{}': {}", user, e.getMessage(), e );
             return false;
         }
     }
 }
