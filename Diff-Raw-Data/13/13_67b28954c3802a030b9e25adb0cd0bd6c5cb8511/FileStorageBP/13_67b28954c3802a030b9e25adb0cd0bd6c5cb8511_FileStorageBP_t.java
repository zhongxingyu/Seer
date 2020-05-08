 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.archive.businessprocess;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 
 import org.apache.commons.lang.Validate;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jubula.client.archive.XmlStorage;
 import org.eclipse.jubula.client.archive.errorhandling.IProjectNameConflictResolver;
 import org.eclipse.jubula.client.archive.errorhandling.NullProjectNameConflictResolver;
 import org.eclipse.jubula.client.archive.i18n.Messages;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesDecorator;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.INameMapper;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameCache;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.ParamNameBP;
 import org.eclipse.jubula.client.core.businessprocess.ParamNameBPDecorator;
 import org.eclipse.jubula.client.core.businessprocess.ProjectComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.ProjectNameBP;
 import org.eclipse.jubula.client.core.businessprocess.UsedToolkitBP;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.businessprocess.progress.ProgressMonitorTracker;
 import org.eclipse.jubula.client.core.errorhandling.ErrorMessagePresenter;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.persistence.CompNamePM;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.ISpecPersistable;
 import org.eclipse.jubula.client.core.persistence.IncompatibleTypeException;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.PMReadException;
 import org.eclipse.jubula.client.core.persistence.PMSaveException;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.core.progress.IProgressConsole;
 import org.eclipse.jubula.toolkit.common.businessprocess.ToolkitSupportBP;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.utils.ToolkitUtils;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.ConfigXmlException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.exception.JBVersionException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.jarutils.IVersion;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.xml.businessmodell.CompSystem;
 import org.eclipse.jubula.tools.xml.businessmodell.ToolkitPluginDescriptor;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created Jun 4, 2010
  */
 public class FileStorageBP {
 
     /**
      * Reads XML files and parses them into related domain objects.
      * 
      * @author BREDEX GmbH
      * @created Jan 9, 2008
      */
     private static class ReadFilesOperation implements IRunnableWithProgress {
     
         /** indicates what part(s) of the project(s) will be imported */
         private boolean m_isImportWholeProjects;
     
         /** 
          * mapping: projects to import => corresponding param name mapper 
          */
         private Map<IProjectPO, List<INameMapper>> m_projectToMapperMap;
     
         /** 
          * mapping: projects to import => corresponding component name mapper 
          */
         private Map<IProjectPO, List<IWritableComponentNameMapper>> 
         m_projectToCompMapperMap;
     
         /** names of the files to read */
         private List<URL> m_fileURLs;
         
         /** the console to use to display progress and error messages */
         private IProgressConsole m_console;
         
         /**
          * Constructor
          * 
          * @param isImportWholeProjects 
          *              <code>true</code> if entire projects are being imported.
          *              <code>false</code> if only components (TCs, AUTs, etc.)
          *              are being imported.
          * @param fileURLs
          *              URLs of the project files to read.
          * @param console
          *              The console to use to display pogress and 
          *              error messages.
          */
         public ReadFilesOperation(boolean isImportWholeProjects, 
                 List<URL> fileURLs, IProgressConsole console) {
             m_isImportWholeProjects = isImportWholeProjects;
             m_fileURLs = fileURLs;
             m_projectToMapperMap = 
                 new LinkedHashMap<IProjectPO, List<INameMapper>>();
             m_projectToCompMapperMap = 
                 new LinkedHashMap<IProjectPO, 
                     List<IWritableComponentNameMapper>>();
             m_console = console;
         }
         
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) throws InterruptedException {
             if (m_fileURLs == null) {
                 // Nothing to import. Just return.
                 return;
             }
             SubMonitor subMonitor = SubMonitor.convert(
                     monitor, Messages.ImportFileBPReading,
                     m_fileURLs.size());
             try {
                 showStartingImport(m_console);
                 showStartingReadingProjects(m_console);
                 for (URL fileURL : m_fileURLs) {
                     ParamNameBPDecorator paramNameMapper = 
                         new ParamNameBPDecorator(ParamNameBP.getInstance());
                     final IWritableComponentNameCache compNameCache =
                         new ComponentNamesDecorator(null);
                     String fileName = fileURL.getFile();
                     m_console.writeLine(NLS.bind(Messages
                                     .ImportFileActionInfoStartingReadingProject,
                                     fileName));
                     try {
                         IProjectPO proj = new XmlStorage().readProject(
                             fileURL, paramNameMapper, compNameCache, 
                             !m_isImportWholeProjects, subMonitor.newChild(1),
                             m_console);
                         List<INameMapper> mapperList = 
                             new ArrayList<INameMapper>();
                         List<IWritableComponentNameMapper> compNameMapperList = 
                             new ArrayList<IWritableComponentNameMapper>();
                         mapperList.add(paramNameMapper);
                         compNameMapperList.add(new ProjectComponentNameMapper(
                                 compNameCache, proj));
                         m_projectToMapperMap.put(proj, mapperList);
                         m_projectToCompMapperMap.put(proj, compNameMapperList);
                     } catch (JBVersionException e) {
                         for (Object msg : e.getErrorMsgs()) {
                             m_console.writeErrorLine((String)msg);
                         }
                         m_console.writeErrorLine(NLS.bind(
                                 Messages.ImportFileActionErrorImportFailed,
                                 fileName));
                     }
                 }
                 showFinishedReadingProjects(m_console);
             } catch (final PMReadException e) {
                 handlePMReadException(e, m_fileURLs);
             } catch (final ConfigXmlException ce) {
                 handleCapDataNotFound(ce);
             } finally {
                 monitor.done();
             }
         }
     
         /**
          * 
          * @return the projects to import, as read from the project files.
          */
         public Map<IProjectPO, List<INameMapper>> getProjectToMapperMap() {
             return m_projectToMapperMap;
         }
     
         /**
          * 
          * @return the mapping between projects to import and their 
          *         corresponding component name mapper
          */
         public Map<IProjectPO, List<IWritableComponentNameMapper>> 
         getProjectToCompCacheMap() {
         
             return m_projectToCompMapperMap;
         }
     }
 
     /**
      * imports an entire project
      * 
      * @author BREDEX GmbH
      * 
      */
     private static class CompleteImportOperation 
             implements IRunnableWithProgress {
     
         /** mapping: projects to import => corresponding param name mapper */
         private Map<IProjectPO, List<INameMapper>> m_projectToMapperMap;
     
         /** mapping: projects to import => corresponding comp name mapper */
         private Map<IProjectPO, List<IWritableComponentNameMapper>> 
         m_projectToCompCacheMap;
     
         /** whether a refresh is required after import */
         private boolean m_isRefreshRequired = false;
         
         /** whether the import succeeded */
         private boolean m_wasImportSuccessful = false;
 
         /** the console to use for reporting progress and errors */
         private IProgressConsole m_console;
         
         /**
          * constructor 
          * 
          * @param projectToMapperMap
          *            Mapping from projects to import to corresponding param
          *            name mappers.
          * @param projectToCompCacheMap
          *            Mapping from projects to import to corresponding 
          *            component name mappers.
          * @param console
          *              The console to use to display pogress and 
          *              error messages.
          */
         public CompleteImportOperation(
                 Map<IProjectPO, List<INameMapper>> projectToMapperMap, 
                 Map<IProjectPO, List<IWritableComponentNameMapper>> 
                 projectToCompCacheMap, IProgressConsole console) {
     
             m_projectToMapperMap = projectToMapperMap;
             m_projectToCompCacheMap = projectToCompCacheMap;
             m_console = console;
         }
     
         /**
          * 
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) throws InterruptedException {
             SubMonitor subMonitor = SubMonitor.convert(
                     monitor, Messages.ImportFileBPImporting,
                     m_projectToMapperMap.size());
     
             if (checkImportProblems()) {
                 return;
                 
             }
             
             for (final IProjectPO proj : m_projectToMapperMap.keySet()) {
                 if (subMonitor.isCanceled()) {
                     throw new InterruptedException();
                 }
                 String projectName = proj.getDisplayName();
                 showStartingImport(m_console, projectName);
                 try {
                     m_wasImportSuccessful = 
                         importProject(proj, subMonitor.newChild(1));
                     showFinishedImport(m_console, projectName);
                 } catch (PMSaveException e) {
                     LOG.warn(Messages.ErrorWhileImportingProject, e);
                     JBException gde = new JBException(
                         e + StringConstants.SPACE + StringConstants.COLON 
                         + Messages.SaveOf + proj.getName() 
                         + StringConstants.SPACE + Messages.Failed,
                         MessageIDs.E_IMPORT_PROJECT_XML_FAILED);
                     showErrorDuringImport(m_console, projectName, gde);
                     ErrorMessagePresenter.getPresenter().showErrorMessage(
                             gde, new String [] {proj.getName()}, null);
                 } catch (PMException pme) {
                     LOG.warn(Messages.ErrorWhileImportingProject, pme);
                     JBException gde = new JBException(
                         pme + Messages.ImportOf + proj.getName() 
                         + StringConstants.SPACE + Messages.Failed,
                         MessageIDs.E_IMPORT_PROJECT_XML_FAILED);
                     showErrorDuringImport(m_console, projectName, gde);
                     ErrorMessagePresenter.getPresenter().showErrorMessage(
                             gde, new String [] {proj.getName()}, null);
                 } catch (ProjectDeletedException e) {
                     JBException gde = new JBException(
                         e + Messages.ImportOf + proj.getName() 
                         + StringConstants.SPACE + Messages.Failed,
                         MessageIDs.E_ALREADY_DELETED_PROJECT);
                     showErrorDuringImport(m_console, projectName, gde);
                     ErrorMessagePresenter.getPresenter().showErrorMessage(
                             gde, new String [] {proj.getName()}, null);
                 }
             }
             showFinishedImport(m_console);
         }
     
         /**
          * Checks the list of projects to import for problems. Handles problems
          * by displaying an error message to the user.
          * 
          * @return <code>true</code> if a problem was found, meaning that the
          *         operation cannot complete successfully. Otherwise, 
          *         <code>false</code>.
          */
         private boolean checkImportProblems() {
             Map<String, String> guidToNameMap = new HashMap<String, String>();
             if (checkImportedProjects(guidToNameMap)) {
                 return true;
             }
             
             EntityManager circularDependencyCheckSess = 
                 Persistor.instance().openSession();
     
             // if a name/guid conflict occurs
             // then show error message(s) and cancel
             try {
                 if (checkNameGuidConflict(guidToNameMap)) {
                     return true;
                 }
     
                 // check for reusable project problems (circular dependencies)
                 if (checkCircularDependencies(circularDependencyCheckSess)) {
                     return true;
                 }
             } catch (PMException pme) {
                 ErrorMessagePresenter.getPresenter().showErrorMessage(
                     new JBException(
                         pme + Messages.ImportFailed,
                         MessageIDs.E_DATABASE_GENERAL), 
                     null, null);
                 return true;
             } finally {
                 Persistor.instance().dropSessionWithoutLockRelease(
                     circularDependencyCheckSess);
             }
             
             return false;
         }
     
         /**
          * @param circularDependencyCheckSess The session to use for getting
          *                                    projects from the database.
          * @return <code>true</code> if any circular dependencies are found.
          *         Otherwise <code>false</code>.
          */
         private boolean checkCircularDependencies(
                 EntityManager circularDependencyCheckSess) {
             for (final IProjectPO proj : m_projectToMapperMap.keySet()) {
                 Set<IProjectPO> checkedProjects = new HashSet<IProjectPO>();
                 Set<IProjectPO> illegalProjects = new HashSet<IProjectPO>();
                 illegalProjects.add(proj);
                 
                 Set<IProjectPO> projectsToCheck = new HashSet<IProjectPO>();
                 
                 for (IReusedProjectPO reused : proj.getUsedProjects()) {
                     IProjectPO reusedProject = null;
     
                     for (IProjectPO importedProject 
                             : m_projectToMapperMap.keySet()) {
                         
                         if (reused.getProjectGuid().equals(
                                 importedProject.getGuid()) 
                             && reused.getMajorNumber().equals(
                                 importedProject.getMajorProjectVersion()) 
                             && reused.getMinorNumber().equals(
                                 importedProject.getMinorProjectVersion())) {
                             
                             reusedProject = importedProject;
                             break;
                         }
     
                     }
     
                     if (reusedProject == null) {
                         try {
                             reusedProject = ProjectPM.loadReusedProject(
                                     reused, circularDependencyCheckSess);
                         } catch (JBException e) {
                             // We can't detect circular dependencies from a
                             // project if we can't load it from the db.
                             // Report to the user that the error will
                             // cause the import to abort.
                             handleCircularDependency(m_console, proj.getName());
                             return true;
                         }
                     }
                     
                     if (reusedProject != null) {
                         projectsToCheck.add(reusedProject);
                     }
                 }
                 for (IProjectPO projToCheck : projectsToCheck) {
                     ProjectPM.findIllegalProjects(projToCheck, 
                         checkedProjects, illegalProjects, 
                         m_projectToMapperMap.keySet());
                 }
                 
                 illegalProjects.remove(proj);
                 
                 if (!illegalProjects.isEmpty()) {
                     handleCircularDependency(m_console, proj.getName());
                     return true;
                 }
             }
             
             return false;
         }
     
         /**
          * 
          * @return <code>true</code> if the import succeeded. Otherwise 
          *         <code>false</code>
          */
         public boolean wasImportSuccessful() {
             return m_wasImportSuccessful;
         }
     
         /**
          * @param proj
          *            The project to import.
          * @param monitor
          *            The progress monitor for this operation.
          * @return <code>true</code> if the project was successfully imported.
          *         Returns <code>false</code> if their were conflicts that
          *         prevented the project from being successfully imported.
          * @throws PMException
          *             in case of any db error
          * @throws ProjectDeletedException
          *             if project is already deleted
          * @throws InterruptedException
          *             if the operation was canceled
          */
         private boolean importProject(IProjectPO proj, 
             IProgressMonitor monitor) 
             throws PMException, ProjectDeletedException, 
             InterruptedException {
             
             // if (import.guid exists and guid->version == import.version)
             // then show error message and cancel
             if (projectExists(proj.getGuid(), proj.getMajorProjectVersion(),
                 proj.getMinorProjectVersion())) {
                 String projectNameToImport = proj.getName();
                 handleProjectExists(
                     m_console,
                     ProjectNameBP.getInstance().getName(proj.getGuid(), false),
                     projectNameToImport,
                     proj.getMajorProjectVersion(),
                     proj.getMinorProjectVersion());
                 return false;
             }
             String selectedProjectName = 
                 checkProjectAndRename(proj.getGuid(), proj.getName());
             if (selectedProjectName != null) {
                 // Import project
                 proj.setClientMetaDataVersion(
                     IVersion.JB_CLIENT_METADATA_VERSION);
                 boolean willRequireRefresh = false;
                 IProjectPO currentProject = 
                     GeneralStorage.getInstance().getProject();
                 if (currentProject != null) {
                     for (IReusedProjectPO reused 
                         : currentProject.getUsedProjects()) {
     
                         if (m_isRefreshRequired || willRequireRefresh) {
                             break;
                         }
                         String guid = reused.getProjectGuid();
                         Integer majorVersion = reused.getMajorNumber();
                         Integer minorVersion = reused.getMinorNumber();
                         willRequireRefresh = proj.getGuid().equals(guid) 
                             && proj.getMajorProjectVersion().equals(
                                 majorVersion) 
                             && proj.getMinorProjectVersion().equals(
                                 minorVersion);
     
                     }
                     m_isRefreshRequired = willRequireRefresh 
                         || m_isRefreshRequired;
                 }
     
                 // Register Persistence (JPA / EclipseLink) progress listeners
                 ProgressMonitorTracker.getInstance().setProgressMonitor(
                         monitor);
                 monitor.beginTask(StringConstants.EMPTY, getTotalWork(proj));
                 List<INameMapper> mapperList = m_projectToMapperMap.get(proj);
                 List<IWritableComponentNameMapper> compNameBindingList = 
                     m_projectToCompCacheMap.get(proj);
                 try {
                     ProjectPM.saveProject(proj, selectedProjectName, 
                             mapperList, compNameBindingList);
                 } finally {
                     // Remove JPA progress listeners
                     ProgressMonitorTracker.getInstance().setProgressMonitor(
                             null);
                 }
                 UsedToolkitBP.getInstance().refreshToolkitInfo(proj);
                 return true;
             }
             
             return false;
         }
     
         /**
          * 
          * @param proj The project for which to find the required work
          *             amount.
          * @return the amount of work required to save the given project to the
          *         database.
          */
         private int getTotalWork(IProjectPO proj) {
             
             // (project_node=1)
             int totalWork = 1;
             
             // (INodePO=1)
             for (ITestSuitePO testSuite 
                     : TestSuiteBP.getListOfTestSuites(proj)) {
                 
                 totalWork += getWorkForNode(testSuite);
             }
             for (ISpecPersistable spec 
                     : proj.getSpecObjCont().getSpecObjList()) {
                 
                 totalWork += getWorkForNode(spec);
             }
             
             // 1 for each event type
             totalWork *= NUM_HBM_PROGRESS_EVENT_TYPES;
             return totalWork;
         }
     
         /**
          * Recursively determines the amount of work involved in saving the
          * given node to the database.
          * 
          * @param node The node for which to determine the amount of work.
          * @return the amount of work required to save the given node to the 
          *         database.
          */
         private int getWorkForNode(INodePO node) {
             int work = 1;
             if (!(node instanceof IExecTestCasePO)) {
                 Iterator<INodePO> childIter = node.getNodeListIterator();
                 while (childIter.hasNext()) {
                     work += getWorkForNode(childIter.next());
                 }
             }
             
             if (node instanceof ISpecTestCasePO) {
                 work += ((ISpecTestCasePO)node).getAllEventEventExecTC().size();
             }
             
             return work;
         }
         
         /**
          * @param guidToNameMap mapping from project guids to names
          * @return <code>true</code> if any name/guid conflicts are found.
          *         Otherwise <code>false</code>.
          */
         private boolean checkImportedProjects(
             Map<String, String> guidToNameMap) {
             
             for (IProjectPO proj : m_projectToMapperMap.keySet()) {
                 final String projectName = proj.getName();
                 final String guid = proj.getGuid();
                 Validate.notNull(projectName, Messages.ImportWithoutName);
                 Validate.notEmpty(projectName, Messages.ImportEmptyName);
     
                 // Check for name/guid conflicts among the imported projects as
                 // we go.
                 if (isSameGuidOtherName(guidToNameMap, projectName, guid)) {
                     
                     // Same guid, different name
                     handleGuidConflict(projectName, guidToNameMap.get(guid));
                     return true;
                 
                 } else if (isOtherGuidSameName(guidToNameMap, projectName, 
                     guid)) {
                     
                     // Different guid, same name
                     handleNameConflict(projectName);
                     return true;
                 } else {
                     guidToNameMap.put(guid, projectName);
                 }
             }
             return false;
         }
     
         /**
          * Checks if the mapping contains an entry that conflicts with given
          * name and guid.
          * 
          * @param guidToNameMap The mapping to check against.
          * @param projectName The name to check.
          * @param guid The guid to check.
          * @return <code>true</code> if there exists an entry in the mapping
          *         such that name<code>.equals(projectName)</code> and 
          *         not guid<code>.equals(guid)</code>. Otherwise 
          *         <code>false</code>.
          */
         private boolean isOtherGuidSameName(Map<String, String> guidToNameMap, 
             final String projectName, final String guid) {
             
             return guidToNameMap.containsValue(projectName)
                 && !projectName.equals(guidToNameMap.get(guid));
         }
     
         /**
          * Checks if the mapping contains an entry that conflicts with given
          * name and guid.
          * 
          * @param guidToNameMap The mapping to check against.
          * @param projectName The name to check.
          * @param guid The guid to check.
          * @return <code>true</code> if there exists an entry in the mapping
          *         such that not name<code>.equals(projectName)</code> and 
          *         guid<code>.equals(guid)</code>. Otherwise 
          *         <code>false</code>.
          */
         private boolean isSameGuidOtherName(
             Map<String, String> guidToNameMap, final String projectName, 
             final String guid) {
             
             return guidToNameMap.containsKey(guid) 
                     && !projectName.equals(guidToNameMap.get(guid));
         }
     
         /**
          * Creates an error dialog.
          * 
          * @param name The name that is causing the conflict.
          */
         private void handleNameConflict(String name) {
             ErrorMessagePresenter.getPresenter().showErrorMessage(
                 MessageIDs.E_PROJ_NAME_CONFLICT, 
                 new String [] {}, new String [] {name});
         }
     
         /**
          * Creates an error dialog.
          * 
          * @param console
          *              The console to use to display pogress and 
          *              error messages.
          * @param name The name of the project that is causing the problem.
          */
         private void handleCircularDependency(
                 IProgressConsole console, String name) {
             
             console.writeErrorLine(
                     NLS.bind(Messages.ErrorMessagePROJ_CIRC_DEPEND, name));
     
             ErrorMessagePresenter.getPresenter().showErrorMessage(
                     MessageIDs.E_PROJ_CIRC_DEPEND, new String [] {name}, null);
         }
     
         /**
          * Checks that the import will not create any name/GUID conflicts. If
          * a conflict would be caused, this method will attempt to rename the
          * project.
          * 
          * @param guid The guid of the project to check.
          * @param projectName The name of the project to check.
          * @return a name if the import will not cause conflicts
          *         (either because no conflicts existed or because the project
          *         was successfully renamed such that no more conflicts exist).
          *         Otherwise <code>null</code>.
          */
         private String checkProjectAndRename(
             String guid, final String projectName) {
             
             String selectedName = projectName;
             // if (import.guid exists and guid->name != import.name) 
             // then show error message and offer to rename project
             //   : options are guid->name and import.name
             String existingNameForGuid = 
                 ProjectNameBP.getInstance().getName(guid);
             if (existingNameForGuid != null 
                 && !existingNameForGuid.equals(projectName)) {
     
                 if (ProjectPM.doesProjectNameExist(projectName)) {
                     ArrayList<String> possibleNames = new ArrayList<String>(1);
                     possibleNames.add(existingNameForGuid);
                     selectedName = 
                         projectNameConflictResolver.resolveNameConflict(
                                 possibleNames);
                 } else {
                     String [] possibleNames = new String [] {
                         existingNameForGuid, projectName};
                     selectedName = 
                         projectNameConflictResolver.resolveNameConflict(
                                 Arrays.asList(possibleNames));
                 }
             } else if (ProjectPM.doesProjectNameExist(projectName)
                 && !projectName.equals(existingNameForGuid)) {
                 // if (import.name exists and name->guid != import.guid)
                     // then show error message and offer to rename project
 
                 ArrayList<String> possibleNames = new ArrayList<String>(1);
                 possibleNames.add(existingNameForGuid);
                 selectedName = 
                     projectNameConflictResolver.resolveNameConflict(
                             possibleNames);
             }
             return selectedName;
         }
     
         /**
          * Checks whether there are any name/guid conflicts between the given
          * project information and the projects currently existing in the 
          * database.
          * 
          * @param guidToNameMap mapping of imported project guids to names
          * @return <code>true</code> if the given project information contains
          *         any name/guid conflicts. Otherwise <code>false</code>.
          */
         private boolean checkNameGuidConflict(
             Map<String, String> guidToNameMap) throws PMException {
             
             Map<String, String> dbGuidToNameMap = 
                 ProjectNameBP.getInstance().readAllProjectNamesFromDB();
             for (String guid : guidToNameMap.keySet()) {
                 if (isOtherGuidSameName(dbGuidToNameMap, 
                     guidToNameMap.get(guid), guid)) {
                     
                     handleNameConflict(guidToNameMap.get(guid));
                     return true;
                 } else if (isSameGuidOtherName(dbGuidToNameMap, guid, 
                     guidToNameMap.get(guid))) {
                     
                     handleGuidConflict(guidToNameMap.get(guid), 
                         dbGuidToNameMap.get(guid));
                     return true;
                 }
             }
             return false;
         }
     
         /**
          * Displays an error dialog.
          * 
          * @param importName name of the imported proejct causing the guid conflict
          * @param existingName name of the existing project causing the guid conflict
          */
         private void handleGuidConflict(
             String importName, String existingName) {
             
             ErrorMessagePresenter.getPresenter().showErrorMessage(
                     MessageIDs.E_PROJ_GUID_CONFLICT, 
                     new String [0], new String [] {importName, existingName});
         }
         
         /**
          * Checks whether the currently imported project already exists in the
          * database.
          * 
          * @param guid
          *            GUID to check
          * @param majorNumber
          *            Major version number to check
          * @param minorNumber
          *            Minor version number to check
          * @return <code>true</code> if another project with the same GUID and
          *         version number as the currently imported project already 
          *         exists in the database. Otherwise <code>false</code>.
          */
         private boolean projectExists(String guid, Integer majorNumber, 
             Integer minorNumber) {
             
             return ProjectPM.doesProjectVersionExist(guid, majorNumber, 
                 minorNumber);
         }
     
         /**
          * Writes an error to the console.
          * 
          * @param console
          *              The console to use to display pogress and 
          *              error messages.
          * @param existingName 
          *      Name of the project that already exists in the database
          * @param importName 
          *      Name of the project that is being imported
          * @param majNum Project major number
          * @param minNum Project minor number
          */
         private void handleProjectExists(IProgressConsole console, 
                 String existingName, String importName,
                 Integer majNum, Integer minNum) {
             
             console.writeErrorLine(
                     NLS.bind(Messages.ErrorMessageIMPORT_PROJECT_XML_FAILED,
                             new String [] {importName}));
             console.writeErrorLine(NLS.bind(
                     Messages.ErrorMessageIMPORT_PROJECT_XML_FAILED_EXISTING,
                     new String[] { existingName, String.valueOf(majNum),
                             String.valueOf(minNum) }));
         }
     
     }
 
     /**
      * Performs a project import by starting either a complete import operation
      * or a partial import operation, as appropriate.
      * 
      * @author BREDEX GmbH
      * @created Jan 9, 2008
      */
     private static class ImportOperation implements IRunnableWithProgress {
     
         /** indicates what part(s) of the project(s) will be imported */
         private int m_elements;
     
         /** mapping: projects to import => corresponding name mapper List */
         private Map<IProjectPO, List<INameMapper>> m_projectToMapperMap;
     
         /** mapping: projects to import => corresponding comp name cache List */
         private Map<IProjectPO, List<IWritableComponentNameMapper>> 
         m_projectToCompCacheMap;
 
         /** the project to open immediately after import */
         private IProjectPO m_projectToOpen = null;
 
         /** the console used for reporting progress and errors */
         private IProgressConsole m_console;
 
         /** flag for whether to open a project immediately after import */
         private boolean m_isOpenProject;
         
         /**
          * Constructor
          * 
          * @param elements
          *            What to import ? 0 = all >0 = elements
          * @param projectToMapperMap
          *            Mapping from projects to import to corresponding param
          *            name mappers.
          * @param projectToCompCacheMap
          *            Mapping from projects to import to corresponding 
          *            component name mappers.
          * @param console
          *              The console to use to display pogress and 
          *              error messages.
          * @param openProject
          *            Flag indicating whether the imported project should be 
          *            immediately opened after import.
          */
         public ImportOperation(int elements, 
                 Map<IProjectPO, List<INameMapper>> projectToMapperMap, 
                 Map<IProjectPO, List<IWritableComponentNameMapper>> 
                 projectToCompCacheMap, IProgressConsole console, 
                 boolean openProject) {
             
             m_elements = elements;
             m_projectToMapperMap = projectToMapperMap;
             m_projectToCompCacheMap = projectToCompCacheMap;
             m_console = console;
             m_isOpenProject = openProject;
         }
 
         /**
          * 
          * @return the imported project to open, or <code>null</code> if no project should be opened.
          */
         public IProjectPO getProjectToOpen() {
             return m_projectToOpen;
         }
         
         /**
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) throws InterruptedException, 
                 InvocationTargetException {
             try {
                 // run() is used directly here rather than 
                 // starting a new monitor. We want the operation to run 
                 // within this monitor.
                 NodePM.getInstance().setUseCache(true);
     
                 if (m_elements == FileStorageBP.IMPORT_ALL) {
                     CompleteImportOperation op = new CompleteImportOperation(
                             m_projectToMapperMap, m_projectToCompCacheMap, 
                             m_console);
                     op.run(monitor);
                     if (op.wasImportSuccessful() && m_isOpenProject) {
                         for (IProjectPO project 
                                 : m_projectToMapperMap.keySet()) {
                             
                             m_projectToOpen = project;
                             break;
                         }
                     }
                 } else {
                     new PartsImportOperation(m_projectToMapperMap, 
                             m_projectToCompCacheMap, m_elements, m_console)
                         .run(monitor);
                 }
             } catch (final ConfigXmlException ce) {
                 handleCapDataNotFound(ce);
             } finally {
                 NodePM.getInstance().setUseCache(false);
                 monitor.done();
             }
         }
     }
 
     /**
      * merge parts of loaded project into active project
      * @author BREDEX GmbH
      */
     private static class PartsImportOperation implements IRunnableWithProgress {
     
         /** elements to import */
         private int m_elements;
     
         /** mapping: projects to import => corresponding param name mapper */
         private Map<IProjectPO, List<INameMapper>> m_projectToMapperMap;
         
         /** mapping: projects to import => corresponding comp name cache List */
         private Map<IProjectPO, List<IWritableComponentNameMapper>> 
         m_projectToCompCacheMap;
 
         /** the console to use for reporting progress and errors */
         private IProgressConsole m_console;
         
         /**
          * constructor
          * 
          * @param projectToMapperMap
          *            Mapping from projects to import to corresponding param
          *            name mappers.
          * @param projectToCompMapperMap
          *            Mapping from projects to import to corresponding 
          *            Component Name mappers.
          * @param elements elements
          * @param console
          *              The console to use to display pogress and 
          *              error messages.
          */
         public PartsImportOperation(
                 Map<IProjectPO, List<INameMapper>> projectToMapperMap, 
                 Map<IProjectPO, List<IWritableComponentNameMapper>> 
                 projectToCompMapperMap, 
                 int elements, IProgressConsole console) {
             
             m_projectToMapperMap = projectToMapperMap;
             m_projectToCompCacheMap = projectToCompMapperMap;
             m_elements = elements;
             m_console = console;
         }
     
         /**
          * 
          * {@inheritDoc}
          */
         public void run(IProgressMonitor monitor) throws InterruptedException, 
                 InvocationTargetException {
             SubMonitor subMonitor = SubMonitor.convert(
                     monitor, Messages.ImportFileBPImporting,
                     m_projectToMapperMap.size());
             try {
                 for (IProjectPO proj : m_projectToMapperMap.keySet()) {
                     if (subMonitor.isCanceled()) {
                         throw new InterruptedException();
                     }
                     if ((m_elements & FileStorageBP.IMPORT_TESTCASES) 
                         == FileStorageBP.IMPORT_TESTCASES) {
                         
                         List<INameMapper> mapperList = 
                             m_projectToMapperMap.get(proj);
                         List<IWritableComponentNameMapper> compMapperList =
                             m_projectToCompCacheMap.get(proj);
                         String projectName = proj.getDisplayName();
                         showStartingImport(m_console, projectName);
                         // removes reuse in TestSuites
                         List<ISpecPersistable> specObjList = 
                             proj.getSpecObjCont().getSpecObjList();
                         IProjectPO project = 
                             GeneralStorage.getInstance().getProject();
                         String importedToolkit = proj.getToolkit();
                         try {
                             String importedLevel = 
                                 ToolkitSupportBP.getToolkitLevel(
                                     importedToolkit);
                             String currentToolkit = project.getToolkit();
                             String currentLevel = project.getToolkitLevel();
     
                             tryImport(subMonitor, mapperList, compMapperList,
                                     projectName, specObjList, project, 
                                     importedToolkit, importedLevel, 
                                     currentToolkit, currentLevel);
                         } catch (ToolkitPluginException e) {
                             showErrorDuringImport(m_console, 
                                     proj.getDisplayName(), e);
                         } catch (IncompatibleTypeException ite) {
                             ErrorMessagePresenter.getPresenter()
                                 .showErrorMessage(
                                     ite, ite.getErrorMessageParams(), null);
                         }
                     }
                 }
             } catch (PMException e) {
                 showAbortImport(m_console, e);
                 throw new InvocationTargetException(e);
             } catch (ProjectDeletedException e) {
                 showAbortImport(m_console, e);
                 throw new InvocationTargetException(e);
             } finally {
                 // drop cache generated by importer
                 ProjectNameBP.getInstance().clearCache();
             }                       
             showFinishedImport(m_console);
         }
     
         /**
          * Tries to perform the import, and displays an error message if not
          * successful. This method was created because of checkstyle's method
          * length restrictions. As such, there are a lot of arguments, and none
          * of them are well documented.
          * 
          * @param subMonitor
          *            The progress monitor.
          * @param mapperList
          *            This is responsible for mapping Parameter names from the
          *            imported test cases into the given project.
          * @param compMapperList
          *            Responsible for mapping Component Names from the imported
          *            test cases into the given project.
          * @param projectName
          *            The project name.
          * @param specObjList
          *            The specObjList.
          * @param project
          *            The project.
          * @param importedToolkit
          *            The imported toolkit.
          * @param importedLevel
          *            The imported level.
          * @param currentToolkit
          *            The current toolkit.
          * @param currentLevel
          *            The current level.
          * @throws PMException
          *             if a Persistence (JPA / EclipseLink) exception occurs.
          * @throws ProjectDeletedException
          *             if the the project was already deleted.
          * @throws InterruptedException
          *             if the operation is canceled by the user.
          * @throws IncompatibleTypeException
          * @throws ToolkitPluginException
          *             If a toolkit error occurs.
          */
         private void tryImport(SubMonitor subMonitor,
                 List<INameMapper> mapperList, 
                 List<IWritableComponentNameMapper> compMapperList, 
                 String projectName,
                 List<ISpecPersistable> specObjList, IProjectPO project,
                 String importedToolkit, String importedLevel,
                 String currentToolkit, String currentLevel) throws PMException,
                 ProjectDeletedException, InterruptedException,
                 IncompatibleTypeException, ToolkitPluginException {
            
             // Perform the import if the project toolkits are 
             // compatible...
             if (isToolkitCompatible(
                 importedToolkit, importedLevel, 
                 currentToolkit, currentLevel)) {
                 
                 for (IWritableComponentNameMapper compMapper : compMapperList) {
                     for (IComponentNamePO added 
                             : compMapper.getCompNameCache().getNewNames()) {
                         // NOTE: there is no need to check for Component Names 
                         //       with the same GUID because we always generate 
                         //       new GUIDs when importing parts of a project.
                         
                         // Check whether a Component Name with the same name 
                         // exists in current project. If so, append a number to
                         // make the name unique within the Project.
                         boolean nameExists = 
                             ComponentNamesBP.getInstance().getGuidForName(
                                     added.getName(), project.getId()) != null;
                         int counter = 0;
                         while (nameExists) {
                             counter++;
                             nameExists = 
                                 ComponentNamesBP.getInstance().getGuidForName(
                                         added.getName() + counter, 
                                         project.getId()) != null;
                         }
                         
                         if (counter != 0) {
                             added.setName(added.getName() + counter);
                         }
                     }
                 }
                 importTestCases(mapperList, compMapperList, specObjList, 
                         project, subMonitor.newChild(1));
                 showFinishedImport(m_console, projectName);
             } else {
                 // Otherwise, show the user what went wrong and
                 // continue with the next project
                 String currentToolkitName = 
                     getToolkitName(currentToolkit);
                 String importedToolkitName =
                     getToolkitName(importedToolkit);
                 showIncompatibleToolkit(m_console, 
                     projectName, currentToolkitName, 
                     importedToolkitName);
             }
         }
     
         /**
          * 
          * @param toolkitId A toolkit plugin ID.
          * @return The name associated with the given ID.
          */
         private String getToolkitName(String toolkitId) 
             throws ToolkitPluginException {
             
             CompSystem compSys = 
                 ComponentBuilder.getInstance().getCompSystem();
             ToolkitPluginDescriptor desc = 
                 compSys.getToolkitPluginDescriptor(
                     toolkitId);
             if (desc == null) {
                 throw new ToolkitPluginException(NLS.bind(
                         Messages.ToolkitSupportToolkitNotFound, 
                         new String [] {toolkitId}));
             }
             return desc.getName();
         }
     
         /**
          * Imports the given test cases into the given project.
          * 
          * @param mapperList
          *            This is responsible for mapping Parameter names from the
          *            imported test cases into the given project.
          * @param compMapperList
          *            Responsible for mapping Component Names from the imported
          *            test cases into the given project.
          * @param specObjList
          *            List of test cases to import.
          * @param project
          *            The project into which the test cases will be imported.
          * @param monitor
          *            The progress monitor for this potentially long-running
          *            operation.
          * @return <code>true</code> if the test cases were imported
          *         successfully. Otherwise, <code>false</code>.
          * @throws PMException
          *             if a database error occurs.
          * @throws ProjectDeletedException
          *             if the current project has been deleted.
          */
         private boolean importTestCases(List<INameMapper> mapperList, 
             List<IWritableComponentNameMapper> compMapperList,
             List<ISpecPersistable> specObjList, IProjectPO project, 
             IProgressMonitor monitor) 
             throws PMException, ProjectDeletedException, 
             InterruptedException, IncompatibleTypeException {
             
             String newName = createCategoryName(
                 project.getSpecObjCont().getSpecObjList());
             final ICategoryPO category = 
                 NodeMaker.createCategoryPO(newName);
             if (monitor.isCanceled()) {
                 throw new InterruptedException();
             }
     
             // Register JPA progress listeners
             ProgressMonitorTracker.getInstance().setProgressMonitor(
                     monitor);
             
             monitor.beginTask(StringConstants.EMPTY, getTotalWork(specObjList));
             try {
                 NodePM.addImportedTestCases(category, specObjList);
             } finally {
                 // Remove JPA progress listeners
                 ProgressMonitorTracker.getInstance().setProgressMonitor(
                         null);
             }
             EntityManager compNameSession = 
                 Persistor.instance().openSession();
             EntityTransaction tx = 
                 Persistor.instance().getTransaction(compNameSession);
             for (INameMapper mapper : mapperList) {
                 mapper.persist(compNameSession,  
                         project.getId());
             }
             for (IWritableComponentNameMapper compMapper : compMapperList) {
                 CompNamePM.flushCompNames(
                         compNameSession, project.getId(), compMapper);
             }
     
             Persistor.instance().commitTransaction(compNameSession, tx);
     
             for (IComponentNameMapper compMapper : compMapperList) {
                 compMapper.getCompNameCache()
                     .updateStandardMapperAndCleanup(project.getId());
             }
     
             UsedToolkitBP.getInstance().refreshToolkitInfo(project);
 
             DataEventDispatcher.getInstance().fireDataChangedListener(
                     category, DataState.Added, UpdateState.all);
             DataEventDispatcher.getInstance().fireDataChangedListener(
                     category, DataState.StructureModified, UpdateState.all);
 
             UsedToolkitBP.getInstance().refreshToolkitInfo(
                 GeneralStorage.getInstance().getProject());
 
             return true;
         }
     
         /**
          * @param specObjList The list for which to find the total required
          *                    work.
          * @return the amount of work required to save the given list to the
          *         database.
          */
         private int getTotalWork(List<ISpecPersistable> specObjList) {
             int totalWork = 0;
     
             for (ISpecPersistable spec : specObjList) {
                 
                 totalWork += getWorkForNode(spec);
             }
     
             // 1 for each progress event type except postUpdate
             totalWork *= NUM_HBM_PROGRESS_EVENT_TYPES - 1;
     
             return totalWork;
         }
     
         /**
          * Recursively determines the amount of work involved in saving the
          * given node to the database.
          * 
          * @param node The node for which to determine the amount of work.
          * @return the amount of work required to save the given node to the 
          *         database.
          */
         private int getWorkForNode(INodePO node) {
             int work = 1;
             if (!(node instanceof IExecTestCasePO)) {
                 Iterator<INodePO> childIter = node.getNodeListIterator();
                 while (childIter.hasNext()) {
                     work += getWorkForNode(childIter.next());
                 }
             }
             
             if (node instanceof ISpecTestCasePO) {
                 work += ((ISpecTestCasePO)node).getAllEventEventExecTC().size();
             }
             
             return work;
         }
     
         /**
          * @param importedToolkit The toolkit ID for the imported project.
          * @param importedLevel The toolkit level for the imported project.
          * @param currentToolkit The toolkit ID for the current project.
          * @param currentLevel The toolkit level for the current project.
          * @return <code>true</code> if the imported toolkit/level is compatible
          *         with the current toolkit/level. Otherwise <code>false</code>.
          */
         private boolean isToolkitCompatible(String importedToolkit, 
             String importedLevel, String currentToolkit, String currentLevel) {
             
             return importedToolkit.equals(currentToolkit) 
                 || ToolkitUtils.doesToolkitInclude(
                     currentToolkit, importedToolkit)
                 || ToolkitUtils.isToolkitMoreConcrete(
                     currentLevel, importedLevel);
         }
     
         /**
          * @param specObjList The list into which the new category will be 
          *                    added.
          * @return a suitable name for a new category in the given list.
          */
         private String createCategoryName(List<ISpecPersistable> specObjList) {
             String standardName = Messages.TreeOpsBPImportedCat;
             int postfix = 1;
             String newName = standardName + postfix;
             final Set<String> usedNames = new HashSet<String>();
             for (ISpecPersistable node : specObjList) {
                 if (node instanceof ICategoryPO && (node).getName().
                     startsWith(standardName)) {
     
                     usedNames.add(((INodePO)node).getName());
                 }
             }
             while (usedNames.contains(newName)) {
                 postfix++;
                 newName = standardName + postfix;
             }
             return newName;
         }
     
     }
 
     /** the logger */
     public static final Logger LOG = 
         LoggerFactory.getLogger(FileStorageBP.class);
     
     /** Bit set for importing all */
     public static final int IMPORT_ALL = 0;
     /** Bit set for importing testcases */
     public static final int IMPORT_TESTCASES = 1; 
 
     /** the total amount of work for an import operation */
     private static final int TOTAL_IMPORT_WORK = 100;
 
     /** number of Persistence (JPA / EclipseLink) event types with progress listeners */
     // Event types:
     // save, recreateCollection, postInsert, postUpdate
     private static final int NUM_HBM_PROGRESS_EVENT_TYPES = 4;
     
     /** the amount of work required to read and parse xml files into related domain objects */
     private static final int PARSE_FILES_WORK = 15;
     
     /** the amount of work required to save and commit domain objects to the db */
     private static final int SAVE_TO_DB_WORK = 
         TOTAL_IMPORT_WORK - PARSE_FILES_WORK;
 
     /** 
      * responsible for resolving project name conflicts that occur 
      * during import 
      */
     private static IProjectNameConflictResolver projectNameConflictResolver =
         new NullProjectNameConflictResolver();
     
     /**
      * Private constructor for utility class.
      */
     private FileStorageBP() {
         // Nothing to initialize
     }
     
     /**
      * @param projectList The list of projects to export
      * @param exportDirName The export directory of the projects
      * @param exportSession The session to be used for Persistence (JPA / EclipseLink)
      * @param monitor The progress monitor
      * @param writeToSystemTempDir Indicates whether the projects have to be 
      *                             written to the system temp directory
      * @param listOfProjectFiles The written project files are added to this 
      *                           list, if the temp dir was used and the list  
      *                           is not null.
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      */
     public static void exportProjectList(List<IProjectPO> projectList, 
             String exportDirName, EntityManager exportSession, 
             IProgressMonitor monitor, boolean writeToSystemTempDir, 
             List<File> listOfProjectFiles, IProgressConsole console) 
         throws JBException, InterruptedException {
 
         SubMonitor subMonitor = SubMonitor.convert(monitor, 
                 Messages.ExportAllBPExporting,
                 XmlStorage.getWorkToSave(projectList));
         
         for (IProjectPO proj : projectList) {
             if (subMonitor.isCanceled()) {
                 throw new InterruptedException();
             }
             IProjectPO projectToExport = 
                 ProjectPM.loadProjectById(
                     proj.getId(), exportSession);
             String projectFileName = projectToExport.getDisplayName() + ".xml"; //$NON-NLS-1$
             final String exportFileName;
             
             if (writeToSystemTempDir) {
                 exportFileName = projectFileName;
             } else {
                 if (projectToExport.equals(
                     GeneralStorage.getInstance().getProject())) {
                     
                     // project is current project
                     projectToExport = 
                         GeneralStorage.getInstance().getProject();
                 }
                 
                 exportFileName = exportDirName + projectFileName;
             }
          
             if (subMonitor.isCanceled()) {
                 throw new InterruptedException();
             }
             console.writeLine(
                     NLS.bind(Messages.ExportAllBPInfoStartingExportProject, 
                             new Object [] {projectFileName}));
             try {
                 if (subMonitor.isCanceled()) {
                     throw new InterruptedException();
                 }
                 XmlStorage.save(
                     projectToExport, exportFileName, true,
                     subMonitor.newChild(
                         XmlStorage.getWorkToSave(projectToExport)),
                     writeToSystemTempDir, listOfProjectFiles);
                 
                 if (subMonitor.isCanceled()) {
                     throw new InterruptedException();
                 }                
 
                 console.writeLine(
                         NLS.bind(Messages.ExportAllBPInfoFinishedExportProject,
                                 new Object [] {projectFileName}));
                 
             } catch (final PMSaveException e) {
                 LOG.error(Messages.CouldNotExportProject, e);
                 console.writeErrorLine(
                         NLS.bind(Messages.ExportAllBPErrorExportFailedProject,
                               new Object [] {projectFileName, e.getMessage()}));
             }
             exportSession.detach(projectToExport);
         }
         
     }
 
     /** allow importing some files
      * 
      * @param importProjectURLs list of file URLs. Each URL must be valid.
      * @param monitor The progress monitor for the operation.
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param openProject
      *            Flag indicating whether the imported project should be 
      *            immediately opened after import.
      */
     public static void importFiles(List<URL> importProjectURLs, 
             IProgressMonitor monitor, IProgressConsole console, 
             boolean openProject) throws PMException, ProjectDeletedException {
         // import all data from projects
         try {
             doImport(IMPORT_ALL, importProjectURLs, 
                     SubMonitor.convert(monitor), console, openProject);
         } catch (InterruptedException e) {
             // Operation was canceled. Do nothing.
         }
     }
     
     /**
      * Imports a choosed project from a file.
      * 
      * @param elements
      *            What to import ? 0 = all >0 = elements
      * @param fileURLs
      *            The URLs of the files to import.
      * @param monitor 
      *            The progress monitor for the operation.
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param openProject
      *            Flag indicating whether the imported project should be 
      *            immediately opened after import.
      * 
      * @return the project to open immediately after import, or 
      *         <code>null</code> if no project should be opened.
      * @throws InterruptedException if the operation was canceled or the thread
      *                              was interrupted.
      */
     public static IProjectPO importProject(final int elements,
             final List<URL> fileURLs, IProgressMonitor monitor,
             IProgressConsole console, boolean openProject)
         throws InterruptedException, PMException, ProjectDeletedException {
 
         SubMonitor subMonitor = SubMonitor.convert(monitor,
                 Messages.ImportFileBPImporting, TOTAL_IMPORT_WORK);
         return doImport(elements, fileURLs, subMonitor, console, openProject);
     }
 
     /**
      * actually do the import work. Separated to only batch calls
      * @param elements @see #importProject(int)
      * @param fileURLs
      *            The URLs of the files to import.
      * @param subMonitor @see #importProject(int)
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param openProject
      *            Flag indicating whether the imported project should be 
      *            immediately opened after import.
      *              
      * @return the project to open immediately after import, or 
      *         <code>null</code> if no project should be opened.
      * @throws InterruptedException @see #importProject(int)
      */
     private static IProjectPO doImport(final int elements, List<URL> fileURLs, 
             SubMonitor subMonitor, IProgressConsole console, 
             boolean openProject) 
         throws InterruptedException, PMException, ProjectDeletedException {
         
         // Read project files
         ReadFilesOperation readFilesOp = 
             new ReadFilesOperation(elements == 0, fileURLs, console);
         readFilesOp.run(subMonitor.newChild(
                 PARSE_FILES_WORK));
 
         // Import projects
         ImportOperation importOp = new ImportOperation(
             elements, readFilesOp.getProjectToMapperMap(), 
             readFilesOp.getProjectToCompCacheMap(), console, openProject);
         
         try {
             importOp.run(subMonitor.newChild(SAVE_TO_DB_WORK));
         } catch (InvocationTargetException ite) {
             Throwable cause = ExceptionUtils.getRootCause(ite);
             if (cause instanceof PMException) {
                 throw (PMException)cause;
             } else if (cause instanceof ProjectDeletedException) {
                 throw (ProjectDeletedException)cause;
             } else {
                 throw new RuntimeException(ite);
             }
         }
         return importOp.getProjectToOpen();
     }
 
     /**
      * Report to the user that the import operation was aborted due to an
      * error.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param e The error that caused the import operation to abort.
      */
     private static void showAbortImport(IProgressConsole console, Exception e) {
         console.writeErrorLine(
                 NLS.bind(Messages.ImportFileActionErrorImportFailed, 
                         new Object [] {e.getMessage()}));
     }
 
     /**
      * Report to the user that an error occurred while importing the project.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param projectFileName The filename of the project that was being 
      *                        imported.
      * @param e The error that occurred.
      */
     private static void showErrorDuringImport(IProgressConsole console, 
             String projectFileName, Exception e) {
         
         console.writeErrorLine(
                 NLS.bind(Messages.ImportFileActionErrorImportFailedProject, 
                         new Object [] {projectFileName, e.getMessage()}));
     }
 
     /**
      * Report to the user that all projects have been imported.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      */
     private static void showFinishedImport(IProgressConsole console) {
         console.writeLine(
             Messages.ImportFileActionInfoFinishedImport);
     }
 
     /**
      * Report to the user that the project has been imported.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param projectFileName The filename of the imported project.
      */
     private static void showFinishedImport(IProgressConsole console, 
             String projectFileName) {
         console.writeLine(
                 NLS.bind(Messages.ImportFileActionInfoFinishedImportProject, 
                         new Object [] {projectFileName}));
     }
 
     /**
      * Report to the user that all projects to import have been analyzed.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      */
     private static void showFinishedReadingProjects(IProgressConsole console) {
         console.writeLine(
             Messages.ImportFileActionInfoFinishedReadingProjects);
     }
 
     /**
      * Report to the user that the import of a project failed because the
      * toolkit for the imported project was not compatible with the toolkit
      * ofthe current project.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param projectName Name of the project to import.
      * @param currentToolkit Toolkit of the current project.
      * @param importedToolkit Toolkit of the imported project.
      */
     private static void showIncompatibleToolkit(IProgressConsole console, 
             String projectName, String currentToolkit, String importedToolkit) {
         
         console.writeErrorLine(
                 NLS.bind(Messages.ImportFileActionErrorIncompatibleToolkits,
                         new Object [] {projectName, importedToolkit, 
                             currentToolkit}));
     }
 
     /**
      * Report to the user that the import process is beginning.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      */
     private static void showStartingImport(IProgressConsole console) {
         console.writeLine(
                 Messages.ImportFileActionInfoStartingImport);
     }
 
     /**
      * Report to the user that the project is being imported.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      * @param projectFileName The filename of the imported project.
      */
     private static void showStartingImport(IProgressConsole console, 
             String projectFileName) {
         console.writeLine(
                 NLS.bind(Messages.ImportFileActionInfoStartingImportProject,
                         new Object [] {projectFileName}));
     }
 
     /**
      * Report to the user that all projects to import will be analyzed.
      * 
      * @param console
      *              The console to use to display pogress and 
      *              error messages.
      */
     private static void showStartingReadingProjects(IProgressConsole console) {
         console.writeLine(
                 Messages.ImportFileActionInfoStartingReadingProjects);
     }
 
     /**
      * 
      * @param resolver The new conflict resolver.
      */
     public static void setProjectNameConflictResolver(
             IProjectNameConflictResolver resolver) {
         
         Validate.notNull(resolver);
         projectNameConflictResolver = resolver;
     }
 
     /**
      * @param e PMReadException
      * @param fileURLs The URLs of the files that were being imported.
      */
     private static void handlePMReadException(final PMReadException e, 
             final List<URL> fileURLs) {
         ErrorMessagePresenter.getPresenter().showErrorMessage(
                 new JBException(e + Messages.Reading + fileURLs.toArray()
                         + Messages.Failed,
                        MessageIDs.E_IMPORT_XML_FAILED), null,
                 MessageIDs.getMessageObject(e.getErrorId()).getDetails());
     }
 
     /**
      * Create an appropriate error dialog.
      * 
      * @param ce The exception that prevented the import of the 
      *           project.
      */
     private static void handleCapDataNotFound(final ConfigXmlException ce) {
 
         ErrorMessagePresenter.getPresenter().showErrorMessage(
                 MessageIDs.E_IMPORT_PROJECT_CONFIG_CONFLICT, 
                 null, new String[] {ce.getMessage()});
     }
 
 }
