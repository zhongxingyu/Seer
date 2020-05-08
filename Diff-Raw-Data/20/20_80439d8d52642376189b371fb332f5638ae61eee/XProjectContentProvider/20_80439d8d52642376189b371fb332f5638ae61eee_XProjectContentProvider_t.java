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
 
import java.util.ArrayList;
import java.util.List;

 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jdt.core.IJavaProject;
 import org.jboss.tools.common.model.XFilteredTree;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.project.IModelNature;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.jst.web.ui.navigator.XLabelProvider.RootWrapper;
 
 public class XProjectContentProvider extends XContentProvider {
 
 	public Object[] getChildren(Object parentElement) {
		boolean root = false;
 		if(parentElement instanceof RootWrapper) {
			root = true;
 			parentElement = ((RootWrapper)parentElement).element;
 		}
 		if(parentElement instanceof XModelObject) {
 			XModelObject o = (XModelObject)parentElement;
 			XFilteredTree filteredTree = getFilteredTree(o);
 			if(filteredTree != null) {
				XModelObject[] result = filteredTree.getChildren(o);
				if(root) {
					List<XModelObject> list = new ArrayList<XModelObject>();
					for (XModelObject c: result) {
						if(c.getFileType() != XModelObject.SYSTEM) {
							list.add(c);
						}
					}
					return list.toArray(new XModelObject[0]);
				}
				return result;
 			}
 			return o.getChildrenForSave();
 		} else if(parentElement instanceof IProject || parentElement instanceof IJavaProject) {
 			if(parentElement instanceof IJavaProject) {
 				parentElement = ((IJavaProject)parentElement).getProject();
 			}
 			IProject f = (IProject)parentElement;
 			IModelNature n = EclipseResourceUtil.getModelNature(f);
 			if(n == null) return new Object[0];
 			XModelObject o = EclipseResourceUtil.getObjectByResource(f);
 			if(o == null) o = EclipseResourceUtil.createObjectForResource(f);
 			XFilteredTree filteredTree = getFilteredTree(o);
 			if(filteredTree == null) {
 				filteredTree = getFilteredTree(o);
 			}
 			if(filteredTree != null) {
 				RootWrapper w = new RootWrapper();
 				w.element = filteredTree.getRoot();
 				return new Object[]{w};
 			}
 			return new Object[0];
 		}
 		return new Object[0];
 	}
 
 	public Object getParent(Object element) {
 		if(element instanceof RootWrapper) {
 			element = ((RootWrapper)element).element;
 		}
 		if(element instanceof XModelObject) {
 			XModelObject o = (XModelObject)element;
 			XFilteredTree filteredTree = getFilteredTree(o);
 			if(filteredTree != null && o == filteredTree.getRoot()) {
 				return EclipseResourceUtil.getProject(o);
 			}
 			return o.getParent();
 		} else if(element instanceof IProject) {
 			IResource r = (IResource)element;
 			return r.getParent();
 		}
 		return null;
 	}
 
 }
