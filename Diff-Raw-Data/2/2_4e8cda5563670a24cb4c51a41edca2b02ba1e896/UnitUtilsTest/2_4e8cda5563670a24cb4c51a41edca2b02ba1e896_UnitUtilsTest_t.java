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
 
 package org.patientview.test.utils;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.Before;
 import org.junit.Test;
 import org.patientview.patientview.logon.UnitAdmin;
 import org.patientview.patientview.model.Specialty;
 import org.patientview.patientview.model.Unit;
 import org.patientview.patientview.model.User;
 import org.patientview.patientview.model.UserMapping;
 import org.patientview.patientview.unit.UnitUtils;
 import org.patientview.repository.UnitDao;
 import org.patientview.repository.UserMappingDao;
 import org.patientview.service.*;
 import org.patientview.test.helpers.SecurityHelpers;
 import org.patientview.test.helpers.ServiceHelpers;
 import org.patientview.test.service.BaseServiceTest;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 public class UnitUtilsTest extends BaseServiceTest {
 
     @Inject
     private ServiceHelpers serviceHelpers;
 
     @Inject
     private SecurityUserManager securityUserManager;
 
     @Inject
     private SecurityHelpers securityHelpers;
 
     @Inject
     private UnitDao unitDao;
 
     @Inject
     private UserMappingDao userMappingDao;
 
     // Test suite wide references
     private User unitadmin, superadmin,radaradmin,comboAdmin;
     private Specialty specialty1;
 
     /**
      * create users and units, set relationships user and unit.
      * 4 users('unitadmin', 'superadmin', 'radaradmin', 'comboAdmin'),
      * 4 units('RADAR1', 'RADAR2', 'RENAL1', 'RENAL2') and 1 specialty
      * 'radaradmin' is in 'RADAR1' unit, 'comboAdmin' in 'RADAR1', 'RENAL1' and 'RENAL2' units, 'unitadmin' in 'RENAL2'
      */
     @Before
     public void initDatas() {
         unitadmin = serviceHelpers.createUser("unitadmin", "username@test.com", "pass", "Test User");
         superadmin = serviceHelpers.createUser("superadmin", "username@test.com", "pass", "Test User");
         radaradmin = serviceHelpers.createUser("radaradmin", "username@test.com", "pass", "Test User");
         comboAdmin = serviceHelpers.createUser("comboAdmin", "username@test.com", "pass", "Test User");
 
         specialty1 = serviceHelpers.createSpecialty("Specialty 1", "Specialty1", "Test description");
 
         serviceHelpers.createSpecialtyUserRole(specialty1, superadmin, "superadmin");
         serviceHelpers.createSpecialtyUserRole(specialty1, unitadmin, "unitadmin");
         serviceHelpers.createSpecialtyUserRole(specialty1, comboAdmin, "unitadmin");
         serviceHelpers.createSpecialtyUserRole(specialty1, radaradmin, "radaradmin");
 
         UserMapping userMapping = new UserMapping();
         userMapping.setNhsno("123");
         userMapping.setUsername("radaradmin");
         userMapping.setSpecialty(specialty1);
         userMapping.setUnitcode("RADAR1");
         userMappingDao.save(userMapping);
 
         userMapping = new UserMapping();
         userMapping.setNhsno("1234");
         userMapping.setSpecialty(specialty1);
         userMapping.setUsername("comboAdmin");
         userMapping.setUnitcode("RADAR1");
         userMappingDao.save(userMapping);
         userMapping = new UserMapping();
         userMapping.setNhsno("12345");
         userMapping.setSpecialty(specialty1);
         userMapping.setUsername("comboAdmin");
         userMapping.setUnitcode("RENAL2");
         userMappingDao.save(userMapping);
         userMapping = new UserMapping();
         userMapping.setNhsno("12346");
         userMapping.setSpecialty(specialty1);
         userMapping.setUsername("comboAdmin");
         userMapping.setUnitcode("RENAL1");
         userMappingDao.save(userMapping);
 
         userMapping = new UserMapping();
         userMapping.setNhsno("12347");
         userMapping.setSpecialty(specialty1);
         userMapping.setUsername("unitadmin");
         userMapping.setUnitcode("RENAL2");
         userMappingDao.save(userMapping);
 
         Unit unit = new Unit();
         unit.setSpecialty(specialty1);
         // required fields
         unit.setUnitcode("RADAR1");
         unit.setName("z");
         unit.setShortname("nam1");
         unit.setSourceType("radargroup");
         unitDao.save(unit);
 
         unit = new Unit();
         unit.setSpecialty(specialty1);
         // required fields
         unit.setUnitcode("RADAR2");
         unit.setName("y");
         unit.setShortname("nam2");
         unit.setSourceType("radargroup");
         unitDao.save(unit);
 
         unit = new Unit();
         unit.setSpecialty(specialty1);
         // required fields
         unit.setUnitcode("RENAL1");
         unit.setName("x");
         unit.setShortname("nam3");
         unit.setSourceType("renalunit");
         unitDao.save(unit);
 
         unit = new Unit();
         unit.setSpecialty(specialty1);
         // required fields
         unit.setUnitcode("RENAL2");
         unit.setName("w");
         unit.setShortname("nam4");
         unit.setSourceType("renalunit");
         unitDao.save(unit);
     }
 
 
     /**
      * test UnitUtils.setUserUnits method, this method is used by LogonAddInputAction and UnitAdminAddAction.
      * this method will search units depend on login user's role and sourceType of unit('radargroup', 'renalunit').
      * superadmin will get all 'radargroup' and 'renalunit' units, unitadmin will get all units which he belongs to,
      * other role user won't get any unit, like radaradmin.
      * searching result will be as a attribute in request.
      */
     @Test
     public void testSetUserUnits() {
         //superadmin will get all 4 units.
         loginAsUser(superadmin.getUsername(), specialty1);
         assertTrue("superadmin should be present", securityUserManager.isRolePresent("superadmin"));
         MockHttpServletRequest request = new MockHttpServletRequest();
         UnitUtils.setUserUnits(request);
         List<Unit> units = (List<Unit>) request.getAttribute("units");
         assertEquals("units size is wrong.", 4, units.size());
         List<String> unitcodes = new ArrayList<String>();
         for (Unit unit : units) {
             unitcodes.add(unit.getUnitcode());
         }
         assertEquals("searching result[" + unitcodes.toString() +
                 "] is wrong", 0,
                 CollectionUtils.subtract(Arrays.asList(new String[]{"RADAR1", "RADAR2", "RENAL1", "RENAL2"}),
                         unitcodes).size());
         logout();
 
         // unitadmin will get one unit.
         loginAsUser(unitadmin.getUsername(), specialty1);
         assertTrue("unitadmin should be present", securityUserManager.isRolePresent("unitadmin"));
         request = new MockHttpServletRequest();
         UnitUtils.setUserUnits(request);
         List<Unit> unitadminUnits = (List<Unit>) request.getAttribute("units");
         assertEquals("units size is wrong.", 1, ((List)request.getAttribute("units")).size());
         assertEquals("searching result is wrong", "RENAL2", unitadminUnits.get(0).getUnitcode());
         logout();
 
         // comboAdmin will get 3 units.
         loginAsUser(comboAdmin.getUsername(), specialty1);
         assertTrue("comboAdmin should be present", securityUserManager.isRolePresent("unitadmin"));
         request = new MockHttpServletRequest();
         UnitUtils.setUserUnits(request);
         List<Unit> comboAdminUnits = (List<Unit>) request.getAttribute("units");
         assertEquals("units size is wrong.",3, comboAdminUnits.size());
         List<String> comboAdminUnitcodes = new ArrayList<String>();
         for (Unit unit : comboAdminUnits) {
             comboAdminUnitcodes.add(unit.getUnitcode());
         }
         assertEquals("searching result is wrong", 0,
                 CollectionUtils.subtract(Arrays.asList(new String[]{"RADAR1", "RENAL1", "RENAL2"}),
                         comboAdminUnitcodes).size());
         logout();
 
         //radaradmin won't get any unit.
         loginAsUser(radaradmin.getUsername(), specialty1);
         assertTrue("radaradmin should be present", securityUserManager.isRolePresent("radaradmin"));
         request = new MockHttpServletRequest();
         UnitUtils.setUserUnits(request);
        assertEquals("units size is wrong.", 0, ((List) request.getAttribute("units")).size());
         logout();
     }
 
     /**
      * test UnitUtils.setUserRenalUnits method, this method is used by PatientAddInputAction.
      * this method will search units depend on login user's role and 'renalunit' unit
      * superadmin will get all 'renalunit' units, unitadmin will get all 'renalunit' units which he belongs to,
      * other role user won't get any unit, like radaradmin.
      * searching result will be as a attribute in request.
      */
     @Test
     public void testSetUserRenalUnits() {
 
         //superadmin will get 2 units.
         loginAsUser(superadmin.getUsername(), specialty1);
         assertTrue("superadmin should be present", securityUserManager.isRolePresent("superadmin"));
         MockHttpServletRequest request = new MockHttpServletRequest();
         UnitUtils.setUserRenalUnits(request);
         List<Unit> units = (List<Unit>) request.getAttribute("units");
         assertEquals("units size is wrong.", 2, units.size());
         List<String> unitcodes = new ArrayList<String>();
         for (Unit unit : units) {
             unitcodes.add(unit.getUnitcode());
         }
         assertEquals("searching result is wrong", 0,
                 CollectionUtils.subtract(Arrays.asList(new String[]{"RENAL1", "RENAL2"}),
                         unitcodes).size());
         logout();
 
         // unitadmin will get one unit.
         loginAsUser(unitadmin.getUsername(), specialty1);
         assertTrue("unitadmin should be present", securityUserManager.isRolePresent("unitadmin"));
         request = new MockHttpServletRequest();
         UnitUtils.setUserRenalUnits(request);
         List<Unit> unitadminUnits = (List<Unit>) request.getAttribute("units");
         assertEquals("units size is wrong.", 1, ((List)request.getAttribute("units")).size());
         assertEquals("searching result is wrong", "RENAL2", unitadminUnits.get(0).getUnitcode());
         logout();
 
         // comboAdmin will get 2 units.
         loginAsUser(comboAdmin.getUsername(), specialty1);
         assertTrue("comboAdmin should be present", securityUserManager.isRolePresent("unitadmin"));
         request = new MockHttpServletRequest();
         UnitUtils.setUserRenalUnits(request);
         List<Unit> comboAdminUnits = (List<Unit>) request.getAttribute("units");
         assertEquals("units size is wrong.",2, comboAdminUnits.size());
         List<String> comboAdminUnitcodes = new ArrayList<String>();
         for (Unit unit : comboAdminUnits) {
             comboAdminUnitcodes.add(unit.getUnitcode());
         }
         assertEquals("searching result is wrong", 0,
                 CollectionUtils.subtract(Arrays.asList(new String[]{"RENAL1", "RENAL2"}),
                         comboAdminUnitcodes).size());
         logout();
 
         //radaradmin won't get any unit.
         loginAsUser(radaradmin.getUsername(), specialty1);
         assertTrue("radaradmin should be present", securityUserManager.isRolePresent("radaradmin"));
         request = new MockHttpServletRequest();
         UnitUtils.setUserRenalUnits(request);
         assertEquals("units size is wrong.", null, request.getAttribute("units"));
         logout();
     }
 
 
 
     private void loginAsUser(String username, Specialty specialty) {
         securityHelpers.loginAsUser(username, specialty);
     }
 
     private void logout() {
         securityHelpers.logout();
     }
 }
