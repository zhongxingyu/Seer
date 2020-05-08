 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.xtext.base.attributes;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.scoping.AbstractAttribution;
 import org.eclipse.ocl.examples.pivot.scoping.EnvironmentView;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeFilter;
 import org.eclipse.ocl.examples.pivot.scoping.ScopeView;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PathElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.PathNameCS;
 
 public class PathElementCSAttribution extends AbstractAttribution
 {
 	public static final @NonNull PathElementCSAttribution INSTANCE = new PathElementCSAttribution();
 
 	@Override
 	public ScopeView computeLookup(@NonNull EObject target, @NonNull EnvironmentView environmentView, @NonNull ScopeView scopeView) {
 		PathElementCS csPathElement = (PathElementCS)target;
 		EClassifier eClassifier = csPathElement.getElementType();
 		if (eClassifier == null) {									// If this is actually a definition
			Element element = csPathElement.basicGetElement();
 			assert (element instanceof NamedElement) && !element.eIsProxy();
 			environmentView.addNamedElement((NamedElement)element);
 			return null;
 		}
 		EClassifier savedRequiredType = environmentView.getRequiredType();
 		ScopeFilter scopeFilter = null;
 		try {
 			environmentView.setRequiredType(eClassifier);
 			PathNameCS csPathName = csPathElement.getPathName();
 			List<PathElementCS> path = csPathName.getPath();
 			int index = path.indexOf(csPathElement);
 			if (index >= path.size()-1) {			// Last element may have a scope filter
 				scopeFilter = csPathName.getScopeFilter();
 				if (scopeFilter != null) {
 					environmentView.addFilter(scopeFilter);
 				}
 			}
 			if (index <= 0) {						// First path element is resolved in parent's parent scope
 				environmentView.computeLookups(scopeView.getParent().getParent());
 			}
 			else {									// Subsequent elements in previous scope
 				Element parent = path.get(index-1).getElement();
 				if ((parent != null) && !parent.eIsProxy()) {
 //					environmentView.computeLookups(parent, null);
 					environmentView.computeQualifiedLookups(parent);
 				}
 			}
 			return null;
 		}
 		finally {
 			if (scopeFilter != null) {
 				environmentView.removeFilter(scopeFilter);
 			}
 			environmentView.setRequiredType(savedRequiredType);
 		}
 	}
 }
