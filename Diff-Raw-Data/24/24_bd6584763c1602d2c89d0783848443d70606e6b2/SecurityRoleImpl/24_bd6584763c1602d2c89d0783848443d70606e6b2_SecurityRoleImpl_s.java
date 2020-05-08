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
 package org.eclipse.jst.j2ee.common.internal.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.jst.j2ee.common.CommonPackage;
 import org.eclipse.jst.j2ee.common.Description;
 import org.eclipse.jst.j2ee.common.SecurityRole;
 import org.eclipse.wst.common.frameworks.internal.enablement.nonui.WorkbenchUtil;
 
 /**
  * A semantic grouping of operations that may be performed by a subject.
  * The security-role element contains the declaration of a security role which is used in the security-constraints placed on the web application.
  */
 public class SecurityRoleImpl extends EObjectImpl implements SecurityRole, EObject{
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = null;
 
 	static boolean inEclipse = WorkbenchUtil.workbenchIsRunning();
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 	/**
 	 * The default value of the '{@link #getRoleName() <em>Role Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getRoleName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String ROLE_NAME_EDEFAULT = null;
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected String roleName = ROLE_NAME_EDEFAULT;
 	/**
 	 * The cached value of the '{@link #getDescriptions() <em>Descriptions</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescriptions()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList descriptions = null;
 
 	public SecurityRoleImpl() {
 		super();
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return CommonPackage.eINSTANCE.getSecurityRole();
 	}
 
 public boolean equals(Object o) {
 	if (inEclipse)
 		return super.equals(o);
 		
 	if (o instanceof SecurityRoleImpl) {
 		String r1 = getRoleName();
 		String r2 = ((SecurityRole) o).getRoleName();
 		if ((r1 != null) && (r2 != null)) {
 			if (r1.equals(r2)) {
 				return true;
 			}
 		} else if (r1 == null && r2 == null)
 			return super.equals(o);
 	}
 	return false;
 }
 public int hashCode() {
 	if (inEclipse || getRoleName() == null)
 		return super.hashCode();
 	return getRoleName().hashCode();
 }
 public String toString() {
 	String name = getRoleName() == null ? "<unknown>" : getRoleName(); //$NON-NLS-1$
 	return getClass().getName() + "(" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 }
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, CommonPackage.SECURITY_ROLE__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
	 * @generated This field/method will be replaced during code generation 
	 * The role-name element contains the name of a role. This element must contain a non-empty string.

 	 */
 	public String getRoleName() {
 		return roleName;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setRoleName(String newRoleName) {
 		String oldRoleName = roleName;
 		roleName = newRoleName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, CommonPackage.SECURITY_ROLE__ROLE_NAME, oldRoleName, roleName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList getDescriptions() {
 		if (descriptions == null) {
 			descriptions = new EObjectContainmentEList(Description.class, this, CommonPackage.SECURITY_ROLE__DESCRIPTIONS);
 		}
 		return descriptions;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case CommonPackage.SECURITY_ROLE__DESCRIPTIONS:
 					return ((InternalEList)getDescriptions()).basicRemove(otherEnd, msgs);
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
 	public Object eGet(EStructuralFeature eFeature, boolean resolve) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonPackage.SECURITY_ROLE__DESCRIPTION:
 				return getDescription();
 			case CommonPackage.SECURITY_ROLE__ROLE_NAME:
 				return getRoleName();
 			case CommonPackage.SECURITY_ROLE__DESCRIPTIONS:
 				return getDescriptions();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonPackage.SECURITY_ROLE__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
 			case CommonPackage.SECURITY_ROLE__ROLE_NAME:
 				return ROLE_NAME_EDEFAULT == null ? roleName != null : !ROLE_NAME_EDEFAULT.equals(roleName);
 			case CommonPackage.SECURITY_ROLE__DESCRIPTIONS:
 				return descriptions != null && !descriptions.isEmpty();
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonPackage.SECURITY_ROLE__DESCRIPTION:
 				setDescription((String)newValue);
 				return;
 			case CommonPackage.SECURITY_ROLE__ROLE_NAME:
 				setRoleName((String)newValue);
 				return;
 			case CommonPackage.SECURITY_ROLE__DESCRIPTIONS:
 				getDescriptions().clear();
 				getDescriptions().addAll((Collection)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case CommonPackage.SECURITY_ROLE__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 			case CommonPackage.SECURITY_ROLE__ROLE_NAME:
 				setRoleName(ROLE_NAME_EDEFAULT);
 				return;
 			case CommonPackage.SECURITY_ROLE__DESCRIPTIONS:
 				getDescriptions().clear();
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public String toStringGen() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (description: ");
 		result.append(description);
 		result.append(", roleName: ");
 		result.append(roleName);
 		result.append(')');
 		return result.toString();
 	}
 
 }
 
 
 
 
 
