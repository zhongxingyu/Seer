 package net.kbserve.pyjdt.properties.models;
 
 import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
 
 public class CPELibrary extends CPEAbstractContainer implements ICPEType {
 
 	@Override
 	public String getRealPath(IProject project) {
		String realPath = getPath();
		IPath realIPath = new Path(realPath);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject origProj = workspaceRoot
				.getProject(realIPath.segment(0));
		if (origProj.isAccessible()) {// path is relative to the workspace.
			IPath workspacePath = workspaceRoot.getLocation().makeAbsolute();
			return makeStringPath(workspacePath.append(realIPath).makeAbsolute());
		}
		return realPath;
 	}
 }
