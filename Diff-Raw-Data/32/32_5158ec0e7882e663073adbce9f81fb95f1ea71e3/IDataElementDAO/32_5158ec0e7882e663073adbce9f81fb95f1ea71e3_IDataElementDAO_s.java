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
 
 package eionet.meta.dao;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 import eionet.meta.dao.domain.Attribute;
 import eionet.meta.dao.domain.DataElement;
 import eionet.meta.dao.domain.FixedValue;
 import eionet.meta.service.data.DataElementsFilter;
 import eionet.meta.service.data.DataElementsResult;
 
 /**
  * Data element DAO.
  *
  * @author Juhan Voolaid
  */
 public interface IDataElementDAO {
 
     /**
      * Search data elements.
      *
      * @param filter
      * @return
      */
     DataElementsResult searchDataElements(DataElementsFilter filter);
 
     /**
      * Returns data element attributes.
      *
      * @return
      */
     List<Attribute> getDataElementAttributes() throws SQLException;
 
     /**
      * Returns data element's fixed values.
      *
      * @return
      */
     List<FixedValue> getFixedValues(int dataElementId);
 
     /**
      * Returns data element by id.
      *
      * @param id
      * @return
      */
     DataElement getDataElement(int id);
 
     /**
      * Returns latest version of the data element.
      *
      * @param identifier data element identifier
      * @return element
      */
     DataElement getDataElement(String identifier);
 
     /**
      * Returns latest version of the COMMON data element id.
      *
      * @param identifier common element identifier
      * @return ID in DATAELEM table
      */
     int getCommonDataElementId(String identifier);
 
     /**
      *
      * @param dataElementId
      * @return
      */
     String getDataElementDataType(int dataElementId);
 
     /**
      * Adds data element to vocabulary folder.
      *
      * @param vocabularyFolderId
      * @param dataElementId
      */
     void addDataElement(int vocabularyFolderId, int dataElementId);
 
     /**
      * Removes data element from vocabulary folder.
      *
      * @param vocabularyFolderId
      * @param dataElementId
      */
     void removeDataElement(int vocabularyFolderId, int dataElementId);
 
     /**
      * Returns data elements bound with vocabulary folder.
      *
      * @param vocabularyFolderId folder ID
      * @return list of data elements for the Vocabulary
      */
     List<DataElement> getVocabularyDataElements(int vocabularyFolderId);
 
     /**
      * Deletes all vocabulary's data element relations.
      *
      * @param vocabularyFolderId
      */
     void deleteVocabularyDataElements(int vocabularyFolderId);
 
     /**
      * Deletes all vocabulary concept's data element values.
      *
      * @param vocabularyConceptId
      */
     void deleteVocabularyConceptDataElementValues(int vocabularyConceptId);
 
     /**
      * Inserts data element values.
      *
      * @param vocabularyConceptId
      * @param dataElementValues
      */
     void insertVocabularyConceptDataElementValues(int vocabularyConceptId, List<DataElement> dataElementValues);
 
     /**
      * Moves all vocabulary's data element relations to other vocabulary.
      *
      * @param sourceVocabularyFolderId
      * @param targetVocabularyFolderId
      */
     void moveVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId);
 
     /**
      * Copy all vocabulary's data element relations to other vocabulary.
      *
      * @param sourceVocabularyFolderId
      * @param targetVocabularyFolderId
      */
     void copyVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId);
 
     /**
      * Returns data element attributes for vocabulary concept.
      *
      * @param vocabularyFolderId vocabularyID
      * @param vocabularyConceptId concept ID
      * @param emptyAttributes
      *            when true, then attributes that are not valued are also included
      * @return list of lists where each list contains element values of one bound element
      */
     List<List<DataElement>> getVocabularyConceptDataElementValues(int vocabularyFolderId, int vocabularyConceptId,
             boolean emptyAttributes);
 
     /**
      * Copies data element values from old concepts to new concepts.
      * Can be used when checking out the vocabulary
      *
      * @param newVocabularyFolderId new vocabulary Folder ID
      */
     void checkoutVocabularyConceptDataElementValues(int newVocabularyFolderId);
 
     /**
      * Copies data element values from old vocabulary concepts to new vocabulary concepts.
      * @param oldVocabularyFolderId old vocabulary Folder ID
      * @param newVocabularyFolderId new vocabulary Folder ID
      */
     void copyVocabularyConceptDataElementValues(int oldVocabularyFolderId, int newVocabularyFolderId);
 
     /**
      * Checks if the vocabulary has binding of this element.
      * @param vocabularyFolderId vocabulary Id
      * @param elementId element id
      * @return true if binding exists
      */
     boolean vocabularyHasElemendBinding(int vocabularyFolderId, int elementId);
 
     /**
      * Updates the VOCABULARY_CONCEPT_ELEMENT.RELATED_CONCEPT_ID to what the new checked out concept's id currently is.
      *
      * @param newVocabularyFolderId
      */
     //void updateRelatedConceptIds(int newVocabularyFolderId);
 
     /**
      * Deletes related concept elements of this concept.
      * @param vocabularyConceptId  concept Id
      */
     void deleteRelatedElements(int vocabularyConceptId);
 
     /**
      * Finds element attribute values for the data element.
      *
      * @param elementId
      *            element ID
      * @return Map where key is attribute name and value is list of element values
      */
     Map<String, List<String>> getDataElementAttributeValues(int elementId);
 
 
     /**
      * Finds unique set of elements used in all dataset tables.
      * @param datasetId dataset id
      * @return distinct collection of data elements
      */
     List<DataElement> getDataSetElements(int datasetId);
 
     /**
      * Sets relation to an external vocabulary.
      * @param elementId data element id
      * @param vocabularyId vocabulary Id
      */
     void bindVocabulary(int elementId, int vocabularyId);
 
     /**
      * Finds list of elements where given vocabularies is used as source for values.
      * @param vocabularyIds vocabulary ids
      * @return collection of data elements
      */
     List<DataElement> getVocabularySourceElements(List<Integer>vocabularyIds);
 
 
 }
