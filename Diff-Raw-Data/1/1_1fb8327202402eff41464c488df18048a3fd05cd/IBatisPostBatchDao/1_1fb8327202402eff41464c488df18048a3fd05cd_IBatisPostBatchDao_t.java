 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.orangeleap.tangerine.dao.ibatis;
 
 import com.ibatis.sqlmap.client.SqlMapClient;
 import com.orangeleap.tangerine.dao.PostBatchDao;
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.domain.PostBatchEntry;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 @Repository("postBatchDAO")
 public class IBatisPostBatchDao extends AbstractIBatisDao implements PostBatchDao {
 
 	/** Logger for this class and subclasses */
 	protected final Log logger = OLLogger.getLog(getClass());
 
 	@Autowired
 	public IBatisPostBatchDao(SqlMapClient sqlMapClient) {
 		super(sqlMapClient);
 	}
 
     private void addBatchStatusParam(final String showBatchStatus, final Map<String, Object> params) {
         if (StringConstants.ERRORS.equals(showBatchStatus))  {
             params.put("showErrors", true);
         }
         else if (StringConstants.EXECUTED.equals(showBatchStatus)) {
             params.put("showRanBatches", true);
         }
         else {
             // assume 'open' is the batch type
             params.put("showRanBatches", false);
            params.put("showErrors", false);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<PostBatch> readBatchesByStatus(final String showBatchStatus, String sortPropertyName, String direction, int start, int limit, Locale locale) {
         if (logger.isTraceEnabled()) {
             logger.trace("readBatchesByStatus: showBatchStatus = " + showBatchStatus + " sortPropertyName = " + sortPropertyName + " direction = " + direction +
                     " start = " + start + " limit = " + limit);
         }
         Map<String, Object> params = setupSortParams("postBatch", "POST_BATCH.POST_BATCH_RESULT",
                 sortPropertyName, direction, start, limit, locale);
 
         addBatchStatusParam(showBatchStatus, params);
         return getSqlMapClientTemplate().queryForList("SELECT_LIMITED_POST_BATCHES", params);
     }
 
     @Override
     public int countBatchesByStatus(String showBatchStatus) {
         if (logger.isTraceEnabled()) {
             logger.trace("countBatchesByStatus: showBatchStatus = " + showBatchStatus);
         }
         Map<String, Object> params = setupParams();
         addBatchStatusParam(showBatchStatus, params);
         return (Integer) getSqlMapClientTemplate().queryForObject("COUNT_LIMITED_POST_BATCHES", params);
     }
 
     /**
      * Retrieves the PostBatch for the specified postBatchId, including the PostBatchEntries
      * @param postBatchId the specified postBatchId
      * @return PostBatch object
      */
     @Override
     public PostBatch readPostBatchById(Long postBatchId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readPostBatchById: postBatchId = " + postBatchId);
         }
         Map<String, Object> params = setupParams();
         params.put(StringConstants.ID, postBatchId);
         PostBatch batch = (PostBatch)getSqlMapClientTemplate().queryForObject("SELECT_POST_BATCH_BY_ID", params);
         readCustomFields(batch);
         return batch;
     }
 
 	@Override
 	public PostBatch maintainPostBatch(PostBatch batch) {
 		if (logger.isTraceEnabled()) {
 			logger.trace("maintainPostBatch: postBatchId = " + batch.getId());
 		}
         /* Delete PostBatchEntries first if the batch is being edited */
         if ( ! batch.isNew()) {
             getSqlMapClientTemplate().delete("DELETE_POST_BATCH_ENTRIES_BY_POST_BATCH_ID", batch);
         }
         setCustomFields(batch);
 		batch = (PostBatch) insertOrUpdate(batch, "POST_BATCH");
         maintainPostBatchEntries(batch);
         return batch;
 	}
 
     /**
      * Delete the existing PostBatchEntries if any, and insert again
      * @param batch batch that contains the entries
      */
     private void maintainPostBatchEntries(PostBatch batch) {
         if (batch.getPostBatchEntries() != null) {
             for (PostBatchEntry entry : batch.getPostBatchEntries()) {
                 entry.setId(null); // a new ID will be generated during the insert
                 entry.setPostBatchId(batch.getId());
                 insertOrUpdate(entry, "POST_BATCH_ENTRY");
             }
         }
     }
 
     private void setCustomFields(PostBatch batch) {
         if (batch != null) {
             batch.clearCustomFieldMap();
             for (Map.Entry<String, String> entry : batch.getUpdateFields().entrySet()) {
                 batch.setCustomFieldValue(entry.getKey(), entry.getValue());
             }
         }
     }
 
     private void readCustomFields(PostBatch batch) {
         if (batch != null) {
             batch.clearUpdateFields();
             for (Map.Entry<String, CustomField> entry : batch.getCustomFieldMap().entrySet()) {
                 String key = entry.getKey();
                 String value = entry.getValue().getValue();
                 batch.addUpdateField(key, value);
             }
         }
     }
 
     @Override
     public void deletePostBatch(PostBatch batch) {
         if (logger.isTraceEnabled()) {
             logger.trace("deletePostBatch: batchId = " + batch.getId());
         }
         Map<String, Object> params = setupParams();
         params.put("postBatch", batch);
         
         // First, delete the segmentations, then the batch itself
         getSqlMapClientTemplate().delete("DELETE_POST_BATCH_ENTRIES_BY_POST_BATCH_ID", batch);
         getSqlMapClientTemplate().delete("DELETE_POST_BATCH", params);
     }
 
     /********************************** below will be removed ******************************/
     @SuppressWarnings("unchecked")
 	@Override
     public List<PostBatchEntry> readPostBatchReviewSetItems(Long postBatchId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readPostBatchById: postBatchId = " + postBatchId);
         }
         Map<String, Object> params = setupParams();
         params.put("postBatchId", postBatchId);
         List<PostBatchEntry> result = (List<PostBatchEntry>)getSqlMapClientTemplate().queryForList("SELECT_POST_BATCH_REVIEW_SET_ITEMS", params);
         return result;
     }
     
     @SuppressWarnings("unchecked")
 	@Override
     public PaginatedResult readPostBatchReviewSetItems(Long postBatchId, SortInfo sortinfo) {
         if (logger.isTraceEnabled()) {
             logger.trace("readPostBatchReviewSetItems: postBatchId = " + postBatchId);
         }
         Map<String, Object> params = setupParams();
         sortinfo.addParams(params);
 
 		params.put("postBatchId", postBatchId);
 
         Long count = (Long)getSqlMapClientTemplate().queryForObject("SELECT_POST_BATCH_REVIEW_SET_SIZE", params);
         PostBatch postbatch = readPostBatchById(postBatchId);
         if (postbatch == null) return null;
         List rows;
         if (StringConstants.GIFT.equals(postbatch.getBatchType())) {
            rows = getSqlMapClientTemplate().queryForList("SELECT_POST_BATCH_REVIEW_SET_ITEMS_GIFT_PAGINATED", params);
         } else {
            rows = getSqlMapClientTemplate().queryForList("SELECT_POST_BATCH_REVIEW_SET_ITEMS_ADJUSTED_GIFT_PAGINATED", params);
         }
         
         PaginatedResult resp = new PaginatedResult();
         resp.setRows(rows);
         resp.setRowCount(count);
         return resp;
     }
 
     @Override
     public PostBatchEntry maintainPostBatchReviewSetItem(PostBatchEntry postBatchEntry) {
         if (logger.isTraceEnabled()) {
             logger.trace("maintainPostBatchReviewSetItem: maintainPostBatchReviewSetItemId = " + postBatchEntry.getId());
         }
         PostBatchEntry aPostBatchEntry = (PostBatchEntry) insertOrUpdate(postBatchEntry, "POST_BATCH_REVIEW_SET_ITEM");
         return aPostBatchEntry;
     }
 
     public void insertIntoPostBatchFromGiftSelect(PostBatch postbatch, Map<String, Object> searchmap) {
         Map<String, Object> params = setupParams();
         params.put("postBatchId", postbatch.getId());
         params.putAll(searchmap);
         getSqlMapClientTemplate().insert("INSERT_POST_BATCH_REVIEW_SET_ITEM_FROM_GIFT_SELECT", params);
     }
     
     public void insertIntoPostBatchFromAdjustedGiftSelect(PostBatch postbatch, Map<String, Object> searchmap) {
         Map<String, Object> params = setupParams();
         params.put("postBatchId", postbatch.getId());
         params.putAll(searchmap);
         getSqlMapClientTemplate().insert("INSERT_POST_BATCH_REVIEW_SET_ITEM_FROM_ADJUSTED_GIFT_SELECT", params);
     }
 
     public Long getReviewSetSize(Long postBatchId) {
         Map<String, Object> params = setupParams();
         params.put("postBatchId", postBatchId);
         return (Long)getSqlMapClientTemplate().queryForObject("SELECT_POST_BATCH_REVIEW_SET_SIZE", params);
     }
 
     @SuppressWarnings("unchecked")
 	@Override
     public List<PostBatch> listBatches() {
         if (logger.isTraceEnabled()) {
             logger.trace("listBatches:");
         }
         Map<String, Object> params = setupParams();
         return (List<PostBatch>)getSqlMapClientTemplate().queryForList("SELECT_POST_BATCHES", params);
     }
 }
