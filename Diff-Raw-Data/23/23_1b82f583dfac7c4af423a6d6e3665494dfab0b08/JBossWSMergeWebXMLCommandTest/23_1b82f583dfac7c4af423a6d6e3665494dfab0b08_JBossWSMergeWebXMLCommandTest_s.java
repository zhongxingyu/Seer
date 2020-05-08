 package org.jboss.tools.ws.creation.core.test.command;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
import org.jboss.tools.common.test.util.TestProjectProvider;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.ResourcesUtils;
 import org.jboss.tools.ws.creation.core.commands.MergeWebXMLCommand;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 
 public class JBossWSMergeWebXMLCommandTest extends TestCase {
 
 	static String BUNDLE = "org.jboss.tools.ws.creation.core.test";
 	IProject prj;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		TestProjectProvider provider = new TestProjectProvider(BUNDLE,
 				"/projects/" + "WebTest", "WebTest", true);
 		prj = provider.getProject();
 		JobUtils.delay(3000);
 	}
 	
 	public void testMergeWebXMLCommand() throws ExecutionException{
 		File file = JBossWSCreationUtils.findFileByPath("web.xml", prj.getLocation().toOSString());
 		assertTrue("For now, no web.xml",file == null);
 		ServiceModel model = new ServiceModel();
 		model.setUpdateWebxml(true);
 		model.setWebProjectName("WebTest");
 		MergeWebXMLCommand command = new MergeWebXMLCommand(model);
 		command.execute(null, null);
 		file = JBossWSCreationUtils.findFileByPath("web.xml", prj.getLocation().toOSString());
 		assertTrue("For now, web.xml should be there",file != null);
 	}
 
 	protected void tearDown() throws Exception {
 		boolean oldAutoBuilding = ResourcesUtils.setBuildAutomatically(false);
 		Exception last = null;
 		try {
 			JobUtils.delay(500);
 			try {
 				System.out.println("Deleting " + prj);
 				prj.delete(true, null);
 				JobUtils.delay(500);
 			} catch (Exception e) {
 				System.out.println("Error deleting " + prj);
 				e.printStackTrace();
 				last = e;
 			}
 		} finally {
 			ResourcesUtils.setBuildAutomatically(oldAutoBuilding);
 		}
 
 		if (last != null)
 			throw last;
 		super.tearDown();
 	}
 }
