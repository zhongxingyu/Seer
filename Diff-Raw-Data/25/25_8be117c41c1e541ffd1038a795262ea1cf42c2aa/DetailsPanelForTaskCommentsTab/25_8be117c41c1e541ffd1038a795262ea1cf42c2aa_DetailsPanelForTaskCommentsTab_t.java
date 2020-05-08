 package com.teamdev.projects.test.detailsPanel;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.labels.Labels_Eng;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.TaskFlow;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 
 /**
  * @author Sergii Moroz
 */
 public class DetailsPanelForTaskCommentsTab extends SingleAccountTest {
 
     @Test
     public void tabLayoutVerification(){
         TaskData taskData = new TaskData(TestVars.regularTaskName);
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         TaskFlow task = areaFlow.createTaskInTodayFocus(taskData);
         task.select();
         generalFlow.getCommentsTabFlow().pressEmptyCommentsTab();
         assertEquals(generalFlow.getDetailsPanelForTask().getPanelTitle(), TestVars.regularTaskName);
         assertEquals(generalFlow.getCommentsTabFlow().getCommentsTabTitle(), Labels_Eng.commentsTabTitle);
         assertEquals(generalFlow.getCommentsTabFlow().getNoCommentsNote(), Labels_Eng.emptyCommentsNotifier);
         assertEquals(generalFlow.getCommentsTabFlow().getTextFromInputField(), Labels_Eng.commentFieldText);
         generalFlow.getCommentsTabFlow().inputComment(TestVars.regularComment2);
         assertEquals(generalFlow.getCommentsTabFlow().getPostButtonLabel(), Labels_Eng.postCommentButton);
     }
 
     @Test
     public void commentsVerification(){
         AreaFlow areaFlow = generalFlow.createArea(randomArea());
         TaskData taskData = new TaskData(TestVars.regularTaskName);
         TaskFlow task = areaFlow.createTaskInTodayFocus(taskData);
         task.select();
         generalFlow.getDetailsPanelForTask().pressEmptyCommentsTab();
         generalFlow.getCommentsTabFlow().inputComment(TestVars.regularComment);
         generalFlow.getCommentsTabFlow().postComment();
//        assertEquals(generalFlow.getCommentsTabFlow().getAllComments().contains(TestVars.regularComment), true);
     }
 
 }
