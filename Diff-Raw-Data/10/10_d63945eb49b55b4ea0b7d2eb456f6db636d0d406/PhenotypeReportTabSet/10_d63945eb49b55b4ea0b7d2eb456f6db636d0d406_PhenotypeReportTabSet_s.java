 package edu.mayo.phenoportal.client.phenotype.report;
 
 import java.util.List;
 
 import com.smartgwt.client.types.Side;
 import com.smartgwt.client.widgets.tab.TabSet;
 
 import edu.mayo.phenoportal.client.Htp;
 import edu.mayo.phenoportal.client.core.AlgorithmData;
 import edu.mayo.phenoportal.client.events.LoggedOutEvent;
 import edu.mayo.phenoportal.client.events.LoggedOutEventHandler;
 import edu.mayo.phenoportal.client.events.PhenotypeExecuteCompletedEvent;
 import edu.mayo.phenoportal.client.events.PhenotypeExecuteCompletedEventHandler;
 import edu.mayo.phenoportal.client.events.PhenotypeSelectionChangedEvent;
 import edu.mayo.phenoportal.client.events.PhenotypeSelectionChangedEventHandler;
 import edu.mayo.phenoportal.shared.Demographic;
 import edu.mayo.phenoportal.shared.Execution;
 import edu.mayo.phenoportal.shared.Image;
 
 /**
  * TabSet to manage the tabs for a selected phenotype. There will be 4 tabs:
  * metatdata, criteria, summary, and demographics. The summary and demographics
  * tabs will not be enabled until the phenotype is executed. The default tab to
  * display is the criteria tab.
  */
 public class PhenotypeReportTabSet extends TabSet {
 
     private CriteriaTab i_criteriaTab;
     private FileInfoTab i_fileInfoTab;
     private SummaryTab i_summaryTab;
     private DemographicsTab i_demographicsTab;
     private WorkflowTab i_workflowTab;
     private List<Demographic> demographic;
 
     private AlgorithmData i_algorithmData;
 
     public List<Demographic> getDemographic() {
         return demographic;
     }
 
     public void setDemographic(List<Demographic> demographic) {
         this.demographic = demographic;
     }
 
     public PhenotypeReportTabSet() {
         super();
         init();
         createPhenotypeExecuteCompletedEventHandler();
         createLoggeOutRequestEventHanlder();
         createPhenotypeSelectionChangedEventHandler();
     }
 
     public void init() {
 
         // create the tabset
         setTabBarAlign(Side.LEFT);
         setTabBarPosition(Side.TOP);
         setWidth100();
         setHeight100();
 
         // create the individual tabs
         i_fileInfoTab = new FileInfoTab("File Info");
         i_criteriaTab = new CriteriaTab("Criteria");
         i_summaryTab = new SummaryTab("Summary");
         i_demographicsTab = new DemographicsTab("Demographics");
         i_workflowTab = new WorkflowTab("Workflow");
 
         // add them to the tabset
         addTab(i_fileInfoTab);
         addTab(i_criteriaTab);
        addTab(i_workflowTab);
         addTab(i_summaryTab);
         addTab(i_demographicsTab);
 
         // initially select this tab
         selectTab(i_fileInfoTab);
 
         // initially disable the summary and demographics tabs until the
         // phenotype file is executed.
         disableAllTabs(true);
 
     }
 
     // clear out all data for the tabs
     private void clearAllTabs() {
         i_criteriaTab.clearTab();
         i_fileInfoTab.clearTab();
         i_summaryTab.clearTab();
         i_demographicsTab.clearTab();
         i_workflowTab.clearTab();
     }
 
     private void disableAllTabs(boolean disable) {
         i_fileInfoTab.setDisabled(disable);
         i_criteriaTab.setDisabled(disable);
 
         disableExecutionResultsTabs(disable);
     }
 
     public void disableExecutionResultsTabs(boolean disable) {
         i_summaryTab.setDisabled(disable);
         i_demographicsTab.setDisabled(disable);
         i_workflowTab.setDisabled(disable);
     }
 
     public void updateSelection(AlgorithmData algorithmData) {
         i_algorithmData = algorithmData;
 
         // New algorithm selected, disable tabs.
         disableExecutionResultsTabs(true);
 
         i_fileInfoTab.setDisabled(false);
         i_criteriaTab.setDisabled(false);
 
         i_fileInfoTab.updateSelection(i_algorithmData);
         i_criteriaTab.updateSelection(i_algorithmData);
 
         // select the criteria tab
         selectTab(i_fileInfoTab);
     }
 
     public void updateResults(Execution execution) {
         if (execution != null && execution.getDemographics() != null
                && execution.getImage() != null) {
             generateGraphs(execution.getDemographics());
            generateWorkflow(execution.getImage());
 
             disableExecutionResultsTabs(false);
         } else {
             disableExecutionResultsTabs(true);
         }
     }
 
     /*
      * create both the summary and demographics graphs
      */
     private void generateGraphs(List<Demographic> demographicResult) {
 
         setDemographic(demographicResult);
         i_summaryTab.createGraphs(demographicResult);
         i_demographicsTab.createGraphs(demographicResult);
     }
 
     /*
      * create workflow tab
      */
     private void generateWorkflow(Image image) {
 
         i_workflowTab.insertImage(image);
 
         // select the workflow tab
         // selectTab(i_workflowTab);
     }
 
     /**
      * Listen for when a phenotype execution completes
      */
     private void createPhenotypeExecuteCompletedEventHandler() {
 
         Htp.EVENT_BUS.addHandler(PhenotypeExecuteCompletedEvent.TYPE,
                 new PhenotypeExecuteCompletedEventHandler() {
 
                     @Override
                     public void onPhenotypeExecuteCompleted(
                             PhenotypeExecuteCompletedEvent phenotypeExecuteCompletedEvent) {
 
                         // if the execute was successful, enable the tabs and
                         // generate the graphs.
                         if (phenotypeExecuteCompletedEvent.getExecuteSuccess()) {
 
                             generateGraphs(phenotypeExecuteCompletedEvent.getExecution()
                                     .getDemographics());
                             generateWorkflow(phenotypeExecuteCompletedEvent.getExecution()
                                     .getImage());
 
                             disableExecutionResultsTabs(false);
                         }
                     }
 
                 });
     }
 
     private void createPhenotypeSelectionChangedEventHandler() {
         Htp.EVENT_BUS.addHandler(PhenotypeSelectionChangedEvent.TYPE,
                 new PhenotypeSelectionChangedEventHandler() {
 
                     @Override
                     public void onPhenotypeSelectionChanged(
                             PhenotypeSelectionChangedEvent phenotypeSelectionChangedEvent) {
                         selectTab(i_fileInfoTab);
                     }
                 });
     }
 
     /**
      * Create a handler to listen for a logged out request.
      */
     private void createLoggeOutRequestEventHanlder() {
         Htp.EVENT_BUS.addHandler(LoggedOutEvent.TYPE, new LoggedOutEventHandler() {
 
             @Override
             public void onLoggedOut(LoggedOutEvent loggedOutEvent) {
                 clearAllTabs();
                 disableAllTabs(true);
 
                 // initially select this tab
                 selectTab(i_fileInfoTab);
             }
         });
 
     }
 
 }
