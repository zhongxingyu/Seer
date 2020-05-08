 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.servlet.ui.internal.navigator;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.navigator.CommonViewer;
 import org.eclipse.ui.navigator.ICommonContentExtensionSite;
 import org.eclipse.ui.navigator.INavigatorContentExtension;
 import org.eclipse.ui.navigator.INavigatorContentService;
 import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
 import org.eclipse.ui.navigator.PipelinedShapeModification;
 import org.eclipse.ui.navigator.PipelinedViewerUpdate;
 
 public class WebJavaContentProvider implements IPipelinedTreeContentProvider  {
 
 	private static final Object[] NO_CHILDREN = new Object[0];
 	private static final String JAVA_EXTENSION_ID = "org.eclipse.jdt.java.ui.javaContent"; //$NON-NLS-1$
 
 	/*
 	 * JDT likes to make it interesting so just one of their viewer types is made internal To avoid
 	 * the dependency, we use some reflection here to filter the type.
 	 */
 
 	private static Class INTERNAL_CONTAINER_CLASS;
 	static {
 		try {
 			INTERNAL_CONTAINER_CLASS = Class.forName("org.eclipse.jdt.internal.ui.packageview.ClassPathContainer"); //$NON-NLS-1$
 		} catch (Throwable t) {
 			// ignore if the class has been removed or renamed.
 			INTERNAL_CONTAINER_CLASS = null;
 		}
 	}
 
 
 	private CommonViewer commonViewer;
 	private ITreeContentProvider delegateContentProvider;
 	private final Map compressedNodes = new HashMap();
 
 	public void getPipelinedChildren(Object aParent, Set theCurrentChildren) {
 		if (aParent instanceof IJavaProject) {
 			cleanJavaContribution(theCurrentChildren);
 			theCurrentChildren.add(getCompressedNode((IJavaProject) aParent));
 		} 
 	}
 
 	private void cleanJavaContribution(Set theCurrentChildren) {
 		IJavaElement element = null;
 		for (Iterator iter = theCurrentChildren.iterator(); iter.hasNext();) {
 			Object child = (Object) iter.next();
 			if (child instanceof IResource && ((element = JavaCore.create((IResource) child)) != null) && element.exists())
 				iter.remove();
 			else if (child instanceof IJavaElement)
 				iter.remove();
 			else if (INTERNAL_CONTAINER_CLASS != null && INTERNAL_CONTAINER_CLASS.isInstance(child))
 				iter.remove();
 		}
 	}
 
 	private CompressedJavaProject getCompressedNode(IJavaProject project) {
 		CompressedJavaProject result = (CompressedJavaProject) compressedNodes.get(project);
 		if (result == null) {
 			compressedNodes.put(project, result = new CompressedJavaProject(commonViewer, project));
 		}
 		return result;
 	}
 
 	public void getPipelinedElements(Object anInput, Set theCurrentElements) { 
 
 	}
 
 	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
 
 		if (anObject instanceof IPackageFragmentRoot) {
 			IPackageFragmentRoot root = (IPackageFragmentRoot) anObject;
 			if (!root.isExternal() && aSuggestedParent instanceof IJavaProject) {
 				return getCompressedNode((IJavaProject) aSuggestedParent);
 			}
 		} else if(INTERNAL_CONTAINER_CLASS.isInstance(anObject) && aSuggestedParent instanceof IJavaProject) {
 			return getCompressedNode( (IJavaProject) aSuggestedParent ).getCompressedJavaLibraries();
 		}
 		
 		return null;
 	}
 
 	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
 		Object parent = anAddModification.getParent();
 		
 		if (parent instanceof IPackageFragmentRoot) {
 			IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) parent;
 			CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject());
 			if(compressedNode.isFlatteningSourceFolder()) {
 				anAddModification.setParent(compressedNode);
 			}
 			
 		} 
 		
 		return anAddModification;
 	}
 	
 
 	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
 
 		Object parent = aRemoveModification.getParent();
 		
 		if (parent instanceof IPackageFragmentRoot) {
 			IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) parent;
 			CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject());
 			if(compressedNode.isFlatteningSourceFolder()) {
 				aRemoveModification.setParent(compressedNode);
 			}
 			
 		} 
 		
 		return aRemoveModification;
 	}
 
 	public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
 		
 		Set refreshTargets = aRefreshSynchronization.getRefreshTargets();
 		for (Iterator iter = refreshTargets.iterator(); iter.hasNext();) {
 			Object refreshTarget = (Object) iter.next();
 			if (refreshTarget instanceof IPackageFragmentRoot) {
 				IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) refreshTarget;
 				CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject());
 				if(compressedNode.isFlatteningSourceFolder()) {
 					iter.remove(); // voids the iter but is okay because we're done with it
 					refreshTargets.add(compressedNode);
 					return true;
 				}
 				
 			}
 		}
 		
 		return false;
 	}
 
 
 	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
 		Set refreshTargets = anUpdateSynchronization.getRefreshTargets();
 		for (Iterator iter = refreshTargets.iterator(); iter.hasNext();) {
 			Object refreshTarget = (Object) iter.next();
 			if (refreshTarget instanceof IPackageFragmentRoot) {
 				IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) refreshTarget;
				if (sourceFolder.getJavaProject()!=null && sourceFolder.getJavaProject().exists() && sourceFolder.getJavaProject().isOpen()) {
					CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject());
					if(compressedNode.isFlatteningSourceFolder()) {
						iter.remove(); // voids the iterator; but is okay because we're done with it
						refreshTargets.add(compressedNode);
						return true;
					}
 				}
 			}
 		}
 		
 		return false; 
 	}
 
 	public void init(ICommonContentExtensionSite aSite) { 
 	}
 
 	public Object[] getChildren(Object parentElement) {
 		if (delegateContentProvider != null) {
 			if (parentElement instanceof CompressedJavaProject) {
 				return ((CompressedJavaProject)parentElement).getChildren(delegateContentProvider);
 			} else if (parentElement instanceof CompressedJavaLibraries) { 
 				return ((CompressedJavaLibraries)parentElement).getChildren(delegateContentProvider);
 			}
 		}
 		return NO_CHILDREN;
 	}
 
 	public Object getParent(Object element) {
 		if (element instanceof CompressedJavaProject)
 			return ((CompressedJavaProject) element).getProject();
 		if (element instanceof CompressedJavaLibraries) 
 			return ((CompressedJavaLibraries) element).getCompressedProject();
 		return null;
 	}
 
 	public boolean hasChildren(Object element) {
 		return (element instanceof CompressedJavaProject || element instanceof CompressedJavaLibraries);
 	}
 
 	public Object[] getElements(Object inputElement) {
 		return NO_CHILDREN;
 	}
 
 	public void dispose() { 
 		compressedNodes.clear(); 
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		if (viewer instanceof CommonViewer) {
 			commonViewer = (CommonViewer) viewer;
 			INavigatorContentService service = commonViewer.getNavigatorContentService();
 			INavigatorContentExtension javaext = service.getContentExtensionById(JAVA_EXTENSION_ID);
 			if (javaext != null)
 				delegateContentProvider = javaext.getContentProvider();
 			compressedNodes.clear();
 		}
 
 	}
 
 	public void restoreState(IMemento aMemento) {
 
 	}
 
 	public void saveState(IMemento aMemento) {
 
 	}
 	
 	public boolean isClasspathContainer(Object o) {		
 		return INTERNAL_CONTAINER_CLASS != null && INTERNAL_CONTAINER_CLASS.isInstance(o);
 	} 
 
 }
