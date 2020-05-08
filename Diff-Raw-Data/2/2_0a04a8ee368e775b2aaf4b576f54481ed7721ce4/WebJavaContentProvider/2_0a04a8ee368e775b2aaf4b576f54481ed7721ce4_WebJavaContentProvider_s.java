 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.servlet.ui.internal.navigator;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.servlet.ui.internal.plugin.ServletUIPlugin;
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
 		try {
 			if (aParent instanceof IProject && ((IProject)aParent).hasNature(JavaCore.NATURE_ID) && JavaEEProjectUtilities.isDynamicWebProject((IProject)aParent)) {
 				cleanJavaContribution(theCurrentChildren);
 				theCurrentChildren.add(getCompressedNode((IProject)aParent));
 			}
 		} catch (CoreException e) {
 			ServletUIPlugin.log(e);
 		} 
 	}
 
 	private void cleanJavaContribution(Set theCurrentChildren) {
 		IJavaElement element = null;
 		for (Iterator iter = theCurrentChildren.iterator(); iter.hasNext();) {
 			Object child = iter.next();
 			if (child instanceof IResource && ((element = JavaCore.create((IResource) child)) != null) && element.exists())
 				iter.remove();
 			else if (child instanceof IJavaElement)
 				iter.remove();
 			else if (INTERNAL_CONTAINER_CLASS != null && INTERNAL_CONTAINER_CLASS.isInstance(child))
 				iter.remove();
 		}
 	}
 
 	private CompressedJavaProject getCompressedNode(IProject project) {
 		if (!JavaEEProjectUtilities.isDynamicWebProject(project))
 			return null;
 		CompressedJavaProject result = (CompressedJavaProject) compressedNodes.get(project);
 		if (result == null) {
 			compressedNodes.put(project, result = new CompressedJavaProject(commonViewer, project));
 		}
 		return result;
 	}
 
 	public void getPipelinedElements(Object anInput, Set theCurrentElements) { 
 
 	}
 
 	public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
 
 		try {
 			if (anObject instanceof IPackageFragmentRoot) {
 				IPackageFragmentRoot root = (IPackageFragmentRoot) anObject;
 				if (!root.isExternal()) {
 					if( aSuggestedParent instanceof IJavaProject ) {
 						return getCompressedNode( ((IJavaProject) aSuggestedParent).getProject() );
 					} else if ( aSuggestedParent instanceof IProject && ((IProject)aSuggestedParent).hasNature(JavaCore.NATURE_ID) && JavaEEProjectUtilities.isDynamicWebProject((IProject)aSuggestedParent) ){
 						return getCompressedNode( ((IProject) aSuggestedParent) );
 					}
 				}
 			} else if(INTERNAL_CONTAINER_CLASS.isInstance(anObject)) {
 				if( aSuggestedParent instanceof IJavaProject ) {
 					return getCompressedNode( ((IJavaProject) aSuggestedParent).getProject() ).getCompressedJavaLibraries();
 				} else if ( aSuggestedParent instanceof IProject && ((IProject)aSuggestedParent).hasNature(JavaCore.NATURE_ID) && JavaEEProjectUtilities.isDynamicWebProject((IProject)aSuggestedParent)){
 					return getCompressedNode( ((IProject) aSuggestedParent) ).getCompressedJavaLibraries();
 				} 
 			}
 		} catch (CoreException e) {
 			ServletUIPlugin.log(e);
 		}
 		
 		return null;
 	}
 
 	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
 		Object parent = anAddModification.getParent();
 		
 		if (parent instanceof IPackageFragmentRoot) {
 			IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) parent;
 			if (JavaEEProjectUtilities.isDynamicWebProject(sourceFolder.getJavaProject().getProject())) {
 				CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject().getProject());
 				if(compressedNode.isFlatteningSourceFolder()) {
 					anAddModification.setParent(compressedNode);
 				}
 			}
 		} 
 		
 		return anAddModification;
 	}
 	
 
 	public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
 
 		Object parent = aRemoveModification.getParent();
 		
 		Set children = aRemoveModification.getChildren();
 		
 		for (Object child : children) {
 			if (child instanceof IFolder){
 				try {
 					IPackageFragment locatePackageFragment = locatePackageFragment((IFolder)child);
 					if(locatePackageFragment != null){
 						aRemoveModification.getChildren().remove(child);
 						aRemoveModification.getChildren().add(locatePackageFragment);
 						
 						return aRemoveModification;
 					}
 				} catch (JavaModelException e) {
 				}
 			}
 		}
 		
 		if (parent instanceof IPackageFragmentRoot) {
 			IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) parent;
 			if (JavaEEProjectUtilities.isDynamicWebProject(sourceFolder.getJavaProject().getProject())) {
 				CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject().getProject());
 				if(compressedNode.isFlatteningSourceFolder()) {
 					aRemoveModification.setParent(compressedNode);
 				}
 			}
 			
 		} 
 		
 		return aRemoveModification;
 	}
 
 	private IPackageFragment locatePackageFragment(IFolder child) throws JavaModelException {
 		IJavaElement elem = JavaCore.create(child);
 		if (elem instanceof IPackageFragment) {
 			IPackageFragment packageFragment = (IPackageFragment)elem;
 			for (IJavaElement javaElem = packageFragment ; javaElem != null ; javaElem = javaElem.getParent()) {
 				if (javaElem instanceof IPackageFragmentRoot) {
 					IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot)javaElem;
 					if (!fragmentRoot.isReadOnly() && !fragmentRoot.isExternal()) {
 						return packageFragment;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
 		
 		Set refreshTargets = aRefreshSynchronization.getRefreshTargets();
 		for (Iterator iter = refreshTargets.iterator(); iter.hasNext();) {
 			Object refreshTarget = iter.next();
 			if (refreshTarget instanceof IPackageFragmentRoot) {
 				IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) refreshTarget;
 				if (JavaEEProjectUtilities.isDynamicWebProject(sourceFolder.getJavaProject().getProject())) {
 					CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject().getProject());
 					if(compressedNode.isFlatteningSourceFolder()) {
 						iter.remove(); // voids the iter but is okay because we're done with it
 						refreshTargets.add(compressedNode);
 						return true;
 					}
 				}
 				
 			}
 		}
 		
 		return false;
 	}
 
 
 	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
 		Set refreshTargets = anUpdateSynchronization.getRefreshTargets();
 		for (Iterator iter = refreshTargets.iterator(); iter.hasNext();) {
 			Object refreshTarget = iter.next();
 			if (refreshTarget instanceof IPackageFragmentRoot) {
 				IPackageFragmentRoot sourceFolder = (IPackageFragmentRoot) refreshTarget;
 				if (sourceFolder.getJavaProject()!=null && sourceFolder.getJavaProject().exists() && sourceFolder.getJavaProject().isOpen() && JavaEEProjectUtilities.isDynamicWebProject(sourceFolder.getJavaProject().getProject())) {
 					CompressedJavaProject compressedNode = getCompressedNode(sourceFolder.getJavaProject().getProject());
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
