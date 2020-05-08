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
 package org.eclipse.emf.eef.extended.query.impl;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.eef.extended.query.ExplicitPathQuery;
 import org.eclipse.emf.eef.extended.query.OCLQuery;
 import org.eclipse.emf.eef.extended.query.QueryFactory;
 import org.eclipse.emf.eef.extended.query.QueryPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class QueryFactoryImpl extends EFactoryImpl implements QueryFactory {
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static QueryFactory init() {
 		try {
 			QueryFactory theQueryFactory = (QueryFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/emf/eef/extended/query/1.0.0"); 
 			if (theQueryFactory != null) {
 				return theQueryFactory;
 			}
 		}
 		catch (Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new QueryFactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public QueryFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 			case QueryPackage.OCL_QUERY: return createOCLQuery();
 			case QueryPackage.EXPLICIT_PATH_QUERY: return createExplicitPathQuery();
 			default:
 				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public OCLQuery createOCLQuery() {
 		OCLQueryImpl oclQuery = new OCLQueryImpl();
 		return oclQuery;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExplicitPathQuery createExplicitPathQuery() {
 		ExplicitPathQueryImpl explicitPathQuery = new ExplicitPathQueryImpl();
 		return explicitPathQuery;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public QueryPackage getQueryPackage() {
 		return (QueryPackage)getEPackage();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static QueryPackage getPackage() {
 		return QueryPackage.eINSTANCE;
 	}
 
 } //QueryFactoryImpl
