 package hu.modembed.test;
 
 import static org.junit.Assert.fail;
 import hu.modembed.MODembedCore;
 import hu.modembed.includedcode.CreateProjectInWorkspaceTask;
 import hu.modembed.includedcode.IncludedProject;
 import hu.modembed.includedcode.IncludedProjectsRegistry;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.ant.core.AntRunner;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.BasicMonitor.Printing;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.match.DefaultMatchEngine;
 import org.eclipse.emf.compare.match.IMatchEngine;
 import org.eclipse.emf.compare.scope.DefaultComparisonScope;
 import org.eclipse.emf.compare.utils.UseIdentifiers;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.junit.Assert;
 import org.junit.runner.RunWith;
 import org.junit.runners.Suite;
 import org.junit.runners.Suite.SuiteClasses;
 
 @RunWith(Suite.class)
 @SuiteClasses({ CompilerTests.class, HexTests.class, SimulatorTests.class, BuildAllExamples.class})
 public class ModembedTests {
 
 	public static final String TEST_CATEGORY = "hu.modembed.test.category"; 
 	
 	public static void assertModelsAreEquivalent(IProject project, String file1, String file2){
 		IFile f1 = project.getFile(file1);
 		IFile f2 = project.getFile(file2);
 		Assert.assertTrue("File "+file1+" does not exists!", f1.exists());
 		Assert.assertTrue("File "+file2+" does not exists!", f2.exists());
 		
 		try {
 			Assert.assertTrue("Files are not equivalent!", modelsAreEquivalent(f1, f2));
 		} catch (Exception e) {
 			Assert.fail(e.getMessage());
 		}
 	}
 	
 	public static EObject load(IFile file, ResourceSet resourceSet){
 		URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
 		Resource res = resourceSet.getResource(uri, true);
 		return res.getContents().get(0);
 	}
 	
 	public static <T extends EObject> T load(IFile file, ResourceSet resourceSet, Class<T> clazz){
 		EObject eo = load(file, resourceSet);
 		Assert.assertTrue(clazz.isInstance(eo));
 		return clazz.cast(eo);
 	}
 	
 	public static boolean modelsAreEquivalent(IFile file1, IFile file2) throws InterruptedException, IOException{
 		ResourceSet rs = MODembedCore.createResourceSet();
 		
 		EObject e1 = load(file1, rs);
 		EObject e2 = load(file2, rs);
 		
 		IMatchEngine matchEngine = DefaultMatchEngine.create(UseIdentifiers.NEVER);
 		
 		
 		
 		
 		Comparison mm = matchEngine.match(new DefaultComparisonScope(e1, e2, e1), Printing.toMonitor(new NullProgressMonitor()));
 		
 		if (!mm.getDifferences().isEmpty()){
 			for(Diff de : mm.getDifferences()){
 				System.out.println(de);
 			}
 		}
 		
 		return mm.getDifferences().isEmpty();
 	}
 	
 	public static void build() throws CoreException{
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		root.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
 	}
 	
 	public static void testSetUp() throws CoreException{
 		//Clean workspace
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		root.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 		System.out.println("Cleaning workspace");
 		for(IProject p : root.getProjects()){
 			System.out.println("Deleting "+p.getProject());
 			p.delete(true, new NullProgressMonitor());
 		}
 		root.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 
 //		List<IncludedProject> projects = IncludedProjectsRegistry.getInstance().getAllProjectsByCategory(TEST_CATEGORY);
 //		projects = IncludedProjectsRegistry.getInstance().resolveDependencies(projects);
 //		
 //		//Import projects
 //		for(IncludedProject ip : projects){
 //			System.out.println("Importing "+ip.getName());
 //			CreateProjectInWorkspaceTask task = new CreateProjectInWorkspaceTask(ip);
 //			task.run(new NullProgressMonitor());
 //		}
 		
 		build();
 	}
 	
 	public static void checkMarkers(IProject project) throws CoreException{
 		for(IMarker m : project.findMarkers(null, true, IResource.DEPTH_INFINITE)){
 			if (IStatus.OK != m.getAttribute(IMarker.SEVERITY, IStatus.OK)){
 				String msg = m.getAttribute(IMarker.MESSAGE, "Error");
 				String loc = m.getAttribute(IMarker.LOCATION, "Unknown location");
 				String ln = m.getAttribute(IMarker.LINE_NUMBER, "");
 				fail(msg+" at "+loc+" "+ln);
 			}
 		}
 	}
 	
 	public static void runAntScript(IProject project, String antScript) throws CoreException{
 		runAntScript(project, antScript, null);
 	}
 	
 	public static void runAntScript(IProject project, String antScript, String target) throws CoreException{
 		runAntScript(project, antScript, target, Collections.<String,String>emptyMap());
 	}
 	
 	public static void runAntScript(IProject project, String antScript, String target, Map<String, String> properties) throws CoreException{
 		IFile buildFile = project.getFile(antScript);
 		Assert.assertTrue("Ant file does not exist!", buildFile.exists());
 		System.out.println("Executing "+buildFile.toString());
 		AntRunner runner = new AntRunner();
 		runner.addUserProperties(properties);
 		runner.setBuildFileLocation(buildFile.getLocation().toPortableString());
 		if (target != null){
 			runner.setExecutionTargets(new String[]{target});
 		}
 		runner.run();
 		project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 	}
 	
 	public static IProject loadProject(String testID) throws CoreException{
 		IncludedProject testp = IncludedProjectsRegistry.getInstance().getProject(testID);
		Assert.assertNotNull("Included project "+testID+" does not exists!", testp);
 		List<IncludedProject> projects = IncludedProjectsRegistry.getInstance().resolveDependencies(Collections.singleton(testp));
 		//Import projects
 		for(IncludedProject ip : projects){
 			System.out.println("Importing "+ip.getName());
 			CreateProjectInWorkspaceTask task = new CreateProjectInWorkspaceTask(ip);
 			task.run(new NullProgressMonitor());
 		}
 
 		
 		
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		root.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 		
 		IProject project = root.getProject(testID);
 		Assert.assertTrue(project.exists());
 		if (!project.isOpen()) project.open(new NullProgressMonitor());
 		
 		ModembedTests.build();
 		root.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 		
 		return project;
 	}
 	
 }
