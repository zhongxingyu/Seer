 /**
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.emf.compare.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.compare.ComparePackage;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.MatchResource;
 
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Match Resource</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.emf.compare.impl.MatchResourceImpl#getLeftURI <em>Left URI</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.impl.MatchResourceImpl#getRightURI <em>Right URI</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.impl.MatchResourceImpl#getOriginURI <em>Origin URI</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.impl.MatchResourceImpl#getLeft <em>Left</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.impl.MatchResourceImpl#getRight <em>Right</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.impl.MatchResourceImpl#getOrigin <em>Origin</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.impl.MatchResourceImpl#getComparison <em>Comparison</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
//Supressing warnings : generated code
@SuppressWarnings("all")
 public class MatchResourceImpl extends MinimalEObjectImpl implements MatchResource {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2012 Obeo.\r\nAll rights reserved. This program and the accompanying materials\r\nare made available under the terms of the Eclipse Public License v1.0\r\nwhich accompanies this distribution, and is available at\r\nhttp://www.eclipse.org/legal/epl-v10.html\r\n\r\nContributors:\r\n    Obeo - initial API and implementation"; //$NON-NLS-1$
 
 	/**
 	 * The default value of the '{@link #getLeftURI() <em>Left URI</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLeftURI()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LEFT_URI_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLeftURI() <em>Left URI</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLeftURI()
 	 * @generated
 	 * @ordered
 	 */
 	protected String leftURI = LEFT_URI_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getRightURI() <em>Right URI</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRightURI()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String RIGHT_URI_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getRightURI() <em>Right URI</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRightURI()
 	 * @generated
 	 * @ordered
 	 */
 	protected String rightURI = RIGHT_URI_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getOriginURI() <em>Origin URI</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOriginURI()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ORIGIN_URI_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getOriginURI() <em>Origin URI</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOriginURI()
 	 * @generated
 	 * @ordered
 	 */
 	protected String originURI = ORIGIN_URI_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getLeft() <em>Left</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLeft()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Resource LEFT_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getLeft() <em>Left</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLeft()
 	 * @generated
 	 * @ordered
 	 */
 	protected Resource left = LEFT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getRight() <em>Right</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRight()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Resource RIGHT_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getRight() <em>Right</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRight()
 	 * @generated
 	 * @ordered
 	 */
 	protected Resource right = RIGHT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getOrigin() <em>Origin</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOrigin()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final Resource ORIGIN_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getOrigin() <em>Origin</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getOrigin()
 	 * @generated
 	 * @ordered
 	 */
 	protected Resource origin = ORIGIN_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected MatchResourceImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ComparePackage.Literals.MATCH_RESOURCE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLeftURI() {
 		return leftURI;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLeftURI(String newLeftURI) {
 		String oldLeftURI = leftURI;
 		leftURI = newLeftURI;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ComparePackage.MATCH_RESOURCE__LEFT_URI,
 					oldLeftURI, leftURI));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getRightURI() {
 		return rightURI;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRightURI(String newRightURI) {
 		String oldRightURI = rightURI;
 		rightURI = newRightURI;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ComparePackage.MATCH_RESOURCE__RIGHT_URI,
 					oldRightURI, rightURI));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getOriginURI() {
 		return originURI;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOriginURI(String newOriginURI) {
 		String oldOriginURI = originURI;
 		originURI = newOriginURI;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ComparePackage.MATCH_RESOURCE__ORIGIN_URI,
 					oldOriginURI, originURI));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Resource getLeft() {
 		return left;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLeft(Resource newLeft) {
 		Resource oldLeft = left;
 		left = newLeft;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ComparePackage.MATCH_RESOURCE__LEFT,
 					oldLeft, left));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Resource getRight() {
 		return right;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRight(Resource newRight) {
 		Resource oldRight = right;
 		right = newRight;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ComparePackage.MATCH_RESOURCE__RIGHT,
 					oldRight, right));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Resource getOrigin() {
 		return origin;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setOrigin(Resource newOrigin) {
 		Resource oldOrigin = origin;
 		origin = newOrigin;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ComparePackage.MATCH_RESOURCE__ORIGIN,
 					oldOrigin, origin));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Comparison getComparison() {
 		if (eContainerFeatureID() != ComparePackage.MATCH_RESOURCE__COMPARISON)
 			return null;
 		return (Comparison)eContainer();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetComparison(Comparison newComparison, NotificationChain msgs) {
 		msgs = eBasicSetContainer((InternalEObject)newComparison, ComparePackage.MATCH_RESOURCE__COMPARISON,
 				msgs);
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setComparison(Comparison newComparison) {
 		if (newComparison != eInternalContainer()
 				|| (eContainerFeatureID() != ComparePackage.MATCH_RESOURCE__COMPARISON && newComparison != null)) {
 			if (EcoreUtil.isAncestor(this, newComparison))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString()); //$NON-NLS-1$
 			NotificationChain msgs = null;
 			if (eInternalContainer() != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newComparison != null)
 				msgs = ((InternalEObject)newComparison).eInverseAdd(this,
 						ComparePackage.COMPARISON__MATCHED_RESOURCES, Comparison.class, msgs);
 			msgs = basicSetComparison(newComparison, msgs);
 			if (msgs != null)
 				msgs.dispatch();
 		} else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, ComparePackage.MATCH_RESOURCE__COMPARISON,
 					newComparison, newComparison));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ComparePackage.MATCH_RESOURCE__COMPARISON:
 				if (eInternalContainer() != null)
 					msgs = eBasicRemoveFromContainer(msgs);
 				return basicSetComparison((Comparison)otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case ComparePackage.MATCH_RESOURCE__COMPARISON:
 				return basicSetComparison(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
 		switch (eContainerFeatureID()) {
 			case ComparePackage.MATCH_RESOURCE__COMPARISON:
 				return eInternalContainer().eInverseRemove(this,
 						ComparePackage.COMPARISON__MATCHED_RESOURCES, Comparison.class, msgs);
 		}
 		return super.eBasicRemoveFromContainerFeature(msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ComparePackage.MATCH_RESOURCE__LEFT_URI:
 				return getLeftURI();
 			case ComparePackage.MATCH_RESOURCE__RIGHT_URI:
 				return getRightURI();
 			case ComparePackage.MATCH_RESOURCE__ORIGIN_URI:
 				return getOriginURI();
 			case ComparePackage.MATCH_RESOURCE__LEFT:
 				return getLeft();
 			case ComparePackage.MATCH_RESOURCE__RIGHT:
 				return getRight();
 			case ComparePackage.MATCH_RESOURCE__ORIGIN:
 				return getOrigin();
 			case ComparePackage.MATCH_RESOURCE__COMPARISON:
 				return getComparison();
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
 			case ComparePackage.MATCH_RESOURCE__LEFT_URI:
 				setLeftURI((String)newValue);
 				return;
 			case ComparePackage.MATCH_RESOURCE__RIGHT_URI:
 				setRightURI((String)newValue);
 				return;
 			case ComparePackage.MATCH_RESOURCE__ORIGIN_URI:
 				setOriginURI((String)newValue);
 				return;
 			case ComparePackage.MATCH_RESOURCE__LEFT:
 				setLeft((Resource)newValue);
 				return;
 			case ComparePackage.MATCH_RESOURCE__RIGHT:
 				setRight((Resource)newValue);
 				return;
 			case ComparePackage.MATCH_RESOURCE__ORIGIN:
 				setOrigin((Resource)newValue);
 				return;
 			case ComparePackage.MATCH_RESOURCE__COMPARISON:
 				setComparison((Comparison)newValue);
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
 			case ComparePackage.MATCH_RESOURCE__LEFT_URI:
 				setLeftURI(LEFT_URI_EDEFAULT);
 				return;
 			case ComparePackage.MATCH_RESOURCE__RIGHT_URI:
 				setRightURI(RIGHT_URI_EDEFAULT);
 				return;
 			case ComparePackage.MATCH_RESOURCE__ORIGIN_URI:
 				setOriginURI(ORIGIN_URI_EDEFAULT);
 				return;
 			case ComparePackage.MATCH_RESOURCE__LEFT:
 				setLeft(LEFT_EDEFAULT);
 				return;
 			case ComparePackage.MATCH_RESOURCE__RIGHT:
 				setRight(RIGHT_EDEFAULT);
 				return;
 			case ComparePackage.MATCH_RESOURCE__ORIGIN:
 				setOrigin(ORIGIN_EDEFAULT);
 				return;
 			case ComparePackage.MATCH_RESOURCE__COMPARISON:
 				setComparison((Comparison)null);
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
 			case ComparePackage.MATCH_RESOURCE__LEFT_URI:
 				return LEFT_URI_EDEFAULT == null ? leftURI != null : !LEFT_URI_EDEFAULT.equals(leftURI);
 			case ComparePackage.MATCH_RESOURCE__RIGHT_URI:
 				return RIGHT_URI_EDEFAULT == null ? rightURI != null : !RIGHT_URI_EDEFAULT.equals(rightURI);
 			case ComparePackage.MATCH_RESOURCE__ORIGIN_URI:
 				return ORIGIN_URI_EDEFAULT == null ? originURI != null : !ORIGIN_URI_EDEFAULT
 						.equals(originURI);
 			case ComparePackage.MATCH_RESOURCE__LEFT:
 				return LEFT_EDEFAULT == null ? left != null : !LEFT_EDEFAULT.equals(left);
 			case ComparePackage.MATCH_RESOURCE__RIGHT:
 				return RIGHT_EDEFAULT == null ? right != null : !RIGHT_EDEFAULT.equals(right);
 			case ComparePackage.MATCH_RESOURCE__ORIGIN:
 				return ORIGIN_EDEFAULT == null ? origin != null : !ORIGIN_EDEFAULT.equals(origin);
 			case ComparePackage.MATCH_RESOURCE__COMPARISON:
 				return getComparison() != null;
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
 		if (eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (leftURI: "); //$NON-NLS-1$
 		result.append(leftURI);
 		result.append(", rightURI: "); //$NON-NLS-1$
 		result.append(rightURI);
 		result.append(", originURI: "); //$NON-NLS-1$
 		result.append(originURI);
 		result.append(", left: "); //$NON-NLS-1$
 		result.append(left);
 		result.append(", right: "); //$NON-NLS-1$
 		result.append(right);
 		result.append(", origin: "); //$NON-NLS-1$
 		result.append(origin);
 		result.append(')');
 		return result.toString();
 	}
 
 } //MatchResourceImpl
