 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.b3.backend.evaluator.b3backend.impl;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.b3.backend.evaluator.BackendHelper;
 import org.eclipse.b3.backend.evaluator.b3backend.B3ParameterizedType;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendFactory;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendPackage;
 import org.eclipse.b3.backend.evaluator.b3backend.BExecutionContext;
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
 import org.eclipse.b3.backend.evaluator.b3backend.BLiteralListExpression;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>BLiteral List Expression</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BLiteralListExpressionImpl#getEntries <em>Entries</em>}</li>
  *   <li>{@link org.eclipse.b3.backend.evaluator.b3backend.impl.BLiteralListExpressionImpl#getEntryType <em>Entry Type</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class BLiteralListExpressionImpl extends BExpressionImpl implements BLiteralListExpression {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Copyright (c) 2009, Cloudsmith Inc and others.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n\rContributors:\n- Cloudsmith Inc - initial API and implementation.\r";
 
 	/**
 	 * The cached value of the '{@link #getEntries() <em>Entries</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEntries()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<BExpression> entries;
 
 	/**
 	 * The cached value of the '{@link #getEntryType() <em>Entry Type</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getEntryType()
 	 * @generated
 	 * @ordered
 	 */
 	protected Type entryType;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected BLiteralListExpressionImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return B3backendPackage.Literals.BLITERAL_LIST_EXPRESSION;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<BExpression> getEntries() {
 		if (entries == null) {
 			entries = new EObjectContainmentEList<BExpression>(BExpression.class, this, B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRIES);
 		}
 		return entries;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Type getEntryType() {
 		return entryType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetEntryType(Type newEntryType, NotificationChain msgs) {
 		Type oldEntryType = entryType;
 		entryType = newEntryType;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE, oldEntryType, newEntryType);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * entryType may not be an EObject. If it is not, then no notification is generated on change.
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setEntryType(Type newEntryType) {
 		if (newEntryType != entryType) {
 			NotificationChain msgs = null;
 			if (entryType != null && entryType instanceof EObject)
 				msgs = ((InternalEObject)entryType).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE, null, msgs);
 			if (newEntryType != null && newEntryType instanceof EObject)
 				msgs = ((InternalEObject)newEntryType).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE, null, msgs);
 			msgs = basicSetEntryType(newEntryType, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE, newEntryType, newEntryType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRIES:
 				return ((InternalEList<?>)getEntries()).basicRemove(otherEnd, msgs);
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE:
 				return basicSetEntryType(null, msgs);
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
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRIES:
 				return getEntries();
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE:
 				return getEntryType();
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
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRIES:
 				getEntries().clear();
 				getEntries().addAll((Collection<? extends BExpression>)newValue);
 				return;
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE:
 				setEntryType((Type)newValue);
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
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRIES:
 				getEntries().clear();
 				return;
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE:
 				setEntryType((Type)null);
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
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRIES:
 				return entries != null && !entries.isEmpty();
 			case B3backendPackage.BLITERAL_LIST_EXPRESSION__ENTRY_TYPE:
 				return entryType != null;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	@Override
 	public Object evaluate(BExecutionContext ctx) throws Throwable {
 		Type t = entryType == null ? Object.class : entryType;
 		List<Object> list = new ArrayList<Object>(getEntries().size());
 		int counter = 0;
 		for(BExpression expr : entries) {
 			Object result = expr.evaluate(ctx);
			if(!TypeUtils.isAssignableFrom(t, result.getClass()))
 				throw BackendHelper.createException(expr, 
 						"List creation error for index {0}. "
 						+"A List<{1}>, does not accept an instance of type {2}.",
 						new Object[] { counter, t, result.getClass()});
 			list.add(result);
 			counter++;
 			}
 		return list;
 	}
 	/**
 	 * Returns the declared entry, type or Object if not specified.
 	 * TODO: the common super type could be computed instead.
 	 */
 	@Override
 	public Type getDeclaredType(BExecutionContext ctx) throws Throwable {
 		Type eType = entryType == null ? Object.class : entryType;
 		B3ParameterizedType pt = B3backendFactory.eINSTANCE.createB3ParameterizedType();
 		pt.setRawType(List.class);
 		pt.getActualArgumentsList().add(eType);
 		return pt;
 	}
 } //BLiteralListExpressionImpl
