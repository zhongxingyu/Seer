 package net.sourceforge.eclipseccase;
 
 import net.sourceforge.eclipseccase.jni.Clearcase;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.team.IMoveDeleteHook;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.core.TeamPlugin;
 import org.eclipse.team.core.internal.simpleAccess.SimpleAccessOperations;
 
 public class ClearcaseProvider
 	extends RepositoryProvider
 	implements SimpleAccessOperations
 {
 
 	private IMoveDeleteHook moveHandler = new MoveHandler(this);
 	private IFileModificationValidator modificationValidator = new ModificationHandler(this);
 	private String comment = "";
 	
 	public static final String ID = "net.sourceforge.eclipseccase.ClearcaseProvider";
 
 	/**
 	 * @see RepositoryProvider#configureProject()
 	 */
 	public void configureProject() throws CoreException
 	{
 	}
 
 	/**
 	 * @see RepositoryProvider#getID()
 	 */
 	public String getID()
 	{
 		return ID;
 	}
 
 	/**
 	 * @see IProjectNature#deconfigure()
 	 */
 	public void deconfigure() throws CoreException
 	{
 	}
 
 	/**
 	 * @see SimpleAccessOperations#get(IResource[], int, IProgressMonitor)
 	 */
 	public void get(IResource[] resources, int depth, IProgressMonitor progress)
 		throws TeamException
 	{
 	}
 
 	/**
 	 * @see SimpleAccessOperations#checkout(IResource[], int, IProgressMonitor)
 	 */
 	public void checkout(
 		IResource[] resources,
 		int depth,
 		IProgressMonitor progress)
 		throws TeamException
 	{
 		execute(new IRecursiveOperation()
 		{
 			public IStatus visit(IResource resource, IProgressMonitor progress)
 			{
 				IStatus result =
 					new Status(IStatus.OK, TeamPlugin.ID, TeamException.OK, "OK", null);
 				Clearcase.Status status =
 					Clearcase.checkout(resource.getLocation().toOSString(), "", false);
 				if (!status.status)
 				{
 					result =
 						new Status(
 							IStatus.ERROR,
 							TeamPlugin.ID,
 							TeamException.UNABLE,
 							"Checkout failed: " + status.message,
 							null);
 				}
 				return result;
 			}
 		}, resources, depth, progress);
 	}
 
 	/**
 	 * @see SimpleAccessOperations#checkin(IResource[], int, IProgressMonitor)
 	 */
 	public void checkin(
 		IResource[] resources,
 		int depth,
 		IProgressMonitor progress)
 		throws TeamException
 	{
 		execute(new IRecursiveOperation()
 		{
 			public IStatus visit(IResource resource, IProgressMonitor progress)
 			{
 				IStatus result =
 					new Status(IStatus.OK, TeamPlugin.ID, TeamException.OK, "OK", null);
 				Clearcase.Status status =
 					Clearcase.checkin(resource.getLocation().toOSString(), comment);
 				if (!status.status)
 				{
 					result =
 						new Status(
 							IStatus.ERROR,
 							TeamPlugin.ID,
 							TeamException.UNABLE,
 							"Checkin failed: " + status.message,
 							null);
 				}
 				return result;
 			}
 		}, resources, depth, progress);
 	}
 
 	/**
 	 * @see SimpleAccessOperations#uncheckout(IResource[], int, IProgressMonitor)
 	 */
 	public void uncheckout(
 		IResource[] resources,
 		int depth,
 		IProgressMonitor progress)
 		throws TeamException
 	{
 		execute(new IRecursiveOperation()
 		{
 			public IStatus visit(IResource resource, IProgressMonitor progress)
 			{
 				IStatus result =
 					new Status(IStatus.OK, TeamPlugin.ID, TeamException.OK, "OK", null);
 				Clearcase.Status status =
 					Clearcase.uncheckout(resource.getLocation().toOSString(), false);
 				if (!status.status)
 				{
 					result =
 						new Status(
 							IStatus.ERROR,
 							TeamPlugin.ID,
 							TeamException.UNABLE,
 							"Uncheckout failed: " + status.message,
 							null);
 				}
 				return result;
 			}
 		}, resources, depth, progress);
 	}
 
 	/**
 	 * @see SimpleAccessOperations#delete(IResource[], IProgressMonitor)
 	 */
 	public void delete(IResource[] resources, IProgressMonitor progress)
 		throws TeamException
 	{
 		execute(new IIterativeOperation()
 		{
 			public IStatus visit(IResource resource, int depth, IProgressMonitor progress)
 			{
 				IStatus result = checkoutParent(resource);
 				if (result.isOK())
 				{
 					Clearcase.Status status =
 						Clearcase.delete(resource.getLocation().toOSString(), "");
 					if (!status.status)
 					{
 						result =
 							new Status(
 								IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Delete failed: " + status.message,
 								null);
 					}
 				}
 				return result;
 			}
 		}, resources, IResource.DEPTH_INFINITE, progress);
 	}
 
 	public void add(IResource[] resources, IProgressMonitor progress)
 		throws TeamException
 	{
 		execute(new IIterativeOperation()
 		{
 			public IStatus visit(IResource resource, int depth, IProgressMonitor progress)
 			{
 				// Should really walk up parent heirarchy, find first ccase
 				// element that is a parent, and walk back down, adding each to ccase
 				IStatus result = checkoutParent(resource);
 				if (result.isOK())
 				{
 					Clearcase.Status status =
 						Clearcase.add(resource.getLocation().toOSString(), "");
 					if (!status.status)
 					{
 						result =
 							new Status(
 								IStatus.ERROR,
 								TeamPlugin.ID,
 								TeamException.UNABLE,
 								"Add failed: " + status.message,
 								null);
 					}
 				}
 				return result;
 			}
 		}, resources, IResource.DEPTH_INFINITE, progress);
 	}
 
 	/**
 	 * @see SimpleAccessOperations#moved(IPath, IResource, IProgressMonitor)
 	 */
 	public void moved(IPath source, IResource target, IProgressMonitor progress)
 		throws TeamException
 	{
 	}
 
 	/**
 	 * @see SimpleAccessOperations#isCheckedOut(IResource)
 	 */
 	public boolean isCheckedOut(IResource resource)
 	{
 		boolean result = false;
 		String path = resource.getLocation().toOSString();
 		// Team seems to call isCheckedOut for non elements (bug?), so do a check
 		if (Clearcase.isElement(path))
 			result = Clearcase.isCheckedOut(path);
 		return result;
 	}
 
 	/**
 	 * @see SimpleAccessOperations#hasRemote(IResource)
 	 */
 	public boolean hasRemote(IResource resource)
 	{
 		boolean result = Clearcase.isElement(resource.getLocation().toOSString());
 		return result;
 	}
 
 	/**
 	 * @see SimpleAccessOperations#isDirty(IResource)
 	 */
 	public boolean isDirty(IResource resource)
 	{
 		String file = resource.getLocation().toOSString();
 		boolean result = Clearcase.isCheckedOut(file) && Clearcase.isDifferent(file);
 		return result;
 	}
 
 	public IStatus move(IResource source, IResource destination)
 	{
 		IStatus result = checkoutParent(source);
 
 		if (result.isOK())
 			result = checkoutParent(destination);
 
 		if (result.isOK())
 		{
 			Clearcase.Status ccStatus = Clearcase.move(source.getLocation().toOSString(),
 														destination.getLocation().toOSString(), "");
 			if (! ccStatus.status)
 			{
 				result = new Status(IStatus.ERROR,
 									TeamPlugin.ID,
 									TeamException.UNABLE,
 									ccStatus.message,
 									null);
 			}
 		}
 		return result;
 	}
 
 	public IStatus checkoutParent(IResource resource)
 	{
 		IStatus result =
 			new Status(IStatus.OK, TeamPlugin.ID, TeamException.OK, "OK", null);
		String parent = null;
		// IProject's parent is the workspace directory, we want the filesystem parent
		if (resource instanceof IProject)
		{
			parent = resource.getLocation().toFile().getParent().toString();
		}
		else
		{
			parent = resource.getParent().getLocation().toOSString();
		}
 		if (!Clearcase.isCheckedOut(parent))
 		{
 			Clearcase.Status ccStatus = Clearcase.checkout(parent, "", false);
 			if (! ccStatus.status)
 			{
 				result = new Status(
 						IStatus.ERROR,
 						TeamPlugin.ID,
 						TeamException.UNABLE,
 						"Could not check out parent: " + ccStatus.message,
 						null);
 			}
 		}
 		return result;
 	}
 	
 	
 	/**
 	 * @see RepositoryProvider#getSimpleAccess()
 	 */
 	public SimpleAccessOperations getSimpleAccess()
 	{
 		return this;
 	}
 
 	/**
 	 * @see RepositoryProvider#getMoveDeleteHook()
 	 */
 	public IMoveDeleteHook getMoveDeleteHook()
 	{
 		return moveHandler;
 	}
 
 	/**
 	 * @see RepositoryProvider#getFileModificationValidator()
 	 */
 	public IFileModificationValidator getFileModificationValidator()
 	{
 		return modificationValidator;
 	}
 
 	/**
 	 * Gets the comment.
 	 * @return Returns a String
 	 */
 	public String getComment()
 	{
 		return comment;
 	}
 
 	/**
 	 * Sets the comment.
 	 * @param comment The comment to set
 	 */
 	public void setComment(String comment)
 	{
 		this.comment = comment;
 	}
 
 	// Out of sheer laziness, I appropriated the following code from the team provider example =)
 	
 	/**
 	 * These interfaces are to operations that can be performed on the array of resources,
 	 * and on all resources identified by the depth parameter.
 	 * @see execute(IOperation, IResource[], int, IProgressMonitor)
 	 */
 	public static interface IOperation
 	{
 	}
 	public static interface IIterativeOperation extends IOperation
 	{
 		public IStatus visit(IResource resource, int depth, IProgressMonitor progress);
 	}
 	public static interface IRecursiveOperation extends IOperation
 	{
 		public IStatus visit(IResource resource, IProgressMonitor progress);
 	}
 
 	/**
 	 * Perform the given operation on the array of resources, each to the
 	 * specified depth.  Throw an exception if a problem ocurs, otherwise
 	 * remain silent.
 	 */
 	protected void execute(
 		IOperation operation,
 		IResource[] resources,
 		int depth,
 		IProgressMonitor progress)
 		throws TeamException
 	{
 
 		// Create an array to hold the status for each resource.
 		IStatus[] statuses = new IStatus[resources.length];
 
 		// Remember if a failure occurred in any resource, so we can throw an exception at the end.
 		boolean failureOccurred = false;
 
 		// For each resource in the local resources array.
 		for (int i = 0; i < resources.length; i++)
 		{
 			if (operation instanceof IRecursiveOperation)
 				statuses[i] =
 					execute((IRecursiveOperation) operation, resources[i], depth, progress);
 			else
 				statuses[i] =
 					((IIterativeOperation) operation).visit(resources[i], depth, progress);
 			failureOccurred = failureOccurred || (!statuses[i].isOK());
 		}
 
 		// Finally, if any problems occurred, throw the exeption with all the statuses,
 		// but if there were no problems exit silently.
 		if (failureOccurred)
 			throw new TeamException(
 				new MultiStatus(getID(), IStatus.ERROR, statuses, "Errors occurred.", null));
 
 		// Cause all the resource changes to be broadcast to listeners.
 		//		TeamPlugin.getManager().broadcastResourceStateChanges(resources);
 	}
 
 	/**
 	 * Perform the given operation on a resource to the given depth.
 	 */
 	protected IStatus execute(
 		IRecursiveOperation operation,
 		IResource resource,
 		int depth,
 		IProgressMonitor progress)
 	{
 
 		// Visit the given resource first.
 		IStatus status = operation.visit(resource, progress);
 
 		// If the resource is a file then the depth parameter is irrelevant.
 		if (resource.getType() == IResource.FILE)
 			return status;
 
 		// If we are not considering any members of the container then we are done.
 		if (depth == IResource.DEPTH_ZERO)
 			return status;
 
 		// If the operation was unsuccessful, do not attempt to go deep.
 		if (!status.isOK())
 			return status;
 
 		// If the container has no children then we are done.
 		IResource[] members = getMembers(resource);
 		if (members.length == 0)
 			return status;
 
 		// There are children and we are going deep, the response will be a multi-status.
 		MultiStatus multiStatus =
 			new MultiStatus(
 				status.getPlugin(),
 				status.getCode(),
 				status.getMessage(),
 				status.getException());
 
 		// The next level will be one less than the current level...
 		int childDepth =
 			(depth == IResource.DEPTH_ONE)
 				? IResource.DEPTH_ZERO
 				: IResource.DEPTH_INFINITE;
 
 		// Collect the responses in the multistatus.
 		for (int i = 0; i < members.length; i++)
 			multiStatus.add(execute(operation, members[i], childDepth, progress));
 
 		return multiStatus;
 	}
 
 	protected IResource[] getMembers(IResource resource)
 	{
 		if (resource.getType() != IResource.FILE)
 		{
 			try
 			{
 				return ((IContainer) resource).members();
 			}
 			catch (CoreException exception)
 			{
 				exception.printStackTrace();
 				throw new RuntimeException();
 			}
 		} //end-if
 		else
 			return new IResource[0];
 	}
 
 
 }
