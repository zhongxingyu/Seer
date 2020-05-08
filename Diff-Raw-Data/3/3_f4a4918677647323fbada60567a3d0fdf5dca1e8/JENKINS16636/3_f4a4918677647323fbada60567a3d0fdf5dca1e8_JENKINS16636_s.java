 package net.praqma.hudson.test.integration.userstories;
 
 import hudson.model.AbstractBuild;
 import hudson.model.Project;
 import hudson.model.Result;
import hudson.scm.PollingResult;
 import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
 import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.hudson.test.BaseTestClass;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.junit.DescriptionRule;
 import net.praqma.junit.TestDescription;
 import org.junit.Rule;
 import org.junit.Test;
 
 import static net.praqma.hudson.test.CCUCMRule.ProjectCreator.Type.child;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 /**
  * @author cwolfgang
  *         Date: 04-02-13
  *         Time: 12:00
  */
 public class JENKINS16636 extends BaseTestClass {
 
     @Rule
     public static ClearCaseRule ccenv = new ClearCaseRule( "JENKINS-16636", "setup-JENKINS-16620.xml" );
 
     @Rule
     public static DescriptionRule desc = new DescriptionRule();
 
     @Test
     @TestDescription( title = "JENKINS-16636", text = "No new baseline found, but can be build anyway" )
     @ClearCaseUniqueVobName( name = "NORMAL" )
     public void jenkins16636() throws Exception {
         Project project = new CCUCMRule.ProjectCreator( "JENKINS-16636", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob() ).getProject();
 
         /* First build must be a success, because there is a valid baseline.
          * This build is done because we need a previous action object */
         AbstractBuild build1 = new CCUCMRule.ProjectBuilder( project ).build();
         new SystemValidator( build1 ).validateBuild( Result.SUCCESS ).validate();
 
         /* Because there are no new baselines, the build must fail */
         AbstractBuild build2 = new CCUCMRule.ProjectBuilder( project ).build();
         new SystemValidator( build2 ).validateBuild( Result.NOT_BUILT ).validate();
     }
 
     @Test
     @TestDescription( title = "JENKINS-16636", text = "No new baseline found, but can be build anyway. Correct behaviour for ANY" )
     @ClearCaseUniqueVobName( name = "ANY" )
     public void jenkins16636Any() throws Exception {
         Project project = new CCUCMRule.ProjectCreator( "JENKINS-16636-any", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob() ).setPromotionLevel( null ).getProject();
 
         /* First build must be a success, because there is a valid baseline.
          * This build is done because we need a previous action object */
         AbstractBuild build1 = new CCUCMRule.ProjectBuilder( project ).build();
         new SystemValidator( build1 ).validateBuild( Result.SUCCESS ).validate();
 
         /* Because we have ANY promotion level, the build must NOT fail */
         AbstractBuild build2 = new CCUCMRule.ProjectBuilder( project ).build();
         new SystemValidator( build2 ).validateBuild( Result.SUCCESS ).validate();
     }
 
 
 
 }
