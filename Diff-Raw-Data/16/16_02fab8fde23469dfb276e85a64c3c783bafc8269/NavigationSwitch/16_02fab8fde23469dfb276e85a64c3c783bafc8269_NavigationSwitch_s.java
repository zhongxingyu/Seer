 /*******************************************************************************
 * Copyright (c) 2008, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.mapping.navigation.util;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.eef.mapping.navigation.ChainedModelNavigation;
 import org.eclipse.emf.eef.mapping.navigation.CustomModelNavigation;
 import org.eclipse.emf.eef.mapping.navigation.ModelNavigation;
 import org.eclipse.emf.eef.mapping.navigation.NavigationPackage;
 import org.eclipse.emf.eef.mapping.navigation.SimpleModelNavigation;
 import org.eclipse.emf.eef.mapping.navigation.StructuredModelNavigation;
 
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
  * @see org.eclipse.emf.eef.mapping.navigation.NavigationPackage
  * @generated
  */
 public class NavigationSwitch<T> {
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static NavigationPackage modelPackage;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NavigationSwitch() {
 		if (modelPackage == null) {
 			modelPackage = NavigationPackage.eINSTANCE;
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
 			case NavigationPackage.MODEL_NAVIGATION: {
 				ModelNavigation modelNavigation = (ModelNavigation)theEObject;
 				T result = caseModelNavigation(modelNavigation);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case NavigationPackage.SIMPLE_MODEL_NAVIGATION: {
 				SimpleModelNavigation simpleModelNavigation = (SimpleModelNavigation)theEObject;
 				T result = caseSimpleModelNavigation(simpleModelNavigation);
 				if (result == null)
 					result = caseStructuredModelNavigation(simpleModelNavigation);
 				if (result == null)
 					result = caseModelNavigation(simpleModelNavigation);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case NavigationPackage.CHAINED_MODEL_NAVIGATION: {
 				ChainedModelNavigation chainedModelNavigation = (ChainedModelNavigation)theEObject;
 				T result = caseChainedModelNavigation(chainedModelNavigation);
 				if (result == null)
 					result = caseStructuredModelNavigation(chainedModelNavigation);
 				if (result == null)
 					result = caseModelNavigation(chainedModelNavigation);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case NavigationPackage.STRUCTURED_MODEL_NAVIGATION: {
 				StructuredModelNavigation structuredModelNavigation = (StructuredModelNavigation)theEObject;
 				T result = caseStructuredModelNavigation(structuredModelNavigation);
 				if (result == null)
 					result = caseModelNavigation(structuredModelNavigation);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case NavigationPackage.CUSTOM_MODEL_NAVIGATION: {
 				CustomModelNavigation customModelNavigation = (CustomModelNavigation)theEObject;
 				T result = caseCustomModelNavigation(customModelNavigation);
 				if (result == null)
 					result = caseModelNavigation(customModelNavigation);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			default:
 				return defaultCase(theEObject);
 		}
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Model Navigation</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Model Navigation</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseModelNavigation(ModelNavigation object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Simple Model Navigation</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Simple Model Navigation</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseSimpleModelNavigation(SimpleModelNavigation object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Chained Model Navigation</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Chained Model Navigation</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseChainedModelNavigation(ChainedModelNavigation object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Structured Model Navigation</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Structured Model Navigation</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseStructuredModelNavigation(StructuredModelNavigation object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Custom Model Navigation</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Custom Model Navigation</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseCustomModelNavigation(CustomModelNavigation object) {
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
 
 } //NavigationSwitch
