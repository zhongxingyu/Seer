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
 package org.eclipse.jst.j2ee.webservice.wsclient.internal.impl;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jst.j2ee.common.internal.impl.J2EEEObjectImpl;
 import org.eclipse.jst.j2ee.webservice.wsclient.PortComponentRef;
 import org.eclipse.jst.j2ee.webservice.wsclient.Webservice_clientPackage;
 
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Port Component Ref</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.jst.j2ee.webservice.wsclient.internal.impl.PortComponentRefImpl#getPortComponentLink <em>Port Component Link</em>}</li>
  *   <li>{@link org.eclipse.jst.j2ee.webservice.wsclient.internal.impl.PortComponentRefImpl#getServiceEndpointInterface <em>Service Endpoint Interface</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class PortComponentRefImpl extends J2EEEObjectImpl implements PortComponentRef {
 	/**
 	 * The default value of the '{@link #getPortComponentLink() <em>Port Component Link</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPortComponentLink()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String PORT_COMPONENT_LINK_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getPortComponentLink() <em>Port Component Link</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getPortComponentLink()
 	 * @generated
 	 * @ordered
 	 */
 	protected String portComponentLink = PORT_COMPONENT_LINK_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getServiceEndpointInterface() <em>Service Endpoint Interface</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getServiceEndpointInterface()
 	 * @generated
 	 * @ordered
 	 */
 	protected JavaClass serviceEndpointInterface = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected PortComponentRefImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EClass eStaticClass() {
 		return Webservice_clientPackage.Literals.PORT_COMPONENT_REF;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getPortComponentLink() {
 		return portComponentLink;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setPortComponentLink(String newPortComponentLink) {
 		String oldPortComponentLink = portComponentLink;
 		portComponentLink = newPortComponentLink;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, Webservice_clientPackage.PORT_COMPONENT_REF__PORT_COMPONENT_LINK, oldPortComponentLink, portComponentLink));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
	public JavaClass getServiceEndpointInterface() {
 		if (serviceEndpointInterface != null && serviceEndpointInterface.eIsProxy()) {
 			InternalEObject oldServiceEndpointInterface = (InternalEObject)serviceEndpointInterface;
 			serviceEndpointInterface = (JavaClass)eResolveProxy(oldServiceEndpointInterface);
 			if (serviceEndpointInterface != oldServiceEndpointInterface) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, Webservice_clientPackage.PORT_COMPONENT_REF__SERVICE_ENDPOINT_INTERFACE, oldServiceEndpointInterface, serviceEndpointInterface));
 			}
 		}
 		return serviceEndpointInterface;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public JavaClass basicGetServiceEndpointInterface() {
 		return serviceEndpointInterface;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setServiceEndpointInterface(JavaClass newServiceEndpointInterface) {
 		JavaClass oldServiceEndpointInterface = serviceEndpointInterface;
 		serviceEndpointInterface = newServiceEndpointInterface;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, Webservice_clientPackage.PORT_COMPONENT_REF__SERVICE_ENDPOINT_INTERFACE, oldServiceEndpointInterface, serviceEndpointInterface));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case Webservice_clientPackage.PORT_COMPONENT_REF__PORT_COMPONENT_LINK:
 				return getPortComponentLink();
 			case Webservice_clientPackage.PORT_COMPONENT_REF__SERVICE_ENDPOINT_INTERFACE:
 				if (resolve) return getServiceEndpointInterface();
 				return basicGetServiceEndpointInterface();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case Webservice_clientPackage.PORT_COMPONENT_REF__PORT_COMPONENT_LINK:
 				setPortComponentLink((String)newValue);
 				return;
 			case Webservice_clientPackage.PORT_COMPONENT_REF__SERVICE_ENDPOINT_INTERFACE:
 				setServiceEndpointInterface((JavaClass)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case Webservice_clientPackage.PORT_COMPONENT_REF__PORT_COMPONENT_LINK:
 				setPortComponentLink(PORT_COMPONENT_LINK_EDEFAULT);
 				return;
 			case Webservice_clientPackage.PORT_COMPONENT_REF__SERVICE_ENDPOINT_INTERFACE:
 				setServiceEndpointInterface((JavaClass)null);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case Webservice_clientPackage.PORT_COMPONENT_REF__PORT_COMPONENT_LINK:
 				return PORT_COMPONENT_LINK_EDEFAULT == null ? portComponentLink != null : !PORT_COMPONENT_LINK_EDEFAULT.equals(portComponentLink);
 			case Webservice_clientPackage.PORT_COMPONENT_REF__SERVICE_ENDPOINT_INTERFACE:
 				return serviceEndpointInterface != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (portComponentLink: ");
 		result.append(portComponentLink);
 		result.append(')');
 		return result.toString();
 	}
 
 } //PortComponentRefImpl
