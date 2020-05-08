 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertTrue;
 
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.reddeer.requirements.server.ServerReqState;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
 import org.jboss.reddeer.swt.wait.AbstractWait;
 import org.jboss.reddeer.swt.wait.TimePeriod;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.server.ServerManipulator;
 import org.jboss.tools.fuse.reddeer.view.FuseShell;
 import org.jboss.tools.fuse.ui.bot.test.utils.FuseArchetypeNotFoundException;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 import org.jboss.tools.runtime.reddeer.impl.ServerFuse;
 import org.jboss.tools.runtime.reddeer.requirement.ServerReqType;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement;
 import org.jboss.tools.runtime.reddeer.requirement.ServerRequirement.Server;
 import org.junit.After;
 import org.junit.BeforeClass;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests a Fuse project deployment
  * 
  * @author tsedmik
  */
 @Server(type = ServerReqType.Fuse, state = ServerReqState.PRESENT)
 @CleanWorkspace
 @OpenPerspective(FuseIntegrationPerspective.class)
 @RunWith(RedDeerSuite.class)
 public class DeploymentTest extends DefaultTest {
 
 	private static final String PROJECT_ARCHETYPE = "camel-archetype-blueprint";
 	private static final String PROJECT_NAME = "camel-blueprint";
 	private static final String PROJECT2_ARCHETYPE = "camel-archetype-spring-dm";
 	private static final String PROJECT2_NAME = "camel-spring-dm";
 	private static final String TEST_SERVER = "TEST";
 	private static final String PROJECT_IS_DEPLOYED = "Route: timerToLog started and consuming from: Endpoint[timer://foo?period=5000";
 	private static final String PROJECT_IS_UNDEPLOYED = "(CamelContext: camel-1) is shutdown";
 	private static final String PROJECT2_IS_DEPLOYED = "Publishing application context as OSGi service with properties {org.springframework.context.service.name=camel-spring-dm";
 	
 	@InjectRequirement
 	private static ServerRequirement serverRequirement;
 
 	private ServerFuse fuse = (ServerFuse) serverRequirement.getConfig().getServerBase();
 	
 	@BeforeClass
 	public static void setUp() throws FuseArchetypeNotFoundException {
 
 		ProjectFactory.createProject(PROJECT_NAME, PROJECT_ARCHETYPE);
 		ProjectFactory.createProject(PROJECT2_NAME, PROJECT2_ARCHETYPE);
 	}
 
 	@After
 	public void manageServers() {
 
 		new WorkbenchShell().setFocus();
 		ServerManipulator.removeAllModules(serverRequirement.getConfig().getName());
 		ServerManipulator.stopServer(serverRequirement.getConfig().getName());
 		ServerManipulator.removeAllModules(TEST_SERVER);
 		ServerManipulator.stopServer(TEST_SERVER);
 		ServerManipulator.removeServer(TEST_SERVER);
 	}
 
 	@Test
	public void testServerDeployment() {
 
 		ServerManipulator.addServer(fuse.getServerType(), fuse.getHost(), TEST_SERVER, fuse.getPort(), fuse.getUsername(), fuse.getPassword(), PROJECT_NAME);
 		assertTrue(ServerManipulator.hasServerModule(TEST_SERVER, PROJECT_NAME));
 		ServerManipulator.startServer(TEST_SERVER);
		ServerManipulator.publish(TEST_SERVER);
 		assertTrue(new FuseShell().containsLog(PROJECT_IS_DEPLOYED));
 		ServerManipulator.removeAllModules(TEST_SERVER);
 		AbstractWait.sleep(TimePeriod.getCustom(10));
 		assertTrue(new FuseShell().containsLog(PROJECT_IS_UNDEPLOYED));
 	}
 
	@Ignore // TODO need to be reworked see https://github.com/jbosstools/jbosstools-integration-stack-tests/issues/313
 	@Test
 	public void testImmeadiatelyPublishing() {
 
 		ServerManipulator.startServer(fuse.getName());
 		ServerManipulator.setImmeadiatelyPublishing(fuse.getName(), false);
 		ServerManipulator.addModule(fuse.getName(), PROJECT2_NAME);
 		assertTrue(ServerManipulator.hasServerModule(fuse.getName(), PROJECT2_NAME));
 		ServerManipulator.publish(fuse.getName());
 		AbstractWait.sleep(TimePeriod.NORMAL);
 		assertTrue(new FuseShell().containsLog(PROJECT2_IS_DEPLOYED));
 	}
 }
