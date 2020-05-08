 /**
 /**
  * Copyright (c) 2010, 2012 Ericsson
  *  
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  * Alvaro Sanchez-Leon  - Initial API and implementation
  * 
  */
 package org.eclipse.mylyn.reviews.r4e.core.model.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EID;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>R4EID</b></em>'. <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EIDImpl#getSequenceID <em>Sequence ID</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.impl.R4EIDImpl#getUserID <em>User ID</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class R4EIDImpl extends EObjectImpl implements R4EID {
 	/**
 	 * The default value of the '{@link #getSequenceID() <em>Sequence ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getSequenceID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final int SEQUENCE_ID_EDEFAULT = 0;
 
 	/**
 	 * The cached value of the '{@link #getSequenceID() <em>Sequence ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getSequenceID()
 	 * @generated
 	 * @ordered
 	 */
 	protected int sequenceID = SEQUENCE_ID_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getUserID() <em>User ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getUserID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String USER_ID_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getUserID() <em>User ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * @see #getUserID()
 	 * @generated
 	 * @ordered
 	 */
 	protected String userID = USER_ID_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected R4EIDImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return RModelPackage.Literals.R4EID;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public int getSequenceID() {
 		return sequenceID;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setSequenceID(int newSequenceID) {
 		int oldSequenceID = sequenceID;
 		sequenceID = newSequenceID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4EID__SEQUENCE_ID, oldSequenceID, sequenceID));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getUserID() {
 		return userID;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUserID(String newUserID) {
 		String oldUserID = userID;
 		userID = newUserID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, RModelPackage.R4EID__USER_ID, oldUserID, userID));
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case RModelPackage.R4EID__SEQUENCE_ID:
 				return getSequenceID();
 			case RModelPackage.R4EID__USER_ID:
 				return getUserID();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case RModelPackage.R4EID__SEQUENCE_ID:
 				setSequenceID((Integer)newValue);
 				return;
 			case RModelPackage.R4EID__USER_ID:
 				setUserID((String)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case RModelPackage.R4EID__SEQUENCE_ID:
 				setSequenceID(SEQUENCE_ID_EDEFAULT);
 				return;
 			case RModelPackage.R4EID__USER_ID:
 				setUserID(USER_ID_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case RModelPackage.R4EID__SEQUENCE_ID:
 				return sequenceID != SEQUENCE_ID_EDEFAULT;
 			case RModelPackage.R4EID__USER_ID:
 				return USER_ID_EDEFAULT == null ? userID != null : !USER_ID_EDEFAULT.equals(userID);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (sequenceID: ");
 		result.append(sequenceID);
 		result.append(", userID: ");
 		result.append(userID);
 		result.append(')');
 		return result.toString();
 	}
 
 } //R4EIDImpl
