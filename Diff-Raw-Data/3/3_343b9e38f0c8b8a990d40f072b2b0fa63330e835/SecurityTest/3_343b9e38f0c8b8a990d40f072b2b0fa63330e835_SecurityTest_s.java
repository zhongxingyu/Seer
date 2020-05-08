 package com.worthsoln.test.security;
 
 import com.worthsoln.patientview.model.Tenancy;
 import com.worthsoln.patientview.model.User;
 import com.worthsoln.security.model.SecurityUser;
 import com.worthsoln.service.SecurityUserManager;
 import com.worthsoln.test.helpers.RepositoryHelpers;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.inject.Inject;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 /**
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:spring-context.xml", "classpath:test-context.xml"})
 @Transactional
 public class SecurityTest {
 
     @Inject
     private RepositoryHelpers repositoryHelpers;
 
     @Inject
     private SecurityUserManager securityUserManager;
 
     @Inject
     private UserDetailsService userDetailsService;
 
     // Test suite wide references
     private User user;
     Tenancy tenancy1, tenancy2, tenancy3;
 
     @Before
     public void setupTenancies() {
         user = repositoryHelpers.createUser("Username", "username@test.com", "pass", "Test User", "Testy");
 
         tenancy1 = repositoryHelpers.createTenancy("Tenant 1", "ten1", "Test description");
         tenancy2 = repositoryHelpers.createTenancy("Tenant 2", "ten2", "Test description 2");
         tenancy3 = repositoryHelpers.createTenancy("Tenant 3", "ten3", "Test description 3");   // no access
 
         repositoryHelpers.createTenancyUserRole(tenancy1, user, "patient");
         repositoryHelpers.createTenancyUserRole(tenancy2, user, "admin");
     }
 
 
     @Test
     public void testGetSecurityUserFromUsername() {
 
         UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
 
         assertTrue("Security User is null", userDetails != null);
         assertEquals("Username is incorrect", user.getUsername(), userDetails.getUsername());
 
         Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
 
        assertEquals("Incorrect number of roles", 2, authorities.size());
 
         Set<GrantedAuthority> authoritySet = new HashSet<GrantedAuthority>(authorities);
         GrantedAuthority grantedAuthority1 = new SimpleGrantedAuthority("ROLE_TEN2_ADMIN");
         GrantedAuthority grantedAuthority2 = new SimpleGrantedAuthority("ROLE_TEN1_PATIENT");
 
         assertTrue("Incorrect authority", authoritySet.contains(grantedAuthority1));
         assertTrue("Incorrect authority", authoritySet.contains(grantedAuthority2));
     }
 
     @Test
     public void testIsRolePresentForSecurityUser() {
 
         loginAsUser(user.getUsername(), tenancy1);
 
         assertTrue("Patient should be present", securityUserManager.isRolePresent("patient"));
 
         assertFalse("Admin should not be present", securityUserManager.isRolePresent("admin"));
 
         logout();
 
         loginAsUser(user.getUsername(), tenancy2);
 
         assertFalse("Patient should not be present", securityUserManager.isRolePresent("patient"));
 
         assertTrue("Admin should be present", securityUserManager.isRolePresent("admin"));
     }
 
     @Test
     public void testGetLoggedInTenancy() {
         loginAsUser(user.getUsername(), tenancy1);
 
         assertEquals("Incorrect logged in tenancy", tenancy1, securityUserManager.getLoggedInTenancy());
     }
 
     @Test
     public void testSetLoggedInTenancy() throws Exception {
 
         // first login without setting the tenancy
         loginAsUser(user.getUsername());
 
         assertNull("Tenancy found in session", securityUserManager.getLoggedInTenancy());
 
         securityUserManager.setLoggedInTenancy(tenancy1.getId());
 
         assertEquals("Incorrect logged in tenancy", tenancy1, securityUserManager.getLoggedInTenancy());
     }
 
     @Test(expected = IllegalStateException.class)
     public void testCannotSetLoggedInTenancyWhenNoAccess() throws Exception {
 
         // first login without setting the tenancy
         loginAsUser(user.getUsername());
 
         assertNull("Tenancy found in session", securityUserManager.getLoggedInTenancy());
 
         securityUserManager.setLoggedInTenancy(tenancy3.getId());
     }
 
     @Test
     public void testUserGetsAnyUserRole() {
 
         loginAsUser(user.getUsername());
 
         assertTrue("User does not have ANY_USER role", securityUserManager.isRolePresent("any_user"));
     }
 
     @Test
     public void testIsTenancyPresent() {
         loginAsUser(user.getUsername(), tenancy1);
 
         assertTrue("Tenancy is not present", securityUserManager.isTenancyPresent(tenancy1.getContext()));
     }
 
 
     private void loginAsUser(String username, Tenancy tenancy) {
         SecurityUser user = (SecurityUser) userDetailsService.loadUserByUsername(username);
         user.setTenancy(tenancy);
 
         SecurityContextHolder.getContext().setAuthentication(
                 new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
     }
 
     private void loginAsUser(String username) {
         SecurityUser user = (SecurityUser) userDetailsService.loadUserByUsername(username);
 
         SecurityContextHolder.getContext().setAuthentication(
                 new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
     }
 
     private void logout() {
         SecurityContextHolder.getContext().setAuthentication(null);
     }
 }
