 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend;
 
 import java.lang.reflect.Type;
 import org.eclipse.emf.common.util.EList;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>BLiteral List Expression</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.BLiteralListExpression#getEntries <em>Entries</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.BLiteralListExpression#getEntryType <em>Entry Type</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage#getBLiteralListExpression()
  * @model
  * @generated
  */
 public interface BLiteralListExpression extends BExpression {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * Returns the value of the '<em><b>Entries</b></em>' containment reference list.
 	 * The list contents are of type {@link org.eclipse.b3.backend.evaluator.b3backend.BExpression}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Entries</em>' containment reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Entries</em>' containment reference list.
 	 * @see org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage#getBLiteralListExpression_Entries()
 	 * @model containment="true"
 	 * @generated
 	 */
 	EList<BExpression> getEntries();
 
 	/**
 	 * Returns the value of the '<em><b>Entry Type</b></em>' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Entry Type</em>' reference isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Entry Type</em>' containment reference.
 	 * @see #setEntryType(Type)
 	 * @see org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage#getBLiteralListExpression_EntryType()
	 * @model type="org.eclipse.b3.backend.evaluator.b3backend.IType" containment="true"
 	 * @generated
 	 */
 	Type getEntryType();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.b3.backend.evaluator.b3backend.BLiteralListExpression#getEntryType <em>Entry Type</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Entry Type</em>' containment reference.
 	 * @see #getEntryType()
 	 * @generated
 	 */
 	void setEntryType(Type value);
 
 } // BLiteralListExpression
