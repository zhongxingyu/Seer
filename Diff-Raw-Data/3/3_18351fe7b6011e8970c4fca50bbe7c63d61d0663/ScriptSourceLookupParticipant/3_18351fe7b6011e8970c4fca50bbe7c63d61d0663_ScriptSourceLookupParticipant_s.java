 package org.eclipse.dltk.launching.sourcelookup;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
 import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
 import org.eclipse.dltk.internal.launching.LaunchConfigurationUtils;
 
 public class ScriptSourceLookupParticipant extends
 		AbstractSourceLookupParticipant {
 
 	public String getSourceName(Object object) throws CoreException {
 		ScriptStackFrame frame = (ScriptStackFrame) object;
 
 		String path = frame.getFileName().getPath();
 		if (Platform.getOS().equals(Platform.OS_WIN32)) {
 			path = path.substring(1);
 		}
 
 		String root = getProjectRoot();
 
 		// strip off the project root
 		if (path.indexOf(root) != -1) {
 			return path.substring(root.length() + 1);
 		}
 
 		return path;
 	}
 	
 	protected String getProjectRoot() throws CoreException {
 		IProject project = LaunchConfigurationUtils.getProject(getDirector()
 				.getLaunchConfiguration());
 		return project.getLocation().toPortableString();
 	}
 	
 }
