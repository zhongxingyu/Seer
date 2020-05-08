 package descent.internal.debug.ui.console;
 
 import java.io.File;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 
 import descent.core.ICompilationUnit;
 import descent.core.IJavaProject;
 import descent.core.IPackageFragmentRoot;
 import descent.core.JavaCore;
 import descent.core.JavaModelException;
 import descent.core.compiler.CharOperation;
 import descent.internal.compiler.env.INameEnvironment;
 import descent.internal.core.CancelableNameEnvironment;
 import descent.internal.core.JavaProject;
 
 /*package*/ class ResourceSearch
 {
 	private INameEnvironment env;
 	private IJavaProject activeProject;
 	
 	public ICompilationUnit search(String filename)
 	{
 		if (this.env == null) {
 			activeProject = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getActiveProject();
 			try {
 				this.env = new CancelableNameEnvironment((JavaProject) activeProject, null, null);
 			} catch (CoreException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 		// If the file exists, then this is an absolute path and it must
 		// be resolved in a library or include path --> IClassFile
 		File file = new File(filename);
 		if (file.exists()) {
 			try {
 				return searchClassFile(filename);
 			} catch (JavaModelException e) {
 				return null;
 			}
 		}
 		
 		char[][] compoundName = getCompoundName(filename);
 		while(compoundName.length > 0)
 		{
 			ICompilationUnit unit = env.findCompilationUnit(compoundName);
 			if (unit != null)
 				return unit;
 			compoundName = CharOperation.subarray(compoundName, 1, -1); // I miss slices...
 		}
 		return null;
 	}
 	
 	private ICompilationUnit searchClassFile(String filename) throws JavaModelException {
 		for(IPackageFragmentRoot root : activeProject.getAllPackageFragmentRoots()) {
			String rootDir;
			if (root.isExternal()) {
				rootDir = root.getPath().toOSString();
			} else {
				rootDir = root.getResource().getLocation().toOSString();
			}
 			
 			if (filename.startsWith(rootDir)) {
 				String fqn = filename.substring(rootDir.length());
 				if (fqn.startsWith("/") || fqn.startsWith("\\")) {
 					fqn = fqn.substring(1);
 				}
 				ICompilationUnit result = search(fqn);
 				if (result != null)
 					return result;
 			}
 		}
 		return null;
 	}
 
 	private static char[][] getCompoundName(String filename) {
 		filename = removeDExtension(filename);
 		
 		char separator;
 		if (filename.indexOf('/') != -1) {
 			separator = '/';
 		} else {
 			separator = '\\';
 		}
 		
 		char[] chr = filename.toCharArray();
 		return CharOperation.splitOn(separator, chr);
 	}
 	
 	private static String removeDExtension(String filename) {
 		if (filename.endsWith(".d")) {
 			filename = filename.substring(0, filename.length() - 2);
 		} else if (filename.endsWith(".di")) {
 			filename = filename.substring(0, filename.length() - 3);
 		} 
 		return filename;
 	}
 }
