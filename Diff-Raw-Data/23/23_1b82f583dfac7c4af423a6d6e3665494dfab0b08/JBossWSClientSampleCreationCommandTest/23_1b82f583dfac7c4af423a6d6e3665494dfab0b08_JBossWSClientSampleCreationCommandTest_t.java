 package org.jboss.tools.ws.creation.core.test.command;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.JavaCore;
import org.jboss.tools.test.util.TestProjectProvider;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.ResourcesUtils;
 import org.jboss.tools.ws.creation.core.commands.ClientSampleCreationCommand;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 
 public class JBossWSClientSampleCreationCommandTest extends TestCase{
 	static String BUNDLE = "org.jboss.tools.ws.creation.core.test";
 	IProject prj;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		TestProjectProvider provider = new TestProjectProvider(BUNDLE,
 				"/projects/" + "WebTest", "WebTest", true);
 		prj = provider.getProject();
 		JobUtils.delay(3000);
 	}
 	
 	public void testJBIDE6175() throws ExecutionException{
 		IResource src = prj.findMember("src");
 		assertTrue("src is there",src.exists());
 		ServiceModel model = new ServiceModel();
 		model.setCustomPackage("");
 		model.setWebProjectName("WebTest");
 		ClientSampleCreationCommand command = new ClientSampleCreationCommand(model);
 		List<ICompilationUnit> list = command.findJavaUnitsByAnnotation(JavaCore.create(prj), "@WebService");	
 		assertTrue("No java files in src!",list.isEmpty());
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
