 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: ExplicitNavigationProperty.java,v 1.2 2011/05/07 16:41:20 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.library;
 
import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.ocl.examples.domain.elements.DomainProperty;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.evaluation.DomainEvaluator;
 import org.eclipse.ocl.examples.domain.evaluation.InvalidValueException;
 import org.eclipse.ocl.examples.domain.library.AbstractProperty;
 import org.eclipse.ocl.examples.domain.values.Value;
 import org.eclipse.ocl.examples.domain.values.ValueFactory;
import org.eclipse.ocl.examples.pivot.TemplateableElement;
import org.eclipse.ocl.examples.pivot.Type;
 
 /**
  * The static instance of CompositionProperty supports evaluation of
  * a property call that navigates a relationship to a container.
  */
 public class CompositionProperty extends AbstractProperty
 {
 	public static final CompositionProperty INSTANCE = new CompositionProperty();
 
 	public Value evaluate(DomainEvaluator evaluator, DomainType returnType, Value sourceValue, DomainProperty property) throws InvalidValueException {
 		ValueFactory valueFactory = evaluator.getValueFactory();
 		EObject eObject = sourceValue.asNavigableObject(); 
		Object eValue;
		EClass eClass = eObject.eClass();
		EStructuralFeature eFeature = eClass.getEStructuralFeature(property.getName());
		if (eFeature != null) {
			// A specialized property such as CollectionType.elementType is returned from the specialized type
			// An unspecialized property such as CollectionType.ownedOperation is returned from the unspecialized type
			if ((eObject instanceof Type) && !eObject.eIsSet(eFeature)) {
				TemplateableElement rawType = ((Type)eObject).getUnspecializedElement();
				if (rawType != null) {
					eObject = rawType;
				}
			}
			eValue = eObject.eGet(eFeature);
		}
		else {		
			eValue = eObject.eContainer();		// FIXME this only works for single container type
		}
 		if (eValue == null) {
 			return valueFactory.getNull();
 		}
 		else {
 	//		EReference eContainmentFeature = eObject.eContainmentFeature();
 			return valueFactory.valueOf(eValue /*, eContainmentFeature.getEContainingClass()*/);
 		}
 		// ??? Type conformance check
 	}
 }
