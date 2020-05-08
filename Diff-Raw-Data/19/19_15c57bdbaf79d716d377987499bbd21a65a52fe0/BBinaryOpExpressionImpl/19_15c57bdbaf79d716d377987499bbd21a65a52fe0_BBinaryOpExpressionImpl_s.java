 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BBinaryOpExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;

 import org.eclipse.emf.common.notify.Notification;

 import org.eclipse.emf.ecore.EClass;

 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BBinary Op Expression</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  * <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BBinaryOpExpressionImpl#getFunctionName <em>Function Name</em>}</li>
  * </ul>
  * </p>
  * 
  * @generated
  */
 public class BBinaryOpExpressionImpl extends BBinaryExpressionImpl implements BBinaryOpExpression {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The default value of the '{@link #getFunctionName() <em>Function Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getFunctionName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String FUNCTION_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getFunctionName() <em>Function Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @see #getFunctionName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String functionName = FUNCTION_NAME_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected BBinaryOpExpressionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch(featureID) {
 			case B3backendPackage.BBINARY_OP_EXPRESSION__FUNCTION_NAME:
 				return getFunctionName();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch(featureID) {
 			case B3backendPackage.BBINARY_OP_EXPRESSION__FUNCTION_NAME:
 				return FUNCTION_NAME_EDEFAULT == null
 						? functionName != null
 						: !FUNCTION_NAME_EDEFAULT.equals(functionName);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch(featureID) {
 			case B3backendPackage.BBINARY_OP_EXPRESSION__FUNCTION_NAME:
 				setFunctionName((String) newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BBINARY_OP_EXPRESSION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch(featureID) {
 			case B3backendPackage.BBINARY_OP_EXPRESSION__FUNCTION_NAME:
 				setFunctionName(FUNCTION_NAME_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	@Override
 	public Object evaluate(BExecutionContext ctx) throws Throwable {
		return ctx.callFunction(
			functionName, new Object[] { leftExpr.evaluate(ctx), rightExpr.evaluate(ctx) },
			new Type[] { leftExpr.getDeclaredType(ctx), rightExpr.getDeclaredType(ctx) });
 	}
 
 	@Override
 	public Type getDeclaredType(BExecutionContext ctx) throws Throwable {
 		return ctx.getDeclaredFunctionType(
 			functionName, new Type[] { leftExpr.getDeclaredType(ctx), rightExpr.getDeclaredType(ctx) });
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public String getFunctionName() {
 		return functionName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public void setFunctionName(String newFunctionName) {
 		String oldFunctionName = functionName;
 		functionName = newFunctionName;
 		if(eNotificationRequired())
 			eNotify(new ENotificationImpl(
 				this, Notification.SET, B3backendPackage.BBINARY_OP_EXPRESSION__FUNCTION_NAME, oldFunctionName,
 				functionName));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if(eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (functionName: ");
 		result.append(functionName);
 		result.append(')');
 		return result.toString();
 	}
 } // BBinaryOpExpressionImpl
