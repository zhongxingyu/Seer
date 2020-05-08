 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.stem.definitions.labels;
 
 import org.eclipse.emf.common.util.EList;
 
 import org.eclipse.stem.core.graph.LabelValue;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>Earth Science Label Value</b></em>'.
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.stem.definitions.labels.EarthScienceLabelValue#getData <em>Data</em>}</li>
  *   <li>{@link org.eclipse.stem.definitions.labels.EarthScienceLabelValue#getDataType <em>Data Type</em>}</li>
  *   <li>{@link org.eclipse.stem.definitions.labels.EarthScienceLabelValue#getUnits <em>Units</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.stem.definitions.labels.LabelsPackage#getEarthScienceLabelValue()
  * @model
  * @generated
  */
 public interface EarthScienceLabelValue extends LabelValue {
 	/**
 	 * Returns the value of the '<em><b>Data</b></em>' attribute list.
 	 * The list contents are of type {@link java.lang.Double}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Data</em>' attribute list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Data</em>' attribute list.
 	 * @see org.eclipse.stem.definitions.labels.LabelsPackage#getEarthScienceLabelValue_Data()
	 * @model
 	 * @generated
 	 */
 	EList<Double> getData();
 
 	/**
 	 * Returns the value of the '<em><b>Data Type</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Data Type</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Data Type</em>' attribute.
 	 * @see #setDataType(String)
 	 * @see org.eclipse.stem.definitions.labels.LabelsPackage#getEarthScienceLabelValue_DataType()
 	 * @model
 	 * @generated
 	 */
 	String getDataType();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.stem.definitions.labels.EarthScienceLabelValue#getDataType <em>Data Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Data Type</em>' attribute.
 	 * @see #getDataType()
 	 * @generated
 	 */
 	void setDataType(String value);
 
 	/**
 	 * Returns the value of the '<em><b>Units</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Units</em>' attribute isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Units</em>' attribute.
 	 * @see #setUnits(String)
 	 * @see org.eclipse.stem.definitions.labels.LabelsPackage#getEarthScienceLabelValue_Units()
 	 * @model
 	 * @generated
 	 */
 	String getUnits();
 
 	/**
 	 * Sets the value of the '{@link org.eclipse.stem.definitions.labels.EarthScienceLabelValue#getUnits <em>Units</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @param value the new value of the '<em>Units</em>' attribute.
 	 * @see #getUnits()
 	 * @generated
 	 */
 	void setUnits(String value);
 
 } // EarthScienceLabelValue
