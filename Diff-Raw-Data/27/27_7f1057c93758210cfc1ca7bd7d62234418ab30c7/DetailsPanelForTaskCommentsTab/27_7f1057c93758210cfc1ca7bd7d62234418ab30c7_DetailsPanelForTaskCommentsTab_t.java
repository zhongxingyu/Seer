package com.teamdev.projects.test.detailsPanel;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.TaskFlow;
 import com.teamdev.projects.test.web.businessflow.detailspanel.DetailsPanelForTaskCommentsFlow;
 import com.teamdev.projects.test.web.businessflow.detailspanel.DetailsPanelForTaskFlow;
 import com.teamdev.projects.test.web.page.AppPage;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 
 /**
  * @author Sergii Moroz
 */
 public class DetailsPanelForTaskCommentsTab extends SingleAccountTest {
     AppPage appPage = new AppPage();
     DetailsPanelForTaskCommentsFlow detailsPanelCommentsFlow = new DetailsPanelForTaskCommentsFlow(appPage);
     DetailsPanelForTaskFlow detailsPanelFlow = new DetailsPanelForTaskFlow(appPage);
     String taskName = "My New Task";
     String comment = "My Some Comment";
 
     @Test
     public void tabLayoutVerification(){
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         areaFlow.clickTodayFocus();
        TaskData taskData = new TaskData(taskName);
         TaskFlow task = areaFlow.createTask(taskData);
         task.select();
         detailsPanelFlow.pressEmptyCommentsTab();
         assertEquals(detailsPanelFlow.getPanelTitle(), taskName);
         assertEquals(detailsPanelCommentsFlow.getCommentsTabTitle(), "COMMENTS");
         assertEquals(detailsPanelCommentsFlow.getNoCommentsNote(), "There are no comments.");
         assertEquals(detailsPanelCommentsFlow.getTextFromInputField(), "Type your comment here");
        detailsPanelCommentsFlow.inputComment("asd");
         assertEquals(detailsPanelCommentsFlow.getPostButtonLabel(), "Post Comment");
     }
 
     @Test
     public void commentsVerification(){
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         areaFlow.clickTodayFocus();
        TaskData taskData = new TaskData(taskName);
         TaskFlow task = areaFlow.createTask(taskData);
         task.select();
         detailsPanelFlow.pressEmptyCommentsTab();
         detailsPanelCommentsFlow.inputComment(comment);
         detailsPanelCommentsFlow.postComment();
         assertEquals(detailsPanelCommentsFlow.getAllComments().contains(comment), true);
     }
 
 }
