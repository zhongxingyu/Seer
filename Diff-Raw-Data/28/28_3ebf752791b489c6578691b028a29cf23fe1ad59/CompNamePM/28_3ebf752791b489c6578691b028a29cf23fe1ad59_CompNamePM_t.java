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
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.FlushModeType;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaQuery;
 
 import org.eclipse.jubula.client.core.businessprocess.CompNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameCache;
 import org.eclipse.jubula.client.core.businessprocess.IWritableComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.progress.OperationCanceledUtil;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IObjectMappingAssoziationPO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.persistence.locking.LockManager;
 import org.eclipse.jubula.client.core.persistence.locking.LockedObjectPO;
 import org.eclipse.jubula.toolkit.common.xml.businessprocess.ComponentBuilder;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.exception.JBFatalAbortException;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.objects.IComponentIdentifier;
import org.eclipse.jubula.tools.utils.ValueListIterator;
 import org.eclipse.jubula.tools.xml.businessmodell.CompSystem;
 import org.eclipse.jubula.tools.xml.businessmodell.Component;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * Logic to persist Component Names.
  *
  * @author BREDEX GmbH
  * @created Apr 8, 2008
  */
 public class CompNamePM extends AbstractNamePM {
 
     /**
      * The maximum number of expressions allowed in a database list
      * See: "ORA-01795: maximum number of expressions in a list is 1000"
      */
     private static final int MAX_DB_EXPRESSIONS = 1000;
 
     /**
      * <code>COMP_NAME_TABLE_ID</code>
      */
     private static final String COMP_NAME_TABLE_ID = "org.eclipse.jubula.client.core.model.ComponentNamePO"; //$NON-NLS-1$
 
     /**
      * Query Parameter for Component Name Pairs to include.
      */
     private static final String P_PAIR_LIST = "pairList"; //$NON-NLS-1$
 
     /**
      * Query Parameter for component names pairs to ignore.
      */
     private static final String P_IGNORE_PAIRS = "ignoreNamePairIds"; //$NON-NLS-1$
 
     /**
      * Query Parameter for test steps to ignore.
      */
     private static final String P_IGNORE_CAPS = "ignoreCapIds"; //$NON-NLS-1$
 
     /**
      * Query Parameter for component name guid.
      */
     private static final String P_COMP_NAME_GUID = "compNameGuid"; //$NON-NLS-1$
 
     /**
      * Query Parameter for parent project id.
      */
     private static final String P_PARENT_PROJECT_ID = "parentProjectId"; //$NON-NLS-1$
 
     /**
      * Query Parameter for Component Name names to search.
      */
     private static final String P_NAME_SET = "nameSet"; //$NON-NLS-1$
 
     /**
      * Query to find preexisting Component Names.
      */
     private static final String Q_PREEXISTING_NAMES = 
         "select compName from ComponentNamePO compName where compName.hbmParentProjectId = :"  //$NON-NLS-1$
         + P_PARENT_PROJECT_ID + " and compName.hbmName in :" + P_NAME_SET; //$NON-NLS-1$
 
     /**
      * Query to find the types of reuse of a component name by component name 
      * pairs.
      */
     private static final String Q_REUSE_TYPE_PAIRS = 
         "select compNamePair from CompNamesPairPO as compNamePair," //$NON-NLS-1$
         + " ComponentNamePO as secondCompName," //$NON-NLS-1$
         + " ComponentNamePO as firstCompName" //$NON-NLS-1$
         + " where compNamePair.secondName = :" + P_COMP_NAME_GUID //$NON-NLS-1$
         + " and compNamePair.hbmParentProjectId = :" + P_PARENT_PROJECT_ID //$NON-NLS-1$
         + " and compNamePair.firstName = firstCompName.hbmGuid"; //$NON-NLS-1$
 
     /**
      * Sub-Query to ignore certain component name pairs.
      */
     private static final String SQ_REUSE_TYPE_PAIRS_IGNORE = 
         " and not compNamePair.id in :" + P_IGNORE_PAIRS; //$NON-NLS-1$
 
 
     /**
      * Query to find the types of reuse of a component name by object mapping 
      * associations.
      */
     private static final String Q_REUSE_TYPE_ASSOCS = 
         "select assoc from ObjectMappingAssoziationPO as assoc," //$NON-NLS-1$
         + " CompIdentifierPO as compId," //$NON-NLS-1$
         + " ComponentNamePO as compName" //$NON-NLS-1$
         + " join assoc.logicalNames as logicalName" //$NON-NLS-1$
         + " where logicalName = compName.hbmGuid" //$NON-NLS-1$
         + " and logicalName = :" + P_COMP_NAME_GUID //$NON-NLS-1$
         + " and assoc.technicalName = compId" //$NON-NLS-1$
         + " and compName.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Query to find the types of reuse of a component name by test steps.
      */
     private static final String Q_REUSE_TYPE_CAPS = 
         "select cap.componentType from CapPO as cap" //$NON-NLS-1$
         + " where cap.componentName = :" + P_COMP_NAME_GUID //$NON-NLS-1$
         + " and cap.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Sub-Query to ignore certain object mapping associations.
      */
     private static final Object SQ_REUSE_TYPE_CAPS_IGNORE = 
         " and not cap.id in :" + P_IGNORE_CAPS; //$NON-NLS-1$
 
     /**
      * Query to find the GUIDs of all Component Names within a given Project 
      * that reference other Component Names.
      */
     private static final String Q_REF_COMP_NAME_GUIDS = 
         "select compName.hbmGuid from ComponentNamePO as compName" //$NON-NLS-1$
         + " where compName.hbmReferencedGuid is not null" //$NON-NLS-1$
         + " and compName.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Query to find the GUIDs of all Component Names that are referenced by
      * one or more Test Steps in a given Project.
      */
     private static final String Q_CAP_COMP_NAME_GUIDS = 
         "select cap.componentName from CapPO as cap" //$NON-NLS-1$
         + " where cap.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Query to find the GUIDs of all Component Names that are referenced by
      * the First Name of one or more Component Name Pairs in a given Project.
      */
     private static final String Q_PAIR_FIRST_COMP_NAME_GUIDS = 
         "select pair.firstName from CompNamesPairPO as pair" //$NON-NLS-1$
         + " where pair.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Query to find the GUIDs of all Component Names that are referenced by
      * the Second Name of one or more Component Name Pairs in a given Project.
      */
     private static final String Q_PAIR_SECOND_COMP_NAME_GUIDS = 
         "select pair.secondName from CompNamesPairPO as pair" //$NON-NLS-1$
         + " where pair.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Query to find the GUIDs of all Component Names that are referenced by
      * one or more Object Mapping Associations in a given Project.
      */
     private static final String Q_ASSOC_COMP_NAME_GUIDS = 
         "select logicalName from ObjectMappingAssoziationPO as assoc" //$NON-NLS-1$
         + " join assoc.logicalNames as logicalName" //$NON-NLS-1$
         + " where assoc.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Query to find the GUIDs of all Component Names that are referenced by
      * one or more other Component Names in a given Project.
      */
     private static final String Q_COMP_NAME_REF_GUIDS = 
         "select compName.hbmReferencedGuid from ComponentNamePO as compName" //$NON-NLS-1$
         + " where compName.hbmReferencedGuid is not null" //$NON-NLS-1$
         + " and compName.hbmParentProjectId = :" + P_PARENT_PROJECT_ID; //$NON-NLS-1$
 
     /**
      * Query to find all Test Case References that make use of certain 
      * Component Name Pairs.
      */
     private static final String Q_EXEC_TCS_WITH_PAIR = 
         "select execTc from ExecTestCasePO execTc" //$NON-NLS-1$
         + " left outer join execTc.hbmCompNamesMap as compNamesMap" //$NON-NLS-1$
         + " where compNamesMap.id in :" + P_PAIR_LIST; //$NON-NLS-1$
 
     /** GUIDs of Component Names to delete from the DB */
     private static final String P_COMP_NAME_REMOVAL_LIST = "compNameRemovalList"; //$NON-NLS-1$
 
     /**
      * "Query" to delete all Component Names based on GUID and parent Project.
      */
     private static final String Q_DELETE_COMP_NAMES = 
         "delete from ComponentNamePO compName" //$NON-NLS-1$
         + " where compName.hbmParentProjectId = :" + P_PARENT_PROJECT_ID //$NON-NLS-1$
         + " and compName.hbmGuid in :" + P_COMP_NAME_REMOVAL_LIST; //$NON-NLS-1$
 
     /**
      * <code>log</code>logger
      */
     private static Logger log = LoggerFactory.getLogger(ParamNamePM.class);
     
     /** The ILockedObjectPO for locks on ComponentNames tabe in database */
     private static LockedObjectPO lockObj = null;
     
     /**
      * @param parentProjectId id from root project
      * @return list of all param name objects for given project
      * @throws PMException in case of any db problem
      */
     public static final List<IComponentNamePO> readAllCompNames(
         Long parentProjectId) throws PMException {
 
         EntityManager s = null;
         try {
             s = Persistor.instance().openSession();
             return readAllCompNames(parentProjectId, s);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(s);
         }
     }
 
     /**
      * @param parentProjectId id from root project
      * @param s The session to use for the query.
      * @return list of all param name objects for given project
      * @throws PMException in case of any db problem
      */
     @SuppressWarnings("unchecked")
     private static final List<IComponentNamePO> readAllCompNames(
         Long parentProjectId, EntityManager s) throws PMException {
 
         final List <IComponentNamePO> compNames = 
             new ArrayList<IComponentNamePO>();
         try {
             final Query q = s.createQuery(
                     "select compName from ComponentNamePO compName where compName.hbmParentProjectId = :parentProjId"); //$NON-NLS-1$
             q.setParameter("parentProjId", parentProjectId); //$NON-NLS-1$
             compNames.addAll(q.getResultList());
         } catch (PersistenceException e) {
             OperationCanceledUtil.checkForOperationCanceled(e);
             log.error(Messages.CouldNotReadComponentNamesFromDBOfProjectWithID
                 + StringConstants.SPACE + StringConstants.APOSTROPHE
                 + String.valueOf(parentProjectId) + StringConstants.APOSTROPHE, 
                      e);
             PersistenceManager.handleDBExceptionForAnySession(null, e, s);
         }
         return compNames;
     }
 
     /**
      * @param parentProjectId id from root project
      * @return list of all param name objects for given project
      * @throws PMException in case of any db problem
      */
     @SuppressWarnings("unchecked")
     public static final List<IComponentNamePO> readAllRefCompNames(
         Long parentProjectId) throws PMException {
 
         EntityManager s = null;
         final List <IComponentNamePO> compNames = 
             new ArrayList<IComponentNamePO>();
         try {
             s = Persistor.instance().openSession();
             final Query q = s.createQuery(
                     "select compName from ComponentNamePO compName where compName.hbmParentProjectId = :parentProjId and compName.hbmReferenceGuid is not null"); //$NON-NLS-1$
             q.setParameter("parentProjId", parentProjectId); //$NON-NLS-1$
             compNames.addAll(q.getResultList());            
         } catch (PersistenceException e) {
             OperationCanceledUtil.checkForOperationCanceled(e);
             log.error(Messages.CouldNotReadComponentNamesFromDBOfProjectWithID
                 + StringConstants.SPACE + StringConstants.APOSTROPHE
                 + String.valueOf(parentProjectId) + StringConstants.APOSTROPHE, 
                     e);
             PersistenceManager.handleDBExceptionForAnySession(null, e, s);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(s);
         }
         return compNames;
     }
 
     /**
      * Loads the IComponentNamePO with the given GUID and parentProjId.
      * @param compNameGuid the GUID of the IComponentNamePO which is to load.
      * @param parentProjId the parentProjId of the IComponentNamePO which is to load.
      * @return the IComponentNamePO with the given GUID and parentProjId or 
      * null if not found.
      * @throws PMException in case of any db problem.
      */
     public static final IComponentNamePO loadCompName(String compNameGuid, 
         Long parentProjId) throws PMException {
         
         EntityManager s = null;
         IComponentNamePO compNamePO = null;
         try {
             s = Persistor.instance().openSession();
             final Query q = s.createQuery(
                     "select compName from ComponentNamePO compName where compName.hbmParentProjectId = :parentProjId and compName.hbmGuid = :compNameGuid"); //$NON-NLS-1$
             q.setParameter("parentProjId", parentProjId); //$NON-NLS-1$
             q.setParameter("compNameGuid", compNameGuid); //$NON-NLS-1$
             compNamePO = (IComponentNamePO)q.getSingleResult();
             if (compNamePO != null && compNamePO.getReferencedGuid() != null) {
                 return loadCompName(
                         compNamePO.getReferencedGuid(), parentProjId);
             }
         } catch (NoResultException nre) {
             // Fall through to return null
         } catch (PersistenceException e) {
             OperationCanceledUtil.checkForOperationCanceled(e);
             StringBuilder msg = new StringBuilder();
             msg.append(
                     Messages.CouldNotReadComponentNamesFromDBOfProjectWithID);
             msg.append(StringConstants.SPACE);
             msg.append(StringConstants.APOSTROPHE);
             msg.append(String.valueOf(parentProjId));
             msg.append(StringConstants.APOSTROPHE);
             msg.append(StringConstants.SPACE);
             msg.append(Messages.AndGUID);
             msg.append(StringConstants.SPACE);
             msg.append(StringConstants.APOSTROPHE);
             msg.append(String.valueOf(compNameGuid));
             msg.append(StringConstants.APOSTROPHE);
             log.error(msg.toString(), e);
             PersistenceManager.handleDBExceptionForAnySession(null, e, s);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(s);
         }
         return compNamePO;
     }
     
     
     /**
      * deletes ComponentNames from DataBase without a commit!
      * @param s the Session which is to use.
      * @param toDelete all component names marked for deletion.
      */
     public static final void deleteCompNames(EntityManager s,
             final Collection<IComponentNamePO> toDelete) throws PMException {
 
         lockComponentNames(s);
         for (IComponentNamePO compName : toDelete) {
             s.remove(compName);
         }
     }
     
     /**
      * deletes all ComponentNames of the Project with the 
      * given rootProjId from DataBase without a commit!
      * @param s the Session which is to use.
      * @param rootProjId the parent project ID.
      * @throws PMException in case of any db problem
      */
     public static final void deleteCompNames(EntityManager s, Long rootProjId) 
         throws PMException {
         
         try {
             lockComponentNames(s);
             final Query q = s.createQuery(
                     "delete from ComponentNamePO c where c.hbmParentProjectId = :rootProjId"); //$NON-NLS-1$
             q.setParameter("rootProjId", rootProjId); //$NON-NLS-1$
             q.executeUpdate();
         } catch (PersistenceException e) {
             OperationCanceledUtil.checkForOperationCanceled(e);
             log.error(Messages.CouldNotReadComponentNamesFromDBOfProjectWithID
                 + StringConstants.SPACE + StringConstants.APOSTROPHE
                 + String.valueOf(rootProjId) + StringConstants.APOSTROPHE, e);
             PersistenceManager.handleDBExceptionForAnySession(null, e, s);
         }
     }
     
     
     /**
      * Updates component names in the database without a commit.
      * @param s the Session to use.
      * @param compNamesBinding The object responsible for determining all of
      *                         types as which a component name is used/mapped.
      * @param guids GUIDs of component names with types that may need updating
      * @param rootProjId the parent project ID.
      */
     @SuppressWarnings("unchecked")
     public static final void updateCompNameTypes(EntityManager s, 
             IComponentNameMapper compNamesBinding,
             Collection<String> guids, Long rootProjId) 
         throws PMException, IncompatibleTypeException {
         
         if (guids.isEmpty()) {
             return;
         }
         try {
             lockComponentNames(s);
             final Query q = s.createQuery(
                     "select c from ComponentNamePO c where c.hbmGuid in :guidList AND c.hbmParentProjectId = :projId"); //$NON-NLS-1$
             q.setParameter("guidList", guids); //$NON-NLS-1$
             q.setParameter("projId", rootProjId); //$NON-NLS-1$
             final List<IComponentNamePO> compNames = q.getResultList();
             for (IComponentNamePO compName : compNames) {
                 final String guid = compName.getGuid();
                 
                 final String newType = 
                     ComponentNamesBP.getInstance().computeComponentType(
                             compName.getName(),
                             compNamesBinding.getUsedTypes(guid));
                 if ((newType != null && !newType.equals(
                         ComponentNamesBP.UNKNOWN_COMPONENT_TYPE))
                     || (newType != null 
                             && newType.equals(compName.getComponentType()))) {
                     compName.setComponentType(newType);
                 } else {
                     String currType = StringConstants.EMPTY;
                     IComponentNamePO masterCompName = 
                         GeneralStorage.getInstance()
                             .getMasterSession().find(
                                 PoMaker.getComponentNameClass(), 
                                 compName.getId());
                     if (masterCompName != null) {
                         currType = masterCompName.getComponentType();
                     }
                     // Component types are incompatible.
                     // Unlock the component names table and throw an 
                     // exception with information about the incompatibility.
                     unlockComponentNames();
                     StringBuilder msgbuild = new StringBuilder();
                     msgbuild.append(
                             Messages.ErrorSavingChangedComponentNameType);
                     msgbuild.append(StringConstants.DOT);
                     msgbuild.append(StringConstants.NEWLINE);
                     msgbuild.append(Messages.IncompatibleType);
                     msgbuild.append(currType);
                     msgbuild.append(StringConstants.APOSTROPHE);
                     msgbuild.append(StringConstants.SPACE);
                     msgbuild.append(StringConstants.MINUS);
                     msgbuild.append(StringConstants.RIGHT_INEQUALITY_SING);
                     msgbuild.append(StringConstants.SPACE);
                     msgbuild.append(StringConstants.APOSTROPHE);
                     msgbuild.append(compName.getComponentType());
                     msgbuild.append(StringConstants.APOSTROPHE);
                     msgbuild.append(StringConstants.EXCLAMATION_MARK);
                     String msg = msgbuild.toString();
                     throw new IncompatibleTypeException(
                             compName, msg, 
                             MessageIDs.E_COMP_TYPE_INCOMPATIBLE, new String[]{
                                     compName.getName(), 
                                     CompSystemI18n.getString(currType), 
                                     CompSystemI18n.getString(
                                             compName.getComponentType())});
                 }
             }
         } catch (PMObjectDeletedException e) {
             // Should not happen
             log.error(Messages.ExceptionShouldNotHappen, e);
             Assert.notReached(Messages.ExceptionShouldNotHappen 
                 + StringConstants.COLON + e);
         }
     }
     
     /**
      * Persists several Component Names to the database without a commit.
      * 
      * @param namePoList list of name objects
      * @param s A Session.
      * @param rootProjId the project ID.
      * @return a mapping from GUID of Component Names that were supposed to be 
      *         inserted in the database to the GUID of Component Names that 
      *         already exist in the database.     
      * @throws PMException in case of any db-problem
      */
     @SuppressWarnings("unchecked")
     public static final Map<String, String> writeNamePOList(
             final EntityManager s, Collection<IComponentNamePO> namePoList, 
             Long rootProjId) throws PMException {
         
         if (namePoList.isEmpty()) {
             return Collections.emptyMap();
         }
         
         IPersistentObject persObj = null;
         Set<String> names = new HashSet<String>();
         Set<IComponentNamePO> toAddSet = 
             new HashSet<IComponentNamePO>(namePoList);
         for (IComponentNamePO compName : toAddSet) {
             names.add(compName.getName());
         }
         Set<IComponentNamePO> preExistingNames = 
             new HashSet<IComponentNamePO>();
         Map<String, String> createdToExistingMap = 
             new HashMap<String, String>();
         try {
             lockComponentNames(s);
            for (ValueListIterator iter = new ValueListIterator(
                    new ArrayList<String>(names)); iter.hasNext();) {
                Query existingNamesQuery = s.createQuery(Q_PREEXISTING_NAMES);
                existingNamesQuery.setParameter(P_PARENT_PROJECT_ID, 
                        rootProjId);
                existingNamesQuery.setParameter(P_NAME_SET, iter.next());
                preExistingNames.addAll(existingNamesQuery.getResultList());
            }
             Set<IComponentNamePO> removeFromAddList = 
                 new HashSet<IComponentNamePO>();
             for (IComponentNamePO preExistingName : preExistingNames) {
                 for (IComponentNamePO nameToAdd : toAddSet) {
                     if (nameToAdd.getName().equals(preExistingName.getName())) {
                         createdToExistingMap.put(nameToAdd.getGuid(), 
                                 preExistingName.getGuid());
                         preExistingName.setComponentType(
                                 nameToAdd.getComponentType());
                         removeFromAddList.add(nameToAdd);
                         break;
                     }
                 }
             }
             toAddSet.removeAll(removeFromAddList);
             for (IPersistentObject namePO : toAddSet) {
                 persObj = namePO;
                 namePO.setParentProjectId(rootProjId);
                 s.persist(namePO);
             }
         } catch (PersistenceException e) {
             OperationCanceledUtil.checkForOperationCanceled(e);
             log.error(Messages.ErrorWritingComponentNamesToDBOfProjectID
                     + StringConstants.SPACE + StringConstants.APOSTROPHE
                     + String.valueOf(rootProjId) + StringConstants.APOSTROPHE, 
                         e);
             PersistenceManager.handleDBExceptionForAnySession(persObj, e, s);
         } 
         return createdToExistingMap;
     }
    
     /**
      * 
      * @param parentProjId the Parent Project ID
      * @param name the name of the Component
      * @param guid the GUID of the Component
      * @return an IComponentNamePO if the given name already exists in the 
      * given Project ID, null otherwise.
      * @throws PMException in case of any DB error.
      */
     public static final IComponentNamePO checkExistingName(Long parentProjId, 
             String guid, String name) throws PMException {
         
         EntityManager s = null;
         IComponentNamePO compNamePO = null;
         try {
             s = Persistor.instance().openSession();
             final Query q = s.createQuery(
                     "select compName from ComponentNamePO compName where compName.hbmParentProjectId = :parentProjId and not compName.hbmGuid = :guid and compName.hbmName = :name"); //$NON-NLS-1$
             q.setParameter("parentProjId", parentProjId); //$NON-NLS-1$
             q.setParameter("guid", guid); //$NON-NLS-1$
             q.setParameter("name", name); //$NON-NLS-1$
             compNamePO = (IComponentNamePO)q.getSingleResult();
         } catch (NoResultException nre) {
             // No result found. Fall through to return null as per javadoc.
         } catch (PersistenceException e) {
             OperationCanceledUtil.checkForOperationCanceled(e);
             StringBuilder msg = new StringBuilder();
             msg.append(Messages.CouldNotReadComponentNameWithParentProjID);
             msg.append(StringConstants.SPACE);
             msg.append(StringConstants.APOSTROPHE);
             msg.append(String.valueOf(parentProjId));
             msg.append(StringConstants.APOSTROPHE);
             msg.append(StringConstants.SPACE);
             msg.append(Messages.AndName);
             msg.append(StringConstants.SPACE);
             msg.append(StringConstants.APOSTROPHE);
             msg.append(String.valueOf(name));
             msg.append(StringConstants.APOSTROPHE);
             log.error(msg.toString(), e);
             PersistenceManager.handleDBExceptionForAnySession(null, e, s);
         } finally {
             Persistor.instance().dropSession(s);
         }
         return compNamePO;
     }
     
     /**
      * Gets a lock of the ComponentNames Table in database.
      * @param s A Session.
      * @throws PMAlreadyLockedException if no lock is available.
      */
     private static final void lockComponentNames(EntityManager s) 
         throws PMObjectDeletedException, PMAlreadyLockedException {
         
         final long timeOut = 5000;
         try {
             if (lockObj == null) {
                 initLockedObj();
             }
             final long start = System.currentTimeMillis();
             while (!LockManager.instance().lockPO(s, lockObj, false)) {
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException e) {
                     // nothing is OK here!
                 }
                 final long stop = System.currentTimeMillis();
                 if ((stop - start) > timeOut) {
                     throw new PMAlreadyLockedException(lockObj, 
                             Messages.CouldNotGetALockOnTableCOMPONENT_NAMES,
                             MessageIDs.E_DATABASE_GENERAL);
                 }
             }
         } catch (PMDirtyVersionException e) {
             // cannot happen because checkVersion == false!
             Assert.notReached(Messages.ExceptionShouldNotHappen 
                 + StringConstants.COLON + StringConstants.SPACE + e);
         }
     }
 
     /**
      * Releases the lock of the ComponentNames Table in database.
      */
     private static final void unlockComponentNames() {
         LockManager.instance().unlockPO(lockObj);
     }
 
     /**
      * Initializes the locking of the CompNames table
      */
     private static void loadLockedObj() {
         EntityManager sess = null;
         try {
             sess = Persistor.instance().openSession();
             EntityTransaction tx = sess.getTransaction();
             tx.begin();
             
             final Query q = sess.createQuery(
                     "select p from LockedObjectPO p where p.hbmObjectName = :hbmObjectName"); //$NON-NLS-1$
             q.setParameter("hbmObjectName", COMP_NAME_TABLE_ID); //$NON-NLS-1$
             
             try {
                 lockObj = (LockedObjectPO)q.getSingleResult();
             } catch (NoResultException nre) {
                 lockObj = null;
             }
             
             tx.commit();
         } catch (PersistenceException e) {
             throw new JBFatalAbortException(
                 Messages.ErrorInitializingComponentNamesLocking
                 + StringConstants.EXCLAMATION_MARK
                 , e, MessageIDs.E_DATABASE_GENERAL);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(sess);
         }
     }
     
     /**
      * Initializes the LockedObj.
      */
     private static void initLockedObj() {
         createOrUpdateCompNamesLock();
         loadLockedObj();
     }
    
     /**
      * 
      * @throws PersistenceException if we blow up
      */
     private static void createOrUpdateCompNamesLock() 
         throws PersistenceException {
         
         EntityManager s = null;
         EntityTransaction tx = null;
         try {
             s = Persistor.instance().openSession();
             tx = s.getTransaction();
             tx.begin();
             final Query q = s.createQuery(
                     "select p from LockedObjectPO p where p.hbmObjectName = :hbmObjectName"); //$NON-NLS-1$
             q.setParameter("hbmObjectName", COMP_NAME_TABLE_ID); //$NON-NLS-1$
             try {
                 q.getSingleResult();
             } catch (NoResultException nre) {
                 s.persist(new LockedObjectPO(COMP_NAME_TABLE_ID));
             }
 
             tx.commit();
         } catch (PersistenceException e) {
             if (tx != null) {
                 tx.rollback();
             }
             throw e;
         } finally {
             Persistor.instance().dropSession(s);
         }
     }
 
     /**
      * 
      * @param sess The session used to retrieve the number of reuse instances.
      * @param parentProjectId The ID of the parent Project of the Component Name
      *                        for which to find instances of reuse.
      * @param compNameGuid The GUID of the Component Name for which to find
      *                     instances of reuse.
      * @return the number of instances of reuse for the Component Name with the
      *         given GUID and parent Project.
      */
     public static synchronized int getNumReuseInstances(EntityManager sess, 
             Long parentProjectId, String compNameGuid) {
         Set<Long> emptySet = Collections.emptySet();
         return fillComponentNameTypeReuseCollection(sess, 
                 parentProjectId, compNameGuid, emptySet, 
                 emptySet, emptySet, new ArrayList<String>()).size();
     }
     
     /**
      * Returns a collection of <code>String</code>s representing the various
      * component types for which the given component name is used.
      * 
      * @param s A master session. This session will be used to perform the 
      *          check.
      * @param parentProjectId The id of the active project. Only reuses and 
      *                        component names belonging to the project with this
      *                        id will be considered during the check.
      * @param compNameGuid The guid of the component name to check.
      * @param ignoreNamePairIds Ids of all component name pairs to 
      *                          ignore during the check.
      * @param ignoreCapIds Ids of all test steps to 
      *                     ignore during the check.
      * @param ignoreAutIds Ids of all AUTs to ignore during the check.
      * @return the component types for which the given component name is reused.
      */
     public static synchronized Collection<String> getReuseTypes(
             EntityManager s, Long parentProjectId, 
             String compNameGuid, Set<Long> ignoreNamePairIds, 
             Set<Long> ignoreCapIds, Set<Long> ignoreAutIds) {
         
         final Collection<String> types = 
             fillComponentNameTypeReuseCollection(s, parentProjectId, 
                     compNameGuid, ignoreNamePairIds, ignoreCapIds, 
                     ignoreAutIds, new HashSet<String>());
         
         return types;
     }
 
     /**
      * Fills the given collection with all current types of reuse for the 
      * Component Name with the given GUID.
      * 
      * @param s The session in which to execute the various queries required to
      *          find the types of reuse.
      * @param parentProjectId The id of the active project. Only reuses and 
      *                        component names belonging to the project with this
      *                        id will be considered during the check.
      * @param compNameGuid The guid of the component name to check.
      * @param ignoreNamePairIds Ids of all component name pairs to 
      *                          ignore during the check.
      * @param ignoreCapIds Ids of all test steps to 
      *                     ignore during the check.
      * @param ignoreAutIds Ids of all AUTs to ignore during the check.
      * @param toFill The collection to fill.
      * @return <code>toFill</code>.
      */
     @SuppressWarnings("unchecked")
     private static synchronized Collection<String> 
     fillComponentNameTypeReuseCollection(
             EntityManager s, Long parentProjectId, 
             String compNameGuid, Set<Long> ignoreNamePairIds, 
             Set<Long> ignoreCapIds, Set<Long> ignoreAutIds,
             Collection<String> toFill) {
  
         boolean shouldIgnoreCaps = !ignoreCapIds.isEmpty();
 
         FlushModeType flushMode = s.getFlushMode();
         // Disable automatic flushing during this read-only operation because
         // a flush may cause database-level locks to be acquired.
         s.setFlushMode(FlushModeType.COMMIT);
         
         try {
             toFill.addAll(getPairReuseTypes(
                     compNameGuid, parentProjectId, ignoreNamePairIds, s));
             
             final Query mappingQuery = s.createQuery(Q_REUSE_TYPE_ASSOCS);
             mappingQuery.setParameter(P_PARENT_PROJECT_ID, parentProjectId);
             mappingQuery.setParameter(P_COMP_NAME_GUID, compNameGuid);
             
             List<IObjectMappingAssoziationPO> assocs = 
                 mappingQuery.getResultList();
             List<String> assocCompTypes = new ArrayList<String>();
             CompSystem compSystem = 
                 ComponentBuilder.getInstance().getCompSystem();
 
             // Find the toolkit corresponding to each Association. This allows
             // us to perform the necessary mapping from Component Class to
             // Component Type.
             CriteriaQuery query = s.getCriteriaBuilder().createQuery();
             List<IAUTMainPO> allAuts = s.createQuery(query.select(
                     query.from(PoMaker.getAUTMainClass()))).getResultList();
             for (IObjectMappingAssoziationPO assoc : assocs) {
                 IComponentIdentifier technicalName = assoc.getTechnicalName();
                 if (technicalName != null) {
                     for (IAUTMainPO aut : allAuts) {
                         if (!ignoreAutIds.contains(aut.getId())) {
                             if (aut.getObjMap().getMappings().contains(assoc)) {
                                 List<Component> availableComponents = 
                                     compSystem.getComponents(
                                             aut.getToolkit(), true);
                                 assocCompTypes.add(CompSystem.getComponentType(
                                         technicalName.getSupportedClassName(), 
                                         availableComponents));
                                 break;
                             }
                         }
                     }
                 }
             }
             
             toFill.addAll(assocCompTypes);
     
             StringBuilder capQuerySb = 
                 new StringBuilder(Q_REUSE_TYPE_CAPS);
             if (shouldIgnoreCaps) {
                 capQuerySb.append(SQ_REUSE_TYPE_CAPS_IGNORE);
             }
             final Query capQuery = s.createQuery(capQuerySb.toString());
             capQuery.setParameter(P_PARENT_PROJECT_ID, parentProjectId);
             capQuery.setParameter(P_COMP_NAME_GUID, compNameGuid);
             if (shouldIgnoreCaps) {
                 capQuery.setParameter(P_IGNORE_CAPS, ignoreCapIds);
             }
             toFill.addAll(capQuery.getResultList());
         } finally {
             s.setFlushMode(flushMode);
         }
         return toFill;
     }
     
     /**
      * 
      * @param compNameGuid The GUID of the Component Name for which to check.
      * @param parentProjectId The ID of the Project in which to check.
      * @param ignoreNamePairIds IDs of all Component Name Pairs to be ignored
      *                          during this search.
      * @param s The session in which the search is to occur.
      * @return the types of reuse with regards to Component Name Pairs.
      */
     @SuppressWarnings("unchecked")
     private static Collection<String> getPairReuseTypes(String compNameGuid,
             Long parentProjectId, Set<Long> ignoreNamePairIds, 
             EntityManager s) {
 
         Set<String> returnSet = new HashSet<String>();
         boolean shouldIgnorePairs = !ignoreNamePairIds.isEmpty();
         StringBuilder reuseQuerySb = 
             new StringBuilder(Q_REUSE_TYPE_PAIRS);
         if (shouldIgnorePairs) {
             reuseQuerySb.append(SQ_REUSE_TYPE_PAIRS_IGNORE);
         }
         final Query reuseQuery = s.createQuery(reuseQuerySb.toString());
         reuseQuery.setParameter(P_PARENT_PROJECT_ID, parentProjectId);
         if (shouldIgnorePairs) {
             reuseQuery.setParameter(P_IGNORE_PAIRS, ignoreNamePairIds);
         }
         reuseQuery.setParameter(P_COMP_NAME_GUID, compNameGuid);
         Collection<ICompNamesPairPO> compNamePairs = 
             new HashSet<ICompNamesPairPO>(reuseQuery.getResultList());
         
         Collection<Long> compNamePairIds = new HashSet<Long>();
         for (ICompNamesPairPO pair : compNamePairs) {
             compNamePairIds.add(pair.getId());
         }
         
         if (!compNamePairs.isEmpty()) {
             Query execTcQuery;
             if (compNamePairs.size() <= MAX_DB_EXPRESSIONS) {
                 execTcQuery = s.createQuery(Q_EXEC_TCS_WITH_PAIR);
                 execTcQuery.setParameter(P_PAIR_LIST, compNamePairIds);
             } else {
                 // FIXME zeb The code in this "else" block is a quick-fix for the
                 //           "ORA-01795: maximum number of expressions in a list is 1000".
                 StringBuilder sb = new StringBuilder();
                 sb.append("select execTc from ExecTestCasePO execTc" //$NON-NLS-1$
                         + " left outer join execTc.hbmCompNamesMap as compNamesMap " //$NON-NLS-1$
                         + "where"); //$NON-NLS-1$
                 Object [] compNamePairArray = compNamePairIds.toArray();
                 int numLists = 
                     (compNamePairArray.length / MAX_DB_EXPRESSIONS) + 1;
                 for (int i = 0; i < numLists; i++) {
                     if (i != 0) {
                         sb.append(" or"); //$NON-NLS-1$
                     }
                     sb.append(" compNamesMap.id in :" + P_PAIR_LIST + i); //$NON-NLS-1$
                 }
                 execTcQuery = s.createQuery(sb.toString());
                 for (int i = 0; i < numLists; i++) {
                     Object [] subArray = 
                         new Object[Math.min((i + 1) * MAX_DB_EXPRESSIONS, 
                                 compNamePairArray.length)
                             - (i * MAX_DB_EXPRESSIONS)];
                     System.arraycopy(
                             compNamePairArray, (i * MAX_DB_EXPRESSIONS), 
                             subArray, 0, subArray.length);
                     execTcQuery.setParameter(P_PAIR_LIST + i, subArray);
                 }
             }
 
             List<IExecTestCasePO> list = execTcQuery.getResultList();
             for (IExecTestCasePO execTc : list) {
                 for (ICompNamesPairPO pair : execTc.getCompNamesPairs()) {
                     if (compNamePairs.contains(pair)) {
                         CompNamesBP.searchCompType(pair, execTc);
                     }
                 }
             }
             
             for (ICompNamesPairPO pair : compNamePairs) {
                 returnSet.add(pair.getType());
             }
         }
         
         return returnSet;
     }
     
     /**
      * Saves the Component Name changes from <code>compNamesBinding</code> to 
      * database. Note that this method does not perform a commit.
      * 
      * @param session The session to use for the operation.
      * @param projId The ID of the currently active project.
      * @param compNamesBinding The Component Names mapper to use for the 
      *                         operation.
      */
     public static void flushCompNames(EntityManager session, Long projId,
             IWritableComponentNameMapper compNamesBinding) 
         throws PMException, IncompatibleTypeException {
 
         // No need to take any action for renamed Component Names. 
         // "Rename" changes will be automatically written to the database on 
         // commit because the Component Names belong to the session and have 
         // been modified.
         
         IWritableComponentNameCache cache = compNamesBinding.getCompNameCache();
         Collection<IComponentNamePO> namesToInsert = 
             cache.getNewNames();
         Collection<String> namesWithChangedReuse = 
             cache.getReusedNames(); 
         Collection<IComponentNamePO> namesToDelete = 
             cache.getDeletedNames();
 
         Map<String, String> preExistingGuidMap = 
             writeNamePOList(session, namesToInsert, projId);
         compNamesBinding.handleExistingNames(preExistingGuidMap);
         
         Set<String> namesToChangeReuse = 
             new HashSet<String>(namesWithChangedReuse);
         namesToChangeReuse.addAll(preExistingGuidMap.values());
 
         updateCompNameTypes(session, compNamesBinding, 
                 namesToChangeReuse, projId);
         
         deleteCompNames(session, namesToDelete);
 
     }
 
     /**
      * Deletes all unused Component Names that reference other Component Names.
      * Will only delete Component Names belonging to the Project with the given
      * ID. The search for reuse instances is also limited to the scope of the 
      * Project with the given ID.
      * 
      * @param projectId The ID of the Project to use as the scope for this
      *                  operation.
      * @param session The session in which the operation will take place.
      */
     @SuppressWarnings("unchecked")
     public static void removeUnusedCompNames(
             Long projectId, EntityManager session) {
 
         Query refCompNameGuidQuery = session.createQuery(Q_REF_COMP_NAME_GUIDS);
         refCompNameGuidQuery.setParameter(P_PARENT_PROJECT_ID, projectId);
         List refCompNameGuids = refCompNameGuidQuery.getResultList();
 
         if (refCompNameGuids.isEmpty()) {
             return;
         }
         
         Query capQuery = session.createQuery(Q_CAP_COMP_NAME_GUIDS);
         capQuery.setParameter(P_PARENT_PROJECT_ID, projectId);
         List capCompNames = capQuery.getResultList();
         
         refCompNameGuids.removeAll(capCompNames);
 
         if (refCompNameGuids.isEmpty()) {
             return;
         }
         
         Query pairQuery = session.createQuery(Q_PAIR_FIRST_COMP_NAME_GUIDS);
         pairQuery.setParameter(P_PARENT_PROJECT_ID, projectId);
         List pairCompNameGuids = pairQuery.getResultList();
 
         refCompNameGuids.removeAll(pairCompNameGuids);
 
         if (refCompNameGuids.isEmpty()) {
             return;
         }
 
         pairQuery = session.createQuery(Q_PAIR_SECOND_COMP_NAME_GUIDS);
         pairQuery.setParameter(P_PARENT_PROJECT_ID, projectId);
         pairCompNameGuids = pairQuery.getResultList();
 
         refCompNameGuids.removeAll(pairCompNameGuids);
 
         if (refCompNameGuids.isEmpty()) {
             return;
         }
 
         Query assocQuery = session.createQuery(Q_ASSOC_COMP_NAME_GUIDS);
         assocQuery.setParameter(P_PARENT_PROJECT_ID, projectId);
         List assocCompNameGuids = assocQuery.getResultList();
         
         refCompNameGuids.removeAll(assocCompNameGuids);
 
         if (refCompNameGuids.isEmpty()) {
             return;
         }
 
         Query compNameRefQuery = session.createQuery(Q_COMP_NAME_REF_GUIDS);
         compNameRefQuery.setParameter(P_PARENT_PROJECT_ID, projectId);
         List compNameRefGuidList = compNameRefQuery.getResultList();
         
         refCompNameGuids.removeAll(compNameRefGuidList);
         
         if (refCompNameGuids.isEmpty()) {
             return;
         }
         
         Query deleteQuery = session.createQuery(Q_DELETE_COMP_NAMES);
         deleteQuery.setParameter(P_PARENT_PROJECT_ID, projectId);
         deleteQuery.setParameter(P_COMP_NAME_REMOVAL_LIST, refCompNameGuids);
         deleteQuery.executeUpdate();
         
     }
     
     /**
      * sets lockObj to null, when database is changed
      */
     public static void dispose() {
         lockObj = null;
     }
 
 }
