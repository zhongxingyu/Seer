 package org.spring.security.preauthorize;
 
 import org.junit.After;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
 import org.springframework.security.authentication.TestingAuthenticationToken;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.authority.AuthorityUtils;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:app-context.xml")
 public class SecuredServiceImplTest {
 
     @Autowired
     private SecuredService securedService;
 
     @Test
     public void adminAccess() {
         Authentication admin =
                 new UsernamePasswordAuthenticationToken("admin", "admin", AuthorityUtils.createAuthorityList(
                         "ROLE_ADMIN"));
         SecurityContextHolder.getContext().setAuthentication(admin);
         securedService.securedMethod();
     }
 
     @Test(expected = AccessDeniedException.class)
     public void userAccess() {
         Authentication user =
                 new UsernamePasswordAuthenticationToken("user", "user", AuthorityUtils.createAuthorityList(
                         "ROLE_USER"));
         SecurityContextHolder.getContext().setAuthentication(user);
         securedService.securedMethod();
     }
 
     @Test(expected = AuthenticationCredentialsNotFoundException.class)
     public void testSecuredClassNotAuthenticated() throws Exception {
         securedService.securedMethod();
     }
 
 
     @Test
     public void itemReadAccessForUser() {
         Authentication user = new UsernamePasswordAuthenticationToken("user", "user", AuthorityUtils.NO_AUTHORITIES);
         SecurityContextHolder.getContext().setAuthentication(user);
         // user has read access for the Page domain object with id 1
         securedService.getItem(1L);
     }
 
     @Test
     public void itemReadHierarchyPermissionForAdmin() {
         Authentication admin = new UsernamePasswordAuthenticationToken("admin", "admin", AuthorityUtils.NO_AUTHORITIES);
         SecurityContextHolder.getContext().setAuthentication(admin);
         // admin has read access indirectly to a Widget domain object with id 3 (without having an acl entry)
         // because that widget is in a parent-child relationship (through Container with id 2)
         // with a Page domain object with id 1 to which admin has direct read access (having an acl entry)
         // and the inherit flag is set to true on Widget(id=3) and Container(id=2)
         securedService.getPageItem(3L);
     }
 
     @Test(expected = AccessDeniedException.class)
     public void writePermissionForUserWithoutGrantingACE() {
         Authentication user = new UsernamePasswordAuthenticationToken("user", "user", AuthorityUtils.NO_AUTHORITIES);
         SecurityContextHolder.getContext().setAuthentication(user);
        // user has a acl entry for Page object with id 1, but the granting flag of acl entry is set to false)
         securedService.getPage(1L);
     }
 
 
     @After
     public void tearDown() {
         SecurityContextHolder.clearContext();
     }
 
 }
