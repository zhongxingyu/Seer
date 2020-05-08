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
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.impl.EObjectImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
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
  *   <li>{@link org.eclipse.stem.analysis.impl.ErrorResultImpl#getReferenceByTime <em>Reference By Time</em>}</li>
  *   <li>{@link org.eclipse.stem.analysis.impl.ErrorResultImpl#getModelByTime <em>Model By Time</em>}</li>
  *   <li>{@link org.eclipse.stem.analysis.impl.ErrorResultImpl#getValidationError <em>Validation Error</em>}</li>
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
 	 * The cached value of the '{@link #getReferenceByTime() <em>Reference By Time</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getReferenceByTime()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Double> referenceByTime;
 	/**
 	 * The cached value of the '{@link #getModelByTime() <em>Model By Time</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getModelByTime()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Double> modelByTime;
 
 	/**
 	 * The default value of the '{@link #getValidationError() <em>Validation Error</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationError()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double VALIDATION_ERROR_EDEFAULT = 0.0;
 	/**
 	 * The cached value of the '{@link #getValidationError() <em>Validation Error</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getValidationError()
 	 * @generated
 	 * @ordered
 	 */
 	protected double validationError = VALIDATION_ERROR_EDEFAULT;
 
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
 	public EList<Double> getReferenceByTime() {
 		return referenceByTime;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setReferenceByTime(EList<Double> newReferenceByTime) {
 		EList<Double> oldReferenceByTime = referenceByTime;
 		referenceByTime = newReferenceByTime;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, AnalysisPackage.ERROR_RESULT__REFERENCE_BY_TIME, oldReferenceByTime, referenceByTime));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Double> getModelByTime() {
 		return modelByTime;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setModelByTime(EList<Double> newModelByTime) {
 		EList<Double> oldModelByTime = modelByTime;
 		modelByTime = newModelByTime;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, AnalysisPackage.ERROR_RESULT__MODEL_BY_TIME, oldModelByTime, modelByTime));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getValidationError() {
 		return validationError;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setValidationError(double newValidationError) {
 		double oldValidationError = validationError;
 		validationError = newValidationError;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, AnalysisPackage.ERROR_RESULT__VALIDATION_ERROR, oldValidationError, validationError));
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
 				return getError();
 			case AnalysisPackage.ERROR_RESULT__REFERENCE_BY_TIME:
 				return getReferenceByTime();
 			case AnalysisPackage.ERROR_RESULT__MODEL_BY_TIME:
 				return getModelByTime();
 			case AnalysisPackage.ERROR_RESULT__VALIDATION_ERROR:
 				return getValidationError();
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
 				setError((Double)newValue);
 				return;
 			case AnalysisPackage.ERROR_RESULT__REFERENCE_BY_TIME:
 				setReferenceByTime((EList<Double>)newValue);
 				return;
 			case AnalysisPackage.ERROR_RESULT__MODEL_BY_TIME:
 				setModelByTime((EList<Double>)newValue);
 				return;
 			case AnalysisPackage.ERROR_RESULT__VALIDATION_ERROR:
 				setValidationError((Double)newValue);
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
 			case AnalysisPackage.ERROR_RESULT__REFERENCE_BY_TIME:
 				setReferenceByTime((EList<Double>)null);
 				return;
 			case AnalysisPackage.ERROR_RESULT__MODEL_BY_TIME:
 				setModelByTime((EList<Double>)null);
 				return;
 			case AnalysisPackage.ERROR_RESULT__VALIDATION_ERROR:
 				setValidationError(VALIDATION_ERROR_EDEFAULT);
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
 			case AnalysisPackage.ERROR_RESULT__REFERENCE_BY_TIME:
 				return referenceByTime != null;
 			case AnalysisPackage.ERROR_RESULT__MODEL_BY_TIME:
 				return modelByTime != null;
 			case AnalysisPackage.ERROR_RESULT__VALIDATION_ERROR:
 				return validationError != VALIDATION_ERROR_EDEFAULT;
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
 		result.append(", referenceByTime: ");
 		result.append(referenceByTime);
 		result.append(", modelByTime: ");
 		result.append(modelByTime);
 		result.append(", validationError: ");
 		result.append(validationError);
 		result.append(')');
 		return result.toString();
 	}
 
 	public ErrorResult copy() {
 		ErrorResultImpl res = new ErrorResultImpl();
 		res.setError(this.getError());
		res.setValidationError(this.getValidationError());
 		EList<Double>newlist = new BasicEList<Double>();
 		for(double d:this.getErrorByTimeStep())newlist.add(d);
 		res.setErrorByTimeStep(newlist);
 		return res;
 	}
 
 } //ErrorResultImpl
