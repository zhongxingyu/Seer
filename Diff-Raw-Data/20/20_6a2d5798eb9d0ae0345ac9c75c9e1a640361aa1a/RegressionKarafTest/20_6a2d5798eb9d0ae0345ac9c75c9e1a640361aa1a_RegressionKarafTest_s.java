 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertTrue;
 
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
import org.jboss.reddeer.eclipse.ui.console.ConsoleView;
 import org.jboss.reddeer.eclipse.wst.server.ui.view.ServersView;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.reddeer.swt.api.TreeItem;
import org.jboss.reddeer.swt.handler.ShellHandler;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
import org.jboss.reddeer.swt.impl.toolbar.DefaultToolItem;
 import org.jboss.reddeer.swt.impl.tree.DefaultTree;
import org.jboss.reddeer.swt.matcher.RegexMatcher;
import org.jboss.reddeer.swt.matcher.WithTooltipTextMatcher;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.server.ServerManipulator;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Class contains test cases verifying resolved issues related to Apache Karaf Server
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
 @Server(type = ServerReqType.Karaf, state = ServerReqState.PRESENT)
public class RegressionKarafTest {
 
 	@InjectRequirement
 	private ServerRequirement serverRequirement;
 
 	@After
 	public void clean() {
 
 		String server = serverRequirement.getConfig().getName();
 		if (ServerManipulator.isServerStarted(server)) {
 			ServerManipulator.stopServer(server);
 		}
		new DefaultToolItem(new WorkbenchShell(), 0, new WithTooltipTextMatcher(new RegexMatcher("Save All.*"))).click();
 		new ProjectExplorer().deleteAllProjects();
		ShellHandler.getInstance().closeAllNonWorbenchShells();
		try {
			new ConsoleView().terminateConsole();
		} catch (Exception e) {
		}
 	}
 
 	/**
 	 * JMX Node in servers view doesn't work if server has secured JMX access via credentials
 	 * https://issues.jboss.org/browse/FUSETOOLS-1264
 	 */
 	@Test
 	public void issue_1264() {
 
 		String server = serverRequirement.getConfig().getName();
 		ServerManipulator.startServer(server);
 		new ServersView().open();
 		TreeItem item = new DefaultTree(0).getAllItems().get(0);
 		item.expand(TimePeriod.NORMAL);
 		item = item.getItems().get(0);
 		item.select();
 		new ContextMenu("Connect...").select();
 		AbstractWait.sleep(TimePeriod.getCustom(5));
 		assertTrue(item.getItems().size() != 0);
 	}
 }
