 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertFalse;
 
 import org.jboss.reddeer.common.logging.Logger;
 import org.jboss.reddeer.eclipse.condition.ConsoleHasText;
 import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.swt.api.Shell;
 import org.jboss.reddeer.swt.condition.JobIsRunning;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
 import org.jboss.tools.fuse.reddeer.wizard.CamelTestCaseWizard;
 import org.jboss.tools.fuse.ui.bot.test.utils.EditorManipulator;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
import org.junit.After;
import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests option <i>Run a Project as Local Camel Context (with or without
  * tests)</i>. The option is tested on a project from archetype
  * <i>camel-archetype-spring</i>. If you want to change archetype (see static
  * variables), you have to change <i>resources/FailingTest.java</i> and
  * <i>resources/PassingTest.java</i> too (to correspondent with a new project).
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
public class ProjectLocalRunTest {
 
 	private static final String PROJECT_ARCHETYPE = "camel-archetype-spring";
 	private static final String PROJECT_NAME = "camel-spring";
 	private static final String PROJECT_CAMEL_CONTEXT = "camel-context.xml";
 
 	private static Logger log = Logger.getLogger(ProjectLocalRunTest.class);
 
 	/**
 	 * Creates a new Camel Test Case <b>which fails</b>
 	 */
 	private static void createTestClass() {
 
 		new CamelProject(PROJECT_NAME).selectCamelContext(PROJECT_CAMEL_CONTEXT);
 		CamelTestCaseWizard camelTestCase = new CamelTestCaseWizard();
 		camelTestCase.open();
 		camelTestCase.next();
 		camelTestCase.finish();
 
 		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
 		log.info("The test case for the Fuse project was created.");
 	}
 
 	@BeforeClass
 	public static void createProject() {
 
 		log.info("Create a new Fuse project (" + PROJECT_ARCHETYPE + ")");
 		ProjectFactory.createProject(PROJECT_NAME, PROJECT_ARCHETYPE);
 		createTestClass();
 	}
 
	@After
	public void terminateConsole() {

		log.info("Try to terminate a console.");
		new ConsoleView().terminateConsole();
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
	}

 	@Test
 	public void runProjectWithPassingTestsTest() {
 
 		Shell workbenchShell = new WorkbenchShell();
 		log.info("Run a project as Local Camel Context (Project contains a passing test case).");
 		EditorManipulator.copyFileContent("resources/PassingTest.java");
 		new CamelProject(PROJECT_NAME).runCamelContext(PROJECT_CAMEL_CONTEXT);
 		new WaitUntil(new ConsoleHasText("Total 1 routes, of which 1 is started."), TimePeriod.getCustom(300));
 		workbenchShell.setFocus();
 
 		assertFalse("This build should be successful.", new ConsoleView().getConsoleText().contains("BUILD FAILURE"));
 	}
 
 	@Test
 	public void runProjectWithFailingTestsTest() {
 
 		Shell workbenchShell = new WorkbenchShell();
 		log.info("Run a project as Local Camel Context (Project contains a failing test case).");
 		EditorManipulator.copyFileContent("resources/FailingTest.java");
 		new CamelProject(PROJECT_NAME).runCamelContext(PROJECT_CAMEL_CONTEXT);
 		new WaitUntil(new ConsoleHasText("BUILD FAILURE"), TimePeriod.getCustom(300));
 		workbenchShell.setFocus();
 	}
 
 	@Test
 	public void runProjectWithoutTestsTest() {
 
 		Shell workbenchShell = new WorkbenchShell();
 		log.info("Run a project as Local Camel Context (without tests).");
 		EditorManipulator.copyFileContent("resources/FailingTest.java");
 		new CamelProject(PROJECT_NAME).runCamelContextWithoutTests(PROJECT_CAMEL_CONTEXT);
 		new WaitUntil(new ConsoleHasText("Total 1 routes, of which 1 is started."), TimePeriod.getCustom(300));
 		workbenchShell.setFocus();
 
 		assertFalse("This build should be successful.", new ConsoleView().getConsoleText().contains("BUILD FAILURE"));
 	}

	@AfterClass
	public static void deleteProject() {

		new CamelProject(PROJECT_NAME).deleteProject();
		log.info("Workspace was cleaned!");
	}

 }
