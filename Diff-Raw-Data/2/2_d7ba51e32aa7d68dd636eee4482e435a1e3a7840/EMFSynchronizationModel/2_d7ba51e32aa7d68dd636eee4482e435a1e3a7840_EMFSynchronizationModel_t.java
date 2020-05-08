 /*******************************************************************************
  * Copyright (c) 2011, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.logical;
 
 import static org.eclipse.emf.compare.ide.utils.ResourceUtil.binaryIdentical;
 import static org.eclipse.emf.compare.ide.utils.ResourceUtil.createURIFor;
 
 import com.google.common.annotations.Beta;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Sets;
 
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.compare.IResourceProvider;
 import org.eclipse.compare.ISharedDocumentAdapter;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.mapping.RemoteResourceMappingContext;
 import org.eclipse.core.resources.mapping.ResourceMappingContext;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.ide.internal.utils.NotLoadingResourceSet;
 import org.eclipse.emf.compare.ide.internal.utils.SyncResourceSet;
 import org.eclipse.emf.compare.ide.utils.StorageTraversal;
 import org.eclipse.emf.compare.ide.utils.StorageURIConverter;
 import org.eclipse.emf.compare.scope.DefaultComparisonScope;
 import org.eclipse.emf.compare.scope.FilterComparisonScope;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.ui.IEditorInput;
 
 /**
  * This class will act as a logical model for EMF. It will hold the necessary logic to be able to determine
  * which "other" files are to be considered dependencies of a "starting point". For example, when trying to
  * compare a "genmodel" file, we cannot compare the genmodel alone, we need to compare the underlying "ecore"
  * file along with it.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 @Beta
 public final class EMFSynchronizationModel {
 	/** The traversal corresponding to the left side. */
 	private final StorageTraversal leftTraversal;
 
 	/** The traversal corresponding to the right side. */
 	private final StorageTraversal rightTraversal;
 
 	/** The traversal corresponding to the common ancestor of both other side. */
 	private final StorageTraversal originTraversal;
 
 	/**
 	 * While loading this model, we might find that the left side cannot be edited (i.e. we could not save it
 	 * even if we were to edit it). This might notably be the case for comparison with the Git Index : Git
 	 * allows modification of the index, but we would not be able to save these modifications (cannot open an
 	 * output stream towards the 'index' revisions of all files composing the logical model). This will be
 	 * used to alter the compare configuration.
 	 */
 	private final boolean leftEditable;
 
 	/**
 	 * See {@link #leftEditable}.
 	 * 
 	 * @see #leftEditable
 	 */
 	private final boolean rightEditable;
 
 	/**
 	 * Constructs our logical model given the three traversal for our sides.
 	 * 
 	 * @param leftTraversal
 	 *            The traversal corresponding to the left side.
 	 * @param rightTraversal
 	 *            The traversal corresponding to the right side.
 	 * @param originTraversal
 	 *            The traversal corresponding to the common ancestor of both other side. Can be
 	 *            <code>null</code>.
 	 */
 	private EMFSynchronizationModel(StorageTraversal leftTraversal, StorageTraversal rightTraversal,
 			StorageTraversal originTraversal, boolean leftEditable, boolean rightEditable) {
 		if (leftTraversal == null) {
 			this.leftTraversal = new StorageTraversal(Sets.<IStorage> newHashSet());
 		} else {
 			this.leftTraversal = leftTraversal;
 		}
 
 		if (rightTraversal == null) {
 			this.rightTraversal = new StorageTraversal(Sets.<IStorage> newHashSet());
 		} else {
 			this.rightTraversal = rightTraversal;
 		}
 
 		if (originTraversal == null) {
 			this.originTraversal = new StorageTraversal(Sets.<IStorage> newHashSet());
 		} else {
 			this.originTraversal = originTraversal;
 		}
 
 		this.leftEditable = leftEditable;
 		this.rightEditable = rightEditable;
 	}
 
 	// TODO comment supported ITypedElements
 	/**
 	 * Creates a synchronization model by resolving the full logical model of the given elements.
 	 * 
 	 * @param left
 	 *            The left resource, starting point of the left logical model we are to resolve.
 	 * @param right
 	 *            The right resource, starting point of the right logical model we are to resolve.
 	 * @param origin
 	 *            The origin resource, starting point of the logical model we are to resolve as the origin
 	 *            one. Can be <code>null</code>.
 	 * @return The created synchronization model.
 	 */
 	public static EMFSynchronizationModel createSynchronizationModel(ITypedElement left, ITypedElement right,
 			ITypedElement origin) {
 		/*
 		 * We need a way to load these models. If it is a local file, we'll simply resolve the resource set.
 		 * For any ITypedElement from which we can find an IFileRevision, we'll resolve the resource set by
 		 * using this file revision as a "base" i.e : we won't load any resource which revision is younger.
 		 */
 		final IFileRevision leftRevision = findFileRevision(left);
 		final IFileRevision rightRevision = findFileRevision(right);
 		final IFileRevision originRevision = findFileRevision(origin);
 
 		boolean canEditLeft = true;
 		boolean canEditRight = true;
 
 		final StorageTraversal leftTraversal;
 		final StorageTraversal rightTraversal;
 		final StorageTraversal originTraversal;
 		if (leftRevision == null) {
 			// Load it as a local model
 			final IResource leftRes = findResource(left);
 			leftTraversal = resolveTraversal(leftRes);
 		} else {
 			canEditLeft = false;
 			leftTraversal = resolveTraversal(leftRevision);
 		}
 		if (rightRevision == null) {
 			// Load it as a local model
 			final IResource rightRes = findResource(right);
 			rightTraversal = resolveTraversal(rightRes);
 		} else {
 			canEditRight = false;
 			rightTraversal = resolveTraversal(rightRevision);
 		}
 		if (originRevision == null) {
 			// Load it as a local model
 			final IResource originRes = findResource(origin);
 			originTraversal = resolveTraversal(originRes);
 		} else {
 			originTraversal = resolveTraversal(originRevision);
 		}
 
 		return new EMFSynchronizationModel(leftTraversal, rightTraversal, originTraversal, canEditLeft,
 				canEditRight);
 	}
 
 	/**
 	 * Creates a synchronization model by resolving the full logical model of the given local resource,
 	 * retrieving remote content through the given {@link ResourceMappingContext context}.
 	 * 
 	 * @param local
 	 *            The local file, will be considered "left" side of this model.
 	 * @param context
 	 *            The context from which we are to retrieve remote content.
 	 * @param monitor
 	 *            Will be used to report progress to the user.
 	 * @return The created synchronization model.
 	 */
 	public static EMFSynchronizationModel createSynchronizationModel(IFile local,
 			RemoteResourceMappingContext context, IProgressMonitor monitor) {
 		final StorageTraversal leftTraversal = resolveTraversal(local);
 		StorageTraversal rightTraversal = null;
 		StorageTraversal originTraversal = null;
 
 		try {
 			final IStorage right = context.fetchRemoteContents(local, monitor);
 			System.out.println("here");
 		} catch (CoreException e) {
 			// FIXME log, fail?
 			rightTraversal = new StorageTraversal(Sets.<IFile> newLinkedHashSet());
 		}
 
 		try {
 			final IStorage origin = context.fetchBaseContents(local, monitor);
 		} catch (CoreException e) {
 			// FIXME log
 			originTraversal = new StorageTraversal(Sets.<IFile> newLinkedHashSet());
 		}
 		return new EMFSynchronizationModel(leftTraversal, rightTraversal, originTraversal, local.exists()
 				&& !local.isReadOnly(), false);
 	}
 
 	/**
 	 * Creates a synchronization model by resolving the full logical model of the given local resources.
 	 * 
 	 * @param left
 	 *            The left resource, starting point of the left logical model we are to resolve.
 	 * @param right
 	 *            The right resource, starting point of the right logical model we are to resolve.
 	 * @param origin
 	 *            The origin resource, starting point of the logical model we are to resolve as the origin
 	 *            one. Can be <code>null</code>.
 	 * @return The created synchronization model.
 	 */
 	public static EMFSynchronizationModel createSynchronizationModel(IResource left, IResource right,
 			IResource origin) {
 		final StorageTraversal leftTraversal = resolveTraversal(left);
 		final StorageTraversal rightTraversal = resolveTraversal(right);
 		final StorageTraversal originTraversal = resolveTraversal(origin);
 
 		return new EMFSynchronizationModel(leftTraversal, rightTraversal, originTraversal, true, true);
 	}
 
 	/**
 	 * Creates a synchronization model by resolving the full logical model from the three given starting
 	 * points. We'll consider all of this URIs as pointing towards "local" resources.
 	 * 
 	 * @param left
 	 *            The left URI, starting point of the left logical model we are to resolve.
 	 * @param right
 	 *            The right URI, starting point of the right logical model we are to resolve.
 	 * @param origin
 	 *            The origin URI, starting point of the logical model we are to resolve as the origin one. Can
 	 *            be <code>null</code>.
 	 * @return The created synchronization model.
 	 */
 	public static EMFSynchronizationModel createSynchronizationModel(URI left, URI right, URI origin) {
 		final StorageTraversal leftTraversal = resolveTraversal(left);
 		final StorageTraversal rightTraversal = resolveTraversal(right);
 		final StorageTraversal originTraversal = resolveTraversal(origin);
 
 		return new EMFSynchronizationModel(leftTraversal, rightTraversal, originTraversal, true, true);
 	}
 
 	public IComparisonScope createMinimizedScope() {
 		// Minimize the traversals to non-read-only resources with no binary identical counterparts.
 		minimize();
 
 		// Create the left, right and origin resource sets.
 		final ResourceSet leftResourceSet = new NotLoadingResourceSet(leftTraversal);
 		final ResourceSet rightResourceSet = new NotLoadingResourceSet(rightTraversal);
 		final ResourceSet originResourceSet;
 		if (originTraversal == null || originTraversal.getStorages().isEmpty()) {
 			// FIXME why would an empty resource set yield a different result ?
 			originResourceSet = null;
 		} else {
 			originResourceSet = new NotLoadingResourceSet(originTraversal);
 		}
 
 		final Set<URI> urisInScope = Sets.newLinkedHashSet();
 		for (IStorage left : leftTraversal.getStorages()) {
 			urisInScope.add(createURIFor(left));
 		}
 		for (IStorage right : rightTraversal.getStorages()) {
 			urisInScope.add(createURIFor(right));
 		}
		for (IStorage origin : originTraversal.getStorages()) {
 			urisInScope.add(createURIFor(origin));
 		}
 
 		final FilterComparisonScope scope = new DefaultComparisonScope(leftResourceSet, rightResourceSet,
 				originResourceSet);
 		scope.setResourceSetContentFilter(isInScope(urisInScope));
 		return scope;
 	}
 
 	/**
 	 * This can be called to reduce the number of resources in this model's traversals. Specifically, we'll
 	 * remove all resources that can be seen as binary identical (we match resources through exact equality of
 	 * thier names) or read-only.
 	 */
 	public void minimize() {
 		final boolean threeWay = !originTraversal.getStorages().isEmpty();
 		// Copy the sets to update them as we go.
 		final Set<IStorage> leftCopy = Sets.newLinkedHashSet(leftTraversal.getStorages());
 		final Set<IStorage> rightCopy = Sets.newLinkedHashSet(rightTraversal.getStorages());
 		final Set<IStorage> originCopy = Sets.newLinkedHashSet(originTraversal.getStorages());
 
 		for (IStorage left : leftCopy) {
 			final IStorage right = removeLikeNamedStorageFrom(left, rightCopy);
 			if (right != null && threeWay) {
 				final IStorage origin = removeLikeNamedStorageFrom(left, originCopy);
 
 				if (origin != null && binaryIdentical(left, right, origin)) {
 					leftTraversal.getStorages().remove(left);
 					rightTraversal.getStorages().remove(right);
 					originTraversal.getStorages().remove(origin);
 				}
 			} else if (right != null && binaryIdentical(left, right)) {
 				leftTraversal.getStorages().remove(left);
 				rightTraversal.getStorages().remove(right);
 			} else if (right == null) {
 				// This file has no match. remove it if read only
 				if (left.isReadOnly()) {
 					leftTraversal.getStorages().remove(left);
 				}
 			}
 		}
 
 		for (IStorage right : rightCopy) {
 			// These have no match on left. Remove if read only
 			if (right.isReadOnly()) {
 				rightTraversal.getStorages().remove(right);
 			}
 		}
 
 		for (IStorage origin : originCopy) {
 			// These have no match on left and right. Remove if read only
 			if (origin.isReadOnly()) {
 				originTraversal.getStorages().remove(origin);
 			}
 		}
 	}
 
 	/**
 	 * Looks up into the {@code candidates} set for a storage which name matches that of the {@code reference}
 	 * storage, removing it if there is one.
 	 * 
 	 * @param reference
 	 *            The storage for which we'll seek a match into {@code candidates}.
 	 * @param candidates
 	 *            The set of candidates into which to look up for a match to {@code reference}.
 	 * @return The first storage from the set of candidates that matches the {@code reference}, if any.
 	 *         <code>null</code> if none match.
 	 */
 	private IStorage removeLikeNamedStorageFrom(IStorage reference, Set<IStorage> candidates) {
 		final String referenceName = reference.getName();
 		final Iterator<IStorage> candidatesIterator = candidates.iterator();
 		while (candidatesIterator.hasNext()) {
 			final IStorage candidate = candidatesIterator.next();
 			final String candidateName = candidate.getName();
 
 			if (referenceName.equals(candidateName)) {
 				candidatesIterator.remove();
 				return candidate;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This is only meant for internal usage.
 	 * 
 	 * @return The left traversal of this model.
 	 */
 	/* package */StorageTraversal getLeftTraversal() {
 		return leftTraversal;
 	}
 
 	/**
 	 * Clients may call this in order to determine whether the left logical model can be edited.
 	 * 
 	 * @return <code>true</code> if modifications to the left model should be allowed, <code>false</code>
 	 *         otherwise.
 	 * @see #leftEditable
 	 */
 	public boolean isLeftEditable() {
 		return leftEditable;
 	}
 
 	/**
 	 * Clients may call this in order to determine whether the right logical model can be edited.
 	 * 
 	 * @return <code>true</code> if modifications to the right model should be allowed, <code>false</code>
 	 *         otherwise.
 	 * @see #leftEditable
 	 */
 	public boolean isRightEditable() {
 		return rightEditable;
 	}
 
 	/**
 	 * Tries and resolve the resource traversal corresponding to the given starting point.
 	 * 
 	 * @param start
 	 *            The resource that will be considered as the "starting point" of the traversal to resolve.
 	 * @return The resource traversal corresponding to the logical model that's been computed from the given
 	 *         starting point.
 	 */
 	// package visibility as this will be used by our model provider
 	/* package */static StorageTraversal resolveTraversal(IResource start) {
 		if (!(start instanceof IFile)) {
 			return new StorageTraversal(Sets.<IFile> newLinkedHashSet());
 		}
 
 		/*
 		 * TODO make model resolver extension point use IResource instead of EMF resource ... and use it here.
 		 * For now, we'll simply load the resource as an EMF model and resolve it all.
 		 */
 		final SyncResourceSet resourceSet = new SyncResourceSet();
 		final StorageURIConverter converter = new StorageURIConverter(resourceSet.getURIConverter());
 		resourceSet.setURIConverter(converter);
 
 		if (resourceSet.resolveAll((IFile)start)) {
 			final Set<IStorage> storages = Sets.newLinkedHashSet(Sets.union(Collections
 					.singleton((IFile)start), converter.getLoadedRevisions()));
 			return new StorageTraversal(storages);
 		}
 
 		return new StorageTraversal(Collections.singleton((IFile)start));
 	}
 
 	/**
 	 * Tries and resolve the resource traversal corresponding to the given starting point.
 	 * 
 	 * @param start
 	 *            The URI of the resource that will be considered as the "starting point" of the traversal to
 	 *            resolve.
 	 * @return The resource traversal corresponding to the logical model that's been computed from the given
 	 *         starting point.
 	 */
 	private static StorageTraversal resolveTraversal(URI start) {
 		if (start == null) {
 			return new StorageTraversal(Sets.<IFile> newLinkedHashSet());
 		}
 
 		final SyncResourceSet resourceSet = new SyncResourceSet();
 		final StorageURIConverter converter = new StorageURIConverter(resourceSet.getURIConverter());
 		resourceSet.setURIConverter(converter);
 		resourceSet.resolveAll(start);
 
 		final Set<IStorage> storages = Sets.newLinkedHashSet(converter.getLoadedRevisions());
 		return new StorageTraversal(storages);
 
 	}
 
 	/**
 	 * Tries and resolve the resource traversal corresponding to the given starting point.
 	 * 
 	 * @param start
 	 *            The revision that will be considered as the "starting point" of the traversal to resolve.
 	 * @return The resource traversal corresponding to the logical model that's been computed from the given
 	 *         starting point.
 	 */
 	private static StorageTraversal resolveTraversal(IFileRevision start) {
 		if (start == null) {
 			return new StorageTraversal(Sets.<IFile> newLinkedHashSet());
 		}
 
 		// TODO how could we make this extensible?
 		StorageTraversal traversal = new StorageTraversal(Sets.<IFile> newLinkedHashSet());
 		final SyncResourceSet resourceSet = new SyncResourceSet();
 		final StorageURIConverter converter = new RevisionedURIConverter(resourceSet.getURIConverter(), start);
 		resourceSet.setURIConverter(converter);
 		try {
 			final IStorage startStorage = start.getStorage(new NullProgressMonitor());
 			if (resourceSet.resolveAll(startStorage)) {
 				final Set<IStorage> storages = Sets.newLinkedHashSet(Sets.union(Collections
 						.singleton(startStorage), converter.getLoadedRevisions()));
 				traversal = new StorageTraversal(storages);
 			} else {
 				// FIXME log
 				// We failed to load the starting point. simply return an empty traversal.
 			}
 		} catch (CoreException e) {
 			// FIXME ignore for now
 		}
 		return traversal;
 	}
 
 	/**
 	 * Try and determine the file revision of the given element.
 	 * 
 	 * @param element
 	 *            The element for which we need an {@link IFileRevision}.
 	 * @return The file revision of the given element if we could find one, <code>null</code> otherwise.
 	 */
 	private static IFileRevision findFileRevision(ITypedElement element) {
 		if (element == null) {
 			return null;
 		}
 
 		// Can we adapt it directly?
 		IFileRevision revision = adaptAs(element, IFileRevision.class);
 		if (revision == null) {
 			// Quite the workaround... but CVS does not offer us any other way.
 			// These few lines of code is what make us depend on org.eclipse.ui... Can we find another way?
 			final ISharedDocumentAdapter documentAdapter = adaptAs(element, ISharedDocumentAdapter.class);
 			if (documentAdapter != null) {
 				final IEditorInput editorInput = documentAdapter.getDocumentKey(element);
 				if (editorInput != null) {
 					revision = adaptAs(editorInput, IFileRevision.class);
 				}
 			}
 		}
 
 		if (revision == null) {
 			// Couldn't do it the API way ...
 			// At the time of writing, this was the case with EGit
 			try {
 				final Method method = element.getClass().getMethod("getFileRevision"); //$NON-NLS-1$
 				final Object value = method.invoke(element);
 				if (value instanceof IFileRevision) {
 					revision = (IFileRevision)value;
 				}
 				// CHECKSTYLE:OFF this would require five "catch" for ignored exceptions...
 			} catch (Exception e) {
 				// CHECKSTYLE:ON
 			}
 		}
 
 		return revision;
 	}
 
 	/**
 	 * Try and determine the resource of the given element.
 	 * 
 	 * @param element
 	 *            The element for which we need an {@link IResource}.
 	 * @return The resource corresponding to the given {@code element} if we could find it, <code>null</code>
 	 *         otherwise.
 	 */
 	private static IResource findResource(ITypedElement element) {
 		if (element == null) {
 			return null;
 		}
 
 		// Can we adapt it directly?
 		IResource resource = adaptAs(element, IResource.class);
 		if (resource == null) {
 			// We know about some types ...
 			if (element instanceof IResourceProvider) {
 				resource = ((IResourceProvider)element).getResource();
 			}
 		}
 
 		return resource;
 	}
 
 	/**
 	 * Tries and adapt the given <em>object</em> to an instance of the given class.
 	 * 
 	 * @param <T>
 	 *            Type to which we need to adapt <em>object</em>.
 	 * @param object
 	 *            The object we need to coerce to a given {@link Class}.
 	 * @param clazz
 	 *            Class to which we are to adapt <em>object</em>.
 	 * @return <em>object</em> cast to type <em>T</em> if possible, <code>null</code> if not.
 	 */
 	@SuppressWarnings("unchecked")
 	private static <T> T adaptAs(Object object, Class<T> clazz) {
 		if (object == null) {
 			return null;
 		}
 
 		T result = null;
 		if (clazz.isInstance(object)) {
 			result = (T)object;
 		} else if (object instanceof IAdaptable) {
 			result = (T)((IAdaptable)object).getAdapter(clazz);
 		}
 
 		if (result == null) {
 			result = (T)Platform.getAdapterManager().getAdapter(object, clazz);
 		}
 
 		return result;
 	}
 
 	/**
 	 * Returns a predicate that can be applied to {@link Resource}s in order to check if their URI is
 	 * contained in the given set.
 	 * 
 	 * @param uris
 	 *            URIs that we consider to be in this scope.
 	 * @return A useable Predicate.
 	 */
 	private static Predicate<Resource> isInScope(final Set<URI> uris) {
 		return new Predicate<Resource>() {
 			public boolean apply(Resource input) {
 				return input != null && uris.contains(input.getURI());
 			}
 		};
 	}
 }
