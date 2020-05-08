 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.collab.ide.adapters;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.SaveException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.ide.notification.WorkspaceTypeListener;
 import org.eclipse.mylyn.docs.intent.collab.ide.repository.WorkspaceRepository;
 import org.eclipse.mylyn.docs.intent.collab.ide.repository.WorkspaceSession;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 
 /**
  * Adapter that allows the RepositoryObjectHandler to work with an Eclipse Workspace.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class WorkspaceAdapter implements RepositoryAdapter {
 
 	/**
 	 * Represents the time to wait before asking the Workspace Session if it's locked.
 	 */
 	private static final long TIME_TO_WAIT_BEFORE_CHECKING_SESSIONDELTA = 5;
 
 	/**
 	 * The save options that have to be used for saving resources of this repository.
 	 */
 	private static Map<String, Object> saveOptions;
 
 	/**
 	 * The load options that have to be used for loading this repository.
 	 */
 	private static Map<String, Object> loadOptions;
 
 	/**
 	 * The Workspace repository associated to this adapter.
 	 */
 	private WorkspaceRepository repository;
 
 	/**
 	 * Indicates if the current opened context is ReadOnly (false if Read/Write).
 	 */
 	private boolean isReadOnlyContext;
 
 	/**
 	 * Mapping between notificator and listeners.
 	 */
 	private Map<Notificator, Set<WorkspaceTypeListener>> notificatorToListener;
 
 	/**
 	 * The {@link RepositoryStructurer} used to structured the IntentDocument.
 	 */
 	private RepositoryStructurer documentStructurer;
 
 	/**
 	 * Indicates if this adapter should send or not warning to the WorkspaceSession before saving a resource
 	 * (default value : true).
 	 */
 	private boolean sendSessionWarningBeforeSaving;
 
 	/**
 	 * A list of path that should not cause warning sending to the WorkspaceSession before saving a resource
 	 * conform to the given path.
 	 * <p>
 	 * For example, if this list contain '/FOLDER/SUBFOLDER1/', any resource located in this folder or
 	 * sub-folders should be ignored. It can also contains path relative to a single resource, like
 	 * '/FOLDER/SUBFOLDER1/MySingleResource'.
 	 * </p>
 	 */
 	private List<String> resourcesToIgnorePaths;
 
 	/**
 	 * A {@link Predicate} that returns true if the considered resource must not be unloaded when undoing
 	 * changes, false otherwise.
 	 */
 	private Predicate<Resource> unloadableResourcePredicate = new Predicate<Resource>() {
 
 		public boolean apply(Resource input) {
 			// Default implementation considers that all resources should be unloaded when undoing changes
 			return false;
 		}
 	};
 
 	/**
 	 * WorkspaceAdapterconstructor.
 	 * 
 	 * @param repository
 	 *            the Workspace repository associated to this adapter
 	 */
 	public WorkspaceAdapter(WorkspaceRepository repository) {
 		this.repository = repository;
 		this.isReadOnlyContext = false;
 		this.notificatorToListener = new HashMap<Notificator, Set<WorkspaceTypeListener>>();
 		this.resourcesToIgnorePaths = new ArrayList<String>();
 	}
 
 	/**
 	 * Returns the save options that have to be used for saving resources.
 	 * 
 	 * @return the save options that have to be used for saving resources
 	 */
 	public static Map<String, Object> getSaveOptions() {
 		if (saveOptions == null) {
 			saveOptions = new HashMap<String, Object>();
 			// We do not format the document when saving (less human-readable but faster to save and to load)
 			saveOptions.put(XMLResource.OPTION_FORMATTED, false);
 		}
 		return saveOptions;
 	}
 
 	/**
 	 * Returns the load options that have to be used for loading resources.
 	 * 
 	 * @return the load options that have to be used for loading resources
 	 */
 	public static Map<String, Object> getLoadOptions() {
 		if (loadOptions == null) {
 			loadOptions = new HashMap<String, Object>();
 			// We want the resource to be saved only if changes have been detected.
 			// In order to make the system as scalable as possible,
 			// We use a fileBuffer instead of a memory buffer
 			loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, true);
 			// Parser Pool for tweaking performances
 			loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
 		}
 		return loadOptions;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#openSaveContext()
 	 */
 	public Object openSaveContext() {
 		// Nothing to do here except setting the isReadOnluContext value to false
 		isReadOnlyContext = false;
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#openReadOnlyContext()
 	 */
 	public Object openReadOnlyContext() {
 		// Nothing to do here except setting the isReadOnluContext value to true
 		isReadOnlyContext = true;
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#save()
 	 */
 	public void save() throws ReadOnlyException, SaveException {
 		if (isReadOnlyContext) {
 			throw new ReadOnlyException(
 					"Cannot save with a read-only context. The context should have been started with the 'openSaveContext' method.");
 		}
 
 		// Step 1: we use the documentStructurer to structure the resource set
 		if (documentStructurer != null) {
 			documentStructurer.structure(WorkspaceAdapter.this);
 		}
 		final Collection<Resource> resources = Lists.newArrayList(this.repository.getResourceSet()
 				.getResources());
 		SaveException saveException = null;
 		try {
 			for (Resource resource : resources) {
 
 				try {
 					if (!removeDanglingElements(resource) && hasDifferentSerialization(resource)) {
 						try {
 							// We make sure the session isn't still reacting to previous saves
 							while (((WorkspaceSession)this.repository.getOrCreateSession())
 									.isProcessingDelta()) {
 								try {
 									Thread.sleep(TIME_TO_WAIT_BEFORE_CHECKING_SESSIONDELTA);
 								} catch (InterruptedException e) {
 									throw new SaveException(e.getMessage());
 								}
 							}
 
 							// Step 2: we send a warning to the WorkspaceSession if necessary
 							treatSessionWarning(resource);
 
 							// Step 3: save the resource
 
 							if (resource.getContents().isEmpty()) {
 								// if the resource is empty, we delete it
 								resource.delete(getSaveOptions());
 							} else {
 								resource.save(getSaveOptions());
 							}
 						} catch (IOException e) {
 							removeDanglingElements(resource);
 						} catch (RepositoryConnectionException e) {
 							saveException = new SaveException(e.getMessage());
 						}
 					}
 				} catch (RepositoryConnectionException e) {
 					saveException = new SaveException(e.getMessage());
 				} catch (IOException e) {
 					saveException = new SaveException(e.getMessage());
 				}
 
 			}
 
 		} catch (ConcurrentModificationException cme) {
 			// If there were a concurrent modification, we simply retry
 			// FIXME : can we make a better choice ? The causes of this exception don't seem obvious
 			save();
 		}
 		if (saveException != null) {
 			throw saveException;
 		}
 
 	}
 
 	/**
 	 * Returns true if the resource will get a different serialization than the one on the disk.
 	 * 
 	 * @param options
 	 *            save options.
 	 * @return true if the resource will get a different serialization than the one on the disk.
 	 * @throws IOException
 	 *             on error while saving.
 	 */
 	public boolean hasDifferentSerialization(final Resource resourcetoSave) throws IOException {
 		// CHECKSTYLE:OFF : code coming from
 		// ResourceImpl.saveOnlyIfChangedWithFileBuffer
 		resourcetoSave.eSetDeliver(false);
 		final File temporaryFile = File.createTempFile("ResourceSaveHelper", null);
 		boolean equal = true;
 		try {
 			final URI temporaryFileURI = URI.createFileURI(temporaryFile.getPath());
 
 			final URIConverter uriConverter = resourcetoSave.getResourceSet() == null ? new ResourceSetImpl()
 					.getURIConverter() : resourcetoSave.getResourceSet().getURIConverter();
 			final OutputStream temporaryFileOutputStream = uriConverter.createOutputStream(temporaryFileURI);
 			try {
 				resourcetoSave.save(temporaryFileOutputStream, getSaveOptions());
 			} finally {
 				temporaryFileOutputStream.close();
 			}
 
 			InputStream oldContents = null;
 			try {
 				oldContents = uriConverter.createInputStream(resourcetoSave.getURI());
 			} catch (final IOException exception) {
 				equal = false;
 			}
 			final byte[] newContentBuffer = new byte[4000];
 			if (oldContents != null) {
 				try {
 					final InputStream newContents = uriConverter.createInputStream(temporaryFileURI);
 					try {
 						final byte[] oldContentBuffer = new byte[4000];
 						LOOP: for (int oldLength = oldContents.read(oldContentBuffer), newLength = newContents
 								.read(newContentBuffer); (equal = oldLength == newLength) && oldLength > 0; oldLength = oldContents
 								.read(oldContentBuffer), newLength = newContents.read(newContentBuffer)) {
 							for (int i = 0; i < oldLength; ++i) {
 								if (oldContentBuffer[i] != newContentBuffer[i]) {
 									equal = false;
 									break LOOP;
 								}
 							}
 						}
 					} finally {
 						newContents.close();
 					}
 				} finally {
 					oldContents.close();
 				}
 			}
 		} finally {
 			temporaryFile.delete();
 			resourcetoSave.eSetDeliver(true);
 		}
 		// CHECKSTYLE:ON
 
 		return !equal;
 	}
 
 	/**
 	 * Determine if a warning should be sent to the WorkspaceSession before saving the given resource and send
 	 * this warning.
 	 * <p>
 	 * If a warning is sent, the WorkspaceSession will ignore the next modification made on the given
 	 * resource.
 	 * </p>
 	 * 
 	 * @param resource
 	 *            the resource being saved
 	 * @throws RepositoryConnectionException
 	 *             if a connection to the repository cannot be made
 	 */
 	private void treatSessionWarning(Resource resource) throws RepositoryConnectionException {
 		// If this adapter must warn the session about any saved resource
 		if (sendSessionWarningBeforeSaving) {
 			// We warn the session
 			((WorkspaceSession)this.repository.getOrCreateSession()).addSavedResource(resource);
 		} else {
 			// If the given resource must be ignored (i.e is include in any of the
 			// resourcesToIgnorePaths)
 			if (isInResourcesToIgnorePath(resource)) {
 				// We warn the session
 				((WorkspaceSession)this.repository.getOrCreateSession()).addSavedResource(resource);
 			}
 		}
 	}
 
 	/**
 	 * Indicates if the given resource is conform to any declared resourceToIgnore path.
 	 * 
 	 * @param resource
 	 *            the resource to determine if it's conform to any declared resourceToIgnore path
 	 * @return true if the given resource is conform to any declared resourceToIgnore path , false otherwise
 	 */
 	public boolean isInResourcesToIgnorePath(Resource resource) {
 		boolean isInResourceToIgnorePath = false;
 		Iterator<String> iterator = this.resourcesToIgnorePaths.iterator();
 		while (iterator.hasNext() && !isInResourceToIgnorePath) {
 			String resourceToIgnorePath = iterator.next();
 			isInResourceToIgnorePath = this.repository.isIncludedInPath(resourceToIgnorePath, resource);
 		}
 		return isInResourceToIgnorePath;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#undo()
 	 */
 	public void undo() throws ReadOnlyException {
 		// TODO accurate undo strategy
 		CommandStack commandStack = repository.getEditingDomain().getCommandStack();
 		if (commandStack != null) {
 			commandStack.undo();
 		} else {
 			// TODO ?
 		}
 	}
 
 	/**
 	 * Sets a predicate that will be used to determine which resource must not be unloaded when undoing
 	 * changes.
 	 * 
 	 * @param unloadableResourcePredicate
 	 *            a {@link Predicate} that returns true if the considered resource must not be unloaded when
 	 *            undoing changes, false otherwise
 	 */
 	public void setUnloadableResourcePredicate(Predicate<Resource> unloadableResourcePredicate) {
 		this.unloadableResourcePredicate = unloadableResourcePredicate;
 	}
 
 	/**
 	 * Indicates whether the given Resource should not be unloaded when undoing any operation.
 	 * 
 	 * @param resource
 	 *            the resource to test
 	 * @return false if the Resource should be unloaded when undoing changes, true otherwise
 	 */
 	private boolean isUnloadableResource(Resource resource) {
 		return unloadableResourcePredicate.apply(resource);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#closeContext()
 	 */
 	public void closeContext() {
 		isReadOnlyContext = false;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#attachSessionListenerForTypes(org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator,
 	 *      java.util.Set)
 	 */
 	public void attachSessionListenerForTypes(Notificator typeNotificator,
 			Set<EStructuralFeature> listenedTypes) {
 		try {
 
 			WorkspaceTypeListener typeListener = new WorkspaceTypeListener(typeNotificator, listenedTypes);
 			if (this.notificatorToListener.get(typeNotificator) == null) {
 				this.notificatorToListener.put(typeNotificator, new LinkedHashSet<WorkspaceTypeListener>());
 			}
 			this.notificatorToListener.get(typeNotificator).add(typeListener);
 			((WorkspaceSession)this.repository.getOrCreateSession()).addListener(typeListener);
 		} catch (RepositoryConnectionException e) {
 			// TODO handle properly such a repository connection exception
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#detachSessionListenerForTypes(org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator)
 	 */
 	public void detachSessionListenerForTypes(Notificator typeNotificator) {
 		try {
 			for (WorkspaceTypeListener listenerToRemove : this.notificatorToListener.get(typeNotificator)) {
 				((WorkspaceSession)this.repository.getOrCreateSession()).removeListener(listenerToRemove);
 			}
 
 			this.notificatorToListener.remove(typeNotificator);
 		} catch (RepositoryConnectionException e) {
 			// TODO handle properly such a repository connection exception
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#allowChangeSubscriptionPolicy()
 	 */
 	public void allowChangeSubscriptionPolicy() {
 		// Nothing to do here
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#getContext()
 	 */
 	public Object getContext() {
 		// No context to return as no context was created
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#getResource(java.lang.String)
 	 */
 	public Resource getResource(String repositoryRelativePath) {
 		return getResource(repositoryRelativePath, true);
 	}
 
 	/**
 	 * Returns the resource located at the given path.
 	 * 
 	 * @param repositoryRelativePath
 	 *            path of the searched resource (from the root of the repository)
 	 * @param loadResourceOnDemand
 	 *            indicates if the resource should be loaded on demand or not
 	 * @return the resource located at the given path
 	 */
 	public Resource getResource(String repositoryRelativePath, boolean loadResourceOnDemand) {
 		// We calculate the Repository URI corresponding to the given path
 		URI uri = this.repository.getURIMatchingPath(repositoryRelativePath);
 		final Resource resource = this.repository.getResourceSet().getResource(uri, loadResourceOnDemand);
 		return resource;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#getOrCreateResource(java.lang.String)
 	 */
 	public Resource getOrCreateResource(String path) throws ReadOnlyException {
 		if (isReadOnlyContext) {
 			throw new ReadOnlyException(
 					"Cannot create a resource with a read-only context. The context should have been started with the 'openSaveContext' method.");
 		}
 
 		// We calculate the Repository URI corresponding to the given path
 		URI uri = this.repository.getURIMatchingPath(path);
 
 		// We first try to get the resource
 		Resource returnedResource = this.repository.getResourceSet().getResource(uri, false);
 		if (returnedResource == null) {
 			// If it doesn't exist, we create it
 			returnedResource = this.repository.getResourceSet().createResource(uri);
 		} else {
 			if (!returnedResource.isLoaded()) {
 				try {
 					returnedResource.load(getLoadOptions());
 				} catch (IOException e) {
 					returnedResource = null;
 				}
 			}
 		}
 		return returnedResource;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#getElementWithID(java.lang.Object)
 	 */
 	public EObject getElementWithID(Object uri) {
 		if (uri instanceof URI) {
 			EObject eObject = this.repository.getResourceSet().getEObject((URI)uri, true);
 			return eObject;
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#getIDFromElement(org.eclipse.emf.ecore.EObject)
 	 */
 	public Object getIDFromElement(EObject element) {
		URI uri = null;
		if (element != null) {
			uri = EcoreUtil.getURI(element);

			// if the URI starts with "#", it means that the resource is no longer part of the resource set.
			// we return null to indicate the the given element is now invalid
			if (uri.toString().startsWith("#")) {
				return null;
			}
 		}
 		return uri;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#attachRepositoryStructurer(org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer)
 	 */
 	public void attachRepositoryStructurer(RepositoryStructurer structurer) {
 		if (!(structurer instanceof RepositoryStructurer)) {
 			throw new IllegalArgumentException("Cannot attach " + structurer.getClass().getName()
 					+ " to this adapter : should be " + RepositoryStructurer.class.getName());
 		}
 		documentStructurer = structurer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#setSendSessionWarningBeforeSaving(boolean)
 	 */
 	public void setSendSessionWarningBeforeSaving(boolean notifySessionBeforeSaving) {
 		this.sendSessionWarningBeforeSaving = notifySessionBeforeSaving;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#setSendSessionWarningBeforeSaving(java.util.Collection)
 	 */
 	public void setSendSessionWarningBeforeSaving(Collection<String> resourcesToIgnorePathList) {
 		this.sendSessionWarningBeforeSaving = false;
 		this.resourcesToIgnorePaths.clear();
 		this.resourcesToIgnorePaths.addAll(resourcesToIgnorePathList);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#reload(org.eclipse.emf.ecore.EObject)
 	 */
 	public EObject reload(EObject elementToReload) {
 		EObject resolve = elementToReload;
 		if (elementToReload.eIsProxy()) {
 			resolve = EcoreUtil.resolve(elementToReload, this.repository.getResourceSet());
 		}
 		return resolve;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#execute(org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand)
 	 */
 	public void execute(final IntentCommand command) {
 		final TransactionalEditingDomain editingDomain = repository.getEditingDomain();
 		RecordingCommand recordingCommand = new RecordingCommand(editingDomain) {
 			@Override
 			protected void doExecute() {
 				command.execute();
 			}
 		};
 		// first we check that the repository has not been disposed
 		final CommandStack commandStack = editingDomain.getCommandStack();
 		if (commandStack != null) {
 			try {
 				commandStack.execute(recordingCommand);
 			} catch (NullPointerException e) {
 				// can happen when TED is disposed
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter#getResourcePath(org.eclipse.emf.common.util.URI)
 	 */
 	public String getResourcePath(URI resourceURI) {
 		return resourceURI.toString().replace("." + resourceURI.fileExtension(), "")
 				.replace("platform:/resource", "")
 				.replace(this.repository.getWorkspaceConfig().getRepositoryAbsolutePath(), "");
 	}
 
 	/**
 	 * Removes the dangling references contained in the given resource and saves the resource (without
 	 * notifying other clients).
 	 * 
 	 * @param resource
 	 *            the resource to clean
 	 * @return true if the given resource contained dangling references (and hence was cleaned and saved),
 	 *         false otherwise
 	 * @throws SaveException
 	 *             if resource cannot be save
 	 * @throws RepositoryConnectionException
 	 *             if repository cannot be accessed
 	 */
 	private boolean removeDanglingElements(Resource resource) throws SaveException,
 			RepositoryConnectionException {
 
 		// Step 1: detect dangling references
 		Collection<EObject> objectsToRemove = Sets.newLinkedHashSet();
 		Iterator<EObject> iterator = resource.getContents().iterator();
 		while (iterator.hasNext()) {
 			EObject root = iterator.next();
 			if (root.eContainer() != null && root.eContainer().eResource() == null) {
 				objectsToRemove.add(root);
 			}
 		}
 
 		try {
 			if (!objectsToRemove.isEmpty()) {
 				// Step 2: save or delete the resource if empty
 				treatSessionWarning(resource);
 				if (resource.getContents().size() <= objectsToRemove.size()) {
 					resource.delete(getSaveOptions());
 				} else {
 					for (EObject objectToRemove : objectsToRemove) {
 						EcoreUtil.remove(objectToRemove);
 					}
 					for (EObject nonDanglingElement : resource.getContents()) {
 						new RemoveDanglingReferences(this.repository.getEditingDomain(), nonDanglingElement)
 								.execute();
 					}
 					resource.save(getSaveOptions());
 				}
 				return true;
 			} else {
 				for (EObject nonDanglingElement : resource.getContents()) {
 					new RemoveDanglingReferences(this.repository.getEditingDomain(), nonDanglingElement)
 							.execute();
 				}
 			}
 		} catch (IOException ioE) {
 			throw new SaveException(ioE.getMessage());
 		}
 		return false;
 	}
 }
