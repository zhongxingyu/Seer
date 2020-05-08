 package org.eclipse.stem.definitions.labels.impl;
 
 /*******************************************************************************
  * Copyright (c) 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.stem.core.graph.impl.LabelValueImpl;
 import org.eclipse.stem.definitions.labels.AreaLabelValue;
 import org.eclipse.stem.definitions.labels.LabelsPackage;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Area Label Value</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.definitions.labels.impl.AreaLabelValueImpl#getArea <em>Area</em>}</li>
  *   <li>{@link org.eclipse.stem.definitions.labels.impl.AreaLabelValueImpl#getAverageExtent <em>Average Extent</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 @SuppressWarnings("boxing")
 public class AreaLabelValueImpl extends LabelValueImpl implements
 		AreaLabelValue {
 	/**
 	 * The default value of the '{@link #getArea() <em>Area</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getArea()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double AREA_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getArea() <em>Area</em>}' attribute.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getArea()
 	 * @generated
 	 * @ordered
 	 */
 	protected double area = AREA_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAverageExtent() <em>Average Extent</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAverageExtent()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final double AVERAGE_EXTENT_EDEFAULT = 0.0;
 
 	/**
 	 * The cached value of the '{@link #getAverageExtent() <em>Average Extent</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAverageExtent()
 	 * @generated
 	 * @ordered
 	 */
 	protected double averageExtent = AVERAGE_EXTENT_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected AreaLabelValueImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return LabelsPackage.Literals.AREA_LABEL_VALUE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getArea() {
 		return area;
 	}
 
 	/**
 	 * @see org.eclipse.stem.core.graph.impl.LabelValueImpl#reset()
 	 */
 	@Override
 	public void reset() {
 		// We don't reset
 	} // reset
 
 	/**
 	 * @see org.eclipse.stem.core.common.SanityChecker#sane()
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = true;
 
 		// The area should be greater than zero
 		retValue = retValue && getArea() > 0;
 		//assert retValue;
 
 		return retValue;
 	} // sane
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param newArea
 	 * 
 	 * <!-- end-user-doc -->
	 * @generated NOT
 	 */
 	public void setArea(double newArea) {
 		double oldArea = area;
 		area = newArea;
 		if(area >= 1.0) {
 			setAverageExtent(Math.sqrt(area));
 		} else {
 			setAverageExtent(1.0);
 		}
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, LabelsPackage.AREA_LABEL_VALUE__AREA, oldArea, area));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public double getAverageExtent() {
 		return averageExtent;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAverageExtent(double newAverageExtent) {
 		double oldAverageExtent = averageExtent;
 		averageExtent = newAverageExtent;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, LabelsPackage.AREA_LABEL_VALUE__AVERAGE_EXTENT, oldAverageExtent, averageExtent));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param resolve
 	 * @param coreType
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case LabelsPackage.AREA_LABEL_VALUE__AREA:
 				return getArea();
 			case LabelsPackage.AREA_LABEL_VALUE__AVERAGE_EXTENT:
 				return getAverageExtent();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @param newValue
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case LabelsPackage.AREA_LABEL_VALUE__AREA:
 				setArea((Double)newValue);
 				return;
 			case LabelsPackage.AREA_LABEL_VALUE__AVERAGE_EXTENT:
 				setAverageExtent((Double)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case LabelsPackage.AREA_LABEL_VALUE__AREA:
 				setArea(AREA_EDEFAULT);
 				return;
 			case LabelsPackage.AREA_LABEL_VALUE__AVERAGE_EXTENT:
 				setAverageExtent(AVERAGE_EXTENT_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @param featureID
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case LabelsPackage.AREA_LABEL_VALUE__AREA:
 				return area != AREA_EDEFAULT;
 			case LabelsPackage.AREA_LABEL_VALUE__AVERAGE_EXTENT:
 				return averageExtent != AVERAGE_EXTENT_EDEFAULT;
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * 
 	 * @return
 	 * 
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy())
 			return super.toString();
 
 		StringBuffer result = new StringBuffer();
 		result.append(area);
 		result.append(" km^2");
 		return result.toString();
 	}
 
 } // AreaLabelValueImpl
