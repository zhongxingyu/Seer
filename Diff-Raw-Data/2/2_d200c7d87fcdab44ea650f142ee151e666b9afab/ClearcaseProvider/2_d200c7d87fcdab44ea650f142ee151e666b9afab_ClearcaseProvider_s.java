 package net.sourceforge.eclipseccase;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFileModificationValidator;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.resources.team.IMoveDeleteHook;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations;
 
 public class ClearcaseProvider
 	extends RepositoryProvider
 	implements SimpleAccessOperations
 {
 
 	private IMoveDeleteHook moveHandler = new MoveHandler(this);
 	private IFileModificationValidator modificationValidator =
 		new ModificationHandler(this);
 	private String comment = "";
 
 	public static final String ID =
 		"net.sourceforge.eclipseccase.ClearcaseProvider";
 	public static final String STATE_CHANGE_MARKER_TYPE =
 		"net.sourceforge.eclipseccase.statechangedmarker";
 
 	Boolean isSnapShot = null;
 	
 	public ClearcaseProvider()
 	{
 		super();
 	}
 	
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
 
 	public static ClearcaseProvider getProvider(IResource resource)
 	{
 			RepositoryProvider provider = RepositoryProvider.getProvider(resource.getProject());
 			if (provider instanceof ClearcaseProvider)
 				return (ClearcaseProvider) provider;
 			else
 				return null;
 	}
 	
 	/**
 	 * @see SimpleAccessOperations#get(IResource[], int, IProgressMonitor)
 	 */
 	public void get(IResource[] resources, int depth, IProgressMonitor progress)
 		throws TeamException
 	{
 		execute(new IIterativeOperation()
 		{
 			public IStatus visit(IResource resource, int depth, IProgressMonitor progress)
 			{
 				IStatus result =
 					new Status(IStatus.OK, ID, TeamException.OK, "OK", null);
 				String filename = resource.getLocation().toOSString();
				IClearcase.Status status = ClearcasePlugin.getEngine().cleartool("update -ptime " + filename);
 				changeState(resource, IResource.DEPTH_INFINITE, progress);
 				if (!status.status)
 				{
 					result =
 						new Status(
 							IStatus.ERROR,
 							ID,
 							TeamException.UNABLE,
 							"Update failed: " + status.message,
 							null);
 				}
 				return result;
 			}
 		}, resources, IResource.DEPTH_INFINITE, progress);
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
 		try
 		{
 			execute(new IRecursiveOperation()
 			{
 				public IStatus visit(IResource resource, IProgressMonitor progress)
 				{
 					IStatus result =
 						new Status(IStatus.OK, ID, TeamException.OK, "OK", null);
 					boolean reserved = ClearcasePlugin.isReservedCheckouts();
 					IClearcase.Status status =
 						ClearcasePlugin.getEngine().checkout(resource.getLocation().toOSString(), "", reserved, true);
 					changeState(resource, IResource.DEPTH_ZERO, progress);
 					if (!status.status)
 					{
 						result =
 							new Status(
 								IStatus.ERROR,
 								ID,
 								TeamException.UNABLE,
 								"Checkout failed: " + status.message,
 								null);
 					}
 					return result;
 				}
 			}, resources, depth, progress);
 		}
 		finally
 		{
 			comment = "";
 		}
 	}
 
 	public void refresh(
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
 					new Status(IStatus.OK, ID, TeamException.OK, "OK", null);
 				changeClearcaseState(resource, IResource.DEPTH_ZERO, progress);
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
 		try
 		{
 			execute(new IRecursiveOperation()
 			{
 				public IStatus visit(IResource resource, IProgressMonitor progress)
 				{
 					IStatus result =
 						new Status(IStatus.OK, ID, TeamException.OK, "OK", null);
 					IClearcase.Status status =
 						ClearcasePlugin.getEngine().checkin(resource.getLocation().toOSString(), comment, true);
 					changeState(resource, IResource.DEPTH_ZERO, progress);
 					if (!status.status)
 					{
 						result =
 							new Status(
 								IStatus.ERROR,
 								ID,
 								TeamException.UNABLE,
 								"Checkin failed: " + status.message,
 								null);
 					}
 					return result;
 				}
 			}, resources, depth, progress);
 		}
 		finally
 		{
 			comment = "";
 		}
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
 					new Status(IStatus.OK, ID, TeamException.OK, "OK", null);
 				IClearcase.Status status =
 					ClearcasePlugin.getEngine().uncheckout(resource.getLocation().toOSString(), false);
 				changeState(resource, IResource.DEPTH_ONE, progress);
 				if (!status.status)
 				{
 					result =
 						new Status(
 							IStatus.ERROR,
 							ID,
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
 					IClearcase.Status status =
 						ClearcasePlugin.getEngine().delete(resource.getLocation().toOSString(), "");
 					StateCacheFactory.getInstance().remove(resource);
 					changeState(resource.getParent(), IResource.DEPTH_ONE, progress);
 					if (!status.status)
 					{
 						result =
 							new Status(
 								IStatus.ERROR,
 								ID,
 								TeamException.UNABLE,
 								"Delete failed: " + status.message,
 								null);
 					}
 				}
 				return result;
 			}
 		}, resources, IResource.DEPTH_INFINITE, progress);
 	}
 
 	public void add(IResource[] resources, int depth, IProgressMonitor progress)
 		throws TeamException
 	{
 		try
 		{
 			execute(new IRecursiveOperation()
 			{
 				public IStatus visit(IResource resource, IProgressMonitor progress)
 				{
 					IStatus result;
 			
 					// Sanity check - can't add something that already is under VC
 					if (hasRemote(resource))
 					{
 						return new Status(
 							IStatus.ERROR,
 							ID,
 							TeamException.UNABLE,
 							"Cannot add an element already under version control: " + resource.toString(),
 							null);
 					}
 			
 					// Walk up parent heirarchy, find first ccase
 					// element that is a parent, and walk back down, adding each to ccase
 					IResource parent = resource.getParent();
 			
 					// When resource is a project, try checkout its parent, and if that fails,
 					// then neither project nor workspace is in clearcase.
 					if (resource instanceof IProject || hasRemote(parent))
 					{
 						result = checkoutParent(resource);
 					}
 					else
 					{
 						result = visit(parent, progress);
 					}
 			
 					if (result.isOK())
 					{
 						if (resource instanceof IFolder)
 						{
 							try
 							{
 								IFolder folder = (IFolder) resource;
 								IPath mkelemPath = folder.getFullPath().addFileExtension("mkelem");
 								folder.move(mkelemPath, true, false, null);
 								IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 								IFolder mkelemFolder = root.getFolder(mkelemPath);
 								IClearcase.Status status =
 									ClearcasePlugin.getEngine().add(folder.getLocation().toOSString(), "", true);
 								if (status.status)
 								{
 									changeState(folder.getParent(), IResource.DEPTH_ONE, progress);
 									IResource[] members =
 										mkelemFolder.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
 									for (int i = 0; i < members.length; i++)
 									{
 										IResource member = members[i];
 										IPath newPath = folder.getFullPath().append(member.getName());
 										member.move(newPath, true, progress);
 									}
 									mkelemFolder.delete(true, false, progress);
 								}
 								else
 								{
 									result =
 										new Status(
 											IStatus.ERROR,
 											ID,
 											TeamException.UNABLE,
 											"Add failed: " + status.message,
 											null);
 								}
 			
 							}
 							catch (CoreException ex)
 							{
 								result = ex.getStatus();
 							}
 						}
 						else
 						{
 							IClearcase.Status status =
 								ClearcasePlugin.getEngine().add(resource.getLocation().toOSString(), "", false);
 							changeState(resource, IResource.DEPTH_ZERO, progress);
 							if (!status.status)
 							{
 								result =
 									new Status(
 										IStatus.ERROR,
 										ID,
 										TeamException.UNABLE,
 										"Add failed: " + status.message,
 										null);
 							}
 						}
 			
 					}
 			
 					return result;
 				}
 			}, resources, depth, progress);
 		}
 		finally
 		{
 			comment = "";
 		}
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
 		return StateCacheFactory.getInstance().get(resource).isCheckedOut();
 	}
 
 	/**
 	 * @see SimpleAccessOperations#isSnapShot(IResource)
 	 */
 	public boolean isSnapShot()
 	{
 		if (isSnapShot == null)
 		{
 			// no need to calculate this for each resource as all resources
 			// within a project must belong to the same view.
 			isSnapShot = new Boolean(ClearcasePlugin.getEngine().isSnapShot(getProject().getLocation().toOSString()));
 		}
 		return isSnapShot.booleanValue();
 	}
 
 	/**
 	 * @see SimpleAccessOperations#hasRemote(IResource)
 	 */
 	public boolean hasRemote(IResource resource)
 	{
 		return StateCacheFactory.getInstance().get(resource).hasRemote();
 	}
 
 	/**
 	 * @see SimpleAccessOperations#isDirty(IResource)
 	 */
 	public boolean isDirty(IResource resource)
 	{
 		return StateCacheFactory.getInstance().get(resource).isDirty();
 	}
 
 	public String getVersion(IResource resource)
 	{
 		return StateCacheFactory.getInstance().get(resource).getVersion();
 	}
 	
 	public String getViewName(IResource resource)
 	{
 		IClearcase.Status status = ClearcasePlugin.getEngine().getViewName(resource.getLocation().toOSString());
 		if (status.status)
 			return status.message.trim();
 		else
 			return "none";
 	}
 	
 	public IStatus move(IResource source, IResource destination)
 	{
 		IStatus result = checkoutParent(source);
 
 		if (result.isOK())
 			result = checkoutParent(destination);
 
 		if (result.isOK())
 		{
 			IClearcase.Status ccStatus =
 				ClearcasePlugin.getEngine().move(
 					source.getLocation().toOSString(),
 					destination.getLocation().toOSString(),
 					"");
 			StateCacheFactory.getInstance().remove(source);
 			changeState(source.getParent(), IResource.DEPTH_ZERO, null);
 			changeState(destination.getParent(), IResource.DEPTH_ZERO, null);
 		}
 		return result;
 	}
 
 	public IStatus checkoutParent(IResource resource)
 	{
 		IStatus result =
 			new Status(IStatus.OK, ID, TeamException.OK, "OK", null);
 		String parent = null;
 		// IProject's parent is the workspace directory, we want the filesystem
 		// parent if the workspace is not itself in clearcase
 		boolean flag = resource instanceof IProject && !hasRemote(resource.getParent());
 		if (flag)
 		{
 			parent = resource.getLocation().toFile().getParent().toString();
 		}
 		else
 		{
 			parent = resource.getParent().getLocation().toOSString();
 		}
 		if (!ClearcasePlugin.getEngine().isCheckedOut(parent))
 		{
 			IClearcase.Status ccStatus = ClearcasePlugin.getEngine().checkout(parent, "", false, true);
 			if (! flag)
 				changeState(resource.getParent(), IResource.DEPTH_ZERO, null);
 			if (!ccStatus.status)
 			{
 				result =
 					new Status(
 						IStatus.ERROR,
 						ID,
 						TeamException.UNABLE,
 						"Could not check out parent: " + ccStatus.message,
 						null);
 			}
 		}
 		return result;
 	}
 
 	// Notifies decorator that state has changed for an element
 	private void changeState(
 		IResource resource,
 		int depth,
 		IProgressMonitor monitor)
 	{
 		try
 		{
 			changeClearcaseState(resource, depth, monitor);
 			resource.refreshLocal(depth, monitor);
 		}
 		catch (CoreException ex)
 		{
 		}
 	}
 
 	// Notifies decorator that state has changed for an element
 	private void changeClearcaseState(
 		IResource resource,
 		int depth,
 		IProgressMonitor monitor)
 	{
 		try
 		{
 			// probably overkill/expensive to do it here - should do it on a
 			// case by case basis for eac method that actually changes state
 			StateCache cache = StateCacheFactory.getInstance().get(resource);
 			cache.update();
 
 			// This is a hack until I get around to creating my own state change mechanism for decorators
 			// create a marker and set attribute so decorator gets notified without the resource actually
 			// changing (so refactoring doesn't fail).  Should we delete the marker?
 			IMarker[] markers =
 				resource.findMarkers(
 					ClearcaseProvider.STATE_CHANGE_MARKER_TYPE,
 					false,
 					IResource.DEPTH_ZERO);
 			IMarker marker = null;
 			if (markers.length == 0)
 			{
 				marker = resource.createMarker(STATE_CHANGE_MARKER_TYPE);
 			}
 			else
 			{
 				marker = markers[0];
 			}
 			marker.setAttribute("statechanged", true);
 		}
 		catch (CoreException ex)
 		{
 		}
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
