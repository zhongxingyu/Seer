 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.jst.jsf.common.metadata.traittypes.traittypes;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * <!-- begin-user-doc -->
  * A representation of the model object '<em><b>List Of Values</b></em>'.
 * 
 * <p><b>Provisional API - subject to change</b></p>
  * <!-- end-user-doc -->
  *
  * <p>
  * The following features are supported:
  * <ul>
  *   <li>{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.ListOfValues#getEntries <em>Entries</em>}</li>
  * </ul>
  * </p>
  *
  * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.TraitTypesPackage#getListOfValues()
  * @model
  * @generated
  */
 public interface ListOfValues extends EObject {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String copyright = "Copyright (c) 2007 Oracle Corporation";
 
 	/**
 	 * Returns the value of the '<em><b>Entries</b></em>' containment reference list.
 	 * The list contents are of type {@link org.eclipse.emf.ecore.xml.type.SimpleAnyType}.
 	 * <!-- begin-user-doc -->
 	 * <p>
 	 * If the meaning of the '<em>Entries</em>' containment reference list isn't clear,
 	 * there really should be more of a description here...
 	 * </p>
 	 * <!-- end-user-doc -->
 	 * @return the value of the '<em>Entries</em>' containment reference list.
 	 * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.TraitTypesPackage#getListOfValues_Entries()
 	 * @model type="org.eclipse.emf.ecore.xml.type.SimpleAnyType" containment="true"
 	 *        extendedMetaData="kind='element' name='item'"
 	 * @generated
 	 */
 	EList getEntries();
 
 } // ListOfValues
