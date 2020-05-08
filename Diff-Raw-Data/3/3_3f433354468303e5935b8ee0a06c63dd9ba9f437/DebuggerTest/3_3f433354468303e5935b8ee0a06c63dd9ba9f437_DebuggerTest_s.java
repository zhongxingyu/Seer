 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.jboss.reddeer.eclipse.condition.ConsoleHasText;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
 import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.tools.fuse.reddeer.debug.Breakpoint;
 import org.jboss.tools.fuse.reddeer.debug.BreakpointsView;
 import org.jboss.tools.fuse.reddeer.debug.IsRunning;
 import org.jboss.tools.fuse.reddeer.debug.IsSuspended;
 import org.jboss.tools.fuse.reddeer.debug.ResumeButton;
 import org.jboss.tools.fuse.reddeer.debug.StepOverButton;
 import org.jboss.tools.fuse.reddeer.debug.TerminateButton;
 import org.jboss.tools.fuse.reddeer.debug.VariablesView;
 import org.jboss.tools.fuse.reddeer.editor.CamelEditor;
 import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
 import org.jboss.tools.fuse.reddeer.view.ErrorLogView;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 import org.junit.After;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests Camel Routes Debugger
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(JavaEEPerspective.class)
 @RunWith(RedDeerSuite.class)
 public class DebuggerTest extends DefaultTest {
 
 	private static final String PROJECT_ARCHETYPE = "camel-archetype-spring";
 	private static final String PROJECT_NAME = "camel-spring";
 	private static final String CAMEL_CONTEXT = "camel-context.xml";
 	private static final String CHOICE = "choice1";
 	private static final String LOG = "log1";
 	private static final String LOG2 = "log2";
 
 	@BeforeClass
 	public static void setup() {
 
 		ProjectFactory.createProject(PROJECT_NAME, PROJECT_ARCHETYPE);
 		new CamelProject(PROJECT_NAME).openCamelContext(CAMEL_CONTEXT);
 		CamelEditor.switchTab("Design");
 		CamelEditor editor = new CamelEditor(CAMEL_CONTEXT);
 		editor.setId("choice", CHOICE);
 		editor.setId("log", LOG);
 		editor.setId("log", LOG2);
 		editor.save();
 	}
 
 	@After
 	public void removeAllBreakpoints() {
 
 		new BreakpointsView().removeAllBreakpoints();
 	}
 
 	@Test
 	public void testBreakpointManipulation() {
 
 		CamelEditor editor = new CamelEditor(CAMEL_CONTEXT);
 
 		// set some breakpoints
 		editor.setBreakpoint(CHOICE);
 		editor.setBreakpoint(LOG);
 
 		// check Breakpoints View
 		BreakpointsView view = new BreakpointsView();
 		assertTrue(view.isBreakpointSet(CHOICE));
 		assertTrue(view.isBreakpointSet(LOG));
 
 		// do some operations (disable/enable/remove) and check
 		Breakpoint choice = view.getBreakpoint(CHOICE);
 		choice.disable();
 		assertFalse(choice.isEnabled());
 		assertFalse(editor.isBreakpointEnabled(CHOICE));
 		editor.enableBreakpoint(CHOICE);
 		assertTrue(editor.isBreakpointEnabled(CHOICE));
 		view.open();
 		assertTrue(choice.isEnabled());
 		editor.deleteBreakpoint(CHOICE);
 		assertFalse(editor.isBreakpointSet(CHOICE));
 		view.open();
 		assertTrue(view.getBreakpoint(CHOICE) == null);
 		view.open();
 		view.getBreakpoint(LOG).remove();
 		assertFalse(editor.isBreakpointSet(LOG));
 		assertTrue(new ErrorLogView().getErrorMessages().size() == 0);
 	}
 
 	@Test
 	public void testDebugger() {
 
 		new CamelProject(PROJECT_NAME).openCamelContext(CAMEL_CONTEXT);
 		CamelEditor editor = new CamelEditor(CAMEL_CONTEXT);
 		editor.setBreakpoint(CHOICE);
 		editor.setBreakpoint(LOG);
 		new CamelProject(PROJECT_NAME).debugCamelContextWithoutTests(CAMEL_CONTEXT);
 
 		// should stop on the 'choice1' node
 		new WaitUntil(new IsSuspended(), TimePeriod.NORMAL);
 		assertTrue(new ConsoleHasText("Enabling debugger").test());
 		VariablesView variables = new VariablesView();
 		assertEquals(CHOICE, variables.getValue("Endpoint"));
 
 		// get body of message
 		variables.close();
 		AbstractWait.sleep(TimePeriod.SHORT);
 		variables.open();
 		new DefaultTreeItem(4).getItems().get(0).select();
 		assertTrue(new DefaultStyledText().getText().contains("<city>London</city>"));
 
 		// resume and then should stop on the 'log1' node
 		ResumeButton resume = new ResumeButton();
 		assertTrue(resume.isEnabled());
 		resume.select();
 		new WaitUntil(new IsSuspended(), TimePeriod.NORMAL);
 		AbstractWait.sleep(TimePeriod.getCustom(2));
 		assertEquals(LOG, variables.getValue("Endpoint"));
 
 		// step over then should stop on the 'to1' endpoint
 		assertTrue(resume.isEnabled());
 		new StepOverButton().select();
 		new WaitUntil(new IsSuspended(), TimePeriod.NORMAL);
 		assertTrue(new ConsoleHasText("UK message").test());
 		assertTrue(resume.isEnabled());
 		assertEquals("to1", variables.getValue("Endpoint"));
 
 		// remove all breakpoints
 		new BreakpointsView().removeAllBreakpoints();
 		assertTrue(new ConsoleHasText("Removing breakpoint choice1").test());
 		assertTrue(new ConsoleHasText("Removing breakpoint log1").test());
 		resume.select();
 
 		// all breakpoints should be processed
 		new WaitUntil(new IsRunning(), TimePeriod.NORMAL);
 		new TerminateButton().select();
 		assertTrue(new ConsoleHasText("Disabling debugger").test());
 		assertTrue(new ErrorLogView().getErrorMessages().size() == 0);
 	}
 
 	@Test
 	public void testConditionalBreakpoints() {
 
 		new CamelProject(PROJECT_NAME).openCamelContext(CAMEL_CONTEXT);
 		CamelEditor editor = new CamelEditor(CAMEL_CONTEXT);
 		editor.setConditionalBreakpoint(CHOICE, "simple", "${in.header.CamelFileName} == 'message1.xml'");
 		editor.setConditionalBreakpoint(LOG, "simple", "${in.header.CamelFileName} == 'message2.xml'");
 		ResumeButton resume = new ResumeButton();
 		new CamelProject(PROJECT_NAME).debugCamelContextWithoutTests(CAMEL_CONTEXT);
 		new WaitUntil(new IsSuspended(), TimePeriod.NORMAL);
 
 		// should stop on 'choice1' node
 		VariablesView variables = new VariablesView();
 		assertEquals(CHOICE, variables.getValue("Endpoint"));
 		assertTrue(resume.isEnabled());
 
 		// all breakpoint should be processed
 		resume.select();
 		new WaitUntil(new IsRunning(), TimePeriod.NORMAL);
 		assertTrue(new ConsoleHasText("UK message").test());
 		assertTrue(new ConsoleHasText("Other message").test());
 		new TerminateButton().select();
 		assertTrue(new ConsoleHasText("Disabling debugger").test());
 		assertTrue(new ErrorLogView().getErrorMessages().size() == 0);
 	}
 }
