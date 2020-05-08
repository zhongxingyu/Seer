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
 
 package eionet.meta.dao.mysql;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import eionet.meta.dao.IVocabularyConceptDAO;
 import eionet.meta.dao.domain.VocabularyConcept;
 
 /**
  * Vocabulary concept DAO.
  *
  * @author Juhan Voolaid
  */
 @Repository
 public class VocabularyConceptDAOImpl extends GeneralDAOImpl implements IVocabularyConceptDAO {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyConcept> getVocabularyConcepts(int vocabularyFolderId) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("vocabularyFolderId", vocabularyFolderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select VOCABULARY_CONCEPT_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION ");
        sql.append("from T_VOCABULARY_CONCEPT where VOCABULARY_FOLDER_ID=:vocabularyFolderId order by IDENTIFIER + 0");
 
         List<VocabularyConcept> resultList =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyConcept>() {
                     public VocabularyConcept mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyConcept vc = new VocabularyConcept();
                         vc.setId(rs.getInt("VOCABULARY_CONCEPT_ID"));
                         vc.setIdentifier(rs.getString("IDENTIFIER"));
                         vc.setLabel(rs.getString("LABEL"));
                         vc.setDefinition(rs.getString("DEFINITION"));
                         vc.setNotation(rs.getString("NOTATION"));
                         return vc;
                     }
                 });
 
         return resultList;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int createVocabularyConcept(int vocabularyFolderId, VocabularyConcept vocabularyConcept) {
         StringBuilder sql = new StringBuilder();
         sql.append("insert into T_VOCABULARY_CONCEPT (VOCABULARY_FOLDER_ID, IDENTIFIER, LABEL, DEFINITION, NOTATION) ");
         sql.append("values (:vocabularyFolderId, :identifier, :label, :definition, :notation)");
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("vocabularyFolderId", vocabularyFolderId);
         parameters.put("identifier", vocabularyConcept.getIdentifier());
         parameters.put("label", vocabularyConcept.getLabel());
         parameters.put("definition", vocabularyConcept.getDefinition());
         parameters.put("notation", vocabularyConcept.getNotation().trim());
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
         return getLastInsertId();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updateVocabularyConcept(VocabularyConcept vocabularyConcept) {
 
         StringBuilder sql = new StringBuilder();
         sql.append("update T_VOCABULARY_CONCEPT set IDENTIFIER = :identifier, LABEL = :label, DEFINITION = :definition, NOTATION = :notation ");
         sql.append("where VOCABULARY_CONCEPT_ID = :vocabularyConceptId");
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("vocabularyConceptId", vocabularyConcept.getId());
         parameters.put("identifier", vocabularyConcept.getIdentifier());
         parameters.put("label", vocabularyConcept.getLabel());
         parameters.put("definition", vocabularyConcept.getDefinition());
         parameters.put("notation", vocabularyConcept.getNotation().trim());
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyConcepts(List<Integer> ids) {
         String sql = "delete from T_VOCABULARY_CONCEPT where VOCABULARY_CONCEPT_ID in (:ids)";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("ids", ids);
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyConcepts(int vocabularyFolderId) {
         String sql = "delete from T_VOCABULARY_CONCEPT where VOCABULARY_FOLDER_ID = :vocabularyFolderId";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("vocabularyFolderId", vocabularyFolderId);
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void moveVocabularyConcepts(int fromVocabularyFolderId, int toVocabularyFolderId) {
         StringBuilder sql = new StringBuilder();
         sql.append("update T_VOCABULARY_CONCEPT set VOCABULARY_FOLDER_ID = :toVocabularyFolderId ");
         sql.append("where VOCABULARY_FOLDER_ID = :fromVocabularyFolderId");
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("fromVocabularyFolderId", fromVocabularyFolderId);
         parameters.put("toVocabularyFolderId", toVocabularyFolderId);
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isUniqueConceptIdentifier(String identifier, int vocabularyFolderId, int vocabularyConceptId) {
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("identifier", identifier);
         parameters.put("vocabularyFolderId", vocabularyFolderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select count(VOCABULARY_CONCEPT_ID) from T_VOCABULARY_CONCEPT ");
         sql.append("where IDENTIFIER = :identifier and VOCABULARY_FOLDER_ID = :vocabularyFolderId ");
         if (vocabularyConceptId != 0) {
             sql.append("and VOCABULARY_CONCEPT_ID != :vocabularyConceptId");
             parameters.put("vocabularyConceptId", vocabularyConceptId);
         }
 
         int result = getNamedParameterJdbcTemplate().queryForInt(sql.toString(), parameters);
 
         return result == 0;
     }
 
 }
