 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 346932 - Navigation history broken
  *    Bug 336488 - DiagramEditor API
  *    mwenz - Bug 378342 - Cannot store more than a diagram per file
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPersistableElement;
 
 /**
  * The editor input object for {@link DiagramEditor}s. Wraps the {@link URI} of
  * a {@link Diagram} and an ID of a diagram type provider for displaying it with
  * a Graphiti diagram editor.<br>
  * 
  * @see {@link IEditorInput}
  * @see {@link IPersistableElement}
  * @see {@link DiagramEditorInputFactory}
  * @see {@link DiagramEditor}
  */
 public class DiagramEditorInput implements IEditorInput, IPersistableElement, IDiagramEditorInput {
 
 	/**
 	 * The memento key for the stored {@link URI} string
 	 */
 	public static final String KEY_URI = "org.eclipse.graphiti.uri"; //$NON-NLS-1$
 
 	/**
 	 * The memento key for the ID of the diagram type provider.
 	 */
 	public static String KEY_PROVIDER_ID = "org.eclipse.graphiti.providerId"; //$NON-NLS-1$
 
 	/**
 	 * The stored {@link URI} string
 	 */
 	private URI uri;
 
 	/**
 	 * The ID of the diagram type provider.
 	 */
 	private String providerId;
 
 	/**
 	 * The cached input name (e.g. for displaying the name in the navigation
 	 * history without having to instantiate the {@link EObject})
 	 * 
 	 * @see #getLiveName()
 	 */
 	private String name; 
 
 	/**
 	 * The cached input tooltip
 	 * 
 	 * @see #getLiveToolTipText()
 	 */
 	private String tooltip;
 
 
 	/**
 	 * Creates a new {@link DiagramEditorInput} out of a {@link URI} string and
 	 * a Graphiti diagram type provider ID. For resolving the {@link URI} to an
 	 * {@link EObject} the {@link ResourceSet} that will be created when a
 	 * diagram editor starts is taken. This input object will not resolve the
 	 * diagram.<br>
 	 * A diagram type provider ID is held in this class.
 	 * 
 	 * @param diagramUri
 	 *            A {@link URI} that denotes the input's {@link EObject}. This
 	 *            can either be a URI of a Graphiti diagram or the URI of an EMF
 	 *            resource storing a Graphiti diagram. In the latter case the
 	 *            given URI will b e trimmed to point to the first element in
 	 *            the resource; make sure that this lemenet is a Graphiti
 	 *            diagram, otherwise an exception will be thrown when the
 	 *            diagram editor opens. No check on this is done inside the
 	 *            input object itself!
 	 * @param providerId
 	 *            A {@link String} which holds the diagram type id. When it is
 	 *            null, it is set later in
 	 *            {@link DiagramEditor#setInput(IEditorInput)}
 	 * @throws IllegalArgumentException
 	 *             if <code>uriString</code> parameter is null <br>
 	 * 
 	 * @see URI
 	 * @since 0.9
 	 */
 	public DiagramEditorInput(URI diagramUri, String providerId) {
 
 		Assert.isNotNull(diagramUri, "diagram must not be null"); //$NON-NLS-1$
 		// Normalize URI for later compare operations
 		this.uri = normalizeUriString(diagramUri);
 		setProviderId(providerId);
 	}
 
 	private URI normalizeUriString(URI diagramUri) {
 		URI normalizedURI = new ResourceSetImpl().getURIConverter().normalize(diagramUri);
 		// Do the trimming only in case no explicit fragment (no specific
 		// diagram inside the resource) was provided. In case a fragment was
 		// provided, use it, otherwise simply take the first element in the
 		// resource (#0)
		if (!normalizedURI.hasFragment()) {
 			URI trimFragment = normalizedURI.trimFragment();
 			normalizedURI = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(trimFragment);
 		}
 		return normalizedURI;
 	}
 
 	/**
 	 * Creates a new {@link DiagramEditorInput} for the given {@link Diagram}
 	 * and the given diagram type provider ID.
 	 * 
 	 * @param diagram
 	 *            A {@link Diagram}
 	 * @param providerId
 	 *            A {@link String} which holds the diagram type provider id.
 	 * @return A {@link DiagramEditorInput} editor input
 	 * @since 0.9
 	 */
 	public static DiagramEditorInput createEditorInput(Diagram diagram, String providerId) {
 		final Resource resource = diagram.eResource();
 		if (resource == null) {
 			throw new IllegalArgumentException("Diagram must be contained within a resource");
 		}
 		URI diagramUri = EcoreUtil.getURI(diagram);
 		DiagramEditorInput diagramEditorInput = new DiagramEditorInput(diagramUri, providerId);
 		return diagramEditorInput;
 	}
 
 
 	/**
 	 * Returns the diagram type provider id.
 	 * 
 	 * @return The providerId.
 	 */
 	public String getProviderId() {
 		return this.providerId;
 	}
 
 	/**
 	 * Sets the diagram type provider id.
 	 * 
 	 * @param providerId
 	 *            The providerId to set.
 	 */
 	public void setProviderId(String providerId) {
 		this.providerId = providerId;
 	}
 
 	/**
 	 * Returns the factory ID for creating {@link DiagramEditorInput}s from
 	 * mementos.
 	 * 
 	 * @return The ID of the associated factory
 	 */
 	public String getFactoryId() {
 		return DiagramEditorInputFactory.class.getName();
 	}
 
 	/**
 	 * @return Simply returns <code>null</code>.
 	 */
 	public ImageDescriptor getImageDescriptor() {
 		return null;
 	}
 
 	/**
 	 * @return The cached name or the input's {@link URI} string
 	 * @see #getLiveName()
 	 */
 	public String getName() {
 		if (this.name != null) {
 			return this.name;
 		}
 		return this.uri.toString();
 	}
 
 	/**
 	 * Checks if a name is set for this instance
 	 * 
 	 * @return <code>true</code> in case a name is set, <code>false</code> in
 	 *         name is <code>null</code>.
 	 */
 	protected boolean hasName() {
 		return this.name != null;
 	}
 
 	/**
 	 * Sets the name for this instance.
 	 * 
 	 * @param name
 	 *            The name to set.
 	 */
 	protected void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return The cached tooltip or the input's {@link URI} string
 	 * @see #getLiveToolTipText()
 	 */
 	public String getToolTipText() {
 		if (this.tooltip != null) {
 			return this.tooltip;
 		}
 		return getName();
 	}
 
 
 	/**
 	 * Adapter method as defined in {@link IAdaptable}, supports adaptation to
 	 * {@link IFile}.
 	 * 
 	 * @param adapter
 	 *            The adapter class to look up
 	 * @return A object castable to the given class, or <code>null</code> if
 	 *         this object does not have an adapter for the given class
 	 */
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
 		if (IFile.class.isAssignableFrom(adapter)) {
 		 	return GraphitiUiInternal.getEmfService().getFile(getUri());
 	    } 
 		return null;
 	}
 	
 
 	/**
 	 * Saves {@link URI} string, object name and provider ID to the given
 	 * {@link IMemento}.
 	 * 
 	 * @param memento
 	 *            The memento to store the information in
 	 */
 	public void saveState(IMemento memento) {
 		// Do not store anything for deleted objects
 		boolean exists = exists();
 		if (!exists) {
 			return;
 		}
 		// Store object name, URI and diagram type provider ID
 		memento.putString(KEY_URI, this.uri.toString());
 		memento.putString(KEY_PROVIDER_ID, this.providerId);
 	}
 
 
 	/**
 	 * @return The {@link URI} string this input and its editors operate on
 	 */
 	public final String getUriString() {
 		return this.uri.toString();
 	}
 
 	/**
 	 * Checks if the diagram this input represents exist.
 	 * <p>
 	 * Note: The editor gets only restored, when <code>true</code> is returned.
 	 * 
 	 * @return <code>true</code> if the input's state denotes a living EMF
 	 *         object <br>
 	 */
 	public boolean exists() {
 		if (uri == null) {
 			return false;
 		}
 		// TODO check if URI points to something
 		return true;
 	}
 
 	/**
 	 * @return this input if it is persistable, otherwise null
 	 */
 	public IPersistableElement getPersistable() {
 		if (uri != null && providerId != null) {
 			return this;
 		}
 		return null;
 	}
 
 
 	/**
 	 * @return the resolved {@link URI} or <code>null</code> in case of failures
 	 * @since 0.9
 	 */
 	public URI getUri() {
 		return this.uri;
 	}
 
 
 	/**
 	 * Checks if this instance of the input represent the same object as the
 	 * given instance.
 	 * 
 	 * @param obj
 	 *            The object to compare this instance with.
 	 * @return <code>true</code> if the represented objects are the same
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		DiagramEditorInput other = (DiagramEditorInput) obj;
 		if (uri == null) {
 			if (other.uri != null) {
 				return false;
 			}
 		} else if (!uri.equals(other.uri)) {
 			return false;
 		}
 		if (providerId == null) {
 			if (other.providerId != null) {
 				return false;
 			}
 		} else if (!providerId.equals(other.providerId)) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
 		result = prime * result + ((providerId == null) ? 0 : providerId.hashCode());
 		return result;
 	}
 
 	/**
 	 * Used for logging only!
 	 */
 	@Override
 	public String toString() {
 		final String s = super.toString() + " uri: " + this.uri; //$NON-NLS-1$
 		return s;
 	}
 	
 	/**
 	 * @since 0.9
 	 */
 	public void updateUri(URI diagramFileUri) {
 		URI uri = GraphitiUiInternal.getEmfService().mapDiagramFileUriToDiagramUri(diagramFileUri);
 		URI normalizedUri = normalizeUriString(uri);
 		this.uri = normalizedUri;
 	}
 }
