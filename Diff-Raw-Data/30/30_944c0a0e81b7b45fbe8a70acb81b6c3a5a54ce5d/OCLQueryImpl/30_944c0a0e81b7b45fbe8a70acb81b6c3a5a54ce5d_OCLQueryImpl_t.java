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
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.eef.extended.query.OCLQuery;
 import org.eclipse.emf.eef.extended.query.QueryPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>OCL Query</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.emf.eef.extended.query.impl.OCLQueryImpl#getQuery <em>Query</em>}</li>
  *   <li>{@link org.eclipse.emf.eef.extended.query.impl.OCLQueryImpl#getContext <em>Context</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class OCLQueryImpl extends EEFUnderstandableQueryImpl implements OCLQuery {
 	/**
 	 * The default value of the '{@link #getQuery() <em>Query</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getQuery()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String QUERY_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getQuery() <em>Query</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getQuery()
 	 * @generated
 	 * @ordered
 	 */
 	protected String query = QUERY_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getContext() <em>Context</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getContext()
 	 * @generated
 	 * @ordered
 	 */
 	protected EClassifier context;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected OCLQueryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return QueryPackage.Literals.OCL_QUERY;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getQuery() {
 		return query;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setQuery(String newQuery) {
 		String oldQuery = query;
 		query = newQuery;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, QueryPackage.OCL_QUERY__QUERY, oldQuery, query));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClassifier getContext() {
 		if (context != null && context.eIsProxy()) {
 			InternalEObject oldContext = (InternalEObject)context;
 			context = (EClassifier)eResolveProxy(oldContext);
 			if (context != oldContext) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, QueryPackage.OCL_QUERY__CONTEXT, oldContext, context));
 			}
 		}
 		return context;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClassifier basicGetContext() {
 		return context;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setContext(EClassifier newContext) {
 		EClassifier oldContext = context;
 		context = newContext;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, QueryPackage.OCL_QUERY__CONTEXT, oldContext, context));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case QueryPackage.OCL_QUERY__QUERY:
 				return getQuery();
 			case QueryPackage.OCL_QUERY__CONTEXT:
 				if (resolve) return getContext();
 				return basicGetContext();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case QueryPackage.OCL_QUERY__QUERY:
 				setQuery((String)newValue);
 				return;
 			case QueryPackage.OCL_QUERY__CONTEXT:
 				setContext((EClassifier)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case QueryPackage.OCL_QUERY__QUERY:
 				setQuery(QUERY_EDEFAULT);
 				return;
 			case QueryPackage.OCL_QUERY__CONTEXT:
 				setContext((EClassifier)null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case QueryPackage.OCL_QUERY__QUERY:
 				return QUERY_EDEFAULT == null ? query != null : !QUERY_EDEFAULT.equals(query);
 			case QueryPackage.OCL_QUERY__CONTEXT:
 				return context != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (query: ");
 		result.append(query);
 		result.append(')');
 		return result.toString();
 	}
 
 } //OCLQueryImpl
