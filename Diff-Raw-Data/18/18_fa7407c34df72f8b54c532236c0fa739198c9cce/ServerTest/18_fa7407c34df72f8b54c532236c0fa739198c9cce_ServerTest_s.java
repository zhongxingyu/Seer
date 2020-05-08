 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.tools.fuse.reddeer.server.ServerManipulator;
 import org.jboss.tools.fuse.reddeer.view.ErrorLogView;
 import org.jboss.tools.runtime.reddeer.impl.ServerFuse;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests managing a Fuse server
  * 
  * @author tsedmik
  */
 @Server(type = ServerReqType.Fuse, state = ServerReqState.PRESENT)
 @CleanWorkspace
 @OpenPerspective(JavaEEPerspective.class)
 @RunWith(RedDeerSuite.class)
public class ServerTest {
 
 	@InjectRequirement
 	private ServerRequirement serverRequirement;
 
 	@Before
 	public void cleanUp() {
 
 		ServerManipulator.removeServer(serverRequirement.getConfig().getName());
 		ServerManipulator.removeServerRuntime(serverRequirement.getConfig().getName() + " Runtime");
		new ErrorLogView().deleteLog();
	}

	@After
	public void cleanUpServer() {

		String serverName = serverRequirement.getConfig().getName();
		new ProjectExplorer().deleteAllProjects();
		ServerManipulator.stopServer(serverName);
		ServerManipulator.removeServer(serverName);
		ServerManipulator.removeServerRuntime(serverName + " Runtime");
 	}
 
 	@Test
 	public void complexServerTest() {
 
 		ServerFuse fuse = (ServerFuse) serverRequirement.getConfig().getServerBase();
 		ErrorLogView errorLog = new ErrorLogView();
 
 		ServerManipulator.addServerRuntime(fuse.getName(), fuse.getHome());
 		assertEquals(1, ServerManipulator.getServerRuntimes().size());
 		ServerManipulator.editServerRuntime(fuse.getName(), fuse.getHome());
 		ServerManipulator.addServer(fuse.getServerType(), fuse.getHost(), fuse.getName(), fuse.getPort(), fuse.getUsername(), fuse.getPassword());
 		assertEquals(1, ServerManipulator.getServers().size());
 		assertTrue(ServerManipulator.isServerPresent(fuse.getName()));
 		ServerManipulator.startServer(fuse.getName());
 		assertTrue(ServerManipulator.isServerStarted(fuse.getName()));
 		assertTrue(errorLog.getErrorMessages().size() == 0);
 		errorLog.deleteLog();
 		ServerManipulator.stopServer(fuse.getName());
 		assertFalse(ServerManipulator.isServerStarted(fuse.getName()));
 		assertTrue(errorLog.getErrorMessages().size() == 0);
 		ServerManipulator.removeServer(fuse.getName());
 		assertEquals(0, ServerManipulator.getServers().size());
 		ServerManipulator.removeServerRuntime(fuse.getName());
 		assertEquals(0, ServerManipulator.getServerRuntimes().size());
 	}
 }
