 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas from Eclipse
  *     Gunnar Wagenknecht - new features, enhancements and bug fixes
  *******************************************************************************/
 package net.sourceforge.eclipseccase;
 
 import java.io.File;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sourceforge.clearcase.ClearCase;
 import net.sourceforge.clearcase.ClearCaseElementState;
 import net.sourceforge.clearcase.ClearCaseException;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceRuleFactory;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.resources.team.FileModificationValidator;
 import org.eclipse.core.resources.team.IMoveDeleteHook;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.team.core.Team;
 import org.eclipse.team.core.TeamException;
 
 /**
  * The ClearCase repository provider.
  */
 public class ClearcaseProvider extends RepositoryProvider {
 
 	/** trace id */
 	private static final String TRACE_ID_IS_IGNORED = "ClearcaseProvider#isIgnored"; //$NON-NLS-1$
 
 	UncheckOutOperation UNCHECK_OUT = new UncheckOutOperation();
 
 	CheckInOperation CHECK_IN = new CheckInOperation();
 
 	CheckOutOperation CHECKOUT = new CheckOutOperation();
 
 	AddOperation ADD = new AddOperation();
 
 	RefreshStateOperation REFRESH_STATE = new RefreshStateOperation();
 
 	private IMoveDeleteHook moveHandler = new MoveHandler(this);
 
 	private String comment = ""; //$NON-NLS-1$
 
 	public static final String ID = "net.sourceforge.eclipseccase.ClearcaseProvider"; //$NON-NLS-1$
 
 	public static final Status OK_STATUS = new Status(IStatus.OK, ID,
 			TeamException.OK, "OK", null); //$NON-NLS-1$
 
 	public static final IStatus CANCEL_STATUS = Status.CANCEL_STATUS;
 
 	public ClearcaseProvider() {
 		super();
 	}
 
 	UpdateOperation UPDATE = new UpdateOperation();
 
 	DeleteOperation DELETE = new DeleteOperation();
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.team.core.RepositoryProvider#configureProject()
 	 */
 	public void configureProject() throws CoreException {
 		// configureProject
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.team.core.RepositoryProvider#getID()
 	 */
 	public String getID() {
 		return ID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
 	 */
 	public void deconfigure() throws CoreException {
 		// deconfigure
 	}
 
 	public static ClearcaseProvider getClearcaseProvider(IResource resource) {
 		if (null == resource || null == resource.getProject())
 			return null;
 		RepositoryProvider provider = RepositoryProvider.getProvider(resource
 				.getProject());
 		if (provider instanceof ClearcaseProvider)
 			return (ClearcaseProvider) provider;
 		else
 			return null;
 	}
 
 	/*
 	 * @see SimpleAccessOperations#get(IResource[], int, IProgressMonitor)
 	 */
 	public void get(IResource[] resources, int depth, IProgressMonitor progress)
 			throws TeamException {
 		execute(UPDATE, resources, depth, progress);
 	}
 
 	/*
 	 * @see SimpleAccessOperations#checkout(IResource[], int, IProgressMonitor)
 	 */
 	public void checkout(IResource[] resources, int depth,
 			IProgressMonitor progress) throws TeamException {
 		try {
 			execute(CHECKOUT, resources, depth, progress);
 		} finally {
 			setComment("");
 		}
 	}
 
 	/**
 	 * Invalidates the state cache of all specified resources.
 	 * 
 	 * @param resourceToRefresh
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	public void refreshRecursive(IResource resourceToRefresh,
 			IProgressMonitor monitor) throws CoreException {
 
 		try {
 			monitor.beginTask("Refreshing " + resourceToRefresh.getName(), 50);
 			final List toRefresh = new ArrayList(80);
 			monitor.subTask("collecting members");
 			resourceToRefresh.accept(new IResourceVisitor() {
 
 				public boolean visit(IResource resource) throws CoreException {
 					if (!Team.isIgnoredHint(resource))
 						toRefresh.add(resource);
 					return true;
 				}
 			});
 			monitor.worked(30);
 			monitor.subTask("scheduling updates");
 			if (!toRefresh.isEmpty()) {
 				StateCacheFactory.getInstance().refreshStateAsyncHighPriority(
 						(IResource[]) toRefresh.toArray(new IResource[toRefresh
 								.size()]));
 			}
 			monitor.worked(10);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	/**
 	 * Invalidates the state of the specified resource and only of the specified
 	 * resource
 	 * 
 	 * @param resources
 	 */
 	public void refresh(IResource resources) {
 		StateCacheFactory.getInstance().get(resources).updateAsyncHighPriority(
 				false);
 	}
 
 	/*
 	 * @see SimpleAccessOperations#checkin(IResource[], int, IProgressMonitor)
 	 */
 	public void checkin(IResource[] resources, int depth,
 			IProgressMonitor progressMonitor) throws TeamException {
 		try {
 			execute(CHECK_IN, resources, depth, progressMonitor);
 		} finally {
 			setComment("");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.team.core.RepositoryProvider#getRuleFactory()
 	 */
 	public IResourceRuleFactory getRuleFactory() {
 		return new ClearcaseResourceRuleFactory();
 	}
 
 	/**
 	 * @see SimpleAccessOperations#uncheckout(IResource[], int,
 	 *      IProgressMonitor)
 	 */
 	public void uncheckout(IResource[] resources, int depth,
 			IProgressMonitor progress) throws TeamException {
 		execute(UNCHECK_OUT, resources, depth, progress);
 	}
 
 	/**
 	 * @see SimpleAccessOperations#delete(IResource[], IProgressMonitor)
 	 */
 	public void delete(IResource[] resources, IProgressMonitor progress)
 			throws TeamException {
 		try {
 			execute(DELETE, resources, IResource.DEPTH_INFINITE, progress);
 		} finally {
 			setComment("");
 		}
 	}
 
 	public void add(IResource[] resources, int depth, IProgressMonitor progress)
 			throws TeamException {
 		try {
 			execute(ADD, resources, depth, progress);
 		} finally {
 			setComment("");
 		}
 	}
 	
 	/*
 	 * @see SimpleAccessOperations#moved(IPath, IResource, IProgressMonitor)
 	 */
 	public void moved(IPath source, IResource target, IProgressMonitor progress)
 			throws TeamException {
 		// moved
 	}
 
 	/**
 	 * @see SimpleAccessOperations#isCheckedOut(IResource)
 	 */
 	public boolean isCheckedOut(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).isCheckedOut();
 	}
 
 	/**
 	 * Indicates if the specified resource is contained in a Snapshot view.
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	public boolean isSnapShot(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).isSnapShot();
 	}
 
 	public boolean isHijacked(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).isHijacked();
 	}
 
 	public boolean isUnknownState(IResource resource) {
 		return StateCacheFactory.getInstance().isUnitialized(resource);
 	}
 
 	/**
 	 * @see SimpleAccessOperations#hasRemote(IResource)
 	 */
 	public boolean hasRemote(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).hasRemote();
 	}
 
 	/*
 	 * @see SimpleAccessOperations#isDirty(IResource)
 	 */
 	public boolean isDirty(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).isDirty();
 	}
 
 	public String getVersion(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).getVersion();
 	}
 
 	public String getPredecessorVersion(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource)
 				.getPredecessorVersion();
 	}
 
 	public String getViewName(IResource resource) {
 		return ClearcasePlugin.getEngine().getViewName(
 				resource.getLocation().toOSString());
 	}
 
 	/**
 	 * Returns the root of the view. An empty view root indicates a dynamic
 	 * view.
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	public String getViewRoot(IResource resource) throws TeamException {
 		return ClearcasePlugin.getEngine().getViewLocation(
 				getViewName(resource));
 	}
 
 	/**
 	 * Returns the name of the vob that contains the specified element
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	public String getVobName(IResource resource) throws TeamException {
 		String viewRoot = getViewRoot(resource);
 		IPath viewLocation = new Path(viewRoot);
 		IPath resourceLocation = resource.getLocation();
 		// ignore device when dealing with dynamic views
 		if (viewRoot.length() == 0)
 			viewLocation = viewLocation.setDevice(resourceLocation.getDevice());
 		if (viewLocation.isPrefixOf(resourceLocation)) {
 			IPath vobLocation = resourceLocation
 					.removeFirstSegments(viewLocation.segmentCount());
 			if (!ClearcasePlugin.isWindows() && vobLocation.segmentCount() > 0) {
 				// on unix vobs are prefixed with directory named "/vobs"
 				vobLocation = vobLocation.removeFirstSegments(1);
 			}
 			if (vobLocation.segmentCount() > 0)
 				return vobLocation.segment(0);
 		}
 		return "none";
 	}
 
 	/**
 	 * Returns the vob relative path of the specified element
 	 * 
 	 * @param resource
 	 * @return the vob relativ path (maybe <code>null</code> if outside vob)
 	 */
 	public String getVobRelativPath(IResource resource) throws TeamException {
 		String viewRoot = getViewRoot(resource);
 		IPath viewLocation = new Path(viewRoot).setDevice(null); // ignore
 		// device
 		IPath resourceLocation = resource.getLocation().setDevice(null); // ignore
 		// devices
 		if (viewLocation.isPrefixOf(resourceLocation)) {
 			IPath vobLocation = resourceLocation
 					.removeFirstSegments(viewLocation.segmentCount());
 			if (!ClearcasePlugin.isWindows() && vobLocation.segmentCount() > 0) {
 				// on unix vobs are prefixed with directory named "/vobs"
 				vobLocation = vobLocation.removeFirstSegments(1);
 			}
 			if (vobLocation.segmentCount() > 0)
 				return vobLocation.removeFirstSegments(1).makeRelative()
 						.toString();
 		}
 		return null;
 	}
 
 	public IStatus move(IResource source, IResource destination,
 			IProgressMonitor monitor) {
 		try {
 			monitor.beginTask("Moving " + source.getFullPath() + " to "
 					+ destination.getFullPath(), 100);
 			// Sanity check - can't move something that is not part of clearcase
 			if (!hasRemote(source)) {
 				return new Status(
 						IStatus.ERROR,
 						ID,
 						TeamException.NO_REMOTE_RESOURCE,
 						MessageFormat
 								.format(
 										"Resource \"{0}\" is not under source control!",
 										new Object[] { source.getFullPath()
 												.toString() }), null);
 			}
 			IStatus result = checkoutParent(source, new SubProgressMonitor(
 					monitor, 10));
 			if (result.isOK())
 				result = checkoutParent(destination, new SubProgressMonitor(
 						monitor, 10));
 			if (result.isOK()) {
 				ClearCaseElementState state = ClearcasePlugin.getEngine().move(
 						source.getLocation().toOSString(),
 						destination.getLocation().toOSString(), getComment(),
 						ClearCase.FORCE, null);
 				monitor.worked(40);
 				StateCacheFactory.getInstance().remove(source);
 				updateState(source.getParent(), IResource.DEPTH_ZERO,
 						new SubProgressMonitor(monitor, 10));
 				updateState(destination.getParent(), IResource.DEPTH_ZERO,
 						new SubProgressMonitor(monitor, 10));
 				updateState(destination, IResource.DEPTH_INFINITE,
 						new SubProgressMonitor(monitor, 10));
 				if (!state.isMoved()) {
 					return new Status(IStatus.ERROR, ID, TeamException.UNABLE,
 							"Could not move element: "
 							// + ccStatus.message
 							, null);
 				}
 			}
 			return result;
 		} finally {
 			setComment("");
 		}
 	}
 
 	public IStatus checkoutParent(IResource resource, IProgressMonitor monitor) {
 		try {
 			monitor.beginTask("Checking out "
 					+ resource.getParent().getFullPath().toString(), 10);
 			IStatus result = OK_STATUS;
 			String parent = null;
 			// IProject's parent is the workspace directory, we want the
 			// filesystem
 			// parent if the workspace is not itself in clearcase
 			boolean flag = resource instanceof IProject
 					&& !hasRemote(resource.getParent());
 			if (flag) {
 				parent = resource.getLocation().toFile().getParent().toString();
 			} else {
 				parent = resource.getParent().getLocation().toOSString();
 			}
 			monitor.worked(2);
 			ClearCaseElementState elementState = ClearcasePlugin.getEngine()
 					.getElementState(parent);
 			if (!elementState.isElement()) {
 				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
 						"Could not find a parent that is a clearcase element",
 						null);
 				return result;
 			}
 			monitor.worked(2);
 			if (!elementState.isCheckedOut() && !elementState.isLink()) {
 				String[] element = { parent };
 				// TODO: In old we used
 				// ClearcasePlugin.isReservedCheckoutsAlways(). How to handle
 				// that.
 				ClearCaseElementState[] elementState2 = ClearcasePlugin
 						.getEngine().checkout(element, getComment(), 0, null);
 				// ClearCaseInterface.Status ccStatus = ClearcasePlugin
 				// .getEngine().checkout(parent, getComment(),
 				// ClearcasePlugin.isReservedCheckoutsAlways(),
 				// true);
 
 				monitor.worked(4);
 				if (!flag)
 					updateState(resource.getParent(), IResource.DEPTH_ZERO,
 							new SubProgressMonitor(monitor, 10));
 				if (elementState2 == null) {
 					// TODO: Handle ccStatus.message.
 					result = new Status(IStatus.ERROR, ID,
 							TeamException.UNABLE,
 							"Could not check out parent: " + "ccStatus", null);
 				}
 			}
 			return result;
 		} finally {
 			monitor.done();
 		}
 	}
 
 	boolean refreshResources = true;
 
 	// Notifies decorator that state has changed for an element
 	void updateState(IResource resource, int depth, IProgressMonitor monitor) {
 		try {
 			monitor.beginTask("Refreshing " + resource.getFullPath(), 20);
 			if (!refreshResources) {
 				StateCacheFactory.getInstance().removeSingle(resource);
 				monitor.worked(10);
 			} else {
 				resource.refreshLocal(depth,
 						new SubProgressMonitor(monitor, 10));
 			}
 
 			if (resource.exists()) {
 				doUpdateState(resource, depth, new SubProgressMonitor(monitor,
 						10));
 			} else {
 				StateCacheFactory.getInstance().refreshStateAsyncHighPriority(
 						new IResource[] { resource });
 			}
 		} catch (CoreException ex) {
 			ClearcasePlugin.log(IStatus.ERROR,
 					"Error refreshing ClearCase state: " + ex.getMessage(), ex);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	private IStatus doUpdateState(IResource resource, int depth,
 			IProgressMonitor progressMonitor) {
 		IStatus result = execute(REFRESH_STATE, resource, depth,
 				progressMonitor);
 		return result;
 	}
 
 	/**
 	 * @see RepositoryProvider#getMoveDeleteHook()
 	 */
 	public IMoveDeleteHook getMoveDeleteHook() {
 		return moveHandler;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.team.core.RepositoryProvider#getFileModificationValidator2()
 	 */
 	public FileModificationValidator getFileModificationValidator2() {
 		return ClearcasePlugin.getInstance().getClearcaseModificationHandler();
 	}
 
 	/**
 	 * Gets the comment.
 	 * 
 	 * @return Returns a String
 	 */
 	public String getComment() {
 		return comment;
 	}
 
 	/**
 	 * Sets the comment.
 	 * 
 	 * @param comment
 	 *            The comment to set
 	 */
 	public void setComment(String comment) {
 		// escape comment if enabled
 		// if (comment.trim().length() > 0 && ClearcasePlugin.isCommentEscape())
 		// comment = ClearcaseUtil.getEscaped(comment);
 		this.comment = comment;
 	}
 
 	// Out of sheer laziness, I appropriated the following code from the team
 	// provider example =)
 	private final class RefreshStateOperation implements IRecursiveOperation {
 
 		public IStatus visit(IResource resource, IProgressMonitor monitor) {
 			try {
 				monitor.beginTask("Refreshing State " + resource.getFullPath(),
 						10);
 				// probably overkill/expensive to do it here - should do it
 				// on a
 				// case by case basis for eac method that actually changes
 				// state
 				StateCache cache = StateCacheFactory.getInstance()
 						.get(resource);
 				// force update immediately
 				cache.doUpdate();
 				// check if a symbolic link target is also in our workspace
 				if (cache.isSymbolicLink()
 						&& null != cache.getSymbolicLinkTarget()) {
 					File target = new File(cache.getSymbolicLinkTarget());
 					if (!target.isAbsolute()) {
 						target = null != cache.getPath() ? new File(cache
 								.getPath()).getParentFile() : null;
 						if (null != target)
 							target = new File(target, cache
 									.getSymbolicLinkTarget());
 					}
 					if (null != target && target.exists()) {
 						IPath targetLocation = new Path(target
 								.getAbsolutePath());
 						IResource[] resources = null;
 						if (target.isDirectory()) {
 							resources = ResourcesPlugin.getWorkspace()
 									.getRoot().findContainersForLocation(
 											targetLocation);
 						} else {
 							resources = ResourcesPlugin.getWorkspace()
 									.getRoot().findFilesForLocation(
 											targetLocation);
 						}
 						if (null != resources)
 							for (int i = 0; i < resources.length; i++) {
 								IResource foundResource = resources[i];
 								ClearcaseProvider provider = ClearcaseProvider
 										.getClearcaseProvider(foundResource);
 								if (null != provider)
 									StateCacheFactory.getInstance().get(
 											foundResource).updateAsync(false);
 							}
 					}
 				}
 				return OK_STATUS;
 			} finally {
 				monitor.done();
 			}
 		}
 	}
 
 	private final class AddOperation implements IRecursiveOperation {
 
 		public IStatus visit(IResource resource, IProgressMonitor monitor) {
 			try {
 				monitor.beginTask(
 						"Adding " + resource.getFullPath().toString(), 100);
 				IStatus result;
 				// Sanity check - can't add something that already is under VC
 				if (hasRemote(resource)) {
 					// return status with severity OK
 					return new Status(
 							IStatus.OK,
 							ID,
 							TeamException.UNABLE,
 							MessageFormat
 									.format(
 											"Resource \"{0}\" is already under source control!",
 											new Object[] { resource
 													.getFullPath().toString() }),
 							null);
 				}
 
 				IResource parent = resource.getParent();
 
 				// When resource is a project, try checkout its parent, and if
 				// that fails,
 				// then neither project nor workspace is in clearcase.
 				if (resource instanceof IProject || hasRemote(parent)) {
 					result = checkoutParent(resource, new SubProgressMonitor(
 							monitor, 10));
 				} else {
 					result = visit(parent, new SubProgressMonitor(monitor, 10));
 				}
 
 				// Now make elements of the view private files if parent could
 				// be checked-out.
 				if (result.isOK()) {
 					if (resource.getType() == IResource.FOLDER) {
 						result = makeFolderElement(resource, monitor);
 					} else if (resource.getType() == IResource.FILE) {
 						result = makeFileElement(resource, monitor);
 					}
 				}
 				// refresh state on all elements.
 				monitor.worked(40);
				updateState(parent, IResource.DEPTH_INFINITE,
 						new SubProgressMonitor(monitor, 10));
 				return result;
 			} finally {
 				monitor.done();
 			}
 		}
 	}
 
 	private Status makeFileElement(IResource resource, IProgressMonitor monitor) {
 		Status result = OK_STATUS;
 
 		ClearCaseElementState state = ClearcasePlugin.getEngine().add(
 				resource.getLocation().toOSString(), false, getComment(),
 				ClearCase.PTIME | ClearCase.MASTER | ClearCase.CHECKIN, null);
 		if (state.isCheckedIn()) {
 			// check-in parent dir
 			String[] dir = { resource.getParent().getLocation().toOSString() };
 			ClearCaseElementState[] stateB = ClearcasePlugin.getEngine()
 					.checkin(dir, comment, 0, null);
 		} else {
 			result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
 					"Add failed: " + "Could not add element"
 							+ resource.getName(), null);
 		}
 
 		return result;
 	}
 
 	private Status makeFolderElement(IResource resource,
 			IProgressMonitor monitor) {
 
 		IFolder folder = (IFolder) resource;
 		Status result = OK_STATUS;
 		try {
 			IPath path = new Path(folder.getName() + ".tmp");
 			IFolder tmpFolder = resource.getParent().getFolder(path);
 			if (!tmpFolder.exists()) {
 				tmpFolder.create(false, true, new SubProgressMonitor(monitor,
 						10));
 			}
 			// Move content of original directory to tmp
 			IResource[] resources = folder.members();
 			if (resources != null && resources.length > 0) {
 				for (int i = 0; i < resources.length; i++) {
 					IPath renamedPath = tmpFolder.getFullPath().append(
 							resources[i].getName());
 					resources[i].move(renamedPath, false,
 							new SubProgressMonitor(monitor, 10));
 				}
 
 			}
 
 			// Now all content of directory is moved delete
 			// original directory.
 			if (folder.exists()) {
 				folder.delete(true, true, new SubProgressMonitor(monitor, 10));
 			}
 
 			// Now time to create the original directory in
 			// clearcase.
 			ClearCaseElementState state = ClearcasePlugin.getEngine().add(
 					resource.getLocation().toOSString(), true, getComment(), 0,
 					null);
 			if (!state.isElement()) {
 				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
 						"Add failed: " + "Could not add element"
 								+ resource.getName(), null);
 			}
 
 			// Now move back the content of tmp to original.
 			// To avoid CoreException do a refreshLocal(). Does
 			// not recognize the cc created resource directory.
 			resource.refreshLocal(IResource.DEPTH_ZERO, new SubProgressMonitor(
 					monitor, 10));
 			IResource[] tmpResources = tmpFolder.members();
 			for (int i = 0; i < tmpResources.length; i++) {
 				IPath renamedPath = folder.getFullPath().append(
 						tmpResources[i].getName());
 				tmpResources[i].move(renamedPath, true, new SubProgressMonitor(
 						monitor, 10));
 			}
 
 			// Remove the temporary.
 			if (tmpFolder.exists()) {
 				tmpFolder.delete(true, true,
 						new SubProgressMonitor(monitor, 10));
 			}
 
 			// Check-in parent since since new directory is now
 			// created.
 			String[] parentResource = { resource.getParent().getLocation()
 					.toOSString() };
 
 			ClearCaseElementState[] stateB = ClearcasePlugin.getEngine()
 					.checkin(parentResource, comment, 0,
 
 					null);
 			// hasRemote checks if element is a clearcase element.
 			// if(stateB[0].isCheckedOut()){
 			//			
 			// result = new Status(
 			// IStatus.ERROR,
 			// ID,
 			// TeamException.UNABLE,
 			// "Add failed: "
 			// + "Could not check-in directory"
 			// + resource.getName(), null);
 			// }
 
 		} catch (CoreException ce) {
 			System.out.println("We got an exception!");
 			ce.printStackTrace();
 			result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
 					"Add failed: " + "Exception" + ce.getMessage(), null);
 		}
 
 		return result;
 	}
 
 	private final class UncheckOutOperation implements IRecursiveOperation {
 
 		public IStatus visit(IResource resource, IProgressMonitor monitor) {
 			try {
 				monitor.beginTask("Uncheckout " + resource.getFullPath(), 100);
 				// Sanity check - can't process something that is not part of
 				// clearcase
 				if (!hasRemote(resource)) {
 					return new Status(
 							IStatus.WARNING,
 							ID,
 							TeamException.NO_REMOTE_RESOURCE,
 							MessageFormat
 									.format(
 											"Resource \"{0}\" is not a ClearCase element!",
 											new Object[] { resource
 													.getFullPath().toString() }),
 							null);
 				}
 				// Sanity check - can't uncheckout something that is not checked
 				// out
 				if (!isCheckedOut(resource)) {
 					// return severity OK
 					return new Status(IStatus.OK, ID,
 							TeamException.NOT_CHECKED_OUT,
 							MessageFormat.format(
 									"Resource \"{0}\" is not checked out!",
 									new Object[] { resource.getFullPath()
 											.toString() }), null);
 				}
 				IStatus result = OK_STATUS;
 				ClearcasePlugin.getEngine().uncheckout(
 						new String[] { resource.getLocation().toOSString() },
 						ClearCase.RECURSIVE | ClearCase.KEEP, null);
 				monitor.worked(40);
 				updateState(resource, IResource.DEPTH_ZERO,
 						new SubProgressMonitor(monitor, 10));
 				// if (!status.status) {
 				// result = new Status(IStatus.ERROR, ID,
 				// TeamException.UNABLE, "Uncheckout failed: "
 				// + status.message, null);
 				// }
 				return result;
 			} finally {
 				monitor.done();
 			}
 		}
 	}
 
 	private final class DeleteOperation implements IIterativeOperation {
 
 		public IStatus visit(IResource resource, int depth,
 				IProgressMonitor monitor) {
 			try {
 				monitor.beginTask("Deleting " + resource.getFullPath(), 100);
 				// Sanity check - can't delete something that is not part of
 				// clearcase
 				if (!hasRemote(resource)) {
 					return new Status(
 							IStatus.ERROR,
 							ID,
 							TeamException.NO_REMOTE_RESOURCE,
 							MessageFormat
 									.format(
 											"Resource \"{0}\" is not a ClearCase element!",
 											new Object[] { resource
 													.getFullPath().toString() }),
 							null);
 				}
 				IStatus result = checkoutParent(resource,
 						new SubProgressMonitor(monitor, 10));
 				if (result.isOK()) {
 					ClearcasePlugin.getEngine()
 							.delete(
 									new String[] { resource.getLocation()
 											.toOSString() }, getComment(),
 									ClearCase.RECURSIVE | ClearCase.KEEP, null);
 					monitor.worked(40);
 					updateState(resource, IResource.DEPTH_INFINITE,
 							new SubProgressMonitor(monitor, 10));
 					// if (!status.status) {
 					// result = new Status(IStatus.ERROR, ID,
 					// TeamException.UNABLE, "Delete failed: "
 					// + status.message, null);
 					// }
 				}
 				return result;
 			} finally {
 				monitor.done();
 			}
 		}
 
 	}
 	private final class CheckInOperation implements IRecursiveOperation {
 
 		public IStatus visit(IResource resource, IProgressMonitor monitor) {
 			try {
 				monitor.beginTask("Checkin in " + resource.getFullPath(), 100);
 				// Sanity check - can't check in something that is not part of
 				// clearcase
 				if (!hasRemote(resource)) {
 					return new Status(
 							IStatus.WARNING,
 							ID,
 							TeamException.NO_REMOTE_RESOURCE,
 							MessageFormat
 									.format(
 											"Resource \"{0}\" is not a ClearCase element!",
 											new Object[] { resource
 													.getFullPath().toString() }),
 							null);
 				}
 				// Sanity check - can't checkin something that is not checked
 				// out
 				if (!isCheckedOut(resource)) {
 					// return status with severity OK
 					return new Status(IStatus.OK, ID,
 							TeamException.NOT_CHECKED_OUT,
 							MessageFormat.format(
 									"Resource \"{0}\" is not checked out!",
 									new Object[] { resource.getFullPath()
 											.toString() }), null);
 				}
 				IStatus result = OK_STATUS;
 
 				if (ClearcasePlugin.isCheckinIdenticalAllowed()) {
 					ClearcasePlugin
 							.getEngine()
 							.checkin(
 									new String[] { resource.getLocation()
 											.toOSString() }, getComment(),
 									ClearCase.PTIME | ClearCase.IDENTICAL, null);
 				} else {
 
 					try {
 						ClearcasePlugin.getEngine().checkin(
 								new String[] { resource.getLocation()
 										.toOSString() }, getComment(),
 								ClearCase.PTIME, null);
 					} catch (ClearCaseException cce) {
 						// check error
 						switch (cce.getErrorCode()) {
 						case ClearCase.ERROR_PREDECESSOR_IS_IDENTICAL:
 							result = new Status(
 									IStatus.ERROR,
 									ID,
 									TeamException.NOT_CHECKED_IN,
 									MessageFormat
 											.format(
 													Messages
 															.getString("ClearcasePlugin.error.checkin.identicalPredecessor"),
 													new Object[] { cce
 															.getElements() }),
 									null);
 							break;
 						case ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS:
 							//FIXME:Add message here.
 							result = new Status(
 									IStatus.ERROR,
 									ID,
 									TeamException.NOT_CHECKED_IN,
 									MessageFormat
 											.format(
 													Messages
 															.getString("ClearcasePlugin.error.checkin.elementHasCheckouts"),
 													new Object[] { cce
 															.getElements() }),
 									null);
 							break;
 						case ClearCase.ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION:
 							result = new Status(
 									IStatus.ERROR,
 									ID,
 									TeamException.NOT_CHECKED_IN,
 									MessageFormat
 											.format(
 													Messages
 															.getString("ClearcasePlugin.error.checkin.mergeneeded"),
 													new Object[] { cce
 															.getElements() }),
 									null);
 							//TODO: Add simple Merge
 							//ClearcasePlugin.getEngine().merge(resource.getLocation().toOSString());
 							break;
 
 						default:
 							result = new Status(
 									IStatus.ERROR,
 									ID,
 									TeamException.NOT_CHECKED_IN,
 									MessageFormat
 											.format(
 													Messages
 															.getString("ClearcasePlugin.error.checkin.unknown"),
 													new Object[] { cce
 															.getElements() }),
 									null);
 
 							break;
 						}
 
 					}
 				}
 
 				monitor.worked(40);
 				updateState(resource, IResource.DEPTH_ZERO,
 						new SubProgressMonitor(monitor, 10));
 				// if (!status.status) {
 				// result = new Status(IStatus.ERROR, ID,
 				// TeamException.UNABLE, "Checkin failed: "
 				// + status.message, null);
 				// }
 				return result;
 			} finally {
 				monitor.done();
 			}
 		}
 	}
 
 	private final class CheckOutOperation implements IRecursiveOperation {
 
 		public IStatus visit(IResource resource, IProgressMonitor monitor) {
 			try {
 				monitor.beginTask("Checkin out " + resource.getFullPath(), 100);
 
 				// Sanity check - can't checkout something that is not part of
 				// clearcase
 				if (!hasRemote(resource)) {
 					return new Status(
 							IStatus.WARNING,
 							ID,
 							TeamException.NO_REMOTE_RESOURCE,
 							MessageFormat
 									.format(
 											"Resource \"{0}\" is not a ClearCase element!",
 											new Object[] { resource
 													.getFullPath().toString() }),
 							null);
 				}
 
 				// Sanity check - can't checkout something that is already
 				// checked out
 				if (isCheckedOut(resource)) {
 					// return status with severity OK
 					return new Status(IStatus.OK, ID,
 							TeamException.NOT_CHECKED_IN, MessageFormat.format(
 									"Resource \"{0}\" is already checked out!",
 									new Object[] { resource.getFullPath()
 											.toString() }), null);
 				}
 
 				IStatus result = OK_STATUS;
 
 				// update if necessary
 				if (ClearcasePlugin.isCheckoutLatest() && isSnapShot(resource)) {
 					monitor.subTask("Updating " + resource.getName());
 
 					// ClearCaseElementState[] state =
 					// ClearcasePlugin.getEngine()
 					// .update(
 					// new String[] { resource.getLocation()
 					// .toOSString() }, comment, 0, null);
 					ClearcasePlugin.getEngine()
 							.update(
 									new String[] { resource.getLocation()
 											.toOSString() }, comment, 0, null);
 					// if (state[0] == null) {
 					// result = new Status(IStatus.ERROR, ID,
 					// TeamException.UNABLE,
 					// "Update before checkout failed: "
 					// + resource.getName(), null);
 					//
 					// }
 
 //					ClearCaseElementState[] state = ClearcasePlugin.getEngine()
 //							.update(
 //									new String[] { resource.getLocation()
 //											.toOSString() }, comment, 0, null);
 //					if (state[0] == null) {
 //						result = new Status(IStatus.ERROR, ID,
 //								TeamException.UNABLE,
 //								"Update before checkout failed: "
 //										+ resource.getName(), null);
 //
 //					}
 					
 					//FIXME: Handle exceptions from update.
 				}
 				monitor.worked(20);
 
 				// only checkout if update was successful
 				if (result == OK_STATUS) {
 					monitor.subTask("Checking out " + resource.getName());
 					ClearCaseElementState[] state = null;
 
 					boolean reserved = ClearcasePlugin
 							.isReservedCheckoutsAlways()
 							|| ClearcasePlugin.isReservedCheckoutsIfPossible();
 					if (!reserved) {
 
 						// unreserved
 						state = ClearcasePlugin.getEngine().checkout(
 								new String[] { resource.getLocation()
 										.toOSString() }, getComment(),
 								ClearCase.UNRESERVED | ClearCase.PTIME, null);
 					} else {
 						// reserved
 						state = ClearcasePlugin.getEngine().checkout(
 								new String[] { resource.getLocation()
 										.toOSString() }, getComment(),
 								ClearCase.RESERVED | ClearCase.PTIME, null);
 					}
 
 					monitor.worked(20);
 
 					if (state == null) {
 						result = new Status(IStatus.ERROR, ID,
 								TeamException.UNABLE, "Checkout failed: "
 										+ resource.getName(), null);
 					}
 
 				}
 				monitor.worked(20);
 
 				// update state
 				updateState(resource, IResource.DEPTH_ZERO,
 						new SubProgressMonitor(monitor, 10));
 				return result;
 			} finally {
 				monitor.done();
 			}
 		}
 	}
 
 	private final class UpdateOperation implements IIterativeOperation {
 
 		public IStatus visit(IResource resource, int depth,
 				IProgressMonitor monitor) {
 			try {
 				monitor.beginTask("Updating " + resource.getFullPath(), 100);
 
 				// Sanity check - can't update something that is not part of
 				// clearcase
 				if (!hasRemote(resource)) {
 					return new Status(
 							IStatus.ERROR,
 							ID,
 							TeamException.NO_REMOTE_RESOURCE,
 							MessageFormat
 									.format(
 											"Resource \"{0}\" is not a ClearCase element!",
 											new Object[] { resource
 													.getFullPath().toString() }),
 							null);
 				}
 				IStatus result = OK_STATUS;
 				// String filename = resource.getLocation().toOSString();
 				// ClearCaseInterface.Status status =
 				// ClearcasePlugin.getEngine()
 				// .cleartool(
 				// "update -log NUL -force -ptime "
 				// + ClearcaseUtil.quote(filename));
 				monitor.worked(40);
 				updateState(resource, IResource.DEPTH_INFINITE,
 						new SubProgressMonitor(monitor, 10));
 				// if (!status.status) {
 				// result = new Status(IStatus.ERROR, ID,
 				// TeamException.UNABLE, "Update failed: "
 				// + status.message, null);
 				// }
 				return result;
 			} finally {
 				monitor.done();
 			}
 		}
 	}
 
 	/**
 	 * These interfaces are to operations that can be performed on the array of
 	 * resources, and on all resources identified by the depth parameter.
 	 * 
 	 * @see execute(IOperation, IResource[], int, IProgressMonitor)
 	 */
 	public static interface IOperation {
 		// empty
 	}
 
 	public static interface IIterativeOperation extends IOperation {
 
 		public IStatus visit(IResource resource, int depth,
 				IProgressMonitor progress);
 	}
 
 	public static interface IRecursiveOperation extends IOperation {
 
 		public IStatus visit(IResource resource, IProgressMonitor progress);
 	}
 
 	/**
 	 * Perform the given operation on the array of resources, each to the
 	 * specified depth. Throw an exception if a problem ocurs, otherwise remain
 	 * silent.
 	 */
 	protected void execute(IOperation operation, IResource[] resources,
 			int depth, IProgressMonitor progress) throws TeamException {
 		if (null == progress)
 			progress = new NullProgressMonitor();
 		// Create an array to hold the status for each resource.
 		MultiStatus multiStatus = new MultiStatus(getID(), TeamException.OK,
 				"OK", null);
 		// For each resource in the local resources array until we have errors.
 		try {
 			progress.beginTask("Processing", 1000 * resources.length);
 			for (int i = 0; i < resources.length
 					&& !multiStatus.matches(IStatus.ERROR); i++) {
 				progress.subTask(resources[i].getFullPath().toString());
 				if (!isIgnored(resources[i])) {
 					if (operation instanceof IRecursiveOperation)
 						multiStatus.merge(execute(
 								(IRecursiveOperation) operation, resources[i],
 								depth, new SubProgressMonitor(progress, 1000)));
 					else
 						multiStatus
 								.merge(((IIterativeOperation) operation).visit(
 										resources[i], depth,
 										new SubProgressMonitor(progress, 1000)));
 				} else {
 					progress.worked(1000);
 				}
 			}
 			// Finally, if any problems occurred, throw the exeption with all
 			// the statuses,
 			// but if there were no problems exit silently.
 			if (!multiStatus.isOK()) {
 				String message = multiStatus.matches(IStatus.ERROR) ? "There were errors that prevent the requested operation from finishing successfully."
 						: "The requested operation finished with warnings.";
 				throw new TeamException(new MultiStatus(
 						multiStatus.getPlugin(), multiStatus.getCode(),
 						multiStatus.getChildren(), message, multiStatus
 								.getException()));
 			}
 			// Cause all the resource changes to be broadcast to listeners.
 			// TeamPlugin.getManager().broadcastResourceStateChanges(resources);
 		} finally {
 			progress.done();
 		}
 	}
 
 	/**
 	 * Perform the given operation on a resource to the given depth.
 	 */
 	protected IStatus execute(IRecursiveOperation operation,
 			IResource resource, int depth, IProgressMonitor progress) {
 		if (null == progress)
 			progress = new NullProgressMonitor();
 		try {
 			progress.beginTask("Processing", 1000);
 			// Visit the given resource first.
 			IStatus status = operation.visit(resource, new SubProgressMonitor(
 					progress, 200));
 			// If the resource is a file then the depth parameter is irrelevant.
 			if (resource.getType() == IResource.FILE)
 				return status;
 			// If we are not considering any members of the container then we
 			// are done.
 			if (depth == IResource.DEPTH_ZERO)
 				return status;
 			// If the operation was unsuccessful, do not attempt to go deep.
 			if (status.matches(IStatus.ERROR)) // if (!status.isOK())
 				return status;
 
 			// if operation was cancaled, do not go deep
 			if (CANCEL_STATUS == status)
 				return OK_STATUS;
 
 			// If the container has no children then we are done.
 			IResource[] members = getMembers(resource);
 			if (members.length == 0)
 				return status;
 			// There are children and we are going deep, the response will be a
 			// multi-status.
 			MultiStatus multiStatus = new MultiStatus(status.getPlugin(),
 					status.getCode(), status.getMessage(), status
 							.getException());
 			// The next level will be one less than the current level...
 			int childDepth = (depth == IResource.DEPTH_ONE) ? IResource.DEPTH_ZERO
 					: IResource.DEPTH_INFINITE;
 			// Collect the responses in the multistatus (use merge to flatten
 			// the tree).
 			int ticks = 800 / members.length;
 			for (int i = 0; i < members.length
 					&& !multiStatus.matches(IStatus.ERROR); i++) {
 				progress.subTask(members[i].getFullPath().toString());
 				if (!isIgnored(members[i])) {
 					multiStatus
 							.merge(execute(operation, members[i], childDepth,
 									new SubProgressMonitor(progress, ticks)));
 				} else {
 					progress.worked(ticks);
 				}
 			}
 			// correct the MultiStatus message
 			if (!multiStatus.isOK()) {
 				/*
 				 * Remember: the multi status was created with "OK" as message!
 				 * This is not meaningful anymore. We have to correct it.
 				 */
 				String message = multiStatus.matches(IStatus.ERROR) ? "There were errors that prevent the requested operation from finishing successfully."
 						: "The requested operation finished with warnings.";
 				multiStatus = new MultiStatus(multiStatus.getPlugin(),
 						multiStatus.getCode(), multiStatus.getChildren(),
 						message, multiStatus.getException());
 			}
 			return multiStatus;
 		} finally {
 			progress.done();
 		}
 	}
 
 	protected IResource[] getMembers(IResource resource) {
 		if (resource.getType() != IResource.FILE) {
 			try {
 				return ((IContainer) resource).members();
 			} catch (CoreException exception) {
 				exception.printStackTrace();
 				throw new RuntimeException();
 			}
 		} // end-if
 		else
 			return new IResource[0];
 	}
 
 	/**
 	 * @see org.eclipse.team.core.RepositoryProvider#canHandleLinkedResources()
 	 */
 	public boolean canHandleLinkedResources() {
 		return true;
 	}
 	
 	public boolean canHandleLinkedResourceURI() {
         return true;
     }
     
 
 	/**
 	 * Indicates if a resource is ignored and not handled.
 	 * <p>
 	 * Resources are never ignored, if they have a remote resource.
 	 * </p>
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	public boolean isIgnored(IResource resource) {
 		// ignore eclipse linked resource
 		if (resource.isLinked()) {
 			if (ClearcasePlugin.DEBUG_PROVIDER_IGNORED_RESOURCES)
 				ClearcasePlugin.trace(TRACE_ID_IS_IGNORED,
 						"linked resource: " + resource); //$NON-NLS-1$
 			return true;
 		}
 
 		// never ignore handled resources
 		if (hasRemote(resource))
 			return false;
 
 		// never ignore workspace root
 		IResource parent = resource.getParent();
 		if (null == parent)
 			return false;
 
 		// check the global ignores from Team (includes derived resources)
 		if (Team.isIgnoredHint(resource)) {
 			if (ClearcasePlugin.DEBUG_PROVIDER_IGNORED_RESOURCES)
 				ClearcasePlugin.trace(TRACE_ID_IS_IGNORED,
 						"ignore hint from team plug-in: " + resource); //$NON-NLS-1$
 			return true;
 		}
 
 		// never ignore uninitialized resources
 		if (isUnknownState(resource))
 			return false;
 
 		// ignore resources outside view
 		if (!isInsideView(resource)) {
 			if (ClearcasePlugin.DEBUG_PROVIDER_IGNORED_RESOURCES)
 				ClearcasePlugin.trace(TRACE_ID_IS_IGNORED,
 						"outside view: " + resource); //$NON-NLS-1$
 			return true;
 		}
 
 		// bug 904248: do not ignore if parent is a linked resource
 		if (parent.isLinked())
 			return false;
 
 		// check the parent, if the parent is ignored
 		// then this resource is ignored also
 		return isIgnored(parent);
 	}
 
 	/**
 	 * @param resource
 	 * @return
 	 */
 	public boolean isSymbolicLink(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).isSymbolicLink();
 	}
 
 	/**
 	 * @param resource
 	 * @return
 	 */
 	public boolean isSymbolicLinkTargetValid(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource)
 				.isSymbolicLinkTargetValid();
 	}
 
 	/**
 	 * @param resource
 	 * @return
 	 */
 	public String getSymbolicLinkTarget(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource)
 				.getSymbolicLinkTarget();
 	}
 
 	/**
 	 * Indicates if the specified resource is edited (checked out) by someone
 	 * else.
 	 * 
 	 * @param childResource
 	 * @return <code>true</code> if the specified resource is edited (checked
 	 *         out) by someone else, <code>false</code> otherwise
 	 */
 	public boolean isEdited(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).isEdited();
 	}
 
 	/**
 	 * Indicates if the specified resource is a view root directory containing
 	 * vobs.
 	 * 
 	 * @param resource
 	 * @return <code>true</code> if the specified resource is a view root
 	 *         directory
 	 */
 	public boolean isViewRoot(IResource resource) {
 		/*
 		 * todo: we need a better check for the view root; this only supports
 		 * structures where a project is the view directory containing the vobs
 		 */
 		return null != resource && resource.getType() == IResource.PROJECT
 				&& !hasRemote(resource);
 	}
 
 	/**
 	 * Indicates if the specified resource is a vob root directory.
 	 * 
 	 * @param resource
 	 * @return <code>true</code> if the specified resource is a vob root
 	 *         directory
 	 */
 	public boolean isVobRoot(IResource resource) {
 		/*
 		 * todo: we need a better check for the vob root; this only supports
 		 * structures where a project is the view directory containing the vobs
 		 */
 		return resource.getType() == IResource.FOLDER && !resource.isLinked()
 				&& isViewRoot(resource.getParent());
 	}
 
 	/**
 	 * Indicates if the specified resource is inside a view directory.
 	 * 
 	 * @param resource
 	 * @return <code>true</code> if the specified resource is a view directory
 	 */
 	public boolean isInsideView(IResource resource) {
 		return StateCacheFactory.getInstance().get(resource).isInsideView();
 	}
 
 	/**
 	 * Ensures the specified resource is initialized.
 	 * 
 	 * @param resource
 	 */
 	public void ensureInitialized(IResource resource) {
 		StateCacheFactory.getInstance().ensureInitialized(resource);
 	}
 }
