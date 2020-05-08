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
 
 import org.eclipse.emf.eef.mapping.navigation.ModelNavigation;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Explicit Path Query</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.emf.eef.extended.query.ExplicitPathQuery#getQuery <em>Query</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.emf.eef.extended.query.QueryPackage#getExplicitPathQuery()
  * @model
  * @generated
  */
 public interface ExplicitPathQuery extends EEFUnderstandableQuery {
 	/**
 	 * Returns the value of the '<em><b>Query</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Query</em>' reference isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Query</em>' reference.
 	 * @see #setQuery(ModelNavigation)
 	 * @see org.eclipse.emf.eef.extended.query.QueryPackage#getExplicitPathQuery_Query()
 	 * @model required="true"
 	 * @generated
 	 */
 	ModelNavigation getQuery();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.emf.eef.extended.query.ExplicitPathQuery#getQuery <em>Query</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Query</em>' reference.
 	 * @see #getQuery()
 	 * @generated
 	 */
 	void setQuery(ModelNavigation value);
 
 } // ExplicitPathQuery
