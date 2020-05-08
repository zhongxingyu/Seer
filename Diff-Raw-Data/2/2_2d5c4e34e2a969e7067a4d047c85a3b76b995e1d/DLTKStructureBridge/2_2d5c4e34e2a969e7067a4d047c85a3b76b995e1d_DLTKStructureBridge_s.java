 /*******************************************************************************
  * Copyright (c) 2004 - 2006 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Apr 7, 2005
  */
 package org.eclipse.mylyn.internal.dltk;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.ProjectFragment;
 import org.eclipse.dltk.internal.ui.scriptview.BuildPathContainer;
 import org.eclipse.dltk.ui.util.ExceptionHandler;
 import org.eclipse.mylyn.commons.core.StatusHandler;
 import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
 import org.eclipse.mylyn.context.core.IInteractionElement;
 import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
 import org.eclipse.mylyn.internal.resources.ui.ResourceStructureBridge;
 import org.eclipse.ui.internal.WorkingSet;
 import org.eclipse.ui.views.markers.internal.ConcreteMarker;
 
 public class DLTKStructureBridge extends AbstractContextStructureBridge
 		implements IExecutableExtension {
 
 	public String contentType = "";
 	public static final String ID_PLUGIN = "org.eclipse.dltk.mylyn.ui";
 	public List providers;
 
 	public DLTKStructureBridge() {
 		providers = new ArrayList();
 
 	}
 
 	public String getContentType() {
 		return contentType;
 	}
 
 	public Object getAdaptedParent(Object object) {
 		if (object instanceof IFile) {
 			IFile file = (IFile) object;
 			return DLTKCore.create(file.getParent());
 		} else {
 			return super.getAdaptedParent(object);
 		}
 	}
 
 	public String getParentHandle(String handle) {
 		IModelElement modelElement = (IModelElement) getObjectForHandle(handle);
 		if (modelElement != null && modelElement.getParent() != null) {
 			return getHandleIdentifier(modelElement.getParent());
 		} else {
 			return null;
 		}
 	}
 
 	public List getChildHandles(String handle) {
 		Object object = getObjectForHandle(handle);
 		if (object instanceof IModelElement) {
 			IModelElement element = (IModelElement) object;
 			if (element instanceof IParent) {
 				IParent parent = (IParent) element;
 				IModelElement[] children;
 				try {
 					children = parent.getChildren();
 					List childHandles = new ArrayList();
 					for (int i = 0; i < children.length; i++) {
 						String childHandle = getHandleIdentifier(children[i]);
 						if (childHandle != null)
 							childHandles.add(childHandle);
 					}
 					AbstractContextStructureBridge parentBridge = ContextCorePlugin
 							.getDefault().getStructureBridge(parentContentType);
 					if (parentBridge != null
 							&& parentBridge instanceof ResourceStructureBridge) {
 						if (element.getElementType() < IModelElement.TYPE) {
 							List resourceChildren = parentBridge
 									.getChildHandles(handle);
 							if (!resourceChildren.isEmpty())
 								childHandles.addAll(resourceChildren);
 						}
 					}
 
 					return childHandles;
 				} catch (ModelException e) {
 					// ignore these, usually indicate no-existent element
 				} catch (Exception e) {
 					MylynStatusHandler.fail(e, "could not get child", false);
 				}
 			}
 		}
 		return new ArrayList();
 	}
 
 	public Object getObjectForHandle(String handle) {
 		try {
 			return DLTKCore.create(handle);
 		} catch (Throwable t) {
 			MylynStatusHandler.log(
 					"Could not create script element for handle: " + handle,
 					this);
 			return null;
 		}
 	}
 
 	/**
 	 * Uses resource-compatible path for projects
 	 */
 
 	public String getHandleIdentifier(Object object) {
 		if (object instanceof IModelElement) {
 			return ((IModelElement) object).getHandleIdentifier();
 		} else {
 			if (object instanceof IAdaptable) {
 				Object adapter = ((IAdaptable) object)
 						.getAdapter(IModelElement.class);
 				if (adapter instanceof IModelElement) {
 					return ((IModelElement) adapter).getHandleIdentifier();
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean canBeLandmark(String handle) {
 		IModelElement element = (IModelElement) getObjectForHandle(handle);
 		if ((element instanceof IMember || element instanceof IType || element instanceof ISourceModule)
 				&& element.exists()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * TODO: figure out if the non IModelElement stuff is needed
 	 */
 
 	public boolean acceptsObject(Object object) {
 		if (object instanceof IResource) {
 			Object adapter = ((IResource) object)
 					.getAdapter(IModelElement.class);
 			return adapter instanceof IModelElement;
 		}
 
 		boolean accepts = object instanceof IModelElement
				|| object instanceof ProjectFragment
 				|| object instanceof BuildPathContainer.RequiredProjectWrapper
 				|| object instanceof IProjectFragment
 				|| object instanceof WorkingSet;
 
 		return accepts;
 	}
 
 	/**
 	 * Uses special rules for classpath containers since these do not have an
 	 * associated interest, i.e. they're not IModelElement(s).
 	 */
 	public boolean canFilter(Object object) {
 		if (object instanceof BuildPathContainer.RequiredProjectWrapper) {
 			return true;
 		}
 
 		else if (object instanceof WorkingSet) {
 			try {
 				WorkingSet workingSet = (WorkingSet) object;
 				IAdaptable[] elements = workingSet.getElements();
 				for (int i = 0; i < elements.length; i++) {
 					IAdaptable adaptable = elements[i];
 					IInteractionElement element = ContextCorePlugin
 							.getContextManager().getElement(
 									getHandleIdentifier(adaptable));
 					if (element.getInterest().isInteresting())
 						return false;
 				}
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean isDocument(String handle) {
 		IModelElement element = (IModelElement) getObjectForHandle(handle);
 		return element instanceof ISourceModule;
 	}
 
 	public String getHandleForOffsetInObject(Object object, int offset) {
 		IMarker marker;
 		if (object instanceof ConcreteMarker) {
 			marker = ((ConcreteMarker) object).getMarker();
 		} else if (object instanceof IMarker) {
 			marker = (IMarker) object;
 		} else {
 			return null;
 		}
 
 		int charStart = marker.getAttribute(IMarker.CHAR_START, 0);
 
 		try {
 			ISourceModule compilationUnit = null;
 			IResource resource = marker.getResource();
 			if (resource instanceof IFile) {
 				IFile file = (IFile) resource;
 				// TODO: get rid of file extension check
 				// String ext = file.getFileExtension();
 				// if (ext.equals(contentType)) {
 				compilationUnit = DLTKCore.createSourceModuleFrom(file);
 				// }
 			}
 			if (compilationUnit != null) {
 				IModelElement javaElement = compilationUnit
 						.getElementAt(charStart);
 				if (javaElement != null) {
 					return javaElement.getHandleIdentifier();
 				} else {
 					return null;
 				}
 			} else {
 				return null;
 			}
 		} catch (ModelException ex) {
 			if (!ex.isDoesNotExist()) {
 				ExceptionHandler.handle(ex, "error",
 						"could not find script element");
 			}
 			return null;
 		} catch (Throwable t) {
 			StatusHandler.log(new Status(IStatus.ERROR,
 					DLTKStructureBridge.ID_PLUGIN,
 					"Could not find element for: " + marker, t));
 			return null;
 		}
 	}
 
 	public String getContentType(String elementHandle) {
 		return getContentType();
 	}
 
 	public List getRelationshipProviders() {
 		return providers;
 	}
 
 	/**
 	 * Some copying from:
 	 * 
 	 * @see org.eclipse.jdt.ui.ProblemsLabelDecorator
 	 */
 	public boolean containsProblem(IInteractionElement node) {
 		try {
 			IModelElement element = (IModelElement) getObjectForHandle(node
 					.getHandleIdentifier());
 			switch (element.getElementType()) {
 			case IModelElement.SCRIPT_PROJECT:
 			case IModelElement.PROJECT_FRAGMENT:
 				return getErrorTicksFromMarkers(element.getResource(),
 						IResource.DEPTH_INFINITE, null);
 			case IModelElement.PACKAGE_DECLARATION:
 			case IModelElement.SOURCE_MODULE:
 			case IModelElement.BINARY_MODULE:
 				return getErrorTicksFromMarkers(element.getResource(),
 						IResource.DEPTH_ONE, null);
 			case IModelElement.TYPE:
 			case IModelElement.METHOD:
 			case IModelElement.FIELD:
 				ISourceModule cu = (ISourceModule) element
 						.getAncestor(IModelElement.SOURCE_MODULE);
 				if (cu != null)
 					return getErrorTicksFromMarkers(element.getResource(),
 							IResource.DEPTH_ONE, null);
 			}
 		} catch (CoreException e) {
 			// ignore
 		}
 		return false;
 	}
 
 	private boolean getErrorTicksFromMarkers(IResource res, int depth,
 			ISourceReference sourceElement) throws CoreException {
 		if (res == null || !res.isAccessible())
 			return false;
 		IMarker[] markers = res.findMarkers(IMarker.PROBLEM, true, depth);
 		if (markers != null) {
 			for (int i = 0; i < markers.length; i++) {
 				IMarker curr = markers[i];
 				if (sourceElement == null) {
 					int priority = curr.getAttribute(IMarker.SEVERITY, -1);
 					if (priority == IMarker.SEVERITY_ERROR) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public String getLabel(Object arg0) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 		contentType = config.getAttribute("extension");
 	}
 }
