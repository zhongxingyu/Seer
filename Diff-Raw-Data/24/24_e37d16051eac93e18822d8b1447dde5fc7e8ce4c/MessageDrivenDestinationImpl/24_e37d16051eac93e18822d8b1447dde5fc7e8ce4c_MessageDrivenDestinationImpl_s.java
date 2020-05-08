 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.ejb.internal.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jst.j2ee.ejb.DestinationType;
 import org.eclipse.jst.j2ee.ejb.EjbPackage;
 import org.eclipse.jst.j2ee.ejb.MessageDriven;
 import org.eclipse.jst.j2ee.ejb.MessageDrivenDestination;
 import org.eclipse.jst.j2ee.ejb.SubscriptionDurabilityKind;
 
 /**
  * The message-driven-destination element provides advice to the Deployer as to whether a message-driven bean is intended for a Queue or a Topic. The declaration consists of: the type of the message-driven bean's intended destination and an optional declaration of whether a durable or non-durable subscription should be used if the destination-type is javax.jms.Topic.
  */
 public class MessageDrivenDestinationImpl extends EObjectImpl implements MessageDrivenDestination, EObject{
 
 	/**
 	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final DestinationType TYPE_EDEFAULT = DestinationType.QUEUE_LITERAL;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected DestinationType type = TYPE_EDEFAULT;
 	/**
 	 * This is true if the Type attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean typeESet = false;
 
 	/**
 	 * The default value of the '{@link #getSubscriptionDurability() <em>Subscription Durability</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getSubscriptionDurability()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final SubscriptionDurabilityKind SUBSCRIPTION_DURABILITY_EDEFAULT = SubscriptionDurabilityKind.DURABLE_LITERAL;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected SubscriptionDurabilityKind subscriptionDurability = SUBSCRIPTION_DURABILITY_EDEFAULT;
 	/**
 	 * This is true if the Subscription Durability attribute has been set.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean subscriptionDurabilityESet = false;
 
 	public MessageDrivenDestinationImpl() {
 		super();
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return EjbPackage.eINSTANCE.getMessageDrivenDestination();
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 * The destination-type element specifies the type of the JMS destination. The type is specified by the Java interface expected to be implemented by the destination.
 	 * 
 	 * The destination-type element must be one of the two following: javax.jms.Queue, javax.jms.Topic
 	 */
 	public DestinationType getType() {
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setType(DestinationType newType) {
 		DestinationType oldType = type;
 		type = newType == null ? TYPE_EDEFAULT : newType;
 		boolean oldTypeESet = typeESet;
 		typeESet = true;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.MESSAGE_DRIVEN_DESTINATION__TYPE, oldType, type, !oldTypeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetType() {
 		DestinationType oldType = type;
 		boolean oldTypeESet = typeESet;
 		type = TYPE_EDEFAULT;
 		typeESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, EjbPackage.MESSAGE_DRIVEN_DESTINATION__TYPE, oldType, TYPE_EDEFAULT, oldTypeESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetType() {
 		return typeESet;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 * The subscription-durability element specifies whether a JMS topic subscription is intended to be durable or nondurable.
 	 * 
 	 * The subscription-durability element must be one of the two following:  Durable, NonDurable
 
 	 */
 	public SubscriptionDurabilityKind getSubscriptionDurability() {
 		return subscriptionDurability;
 	}
 
 	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSubscriptionDurability(SubscriptionDurabilityKind newSubscriptionDurability) {
		SubscriptionDurabilityKind oldSubscriptionDurability = subscriptionDurability;
		subscriptionDurability = newSubscriptionDurability == null ? SUBSCRIPTION_DURABILITY_EDEFAULT : newSubscriptionDurability;
		boolean oldSubscriptionDurabilityESet = subscriptionDurabilityESet;
		subscriptionDurabilityESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.MESSAGE_DRIVEN_DESTINATION__SUBSCRIPTION_DURABILITY, oldSubscriptionDurability, subscriptionDurability, !oldSubscriptionDurabilityESet));
	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void unsetSubscriptionDurability() {
 		SubscriptionDurabilityKind oldSubscriptionDurability = subscriptionDurability;
 		boolean oldSubscriptionDurabilityESet = subscriptionDurabilityESet;
 		subscriptionDurability = SUBSCRIPTION_DURABILITY_EDEFAULT;
 		subscriptionDurabilityESet = false;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.UNSET, EjbPackage.MESSAGE_DRIVEN_DESTINATION__SUBSCRIPTION_DURABILITY, oldSubscriptionDurability, SUBSCRIPTION_DURABILITY_EDEFAULT, oldSubscriptionDurabilityESet));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isSetSubscriptionDurability() {
 		return subscriptionDurabilityESet;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 */
 	public MessageDriven getBean() {
 		if (eContainerFeatureID != EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN) return null;
 		return (MessageDriven)eContainer;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setBean(MessageDriven newBean) {
 		if (newBean != eContainer || (eContainerFeatureID != EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN && newBean != null)) {
 			if (EcoreUtil.isAncestor(this, newBean))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
 			NotificationChain msgs = null;
 			if (eContainer != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newBean != null)
 				msgs = ((InternalEObject)newBean).eInverseAdd(this, EjbPackage.MESSAGE_DRIVEN__DESTINATION, MessageDriven.class, msgs);
 			msgs = eBasicSetContainer((InternalEObject)newBean, EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN, newBean, newBean));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN:
 					if (eContainer != null)
 						msgs = eBasicRemoveFromContainer(msgs);
 					return eBasicSetContainer(otherEnd, EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN, msgs);
 				default:
 					return eDynamicInverseAdd(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		if (eContainer != null)
 			msgs = eBasicRemoveFromContainer(msgs);
 		return eBasicSetContainer(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN:
 					return eBasicSetContainer(null, EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN, msgs);
 				default:
 					return eDynamicInverseRemove(otherEnd, featureID, baseClass, msgs);
 			}
 		}
 		return eBasicSetContainer(null, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eBasicRemoveFromContainer(NotificationChain msgs) {
 		if (eContainerFeatureID >= 0) {
 			switch (eContainerFeatureID) {
 				case EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN:
 					return eContainer.eInverseRemove(this, EjbPackage.MESSAGE_DRIVEN__DESTINATION, MessageDriven.class, msgs);
 				default:
 					return eDynamicBasicRemoveFromContainer(msgs);
 			}
 		}
 		return eContainer.eInverseRemove(this, EOPPOSITE_FEATURE_BASE - eContainerFeatureID, null, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__TYPE:
 				return getType();
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__SUBSCRIPTION_DURABILITY:
 				return getSubscriptionDurability();
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN:
 				return getBean();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__TYPE:
 				return isSetType();
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__SUBSCRIPTION_DURABILITY:
 				return isSetSubscriptionDurability();
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN:
 				return getBean() != null;
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__TYPE:
 				setType((DestinationType)newValue);
 				return;
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__SUBSCRIPTION_DURABILITY:
 				setSubscriptionDurability((SubscriptionDurabilityKind)newValue);
 				return;
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN:
 				setBean((MessageDriven)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__TYPE:
 				unsetType();
 				return;
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__SUBSCRIPTION_DURABILITY:
 				unsetSubscriptionDurability();
 				return;
 			case EjbPackage.MESSAGE_DRIVEN_DESTINATION__BEAN:
 				setBean((MessageDriven)null);
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (type: ");
 		if (typeESet) result.append(type); else result.append("<unset>");
 		result.append(", subscriptionDurability: ");
 		if (subscriptionDurabilityESet) result.append(subscriptionDurability); else result.append("<unset>");
 		result.append(')');
 		return result.toString();
 	}
 
 }
 
 
 
 
 
