 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.alm.mylyn.core.utils;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jubula.client.alm.mylyn.core.Activator;
 import org.eclipse.jubula.client.alm.mylyn.core.i18n.Messages;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.mylyn.commons.net.AuthenticationType;
 import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
 import org.eclipse.mylyn.tasks.core.IRepositoryManager;
 import org.eclipse.mylyn.tasks.core.ITaskMapping;
 import org.eclipse.mylyn.tasks.core.RepositoryResponse;
 import org.eclipse.mylyn.tasks.core.TaskMapping;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
 import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
 import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
 import org.eclipse.mylyn.tasks.core.data.TaskData;
 import org.eclipse.mylyn.tasks.ui.TasksUi;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author BREDEX GmbH
  */
 public final class ALMAccess {
     /**
      * @author BREDEX GmbH
      */
     public static class ALMDefaultTaskMapping extends TaskMapping {
         /** m_values */
         private Map<String, String> m_values = new HashMap<String, String>();
 
         @Override
         public String getDescription() {
             return m_values.get(TaskAttribute.DESCRIPTION);
         }
 
         @Override
         public String getSummary() {
             return m_values.get(TaskAttribute.SUMMARY);
         }
 
         @Override
         public String getProduct() {
             return m_values.get(TaskAttribute.PRODUCT);
         }
         
         @Override
         public String getTaskKind() {
             return m_values.get(TaskAttribute.TASK_KIND);
         }
     }
     
     /** the logger */
     private static final Logger LOG = LoggerFactory.getLogger(ALMAccess.class);
 
     /** Constructor */
     private ALMAccess() {
         // hide
     }
 
     /**
      * @param repoLabel
      *            the label of the repository
      * @return the task repository or <code>null</code> if not found
      */
     public static TaskRepository getRepositoryByLabel(String repoLabel) {
         List<TaskRepository> allRepositories = getAllRepositories();
 
         for (TaskRepository repo : allRepositories) {
             if (repo.getRepositoryLabel().equals(repoLabel)) {
                 return repo;
             }
         }
         return null;
     }
 
     /**
      * @return a list of all available task repositories
      */
     public static List<TaskRepository> getAllRepositories() {
         IRepositoryManager repositoryManager = TasksUi.getRepositoryManager();
         return repositoryManager.getAllRepositories();
     }
 
     /**
      * @param repo
      *            the task repository
      * @param taskId
      *            the taskId
      * @param monitor
      *            the monitor to use
      * @return the tasks data or <code>null</code> if not found
      * @throws CoreException
      *             in case of a problem
      */
     private static TaskData getTaskDataByID(TaskRepository repo, String taskId,
             IProgressMonitor monitor) throws CoreException {
         TaskData taskData = null;
         if (repo != null && !repo.isOffline()) {
             AbstractRepositoryConnector connector = TasksUi
                     .getRepositoryConnector(repo.getConnectorKind());
             taskData = connector.getTaskData(repo, taskId, monitor);
         }
         return taskData;
     }
 
     /**
      * @param repoLabel
      *            repoLabel
      * @param taskId
      *            the taskId
      * @param comment
      *            the comment
      * @param monitor
      *            the monitor to use
      * @return true if succeeded; false otherwise
      */
     public static boolean createComment(String repoLabel, String taskId,
             String comment, IProgressMonitor monitor) {
         boolean succeeded = false;
         TaskRepository repo = getRepositoryByLabel(repoLabel);
         try {
             TaskData taskData = getTaskDataByID(repo, taskId, monitor);
             if (taskData != null) {
                 AbstractRepositoryConnector connector = TasksUi
                         .getRepositoryConnector(repo.getConnectorKind());
                 AbstractTaskDataHandler taskDataHandler = connector
                         .getTaskDataHandler();
                 TaskAttribute root = taskData.getRoot();
                 TaskAttribute newComment = root
                         .createMappedAttribute(TaskAttribute.COMMENT_NEW);
                 newComment.setValue(comment);
                 RepositoryResponse response = taskDataHandler.postTaskData(
                         repo, taskData, null, monitor);
                 succeeded = RepositoryResponse.ResponseKind.TASK_UPDATED
                         .equals(response.getReposonseKind());
             }
         } catch (CoreException e) {
             LOG.error(e.getLocalizedMessage(), e);
         }
         return succeeded;
     }
 
     /**
      * @param repoLabel
      *            repoLabel
      * @param taskId
      *            the taskId
      * @param taskAttributeId
      *            the id of the attribute to retrieve
      * @return the value or null if not found
      */
     public static String getTaskAttributeValue(String repoLabel, String taskId,
             String taskAttributeId) {
         String value = null;
         TaskRepository repo = getRepositoryByLabel(repoLabel);
         try {
             TaskData taskData = getTaskDataByID(repo, taskId,
                     new NullProgressMonitor());
             if (taskData != null) {
                 TaskAttribute root = taskData.getRoot();
                 TaskAttributeMapper attributeMapper = taskData
                         .getAttributeMapper();
                 TaskAttribute mappedAttribute = root
                         .getMappedAttribute(taskAttributeId);
                 if (mappedAttribute != null) {
                     value = attributeMapper.getValue(mappedAttribute);
                 }
             }
         } catch (CoreException e) {
             LOG.error(e.getLocalizedMessage(), e);
         }
         return value;
     }
 
     /**
      * @param repoLabel
      *            repoLabel
      * @param product
      *            the product
      * @param summary
      *            the summary
      * @param description
      *            the description
      * @return true if succeeded; false otherwise
      */
     public static boolean createNewTask(String repoLabel, String product,
         String summary, String description) {
         boolean succeeded = false;
         TaskRepository repo = getRepositoryByLabel(repoLabel);
         AbstractRepositoryConnector connector = TasksUi
                 .getRepositoryConnector(repo.getConnectorKind());
         
         AbstractTaskDataHandler taskDataHandler = connector
                 .getTaskDataHandler();
         
         // e.g. when local has been chosen
         if (taskDataHandler == null) {
             return succeeded;
         }
         
         TaskAttributeMapper attributeMapper = taskDataHandler
                 .getAttributeMapper(repo);
         
         TaskData newTask = new TaskData(attributeMapper,
                 repo.getConnectorKind(), 
                 repo.getRepositoryUrl(), 
                 StringConstants.EMPTY);
         
         ITaskMapping taskMapping = new ALMDefaultTaskMapping();
         try {
             
             TaskAttribute newTaskRoot = newTask.getRoot();
             
             TaskAttribute summaryAttribute = newTaskRoot
                     .createMappedAttribute(TaskAttribute.SUMMARY);
             TaskAttribute descriptionAttribute = newTaskRoot
                     .createMappedAttribute(TaskAttribute.DESCRIPTION);
             
             // JIRA - start
             newTaskRoot.createMappedAttribute(TaskAttribute.TASK_KIND);
             newTaskRoot.createMappedAttribute(TaskAttribute.STATUS);
             TaskAttribute productAttribute = newTaskRoot
                     .createMappedAttribute(TaskAttribute.PRODUCT);
             // JIRA - end
 
             taskDataHandler.initializeTaskData(repo, newTask, taskMapping,
                     new NullProgressMonitor());
 
             attributeMapper.setValue(descriptionAttribute, description);
             attributeMapper.setValue(summaryAttribute, summary);
             attributeMapper.setValue(productAttribute, product);
             
             RepositoryResponse response = taskDataHandler.postTaskData(repo,
                     newTask, null, new NullProgressMonitor());
             succeeded = RepositoryResponse.ResponseKind.TASK_CREATED
                     .equals(response .getReposonseKind());
         } catch (CoreException e) {
             LOG.error(e.getLocalizedMessage(), e);
         }
         return succeeded;
     }
 
     /**
      * @param repoLabel
      *            the repository to test the connection for
      * @return a status reflecting the current connection state
      */
     public static IStatus testConnection(String repoLabel) {
         TaskRepository repository = getRepositoryByLabel(repoLabel);
         if (repository == null) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryNotFound, repoLabel));
         }
         if (repository.isOffline()) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryOffline, repoLabel));
         }
         
         boolean savePassword = repository
                 .getSavePassword(AuthenticationType.REPOSITORY);
         if (!savePassword) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryNoCredentialsStored, repoLabel));
         }
         
         AbstractRepositoryConnector connector = TasksUi
                 .getRepositoryConnector(repository.getConnectorKind());
         if (connector == null) {
             return new Status(IStatus.ERROR, Activator.ID, NLS.bind(
                     Messages.TaskRepositoryNoCredentialsStored, repoLabel));
         }
         
         try {
             connector.updateRepositoryConfiguration(repository,
                     new NullProgressMonitor());
         } catch (CoreException e) {
             return new Status(IStatus.ERROR, Activator.ID,
                    e.getLocalizedMessage().replace("\n\n", " ")); //$NON-NLS-1$ //$NON-NLS-2$
         }
         
         IStatus repoStatus = repository.getStatus();
         if (repoStatus != null) {
             return repoStatus;
         }
         return Status.OK_STATUS;
     }
 }
