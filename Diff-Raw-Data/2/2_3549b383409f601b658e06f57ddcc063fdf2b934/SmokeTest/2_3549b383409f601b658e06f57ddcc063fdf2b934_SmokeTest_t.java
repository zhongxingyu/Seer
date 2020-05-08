 package org.jboss.tools.fuse.ui.bot.test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.jboss.reddeer.common.logging.Logger;
 import org.jboss.reddeer.eclipse.exception.EclipseLayerException;
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.eclipse.ui.perspectives.AbstractPerspective;
 import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.requirements.cleanworkspace.CleanWorkspaceRequirement.CleanWorkspace;
 import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
 import org.jboss.tools.fuse.reddeer.perspectives.FuseIntegrationPerspective;
 import org.jboss.tools.fuse.reddeer.view.JMXNavigator;
 import org.jboss.tools.fuse.ui.bot.test.utils.ProjectFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Simple tests verifies only presence of JBoss Fuse Tooling plugins
  * 
  * @author tsedmik
  */
 @CleanWorkspace
 @OpenPerspective(JavaEEPerspective.class)
 @RunWith(RedDeerSuite.class)
public class SmokeTest extends DefaultTest {
 
 	private static final String PROJECT_ARCHETYPE = "camel-archetype-spring";
 	private static final String PROJECT_NAME = "camel-spring";
 
 	private static Logger log = Logger.getLogger(SmokeTest.class);
 
 	@Test
 	public void testCreateFuseProject() {
 
 		log.info("Create a new Fuse project (" + PROJECT_ARCHETYPE + ")");
 		ProjectFactory.createProject(PROJECT_NAME, PROJECT_ARCHETYPE);
 		try {
 			new ProjectExplorer().getProject(PROJECT_NAME);
 		} catch (EclipseLayerException ex) {
 			fail("Created project is not present in Project Explorer");
 		}
 	}
 
 	@Test
 	public void testOpenViews() {
 
 		new JMXNavigator().open();
 	}
 
 	@Test
 	public void testOpenPerspectives() {
 
 		AbstractPerspective perspective = new FuseIntegrationPerspective();
 		perspective.open();
 		assertTrue(perspective.isOpened());
 	}
 }
