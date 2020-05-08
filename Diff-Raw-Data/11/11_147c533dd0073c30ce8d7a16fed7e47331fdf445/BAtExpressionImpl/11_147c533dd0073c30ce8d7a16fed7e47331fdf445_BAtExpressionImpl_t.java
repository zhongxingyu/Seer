 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import org.eclipse.b3.backend.core.B3EngineException;
 import org.eclipse.b3.backend.core.B3ImmutableTypeException;
 import org.eclipse.b3.backend.core.LValue;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BAtExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 
 import java.lang.reflect.Type;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.b3.backend.evaluator.BackendHelper;
 
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BAt Expression</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BAtExpressionImpl#getObjExpr <em>Obj Expr</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BAtExpressionImpl#getIndexExpr <em>Index Expr</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class BAtExpressionImpl extends BExpressionImpl implements BAtExpression {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The cached value of the '{@link #getObjExpr() <em>Obj Expr</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getObjExpr()
 	 * @generated
 	 * @ordered
 	 */
 	protected BExpression objExpr;
 
 	/**
 	 * The cached value of the '{@link #getIndexExpr() <em>Index Expr</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getIndexExpr()
 	 * @generated
 	 * @ordered
 	 */
 	protected BExpression indexExpr;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected BAtExpressionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BAT_EXPRESSION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BExpression getObjExpr() {
 		return objExpr;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetObjExpr(BExpression newObjExpr, NotificationChain msgs) {
 		BExpression oldObjExpr = objExpr;
 		objExpr = newObjExpr;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.BAT_EXPRESSION__OBJ_EXPR, oldObjExpr, newObjExpr);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setObjExpr(BExpression newObjExpr) {
 		if (newObjExpr != objExpr) {
 			NotificationChain msgs = null;
 			if (objExpr != null)
 				msgs = ((InternalEObject)objExpr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BAT_EXPRESSION__OBJ_EXPR, null, msgs);
 			if (newObjExpr != null)
 				msgs = ((InternalEObject)newObjExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BAT_EXPRESSION__OBJ_EXPR, null, msgs);
 			msgs = basicSetObjExpr(newObjExpr, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BAT_EXPRESSION__OBJ_EXPR, newObjExpr, newObjExpr));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public BExpression getIndexExpr() {
 		return indexExpr;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetIndexExpr(BExpression newIndexExpr, NotificationChain msgs) {
 		BExpression oldIndexExpr = indexExpr;
 		indexExpr = newIndexExpr;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.BAT_EXPRESSION__INDEX_EXPR, oldIndexExpr, newIndexExpr);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setIndexExpr(BExpression newIndexExpr) {
 		if (newIndexExpr != indexExpr) {
 			NotificationChain msgs = null;
 			if (indexExpr != null)
 				msgs = ((InternalEObject)indexExpr).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BAT_EXPRESSION__INDEX_EXPR, null, msgs);
 			if (newIndexExpr != null)
 				msgs = ((InternalEObject)newIndexExpr).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BAT_EXPRESSION__INDEX_EXPR, null, msgs);
 			msgs = basicSetIndexExpr(newIndexExpr, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BAT_EXPRESSION__INDEX_EXPR, newIndexExpr, newIndexExpr));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.BAT_EXPRESSION__OBJ_EXPR:
 				return basicSetObjExpr(null, msgs);
 			case B3backendPackage.BAT_EXPRESSION__INDEX_EXPR:
 				return basicSetIndexExpr(null, msgs);
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
 			case B3backendPackage.BAT_EXPRESSION__OBJ_EXPR:
 				return getObjExpr();
 			case B3backendPackage.BAT_EXPRESSION__INDEX_EXPR:
 				return getIndexExpr();
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
 			case B3backendPackage.BAT_EXPRESSION__OBJ_EXPR:
 				setObjExpr((BExpression)newValue);
 				return;
 			case B3backendPackage.BAT_EXPRESSION__INDEX_EXPR:
 				setIndexExpr((BExpression)newValue);
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
 			case B3backendPackage.BAT_EXPRESSION__OBJ_EXPR:
 				setObjExpr((BExpression)null);
 				return;
 			case B3backendPackage.BAT_EXPRESSION__INDEX_EXPR:
 				setIndexExpr((BExpression)null);
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
 			case B3backendPackage.BAT_EXPRESSION__OBJ_EXPR:
 				return objExpr != null;
 			case B3backendPackage.BAT_EXPRESSION__INDEX_EXPR:
 				return indexExpr != null;
 		}
 		return super.eIsSet(featureID);
 	}
 	@Override
 	public Object evaluate(BExecutionContext ctx) throws Throwable {
 		Object o = objExpr.evaluate(ctx);
 		Object i = indexExpr.evaluate(ctx);
 		if(o instanceof List<?>) {
 			int index = BackendHelper.intValue(i, indexExpr);
 			return ((List<?>)o).get(index);
 		}
 		if(o instanceof Map<?, ?>) {
 			return ((Map<?,?>)o).get(i);
 		}
 		throw BackendHelper.createException(objExpr, "expression is neither a list or map - [] not applicable");
 	}
	@SuppressWarnings("rawtypes")
 	@Override
 	public LValue getLValue(BExecutionContext ctx) throws Throwable {
 		Object o = objExpr.evaluate(ctx);
 		Object i = indexExpr.evaluate(ctx);
 		if(o instanceof List) {
 			int index = BackendHelper.intValue(i, indexExpr);
 			return new ListLValue((List)o, index, getDeclaredType(ctx));
 		}
 		if(o instanceof Map) {
 			return new MapLValue((Map)o, i, getDeclaredType(ctx));
 		}
 		throw BackendHelper.createException(objExpr, "expression is neither a list or map - [] not applicable");
 	}
 
	@SuppressWarnings("rawtypes")
 	private static class ListLValue implements LValue {
 		List list;
 		int index;
 		Type type;
 		public ListLValue(List list, int index, Type t) {
 			this.list = list;
 			this.index = index;
 			this.type = t;
 		}
 		public Object get() throws B3EngineException {
 			return list.get(index);
 		}
 
 		public boolean isSettable() throws B3EngineException {
 			return list != null && list.size() > index;
 		}
		@SuppressWarnings("unchecked")
 		public Object set(Object value) throws B3EngineException {
 			list.set(index, value);
 			return value;
 		}
 		public Type getDeclaredType() {
 			return type;
 		}
 		public void setDeclaredType(Type t) throws B3EngineException {
 			throw new B3ImmutableTypeException(list, type, t);
 		}
 		public boolean isGetable() throws B3EngineException {
 			return true;
 		}
 	}
	@SuppressWarnings({"rawtypes", "unchecked"})
 	private static class MapLValue implements LValue {
 		Map map;
 		Object key;
 		Type type;
 		public MapLValue(Map map, Object key, Type t) {
 			this.map = map;
 			this.key = key;
 			this.type = t;
 		}
 		public Object get() throws B3EngineException {
 			return map.get(key);
 		}
 
 		public boolean isSettable() throws B3EngineException {
 			return map != null && map.containsKey(key);
 		}
 
 		public Object set(Object value) throws B3EngineException {
 			map.put(key, value);
 			return value;
 		}		
 		public Type getDeclaredType() {
 			return type;
 		}
 		public void setDeclaredType(Type t) throws B3EngineException {
 			throw new B3ImmutableTypeException(map, type, t);
 		}
 		public boolean isGetable() throws B3EngineException {
 			return true;
 		}
 
 	}
 	@Override
 	public Type getDeclaredType(BExecutionContext ctx) throws Throwable {
 		// TODO: Assumes that lhs is a list or map - if so, the value element type is returned
 		// or, if not list, map, or these are raw - then object is returned.
 		// This is probably not enough - should probably get <?> type instead if not known
 		// to be able to give editor/compiler a hint that specification is missing.
 		//
 		return TypeUtils.getElementType(objExpr.getDeclaredType(ctx));
 	
 	}
 } //BAtExpressionImpl
