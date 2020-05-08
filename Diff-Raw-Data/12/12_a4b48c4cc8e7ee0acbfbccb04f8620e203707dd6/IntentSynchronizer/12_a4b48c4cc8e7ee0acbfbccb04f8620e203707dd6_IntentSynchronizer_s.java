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
 package org.eclipse.mylyn.docs.intent.client.synchronizer.synchronizer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.Monitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.compare.diff.metamodel.DiffElement;
 import org.eclipse.emf.compare.diff.metamodel.DiffModel;
 import org.eclipse.emf.compare.diff.service.DiffService;
 import org.eclipse.emf.compare.match.metamodel.MatchModel;
 import org.eclipse.emf.compare.match.service.MatchService;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.factory.SynchronizerMessageProvider;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.factory.SynchronizerStatusFactory;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.listeners.GeneratedElementListener;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.strategy.CopyInternalResourceStrategy;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.strategy.SynchronizerStrategy;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;
 import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
 
 /**
  * In charge of comparing the compiled models of the repository with the compiled models generated at the
  * location indicated by a Intent ResourceDeclaration.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentSynchronizer {
 
 	/**
 	 * The Synchronizer strategy to use, defining several behaviors in case of conflict.
 	 */
 	private SynchronizerStrategy synchronizerStrategy;
 
 	/**
 	 * Listens generated elements.
 	 */
 	private GeneratedElementListener generatedElementListener;
 
 	/**
 	 * IntentSynchronizer constructor.
 	 */
 	public IntentSynchronizer() {
 		this.synchronizerStrategy = new CopyInternalResourceStrategy();
 	}
 
 	/**
 	 * Sets the Synchronizer strategy to use.
 	 * 
 	 * @param strategy
 	 *            the Synchronizer strategy to use
 	 */
 	public void setSynchronizerStrategy(SynchronizerStrategy strategy) {
 		this.synchronizerStrategy = strategy;
 	}
 
 	/**
 	 * Sets the generatedElement listener, which will notify the Synchronizer if any generatedElement has
 	 * changed.
 	 * 
 	 * @param generatedElementListener
 	 *            the GeneratedElementListener
 	 */
 	public void setGeneratedElementListener(GeneratedElementListener generatedElementListener) {
 		this.generatedElementListener = generatedElementListener;
 	}
 
 	/**
 	 * Using the given traceability index, compares the compiled models of the repository with the compiled
 	 * models generated at the location indicated by this index ; if the two models aren't similar, adds a
 	 * status representing the differences to the returned list of status.
 	 * 
 	 * @param adapter
 	 *            the repositoryAdapter to use for getting the repository content
 	 * @param tracabilityIndex
 	 *            the Traceability index to use
 	 * @param progressMonitor
 	 *            the progress Monitor indicating if this synchronization operation has been canceled
 	 * @return a list containing status relatives to synchronization
 	 * @throws InterruptedException
 	 *             if this operation was interrupted
 	 */
 	public Collection<? extends CompilationStatus> synchronize(RepositoryAdapter adapter,
 			TraceabilityIndex tracabilityIndex, Monitor progressMonitor) throws InterruptedException {
 		List<CompilationStatus> statusList = new ArrayList<CompilationStatus>();
 		if (generatedElementListener != null) {
 			generatedElementListener.clearElementToListen();
 		}
 		Iterator<TraceabilityIndexEntry> indexEntryIterator = tracabilityIndex.getEntries().iterator();
 		while (indexEntryIterator.hasNext()) {
 			this.stopIfCanceled(progressMonitor);
			TraceabilityIndexEntry indexEntry = indexEntryIterator.next();
 			// First of all, we clear the old synchronization statuses
			clearSyncStatusesFromIndexEntry(indexEntry);
 
 			// We do not synchronize abstract resources (i.e. resources with no associated URI)
 			if (indexEntry.getResourceDeclaration().getUri() != null) {
 				// We then generate the synchronization status for this entry
 				statusList.addAll(synchronize(adapter, indexEntry, progressMonitor));
 			}
 		}
 		return statusList;
 	}
 
 	/**
 	 * Clears the synchronization statues associated to the given indexEntry (i.e in the ResourceDeclaration
 	 * and the instructions that describes the generated content).
 	 * 
 	 * @param indexEntry
 	 *            the index Entry containing the instructions to clear
 	 */
 	private void clearSyncStatusesFromIndexEntry(TraceabilityIndexEntry indexEntry) {
 
 		// We first remove the synchronization statutes associated to the resource Declaration
 		Iterator<CompilationStatus> statusIterator = indexEntry.getResourceDeclaration()
 				.getCompilationStatus().iterator();
 
 		while (statusIterator.hasNext()) {
 			CompilationStatus status = statusIterator.next();
 			if (status.getType().equals(CompilationMessageType.SYNCHRONIZER_WARNING)) {
 				statusIterator.remove();
 			}
 		}
 		// Then, for each mapped element
 		for (EObject containedElement : indexEntry.getContainedElementToInstructions().keySet()) {
 
 			// We must remove the synchronization statuses from the instruction that generated this element
 			IntentGenericElement instruction = indexEntry.getContainedElementToInstructions().get(
 					containedElement);
 			if (instruction != null) {
 				Iterator<CompilationStatus> iterator = instruction.getCompilationStatus().iterator();
 				while (iterator.hasNext()) {
 					CompilationStatus status = iterator.next();
 					if (status.getType().equals(CompilationMessageType.SYNCHRONIZER_WARNING)) {
 						iterator.remove();
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Using the given TraceabilitIndexEntry, compares the model located at the indicated path with the model
 	 * located at the path indicated by the resource declaration.
 	 * 
 	 * @param adapter
 	 *            the repositoryAdapter to use for getting the repository content
 	 * @param indexEntry
 	 *            the indexEntry to use for obtaining synchronization informations
 	 * @param progressMonitor
 	 *            the progress Monitor indicating if this synchronization operation has been canceled
 	 * @return a list of status relatives to synchronization of the model described in the given indexEntry
 	 * @throws InterruptedException
 	 *             if this operation was interrupted
 	 */
 	private Collection<? extends CompilationStatus> synchronize(RepositoryAdapter adapter,
 			TraceabilityIndexEntry indexEntry, Monitor progressMonitor) throws InterruptedException {
 		List<CompilationStatus> statusList = new ArrayList<CompilationStatus>();
 		boolean continueSynchronization = true;
 
 		Resource internalResource = null;
 		// Step 1 : getting the repository resource
 		stopIfCanceled(progressMonitor);
 		internalResource = getInternalResource(adapter, indexEntry);
 
 		Resource externalResource = null;
 		stopIfCanceled(progressMonitor);
 		// Step 2 : getting the generated resource
 		externalResource = getExternalResource(indexEntry);
 
 		stopIfCanceled(progressMonitor);
 		// Step 3 : if one of the resource is null,
 		// we use the strategy to handle these cases
 		if (internalResource == null) {
 			internalResource = synchronizerStrategy.handleNullInternalResource(
 					indexEntry.getGeneratedResourcePath(), externalResource);
 			// TODO : we can create here a status if the internal Resource has not been created
 			continueSynchronization = internalResource != null;
 		}
 		if (externalResource == null) {
 			externalResource = synchronizerStrategy.handleNullExternalResource(indexEntry
 					.getResourceDeclaration(), internalResource, (String)indexEntry.getResourceDeclaration()
 					.getUri());
 			// TODO : we can create here a status if the external Resource has not been created
 			continueSynchronization = externalResource != null;
 		}
 
 		stopIfCanceled(progressMonitor);
 		// If no resource was null or if the strategy authorizes the operation to continue
 		if (continueSynchronization) {
 
 			// Step 4 : comparing those two resources
 			Resource leftResource = synchronizerStrategy.getLeftResource(internalResource, externalResource);
 			Resource rightResource = synchronizerStrategy
 					.getRightResource(internalResource, externalResource);
 
 			List<DiffElement> differences = null;
 			stopIfCanceled(progressMonitor);
 			differences = compareResource(leftResource, rightResource);
 
 			stopIfCanceled(progressMonitor);
 			// Step 5 : creating status from the DiffElement
 			statusList = createSynchronizerSatusListFromDiffModel(indexEntry, differences, progressMonitor);
 
 			// Step 6 : unloading the external resource
 			externalResource.unload();
 
 			// Step 7 : we ask the generated element listener to listen to the external Resource
 			if (this.generatedElementListener != null) {
 				this.generatedElementListener.addElementToListen(externalResource.getURI());
 			}
 		} else {
 			stopIfCanceled(progressMonitor);
 			// TODO we can imagine creating a status, unless it's the responsability of the Strategy
 
 		}
 
 		return statusList;
 	}
 
 	/**
 	 * Ensure the current synchronization operation (represented by the given monitor) hasn't been canceled ;
 	 * if so, throws an InterruptedException.
 	 * 
 	 * @param progressMonitor
 	 *            the progressMonitor to use for determining if the operation has been canceled
 	 * @throws InterruptedException
 	 *             if the operation has been canceled
 	 */
 	private void stopIfCanceled(Monitor progressMonitor) throws InterruptedException {
 		if (progressMonitor.isCanceled()) {
 			throw new InterruptedException();
 		}
 
 	}
 
 	/**
 	 * Creates the synchronization statuses corresponding to the given list of {@link DiffElement}.
 	 * 
 	 * @param indexEntry
 	 *            the indexEntry indicating the compared resources
 	 * @param differences
 	 *            the list of {@link DiffElement} between the compared resources
 	 * @param progressMonitor
 	 *            the progress Monitor indicating if this synchronization operation has been canceled
 	 * @return a list containing the synchronization statuses corresponding to the given list of
 	 *         {@link DiffElement}
 	 * @throws InterruptedException
 	 *             if the operation has been canceled
 	 */
 	private List<CompilationStatus> createSynchronizerSatusListFromDiffModel(
 			TraceabilityIndexEntry indexEntry, List<DiffElement> differences, Monitor progressMonitor)
 			throws InterruptedException {
 		Map<IntentGenericElement, CompilationStatus> elementToSyncStatus = new HashMap<IntentGenericElement, CompilationStatus>();
 		List<CompilationStatus> statusList = new ArrayList<CompilationStatus>();
 
 		for (DiffElement difference : differences) {
 			stopIfCanceled(progressMonitor);
 			// For each synchronization status relative to the consider diffElement
 			for (CompilationStatus newStatus : SynchronizerStatusFactory.createStatusFromDiffElement(
 					indexEntry, difference)) {
 				stopIfCanceled(progressMonitor);
 
 				// If the target element has no defined synchronization satus
 				if (elementToSyncStatus.get(newStatus.getTarget()) == null) {
 					elementToSyncStatus.put(newStatus.getTarget(), newStatus);
 					statusList.add(newStatus);
 				} else {
 					// If the target element has already a synchronization status
 					// We construct a new status corresponding to the old
 					// one and the new one
 					CompilationStatus oldStatus = elementToSyncStatus.get(newStatus.getTarget());
 					statusList.remove(oldStatus);
 					oldStatus.setMessage(oldStatus.getMessage()
 							+ SynchronizerMessageProvider.getStatusSeparator() + newStatus.getMessage());
 					statusList.add(oldStatus);
 				}
 			}
 		}
 
 		return statusList;
 	}
 
 	/**
 	 * Return the resource containing the compiled model currently inspected (<b>internal</b> resource of the
 	 * repository).
 	 * 
 	 * @param adapter
 	 *            the repositoryAdapter to use for getting the repository content
 	 * @param indexEntry
 	 *            the indexEntry indicating the location of the compiled resource
 	 * @return the resource containing the compiled model currently inspected (<b>internal</b> resource of the
 	 *         repository)
 	 */
 	private Resource getInternalResource(RepositoryAdapter adapter, TraceabilityIndexEntry indexEntry) {
 		return adapter.getResource(indexEntry.getGeneratedResourcePath());
 	}
 
 	/**
 	 * Returns the resource containing the compiled model currently inspected (<b>external</b> resource of the
 	 * repository : can be in a workspace, on internet...).
 	 * 
 	 * @param indexEntry
 	 *            the indexEntry indicating the location of the compiled resource
 	 * @return the resource containing the compiled model currently inspected (<b>external</b> resource of the
 	 *         repository) - can be null if the resource doesn't exist.
 	 */
 	private Resource getExternalResource(TraceabilityIndexEntry indexEntry) {
 		ResourceSet resourceSet = new ResourceSetImpl();
 		Resource resource = null;
 		if (indexEntry.getResourceDeclaration() != null) {
 			String uri = (String)indexEntry.getResourceDeclaration().getUri();
 			uri = uri.replace("\"", "");
 			URI externalURI = URI.createURI(uri);
 
 			try {
 				resource = resourceSet.getResource(externalURI, true);
 			} catch (WrappedException e) {
 				resource = null;
 			}
 		}
 		return resource;
 	}
 
 	/**
 	 * Compares the roots of the given resources and return a list of differences.
 	 * 
 	 * @param leftResource
 	 *            the <i>"left"</i> resource of the two resources to get compared.
 	 * @param rightResource
 	 *            the <i>"right"</i> resource of the two resources to get compared.
 	 * @return a list of diffElement corresponding to all the differences between the left resources and the
 	 *         right resource
 	 * @throws InterruptedException
 	 *             if the comparison is interrupted
 	 */
 	private List<DiffElement> compareResource(Resource leftResource, Resource rightResource)
 			throws InterruptedException {
 
 		try {
 			// TODO : treat differently models and meta-models : this match isn't efficient on
 			// simple meta-models instances
 			MatchModel matchModel = MatchService.doResourceMatch(leftResource, rightResource,
 					new HashMap<String, Object>());
 			DiffModel diff = DiffService.doDiff(matchModel, false);
 			return diff.getDifferences();
 			// CHECKSTYLE:OFF
 		} catch (Exception e) {
 			// CHECKSTYLE :ON
 			// TODO create a Status which has the left resource has target
 			return new ArrayList<DiffElement>();
 		}
 
 	}
 
 }
