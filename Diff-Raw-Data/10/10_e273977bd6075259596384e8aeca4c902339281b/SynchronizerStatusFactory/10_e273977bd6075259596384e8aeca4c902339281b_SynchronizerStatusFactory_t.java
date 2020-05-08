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
 import org.eclipse.emf.compare.diff.metamodel.AttributeChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.AttributeChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.AttributeOrderChange;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffPackage;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceOrderChange;
 import org.eclipse.emf.compare.diff.metamodel.UpdateAttribute;
 import org.eclipse.emf.compare.diff.metamodel.UpdateReference;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.compiler.InstructionTraceabilityEntry;
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
 			SynchronizerCompilationStatus status = CompilerFactory.eINSTANCE
 					.createSynchronizerCompilationStatus();
 
 			if (difference instanceof ReferenceOrderChange) {
 				status.setSeverity(CompilationStatusSeverity.INFO);
 				status.setType(CompilationMessageType.SYNCHRONIZER_INFO);
 			} else {
 				status.setSeverity(CompilationStatusSeverity.WARNING);
 				status.setType(CompilationMessageType.SYNCHRONIZER_WARNING);
 			}
 
 			IntentGenericElement targetInstruction = getTargetInstructionFromDiffElement(indexEntry,
 					difference);
 			status.setMessage(SynchronizerMessageProvider.createMessageFromDiffElement(difference));
 			status.setWorkingCopyResourceURI(indexEntry.getResourceDeclaration().getUri().toString());
 			status.setCompiledResourceURI(indexEntry.getGeneratedResourcePath());
 
 			// experimental: we provide a fix only in this case
 			if (difference instanceof ModelElementChangeRightTarget) {
 				ModelElementChangeRightTarget change = (ModelElementChangeRightTarget)difference;
 				status.setWorkingCopyElementURIFragment(change.getRightElement().eResource()
 						.getURIFragment(change.getRightElement()));
 			}
 
 			if (targetInstruction != null) {
 				status.setTarget(targetInstruction);
 				statusList.add(status);
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
 	 * Returns the instruction that defined the target of the given diffElement ; if not element found,
 	 * returns the resourceDeclaration that defined this element.
 	 * 
 	 * @param indexEntry
 	 *            the indexEntry currently visited
 	 * @param difference
 	 *            the {@link DiffElement} describing the differences between an element of the internal
 	 *            generated model and the element of an external generated model
 	 * @return the instruction that defined the target of the given diffElement ; if not element found,
 	 *         returns the resourceDeclaration that defined this element
 	 */
 	private static IntentGenericElement getTargetInstructionFromDiffElement(
 			TraceabilityIndexEntry indexEntry, DiffElement difference) {
 		IntentGenericElement targetInstruction = null;
 		EObject compiledElement = null;
 
 		switch (difference.eClass().getClassifierID()) {
 
 			case DiffPackage.UPDATE_REFERENCE:
 				UpdateReference updateReference = (UpdateReference)difference;
 				compiledElement = updateReference.getLeftElement();
 				targetInstruction = getInstructionFromAffectation(indexEntry, compiledElement,
 						updateReference.getReference(), compiledElement.eGet(updateReference.getReference()));
 				break;
 
 			case DiffPackage.UPDATE_ATTRIBUTE:
 				UpdateAttribute updateAttribute = (UpdateAttribute)difference;
 				compiledElement = updateAttribute.getLeftElement();
 				targetInstruction = getInstructionFromAffectation(indexEntry, compiledElement,
 						updateAttribute.getAttribute(), compiledElement.eGet(updateAttribute.getAttribute()));
 				break;
 
 			case DiffPackage.ATTRIBUTE_CHANGE_RIGHT_TARGET:
 				AttributeChangeRightTarget attributeChangeRightTarget = (AttributeChangeRightTarget)difference;
 				compiledElement = attributeChangeRightTarget.getLeftElement();
 				targetInstruction = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				break;
 
 			case DiffPackage.ATTRIBUTE_CHANGE_LEFT_TARGET:
 				AttributeChangeLeftTarget attributeChangeLeftTarget = (AttributeChangeLeftTarget)difference;
 				compiledElement = attributeChangeLeftTarget.getLeftElement();
 				targetInstruction = getInstructionFromAffectation(indexEntry, compiledElement,
 						attributeChangeLeftTarget.getAttribute(), attributeChangeLeftTarget.getLeftTarget());
 				break;
 
 			case DiffPackage.REFERENCE_CHANGE_RIGHT_TARGET:
 				ReferenceChangeRightTarget referenceChangeRightTarget = (ReferenceChangeRightTarget)difference;
 				compiledElement = referenceChangeRightTarget.getLeftElement();
 				targetInstruction = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				break;
 
 			case DiffPackage.REFERENCE_CHANGE_LEFT_TARGET:
 				ReferenceChangeLeftTarget referenceChangeLeftTarget = (ReferenceChangeLeftTarget)difference;
 				compiledElement = referenceChangeLeftTarget.getLeftElement();
 				targetInstruction = getInstructionFromAffectation(indexEntry, compiledElement,
 						referenceChangeLeftTarget.getReference(), referenceChangeLeftTarget.getLeftTarget());
 				break;
 
 			case DiffPackage.MODEL_ELEMENT_CHANGE_LEFT_TARGET:
 				compiledElement = ((ModelElementChangeLeftTarget)difference).getLeftElement();
 				targetInstruction = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				break;
 
 			case DiffPackage.MODEL_ELEMENT_CHANGE_RIGHT_TARGET:
 				compiledElement = ((ModelElementChangeRightTarget)difference).getLeftParent();
 				targetInstruction = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				break;
 
 			case DiffPackage.ATTRIBUTE_ORDER_CHANGE:
 				compiledElement = ((AttributeOrderChange)difference).getLeftElement();
 				targetInstruction = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				break;
 
 			case DiffPackage.REFERENCE_ORDER_CHANGE:
 				compiledElement = ((ReferenceOrderChange)difference).getLeftElement();
 				targetInstruction = getInstructionFromCompiledElement(indexEntry, compiledElement);
 				break;
 
 			case DiffPackage.RESOURCE_DIFF:
 				// TODO
 				break;
 
 			default:
 				break;
 		}
 
 		// If no instruction has been found, we associated the status with the currently compiled resource
 		if (targetInstruction == null) {
 			System.err.println("CANNOT FIND ANY INSTRUCTION FOR " + difference.eClass().getName() + ": "
 					+ difference);
 			targetInstruction = indexEntry.getResourceDeclaration();
 		}
 
 		return targetInstruction;
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
		return getInstructionFromCompiledElement(indexEntry, compiledElement);
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
