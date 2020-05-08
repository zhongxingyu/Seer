 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.navigator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.jboss.tools.common.model.XFilteredTree;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.project.IModelNature;
import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.ui.navigator.TreeViewerModelListenerImpl;
 import org.jboss.tools.common.model.ui.preferences.DecoratorPreferencesListener;
 import org.jboss.tools.common.model.ui.views.navigator.FilteredTreesCache;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.model.util.XModelTreeListenerSWTASync;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
 
 public class XContentProvider implements ITreeContentProvider {
 	protected Viewer viewer = null;
 	protected TreeViewerModelListenerImpl listener = createListener();
 	protected XModelTreeListenerSWTASync syncListener = new XModelTreeListenerSWTASync(listener);
 
 	DecoratorPreferencesListener decoratorListener = null;
 
 	protected TreeViewerModelListenerImpl createListener() {
 		return new TreeViewerModelListenerImpl();
 	}
 
 	public Object[] getChildren(Object parentElement) {
 		if(parentElement instanceof XModelObject) {
 			XModelObject o = (XModelObject)parentElement;
 			XFilteredTree filteredTree = getFilteredTree(parentElement);
 			if(filteredTree != null) {
 				return filteredTree.getChildren(o);
 			}
 			return o.getChildrenForSave();
 		} else if(parentElement instanceof IFile) {
 			IFile f = (IFile)parentElement;
 			if(!f.isSynchronized(IResource.DEPTH_ZERO)) {
 				IPath p = f.getLocation();
 				if(p != null) {
 					if(!p.toFile().isFile()) return null;
 				}
 			}
 			if(!f.isAccessible()) return null;
 			XModelObject o = EclipseResourceUtil.getObjectByResource(f);
 			XFilteredTree filteredTree = getFilteredTree(o);
 			if(filteredTree != null) {
 				return filteredTree.getChildren(o);
 			}
 			if(o == null) {
 				o = EclipseResourceUtil.createObjectForResource(f);
 				if(o != null) {
 					FilteredTreesCache.getInstance().addListener(syncListener, o.getModel());
 				}
 			}
 			return getChildren(o);
 		}
 		return new Object[0];
 	}
 
 	public Object getParent(Object element) {
 		if(element instanceof XModelObject) {
 			XModelObject o = (XModelObject)element;
 			XModelObject p = o.getParent();
 			if(p != null && p.getFileType() == XModelObject.FILE) {
 				return EclipseResourceUtil.getResource(p);
 			}
 			return p;
 		} else if(element instanceof IResource) {
 			IResource r = (IResource)element;
 			return r.getParent();
 		}
 		return null;
 	}
 
 	public boolean hasChildren(Object element) {
 		if(element instanceof XModelObject) {
 			XModelObject o = (XModelObject)element;
 			XFilteredTree filteredTree = getFilteredTree(o);
 			if(filteredTree != null) {
 				return filteredTree.hasChildren(o);
 			}
 			return o.hasChildren();
 		}
 		return true;
 	}
 
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	public void dispose() {
 		viewer = null;
 		if(syncListener != null) {
 			FilteredTreesCache.getInstance().removeListener(syncListener);
 			syncListener = null;
 			listener = null;
 		}
 		if(decoratorListener != null) {
 			decoratorListener.dispose();
 			decoratorListener = null;
 		}
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		this.viewer = viewer;
 		if(listener != null && viewer instanceof TreeViewer) {
 			listener.setViewer((TreeViewer)viewer);
 		}
 		if(viewer instanceof StructuredViewer) {
 			if(decoratorListener == null) {
 				decoratorListener = new DecoratorPreferencesListener();
 				decoratorListener.init();
 			}
 			decoratorListener.setViewer((StructuredViewer)viewer);
 		}
 	}
 
 	protected XFilteredTree getFilteredTree(Object object)	{
 		XFilteredTree result = null;		
 		if (result == null && object instanceof XModelObject) {			
 			XModel model = ((XModelObject)object).getModel();
 			String n = getFilteredTreeName(model);
 			result = FilteredTreesCache.getInstance().getFilteredTree(n, model);
 			if(result == null) return null;
 			if(result.getRoot() == null) {
 				result = null; 
 			} else { 
 				FilteredTreesCache.getInstance().addListener(syncListener, model);
 			}
 		}		
 		return result;
 	}
 
 	protected String getFilteredTreeName(XModel model) {
 		String nature = model.getProperties().getProperty("nature"); //$NON-NLS-1$
 		IModelNature n = EclipseResourceUtil.getModelNature((IProject)model.getProperties().get("project")); //$NON-NLS-1$
 		if(nature != null && n != null && !n.getID().equals(nature)) {
 			nature = n.getID();
 			model.getProperties().setProperty("nature", nature); //$NON-NLS-1$
 		}
 		if(nature != null && nature.indexOf("struts") >= 0) { //$NON-NLS-1$
 			return "StrutsProjects"; //$NON-NLS-1$
 		} else if(nature != null && nature.indexOf("jsf") >= 0) { //$NON-NLS-1$
 			return "JSFProjects"; //$NON-NLS-1$
 		} else {
 			return null;
 		}
 	}
 
 }
