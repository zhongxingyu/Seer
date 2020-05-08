 /*******************************************************************************
  * This file is protected by Copyright. 
  * Please refer to the COPYRIGHT file distributed with this source distribution.
  *
  * This file is part of REDHAWK IDE.
  *
  * All rights reserved.  This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package mil.jpeojtrs.sca.prf.impl;
 
 import mil.jpeojtrs.sca.prf.AbstractProperty;
 import mil.jpeojtrs.sca.prf.AbstractPropertyRef;
 import mil.jpeojtrs.sca.prf.PrfPackage;
 import mil.jpeojtrs.sca.prf.PropertyContainer;
 import mil.jpeojtrs.sca.prf.PropertyRefContainer;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.omg.CORBA.Any;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Property Ref</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link mil.jpeojtrs.sca.prf.impl.AbstractPropertyRefImpl#getRefID <em>Ref ID</em>}</li>
  *   <li>{@link mil.jpeojtrs.sca.prf.impl.AbstractPropertyRefImpl#getProperty <em>Property</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class AbstractPropertyRefImpl<P extends AbstractProperty> extends EObjectImpl implements AbstractPropertyRef<P> {
 	/**
 	 * The default value of the '{@link #getRefID() <em>Ref ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRefID()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String REF_ID_EDEFAULT = null;
 	/**
 	 * The cached value of the '{@link #getRefID() <em>Ref ID</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRefID()
 	 * @generated
 	 * @ordered
 	 */
 	protected String refID = REF_ID_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected AbstractPropertyRefImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return PrfPackage.Literals.ABSTRACT_PROPERTY_REF;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getRefID() {
 		return refID;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setRefID(String newRefID) {
 		String oldRefID = refID;
 		refID = newRefID;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, PrfPackage.ABSTRACT_PROPERTY_REF__REF_ID, oldRefID, refID));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	public P getProperty() {
 		P property = basicGetProperty();
 		return property != null && property.eIsProxy() ? (P)eResolveProxy((InternalEObject)property) : property;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public P basicGetProperty() {
 		if (eContainer instanceof PropertyRefContainer) {
 			PropertyContainer propertyContainer = ((PropertyRefContainer) eContainer).getPropertyContainer();
 			if (propertyContainer != null) {
 				return (P) propertyContainer.getProperty(getRefID());
 			}
 		}
		return null;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setProperty(P newProperty) {
 		if (newProperty != null) {
 			setRefID(newProperty.getId());
 		} else {
 			setRefID(null);
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public abstract Any toAny();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case PrfPackage.ABSTRACT_PROPERTY_REF__REF_ID:
 				return getRefID();
 			case PrfPackage.ABSTRACT_PROPERTY_REF__PROPERTY:
 				if (resolve) return getProperty();
 				return basicGetProperty();
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
 			case PrfPackage.ABSTRACT_PROPERTY_REF__REF_ID:
 				setRefID((String)newValue);
 				return;
 			case PrfPackage.ABSTRACT_PROPERTY_REF__PROPERTY:
 				setProperty((P)newValue);
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
 			case PrfPackage.ABSTRACT_PROPERTY_REF__REF_ID:
 				setRefID(REF_ID_EDEFAULT);
 				return;
 			case PrfPackage.ABSTRACT_PROPERTY_REF__PROPERTY:
 				setProperty((P)null);
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
 			case PrfPackage.ABSTRACT_PROPERTY_REF__REF_ID:
 				return REF_ID_EDEFAULT == null ? refID != null : !REF_ID_EDEFAULT.equals(refID);
 			case PrfPackage.ABSTRACT_PROPERTY_REF__PROPERTY:
 				return basicGetProperty() != null;
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
 		result.append(" (refID: ");
 		result.append(refID);
 		result.append(')');
 		return result.toString();
 	}
 
 } //PropertyRefImpl
