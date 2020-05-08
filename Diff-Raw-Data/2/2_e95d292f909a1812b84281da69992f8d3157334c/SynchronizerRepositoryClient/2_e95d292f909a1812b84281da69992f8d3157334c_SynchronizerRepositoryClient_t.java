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
 package org.eclipse.mylyn.docs.intent.client.synchronizer;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.listeners.GeneratedElementListener;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.synchronizer.IntentSynchronizer;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusManager;
 import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerCompilationStatus;
 import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
 
 /**
  * In charge of communication between the repository and the synchronizer ; launch a synchronization operation
  * each time a modification on the compiler's generated elements index is detected.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class SynchronizerRepositoryClient extends AbstractRepositoryClient {
 
 	/**
 	 * The synchronizer to use.
 	 */
 	private IntentSynchronizer synchronizer;
 
 	/**
 	 * The listened TraceAbilityIndex.
 	 */
 	private TraceabilityIndex traceabilityIndex;
 
 	/**
 	 * The {@link CompilationStatusManager} to use for adding statuses.
 	 */
 	private CompilationStatusManager statusManager;
 
 	/**
 	 * SynchronizerRepositoryClient constructor.
 	 * 
 	 * @param traceabilityIndex
 	 *            the listened {@link TraceabilityIndex}
 	 * @param statusManager
 	 *            the {@link CompilationStatusManager} to use for adding statuses
 	 */
 	public SynchronizerRepositoryClient(TraceabilityIndex traceabilityIndex,
 			CompilationStatusManager statusManager) {
 		IntentLogger.getInstance().log(LogType.LIFECYCLE, "[Synchronizer] Ready");
 		this.synchronizer = new IntentSynchronizer(this);
 		this.traceabilityIndex = traceabilityIndex;
 		this.statusManager = statusManager;
 	}
 
 	/**
 	 * Adds all the given compilationStatus to their targets instructions.
 	 * 
 	 * @param statusList
 	 *            the list of status to add
 	 */
 	public void addAllStatusToTargetElement(final Collection<? extends CompilationStatus> statusList) {
 
 		// Step 1: removing all old synchronization status
 		Iterator<SynchronizerCompilationStatus> iterator2 = Iterables.filter(
 				statusManager.getCompilationStatusList(), SynchronizerCompilationStatus.class).iterator();
 		Collection<SynchronizerCompilationStatus> toRemove = Sets.newLinkedHashSet();
 		while (iterator2.hasNext()) {
 			SynchronizerCompilationStatus oldStatus = iterator2.next();
 			if (oldStatus.getTarget() != null) {
 				oldStatus.getTarget().getCompilationStatus().remove(oldStatus);
 			}
 			statusManager.getModelingUnitToStatusList().remove(oldStatus);
 			toRemove.add(oldStatus);
 		}
 		statusManager.getCompilationStatusList().removeAll(toRemove);
 
 		// Step 2 : for each status to add
 		for (CompilationStatus status : statusList) {
 			// We add it to its target and to the status manager
 			if (status.getTarget() != null) {
 				status.getTarget().getCompilationStatus().add(status);
 				statusManager.getCompilationStatusList().add(status);
 			}
 		}
 	}
 
 	/**
 	 * Sets the generatedElement listener, which will notify the Synchronizer if any generatedElement has
 	 * changed.
 	 * 
 	 * @param generatedElementListener
 	 *            the GeneratedElementListener
 	 */
 	public void setGeneratedElementListener(GeneratedElementListener generatedElementListener) {
 		synchronizer.setGeneratedElementListener(generatedElementListener);
 		generatedElementListener.setSynchronizer(this);
 
 	}
 
 	public TraceabilityIndex getTraceabilityIndex() {
 		return traceabilityIndex;
 	}
 
 	public void setTraceabilityIndex(TraceabilityIndex traceabilityIndex) {
 		this.traceabilityIndex = traceabilityIndex;
 	}
 
 	IntentSynchronizer getSynchronizer() {
 		return synchronizer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient#createNotificationJob(org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification)
 	 */
 	@Override
 	protected Job createNotificationJob(RepositoryChangeNotification notification) {
 		return new SynchronizeRepositoryJob(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient#dispose()
 	 */
 	@Override
 	public void dispose() {
 		synchronizer.dispose();
 		super.dispose();
 	}
 }
