 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.common.service.nontransactional;
 
 import org.jtalks.common.model.dao.UserDao;
 import org.jtalks.common.model.entity.Entity;
 import org.jtalks.common.model.entity.User;
 import org.jtalks.common.service.SecurityService;
 import org.jtalks.common.service.security.AclBuilder;
 import org.jtalks.common.service.security.AclManager;
 import org.jtalks.common.service.security.SecurityConstants;
 import org.jtalks.common.service.security.SecurityContextFacade;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import sun.security.acl.PrincipalImpl;
 
 import java.security.Principal;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.*;
 
 /**
  * Test for {@link SecurityServiceImpl}.
  *
  * @author Kirill Afonin
  * @author Max Malakhov
  * @author Dmitry Sokolov
  */
 public class SecurityServiceImplTest {
 
     private static class TestEntity extends Entity {
 
     }
 
     private static final String USERNAME = "username";
     private static final String PASSWORD = "password";
 
     private UserDao userDao;
     private SecurityService securityService;
     private SecurityContextFacade securityContextFacade;
     private SecurityContext securityContext;
     private AclManager aclManager;
 
     private User getUser() {
         return new User(USERNAME, "email", PASSWORD);
     }
 
     @BeforeMethod
     public void setUp() throws Exception {
         userDao = mock(UserDao.class);
         securityContextFacade = mock(SecurityContextFacade.class);
         securityContext = mock(SecurityContext.class);
         aclManager = mock(AclManager.class);
         securityService = new SecurityServiceImpl(userDao, securityContextFacade, aclManager);
         when(securityContextFacade.getContext()).thenReturn(securityContext);
     }
 
     @Test
     public void testGetCurrentUser() throws Exception {
         User user = getUser();
         Authentication auth = mock(Authentication.class);
         when(auth.getPrincipal()).thenReturn(user);
         when(securityContext.getAuthentication()).thenReturn(auth);
         when(userDao.getByUsername(USERNAME)).thenReturn(user);
 
         User result = securityService.getCurrentUser();
 
         assertEquals(result.getUsername(), USERNAME, "Username not equals");
         assertEquals(result.getAuthorities().iterator().next().getAuthority(), "ROLE_USER");
         assertTrue(result.isAccountNonExpired());
         assertTrue(result.isAccountNonLocked());
        assertFalse(result.isEnabled());
         assertTrue(result.isCredentialsNonExpired());
         verify(userDao).getByUsername(USERNAME);
         verify(auth).getPrincipal();
         verify(securityContext).getAuthentication();
     }
 
     @Test
     public void testGetCurrentUserNotFound() throws Exception {
         when(securityContext.getAuthentication()).thenReturn(null);
 
         User result = securityService.getCurrentUser();
 
         assertNull(result, "User not null");
         verify(securityContext).getAuthentication();
         verify(userDao, never()).getByUsername(USERNAME);
     }
 
     @Test
     public void testGetCurrentUserUsername() throws Exception {
         User user = getUser();
         Authentication auth = mock(Authentication.class);
         when(auth.getPrincipal()).thenReturn(user);
         when(securityContext.getAuthentication()).thenReturn(auth);
 
         String username = securityService.getCurrentUserUsername();
 
         assertEquals(username, USERNAME, "Username not equals");
         verify(auth).getPrincipal();
         verify(securityContext).getAuthentication();
     }
 
     @Test
     public void testGetCurrentUserUsernamePrincipal() throws Exception {
         Principal user = new PrincipalImpl(USERNAME);
         Authentication auth = mock(Authentication.class);
         when(auth.getPrincipal()).thenReturn(user);
         when(securityContext.getAuthentication()).thenReturn(auth);
 
         String username = securityService.getCurrentUserUsername();
 
         assertEquals(username, USERNAME, "Username not equals");
         verify(auth).getPrincipal();
         verify(securityContext).getAuthentication();
     }
 
     @Test
     public void testGetCurrentUserUsernameWithoutAuthentication() throws Exception {
         when(securityContext.getAuthentication()).thenReturn(null);
 
         String username = securityService.getCurrentUserUsername();
 
         assertNull(username, "Username not null");
         verify(securityContext).getAuthentication();
     }
 
     @Test
     public void testGetCurrentUserUsernameAnonymousUser() throws Exception {
         Principal user = new PrincipalImpl(SecurityConstants.ANONYMOUS_USERNAME);
         Authentication auth = mock(Authentication.class);
         when(auth.getPrincipal()).thenReturn(user);
         when(securityContext.getAuthentication()).thenReturn(auth);
 
         String username = securityService.getCurrentUserUsername();
 
         assertNull(username, "Username not null");
         verify(auth).getPrincipal();
         verify(securityContext).getAuthentication();
     }
 
     @Test
     public void testLoadUserByUsername() throws Exception {
         User user = getUser();
 
         when(userDao.getByUsername(USERNAME)).thenReturn(user);
 
         UserDetails result = securityService.loadUserByUsername(USERNAME);
 
         assertEquals(result.getUsername(), USERNAME, "Username not equals");
         verify(userDao).getByUsername(USERNAME);
     }
 
     @Test(expectedExceptions = UsernameNotFoundException.class)
     public void testLoadUserByUsernameNotFound() throws Exception {
         when(userDao.getByUsername(USERNAME)).thenReturn(null);
 
         securityService.loadUserByUsername(USERNAME);
     }
 
     @Test
     public void testDeleteFromAcl() throws Exception {
         long entityId = 1L;
         TestEntity object = new TestEntity();
         object.setId(entityId);
 
         securityService.deleteFromAcl(object);
 
         verify(aclManager).deleteFromAcl(TestEntity.class, entityId);
     }
 
     @Test
     public void testDeleteFromAclByClassAndId() throws Exception {
         long entityId = 1L;
         securityService.deleteFromAcl(TestEntity.class, entityId);
 
         verify(aclManager).deleteFromAcl(TestEntity.class, entityId);
     }
 
     @Test
     public void testGrantToCurrentUser() throws Exception {
         mockCurrentUserPrincipal();
 
         AclBuilder builder = securityService.grantToCurrentUser();
 
         assertTrue(builder.containsSid(USERNAME));
     }
 
 
     @Test
     public void testGrant() {
         AclBuilder builder = securityService.grant();
 
         assertNotNull(builder);
         assertTrue(builder.getSids().isEmpty());
     }
 
     @Test
     public void testDelete() {
         AclBuilder builder = securityService.delete();
 
         assertNotNull(builder);
         assertTrue(builder.getSids().isEmpty());
     }
 
 
     @Test
     public void testRevoke() {
         AclBuilder builder = securityService.revoke();
 
         assertNotNull(builder);
         assertTrue(builder.getSids().isEmpty());
     }
 
     @Test
     public void testRevokeFromCurrentUser() {
         mockCurrentUserPrincipal();
         AclBuilder builder = securityService.revokeFromCurrentUser();
 
         assertNotNull(builder);
         assertFalse(builder.getSids().isEmpty());
     }
 
     private void mockCurrentUserPrincipal() {
         Principal user = new PrincipalImpl(USERNAME);
         Authentication auth = mock(Authentication.class);
         when(auth.getPrincipal()).thenReturn(user);
         when(securityContext.getAuthentication()).thenReturn(auth);
     }
 
 }
