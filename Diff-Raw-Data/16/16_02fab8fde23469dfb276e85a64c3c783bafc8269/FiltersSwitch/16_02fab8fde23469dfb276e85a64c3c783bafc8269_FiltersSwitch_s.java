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
 package org.eclipse.emf.eef.mapping.filters.util;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 
 import org.eclipse.emf.eef.mapping.DocumentedElement;
 import org.eclipse.emf.eef.mapping.filters.BindingFilter;
 import org.eclipse.emf.eef.mapping.filters.BusinessFilter;
 import org.eclipse.emf.eef.mapping.filters.FiltersPackage;
 import org.eclipse.emf.eef.mapping.filters.JavaDeclarationFilter;
 import org.eclipse.emf.eef.mapping.filters.JavaExpressionFilter;
 import org.eclipse.emf.eef.mapping.filters.JavaFilter;
 import org.eclipse.emf.eef.mapping.filters.OCLFilter;
 import org.eclipse.emf.eef.mapping.filters.OnlyReferenceTypeFilter;
 import org.eclipse.emf.eef.mapping.filters.StrictTypingFilter;
 
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
  * @see org.eclipse.emf.eef.mapping.filters.FiltersPackage
  * @generated
  */
 public class FiltersSwitch<T> {
 	/**
 	 * The cached model package
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected static FiltersPackage modelPackage;
 
 	/**
 	 * Creates an instance of the switch.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public FiltersSwitch() {
 		if (modelPackage == null) {
 			modelPackage = FiltersPackage.eINSTANCE;
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
 			case FiltersPackage.BINDING_FILTER: {
 				BindingFilter bindingFilter = (BindingFilter)theEObject;
 				T result = caseBindingFilter(bindingFilter);
 				if (result == null)
 					result = caseDocumentedElement(bindingFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case FiltersPackage.OCL_FILTER: {
 				OCLFilter oclFilter = (OCLFilter)theEObject;
 				T result = caseOCLFilter(oclFilter);
 				if (result == null)
 					result = caseBindingFilter(oclFilter);
 				if (result == null)
 					result = caseDocumentedElement(oclFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case FiltersPackage.JAVA_FILTER: {
 				JavaFilter javaFilter = (JavaFilter)theEObject;
 				T result = caseJavaFilter(javaFilter);
 				if (result == null)
 					result = caseBindingFilter(javaFilter);
 				if (result == null)
 					result = caseDocumentedElement(javaFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case FiltersPackage.JAVA_DECLARATION_FILTER: {
 				JavaDeclarationFilter javaDeclarationFilter = (JavaDeclarationFilter)theEObject;
 				T result = caseJavaDeclarationFilter(javaDeclarationFilter);
 				if (result == null)
 					result = caseJavaFilter(javaDeclarationFilter);
 				if (result == null)
 					result = caseBindingFilter(javaDeclarationFilter);
 				if (result == null)
 					result = caseDocumentedElement(javaDeclarationFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case FiltersPackage.JAVA_EXPRESSION_FILTER: {
 				JavaExpressionFilter javaExpressionFilter = (JavaExpressionFilter)theEObject;
 				T result = caseJavaExpressionFilter(javaExpressionFilter);
 				if (result == null)
 					result = caseJavaFilter(javaExpressionFilter);
 				if (result == null)
 					result = caseBindingFilter(javaExpressionFilter);
 				if (result == null)
 					result = caseDocumentedElement(javaExpressionFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case FiltersPackage.BUSINESS_FILTER: {
 				BusinessFilter businessFilter = (BusinessFilter)theEObject;
 				T result = caseBusinessFilter(businessFilter);
 				if (result == null)
 					result = caseBindingFilter(businessFilter);
 				if (result == null)
 					result = caseDocumentedElement(businessFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case FiltersPackage.ONLY_REFERENCE_TYPE_FILTER: {
 				OnlyReferenceTypeFilter onlyReferenceTypeFilter = (OnlyReferenceTypeFilter)theEObject;
 				T result = caseOnlyReferenceTypeFilter(onlyReferenceTypeFilter);
 				if (result == null)
 					result = caseBusinessFilter(onlyReferenceTypeFilter);
 				if (result == null)
 					result = caseBindingFilter(onlyReferenceTypeFilter);
 				if (result == null)
 					result = caseDocumentedElement(onlyReferenceTypeFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			case FiltersPackage.STRICT_TYPING_FILTER: {
 				StrictTypingFilter strictTypingFilter = (StrictTypingFilter)theEObject;
 				T result = caseStrictTypingFilter(strictTypingFilter);
 				if (result == null)
 					result = caseBusinessFilter(strictTypingFilter);
 				if (result == null)
 					result = caseBindingFilter(strictTypingFilter);
 				if (result == null)
 					result = caseDocumentedElement(strictTypingFilter);
 				if (result == null)
 					result = defaultCase(theEObject);
 				return result;
 			}
 			default:
 				return defaultCase(theEObject);
 		}
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Binding Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Binding Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseBindingFilter(BindingFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>OCL Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>OCL Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseOCLFilter(OCLFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Java Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Java Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseJavaFilter(JavaFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Java Declaration Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Java Declaration Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseJavaDeclarationFilter(JavaDeclarationFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Java Expression Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Java Expression Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseJavaExpressionFilter(JavaExpressionFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Business Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Business Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseBusinessFilter(BusinessFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Only Reference Type Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Only Reference Type Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseOnlyReferenceTypeFilter(OnlyReferenceTypeFilter object) {
 		return null;
 	}
 
 	/**
 	 * Returns the result of interpreting the object as an instance of '<em>Strict Typing Filter</em>'.
 	 * <!-- begin-user-doc -->
 	 * This implementation returns null;
 	 * returning a non-null result will terminate the switch.
 	 * <!-- end-user-doc -->
 	 * @param object the target of the switch.
 	 * @return the result of interpreting the object as an instance of '<em>Strict Typing Filter</em>'.
 	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
 	 * @generated
 	 */
 	public T caseStrictTypingFilter(StrictTypingFilter object) {
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
 
 } //FiltersSwitch
