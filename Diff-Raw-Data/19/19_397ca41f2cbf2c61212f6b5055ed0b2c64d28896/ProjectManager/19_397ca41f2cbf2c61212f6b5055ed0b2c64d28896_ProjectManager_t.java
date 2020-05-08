 package org.magnolialang.resources.internal;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
 import org.eclipse.imp.pdb.facts.visitors.VisitorException;
 import org.magnolialang.compiler.ICompiler;
 import org.magnolialang.eclipse.MagnoliaPlugin;
 import org.magnolialang.errors.ErrorMarkers;
 import org.magnolialang.errors.ImplementationError;
 import org.magnolialang.magnolia.resources.MagnoliaPackage;
 import org.magnolialang.nullness.Nullable;
 import org.magnolialang.resources.ILanguage;
 import org.magnolialang.resources.IManagedCodeUnit;
 import org.magnolialang.resources.IManagedPackage;
 import org.magnolialang.resources.IManagedResource;
 import org.magnolialang.resources.IResourceManager;
 import org.magnolialang.resources.IWorkspaceManager;
 import org.magnolialang.resources.LanguageRegistry;
 import org.magnolialang.resources.WorkspaceManager;
 import org.magnolialang.util.depgraph.IDepGraph;
 import org.magnolialang.util.depgraph.IWritableDepGraph;
 import org.magnolialang.util.depgraph.UnsyncedDepGraph;
 import org.rascalmpl.eclipse.nature.RascalMonitor;
 import org.rascalmpl.interpreter.IRascalMonitor;
 
 public final class ProjectManager implements IResourceManager {
 	/**
 	 * This read/write lock protects the fields of the project manager, except
 	 * when specifically noted in a field's documentation.
 	 * 
 	 * Reading from any field should not be done without first obtaining a read
 	 * lock. Modifying any of the field objects should
 	 * not be done without first obtaining a write lock.
 	 * 
 	 * The read lock can be obtained while holding the write lock, but a read
 	 * lock cannot be upgraded to a write lock.
 	 * 
 	 * *Note:* that returning
 	 * or accepting a reference to an object may actually constitute read or
 	 * write access outside the project manager class.
 	 * 
 	 * *Important note:* be careful with what external methods are called while
 	 * holding the lock, as a deadlock may result if the external method
 	 * ends up waiting on another thread which calls back to the project manager
 	 * (callbacks from the same thread is ok, since the lock is reentrant).
 	 */
 	private final ReadWriteLock							lock						= new ReentrantReadWriteLock();
 	private final IWorkspaceManager						manager;
 	private final Map<URI, IManagedResource>			resources					= new HashMap<URI, IManagedResource>();
 	private final Map<String, IManagedPackage>			packagesByName				= new HashMap<String, IManagedPackage>();
 	private final Map<URI, String>						packageNamesByURI			= new HashMap<URI, String>();
 	private final Map<ILanguage, ICompiler>				compilers					= new HashMap<ILanguage, ICompiler>();
 	private final IProject								project;
 	private final IPath									basePath;
 	private static final String							MODULE_LANG_SEP				= "%";
 	private static final String							OUT_FOLDER					= "cxx";
 	private static final boolean						debug						= true;
 	private final IPath									srcPath;
 	private final IPath									outPath;
 
 	/**
 	 * Synchronized on depGraphTodo, does not use lock.
 	 * 
 	 * The dependency graph may be incomplete, in which case depGraphTodo will
 	 * hold a list of packages which are missing from the graph.
 	 */
 	private final IWritableDepGraph<IManagedPackage>	depGraph					= new UnsyncedDepGraph<IManagedPackage>();
 
 	/**
 	 * A list of packages that have outdated dependency information.
 	 * 
 	 * Synchronized on depGraphTodo, does not use lock.
 	 * 
 	 * Note: depGraphJob must be scheduled after adding elements to
 	 * depGraphTodo.
 	 */
 	private final Set<IManagedPackage>					depGraphTodo				= new HashSet<IManagedPackage>();
 
 	/**
 	 * A job which processes depGraphTodo and pendingChanges, and recomputes the
 	 * dependency graph, and
 	 * notifies dependents of changes once dependencies are known.
 	 */
 	private final DepGraphJob							depGraphJob;
 	private static final long							DEP_GRAPH_JOB_DELAY			= 10L;
 
 	/**
 	 * A list of changes for which change notification has not yet been sent to
 	 * the relevant dependents.
 	 * 
 	 * Synchronized on depGraphTodo, does not use lock.
 	 * 
 	 * Note: depGraphJob must be scheduled after adding elements to
 	 * pendingChanges.
 	 */
 	private final List<Change>							pendingChanges				= new ArrayList<Change>();
 
 	private final DepGraphChecker						depGraphCheckerJob;
 	private static final long							DEP_GRAPH_CHECKER_JOB_DELAY	= 60000L;
 
 	private int											revision					= 0;
 
 
 	public ProjectManager(IWorkspaceManager manager, IProject project) throws CoreException {
 
 		this.manager = manager;
 		this.project = project;
 		this.basePath = project.getFullPath();
 		this.depGraphJob = new DepGraphJob(this, project.getName());
 		this.depGraphCheckerJob = new DepGraphChecker(this);
 		srcPath = null;
 		outPath = project.getFolder(OUT_FOLDER).getFullPath();
 		System.err.println("New projectmanager: basepath=" + basePath);
 		addAllResources();
 
 		dataInvariant();
 		// depGraphCheckerJob.schedule(DEP_GRAPH_CHECKER_JOB_DELAY);
 	}
 
 
 	private void addAllResources() throws CoreException {
 		project.accept(new IResourceVisitor() {
 			@Override
 			public boolean visit(IResource resource) {
 				if(resource.getType() == IResource.FILE) {
 					addResource(resource);
 				}
 				return true;
 			}
 
 		});
 	}
 
 
 	@Override
 	public IManagedResource findResource(URI uri) {
 		Lock l = lock.readLock();
 		l.lock();
 		try {
 
 			// see if we already track the URI
 			IManagedResource res = resources.get(uri);
 			if(res != null)
 				return res;
 
 			String scheme = uri.getScheme();
 
 			// check if it is a project URI 
 			if(scheme.equals("project")) {
 				l.unlock();
 				l = null;
 				if(uri.getAuthority().equals(project.getName()))
 					return null; // we should already have found it if we were tracking it
 				else {
 					IResourceManager mng = WorkspaceManager.getManager(uri.getAuthority());
 					if(mng != null)
 						return mng.findResource(uri);
 					else
 						return null;
 				}
 			}
 			else if(scheme.equals("magnolia")) {
 				return null; // not handled yet
 			}
 			// see if we can find it using Eclipse's pkg system
 			try {
 				IFileStore store = EFS.getStore(uri);
 				return findResource(uri, store);
 			}
 			catch(CoreException e) {
 
 			}
 
 			// give up
 			return null;
 		}
 		finally {
 			if(l != null)
 				l.unlock();
 		}
 	}
 
 
 	private IManagedResource findResource(URI uri, IFileStore store) {
 		IResource[] rs;
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IManagedResource res = null;
 		if(store.fetchInfo().isDirectory())
 			rs = root.findContainersForLocationURI(uri);
 		else
 			rs = root.findFilesForLocationURI(uri);
 		// search in *this* project first
 		for(IResource r : rs) {
 			if(r.getProject().equals(project))
 				res = findResource(MagnoliaPlugin.constructProjectURI(project, r.getProjectRelativePath()));
 			if(res != null)
 				return res;
 		}
 		for(IResource r : rs) {
 			res = findResource(MagnoliaPlugin.constructProjectURI(r.getProject(), r.getProjectRelativePath()));
 			if(res != null)
 				return res;
 		}
 		return null;
 	}
 
 
 	@Override
 	public IManagedResource findResource(String path) {
 		Lock l = lock.readLock();
 		l.lock();
 
 		try {
 			URI uri = MagnoliaPlugin.constructProjectURI(project, new Path(path));
 			System.err.println("find: uri: " + uri);
 			IManagedResource resource = findResource(uri);
 
 			System.err.println("find: pkg: " + resource);
 			return resource;
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	/**
 	 * @param resource
 	 * @return The resource associated with the Eclipse resource handle, or null
 	 *         if not found
 	 */
 	IManagedResource findResource(IResource resource) {
 		if(!project.equals(resource.getProject()))
 			throw new IllegalArgumentException("Resource must belong to this project (" + project.getName() + ")");
 		return findResource(MagnoliaPlugin.constructProjectURI(resource.getProject(), resource.getProjectRelativePath()));
 	}
 
 
 	private void addResource(IResource resource) {
 //		IResource pkg = project.findMember(path);
 		if(resource instanceof IFile) {
 			URI uri = MagnoliaPlugin.constructProjectURI(project, resource.getProjectRelativePath());
 			if(resources.get(uri) != null)
 				removeResource(uri);
 
 			ILanguage language = LanguageRegistry.getLanguageForFile(uri);
 			if(language != null)
 				addPackageResource(uri, resource, language);
 			else
 				addFileResource(uri, resource);
 
 		}
 	}
 
 
 	private void addFileResource(URI uri, IResource resource) {
 		if(debug)
 			System.err.println("PROJECT NEW FILE: " + uri);
 		ManagedFile file = new ManagedFile(this, resource);
 		resources.put(uri, file);
 	}
 
 
 	private void addPackageResource(URI uri, IResource resource, ILanguage lang) {
 		if(debug)
 			System.err.println("PROJECT NEW MODULE: " + uri);
 
 		if(lang != null) {
 			IPath srcRelativePath = resource.getFullPath();
 			srcRelativePath = srcRelativePath.makeRelativeTo(getSrcFolder());
 			String modName = lang.getModuleName(srcRelativePath.toString());
 			IConstructor modId = lang.getNameAST(modName);
 			MagnoliaPackage pkg = new MagnoliaPackage(this, resource, modId, lang);
 			resources.put(uri, pkg);
 			packagesByName.put(lang.getId() + MODULE_LANG_SEP + modName, pkg);
 			packageNamesByURI.put(uri, lang.getId() + MODULE_LANG_SEP + modName);
 			synchronized(depGraphTodo) {
 				depGraphTodo.add(pkg);
 				// TODO: no need to notify any dependents, since it can't have any when it just got added
 				// (1) but, perhaps we should notify any package that has a dependency error, since this might just resolve it?
 				// (2) or, maybe we should notify *all* packages, in case adding a package can create a dependency error?
 				// let's go for (1) for now
 				pendingChanges.add(new Change(pkg, Change.Kind.ADDED));
 				depGraphJob.schedule(DEP_GRAPH_JOB_DELAY);
 			}
 		}
 	}
 
 
 	private void removeResource(URI uri) {
 		if(debug)
 			System.err.println("PROJECT REMOVED: " + uri);
 		resources.remove(uri);
 		// removed.dispose();
 		String modName = packageNamesByURI.remove(uri);
 		if(modName != null) {
 			IManagedPackage removed = packagesByName.remove(modName);
 			synchronized(depGraphTodo) {
 				if(depGraphTodo.isEmpty()) {
 					for(IManagedPackage dep : depGraph.getTransitiveDependents(removed)) {
 						dep.onDependencyChanged();
 					}
 					depGraphTodo.addAll(depGraph.getDependents(removed));
 					depGraph.remove(removed);
 					depGraphTodo.remove(removed);
 					depGraphJob.schedule(DEP_GRAPH_JOB_DELAY);
 				}
 				else {
 					pendingChanges.add(new Change(removed, Change.Kind.REMOVED));
 					depGraphJob.schedule(DEP_GRAPH_JOB_DELAY);
 				}
 			}
 		}
 	}
 
 
 	/**
 	 * Called by the EclipseWorkspaceManager whenever a pkg has been changed
 	 * (i.e., the file contents have changed)
 	 * 
 	 * @param uri
 	 *            A full, workspace-relative path
 	 */
 	private void changeResource(URI uri) {
 		if(debug)
 			System.err.println("PROJECT CHANGED: " + uri);
 
 		IManagedResource resource = resources.get(uri);
 		if(resource != null) {
 			resource.onResourceChanged();
 			if(resource instanceof IManagedPackage)
 				synchronized(depGraphTodo) {
 					IManagedPackage pkg = (IManagedPackage) resource;
 					if(depGraphTodo.isEmpty()) {
 						for(IManagedPackage dep : depGraph.getTransitiveDependents(pkg)) {
 							dep.onDependencyChanged();
 						}
 						depGraphTodo.add(pkg);
 					}
 					else {
 						pendingChanges.add(new Change(pkg, Change.Kind.CHANGED));
 					}
 					depGraphJob.schedule(DEP_GRAPH_JOB_DELAY);
 				}
 		}
 
 	}
 
 
 	@Override
 	public void stop() {
 		depGraphJob.cancel();
 		depGraphCheckerJob.cancel();
 	}
 
 
 	@Override
 	public void dispose() {
 		Lock l = lock.writeLock();
 		l.lock();
 
 		try {
 			// for(IPath path : resources.keySet()) {
 			// resourceRemoved(path);
 			// }
 			resources.clear();
 			packageNamesByURI.clear();
 			packagesByName.clear();
 			depGraphJob.cancel();
 			depGraphCheckerJob.cancel();
 			dataInvariant();
 		}
 		finally {
 			l.unlock();
 		}
 		synchronized(depGraphTodo) {
 			depGraphTodo.clear();
 			depGraph.clear();
 			pendingChanges.clear();
 		}
 	}
 
 
 	private void dataInvariant() {
 	}
 
 
 	@Override
 	@Nullable
 	public IManagedPackage findPackage(ILanguage language, String moduleName) {
 		Lock l = lock.readLock();
 		l.lock();
 
 		try {
 			return packagesByName.get(language.getId() + MODULE_LANG_SEP + moduleName);
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	@Nullable
 	public IManagedPackage findPackage(ILanguage language, IConstructor moduleId) {
 		Lock l = lock.readLock();
 		l.lock();
 
 		try {
 			return packagesByName.get(language.getId() + MODULE_LANG_SEP + language.getNameString(moduleId));
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public IManagedPackage findPackage(URI uri) {
 		Lock l = lock.readLock();
 		l.lock();
 
 		try {
 			IManagedResource resource = resources.get(uri);
 			if(resource instanceof IManagedPackage)
 				return (IManagedPackage) resource;
 			else
 				return null;
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public URI getURI(String path) throws URISyntaxException {
 		Lock l = lock.readLock();
 		l.lock();
 
 		try {
			return MagnoliaPlugin.constructProjectURI(project, new Path(path));
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public void refresh() {
 		Lock l = lock.writeLock();
 		l.lock();
 
 		try {
 			List<URI> uris = new ArrayList<URI>(resources.keySet());
 			synchronized(depGraphTodo) {
 				for(URI p : uris)
 					removeResource(p);
 				dispose();
 
 				try {
 					addAllResources();
 				}
 				catch(CoreException e) {
 					throw new ImplementationError("CoreException caught", e);
 				}
 				depGraphJob.schedule();
 			}
 			for(ICompiler c : compilers.values()) {
 				c.refresh();
 			}
 			System.err.println("REFRESH DONE!");
 			dataInvariant();
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public Collection<IManagedPackage> allPackages(final ILanguage language) {
 		Lock l = lock.readLock();
 		l.lock();
 
 		try {
 			List<IManagedPackage> list = new ArrayList<IManagedPackage>();
 			for(Entry<URI, IManagedResource> entry : resources.entrySet()) {
 				if(entry.getValue() instanceof IManagedPackage && ((IManagedPackage) entry.getValue()).getLanguage().equals(language))
 					list.add((IManagedPackage) entry.getValue());
 			}
 			return list;
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public Collection<IManagedResource> allFiles() {
 		Lock l = lock.readLock();
 		l.lock();
 
 		try {
 			return new ArrayList<IManagedResource>(resources.values());
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc) {
 		addMarker(message, loc, ErrorMarkers.TYPE_DEFAULT, ErrorMarkers.SEVERITY_DEFAULT_NUMBER);
 	}
 
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc, int severity) {
 		addMarker(message, loc, ErrorMarkers.TYPE_DEFAULT, severity);
 	}
 
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc, String markerType) {
 		addMarker(message, loc, markerType, ErrorMarkers.SEVERITY_DEFAULT_NUMBER);
 	}
 
 
 	@Override
 	public void addMarker(String message, ISourceLocation loc, String markerType, int severity) {
 		Lock l = lock.readLock();
 		l.lock();
 
 		IManagedResource pkg;
 		try {
 			if(loc == null)
 				throw new ImplementationError("Missing location on marker add: " + message);
 
 			URI uri = loc.getURI();
 
 			pkg = resources.get(uri);
 		}
 		finally {
 			l.unlock();
 		}
 
 		if(pkg instanceof IManagedPackage)
 			((IManagedPackage) pkg).addMarker(message, loc, markerType, severity);
 		else
 			throw new ImplementationError(message + "\nat location " + loc + " (pkg not found)");
 	}
 
 
 	@Override
 	public IWorkspaceManager getResourceManager() {
 		return manager;
 	}
 
 
 	@Override
 	public IPath getSrcFolder() {
 		Lock l = lock.readLock();
 		l.lock();
 		try {
 			if(srcPath == null) {
 				IResource src = project.findMember("src");
 				if(src != null && src.getType() == IResource.FOLDER)
 					return src.getFullPath();
 				else
 					return basePath;
 			}
 			else
 				return srcPath;
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public IPath getOutFolder() {
 		return outPath;
 	}
 
 
 	@Override
 	public IManagedResource getParent() {
 		return null;
 	}
 
 
 	@Override
 	public URI getURI() {
		return MagnoliaPlugin.constructProjectURI(project, new Path("/"));
 	}
 
 
 	@Override
 	public boolean isFile() {
 		return false;
 	}
 
 
 	@Override
 	public boolean isContainer() {
 		return true;
 	}
 
 
 	@Override
 	public long getModificationStamp() {
 		return project.getModificationStamp(); // unlocked access ok
 	}
 
 
 	@Override
 	public void onResourceChanged() {
 	}
 
 
 	@Override
 	public Type getType() {
 		return IManagedResource.ResourceType;
 	}
 
 
 	@Override
 	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 	@Override
 	public boolean isEqual(IValue other) {
 		return this == other;
 	}
 
 
 	@Override
 	public Collection<IManagedResource> getChildren(IRascalMonitor rm) {
 		Lock l = lock.readLock();
 		l.lock();
 		try {
 			return new ArrayList<IManagedResource>(resources.values());
 		}
 		finally {
 			l.unlock();
 		}
 	}
 
 
 	@Override
 	public boolean isCodeUnit() {
 		return false;
 	}
 
 
 	@Override
 	public boolean isProject() {
 		return true;
 	}
 
 
 	@Override
 	public IDepGraph<IManagedPackage> getPackageDependencyGraph(ILanguage lang, IRascalMonitor rm) {
 		long t0 = System.currentTimeMillis();
 
 		/* We'll make a copy of the todo list and the current graph while we have the lock,
 		 * and the complete the graph afterwards if it is missing anything.
 		 * 
 		 * Since we can't be entirely sure that pkg.getDepends() won't make calls to the project manager
 		 * from another thread, we can't hold the lock while finishing the computation. 
 		 * 
 		 * We'll leave the job of updating depGraph to a dedicated dependency graph job. 
 		 */
 		List<IManagedPackage> todo;
 		IWritableDepGraph<IManagedPackage> graph;
 		synchronized(depGraphTodo) {
 			todo = new ArrayList<IManagedPackage>(depGraphTodo);
 			graph = depGraph.copy();
 		}
 		if(!todo.isEmpty()) {
 			for(IManagedPackage pkg : todo) {
 				graph.add(pkg);
 				for(IManagedCodeUnit d : pkg.getDepends(rm))
 					graph.add(pkg, (IManagedPackage) d);
 			}
 		}
 
 		System.err.printf("Compute dependency graph" + ": %dms%n", System.currentTimeMillis() - t0);
 		return graph;
 	}
 
 
 	@Override
 	public Set<IManagedPackage> getPackageTransitiveDependents(IManagedPackage pkg, IRascalMonitor rm) {
 		waitForDepGraph(rm);
 		synchronized(depGraphTodo) {
 			Set<IManagedPackage> dependents = depGraph.getTransitiveDependents(pkg);
 			if(dependents != null)
 				return dependents;
 			else
 				return Collections.EMPTY_SET;
 		}
 	}
 
 
 	/**
 	 * Wait for any pending updates to the dependency graph.
 	 * 
 	 * Note that, unless the workspace is locked, new changes may be pending
 	 * when this method returns.
 	 * 
 	 * @param rm
 	 */
 	public void waitForDepGraph(IRascalMonitor rm) {
 		synchronized(depGraphTodo) {
 			if(depGraphTodo.isEmpty())
 				return;
 		}
 		depGraphJob.schedule();
 		try {
 			depGraphJob.join();
 		}
 		catch(InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Wait for any pending updates to the dependency graph and return a copy of
 	 * the dependency graph.
 	 * 
 	 * Note that, unless the workspace is locked, new changes may be pending
 	 * when this method returns.
 	 * 
 	 * @param rm
 	 */
 	@Override
 	public IDepGraph<IManagedPackage> getPackageDependencyGraph(IRascalMonitor rm) {
 		for(int i = 0; i < 1000; i++) { // TODO do this in a better way...
 			synchronized(depGraphTodo) {
 				if(depGraphTodo.isEmpty())
 					return depGraph.copy();
 				depGraphJob.schedule();
 			}
 			try {
 				depGraphJob.join();
 			}
 			catch(InterruptedException e) {
 				e.printStackTrace();
 
 			}
 		}
 		synchronized(depGraphTodo) {
 			return depGraph.copy();
 		}
 	}
 
 
 	public void printDepGraph() {
 		synchronized(depGraphTodo) {
 			System.err.flush();
 			System.out.flush();
 			System.out.println("DEPENDENCY GRAPH FOR PROJECT " + project.getName());
 			for(IManagedPackage pkg : depGraph.topological()) {
 				System.out.print("\t  " + pkg.getName() + " <- ");
 				for(IManagedPackage dep : depGraph.getDependents(pkg))
 					System.out.print(dep.getName() + " ");
 				System.out.println();
 			}
 			System.out.println("\tPACKAGES WITH INCOMPLETE INFORMATION:");
 			for(IManagedPackage pkg : depGraphTodo)
 				System.out.println("\t  " + pkg.getName());
 			System.out.println("\tPENDING CHANGES:");
 			for(Change c : pendingChanges)
 				System.out.println("\t  " + c.kind.name() + " " + c.pkg.getName());
 		}
 	}
 
 
 	static class DepGraphJob extends Job {
 
 		private static final int		WORK_PER_GET_DEPENDS	= 10;
 		private static final int		WORK_PER_NOTIFY			= 1;
 		private final ProjectManager	pManager;
 		private int						todo;
 		private boolean					packagesAdded;
 
 
 		public DepGraphJob(ProjectManager pManager, String projectName) {
 			super("Computing dependencies for " + projectName);
 			this.pManager = pManager;
 		}
 
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 
 			IRascalMonitor rm = new RascalMonitor(monitor);
 			synchronized(pManager.depGraphTodo) {
 				todo = pManager.depGraphTodo.size() * WORK_PER_GET_DEPENDS + pManager.pendingChanges.size() * WORK_PER_NOTIFY;
 				rm.startJob(getName(), todo);
 			}
 			System.err.println("DepGraphJob: STARTING");
 			long t0 = System.currentTimeMillis();
 			packagesAdded = false;
 
 			while(!monitor.isCanceled() && rebuildDepGraph(rm))
 				;
 
 			while(!monitor.isCanceled() && notifyChanges(rm))
 				;
 
 			synchronized(pManager.depGraphTodo) {
 				if(packagesAdded) {
 					for(IManagedPackage p : pManager.depGraph.getElements()) {
 						p.onDependencyChanged();
 						pManager.depGraphTodo.add(p);
 					}
 				}
 				todo = pManager.depGraphTodo.size() * WORK_PER_GET_DEPENDS;
 				rm.todo(todo);
 			}
 
 			while(!monitor.isCanceled() && rebuildDepGraph(rm))
 				;
 
 			System.err.printf("DepGraphJob: DONE: %dms%n", System.currentTimeMillis() - t0);
 			// pManager.printDepGraph();
 			rm.endJob(true);
 			return Status.OK_STATUS;
 		}
 
 
 		@Override
 		public boolean belongsTo(Object obj) {
 			return obj == MagnoliaPlugin.JOB_FAMILY_MAGNOLIA;
 		}
 
 
 		private boolean rebuildDepGraph(IRascalMonitor rm) {
 			IManagedPackage pkg = null;
 			synchronized(pManager.depGraphTodo) {
 				Iterator<IManagedPackage> iterator = pManager.depGraphTodo.iterator();
 				if(iterator.hasNext()) {
 					pkg = iterator.next();
 					iterator.remove();
 				}
 			}
 
 			Collection<? extends IManagedPackage> depends = null;
 			if(pkg != null) {
 				rm.event("Obtaining dependencies for " + pkg.getName(), WORK_PER_GET_DEPENDS);
 				todo -= WORK_PER_GET_DEPENDS;
 				// System.err.println("DepGraphJob: GET DEPENDS " + pkg.getName());
 				depends = pkg.getDepends(rm);
 			}
 
 			synchronized(pManager.depGraphTodo) {
 				if(depends != null) {
 					for(IManagedCodeUnit p : depends)
 						if(p instanceof IManagedPackage)
 							pManager.depGraph.add(pkg, (IManagedPackage) p);
 				}
 				return !pManager.depGraphTodo.isEmpty();
 			}
 		}
 
 
 		private boolean notifyChanges(IRascalMonitor rm) {
 			synchronized(pManager.depGraphTodo) {
 				if(!pManager.pendingChanges.isEmpty()) {
 					Change changed = pManager.pendingChanges.remove(0);
 					rm.event("Notifying dependents of changes to " + changed.pkg.getName(), WORK_PER_NOTIFY);
 					todo -= WORK_PER_NOTIFY;
 					// System.err.println("DepGraphJob: NOTIFY " + changed.pkg.getName() + "(" + changed.kind.name() + ")");
 
 					if(changed.kind == Change.Kind.ADDED) {
 						pManager.depGraph.add(changed.pkg);
 						packagesAdded = true;
 					}
 					else {
 						// notify transitive dependents that the package has changed
 						Set<IManagedPackage> dependents = pManager.depGraph.getTransitiveDependents(changed.pkg);
 						if(dependents != null)
 							for(IManagedPackage d : dependents)
 								d.onDependencyChanged();
 
 						// recalculate dependencies of immediate dependents
 						if(changed.kind == Change.Kind.REMOVED) {
 							dependents = pManager.depGraph.getDependents(changed.pkg);
 							pManager.depGraphTodo.addAll(dependents);
 							pManager.depGraph.remove(changed.pkg);
 							todo += dependents.size() * WORK_PER_GET_DEPENDS;
 							rm.todo(todo);
 						}
 					}
 				}
 				return !pManager.pendingChanges.isEmpty();
 			}
 		}
 
 
 		@Override
 		public boolean shouldRun() {
 			synchronized(pManager.depGraphTodo) {
 				return !pManager.depGraphTodo.isEmpty() || !pManager.pendingChanges.isEmpty();
 			}
 		}
 	}
 
 
 	static class DepGraphChecker extends Job {
 
 		private final ProjectManager	pManager;
 
 
 		public DepGraphChecker(ProjectManager pManager) {
 			super("Checking dependency graph for " + pManager.project.getName());
 			setRule(pManager.project);
 			this.pManager = pManager;
 		}
 
 
 		@Override
 		protected IStatus run(IProgressMonitor monitor) {
 			IRascalMonitor rm = new RascalMonitor(monitor);
 			Collection<IManagedPackage> packages = null;
 			Lock l = pManager.lock.readLock();
 			l.lock();
 			try {
 				packages = new ArrayList<IManagedPackage>(pManager.packagesByName.values());
 			}
 			finally {
 				l.unlock();
 			}
 
 			rm.startJob(this.getName(), 5 + packages.size());
 			synchronized(pManager.depGraphTodo) {
 				if(pManager.depGraphTodo.isEmpty()) {
 					IDepGraph<IManagedPackage> graph = new UnsyncedDepGraph<IManagedPackage>();
 					for(IManagedPackage pkg : packages) {
 						rm.event("Adding " + pkg.getName(), 1);
 						pkg.onResourceChanged();
 						pkg.getAST(rm);
 						graph.add(pkg, pkg.getDepends(rm));
 					}
 					rm.event("Checking equality", 5);
 					if(graph.equals(pManager.depGraph))
 						System.err.println("DepGraphChecker: " + pManager.project.getName() + ": OK");
 					else {
 						System.err.println("DepGraphChecker: " + pManager.project.getName() + ": ********** DEPENDENCY GRAPH DIFFERS **********");
 						System.out.println("RESULT OF NEW ANALYSIS FOR PROJECT " + pManager.project.getName());
 						for(IManagedPackage pkg : graph.topological()) {
 							System.out.print("\t  " + pkg.getName() + " <- ");
 							for(IManagedPackage dep : graph.getDependents(pkg))
 								System.out.print(dep.getName() + " ");
 							System.out.println();
 						}
 						pManager.printDepGraph();
 					}
 				}
 				else {
 					System.err.println("DepGraphChecker: aborting, dependency graph not complete");
 				}
 			}
 			schedule(DEP_GRAPH_CHECKER_JOB_DELAY);
 			return Status.OK_STATUS;
 		}
 
 
 		@Override
 		public boolean belongsTo(Object obj) {
 			return obj == MagnoliaPlugin.JOB_FAMILY_MAGNOLIA;
 		}
 	}
 
 
 	static class Change {
 		enum Kind {
 			ADDED, REMOVED, CHANGED
 		};
 
 		final Kind				kind;
 		final IManagedPackage	pkg;
 
 
 		Change(IManagedPackage resource, Kind kind) {
 			this.pkg = resource;
 			this.kind = kind;
 		}
 	}
 
 
 	public void processChanges(List<EclipseWorkspaceManager.Change> list) {
 		Lock l = lock.writeLock();
 		l.lock();
 		try {
 			for(EclipseWorkspaceManager.Change change : list) {
 				switch(change.kind) {
 				case ADDED:
 					addResource(change.resource);
 					break;
 				case CHANGED:
 					changeResource(change.uri);
 					break;
 				case REMOVED:
 					removeResource(change.uri);
 					break;
 				}
 			}
 		}
 		finally {
 			l.unlock();
 		}
 		revision++;
 	}
 
 }
