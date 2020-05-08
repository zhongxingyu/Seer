 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.impl.filters;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 /**
  * Provide a generic filter, it returns the exact elements and the containers of elements of the given
  * <Eclass> type.
  * 
  * @author <a href="mailto:jerome.benois@obeo.fr">Jerome Benois</a>
  * @author <a href="mailto:stephane.bouchet@obeo.fr">Stephane Bouchet</a>
  */
 public class EObjectFilter extends ViewerFilter {
 
 	protected EClass eClassToFilter;
 
 	/**
 	 * @param eClassToFilter
 	 *            the type use to filter elements
 	 */
 	public EObjectFilter(EClass eClassToFilter) {
 		this.eClassToFilter = eClassToFilter;
 	}
 
 	@Override
 	public boolean select(Viewer viewer, Object parentElement, Object element) {
 		// this is a resource display it only if it contains a given <EClass> type
 		if (element instanceof Resource) {
 			TreeIterator<EObject> iter = (((Resource)element)).getAllContents();
 			while (iter.hasNext()) {
 				if (EEFUtils.containsInstanceOfEClass(iter.next(), eClassToFilter))
 					return true;
 			}
 		}
 		if (element instanceof EObject) {
 			EObject eObject = (EObject)element;
 			return EEFUtils.containsInstanceOfEClass(eObject, eClassToFilter);
 		}
 		return false;
 	}
 
 }
