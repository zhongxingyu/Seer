 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.jst.jsf.common.metadata.traittypes.traittypes;
 
 import org.eclipse.emf.ecore.EFactory;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Factory</b> for the model.
  * It provides a create method for each non-abstract class of the model.
  * <!-- end-user-doc -->
  * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.TraitTypesPackage
  * @generated
  */
 public interface TraitTypesFactory extends EFactory {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String copyright = "Copyright (c) 2007 Oracle Corporation";
 
 	/**
 	 * The singleton instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	TraitTypesFactory eINSTANCE = org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.TraitTypesFactoryImpl.init();
 
 	/**
 	 * Returns a new object of class '<em>List Of Values</em>'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return a new object of class '<em>List Of Values</em>'.
 	 * @generated
 	 */
 	ListOfValues createListOfValues();
 
 	/**
      * Returns a new object of class '<em>Set Generator</em>'.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @return a new object of class '<em>Set Generator</em>'.
      * @generated
      */
     SetGenerator createSetGenerator();
 
     /**
 	 * Returns the package supported by this factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the package supported by this factory.
 	 * @generated
 	 */
 	TraitTypesPackage getTraitTypesPackage();
 
 } //TraitTypesFactory
