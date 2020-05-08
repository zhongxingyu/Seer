package com.teamdev.projects.test.detailsPanel;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.TaskFlow;
 import com.teamdev.projects.test.web.businessflow.detailspanel.DetailsPanelForTaskFlow;
 import com.teamdev.projects.test.web.businessflow.detailspanel.DetailsPanelForTaskInfoFlow;
 import com.teamdev.projects.test.web.page.AppPage;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 /**
  * @author Sergii Moroz
  */
 public class DetailsPanelForTaskInfoTab extends SingleAccountTest {
     AppPage appPage = new AppPage();
     DetailsPanelForTaskInfoFlow detailsPanelInfoFlow = new DetailsPanelForTaskInfoFlow(appPage);
     DetailsPanelForTaskFlow detailsPanelFlow = new DetailsPanelForTaskFlow(appPage);
 
     String taskName = "TaskName1";
     String taskName2 = "TaskName2";
     String note = "MyNote";
     String note2 = "MyAnotherNote";
     String tags = "tag1";
     String assignee = "Sergii Moroz";
     String assignee2 = "Alexander Orlov";
     int estimate = 10;
     String dueTo = "12/11/12";
     int estimate2 = 4;
 
     @Test
     public void layoutVerificationInfoTab(){
         TaskData taskData = new TaskData(taskName, note, tags, assignee, estimate, dueTo);
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         areaFlow.clickTodayFocus();
         TaskFlow task = areaFlow.createTask(taskData);
         task.select();
         assertEquals(detailsPanelFlow.getPanelTitle(), taskName);
         assertEquals(detailsPanelInfoFlow.getNotesTitle(), "NOTES");
         assertEquals(detailsPanelInfoFlow.getNoteText(), note);
         assertEquals(detailsPanelInfoFlow.getTagsTitle(), "TAGS");
         assertEquals(detailsPanelInfoFlow.getTags(), tags);
         assertEquals(detailsPanelInfoFlow.getAssigneeTitle(), "ASSIGNEE");
         assertEquals(detailsPanelInfoFlow.getAssignee(), assignee);
         assertEquals(detailsPanelInfoFlow.getOwnerTitle(), "OWNER");
         assertEquals(detailsPanelInfoFlow.getOwner(), assignee);
         assertEquals(detailsPanelInfoFlow.getAttachmentTitle(), "ATTACHMENTS");
         assertEquals(detailsPanelInfoFlow.getAttachmentLinkText(), "Add attachment");
         assertEquals(detailsPanelInfoFlow.getEstimateTitle(), "ESTIMATED TIME");
         assertEquals(detailsPanelInfoFlow.getEstimate(), ""+estimate+"h");
         assertEquals(detailsPanelInfoFlow.getLoggedTitle(), "TOTAL LOGGED");
         assertEquals(detailsPanelInfoFlow.getLoggedTime(), "N/A");
         assertEquals(detailsPanelInfoFlow.getDueByTitle(), "DUE BY");
         assertEquals(detailsPanelInfoFlow.getDueByValue(), "11 Nov, 2012");
         assertEquals(detailsPanelInfoFlow.getWatchersTitle(), "WATCHERS");
     }
 
     @Test
     public void changingValuesOnInfoTab(){
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         areaFlow.clickTodayFocus();
         TaskData taskData = new TaskData(taskName, note, tags, assignee, estimate, dueTo);
         TaskFlow task = areaFlow.createTask(taskData);
         task.select();
         detailsPanelInfoFlow.setAssignee(assignee2);
         detailsPanelInfoFlow.setDueBy(23, "Dec", 2012);
         detailsPanelInfoFlow.setEstimate(estimate2);
         detailsPanelInfoFlow.enterNote(note2);
         detailsPanelFlow.changeTaskName(taskName2);
         areaFlow.clickSomedayFocus();
         areaFlow.clickTodayFocus();
         task.select();
         assertEquals(detailsPanelFlow.getPanelTitle(), taskName2);
         assertEquals(detailsPanelInfoFlow.getAssignee(), assignee2);
         assertEquals(detailsPanelInfoFlow.getDueByValue(), "23 Dec, 2012");
         assertEquals(detailsPanelInfoFlow.getEstimate(), "4h");
         assertEquals(detailsPanelInfoFlow.getNoteText(), note2);
     }
 }
