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
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.eef.extended.editor.EditorPackage;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Package</b> for the model.
  * It contains accessors for the meta objects to represent
  * <ul>
  *   <li>each class,</li>
  *   <li>each feature of each class,</li>
  *   <li>each enum,</li>
  *   <li>and each data type</li>
  * </ul>
  * <!-- end-user-doc -->
  * @see org.eclipse.emf.eef.extended.query.QueryFactory
  * @model kind="package"
  * @generated
  */
 public interface QueryPackage extends EPackage {
 	/**
 	 * The package name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNAME = "query";
 
 	/**
 	 * The package namespace URI.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNS_URI = "http://www.eclipse.org/emf/eef/extended/query/1.0.0";
 
 	/**
 	 * The package namespace name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNS_PREFIX = "eef-ext-qry";
 
 	/**
 	 * The singleton instance of the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	QueryPackage eINSTANCE = org.eclipse.emf.eef.extended.query.impl.QueryPackageImpl.init();
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.eef.extended.query.impl.EEFUnderstandableQueryImpl <em>EEF Understandable Query</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.eef.extended.query.impl.EEFUnderstandableQueryImpl
 	 * @see org.eclipse.emf.eef.extended.query.impl.QueryPackageImpl#getEEFUnderstandableQuery()
 	 * @generated
 	 */
 	int EEF_UNDERSTANDABLE_QUERY = 0;
 
 	/**
 	 * The number of structural features of the '<em>EEF Understandable Query</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EEF_UNDERSTANDABLE_QUERY_FEATURE_COUNT = EditorPackage.REFERENCEABLE_OBJECT_FEATURE_COUNT + 0;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.eef.extended.query.impl.OCLQueryImpl <em>OCL Query</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.eef.extended.query.impl.OCLQueryImpl
 	 * @see org.eclipse.emf.eef.extended.query.impl.QueryPackageImpl#getOCLQuery()
 	 * @generated
 	 */
 	int OCL_QUERY = 1;
 
 	/**
 	 * The feature id for the '<em><b>Query</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int OCL_QUERY__QUERY = EEF_UNDERSTANDABLE_QUERY_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Context</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int OCL_QUERY__CONTEXT = EEF_UNDERSTANDABLE_QUERY_FEATURE_COUNT + 1;
 
 	/**
 	 * The number of structural features of the '<em>OCL Query</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int OCL_QUERY_FEATURE_COUNT = EEF_UNDERSTANDABLE_QUERY_FEATURE_COUNT + 2;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.eef.extended.query.impl.ExplicitPathQueryImpl <em>Explicit Path Query</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.eef.extended.query.impl.ExplicitPathQueryImpl
 	 * @see org.eclipse.emf.eef.extended.query.impl.QueryPackageImpl#getExplicitPathQuery()
 	 * @generated
 	 */
 	int EXPLICIT_PATH_QUERY = 2;
 
 	/**
 	 * The feature id for the '<em><b>Query</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EXPLICIT_PATH_QUERY__QUERY = EEF_UNDERSTANDABLE_QUERY_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Explicit Path Query</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int EXPLICIT_PATH_QUERY_FEATURE_COUNT = EEF_UNDERSTANDABLE_QUERY_FEATURE_COUNT + 1;
 
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.eef.extended.query.EEFUnderstandableQuery <em>EEF Understandable Query</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>EEF Understandable Query</em>'.
 	 * @see org.eclipse.emf.eef.extended.query.EEFUnderstandableQuery
 	 * @generated
 	 */
 	EClass getEEFUnderstandableQuery();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.eef.extended.query.OCLQuery <em>OCL Query</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>OCL Query</em>'.
 	 * @see org.eclipse.emf.eef.extended.query.OCLQuery
 	 * @generated
 	 */
 	EClass getOCLQuery();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.eef.extended.query.OCLQuery#getQuery <em>Query</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Query</em>'.
 	 * @see org.eclipse.emf.eef.extended.query.OCLQuery#getQuery()
 	 * @see #getOCLQuery()
 	 * @generated
 	 */
 	EAttribute getOCLQuery_Query();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.eef.extended.query.OCLQuery#getContext <em>Context</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Context</em>'.
 	 * @see org.eclipse.emf.eef.extended.query.OCLQuery#getContext()
 	 * @see #getOCLQuery()
 	 * @generated
 	 */
 	EReference getOCLQuery_Context();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.eef.extended.query.ExplicitPathQuery <em>Explicit Path Query</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Explicit Path Query</em>'.
 	 * @see org.eclipse.emf.eef.extended.query.ExplicitPathQuery
 	 * @generated
 	 */
 	EClass getExplicitPathQuery();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.eef.extended.query.ExplicitPathQuery#getQuery <em>Query</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Query</em>'.
 	 * @see org.eclipse.emf.eef.extended.query.ExplicitPathQuery#getQuery()
 	 * @see #getExplicitPathQuery()
 	 * @generated
 	 */
 	EReference getExplicitPathQuery_Query();
 
 	/**
 	 * Returns the factory that creates the instances of the model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the factory that creates the instances of the model.
 	 * @generated
 	 */
 	QueryFactory getQueryFactory();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Defines literals for the meta objects that represent
 	 * <ul>
 	 *   <li>each class,</li>
 	 *   <li>each feature of each class,</li>
 	 *   <li>each enum,</li>
 	 *   <li>and each data type</li>
 	 * </ul>
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	interface Literals {
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.eef.extended.query.impl.EEFUnderstandableQueryImpl <em>EEF Understandable Query</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.eef.extended.query.impl.EEFUnderstandableQueryImpl
 		 * @see org.eclipse.emf.eef.extended.query.impl.QueryPackageImpl#getEEFUnderstandableQuery()
 		 * @generated
 		 */
 		EClass EEF_UNDERSTANDABLE_QUERY = eINSTANCE.getEEFUnderstandableQuery();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.eef.extended.query.impl.OCLQueryImpl <em>OCL Query</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.eef.extended.query.impl.OCLQueryImpl
 		 * @see org.eclipse.emf.eef.extended.query.impl.QueryPackageImpl#getOCLQuery()
 		 * @generated
 		 */
 		EClass OCL_QUERY = eINSTANCE.getOCLQuery();
 
 		/**
 		 * The meta object literal for the '<em><b>Query</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute OCL_QUERY__QUERY = eINSTANCE.getOCLQuery_Query();
 
 		/**
 		 * The meta object literal for the '<em><b>Context</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference OCL_QUERY__CONTEXT = eINSTANCE.getOCLQuery_Context();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.eef.extended.query.impl.ExplicitPathQueryImpl <em>Explicit Path Query</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.eef.extended.query.impl.ExplicitPathQueryImpl
 		 * @see org.eclipse.emf.eef.extended.query.impl.QueryPackageImpl#getExplicitPathQuery()
 		 * @generated
 		 */
 		EClass EXPLICIT_PATH_QUERY = eINSTANCE.getExplicitPathQuery();
 
 		/**
 		 * The meta object literal for the '<em><b>Query</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference EXPLICIT_PATH_QUERY__QUERY = eINSTANCE.getExplicitPathQuery_Query();
 
 	}
 
 } //QueryPackage
