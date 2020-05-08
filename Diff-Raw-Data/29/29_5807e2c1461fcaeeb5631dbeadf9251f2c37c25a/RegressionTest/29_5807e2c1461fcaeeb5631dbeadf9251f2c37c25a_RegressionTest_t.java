 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.fail;
 
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.shell.DefaultShell;
import org.jboss.reddeer.swt.impl.tree.DefaultTreeItem;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
import org.jboss.tools.fuse.reddeer.preference.ServerRuntimePreferencePage;
 import org.jboss.tools.fuse.reddeer.projectexplorer.CamelProject;
 import org.jboss.tools.fuse.reddeer.view.FuseJMXNavigator;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Class contains test cases verifying resolved issues
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
 public class RegressionTest {
 
 	@After
 	public void clean() {
 
 		new ProjectExplorer().deleteAllProjects();
 	}
 
 	/**
	 * New Server Runtime Wizard - Finish button error
	 * https://issues.jboss.org/browse/FUSETOOLS-1076
	 */
	@Test
	public void issue_1076() {

		ServerRuntimePreferencePage serverRuntime = new ServerRuntimePreferencePage();
		serverRuntime.open();
		new PushButton("Add...").click();
		new DefaultShell("New Server Runtime Environment").setFocus();
		new DefaultTreeItem("JBoss Fuse", "JBoss Fuse 6.1").select();
		if (new PushButton("Finish").isEnabled()) {
			new PushButton("Cancel").click();
			new DefaultShell("Preferences").close();
			fail("'Finish' button should not be enabled!");
		}
	}

	/**
 	 * JMX Navigator - prevent from close Camel Context
 	 * https://issues.jboss.org/browse/FUSETOOLS-1115
 	 */
 	@Test
 	public void issue_1115() {
 
 		ProjectFactory.createProject("camel-archetype-spring");
 		new CamelProject("camel-spring").runCamelContext("camel-context.xml");
 		new FuseJMXNavigator().getNode("Local Processes", "Local Camel Context", "Camel", "camel").select();
 
 		try {
 			new ContextMenu("Close Camel Context");
 		} catch (SWTLayerException ex) {
 			return;
 		} finally {
 			new ConsoleView().terminateConsole();
 		}
 
		fail("Context menu item 'Close Camel Context' is available!");
 	}
 }
