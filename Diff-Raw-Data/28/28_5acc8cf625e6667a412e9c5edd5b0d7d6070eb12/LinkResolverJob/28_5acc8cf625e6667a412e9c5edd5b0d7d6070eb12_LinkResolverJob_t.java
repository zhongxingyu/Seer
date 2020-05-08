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
 package org.eclipse.mylyn.docs.intent.client.linkresolver.repository;
 
 import com.google.common.collect.Lists;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.mylyn.docs.intent.client.linkresolver.resolver.LinkResolver;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
 import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.SaveException;
 
 /**
  * A job that will triger the link resolving of all references to Intent Structured Element (sections,
  * chapters...).
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class LinkResolverJob extends Job {
 
 	private static final String LINK_RESOLVER_JOB_NAME = "Resolving references inside Intent Document";
 
 	/**
 	 * The repository Adapter to use for manipulating the Intent repository.
 	 */
 	private RepositoryAdapter repositoryAdapter;
 
 	private LinkResolver linkResolver;
 
 	/**
 	 * Creates a new {@link LinkResolverJob}.
 	 * 
 	 * @param repositoryAdapter
 	 *            the repository Adapter to use for manipulating the Intent repository
 	 */
 	public LinkResolverJob(RepositoryAdapter repositoryAdapter) {
 		super(LINK_RESOLVER_JOB_NAME);
 		this.repositoryAdapter = repositoryAdapter;
 		this.linkResolver = new LinkResolver(repositoryAdapter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	protected IStatus run(final IProgressMonitor monitor) {
 		repositoryAdapter.execute(new IntentCommand() {
 
 			public void execute() {
				try {
					repositoryAdapter.openSaveContext();
					linkResolver.resolve(monitor);
					repositoryAdapter.setSendSessionWarningBeforeSaving(Lists
							.newArrayList(IntentLocations.INTENT_FOLDER));
					repositoryAdapter.save();
				} catch (ReadOnlyException e) {
					IntentLogger.getInstance().log(LogType.ERROR,
							"Failed to resolve links inside the Intent Document: insufficiant rights");
				} catch (SaveException e) {
					IntentLogger.getInstance().log(LogType.ERROR,
							"Failed to resolve links inside the Intent Document:" + e.getMessage());
				}
 			}
 		});
 		return Status.OK_STATUS;
 	}
 
 }
