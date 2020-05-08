 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: CompleteOCLEObjectValidator.java,v 1.4 2011/05/20 15:26:51 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.validation;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.evaluation.DomainModelManager;
 import org.eclipse.ocl.examples.domain.messages.EvaluatorMessages;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.domain.values.impl.InvalidValueException;
 import org.eclipse.ocl.examples.domain.values.util.ValuesUtil;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.Environment;
 import org.eclipse.ocl.examples.pivot.EnvironmentFactory;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.UMLReflection;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.evaluation.EvaluationEnvironment;
 import org.eclipse.ocl.examples.pivot.evaluation.EvaluationVisitor;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.messages.OCLMessages;
 import org.eclipse.ocl.examples.pivot.utilities.PivotEnvironmentFactory;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 /**
  * A PivotEObjectValidator augments EValidator.Registry.INSTANCE validation by validation of
  * additional Pivot-defined invariants.
  * 
  * Since there is no per-ResourceSet EValidator.Registry it is necessary for the additional
  * functionality for a particular EPackage to be provided by displacing the global entry into
  * PivotEObjectValidator.eValidators and installing PivotEObjectValidator.INSTANCE in its stead.
  * 
  * When validation occurs, the static INSTANCE first invokes the displaced functionality and
  * then looks for a ValidationAdapter in the ResourceSet for the object to be validated.
  * This ValidationAdapter is only available if the ResourceSet is for an application for which
  * Pivot invariants were defined. Other applications see only a small overhead in their
  * processing time.
  */
 public class PivotEObjectValidator extends EObjectValidator
 {
 	/**
 	 * A ValidationAdapter is installed in the ResourceSet of applications that register for additional
 	 * PIvot-defined constraints. The standard validation is performed by PivotEObjectValidator.INSTANCE
	 * before additional functionality is provided by the ValidationAdapter.
	 * 
	 * For non-Pivot applications the ValidationAdapter adapts the ResourceSet containing the validatable
	 * (Ecore) instances. Validation is invoked for validatable (Ecore) instances.
	 * 
	 * For Pivot applications the ValidationAdapter adapts the ResourceSet containing the validatable
	 * metamodel elements. Validation is invoked for validatable (Pivot) elements so a redirection via
	 * the MetaModelManager is needed to find the ValidationAdapter on the externalResourceSet.
 	 */
 	public static class ValidationAdapter extends AdapterImpl
 	{
 		public static ValidationAdapter findAdapter(@NonNull ResourceSet resourceSet) {
 			for (Adapter adapter : resourceSet.eAdapters()) {
 				if (adapter instanceof ValidationAdapter) {
 					return (ValidationAdapter)adapter;
 				}
 			}
			MetaModelManager metaModelManager = PivotUtil.findMetaModelManager(resourceSet);
			if (metaModelManager != null) {
				ResourceSet externalResourceSet = metaModelManager.getExternalResourceSet();
				if (externalResourceSet != null) {
					for (Adapter adapter : externalResourceSet.eAdapters()) {
						if (adapter instanceof ValidationAdapter) {
							return (ValidationAdapter)adapter;
						}
					}
				}
			}
 			return null;
 		}
 
 		protected final @NonNull MetaModelManager metaModelManager;
 		protected final @NonNull EnvironmentFactory environmentFactory;
 		protected final @NonNull Environment rootEnvironment;
 		
 		public ValidationAdapter(@NonNull MetaModelManager metaModelManager) {
 			this.metaModelManager = metaModelManager;
 			this.environmentFactory = new PivotEnvironmentFactory(null, metaModelManager);
 			this.rootEnvironment = environmentFactory.createEnvironment();
 		}
 
 		public @NonNull MetaModelManager getMetaModelManager() {
 			return metaModelManager;
 		}
 
 		public boolean validate(@NonNull EClassifier eClassifier, @NonNull Object object, DiagnosticChain diagnostics, Map<Object, Object> context) {
 			boolean allOk = true;
 			Type type = metaModelManager.getPivotOfEcore(Type.class, eClassifier);
 			if (type != null) {
 				for (Constraint constraint : metaModelManager.getAllConstraints(type)) {
 					if (UMLReflection.INVARIANT.equals(constraint.getStereotype())) {
 						String constraintName = constraint.getName();
 						ValueSpecification specification = constraint.getSpecification();
 						if (specification instanceof ExpressionInOCL) {			// Ignore OpaqueExpression -- probably from EAnnotations
 							ExpressionInOCL query = (ExpressionInOCL)specification;
 							EvaluationEnvironment evaluationEnvironment = environmentFactory.createEvaluationEnvironment();
 							Object value = metaModelManager.getIdResolver().boxedValueOf(object);
 							evaluationEnvironment.add(DomainUtil.nonNullState(query.getContextVariable()), value);
 							DomainModelManager extents = evaluationEnvironment.createModelManager(object);
 							EvaluationVisitor evaluationVisitor = environmentFactory.createEvaluationVisitor(rootEnvironment, evaluationEnvironment, extents);
 							int severity = Diagnostic.ERROR;
 							String message = null;
 							if (query.getType() != evaluationVisitor.getMetaModelManager().getBooleanType()) {
 								String objectLabel = DomainUtil.getLabel(eClassifier, object, context);
 								message = DomainUtil.bind(OCLMessages.ValidationConstraintIsNotBoolean_ERROR_,
 									PivotUtil.getConstraintTypeName(constraint), constraintName, objectLabel);
 							}
 							try {
 								Object expressionResult = query.accept(evaluationVisitor);
 								boolean isOk = false;
 								if (expressionResult != null) {
 									isOk = ValuesUtil.asBoolean(expressionResult);
 									severity = Diagnostic.WARNING;
 								}
 								if (!isOk) {
 									String objectLabel = DomainUtil.getLabel(eClassifier, object, context);
 									OCLExpression messageExpression = query.getMessageExpression();
 									if (messageExpression != null) {
 										try {
 											Object messageResult = messageExpression.accept(evaluationVisitor);
 											if (messageResult != null) {
 												message = ValuesUtil.asString(messageResult);
 											}
 										} catch (InvalidValueException e) {
 											message = DomainUtil.bind(OCLMessages.ValidationMessageIsNotString_ERROR_,
 												PivotUtil.getConstraintTypeName(constraint), constraintName, objectLabel);
 											severity = Diagnostic.ERROR;
 										}
 										catch (Exception e) {
 											message = DomainUtil.bind(OCLMessages.ValidationMessageException_ERROR_,
 												PivotUtil.getConstraintTypeName(constraint), constraintName, objectLabel, e.getMessage());
 											severity = Diagnostic.ERROR;
 										}
 									}
 									if (message == null) {
 										message = DomainUtil.bind(EvaluatorMessages.ValidationConstraintIsNotSatisfied_ERROR_,
 											PivotUtil.getConstraintTypeName(constraint), constraintName, objectLabel);
 									}
 								}
 							} catch (InvalidValueException e) {
 								String objectLabel = DomainUtil.getLabel(eClassifier, object, context);
 								message = DomainUtil.bind(OCLMessages.ValidationResultIsNotBoolean_ERROR_,
 									PivotUtil.getConstraintTypeName(constraint), constraintName, objectLabel);
 //							} catch (InvalidEvaluationException e) {
 //								String objectLabel = DomainUtil.getLabel(eClassifier, object, context);
 //								message = DomainUtil.bind(OCLMessages.ValidationResultIsInvalid_ERROR_,
 //									PivotUtil.getConstraintTypeName(constraint), constraintName, objectLabel);
 							} catch (Throwable e) {
 								String objectLabel = DomainUtil.getLabel(eClassifier, object, context);
 								message = DomainUtil.bind(OCLMessages.ValidationConstraintException_ERROR_,
 									PivotUtil.getConstraintTypeName(constraint), constraintName, objectLabel, e.getMessage());
 							}
 							if (message != null) {
 								diagnostics.add(new BasicDiagnostic(severity, DIAGNOSTIC_SOURCE, 0, message, new Object [] { object }));
 							    allOk = false;
 							    if (severity == Diagnostic.ERROR) {
 							    	break;		// Generate many warnings but only one error
 							    }
 							}
 						}
 					}
 				}
 			}
 			return allOk;
 		}
 	}
 
 	/**
 	 * The static instance that is installed in the EValidator.Registry.INSTANCE to compose
 	 * Pivot validation with whatever other validation was installed. 
 	 */
 	private static final @NonNull PivotEObjectValidator INSTANCE = new PivotEObjectValidator();
 
 	/**
 	 * The original EValidator.Registry.INSTANCE entries that were displaced by the installation
 	 * of the composing INSTANCE.
 	 */
 	private static final @NonNull Map<EPackage, EValidator> eValidators = new HashMap<EPackage, EValidator>();
 
 	/**
 	 * Install Complete OCL validation support in resourceSet for metaModelManager.
 	 */
 	public static @NonNull ValidationAdapter install(@NonNull ResourceSet resourceSet, @NonNull MetaModelManager metaModelManager) {
 		ValidationAdapter validationAdapter = ValidationAdapter.findAdapter(resourceSet);
 		if (validationAdapter != null) {
 			if (validationAdapter.getMetaModelManager() != metaModelManager) {
 				throw new IllegalArgumentException("Inconsistent metaModelManager");
 			}
 		}
 		else {
 			validationAdapter = new ValidationAdapter(metaModelManager);
 			resourceSet.eAdapters().add(validationAdapter);
 		}
 		return validationAdapter;
 	}
 	
 	/**
 	 * Install Pivot-defined validation support for ePackage.
 	 */
 	public static synchronized void install(@NonNull EPackage ePackage) {
 		if (!eValidators.containsKey(ePackage)) {
 			Object oldEntry = EValidator.Registry.INSTANCE.put(ePackage, INSTANCE);
 			if (oldEntry instanceof EValidator.Descriptor) {
 				oldEntry = ((EValidator.Descriptor)oldEntry).getEValidator();
 			}
 			EValidator eValidator = (EValidator) oldEntry;
 			eValidators.put(ePackage, eValidator);
 		}
 	}
 
 	/**
 	 * Return the user's ResourceSet, preferably as a data element of the diagnostics, corresponding to
 	 * the original validation context, else from the object else from the eClassifier.
 	 */
 	public static ResourceSet getResourceSet(@NonNull EClassifier eClassifier, @NonNull Object object, @Nullable DiagnosticChain diagnostics) {
 		ResourceSet resourceSet = null;
 		if (diagnostics instanceof BasicDiagnostic) {
 			for (Object dataObject : ((BasicDiagnostic)diagnostics).getData()) {
 				if (dataObject instanceof EObject) {
 					Resource resource = EcoreUtil.getRootContainer((EObject) dataObject).eResource();
 					if (resource != null) {
 						resourceSet = resource.getResourceSet();
 						if (resourceSet != null) {
 							break;
 						}
 					}
 				}
 			}
 		}
 		if (resourceSet == null) {
 			if (object instanceof EObject) {
 				Resource resource = EcoreUtil.getRootContainer((EObject) object).eResource();
 				if (resource != null) {
 					resourceSet = resource.getResourceSet();
 				}
 			}
 			if (resourceSet == null) {
 				Resource resource = EcoreUtil.getRootContainer(eClassifier).eResource();
 				if (resource != null) {
 					resourceSet = resource.getResourceSet();
 				}
 			}
 		}
 		return resourceSet;
 	}
 
 	/**
 	 * Overriden to intercept the validation of an EObject to add the additional Pivot-defined validation.
 	 */
 	@Override
 	public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context) {
 		boolean allOk = true;
 		EPackage ePackage = eClass.getEPackage();
 		EValidator eValidator = eValidators.get(ePackage);
 		if (eValidator != null) {
 			allOk &= eValidator.validate(eClass, eObject, diagnostics, context);
 		}
 		if ((allOk || (diagnostics != null)) && !eObject.eIsProxy()) {
 			allOk &= validatePivot(eClass, eObject, diagnostics, context);
 		}
 		return allOk;
 	}
 
 	/**
 	 * Overriden to intercept the validation of an EDataType value to add the additional Pivot-defined validation.
 	 */
 	@Override
 	public boolean validate(EDataType eDataType, Object value, DiagnosticChain diagnostics, Map<Object, Object> context) {
 		assert value != null;
 		boolean allOk = true;
 		EPackage ePackage = eDataType.getEPackage();
 		EValidator eValidator = eValidators.get(ePackage);
 		if (eValidator != null) {
 			allOk &= eValidator.validate(eDataType, value, diagnostics, context);
 		}
 		if ((allOk || (diagnostics != null)) && eDataType.isInstance(value)) {
 			allOk &= validatePivot(eDataType, value, diagnostics, context);
 		}
 		return allOk;
 	}
 
 	/**
 	 * Perform the additional Pivot-defined validation.
 	 */
 	protected boolean validatePivot(@NonNull EClassifier eClassifier, @NonNull Object object, @Nullable DiagnosticChain diagnostics, Map<Object, Object> context) {
 		ResourceSet resourceSet = getResourceSet(eClassifier, object, diagnostics);
 		if (resourceSet != null) {
 			ValidationAdapter validationAdapter = ValidationAdapter.findAdapter(resourceSet);
 			if (validationAdapter != null) {
 				boolean allOk = validationAdapter.validate(eClassifier, object, diagnostics, context);
 				return allOk || (diagnostics != null);
 			}
 		}
 		return true;
 	}
 }
