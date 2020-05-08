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
 package org.eclipse.mylyn.docs.intent.collab.common.query;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
 import org.eclipse.mylyn.docs.intent.core.compiler.InstructionTraceabilityEntry;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 import org.eclipse.mylyn.docs.intent.core.genericunit.UnitInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstructionReference;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstruction;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstructionReference;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ReferenceValueForStructuralFeature;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.StructuralFeatureAffectation;
 
 /**
  * An utility class allowing to query the {@link TraceabilityIndex} to get useful traceability informations.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class TraceabilityInformationsQuery extends AbstractIntentQuery {
 
 	private TraceabilityIndex traceabilityIndex;
 
 	/**
 	 * Creates the query.
 	 * 
 	 * @param repositoryAdapter
 	 *            the {@link RepositoryAdapter} to use for querying the repository.
 	 */
 	public TraceabilityInformationsQuery(RepositoryAdapter repositoryAdapter) {
 		super(repositoryAdapter);
 	}
 
 	/**
 	 * Returns the {@link TraceabilityIndex} of the queried repository. If none find, creates it.
 	 * 
 	 * @return the {@link TraceabilityIndex} index of the queried repository. If none find, creates it
 	 */
 	public TraceabilityIndex getOrCreateTraceabilityIndex() {
 		if (traceabilityIndex == null) {
 			try {
 				final Resource traceabilityResource = repositoryAdapter
 						.getOrCreateResource(IntentLocations.TRACEABILITY_INFOS_INDEX_PATH);
 
 				if (traceabilityResource.getContents().isEmpty()) {
 					repositoryAdapter.execute(new IntentCommand() {
 
 						public void execute() {
 							traceabilityResource.getContents().add(
 									CompilerFactory.eINSTANCE.createTraceabilityIndex());
 						}
 					});
 				}
 				traceabilityIndex = (TraceabilityIndex)traceabilityResource.getContents().get(0);
 			} catch (ReadOnlyException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return traceabilityIndex;
 	}
 
 	/**
 	 * Returns all the {@link ContributionInstruction}s related to the same element than the given
 	 * {@link UnitInstruction}.
 	 * 
 	 * @param instruction
 	 *            the instruction to consider (can be whether an InstanciationInstruction or a
 	 *            {@link ContributionInstruction}.
 	 * @return all the {@link ContributionInstruction}s related to the given {@link UnitInstruction}
 	 */
 	public Collection<ContributionInstruction> getAllRelatedContributions(UnitInstruction instruction) {
 		return Sets.newLinkedHashSet(Iterables.filter(getAllRelatedInstructions(instruction),
 				ContributionInstruction.class));
 	}
 
 	/**
 	 * Returns the instanciation associated to the given element.
 	 * 
 	 * @param instance
 	 *            the instance element
 	 * @return the instanciation associated to the given element
 	 */
 	public InstanciationInstruction getInstanciation(EObject instance) {
 		for (TraceabilityIndexEntry entry : getOrCreateTraceabilityIndex().getEntries()) {
 			EList<InstructionTraceabilityEntry> instructions = entry.getContainedElementToInstructions().get(
 					instance);
 			if (instructions != null) {
 				for (InstructionTraceabilityEntry instructionTraceabilityEntry : instructions) {
 					if (instructionTraceabilityEntry.getInstruction() instanceof InstanciationInstruction) {
 						return (InstanciationInstruction)instructionTraceabilityEntry.getInstruction();
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the instanciations.
 	 * 
 	 * @return the instanciations list
 	 */
 	public List<InstanciationInstruction> getInstanciations() {
 		List<InstanciationInstruction> instanciations = new ArrayList<InstanciationInstruction>();
 		for (TraceabilityIndexEntry entry : getOrCreateTraceabilityIndex().getEntries()) {
 			Collection<EList<InstructionTraceabilityEntry>> mapValues = entry
 					.getContainedElementToInstructions().values();
 			if (mapValues != null) {
 				for (EList<InstructionTraceabilityEntry> instructions : mapValues) {
 					for (InstructionTraceabilityEntry instructionTraceabilityEntry : instructions) {
 						if (instructionTraceabilityEntry.getInstruction() instanceof InstanciationInstruction) {
 							instanciations.add((InstanciationInstruction)instructionTraceabilityEntry
 									.getInstruction());
 						}
 					}
 				}
 
 			}
 		}
 		return instanciations;
 	}
 
 	/**
 	 * Returns the instance associated to the given instanciation.
 	 * 
 	 * @param instanciation
 	 *            the instanciation
 	 * @return the instance associated to the given instanciation
 	 */
 	public EObject getInstance(InstanciationInstruction instanciation) {
 		for (TraceabilityIndexEntry entry : getOrCreateTraceabilityIndex().getEntries()) {
 			for (Entry<EObject, EList<InstructionTraceabilityEntry>> instructionsEntry : entry
 					.getContainedElementToInstructions()) {
 				for (InstructionTraceabilityEntry instructionTraceabilityEntry : instructionsEntry.getValue()) {
 					if (instructionTraceabilityEntry.getInstruction().equals(instanciation)) {
 						return instructionsEntry.getKey();
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return the ModelingUnitInstructionReference which refers to the given instanciation if present.
 	 * 
 	 * @param instanciation
 	 *            the instanciation
 	 * @return the ModelingUnitInstructionReference or null
 	 */
 	public ModelingUnitInstructionReference getModelingUnitInstructionReference(
 			InstanciationInstruction instanciation) {
 		for (TraceabilityIndexEntry entry : getOrCreateTraceabilityIndex().getEntries()) {
 			for (ModelingUnitInstructionReference reference : entry.getResourceDeclaration().getContent()) {
 				if (instanciation.equals(reference.getReferencedInstruction())) {
 					return reference;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns all the {@link ModelingUnitInstruction}s related to the same element than the given
 	 * {@link UnitInstruction}.
 	 * 
 	 * @param instruction
 	 *            the instruction to consider (can be whether an InstanciationInstruction or a
 	 *            {@link ContributionInstruction}.
 	 * @return all the {@link ContributionInstruction}s related to the given {@link UnitInstruction}
 	 */
 	private Collection<ModelingUnitInstruction> getAllRelatedInstructions(UnitInstruction instruction) {
 		Collection<ModelingUnitInstruction> relatedInstructions = Sets.newLinkedHashSet();
 		boolean foundContributions = false;
 		for (Iterator<TraceabilityIndexEntry> traceabilityIterator = getOrCreateTraceabilityIndex()
 				.getEntries().iterator(); traceabilityIterator.hasNext() && !foundContributions;) {
 			TraceabilityIndexEntry entry = (TraceabilityIndexEntry)traceabilityIterator.next();
 
 			for (Iterator<Entry<EObject, EList<InstructionTraceabilityEntry>>> iterator = entry
 					.getContainedElementToInstructions().iterator(); iterator.hasNext()
 					&& !foundContributions;) {
 				Entry<EObject, EList<InstructionTraceabilityEntry>> element = (Entry<EObject, EList<InstructionTraceabilityEntry>>)iterator
 						.next();
 
 				final Set<IntentGenericElement> instructions = new HashSet<IntentGenericElement>(element
 						.getValue().size());
 				for (InstructionTraceabilityEntry instructionEntry : element.getValue()) {
 					instructions.add(instructionEntry.getInstruction());
 				}
 
 				if (instructions.contains(instruction)) {
 					relatedInstructions.addAll(Sets.newLinkedHashSet(Iterables.filter(instructions,
 							ContributionInstruction.class)));
 					foundContributions = true;
 				}
 			}
 
 		}
 		return relatedInstructions;
 	}
 
 	/**
 	 * Returns the {@link InstanciationInstruction} corresponding to the given {@link ModelingUnitInstruction}
 	 * .
 	 * 
 	 * @param instruction
 	 *            a {@link ModelingUnitInstruction}
 	 * @return the {@link InstanciationInstruction} corresponding to the given {@link ModelingUnitInstruction}
 	 */
 	public InstanciationInstruction getInstanciationInstruction(ModelingUnitInstruction instruction) {
 		InstanciationInstruction instancationInstruction = null;
 		ModelingUnitInstruction mostSpecificInstruction = instruction;
 		if (mostSpecificInstruction instanceof InstanciationInstruction) {
 			instancationInstruction = (InstanciationInstruction)mostSpecificInstruction;
 		} else if (mostSpecificInstruction instanceof ContributionInstruction) {
 			if (((ContributionInstruction)mostSpecificInstruction).getContributionReference() != null
 					&& ((ContributionInstruction)mostSpecificInstruction).getContributionReference()
 							.getReferencedInstruction() instanceof InstanciationInstruction) {
 				instancationInstruction = (InstanciationInstruction)((ContributionInstruction)mostSpecificInstruction)
 						.getContributionReference().getReferencedInstruction();
 
 			}
 		} else if (mostSpecificInstruction instanceof StructuralFeatureAffectation) {
 			if (((StructuralFeatureAffectation)mostSpecificInstruction).getValues().size() > 0
 					&& ((StructuralFeatureAffectation)mostSpecificInstruction).getValues().iterator().next() instanceof ReferenceValueForStructuralFeature) {
 				mostSpecificInstruction = (ReferenceValueForStructuralFeature)((StructuralFeatureAffectation)instruction)
 						.getValues().iterator().next();
 			}
 		}
 		if (mostSpecificInstruction instanceof ReferenceValueForStructuralFeature
 				&& ((ReferenceValueForStructuralFeature)mostSpecificInstruction).getInstanciationReference() instanceof InstanciationInstructionReference) {
 			instancationInstruction = ((ReferenceValueForStructuralFeature)mostSpecificInstruction)
 					.getInstanciationReference().getInstanciation();
 		}
 		return instancationInstruction;
 	}
 
 	/**
 	 * Returns the {@link URI} of the working copy Resource corresponding to the given
 	 * {@link ModelingUnitInstruction}. If the given parameter is true, then we will return the URI of the
 	 * compiled Resource if no working copy resource is found.
 	 * 
 	 * @param instruction
 	 *            the {@link ModelingUnitInstruction} to query
 	 * @param ifNoneFoundReturnCompiledResource
 	 *            indicates the behavior if no working copy resource is found: if true then we will return the
 	 *            URI of the compiled Resource if no working copy resource is found; if false, then null will
 	 *            be returned in that case.
 	 * @return the {@link URI} of the working copy Resource corresponding to the given
 	 *         {@link ModelingUnitInstruction}
 	 */
 	public URI getWorkingCopyResourceURI(ModelingUnitInstruction instruction,
 			boolean ifNoneFoundReturnCompiledResource) {
 		URI workingCopyResourceURI = null;
 
 		// Step 1: find the traceability index entry corresponding to the given instruction
 		InstanciationInstruction instanciationInstruction = getInstanciationInstruction(instruction);
 		if (instanciationInstruction != null) {
 			TraceabilityIndexEntry matchingTraceabilityEntry = null;
 			for (TraceabilityIndexEntry entry : getOrCreateTraceabilityIndex().getEntries()) {
 				for (Entry<EObject, EList<InstructionTraceabilityEntry>> instructionsEntry : entry
 						.getContainedElementToInstructions()) {
 					for (InstructionTraceabilityEntry instructionTraceabilityEntry : instructionsEntry
 							.getValue()) {
 						if (instructionTraceabilityEntry.getInstruction().equals(instanciationInstruction)) {
 							matchingTraceabilityEntry = entry;
 							break;
 						}
 					}
 				}
 			}
			if (matchingTraceabilityEntry != null) {
 				// Step 2: get the working copy resource URI from the resource declaration (if any)
 				if (matchingTraceabilityEntry.getResourceDeclaration().getUri() != null) {
 					workingCopyResourceURI = URI.createURI(matchingTraceabilityEntry.getResourceDeclaration()
 							.getUri().toString().replace("\"", ""));
 				} else {
 					// If no working copy resource URI was find, return the compiled resource URI
 					if (ifNoneFoundReturnCompiledResource) {
 						workingCopyResourceURI = repositoryAdapter.getResource(
 								matchingTraceabilityEntry.getGeneratedResourcePath().replace("\"", ""))
 								.getURI();
 					}
 				}
 			}
 		}
 		return workingCopyResourceURI;
 	}
 }
