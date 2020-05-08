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
 
 package org.patientview.test.repository;
 
 import org.patientview.patientview.model.JoinRequest;
 import org.patientview.repository.JoinRequestDao;
 import org.junit.Test;
 
 import javax.inject.Inject;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 public class JoinRequestDaoTest extends BaseDaoTest {
 
     @Inject
     JoinRequestDao joinRequestDao;
 
     @Test
     public void testAddGetJoinRequest() throws Exception {
         JoinRequest joinRequest = getTestObject();
 
         /**
          * add
          */
         joinRequestDao.save(joinRequest);
         assertTrue("Can't save joinRequest", joinRequest.getId() > 0);
     }
 
     private JoinRequest getTestObject() throws Exception {
         JoinRequest joinRequest = new JoinRequest();
 
         joinRequest.setFirstName("Jack");
         joinRequest.setLastName("London");
         joinRequest.setDateOfBirth(new Date());
         joinRequest.setEmail("jack@london.com");
         joinRequest.setNhsNo("9434765919");
         joinRequest.setUnitcode("SNC01");
        joinRequest.setDateOfRequest(new Date());
 
         return joinRequest;
     }
 
     @Test
     public void testGetJoinRequestList() throws Exception {
         JoinRequest joinRequest = new JoinRequest();
         JoinRequest joinRequest2 = getTestObject();
 
         joinRequest.setFirstName("testName");
         joinRequest.setLastName("London");
         joinRequest.setDateOfBirth(new Date());
         joinRequest.setEmail("test@london.com");
         joinRequest.setNhsNo("9876543210");
         joinRequest.setUnitcode("SNC01");
         joinRequest.setComplete(true);
         joinRequest.setNotes("test notes");
        joinRequest.setDateOfRequest(new Date());
 
         joinRequestDao.save(joinRequest);
         joinRequestDao.save(joinRequest2);
 
         List<String> unitCodes = new ArrayList<String>();
         unitCodes.add("SNC01");
 
         List<JoinRequest> allList = joinRequestDao.getJoinRequestsForUnitCodes(unitCodes);
         assertTrue("Can't save joinRequest", allList.size() == 2);
 
         List<JoinRequest> inCompleteList = joinRequestDao.getJoinRequestsForUnitCodes(unitCodes, false);
         assertTrue("Can't get the incomplete joinRequest list", inCompleteList.size() == 1);
         assertEquals("Get wrong incomplete joinRequest", "Jack", inCompleteList.get(0).getFirstName());
 
         List<JoinRequest> completeList = joinRequestDao.getJoinRequestsForUnitCodes(unitCodes, true);
         assertTrue("Can't get the complete joinRequest list", completeList.size() == 1);
         assertEquals("Get wrong complete joinRequest", "testName", completeList.get(0).getFirstName());
 
     }
 
     @Test
     public void testGetAllJoinRequestList() throws Exception {
         JoinRequest joinRequest = new JoinRequest();
         JoinRequest joinRequest2 = getTestObject();
 
         joinRequest.setFirstName("testName");
         joinRequest.setLastName("London");
         joinRequest.setDateOfBirth(new Date());
         joinRequest.setEmail("test@london.com");
         joinRequest.setNhsNo("9876543210");
         joinRequest.setUnitcode("SNC01");
         joinRequest.setComplete(true);
         joinRequest.setNotes("test notes");
        joinRequest.setDateOfRequest(new Date());
 
         joinRequestDao.save(joinRequest);
         joinRequestDao.save(joinRequest2);
 
         List<JoinRequest> allList = joinRequestDao.getAll();
         assertNotNull("Couldn't get the joinRequests", allList);
         assertTrue("Can't save joinRequest", allList.size() == 2);
 
         List<JoinRequest> inCompleteList = joinRequestDao.getAll(false);
         assertTrue("Can't get the incomplete joinRequest list", inCompleteList.size() == 1);
         assertEquals("Get wrong incomplete joinRequest", "Jack", inCompleteList.get(0).getFirstName());
 
         List<JoinRequest> completeList = joinRequestDao.getAll(true);
         assertTrue("Can't get the complete joinRequest list", completeList.size() == 1);
         assertEquals("Get wrong complete joinRequest", "testName", completeList.get(0).getFirstName());
 
     }
 
 }
