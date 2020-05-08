 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.stem.definitions.labels.impl;
 
 import java.util.Collection;
 
 import org.eclipse.emf.common.notify.Notification;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.emf.ecore.EClass;
 
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 
import org.eclipse.emf.ecore.util.EDataTypeEList;
 import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
 
 import org.eclipse.stem.core.graph.impl.LabelValueImpl;
 
 import org.eclipse.stem.definitions.labels.EarthScienceLabelValue;
 import org.eclipse.stem.definitions.labels.LabelsPackage;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Earth Science Label Value</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.definitions.labels.impl.EarthScienceLabelValueImpl#getData <em>Data</em>}</li>
  *   <li>{@link org.eclipse.stem.definitions.labels.impl.EarthScienceLabelValueImpl#getDataType <em>Data Type</em>}</li>
  *   <li>{@link org.eclipse.stem.definitions.labels.impl.EarthScienceLabelValueImpl#getUnits <em>Units</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class EarthScienceLabelValueImpl extends LabelValueImpl implements EarthScienceLabelValue {
 	/**
 	 * The cached value of the '{@link #getData() <em>Data</em>}' attribute list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getData()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Double> data;
 
 	/**
 	 * The default value of the '{@link #getDataType() <em>Data Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDataType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DATA_TYPE_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDataType() <em>Data Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDataType()
 	 * @generated
 	 * @ordered
 	 */
 	protected String dataType = DATA_TYPE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getUnits() <em>Units</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUnits()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String UNITS_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getUnits() <em>Units</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getUnits()
 	 * @generated
 	 * @ordered
 	 */
 	protected String units = UNITS_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	protected EarthScienceLabelValueImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return LabelsPackage.Literals.EARTH_SCIENCE_LABEL_VALUE;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Double> getData() {
 		if (data == null) {
			data = new EDataTypeEList<Double>(Double.class, this, LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA);
 		}
 		return data;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDataType() {
 		return dataType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDataType(String newDataType) {
 		String oldDataType = dataType;
 		dataType = newDataType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA_TYPE, oldDataType, dataType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getUnits() {
 		return units;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUnits(String newUnits) {
 		String oldUnits = units;
 		units = newUnits;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__UNITS, oldUnits, units));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA:
 				return getData();
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA_TYPE:
 				return getDataType();
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__UNITS:
 				return getUnits();
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
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA:
 				getData().clear();
 				getData().addAll((Collection<? extends Double>)newValue);
 				return;
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA_TYPE:
 				setDataType((String)newValue);
 				return;
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__UNITS:
 				setUnits((String)newValue);
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
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA:
 				getData().clear();
 				return;
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA_TYPE:
 				setDataType(DATA_TYPE_EDEFAULT);
 				return;
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__UNITS:
 				setUnits(UNITS_EDEFAULT);
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
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA:
 				return data != null && !data.isEmpty();
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__DATA_TYPE:
 				return DATA_TYPE_EDEFAULT == null ? dataType != null : !DATA_TYPE_EDEFAULT.equals(dataType);
 			case LabelsPackage.EARTH_SCIENCE_LABEL_VALUE__UNITS:
 				return UNITS_EDEFAULT == null ? units != null : !UNITS_EDEFAULT.equals(units);
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
 		result.append(" (data: "); //$NON-NLS-1$
 		result.append(data);
 		result.append(", dataType: "); //$NON-NLS-1$
 		result.append(dataType);
 		result.append(", units: "); //$NON-NLS-1$
 		result.append(units);
 		result.append(')');
 		return result.toString();
 	}
 
 } //EarthScienceLabelValueImpl
