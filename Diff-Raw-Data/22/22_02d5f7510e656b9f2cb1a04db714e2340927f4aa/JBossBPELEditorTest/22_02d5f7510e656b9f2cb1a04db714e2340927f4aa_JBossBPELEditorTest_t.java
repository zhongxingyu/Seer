 package org.jboss.tools.bpel.ui.test.editor;
 
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
import org.jboss.tools.test.util.JUnitUtils;
import org.jboss.tools.test.util.ResourcesUtils;
 
 import junit.framework.TestCase;
 
 public class JBossBPELEditorTest extends TestCase{
 	
 	static String bpelFileName = "HelloWorld.bpel";
 	static String folder = "bpelContent";
 	IProject fproject;
 	IFile bpelFile;
 	static String BUNDLE = "org.jboss.tools.bpel.ui.test";
 	
 	public void setUp() throws Exception {
 		super.setUp();
 
 		//create jboss bpel project
 		fproject = createProject("ODE_Test");
 		bpelFile = fproject.getProject().getFile(folder+"/"+bpelFileName);
 		assertTrue(bpelFile.exists());
 	}
 	public void tearDown() throws Exception {
 		super.tearDown();
 		
 		
 	}
 	
 	public void testOpenEditor(){
 		IEditorPart bpelEditor = openEditor(bpelFile.getFullPath().toString());
 		assertNotNull(bpelEditor);
 	}
 	
 	public IProject createProject(String prjName) throws CoreException {
		IProject prj=null;
		try {
			prj = ResourcesUtils.importProject(BUNDLE, "/projects/" + prjName);
		} catch (IOException e) {
			JUnitUtils.fail("", e);
		} catch (InvocationTargetException e) {
			JUnitUtils.fail("", e);
		} catch (InterruptedException e) {
			JUnitUtils.fail("", e);
		}
 		assertNotNull(prj);
 		return prj;
 	}
 	
 	public static IEditorPart openEditor(String inputFile) {
 		IEditorPart part = null;
 		try {
 			part = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(inputFile)));
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}
 		return part;
 	}
 }
