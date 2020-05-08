 package hudson.plugins.iphoneview;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.FreeStyleProject;
 import hudson.model.TopLevelItem;
 import hudson.tasks.junit.TestResultAction;
 import hudson.tasks.test.AbstractTestResultAction;
 import hudson.tasks.test.TestResultProjectAction;
 import mockit.Expectations;
 import org.junit.Test;
 import org.junit.Before;
 
 import static org.junit.Assert.*;
 
 /**
  * Test for {@link IPhoneView}
  * 
  * @author Seiji Sogabe
  */
 public class IPhoneViewTest<P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>>     {
 
     private IPhoneView<P, B> view;
 
     @Before
     public void setUp() throws Exception {
         view = new IPhoneViewObject<P, B>("iPhone");
     }
 
     /**
      * Test of hasJobTestResult method, of class IPhoneView.
      */
     @Test(expected = IllegalArgumentException.class)
     public void testHasJobTestResult_NoJobs() throws Exception {
 
         new Expectations(view) {
 
             {
                 view.getJob(anyString);
                 returns(null);
             }
         };
 
         view.hasJobTestResult("job");
     }
 
     /**
      * Test of hasJobTestResult method, of class IPhoneView.
      */
     @Test
     public void testHasJobTestResult_NoActions() throws Exception {
 
         new Expectations(view) {
 
             FreeStyleProject mockFreeStyleProject;
 
             {
                 view.getJob(anyString);
                 returns(mockFreeStyleProject);
 
                 mockFreeStyleProject.getAction(TestResultProjectAction.class);
                 returns(null);
             }
         };
 
         boolean result = view.hasJobTestResult("job");
 
         assertFalse(result);
     }
 
     /**
      * Test of hasJobTestResult method, of class IPhoneView.
      */
     @Test(expected = IllegalArgumentException.class)
     public void testHasJobTestResult_NotJob() throws Exception {
 
         new Expectations(view) {
 
             TopLevelItem notJob;
 
             {
                 view.getJob(anyString);
                 returns(notJob);
             }
         };
 
         boolean result = view.hasJobTestResult("job");
     }
 
     /**
      * Test of hasJobTestResult method, of class IPhoneView.
      */
     @Test
     public void testHasJobTestResult_NoPreviousResult() throws Exception {
 
         new Expectations(view) {
 
             FreeStyleProject mockFreeStyleProject;
 
             TestResultProjectAction mockTestResultProjectAction;
 
             AbstractTestResultAction mockTestResultAction;
 
             {
                 view.getJob(anyString);
                 returns(mockFreeStyleProject);
 
                 mockFreeStyleProject.getAction(TestResultProjectAction.class);
                 returns(mockTestResultProjectAction);
 
                 mockTestResultProjectAction.getLastTestResultAction();
                 returns(mockTestResultAction);
 
                 mockTestResultAction.getPreviousResult();
                 returns(null);
             }
         };
 
         boolean result = view.hasJobTestResult("job");
 
         assertFalse(result);
     }
 
     /**
      * Test of hasJobTestResult method, of class IPhoneView.
      */
     @Test
     public void testHasJobTestResult_NotNullPreviousResult() throws Exception {
 
         new Expectations(view) {
 
             FreeStyleProject mockFreeStyleProject;
 
             TestResultProjectAction mockTestResultProjectAction;
 
             TestResultAction mockTestResultAction;
 
             {
                 view.getJob(anyString);
                 returns(mockFreeStyleProject);
 
                 mockFreeStyleProject.getAction(TestResultProjectAction.class);
                 returns(mockTestResultProjectAction);
 
                 mockTestResultProjectAction.getLastTestResultAction();
                 returns(mockTestResultAction);
 
                 mockTestResultAction.getPreviousResult();
                 returns(mockTestResultAction);
             }
         };
 
         boolean result = view.hasJobTestResult("job");
 
         assertTrue(result);
     }
 
     /**
      * Test of getIPhoneJob method, of class IPhoneView.
      */
     @Test
     public void testGetIPhoneJob() throws Exception {
 
         new Expectations(view) {
 
             FreeStyleProject mockFreeStyleProject;
 
             {
                 view.getJob(anyString);
                 returns(mockFreeStyleProject);
             }
         };
 
         IPhoneJob<P, B> job = view.getIPhoneJob("job");
         assertNotNull(job);
     }
 
     /**
      * Test of getIPhoneJob method, of class IPhoneView.
      */
     @Test(expected = IllegalArgumentException.class)
     public void testGetIPhoneJob_NotJob() throws Exception {
 
         new Expectations(view) {
 
             TopLevelItem notJob;
 
             {
                 view.getJob(anyString);
                 returns(notJob);
             }
         };
 
         IPhoneJob<P, B> job = view.getIPhoneJob("job");
     }
 
     public static class IPhoneViewObject<P extends AbstractProject<P, B>, B extends AbstractBuild<P, B>>
                 extends IPhoneView<P, B> {
 
         public IPhoneViewObject(String name) {
             super(name);
         }
 
         @Override
         protected void initColumns() {
             // do nothing
         }
     }
 }
