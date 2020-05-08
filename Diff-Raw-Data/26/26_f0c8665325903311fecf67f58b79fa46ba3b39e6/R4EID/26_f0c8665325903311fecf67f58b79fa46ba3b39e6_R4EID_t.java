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
 package org.eclipse.mylyn.reviews.r4e.core.model;
 
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <!-- begin-user-doc --> A representation of the model object '<em><b>R4EID</b></em>'. <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.R4EID#getSequenceID <em>Sequence ID</em>}</li>
  *   <li>{@link org.eclipse.mylyn.reviews.r4e.core.model.R4EID#getUserID <em>User ID</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.mylyn.reviews.r4e.core.model.RModelPackage#getR4EID()
  * @model
  * @generated
  */
 public interface R4EID extends EObject {
 	/**
 	 * Returns the value of the '<em><b>Sequence ID</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Sequence ID</em>' attribute isn't clear, there really should be more of a description
 	 * here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Sequence ID</em>' attribute.
 	 * @see #setSequenceID(int)
 	 * @see org.eclipse.mylyn.reviews.r4e.core.model.RModelPackage#getR4EID_SequenceID()
 	 * @model
 	 * @generated
 	 */
 	int getSequenceID();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.mylyn.reviews.r4e.core.model.R4EID#getSequenceID <em>Sequence ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Sequence ID</em>' attribute.
 	 * @see #getSequenceID()
 	 * @generated
 	 */
 	void setSequenceID(int value);
 
 	/**
 	 * Returns the value of the '<em><b>User ID</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>User ID</em>' attribute isn't clear, there really should be more of a description
 	 * here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>User ID</em>' attribute.
 	 * @see #setUserID(String)
 	 * @see org.eclipse.mylyn.reviews.r4e.core.model.RModelPackage#getR4EID_UserID()
 	 * @model
 	 * @generated
 	 */
 	String getUserID();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.mylyn.reviews.r4e.core.model.R4EID#getUserID <em>User ID</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @param value the new value of the '<em>User ID</em>' attribute.
 	 * @see #getUserID()
 	 * @generated
 	 */
 	void setUserID(String value);
 
 } // R4EID
