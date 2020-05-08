 /*******************************************************************************
  * Copyright (c) 2001, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.webapplication.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.InternalEList;
 import org.eclipse.jst.j2ee.internal.webapplication.WebapplicationPackage;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WelcomeFile;
 import org.eclipse.jst.j2ee.webapplication.WelcomeFileList;
 
 /**
  * The welcome-file-list contains an ordered list of welcome files elements.
  */
 public class WelcomeFileListImpl extends EObjectImpl implements WelcomeFileList, EObject {
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	protected EList file = null;
 	public WelcomeFileListImpl() {
 		super();
 	}
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return WebapplicationPackage.eINSTANCE.getWelcomeFileList();
 	}
 
 /**
  * addFileNamed method comment.
  */
 public void addFileNamed(java.lang.String name) {
 	WelcomeFile lfile = ((WebapplicationPackage)EPackage.Registry.INSTANCE.getEPackage(WebapplicationPackage.eNS_URI)).getWebapplicationFactory().createWelcomeFile();
 	lfile.setWelcomeFile(name);
 	getFile().add(lfile);
 }
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 */
 	public WebApp getWebApp() {
 		if (eContainerFeatureID != WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP) return null;
 		return (WebApp)eContainer;
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void setWebApp(WebApp newWebApp) {
 		if (newWebApp != eContainer || (eContainerFeatureID != WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP && newWebApp != null)) {
 			if (EcoreUtil.isAncestor(this, newWebApp))
 				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());//$NON-NLS-1$
 			NotificationChain msgs = null;
 			if (eContainer != null)
 				msgs = eBasicRemoveFromContainer(msgs);
 			if (newWebApp != null)
 				msgs = ((InternalEObject)newWebApp).eInverseAdd(this, WebapplicationPackage.WEB_APP__FILE_LIST, WebApp.class, msgs);
 			msgs = eBasicSetContainer((InternalEObject)newWebApp, WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP, newWebApp, newWebApp));
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation 
 	 */
 	public EList getFile() {
 		if (file == null) {
 			file = new EObjectContainmentWithInverseEList(WelcomeFile.class, this, WebapplicationPackage.WELCOME_FILE_LIST__FILE, WebapplicationPackage.WELCOME_FILE__FILE_LIST);
 		}
 		return file;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, Class baseClass, NotificationChain msgs) {
 		if (featureID >= 0) {
 			switch (eDerivedStructuralFeatureID(featureID, baseClass)) {
 				case WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP:
 					if (eContainer != null)
 						msgs = eBasicRemoveFromContainer(msgs);
 					return eBasicSetContainer(otherEnd, WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP, msgs);
 				case WebapplicationPackage.WELCOME_FILE_LIST__FILE:
 					return ((InternalEList)getFile()).basicAdd(otherEnd, msgs);
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
 				case WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP:
 					return eBasicSetContainer(null, WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP, msgs);
 				case WebapplicationPackage.WELCOME_FILE_LIST__FILE:
 					return ((InternalEList)getFile()).basicRemove(otherEnd, msgs);
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
 				case WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP:
 					return eContainer.eInverseRemove(this, WebapplicationPackage.WEB_APP__FILE_LIST, WebApp.class, msgs);
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
 			case WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP:
 				return getWebApp();
 			case WebapplicationPackage.WELCOME_FILE_LIST__FILE:
 				return getFile();
 		}
 		return eDynamicGet(eFeature, resolve);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public boolean eIsSet(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP:
 				return getWebApp() != null;
 			case WebapplicationPackage.WELCOME_FILE_LIST__FILE:
 				return file != null && !file.isEmpty();
 		}
 		return eDynamicIsSet(eFeature);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eSet(EStructuralFeature eFeature, Object newValue) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP:
 				setWebApp((WebApp)newValue);
 				return;
 			case WebapplicationPackage.WELCOME_FILE_LIST__FILE:
 				getFile().clear();
				getFile().addAll((Collection)newValue);
 				return;
 		}
 		eDynamicSet(eFeature, newValue);
 	}
 
 	/**
 	 * @generated This field/method will be replaced during code generation.
 	 */
 	public void eUnset(EStructuralFeature eFeature) {
 		switch (eDerivedStructuralFeatureID(eFeature)) {
 			case WebapplicationPackage.WELCOME_FILE_LIST__WEB_APP:
 				setWebApp((WebApp)null);
 				return;
 			case WebapplicationPackage.WELCOME_FILE_LIST__FILE:
 				getFile().clear();
 				return;
 		}
 		eDynamicUnset(eFeature);
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
