 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    mwenz - Bug 329523 - Add notification of DiagramTypeProvider after saving a diagram
 *    mwenz - Bug 320635 - Could not open an existing diagram
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.services.impl;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.transaction.Transaction;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.impl.TransactionalEditingDomainImpl;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.internal.services.IEmfService;
 import org.eclipse.graphiti.ui.internal.util.ModelElementNameComparator;
 import org.eclipse.jface.viewers.IStructuredSelection;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class EmfService implements IEmfService {
 
 	private final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
 	private final String ATTRIBUTE_OBJ_NAME = "name";//$NON-NLS-1$
 	private final String ATTRIBUTE_OBJ_ID = "id";//$NON-NLS-1$
 
 	@Override
 	public String getObjectName(final Object obj) {
 		if (obj == null) {
 			throw new IllegalArgumentException("Obj must not be null"); //$NON-NLS-1$
 		}
 
 		if (obj instanceof ENamedElement) {
 			final ENamedElement elem = (ENamedElement) obj;
 			final String name = elem.getName();
 			if (name != null) {
 				return name;
 			}
 		}
 		if (obj instanceof EObject) {
 			final EObject refObject = (EObject) obj;
 			final Entry<EAttribute, String> attr = getObjectNameAttribute(refObject);
 			if (attr != null) {
 				final String value = attr.getValue();
 				if (value != null) {
 					return value;
 				}
 			}
 		}
 
 		return obj.toString();
 	}
 
 	@Override
 	public EObject getEObject(Object object) {
 		EObject eObject = null;
 		if (object != null && object instanceof EObject) {
 			eObject = (EObject) object;
 			return eObject;
 		}
 		// unwrap a structured selection
 		if (object instanceof IStructuredSelection) {
 			if (((IStructuredSelection) object).isEmpty()) {
 				return null;
 			}
 			final Object element = ((IStructuredSelection) object).getFirstElement();
 			return getEObject(element);
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the EMF attribute that contains a human readable name of a given
 	 * object as its value. The method tries to access a modeled attribute
 	 * "name" or "id" in this order. If no attribute with this name is modeled,
 	 * the first attribute whose value is a String is returned, otherwise
 	 * <code>null</code> .
 	 * 
 	 * @param eObject
 	 *            the object to get a name for
 	 * @return the attribute (currently "name", id") and the readable name
 	 * 
 	 * @see #getObjectName(Object)
 	 */
 	private Entry<EAttribute, String> getObjectNameAttribute(final EObject eObject) {
 		final EClass metaObject = eObject.eClass();
 		final EList<EAttribute> attrs = metaObject.getEAllAttributes();
 		if (attrs != null) {
 			for (final EAttribute attr : attrs) {
 				final Object value = eObject.eGet(attr);
 				// "name"
 				if (ATTRIBUTE_OBJ_NAME.equalsIgnoreCase(attr.getName())) {
 					if (value != null) {
 						final String v = String.valueOf(value);
 						return Collections.singletonMap(attr, v).entrySet().iterator().next();
 					}
 				}
 				// "id"
 				if (ATTRIBUTE_OBJ_ID.equalsIgnoreCase(attr.getName())) {
 					if (value != null) {
 						final String v = String.valueOf(value);
 						return Collections.singletonMap(attr, v).entrySet().iterator().next();
 					}
 				}
 				// string value
 				if (value instanceof String) {
 					return Collections.singletonMap(attr, (String) value).entrySet().iterator().next();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public IFile getFile(EObject object) {
 		IFile result = null;
 		final Resource resource = object.eResource();
 		if (resource != null) {
 			final ResourceSet resourceSet = resource.getResourceSet();
 			if (resourceSet != null) {
 				result = getFile(resource.getURI(), resourceSet);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public IFile getFile(URI uri, TransactionalEditingDomain editingDomain) {
 		return getFile(uri, editingDomain.getResourceSet());
 	}
 
 	@Override
 	public IFile getFile(URI uri, ResourceSet resourceSet) {
 		if (uri == null) {
 			return null;
 		}
 
 		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 
 		// File URIs
 		final String filePath = getWorkspaceFilePath(uri.trimFragment());
 		if (filePath == null) {
 			final IPath location = Path.fromOSString(uri.toString());
 			final IFile file = workspaceRoot.getFileForLocation(location);
 			if (file != null) {
 				return file;
 			}
 			return null;
 		}
 
 		// Platform resource URIs
 		else {
 			final IResource workspaceResource = workspaceRoot.findMember(filePath);
 			return (IFile) workspaceResource;
 		}
 	}
 
 	private String getWorkspaceFilePath(URI uri) {
 		if (uri.isPlatform()) {
 			return uri.toPlatformString(true);
 		}
 		return null;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public Set<Resource> save(TransactionalEditingDomain editingDomain) throws WrappedException {
 		return save(editingDomain, Collections.EMPTY_MAP);
 	}
 
 	@Override
 	public Set<Resource> save(final TransactionalEditingDomain editingDomain, final Map<Resource, Map<?, ?>> options) {
 
 		final Map<URI, Throwable> failedSaves = new HashMap<URI, Throwable>();
 		final Set<Resource> savedResources = new HashSet<Resource>();
 		final IWorkspaceRunnable wsRunnable = new IWorkspaceRunnable() {
 			@Override
 			public void run(final IProgressMonitor monitor) throws CoreException {
 
 				final Runnable runnable = new Runnable() {
 
 					@Override
 					public void run() {
 						Transaction parentTx;
 						if (editingDomain != null
 								&& (parentTx = ((TransactionalEditingDomainImpl) editingDomain).getActiveTransaction()) != null) {
 							do {
 								if (!parentTx.isReadOnly()) {
 									throw new IllegalStateException(
 											"saveInWorkspaceRunnable() called from within a command (likely to produce deadlock)"); //$NON-NLS-1$
 								}
 							} while ((parentTx = ((TransactionalEditingDomainImpl) editingDomain).getActiveTransaction().getParent()) != null);
 						}
 
 						final EList<Resource> resources = editingDomain.getResourceSet().getResources();
 						// Copy list to an array to prevent ConcurrentModificationExceptions
 						// during the saving of the dirty resources
 						Resource[] resourcesArray = new Resource[resources.size()];
 						resourcesArray = resources.toArray(resourcesArray);
 						for (int i = 0; i < resourcesArray.length; i++) {
 							// In case resource modification tracking is switched on, 
 							// we can check if a resource has been modified, so that we only need to save
 							// really changed resources; otherwise we need to save all resources in the set
 							final Resource resource = resourcesArray[i];
 							if (!resource.isTrackingModification() || resource.isModified()) {
 								try {
 									resource.save(options.get(resource));
 									savedResources.add(resource);
 								} catch (final Throwable t) {
 									failedSaves.put(resource.getURI(), t);
 								}
 							}
 						}
 					}
 				};
 
 				try {
 					editingDomain.runExclusive(runnable);
 				} catch (final InterruptedException e) {
 					throw new RuntimeException(e);
 				}
 				// For the time being, we clear the command stack after saving.
 				// In a later sprint we might try to implement undo/redo
 				// across save calls (not that easy to handle the ResourceImpl.isModified flag, which is also
 				// set by save() outside any command).
 				editingDomain.getCommandStack().flush();
 			}
 		};
 		try {
 			ResourcesPlugin.getWorkspace().run(wsRunnable, null);
 			if (!failedSaves.isEmpty()) {
 				throw new WrappedException(createMessage(failedSaves), new RuntimeException());
 			}
 		} catch (final CoreException e) {
 			final Throwable cause = e.getStatus().getException();
 			if (cause instanceof RuntimeException) {
 				throw (RuntimeException) cause;
 			}
 			throw new RuntimeException(e);
 		}
 
 		return savedResources;
 	}
 
 	private String createMessage(Map<URI, Throwable> failedSaves) {
 		final StringBuilder buf = new StringBuilder("The following resources could not be saved:"); //$NON-NLS-1$
 		for (final Entry<URI, Throwable> entry : failedSaves.entrySet()) {
 			buf.append("\nURI: ").append(entry.getKey().toString()).append(", cause: \n").append(getExceptionAsString(entry.getValue())); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return buf.toString();
 	}
 
 	private String getExceptionAsString(Throwable t) {
 		final StringWriter stringWriter = new StringWriter();
 		final PrintWriter printWriter = new PrintWriter(stringWriter);
 		t.printStackTrace(printWriter);
 		final String result = stringWriter.toString();
 		try {
 			stringWriter.close();
 		} catch (final IOException e) {
 			// $JL-EXC$ ignore
 		}
 		printWriter.close();
 		return result;
 	}
 
 	@Override
 	public StringBuilder toString(final EObject o, final StringBuilder result) {
 		final EClass metaObject = o.eClass();
 		// type
 		result.append(metaObject.getName());
 		result.append(" \""); //$NON-NLS-1$
 		// human-readable name
 		result.append(getObjectName(o));
 		result.append("\": "); //$NON-NLS-1$
 		// URI
 		result.append(EcoreUtil.getURI(o));
 		result.append(LINE_SEP);
 		// all attributes with values (sorted by name)
 		if (o instanceof EObject) {
 			final Map<EAttribute, Object> atts = new TreeMap<EAttribute, Object>(ModelElementNameComparator.INSTANCE_IGNORING_CASE);
 			atts.putAll(getAttributesWithValues(o, true));
 			final Set<Entry<EAttribute, Object>> attsWithValues = atts.entrySet();
 			for (final Entry<EAttribute, Object> attr : attsWithValues) {
 				result.append("  "); //$NON-NLS-1$
 				result.append(attr.getKey().getName());
 				result.append("="); //$NON-NLS-1$
 				result.append(String.valueOf(attr.getValue()));
 				result.append(LINE_SEP);
 			}
 		}
 		// class name of Jmi interface
 		result.append("  class="); //$NON-NLS-1$
 		result.append(o.eClass().getInstanceClassName());
 		return result;
 	}
 
 	private Map<EAttribute, Object> getAttributesWithValues(EObject refObject, boolean b) {
 		final EClass metaObject = refObject.eClass();
 		final EList<EAttribute> attrs = metaObject.getEAllAttributes();
 		final HashMap<EAttribute, Object> result = new HashMap<EAttribute, Object>(attrs.size());
 		for (final EAttribute attr : attrs) {
 			final Object value = refObject.eGet(attr);
 			result.put(attr, value);
 		}
 		return result;
 	}
 
 	@Override
 	public Diagram getDiagramFromFile(IFile file, ResourceSet resourceSet) {
 		// Get the URI of the model file.
 		URI resourceURI = getFileURI(file, resourceSet);
 		// Demand load the resource for this file.
 		Resource resource;
 		try {
 			resource = resourceSet.getResource(resourceURI, true);
 			if (resource != null) {
 				// does resource contain a diagram as root object?
 				URI diagramUri = mapDiagramFileUriToDiagramUri(resourceURI);
 				EObject eObject = resource.getEObject(diagramUri.fragment());
 				if (eObject instanceof Diagram)
 					return (Diagram) eObject;
 			}
 		} catch (WrappedException e) {
 		}
 		return null;
 	}
 
 	@Override
 	public URI getFileURI(IFile file, ResourceSet resourceSet) {
 		String pathName = file.getFullPath().toString();
 		URI resourceURI = URI.createPlatformResourceURI(pathName, true);
 		if (resourceSet != null) {
 			resourceURI = resourceSet.getURIConverter().normalize(resourceURI);
 		}
 		return resourceURI;
 	}
 
 	@Override
 	public URI mapDiagramFileUriToDiagramUri(URI diagramFileUri) {
 		return diagramFileUri.appendFragment("/"); //$NON-NLS-1$
 	}
 }
