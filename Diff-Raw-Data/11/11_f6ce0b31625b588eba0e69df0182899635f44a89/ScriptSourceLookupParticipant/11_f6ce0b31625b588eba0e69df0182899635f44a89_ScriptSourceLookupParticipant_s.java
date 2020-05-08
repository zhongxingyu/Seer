 package org.eclipse.dltk.launching.sourcelookup;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.internal.core.DefaultWorkingCopyOwner;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
 import org.eclipse.dltk.internal.launching.LaunchConfigurationUtils;
 
 public class ScriptSourceLookupParticipant extends
 		AbstractSourceLookupParticipant {
 
 	public String getSourceName(Object object) throws CoreException {
 		ScriptStackFrame frame = (ScriptStackFrame) object;
 
 		String path = frame.getFileName().getPath();
 		if (path.length() == 0) {
 			return null;
 		}
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			path = path.substring(1);
 		}
 
 		String root = getProjectRoot();
 
 		// strip off the project root
 		if (path.indexOf(root) != -1) {
 			return path.substring(root.length() + 1);
 		}
 
 		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
 				.findFilesForLocation(new Path(path));
 
 		IProject project = LaunchConfigurationUtils.getProject(getDirector()
 				.getLaunchConfiguration());
 		for (int i = 0; i < files.length; i++) {
 			IFile file = files[i];
 			if (file.exists()) {
 				if (file.getProject().equals(project)) {
 					return file.getProjectRelativePath().toString();
 				}
 			}
 		}
 		return path;
 	}
 
 	protected String getProjectRoot() throws CoreException {
 		IProject project = LaunchConfigurationUtils.getProject(getDirector()
 				.getLaunchConfiguration());
 		return project.getLocation().toPortableString();
 	}
 
 	public Object[] findSourceElements(Object object) throws CoreException {
 		Object[] elements = super.findSourceElements(object);
 		ILaunchConfiguration launchConfiguration = this.getDirector()
 				.getLaunchConfiguration();
 
 		IProject project = LaunchConfigurationUtils
 				.getProject(launchConfiguration);
 		ScriptProject scriptProject = (ScriptProject) DLTKCore.create(project);
 
 		ScriptStackFrame frame = (ScriptStackFrame) object;
 		String path = frame.getFileName().getPath();
 		File file = new File(path);
 		if (file.exists()) {
 			return new Object[] { new DBGPSourceModule(scriptProject, frame
 					.getFileName().getPath(), DefaultWorkingCopyOwner.PRIMARY,
 					frame) };
 		}
 		return elements;
 	}
 }
