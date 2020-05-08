 package org.eclipse.dltk.launching.sourcelookup.containers;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
 import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;
 import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 import org.eclipse.dltk.launching.ScriptLaunchConfigurationConstants;
 
 public class BuildpathContainerSourceContainer extends AbstractSourceContainer {
 
 	public static final String TYPE_ID = "org.eclipse.dltk.launching.sourceContainer.buildpathContainer"; //$NON-NLS-1$
	
 	private String libraryPath;
	
	
 	public BuildpathContainerSourceContainer(String location) {
 		libraryPath = location;
 	}
 
 	public Object[] findSourceElements(String name) throws CoreException {
 		if (name.indexOf(libraryPath) == -1) {
 			return new Object[0];
 		}
 
		// Lets try to locate model element.		
 		try {
			final ILaunchConfiguration configuration = getDirector().getLaunchConfiguration();
 
 			String projectName;
 
 			projectName = configuration.getAttribute(
 					ScriptLaunchConfigurationConstants.ATTR_PROJECT_NAME,
 					(String) null);
 
 			if (projectName != null) {
 
 				IProject project = ResourcesPlugin.getWorkspace().getRoot()
 						.getProject(projectName);
 				IScriptProject prj = DLTKCore.create(project);
				IDLTKSearchScope scope = SearchEngine
						.createSearchScope(new IModelElement[] { prj });
 				HandleFactory fac = new HandleFactory();
 				Openable op = fac.createOpenable(name, scope);
				
 				if (op != null && op.exists() && op instanceof IStorage) {
 					return new Object[] { op };
 				}
 			}
 		} catch (CoreException e) {
 			// TODO: log this
 			e.printStackTrace();
 		}
 
 		return new Object[0];
 	}
	
 	public String getName() {
 		return libraryPath;
 	}
 
 	public ISourceContainerType getType() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
