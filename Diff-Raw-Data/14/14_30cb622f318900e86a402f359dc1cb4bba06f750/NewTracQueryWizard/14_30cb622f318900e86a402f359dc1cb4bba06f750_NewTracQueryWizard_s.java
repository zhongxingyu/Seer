 /*******************************************************************************
  * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Mylar project committers - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.internal.trac.ui.wizard;
 
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.mylar.internal.tasks.ui.TaskListImages;
 import org.eclipse.mylar.tasks.core.AbstractRepositoryConnector;
 import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;
 import org.eclipse.mylar.tasks.core.TaskRepository;
 import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
 
 /**
  * @author Steffen Pingel
  */
 public class NewTracQueryWizard extends Wizard {
 
 	private static final String TITLE = "New Trac Query";
 
 	private final TaskRepository repository;
 
 	private TracCustomQueryPage queryPage;
 
 	public NewTracQueryWizard(TaskRepository repository) {
 		this.repository = repository;
 
 		setNeedsProgressMonitor(true);
 		setWindowTitle(TITLE);
 		setDefaultPageImageDescriptor(TaskListImages.BANNER_REPOSITORY);
 	}
 
 	@Override
 	public void addPages() {
 		queryPage = new TracCustomQueryPage(repository);
 		queryPage.setWizard(this);
 		addPage(queryPage);
 	}
 
 	@Override
 	public boolean canFinish() {
		if (queryPage.getNextPage() == null) {
			return queryPage.isPageComplete();
		}
		return queryPage.getNextPage().isPageComplete();
 	}
 
 	@Override
 	public boolean performFinish() {
 		AbstractRepositoryQuery query = queryPage.getQuery();
 		if (query != null) {
 			TasksUiPlugin.getTaskListManager().getTaskList().addQuery(query);
 			AbstractRepositoryConnector connector = TasksUiPlugin.getRepositoryManager().getRepositoryConnector(
 					repository.getKind());
 			if (connector != null) {
 				TasksUiPlugin.getSynchronizationManager().synchronize(connector, query, null);
 			}
 		}
 		return true;
 	}
 
 }
