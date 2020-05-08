 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.meta.service;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.StopWatch;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import eionet.meta.DElemAttribute;
 import eionet.meta.DElemAttribute.ParentType;
 import eionet.meta.dao.DAOException;
 import eionet.meta.dao.IAttributeDAO;
 import eionet.meta.dao.IDataElementDAO;
 import eionet.meta.dao.IFolderDAO;
 import eionet.meta.dao.IRdfNamespaceDAO;
 import eionet.meta.dao.ISiteCodeDAO;
 import eionet.meta.dao.IVocabularyConceptDAO;
 import eionet.meta.dao.IVocabularyFolderDAO;
 import eionet.meta.dao.domain.DataElement;
 import eionet.meta.dao.domain.Folder;
 import eionet.meta.dao.domain.RdfNamespace;
 import eionet.meta.dao.domain.SimpleAttribute;
 import eionet.meta.dao.domain.SiteCodeStatus;
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.service.data.ObsoleteStatus;
 import eionet.meta.service.data.VocabularyConceptData;
 import eionet.meta.service.data.VocabularyConceptFilter;
 import eionet.meta.service.data.VocabularyConceptResult;
 import eionet.meta.service.data.VocabularyFilter;
 import eionet.meta.service.data.VocabularyResult;
 import eionet.util.Pair;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.Util;
 
 /**
  * Vocabulary service.
  *
  * @author Juhan Voolaid
  */
 @Service
 @Transactional
 public class VocabularyServiceImpl implements IVocabularyService {
 
     /** Logger. */
     protected static final Logger LOGGER = Logger.getLogger(VocabularyServiceImpl.class);
 
     /** Vocabulary folder DAO. */
     @Autowired
     private IVocabularyFolderDAO vocabularyFolderDAO;
 
     /** Vocabulary concept DAO. */
     @Autowired
     private IVocabularyConceptDAO vocabularyConceptDAO;
 
     /** Site Code DAO. */
     @Autowired
     private ISiteCodeDAO siteCodeDAO;
 
     /** Attribute DAO. */
     @Autowired
     private IAttributeDAO attributeDAO;
 
     /** Folder DAO. */
     @Autowired
     private IFolderDAO folderDAO;
 
     /** Data element DAO. */
     @Autowired
     private IDataElementDAO dataElementDAO;
 
     /** namespace DAO. */
     @Autowired
     private IRdfNamespaceDAO namespaceDAO;
 
     /** special elements . */
     private static EnumMap<RelationalElement, String> relationalElements;
 
     static {
         if (relationalElements == null) {
             relationalElements = new EnumMap<RelationalElement, String>(RelationalElement.class);
             relationalElements.put(RelationalElement.BROADER_CONCEPT, "skos:broader");
             relationalElements.put(RelationalElement.NARROWER_CONCEPT, "skos:narrower");
             relationalElements.put(RelationalElement.RELATED_CONCEPT, "skos:related");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Folder getFolder(int folderId) throws ServiceException {
         try {
             return folderDAO.getFolder(folderId);
         } catch (Exception e) {
             throw new ServiceException("Failed to get folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isFolderEmpty(int folderId) throws ServiceException {
         try {
             return folderDAO.isFolderEmpty(folderId);
         } catch (Exception e) {
             throw new ServiceException("Failed to check if folder is empty: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteFolder(int folderId) throws ServiceException {
         try {
             if (!folderDAO.isFolderEmpty(folderId)) {
                 throw new IllegalStateException("Folder is not empty");
             }
             folderDAO.deleteFolder(folderId);
         } catch (Exception e) {
             throw new ServiceException("Failed to delete folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updateFolder(Folder folder) throws ServiceException {
         try {
             Folder f = folderDAO.getFolder(folder.getId());
             f.setIdentifier(folder.getIdentifier());
             f.setLabel(folder.getLabel());
             folderDAO.updateFolder(f);
         } catch (Exception e) {
             throw new ServiceException("Failed to update folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Folder> getFolders(String userName, int... expandedFolders) throws ServiceException {
         try {
             List<Folder> result = folderDAO.getFolders();
             if (expandedFolders != null && expandedFolders.length > 0) {
                 for (Folder f : result) {
                     for (int expandedId : expandedFolders) {
                         if (f.getId() == expandedId) {
                             f.setExpanded(true);
                             f.setItems(vocabularyFolderDAO.getVocabularyFolders(expandedId, userName));
                         }
                     }
                 }
             }
             return result;
         } catch (Exception e) {
             throw new ServiceException("Failed to get folders: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getVocabularyFolders(String userName) throws ServiceException {
         try {
             return vocabularyFolderDAO.getVocabularyFolders(userName);
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary folders: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(rollbackFor = ServiceException.class)
     public int createVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder, String userName)
             throws ServiceException {
         try {
             if (StringUtils.isEmpty(vocabularyFolder.getContinuityId())) {
                 vocabularyFolder.setContinuityId(Util.generateContinuityId(vocabularyFolder));
             }
             // Validate type
             if (vocabularyFolder.isSiteCodeType() && !vocabularyFolder.isNumericConceptIdentifiers()) {
                 throw new IllegalArgumentException("Site code type vocabulary must have numeric concept identifiers");
             }
 
             if (vocabularyFolder.isSiteCodeType() && siteCodeDAO.siteCodeFolderExists()) {
                 throw new IllegalStateException("Vocabulary folder with type 'SITE_CODE' already exists");
             }
 
             if (newFolder != null) {
                 int newFolderId = folderDAO.createFolder(newFolder);
                 vocabularyFolder.setFolderId(newFolderId);
             }
 
             vocabularyFolder.setUserModified(userName);
             return vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);
         } catch (Exception e) {
             throw new ServiceException("Failed to create vocabulary folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy) throws ServiceException {
         try {
             VocabularyFolder result = vocabularyFolderDAO.getVocabularyFolder(folderName, identifier, workingCopy);
 
             // Load attributes
             List<List<SimpleAttribute>> attributes = attributeDAO.getVocabularyFolderAttributes(result.getId(), true);
             result.setAttributes(attributes);
 
             return result;
         } catch (Exception e) {
             String parameters =
                     "folderName=" + String.valueOf(folderName) + "; identifier=" + String.valueOf(identifier) + "; workingCopy="
                             + workingCopy;
             throw new ServiceException("Failed to get vocabulary folder (" + parameters + "):" + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyConceptResult searchVocabularyConcepts(VocabularyConceptFilter filter) throws ServiceException {
         try {
             return vocabularyConceptDAO.searchVocabularyConcepts(filter);
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary concepts: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional
     public int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) throws ServiceException {
         try {
             VocabularyFolder vocFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
             if (vocFolder != null && vocFolder.isNotationsEqualIdentifiers()) {
                 vocabularyConcept.setNotation(vocabularyConcept.getIdentifier());
             }
             return vocabularyConceptDAO.createVocabularyConcept(vocabularyFolderId, vocabularyConcept);
         } catch (Exception e) {
             throw new ServiceException("Failed to create vocabulary concept: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(rollbackFor = ServiceException.class)
     public void updateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException {
         try {
             VocabularyFolder vocFolder = vocabularyFolderDAO.getVocabularyFolderOfConcept(vocabularyConcept.getId());
             if (vocFolder != null && vocFolder.isNotationsEqualIdentifiers()) {
                 vocabularyConcept.setNotation(vocabularyConcept.getIdentifier());
             }
 
             vocabularyConceptDAO.updateVocabularyConcept(vocabularyConcept);
             // updateVocabularyConceptAttributes(vocabularyConcept);
             updateVocabularyConceptDataElementValues(vocabularyConcept);
         } catch (Exception e) {
             throw new ServiceException("Failed to update vocabulary concept: " + e.getMessage(), e);
         }
 
     }
 
     /**
      * updates bound element values included related bound elements.
      * @param vocabularyConcept concept
      */
     private void updateVocabularyConceptDataElementValues(VocabularyConcept vocabularyConcept) {
         List<DataElement> dataElementValues = new ArrayList<DataElement>();
         if (vocabularyConcept.getElementAttributes() != null) {
             for (List<DataElement> values : vocabularyConcept.getElementAttributes()) {
                 if (values != null) {
                     for (DataElement value : values) {
                         if (value != null
                                 && (StringUtils.isNotEmpty(value.getAttributeValue()) || value.getRelatedConceptId() != null)) {
                             dataElementValues.add(value);
                         }
                     }
                 }
             }
         }
 
         dataElementDAO.deleteVocabularyConceptDataElementValues(vocabularyConcept.getId());
         if (dataElementValues.size() > 0) {
             dataElementDAO.insertVocabularyConceptDataElementValues(vocabularyConcept.getId(), dataElementValues);
         }
 
         fixRelatedElements(vocabularyConcept, dataElementValues);
     }
 
     /**
      * As a last step when updating vocabulary concept, this method checks all the binded elements that represent relations and
      * makes sure that the concepts are related in both sides (A related with B -> B related with A). Also when relation gets
      * deleted from one side, then we make sure to deleted it also from the other side of the relation.
      *
      * @param vocabularyConcept
      *            Concept to be updated
      * @param dataElementValues bound data elements with values
      */
     private void fixRelatedElements(VocabularyConcept vocabularyConcept, List<DataElement> dataElementValues) {
         try {
             // delete all existing relations:
             dataElementDAO.deleteRelatedElements(vocabularyConcept.getId());
 
             for (DataElement elem : dataElementValues) {
                 if (elem.isRelationalElement()) {
                     int relatedConceptId = elem.getRelatedConceptId();
                     VocabularyConcept relatedConcept = vocabularyConceptDAO.getVocabularyConcept(relatedConceptId);
                     List<DataElement> relatedElementValues = new ArrayList<DataElement>();
                     DataElement relationalElement = null;
 
                     try {
                         if (elem.getIdentifier().equals(getRelationalElementPrefix(RelationalElement.RELATED_CONCEPT))) {
                             relationalElement =
                                     dataElementDAO.getDataElement(getRelationalElementPrefix(RelationalElement.RELATED_CONCEPT));
                         } else if (elem.getIdentifier().equals(getRelationalElementPrefix(RelationalElement.BROADER_CONCEPT))) {
                             relationalElement =
                                     dataElementDAO.getDataElement(getRelationalElementPrefix(RelationalElement.NARROWER_CONCEPT));
 
                         } else if (elem.getIdentifier().equals(getRelationalElementPrefix(RelationalElement.NARROWER_CONCEPT))) {
                             relationalElement =
                                     dataElementDAO.getDataElement(getRelationalElementPrefix(RelationalElement.BROADER_CONCEPT));
                         }
                     } catch (Exception e) {
                         LOGGER.warn("Relational skos element not defined " + e);
                     }
 
                     if (relationalElement != null) {
                         relationalElement.setAttributeLanguage(elem.getAttributeLanguage());
                         relationalElement.setAttributeValue(elem.getAttributeValue());
                         relationalElement.setRelatedConceptId(vocabularyConcept.getId());
                         relatedElementValues.add(relationalElement);
                         dataElementDAO.insertVocabularyConceptDataElementValues(relatedConcept.getId(), relatedElementValues);
                     }
 
                 }
             }
         } catch (Exception e) {
             LOGGER.warn("Handling related element bindings failed " + e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void quickUpdateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException {
         try {
             VocabularyFolder vocFolder = vocabularyFolderDAO.getVocabularyFolderOfConcept(vocabularyConcept.getId());
             if (vocFolder != null && vocFolder.isNotationsEqualIdentifiers()) {
                 vocabularyConcept.setNotation(vocabularyConcept.getIdentifier());
             }
             vocabularyConceptDAO.updateVocabularyConcept(vocabularyConcept);
         } catch (Exception e) {
             throw new ServiceException("Failed to update vocabulary concept: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional
     public void updateVocabularyFolder(VocabularyFolder vocabularyFolder, Folder newFolder) throws ServiceException {
         try {
             VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolder.getId());
 
             vf.setIdentifier(vocabularyFolder.getIdentifier());
             vf.setLabel(vocabularyFolder.getLabel());
             vf.setRegStatus(vocabularyFolder.getRegStatus());
             vf.setNumericConceptIdentifiers(vocabularyFolder.isNumericConceptIdentifiers());
             vf.setNotationsEqualIdentifiers(vocabularyFolder.isNotationsEqualIdentifiers());
             vf.setBaseUri(vocabularyFolder.getBaseUri());
             vf.setFolderId(vocabularyFolder.getFolderId());
 
             if (newFolder != null) {
                 int newFolderId = folderDAO.createFolder(newFolder);
                 vf.setFolderId(newFolderId);
             }
 
             vocabularyFolderDAO.updateVocabularyFolder(vf);
 
             attributeDAO.updateSimpleAttributes(vocabularyFolder.getId(), DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(),
                     vocabularyFolder.getAttributes());
 
             if (vf.isNotationsEqualIdentifiers()) {
                 LOGGER.debug("Forcing all concept notations to be equal with identifiers!");
                 vocabularyFolderDAO.forceNotationsToIdentifiers(vf.getId());
             }
         } catch (Exception e) {
             throw new ServiceException("Failed to update vocabulary folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyConcepts(List<Integer> ids) throws ServiceException {
         try {
             vocabularyConceptDAO.deleteVocabularyConcepts(ids);
         } catch (Exception e) {
             throw new ServiceException("Failed to delete vocabulary concepts: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void markConceptsObsolete(List<Integer> ids) throws ServiceException {
         try {
             vocabularyConceptDAO.markConceptsObsolete(ids);
         } catch (Exception e) {
             throw new ServiceException("Failed to mark the concepts obsolete: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void unMarkConceptsObsolete(List<Integer> ids) throws ServiceException {
         try {
             vocabularyConceptDAO.unMarkConceptsObsolete(ids);
         } catch (Exception e) {
             throw new ServiceException("Failed to delete remove obsolete status: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyFolders(List<Integer> ids) throws ServiceException {
         try {
             vocabularyFolderDAO.deleteVocabularyFolders(ids);
             attributeDAO.deleteAttributes(ids, DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());
         } catch (Exception e) {
             throw new ServiceException("Failed to delete vocabulary folders: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyFolder(int vocabularyFolderId) throws ServiceException {
         try {
             return vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
         } catch (Exception e) {
             String parameters = "id=" + vocabularyFolderId;
             throw new ServiceException("Failed to get vocabulary folder ( " + parameters + "): " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(rollbackFor = ServiceException.class)
     public int checkOutVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException {
         if (StringUtils.isBlank(userName)) {
             throw new IllegalArgumentException("User name must not be blank!");
         }
 
         try {
             StopWatch timer = new StopWatch();
             timer.start();
             VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
 
             if (vocabularyFolder.isWorkingCopy()) {
                 throw new ServiceException("Cannot check out a working copy!");
             }
 
             if (StringUtils.isNotBlank(vocabularyFolder.getWorkingUser())) {
                 throw new ServiceException("Cannot check out an already checked-out vocabulary folder!");
             }
 
             // Update existing working user
             vocabularyFolder.setWorkingUser(userName);
             vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);
 
             // Make new copy of vocabulary folder
             vocabularyFolder.setCheckedOutCopyId(vocabularyFolderId);
             vocabularyFolder.setWorkingCopy(true);
             int newVocabularyFolderId = vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);
 
             // Copy simple attributes.
             attributeDAO.copySimpleAttributes(vocabularyFolderId, DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(),
                     newVocabularyFolderId);
 
             // Copy the vocabulary concepts under new vocabulary folder (except of site code type)
             if (!vocabularyFolder.isSiteCodeType()) {
                 vocabularyConceptDAO.copyVocabularyConcepts(vocabularyFolderId, newVocabularyFolderId);
 
                 dataElementDAO.checkoutVocabularyConceptDataElementValues(newVocabularyFolderId);
                 //dataElementDAO.updateRelatedConceptIds(newVocabularyFolderId);
             }
 
             // Copy data element relations
             dataElementDAO.copyVocabularyDataElements(vocabularyFolderId, newVocabularyFolderId);
 
             timer.stop();
             LOGGER.debug("Check-out lasted: " + timer.toString());
             return newVocabularyFolderId;
         } catch (Exception e) {
             throw new ServiceException("Failed to check-out vocabulary folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(rollbackFor = ServiceException.class)
     public int checkInVocabularyFolder(int vocabularyFolderId, String userName) throws ServiceException {
         if (StringUtils.isBlank(userName)) {
             throw new IllegalArgumentException("User name must not be blank!");
         }
 
         try {
             StopWatch timer = new StopWatch();
             timer.start();
             VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
 
             if (!vocabularyFolder.isWorkingCopy()) {
                 throw new ServiceException("Vocabulary is not a working copy.");
             }
 
             if (!StringUtils.equals(userName, vocabularyFolder.getWorkingUser())) {
                 throw new ServiceException("Check-in user is not the current working user.");
             }
 
             int originalVocabularyFolderId = vocabularyFolder.getCheckedOutCopyId();
 
             if (!vocabularyFolder.isSiteCodeType()) {
                 //reference type relations in other vocabularies must get new id's
                 vocabularyConceptDAO.moveReferenceConcepts(originalVocabularyFolderId, vocabularyFolderId);
                 // Remove old vocabulary concepts
                 vocabularyConceptDAO.deleteVocabularyConcepts(originalVocabularyFolderId);
                 // Remove old data element relations
                 dataElementDAO.deleteVocabularyDataElements(originalVocabularyFolderId);
             }
 
             // Update original vocabulary folder
             vocabularyFolder.setCheckedOutCopyId(0);
             vocabularyFolder.setId(originalVocabularyFolderId);
             vocabularyFolder.setUserModified(userName);
             vocabularyFolder.setDateModified(new Date());
             vocabularyFolder.setWorkingCopy(false);
             vocabularyFolder.setWorkingUser(null);
             vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);
 
             if (!vocabularyFolder.isSiteCodeType()) {
                 // Move new vocabulary concepts to folder
                 vocabularyConceptDAO.moveVocabularyConcepts(vocabularyFolderId, originalVocabularyFolderId);
                 // Move data element relations to folder
                 dataElementDAO.moveVocabularyDataElements(vocabularyFolderId, originalVocabularyFolderId);
             }
 
             // Delete old attributes first and then change the parent ID of the new ones
             attributeDAO.deleteAttributes(Collections.singletonList(originalVocabularyFolderId),
                     DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());
             attributeDAO.replaceParentId(vocabularyFolderId, originalVocabularyFolderId,
                     DElemAttribute.ParentType.VOCABULARY_FOLDER);
 
             // Delete checked out version
             vocabularyFolderDAO.deleteVocabularyFolders(Collections.singletonList(vocabularyFolderId));
 
             timer.stop();
             LOGGER.debug("Check-in lasted: " + timer.toString());
             return originalVocabularyFolderId;
         } catch (Exception e) {
             throw new ServiceException("Failed to check-in vocabulary folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(rollbackFor = ServiceException.class)
     public int createVocabularyFolderCopy(VocabularyFolder vocabularyFolder, int vocabularyFolderId, String userName,
             Folder newFolder) throws ServiceException {
         try {
             VocabularyFolder originalVocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
 
             if (originalVocabularyFolder.isSiteCodeType()) {
                 throw new IllegalArgumentException("Cannot make copy of vocabulary with type 'SITE_CODE'");
             }
 
             if (newFolder != null) {
                 int newFolderId = folderDAO.createFolder(newFolder);
                 vocabularyFolder.setFolderId(newFolderId);
             }
 
             vocabularyFolder.setContinuityId(originalVocabularyFolder.getContinuityId());
             vocabularyFolder.setUserModified(userName);
             int newVocabularyFolderId = vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);
 
             // Copy simple attributes.
             attributeDAO.copySimpleAttributes(vocabularyFolderId, DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(),
                     newVocabularyFolderId);
 
             dataElementDAO.copyVocabularyDataElements(vocabularyFolderId, newVocabularyFolderId);
 
             List<VocabularyConcept> concepts = vocabularyConceptDAO.getVocabularyConcepts(vocabularyFolderId);
             for (VocabularyConcept vc : concepts) {
                 vocabularyConceptDAO.createVocabularyConcept(newVocabularyFolderId, vc);
             }
             dataElementDAO.copyVocabularyConceptDataElementValues(vocabularyFolderId, newVocabularyFolderId);
             //dataElementDAO.updateRelatedConceptIds(newVocabularyFolderId);
 
             return newVocabularyFolderId;
         } catch (Exception e) {
             throw new ServiceException("Failed to create vocabulary folder copy: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName)
             throws ServiceException {
         try {
             return vocabularyFolderDAO.getVocabularyFolderVersions(continuityId, vocabularyFolderId, userName);
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary folder versions: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int undoCheckOut(int vocabularyFolderId, String userName) throws ServiceException {
         if (StringUtils.isBlank(userName)) {
             throw new IllegalArgumentException("User name must not be blank!");
         }
 
         try {
             VocabularyFolder vocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
             if (!vocabularyFolder.isWorkingCopy()) {
                 throw new ServiceException("Vocabulary is not a working copy.");
             }
 
             if (!StringUtils.equals(userName, vocabularyFolder.getWorkingUser())) {
                 throw new ServiceException("Check-in user is not the current working user.");
             }
 
             int originalVocabularyFolderId = vocabularyFolder.getCheckedOutCopyId();
 
             // Update original vocabulary folder
             VocabularyFolder originalVocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(originalVocabularyFolderId);
             originalVocabularyFolder.setCheckedOutCopyId(0);
             originalVocabularyFolder.setWorkingUser(null);
             vocabularyFolderDAO.updateVocabularyFolder(originalVocabularyFolder);
 
             // Delete checked out version
             vocabularyFolderDAO.deleteVocabularyFolders(Collections.singletonList(vocabularyFolderId));
             attributeDAO.deleteAttributes(Collections.singletonList(vocabularyFolderId),
                     DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());
 
             return originalVocabularyFolderId;
         } catch (Exception e) {
             throw new ServiceException("Failed to undo checkout for vocabulary folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) throws ServiceException {
         try {
             return vocabularyFolderDAO.getVocabularyWorkingCopy(checkedOutCopyId);
         } catch (Exception e) {
             throw new ServiceException("Failed to get the checked out vocabulary folder: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isUniqueFolderIdentifier(String identifier, int excludedId) throws ServiceException {
         try {
             return folderDAO.isFolderUnique(identifier, excludedId);
         } catch (Exception e) {
             throw new ServiceException("Failed to check unique folder identifier: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds)
             throws ServiceException {
         try {
             return vocabularyFolderDAO.isUniqueVocabularyFolderIdentifier(folderId, identifier, excludedVocabularyFolderIds);
         } catch (Exception e) {
             throw new ServiceException("Failed to check unique vocabulary identifier: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId)
             throws ServiceException {
         try {
             return vocabularyConceptDAO.isUniqueConceptIdentifier(identifier, vocabularyFolderId, vocabularyConceptId);
         } catch (Exception e) {
             throw new ServiceException("Failed to check unique concept identifier: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(rollbackFor = ServiceException.class)
     public void reserveFreeSiteCodes(int vocabularyFolderId, int amount, int startIdentifier, String userName)
             throws ServiceException {
         try {
             VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
 
             if (vf.isWorkingCopy()) {
                 throw new IllegalStateException("Vocabulary folder cannot be checked out");
             }
             if (!vf.isSiteCodeType()) {
                 throw new IllegalStateException("Vocabulary folder must be site code type");
             }
 
             String definition = "Added by " + userName + " on " + Util.formatDateTime(new Date());
             String label = "<" + SiteCodeStatus.AVAILABLE.toString().toLowerCase() + ">";
 
             // Insert empty concepts
             vocabularyConceptDAO.insertEmptyConcepts(vocabularyFolderId, amount, startIdentifier, label, definition);
 
             // Get added concepts
             VocabularyConceptFilter filter = new VocabularyConceptFilter();
             filter.setVocabularyFolderId(vf.getId());
             filter.setDefinition(definition);
             filter.setLabel(label);
             filter.setUsePaging(false);
 
             VocabularyConceptResult newConceptsResult = vocabularyConceptDAO.searchVocabularyConcepts(filter);
 
             // Insert Site code records
             siteCodeDAO.insertSiteCodesFromConcepts(newConceptsResult.getList(), userName);
 
             LOGGER.info(userName + " created " + amount + " new site codes.");
 
         } catch (Exception e) {
             throw new ServiceException("Failed to reserve empty site codes: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getNextIdentifierValue(int vocabularyFolderId) throws ServiceException {
         try {
             return vocabularyConceptDAO.getNextIdentifierValue(vocabularyFolderId);
         } catch (Exception e) {
             throw new ServiceException("Failed to get next concept identifier: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Integer> checkAvailableIdentifiers(int vocabularyFolderId, int amount, int startingIdentifier)
             throws ServiceException {
         try {
             return vocabularyConceptDAO.checkAvailableIdentifiers(vocabularyFolderId, amount, startingIdentifier);
         } catch (Exception e) {
             throw new ServiceException("Failed to check available identifiers: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyConcept getVocabularyConcept(int vocabularyFolderId, String conceptIdentifier, boolean emptyAttributes)
             throws ServiceException {
         try {
             VocabularyConcept result = vocabularyConceptDAO.getVocabularyConcept(vocabularyFolderId, conceptIdentifier);
 
             List<List<DataElement>> elementAttributes =
                     dataElementDAO.getVocabularyConceptDataElementValues(vocabularyFolderId, result.getId(), emptyAttributes);
             result.setElementAttributes(elementAttributes);
 
             return result;
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary concept: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyConcept getVocabularyConcept(int vocabularyConceptId) throws ServiceException {
         try {
             VocabularyConcept result = vocabularyConceptDAO.getVocabularyConcept(vocabularyConceptId);
 
             return result;
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary concept: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyConcept> getVocabularyConceptsWithAttributes(int vocabularyFolderId, boolean numericConceptIdentifiers,
             ObsoleteStatus obsoleteStatus) throws ServiceException {
         try {
 
             VocabularyConceptFilter filter = new VocabularyConceptFilter();
             filter.setVocabularyFolderId(vocabularyFolderId);
             filter.setUsePaging(false);
             filter.setNumericIdentifierSorting(numericConceptIdentifiers);
             filter.setObsoleteStatus(obsoleteStatus);
 
             List<VocabularyConcept> result = vocabularyConceptDAO.searchVocabularyConcepts(filter).getList();
 
             for (VocabularyConcept vc : result) {
 
                 List<List<DataElement>> elementAttributes =
                         dataElementDAO.getVocabularyConceptDataElementValues(vocabularyFolderId, vc.getId(), false);
                 vc.setElementAttributes(elementAttributes);
             }
 
             return result;
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary concept: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void pingCrToReharvestVocabulary(int vocabularyFolderId) throws ServiceException {
 
         String crPingUrl = Props.getProperty(PropsIF.CR_PING_URL);
 
         if (!Util.isEmpty(crPingUrl)) {
 
             VocabularyFolder vocabluary = getVocabularyFolder(vocabularyFolderId);
             StringBuilder rdfUrl = new StringBuilder(Props.getProperty(PropsIF.DD_URL));
 
             if (!Props.getProperty(PropsIF.DD_URL).endsWith("/")) {
                 rdfUrl.append("/");
             }
             rdfUrl.append("vocabulary/");
             rdfUrl.append(vocabluary.getFolderName());
             rdfUrl.append("/");
             rdfUrl.append(vocabluary.getIdentifier());
             rdfUrl.append("/rdf");
 
             try {
                 crPingUrl = String.format(crPingUrl, URLEncoder.encode(rdfUrl.toString(), "UTF-8"));
 
                 if (Util.isURI(crPingUrl)) {
                     URL url = new URL(crPingUrl);
                     HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                     int status = httpConn.getResponseCode();
                     if (status >= 400) {
                         LOGGER.error("Unable to ping CR (responseCode: " + status + ") for reharvesting vocabulary folder: "
                                 + crPingUrl);
                     } else {
                         LOGGER.debug("Ping request (responseCode: " + status
                                 + ") was sent to CR for reharvesting vocabulary folder: " + crPingUrl);
                     }
                 }
             } catch (MalformedURLException e) {
                 LOGGER.error("Unable to ping CR: " + crPingUrl);
                 e.printStackTrace();
             } catch (IOException e) {
                 LOGGER.error("Unable to ping CR: " + crPingUrl);
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getWorkingCopies(String userName) throws ServiceException {
         try {
             return vocabularyFolderDAO.getWorkingCopies(userName);
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary folder working copies: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<SimpleAttribute> getVocabularyFolderAttributesMetadata() throws ServiceException {
         try {
             return attributeDAO.getAttributesMetadata(DElemAttribute.typeWeights.get("VCF"));
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary folder attribute metadata: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getReleasedVocabularyFolders(int folderId) throws ServiceException {
         try {
             return vocabularyFolderDAO.getReleasedVocabularyFolders(folderId);
         } catch (Exception e) {
             throw new ServiceException("Failed to get released vocabulary folders: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Folder getFolderByIdentifier(String folderIdentifier) throws ServiceException {
         try {
             return folderDAO.getFolderByIdentifier(folderIdentifier);
         } catch (Exception e) {
             throw new ServiceException("Failed to get folder by identifier: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void addDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException {
         try {
             dataElementDAO.addDataElement(vocabularyFolderId, dataElementId);
         } catch (Exception e) {
             throw new ServiceException("Failed to add data element: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void removeDataElement(int vocabularyFolderId, int dataElementId) throws ServiceException {
         try {
             dataElementDAO.removeDataElement(vocabularyFolderId, dataElementId);
         } catch (Exception e) {
             throw new ServiceException("Failed to remove data element: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<DataElement> getVocabularyDataElements(int vocabularyFolderId) throws ServiceException {
         try {
             return dataElementDAO.getVocabularyDataElements(vocabularyFolderId);
         } catch (Exception e) {
             throw new ServiceException("Failed to get data elements: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean vocabularyHasDataElementBinding(int vocabularyFolderId, int dataElementId) throws ServiceException {
         try {
             return dataElementDAO.vocabularyHasElemendBinding(vocabularyFolderId, dataElementId);
         } catch (Exception e) {
             throw new ServiceException("Failed to perform element binding existence check: " + e.getMessage(), e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyConcept> getConceptsWithElementValue(int dataElementId, int vocabularyId) throws ServiceException {
         try {
             return vocabularyConceptDAO.getConceptsWithValuedElement(dataElementId, vocabularyId);
         } catch (Exception e) {
             throw new ServiceException("Failed to perform binded element values existence check: " + e.getMessage(), e);
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<RdfNamespace> getVocabularyNamespaces(List<VocabularyFolder> vocabularyFolders) throws ServiceException {
         List<RdfNamespace> nameSpaces = new ArrayList<RdfNamespace>();
         String baseUri = Props.getRequiredProperty(PropsIF.RDF_DATAELEMENTS_BASE_URI);
 
         try {
             for (VocabularyFolder vocabulary : vocabularyFolders) {
                 List<DataElement> elems = getVocabularyDataElements(vocabulary.getId());
                 for (DataElement elem : elems) {
                     RdfNamespace ns;
                     if (elem.isExternalSchema()) {
                         ns = namespaceDAO.getNamespace(elem.getNameSpacePrefix());
                         if (!nameSpaces.contains(ns)) {
                             nameSpaces.add(ns);
                         }
                     }
                 }
 
             }
 
             return nameSpaces;
 
         } catch (DAOException daoe) {
             throw new ServiceException("Failed to get vocabulary namespaces " + daoe.getMessage(), daoe);
         }
     }
 
     /**
      * all relational prefixes.
      *
      * @return collection of skos>relation prefixes
      */
     public static Collection<String> getRelationalPrefixes() {
         return relationalElements.values();
     }
 
     /**
      * Checks if given element has some special behaviour.
      *
      * @param specialElement
      *            special element
      * @return String prefix in RDF
      */
     @Override
     public String getRelationalElementPrefix(RelationalElement specialElement) {
         return relationalElements.get(specialElement);
     }
 
     @Override
     public boolean isReferenceElement(int elementId) {
 
         DataElement elem = dataElementDAO.getDataElement(elementId);
         Map<String, List<String>> elemAttributeValues =
                 attributeDAO.getAttributeValues(elem.getId(), ParentType.ELEMENT.toString());
 
         elem.setElemAttributeValues(elemAttributeValues);
 
         return elem.getDatatype().equals("reference");
     }
 
     @Override
     public List<String> getVocabularyBoundElementNames(VocabularyFolder vocabularyFolder) {
         int vocabularyFolderId = vocabularyFolder.getId();
         List<Pair<String, Integer>> elementsMeta = vocabularyFolderDAO.getVocabularyFolderBoundElementsMeta(vocabularyFolderId);
 
         List<String> result = new ArrayList<String>();
 
         //build list of Strings
         for (Pair<String, Integer> elementCount : elementsMeta) {
             for (int i = 0; i < elementCount.getRight(); i++) {
                 result.add(elementCount.getLeft());
             }
         }
 
         return result;
     }
 
     @Override
     public VocabularyResult searchVocabularies(VocabularyFilter filter) throws ServiceException {
         try {
             return vocabularyFolderDAO.searchVocabularies(filter);
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabularies: " + e.getMessage(), e);
         }
     }
 
     @Override
     //public ConceptSearchResult searchAllVocabularyConcept(VocabularyFilter filter) throws ServiceException; searchAllVocabularyConcept(VocabularyFilter filter) throws ServiceException {
     public List<VocabularyConceptData> searchAllVocabularyConcept(VocabularyConceptFilter filter) throws ServiceException {
         try {
             VocabularyConceptResult vocabularyConceptResult = vocabularyConceptDAO.searchVocabularyConcepts(filter);
             List<VocabularyConceptData> result = new ArrayList<VocabularyConceptData>();
 
             for (VocabularyConcept concept : vocabularyConceptResult.getList()) {
                 VocabularyFolder vocabulary = vocabularyFolderDAO.getVocabularyFolder(concept.getVocabularyId());
 
                 VocabularyConceptData data = new VocabularyConceptData();
                     data.setIdentifier(concept.getIdentifier());
                     data.setLabel(concept.getLabel());
                     data.setUserName(vocabulary.getWorkingUser());
                     data.setVocabularyIdentifier(vocabulary.getIdentifier());
                     data.setVocabularyLabel(vocabulary.getLabel());
                     data.setVocabularySetIdentifier(vocabulary.getFolderName());
                     data.setVocabularySetLabel(vocabulary.getFolderLabel());
 
                     data.setVocabularyStatus(vocabulary.getRegStatus());
                     data.setWorkingCopy(vocabulary.isWorkingCopy());
 
                     result.add(data);
                 }
 
             return result;
         } catch (Exception e) {
             throw new ServiceException("Failed to perform concept search: " + e.getMessage(), e);
         }
     }
 
     @Override
     public void bindVocabulary(int elementId, int vocabularyId) {
         dataElementDAO.bindVocabulary(elementId, vocabularyId);
     }
 
 
 }
