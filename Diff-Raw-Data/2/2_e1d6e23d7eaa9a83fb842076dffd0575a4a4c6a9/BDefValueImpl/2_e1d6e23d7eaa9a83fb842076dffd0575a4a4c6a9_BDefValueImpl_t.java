 /**
  * Copyright (c) 2009, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  * 
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 
 import org.eclipse.b3.backend.core.LValue;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BDefValue;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BDef Value</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BDefValueImpl#getName <em>Name</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BDefValueImpl#isFinal <em>Final</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BDefValueImpl#isImmutable <em>Immutable</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BDefValueImpl#getValueExpr <em>Value Expr</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BDefValueImpl#getType <em>Type</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class BDefValueImpl extends BExpressionImpl implements BDefValue {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String name = NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isFinal() <em>Final</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isFinal()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean FINAL_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isFinal() <em>Final</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isFinal()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean final_ = FINAL_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isImmutable() <em>Immutable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isImmutable()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean IMMUTABLE_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isImmutable() <em>Immutable</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isImmutable()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean immutable = IMMUTABLE_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getValueExpr() <em>Value Expr</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValueExpr()
 	 * @generated
 	 * @ordered
 	 */
 	protected BExpression valueExpr;
 
 	/**
 	 * The cached value of the '{@link #getType() <em>Type</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type type;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected BDefValueImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BDEF_VALUE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setName(String newName) {
 		String oldName = name;
 		name = newName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BDEF_VALUE__NAME, oldName, name));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isFinal() {
 		return final_;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setFinal(boolean newFinal) {
 		boolean oldFinal = final_;
 		final_ = newFinal;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BDEF_VALUE__FINAL, oldFinal, final_));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isImmutable() {
 		return immutable;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setImmutable(boolean newImmutable) {
 		boolean oldImmutable = immutable;
 		immutable = newImmutable;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BDEF_VALUE__IMMUTABLE, oldImmutable, immutable));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BExpression getValueExpr() {
 		return valueExpr;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetValueExpr(BExpression newValueExpr, NotificationChain msgs) {
 		BExpression oldValueExpr = valueExpr;
 		valueExpr = newValueExpr;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.BDEF_VALUE__VALUE_EXPR, oldValueExpr, newValueExpr);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValueExpr(BExpression newValueExpr) {
 		if (newValueExpr != valueExpr) {
 			NotificationChain msgs = null;
 			if (valueExpr != null)
 				msgs = ((InternalEObject)valueExpr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BDEF_VALUE__VALUE_EXPR, null, msgs);
 			if (newValueExpr != null)
 				msgs = ((InternalEObject)newValueExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BDEF_VALUE__VALUE_EXPR, null, msgs);
 			msgs = basicSetValueExpr(newValueExpr, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BDEF_VALUE__VALUE_EXPR, newValueExpr, newValueExpr));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type getType() {
 		return type;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetType(Type newType, NotificationChain msgs) {
 		Type oldType = type;
 		type = newType;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.BDEF_VALUE__TYPE, oldType, newType);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setType(Type newType) {
 		if (newType != type) {
 			NotificationChain msgs = null;
 			if (type != null && type instanceof EObject)
 				msgs = ((InternalEObject)type).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BDEF_VALUE__TYPE, null, msgs);
 			if (newType != null && newType instanceof EObject)
 				msgs = ((InternalEObject)newType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BDEF_VALUE__TYPE, null, msgs);
 			msgs = basicSetType(newType, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BDEF_VALUE__TYPE, newType, newType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.BDEF_VALUE__VALUE_EXPR:
 				return basicSetValueExpr(null, msgs);
 			case B3backendPackage.BDEF_VALUE__TYPE:
 				return basicSetType(null, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case B3backendPackage.BDEF_VALUE__NAME:
 				return getName();
 			case B3backendPackage.BDEF_VALUE__FINAL:
 				return isFinal();
 			case B3backendPackage.BDEF_VALUE__IMMUTABLE:
 				return isImmutable();
 			case B3backendPackage.BDEF_VALUE__VALUE_EXPR:
 				return getValueExpr();
 			case B3backendPackage.BDEF_VALUE__TYPE:
 				return getType();
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
 			case B3backendPackage.BDEF_VALUE__NAME:
 				setName((String)newValue);
 				return;
 			case B3backendPackage.BDEF_VALUE__FINAL:
 				setFinal((Boolean)newValue);
 				return;
 			case B3backendPackage.BDEF_VALUE__IMMUTABLE:
 				setImmutable((Boolean)newValue);
 				return;
 			case B3backendPackage.BDEF_VALUE__VALUE_EXPR:
 				setValueExpr((BExpression)newValue);
 				return;
 			case B3backendPackage.BDEF_VALUE__TYPE:
 				setType((Type)newValue);
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
 			case B3backendPackage.BDEF_VALUE__NAME:
 				setName(NAME_EDEFAULT);
 				return;
 			case B3backendPackage.BDEF_VALUE__FINAL:
 				setFinal(FINAL_EDEFAULT);
 				return;
 			case B3backendPackage.BDEF_VALUE__IMMUTABLE:
 				setImmutable(IMMUTABLE_EDEFAULT);
 				return;
 			case B3backendPackage.BDEF_VALUE__VALUE_EXPR:
 				setValueExpr((BExpression)null);
 				return;
 			case B3backendPackage.BDEF_VALUE__TYPE:
 				setType((Type)null);
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
 			case B3backendPackage.BDEF_VALUE__NAME:
 				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
 			case B3backendPackage.BDEF_VALUE__FINAL:
 				return final_ != FINAL_EDEFAULT;
 			case B3backendPackage.BDEF_VALUE__IMMUTABLE:
 				return immutable != IMMUTABLE_EDEFAULT;
 			case B3backendPackage.BDEF_VALUE__VALUE_EXPR:
 				return valueExpr != null;
 			case B3backendPackage.BDEF_VALUE__TYPE:
 				return type != null;
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
 		result.append(" (name: ");
 		result.append(name);
 		result.append(", final: ");
 		result.append(final_);
 		result.append(", immutable: ");
 		result.append(immutable);
 		result.append(')');
 		return result.toString();
 	}
 	@Override
 	public Object evaluate(BExecutionContext ctx) throws Throwable {
 		Object result = null;
 		if(immutable) {
 			if(final_)
 				ctx.defineFinalValue(name, result = (valueExpr == null ? null : valueExpr.evaluate(ctx)), type);
 			else
 				ctx.defineValue(name, result = (valueExpr == null ? null : valueExpr.evaluate(ctx)), type);
 		} else {
 			if(final_)
 				ctx.defineFinalVariableValue(name, result = (valueExpr == null ? null : valueExpr.evaluate(ctx)), type);
 			else
 				ctx.defineVariableValue(name, result = (valueExpr == null ? null : valueExpr.evaluate(ctx)), type);
 		}
 		return result;
 	}
 	@Override
 	public LValue getLValue(BExecutionContext ctx) throws Throwable {
		// must define it first :)
		evaluate(ctx);
 		return ctx.getLValue(name);
 	}
 } //BDefValueImpl
