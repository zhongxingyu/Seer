 package edu.berkeley.eduride.base_plugin.isafile;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.JavaCore;
 
 import edu.berkeley.eduride.base_plugin.util.Console;
 
 
 /*
  * Calls ISAParse on all isa files.
  * 
  */
 public class ISAVisitor implements IResourceProxyVisitor {
 
 	/**
 	 *  Search the workspace and parse all ISAs
 	 * @return
 	 */
 	public static boolean processAllISAInWorkspace() {
 		ISAVisitor isaVisitor = new ISAVisitor();
 		try {
 			IWorkspace ws = ResourcesPlugin.getWorkspace();
 			IWorkspaceRoot root = ws.getRoot();
 			root.accept(isaVisitor, 0);
 			return true;
 		} catch (CoreException e) {
 			// hm, no workspace yet?
 			Console.err("Whoa, couldn't look for ISA files right now, or the visitor bombed.  You should restart methinks.");
			Console.err(e);
 			return false;
 		}
 	}
 
 
 	// this should throw a CoreException if there is a problem processing ISAs?
 	@Override
 	public boolean visit(IResourceProxy proxy) throws CoreException {
 		
 		if (proxy.getType() == IResource.ROOT) {
 			return true;
 		} else if (proxy.getType() == IResource.PROJECT)  {
 			// Project?  Keep visiting iff its a Java project
 			IProject iproj = proxy.requestResource().getProject();
 			return iproj.hasNature(JavaCore.NATURE_ID);
 		} else if (proxy.getType() == IResource.FOLDER) {
 			//Folder.  always visit.
 			return true;
 		} else if (proxy.getType() == IResource.FILE) {
 			// FILE
 			IFile ifile = (IFile) proxy.requestResource();
 			if (ISAUtil.isISAFile(ifile)) {
 				ISAUtil.setAsISA(ifile.getProject());
 				ISAUtil.parseISA(ifile);
 			}
 		}
 		return false;
 	}
 
 }
