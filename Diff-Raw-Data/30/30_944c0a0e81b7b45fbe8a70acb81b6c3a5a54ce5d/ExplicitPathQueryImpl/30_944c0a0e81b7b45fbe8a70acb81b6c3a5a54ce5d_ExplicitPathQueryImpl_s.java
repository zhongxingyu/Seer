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
 package org.eclipse.emf.eef.extended.query.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.eef.extended.query.ExplicitPathQuery;
 import org.eclipse.emf.eef.extended.query.QueryPackage;
 import org.eclipse.emf.eef.mapping.navigation.ModelNavigation;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Explicit Path Query</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.emf.eef.extended.query.impl.ExplicitPathQueryImpl#getQuery <em>Query</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ExplicitPathQueryImpl extends EEFUnderstandableQueryImpl implements ExplicitPathQuery {
 	/**
 	 * The cached value of the '{@link #getQuery() <em>Query</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getQuery()
 	 * @generated
 	 * @ordered
 	 */
 	protected ModelNavigation query;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ExplicitPathQueryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return QueryPackage.Literals.EXPLICIT_PATH_QUERY;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ModelNavigation getQuery() {
 		if (query != null && query.eIsProxy()) {
 			InternalEObject oldQuery = (InternalEObject)query;
 			query = (ModelNavigation)eResolveProxy(oldQuery);
 			if (query != oldQuery) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, QueryPackage.EXPLICIT_PATH_QUERY__QUERY, oldQuery, query));
 			}
 		}
 		return query;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ModelNavigation basicGetQuery() {
 		return query;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setQuery(ModelNavigation newQuery) {
 		ModelNavigation oldQuery = query;
 		query = newQuery;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, QueryPackage.EXPLICIT_PATH_QUERY__QUERY, oldQuery, query));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case QueryPackage.EXPLICIT_PATH_QUERY__QUERY:
 				if (resolve) return getQuery();
 				return basicGetQuery();
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
 			case QueryPackage.EXPLICIT_PATH_QUERY__QUERY:
 				setQuery((ModelNavigation)newValue);
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
 			case QueryPackage.EXPLICIT_PATH_QUERY__QUERY:
 				setQuery((ModelNavigation)null);
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
 			case QueryPackage.EXPLICIT_PATH_QUERY__QUERY:
 				return query != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 } //ExplicitPathQueryImpl
