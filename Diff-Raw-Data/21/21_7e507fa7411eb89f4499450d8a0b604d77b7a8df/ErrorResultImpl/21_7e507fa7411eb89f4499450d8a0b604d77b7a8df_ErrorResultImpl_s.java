 package org.eclipse.stem.analysis.impl;
 
 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 
 import org.eclipse.stem.analysis.AnalysisPackage;
 import org.eclipse.stem.analysis.ErrorResult;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Error Result</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.analysis.impl.ErrorResultImpl#getErrorByTimeStep <em>Error By Time Step</em>}</li>
  *   <li>{@link org.eclipse.stem.analysis.impl.ErrorResultImpl#getError <em>Error</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ErrorResultImpl extends EObjectImpl implements ErrorResult {
 	/**
 	 * The cached value of the '{@link #getErrorByTimeStep() <em>Error By Time Step</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getErrorByTimeStep()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Double> errorByTimeStep;
 	/**
 	 * The default value of the '{@link #getError() <em>Error</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getError()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double ERROR_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getError() <em>Error</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getError()
 	 * @generated
 	 * @ordered
 	 */
 	protected double error = ERROR_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected ErrorResultImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return AnalysisPackage.Literals.ERROR_RESULT;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Double> getErrorByTimeStep() {
 		return errorByTimeStep;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setErrorByTimeStep(EList<Double> newErrorByTimeStep) {
 		EList<Double> oldErrorByTimeStep = errorByTimeStep;
 		errorByTimeStep = newErrorByTimeStep;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, AnalysisPackage.ERROR_RESULT__ERROR_BY_TIME_STEP, oldErrorByTimeStep, errorByTimeStep));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getError() {
 		return error;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setError(double newError) {
 		double oldError = error;
 		error = newError;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, AnalysisPackage.ERROR_RESULT__ERROR, oldError, error));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case AnalysisPackage.ERROR_RESULT__ERROR_BY_TIME_STEP:
 				return getErrorByTimeStep();
 			case AnalysisPackage.ERROR_RESULT__ERROR:
 				return new Double(getError());
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
 			case AnalysisPackage.ERROR_RESULT__ERROR_BY_TIME_STEP:
 				setErrorByTimeStep((EList<Double>)newValue);
 				return;
 			case AnalysisPackage.ERROR_RESULT__ERROR:
 				setError(((Double)newValue).doubleValue());
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
 			case AnalysisPackage.ERROR_RESULT__ERROR_BY_TIME_STEP:
 				setErrorByTimeStep((EList<Double>)null);
 				return;
 			case AnalysisPackage.ERROR_RESULT__ERROR:
 				setError(ERROR_EDEFAULT);
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
 			case AnalysisPackage.ERROR_RESULT__ERROR_BY_TIME_STEP:
 				return errorByTimeStep != null;
 			case AnalysisPackage.ERROR_RESULT__ERROR:
 				return error != ERROR_EDEFAULT;
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
 		result.append(" (errorByTimeStep: ");
 		result.append(errorByTimeStep);
 		result.append(", error: ");
 		result.append(error);
 		result.append(')');
 		return result.toString();
 	}
 
 } //ErrorResultImpl
