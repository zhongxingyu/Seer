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
 package org.eclipse.mylyn.docs.intent.client.ui.ide.navigator;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.mylyn.docs.intent.collab.common.IntentRepositoryManager;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.ReadWriteRepositoryObjectHandlerImpl;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListNotificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 
 /**
  * A Repository Client used by the IDE bridge to refresh the project explorer when the Indexer computes a new
  * Table of Contents.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class ProjectExplorerRefresher extends AbstractRepositoryClient {
 
	private static final long UPDATE_PROBLEM_VIEW_JOB_DELAY = 1500;
 
 	/**
 	 * The project to refresh.
 	 */
 	private IProject project;
 
 	private Job updateProblemViewJob;
 
 	/**
 	 * Creates a new {@link ProjectExplorerRefresher}.
 	 * 
 	 * @param project
 	 *            the project to refresh
 	 */
 	public ProjectExplorerRefresher(IProject project) {
 		this.project = project;
 		updateProblemView();
 	}
 
 	/**
 	 * Creates a new {@link ProjectExplorerRefresher}, that will refresh the project explorer when the Indexer
 	 * computes a new Index for the given repository.
 	 * 
 	 * @param project
 	 *            the project that will be refreshed by this {@link ProjectExplorerRefresher} (must match a
 	 *            Repository)
 	 * @return a new {@link ProjectExplorerRefresher}
 	 * @throws RepositoryConnectionException
 	 *             if cannot correctly connect to the given repository
 	 * @throws CoreException
 	 *             if needed the repository type cannot be found
 	 */
 	public static ProjectExplorerRefresher createProjectExplorerRefresher(IProject project)
 			throws RepositoryConnectionException, CoreException {
 		// Step 1 : Create a Repository Adapter
 		Repository repository = IntentRepositoryManager.INSTANCE.getRepository(project.getName());
 		final RepositoryAdapter repositoryAdapter = repository.createRepositoryAdapter();
 
 		// Step 2 : Creating the RepositoryObjectHandler for this client
 		RepositoryObjectHandler handler = new ReadWriteRepositoryObjectHandlerImpl(repositoryAdapter);
 		// listening to the Intent Index
 		Set<EObject> listenedElements = new LinkedHashSet<EObject>();
		Resource repositoryIntentResource = repositoryAdapter.getResource(IntentLocations.GENERAL_INDEX_PATH);
		listenedElements.addAll(repositoryIntentResource.getContents());
 		Notificator listenedElementsNotificator = new ElementListNotificator(listenedElements,
 				repositoryAdapter);
 		handler.addNotificator(listenedElementsNotificator);
 
 		// Step 4 : create the ProjectExplorer refresher
 		ProjectExplorerRefresher refresher = new ProjectExplorerRefresher(project);
 		refresher.addRepositoryObjectHandler(handler);
 		return refresher;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.impl.AbstractRepositoryClient#createNotificationJob(org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification)
 	 */
 	@Override
 	protected Job createNotificationJob(RepositoryChangeNotification notification) {
 		Job res = null;
 		if (notification != null && notification.getRightRoots().isEmpty()) {
 			res = new ProjectExplorerRefreshJob(project, notification.getRightRoots().iterator().next());
 		} else {
 			res = new ProjectExplorerRefreshJob(project, null);
 		}
 		updateProblemView();
 		return res;
 	}
 
 	/**
 	 * Updates the problem view (my creating markers for each compilation or synchronization status associated
 	 * to the current document.
 	 */
 	private void updateProblemView() {
 		if (updateProblemViewJob != null) {
 			updateProblemViewJob.cancel();
 		}
 		if (this.getRepositoryObjectHandler() != null
 				&& this.getRepositoryObjectHandler().getRepositoryAdapter() != null) {
 			updateProblemViewJob = new UpdateProblemsViewJob(project, this.getRepositoryObjectHandler()
 					.getRepositoryAdapter());
 			updateProblemViewJob.schedule(UPDATE_PROBLEM_VIEW_JOB_DELAY);
 		}
 	}
 }
