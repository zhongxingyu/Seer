 /*******************************************************************************
  * Copyright (c) 2008-2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.core.initializer;
 
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public abstract class AbstractTransformer {
 
 	protected String genConstraint(EModelElement source, String key) {
 		if (source != null) {
 			EAnnotation annotation = source.getEAnnotation("genConstraint");
 			if (annotation != null) {
 				return annotation.getDetails().get(key);
 			}
 		}
 		return null;
 	}
 
 	protected boolean isSignificant(EStructuralFeature feature) {
 		if (feature instanceof EAttribute) {
 			String genConstraint = genConstraint(feature, "significant");
 			if (genConstraint != null && genConstraint.equals("false"))
 				return false;
 			return true;
 		}
 		if (feature instanceof EReference) {
 			String genConstraint = genConstraint(feature, "significant");
 			if (genConstraint != null && genConstraint.equals("false"))
 				return false;
 			return true;
 		}
 		return true;
 	}
 
 	protected Object getViewName(EStructuralFeature feature) {
 		if (genConstraint(feature, "component") != null)
 			return genConstraint(feature, "component");
 		return null;
 	}
 
 	protected boolean inheritedMember(EStructuralFeature feature, EClass current) {
 		return !feature.getEContainingClass().equals(current) && getViewName(feature) != null;
 	}
 
 	protected EStructuralFeature findEStructuralFeature(EPackage metamodel, String name) {
 		TreeIterator<EObject> allContents = metamodel.eAllContents();
 		while (allContents.hasNext()) {
 			EObject next = allContents.next();
 			if (next instanceof EStructuralFeature && name.equals(((EStructuralFeature)next).getName()))
 				return (EStructuralFeature)next;
 		}
 		return null;
 	}
 
 	protected EClass findEClass(EPackage metamodel, String name) {
 		EcoreUtil.resolveAll(metamodel.eResource().getResourceSet());
 		TreeIterator<Notifier> allContents = metamodel.eResource().getResourceSet().getAllContents();
 		while (allContents.hasNext()) {
 			Notifier next = allContents.next();
 			if (next instanceof EClass && name.equals(((EClass)next).getName()))
 				return (EClass)next;
 		}
 		return null;
 	}
 
	protected boolean isReferenceWithEOpposite(EStructuralFeature feature) {
		return feature instanceof EReference && ((EReference)feature).getEOpposite() != null;
	}


 }
