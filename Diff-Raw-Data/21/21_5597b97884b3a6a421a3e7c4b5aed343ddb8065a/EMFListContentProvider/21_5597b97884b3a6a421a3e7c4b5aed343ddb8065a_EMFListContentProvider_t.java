 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.providers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class EMFListContentProvider implements IStructuredContentProvider {
 
	private boolean nullable;

	/**
	 * @param nullable define if there is a null value or not
	 */
	public EMFListContentProvider(boolean nullable) {
		super();
		this.nullable = nullable;
	}

 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		// TODO Auto-generated method stub
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 * java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		// TODO Auto-generated method stub
 	}
 
 	public Object[] getElements(Object inputElement) {
 		if (inputElement instanceof EObject) {
 			EObject eObject = (EObject)inputElement;
 			TreeIterator allContents = EcoreUtil.getAllContents(eObject, true);
 			return asList(allContents).toArray();
 		}
 		if (inputElement instanceof Resource) {
 			Resource resource = (Resource)inputElement;
 			TreeIterator allContents = resource.getAllContents();
 			return asList(allContents).toArray();
 		}
 		if (inputElement instanceof ResourceSet) {
 			ResourceSet resourceSet = (ResourceSet)inputElement;
 			TreeIterator allContents = resourceSet.getAllContents();
 			return asList(allContents).toArray();
 		}
 		return null;
 	}
 
 	private List asList(TreeIterator iter) {
 		List result = new ArrayList();
		if (nullable)
			result.add("");  //$NON-NLS-1$
 		while (iter.hasNext())
 			result.add(iter.next());
 		return result;
 	}
 
 }
