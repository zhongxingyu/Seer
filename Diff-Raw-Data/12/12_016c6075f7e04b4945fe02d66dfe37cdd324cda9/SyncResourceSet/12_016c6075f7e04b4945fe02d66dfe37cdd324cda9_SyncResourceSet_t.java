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
 package org.eclipse.emf.compare.ide.internal.utils;
 
 import com.google.common.annotations.Beta;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.AbstractEList;
 import org.eclipse.emf.common.util.AbstractTreeIterator;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.ide.internal.utils.PriorityExecutor.Priority;
 import org.eclipse.emf.compare.ide.utils.ResourceUtil;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 
 /**
  * This implementation of a ResourceSet will avoid loading of any resource from any other mean than
  * {@link #resolveAll()}. Furthermore, it will unload any resource it has loaded as soon as its finished with
  * the cross reference links resolving.
  * <p>
  * This is used from our EMFSynchronizationModel in order to resolve the traversals without keeping the whole
  * model in-memory (and striving to never have it in-memory as a whole).
  * </p>
  * <p>
  * Since this class only aims at resolving cross-resource dependencies, its main bottleneck is the parsing of
  * resources when loading (not even the I/O, but the time spent in the SAX parser). We're using a number of
  * tricks to make this bottleneck less problematic. The main improvement in loading performance when compared
  * with the usual resource sets is the threading of the load and unload operations. {@link ResourceLoader}
  * threads are used to load the files and parse them as EMF models. When they're done, they spawn their own
  * {@link #unload(Resource) sub-threads} to unload the models in separate threads.
  * </p>
  * <p>
  * The second improvement of loading performance comes from the specialization of {@link #getLoadOptions()}
  * that allows us to define a set of options that aims at speeding up the parsing process. Further profiling
  * might be needed to isolate other options that would have an impact, such as parser features or XML
  * options...
  * </p>
  * <p>
  * Finally, the humongous CacheAdapter of UML was causing serious issues. Not only did it hinder the
  * performance, but it also caused a random dead lock between our unloading and loading threads. We managed to
  * totally disable it through three means.
  * <ul>
  * <li>The use of the {@link XMLResource#OPTION_DISABLE_NOTIFY} load option.</li>
  * <li>The use of our own {@link NoNotificationParserPool parser pool} that does not re-enable notifications
  * at the end of loading.</li>
  * <li>The removal of all roots from their containing {@link Resource} during iteration when
  * {@link #resolve(Resource) resolving}.</li>
  * </ul>
  * </p>
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 @Beta
 public final class SyncResourceSet extends ResourceSetImpl {
 	/**
 	 * Keeps track of the URIs that have been demanded by the resolving of a resource. This cache will be
 	 * invalidated when we start resolving this new set of resources.
 	 */
 	private final Set<URI> demandedURIs = Sets.newLinkedHashSet();
 
 	/** Keeps track of the URIs of all resources that have been loaded by this resource set. */
 	private final Set<URI> loadedURIs = Sets.newLinkedHashSet();
 
 	/**
 	 * This thread pool will be used to launch the loading and unloading of resources in separate threads.
 	 * <p>
 	 * Take note that the unloading threads will take precedence over the loading threads : we need to free
 	 * the memory as soon as possible, and we expect "unload" threads to complete faster that "load" ones.
 	 * </p>
 	 */
 	private final PriorityExecutor pool = new PriorityExecutor("ModelResolver"); //$NON-NLS-1$
 
 	/**
 	 * Default constructor.
 	 */
 	public SyncResourceSet() {
 		// initialize from here to avoid synchronization
 		resources = new SynchronizedResourcesEList<Resource>();
 		loadOptions = super.getLoadOptions();
 		/*
 		 * This resource set is specifically designed to resolve cross resources links, it thus spends a lot
 		 * of time loading resources. The following set of options is what seems to give the most significant
 		 * boost in loading performances, though I did not fine-tune what's really needed here. Take note that
 		 * the use of our own parser pool along with the disabling of notifications is what allows us to
 		 * bypass UML's CacheAdapter and the potential dead locks it causes.
 		 */
 		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new NoNotificationParserPool());
 		loadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
 		loadOptions.put(XMLResource.OPTION_DISABLE_NOTIFY, Boolean.TRUE);
 
 		final int bufferSize = 16384;
 		final Map<String, Object> parserProperties = Maps.newHashMap();
 		parserProperties.put("http://apache.org/xml/properties/input-buffer-size", Integer //$NON-NLS-1$
 				.valueOf(bufferSize));
 		loadOptions.put(XMLResource.OPTION_PARSER_PROPERTIES, parserProperties);
 
 		// These two might be superfluous
 		loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
 		final int expectedFeatureNames = 256;
 		loadOptions.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, Maps
 				.newHashMapWithExpectedSize(expectedFeatureNames));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#getResource(org.eclipse.emf.common.util.URI,
 	 *      boolean)
 	 */
 	@Override
 	public Resource getResource(URI uri, boolean loadOnDemand) {
 		// Never load resources from here
 		if (!loadedURIs.contains(uri)) {
 			synchronized(demandedURIs) {
 				demandedURIs.add(uri);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Overridden for synchronization.
 	 * </p>
 	 * 
 	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#createResource(org.eclipse.emf.common.util.URI)
 	 */
 	@Override
 	public synchronized Resource createResource(URI uri) {
 		return super.createResource(uri);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * Overridden for synchronization.
 	 * </p>
 	 * 
 	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#createResource(org.eclipse.emf.common.util.URI,
 	 *      java.lang.String)
 	 */
 	@Override
 	public synchronized Resource createResource(URI uri, String contentType) {
 		return super.createResource(uri, contentType);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#getResources()
 	 */
 	@Override
 	public EList<Resource> getResources() {
 		return resources;
 	}
 
 	/**
 	 * Loads the given URI as an EMF resource.
 	 * 
 	 * @param uri
 	 *            The URI to load as a resource.
 	 * @return The loaded Resource.
 	 */
 	public Resource loadResource(URI uri) {
 		/*
 		 * Don't use super.getResource : we know the resource does not exist yet as there will only be one
 		 * "load" call for each given URI. The super implementation iterates over loaded resources before
 		 * doing any actual work. That causes some minimal overhead but, more importantly, it can generate
 		 * concurrent modification exceptions.
 		 */
 		Resource result = delegatedGetResource(uri, true);
 
 		if (result == null) {
 			result = demandCreateResource(uri);
 			if (result == null) {
 				// copy/pasted from super.getResource
 				throw new RuntimeException("Cannot create a resource for '" + uri //$NON-NLS-1$
 						+ "'; a registered resource factory is needed"); //$NON-NLS-1$
 			}
 			demandLoadHelper(result);
 		}
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl#getLoadOptions()
 	 */
 	@Override
 	public Map<Object, Object> getLoadOptions() {
 		return loadOptions;
 	}
 
 	/**
 	 * Resolve all cross resources links of the given starting point. Take note that this resource set is only
 	 * interested in the URIs of the resources, and that it will not keep the loaded content in memory. No
 	 * resource will be kept in the {@link #getResources() resources'} list of this set.
 	 * 
 	 * @param start
 	 *            The starting point from which we'll resolve cross resources references.
	 * @return <code>true</code> if we could resolve the model, <code>false</code> if there was an error
	 *         loading the starting point.
 	 */
	public boolean resolveAll(IStorage start) {
 		final Resource resource = ResourceUtil.loadResource(start, this, getLoadOptions());
		if (resource == null || !resource.getErrors().isEmpty()) {
			return false;
		}

 		// reset the demanded URI that was added by this first call
 		demandedURIs.clear();
 		// and make it "loaded" instead
 		loadedURIs.add(resource.getURI());
 
 		resolve(resource);
 		unload(resource);
 
 		resolveAll();
		return true;
 	}
 
 	/**
 	 * Resolve all cross resources links of the resource located at the given URI. Take note that this
 	 * resource set is only interested in the URIs of the resource, and that it will not keep the loaded
 	 * content in memory. No resource will be kept in the {@link #getResources() resources'} list of this set.
 	 * 
 	 * @param resourceUri
 	 *            The URI of the "starting" resource from which we'll resolve all cross resources references.
 	 */
 	public void resolveAll(URI resourceUri) {
 		Resource resource = super.getResource(resourceUri, true);
 		// reset the demanded URI that was added by this first call
 		demandedURIs.clear();
 		// and make it "loaded" instead
 		loadedURIs.add(resourceUri);
 
 		resolve(resource);
 		unload(resource);
 
 		resolveAll();
 	}
 
 	/**
 	 * We've loaded a first resource (from one of the {@link #resolveAll(IStorage)} or
 	 * {@link #resolveAll(URI)} calls). We will now resolve all of this starting point's cross resources
 	 * references.
 	 */
 	private void resolveAll() {
 		Set<URI> newURIs;
 		synchronized(demandedURIs) {
 			newURIs = new LinkedHashSet<URI>(demandedURIs);
 			demandedURIs.clear();
 		}
 		while (!newURIs.isEmpty()) {
 			loadedURIs.addAll(newURIs);
 			final Set<Future<Object>> resolveThreads = Sets.newLinkedHashSet();
 			for (URI uri : newURIs) {
 				resolveThreads.add(pool.submit(new ResourceLoader(uri), Priority.LOW));
 			}
 			for (Future<Object> future : resolveThreads) {
 				try {
 					future.get();
 				} catch (InterruptedException e) {
 					// Ignore
 				} catch (ExecutionException e) {
 					// FIXME log
 				}
 			}
 			synchronized(demandedURIs) {
 				newURIs = new LinkedHashSet<URI>(demandedURIs);
 				demandedURIs.clear();
 			}
 		}
 	}
 
 	/**
 	 * Retrieve the set of all URIs that have been loaded by {@link #resolveAll()}.
 	 * 
 	 * @return The set of all URIs that have been loaded by {@link #resolveAll()}.
 	 */
 	public Set<URI> getLoadedURIs() {
 		return loadedURIs;
 	}
 
 	/**
 	 * This will resolve all cross references of the given resource, then unload it. It will then swap the
 	 * loaded resource with a new, empty one with the same URI.
 	 * 
 	 * @param resource
 	 *            The resource for which we are to resolve all cross references.
 	 */
 	private void resolve(Resource resource) {
 		resource.eSetDeliver(false);
 		final List<EObject> roots = ((InternalEList<EObject>)resource.getContents()).basicList();
 		final Iterator<EObject> resourceContent = roots.iterator();
 		resource.getContents().clear();
 		while (resourceContent.hasNext()) {
 			final EObject eObject = resourceContent.next();
 			resolveCrossReferences(eObject);
 			final TreeIterator<EObject> childContent = basicEAllContents(eObject);
 			while (childContent.hasNext()) {
 				final EObject child = childContent.next();
 				resolveCrossReferences(child);
 			}
 		}
 		resource.getContents().addAll(roots);
 	}
 
 	/**
 	 * An implementation of {@link EObject#eAllContents()} that will not resolve its proxies.
 	 * 
 	 * @param eObject
 	 *            The eobject for which contents we need an iterator.
 	 * @return The created {@link TreeIterator}.
 	 */
 	private TreeIterator<EObject> basicEAllContents(final EObject eObject) {
 		return new AbstractTreeIterator<EObject>(eObject, false) {
 			/** Generated SUID. */
 			private static final long serialVersionUID = 6874121606163401152L;
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractTreeIterator#getChildren(java.lang.Object)
 			 */
 			@Override
 			public Iterator<EObject> getChildren(Object obj) {
 				return ((InternalEList<EObject>)((EObject)obj).eContents()).basicIterator();
 			}
 		};
 	}
 
 	/**
 	 * Unload the given resource.
 	 * 
 	 * @param resource
 	 *            The resource we are to unload.
 	 */
 	private void unload(final Resource resource) {
 		// only unload those resources that are located in the workspace
 		final URI uri = resource.getURI();
 		if (uri.isPlatformResource() || uri.isRelative() || uri.isFile()) {
 			getResources().remove(resource);
 
 			// Only "unload()" when needed... The one we know of is UML
 			if (resource.getClass().getSimpleName().startsWith("UMLResource")) { //$NON-NLS-1$
 				// We still need to unload what we loaded since some (like UML) cross reference everything...
 				final Runnable unloader = new Runnable() {
 					public void run() {
 						resource.unload();
 					}
 				};
 				pool.submit(unloader, Priority.NORMAL);
 			}
 		}
 	}
 
 	/**
 	 * Resolves the cross references of the given EObject.
 	 * 
 	 * @param eObject
 	 *            The EObject for which we are to resolve the cross references.
 	 */
 	private void resolveCrossReferences(EObject eObject) {
 		final Iterator<EObject> objectChildren = ((InternalEList<EObject>)eObject.eCrossReferences())
 				.basicIterator();
 		while (objectChildren.hasNext()) {
 			final EObject eObj = objectChildren.next();
 			if (eObj.eIsProxy()) {
 				final URI proxyURI = ((InternalEObject)eObj).eProxyURI();
 				getResource(proxyURI.trimFragment(), false);
 			}
 		}
 	}
 
 	/**
 	 * This implementation of a {@link Runnable} will be used to load a given uri from the disk as an EMF
 	 * {@link Resource} and resolve all of its cross-resource links (these will be stored in
 	 * {@link #demandedURIs} as we go). Once done, it will spawn another thread in order to unload that
 	 * resource out of the memory.
 	 * 
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private class ResourceLoader implements Runnable {
 		/** The uri of the EMF Resource we are to load from the disk. */
 		private URI uri;
 
 		/**
 		 * Constructs our loader thread given its target URI.
 		 * 
 		 * @param uri
 		 *            The uri of the EMF Resource this thread is to load from the disk.
 		 */
 		public ResourceLoader(URI uri) {
 			this.uri = uri;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see java.lang.Runnable#run()
 		 */
 		public void run() {
 			final Resource newResource = loadResource(uri);
 			resolve(newResource);
 			unload(newResource);
 		}
 	}
 
 	/**
 	 * A synchronized implementation of {@link ResourcesEList}.
 	 * <p>
 	 * Note that this cannot be extracted out of the {@link SyncResourceSet} since the {@link ResourcesEList}
 	 * type is not visible.
 	 * </p>
 	 * 
 	 * @param <E>
 	 *            Type of this list's contents.
 	 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 	 */
 	private class SynchronizedResourcesEList<E extends Object & Resource> extends ResourcesEList<E> {
 		/** Generated SUID. */
 		private static final long serialVersionUID = 7371376112881960414L;
 
 		/** The lock we'll use for synchronization of the resources list. */
 		private final Object lock = new Object();
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
 		 */
 		@Override
 		public boolean containsAll(Collection<?> c) {
 			synchronized(lock) {
 				return super.containsAll(c);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#set(int, java.lang.Object)
 		 */
 		@Override
 		public E set(int index, E object) {
 			synchronized(lock) {
 				return super.set(index, object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#add(java.lang.Object)
 		 */
 		@Override
 		public boolean add(E object) {
 			synchronized(lock) {
 				return super.add(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#add(int, java.lang.Object)
 		 */
 		@Override
 		public void add(int index, E object) {
 			synchronized(lock) {
 				super.add(index, object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#addAll(java.util.Collection)
 		 */
 		@Override
 		public boolean addAll(Collection<? extends E> collection) {
 			synchronized(lock) {
 				return super.addAll(collection);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#addAll(int, java.util.Collection)
 		 */
 		@Override
 		public boolean addAll(int index, Collection<? extends E> collection) {
 			synchronized(lock) {
 				return super.addAll(index, collection);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#remove(java.lang.Object)
 		 */
 		@Override
 		public boolean remove(Object object) {
 			synchronized(lock) {
 				return super.remove(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#retainAll(java.util.Collection)
 		 */
 		@Override
 		public boolean retainAll(Collection<?> collection) {
 			synchronized(lock) {
 				return super.retainAll(collection);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#move(int, java.lang.Object)
 		 */
 		@Override
 		public void move(int index, E object) {
 			synchronized(lock) {
 				super.move(index, object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object object) {
 			synchronized(lock) {
 				return super.equals(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			synchronized(lock) {
 				return super.hashCode();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#toString()
 		 */
 		@Override
 		public String toString() {
 			synchronized(lock) {
 				return super.toString();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#iterator()
 		 */
 		@Override
 		public Iterator<E> iterator() {
 			return new SynchronizedEIterator();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#listIterator()
 		 */
 		@Override
 		public ListIterator<E> listIterator() {
 			return new SynchronizedEListIterator();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.AbstractEList#listIterator(int)
 		 */
 		@Override
 		public ListIterator<E> listIterator(int index) {
 			synchronized(lock) {
 				int curSize = size();
 				if (index < 0 || index > curSize) {
 					throw new BasicIndexOutOfBoundsException(index, curSize);
 				}
 				return new SynchronizedEListIterator(index);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#indexOf(java.lang.Object)
 		 */
 		@Override
 		public int indexOf(Object object) {
 			synchronized(lock) {
 				return super.indexOf(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#lastIndexOf(java.lang.Object)
 		 */
 		@Override
 		public int lastIndexOf(Object object) {
 			synchronized(lock) {
 				return super.lastIndexOf(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#toArray()
 		 */
 		@Override
 		public Object[] toArray() {
 			synchronized(lock) {
 				return super.toArray();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#toArray(T[])
 		 */
 		@Override
 		public <T> T[] toArray(T[] array) {
 			synchronized(lock) {
 				return super.toArray(array);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#setData(int, java.lang.Object[])
 		 */
 		@Override
 		public void setData(int size, Object[] data) {
 			synchronized(lock) {
 				super.setData(size, data);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#get(int)
 		 */
 		@Override
 		public E get(int index) {
 			synchronized(lock) {
 				return super.get(index);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#basicGet(int)
 		 */
 		@Override
 		public E basicGet(int index) {
 			synchronized(lock) {
 				return super.basicGet(index);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#shrink()
 		 */
 		@Override
 		public void shrink() {
 			synchronized(lock) {
 				super.shrink();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#grow(int)
 		 */
 		@Override
 		public void grow(int minimumCapacity) {
 			synchronized(lock) {
 				super.grow(minimumCapacity);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#clone()
 		 */
 		// CHECKSTYLE:OFF we're overriding...
 		@Override
 		public Object clone() {
 			// CHECKSTYLE:ON
 			synchronized(lock) {
 				return super.clone();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#addUnique(java.lang.Object)
 		 */
 		@Override
 		public void addUnique(E object) {
 			synchronized(lock) {
 				super.addUnique(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#addUnique(int, java.lang.Object)
 		 */
 		@Override
 		public void addUnique(int index, E object) {
 			synchronized(lock) {
 				super.addUnique(index, object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#addAllUnique(java.util.Collection)
 		 */
 		@Override
 		public boolean addAllUnique(Collection<? extends E> collection) {
 			synchronized(lock) {
 				return super.addAllUnique(collection);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#addAllUnique(int, java.util.Collection)
 		 */
 		@Override
 		public boolean addAllUnique(int index, Collection<? extends E> collection) {
 			synchronized(lock) {
 				return super.addAllUnique(index, collection);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#addAllUnique(java.lang.Object[], int,
 		 *      int)
 		 */
 		@Override
 		public boolean addAllUnique(Object[] objects, int start, int end) {
 			synchronized(lock) {
 				return super.addAllUnique(objects, start, end);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#addAllUnique(int, java.lang.Object[],
 		 *      int, int)
 		 */
 		@Override
 		public boolean addAllUnique(int index, Object[] objects, int start, int end) {
 			synchronized(lock) {
 				return super.addAllUnique(index, objects, start, end);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#basicAdd(java.lang.Object,
 		 *      org.eclipse.emf.common.notify.NotificationChain)
 		 */
 		@Override
 		public NotificationChain basicAdd(E object, NotificationChain notifications) {
 			synchronized(lock) {
 				return super.basicAdd(object, notifications);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#remove(int)
 		 */
 		@Override
 		public E remove(int index) {
 			synchronized(lock) {
 				return super.remove(index);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#removeAll(java.util.Collection)
 		 */
 		@Override
 		public boolean removeAll(Collection<?> collection) {
 			synchronized(lock) {
 				return super.removeAll(collection);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#basicRemove(java.lang.Object,
 		 *      org.eclipse.emf.common.notify.NotificationChain)
 		 */
 		@Override
 		public NotificationChain basicRemove(Object object, NotificationChain notifications) {
 			synchronized(lock) {
 				return super.basicRemove(object, notifications);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#clear()
 		 */
 		@Override
 		public void clear() {
 			synchronized(lock) {
 				super.clear();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#setUnique(int, java.lang.Object)
 		 */
 		@Override
 		public E setUnique(int index, E object) {
 			synchronized(lock) {
 				return super.setUnique(index, object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#basicSet(int, java.lang.Object,
 		 *      org.eclipse.emf.common.notify.NotificationChain)
 		 */
 		@Override
 		public NotificationChain basicSet(int index, E object, NotificationChain notifications) {
 			synchronized(lock) {
 				return super.basicSet(index, object, notifications);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#move(int, int)
 		 */
 		@Override
 		public E move(int targetIndex, int sourceIndex) {
 			synchronized(lock) {
 				return super.move(targetIndex, sourceIndex);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicList()
 		 */
 		@Override
 		public List<E> basicList() {
 			synchronized(lock) {
 				return super.basicList();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicIterator()
 		 */
 		@Override
 		public Iterator<E> basicIterator() {
 			return new SynchronizedNonResolvingEIterator();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicListIterator()
 		 */
 		@Override
 		public ListIterator<E> basicListIterator() {
 			return new SynchronizedNonResolvingEListIterator();
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicListIterator(int)
 		 */
 		@Override
 		public ListIterator<E> basicListIterator(int index) {
 			synchronized(lock) {
 				int curSize = size();
 				if (index < 0 || index > curSize) {
 					throw new BasicIndexOutOfBoundsException(index, curSize);
 				}
 				return new SynchronizedNonResolvingEListIterator(index);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl.ResourcesEList#contains(java.lang.Object)
 		 */
 		@Override
 		public boolean contains(Object object) {
 			synchronized(lock) {
 				return super.contains(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicContains(java.lang.Object)
 		 */
 		@Override
 		public boolean basicContains(Object object) {
 			synchronized(lock) {
 				return super.basicContains(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicContainsAll(java.util.Collection)
 		 */
 		@Override
 		public boolean basicContainsAll(Collection<?> collection) {
 			synchronized(lock) {
 				return super.basicContainsAll(collection);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicIndexOf(java.lang.Object)
 		 */
 
 		@Override
 		public int basicIndexOf(Object object) {
 			synchronized(lock) {
 				return super.basicIndexOf(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicLastIndexOf(java.lang.Object)
 		 */
 		@Override
 		public int basicLastIndexOf(Object object) {
 			synchronized(lock) {
 				return super.basicLastIndexOf(object);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicToArray()
 		 */
 		@Override
 		public Object[] basicToArray() {
 			synchronized(lock) {
 				return super.basicToArray();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.util.NotifyingInternalEListImpl#basicToArray(T[])
 		 */
 		@Override
 		public <T> T[] basicToArray(T[] array) {
 			synchronized(lock) {
 				return super.basicToArray(array);
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#data()
 		 */
 		@Override
 		public Object[] data() {
 			synchronized(lock) {
 				return super.data();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.notify.impl.NotifyingListImpl#getFeature()
 		 */
 		@Override
 		public Object getFeature() {
 			synchronized(lock) {
 				return super.getFeature();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl.ResourcesEList#getFeatureID()
 		 */
 		@Override
 		public int getFeatureID() {
 			synchronized(lock) {
 				return super.getFeatureID();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.ecore.resource.impl.ResourceSetImpl.ResourcesEList#getNotifier()
 		 */
 		@Override
 		public Object getNotifier() {
 			synchronized(lock) {
 				return super.getNotifier();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#isEmpty()
 		 */
 		@Override
 		public boolean isEmpty() {
 			synchronized(lock) {
 				return super.isEmpty();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.common.util.BasicEList#size()
 		 */
 		@Override
 		public int size() {
 			synchronized(lock) {
 				return super.size();
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see java.util.AbstractList#subList(int, int)
 		 */
 		@Override
 		public List<E> subList(int fromIndex, int toIndex) {
 			synchronized(lock) {
 				return super.subList(fromIndex, toIndex);
 			}
 		}
 
 		/**
 		 * A synchronized implementation of the {@link AbstractEList.EIterator}.
 		 * 
 		 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 		 */
 		private class SynchronizedEIterator extends AbstractEList<E>.EIterator<E> {
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#hasNext()
 			 */
 			@Override
 			public boolean hasNext() {
 				synchronized(lock) {
 					return super.hasNext();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#next()
 			 */
 			@Override
 			public E next() {
 				synchronized(lock) {
 					return super.next();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#remove()
 			 */
 			@Override
 			public void remove() {
 				synchronized(lock) {
 					super.remove();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#equals(java.lang.Object)
 			 */
 			@Override
 			public boolean equals(Object obj) {
 				synchronized(lock) {
 					return super.equals(obj);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#hashCode()
 			 */
 			@Override
 			public int hashCode() {
 				synchronized(lock) {
 					return super.hashCode();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#toString()
 			 */
 			@Override
 			public String toString() {
 				synchronized(lock) {
 					return super.toString();
 				}
 			}
 		}
 
 		/**
 		 * A synchronized implementation of the {@link AbstractEList.EListIterator}.
 		 * 
 		 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 		 */
 		private class SynchronizedEListIterator extends AbstractEList<E>.EListIterator<E> {
 			/**
 			 * Delegates to the super constructor.
 			 */
 			public SynchronizedEListIterator() {
 				super();
 			}
 
 			/**
 			 * Delegates to the super constructor.
 			 * 
 			 * @param index
 			 *            Index at which this list iterator should start.
 			 */
 			public SynchronizedEListIterator(int index) {
 				super(index);
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#add(java.lang.Object)
 			 */
 			@Override
 			public void add(E object) {
 				synchronized(lock) {
 					super.add(object);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#hasNext()
 			 */
 			@Override
 			public boolean hasNext() {
 				synchronized(lock) {
 					return super.hasNext();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#hasPrevious()
 			 */
 			@Override
 			public boolean hasPrevious() {
 				synchronized(lock) {
 					return super.hasPrevious();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#next()
 			 */
 			@Override
 			public E next() {
 				synchronized(lock) {
 					return super.next();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#previous()
 			 */
 			@Override
 			public E previous() {
 				synchronized(lock) {
 					return super.previous();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#previousIndex()
 			 */
 			@Override
 			public int previousIndex() {
 				synchronized(lock) {
 					return super.previousIndex();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#remove()
 			 */
 			@Override
 			public void remove() {
 				synchronized(lock) {
 					super.remove();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#set(java.lang.Object)
 			 */
 			@Override
 			public void set(E object) {
 				synchronized(lock) {
 					super.set(object);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#equals(java.lang.Object)
 			 */
 			@Override
 			public boolean equals(Object obj) {
 				synchronized(lock) {
 					return super.equals(obj);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#hashCode()
 			 */
 			@Override
 			public int hashCode() {
 				synchronized(lock) {
 					return super.hashCode();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#toString()
 			 */
 			@Override
 			public String toString() {
 				synchronized(lock) {
 					return super.toString();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#nextIndex()
 			 */
 			@Override
 			public int nextIndex() {
 				synchronized(lock) {
 					return super.nextIndex();
 				}
 			}
 		}
 
 		/**
 		 * A synchronized implementation of the {@link AbstractEList.NonResolvingEIterator}.
 		 * 
 		 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 		 */
 		private class SynchronizedNonResolvingEIterator extends AbstractEList<E>.NonResolvingEIterator<E> {
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#hasNext()
 			 */
 			@Override
 			public boolean hasNext() {
 				synchronized(lock) {
 					return super.hasNext();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#next()
 			 */
 			@Override
 			public E next() {
 				synchronized(lock) {
 					return super.next();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.NonResolvingEIterator#remove()
 			 */
 			@Override
 			public void remove() {
 				synchronized(lock) {
 					super.remove();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#equals(java.lang.Object)
 			 */
 			@Override
 			public boolean equals(Object obj) {
 				synchronized(lock) {
 					return super.equals(obj);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#hashCode()
 			 */
 			@Override
 			public int hashCode() {
 				synchronized(lock) {
 					return super.hashCode();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#toString()
 			 */
 			@Override
 			public String toString() {
 				synchronized(lock) {
 					return super.toString();
 				}
 			}
 		}
 
 		/**
 		 * A synchronized implementation of the {@link AbstractEList.NonResolvingEListIterator}.
 		 * 
 		 * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
 		 */
 		private class SynchronizedNonResolvingEListIterator extends AbstractEList<E>.NonResolvingEListIterator<E> {
 			/**
 			 * Delegates to the super constructor.
 			 */
 			public SynchronizedNonResolvingEListIterator() {
 				super();
 			}
 
 			/**
 			 * Delegates to the super constructor.
 			 * 
 			 * @param index
 			 *            Index at which the iteration should start.
 			 */
 			public SynchronizedNonResolvingEListIterator(int index) {
 				super(index);
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.NonResolvingEListIterator#add(java.lang.Object)
 			 */
 			@Override
 			public void add(E object) {
 				synchronized(lock) {
 					super.add(object);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#hasNext()
 			 */
 			@Override
 			public boolean hasNext() {
 				synchronized(lock) {
 					return super.hasNext();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#hasPrevious()
 			 */
 			@Override
 			public boolean hasPrevious() {
 				synchronized(lock) {
 					return super.hasPrevious();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EIterator#next()
 			 */
 			@Override
 			public E next() {
 				synchronized(lock) {
 					return super.next();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#previous()
 			 */
 			@Override
 			public E previous() {
 				synchronized(lock) {
 					return super.previous();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#previousIndex()
 			 */
 			@Override
 			public int previousIndex() {
 				synchronized(lock) {
 					return super.previousIndex();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.NonResolvingEListIterator#remove()
 			 */
 			@Override
 			public void remove() {
 				synchronized(lock) {
 					super.remove();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.NonResolvingEListIterator#set(java.lang.Object)
 			 */
 			@Override
 			public void set(E object) {
 				synchronized(lock) {
 					super.set(object);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#equals(java.lang.Object)
 			 */
 			@Override
 			public boolean equals(Object obj) {
 				synchronized(lock) {
 					return super.equals(obj);
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#hashCode()
 			 */
 			@Override
 			public int hashCode() {
 				synchronized(lock) {
 					return super.hashCode();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see java.lang.Object#toString()
 			 */
 			@Override
 			public String toString() {
 				synchronized(lock) {
 					return super.toString();
 				}
 			}
 
 			/**
 			 * {@inheritDoc}
 			 * 
 			 * @see org.eclipse.emf.common.util.AbstractEList.EListIterator#nextIndex()
 			 */
 			@Override
 			public int nextIndex() {
 				synchronized(lock) {
 					return super.nextIndex();
 				}
 			}
 		}
 	}
 }
