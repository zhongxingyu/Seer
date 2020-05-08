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
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.StopWatch;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import eionet.meta.DElemAttribute;
 import eionet.meta.dao.IAttributeDAO;
 import eionet.meta.dao.ISiteCodeDAO;
 import eionet.meta.dao.IVocabularyConceptDAO;
 import eionet.meta.dao.IVocabularyFolderDAO;
 import eionet.meta.dao.domain.SimpleAttribute;
 import eionet.meta.dao.domain.SiteCodeStatus;
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyConceptAttribute;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.service.data.ObsoleteStatus;
 import eionet.meta.service.data.VocabularyConceptFilter;
 import eionet.meta.service.data.VocabularyConceptResult;
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
     public int createVocabularyFolder(VocabularyFolder vocabularyFolder, String userName) throws ServiceException {
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
     public int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) throws ServiceException {
         try {
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
             vocabularyConceptDAO.updateVocabularyConcept(vocabularyConcept);
 
             // Update vocabulary concept attributes.
             List<VocabularyConceptAttribute> toInsert = new ArrayList<VocabularyConceptAttribute>();
             List<VocabularyConceptAttribute> toUpdate = new ArrayList<VocabularyConceptAttribute>();
             List<Integer> excludedIds = new ArrayList<Integer>();
 
             if (vocabularyConcept.getAttributes() != null) {
                 for (List<VocabularyConceptAttribute> attributes : vocabularyConcept.getAttributes()) {
                     if (attributes != null) {
                         for (VocabularyConceptAttribute attr : attributes) {
                             if (attr != null) {
                                 if (attr.getId() != 0) {
                                     if (StringUtils.isNotEmpty(attr.getValue()) || attr.getRelatedId() != null) {
                                         excludedIds.add(attr.getId());
                                         toUpdate.add(attr);
                                     }
                                 } else {
                                     if (StringUtils.isNotEmpty(attr.getValue()) || attr.getRelatedId() != null) {
                                         attr.setVocabularyConceptId(vocabularyConcept.getId());
                                         toInsert.add(attr);
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
 
             List<VocabularyConceptAttribute> deletedAttributes =
                     attributeDAO.getDeletedConceptAttributes(excludedIds, vocabularyConcept.getId());
 
             attributeDAO.updateVocabularyConceptAttributes(toUpdate);
             attributeDAO.deleteVocabularyConceptAttributes(excludedIds, vocabularyConcept.getId());
             attributeDAO.createVocabularyConceptAttributes(toInsert);
 
             fixRelatedConcepts(vocabularyConcept.getId(), deletedAttributes);
 
         } catch (Exception e) {
             throw new ServiceException("Failed to update vocabulary concept: " + e.getMessage(), e);
         }
 
     }
 
     /**
      * As a last step when updating vocabulary concept, this method checks all the related attributes and makes sure that the
      * concepts are related in both sides (A related with B -> B related with A). Also when relation gets deleted from one side,
      * then we make sure to deleted it also from the other side of the relation.
      *
      * @param vocabularyConceptId
      * @param deletedAttributes
      */
     private void fixRelatedConcepts(int vocabularyConceptId, List<VocabularyConceptAttribute> deletedAttributes) {
         // Delete redundant related attributes
         for (VocabularyConceptAttribute attribute : deletedAttributes) {
             if (VocabularyConceptAttribute.BROADER_LOCAL_CONCEPT.equals(attribute.getIdentifier())) {
                 attributeDAO.checkAndDeleteConceptAttribute(attribute.getRelatedId(), attribute.getVocabularyConceptId(),
                         VocabularyConceptAttribute.NARROWER_LOCAL_CONCEPT);
             }
             if (VocabularyConceptAttribute.NARROWER_LOCAL_CONCEPT.equals(attribute.getIdentifier())) {
                 attributeDAO.checkAndDeleteConceptAttribute(attribute.getRelatedId(), attribute.getVocabularyConceptId(),
                         VocabularyConceptAttribute.BROADER_LOCAL_CONCEPT);
             }
             if (VocabularyConceptAttribute.RELATED_LOCAL_CONCEPT.equals(attribute.getIdentifier())) {
                 attributeDAO.checkAndDeleteConceptAttribute(attribute.getRelatedId(), attribute.getVocabularyConceptId(),
                         VocabularyConceptAttribute.RELATED_LOCAL_CONCEPT);
             }
         }
 
         // Add missing related attributes
         List<List<VocabularyConceptAttribute>> attributes =
                 attributeDAO.getVocabularyConceptAttributes(vocabularyConceptId, false);
 
         for (List<VocabularyConceptAttribute> attribute : attributes) {
             if (VocabularyConceptAttribute.BROADER_LOCAL_CONCEPT.equals(attribute.get(0).getIdentifier())) {
                 for (VocabularyConceptAttribute attr : attribute) {
                     attributeDAO.checkAndAddConceptAttribute(attr.getRelatedId(), attr.getVocabularyConceptId(),
                             VocabularyConceptAttribute.NARROWER_LOCAL_CONCEPT);
                 }
             }
             if (VocabularyConceptAttribute.NARROWER_LOCAL_CONCEPT.equals(attribute.get(0).getIdentifier())) {
                 for (VocabularyConceptAttribute attr : attribute) {
                     attributeDAO.checkAndAddConceptAttribute(attr.getRelatedId(), attr.getVocabularyConceptId(),
                             VocabularyConceptAttribute.BROADER_LOCAL_CONCEPT);
                 }
             }
             if (VocabularyConceptAttribute.RELATED_LOCAL_CONCEPT.equals(attribute.get(0).getIdentifier())) {
                 for (VocabularyConceptAttribute attr : attribute) {
                     attributeDAO.checkAndAddConceptAttribute(attr.getRelatedId(), attr.getVocabularyConceptId(),
                             VocabularyConceptAttribute.RELATED_LOCAL_CONCEPT);
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void quickUpdateVocabularyConcept(VocabularyConcept vocabularyConcept) throws ServiceException {
         try {
             vocabularyConceptDAO.updateVocabularyConcept(vocabularyConcept);
         } catch (Exception e) {
             throw new ServiceException("Failed to update vocabulary concept: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updateVocabularyFolder(VocabularyFolder vocabularyFolder) throws ServiceException {
         try {
             VocabularyFolder vf = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolder.getId());
             vf.setIdentifier(vocabularyFolder.getIdentifier());
             vf.setLabel(vocabularyFolder.getLabel());
             vf.setRegStatus(vocabularyFolder.getRegStatus());
             vf.setNumericConceptIdentifiers(vocabularyFolder.isNumericConceptIdentifiers());
             vf.setBaseUri(vocabularyFolder.getBaseUri());
             vf.setFolderName(vocabularyFolder.getFolderName());
             vocabularyFolderDAO.updateVocabularyFolder(vf);
 
             attributeDAO.updateSimpleAttributes(vocabularyFolder.getId(), DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(),
                     vocabularyFolder.getAttributes());
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
             attributeDAO.copySimpleAttributes(vocabularyFolderId, DElemAttribute.ParentType.VOCABULARY_FOLDER.toString(), newVocabularyFolderId);
 
             // Copy the vocabulary concepts under new vocabulary folder (except of site code type)
             if (!vocabularyFolder.isSiteCodeType()) {
                 vocabularyConceptDAO.copyVocabularyConcepts(vocabularyFolderId, newVocabularyFolderId);
                 vocabularyConceptDAO.copyVocabularyConceptsAttributes(newVocabularyFolderId);
                 vocabularyConceptDAO.updateRelatedConceptIds(newVocabularyFolderId);
             }
 
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
 
             // Remove old vocabulary concepts
             if (!vocabularyFolder.isSiteCodeType()) {
                 vocabularyConceptDAO.deleteVocabularyConcepts(originalVocabularyFolderId);
             }
 
             // Update original vocabulary folder
             vocabularyFolder.setCheckedOutCopyId(0);
             vocabularyFolder.setId(originalVocabularyFolderId);
             vocabularyFolder.setUserModified(userName);
             vocabularyFolder.setWorkingCopy(false);
             vocabularyFolder.setWorkingUser(null);
             vocabularyFolderDAO.updateVocabularyFolder(vocabularyFolder);
 
             // Move new vocabulary concepts to folder
             if (!vocabularyFolder.isSiteCodeType()) {
                 vocabularyConceptDAO.moveVocabularyConcepts(vocabularyFolderId, originalVocabularyFolderId);
             }
 
             // Delete old attributes first and then change the parent ID of the new ones
             attributeDAO.deleteAttributes(Collections.singletonList(originalVocabularyFolderId), DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());
             attributeDAO.replaceParentId(vocabularyFolderId, originalVocabularyFolderId, DElemAttribute.ParentType.VOCABULARY_FOLDER);
 
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
     public int createVocabularyFolderCopy(VocabularyFolder vocabularyFolder, int vocabularyFolderId, String userName)
             throws ServiceException {
         try {
             VocabularyFolder originalVocabularyFolder = vocabularyFolderDAO.getVocabularyFolder(vocabularyFolderId);
 
             if (originalVocabularyFolder.isSiteCodeType()) {
                 throw new IllegalArgumentException("Cannot make copy of vocabulary with type 'SITE_CODE'");
             }
 
             vocabularyFolder.setContinuityId(originalVocabularyFolder.getContinuityId());
             vocabularyFolder.setUserModified(userName);
             int newVocabularyFolderId = vocabularyFolderDAO.createVocabularyFolder(vocabularyFolder);
 
             List<VocabularyConcept> concepts = vocabularyConceptDAO.getVocabularyConcepts(vocabularyFolderId);
             for (VocabularyConcept vc : concepts) {
                 vocabularyConceptDAO.createVocabularyConcept(newVocabularyFolderId, vc);
             }
 
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
             attributeDAO.deleteAttributes(Collections.singletonList(vocabularyFolderId), DElemAttribute.ParentType.VOCABULARY_FOLDER.toString());
 
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
     public boolean isUniqueFolderIdentifier(String folderName, String identifier, int... excludedVocabularyFolderIds)
             throws ServiceException {
         try {
             return vocabularyFolderDAO.isUniqueFolderIdentifier(folderName, identifier, excludedVocabularyFolderIds);
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
             List<List<VocabularyConceptAttribute>> attributes =
                     attributeDAO.getVocabularyConceptAttributes(result.getId(), emptyAttributes);
 
             result.setAttributes(attributes);
 
             return result;
         } catch (Exception e) {
             throw new ServiceException("Failed to get vocabulary concept: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyConcept getVocabularyConcept(int vocabularyConceptId, boolean emptyAttributes) throws ServiceException {
         try {
             VocabularyConcept result = vocabularyConceptDAO.getVocabularyConcept(vocabularyConceptId);
             List<List<VocabularyConceptAttribute>> attributes =
                     attributeDAO.getVocabularyConceptAttributes(result.getId(), emptyAttributes);
 
             result.setAttributes(attributes);
 
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
                 List<List<VocabularyConceptAttribute>> attributes = attributeDAO.getVocabularyConceptAttributes(vc.getId(), false);
                 vc.setAttributes(attributes);
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
 }
