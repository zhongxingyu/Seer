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
 
import org.junit.Test;
import org.patientview.model.Specialty;
import org.patientview.model.Unit;
import org.patientview.patientview.model.ResultHeading;
import org.patientview.patientview.model.TestResult;
import org.patientview.patientview.model.TestResultWithUnitShortname;
import org.patientview.patientview.model.User;
 import org.patientview.repository.ResultHeadingDao;
 import org.patientview.repository.TestResultDao;
 import org.patientview.repository.UnitDao;
 import org.patientview.test.helpers.RepositoryHelpers;
 
 import javax.inject.Inject;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 public class TestResultDaoTest extends BaseDaoTest {
 
     @Inject
     TestResultDao testResultDao;
 
     @Inject
     private UnitDao unitDao;
 
     @Inject
     private ResultHeadingDao resultHeadingDao;
 
     @Inject
     private RepositoryHelpers repositoryHelpers;
 
     @Test
     public void testAddGetTestResult() throws Exception {
         TestResult testResult = getTestObject();
 
         /**
          * add
          */
         testResultDao.save(testResult);
         assertTrue("Can't save testResult", testResult.getId() > 0);
 
         /**
          * get
          */
         List<TestResult> savedTestResults = testResultDao.get(testResult.getNhsno(), testResult.getUnitcode());
         assertTrue("Can't get testResults", savedTestResults.size() > 0);
     }
 
     @Test
     public void testDeleteTestResult() throws Exception {
         TestResult testResult = getTestObject();
 
         /**
          * add
          */
         testResultDao.save(testResult);
         assertTrue("Can't save testResult", testResult.getId() > 0);
 
         /**
          * delete
          */
         testResultDao.deleteTestResults(testResult.getNhsno(), testResult.getUnitcode());
 
         List<TestResult> savedTestResults = testResultDao.get(testResult.getNhsno(), testResult.getUnitcode());
         assertTrue("Can't delete testResults", savedTestResults.size() == 0);
 
         /**
          * delete within a time range
          */
         testResultDao.save(testResult);
         assertTrue("Can't save testResult", testResult.getId() > 0);
 
         testResultDao.deleteTestResultsWithinTimeRange(testResult.getNhsno(), testResult.getUnitcode(),
                 testResult.getTestcode(), getYesterday(), getTomorrow());
 
         savedTestResults = testResultDao.get(testResult.getNhsno(), testResult.getUnitcode());
         assertTrue("Can't delete testResults within a time range", savedTestResults.size() == 0);
 
     }
 
     @Test
     public void testGetTestResults() throws Exception {
         Specialty specialty = repositoryHelpers.createSpecialty("Specialty1", "Specialty1", "A test specialty");
 
         Unit unit = new Unit();
         unit.setSpecialty(specialty);
         unit.setUnitcode("UNITCODEA");
         unit.setName("unit 1");
         unit.setShortname("unit 1");
         unitDao.save(unit);
 
         unit = new Unit();
         unit.setSpecialty(specialty);
         unit.setUnitcode("UNITCODEB");
         unit.setName("unit 2");
         unit.setShortname("unit 2");
         unitDao.save(unit);
 
         User user = repositoryHelpers.createUserWithMapping("testuser", "testuser@test.com", "p", "testuser", "UNITCODEA",
                 "1234567890", specialty);
         repositoryHelpers.createSpecialtyUserRole(specialty, user, "patient");
 
         ResultHeading resultHeading = new ResultHeading();
         resultHeading.setSpecialty(specialty);
         resultHeading.setHeading("headingA");
         resultHeading.setHeadingcode("HEADA");
         resultHeading.setLink("http://www.google.com/");
         resultHeading.setPanel(2);
         resultHeading.setPanelorder(3);
         resultHeading.setRollover("rollover");
         resultHeadingDao.save(resultHeading);
 
         resultHeading = new ResultHeading();
         resultHeading.setSpecialty(specialty);
         resultHeading.setHeading("headingB");
         resultHeading.setHeadingcode("HEADB");
         resultHeading.setLink("http://www.test.com/");
         resultHeading.setPanel(1);
         resultHeading.setPanelorder(2);
         resultHeading.setRollover("rollover");
         resultHeadingDao.save(resultHeading);
 
         Calendar calendar = Calendar.getInstance();
         calendar.add(Calendar.MONTH, -4);
         TestResult testResult1 = new TestResult();
         testResult1.setNhsno("1234567890");
         testResult1.setUnitcode("UNITCODEA");
         testResult1.setPrepost("prepost");
         testResult1.setTestcode("HEADA");
         testResult1.setTimestamp(calendar);
         testResult1.setValue("1");
         testResultDao.save(testResult1);
 
         calendar = Calendar.getInstance();
         calendar.add(Calendar.MONTH, -3);
         TestResult testResult2 = new TestResult();
         testResult2.setNhsno("1234567890");
         testResult2.setUnitcode("UNITCODEA");
         testResult2.setPrepost("prepost");
         testResult2.setTestcode("HEADB");
         testResult2.setTimestamp(calendar);
         testResult2.setValue("2");
         testResultDao.save(testResult2);
 
         List<String> resultCodes = new ArrayList<String>();
         resultCodes.add("HEADA");
         resultCodes.add("HEADB");
         List<TestResultWithUnitShortname> checkList = testResultDao.getTestResultForPatient("testuser", resultCodes, "3");
 
         assertNotNull("Couldn't get testresults", checkList);
         assertEquals("Wrong size of testresults", 1, checkList.size());
         assertEquals("Get incorrect testresults", testResult2.getValue(), checkList.get(0).getValue());
         assertEquals("Get incorrect testresults", testResult2.getTestcode(), checkList.get(0).getTestcode());
 
 
     }
 
     private TestResult getTestObject() throws Exception {
         TestResult testResult = new TestResult();
 
         testResult.setNhsno("1234567890");
         testResult.setUnitcode("unit1");
         testResult.setDatestamp(new Timestamp((new Date()).getTime()));
         testResult.setPrepost("prepost");
         testResult.setTestcode("testcode");
         testResult.setTimestamp(Calendar.getInstance());
         testResult.setValue("1");
 
         return testResult;
     }
 
     private Date getYesterday() {
         Calendar yesterday = Calendar.getInstance();
         yesterday.add(Calendar.DATE, -1);
 
         return yesterday.getTime();
     }
 
     private Date getTomorrow() {
         Calendar tomorrow = Calendar.getInstance();
         tomorrow.add(Calendar.DATE, 1);
 
         return tomorrow.getTime();
     }
 
 }
