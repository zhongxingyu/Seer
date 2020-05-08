 /*******************************************************************************
  * Copyright (c) 2010 Ericsson
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  *   Alvaro Sanchez-Leon - Initial Implementation and API
  *******************************************************************************/
 package org.eclipse.mylyn.reviews.frame.core.model.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.mylyn.reviews.frame.core.model.Comment;
 import org.eclipse.mylyn.reviews.frame.core.model.CommentType;
 import org.eclipse.mylyn.reviews.frame.core.model.ModelPackage;
 import org.eclipse.mylyn.reviews.frame.core.model.User;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Comment</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.CommentImpl#getUser <em>User</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.CommentImpl#getType <em>Type</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.frame.core.model.impl.CommentImpl#getDescription <em>Description</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class CommentImpl extends ReviewComponentImpl implements Comment {
 	/**
 	 * The cached value of the '{@link #getUser() <em>User</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUser()
 	 * @generated
 	 * @ordered
 	 */
 	protected User user;
 
 	/**
 	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected CommentType type;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected CommentImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.COMMENT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public User getUser() {
 		return user;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUser(User newUser) {
 		User oldUser = user;
 		user = newUser;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.COMMENT__USER, oldUser, user));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CommentType getType() {
 		if (type != null && type.eIsProxy()) {
 			InternalEObject oldType = (InternalEObject)type;
 			type = (CommentType)eResolveProxy(oldType);
 			if (type != oldType) {
 				InternalEObject newType = (InternalEObject)type;
 				NotificationChain msgs = oldType.eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.COMMENT__TYPE, null, null);
 				if (newType.eInternalContainer() == null) {
 					msgs = newType.eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.COMMENT__TYPE, null, msgs);
 				}
 				if (msgs != null) msgs.dispatch();
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, ModelPackage.COMMENT__TYPE, oldType, type));
 			}
 		}
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public CommentType basicGetType() {
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetType(CommentType newType, NotificationChain msgs) {
 		CommentType oldType = type;
 		type = newType;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ModelPackage.COMMENT__TYPE, oldType, newType);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setType(CommentType newType) {
 		if (newType != type) {
 			NotificationChain msgs = null;
 			if (type != null)
 				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ModelPackage.COMMENT__TYPE, null, msgs);
 			if (newType != null)
 				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ModelPackage.COMMENT__TYPE, null, msgs);
 			msgs = basicSetType(newType, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.COMMENT__TYPE, newType, newType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDescription() {
		return description!= null ? description : "";
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ModelPackage.COMMENT__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ModelPackage.COMMENT__TYPE:
 				return basicSetType(null, msgs);
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
 			case ModelPackage.COMMENT__USER:
 				return getUser();
 			case ModelPackage.COMMENT__TYPE:
 				if (resolve) return getType();
 				return basicGetType();
 			case ModelPackage.COMMENT__DESCRIPTION:
 				return getDescription();
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
 			case ModelPackage.COMMENT__USER:
 				setUser((User)newValue);
 				return;
 			case ModelPackage.COMMENT__TYPE:
 				setType((CommentType)newValue);
 				return;
 			case ModelPackage.COMMENT__DESCRIPTION:
 				setDescription((String)newValue);
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
 			case ModelPackage.COMMENT__USER:
 				setUser((User)null);
 				return;
 			case ModelPackage.COMMENT__TYPE:
 				setType((CommentType)null);
 				return;
 			case ModelPackage.COMMENT__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
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
 			case ModelPackage.COMMENT__USER:
 				return user != null;
 			case ModelPackage.COMMENT__TYPE:
 				return type != null;
 			case ModelPackage.COMMENT__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
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
 		result.append(" (description: ");
 		result.append(description);
 		result.append(')');
 		return result.toString();
 	}
 
 } //CommentImpl
