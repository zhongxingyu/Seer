 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.gef.impl.editpart.LabeledEditPart;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.swt.condition.ShellWithTextIsAvailable;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.text.DefaultText;
 import org.jboss.reddeer.swt.impl.text.LabeledText;
 import org.jboss.reddeer.swt.matcher.RegexMatcher;
 import org.jboss.reddeer.swt.matcher.WithTextMatcher;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.reddeer.swt.wait.WaitUntil;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.tools.fuse.reddeer.editor.CamelEditor;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.preference.FuseToolingEditorPreferencePage;
 import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Class contains test cases on a variety of feature requests
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
public class FeaturesTest extends DefaultTest {
 
 	/**
 	 * Graphical Editor - Add option to configure if labels should be shown or not
 	 * https://issues.jboss.org/browse/FUSETOOLS-837
 	 */
 	@Test
 	public void test837() {
 
 		ProjectFactory.createProject("camel-spring", "camel-archetype-spring");
 		new CamelProject("camel-spring").openCamelContext("camel-context.xml");
 		CamelEditor.switchTab("Design");
 		CamelEditor editor = new CamelEditor("camel-context.xml");
		editor.activate();
 		editor.setId("file:src/data?noo...", "start");
 
 		// enable "If enabled the ID values will be used for labels if existing"
 		FuseToolingEditorPreferencePage prefPage = new FuseToolingEditorPreferencePage();
 		prefPage.open();
 		prefPage.setShowIDinEditor(true);
 		prefPage.ok();
 		editor.activate();
 		try {
 			new LabeledEditPart("start").select();
 		} catch (Exception e) {
 			fail("'From' endpoint should be named after id value 'start'");
 		}
 		
 		// disable "If enabled the ID values will be used for labels if existing"
 		prefPage.open();
 		prefPage.setShowIDinEditor(false);
 		prefPage.ok();
 		editor.activate();
 		try {
 			new LabeledEditPart("file:src/data?noo...").select();
 		} catch (Exception e) {
 			fail("'From' endpoint should be named after uri value 'file:src/data?noo...'");
 		}
 	}
 
 	/**
 	 * Add Context Menu to Camel Contexts folder
 	 * https://issues.jboss.org/browse/FUSETOOLS-1274
 	 */
 	@Test
 	public void test1274() {
 
 		ProjectFactory.createProject("camel-blueprint", "camel-archetype-blueprint");
 		new ProjectExplorer().getProject("camel-blueprint").getProjectItem("Camel Contexts").select();
 		AbstractWait.sleep(TimePeriod.SHORT);
 
 		// 1. check context menu item "New Camel XML File"
 		try {
 			new ContextMenu("New Camel XML File");
 		} catch (SWTLayerException ex){
 			fail("'Camel Contexts' does not have a context menu entry 'New Camel XML File'!");
 		}
 
 		// 2. check "RouteContainer"
 		new ContextMenu("New Camel XML File").select();
 		new WaitUntil(new ShellWithTextIsAvailable("New Camel Context XML File"));
 		new DefaultShell("New Camel Context XML File");
 		assertTrue(new LabeledText("RouteContainer:").getText().contains("src/main/resources/OSGI-INF/blueprint"));
 		new PushButton("Finish").click();
 		new WaitWhile(new ShellWithTextIsAvailable("New Camel Context XML File"));
 		
 		// 3. check that wizard didn't overwrite existing file
 		new ProjectExplorer().getProject("camel-blueprint").getProjectItem("Camel Contexts").select();
 		AbstractWait.sleep(TimePeriod.SHORT);
 		new ContextMenu("New Camel XML File").select();
 		new WaitUntil(new ShellWithTextIsAvailable("New Camel Context XML File"));
 		new DefaultShell("New Camel Context XML File");
 		new DefaultText(new WithTextMatcher(new RegexMatcher(" A file with that name.*")));
 		new PushButton("Cancel").click();
 	}
 }
