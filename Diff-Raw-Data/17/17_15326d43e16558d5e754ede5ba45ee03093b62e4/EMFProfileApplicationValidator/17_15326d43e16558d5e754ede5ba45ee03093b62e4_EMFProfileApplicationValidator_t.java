 /**
  * Copyright (c) 2010 - 2012 modelversioning.org
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  */
 package org.modelversioning.emfprofileapplication.util;
 
 import java.util.Map;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.modelversioning.emfprofile.EMFProfilePlugin;
 import org.modelversioning.emfprofileapplication.EMFProfileApplicationPackage;
 import org.modelversioning.emfprofileapplication.ProfileApplication;
 import org.modelversioning.emfprofileapplication.ProfileImport;
 import org.modelversioning.emfprofileapplication.StereotypeApplication;
 import org.modelversioning.emfprofileapplication.validation.LowerBoundConstraintValidator;
 import org.modelversioning.emfprofileapplication.validation.LowerBoundConstraintViolation;
 import org.modelversioning.emfprofileapplication.validation.UpperBoundConstraintValidator;
 import org.modelversioning.emfprofileapplication.validation.UpperBoundConstraintViolation;
 
 /**
  * <!-- begin-user-doc --> The <b>Validator</b> for the model. <!-- end-user-doc
  * -->
  * 
  * @see org.modelversioning.emfprofileapplication.EMFProfileApplicationPackage
  * @generated
  */
 public class EMFProfileApplicationValidator extends EObjectValidator {
 	/**
 	 * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public static final EMFProfileApplicationValidator INSTANCE = new EMFProfileApplicationValidator();
 
 	/**
 	 * A constant for the
 	 * {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of
 	 * diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes}
 	 * from this package. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @see org.eclipse.emf.common.util.Diagnostic#getSource()
 	 * @see org.eclipse.emf.common.util.Diagnostic#getCode()
 	 * @generated
 	 */
 	public static final String DIAGNOSTIC_SOURCE = "org.modelversioning.emfprofileapplication";
 
 	/**
 	 * A constant with a fixed name that can be used as the base value for
 	 * additional hand written constants. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;
 
 	/**
 	 * A constant with a fixed name that can be used as the base value for
 	 * additional hand written constants in a derived class. <!-- begin-user-doc
 	 * --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;
 
 	public static final int VIOLATED_UPPER_BOUND = 1;
 	public static final int VIOLATED_LOWER_BOUND = 2;
 	private static final int ILLEGAL_USE_OF_REDEFINED_EXTENSION = 3;
 	private static final int ILLEGAL_USE_OF_SUBSETTED_EXTENSION = 4;
 
 	/**
 	 * Creates an instance of the switch. <!-- begin-user-doc --> <!--
 	 * end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public EMFProfileApplicationValidator() {
 		super();
 	}
 
 	/**
 	 * Returns the package of this validator switch. <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected EPackage getEPackage() {
 		return EMFProfileApplicationPackage.eINSTANCE;
 	}
 
 	/**
 	 * Calls <code>validateXXX</code> for the corresponding classifier of the
 	 * model. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	@Override
 	protected boolean validate(int classifierID, Object value,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		switch (classifierID) {
 		case EMFProfileApplicationPackage.PROFILE_APPLICATION:
 			return validateProfileApplication((ProfileApplication) value,
 					diagnostics, context);
 		case EMFProfileApplicationPackage.PROFILE_IMPORT:
 			return validateProfileImport((ProfileImport) value, diagnostics,
 					context);
 		case EMFProfileApplicationPackage.STEREOTYPE_APPLICATION:
 			return validateStereotypeApplication((StereotypeApplication) value,
 					diagnostics, context);
 		default:
 			return true;
 		}
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean validateProfileApplication(
 			ProfileApplication profileApplication, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		if (!validate_NoCircularContainment(profileApplication, diagnostics,
 				context))
 			return false;
 		boolean result = validate_EveryMultiplicityConforms(profileApplication,
 				diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validate_EveryDataValueConforms(profileApplication,
 					diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validate_EveryReferenceIsContained(profileApplication,
 					diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validate_EveryBidirectionalReferenceIsPaired(
 					profileApplication, diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validate_EveryProxyResolves(profileApplication,
 					diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validate_UniqueID(profileApplication, diagnostics,
 					context);
 		if (result || diagnostics != null)
 			result &= validate_EveryKeyUnique(profileApplication, diagnostics,
 					context);
 		if (result || diagnostics != null)
 			result &= validate_EveryMapEntryUnique(profileApplication,
 					diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validateProfileApplication_violatedUpperBound(
 					profileApplication, diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validateProfileApplication_violatedLowerBound(
 					profileApplication, diagnostics, context);
 		if (result || diagnostics != null)
 			result &= validateProfileApplication_useOfRedefinedExtension(
 					profileApplication, diagnostics, context);
 		return result;
 	}
 
 	/**
 	 * Validates the violatedUpperBound constraint of '
 	 * <em>Profile Application</em>'. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateProfileApplication_violatedUpperBound(
 			ProfileApplication profileApplication, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		UpperBoundConstraintValidator validator = new UpperBoundConstraintValidator(
 				profileApplication);
 		EList<UpperBoundConstraintViolation> violations = validator
				.getViolations();
 
 		if (violations.size() == 0) {
 			return true;
 		} else {
 			addUpperBoundDiagnostic(violations, diagnostics, context);
 			return false;
 		}
 	}
 
 	private void addUpperBoundDiagnostic(
 			EList<UpperBoundConstraintViolation> violations,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		for (UpperBoundConstraintViolation violation : violations) {
 			diagnostics.add(createDiagnostic(
 					Diagnostic.ERROR,
 					DIAGNOSTIC_SOURCE,
 					VIOLATED_UPPER_BOUND,
 					"ProfileApplication.ConstraintViolation.upper_bound",
 					new Object[] { violation.getStereotype().getName(),
 							violation.getExtension().toString(),
 							violation.getModelObject() },
 					new Object[] { violation }, context));
 		}
 	}
 
 	/**
 	 * Validates the violatedLowerBound constraint of '
 	 * <em>Profile Application</em>'. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateProfileApplication_violatedLowerBound(
 			ProfileApplication profileApplication, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		EObject rootObject = obtainRootModelObject(
 				obtainContextObject(context), profileApplication);
 
 		LowerBoundConstraintValidator validator = new LowerBoundConstraintValidator(
 				profileApplication, rootObject);
 		EList<LowerBoundConstraintViolation> violations = validator
 				.getViolations();
 
 		if (violations.size() == 0) {
 			return true;
 		} else {
 			addLowerBoundDiagnostic(violations, diagnostics, context);
 			return false;
 		}
 	}
 
 	private void addLowerBoundDiagnostic(
 			EList<LowerBoundConstraintViolation> violations,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		for (LowerBoundConstraintViolation violation : violations) {
 			diagnostics.add(createDiagnostic(Diagnostic.ERROR,
 					DIAGNOSTIC_SOURCE, VIOLATED_LOWER_BOUND,
 					"ProfileApplication.ConstraintViolation.lower_bound",
 					new Object[] {
 							violation.getExtension().getSource().getName(),
 							violation.getExtension().toString(),
 							violation.getModelObject().toString() },
 					new Object[] { violation }, context));
 		}
 	}
 
 	private EObject obtainRootModelObject(EObject contextObject,
 			ProfileApplication profileApplication) {
 		EObject rootObject = null;
 		if (profileApplication.getAnnotatedObjects().size() > 0) {
 			rootObject = EcoreUtil.getRootContainer(profileApplication
 					.getAnnotatedObjects().get(0));
 		} else if (contextObject != null) {
 			rootObject = EcoreUtil.getRootContainer(contextObject);
 		}
 		return rootObject;
 	}
 
 	private EObject obtainContextObject(Map<Object, Object> context) {
 		Object contextObject = context.get("MODEL_OBJECT");
 		if (contextObject != null && contextObject instanceof EObject) {
 			return (EObject) contextObject;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Validates the useOfRedefinedExtension constraint of '
 	 * <em>Profile Application</em>'. <!-- begin-user-doc --> <!-- end-user-doc
 	 * -->
 	 * 
 	 * @generated
 	 */
 	public boolean validateProfileApplication_useOfRedefinedExtension(
 			ProfileApplication profileApplication, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		// TODO implement the constraint
 		// -> specify the condition that violates the constraint
 		// -> verify the diagnostic details, including severity, code, and
 		// message
 		// Ensure that you remove @generated or mark it @generated NOT
 		if (false) {
 			if (diagnostics != null) {
 				diagnostics.add(createDiagnostic(Diagnostic.ERROR,
 						DIAGNOSTIC_SOURCE, ILLEGAL_USE_OF_REDEFINED_EXTENSION,
 						"_UI_GenericConstraint_diagnostic",
 						new Object[] { "useOfRedefinedExtension",
 								getObjectLabel(profileApplication, context) },
 						new Object[] { profileApplication }, context));
 			}
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean validateProfileImport(ProfileImport profileImport,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return validate_EveryDefaultConstraint(profileImport, diagnostics,
 				context);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated
 	 */
 	public boolean validateStereotypeApplication(
 			StereotypeApplication stereotypeApplication,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return validate_EveryDefaultConstraint(stereotypeApplication,
 				diagnostics, context);
 	}
 
 	/**
 	 * Returns the resource locator that will be used to fetch messages for this
 	 * validator's diagnostics. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		return EMFProfilePlugin.INSTANCE;
 	}
 
 } // EMFProfileApplicationValidator
