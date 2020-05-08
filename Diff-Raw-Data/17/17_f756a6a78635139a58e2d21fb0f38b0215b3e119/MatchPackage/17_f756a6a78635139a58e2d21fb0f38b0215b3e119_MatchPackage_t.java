 /*  
  * Copyright (c) 2006, Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.emf.compare.match;
 
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
  * @see org.eclipse.emf.compare.match.MatchFactory
  * @model kind="package"
  * @generated
  */
 public interface MatchPackage extends EPackage {
 	/**
 	 * The package name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated NOT
 	 */
	String eNAME = "match"; //$NON-NLS-1$
 
 	/**
 	 * The package namespace URI.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated NOT
 	 */
	String eNS_URI = "http://www.eclipse.org/emf/compare/match/1.0"; //$NON-NLS-1$
 
 	/**
 	 * The package namespace name.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
	 * @generated NOT
 	 */
	String eNS_PREFIX = "match"; //$NON-NLS-1$
 
 	/**
 	 * The singleton instance of the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	MatchPackage eINSTANCE = org.eclipse.emf.compare.match.impl.MatchPackageImpl
 			.init();
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.compare.match.impl.MatchModelImpl <em>Model</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.compare.match.impl.MatchModelImpl
 	 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatchModel()
 	 * @generated
 	 */
 	int MATCH_MODEL = 0;
 
 	/**
 	 * The feature id for the '<em><b>Left Model</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_MODEL__LEFT_MODEL = 0;
 
 	/**
 	 * The feature id for the '<em><b>Right Model</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_MODEL__RIGHT_MODEL = 1;
 
 	/**
 	 * The feature id for the '<em><b>Origin Model</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_MODEL__ORIGIN_MODEL = 2;
 
 	/**
 	 * The feature id for the '<em><b>Matched Elements</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_MODEL__MATCHED_ELEMENTS = 3;
 
 	/**
 	 * The feature id for the '<em><b>Un Matched Elements</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_MODEL__UN_MATCHED_ELEMENTS = 4;
 
 	/**
 	 * The number of structural features of the '<em>Model</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_MODEL_FEATURE_COUNT = 5;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.compare.match.impl.MatchElementImpl <em>Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.compare.match.impl.MatchElementImpl
 	 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatchElement()
 	 * @generated
 	 */
 	int MATCH_ELEMENT = 1;
 
 	/**
 	 * The feature id for the '<em><b>Similarity</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_ELEMENT__SIMILARITY = 0;
 
 	/**
 	 * The feature id for the '<em><b>Sub Match Elements</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_ELEMENT__SUB_MATCH_ELEMENTS = 1;
 
 	/**
 	 * The number of structural features of the '<em>Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH_ELEMENT_FEATURE_COUNT = 2;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.compare.match.impl.Match2ElementsImpl <em>Match2 Elements</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.compare.match.impl.Match2ElementsImpl
 	 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatch2Elements()
 	 * @generated
 	 */
 	int MATCH2_ELEMENTS = 2;
 
 	/**
 	 * The feature id for the '<em><b>Similarity</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH2_ELEMENTS__SIMILARITY = MATCH_ELEMENT__SIMILARITY;
 
 	/**
 	 * The feature id for the '<em><b>Sub Match Elements</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH2_ELEMENTS__SUB_MATCH_ELEMENTS = MATCH_ELEMENT__SUB_MATCH_ELEMENTS;
 
 	/**
 	 * The feature id for the '<em><b>Left Element</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH2_ELEMENTS__LEFT_ELEMENT = MATCH_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The feature id for the '<em><b>Right Element</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH2_ELEMENTS__RIGHT_ELEMENT = MATCH_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The number of structural features of the '<em>Match2 Elements</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH2_ELEMENTS_FEATURE_COUNT = MATCH_ELEMENT_FEATURE_COUNT + 2;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.compare.match.impl.Match3ElementImpl <em>Match3 Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.compare.match.impl.Match3ElementImpl
 	 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatch3Element()
 	 * @generated
 	 */
 	int MATCH3_ELEMENT = 3;
 
 	/**
 	 * The feature id for the '<em><b>Similarity</b></em>' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH3_ELEMENT__SIMILARITY = MATCH_ELEMENT__SIMILARITY;
 
 	/**
 	 * The feature id for the '<em><b>Sub Match Elements</b></em>' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH3_ELEMENT__SUB_MATCH_ELEMENTS = MATCH_ELEMENT__SUB_MATCH_ELEMENTS;
 
 	/**
 	 * The feature id for the '<em><b>Origin Element</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH3_ELEMENT__ORIGIN_ELEMENT = MATCH_ELEMENT_FEATURE_COUNT + 0;
 
 	/**
 	 * The number of structural features of the '<em>Match3 Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int MATCH3_ELEMENT_FEATURE_COUNT = MATCH_ELEMENT_FEATURE_COUNT + 1;
 
 	/**
 	 * The meta object id for the '{@link org.eclipse.emf.compare.match.impl.UnMatchElementImpl <em>Un Match Element</em>}' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.compare.match.impl.UnMatchElementImpl
 	 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getUnMatchElement()
 	 * @generated
 	 */
 	int UN_MATCH_ELEMENT = 4;
 
 	/**
 	 * The feature id for the '<em><b>Element</b></em>' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int UN_MATCH_ELEMENT__ELEMENT = 0;
 
 	/**
 	 * The number of structural features of the '<em>Un Match Element</em>' class.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 * @ordered
 	 */
 	int UN_MATCH_ELEMENT_FEATURE_COUNT = 1;
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.compare.match.MatchModel <em>Model</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Model</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchModel
 	 * @generated
 	 */
 	EClass getMatchModel();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.compare.match.MatchModel#getLeftModel <em>Left Model</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Left Model</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchModel#getLeftModel()
 	 * @see #getMatchModel()
 	 * @generated
 	 */
 	EAttribute getMatchModel_LeftModel();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.compare.match.MatchModel#getRightModel <em>Right Model</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Right Model</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchModel#getRightModel()
 	 * @see #getMatchModel()
 	 * @generated
 	 */
 	EAttribute getMatchModel_RightModel();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.compare.match.MatchModel#getOriginModel <em>Origin Model</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Origin Model</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchModel#getOriginModel()
 	 * @see #getMatchModel()
 	 * @generated
 	 */
 	EAttribute getMatchModel_OriginModel();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.emf.compare.match.MatchModel#getMatchedElements <em>Matched Elements</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Matched Elements</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchModel#getMatchedElements()
 	 * @see #getMatchModel()
 	 * @generated
 	 */
 	EReference getMatchModel_MatchedElements();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.emf.compare.match.MatchModel#getUnMatchedElements <em>Un Matched Elements</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Un Matched Elements</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchModel#getUnMatchedElements()
 	 * @see #getMatchModel()
 	 * @generated
 	 */
 	EReference getMatchModel_UnMatchedElements();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.compare.match.MatchElement <em>Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Element</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchElement
 	 * @generated
 	 */
 	EClass getMatchElement();
 
 	/**
 	 * Returns the meta object for the attribute '{@link org.eclipse.emf.compare.match.MatchElement#getSimilarity <em>Similarity</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the attribute '<em>Similarity</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchElement#getSimilarity()
 	 * @see #getMatchElement()
 	 * @generated
 	 */
 	EAttribute getMatchElement_Similarity();
 
 	/**
 	 * Returns the meta object for the containment reference list '{@link org.eclipse.emf.compare.match.MatchElement#getSubMatchElements <em>Sub Match Elements</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the containment reference list '<em>Sub Match Elements</em>'.
 	 * @see org.eclipse.emf.compare.match.MatchElement#getSubMatchElements()
 	 * @see #getMatchElement()
 	 * @generated
 	 */
 	EReference getMatchElement_SubMatchElements();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.compare.match.Match2Elements <em>Match2 Elements</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Match2 Elements</em>'.
 	 * @see org.eclipse.emf.compare.match.Match2Elements
 	 * @generated
 	 */
 	EClass getMatch2Elements();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.compare.match.Match2Elements#getLeftElement <em>Left Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Left Element</em>'.
 	 * @see org.eclipse.emf.compare.match.Match2Elements#getLeftElement()
 	 * @see #getMatch2Elements()
 	 * @generated
 	 */
 	EReference getMatch2Elements_LeftElement();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.compare.match.Match2Elements#getRightElement <em>Right Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Right Element</em>'.
 	 * @see org.eclipse.emf.compare.match.Match2Elements#getRightElement()
 	 * @see #getMatch2Elements()
 	 * @generated
 	 */
 	EReference getMatch2Elements_RightElement();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.compare.match.Match3Element <em>Match3 Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Match3 Element</em>'.
 	 * @see org.eclipse.emf.compare.match.Match3Element
 	 * @generated
 	 */
 	EClass getMatch3Element();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.compare.match.Match3Element#getOriginElement <em>Origin Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Origin Element</em>'.
 	 * @see org.eclipse.emf.compare.match.Match3Element#getOriginElement()
 	 * @see #getMatch3Element()
 	 * @generated
 	 */
 	EReference getMatch3Element_OriginElement();
 
 	/**
 	 * Returns the meta object for class '{@link org.eclipse.emf.compare.match.UnMatchElement <em>Un Match Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for class '<em>Un Match Element</em>'.
 	 * @see org.eclipse.emf.compare.match.UnMatchElement
 	 * @generated
 	 */
 	EClass getUnMatchElement();
 
 	/**
 	 * Returns the meta object for the reference '{@link org.eclipse.emf.compare.match.UnMatchElement#getElement <em>Element</em>}'.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the meta object for the reference '<em>Element</em>'.
 	 * @see org.eclipse.emf.compare.match.UnMatchElement#getElement()
 	 * @see #getUnMatchElement()
 	 * @generated
 	 */
 	EReference getUnMatchElement_Element();
 
 	/**
 	 * Returns the factory that creates the instances of the model.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @return the factory that creates the instances of the model.
 	 * @generated
 	 */
 	MatchFactory getMatchFactory();
 
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
 		 * The meta object literal for the '{@link org.eclipse.emf.compare.match.impl.MatchModelImpl <em>Model</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.compare.match.impl.MatchModelImpl
 		 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatchModel()
 		 * @generated
 		 */
 		EClass MATCH_MODEL = eINSTANCE.getMatchModel();
 
 		/**
 		 * The meta object literal for the '<em><b>Left Model</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute MATCH_MODEL__LEFT_MODEL = eINSTANCE
 				.getMatchModel_LeftModel();
 
 		/**
 		 * The meta object literal for the '<em><b>Right Model</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute MATCH_MODEL__RIGHT_MODEL = eINSTANCE
 				.getMatchModel_RightModel();
 
 		/**
 		 * The meta object literal for the '<em><b>Origin Model</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute MATCH_MODEL__ORIGIN_MODEL = eINSTANCE
 				.getMatchModel_OriginModel();
 
 		/**
 		 * The meta object literal for the '<em><b>Matched Elements</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference MATCH_MODEL__MATCHED_ELEMENTS = eINSTANCE
 				.getMatchModel_MatchedElements();
 
 		/**
 		 * The meta object literal for the '<em><b>Un Matched Elements</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference MATCH_MODEL__UN_MATCHED_ELEMENTS = eINSTANCE
 				.getMatchModel_UnMatchedElements();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.compare.match.impl.MatchElementImpl <em>Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.compare.match.impl.MatchElementImpl
 		 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatchElement()
 		 * @generated
 		 */
 		EClass MATCH_ELEMENT = eINSTANCE.getMatchElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Similarity</b></em>' attribute feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EAttribute MATCH_ELEMENT__SIMILARITY = eINSTANCE
 				.getMatchElement_Similarity();
 
 		/**
 		 * The meta object literal for the '<em><b>Sub Match Elements</b></em>' containment reference list feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference MATCH_ELEMENT__SUB_MATCH_ELEMENTS = eINSTANCE
 				.getMatchElement_SubMatchElements();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.compare.match.impl.Match2ElementsImpl <em>Match2 Elements</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.compare.match.impl.Match2ElementsImpl
 		 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatch2Elements()
 		 * @generated
 		 */
 		EClass MATCH2_ELEMENTS = eINSTANCE.getMatch2Elements();
 
 		/**
 		 * The meta object literal for the '<em><b>Left Element</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference MATCH2_ELEMENTS__LEFT_ELEMENT = eINSTANCE
 				.getMatch2Elements_LeftElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Right Element</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference MATCH2_ELEMENTS__RIGHT_ELEMENT = eINSTANCE
 				.getMatch2Elements_RightElement();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.compare.match.impl.Match3ElementImpl <em>Match3 Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.compare.match.impl.Match3ElementImpl
 		 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getMatch3Element()
 		 * @generated
 		 */
 		EClass MATCH3_ELEMENT = eINSTANCE.getMatch3Element();
 
 		/**
 		 * The meta object literal for the '<em><b>Origin Element</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference MATCH3_ELEMENT__ORIGIN_ELEMENT = eINSTANCE
 				.getMatch3Element_OriginElement();
 
 		/**
 		 * The meta object literal for the '{@link org.eclipse.emf.compare.match.impl.UnMatchElementImpl <em>Un Match Element</em>}' class.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @see org.eclipse.emf.compare.match.impl.UnMatchElementImpl
 		 * @see org.eclipse.emf.compare.match.impl.MatchPackageImpl#getUnMatchElement()
 		 * @generated
 		 */
 		EClass UN_MATCH_ELEMENT = eINSTANCE.getUnMatchElement();
 
 		/**
 		 * The meta object literal for the '<em><b>Element</b></em>' reference feature.
 		 * <!-- begin-user-doc -->
 		 * <!-- end-user-doc -->
 		 * @generated
 		 */
 		EReference UN_MATCH_ELEMENT__ELEMENT = eINSTANCE
 				.getUnMatchElement_Element();
 
 	}
 
 } //MatchPackage
