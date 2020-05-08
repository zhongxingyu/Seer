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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowCallbackHandler;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.DElemAttribute;
 import eionet.meta.dao.IDataElementDAO;
 import eionet.meta.dao.domain.Attribute;
 import eionet.meta.dao.domain.DataElement;
 import eionet.meta.dao.domain.FixedValue;
 import eionet.meta.dao.domain.RegStatus;
 import eionet.meta.service.data.DataElementsFilter;
 import eionet.meta.service.data.DataElementsResult;
 
 /**
  * Data element DAO.
  *
  * @author Juhan Voolaid
  */
 @Repository
 public class DataElementDAOImpl extends GeneralDAOImpl implements IDataElementDAO {
 
     /** Logger. */
     protected static final Logger LOGGER = Logger.getLogger(DataElementDAOImpl.class);
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DataElementsResult searchDataElements(DataElementsFilter filter) {
         boolean commonElements = filter.getElementType().equals(DataElementsFilter.COMMON_ELEMENT_TYPE);
         List<DataElement> dataElements = null;
         if (commonElements) {
             dataElements = executeCommonElementQuery(filter);
         } else {
             dataElements = executeNonCommonElementQuery(filter);
         }
 
         DataElementsResult result = new DataElementsResult();
         result.setDataElements(dataElements);
         result.setCommonElements(commonElements);
 
         return result;
     }
 
     private List<DataElement> executeCommonElementQuery(final DataElementsFilter filter) {
         Map<String, Object> params = new HashMap<String, Object>();
         StringBuilder sql = new StringBuilder();
 
         sql.append("select de.DATAELEM_ID, de.IDENTIFIER, de.SHORT_NAME, de.REG_STATUS, de.DATE, de.TYPE, de.WORKING_USER ");
         sql.append("from DATAELEM de ");
         sql.append("where ");
         sql.append("de.PARENT_NS is null ");
         sql.append("and de.WORKING_COPY = 'N' ");
 
         // Filter parameters
         if (StringUtils.isNotEmpty(filter.getRegStatus())) {
             sql.append("and de.REG_STATUS = :regStatus ");
             params.put("regStatus", filter.getRegStatus());
         }
         if (StringUtils.isNotEmpty(filter.getType())) {
             sql.append("and de.TYPE = :type ");
             params.put("type", filter.getType());
         }
         if (StringUtils.isNotEmpty(filter.getShortName())) {
             sql.append("and de.SHORT_NAME like :shortName ");
             params.put("shortName", "%" + filter.getShortName() + "%");
         }
         if (StringUtils.isNotEmpty(filter.getIdentifier())) {
             sql.append("and de.IDENTIFIER like :identifier ");
             params.put("identifier", "%" + filter.getIdentifier() + "%");
         }
         // attributes
         for (int i = 0; i < filter.getAttributes().size(); i++) {
             Attribute a = filter.getAttributes().get(i);
             String idKey = "attrId" + i;
             String valueKey = "attrValue" + i;
             if (StringUtils.isNotEmpty(a.getValue())) {
                 sql.append("and ");
                 sql.append("de.DATAELEM_ID in ( ");
                 sql.append("select a.DATAELEM_ID from ATTRIBUTE a WHERE ");
                 sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " AND a.VALUE like :" + valueKey + " AND a.PARENT_TYPE = :parentType ");
                 sql.append(") ");
             }
             params.put(idKey, a.getId());
             String value = "%" + a.getValue() + "%";
             params.put(valueKey, value);
             params.put("parentType", DElemAttribute.ParentType.ELEMENT.toString());
         }
 
         sql.append("order by de.IDENTIFIER asc, de.DATAELEM_ID desc");
 
         // LOGGER.debug("SQL: " + sql.toString());
 
         final List<DataElement> dataElements = new ArrayList<DataElement>();
 
         getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
             DataElement de;
             String curElmIdf;
 
             @Override
             public void processRow(ResultSet rs) throws SQLException {
 
                 int elmID = rs.getInt("de.DATAELEM_ID");
                 String elmIdf = rs.getString("de.IDENTIFIER");
                 // skip non-existing elements, ie trash from some erroneous situation
                 if (elmIdf == null) {
                     return;
                 }
 
                 // the following if block skips non-latest
                 if (curElmIdf != null && elmIdf.equals(curElmIdf)) {
                     if (!filter.isIncludeHistoricVersions()) {
                         return;
                     }
                 } else {
                     curElmIdf = elmIdf;
                 }
 
                 de = new DataElement();
                 de.setId(rs.getInt("de.DATAELEM_ID"));
                 de.setShortName(rs.getString("de.SHORT_NAME"));
                 de.setStatus(rs.getString("de.REG_STATUS"));
                 de.setType(rs.getString("de.TYPE"));
                 de.setModified(new Date(rs.getLong("de.DATE")));
                 de.setWorkingUser(rs.getString("de.WORKING_USER"));
                 de.setIdentifier(rs.getString("de.IDENTIFIER"));
 
                 dataElements.add(de);
             }
         });
 
         return dataElements;
     }
 
     private List<DataElement> executeNonCommonElementQuery(final DataElementsFilter filter) {
         Map<String, Object> params = new HashMap<String, Object>();
         StringBuilder sql = new StringBuilder();
 
         sql.append("select de.DATAELEM_ID, de.IDENTIFIER, de.SHORT_NAME, ds.REG_STATUS, de.DATE, de.TYPE, ");
         sql.append("t.SHORT_NAME as tableName, ds.IDENTIFIER as datasetName, ds.IDENTIFIER, ds.DATASET_ID, t.IDENTIFIER, t.TABLE_ID, de.WORKING_USER, de.WORKING_COPY ");
         sql.append("from DATAELEM de ");
         sql.append("left join TBL2ELEM t2e on (de.DATAELEM_ID = t2e.DATAELEM_ID) ");
         sql.append("left join DS_TABLE t on (t2e.TABLE_ID = t.TABLE_ID) ");
         sql.append("left join DST2TBL d2t on (t.TABLE_ID = d2t.TABLE_ID) ");
         sql.append("left join DATASET ds on (d2t.DATASET_ID = ds.DATASET_ID) ");
         sql.append("where ");
         sql.append("de.PARENT_NS is not null ");
         sql.append("and ds.DELETED is null ");
         sql.append("and ds.WORKING_COPY = 'N' ");
         // Filter parameters
         if (StringUtils.isNotEmpty(filter.getDataSet())) {
             sql.append("and ds.IDENTIFIER = :dataSet ");
             params.put("dataSet", filter.getDataSet());
         }
         if (StringUtils.isNotEmpty(filter.getType())) {
             sql.append("and de.TYPE = :type ");
             params.put("type", filter.getType());
         }
         if (StringUtils.isNotEmpty(filter.getShortName())) {
             sql.append("and de.SHORT_NAME like :shortName ");
             params.put("shortName", "%" + filter.getShortName() + "%");
         }
         if (StringUtils.isNotEmpty(filter.getIdentifier())) {
             sql.append("and de.IDENTIFIER like :identifier ");
             params.put("identifier", "%" + filter.getIdentifier() + "%");
         }
         // attributes
         for (int i = 0; i < filter.getAttributes().size(); i++) {
             Attribute a = filter.getAttributes().get(i);
             String idKey = "attrId" + i;
             String valueKey = "attrValue" + i;
             if (StringUtils.isNotEmpty(a.getValue())) {
                 sql.append("and ");
                 sql.append("de.DATAELEM_ID in ( ");
                 sql.append("select a.DATAELEM_ID from ATTRIBUTE a WHERE ");
                 sql.append("a.M_ATTRIBUTE_ID = :" + idKey + " AND a.VALUE like :" + valueKey + " AND a.PARENT_TYPE = :parentType ");
                 sql.append(") ");
             }
             params.put(idKey, a.getId());
             String value = "%" + a.getValue() + "%";
             params.put(valueKey, value);
             params.put("parentType", DElemAttribute.ParentType.ELEMENT.toString());
         }
 
         sql.append("order by ds.IDENTIFIER asc, ds.DATASET_ID desc, t.IDENTIFIER asc, t.TABLE_ID desc, de.IDENTIFIER asc, de.DATAELEM_ID desc");
 
         // LOGGER.debug("SQL: " + sql.toString());
 
         final List<DataElement> dataElements = new ArrayList<DataElement>();
 
         getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
             DataElement de;
             String curDstIdf;
             String curDstID;
 
             @Override
             public void processRow(ResultSet rs) throws SQLException {
 
                 String dstID = rs.getString("ds.DATASET_ID");
                 String dstIdf = rs.getString("ds.IDENTIFIER");
                 if (dstID == null || dstIdf == null) {
                     return;
                 }
 
                 String tblID = rs.getString("t.TABLE_ID");
                 String tblIdf = rs.getString("t.IDENTIFIER");
                 // skip non-existing tables, ie trash from some erroneous situation
                 if (tblID == null || tblIdf == null) {
                     return;
                 }
 
                 int elmID = rs.getInt("de.DATAELEM_ID");
                 String elmIdf = rs.getString("de.IDENTIFIER");
                 // skip non-existing elements, ie trash from some erroneous situation
                 if (elmIdf == null) {
                     return;
                 }
 
                 // the following if block skips elements from non-latest DATASETS
                 if (curDstIdf == null || !curDstIdf.equals(dstIdf)) {
                     curDstID = dstID;
                     curDstIdf = dstIdf;
                 } else if (!filter.isIncludeHistoricVersions()) {
                     if (!curDstID.equals(dstID)) {
                         return;
                     }
                 }
 
                 de = new DataElement();
                 de.setId(rs.getInt("de.DATAELEM_ID"));
                 de.setShortName(rs.getString("de.SHORT_NAME"));
                 de.setStatus(rs.getString("ds.REG_STATUS"));
                 de.setType(rs.getString("de.TYPE"));
                 de.setModified(new Date(rs.getLong("de.DATE")));
                 de.setTableName(rs.getString("tableName"));
                 de.setDataSetName(rs.getString("datasetName"));
                 de.setWorkingUser(rs.getString("de.WORKING_USER"));
 
                 dataElements.add(de);
             }
         });
 
         return dataElements;
     }
 
     /**
      * {@inheritDoc}
      *
      * @throws SQLException
      */
     @Override
     public List<Attribute> getDataElementAttributes() throws SQLException {
         List<Attribute> result = new ArrayList<Attribute>();
         DDSearchEngine searchEngine = new DDSearchEngine(getConnection());
 
         @SuppressWarnings("rawtypes")
         Vector attrs = searchEngine.getDElemAttributes();
 
         for (int i = 0; i < attrs.size(); i++) {
             DElemAttribute attribute = (DElemAttribute) attrs.get(i);
 
             if (attribute.displayFor("CH1") || attribute.displayFor("CH2")) {
                 Attribute a = new Attribute();
                 a.setId(Integer.parseInt(attribute.getID()));
                 a.setName(attribute.getName());
                 a.setShortName(attribute.getShortName());
 
                 result.add(a);
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<FixedValue> getFixedValues(int dataElementId) {
         StringBuffer sql = new StringBuffer();
         sql.append("select * ");
         sql.append(" from FXV ");
         sql.append(" where OWNER_ID = :ownerId ");
         sql.append(" and OWNER_TYPE=:ownerType ");
         sql.append(" order by FXV_ID");
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("ownerId", dataElementId);
         params.put("ownerType", "elem");
 
         List<FixedValue> result = getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowMapper<FixedValue>() {
             @Override
             public FixedValue mapRow(ResultSet rs, int rowNum) throws SQLException {
                 FixedValue fv = new FixedValue();
                 fv.setId(rs.getInt("FXV_ID"));
                 fv.setOwnerId(rs.getInt("OWNER_ID"));
                 fv.setOwnerType(rs.getString("OWNER_TYPE"));
                 fv.setValue(rs.getString("VALUE"));
                 fv.setDefinition(rs.getString("DEFINITION"));
                 fv.setShortDescription(rs.getString("SHORT_DESC"));
                 return fv;
             }
         });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DataElement getDataElement(int id) {
         String sql =
                 "select * from DATAELEM de where de.DATAELEM_ID = :id";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("id", id);
 
         DataElement result = getNamedParameterJdbcTemplate().queryForObject(sql, parameters, new RowMapper<DataElement>() {
             @Override
             public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                 DataElement de = new DataElement();
                 de.setId(rs.getInt("de.DATAELEM_ID"));
                 de.setIdentifier(rs.getString("de.IDENTIFIER"));
                 de.setShortName(rs.getString("de.SHORT_NAME"));
                 de.setStatus(rs.getString("de.REG_STATUS"));
                 de.setType(rs.getString("de.TYPE"));
                 de.setModified(new Date(rs.getLong("de.DATE")));
                 de.setWorkingUser(rs.getString("de.WORKING_USER"));
                 return de;
             }
         });
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DataElement getDataElement(String identifier) {
         return getDataElement(getDataElementId(identifier));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getDataElementId(String identifier) {
         String sql =
                 "select max(de.DATAELEM_ID) from DATAELEM de where de.IDENTIFIER = :identifier and de.REG_STATUS = :regStatus";
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("identifier", identifier);
         parameters.put("regStatus", RegStatus.RELEASED.toString());
 
         return getNamedParameterJdbcTemplate().queryForInt(sql, parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getDataElementDataType(int dataElementId) {
         StringBuffer sql = new StringBuffer();
         sql.append("select at.VALUE from DATAELEM de ");
         sql.append("left join ATTRIBUTE at on at.DATAELEM_ID = de.DATAELEM_ID ");
         sql.append("left join M_ATTRIBUTE ma on ma.M_ATTRIBUTE_ID = at.M_ATTRIBUTE_ID ");
         sql.append("where de.dataelem_id = :dataElementId and ma.NAME like :dataType ");
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("dataElementId", dataElementId);
         params.put("dataType", "datatype");
 
         String result = null;
         try {
             result = getNamedParameterJdbcTemplate().queryForObject(sql.toString(), params, String.class);
         } catch (EmptyResultDataAccessException e) {
             return null;
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void addDataElement(int vocabularyFolderId, int dataElementId) {
         String sql =
                 "insert into T_VOCABULARY_ELEMENT (VOCABULARY_FOLDER_ID, DATAELEM_ID) values (:vocabularyFolderId, :dataElementId)";
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("vocabularyFolderId", vocabularyFolderId);
         params.put("dataElementId", dataElementId);
 
         getNamedParameterJdbcTemplate().update(sql, params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void removeDataElement(int vocabularyFolderId, int dataElementId) {
         String sql =
                 "delete from T_VOCABULARY_ELEMENT where VOCABULARY_FOLDER_ID = :vocabularyFolderId and DATAELEM_ID = :dataElementId";
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("vocabularyFolderId", vocabularyFolderId);
         params.put("dataElementId", dataElementId);
 
         getNamedParameterJdbcTemplate().update(sql, params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<DataElement> getVocabularyDataElements(int vocabularyFolderId) {
         StringBuilder sb = new StringBuilder();
         sb.append("select ve.*, de.* from T_VOCABULARY_ELEMENT ve left join DATAELEM de on ve.DATAELEM_ID = de.DATAELEM_ID ");
         sb.append("where ve.VOCABULARY_FOLDER_ID = :vocabularyFolderId");
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("vocabularyFolderId", vocabularyFolderId);
 
         List<DataElement> result = getNamedParameterJdbcTemplate().query(sb.toString(), params, new RowMapper<DataElement>() {
 
             @Override
             public DataElement mapRow(ResultSet rs, int rowNum) throws SQLException {
                 DataElement de = new DataElement();
                 de.setId(rs.getInt("de.DATAELEM_ID"));
                 de.setShortName(rs.getString("de.SHORT_NAME"));
                 de.setStatus(rs.getString("de.REG_STATUS"));
                 de.setType(rs.getString("de.TYPE"));
                 de.setModified(new Date(rs.getLong("de.DATE")));
                 de.setWorkingUser(rs.getString("de.WORKING_USER"));
                 de.setIdentifier(rs.getString("de.IDENTIFIER"));
 
                 return de;
             }
 
         });
 
         for (DataElement elem : result) {
             List<FixedValue> fxvs = getFixedValues(elem.getId());
             elem.setFixedValues(fxvs);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyDataElements(int vocabularyFolderId) {
         String sql = "delete from T_VOCABULARY_ELEMENT where VOCABULARY_FOLDER_ID = :vocabularyFolderId";
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("vocabularyFolderId", vocabularyFolderId);
 
         getNamedParameterJdbcTemplate().update(sql, params);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteVocabularyConceptDataElementValues(int vocabularyConceptId) {
         String sql = "delete from T_CONCEPT_ELEMENT_VALUE where VOCABULARY_CONCEPT_ID = :vocabularyConceptId";
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("vocabularyConceptId", vocabularyConceptId);
 
         getNamedParameterJdbcTemplate().update(sql, params);
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void moveVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId) {
         String sql =
                 "update T_VOCABULARY_ELEMENT set VOCABULARY_FOLDER_ID = :targetVocabularyFolderId where VOCABULARY_FOLDER_ID = :sourceVocabularyFolderId";
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("sourceVocabularyFolderId", sourceVocabularyFolderId);
         params.put("targetVocabularyFolderId", targetVocabularyFolderId);
 
         getNamedParameterJdbcTemplate().update(sql, params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void copyVocabularyDataElements(int sourceVocabularyFolderId, int targetVocabularyFolderId) {
         StringBuilder sb = new StringBuilder();
         sb.append("insert into T_VOCABULARY_ELEMENT (VOCABULARY_FOLDER_ID, DATAELEM_ID) ");
         sb.append("select :targetVocabularyFolderId, DATAELEM_ID ");
         sb.append("from T_VOCABULARY_ELEMENT ");
         sb.append("where VOCABULARY_FOLDER_ID = :sourceVocabularyFolderId");
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("sourceVocabularyFolderId", sourceVocabularyFolderId);
         params.put("targetVocabularyFolderId", targetVocabularyFolderId);
 
         getNamedParameterJdbcTemplate().update(sb.toString(), params);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<List<DataElement>> getVocabularyConceptDataElementValues(int vocabularyFolderId, int vocabularyConceptId,
             boolean emptyAttributes) {
         final Map<String, Object> params = new HashMap<String, Object>();
         params.put("vocabularyFolderId", vocabularyFolderId);
         params.put("vocabularyConceptId", vocabularyConceptId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select * from T_CONCEPT_ELEMENT_VALUE v ");
         if (emptyAttributes) {
             sql.append("RIGHT OUTER JOIN DATAELEM d ");
         } else {
             sql.append("LEFT JOIN DATAELEM d ");
         }
         sql.append("ON (v.DATAELEM_ID = d.DATAELEM_ID and v.VOCABULARY_CONCEPT_ID = :vocabularyConceptId) ");
         sql.append("LEFT JOIN T_VOCABULARY_ELEMENT ve on ve.DATAELEM_ID = d.DATAELEM_ID ");
         sql.append("LEFT JOIN T_VOCABULARY_CONCEPT rc on v.RELATED_CONCEPT_ID = rc.VOCABULARY_CONCEPT_ID ");
         sql.append("where ve.VOCABULARY_FOLDER_ID = :vocabularyFolderId ");
        sql.append("order by ve.DATAELEM_ID, v.ELEMENT_VALUE, rc.IDENTIFIER");
 
         final List<List<DataElement>> result = new ArrayList<List<DataElement>>();
 
         getNamedParameterJdbcTemplate().query(sql.toString(), params, new RowCallbackHandler() {
 
             List<DataElement> values = null;
             int previousDataElemId = 0;
 
             @Override
             public void processRow(ResultSet rs) throws SQLException {
 
                 if (values == null) {
                     values = new ArrayList<DataElement>();
                     previousDataElemId = rs.getInt("d.DATAELEM_ID");
                 }
 
                 DataElement de = new DataElement();
                 de.setId(rs.getInt("d.DATAELEM_ID"));
                 de.setShortName(rs.getString("d.SHORT_NAME"));
                 de.setStatus(rs.getString("d.REG_STATUS"));
                 de.setType(rs.getString("d.TYPE"));
                 de.setModified(new Date(rs.getLong("d.DATE")));
                 de.setWorkingUser(rs.getString("d.WORKING_USER"));
 
                 de.setIdentifier(rs.getString("d.identifier"));
 
                 de.setAttributeValue(rs.getString("v.ELEMENT_VALUE"));
                 de.setAttributeLanguage(rs.getString("v.LANGUAGE"));
                 de.setRelatedConceptId(rs.getInt("v.RELATED_CONCEPT_ID"));
 
                 de.setRelatedConceptIdentifier(rs.getString("rc.IDENTIFIER"));
                 de.setRelatedConceptLabel(rs.getString("rc.LABEL"));
 
                 List<FixedValue> fxvs = getFixedValues(de.getId());
 
                 de.setFixedValues(fxvs);
 
                 if (previousDataElemId != rs.getInt("d.DATAELEM_ID")) {
                     result.add(values);
                     values = new ArrayList<DataElement>();
                 }
 
                 values.add(de);
                 previousDataElemId = rs.getInt("d.DATAELEM_ID");
 
                 if (rs.isLast()) {
                     result.add(values);
                 }
             }
         });
 
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void insertVocabularyConceptDataElementValues(int vocabularyConceptId, List<DataElement> dataElementValues) {
         StringBuilder sql = new StringBuilder();
         sql.append("insert into T_CONCEPT_ELEMENT_VALUE (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE, LANGUAGE, RELATED_CONCEPT_ID) ");
         sql.append("values (:vocabularyConceptId, :dataElementId, :elementValue, :language, :relatedConceptId)");
 
         @SuppressWarnings("unchecked")
         Map<String, Object>[] batchValues = new HashMap[dataElementValues.size()];
 
         for (int i = 0; i < batchValues.length; i++) {
             Map<String, Object> params = new HashMap<String, Object>();
             params.put("vocabularyConceptId", vocabularyConceptId);
             params.put("dataElementId", dataElementValues.get(i).getId());
             params.put("elementValue", dataElementValues.get(i).getAttributeValue());
             params.put("language", dataElementValues.get(i).getAttributeLanguage());
             params.put("relatedConceptId", dataElementValues.get(i).getRelatedConceptId());
             batchValues[i] = params;
         }
 
         getNamedParameterJdbcTemplate().batchUpdate(sql.toString(), batchValues);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void copyVocabularyConceptDataElementValues(int newVocabularyFolderId) {
         StringBuilder sql = new StringBuilder();
         sql.append("insert into T_CONCEPT_ELEMENT_VALUE (VOCABULARY_CONCEPT_ID, DATAELEM_ID, ELEMENT_VALUE, LANGUAGE, RELATED_CONCEPT_ID) ");
         sql.append("select con.VOCABULARY_CONCEPT_ID, v.DATAELEM_ID, v.ELEMENT_VALUE, v.LANGUAGE, v.RELATED_CONCEPT_ID ");
         sql.append("from T_CONCEPT_ELEMENT_VALUE v ");
         sql.append("left join T_VOCABULARY_CONCEPT con on v.VOCABULARY_CONCEPT_ID = con.ORIGINAL_CONCEPT_ID  ");
         sql.append("where con.VOCABULARY_FOLDER_ID = :newVocabularyFolderId");
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("newVocabularyFolderId", newVocabularyFolderId);
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean vocabularyHasElemendBinding(int vocabularyFolderId, int elementId) {
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("vocabularyId", vocabularyFolderId);
         parameters.put("elementId", elementId);
 
         StringBuilder sql = new StringBuilder();
         sql.append("select count(VOCABULARY_FOLDER_ID) from T_VOCABULARY_ELEMENT ");
         sql.append("where DATAELEM_ID = :elementId and VOCABULARY_FOLDER_ID = :vocabularyId ");
 
         int result = getNamedParameterJdbcTemplate().queryForInt(sql.toString(), parameters);
 
         return result > 0;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void updateRelatedConceptIds(int newVocabularyFolderId) {
         StringBuilder sql = new StringBuilder();
         sql.append("UPDATE T_CONCEPT_ELEMENT_VALUE cev, T_VOCABULARY_CONCEPT con1, T_VOCABULARY_CONCEPT con2, DATAELEM e ");
         sql.append("set cev.RELATED_CONCEPT_ID = con2.VOCABULARY_CONCEPT_ID ");
         sql.append("where cev.VOCABULARY_CONCEPT_ID = con1.VOCABULARY_CONCEPT_ID ");
         sql.append("and cev.RELATED_CONCEPT_ID = con2.ORIGINAL_CONCEPT_ID ");
         sql.append("and cev.DATAELEM_ID = e.DATAELEM_ID ");
         sql.append("and con1.VOCABULARY_FOLDER_ID = :newVocabularyFolderId ");
         //sql.append("and e.IDENTIFIER in (:relationalIdentifiers)");
         //sql.append("and e.IDENTIFIER in (" + DataElement.getCSRelationalPrefixes() + ")");
 
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("newVocabularyFolderId", newVocabularyFolderId);
         //parameters.put("relationalIdentifiers", DataElement.getRelationalPrefixes());
 
 
         getNamedParameterJdbcTemplate().update(sql.toString(), parameters);
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void deleteRelatedElements(int vocabularyConceptId) {
         String sql = "delete from T_CONCEPT_ELEMENT_VALUE where RELATED_CONCEPT_ID = :relatedConceptId";
 
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("relatedConceptId", vocabularyConceptId);
 
         getNamedParameterJdbcTemplate().update(sql, params);
     }
 }
