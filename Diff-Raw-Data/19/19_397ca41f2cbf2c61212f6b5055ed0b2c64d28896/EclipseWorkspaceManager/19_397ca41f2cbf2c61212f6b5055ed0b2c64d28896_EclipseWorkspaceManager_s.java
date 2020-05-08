 package org.magnolialang.resources.internal;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
 import org.magnolialang.eclipse.MagnoliaPlugin;
 import org.magnolialang.resources.IManagedResource;
 import org.magnolialang.resources.IManagedResourceListener;
 import org.magnolialang.resources.IResourceManager;
 import org.magnolialang.resources.IWorkspaceManager;
 import org.magnolialang.util.Pair;
 
 public final class EclipseWorkspaceManager implements IResourceChangeListener, IWorkspaceManager {
 	private static EclipseWorkspaceManager			instance;
 	private final Map<String, ProjectManager>		projects						= new HashMap<String, ProjectManager>();
 	private final List<IManagedResourceListener>	listeners						= new ArrayList<IManagedResourceListener>();
 	private final List<IProject>					closingProjects					= new ArrayList<IProject>();
 	private final static boolean					debug							= false;
 	private static final Object						JOB_FAMILY_WORKSPACE_MANAGER	= new Object();
 	private final Map<String, List<Change>>			changeQueue						= new HashMap<String, List<Change>>();
 
 
 	private EclipseWorkspaceManager() {
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 		initialize();
 	}
 
 
 	private void initialize() {
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IProject[] projects = root.getProjects(0);
 		for(IProject proj : projects) {
 			if(proj.isOpen()) {
 				try {
 					openProject(proj);
 				}
 				catch(CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 
 	public static synchronized EclipseWorkspaceManager getInstance() {
 		if(instance == null)
 			instance = new EclipseWorkspaceManager();
 		return instance;
 	}
 
 
 	public synchronized void stop() {
 		IJobManager jobManager = Job.getJobManager();
 		jobManager.cancel(JOB_FAMILY_WORKSPACE_MANAGER);
 		for(ProjectManager mgr : projects.values())
 			mgr.stop();
 	}
 
 
 	public synchronized IResourceManager getManager(IProject project) {
 		return projects.get(project.getName());
 	}
 
 
 	public synchronized IResourceManager getManager(String project) {
 		return projects.get(project);
 	}
 
 
 	public synchronized IManagedResource findResource(IResource resource) {
 		ProjectManager projectManager = projects.get(resource.getProject().getName());
 		IManagedResource res = null;
 		if(projectManager != null) {
 			res = projectManager.findResource(resource);
 			if(res != null)
 				return res;
 			if(resource.exists()) {
 				try {
 					System.err.println("EclipseWorkspaceManager: adding missing resource " + resource.getFullPath());
 					addResource(resource);
 					return projectManager.findResource(resource);
 				}
 				catch(CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 
 	@Override
 	public synchronized void resourceChanged(IResourceChangeEvent event) {
 		if(event.getType() == IResourceChangeEvent.POST_CHANGE) {
 			IResourceDelta delta = event.getDelta();
 			// System.err.println("PROCESSING RESOURCE CHANGE EVENT");
 			try {
 				delta.accept(new IResourceDeltaVisitor() {
 					@Override
 					public boolean visit(IResourceDelta delta) throws CoreException {
 						if(delta != null) {
 							try {
 								switch(delta.getKind()) {
 								case IResourceDelta.ADDED:
 									if(debug)
 										System.out.println(delta.getFullPath().toString() + " ADDED");
 									addResource(delta.getResource());
 									break;
 								case IResourceDelta.CHANGED:
 									if(delta.getFlags() == IResourceDelta.MARKERS || delta.getFlags() == IResourceDelta.NO_CHANGE) {
 										if(debug)
 											System.out.println(delta.getFullPath().toString() + " NO CHANGE");
 									}
 									else {
 										if(debug)
 											System.out.println(delta.getFullPath().toString() + " CHANGED");
 										// only if its not just the markers
 										resourceChanged(delta);
 									}
 									break;
 								case IResourceDelta.REMOVED:
 									if(debug)
 										System.out.println(delta.getFullPath().toString() + " REMOVED");
 									removeResource(delta.getResource());
 									break;
 								default:
 									throw new UnsupportedOperationException("Resource change on " + delta.getFullPath() + ": " + delta.getKind());
 								}
 							}
 							catch(CoreException e) {
 								MagnoliaPlugin.getInstance().logException("CoreException while processing " + delta.getFullPath(), e);
 								e.printStackTrace();
 							}
 							catch(Throwable t) {
 								MagnoliaPlugin.getInstance().logException("INTERNAL ERROR IN RESOURCE MANAGER (for " + delta.getFullPath() + ")", t);
 								t.printStackTrace();
 							}
 
 						}
 
 						return true;
 					}
 				});
 			}
 			catch(CoreException e) {
 				e.printStackTrace();
 			}
 
 			processChanges();
 
 			for(IProject p : closingProjects) {
 				try {
 					if(p != null)
 						closeProject(p);
 				}
 				catch(Throwable t) {
 					MagnoliaPlugin.getInstance().logException("INTERNAL ERROR IN RESOURCE MANAGER (for " + p + ")", t);
 					t.printStackTrace();
 				}
 			}
 			closingProjects.clear();
 
 		}
 		// System.err.println("FINISHED PROCESSING RESOURCE CHANGE EVENT");
 		dataInvariant();
 	}
 
 
 	private void removeResource(IResource resource) {
 		if(resource.getType() == IResource.PROJECT) {
 			closeProject((IProject) resource);
 		}
 		else {
 			IProject project = resource.getProject();
 			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
 			addChange(project.getName(), uri, resource, Change.Kind.REMOVED);
 		}
 	}
 
 
 	private void resourceChanged(IResourceDelta delta) throws CoreException {
 		IPath path = delta.getFullPath();
 		int flags = delta.getFlags();
 
 		if((flags & (IResourceDelta.TYPE | IResourceDelta.REPLACED)) != 0) {
 			removeResource(delta.getResource());
 			addResource(delta.getResource());
 		}
 
 		if((flags & IResourceDelta.OPEN) != 0) {
 			IProject proj = (IProject) delta.getResource();
 			if(proj.isOpen())
 				openProject(proj);
 			else
 				closingProjects.add(proj);
 
 		}
 
 		if((flags & IResourceDelta.LOCAL_CHANGED) != 0) {
 			System.err.println("LOCAL_CHANGED: " + path);
 		}
 		if((flags & IResourceDelta.CONTENT) != 0 || (flags & IResourceDelta.ENCODING) != 0) {
 			if(debug)
 				System.err.println("RESOURCE CHANGED: " + path);
 			IResource resource = delta.getResource();
 			IProject project = resource.getProject();
 			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
 			addChange(project.getName(), uri, resource, Change.Kind.CHANGED);
 
 		}
 
 	}
 
 
 	private void addChange(String proj, URI uri, IResource resource, Change.Kind kind) {
 		List<Change> list = changeQueue.get(proj);
 		if(list == null) {
 			list = new ArrayList<Change>();
 			changeQueue.put(proj, list);
 		}
 		list.add(new Change(uri, resource, kind));
 	}
 
 
 	private void openProject(IProject project) throws CoreException {
 		// TODO: check nature
 		projects.put(project.getName(), new ProjectManager(this, project));
 
 	}
 
 
 	private void closeProject(IProject project) {
 		// TODO: check nature
 		IResourceManager manager = projects.remove(project.getName());
 		if(manager != null)
 			manager.dispose();
 	}
 
 
 	private void addResource(IResource resource) throws CoreException {
 		if(resource.getType() == IResource.PROJECT) {
 			if(((IProject) resource).isOpen())
 				openProject((IProject) resource);
 		}
 		else {
 			IProject project = resource.getProject();
 			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
 			addChange(project.getName(), uri, resource, Change.Kind.REMOVED);
 		}
 	}
 
 
 	public void dataInvariant() {
 	}
 
 
 	@Override
 	public void addListener(IManagedResourceListener listener) {
 		throw new UnsupportedOperationException();
 		//listeners.add(listener);
 	}
 
 
 	@Override
 	public void removeListener(IManagedResourceListener listener) {
 		throw new UnsupportedOperationException();
 		// listeners.remove(listener);
 	}
 
 
 	@Override
 	public void dispose() {
 		// do nothing
 	}
 
 
 	@Override
 	public boolean hasURI(URI uri) {
 		if(uri.getScheme().equals("project"))
 			return true;
 
 		IFile file = MagnoliaPlugin.getFileHandle(uri);
 		return file != null;
 	}
 
 
 	@Override
 	public URI getURI(String path) throws URISyntaxException {
 		IPath p = new Path(path);
 		String project = p.segment(0);
 		p = p.removeFirstSegments(1);
		return new URI("project", project, p.toString(), null);
 	}
 
 
 	public static IPath getPath(URI uri) {
 		if(uri.getScheme().equals("project"))
 			return new Path("/" + uri.getAuthority() + "/" + uri.getPath());
 		else
 			return null;
 	}
 
 
 	static class Change {
 		enum Kind {
 			ADDED, REMOVED, CHANGED
 		};
 
 		final Kind		kind;
 		final URI		uri;
 		final IResource	resource;
 
 
 		Change(URI uri, IResource resource, Kind kind) {
 			this.uri = uri;
 			this.kind = kind;
 			this.resource = resource;
 		}
 	}
 
 
 	private void processChanges() {
 		for(String proj : changeQueue.keySet()) {
 			List<Change> list = changeQueue.get(proj);
 			ProjectManager manager = projects.get(proj);
 			if(manager != null) {
 				manager.processChanges(list);
 			}
 			list.clear();
 		}
 	}
 
 
 	static class ChangeQueueRunner extends Job {
 		private final List<Change>						changeQueue;
 		private final List<IManagedResourceListener>	listeners;
 
 
 		public ChangeQueueRunner(List<Change> changeQueue, List<IManagedResourceListener> listeners) {
 			super("Do change notifications");
 			this.changeQueue = changeQueue;
 			this.listeners = listeners;
 			this.setSystem(true);
 		}
 
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			for(Change c : changeQueue) {
 				if(monitor.isCanceled())
 					return Status.CANCEL_STATUS;
 				switch(c.kind) {
 				case ADDED:
 					for(IManagedResourceListener l : listeners)
 						l.resourceAdded(c.uri);
 					break;
 				case CHANGED:
 					for(IManagedResourceListener l : listeners)
 						l.resourceChanged(c.uri);
 					break;
 				case REMOVED:
 					for(IManagedResourceListener l : listeners)
 						l.resourceRemoved(c.uri);
 					break;
 				}
 			}
 			return Status.OK_STATUS;
 		}
 
 
 		@Override
 		public boolean belongsTo(Object obj) {
 			return obj == JOB_FAMILY_WORKSPACE_MANAGER || obj == MagnoliaPlugin.JOB_FAMILY_MAGNOLIA;
 		}
 	}
 
 
 	public static Pair<Set<IManagedResource>, Set<IPath>> getResourcesForDelta(IResourceDelta delta) {
 		final EclipseWorkspaceManager manager = getInstance();
 		final Set<IManagedResource> changed = new HashSet<IManagedResource>();
 		final Set<IPath> removed = new HashSet<IPath>();
 
 		try {
 			delta.accept(new IResourceDeltaVisitor() {
 				@Override
 				public boolean visit(IResourceDelta delta) throws CoreException {
 					if(delta != null && delta.getResource() instanceof IFile) {
 						switch(delta.getKind()) {
 						case IResourceDelta.ADDED: {
 							IManagedResource resource = manager.findResource(delta.getResource());
 							if(resource != null)
 								changed.add(resource);
 							break;
 						}
 						case IResourceDelta.CHANGED: {
 							IManagedResource resource = manager.findResource(delta.getResource());
 							if(resource != null)
 								changed.add(resource);
 							break;
 						}
 						case IResourceDelta.REMOVED:
 							removed.add(delta.getResource().getFullPath());
 							break;
 						default:
 							throw new UnsupportedOperationException("Resource change on " + delta.getFullPath() + ": " + delta.getKind());
 						}
 					}
 					return true;
 				}
 			});
 		}
 		catch(CoreException e) {
 			e.printStackTrace();
 		}
 
 		return new Pair<Set<IManagedResource>, Set<IPath>>(changed, removed);
 	}
 }
