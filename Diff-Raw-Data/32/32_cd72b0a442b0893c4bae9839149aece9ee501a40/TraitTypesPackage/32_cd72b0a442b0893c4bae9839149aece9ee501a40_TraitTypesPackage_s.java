 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.jst.jsf.common.metadata.traittypes.traittypes;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Package</b> for the model.
  * It contains accessors for the meta objects to represent
  * <ul>
  *   <li>each class,</li>
  *   <li>each feature of each class,</li>
  *   <li>each enum,</li>
  *   <li>and each data type</li>
  * </ul>
  * <!-- end-user-doc -->
  * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.TraitTypesFactory
  * @model kind="package"
  * @generated
  */
 public interface TraitTypesPackage extends EPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String copyright = "Copyright (c) 2007 Oracle Corporation";
 
 	/**
 	 * The package name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNAME = "traittypes";
 
 	/**
 	 * The package namespace URI.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNS_URI = "http://org.eclipse.jst.jsf.common.metadata/metadataTraitTypes.ecore";
 
 	/**
 	 * The package namespace name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	String eNS_PREFIX = "mdt";
 
 	/**
 	 * The singleton instance of the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	TraitTypesPackage eINSTANCE = org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.TraitTypesPackageImpl.init();
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.ListOfValuesImpl <em>List Of Values</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.ListOfValuesImpl
 	 * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.TraitTypesPackageImpl#getListOfValues()
 	 * @generated
 	 */
 	int LIST_OF_VALUES = 0;
 
 	/**
 	 * The feature id for the '<em><b>Entries</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int LIST_OF_VALUES__ENTRIES = 0;
 
 	/**
 	 * The number of structural features of the '<em>List Of Values</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int LIST_OF_VALUES_FEATURE_COUNT = 1;
 
 
 	/**
      * The meta object id for the '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.SetGeneratorImpl <em>Set Generator</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.SetGeneratorImpl
      * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.TraitTypesPackageImpl#getSetGenerator()
      * @generated
      */
     int SET_GENERATOR = 1;
 
     /**
      * The feature id for the '<em><b>Algorithm</b></em>' attribute.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      * @ordered
      */
     int SET_GENERATOR__ALGORITHM = 0;
 
     /**
      * The feature id for the '<em><b>Expression</b></em>' attribute.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      * @ordered
      */
     int SET_GENERATOR__EXPRESSION = 1;
 
     /**
      * The number of structural features of the '<em>Set Generator</em>' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      * @ordered
      */
     int SET_GENERATOR_FEATURE_COUNT = 0;
 
 
     /**
 	 * Returns the meta object for class '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.ListOfValues <em>List Of Values</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>List Of Values</em>'.
 	 * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.ListOfValues
 	 * @generated
 	 */
 	EClass getListOfValues();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.ListOfValues#getEntries <em>Entries</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Entries</em>'.
 	 * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.ListOfValues#getEntries()
 	 * @see #getListOfValues()
 	 * @generated
 	 */
 	EReference getListOfValues_Entries();
 
 	/**
      * Returns the meta object for class '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.SetGenerator <em>Set Generator</em>}'.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @return the meta object for class '<em>Set Generator</em>'.
      * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.SetGenerator
      * @generated
      */
     EClass getSetGenerator();
 
     /**
      * Returns the meta object for the attribute '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.SetGenerator#getAlgorithm <em>Algorithm</em>}'.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @return the meta object for the attribute '<em>Algorithm</em>'.
      * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.SetGenerator#getAlgorithm()
      * @see #getSetGenerator()
      * @generated
      */
     EAttribute getSetGenerator_Algorithm();
 
     /**
      * Returns the meta object for the attribute '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.SetGenerator#getExpression <em>Expression</em>}'.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @return the meta object for the attribute '<em>Expression</em>'.
      * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.SetGenerator#getExpression()
      * @see #getSetGenerator()
      * @generated
      */
     EAttribute getSetGenerator_Expression();
 
     /**
 	 * Returns the factory that creates the instances of the model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the factory that creates the instances of the model.
 	 * @generated
 	 */
 	TraitTypesFactory getTraitTypesFactory();
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * Defines literals for the meta objects that represent
 	 * <ul>
 	 *   <li>each class,</li>
 	 *   <li>each feature of each class,</li>
 	 *   <li>each enum,</li>
 	 *   <li>and each data type</li>
 	 * </ul>
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	interface Literals {
 		/**
 		 * The meta object literal for the '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.ListOfValuesImpl <em>List Of Values</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.ListOfValuesImpl
 		 * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.TraitTypesPackageImpl#getListOfValues()
 		 * @generated
 		 */
 		@SuppressWarnings("hiding")
 		EClass LIST_OF_VALUES = eINSTANCE.getListOfValues();
 
 		/**
 		 * The meta object literal for the '<em><b>Entries</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		@SuppressWarnings("hiding")
 		EReference LIST_OF_VALUES__ENTRIES = eINSTANCE.getListOfValues_Entries();
 
         /**
          * The meta object literal for the '{@link org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.SetGeneratorImpl <em>Set Generator</em>}' class.
          * <!-- begin-user-doc -->
          * <!-- end-user-doc -->
          * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.SetGeneratorImpl
          * @see org.eclipse.jst.jsf.common.metadata.traittypes.traittypes.internal.impl.TraitTypesPackageImpl#getSetGenerator()
          * @generated
          */
         @SuppressWarnings("hiding")
 		EClass SET_GENERATOR = eINSTANCE.getSetGenerator();
 
         /**
          * The meta object literal for the '<em><b>Algorithm</b></em>' attribute feature.
          * <!-- begin-user-doc -->
          * <!-- end-user-doc -->
          * @generated
          */
         @SuppressWarnings("hiding")
 		EAttribute SET_GENERATOR__ALGORITHM = eINSTANCE.getSetGenerator_Algorithm();
 
         /**
          * The meta object literal for the '<em><b>Expression</b></em>' attribute feature.
          * <!-- begin-user-doc -->
          * <!-- end-user-doc -->
          * @generated
          */
         @SuppressWarnings("hiding")
 		EAttribute SET_GENERATOR__EXPRESSION = eINSTANCE.getSetGenerator_Expression();
 
 	}
 
 } //TraitTypesPackage
