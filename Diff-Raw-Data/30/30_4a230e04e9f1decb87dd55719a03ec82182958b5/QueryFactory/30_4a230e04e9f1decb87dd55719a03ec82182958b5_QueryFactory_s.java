 /*******************************************************************************
 * Copyright (c) 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.extended.query;
 
 import org.eclipse.emf.ecore.EFactory;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Factory</b> for the model.
  * It provides a create method for each non-abstract class of the model.
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.eef.extended.query.QueryPackage
  * @generated
  */
 public interface QueryFactory extends EFactory {
 	/**
 	 * The singleton instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	QueryFactory eINSTANCE = org.eclipse.emf.eef.extended.query.impl.QueryFactoryImpl.init();
 
 	/**
 	 * Returns a new object of class '<em>OCL Query</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return a new object of class '<em>OCL Query</em>'.
 	 * @generated
 	 */
 	OCLQuery createOCLQuery();
 
 	/**
 	 * Returns a new object of class '<em>Explicit Path Query</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return a new object of class '<em>Explicit Path Query</em>'.
 	 * @generated
 	 */
 	ExplicitPathQuery createExplicitPathQuery();
 
 	/**
 	 * Returns the package supported by this factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the package supported by this factory.
 	 * @generated
 	 */
 	QueryPackage getQueryPackage();
 
 } //QueryFactory
