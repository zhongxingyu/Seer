 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.synchronizer.factory;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.compare.diff.metamodel.AttributeChange;
 import org.eclipse.emf.compare.diff.metamodel.AttributeChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.AttributeOrderChange;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffPackage;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChange;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChange;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceOrderChange;
 import org.eclipse.emf.compare.diff.metamodel.ResourceDiff;
 import org.eclipse.emf.compare.diff.metamodel.UpdateReference;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.core.compiler.AttributeChangeStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.compiler.InstructionTraceabilityEntry;
 import org.eclipse.mylyn.docs.intent.core.compiler.ModelElementChangeStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.ReferenceChangeStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerChangeState;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitPackage;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.NativeValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.NewObjectValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ReferenceValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ValueForStructuralFeature;
 
 /**
  * This factory is in charge of creating {@link CompilationStatus} from different elements (
  * {@link DiffElement} of a DiffModel, error on a ResourceDeclaration...).
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class SynchronizerStatusFactory {
 
 	/**
 	 * SynchronizerStatusFactory constructor.
 	 */
 	private SynchronizerStatusFactory() {
 
 	}
 
 	/**
 	 * Create a list of compilationStatus from the given {@link DiffElement}.
 	 * 
 	 * @param indexEntry
 	 *            the indexEntry currently visited
 	 * @param difference
 	 *            the {@link DiffElement} describing the differences between an element of the internal
 	 *            generated model and the element of an external generated model
 	 * @return a list of compilationStatus created from the given {@link DiffElement}
 	 */
 	public static List<CompilationStatus> createStatusFromDiffElement(TraceabilityIndexEntry indexEntry,
 			DiffElement difference) {
 
 		List<CompilationStatus> statusList = new ArrayList<CompilationStatus>();
 
 		// If we have a unitary diffElement
 		if (difference.getSubDiffElements().isEmpty()) {
 			SynchronizerCompilationStatus status = null;
 
 			if (difference instanceof AttributeChange) {
 				status = createStatusFromAttributeChange(indexEntry, (AttributeChange)difference);
 			} else if (difference instanceof ReferenceChange) {
 				status = createStatusFromReferenceChange(indexEntry, (ReferenceChange)difference);
 			} else if (difference instanceof ModelElementChange) {
 				status = createStatusFromModelElementChange(indexEntry, (ModelElementChange)difference);
 			} else if (difference instanceof ResourceDiff) {
 				status = CompilerFactory.eINSTANCE.createResourceChangeStatus();
 			}
 
 			if (status != null) {
 				// reference order differences are ignored for now
 				if (difference instanceof ReferenceOrderChange || difference instanceof AttributeOrderChange) {
 					status.setSeverity(CompilationStatusSeverity.INFO);
 					status.setType(CompilationMessageType.SYNCHRONIZER_INFO);
 				} else {
 					status.setSeverity(CompilationStatusSeverity.WARNING);
 					status.setType(CompilationMessageType.SYNCHRONIZER_WARNING);
 				}
 
 				status.setMessage(SynchronizerMessageProvider.createMessageFromDiffElement(difference));
 				status.setWorkingCopyResourceURI(indexEntry.getResourceDeclaration().getUri().toString());
 				status.setCompiledResourceURI(indexEntry.getGeneratedResourcePath());
 
 				if (status.getTarget() == null) {
 					// If no instruction has been found, we associated the status with the currently compiled
 					// resource
 					IntentLogger.getInstance().log(
 							LogType.WARNING,
 							"CANNOT FIND ANY INSTRUCTION FOR " + difference.eClass().getName() + ": "
 									+ difference);
 					status.setTarget(indexEntry.getResourceDeclaration());
 				}
 				statusList.add(status);
 			} else {
 				IntentLogger.getInstance().log(LogType.WARNING,
 						"CANNOT HANDLE DIFFERENCE " + difference.eClass().getName() + ": " + difference);
 			}
 		} else {
 			// If the given diffElement contains sub-diffElements
 			for (DiffElement subDifference : difference.getSubDiffElements()) {
 				statusList.addAll(createStatusFromDiffElement(indexEntry, subDifference));
 			}
 		}
 		return statusList;
 	}
 
 	/**
 	 * Creates the status related to the given difference.
 	 * 
 	 * @param indexEntry
 	 *            the current index entry
 	 * @param difference
 	 *            the difference
 	 * @return the status
 	 */
 	private static ModelElementChangeStatus createStatusFromModelElementChange(
 			TraceabilityIndexEntry indexEntry, ModelElementChange difference) {
 		ModelElementChangeStatus status = CompilerFactory.eINSTANCE.createModelElementChangeStatus();
 
 		switch (difference.eClass().getClassifierID()) {
 			case DiffPackage.MODEL_ELEMENT_CHANGE_LEFT_TARGET:
 				status.setChangeState(SynchronizerChangeState.COMPILED_TARGET);
 				ModelElementChangeLeftTarget letTargetDiff = (ModelElementChangeLeftTarget)difference;
 				status.setCompiledElement(letTargetDiff.getLeftElement());
 				status.setWorkingCopyParentURIFragment(EcoreUtil.getURI(letTargetDiff.getRightParent())
 						.toString());
 				status.setTarget(getInstructionFromCompiledElement(indexEntry, letTargetDiff.getLeftElement()));
 				break;
 
 			case DiffPackage.MODEL_ELEMENT_CHANGE_RIGHT_TARGET:
 				status.setChangeState(SynchronizerChangeState.WORKING_COPY_TARGET);
 				ModelElementChangeRightTarget rightTargetDiff = (ModelElementChangeRightTarget)difference;
 				status.setWorkingCopyElementURIFragment(EcoreUtil.getURI(rightTargetDiff.getRightElement())
 						.toString());
 				status.setCompiledParent(rightTargetDiff.getLeftParent());
 				status.setTarget(getInstructionFromCompiledElement(indexEntry,
 						rightTargetDiff.getLeftParent()));
 				break;
 
 			case DiffPackage.UPDATE_MODEL_ELEMENT:
 				status.setChangeState(SynchronizerChangeState.UPDATE);
 				break;
 
 			default:
 				status = null;
 				break;
 		}
 		return status;
 	}
 
 	/**
 	 * Creates a fragment if possible.
 	 * 
 	 * @param eo
 	 *            the object
 	 * @return the fragment or null
 	 */
 	private static String createURIFragment(EObject eo) {
 		if (eo != null) {
 			return EcoreUtil.getURI(eo).toString();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Creates the status related to the given difference.
 	 * 
 	 * @param indexEntry
 	 *            the current index entry
 	 * @param difference
 	 *            the difference
 	 * @return the status
 	 */
 	private static ReferenceChangeStatus createStatusFromReferenceChange(TraceabilityIndexEntry indexEntry,
 			ReferenceChange difference) {
 		EObject compiledElement = difference.getLeftElement();
 		IntentGenericElement target = null;
 		ReferenceChangeStatus status = CompilerFactory.eINSTANCE.createReferenceChangeStatus();
 		status.setCompiledElement(compiledElement);
 		status.setFeatureName(difference.getReference().getName());
 		status.setWorkingCopyElementURIFragment(EcoreUtil.getURI(difference.getRightElement()).toString());
 
 		switch (difference.eClass().getClassifierID()) {
 			case DiffPackage.REFERENCE_CHANGE_RIGHT_TARGET:
 				status.setChangeState(SynchronizerChangeState.WORKING_COPY_TARGET);
 				status.setCompiledTarget(((ReferenceChangeRightTarget)difference).getLeftTarget());
 				status.setWorkingCopyTargetURIFragment(createURIFragment(((ReferenceChangeRightTarget)difference)
 						.getRightTarget()));
 				break;
 
 			case DiffPackage.REFERENCE_ORDER_CHANGE:
 				status.setChangeState(SynchronizerChangeState.ORDER);
 				break;
 
 			case DiffPackage.REFERENCE_CHANGE_LEFT_TARGET:
 				status.setChangeState(SynchronizerChangeState.COMPILED_TARGET);
 				target = getInstructionFromAffectation(indexEntry, compiledElement,
 						difference.getReference(), ((ReferenceChangeLeftTarget)difference).getLeftTarget());
 				status.setCompiledTarget(((ReferenceChangeLeftTarget)difference).getLeftTarget());
 				status.setWorkingCopyTargetURIFragment(createURIFragment(((ReferenceChangeLeftTarget)difference)
 						.getRightTarget()));
 				break;
 
 			case DiffPackage.UPDATE_REFERENCE:
 				UpdateReference updateDifference = (UpdateReference)difference;
 				status.setChangeState(SynchronizerChangeState.UPDATE);
 				target = getInstructionFromAffectation(indexEntry, compiledElement,
 						difference.getReference(), difference.getLeftElement()
 								.eGet(difference.getReference()));
 
 				// Workaround EMF compare 1 issue :
 				// Actual targets are in fact merging utilities and may not be relevant.
 				EObject leftTarget = (EObject)updateDifference.getLeftElement().eGet(
 						updateDifference.getReference());
 				EObject rightTarget = (EObject)updateDifference.getRightElement().eGet(
 						updateDifference.getReference());
 
 				status.setCompiledTarget(leftTarget);
 				if (rightTarget != null) {
 					status.setWorkingCopyTargetURIFragment(createURIFragment(rightTarget));
 				}
 				break;
 
 			default:
 				status = null;
 				break;
 		}
 
 		// target setting: if affectation not found (or not available), use the parent compiled element
 		if (status != null) {
 			if (target == null) {
 				if (compiledElement != null) {
 					target = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				}
 			}
 			status.setTarget(target);
 		}
 		return status;
 	}
 
 	/**
 	 * Creates the status related to the given difference.
 	 * 
 	 * @param indexEntry
 	 *            the current index entry
 	 * @param difference
 	 *            the difference
 	 * @return the status
 	 */
 	private static AttributeChangeStatus createStatusFromAttributeChange(TraceabilityIndexEntry indexEntry,
 			AttributeChange difference) {
 		EObject compiledElement = difference.getLeftElement();
 		IntentGenericElement target = null;
 		AttributeChangeStatus status = CompilerFactory.eINSTANCE.createAttributeChangeStatus();
 		status.setCompiledElement(compiledElement);
 		status.setFeatureName(difference.getAttribute().getName());
 		status.setWorkingCopyElementURIFragment(EcoreUtil.getURI(difference.getRightElement()).toString());
 
 		switch (difference.eClass().getClassifierID()) {
 			case DiffPackage.ATTRIBUTE_CHANGE_RIGHT_TARGET:
 				status.setChangeState(SynchronizerChangeState.WORKING_COPY_TARGET);
 				break;
 
 			case DiffPackage.ATTRIBUTE_ORDER_CHANGE:
 				status.setChangeState(SynchronizerChangeState.ORDER);
 				break;
 
 			case DiffPackage.ATTRIBUTE_CHANGE_LEFT_TARGET:
 				status.setChangeState(SynchronizerChangeState.COMPILED_TARGET);
 				target = getInstructionFromAffectation(indexEntry, difference.getLeftElement(),
 						difference.getAttribute(), ((AttributeChangeLeftTarget)difference).getLeftTarget());
 				break;
 
 			case DiffPackage.UPDATE_ATTRIBUTE:
 				status.setChangeState(SynchronizerChangeState.UPDATE);
 				target = getInstructionFromAffectation(indexEntry, compiledElement,
 						difference.getAttribute(), compiledElement.eGet(difference.getAttribute()));
 				break;
 
 			default:
 				status = null;
 				break;
 		}
 
 		// target setting: if affectation not found (or not available), use the parent compiled element
 		if (status != null) {
 			if (target == null) {
 				if (compiledElement != null) {
 					target = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				}
 			}
 			status.setTarget(target);
 		}
 		return status;
 	}
 
 	/**
 	 * Returns the instruction that defined the given compiledElement.
 	 * 
 	 * @param indexEntry
 	 *            the indexEntry currently visited
 	 * @param compiledElement
 	 *            the compiledElement from which we want to determine the instruction
 	 * @return the instruction that defined the given compiledElements (can be null)
 	 */
 	private static IntentGenericElement getInstructionFromCompiledElement(TraceabilityIndexEntry indexEntry,
 			EObject compiledElement) {
 		EList<InstructionTraceabilityEntry> instructionEntries = indexEntry
 				.getContainedElementToInstructions().get(compiledElement);
 		if (instructionEntries != null) {
 			for (InstructionTraceabilityEntry entry : instructionEntries) {
 				IntentGenericElement instruction = entry.getInstruction();
 				if (instruction instanceof InstanciationInstruction) {
 					return instruction;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Lookup for a value instruction. If not found, returns the compiledElement instruction.
 	 * 
 	 * @param indexEntry
 	 *            the index entry
 	 * @param compiledElement
 	 *            the parent element
 	 * @param feature
 	 *            the affected feature
 	 * @param diffValue
 	 *            the feature value too look for
 	 * @return the value
 	 */
 	private static IntentGenericElement getInstructionFromAffectation(TraceabilityIndexEntry indexEntry,
 			EObject compiledElement, EStructuralFeature feature, Object diffValue) {
 		EList<InstructionTraceabilityEntry> instructionEntries = indexEntry
 				.getContainedElementToInstructions().get(compiledElement);
 		if (instructionEntries != null) {
 			for (InstructionTraceabilityEntry entry : instructionEntries) {
 				EList<ValueForStructuralFeature> values = entry.getFeatures().get(feature.getName());
 				if (values != null) {
 					for (ValueForStructuralFeature value : values) {
 						Object compiledValue = getCompiledValue(indexEntry, value);
 						boolean isNativeValueEquals = value instanceof NativeValueForStructuralFeature
								&& diffValue.toString().equals(compiledValue);
						if (isNativeValueEquals
								|| ((value instanceof ReferenceValueForStructuralFeature || value instanceof NewObjectValueForStructuralFeature) && diffValue
										.equals(compiledValue))) {
 							return value;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Computes the compiled value from a given {@link ValueForStructuralFeature}.
 	 * 
 	 * @param indexEntry
 	 *            the index entry
 	 * @param value
 	 *            the modeling unit value
 	 * @return the compiled value
 	 */
 	private static Object getCompiledValue(TraceabilityIndexEntry indexEntry, ValueForStructuralFeature value) {
 		Object res = null;
 		switch (value.eClass().getClassifierID()) {
 			case ModelingUnitPackage.NATIVE_VALUE_FOR_STRUCTURAL_FEATURE:
 				res = ((NativeValueForStructuralFeature)value).getValue().replaceAll("\"", "");
 				break;
 			case ModelingUnitPackage.REFERENCE_VALUE_FOR_STRUCTURAL_FEATURE:
 				InstanciationInstruction referencedInstanciation = ((ReferenceValueForStructuralFeature)value)
 						.getReferencedElement().getReferencedElement();
 				if (referencedInstanciation != null) {
 					res = getCompiledElement(indexEntry, referencedInstanciation);
 				} else {
 					res = ((ReferenceValueForStructuralFeature)value).getReferencedMetaType();
 				}
 				break;
 			case ModelingUnitPackage.NEW_OBJECT_VALUE_FOR_STRUCTURAL_FEATURE:
 				InstanciationInstruction instanciation = ((NewObjectValueForStructuralFeature)value)
 						.getValue();
 				if (instanciation != null) {
 					res = getCompiledElement(indexEntry, instanciation);
 				}
 				break;
 			default:
 				break;
 		}
 		return res;
 	}
 
 	/**
 	 * Retrieves the compiled element from the given instantiation.
 	 * 
 	 * @param indexEntry
 	 *            the index entry
 	 * @param instantiation
 	 *            the element instantiation
 	 * @return the compiled element
 	 */
 	private static EObject getCompiledElement(TraceabilityIndexEntry indexEntry,
 			InstanciationInstruction instantiation) {
 		for (Entry<EObject, EList<InstructionTraceabilityEntry>> entry : indexEntry
 				.getContainedElementToInstructions().entrySet()) {
 			for (InstructionTraceabilityEntry instructionEntry : entry.getValue()) {
 				IntentGenericElement instruction = instructionEntry.getInstruction();
 				if (instruction instanceof InstanciationInstruction && instantiation.equals(instruction)) {
 					return entry.getKey();
 				}
 			}
 		}
 		return null;
 	}
 }
