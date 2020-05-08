 /**
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  */
 package org.eclipse.emf.compare.uml2.impl;
 
 import org.eclipse.emf.compare.uml2.*;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class UMLCompareFactoryImpl extends EFactoryImpl implements UMLCompareFactory {
 	/**
 	 * Creates the default factory implementation.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static UMLCompareFactory init() {
 		try {
			UMLCompareFactory theUMLCompareFactory = (UMLCompareFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.eclipse.org/emf/compare/diff/uml2/2.0"); //$NON-NLS-1$ 
 			if (theUMLCompareFactory != null) {
 				return theUMLCompareFactory;
 			}
 		}
 		catch (Exception exception) {
 			EcorePlugin.INSTANCE.log(exception);
 		}
 		return new UMLCompareFactoryImpl();
 	}
 
 	/**
 	 * Creates an instance of the factory.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public UMLCompareFactoryImpl() {
 		super();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public EObject create(EClass eClass) {
 		switch (eClass.getClassifierID()) {
 			case UMLComparePackage.ASSOCIATION_CHANGE: return createAssociationChange();
 			case UMLComparePackage.DEPENDENCY_CHANGE: return createDependencyChange();
 			case UMLComparePackage.INTERFACE_REALIZATION_CHANGE: return createInterfaceRealizationChange();
 			case UMLComparePackage.SUBSTITUTION_CHANGE: return createSubstitutionChange();
 			case UMLComparePackage.EXTEND_CHANGE: return createExtendChange();
 			case UMLComparePackage.INCLUDE_CHANGE: return createIncludeChange();
 			case UMLComparePackage.GENERALIZATION_SET_CHANGE: return createGeneralizationSetChange();
 			case UMLComparePackage.EXECUTION_SPECIFICATION_CHANGE: return createExecutionSpecificationChange();
 			case UMLComparePackage.INTERVAL_CONSTRAINT_CHANGE: return createIntervalConstraintChange();
 			case UMLComparePackage.MESSAGE_CHANGE: return createMessageChange();
 			case UMLComparePackage.STEREOTYPE_PROPERTY_CHANGE: return createStereotypePropertyChange();
 			case UMLComparePackage.STEREOTYPE_APPLICATION_CHANGE: return createStereotypeApplicationChange();
 			case UMLComparePackage.STEREOTYPE_REFERENCE_CHANGE: return createStereotypeReferenceChange();
 			case UMLComparePackage.PROFILE_APPLICATION_CHANGE: return createProfileApplicationChange();
 			default:
 				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public AssociationChange createAssociationChange() {
 		AssociationChangeImpl associationChange = new AssociationChangeImpl();
 		return associationChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public DependencyChange createDependencyChange() {
 		DependencyChangeImpl dependencyChange = new DependencyChangeImpl();
 		return dependencyChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public InterfaceRealizationChange createInterfaceRealizationChange() {
 		InterfaceRealizationChangeImpl interfaceRealizationChange = new InterfaceRealizationChangeImpl();
 		return interfaceRealizationChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public SubstitutionChange createSubstitutionChange() {
 		SubstitutionChangeImpl substitutionChange = new SubstitutionChangeImpl();
 		return substitutionChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExtendChange createExtendChange() {
 		ExtendChangeImpl extendChange = new ExtendChangeImpl();
 		return extendChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IncludeChange createIncludeChange() {
 		IncludeChangeImpl includeChange = new IncludeChangeImpl();
 		return includeChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GeneralizationSetChange createGeneralizationSetChange() {
 		GeneralizationSetChangeImpl generalizationSetChange = new GeneralizationSetChangeImpl();
 		return generalizationSetChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExecutionSpecificationChange createExecutionSpecificationChange() {
 		ExecutionSpecificationChangeImpl executionSpecificationChange = new ExecutionSpecificationChangeImpl();
 		return executionSpecificationChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public IntervalConstraintChange createIntervalConstraintChange() {
 		IntervalConstraintChangeImpl intervalConstraintChange = new IntervalConstraintChangeImpl();
 		return intervalConstraintChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public MessageChange createMessageChange() {
 		MessageChangeImpl messageChange = new MessageChangeImpl();
 		return messageChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StereotypePropertyChange createStereotypePropertyChange() {
 		StereotypePropertyChangeImpl stereotypePropertyChange = new StereotypePropertyChangeImpl();
 		return stereotypePropertyChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StereotypeApplicationChange createStereotypeApplicationChange() {
 		StereotypeApplicationChangeImpl stereotypeApplicationChange = new StereotypeApplicationChangeImpl();
 		return stereotypeApplicationChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public StereotypeReferenceChange createStereotypeReferenceChange() {
 		StereotypeReferenceChangeImpl stereotypeReferenceChange = new StereotypeReferenceChangeImpl();
 		return stereotypeReferenceChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ProfileApplicationChange createProfileApplicationChange() {
 		ProfileApplicationChangeImpl profileApplicationChange = new ProfileApplicationChangeImpl();
 		return profileApplicationChange;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public UMLComparePackage getUMLComparePackage() {
 		return (UMLComparePackage)getEPackage();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @deprecated
 	 * @generated
 	 */
 	@Deprecated
 	public static UMLComparePackage getPackage() {
 		return UMLComparePackage.eINSTANCE;
 	}
 
 } //UMLCompareFactoryImpl
