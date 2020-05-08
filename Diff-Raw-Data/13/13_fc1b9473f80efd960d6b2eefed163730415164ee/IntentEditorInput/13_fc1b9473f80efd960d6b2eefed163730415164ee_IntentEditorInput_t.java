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
 package org.eclipse.mylyn.docs.intent.client.ui.editor;
 
 import java.lang.reflect.Constructor;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.ui.CommonUIPlugin;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
 import org.eclipse.mylyn.docs.intent.collab.common.IntentRepositoryManager;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnit;
 import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
 import org.eclipse.mylyn.docs.intent.core.query.DescriptionUnitHelper;
 import org.eclipse.mylyn.docs.intent.core.query.StructuredElementHelper;
 import org.eclipse.ui.IMemento;
 import org.osgi.framework.Bundle;
 
 /**
  * EditorInput for Intent elements.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentEditorInput extends URIEditorInput {
 
 	/**
 	 * Maximum size for an input title.
 	 */
 	private static final int MAX_TITLE_SIZE = 25;
 
 	private static final String URI_FRAGMENT_TAG = "uri_fragment";
 
 	/**
 	 * The element that this EditorInput will handle.
 	 */
 	private EObject element;
 
 	/**
 	 * Name of this element to print.
 	 */
 	private String elementTitle;
 
 	/**
 	 * The repository adapter used to load elements.
 	 */
 	private RepositoryAdapter repositoryAdapter;
 
 	/**
 	 * IntentEditorInput constructor.
 	 * 
 	 * @param elementToOpen
 	 *            the element that this EditorInput will handle
 	 * @param repositoryAdapter
 	 *            the repository adapter to use
 	 */
 	public IntentEditorInput(EObject elementToOpen, RepositoryAdapter repositoryAdapter) {
 		super(elementToOpen.eResource().getURI());
 		this.element = elementToOpen;
 		this.elementTitle = getTitleFromElement(elementToOpen);
 		this.repositoryAdapter = repositoryAdapter;
 	}
 
 	/**
 	 * Creates a new {@link IntentEditorInput} from a memento.
 	 * 
 	 * @param memento
 	 *            the memento
 	 */
 	public IntentEditorInput(IMemento memento) {
 		super(memento);
 	}
 
 	/**
 	 * Returns the element associated to this editorInput.
 	 * 
 	 * @return the element associated to this editorInput
 	 */
 	public EObject getIntentElement() {
 		return element;
 	}
 
 	/**
 	 * Returns the title of the editor according to the given element.
 	 * 
 	 * @param newElement
 	 *            the element to compute the title from
 	 * @return the new title of the editor
 	 */
 	public String getTitleFromElement(EObject newElement) {
 		String newTitle = "";
 		if (newElement instanceof ModelingUnit) {
 			newTitle = ((ModelingUnit)newElement).getUnitName();
 			if ((newTitle == null) || (newTitle.length() < 1)) {
 				newTitle = "Untitled ModelingUnit";
 			}
 		}
 		if (newElement instanceof DescriptionUnit) {
 			newTitle = DescriptionUnitHelper.getDescriptionUnitTitle((DescriptionUnit)newElement,
 					MAX_TITLE_SIZE);
 		}
 		if (newElement instanceof IntentStructuredElement) {
 			newTitle = StructuredElementHelper.getTitle((IntentStructuredElement)newElement);
 			if ((newTitle == null) || (newTitle.length() < 1)) {
 				newTitle = newElement.eClass().getName();
 			}
 		}
 		if (newTitle.length() > MAX_TITLE_SIZE) {
 			newTitle = newTitle.substring(0, MAX_TITLE_SIZE);
 		}
 		return newTitle;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.IEditorInput#getName()
 	 */
 	public String getName() {
 		return elementTitle;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
 	 */
 	public String getToolTipText() {
 		return elementTitle;
 	}
 
 	/**
 	 * Sets the name to return.
 	 * 
 	 * @param newName
 	 *            the newName to return
 	 */
 	public void setName(String newName) {
 		elementTitle = newName;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.ui.URIEditorInput#loadState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	protected void loadState(IMemento memento) {
 		super.loadState(memento);
 		Resource resource = getResource();
 		element = resource.getEObject(memento.getString(URI_FRAGMENT_TAG));
 		elementTitle = getTitleFromElement(element);
 	}
 
 	/**
 	 * Load the resource from the repository. Load the repository if needed.
 	 * 
 	 * @return the loaded resource
 	 */
 	private Resource getResource() {
 		if (repositoryAdapter == null) {
 			repositoryAdapter = getRepository().createRepositoryAdapter();
 			repositoryAdapter.openSaveContext();
 		}
 		return repositoryAdapter.getResource(getPath(getURI()));
 	}
 
 	/**
 	 * Fetches the repository from the {@link IntentRepositoryManager}.
 	 * 
 	 * @return the repository
 	 */
 	public Repository getRepository() {
 		try {
 			return IntentRepositoryManager.INSTANCE.getRepository(getProjectName(getURI()));
 		} catch (RepositoryConnectionException e) {
 			IntentUiLogger.logError(e);
 		} catch (CoreException e) {
 			IntentUiLogger.logError(e);
 		}
 		return null;
 	}
 
 	public RepositoryAdapter getRepositoryAdapter() {
 		return repositoryAdapter;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.ui.URIEditorInput#saveState(org.eclipse.ui.IMemento)
 	 */
 	@Override
 	public void saveState(IMemento memento) {
 		super.saveState(memento);
 		memento.putString(URI_FRAGMENT_TAG, element.eResource().getURIFragment(element));
 		memento.putString(CLASS_TAG, getClass().getName());
 	}
 
 	/**
 	 * Retrieves the project name from an URI. TODO merge, there are similar methods in intent.
 	 * 
 	 * @param uri
 	 *            the uri
 	 * @return the project name
 	 */
 	private static String getProjectName(URI uri) {
 		String projectName = null;
 		if (uri.isPlatformResource()) {
 			projectName = uri.toString().replaceFirst("platform:/resource/", "");
 			projectName = projectName.split("/")[0];
 		} else {
 			// should not happen
 		}
 		return projectName;
 	}
 
 	/**
 	 * Retrieves the resource path from an URI. TODO merge, there are similar methods in intent.
 	 * 
 	 * @param uri
 	 *            the uri
 	 * @return the resource path
 	 */
 	private static String getPath(URI uri) {
 		String path = null;
 		if (uri.isPlatformResource()) {
 			path = uri.trimFileExtension().toString().replaceFirst("platform:/resource/", "");
 			path = path.substring(path.indexOf("INTENT") - 1);
 		} else {
 			// should not happen
 		}
 		return path;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.ui.URIEditorInput#getBundleSymbolicName()
 	 */
 	@Override
 	protected String getBundleSymbolicName() {
 		return IntentEditorActivator.getDefault().getBundle().getSymbolicName();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.ui.URIEditorInput#getFactoryId()
 	 */
 	@Override
 	public String getFactoryId() {
 		return IntentEditorInputFactory.ID;
 	}
 
 	/**
 	 * Creates an {@link IntentEditorInput} from a memento.
 	 * 
 	 * @param memento
 	 *            the memento
 	 * @return the {@link IntentEditorInput}
 	 */
 	static IntentEditorInput create(IMemento memento) {
 		String bundleSymbolicName = memento.getString(BUNDLE_TAG);
 		String className = memento.getString(CLASS_TAG);
 		try {
 			Bundle bundle = Platform.getBundle(bundleSymbolicName);
 			Class<?> theClass = bundle.loadClass(className);
 			Constructor<?> constructor = theClass.getConstructor(IMemento.class);
 			return (IntentEditorInput)constructor.newInstance(memento);
 			// CHECKSTYLE:OFF
 		} catch (Exception exception) {
 			CommonUIPlugin.INSTANCE.log(exception);
 			return new IntentEditorInput(memento);
 		}
 		// CHECKSTYLE:ON
 	}
 }
