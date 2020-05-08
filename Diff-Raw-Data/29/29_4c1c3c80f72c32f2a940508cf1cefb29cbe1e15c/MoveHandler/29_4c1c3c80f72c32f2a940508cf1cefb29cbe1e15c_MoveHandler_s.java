 
 package net.sourceforge.eclipseccase;
 
 
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Workspace;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.team.IMoveDeleteHook;
 import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.core.TeamPlugin;
 
import net.sourceforge.eclipseccase.jni.Clearcase;

 public class MoveHandler implements IMoveDeleteHook
 {
 
 	ClearcaseProvider provider;
 
 	/**
 	 * Constructor for MoveHandler.
 	 */
 	public MoveHandler(ClearcaseProvider provider)
 	{
 		this.provider = provider;
 	}
 
 	/**
 	 * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
 	 */
 	public boolean deleteFile(
 		IResourceTree tree,
 		IFile file,
 		int updateFlags,
 		IProgressMonitor monitor)
 	{
 		if (! provider.hasRemote(file))
 		{
 			tree.standardDeleteFile(file, updateFlags, monitor);
 			return true;
 		}
 			
 		boolean failed = false;
 		IStatus status = null;
 
 		if ((IResource.FORCE & updateFlags) != 0 &&
 			!tree.isSynchronized(file, IResource.DEPTH_INFINITE))
 		{
 			failed = true;
 			status = new Status(IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Tree not synchronized",
 								null);
 		}
 		if (!failed && (IResource.KEEP_HISTORY & updateFlags) != 0)
 		{
 			tree.addToLocalHistory(file);
 		}
 		
 		if (! failed)
 		{
 			try
 			{
 				provider.delete(new IResource[] {file}, monitor);
 			}
 			catch(TeamException ex)
 			{
 				failed = true;
 				status = ex.getStatus();
 			}
 		}
 		
 		if (failed)
 			tree.failed(status);
 		else
 			tree.deletedFile(file);
 
 
 		return true;
 	}
 
 	/**
 	 * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
 	 */
 	public boolean deleteFolder(
 		IResourceTree tree,
 		IFolder folder,
 		int updateFlags,
 		IProgressMonitor monitor)
 	{
 		if (! provider.hasRemote(folder))
 		{
 			tree.standardDeleteFolder(folder, updateFlags, monitor);
 			return true;
 		}
 		
 		boolean failed = false;
 		IStatus status = null;
 
 		if ((IResource.FORCE & updateFlags) != 0 &&
 			!tree.isSynchronized(folder, IResource.DEPTH_INFINITE))
 		{
 			failed = true;
 			status = new Status(IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Tree not synchronized",
 								null);
 		}
 		if (!failed && (IResource.KEEP_HISTORY & updateFlags) != 0)
 		{
 			// Have to do this recursively for children?
 			//tree.addToLocalHistory(folder);
 		}
 		
 		if (! failed)
 		{
 			try
 			{
 				provider.delete(new IResource[] {folder}, monitor);
 			}
 			catch(TeamException ex)
 			{
 				failed = true;
 				status = ex.getStatus();
 			}
 		}
 		
 		if (failed)
 			tree.failed(status);
 		else
 			tree.deletedFolder(folder);
 
 
 		return true;
 	}
 
 	/**
 	 * @see IMoveDeleteHook#deleteProject(IResourceTree, IProject, int, IProgressMonitor)
 	 */
 	public boolean deleteProject(
 		IResourceTree tree,
 		IProject project,
 		int updateFlags,
 		IProgressMonitor monitor)
 	{
 		if (! provider.hasRemote(project))
 		{
 			tree.standardDeleteProject(project, updateFlags, monitor);
 			return true;
 		}
 		
 		boolean failed = false;
 		IStatus status = null;
 
 		if ((IResource.FORCE & updateFlags) != 0 &&
 			!tree.isSynchronized(project, IResource.DEPTH_INFINITE))
 		{
 			failed = true;
 			status = new Status(IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Tree not synchronized",
 								null);
 		}
 		// KEEP_HISTORY not needed for project delete
 		
		// Can't honor NEVER_DELETE_PROJECT_CONTENTS for dynamic views as its
		// out of our control.  When we handle static views, we will need to honor it
		
 		
 		if (! failed)
 		{
 			try
 			{
 				provider.delete(new IResource[] {project}, monitor);
 			}
 			catch(TeamException ex)
 			{
 				failed = true;
 				status = ex.getStatus();
 			}
 		}
 		
 		if (failed)
 			tree.failed(status);
 		else
 			tree.deletedProject(project);
 
 
 		return true;
 	}
 
 	/**
 	 * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
 	 */
 	public boolean moveFile(
 		IResourceTree tree,
 		IFile source,
 		IFile destination,
 		int updateFlags,
 		IProgressMonitor monitor)
 	{
 		if (! provider.hasRemote(source))
 		{
 			tree.standardMoveFile(source, destination, updateFlags, monitor);
 			return true;
 		}
 
 		boolean failed = false;
 		IStatus status = null;
 
 		if ((IResource.FORCE & updateFlags) != 0 &&
 			!tree.isSynchronized(source, IResource.DEPTH_INFINITE))
 		{
 			failed = true;
 			status = new Status(IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Tree not synchronized",
 								null);
 		}
 		if (!failed && (IResource.KEEP_HISTORY & updateFlags) != 0)
 		{
 			tree.addToLocalHistory(source);
 		}
 		
 		if (! failed)
 		{
 			status = provider.move(source, destination);
 		}
 		
 		if (failed || status.getCode() != IStatus.OK)
 			tree.failed(status);
 		else
 			tree.movedFile(source, destination);
 
 
 		return true;
 	}
 
 	/**
 	 * @see IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int, IProgressMonitor)
 	 */
 	public boolean moveFolder(
 		IResourceTree tree,
 		IFolder source,
 		IFolder destination,
 		int updateFlags,
 		IProgressMonitor monitor)
 	{
 		if (! provider.hasRemote(source))
 		{
 			tree.standardMoveFolder(source, destination, updateFlags, monitor);
 			return true;
 		}
 
 		boolean failed = false;
 		IStatus status = null;
 
 		if ((IResource.FORCE & updateFlags) != 0 &&
 			!tree.isSynchronized(source, IResource.DEPTH_INFINITE))
 		{
 			failed = true;
 			status = new Status(IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Tree not synchronized",
 								null);
 		}
 		if (!failed && (IResource.KEEP_HISTORY & updateFlags) != 0)
 		{
 			// Have to do this recursively for children?
 			//tree.addToLocalHistory(source);
 		}
 		if (! failed)
 		{
 			status = provider.move(source, destination);
 		}
 
 		if (failed || status.getCode() != IStatus.OK)
 			tree.failed(status);
 		else
 			tree.movedFolderSubtree(source, destination);
 
 
 		return true;
 	}
 
 	/**
 	 * @see IMoveDeleteHook#moveProject(IResourceTree, IProject, IProjectDescription, int, IProgressMonitor)
 	 */
 	public boolean moveProject(
 		IResourceTree tree,
 		IProject source,
 		IProjectDescription description,
 		int updateFlags,
 		IProgressMonitor monitor)
 	{
 		if (! provider.hasRemote(source))
 		{
 			tree.standardMoveProject(source, description, updateFlags, monitor);
 			return true;
 		}
 
 		boolean failed = false;
 		IStatus status = null;
 
 		if ((IResource.FORCE & updateFlags) != 0 &&
 			!tree.isSynchronized(source, IResource.DEPTH_INFINITE))
 		{
 			failed = true;
 			status = new Status(IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Tree not synchronized",
 								null);
 		}
 		if (!failed && (IResource.KEEP_HISTORY & updateFlags) != 0)
 		{
 			// Have to do this recursively for children?
 			//tree.addToLocalHistory(source);
 		}
 		if (! failed)
 		{
 			IResource destination = source.getFolder(description.getLocation());
 			status = provider.move(source, destination);
 		}
 
 		if (failed || status.getCode() != IStatus.OK)
 			tree.failed(status);
 		else
 			tree.movedProjectSubtree(source, description);
 
 
 		return true;
 	}
 
 }
