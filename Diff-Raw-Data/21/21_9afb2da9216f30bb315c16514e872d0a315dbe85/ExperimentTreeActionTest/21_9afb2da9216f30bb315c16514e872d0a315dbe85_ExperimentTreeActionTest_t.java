 package gov.nih.nci.protexpress.ui.actions.experiment.viewedit.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import gov.nih.nci.protexpress.domain.experiment.Experiment;
 import gov.nih.nci.protexpress.domain.experiment.ExperimentRun;
 import gov.nih.nci.protexpress.test.ProtExpressBaseHibernateTest;
 import gov.nih.nci.protexpress.ui.actions.experiment.viewedit.ExperimentTreeAction;
 
 public class ExperimentTreeActionTest extends ProtExpressBaseHibernateTest {
 
     private ExperimentTreeAction action;
     private Experiment experiment;
 
     @Override
     protected void onSetUp() throws Exception {
         super.onSetUp();
         this.action = new ExperimentTreeAction();
 
         this.experiment = new Experiment("Name - Test Experiment 1");
         this.experiment.setDescription("Description - Test Experiment 1");
         this.experiment.setHypothesis("Hypothesis - Test Experiment 1");
         this.experiment.setUrl("URL - Test Experiment 1");
 
         List<ExperimentRun> experimentRuns = new ArrayList<ExperimentRun>();
         ExperimentRun experimentRun = new ExperimentRun("run name");
         experimentRun.setExperiment(this.experiment);
         experimentRuns.add(experimentRun);
         this.experiment.setExperimentRuns(experimentRuns);
         
         this.theSession.saveOrUpdate(this.experiment);
         this.theSession.flush();
         this.theSession.clear();
     }
 
     public void testPrepare() throws Exception {
         this.action.prepare();
         assertNull(this.action.getExperiment());
         assertNull(this.action.getExperimentRun());
         assertNull(this.action.getProtApp());
         assertNull(this.action.getInput());
         assertNull(this.action.getOutput());
         assertNull(this.action.getSelectedNodeId());
         assertNull(this.action.getTreeMode());
 
         this.action = new ExperimentTreeAction();
         this.action.setExperimentId(this.experiment.getId());
         this.action.prepare();
         assertNotNull(this.action.getExperiment());
         assertEquals(this.action.getExperiment().getId().toString(), this.action.getSelectedNodeId());
         assertNull(this.action.getExperimentRun());
         assertNull(this.action.getProtApp());
         assertNull(this.action.getInput());
         assertNull(this.action.getOutput());
         assertNull(this.action.getTreeMode());
 
         this.action = new ExperimentTreeAction();
         this.action.setExperimentRunId(this.experiment.getExperimentRuns().get(0).getId());
         this.action.prepare();
         assertNotNull(this.action.getExperimentRun());
         assertEquals(this.action.getExperimentRun().getId().toString(), this.action.getSelectedNodeId());
        assertNotNull(this.action.getExperiment());
         assertNull(this.action.getProtApp());
         assertNull(this.action.getInput());
         assertNull(this.action.getOutput());
         assertNull(this.action.getTreeMode());
     }
 
 }
