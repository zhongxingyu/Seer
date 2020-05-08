 package mmrnmhrm.core.model;
 
 import mmrnmhrm.core.model.lang.LangModelRoot;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 
 import dtool.dom.definitions.Module;
 import dtool.refmodel.pluginadapters.IModuleResolver;
 
 
 /**
  * Represents the root of the D Model.
  * TODO: use Resource listener 
  */
 public class DeeModelRoot extends LangModelRoot implements IDeeElement, IModuleResolver {
 
 	private static DeeModelRoot deemodel = new DeeModelRoot();
 	
 	/** @return the shared instance */
 	public static DeeModelRoot getInstance() {
 		return deemodel;
 	}
 	
 	@Override
 	public DeeProject[] newChildrenArray(int size) {
 		return new DeeProject[size];
 	}
 
 	/** Creates a D project in the given existing workspace project. 
 	 * Sets a default build path (src:src, out:bin) on the project. */
 	public DeeProject createDeeProject(IProject project) throws CoreException {
 		DeeNature.addNature(project, DeeNature.NATURE_ID);
 	
 		DeeProject deeproj = new DeeProject(project);
 		deeproj.setDefaultBuildPath();
 		deeproj.saveProjectConfigFile();
 		addDeeProject(deeproj);
 		return deeproj;
 	}
 
 	/** Adds an existing D project to the model. Refreshes the project.  */
 	public void addDeeProject(DeeProject deeproj) throws CoreException {
 		addChild(deeproj);
 		deeproj.updateElementRecursive();
 	}
 	
 	/** Returns all D projects in the D model. */
 	public DeeProject[] getDeeProjects() {
 		return (DeeProject[]) getChildren();
 	}
 	
 	/** Returns the D project for given project */
 	public DeeProject getLangProject(IProject project) {
 		return (DeeProject) getLangProject(project.getName());
 	}
 
 	/** Removes a D project from the model. Does not delete workspace project. */
 	public void removeDeeProject(DeeProject deeproject) throws CoreException {
 		removeChild(deeproject);
 	}
 	
 	/** Delete D project. Removes workspace project. */
 	public void deleteDeeProject(DeeProject deeproject) throws CoreException {
 		removeDeeProject(deeproject);
 		deeproject.getProject().delete(false, null);
 	}
 
 	public void updateElement() throws CoreException {
 		opened = true;
 		//Ok, don't actually reload projects
 		//DeeModelManager.initDeeModel();
 	}
 	
 	public void updateElementRecursive() throws CoreException {
 		updateElement();
 		for(DeeProject deeproj : getDeeProjects()) {
 			deeproj.updateElementRecursive();
 		}
 	}
 
 	public IResource getUnderlyingResource() {
 		return ResourcesPlugin.getWorkspace().getRoot();
 	}
 
	
	/** Finds the module with the given package and module name.
	 * refModule is used to determine which project/build-path to search. */
 	public Module findModule(Module refModule, String packageName, String moduleName) {
 		CompilationUnit refcunit = (CompilationUnit) refModule.getCUnit();
 		
 		DeeProject deeproj = refcunit.getProject();
		if(deeproj == null)
			return null;
 		
 		for (IDeeSourceRoot srcRoot : deeproj.getSourceRoots()) {
 
 			
 			for (PackageFragment pkgFrag : srcRoot.getPackageFragments()) {
 				if(pkgFrag.getElementName().equals(packageName)) {
 				
 					for (CompilationUnit cunit : pkgFrag.getCompilationUnits()) {
 						String str = cunit.getElementName();
 						str = str.substring(0, str.lastIndexOf('.'));
 						if(moduleName.equals(str))
 							return cunit.getNeoModule();
 	
 					}
 				
 				}
 			}
 		}
 		return null;
 	}
 
 }
