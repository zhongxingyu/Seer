 /*******************************************************************************
  * Copyright (c) 2004, 2008 Tasktop Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.dltk.internal.mylyn;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.internal.resources.Marker;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
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
 import org.eclipse.dltk.internal.ui.navigator.ProjectFragmentContainer;
 import org.eclipse.dltk.internal.ui.scriptview.BuildPathContainer;
 import org.eclipse.dltk.ui.util.ExceptionHandler;
 import org.eclipse.mylyn.commons.core.StatusHandler;
 import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
 import org.eclipse.mylyn.context.core.ContextCore;
 import org.eclipse.mylyn.context.core.IInteractionElement;
 import org.eclipse.ui.internal.WorkingSet;
 import org.eclipse.ui.views.markers.internal.ConcreteMarker;
 
 /**
  * @author Mik Kersten
  */
 public class DLTKStructureBridge extends AbstractContextStructureBridge {
 
 	public final static String CONTENT_TYPE = "DLTK"; //$NON-NLS-1$
 
 	@Override
 	public String getContentType() {
 		return CONTENT_TYPE;
 	}
 
 	@Override
 	public Object getAdaptedParent(Object object) {
 		if (object instanceof IFile) {
 			IFile file = (IFile) object;
 			return DLTKCore.create(file.getParent());
 		} else {
 			return super.getAdaptedParent(object);
 		}
 	}
 
 	@Override
 	public String getParentHandle(String handle) {
 		IModelElement javaElement = (IModelElement) getObjectForHandle(handle);
		if (javaElement != null && javaElement.getParent() != null) {
			return getHandleIdentifier(javaElement.getParent());
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public List<String> getChildHandles(String handle) {
 		Object object = getObjectForHandle(handle);
 		if (object instanceof IModelElement) {
 			IModelElement element = (IModelElement) object;
 			if (element instanceof IParent) {
 				IParent parent = (IParent) element;
				IModelElement[] children;
 				try {
					children = parent.getChildren();
 					List<String> childHandles = new ArrayList<String>();
					for (IModelElement element2 : children) {
 						String childHandle = getHandleIdentifier(element2);
 						if (childHandle != null) {
 							childHandles.add(childHandle);
 						}
 					}
 					AbstractContextStructureBridge parentBridge = ContextCore.getStructureBridge(parentContentType);
 					if (parentBridge != null && ContextCore.CONTENT_TYPE_RESOURCE.equals(parentBridge.getContentType())) {
 						if (element.getElementType() < IModelElement.TYPE) {
 							List<String> resourceChildren = parentBridge.getChildHandles(handle);
 							if (!resourceChildren.isEmpty()) {
 								childHandles.addAll(resourceChildren);
 							}
 						}
 					}
 
 					return childHandles;
 				} catch (ModelException e) {
 					// NOTE: it would be better if this was not hard-wired but used the parent/child bridge mapping
 					AbstractContextStructureBridge parentBridge = ContextCore.getStructureBridge(ContextCore.CONTENT_TYPE_RESOURCE);
 					return parentBridge.getChildHandles(handle);
 				} catch (Exception e) {
 					StatusHandler.log(new Status(IStatus.ERROR, DLTKUiBridgePlugin.ID_PLUGIN, "Could not get children", //$NON-NLS-1$
 							e));
 				}
 			}
 		}
 		return Collections.emptyList();
 	}
 
 	@Override
 	public Object getObjectForHandle(String handle) {
 		try {
 			return DLTKCore.create(handle);
 		} catch (Throwable t) {
 			StatusHandler.log(new Status(IStatus.WARNING, DLTKUiBridgePlugin.ID_PLUGIN,
 					"Could not create java element for handle: " + handle, t)); //$NON-NLS-1$
 			return null;
 		}
 	}
 
 	/**
 	 * Uses resource-compatible path for projects
 	 */
 	@Override
 	public String getHandleIdentifier(Object object) {
 		if (object instanceof IModelElement) {
 			return ((IModelElement) object).getHandleIdentifier();
 		} else {
 			if (object instanceof IAdaptable) {
 				Object adapter = ((IAdaptable) object).getAdapter(IModelElement.class);
 				if (adapter instanceof IModelElement) {
 					return ((IModelElement) adapter).getHandleIdentifier();
 				}
 //			} else if (isWtpClass(object)) {
 //				return getWtpElementHandle(object);
 			}
 		}
 		return null;
 	}
 
 //	/**
 //	 * TODO: remove after WTP 1.5.1 is generally available
 //	 */
 //	private String getWtpElementHandle(Object object) {
 //		Class<?> objectClass = object.getClass();
 //		try {
 //			Method getProjectMethod = objectClass.getMethod("getProject", new Class[0]); //$NON-NLS-1$
 //			Object javaProject = getProjectMethod.invoke(object, new Object[0]);
 //			if (javaProject instanceof IScriptProject) {
 //				return ((IModelElement) javaProject).getHandleIdentifier();
 //			}
 //		} catch (Exception e) {
 //			// ignore
 //			e.printStackTrace();
 //		}
 //		return null;
 //	}
 
 	private boolean isWtpClass(Object object) {
 		try {
 			return object != null && object.getClass().getSimpleName().equals("CompressedJavaProject"); //$NON-NLS-1$
 		} catch (Throwable t) {
 			// could have malformed name, see bug 165065
 			return false;
 		}
 	}
 
 	@Override
 	public String getLabel(Object object) {
 		if (object instanceof IModelElement) {
 			return ((IModelElement) object).getElementName();
 		} else {
 			return ""; //$NON-NLS-1$
 		}
 	}
 
 	@Override
 	public boolean canBeLandmark(String handle) {
 		IModelElement element = (IModelElement) getObjectForHandle(handle);
 		if ((element instanceof IMember || element instanceof IType) && element.exists()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * TODO: figure out if the non IModelElement stuff is needed
 	 */
 	@Override
 	public boolean acceptsObject(Object object) {
 		if (object instanceof IResource) {
 			Object adapter = ((IResource) object).getAdapter(IModelElement.class);
 			return adapter instanceof IModelElement;
 		}
 
 		boolean accepts = object instanceof IModelElement || object instanceof ProjectFragmentContainer
 //				|| object instanceof ClassPathContainer.RequiredProjectWrapper || object instanceof JarEntryFile
 				|| object instanceof IProjectFragment || object instanceof WorkingSet || isWtpClass(object);
 
 		return accepts;
 	}
 
 	/**
 	 * Uses special rules for classpath containers since these do not have an associated interest, i.e. they're not
 	 * IModelElement(s).
 	 */
 	@Override
 	public boolean canFilter(Object object) {
 		if (object instanceof BuildPathContainer.RequiredProjectWrapper) {
 			return true;
 		} else if (object instanceof ProjectFragmentContainer) {
 			// since not in model, check if it contains anything interesting
 			ProjectFragmentContainer container = (ProjectFragmentContainer) object;
 
 			Object[] children = container.getChildren();
 			for (Object element2 : children) {
 				if (element2 instanceof ProjectFragment) {
 					ProjectFragment element = (ProjectFragment) element2;
 					IInteractionElement node = ContextCore.getContextManager()
 							.getElement(element.getHandleIdentifier());
 					if (node != null && node.getInterest().isInteresting()) {
 						return false;
 					}
 				}
 			}
 		} else if (object instanceof WorkingSet) {
 			try {
 				WorkingSet workingSet = (WorkingSet) object;
 				IAdaptable[] elements = workingSet.getElements();
 				for (IAdaptable adaptable : elements) {
 					IInteractionElement interactionElement = ContextCore.getContextManager().getElement(
 							getHandleIdentifier(adaptable));
 					if (interactionElement != null && interactionElement.getInterest().isInteresting()) {
 						return false;
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean isDocument(String handle) {
 		IModelElement element = (IModelElement) getObjectForHandle(handle);
 		return element instanceof ISourceModule/* || element instanceof IClassFile*/;
 	}
 
 	@Override
 	public String getHandleForOffsetInObject(Object object, int offset) {
 		IMarker marker;
 		if (object instanceof ConcreteMarker) {
 			marker = ((ConcreteMarker) object).getMarker();
 		} else if (object instanceof Marker) {
 			marker = (Marker) object;
 		} else {
 			return null;
 		}
 
 		try {
 			ISourceModule compilationUnit = null;
 			IResource resource = marker.getResource();
 			if (resource instanceof IFile) {
 				IFile file = (IFile) resource;
 				// TODO: get rid of file extension check
 				if (file.getFileExtension().equals("java")) { //$NON-NLS-1$
 					compilationUnit = DLTKCore.createSourceModuleFrom(file);
 				} else {
 					return null;
 				}
 			}
 			if (compilationUnit != null) {
 				// first try to resolve the character start, then the line number if not present
 				int charStart = 0;
 				Object attribute = marker.getAttribute(IMarker.CHAR_START, 0);
 				if (attribute instanceof Integer) {
 					charStart = ((Integer) attribute).intValue();
 				}
 				IModelElement javaElement = null;
 				if (charStart != -1) {
 					javaElement = compilationUnit.getElementAt(charStart);
 				} else {
 					int lineNumber = 0;
 					Object lineNumberAttribute = marker.getAttribute(IMarker.LINE_NUMBER, 0);
 					if (lineNumberAttribute instanceof Integer) {
 						lineNumber = ((Integer) lineNumberAttribute).intValue();
 					}
 					if (lineNumber != -1) {
 						// could do finer granularity by uncommenting what's below, see bug 132092
 //						Document document = new Document(compilationUnit.getSource());
 //						IRegion region = document.getLineInformation(lineNumber);
 //						javaElement = compilationUnit.getElementAt(region.getOffset());
 						javaElement = compilationUnit;
 					}
 				}
 
 				if (javaElement != null) {
 //					if (javaElement instanceof IImportDeclaration) {
 //						javaElement = javaElement.getParent().getParent();
 //					}
 					return javaElement.getHandleIdentifier();
 				} else {
 					return null;
 				}
 			} else {
 				return null;
 			}
 		} catch (ModelException ex) {
 			if (!ex.isDoesNotExist()) {
 				ExceptionHandler.handle(ex, "error", "could not find java element"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			return null;
 		} catch (Throwable t) {
 			StatusHandler.log(new Status(IStatus.ERROR, DLTKUiBridgePlugin.ID_PLUGIN, "Could not find element for: " //$NON-NLS-1$
 					+ marker, t));
 			return null;
 		}
 	}
 
 	@Override
 	public String getContentType(String elementHandle) {
 		return getContentType();
 	}
 
 	/**
 	 * Some copying from:
	 * 
 	 * @see org.eclipse.jdt.ui.ProblemsLabelDecorator
 	 */
 	public boolean containsProblem(IInteractionElement node) {
 		try {
 			IModelElement element = (IModelElement) getObjectForHandle(node.getHandleIdentifier());
 			switch (element.getElementType()) {
 			case IModelElement.SCRIPT_PROJECT:
 			case IModelElement.PROJECT_FRAGMENT:
 				return getErrorTicksFromMarkers(element.getResource(), IResource.DEPTH_INFINITE, null);
 			case IModelElement.SCRIPT_FOLDER:
 			case IModelElement.SOURCE_MODULE:
 //			case IModelElement.CLASS_FILE:
 				return getErrorTicksFromMarkers(element.getResource(), IResource.DEPTH_ONE, null);
 			case IModelElement.PACKAGE_DECLARATION:
 //			case IModelElement.IMPORT_DECLARATION:
 //			case IModelElement.IMPORT_CONTAINER:
 			case IModelElement.TYPE:
 //			case IModelElement.INITIALIZER:
 			case IModelElement.METHOD:
 			case IModelElement.FIELD:
 //			case IModelElement.LOCAL_VARIABLE:
 				ISourceModule cu = (ISourceModule) element.getAncestor(IModelElement.SCRIPT_MODEL);
 				if (cu != null) {
 					return getErrorTicksFromMarkers(element.getResource(), IResource.DEPTH_ONE, null);
 				}
 			}
 		} catch (CoreException e) {
 			// ignore
 		}
 		return false;
 	}
 
 	private boolean getErrorTicksFromMarkers(IResource res, int depth, ISourceReference sourceElement)
 			throws CoreException {
 		if (res == null || !res.isAccessible()) {
 			return false;
 		}
 		IMarker[] markers = res.findMarkers(IMarker.PROBLEM, true, depth);
 		if (markers != null) {
 			for (IMarker curr : markers) {
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
 }
