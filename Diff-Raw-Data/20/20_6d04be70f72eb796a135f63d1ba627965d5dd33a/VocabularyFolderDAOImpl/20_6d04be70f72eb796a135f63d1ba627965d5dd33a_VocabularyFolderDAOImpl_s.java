 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either exprevf or
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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import eionet.meta.dao.IVocabularyFolderDAO;
 import eionet.meta.dao.domain.RegStatus;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.dao.domain.VocabularyType;
 
 /**
  * Vocabualary folder DAO.
  *
  * @author Juhan Voolaid
  */
 @Repository
 public class VocabularyFolderDAOImpl extends GeneralDAOImpl implements IVocabularyFolderDAO {
 
 
     @Override
     public List<VocabularyFolder> getReleasedVocabularyFolders(int folderId) {
         List<String> statuses = new ArrayList<String>();
         statuses.add(RegStatus.RELEASED.toString());
         statuses.add(RegStatus.PUBLIC_DRAFT.toString());
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("folderId", folderId);
         params.put("statuses", statuses);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
        sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where v.WORKING_COPY=FALSE and v.FOLDER_ID=:folderId and v.REG_STATUS in (:statuses) ");
         sql.append("order by f.IDENTIFIER, v.IDENTIFIER ");
 
         List<VocabularyFolder> items =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return items;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getVocabularyFolders(String userName) {
         Map<String, Object> params = new HashMap<String, Object>();
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where ");
         if (StringUtils.isBlank(userName)) {
             sql.append("v.WORKING_COPY=FALSE ");
         } else {
             sql.append("(v.WORKING_COPY=FALSE or v.WORKING_USER=:workingUser) ");
             params.put("workingUser", userName);
         }
         sql.append("order by f.IDENTIFIER, v.IDENTIFIER ");
 
         List<VocabularyFolder> items =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return items;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getVocabularyFolders(int folderId, String userName) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("folderId", folderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where ");
         if (StringUtils.isBlank(userName)) {
             sql.append("v.WORKING_COPY=FALSE ");
         } else {
             sql.append("(v.WORKING_COPY=FALSE or v.WORKING_USER=:workingUser) ");
             params.put("workingUser", userName);
         }
         sql.append("and v.FOLDER_ID=:folderId ");
         sql.append("order by f.IDENTIFIER, v.IDENTIFIER ");
 
         List<VocabularyFolder> items =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return items;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getVocabularyFolderVersions(String continuityId, int vocabularyFolderId, String userName) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("continuityId", continuityId);
         params.put("vocabularyFolderId", vocabularyFolderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where v.CONTINUITY_ID = :continuityId and v.VOCABULARY_FOLDER_ID != :vocabularyFolderId ");
         if (StringUtils.isBlank(userName)) {
             sql.append("and v.WORKING_COPY=FALSE ");
         } else {
             sql.append("and (v.WORKING_COPY=FALSE or v.WORKING_USER=:workingUser) ");
             params.put("workingUser", userName);
         }
 
         List<VocabularyFolder> items =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return items;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<VocabularyFolder> getWorkingCopies(String userName) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("workingUser", userName);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where v.WORKING_USER=:workingUser and v.WORKING_COPY = 1");
 
         List<VocabularyFolder> items =
                 getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderId(rs.getInt("f.ID"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return items;
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int createVocabularyFolder(VocabularyFolder vocabularyFolder) {
 
         String sql = "insert into T_VOCABULARY_FOLDER (IDENTIFIER, LABEL, REG_STATUS, CONTINUITY_ID, VOCABULARY_TYPE," +
         		" WORKING_COPY, WORKING_USER, DATE_MODIFIED, USER_MODIFIED, CHECKEDOUT_COPY_ID, CONCEPT_IDENTIFIER_NUMERIC," +
         		" BASE_URI, FOLDER_ID, NOTATIONS_EQUAL_IDENTIFIERS)" +
         		" values (:identifier,  :label, :regStatus, :continuityId, :vocabularyType, :workingCopy, :workingUser, now()," +
         		" :userModified, :checkedOutCopyId, :numericConceptIdentifiers, :baseUri, :folderId, :enforceNotationToId)";
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("identifier", vocabularyFolder.getIdentifier());
         parameters.put("label", vocabularyFolder.getLabel());
         parameters.put("regStatus", vocabularyFolder.getRegStatus().toString());
         parameters.put("continuityId", vocabularyFolder.getContinuityId());
         parameters.put("vocabularyType", vocabularyFolder.getType().name());
         parameters.put("workingCopy", vocabularyFolder.isWorkingCopy());
         parameters.put("workingUser", vocabularyFolder.getWorkingUser());
         parameters.put("userModified", vocabularyFolder.getUserModified());
         parameters.put("checkedOutCopyId",
                 vocabularyFolder.getCheckedOutCopyId() <= 0 ? null : vocabularyFolder.getCheckedOutCopyId());
         parameters.put("numericConceptIdentifiers", vocabularyFolder.isNumericConceptIdentifiers());
         parameters.put("baseUri", vocabularyFolder.getBaseUri());
         parameters.put("folderId", vocabularyFolder.getFolderId());
         parameters.put("enforceNotationToId", vocabularyFolder.isNotationsEqualIdentifiers());
 
         getNamedParameterJdbcTemplate().update(sql, parameters);
         return getLastInsertId();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyFolder(String folderName, String identifier, boolean workingCopy) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("identifier", identifier);
         params.put("workingCopy", workingCopy);
         params.put("folderIdentifier", folderName);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where v.IDENTIFIER=:identifier and v.WORKING_COPY=:workingCopy and f.IDENTIFIER=:folderIdentifier");
 
         VocabularyFolder result =
                 getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updateVocabularyFolder(VocabularyFolder vocabularyFolder) {
         String sql = "update T_VOCABULARY_FOLDER set" +
         		" IDENTIFIER = :identifier,  LABEL = :label, REG_STATUS = :regStatus, CHECKEDOUT_COPY_ID = :checkedOutCopyId," +
                 " WORKING_COPY = :workingCopy, WORKING_USER = :workingUser, DATE_MODIFIED = :dateModified," +
                 " USER_MODIFIED = :userModified, CONCEPT_IDENTIFIER_NUMERIC = :numericConceptIdentifiers, BASE_URI = :baseUri," +
                 " FOLDER_ID = :folderId, NOTATIONS_EQUAL_IDENTIFIERS=:enforceNotationId" +
                 " where VOCABULARY_FOLDER_ID = :vocabularyFolderId";
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("vocabularyFolderId", vocabularyFolder.getId());
         parameters.put("identifier", vocabularyFolder.getIdentifier());
         parameters.put("label", vocabularyFolder.getLabel());
         parameters.put("regStatus", vocabularyFolder.getRegStatus().toString());
         parameters.put("workingCopy", vocabularyFolder.isWorkingCopy());
         parameters.put("workingUser", vocabularyFolder.getWorkingUser());
         parameters.put("userModified", vocabularyFolder.getUserModified());
         parameters.put("checkedOutCopyId",
                 vocabularyFolder.getCheckedOutCopyId() <= 0 ? null : vocabularyFolder.getCheckedOutCopyId());
         parameters.put("numericConceptIdentifiers", vocabularyFolder.isNumericConceptIdentifiers());
         parameters.put("baseUri", vocabularyFolder.getBaseUri());
         parameters.put("folderId", vocabularyFolder.getFolderId());
         parameters.put("dateModified", vocabularyFolder.getDateModified());
         parameters.put("enforceNotationId", vocabularyFolder.isNotationsEqualIdentifiers());
 
         getNamedParameterJdbcTemplate().update(sql, parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyFolders(List<Integer> ids) {
         String sql = "delete from T_VOCABULARY_FOLDER where VOCABULARY_FOLDER_ID in (:ids)";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("ids", ids);
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyFolder(int vocabularyFolderId) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("id", vocabularyFolderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where VOCABULARY_FOLDER_ID=:id");
 
         VocabularyFolder result =
                 getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public VocabularyFolder getVocabularyWorkingCopy(int checkedOutCopyId) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("checkedOutCopyId", checkedOutCopyId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where v.CHECKEDOUT_COPY_ID=:checkedOutCopyId");
 
         VocabularyFolder result =
                 getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isUniqueVocabularyFolderIdentifier(int folderId, String identifier, int... excludedVocabularyFolderIds) {
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("identifier", identifier);
         parameters.put("folderId", folderId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select count(VOCABULARY_FOLDER_ID) from T_VOCABULARY_FOLDER ");
         sql.append("where IDENTIFIER = :identifier and FOLDER_ID = :folderId ");
         if (excludedVocabularyFolderIds != null && excludedVocabularyFolderIds.length > 0) {
             sql.append("and VOCABULARY_FOLDER_ID not in (:excludedVocabularyFolderIds)");
 
             List<Integer> excluded = new ArrayList<Integer>();
             for (int i : excludedVocabularyFolderIds) {
                 excluded.add(i);
             }
             parameters.put("excludedVocabularyFolderIds", excluded);
         }
 
         int result = getNamedParameterJdbcTemplate().queryForInt(sql.toString(), parameters);
 
         return result == 0;
     }
 
     /*
      * (non-Javadoc)
      * @see eionet.meta.dao.IVocabularyFolderDAO#forceNotationsToIdentifiers(int)
      */
     @Override
     public int forceNotationsToIdentifiers(int vocabularyId) {
 
         String sql = "update T_VOCABULARY_CONCEPT set NOTATION=IDENTIFIER where VOCABULARY_FOLDER_ID=:vocId";
         return getNamedParameterJdbcTemplate().update(sql, Collections.singletonMap("vocId", vocabularyId));
     }
 
     /*
      * (non-Javadoc)
      * @see eionet.meta.dao.IVocabularyFolderDAO#getVocabularyFolderOfConcept(int)
      */
     @Override
     public VocabularyFolder getVocabularyFolderOfConcept(int conceptId) {
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("conceptId", conceptId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select v.VOCABULARY_FOLDER_ID, v.IDENTIFIER, v.LABEL, v.REG_STATUS, v.WORKING_COPY, v.BASE_URI, v.VOCABULARY_TYPE, ");
         sql.append("v.WORKING_USER, v.DATE_MODIFIED, v.USER_MODIFIED, v.CHECKEDOUT_COPY_ID, v.CONTINUITY_ID, v.CONCEPT_IDENTIFIER_NUMERIC, ");
         sql.append("f.ID, f.IDENTIFIER, f.LABEL, v.NOTATIONS_EQUAL_IDENTIFIERS ");
         sql.append("from T_VOCABULARY_FOLDER v ");
         sql.append("left join T_FOLDER f on f.ID=v.FOLDER_ID ");
         sql.append("where VOCABULARY_FOLDER_ID=");
         sql.append("(select VOCABULARY_FOLDER_ID from T_VOCABULARY_CONCEPT where VOCABULARY_CONCEPT_ID=:conceptId)");
 
         VocabularyFolder result =
                 getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, new RowMapper<VocabularyFolder>() {
                     @Override
                     public VocabularyFolder mapRow(ResultSet rs, int rowNum) throws SQLException {
                         VocabularyFolder vf = new VocabularyFolder();
                         vf.setId(rs.getInt("v.VOCABULARY_FOLDER_ID"));
                         vf.setIdentifier(rs.getString("v.IDENTIFIER"));
                         vf.setLabel(rs.getString("v.LABEL"));
                         vf.setRegStatus(RegStatus.fromString(rs.getString("v.REG_STATUS")));
                         vf.setType(VocabularyType.valueOf(rs.getString("v.VOCABULARY_TYPE")));
                         vf.setWorkingCopy(rs.getBoolean("v.WORKING_COPY"));
                         vf.setWorkingUser(rs.getString("v.WORKING_USER"));
                         vf.setDateModified(rs.getTimestamp("v.DATE_MODIFIED"));
                         vf.setUserModified(rs.getString("v.USER_MODIFIED"));
                         vf.setCheckedOutCopyId(rs.getInt("v.CHECKEDOUT_COPY_ID"));
                         vf.setContinuityId(rs.getString("v.CONTINUITY_ID"));
                         vf.setNumericConceptIdentifiers(rs.getBoolean("v.CONCEPT_IDENTIFIER_NUMERIC"));
                         vf.setNotationsEqualIdentifiers(rs.getBoolean("NOTATIONS_EQUAL_IDENTIFIERS"));
                         vf.setBaseUri(rs.getString("v.BASE_URI"));
                         vf.setFolderId(rs.getShort("f.ID"));
                         vf.setFolderName(rs.getString("f.IDENTIFIER"));
                         vf.setFolderLabel(rs.getString("f.LABEL"));
                         return vf;
                     }
                 });
 
         return result;    }
 }
