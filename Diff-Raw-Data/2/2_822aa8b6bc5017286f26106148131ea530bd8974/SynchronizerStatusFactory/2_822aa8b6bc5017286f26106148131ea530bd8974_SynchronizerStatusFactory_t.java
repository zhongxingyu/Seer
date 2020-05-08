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
 
 import org.eclipse.emf.compare.diff.metamodel.AttributeChange;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffPackage;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ModelElementChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChange;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeLeftTarget;
 import org.eclipse.emf.compare.diff.metamodel.ReferenceChangeRightTarget;
 import org.eclipse.emf.compare.diff.metamodel.UpdateAttribute;
 import org.eclipse.emf.compare.diff.metamodel.UpdateReference;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 
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
 
 			status.setSeverity(CompilationStatusSeverity.WARNING);
 			status.setType(CompilationMessageType.SYNCHRONIZER_WARNING);
 			IntentGenericElement targetInstruction = getTargetInstructionFromDiffElement(indexEntry,
 					difference);
 			status.setMessage(SynchronizerMessageProvider.createMessageFromDiffElement(difference));
 			status.setWorkingCopyResourceURI(indexEntry.getResourceDeclaration().getUri().toString());
 			status.setCompiledResourceURI(indexEntry.getGeneratedResourcePath());
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
 		// We get the compiled Element target of this diffElement
 		EObject compiledElement = getCompiledElementTargetFromDiffElement(difference);
 		IntentGenericElement targetInstruction = null;
 
 		if (compiledElement != null) {
 			// We use the traceabilityElementIndex to determine which instruction has defined this element
 			EObject compiledContainer = compiledElement;
 
 			while ((targetInstruction == null) && (compiledContainer != null)
 					&& (!(compiledContainer instanceof Resource))) {
 				targetInstruction = getInstructionFromCompiledElement(indexEntry, compiledContainer);
 				compiledContainer = compiledContainer.eContainer();
 			}
 		}
 		// If no instruction has been found, we associated the status with the currently compiled resource
 		if (targetInstruction == null) {
 			System.err.println("CANNOT FIND ANY INSTRUCTION FOR " + compiledElement);
 			targetInstruction = indexEntry.getResourceDeclaration();
 		}
 
 		return targetInstruction;
 	}
 
 	/**
 	 * Returns the target of the given DiffElement.
 	 * 
 	 * @param difference
 	 *            the DiffElement to inspect
 	 * @return the target of the given DiffElement
 	 */
 	private static EObject getCompiledElementTargetFromDiffElement(DiffElement difference) {
 		EObject compiledElementTarget = null;
 
 		switch (difference.eClass().getClassifierID()) {
 			case DiffPackage.ATTRIBUTE_CHANGE:
 				compiledElementTarget = ((AttributeChange)difference).getLeftElement();
 				break;
 
 			case DiffPackage.REFERENCE_CHANGE:
 				compiledElementTarget = ((ReferenceChange)difference).getLeftElement();
 				break;
 
 			case DiffPackage.UPDATE_ATTRIBUTE:
 				compiledElementTarget = ((UpdateAttribute)difference).getLeftElement();
 				break;
 			case DiffPackage.UPDATE_REFERENCE:
 				compiledElementTarget = ((UpdateReference)difference).getLeftElement();
 				break;
 
 			case DiffPackage.MODEL_ELEMENT_CHANGE_LEFT_TARGET:
 				compiledElementTarget = ((ModelElementChangeLeftTarget)difference).getLeftElement();
 				break;
 			case DiffPackage.MODEL_ELEMENT_CHANGE_RIGHT_TARGET:
 				compiledElementTarget = ((ModelElementChangeRightTarget)difference).getLeftParent();
 				break;
 			case DiffPackage.REFERENCE_CHANGE_LEFT_TARGET:
 				compiledElementTarget = ((ReferenceChangeLeftTarget)difference).getLeftElement();
 				break;
 			case DiffPackage.REFERENCE_CHANGE_RIGHT_TARGET:
				compiledElementTarget = ((ReferenceChangeRightTarget)difference).getLeftElement();
 				break;
 			case DiffPackage.RESOURCE_DIFF:
 				// TODO
 				break;
 			default:
 				break;
 
 		}
 
 		return compiledElementTarget;
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
 		return indexEntry.getContainedElementToInstructions().get(compiledElement);
 	}
 
 }
