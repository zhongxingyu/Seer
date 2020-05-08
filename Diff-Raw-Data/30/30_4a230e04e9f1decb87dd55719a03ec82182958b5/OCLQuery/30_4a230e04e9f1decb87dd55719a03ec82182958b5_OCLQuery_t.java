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
 package org.eclipse.emf.eef.extended.query;
 
 import org.eclipse.emf.ecore.EClassifier;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>OCL Query</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.emf.eef.extended.query.OCLQuery#getQuery <em>Query</em>}</li>
  *   <li>{@link org.eclipse.emf.eef.extended.query.OCLQuery#getContext <em>Context</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.emf.eef.extended.query.QueryPackage#getOCLQuery()
  * @model
  * @generated
  */
 public interface OCLQuery extends EEFUnderstandableQuery {
 	/**
 	 * Returns the value of the '<em><b>Query</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Query</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Query</em>' attribute.
 	 * @see #setQuery(String)
 	 * @see org.eclipse.emf.eef.extended.query.QueryPackage#getOCLQuery_Query()
 	 * @model required="true"
 	 * @generated
 	 */
 	String getQuery();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.emf.eef.extended.query.OCLQuery#getQuery <em>Query</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Query</em>' attribute.
 	 * @see #getQuery()
 	 * @generated
 	 */
 	void setQuery(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Context</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Context</em>' reference isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Context</em>' reference.
 	 * @see #setContext(EClassifier)
 	 * @see org.eclipse.emf.eef.extended.query.QueryPackage#getOCLQuery_Context()
 	 * @model required="true"
 	 * @generated
 	 */
 	EClassifier getContext();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.emf.eef.extended.query.OCLQuery#getContext <em>Context</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Context</em>' reference.
 	 * @see #getContext()
 	 * @generated
 	 */
 	void setContext(EClassifier value);
 
 } // OCLQuery
