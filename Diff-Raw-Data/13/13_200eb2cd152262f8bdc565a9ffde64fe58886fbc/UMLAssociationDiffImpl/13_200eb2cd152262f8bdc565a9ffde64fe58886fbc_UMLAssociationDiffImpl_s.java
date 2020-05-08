 /**
  * 
  *  Copyright (c) 2006, 2007 Obeo.
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *  
  *  Contributors:
  *      Obeo - initial API and implementation
  * 
  *
 * $Id: UMLAssociationDiffImpl.java,v 1.1 2007/08/29 07:20:03 cbrun Exp $
  */
 package org.eclipse.emf.compare.examples.diff.extension.metamodel.diff_extension.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.compare.diff.metamodel.impl.AbstractDiffExtensionImpl;
 
 import org.eclipse.emf.compare.examples.diff.extension.metamodel.diff_extension.DiffExtensionPackage;
 import org.eclipse.emf.compare.examples.diff.extension.metamodel.diff_extension.UMLAssociationDiff;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>UML Association Diff</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.emf.compare.examples.diff.extension.metamodel.diff_extension.impl.UMLAssociationDiffImpl#getProperties <em>Properties</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.examples.diff.extension.metamodel.diff_extension.impl.UMLAssociationDiffImpl#isIsNavigable <em>Is Navigable</em>}</li>
  *   <li>{@link org.eclipse.emf.compare.examples.diff.extension.metamodel.diff_extension.impl.UMLAssociationDiffImpl#getContainerPackage <em>Container Package</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public abstract class UMLAssociationDiffImpl extends AbstractDiffExtensionImpl implements UMLAssociationDiff {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "\n Copyright (c) 2006, 2007 Obeo.\n All rights reserved. This program and the accompanying materials\n are made available under the terms of the Eclipse Public License v1.0\n which accompanies this distribution, and is available at\n http://www.eclipse.org/legal/epl-v10.html\n \n Contributors:\n     Obeo - initial API and implementation\n";
 
 	/**
 	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getProperties()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<EObject> properties;
 
 	/**
 	 * The default value of the '{@link #isIsNavigable() <em>Is Navigable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isIsNavigable()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IS_NAVIGABLE_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isIsNavigable() <em>Is Navigable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isIsNavigable()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean isNavigable = IS_NAVIGABLE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getContainerPackage() <em>Container Package</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getContainerPackage()
 	 * @generated
 	 * @ordered
 	 */
 	protected EObject containerPackage;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected UMLAssociationDiffImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return DiffExtensionPackage.Literals.UML_ASSOCIATION_DIFF;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<EObject> getProperties() {
 		if (properties == null) {
 			properties = new EObjectResolvingEList<EObject>(EObject.class, this, DiffExtensionPackage.UML_ASSOCIATION_DIFF__PROPERTIES);
 		}
 		return properties;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isIsNavigable() {
 		return isNavigable;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIsNavigable(boolean newIsNavigable) {
 		boolean oldIsNavigable = isNavigable;
 		isNavigable = newIsNavigable;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DiffExtensionPackage.UML_ASSOCIATION_DIFF__IS_NAVIGABLE, oldIsNavigable, isNavigable));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EObject getContainerPackage() {
 		if (containerPackage != null && containerPackage.eIsProxy()) {
 			InternalEObject oldContainerPackage = (InternalEObject)containerPackage;
 			containerPackage = eResolveProxy(oldContainerPackage);
 			if (containerPackage != oldContainerPackage) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DiffExtensionPackage.UML_ASSOCIATION_DIFF__CONTAINER_PACKAGE, oldContainerPackage, containerPackage));
 			}
 		}
 		return containerPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EObject basicGetContainerPackage() {
 		return containerPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setContainerPackage(EObject newContainerPackage) {
 		EObject oldContainerPackage = containerPackage;
 		containerPackage = newContainerPackage;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, DiffExtensionPackage.UML_ASSOCIATION_DIFF__CONTAINER_PACKAGE, oldContainerPackage, containerPackage));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__PROPERTIES:
 				return getProperties();
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__IS_NAVIGABLE:
 				return isIsNavigable() ? Boolean.TRUE : Boolean.FALSE;
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__CONTAINER_PACKAGE:
 				if (resolve) return getContainerPackage();
 				return basicGetContainerPackage();
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
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__PROPERTIES:
 				getProperties().clear();
 				getProperties().addAll((Collection<? extends EObject>)newValue);
 				return;
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__IS_NAVIGABLE:
 				setIsNavigable(((Boolean)newValue).booleanValue());
 				return;
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__CONTAINER_PACKAGE:
 				setContainerPackage((EObject)newValue);
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
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__PROPERTIES:
 				getProperties().clear();
 				return;
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__IS_NAVIGABLE:
 				setIsNavigable(IS_NAVIGABLE_EDEFAULT);
 				return;
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__CONTAINER_PACKAGE:
 				setContainerPackage((EObject)null);
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
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__PROPERTIES:
 				return properties != null && !properties.isEmpty();
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__IS_NAVIGABLE:
 				return isNavigable != IS_NAVIGABLE_EDEFAULT;
 			case DiffExtensionPackage.UML_ASSOCIATION_DIFF__CONTAINER_PACKAGE:
 				return containerPackage != null;
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
 		result.append(" (isNavigable: ");
 		result.append(isNavigable);
 		result.append(')');
 		return result.toString();
 	}
 
 } //UMLAssociationDiffImpl
