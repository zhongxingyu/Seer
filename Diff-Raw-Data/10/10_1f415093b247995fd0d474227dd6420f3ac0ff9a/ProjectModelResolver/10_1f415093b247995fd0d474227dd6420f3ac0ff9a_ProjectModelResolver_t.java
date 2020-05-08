 /*******************************************************************************
  * Copyright (c) 2011, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.logical;
 
 import static com.google.common.collect.Sets.difference;
 import static com.google.common.collect.Sets.intersection;
 import static com.google.common.collect.Sets.newLinkedHashSet;
 import static org.eclipse.emf.compare.ide.ui.internal.util.PlatformElementUtil.adaptAs;
 import static org.eclipse.emf.compare.ide.utils.ResourceUtil.createURIFor;
 import static org.eclipse.emf.compare.ide.utils.ResourceUtil.hasContentType;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIMessages;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
 import org.eclipse.emf.compare.ide.ui.logical.IModelResolver;
 import org.eclipse.emf.compare.ide.ui.logical.IStorageProvider;
 import org.eclipse.emf.compare.ide.ui.logical.IStorageProviderAccessor;
 import org.eclipse.emf.compare.ide.ui.logical.IStorageProviderAccessor.DiffSide;
 import org.eclipse.emf.compare.ide.ui.logical.SynchronizationModel;
 import org.eclipse.emf.compare.ide.utils.StorageTraversal;
 import org.eclipse.emf.compare.ide.utils.StorageURIConverter;
 
 /**
  * This implementation of an {@link IModelResolver} will look up in the whole project contained by the
  * "starting points" of the models to resolve in order to check for referencing parents. Once this is done,
  * we'll know the whole logical model for the "local" resource. The right and origin (if any) resources will
  * be provided with the same traversal of resources, expanded with a "top-down" approach : load all models of
  * the traversal from the remote side, then resolve their containment tree to check whether there are new
  * remote resources in the logical model.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public class ProjectModelResolver extends LogicalModelResolver {
 	/** Content types of the files to consider as potential models. */
 	private static final String[] MODEL_CONTENT_TYPES = new String[] {
 			"org.eclipse.emf.compare.content.type", "org.eclipse.emf.ecore", //$NON-NLS-1$ //$NON-NLS-2$
 			"org.eclipse.emf.ecore.xmi", }; //$NON-NLS-1$
 
 	/**
 	 * Keeps track of the discovered dependency graph. Model resolvers are created from the extension point
 	 * registry, we can thus keep this graph around to avoid multiple crawlings of the same IProject. Team,
 	 * and the EMFResourceMapping, tend to be over-enthusiast with the resolution of model traversals. For
 	 * example, a single "right-click -> compare with -> commit..." with EGit ends up calling 8 distinct times
 	 * for the resource traversal of the selected resource.
 	 */
 	private Graph<URI> dependencyGraph;
 
 	/**
 	 * This resolver will keep a resource listener over the workspace in order to keep its dependencies graph
 	 * in sync.
 	 */
 	private ModelResourceListener resourceListener;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.logical.AbstractModelResolver#initialize()
 	 */
 	@Override
 	public void initialize() {
 		super.initialize();
 		this.dependencyGraph = new Graph<URI>();
 		this.resourceListener = new ModelResourceListener();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this.resourceListener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.logical.AbstractModelResolver#dispose()
 	 */
 	@Override
 	public void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this.resourceListener);
 		this.resourceListener = null;
 		this.dependencyGraph = null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.logical.LogicalModelResolver#resolveLocalModels(org.eclipse.core.resources.IResource,
 	 *      org.eclipse.core.resources.IResource, org.eclipse.core.resources.IResource,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public SynchronizationModel resolveLocalModels(IResource left, IResource right, IResource origin,
 			IProgressMonitor monitor) {
 		if (!(left instanceof IFile) || !(right instanceof IFile)) {
 			return super.resolveLocalModels(left, right, origin, monitor);
 		}
 
 		updateChangedDependencies(monitor);
 		updateDependencies((IFile)left, monitor);
 		updateDependencies((IFile)right, monitor);
 		if (origin instanceof IFile) {
 			updateDependencies((IFile)origin, monitor);
 		}
 
 		final Set<IFile> startingPoints;
 		if (origin != null) {
 			startingPoints = ImmutableSet.of((IFile)left, (IFile)right, (IFile)origin);
 		} else {
 			startingPoints = ImmutableSet.of((IFile)left, (IFile)right);
 		}
 
 		final Set<IStorage> leftTraversal = resolveTraversal((IFile)left, difference(startingPoints,
 				Collections.singleton(left)), monitor);
 		final Set<IStorage> rightTraversal = resolveTraversal((IFile)right, difference(startingPoints,
 				Collections.singleton(right)), monitor);
 		final Set<IStorage> originTraversal;
 		if (origin instanceof IFile) {
 			originTraversal = resolveTraversal((IFile)origin, difference(startingPoints, Collections
 					.singleton(origin)), monitor);
 		} else {
 			originTraversal = Collections.emptySet();
 		}
 
 		/*
 		 * If one resource of the logical model was pointing to both (or "all three") of our starting
 		 * elements, we'll have way too many things in our traversal. We need to remove the intersection
 		 * before going any further.
 		 */
 		Set<IStorage> intersection = intersection(leftTraversal, rightTraversal);
 		if (!originTraversal.isEmpty()) {
 			intersection = intersection(intersection, originTraversal);
 		}
 		logCoherenceThreats(startingPoints, intersection);
 
 		final Set<IStorage> actualLeft = newLinkedHashSet(difference(leftTraversal, intersection));
 		final Set<IStorage> actualRight = newLinkedHashSet(difference(rightTraversal, intersection));
 		final Set<IStorage> actualOrigin = newLinkedHashSet(difference(originTraversal, intersection));
 		return new SynchronizationModel(new StorageTraversal(actualLeft), new StorageTraversal(actualRight),
 				new StorageTraversal(actualOrigin));
 	}
 
 	/**
 	 * When executing local comparisons, we resolve the full logical model of both (or "all three of") the
 	 * compared files.
 	 * <p>
 	 * If there is one resource in the scope that references all of these starting points, then we'll have
 	 * perfectly identical logical models for all comparison sides. Because of that, we need to constrain the
 	 * logical model of each starting point to only parts that are not accessible from other starting points.
 	 * This might cause coherence issues as merging could thus "break" references from other files to our
 	 * compared ones.
 	 * </p>
 	 * <p>
 	 * This method will be used to browse the files that are removed from the logical model, and log a warning
 	 * for the files that are removed even though they are "parents" of one of the starting points.
 	 * </p>
 	 * 
 	 * @param startingPoints
 	 *            Starting points of the comparison.
 	 * @param removedFromModel
 	 *            All files that have been removed from the comparison scope.
 	 */
 	private void logCoherenceThreats(Set<IFile> startingPoints, Set<IStorage> removedFromModel) {
 		final Set<URI> coherenceThreats = new LinkedHashSet<URI>();
 		for (IStorage start : startingPoints) {
 			final URI startURI = createURIFor(start);
 			for (IStorage removed : removedFromModel) {
 				final URI removedURI = createURIFor(removed);
 				if (dependencyGraph.hasChild(removedURI, startURI)) {
 					coherenceThreats.add(removedURI);
 				}
 			}
 		}
 
		if (!coherenceThreats.isEmpty()) {
			final String message = EMFCompareIDEUIMessages.getString("ModelResolver.coherenceWarning"); //$NON-NLS-1$
			final String details = Iterables.toString(coherenceThreats);
			EMFCompareIDEUIPlugin.getDefault().getLog().log(
					new Status(IStatus.WARNING, EMFCompareIDEUIPlugin.PLUGIN_ID, message + '\n' + details));
		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.logical.LogicalModelResolver#resolveModels(org.eclipse.emf.compare.ide.ui.logical.IStorageProviderAccessor,
 	 *      org.eclipse.core.resources.IStorage, org.eclipse.core.resources.IStorage,
 	 *      org.eclipse.core.resources.IStorage, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public SynchronizationModel resolveModels(IStorageProviderAccessor storageAccessor, IStorage left,
 			IStorage right, IStorage origin, IProgressMonitor monitor) {
 		final IFile leftFile = adaptAs(left, IFile.class);
 		if (leftFile == null) {
 			return super.resolveModels(storageAccessor, leftFile, right, origin, monitor);
 		}
 
 		updateChangedDependencies(monitor);
 		updateDependencies(leftFile, storageAccessor, monitor);
 
 		final Set<IStorage> leftTraversal = resolveLocalTraversal(storageAccessor, leftFile, monitor);
 		final Set<IStorage> rightTraversal = resolveTraversal(storageAccessor, DiffSide.REMOTE,
 				leftTraversal, monitor);
 		final Set<IStorage> originTraversal;
 		if (origin != null) {
 			originTraversal = resolveTraversal(storageAccessor, DiffSide.ORIGIN, leftTraversal, monitor);
 		} else {
 			originTraversal = Collections.emptySet();
 		}
 
 		return new SynchronizationModel(new StorageTraversal(leftTraversal), new StorageTraversal(
 				rightTraversal), new StorageTraversal(originTraversal));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.logical.LogicalModelResolver#resolveLocalModel(org.eclipse.core.resources.IResource,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public StorageTraversal resolveLocalModel(IResource start, IProgressMonitor monitor) {
 		if (!(start instanceof IFile)) {
 			return super.resolveLocalModel(start, monitor);
 		}
 
 		updateChangedDependencies(monitor);
 		updateDependencies((IFile)start, monitor);
 
 		return new StorageTraversal(resolveTraversal((IFile)start, monitor));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.logical.LogicalModelResolver#canResolve(org.eclipse.core.resources.IStorage)
 	 */
 	@Override
 	public boolean canResolve(IStorage sourceStorage) {
 		if (sourceStorage instanceof IFile) {
 			IFile file = (IFile)sourceStorage;
 			return file.getProject().isAccessible() && ((IFile)sourceStorage).exists();
 		}
 		return false;
 	}
 
 	/**
 	 * Updates the dependency graph for the given file.
 	 * 
 	 * @param file
 	 *            File which dependencies we are to update.
 	 * @param monitor
 	 *            Monitor to report progress on.
 	 */
 	private void updateDependencies(IFile file, IProgressMonitor monitor) {
 		final URI startURI = createURIFor(file);
 		if (!dependencyGraph.contains(startURI)) {
 			final IProject project = file.getProject();
 			final ModelResourceVisitor modelVisitor = new ModelResourceVisitor(dependencyGraph, monitor);
 			try {
 				project.accept(modelVisitor);
 			} catch (CoreException e) {
 				EMFCompareIDEUIPlugin.getDefault().log(e);
 			}
 		}
 	}
 
 	/**
 	 * Updates the dependency graph for the given file.
 	 * 
 	 * @param file
 	 *            File which dependencies we are to update.
 	 * @param storageAccessor
 	 *            The accessor that can be used to retrieve synchronization information between our resources.
 	 * @param monitor
 	 *            Monitor to report progress on.
 	 */
 	private void updateDependencies(IFile file, IStorageProviderAccessor storageAccessor,
 			IProgressMonitor monitor) {
 		final URI leftURI = createURIFor(file);
 		if (!dependencyGraph.contains(leftURI)) {
 			final IProject project = file.getProject();
 			final ModelResourceVisitor modelVisitor = new ModelResourceVisitor(storageAccessor,
 					DiffSide.SOURCE, dependencyGraph, monitor);
 			try {
 				project.accept(modelVisitor);
 			} catch (CoreException e) {
 				// TODO log
 			}
 		}
 	}
 
 	/**
 	 * Checks all dependencies that have changed since we last checked (as returned by the
 	 * {@link #resourceListener}).
 	 * 
 	 * @param monitor
 	 *            Monitor to report progress on.
 	 */
 	private void updateChangedDependencies(IProgressMonitor monitor) {
 		final Set<URI> removedURIs = resourceListener.popRemovedURIs();
 		final Set<URI> changedURIs = Sets.difference(resourceListener.popChangedURIs(), removedURIs);
 
 		for (URI removed : removedURIs) {
 			dependencyGraph.remove(removed);
 		}
 
 		for (URI changed : changedURIs) {
 			dependencyGraph.remove(changed);
 
 			final IFile file = getFileAt(changed);
 			updateDependencies(file, monitor);
 		}
 	}
 
 	/**
 	 * Checks whether the given file has one of the content types described in {@link #MODEL_CONTENT_TYPES}.
 	 * 
 	 * @param file
 	 *            The file which contents are to be checked.
 	 * @return <code>true</code> if this file has one of the "model" content types.
 	 */
 	protected static final boolean hasModelType(IFile file) {
 		boolean isModel = false;
 		for (int i = 0; i < MODEL_CONTENT_TYPES.length && !isModel; i++) {
 			isModel = hasContentType(file, MODEL_CONTENT_TYPES[i]);
 		}
 		return isModel;
 	}
 
 	/**
 	 * This will be used to resolve the traversal of a file's logical model, according to
 	 * {@link #dependencyGraph}.
 	 * 
 	 * @param resource
 	 *            The resource for which we need the full logical model.
 	 * @param monitor
 	 *            Monitor on which to report progress to the user.
 	 * @return The set of all storages that compose the logical model of <code>resource</code>.
 	 */
 	private Set<IStorage> resolveTraversal(IFile resource, IProgressMonitor monitor) {
 		final Set<IStorage> traversal = new LinkedHashSet<IStorage>();
 		final URI startURI = createURIFor(resource);
 
 		final Iterable<URI> uris = dependencyGraph.getSubgraphOf(startURI);
 		for (URI uri : uris) {
 			traversal.add(getFileAt(uri));
 		}
 		return traversal;
 	}
 
 	/**
 	 * This will be used in case of remote comparisons to resolve the local side's traversal (if there is a
 	 * local side).
 	 * 
 	 * @param storageAccessor
 	 *            The accessor that can be used to retrieve synchronization information between our resources.
 	 * @param resource
 	 *            The resource for which we need the full logical model.
 	 * @param monitor
 	 *            Monitor on which to report progress to the user.
 	 * @return The set of all storages that compose the logical model of <code>resource</code>.
 	 */
 	private Set<IStorage> resolveLocalTraversal(IStorageProviderAccessor storageAccessor, IFile resource,
 			IProgressMonitor monitor) {
 		final Set<IStorage> traversal = new LinkedHashSet<IStorage>();
 		final URI startURI = createURIFor(resource);
 
 		final Iterable<URI> uris = dependencyGraph.getSubgraphOf(startURI);
 		for (URI uri : uris) {
 			final IFile file = getFileAt(uri);
 			try {
 				if (!storageAccessor.isInSync(file)) {
 					traversal.add(file);
 				}
 			} catch (CoreException e) {
 				// swallow
 			}
 		}
 		return traversal;
 	}
 
 	/**
 	 * This will be used to resolve the traversal of a file's logical model, according to
 	 * {@link #dependencyGraph}.
 	 * 
 	 * @param resource
 	 *            The resource for which we need the full logical model.
 	 * @param bounds
 	 *            The resources constituting starting points of "other" logical models. This will be used to
 	 *            constrain the dependency sub-graph.
 	 * @param monitor
 	 *            Monitor on which to report progress to the user.
 	 * @return The set of all storages that compose the logical model of <code>resource</code>.
 	 */
 	private Set<IStorage> resolveTraversal(IFile resource, Set<IFile> bounds, IProgressMonitor monitor) {
 		final Set<IStorage> traversal = new LinkedHashSet<IStorage>();
 		final URI startURI = createURIFor(resource);
 
 		final Set<URI> uriBounds = new LinkedHashSet<URI>(bounds.size());
 		for (IFile bound : bounds) {
 			uriBounds.add(createURIFor(bound));
 		}
 
 		final Iterable<URI> uris = dependencyGraph.getBoundedSubgraphOf(startURI, uriBounds);
 		for (URI uri : uris) {
 			traversal.add(getFileAt(uri));
 		}
 		return traversal;
 	}
 
 	/**
 	 * Returns the IFile located at the given URI.
 	 * 
 	 * @param uri
 	 *            URI we need the file for.
 	 * @return The IFile located at the given URI.
 	 */
 	private IFile getFileAt(URI uri) {
 		final StringBuilder path = new StringBuilder();
 		List<String> segments = uri.segmentsList();
 		if (uri.isPlatformResource()) {
 			segments = segments.subList(1, segments.size());
 		}
 		for (String segment : segments) {
 			path.append(segment).append('/');
 		}
 		return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path.toString()));
 	}
 
 	/**
 	 * This will be used to resolve the logical model of a remote resource variant. Since we have no direct
 	 * access to the resources themselves, we cannot simply browse one of their container for the traversal.
 	 * Instead of that, we'll use the local traversal as a "reference", load remote variants of all of these
 	 * local files, and re-resolve them in a "top-down" approach in case there are new resources on the remote
 	 * side.
 	 * 
 	 * @param storageAccessor
 	 *            The accessor that can be used to retrieve synchronization information between our resources.
 	 * @param side
 	 *            Side of the logical model to resolve. Used in conjunction with the storage accessor to
 	 *            retrieve appropriate contents for the remote variants.
 	 * @param localTraversal
 	 *            Traversal resolved for the logical model of the local file.
 	 * @param monitor
 	 *            Monitor on which to report progress to the user.
 	 * @return The set of all remote storages composing the same logical model as the given local traversal.
 	 */
 	private Set<IStorage> resolveTraversal(IStorageProviderAccessor storageAccessor, DiffSide side,
 			Set<IStorage> localTraversal, IProgressMonitor monitor) {
 		final DependencyResourceSet resourceSet = new DependencyResourceSet(dependencyGraph);
 		final StorageURIConverter converter = new RevisionedURIConverter(resourceSet.getURIConverter(),
 				storageAccessor, side);
 		resourceSet.setURIConverter(converter);
 
 		final Set<IStorage> storages = Sets.newLinkedHashSet();
 		for (IStorage local : localTraversal) {
 			final IFile localFile = adaptAs(local, IFile.class);
 
 			try {
 				final IStorageProvider remoteStorageProvider = storageAccessor.getStorageProvider(localFile,
 						side);
 				if (remoteStorageProvider != null) {
 					final IStorage start = remoteStorageProvider.getStorage(monitor);
 
 					if (resourceSet.resolveAll(start, monitor)) {
 						if (!contains(storages, start)) {
 							storages.add(start);
 						}
 						for (IStorage loaded : converter.getLoadedRevisions()) {
 							if (!contains(storages, loaded)) {
 								storages.add(loaded);
 							}
 						}
 					} else {
 						// failed to load a remote version of this resource
 					}
 				} else {
 					// file only exist locally
 				}
 			} catch (CoreException e) {
 				// failed to load a remote version of this resource
 			}
 		}
 		return storages;
 	}
 
 	/**
 	 * This implementation of a resource visitor will allow us to browse all models in a given hierarchy.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">laurent Goubet</a>
 	 */
 	private static class ModelResourceVisitor implements IResourceVisitor {
 		/** Resource Set in which we should load the temporary resources. */
 		private final DependencyResourceSet resourceSet;
 
 		/** Monitor to report progress on. */
 		// FIXME logarithmic progress
 		private final IProgressMonitor monitor;
 
 		/**
 		 * Instantiates a resource visitor.
 		 * 
 		 * @param graph
 		 *            The dependency graph that is to be populated/completed.
 		 * @param monitor
 		 *            The monitor to report progress on.
 		 */
 		public ModelResourceVisitor(Graph<URI> graph, IProgressMonitor monitor) {
 			this.resourceSet = new DependencyResourceSet(graph);
 			this.monitor = monitor;
 			final StorageURIConverter converter = new StorageURIConverter(resourceSet.getURIConverter());
 			this.resourceSet.setURIConverter(converter);
 		}
 
 		/**
 		 * Instantiates a resource visitor given the storage accessor to use for I/O operations.
 		 * 
 		 * @param storageAccessor
 		 *            The accessor to use for all I/O operations to fetch resource content.
 		 * @param side
 		 *            Side of the resources. Used in conjunction with the storage accessor to fetch proper
 		 *            content.
 		 * @param graph
 		 *            The dependency graph that is to be populated/completed.
 		 * @param monitor
 		 *            The monitor to report progress on.
 		 */
 		public ModelResourceVisitor(IStorageProviderAccessor storageAccessor, DiffSide side,
 				Graph<URI> graph, IProgressMonitor monitor) {
 			this.resourceSet = new DependencyResourceSet(graph);
 			this.monitor = monitor;
 			final StorageURIConverter converter = new RevisionedURIConverter(resourceSet.getURIConverter(),
 					storageAccessor, side);
 			this.resourceSet.setURIConverter(converter);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
 		 */
 		public boolean visit(IResource resource) throws CoreException {
 			if (resource instanceof IFile) {
 				IFile file = (IFile)resource;
 				if (hasModelType(file)) {
 					resourceSet.resolveAll(file, monitor);
 					return true;
 				}
 				return false;
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * This will listen to workspace changes and react to all changes on "model" resources as determined by
 	 * {@link ProjectModelResolver#MODEL_CONTENT_TYPES}.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">laurent Goubet</a>
 	 */
 	private static class ModelResourceListener implements IResourceChangeListener {
 		/** Keeps track of the URIs that need to be reparsed when next we need the dependencies graph . */
 		protected Set<URI> changedURIs;
 
 		/** Tracks the files that have been removed. */
 		protected Set<URI> removedURIs;
 
 		/** Initializes this listener. */
 		public ModelResourceListener() {
 			this.changedURIs = new LinkedHashSet<URI>();
 			this.removedURIs = new LinkedHashSet<URI>();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 		 */
 		public void resourceChanged(IResourceChangeEvent event) {
 			final IResourceDelta delta = event.getDelta();
 			if (delta == null) {
 				return;
 			}
 
 			try {
 				delta.accept(new ModelResourceDeltaVisitor());
 			} catch (CoreException e) {
 				EMFCompareIDEUIPlugin.getDefault().log(e);
 			}
 		}
 
 		/**
 		 * Retrieves the set of all changed URIs since we last updated the dependencies graph, and clears it
 		 * for subsequent calls.
 		 * 
 		 * @return The set of all changed URIs since we last updated the dependencies graph.
 		 */
 		public Set<URI> popChangedURIs() {
 			final Set<URI> changed;
 			synchronized(changedURIs) {
 				changed = ImmutableSet.copyOf(changedURIs);
 				changedURIs.clear();
 			}
 			return changed;
 		}
 
 		/**
 		 * Retrieves the set of all removed URIs since we last updated the dependencies graph, and clears it
 		 * for subsequent calls.
 		 * 
 		 * @return The set of all removed URIs since we last updated the dependencies graph.
 		 */
 		public Set<URI> popRemovedURIs() {
 			final Set<URI> removed;
 			synchronized(removedURIs) {
 				removed = ImmutableSet.copyOf(removedURIs);
 				removedURIs.clear();
 			}
 			return removed;
 		}
 
 		/**
 		 * Visits a resource delta to collect the changed and removed files' URIs.
 		 * 
 		 * @author <a href="mailto:laurent.goubet@obeo.fr">laurent Goubet</a>
 		 */
 		private class ModelResourceDeltaVisitor implements IResourceDeltaVisitor {
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 			 */
 			public boolean visit(IResourceDelta delta) throws CoreException {
 				if (delta.getFlags() == IResourceDelta.MARKERS
 						|| delta.getResource().getType() != IResource.FILE) {
 					return true;
 				}
 
 				final IFile file = (IFile)delta.getResource();
 				final URI fileURI = createURIFor(file);
 				// We can't check the content type of a removed resource
 				if (delta.getKind() == IResourceDelta.REMOVED) {
 					synchronized(removedURIs) {
 						removedURIs.add(fileURI);
 					}
 				} else if (hasModelType(file)) {
 					if ((delta.getKind() & (IResourceDelta.CHANGED | IResourceDelta.ADDED)) != 0) {
 						synchronized(changedURIs) {
 							changedURIs.add(fileURI);
 						}
 					}
 				}
 
 				return true;
 			}
 		}
 	}
 }
