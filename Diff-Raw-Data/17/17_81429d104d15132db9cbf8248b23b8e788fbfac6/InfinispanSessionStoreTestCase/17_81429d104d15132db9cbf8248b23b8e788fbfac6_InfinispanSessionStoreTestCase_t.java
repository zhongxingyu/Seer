 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.picketbox.test.infinispan.session.store;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNotSame;
 import static junit.framework.Assert.assertTrue;
 
 import java.io.Serializable;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.picketbox.core.DefaultPicketBoxManager;
 import org.picketbox.core.PicketBoxManager;
 import org.picketbox.core.UserContext;
 import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
 import org.picketbox.core.config.ConfigurationBuilder;
 import org.picketbox.core.config.PicketBoxConfiguration;
 import org.picketbox.core.exceptions.AuthenticationException;
 import org.picketbox.core.session.PicketBoxSession;
 import org.picketbox.core.session.SessionId;
 import org.picketbox.infinispan.session.store.InfinispanSessionStore;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.User;
 
 /**
  * <p>
  * Tests the core functionality for the {@link InfinispanSessionStore}.
  * </p>
  * 
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  * 
  */
 public class InfinispanSessionStoreTestCase {
 
     private static DefaultPicketBoxManager firstPicketBoxManager;
     private static DefaultPicketBoxManager secondPicketBoxManager;
 
     /**
      * <p>Creates two {@link PicketBoxManager} instances for testing. Each instance will have its own cache node.</p>
      * 
      * @throws Exception
      */
     @BeforeClass
     public static void onSetup() throws Exception {
         firstPicketBoxManager = createPicketBoxManager();
         secondPicketBoxManager = createPicketBoxManager();
     }
     
     @AfterClass
     public static void onFinish() throws Exception {
         firstPicketBoxManager.stop();
         secondPicketBoxManager.stop();
     }
     
     /**
      * <p>
      * Tests the {@link PicketBoxSession} replication between two distinct {@link PicketBoxManager} instances.
      * </p>
      * 
      * @throws Exception
      */
     @Test
     public void testSessionReplication() throws Exception {
         UserContext subject = new UserContext();
 
         subject.setCredential(new UsernamePasswordCredential("admin", "admin"));
 
         // lets authenticated the subject and get its session to use later
         UserContext originalUserContext = firstPicketBoxManager.authenticate(subject);
         PicketBoxSession originalSession = originalUserContext.getSession();
         SessionId<? extends Serializable> originalSessionId = originalSession.getId();
 
         assertTrue(originalUserContext.isAuthenticated());
 
         // lets try to authenticate the subject using the id from the previous session
         UserContext sameSessionUserContext = new UserContext(originalSessionId);
         UserContext replicatedUserContext = secondPicketBoxManager.authenticate(sameSessionUserContext);
         PicketBoxSession replicatedSession = replicatedUserContext.getSession();
         
         // user was automatically authenticated
         assertTrue(replicatedUserContext.isAuthenticated());
         
         // the replicated session is valid
         assertTrue(replicatedSession.isValid());
 
         // the original and the replicated session instances have the same id
         assertEquals(originalSession.getId(), replicatedSession.getId());
         
         // the original and the replicated session instances are not the same. but have the same id.
         assertNotSame(originalSession, replicatedSession);
     }
 
     /**
      * <p>
      * Tests if attributes are properly replicated.
      * </p>
      * 
      * @throws Exception
      */
     @Test
     public void testSessionAttributeReplication() throws Exception {
         UserContext authenticatingUserContext = new UserContext();
 
         authenticatingUserContext.setCredential(new UsernamePasswordCredential("admin", "admin"));
 
         // lets authenticated the subject and get its session to use later
         UserContext authenticatedUserContext = firstPicketBoxManager.authenticate(authenticatingUserContext);
         PicketBoxSession originalSession = authenticatedUserContext.getSession();
         SessionId<? extends Serializable> originalSessionId = originalSession.getId();
 
         assertTrue(authenticatedUserContext.isAuthenticated());
 
         // lets try to authenticate the subject using the id from the previous session
         UserContext sameSessionUserContext = new UserContext(originalSessionId);
         UserContext replicatedUserContext = secondPicketBoxManager.authenticate(sameSessionUserContext);
 
         assertTrue(replicatedUserContext.isAuthenticated());
 
         // lets replicate the attributes
         PicketBoxSession replicatedSession = replicatedUserContext.getSession();
 
         // sets a new attribute in the replicated session. The attribute should be replicated to the original session.
         replicatedSession.setAttribute("attributeA", "attributeA");
 
         // checks if the attribute was replicated to the original session.
         assertNotNull(originalSession.getAttribute("attributeA"));
 
         // now, change the attribute value in the original session.
         originalSession.setAttribute("attributeA", "changed");
 
         // lets see if the value was changed and properly replicated in the replicated session
         assertNotNull(replicatedSession.getAttribute("attributeA"));
         assertEquals("changed", replicatedSession.getAttribute("attributeA"));
     }
 
     /**
      * <p>
      * Tests if when used a invalid session id the authentication will fail.
      * </p>
      * 
      * @throws Exception
      */
     @Test(expected = AuthenticationException.class)
     public void testSessionInvalidation() throws Exception {
         UserContext authenticatingUserContext = new UserContext();
 
         authenticatingUserContext.setCredential(new UsernamePasswordCredential("admin", "admin"));
 
         UserContext authenticatedUserContext = firstPicketBoxManager.authenticate(authenticatingUserContext);
         PicketBoxSession originalSession = authenticatedUserContext.getSession();
         SessionId<? extends Serializable> originalSessionId = originalSession.getId();
 
         UserContext sameSessionUserContext = new UserContext(originalSessionId);
         UserContext replicatedUserContext = secondPicketBoxManager.authenticate(sameSessionUserContext);
 
         // user logout. now the session should be invalidated and removed from the cache.
         firstPicketBoxManager.logout(authenticatedUserContext);
 
         assertFalse(replicatedUserContext.isAuthenticated());
 
         // lets try to authenticated again with the previous session id.
         UserContext invalidSessionUserContext = new UserContext(originalSessionId);
 
         // an exception should be raised because the session id is no more valid.
         secondPicketBoxManager.authenticate(invalidSessionUserContext);
     }
     
     /**
      * <p>
      * Tests if when used a invalid session id the authentication will fail.
      * </p>
      * 
      * @throws Exception
      */
     @Test(expected = AuthenticationException.class)
     public void testSessionExpiration() throws Exception {
         UserContext authenticatingUserContext = new UserContext();
 
         authenticatingUserContext.setCredential(new UsernamePasswordCredential("admin", "admin"));
 
         UserContext authenticatedUserContext = firstPicketBoxManager.authenticate(authenticatingUserContext);
        
        assertNotNull(authenticatedUserContext);
        assertTrue(authenticatedUserContext.isAuthenticated());
        
         PicketBoxSession originalSession = authenticatedUserContext.getSession();
         SessionId<? extends Serializable> originalSessionId = originalSession.getId();
 
         UserContext sameSessionUserContext = new UserContext(originalSessionId);
         UserContext replicatedUserContext = secondPicketBoxManager.authenticate(sameSessionUserContext);
 
         assertTrue(replicatedUserContext.isAuthenticated());
         
         // expiration was configure to 1 minute. Let's wait ...
         Thread.sleep(65000);
         
         // the original session must be expired, lets try to authenticated again with an invalid session id
         UserContext invalidSessionUserContext = new UserContext(originalSessionId);
 
         secondPicketBoxManager.authenticate(invalidSessionUserContext);
     }
 
     private static DefaultPicketBoxManager createPicketBoxManager() {
         ConfigurationBuilder builder = new ConfigurationBuilder();
 
         builder
             .sessionManager()
                 .store(new InfinispanSessionStore())
                 .sessionTimeout(1);
 
         PicketBoxConfiguration configuration = builder.build();
        
         DefaultPicketBoxManager picketBoxManager = new DefaultPicketBoxManager(configuration);
 
         picketBoxManager.start();
 
        IdentityManager identityManager = picketBoxManager.getIdentityManager();
        
        User user = identityManager.createUser("admin");
        
        identityManager.updatePassword(user, "admin");
        
         return picketBoxManager;
     }
 
 }
