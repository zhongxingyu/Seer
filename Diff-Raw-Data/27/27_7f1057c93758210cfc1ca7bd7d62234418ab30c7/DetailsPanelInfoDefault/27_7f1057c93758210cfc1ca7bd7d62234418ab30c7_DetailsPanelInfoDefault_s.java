package com.teamdev.projects.test.detailspanel;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.data.AreaData;
 import com.teamdev.projects.test.data.TaskData;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.detailspanel.DetailsPanelFlowDefault;
 import com.teamdev.projects.test.web.component.detailspanel.focus.DetailsPanelDefault;
 import com.teamdev.projects.test.web.component.detailspanel.focus.DetailsPanelDefaultAreaFocus;
 import com.teamdev.projects.test.web.component.detailspanel.focus.DetailsPanelDefaultPersonalFocus;
 import com.teamdev.projects.test.web.page.AppPage;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 
 /**
  * Author: Sergii Moroz
 */
 public class DetailsPanelInfoDefault extends SingleAccountTest {
     AppPage appPage = new AppPage();
     DetailsPanelFlowDefault detailsPanelFlowDefault = new DetailsPanelFlowDefault(appPage);
     DetailsPanelDefault detailsPanelDefault = new DetailsPanelDefault();
     DetailsPanelDefaultAreaFocus detailsPanelAreaFocus = new DetailsPanelDefaultAreaFocus();
 
 
     @Test
     public void layoutVerificationDefault (){
         AreaData areaData = randomArea();
         AreaFlow areaFlow = generalFlow.createArea(areaData);
         DetailsPanelDefaultPersonalFocus detailsPanelPersonalFocus = new DetailsPanelDefaultPersonalFocus();
 
         //verification of blocks titles
         assertEquals(generalFlow.getDetailsPanel().getTextFromFilterTitle(), "FILTERS");
         assertEquals(generalFlow.getDetailsPanel().getTextFromEstimatedTitle(),"TOTAL ESTIMATE");
         assertEquals(generalFlow.getDetailsPanel().getTextFromLoggedTitle(), "TOTAL LOGGED");
         assertEquals(generalFlow.getDetailsPanel().getTextFromEstimatedTitle(), "TOTAL ESTIMATED");
         assertEquals(generalFlow.getDetailsPanel().getTextFromLoggedTitle(), "TOTAL LOGGED");
 
         //Verification that all filters are empty by default
         assertEquals(generalFlow.getDetailsPanel().getEstimateValue().contains("N/A"), true);
         assertEquals(generalFlow.getDetailsPanel().getLoggedValue().contains("0h"), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelDefault.filter7Days()), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelDefault.filterAddedToday()), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelPersonalFocus.filterEstimated()), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelPersonalFocus.filterNotEstimated()),true);
     }
 
     @Test
     public void layoutVerificationDefaultFocus (){
         //Verification of 'Number of Assigned tasks' and 'Number of Not Assigned tasks' filters
         AreaData areaData = randomArea();
         AreaFlow areaFlow = generalFlow.createArea(areaData);
         areaFlow.clickNextFocus();
 
         //Verification that all filters are empty by default
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filter7Days()), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filterAddedToday()), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filterEstimated()), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filterNotEstimated()),true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filterAssigned()), true);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filterNotAssigned()), true);
         }
 
     @Test
     public void layoutVerificationWithTasks (){
         AreaData areaData = randomArea();
         AreaFlow areaFlow = generalFlow.createArea(areaData);
         areaFlow.createSeveralTasks(randomTask(), 10);
         areaFlow.clickTodayFocus();
 
         //Verification of filters state on Area focus with added tasks
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filterAddedToday()), 10);
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filter7Days()), 10);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filterAssigned()), true);
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filterNotAssigned()), 10);
         assertEquals(detailsPanelFlowDefault.isFilterEmpty(detailsPanelAreaFocus.filterEstimated()), true);
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filterNotEstimated()), 10);
 
         //verification of "Number of Estimated tasks" and 'Number of Not Estimated tasks" filters
         TaskData taskData = new TaskData("New Task1", 10);  //creation of task with estimate
         areaFlow.createTask(taskData);
         areaFlow.clickTodayFocus();
         assertEquals(generalFlow.getDetailsPanel().getEstimateValue().contains("10h"), true);
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filterEstimated()), 1);
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filterNotEstimated()), 10);
 
         //verification of 'Assigned' and 'Not Assigned' filters
         TaskData taskData1 = new TaskData("New Task", "Sergii Moroz", "Note1");
         areaFlow.clickTodayFocus();
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filterAssigned()), 1);
         assertEquals(detailsPanelFlowDefault.getNumberOfTasks(detailsPanelAreaFocus.filterNotAssigned()), 11);
     }
 }
