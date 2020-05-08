 package com.duckasteroid.tycho.utils.core.builder;
 
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * This builder loads PDE and maven models and checks they are in sync: 
  * <ul>
  * <li>POM `artifactId` = Manifest `Bundle-SymbolicName`</li> 
  * <li>POM `version` = Manifest`Bundle-Version` </li>
  * <li>POM `version` = `*-SNAPSHOT` then Manifest `Bundle-Version` = `*.qualifier`</li> 
  * <li>(optional) POM `name` = Manifest `Bundle-Name`</li>
  * </ul> 
  * @author chris
  */
 public class TychoBuilder extends IncrementalProjectBuilder {
 
 	@Override
 	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
 		if (shouldAudit(kind)) {
 			doAudit(monitor);
 		}
 		return null;
 	}
 
 	/**
 	 * Check the build type and the resource delta to determine if an audit is required
 	 * @param kind The build kind
 	 * @return true if audit required
 	 */
 	private boolean shouldAudit(int kind) {
 		if (kind == FULL_BUILD)
 			return true;
 		IResourceDelta delta = getDelta(getProject());
 		if (delta == null)
 			return false;
 		IResourceDelta[] children = delta.getAffectedChildren();
 		for (int i = 0; i < children.length; i++) {
 			IResourceDelta child = children[i];
 			String fileName = child.getProjectRelativePath().lastSegment();
 			if (fileName.equals("pom.xml") || fileName.equals("MANIFEST.MF"))
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Actually perform the audit
 	 * @param monitor A progress monitor
 	 */
 	private void doAudit(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
