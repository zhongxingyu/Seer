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
 package org.eclipse.jubula.client.core.persistence;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.EntityTransaction;
 import javax.persistence.PersistenceException;
 
 import org.apache.commons.lang.Validate;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jubula.client.core.businessprocess.CompNameMapperFactory;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesDecorator;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.ParamNameBPDecorator;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITcParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.ITestDataCategoryPO;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.exception.JBFatalAbortException;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.persistence.jpa.JpaEntityManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 15.07.2005
  */
 public class EditSupport {
     /** standard logging */
     private static Logger log = LoggerFactory.getLogger(EditSupport.class);
 
     /** Persistence (JPA / EclipseLink) session for editing */
     private EntityManager m_session;
 
     /** working version of persistent object */
     private IPersistentObject m_workVersion;
 
     /**
      * <code>m_lockedObjects</code>objects are locked by current edit support
      */
     private List<IPersistentObject> m_lockedObjects = 
         new ArrayList<IPersistentObject>();
     
     /**
      * <code>m_transaction</code> actual transaction
      */
     private EntityTransaction m_transaction = null;
 
     /**
      * <code>m_isLocked</code> lock status for m_workVersion
      */
     private boolean m_isLocked = false;
 
     /**
      * <code>m_isValid</code> signals the validity of this instance
      */
     private boolean m_isValid = true;
     
     /**
      * <code>m_mapper</code>mapper for resolving and persistence of parameter names
      */
     private ParamNameBPDecorator m_paramMapper = null;
     
     /**
      * <code>m_compMapper</code>mapper for resolving and persistence of 
      * component names
      */
     private IWritableComponentNameMapper m_compMapper;
 
 
     /**
      * Instantiate edit support for the supplied persistent object
      * 
      * @param po Master instance for the new editable persistent object
      * @param paramMapper mapper for resolving and persistence of parameter names
      * the mapper is null in case of po objects not derived of NodePO
      * @throws PMException in case of unexpected db error
      * 
      */
     public EditSupport(IPersistentObject po, ParamNameBPDecorator paramMapper) 
         throws PMException {
         
         init();
         m_workVersion = createWorkVersion(po);
         m_paramMapper = paramMapper;
         m_compMapper = CompNameMapperFactory.createCompNameMapper(
                 m_workVersion, new ComponentNamesDecorator(getSession()));
     }
 
     /**
      * (re)set internal data
      */
     private void init() {
         m_workVersion = null;
         m_isValid = true;
         m_session = Persistor.instance().openSession();
         m_transaction = Persistor.instance().getTransaction(m_session);
     }
     
     
 
     /**
      * @param po
      *            the persistent object for which the working version shall be
      *            created.
      * @return a working version of the PO supplied to the constructor. This
      *         version is db identical to its original, but not Java identical.
      * @throws PMException
      *             in case of unspecified db error
      * 
      */
     public IPersistentObject createWorkVersion(IPersistentObject po)
         throws PMException {
         Assert.verify(m_isValid, 
             Messages.InvalidInstanceForInvokingOfThisMethod);
         Validate.notNull(po,
             Messages.OriginalObjectForCreatingOfWorkversionIsNull
             + StringConstants.DOT);
         try {
             IPersistentObject result = m_session
                     .find(po.getClass(), po.getId());
             if (result == null) {
                 throw new EntityNotFoundException(
                         Messages.UnableToFind + StringConstants.SPACE
                         + po.getClass().getName() 
                         + StringConstants.SPACE + Messages.WithID 
                         + StringConstants.SPACE + po.getId());
             }
             /* if po in the mastersession is newer than the corresponding
                object in the editor session, the instance in the editor session
                must be from the session cache; therefore the instance from the
                editor session must be evicted and reloaded */
             if ((result.getVersion() == null)
                 || (po.getVersion().intValue() 
                     > result.getVersion().intValue())) {
                 m_session.detach(result);
                 result = m_session.find(po.getClass(), po.getId());
                 if (result == null) {
                     throw new EntityNotFoundException(
                             Messages.UnableToFind + StringConstants.SPACE
                             + po.getClass().getName() 
                             + StringConstants.SPACE + Messages.WithID
                             + StringConstants.SPACE + po.getId());
                 }
             }
             return result;
         } catch (PersistenceException e) {
             PersistenceManager.handleDBExceptionForEditor(po, e, this);
         }
         return null;
     }
 
     /**
      * locks the actual work version for modification. This method may be called
      * several times during one editing session if the node is locked and the
      * user retries the edit operation.
      * 
      * @throws PMDirtyVersionException
      *             in case of version conflict
      * @throws PMAlreadyLockedException
      *             if another user has locked this object
      * @throws PMReadException
      *             in case of reading error of DB
      * @throws PMException
      *             in case of general db error
      */
     public void lockWorkVersion() throws PMReadException,
         PMAlreadyLockedException, PMDirtyVersionException, PMException {
         Assert.verify(m_isValid, 
             Messages.InvalidInstanceForInvokingOfThisMethod);
         try {
             if (m_workVersion instanceof ISpecTestCasePO) {
                 List<IParamDescriptionPO> params = 
                     ((ISpecTestCasePO)m_workVersion)
                         .getParameterList();
                 for (IParamDescriptionPO desc : params) {
                     ((ITcParamDescriptionPO)desc)
                             .setParamNameMapper(m_paramMapper);
                 }
             } else if (m_workVersion instanceof ITestDataCategoryPO) {
                 for (IParameterInterfacePO pio 
                         : ((ITestDataCategoryPO)m_workVersion)
                             .getTestDataChildren()) {
                     List<IParamDescriptionPO> params = pio.getParameterList();
                     for (IParamDescriptionPO desc : params) {
                         ((ITcParamDescriptionPO)desc)
                                 .setParamNameMapper(m_paramMapper);
                     }
                 }
             }
             Persistor.instance().lockPO(m_session, m_workVersion);
             m_lockedObjects.add(m_workVersion);
             m_isLocked = true;
         } catch (PersistenceException e) {
             PersistenceManager.handleDBExceptionForEditor(m_workVersion, e,
                 this);
         }
     }
 
     
     /**
      * closes the actual session
      */
     private void closeSession() {
         if (Persistor.instance() != null) {
             Persistor.instance().dropSession(m_session);
         }
         
         invalidate();
     }
 
     /**
      * persists the workversion in database
      * 
      * @throws PMReadException
      *             in case of stale state exception for object to refresh
      * @throws PMSaveException
      *             if commit failed
      * @throws PMException
      *             in case of failed rollback
      * @throws ProjectDeletedException
      *             if the project was deleted in another instance
      * @throws IncompatibleTypeException
      *             if a Component Name is reused in an incompatible context.
      */
     public void saveWorkVersion() throws PMReadException, PMSaveException,
         PMException, ProjectDeletedException, IncompatibleTypeException {
         if (m_isValid) {
             if (m_isLocked) {
                 boolean stayLocked = false;
                 try {
                     boolean mayModifyParamNames = 
                         m_workVersion instanceof ISpecTestCasePO
                             || m_workVersion instanceof ITestDataCategoryPO;
                     if (mayModifyParamNames) {
                         saveParamNames();
                     }
                     /*
                      * CARE: isDirty() only checks for the synchronisation state
                      * of the session and the database; --> e.g. isDirty() is
                      * false if the session has been flushed
                      */
                     // The saving of param names that occurs above may flush the
                     // session, which would make the session not "dirty".
                     // We cover this case by assuming that any situation in 
                     // which param names may be modified is a situation where
                     // we definitely want to save+commit.
                     if (mayModifyParamNames 
                             || m_session.unwrap(JpaEntityManager.class)
                                 .getUnitOfWork().hasChanges()) {
                         saveComponentNames();
                         Persistor.instance().commitTransaction(m_session,
                                 m_transaction);
                         Long projId = GeneralStorage.getInstance().getProject()
                                 .getId();
                         if (m_paramMapper != null) {
                             m_paramMapper
                                     .updateStandardMapperAndCleanup(projId);
                         }
                         if (m_compMapper != null) {
                             m_compMapper.getCompNameCache()
                                     .updateStandardMapperAndCleanup(projId);
                         }
                         refreshOriginalVersions();
                     } else {
                         Persistor.instance().rollbackTransaction(m_session,
                                 m_transaction);
                     }
                     m_lockedObjects.clear();
                     if (m_session != null) {
                         m_transaction = m_session.getTransaction();
                         m_transaction.begin();
                     } else {
                         init();
                     }
                 } catch (IncompatibleTypeException ite) {
                     stayLocked = true;
                     throw ite;
                 } catch (PersistenceException e) {
                     PersistenceManager.handleDBExceptionForEditor(
                             m_workVersion, e, this);
                 } finally {
                     m_isLocked = stayLocked;
                 }
             } else {
                 throw new JBFatalAbortException(
                         Messages.NotAllowedToSaveAnUnlockedWorkversion
                         + StringConstants.DOT,
                         MessageIDs.E_CANNOT_SAVE_UNLOCKED); 
             }
         } else {
             throw new JBFatalAbortException(
                     Messages.NotAllowedToSaveAnUnlockedWorkversion
                     + StringConstants.DOT,
                     MessageIDs.E_CANNOT_SAVE_INVALID); 
         }
     }
 
     /**
      * 
      * @return the GUIDs for all of the component names currently used by
      *         the edited node. If the edited node is *not* an 
      *         <code>ISpecTestCasePO</code>, returns an empty set.
      */
     public Set<String> getUsedComponentNameGuids() {
         Set<String> usedCompNameGuids =
             new HashSet<String>();
         if (m_workVersion instanceof ISpecTestCasePO) {
             ISpecTestCasePO specTc = (ISpecTestCasePO)m_workVersion;
             for (Object obj : specTc.getAllEventEventExecTC()) {
                 if (obj instanceof IEventExecTestCasePO) {
                     IEventExecTestCasePO evExTc =
                         (IEventExecTestCasePO)obj;
                     for (ICompNamesPairPO pair : evExTc.getCompNamesPairs()) {
                         usedCompNameGuids.add(pair.getFirstName());
                         usedCompNameGuids.add(pair.getSecondName());
                     }
                 }
             }
             Iterator it = specTc.getNodeListIterator();
             while (it.hasNext()) {
                 Object obj = it.next();
                 if (obj instanceof ICapPO) {
                     ICapPO cap = (ICapPO)obj;
                     usedCompNameGuids.add(cap.getComponentName());
                 } else if (obj instanceof IExecTestCasePO) {
                     IExecTestCasePO execTc = (IExecTestCasePO)obj;
                     for (ICompNamesPairPO pair : execTc.getCompNamesPairs()) {
                         usedCompNameGuids.add(pair.getFirstName());
                         usedCompNameGuids.add(pair.getSecondName());
                     }
                 }
             }
         }
 
         return usedCompNameGuids;
     }
     
     /**
      * Persists the Parameter Names.
      * @throws PMException
      * @throws GDProjectDeletedException
      */
     private void saveParamNames() throws PMException {
         m_paramMapper.persist(m_session, 
             GeneralStorage.getInstance().getProject().getId());
     }
 
     /**
      * Persists the Comnponent Names.
      * @throws PMException
      */
     private void saveComponentNames() 
         throws PMException, IncompatibleTypeException {
         
         if (m_compMapper != null) {
             CompNamePM.flushCompNames(m_session, 
                     GeneralStorage.getInstance().getProject().getId(), 
                     m_compMapper);
         }
     }
     
     /**
      * because of Persistence (JPA / EclipseLink)-Bug HHH-1280 we can't use
      * refresh<br>
      * therefore we use evict to remove the old object from master session and
      * reload the object<br>
      * please attend, that in this case the Java-IDs of the old and the reloaded
      * object are different<br>
      * refreshs the original versions, which were possibly modified in editor
      * 
      * @throws ProjectDeletedException
      *             if the project was deleted in another instance
      */
     private void refreshOriginalVersions() throws ProjectDeletedException {
         try {
             final EntityManager masterSession = GeneralStorage.getInstance()
                     .getMasterSession();
             IPersistentObject original = getOriginal();
             if (original != null) {
                masterSession.merge(getWorkVersion());
                 GeneralStorage.getInstance().fireDataModified(original);
             }
         } catch (PersistenceException e) {
             log.error(Messages.RefreshOfOriginalVersionFailed
                     + StringConstants.DOT, e);
             GeneralStorage.getInstance().reloadMasterSession(
                     new NullProgressMonitor());
         }
     }
 
     /**
      * discards the work version
      * 
      */
     public void close() {
         Assert.verify(m_isValid, 
             Messages.InvalidInstanceForInvokingOfThisMethod);
         closeSession();
     }
 
     /**
      * resets all instance variables
      */
     private void invalidate() {
         m_isValid = false;
         m_isLocked = false;
         m_workVersion = null;
         m_transaction = null;
         m_session = null;
         m_lockedObjects.clear();
     }
 
     /**
      * @return Returns the original.
      */
     public IPersistentObject getOriginal() {
         return GeneralStorage.getInstance().getMasterSession()
                 .find(m_workVersion.getClass(), m_workVersion.getId());
     }
 
     /**
      * @return locked objects of current edit support
      */
     public List<IPersistentObject> getLockedObjects() {
         return m_lockedObjects;
     }
     
 
     /**
      * @return Returns the workVersion.
      */
     public IPersistentObject getWorkVersion() {
         return m_workVersion;
     }
 
     /**
      * @return Returns the session.
      */
     public EntityManager getSession() {
         return m_session;
     }
     
     /**
      * attachs the detached workVersion to a new session
      * to use for postprocessing of Persistence (JPA / EclipseLink) exceptions without refresh of objects 
      * @throws PMException in case of any db error
      */
     public void reinitializeEditSupport()
         throws PMException {
         try {
             IPersistentObject workVersion = m_workVersion;
             close();
             init();
             m_workVersion = workVersion;
             m_compMapper.setCompNameCache(
                     new ComponentNamesDecorator(getSession()));
             m_compMapper.setContext(m_workVersion);
             m_workVersion = m_session.merge(m_workVersion);
         } catch (PersistenceException e) {
             final String msg = Messages.ReinitOfSessionFailed;
             log.error(msg);
             throw new PMException(msg,
                 MessageIDs.E_DATABASE_GENERAL);
         }
     }
     
     /**
      * refreshs the editSession
      * @throws PMException in case of any db error
      */
     public void reloadEditSession() throws PMException {
         try {
             IPersistentObject workVersion = m_workVersion;
             close();
             init();
             m_workVersion = createWorkVersion(workVersion);
             m_compMapper.setCompNameCache(
                     new ComponentNamesDecorator(getSession()));
             m_compMapper.setContext(m_workVersion);
             if (m_paramMapper != null) {
                 Long projId = 
                     GeneralStorage.getInstance().getProject().getId();
                 m_paramMapper.updateStandardMapperAndCleanup(projId);
             }
         } catch (PersistenceException e) {
             final String msg = Messages.ReinitOfSessionFailed;
             log.error(msg);
             throw new PMException(msg,
                 MessageIDs.E_DATABASE_GENERAL);
         }
         
     }
     
     /**
      * @return project associated with current session
      * @throws PMException
      *           in case of unspecified db error
      * 
      */
     public IProjectPO getWorkProject() throws PMException {
         IProjectPO masterProj = GeneralStorage.getInstance().getProject();
         IProjectPO workProj = null;
 
         try {
             workProj = m_session.find(
                     masterProj.getClass(), masterProj.getId());
             if (workProj == null) {
                 throw new EntityNotFoundException(
                         Messages.UnableToFind + StringConstants.SPACE
                         + masterProj.getClass().getName() 
                         + StringConstants.SPACE + Messages.WithID
                         + StringConstants.SPACE + masterProj.getId());
             }
             return workProj;
         } catch (PersistenceException e) {
             PersistenceManager.handleDBExceptionForEditor(masterProj, e, this);
         }
         return null;
     }
 
     /**
      * @return the ParamMapper, businessLogic for Parameter Names.
      */
     public ParamNameBPDecorator getParamMapper() {
         return m_paramMapper;
     }
     
     /**
      * @return the CompMapper, businessLogic for Component Names.
      */
     public IWritableComponentNameMapper getCompMapper() {
         return m_compMapper;
     }
 
 }
