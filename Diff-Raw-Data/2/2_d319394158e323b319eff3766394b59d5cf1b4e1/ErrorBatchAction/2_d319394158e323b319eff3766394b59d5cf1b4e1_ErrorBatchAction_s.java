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
 
 package com.orangeleap.tangerine.web.flow.batch;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 import com.orangeleap.tangerine.web.common.SortInfo;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.ExtTypeHandler;
 import org.apache.commons.logging.Log;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.webflow.execution.RequestContext;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @Component("errorBatchAction")
 public class ErrorBatchAction extends EditBatchAction {
 
     protected final Log logger = OLLogger.getLog(getClass());
 
     /**
      * Display the batch description for this error batch
      * @param flowRequestContext
      * @param batchId
      * @return model
      */
     @SuppressWarnings("unchecked")
     public ModelMap errorStep1(final RequestContext flowRequestContext, final Long batchId) {
         if (logger.isTraceEnabled()) {
             logger.trace("errorStep1: batchId = " + batchId);
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch); // TODO: do as annotation
         determineStepToSave(flowRequestContext);
         
         PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         if (batch == null || (batchId != null && ! batchId.equals(batch.getId()))) {
             batch = postBatchService.readBatch(batchId);
             setFlowScopeAttribute(flowRequestContext, batch, StringConstants.BATCH);
         }
 
         final ModelMap model = new ModelMap();
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
 
         if (batch != null) {
             final Map<String, String> dataMap = new HashMap<String, String>();
             dataMap.put("batchDesc", batch.getBatchDesc());
             dataMap.put("batchType", TangerineMessageAccessor.getMessage(batch.getBatchType()));
 	        dataMap.put("criteriaFields", batch.isForTouchPoints() ? TangerineMessageAccessor.getMessage("touchPointFields") : TangerineMessageAccessor.getMessage("batchTypeFields"));
 	        dataMap.put("hiddenErrorBatchType", batch.getBatchType());
             model.put(StringConstants.DATA, dataMap);
         }
         return model;
     }
 
     /**
      * Display the rows that make up this error batch, in addition to the actual errors  
      * @param flowRequestContext
      * @return model
      */
     @SuppressWarnings("unchecked")
     public ModelMap errorStep2(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("errorStep2:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch); // TODO: do as annotation
         determineStepToSave(flowRequestContext);
 
         final SortInfo sortInfo = getSortInfo(flowRequestContext);
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final ModelMap model = new ModelMap();
 
         // MetaData
         final Map<String, Object> metaDataMap = tangerineListHelper.initMetaData(sortInfo.getStart(), sortInfo.getLimit());
         final Map<String, String> sortInfoMap = new HashMap<String, String>();
         if ( ! StringUtils.hasText(sortInfo.getSort()) ||
                 (StringConstants.GIFT.equals(batch.getBatchType()) && ( ! sortInfo.getSort().equals(StringConstants.GIFT_ID) && ! sortInfo.getSort().equals("errorMsg"))) ||
                 (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType()) && ( ! sortInfo.getSort().equals(StringConstants.ADJUSTED_GIFT_ID) && ! sortInfo.getSort().equals("errorMsg")))) {
             if (StringConstants.GIFT.equals(batch.getBatchType())) {
                 sortInfo.setSort(StringConstants.GIFT_ID);
             }
             else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
                 sortInfo.setSort(StringConstants.ADJUSTED_GIFT_ID);
             }
             sortInfo.setDir(StringConstants.ASC);
         }
         sortInfoMap.put(StringConstants.FIELD, sortInfo.getSort());
         sortInfoMap.put(StringConstants.DIRECTION, sortInfo.getDir());
         metaDataMap.put(StringConstants.SORT_INFO, sortInfoMap);
 
         final List<Map<String, Object>> rowList = postBatchService.readPostBatchEntryErrorsByBatchId(batch.getId(), sortInfo);
         addUniqueSequenceAsId(rowList);
         final int totalRows = postBatchService.countPostBatchEntryErrorsByBatchId(batch.getId());
 
         // Fields
         final List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
         String idName = null;
         String constituentIdName = null;
 
         if (StringConstants.GIFT.equals(batch.getBatchType())) {
             idName = TangerineForm.escapeFieldName(StringConstants.GIFT_ID);
             constituentIdName = "giftConstituentId";
         }
         else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
             idName = TangerineForm.escapeFieldName(StringConstants.ADJUSTED_GIFT_ID);
             constituentIdName = "adjustedGiftConstituentId";
         }
         if (idName != null) {
             Map<String, Object> fieldMap = new HashMap<String, Object>();
             fieldMap.put(StringConstants.NAME, idName);
             fieldMap.put(StringConstants.MAPPING, idName);
             fieldMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_INT);
             fieldMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.ID));
             fieldList.add(fieldMap);
         }
 
         Map<String, Object> fieldMap = new HashMap<String, Object>();
         String errorMsg = TangerineForm.escapeFieldName("errorMsg");
         fieldMap.put(StringConstants.NAME, errorMsg);
         fieldMap.put(StringConstants.MAPPING, errorMsg);
         fieldMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_STRING);
         fieldMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage("errors"));
         fieldList.add(fieldMap);
 
         if (constituentIdName != null) {
             fieldMap = new HashMap<String, Object>();
             fieldMap.put(StringConstants.NAME, constituentIdName);
             fieldMap.put(StringConstants.MAPPING, constituentIdName);
             fieldMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_INT);
             fieldMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.CONSTITUENT_ID));
             fieldList.add(fieldMap);
         }
 
         metaDataMap.put(StringConstants.FIELDS, fieldList);
         model.put(StringConstants.META_DATA, metaDataMap);
         model.put(StringConstants.TOTAL_ROWS, totalRows);
         model.put(StringConstants.ROWS, rowList);
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         return model;
     }
 
     /**
      * Get the batch update fields
      * @param flowRequestContext
      * @return model
      */
     @SuppressWarnings("unchecked")
     public ModelMap errorStep3(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("errorStep3:");
         }
 	    return findUpdateFields(flowRequestContext);
     }
 
     /**
      * Show the update field changes (before and after)
      * @param flowRequestContext
      * @return model
      */
     @SuppressWarnings("unchecked")
     public ModelMap errorStep4(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("errorStep4:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch); // TODO: do as annotation
         determineStepToSave(flowRequestContext);
 
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final SortInfo sortInfo = getSortInfo(flowRequestContext);
         final ModelMap model = new ModelMap();
 
         initReviewUpdateFields(batch, model, sortInfo);
 
         final List<Map<String, Object>> rowValues = new ArrayList<Map<String, Object>>();
         List rows = null;
         int totalRows = 0;
 
         unescapeSortField(sortInfo);
 
         if (StringConstants.GIFT.equals(batch.getBatchType())) {
             final Set<Long> giftIds = batch.getEntryGiftIds();
             rows = giftService.readLimitedGiftsByIds(giftIds, sortInfo, getRequest(flowRequestContext).getLocale());
             totalRows = giftIds.size() * 2;
         }
         else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
             final Set<Long> adjustedGiftIds = batch.getEntryAdjustedGiftIds();
             rows = adjustedGiftService.readLimitedAdjustedGiftsByIds(adjustedGiftIds, sortInfo, getRequest(flowRequestContext).getLocale());
             totalRows = adjustedGiftIds.size() * 2;
         }
         contrastUpdatedValues(batch, rows, rowValues, batch.getUpdateFields());
 
         model.put(StringConstants.ROWS, rowValues);
         model.put(StringConstants.TOTAL_ROWS, totalRows);
 
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         return model;
     }
 
     private void determineStepToSave(final RequestContext flowRequestContext) {
         final String previousStep = getRequestParameter(flowRequestContext, PREVIOUS_STEP);
         if ("step1Error".equals(previousStep)) {
             saveBatchDesc(flowRequestContext);
         }
         else if ("step3Error".equals(previousStep)) {
             saveBatchUpdateFields(flowRequestContext);
         }
     }
 
     private void saveBatchDesc(final RequestContext flowRequestContext) {
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         if (batch != null) {
             final String batchDesc = getRequestParameter(flowRequestContext, StringConstants.BATCH_DESC);
             batch.setBatchDesc(batchDesc);
         }
     }
 
     private void addUniqueSequenceAsId(final List<Map<String, Object>> rowList) {
         for (int x = 0; x < rowList.size(); x++) {
             final Map<String, Object> rowMap = rowList.get(x);
             rowMap.put(StringConstants.ID, x);
         }
     }
 }
