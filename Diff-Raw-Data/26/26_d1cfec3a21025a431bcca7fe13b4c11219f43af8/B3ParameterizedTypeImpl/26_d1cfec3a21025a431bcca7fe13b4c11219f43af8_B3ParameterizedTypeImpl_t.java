 /**
  * Copyright (c) 2009, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  * 
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 
 import java.util.Collection;
 import org.eclipse.b3.backend.evaluator.b3backend.B3ParameterizedType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>B3 Parameterized Type</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3ParameterizedTypeImpl#getOwnerType <em>Owner Type</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3ParameterizedTypeImpl#getRawType <em>Raw Type</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.B3ParameterizedTypeImpl#getActualArgumentsList <em>Actual Arguments List</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class B3ParameterizedTypeImpl extends EObjectImpl implements B3ParameterizedType {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The cached value of the '{@link #getOwnerType() <em>Owner Type</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOwnerType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type ownerType;
 
 	/**
 	 * The cached value of the '{@link #getRawType() <em>Raw Type</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRawType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type rawType;
 
 	/**
 	 * The cached value of the '{@link #getActualArgumentsList() <em>Actual Arguments List</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getActualArgumentsList()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Type> actualArgumentsList;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected B3ParameterizedTypeImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.B3_PARAMETERIZED_TYPE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type getOwnerType() {
 		return ownerType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetOwnerType(Type newOwnerType, NotificationChain msgs) {
 		Type oldOwnerType = ownerType;
 		ownerType = newOwnerType;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE, oldOwnerType, newOwnerType);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOwnerType(Type newOwnerType) {
 		if (newOwnerType != ownerType) {
 			NotificationChain msgs = null;
 			if (ownerType != null)
 				msgs = ((InternalEObject)ownerType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE, null, msgs);
 			if (newOwnerType != null)
 				msgs = ((InternalEObject)newOwnerType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE, null, msgs);
 			msgs = basicSetOwnerType(newOwnerType, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE, newOwnerType, newOwnerType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type getRawType() {
 		if (rawType != null && ((EObject)rawType).eIsProxy()) {
 			InternalEObject oldRawType = (InternalEObject)rawType;
 			rawType = (Type)eResolveProxy(oldRawType);
 			if (rawType != oldRawType) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, B3backendPackage.B3_PARAMETERIZED_TYPE__RAW_TYPE, oldRawType, rawType));
 			}
 		}
 		return rawType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type basicGetRawType() {
 		return rawType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRawType(Type newRawType) {
 		Type oldRawType = rawType;
 		rawType = newRawType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.B3_PARAMETERIZED_TYPE__RAW_TYPE, oldRawType, rawType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Type> getActualArgumentsList() {
 		if (actualArgumentsList == null) {
 			actualArgumentsList = new EObjectContainmentEList<Type>(Type.class, this, B3backendPackage.B3_PARAMETERIZED_TYPE__ACTUAL_ARGUMENTS_LIST);
 		}
 		return actualArgumentsList;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated NOT
 	 */
 	public Type[] getActualTypeArguments() {
		return (Type[]) getActualArgumentsList().toArray();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE:
 				return basicSetOwnerType(null, msgs);
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__ACTUAL_ARGUMENTS_LIST:
 				return ((InternalEList<?>)getActualArgumentsList()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE:
 				return getOwnerType();
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__RAW_TYPE:
 				if (resolve) return getRawType();
 				return basicGetRawType();
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__ACTUAL_ARGUMENTS_LIST:
 				return getActualArgumentsList();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE:
 				setOwnerType((Type)newValue);
 				return;
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__RAW_TYPE:
 				setRawType((Type)newValue);
 				return;
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__ACTUAL_ARGUMENTS_LIST:
 				getActualArgumentsList().clear();
 				getActualArgumentsList().addAll((Collection<? extends Type>)newValue);
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
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE:
 				setOwnerType((Type)null);
 				return;
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__RAW_TYPE:
 				setRawType((Type)null);
 				return;
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__ACTUAL_ARGUMENTS_LIST:
 				getActualArgumentsList().clear();
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
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__OWNER_TYPE:
 				return ownerType != null;
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__RAW_TYPE:
 				return rawType != null;
 			case B3backendPackage.B3_PARAMETERIZED_TYPE__ACTUAL_ARGUMENTS_LIST:
 				return actualArgumentsList != null && !actualArgumentsList.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 	@Override 
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		buf.append(rawType == null ? "null" : rawType.toString());
 		EList<Type> parameters = getActualArgumentsList();
 		if(parameters.size() > 0) {
 			buf.append("<");
 			for(Type p : parameters)
 				buf.append(p.toString());
 			buf.append(">");
 		}
 		return buf.toString();
 	}
 
 } //B3ParameterizedTypeImpl
