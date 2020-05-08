 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.test.security;
 
 import org.patientview.patientview.model.Specialty;
 import org.patientview.patientview.model.Unit;
 import org.patientview.patientview.model.User;
 import org.patientview.patientview.model.Feedback;
 import org.patientview.patientview.model.UserMapping;
 import org.patientview.service.*;
 import org.patientview.test.helpers.SecurityHelpers;
 import org.patientview.test.helpers.ServiceHelpers;
 import org.patientview.test.service.BaseServiceTest;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 
 import javax.inject.Inject;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 /**
  *  Test the core functions of the security user manager and spring security context.
  *
  *  Note: this operates as per the service tests transaction-wise
  */
 public class SecurityTest extends BaseServiceTest {
 
     @Inject
     private ServiceHelpers serviceHelpers;
 
     @Inject
     private SecurityUserManager securityUserManager;
 
     @Inject
     private FeedbackManager feedbackManager;
 
     @Inject
     private SecurityHelpers securityHelpers;
 
     @Inject
     private UserDetailsService userDetailsService;
 
     @Inject
     private UserManager userManager;
 
     @Inject
     private UnitManager unitManager;
 
     @Inject
     private PatientManager patientManager;
 
     // Test suite wide references
     private User user;
     Specialty specialty1, specialty2, specialty3;
 
     @Before
     public void setupSpecialties() {
         user = serviceHelpers.createUser("Username", "username@test.com", "pass", "Test User");
 
         specialty1 = serviceHelpers.createSpecialty("Specialty 1", "Specialty1", "Test description");
         specialty2 = serviceHelpers.createSpecialty("Specialty 2", "Specialty2", "Test description 2");
         specialty3 = serviceHelpers.createSpecialty("Specialty 3", "Specialty3", "Test description 3");   // no access
 
         serviceHelpers.createSpecialtyUserRole(specialty1, user, "patient");
         serviceHelpers.createSpecialtyUserRole(specialty2, user, "admin");
     }
 
 
     @Test
     public void testGetSecurityUserFromUsername() {
 
         UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
 
         assertTrue("Security User is null", userDetails != null);
         assertEquals("Username is incorrect", user.getUsername(), userDetails.getUsername());
 
         Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
 
         // NOTE: there is an extra role in here called ROLE_ANY_USER, hence 3 rather than 2
         assertEquals("Incorrect number of roles", 3, authorities.size());
 
         Set<GrantedAuthority> authoritySet = new HashSet<GrantedAuthority>(authorities);
         GrantedAuthority grantedAuthority1 = new SimpleGrantedAuthority("ROLE_SPECIALTY2_ADMIN");
         GrantedAuthority grantedAuthority2 = new SimpleGrantedAuthority("ROLE_SPECIALTY1_PATIENT");
 
         assertTrue("Incorrect authority", authoritySet.contains(grantedAuthority1));
         assertTrue("Incorrect authority", authoritySet.contains(grantedAuthority2));
     }
 
     @Test
     public void testIsRolePresentForSecurityUser() {
 
         loginAsUser(user.getUsername(), specialty1);
 
         assertTrue("Patient should be present", securityUserManager.isRolePresent("patient"));
 
         assertFalse("Admin should not be present", securityUserManager.isRolePresent("admin"));
 
         logout();
 
         loginAsUser(user.getUsername(), specialty2);
 
         assertFalse("Patient should not be present", securityUserManager.isRolePresent("patient"));
 
         assertTrue("Admin should be present", securityUserManager.isRolePresent("admin"));
     }
 
     @Test
     public void testGetLoggedInSpecialty() {
         loginAsUser(user.getUsername(), specialty1);
 
         assertEquals("Incorrect logged in specialty", specialty1, securityUserManager.getLoggedInSpecialty());
     }
 
     @Test
     public void testSetLoggedInSpecialty() throws Exception {
 
         // first login without setting the specialty
         loginAsUser(user.getUsername());
 
         assertNull("Specialty found in session", securityUserManager.getLoggedInSpecialty());
 
         securityUserManager.setLoggedInSpecialty(specialty1.getId());
 
         assertEquals("Incorrect logged in specialty", specialty1, securityUserManager.getLoggedInSpecialty());
     }
 
     @Test(expected = IllegalStateException.class)
     public void testCannotSetLoggedInSpecialtyWhenNoAccess() throws Exception {
 
         // first login without setting the specialty
         loginAsUser(user.getUsername());
 
         assertNull("Specialty found in session", securityUserManager.getLoggedInSpecialty());
 
         securityUserManager.setLoggedInSpecialty(specialty3.getId());
     }
 
     @Test
     public void testUserGetsAnyUserRole() {
 
         loginAsUser(user.getUsername());
 
         assertTrue("User does not have ANY_USER role", securityUserManager.isRolePresent("any_user"));
     }
 
     @Test
     public void testIsSpecialtyPresent() {
         loginAsUser(user.getUsername(), specialty1);
 
         assertTrue("Specialty is not present", securityUserManager.isSpecialtyPresent(specialty1.getContext()));
     }
 
     @Test
     public void testGetLoggedInUserRole() {
         loginAsUser(user.getUsername(), specialty2);
 
         assertEquals("Incorrect logged in user role", "admin", userManager.getLoggedInUserRole());
     }
 
     @Test
     public void testGetCurrentSpecialtyRole() {
         loginAsUser(user.getUsername(), specialty2);
 
         assertEquals("Incorrect logged in user role", "admin", userManager.getCurrentSpecialtyRole(user));
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testGetUnit() {
 
         User user1 = serviceHelpers.createUserWithMapping("testuser1", "paul@test.com", "p", "Testuser1", "UNITCODEA",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, user1, "unitadmin");
         loginAsUser(user1.getUsername(), specialty2);
 
         Unit unitRm301 = new Unit();
         unitRm301.setUnitcode("UNITCODEA");
         unitRm301.setName("RM301: RUNNING MAN TEST UNIT");
         unitRm301.setShortname("RM301");
         unitRm301.setRenaladminemail("renaladmin@mailinator.com");
         unitRm301.setSpecialty(specialty2);
         unitManager.save(unitRm301);
 
         List<Unit> units = unitManager.getUsersUnits(user1);
         //List<UserMapping> userMappings = userManager.getUserMappings(user1.getUsername());
         assertEquals("Wrong number of login user's unit size ", 1, units.size());
         assertEquals("Unit code is wrong", "UNITCODEA", units.get(0).getUnitcode());
 
         Unit checkValidUnit = unitManager.get("UNITCODEA");
         assertNotNull(checkValidUnit);
         assertEquals("Got the Invalid unit ", checkValidUnit.getUnitcode(), "UNITCODEA");
 
         // request the invalid unit
         Unit checkInvalidUnit = unitManager.get("testunit");
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testGetUnitUser() {
 
         User adminUser = serviceHelpers.createUserWithMapping("adminuser", "adminuser@test.com", "p", "Adminuser", "UNITCODEA",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, adminUser, "unitadmin");
 
         User validUser = serviceHelpers.createUserWithMapping("validUser", "validUser@test.com", "p", "ValidUser", "UNITCODEA",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, validUser, "unitadmin");
 
         User invalidUser = serviceHelpers.createUserWithMapping("invalidUser", "invalidUser@test.com", "p", "InvalidUser", "TestUnit",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, invalidUser, "unitadmin");
 
 
         loginAsUser(adminUser.getUsername(), specialty2);
 
         Unit validUnit = new Unit();
         validUnit.setUnitcode("UNITCODEA");
         validUnit.setName("UNITCODEA");
         validUnit.setShortname("UNITCODEA");
         validUnit.setRenaladminemail("test@mailinator.com");
         validUnit.setSpecialty(specialty2);
         unitManager.save(validUnit);
 
         Unit invalidUnit = new Unit();
         invalidUnit.setUnitcode("TestUnit");
         invalidUnit.setName("TestUnit");
         invalidUnit.setShortname("TestUnit");
         invalidUnit.setRenaladminemail("testunit@mailinator.com");
         invalidUnit.setSpecialty(specialty2);
         unitManager.save(invalidUnit);
 
         User checkValidUser = userManager.get(validUser.getUsername());
         assertNotNull(checkValidUser);
         assertEquals("Got the Invalid user ", checkValidUser.getUsername(), "validUser");
 
         // request the invalid user
         User checkInvalidUser = userManager.get(invalidUser.getUsername());
     }
 
     @Test
     public void testGetValidUnitUser() {
 
         User adminUser = serviceHelpers.createUserWithMapping("adminuser", "adminuser@test.com", "p", "Adminuser", "UNITCODEA",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, adminUser, "unitadmin");
 
         User validUser = serviceHelpers.createUserWithMapping("validUser", "validUser@test.com", "p", "ValidUser", "UNITCODEA",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, validUser, "unitadmin");
 
         loginAsUser(adminUser.getUsername(), specialty2);
 
         Unit validUnit = new Unit();
         validUnit.setUnitcode("UNITCODEA");
         validUnit.setName("UNITCODEA");
         validUnit.setShortname("UNITCODEA");
         validUnit.setRenaladminemail("test@mailinator.com");
         validUnit.setSpecialty(specialty2);
         unitManager.save(validUnit);
 
         User checkValidUser = userManager.get(validUser.getUsername());
         assertNotNull(checkValidUser);
         assertEquals("Got the Invalid user ", checkValidUser.getUsername(), "validUser");
 
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testGetAllUnitUsersWithUnitAdmin() {
         User unitadmin = serviceHelpers.createUserWithMapping("testuser1", "paul@test.com", "p", "Testuser1", "UNITCODEA",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, unitadmin, "unitadmin");
         loginAsUser(unitadmin.getUsername(), specialty2);
         unitManager.getAllUnitUsers();
 
     }
 
     @Test
     public void testGetAllUnitUsersWithSuperAdmin() {
         User superadmin = serviceHelpers.createUser("superadminname", "superadminname@test.com", "p", "Testuser1");
 
         Specialty specialty = serviceHelpers.createSpecialty("RENAL", "RENAL", "Test RENAL");
 
         serviceHelpers.createSpecialtyUserRole(specialty, superadmin, "superadmin");
         loginAsUser(superadmin.getUsername(), specialty);
 
        unitManager.getAllUnitUsers();
     }
 
     @Test(expected = AccessDeniedException.class)
     public void testGetAllUnitPatientsWithTreatmentWithUnitAdmin() {
         User unitadmin = serviceHelpers.createUserWithMapping("testuser1", "paul@test.com", "p", "Testuser1", "UNITCODEA",
                 "nhs1", specialty2);
         serviceHelpers.createSpecialtyUserRole(specialty2, unitadmin, "unitadmin");
         loginAsUser(unitadmin.getUsername(), specialty2);
 
         patientManager.getAllUnitPatientsWithTreatment("", "", true);
 
     }
 
     @Test
     public void testGetAllUnitPatientsWithTreatmentWithSuperAdmin() {
         User superadmin = serviceHelpers.createUser("superadminname", "superadminname@test.com", "p", "Testuser1");
 
         Specialty specialty = serviceHelpers.createSpecialty("RENAL", "RENAL", "Test RENAL");
 
         serviceHelpers.createSpecialtyUserRole(specialty, superadmin, "superadmin");
         loginAsUser(superadmin.getUsername(), specialty);
 
         patientManager.getAllUnitPatientsWithTreatment("", "", true);
     }
 
     private void loginAsUser(String username, Specialty specialty) {
         securityHelpers.loginAsUser(username, specialty);
     }
 
     private void loginAsUser(String username) {
         securityHelpers.loginAsUser(username);
     }
 
     private void logout() {
         securityHelpers.logout();
     }
 }
