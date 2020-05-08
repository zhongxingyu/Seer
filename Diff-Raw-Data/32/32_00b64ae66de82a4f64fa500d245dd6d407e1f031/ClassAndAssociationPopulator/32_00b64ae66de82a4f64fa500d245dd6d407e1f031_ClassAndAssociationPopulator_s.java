 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and generator
  */
 package org.modelexecution.fuml.convert.xmof.internal.ecore;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EReference;
 import org.modelexecution.fuml.convert.impl.ConversionResultImpl;
 import org.modelexecution.fuml.convert.xmof.internal.IElementPopulator;
 
 import fUML.Syntax.Classes.Kernel.AggregationKind;
 import fUML.Syntax.Classes.Kernel.Association;
 import fUML.Syntax.Classes.Kernel.Class_;
 import fUML.Syntax.Classes.Kernel.Classifier;
 import fUML.Syntax.Classes.Kernel.Element;
 import fUML.Syntax.Classes.Kernel.Generalization;
 import fUML.Syntax.Classes.Kernel.Operation;
 import fUML.Syntax.Classes.Kernel.Property;
 
 public class ClassAndAssociationPopulator implements IElementPopulator {
 
 	@Override
 	public void populate(Element fumlElement, EModelElement element,
 			ConversionResultImpl result) {
 		if (!(element instanceof EClass) || !(fumlElement instanceof Class_)) {
 			return;
 		}
 
 		Class_ fumlClass = (Class_) fumlElement;
 		EClass eClass = (EClass) element;
 
 		fumlClass.setIsAbstract(eClass.isAbstract());
 		addGeneralizations(fumlClass, eClass, result);
 		addOperations(fumlClass, eClass, result);
 		addAssociationsAndSetTheirValues(fumlClass, eClass, result);
 		addAttributes(fumlClass, eClass, result);
 	}
 
 	private void addGeneralizations(Class_ fumlClass, EClass eClass,
 			ConversionResultImpl result) {
 		for (EClass superType : eClass.getESuperTypes()) {
 			Generalization generalization = new Generalization();
 			generalization.specific = fumlClass;
 			generalization.setGeneral((Classifier) result
 					.getFUMLElement(superType));
 			fumlClass.addGeneralization(generalization);
 		}
 	}
 
 	private void addOperations(Class_ fumlClass, EClass eClass,
 			ConversionResultImpl result) {
 		for (EOperation operation : eClass.getEOperations()) {
 			fumlClass.addOwnedOperation((Operation) result
 					.getFUMLElement(operation));
 		}
 	}
 
 	private void addAssociationsAndSetTheirValues(Class_ fumlClass,
 			EClass eClass, ConversionResultImpl result) {
 		for (EReference reference : eClass.getEReferences()) {
 			Association association = (Association) result
 					.getFUMLElement(reference);
 			if (reference.getEOpposite() == null) {
 				setupUnidirectionalAssociation(association, reference, result);
 			} else {
 				setupBidirectionalAssociation(association, reference, result);
 			}
 		}
 	}
 
 	private void setupUnidirectionalAssociation(Association association,
 			EReference reference, ConversionResultImpl result) {
 		association.name = reference.getName();
 
 		Class_ fumlSourceClass = (Class_) result.getFUMLElement(reference
 				.getEContainingClass());
 		Class_ fumlTargetClass = (Class_) result.getFUMLElement(reference
 				.getEType());
 
 		Property sourceProperty = createSourceProperty(association, reference,
 				fumlSourceClass, fumlTargetClass);
 		association.memberEnd.add(sourceProperty);
 
 		Property targetProperty = new Property();
 		targetProperty.association = association;
 		targetProperty.setType(fumlSourceClass);
 		targetProperty.setName(reference.getEContainingClass().getName()
 				.toLowerCase());
 		targetProperty.setLower(1);
 		targetProperty.setUpper(1);
 		association.addOwnedEnd(targetProperty);
		association.memberEnd.add(targetProperty);
 	}
 
 	private void setupBidirectionalAssociation(Association association,
 			EReference reference, ConversionResultImpl result) {
 		EReference oppositeReference = reference.getEOpposite();
 
		if (association == null) {
			association = (Association) result
					.getFUMLElement(oppositeReference);
 		}
 
 		association.setName(reference.getName() + "_"
 				+ oppositeReference.getName());
 
 		Class_ fumlSourceClassCurrent = (Class_) result
 				.getFUMLElement(reference.getEContainingClass());
 		Class_ fumlTargetClassCurrent = (Class_) result
 				.getFUMLElement(reference.getEType());
 
 		Property sourceProperty = createSourceProperty(association, reference,
 				fumlSourceClassCurrent, fumlTargetClassCurrent);
 		association.memberEnd.add(sourceProperty);
 
 		Property targetProperty = createSourceProperty(association,
 				oppositeReference, fumlTargetClassCurrent,
 				fumlSourceClassCurrent);
 		association.memberEnd.add(targetProperty);
 
 		association.ownedEnd.clear();
 
 		mapAssociationToBothReferences(reference, association, result);
 	}
 
 	private void mapAssociationToBothReferences(EReference reference,
 			Association association, ConversionResultImpl result) {
 		result.addInOutMapping(reference, association);
 		result.addInOutMapping(reference.getEOpposite(), association);
 	}
 
 	private Property createSourceProperty(Association association,
 			EReference reference, Class_ fumlSourceClass, Class_ fumlTargetClass) {
 		Property property = new Property();
 		property.association = association;
 		property.setAggregation(getAggregationKind(reference));
 		property.setType(fumlTargetClass);
 		property.setName(reference.getName());
 		property.setLower(reference.getLowerBound());
 		property.setUpper(reference.getUpperBound());
 		fumlSourceClass.addOwnedAttribute(property);
 		return property;
 	}
 
 	private AggregationKind getAggregationKind(EReference reference) {
 		return reference.isContainment() ? AggregationKind.composite
 				: AggregationKind.none;
 	}
 
 	private void addAttributes(Class_ fumlClass, EClass eClass,
 			ConversionResultImpl result) {
 		for (EAttribute eAttribute : eClass.getEAttributes()) {
 			fumlClass.addOwnedAttribute((Property) result
 					.getFUMLElement(eAttribute));
 		}
 	}
 }
