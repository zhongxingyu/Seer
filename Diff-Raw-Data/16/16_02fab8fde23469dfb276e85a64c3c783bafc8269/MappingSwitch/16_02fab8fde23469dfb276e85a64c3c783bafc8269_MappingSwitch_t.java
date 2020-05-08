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
 package org.eclipse.emf.eef.mapping.util;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.mapping.AbstractElementBinding;
 import org.eclipse.emf.eef.mapping.AbstractPropertyBinding;
 import org.eclipse.emf.eef.mapping.Category;
 import org.eclipse.emf.eef.mapping.Databinding;
 import org.eclipse.emf.eef.mapping.DocumentedElement;
 import org.eclipse.emf.eef.mapping.EMFElementBinding;
 import org.eclipse.emf.eef.mapping.EMFMultiPropertiesBinding;
 import org.eclipse.emf.eef.mapping.EMFPropertyBinding;
 import org.eclipse.emf.eef.mapping.ElementBindingReference;
 import org.eclipse.emf.eef.mapping.MappingPackage;
 import org.eclipse.emf.eef.mapping.ModelElement;
 import org.eclipse.emf.eef.mapping.ModelProperty;
 import org.eclipse.emf.eef.mapping.StandardElementBinding;
 import org.eclipse.emf.eef.mapping.StandardPropertyBinding;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Switch</b> for the model's inheritance hierarchy.
  * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
  * to invoke the <code>caseXXX</code> method for each class of the model,
  * starting with the actual class of the object
  * and proceeding up the inheritance hierarchy
  * until a non-null result is returned,
  * which is the result of the switch.
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.eef.mapping.MappingPackage
  * @generated
  */
 public class MappingSwitch<T> {
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static MappingPackage modelPackage;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MappingSwitch() {
 		if (modelPackage == null) {
 			modelPackage = MappingPackage.eINSTANCE;
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	public T doSwitch(EObject theEObject) {
 		return doSwitch(theEObject.eClass(), theEObject);
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(EClass theEClass, EObject theEObject) {
 		if (theEClass.eContainer() == modelPackage) {
 			return doSwitch(theEClass.getClassifierID(), theEObject);
 		} else {
 			List<EClass> eSuperTypes = theEClass.getESuperTypes();
 			return eSuperTypes.isEmpty() ? defaultCase(theEObject) : doSwitch(eSuperTypes.get(0), theEObject);
 		}
 	}
 
 	/**
 	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the first non-null result returned by a <code>caseXXX</code> call.
 	 * @generated
 	 */
 	protected T doSwitch(int classifierID, EObject theEObject) {
 		switch (classifierID) {
 			case MappingPackage.DATABINDING: {
 				Databinding databinding = (Databinding)theEObject;
 				T result = caseDatabinding(databinding);
 				if (result == null)
 					result = caseDocumentedElement(databinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.MODEL_ELEMENT: {
 				ModelElement modelElement = (ModelElement)theEObject;
 				T result = caseModelElement(modelElement);
 				if (result == null)
 					result = caseDocumentedElement(modelElement);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.ABSTRACT_ELEMENT_BINDING: {
 				AbstractElementBinding abstractElementBinding = (AbstractElementBinding)theEObject;
 				T result = caseAbstractElementBinding(abstractElementBinding);
 				if (result == null)
 					result = caseDocumentedElement(abstractElementBinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.MODEL_PROPERTY: {
 				ModelProperty modelProperty = (ModelProperty)theEObject;
 				T result = caseModelProperty(modelProperty);
 				if (result == null)
 					result = caseDocumentedElement(modelProperty);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.ABSTRACT_PROPERTY_BINDING: {
 				AbstractPropertyBinding abstractPropertyBinding = (AbstractPropertyBinding)theEObject;
 				T result = caseAbstractPropertyBinding(abstractPropertyBinding);
 				if (result == null)
 					result = caseDocumentedElement(abstractPropertyBinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.EMF_ELEMENT_BINDING: {
 				EMFElementBinding emfElementBinding = (EMFElementBinding)theEObject;
 				T result = caseEMFElementBinding(emfElementBinding);
 				if (result == null)
 					result = caseAbstractElementBinding(emfElementBinding);
 				if (result == null)
 					result = caseDocumentedElement(emfElementBinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.EMF_PROPERTY_BINDING: {
 				EMFPropertyBinding emfPropertyBinding = (EMFPropertyBinding)theEObject;
 				T result = caseEMFPropertyBinding(emfPropertyBinding);
 				if (result == null)
 					result = caseAbstractPropertyBinding(emfPropertyBinding);
 				if (result == null)
 					result = caseDocumentedElement(emfPropertyBinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.STANDARD_ELEMENT_BINDING: {
 				StandardElementBinding standardElementBinding = (StandardElementBinding)theEObject;
 				T result = caseStandardElementBinding(standardElementBinding);
 				if (result == null)
 					result = caseAbstractElementBinding(standardElementBinding);
 				if (result == null)
 					result = caseDocumentedElement(standardElementBinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.STANDARD_PROPERTY_BINDING: {
 				StandardPropertyBinding standardPropertyBinding = (StandardPropertyBinding)theEObject;
 				T result = caseStandardPropertyBinding(standardPropertyBinding);
 				if (result == null)
 					result = caseAbstractPropertyBinding(standardPropertyBinding);
 				if (result == null)
 					result = caseDocumentedElement(standardPropertyBinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.CATEGORY: {
 				Category category = (Category)theEObject;
 				T result = caseCategory(category);
 				if (result == null)
 					result = caseDocumentedElement(category);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.ELEMENT_BINDING_REFERENCE: {
 				ElementBindingReference elementBindingReference = (ElementBindingReference)theEObject;
 				T result = caseElementBindingReference(elementBindingReference);
 				if (result == null)
 					result = caseDocumentedElement(elementBindingReference);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.DOCUMENTED_ELEMENT: {
 				DocumentedElement documentedElement = (DocumentedElement)theEObject;
 				T result = caseDocumentedElement(documentedElement);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case MappingPackage.EMF_MULTI_PROPERTIES_BINDING: {
 				EMFMultiPropertiesBinding emfMultiPropertiesBinding = (EMFMultiPropertiesBinding)theEObject;
 				T result = caseEMFMultiPropertiesBinding(emfMultiPropertiesBinding);
 				if (result == null)
 					result = caseAbstractPropertyBinding(emfMultiPropertiesBinding);
 				if (result == null)
 					result = caseDocumentedElement(emfMultiPropertiesBinding);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			default:
 				return defaultCase(theEObject);
 		}
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Databinding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Databinding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseDatabinding(Databinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Model Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Model Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseModelElement(ModelElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Abstract Element Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Abstract Element Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseAbstractElementBinding(AbstractElementBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Model Property</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Model Property</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseModelProperty(ModelProperty object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Abstract Property Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Abstract Property Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseAbstractPropertyBinding(AbstractPropertyBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EMF Element Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EMF Element Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEMFElementBinding(EMFElementBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EMF Property Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EMF Property Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEMFPropertyBinding(EMFPropertyBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Standard Element Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Standard Element Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseStandardElementBinding(StandardElementBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Standard Property Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Standard Property Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseStandardPropertyBinding(StandardPropertyBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Category</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseCategory(Category object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Element Binding Reference</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Element Binding Reference</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseElementBindingReference(ElementBindingReference object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Documented Element</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseDocumentedElement(DocumentedElement object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EMF Multi Properties Binding</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EMF Multi Properties Binding</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseEMFMultiPropertiesBinding(EMFMultiPropertiesBinding object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch, but this is the last case anyway.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
 	 * @generated
 	 */
 	public T defaultCase(EObject object) {
 		return null;
 	}
 
 } //MappingSwitch
