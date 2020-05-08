 package org.motechproject.care.repository;
 
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.motechproject.care.domain.CareCaseTask;
 import org.motechproject.care.utils.CaseUtils;
 import org.motechproject.care.utils.SpringIntegrationTest;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class AllCareCaseTasksTest extends SpringIntegrationTest {
     @Autowired
     private AllCareCaseTasks allCareCaseTasks;
 
     @Test
     public void shouldSaveTheTask() {
         String caseId = CaseUtils.getUniqueCaseId();
         String clientCaseId = CaseUtils.getUniqueCaseId();
         String milestoneName = "TT 1";
         String ownerId = "ownerID";
         String motechUserId = "motechUserID";
         String currentTime = DateUtil.now().toString();
         String taskId = "taskID";
         String dateEligible = DateUtil.today().toString();
         String dateExpires = DateUtil.today().plusDays(4).toString();
         String clientCaseType = "mother_case_type";
 
         CareCaseTask careCaseTask = new CareCaseTask(milestoneName, ownerId, caseId, motechUserId, currentTime, taskId, dateEligible, dateExpires, clientCaseType, clientCaseId);
         allCareCaseTasks.add(careCaseTask);
 
         CareCaseTask careCaseTaskFromDb = allCareCaseTasks.findByClientCaseIdAndMilestoneName(clientCaseId, milestoneName);
         Assert.assertNotNull(careCaseTaskFromDb);
         markForDeletion(careCaseTaskFromDb);
 
         Assert.assertEquals(caseId, careCaseTaskFromDb.getCaseId());
         Assert.assertEquals(clientCaseId, careCaseTaskFromDb.getClientCaseId());
         Assert.assertEquals(milestoneName, careCaseTaskFromDb.getMilestoneName());
         Assert.assertEquals(ownerId, careCaseTaskFromDb.getOwnerId());
         Assert.assertEquals(motechUserId, careCaseTaskFromDb.getMotechUserId());
         Assert.assertEquals(currentTime, careCaseTaskFromDb.getCurrentTime());
         Assert.assertEquals(taskId, careCaseTaskFromDb.getTaskId());
         Assert.assertEquals(dateEligible, careCaseTaskFromDb.getDateEligible());
         Assert.assertEquals(dateExpires, careCaseTaskFromDb.getDateExpires());
         Assert.assertEquals(clientCaseType, careCaseTaskFromDb.getClientCaseType());
         Assert.assertEquals("task", careCaseTaskFromDb.getCaseType());
     }
 
     @Test
     public void shouldDeleteCareTask() {
         CareCaseTask careCaseTask = createCareTasks();
         allCareCaseTasks.add(careCaseTask);
         List<CareCaseTask> careCaseTasks = new ArrayList<CareCaseTask>();
         careCaseTasks.add(careCaseTask);
         allCareCaseTasks.deleteDuplicateCareTasksIfOpen(careCaseTasks);
         CareCaseTask careCaseTaskFromDb = allCareCaseTasks.findByClientCaseIdAndMilestoneName(careCaseTask.getClientCaseId(), careCaseTask.getMilestoneName());
         Assert.assertNull(careCaseTaskFromDb);
     }
 
    @Test
    public void shouldNotDeleteCareTaskIfTaskIsOpen() {
        CareCaseTask careCaseTask = createCareTasks();
        careCaseTask.setOpen(false);
        allCareCaseTasks.add(careCaseTask);
        List<CareCaseTask> careCaseTasks = new ArrayList<CareCaseTask>();
        careCaseTasks.add(careCaseTask);
        allCareCaseTasks.deleteDuplicateCareTasksIfOpen(careCaseTasks);
        CareCaseTask careCaseTaskFromDb = allCareCaseTasks.findByClientCaseIdAndMilestoneName(careCaseTask.getClientCaseId(), careCaseTask.getMilestoneName());
        Assert.assertNotNull(careCaseTaskFromDb);
    }

     private CareCaseTask createCareTasks() {
         String caseId = CaseUtils.getUniqueCaseId();
         String clientCaseId = CaseUtils.getUniqueCaseId();
         String milestoneName = "TT 1";
         String ownerId = "ownerID";
         String motechUserId = "motechUserID";
         String currentTime = DateUtil.now().toString();
         String taskId = "taskID";
         String dateEligible = DateUtil.today().toString();
         String dateExpires = DateUtil.today().plusDays(4).toString();
         String clientCaseType = "mother_case_type";
         CareCaseTask careCaseTask = new CareCaseTask(milestoneName, ownerId, caseId, motechUserId, currentTime, taskId, dateEligible, dateExpires, clientCaseType, clientCaseId);
         return careCaseTask;
     }
 
 
 }
