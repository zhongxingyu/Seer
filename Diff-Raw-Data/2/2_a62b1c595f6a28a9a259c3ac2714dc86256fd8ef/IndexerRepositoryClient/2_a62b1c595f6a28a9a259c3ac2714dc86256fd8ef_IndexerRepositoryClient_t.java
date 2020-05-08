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
 package org.eclipse.mylyn.docs.intent.client.indexer;
 
 import com.google.common.collect.Lists;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.mylyn.docs.intent.client.indexer.tocmaker.TocMaker;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.collab.common.query.IndexQuery;
 import org.eclipse.mylyn.docs.intent.collab.common.query.IntentDocumentQuery;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.SaveException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
 import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndex;
 
 /**
  * When notified about modifications on the listened elements, update the index.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class IndexerRepositoryClient extends AbstractRepositoryClient {
 
 	/**
 	 * Entity used to compute index from a IntentDocument.
 	 */
 	private TocMaker indexComputor;
 
 	/**
 	 * Indexer constructor.
 	 */
 	public IndexerRepositoryClient() {
 		indexComputor = new TocMaker();
 		IntentLogger.getInstance().log(LogType.LIFECYCLE, "[Indexer] Ready");
 	}
 
 	/**
 	 * Replace the repository index content with the repository document's table of contents.
 	 */
 	public void makeToc() {
 		final RepositoryAdapter repositoryAdapter = repositoryObjectHandler.getRepositoryAdapter();
 		if (repositoryAdapter != null) {
 			repositoryAdapter.execute(new IntentCommand() {
 
 				public void execute() {
 					final IntentIndex index = new IndexQuery(repositoryAdapter).getOrCreateIntentIndex();
 					final IntentDocument document = new IntentDocumentQuery(repositoryAdapter)
 							.getOrCreateIntentDocument();
 					IntentLogger.getInstance().log(LogType.LIFECYCLE,
							"[Indexer] Indexing " + document.getChapters().size() + " chapters");
 
 					try {
 						repositoryAdapter.openSaveContext();
 						indexComputor.computeIndex(index, document);
 
 						repositoryAdapter.setSendSessionWarningBeforeSaving(Lists
 								.newArrayList(IntentLocations.INTENT_FOLDER));
 						repositoryAdapter.save();
 					} catch (SaveException e) {
 						IntentLogger.getInstance().log(LogType.ERROR, "Indexer failed to save changes", e);
 					} catch (ReadOnlyException e) {
 						IntentLogger.getInstance().log(LogType.ERROR, "Indexer failed to save changes", e);
 					}
 					repositoryAdapter.closeContext();
 					IntentLogger.getInstance().log(LogType.LIFECYCLE, "[Indexer] Index saved");
 				}
 			});
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient#createNotificationJob(org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification)
 	 */
 	@Override
 	protected Job createNotificationJob(final RepositoryChangeNotification notification) {
 		Job job = new Job("Indexing") {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				IStatus res = Status.OK_STATUS;
 				makeToc();
 				return res;
 			}
 		};
 		job.setSystem(true);
 		return job;
 	}
 
 }
