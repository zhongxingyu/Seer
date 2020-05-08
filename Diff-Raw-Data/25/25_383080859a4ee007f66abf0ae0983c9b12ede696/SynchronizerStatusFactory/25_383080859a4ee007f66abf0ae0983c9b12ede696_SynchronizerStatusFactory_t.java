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
 import org.eclipse.emf.compare.AttributeChange;
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceKind;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.mylyn.docs.intent.core.compiler.AttributeChangeStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.compiler.InstructionTraceabilityEntry;
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
  * This factory is in charge of creating {@link CompilationStatus} from different elements ( {@link Diff} of a
  * Comparison, error on a ResourceDeclaration...).
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public final class SynchronizerStatusFactory {
 
 	/**
 	 * SynchronizerStatusFactory constructor.
 	 */
 	private SynchronizerStatusFactory() {
 	}
 
 	/**
 	 * Create a list of compilationStatus from the given {@link Diff}.
 	 * 
 	 * @param indexEntry
 	 *            the indexEntry currently visited
 	 * @param difference
 	 *            the {@link Diff} describing the differences between an element of the internal generated
 	 *            model and the element of an external generated model
 	 * @return a list of compilationStatus created from the given {@link Diff}
 	 */
 	public static List<CompilationStatus> createStatusFromDiff(TraceabilityIndexEntry indexEntry,
 			Diff difference) {
 		List<CompilationStatus> statusList = new ArrayList<CompilationStatus>();
 
 		SynchronizerCompilationStatus status = null;
		if (difference.getMatch().getLeft() != null) { // ignoring diffs related to non existing elements
 			if (difference instanceof AttributeChange) {
 				status = createStatusFromAttributeChange(indexEntry, (AttributeChange)difference);
 			} else if (difference instanceof ReferenceChange) {
 				status = createStatusFromReferenceChange(indexEntry, (ReferenceChange)difference);
 			}
 
 			if (status != null) {
 				if (difference.getKind().equals(DifferenceKind.MOVE)) {
 					status.setSeverity(CompilationStatusSeverity.INFO);
 				} else {
 					status.setSeverity(CompilationStatusSeverity.WARNING);
 				}
 				status.setType(CompilationMessageType.SYNCHRONIZER_WARNING);
 				status.setMessage(SynchronizerMessageProvider.createMessageFromDiff(difference));
 				status.setWorkingCopyResourceURI(indexEntry.getResourceDeclaration().getUri().toString());
 				status.setCompiledResourceURI(indexEntry.getGeneratedResourcePath());
 
 				statusList.add(status);
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
 	private static ReferenceChangeStatus createStatusFromReferenceChange(TraceabilityIndexEntry indexEntry,
 			ReferenceChange difference) {
 		EObject compiledElement = difference.getMatch().getLeft();
 		IntentGenericElement target = null;
 		ReferenceChangeStatus status = CompilerFactory.eINSTANCE.createReferenceChangeStatus();
 		status.setCompiledElement(compiledElement);
 		status.setFeatureName(difference.getReference().getName());
 		status.setWorkingCopyElementURIFragment(createURIFragment(difference.getMatch().getRight()));
 		status.setChangeState(convertDifferenceKindToState(difference.getKind()));
 
 		target = getInstructionFromAffectation(indexEntry, compiledElement, difference.getReference(),
 				difference.getValue());
 
 		if (target instanceof NewObjectValueForStructuralFeature && !target.eContents().isEmpty()) {
 			EObject content = target.eContents().get(0);
 			if (content instanceof InstanciationInstruction) {
 				target = (InstanciationInstruction)content;
 			}
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
 		EObject compiledElement = difference.getMatch().getLeft();
 		IntentGenericElement target = null;
 		AttributeChangeStatus status = CompilerFactory.eINSTANCE.createAttributeChangeStatus();
 		status.setCompiledElement(compiledElement);
 		status.setFeatureName(difference.getAttribute().getName());
 		status.setWorkingCopyElementURIFragment(createURIFragment(difference.getMatch().getRight()));
 		status.setChangeState(convertDifferenceKindToState(difference.getKind()));
 
 		target = getInstructionFromAffectation(indexEntry, compiledElement, difference.getAttribute(),
 				difference.getValue());
 
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
 								&& diffValue != null && diffValue.toString().equals(compiledValue);
 						boolean isRefValue = value instanceof ReferenceValueForStructuralFeature
 								|| value instanceof NewObjectValueForStructuralFeature;
 						boolean refValueEquals = (diffValue == null && compiledValue == null)
 								|| (diffValue != null && diffValue.equals(compiledValue));
 						boolean isRefValueEquals = isRefValue && refValueEquals;
 						if (isNativeValueEquals || isRefValueEquals) {
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
 
 	/**
 	 * Converts a difference kind to a state.
 	 * 
 	 * @param differenceKind
 	 *            the difference kind
 	 * @return the state
 	 */
 	private static SynchronizerChangeState convertDifferenceKindToState(DifferenceKind differenceKind) {
 		SynchronizerChangeState state = null;
 		switch (differenceKind.getValue()) {
 			case DifferenceKind.ADD_VALUE:
 				state = SynchronizerChangeState.WORKING_COPY_TARGET;
 				break;
 
 			case DifferenceKind.MOVE_VALUE:
 				state = SynchronizerChangeState.ORDER;
 				break;
 
 			case DifferenceKind.DELETE_VALUE:
 				state = SynchronizerChangeState.COMPILED_TARGET;
 				break;
 
 			case DifferenceKind.CHANGE_VALUE:
 				state = SynchronizerChangeState.UPDATE;
 				break;
 
 			default:
 				break;
 		}
 		return state;
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
 
 }
