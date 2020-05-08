 package org.patientview.test.service;
 
 import org.patientview.patientview.model.*;
 import org.patientview.patientview.model.enums.GroupEnum;
 import org.patientview.patientview.model.enums.SendEmailEnum;
 import org.patientview.service.JobManager;
 import org.patientview.service.UnitManager;
 import org.patientview.test.helpers.ServiceHelpers;
 import org.junit.Test;
 
 import javax.inject.Inject;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  *      These tests require an admin adminUser to be logged into a specialty
  */
 public class JobManagerTest extends BaseServiceTest {
 
     @Inject
     private ServiceHelpers serviceHelpers;
 
     @Inject
     private JobManager jobManager;
 
     @Inject
     private UnitManager unitManager;
 
     @Test
     public void testAddGetJob() throws Exception {
         User user1 = serviceHelpers.createUser("test 1", "tester1@test.com", "test1", "Test 1");
         User user2 = serviceHelpers.createUser("test 2", "tester2@test.com", "test2", "Test 2");
         Conversation conversation = serviceHelpers.createConversation("Test subject", user1, user2, true);
 
         Message message = serviceHelpers.createMessage(conversation, user1, user2, "This is a message", true);
         Specialty specialty = serviceHelpers.createSpecialty("Specialty1", "specialty1", "A test specialty");
 
         Job job1 = new Job();
         job1.setCreator(user1);
         job1.setMessage(message);
         job1.setSpecialty(specialty);
         job1.setStatus(SendEmailEnum.PENDING);
         job1.setGroupEnum(GroupEnum.ALL_ADMINS);
         jobManager.save(job1);
 
         Job job2 = new Job();
         job2.setCreator(user2);
         job2.setMessage(message);
         job2.setSpecialty(specialty);
         job2.setStatus(SendEmailEnum.FAILED);
         job2.setGroupEnum(GroupEnum.ALL_PATIENTS);
         jobManager.save(job2);
 
 
         assertTrue("Invalid id for Job 1 ", job1.getId() > 0);
         assertTrue("Invalid id for Job 2 ", job2.getId() > 0);
 
         List<Job> checkJobList = jobManager.getJobList(SendEmailEnum.PENDING);
 
         assertNotNull(checkJobList);
         assertEquals("Wrong number of job size", checkJobList.size(), 1);
         assertFalse("Job 2 found in SendEmailEnum PENDING", checkJobList.contains(job2));
         assertTrue("Job 1 not found in SendEmailEnum PENDING", checkJobList.contains(job1));
         assertEquals("Specialty not stored", checkJobList.get(0).getSpecialty(), specialty);
         assertEquals("Message not stored", checkJobList.get(0).getMessage(), message);
         assertEquals("User not stored", checkJobList.get(0).getCreator(), user1);
     }
 
     @Test
     public void testGetSpecialGroupUsers() {
         Specialty specialty1 = serviceHelpers.createSpecialty("Specialty 1", "Specialty1", "Test description");
         User adminUser = serviceHelpers.createUserWithMapping("adminuser", "test@admin.com", "p", "Admin", "UNITA", "nhs1", specialty1);
         User user1 = serviceHelpers.createUserWithMapping("testname1", "test1@admin.com", "p", "test1", "UNITA", "nhstest1", specialty1);
         User user2 = serviceHelpers.createUserWithMapping("testname2", "test2@admin.com", "p", "test2", "UNITA", "nhstest2", specialty1);
         User user3 = serviceHelpers.createUserWithMapping("testname3-GP", "test3@admin.com", "p", "test3", "UNITA", "nhstest3", specialty1);
         User user4 = serviceHelpers.createUserWithMapping("testname4", "test4@admin.com", "p", "test4", "unitB", "nhstest4", specialty1);
 
         securityHelpers.loginAsUser(adminUser.getUsername());
 
         // Add SpecialtyUserRole
         serviceHelpers.createSpecialtyUserRole(specialty1, adminUser, "unitadmin");
         serviceHelpers.createSpecialtyUserRole(specialty1, user1, "patient");
         serviceHelpers.createSpecialtyUserRole(specialty1, user2, "patient");
         serviceHelpers.createSpecialtyUserRole(specialty1, user3, "patient");
         serviceHelpers.createSpecialtyUserRole(specialty1, user4, "patient");
 
         Unit unitRm301 = new Unit();
         unitRm301.setUnitcode("unitA");
         unitRm301.setName("RM301: RUNNING MAN TEST UNIT");
         unitRm301.setShortname("RM301");
         unitRm301.setRenaladminemail("renaladmin@mailinator.com");
         unitRm301.setSpecialty(specialty1);
         unitManager.save(unitRm301);
 
         List<User> checkUserList = jobManager.getSpecialGroupUsers(adminUser, specialty1, "patient", unitRm301);
 
         assertEquals("Wrong number of users", checkUserList.size(), 2);
         assertFalse("User 3 found in users", checkUserList.contains(user3));
         assertFalse("User 4 found in users", checkUserList.contains(user4));
         assertTrue("User 1 not found in users", checkUserList.contains(user1));
         assertTrue("User 2 not found in users", checkUserList.contains(user2));
     }
 }
