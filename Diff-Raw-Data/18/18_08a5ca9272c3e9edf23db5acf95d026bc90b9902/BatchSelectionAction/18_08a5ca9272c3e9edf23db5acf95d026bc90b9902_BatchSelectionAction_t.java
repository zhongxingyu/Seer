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
 package com.orangeleap.tangerine.web.flow;
 
 import com.orangeleap.tangerine.controller.TangerineForm;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.customization.CustomField;
 import com.orangeleap.tangerine.domain.customization.FieldDefinition;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.domain.customization.SectionField;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.AbstractPaymentInfoEntity;
 import com.orangeleap.tangerine.service.AdjustedGiftService;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.service.PostBatchService;
 import com.orangeleap.tangerine.service.customization.FieldService;
 import com.orangeleap.tangerine.service.customization.PageCustomizationService;
 import com.orangeleap.tangerine.type.AccessType;
 import com.orangeleap.tangerine.type.FieldType;
 import com.orangeleap.tangerine.type.PageType;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineMessageAccessor;
 import com.orangeleap.tangerine.web.common.SortInfo;
 import com.orangeleap.tangerine.web.common.TangerineListHelper;
 import com.orangeleap.tangerine.web.customization.tag.fields.SectionFieldTag;
 import com.orangeleap.tangerine.web.customization.tag.fields.handlers.ExtTypeHandler;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.util.WebUtils;
 import org.springframework.webflow.context.ExternalContext;
 import org.springframework.webflow.context.servlet.ServletExternalContext;
 import org.springframework.webflow.core.collection.MutableAttributeMap;
 import org.springframework.webflow.execution.RequestContext;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 @Component("batchSelectionAction")
 public class BatchSelectionAction {
 
     protected final Log logger = OLLogger.getLog(getClass());
     public static final String PARAM_PREFIX = "param-";
     public static final String BATCH_FIELDS = "BatchFields";
     public static final String PICKED_SEGMENTATION_IDS = "pickedSegmentationIds";
 
     @Resource(name = "postBatchService")
     private PostBatchService postBatchService;
 
     @Resource(name = "giftService")
     private GiftService giftService;
 
     @Resource(name = "adjustedGiftService")
     private AdjustedGiftService adjustedGiftService;
 
     @Resource(name = "pageCustomizationService")
     protected PageCustomizationService pageCustomizationService;
 
     @Resource(name = "picklistItemService")
     private PicklistItemService picklistItemService;
 
     @Resource(name = "tangerineListHelper")
     protected TangerineListHelper tangerineListHelper;
 
     @Resource(name = "fieldService")
     private FieldService fieldService;
 
     private String resolveSegmentationFieldName(String key) {
         String resolvedName = key;
         if (StringConstants.NAME.equals(key)) {
             resolvedName = "reportName";
         }
         else if ("desc".equals(key)) {
             resolvedName = "reportComment";
         }
         else if ("count".equals(key)) {
             resolvedName = "resultCount";
         }
         else if ("lastDt".equals(key)) {
             resolvedName = "lastRunDateTime";
         }
         else if ("lastUser".equals(key)) {
             resolvedName = "lastRunByUserName";
         }
         return resolvedName;
     }
 
     private HttpServletRequest getRequest(final RequestContext flowRequestContext) {
         return (HttpServletRequest) ((ServletExternalContext) flowRequestContext.getExternalContext()).getNativeRequest();
     }
 
     private void setFlowScopeAttribute(final RequestContext flowRequestContext, final Object object, final String key) {
         MutableAttributeMap flowScopeMap = flowRequestContext.getFlowScope();
         flowScopeMap.put(key, object);
     }
 
     private Object getFlowScopeAttribute(final RequestContext flowRequestContext, final String key) {
         MutableAttributeMap flowScopeMap = flowRequestContext.getFlowScope();
         return flowScopeMap.get(key);
     }
 
     private PostBatch getBatchFromFlowScope(final RequestContext flowRequestContext) {
         return (PostBatch) getFlowScopeAttribute(flowRequestContext, StringConstants.BATCH);
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step1FindBatchInfo(final RequestContext flowRequestContext, final Long batchId) {
         if (logger.isTraceEnabled()) {
             logger.trace("step1FindBatchInfo: batchId = " + batchId);
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final ModelMap model = new ModelMap();
         PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         final Map<String, String> dataMap = new HashMap<String, String>();
         model.put(StringConstants.DATA, dataMap);
 
         /* Create a new batch if one doesn't already exist in this scope or the ID differs */
         if (batch == null || (batchId != null && ! batchId.equals(batch.getId()))) {
             batch = postBatchService.readBatchCreateIfNull(batchId);
             
             if (batch.isNew()) {
                 batch.setBatchDesc(StringConstants.EMPTY);
                 batch.setBatchType(StringConstants.GIFT); // default is gift
             }
 
             // add only the PostBatch to the view scope and remove from the returnMap
             setFlowScopeAttribute(flowRequestContext, batch, StringConstants.BATCH);
         }
         dataMap.put(StringConstants.BATCH_DESC, batch.getBatchDesc());
         dataMap.put(StringConstants.BATCH_TYPE, batch.getBatchType());
 
         return model;
     }
 
     @SuppressWarnings("unchecked")
     private Set<Long> syncPickedSegmentationIds(final RequestContext flowRequestContext, final PostBatch batch, final String pickedIdsStr, final String notPickedIdsStr) {
 
         /**
          * Because users can pick/unpick IDs across multiple grid pages in the step 2 grid, we need to keep a running tab of which segmentation
          * IDs the users selected
          */
         Set<Long> pickedSegmentationIds = (Set<Long>) getFlowScopeAttribute(flowRequestContext, PICKED_SEGMENTATION_IDS);
         if (pickedSegmentationIds == null) {
             pickedSegmentationIds = batch.getEntrySegmentationIds(); // Initialize with all the segmentation IDs for this batch
         }
         Set<String> pickedIds = StringUtils.commaDelimitedListToSet(pickedIdsStr);
         Set<String> notPickedIds = StringUtils.commaDelimitedListToSet(notPickedIdsStr);
 
         Collection<String> commonIds = CollectionUtils.intersection(pickedIds, notPickedIds); // there should not be any IDs BOTH picked and not-picked, but check just in case
         if (commonIds != null && ! commonIds.isEmpty()) {
             // If there are Picked and Not Picked Ids, just assume they are picked
             notPickedIds = new TreeSet<String>(CollectionUtils.subtract(notPickedIds, commonIds));
         }
 
         /* Add any new 'pickedIds' */
         for (String thisPickedId : pickedIds) {
             if (NumberUtils.isDigits(thisPickedId)) {
                 if ( ! pickedSegmentationIds.contains(new Long(thisPickedId))) {
                     pickedSegmentationIds.add(new Long(thisPickedId));
                 }
             }
         }
 
         Iterator<Long> pickedSegIter = pickedSegmentationIds.iterator();
 
         /* Remove any 'not picked' segmentations */
         if ( ! notPickedIds.isEmpty()) {
             while (pickedSegIter.hasNext()) {
                 Long segmentationId =  pickedSegIter.next();
                 if (notPickedIds.contains(segmentationId.toString())) {
                     pickedSegIter.remove();
                 }
             }
         }
         setFlowScopeAttribute(flowRequestContext, pickedSegmentationIds, PICKED_SEGMENTATION_IDS);
         return pickedSegmentationIds;
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step2FindSegmentations(final RequestContext flowRequestContext, final String batchType, final String pickedIds, final String notPickedIds,
                                            final String batchDesc, final String sort, final String dir, final String limit, final String start) {
         if (logger.isTraceEnabled()) {
             logger.trace("step2FindSegmentations: batchType = " + batchType + " pickedIds = " + pickedIds + " notPickedIds = " +
                     notPickedIds + " batchDesc = " + batchDesc + " sort = " + sort + " dir = " + dir + " limit = " + limit + " start = " + start);
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final ModelMap model = new ModelMap();
         final SortInfo sortInfo = new SortInfo(sort, dir, limit, start);
         
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         batch.setBatchDesc(batchDesc);
 
         Set<Long> pickedSegmentationIds = syncPickedSegmentationIds(flowRequestContext, batch, pickedIds, notPickedIds);
 
         /**
          * Check if the batchType is different from what was previously entered during the flow - if so, reset the previously selected segmentation IDs and
          * the updated fields
          */
         if (batch.getBatchType() != null && StringUtils.hasText(batch.getBatchType()) &&
                 StringUtils.hasText(batchType) && ! batchType.equals(batch.getBatchType())) {
             batch.clearPostBatchEntries();
             batch.clearUpdateFields();
             pickedSegmentationIds.clear();
         }
         batch.setBatchType(batchType);
 
         model.put("pickedSegmentationsCount", pickedSegmentationIds.size());
         model.put(StringConstants.ROWS, postBatchService.findSegmentationsForBatchType(batch, pickedSegmentationIds, batchType,
                 resolveSegmentationFieldName(sortInfo.getSort()), sortInfo.getDir(),
                 sortInfo.getStart(), sortInfo.getLimit()));
         model.put(StringConstants.TOTAL_ROWS, postBatchService.findTotalSegmentations(batchType));
 
         return model;
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step3FindRowsForSegmentations(final RequestContext flowRequestContext, final String pickedIds,
                                                   final String notPickedIds, final String sort, final String dir, final String limit, final String start) {
         if (logger.isTraceEnabled()) {
             logger.trace("step3FindRowsForSegmentations: pickedIds = " + pickedIds + " notPickedIds = " + notPickedIds +
                     " sort = " + sort + " dir = " + dir + " limit = " + limit + " start = " + start);
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final ModelMap model = new ModelMap();
         final HttpServletRequest request = getRequest(flowRequestContext);
         final SortInfo sortInfo = new SortInfo(sort, dir, limit, start);
 
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
 
         /* First sync the segmentation Ids */
         Set<Long> pickedSegmentationIds = syncPickedSegmentationIds(flowRequestContext, batch, pickedIds, notPickedIds);
 
         /* Then clear out and add all segmentations */
         batch.clearAddAllPostBatchEntriesForSegmentations(pickedSegmentationIds);
         createJsonModel(request, batch, model, sortInfo);
         model.put(StringConstants.SUCCESS, Boolean.TRUE);
         
         return model;
     }
 
     @SuppressWarnings("unchecked")
     private void createJsonModel(HttpServletRequest request, PostBatch batch, Map model, SortInfo sort) {
         /* MetaData */
         final Map<String, Object> metaDataMap = tangerineListHelper.initMetaData(sort.getStart(), sort.getLimit());
         metaDataMap.put(StringConstants.LIMIT, sort.getLimit());
 
         final List<SectionField> allFields = tangerineListHelper.findSectionFields(batch.getBatchType() + "List");
 
         final BeanWrapper bw = createDefaultEntity(batch);
         final List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
 
         addIdConstituentIdFields(fieldList, bw);
         for (SectionField sectionFld : allFields) {
             final Map<String, Object> fieldMap = new HashMap<String, Object>();
             String escapedFieldName = TangerineForm.escapeFieldName(sectionFld.getFieldPropertyName());
             fieldMap.put(StringConstants.NAME, escapedFieldName);
             fieldMap.put(StringConstants.MAPPING, escapedFieldName);
             String extType = ExtTypeHandler.findExtType(bw.getPropertyType(sectionFld.getFieldPropertyName()));
             fieldMap.put(StringConstants.TYPE, extType);
             fieldMap.put(StringConstants.HEADER, sectionFld.getFieldDefinition().getDefaultLabel());
 
             if (StringConstants.DATE.equals(extType)) {
                 String format;
                 if (FieldType.CC_EXPIRATION.equals(sectionFld.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(sectionFld.getFieldType())) {
                     format = "m-d-Y";
                 }
                 else {
                     format = "Y-m-d H:i:s";
                 }
                 fieldMap.put(StringConstants.DATE_FORMAT, format);
             }
             fieldList.add(fieldMap);
         }
         metaDataMap.put(StringConstants.FIELDS, fieldList);
 
         final Map<String, String> sortInfoMap = new HashMap<String, String>();
         if ( ! StringUtils.hasText(sort.getSort()) || ! bw.isReadableProperty(sort.getSort())) {
             final List<SectionField> fieldsExceptId = pageCustomizationService.getFieldsExceptId(allFields);
             sort.setSort(TangerineForm.escapeFieldName(fieldsExceptId.get(0).getFieldPropertyName()));
             sort.setDir(SectionFieldTag.getInitDirection(fieldsExceptId));
         }
 
         sortInfoMap.put(StringConstants.FIELD, sort.getSort());
         sortInfoMap.put(StringConstants.DIRECTION, sort.getDir());
         metaDataMap.put(StringConstants.SORT_INFO, sortInfoMap);
 
         model.put(StringConstants.META_DATA, metaDataMap);
 
         Set<Long> reportIds = batch.getEntrySegmentationIds();
         List rowObjects = null;
         int totalRows = 0;
         if (StringConstants.GIFT.equals(batch.getBatchType())) {
             totalRows = giftService.readCountGiftsBySegmentationReportIds(reportIds);
             rowObjects = giftService.readGiftsBySegmentationReportIds(reportIds, sort, request.getLocale());
         }
         else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
             totalRows = adjustedGiftService.readCountAdjustedGiftsBySegmentationReportIds(reportIds);
             rowObjects = adjustedGiftService.readAdjustedGiftsBySegmentationReportIds(reportIds, sort, request.getLocale());
         }
 
         model.put(StringConstants.TOTAL_ROWS, totalRows);
 
         List<Map<String, Object>> rowList = new ArrayList<Map<String, Object>>();
         tangerineListHelper.addListFieldsToMap(request, allFields, rowObjects, rowList, false, false); // this needs to be 'allFields' to include the 'id' property
         addConstituentIdsToRows(rowList, rowObjects);
         model.put(StringConstants.ROWS, rowList);
     }
 
     private void addIdConstituentIdFields(final List<Map<String, Object>> fieldList, final BeanWrapper bw) {
         final Map<String, Object> idMap = new HashMap<String, Object>();
         idMap.put(StringConstants.NAME, StringConstants.ID);
         idMap.put(StringConstants.MAPPING, StringConstants.ID);
         idMap.put(StringConstants.TYPE, "int");
         idMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.ID));
         fieldList.add(idMap);
 
         if (bw.isReadableProperty(StringConstants.CONSTITUENT) || bw.isReadableProperty(StringConstants.CONSTITUENT_ID)) {
             final Map<String, Object> constituentIdMap = new HashMap<String, Object>();
             constituentIdMap.put(StringConstants.NAME, StringConstants.CONSTITUENT_ID);
             constituentIdMap.put(StringConstants.MAPPING, StringConstants.CONSTITUENT_ID);
             constituentIdMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_STRING);
             constituentIdMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.CONSTITUENT_ID));
             fieldList.add(constituentIdMap);
         }
     }
 
     private void addConstituentIdsToRows(final List<Map<String, Object>> rowList, final List rows) {
         for (Map<String, Object> objectMap : rowList) {
             Long id = (Long) objectMap.get(StringConstants.ID);
             for (Object thisRow : rows) {
                 BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(thisRow);
                 if (bw.isReadableProperty(StringConstants.ID) && ((Long) bw.getPropertyValue(StringConstants.ID)).equals(id)) {
                     Long constituentId = null;
                     if (bw.isReadableProperty(StringConstants.CONSTITUENT_ID) && bw.getPropertyValue(StringConstants.CONSTITUENT_ID) != null) {
                         constituentId = (Long) bw.getPropertyValue(StringConstants.CONSTITUENT_ID);
                     }
                     else if (bw.isReadableProperty(StringConstants.CONSTITUENT_DOT_ID) && bw.getPropertyValue(StringConstants.CONSTITUENT_DOT_ID) != null) {
                         constituentId = (Long) bw.getPropertyValue(StringConstants.CONSTITUENT_DOT_ID); 
                     }
                     if (constituentId != null) {
                         objectMap.put(StringConstants.CONSTITUENT_ID, constituentId);
                         break;
                     }
                 }
             }
         }
     }
 
     /**
      * Create a mock entity object for introspection
      * @param batch the batch to create a mock default entity object for
      * @return a BeanWrapper of the mock entity object
      */
     private BeanWrapper createDefaultEntity(PostBatch batch) {
         AbstractPaymentInfoEntity entity = null;
         if (StringConstants.GIFT.equals(batch.getBatchType())) {
             entity = new Gift();
         }
         else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
             entity = new AdjustedGift();
         }
         Constituent constituent = new Constituent(0L, new Site());
         entity.setConstituent(constituent);
         entity.setPaymentSource(new PaymentSource(constituent));
         return PropertyAccessorFactory.forBeanPropertyAccess(entity);
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step4FindBatchUpdateFields(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("step4FindBatchUpdateFields:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final String picklistNameId = new StringBuilder(batch.getBatchType()).append(BATCH_FIELDS).toString();
         final Picklist picklist = picklistItemService.getPicklist(picklistNameId);
         final ModelMap model = new ModelMap();
         final List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
 
         if (picklist != null) {
             for (PicklistItem item : picklist.getActivePicklistItems()) {
                 // the defaultDisplayValue will be the fieldDefinitionId like 'adjustedGift.status' which we need to resolve to the fieldName like 'adjustedGift.adjustedStatus'
                 String fieldDefinitionId = new StringBuilder(batch.getBatchType()).append(".").append(item.getDefaultDisplayValue()).toString();
                 FieldDefinition fieldDef = fieldService.readFieldDefinition(fieldDefinitionId);
                 if (fieldDef != null) {
                     FieldType fieldType = fieldDef.getFieldType();
                     if (fieldType.equals(FieldType.PICKLIST)) {
                         final Picklist referencedPicklist = picklistItemService.getPicklist(fieldDef.getFieldName());
                         if (referencedPicklist != null) {
                             final List<Map<String, String>> referencedItemList = new ArrayList<Map<String, String>>();
                             for (PicklistItem referencedItem : referencedPicklist.getActivePicklistItems()) {
                                 final Map<String, String> referencedItemMap = new HashMap<String, String>();
                                 referencedItemMap.put("itemName", referencedItem.getItemName());
                                 referencedItemMap.put("displayVal", referencedItem.getDefaultDisplayValue());
                                 referencedItemList.add(referencedItemMap);
                             }
                             model.put(new StringBuilder(item.getDefaultDisplayValue()).append("-Data").toString(), referencedItemList);
                         }
                     }
                     final Map<String, Object> map = new HashMap<String, Object>();
                     map.put(StringConstants.NAME, item.getDefaultDisplayValue());
                     map.put(StringConstants.DESC, fieldDef.getDefaultLabel());
                     map.put(StringConstants.TYPE, fieldType.name().toLowerCase());
 
                     String updateFieldValue = batch.getUpdateFieldValue(item.getDefaultDisplayValue());
                     map.put(StringConstants.VALUE, updateFieldValue != null ? updateFieldValue : StringConstants.EMPTY);
                     map.put(StringConstants.SELECTED, updateFieldValue != null);
                     returnList.add(map);
                 }
             }
         }
 
         model.put(StringConstants.ROWS, returnList);
         model.put(StringConstants.TOTAL_ROWS, returnList.size());
 
         return model;
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap step5ReviewUpdates(final RequestContext flowRequestContext, final String sort, final String dir,
                                        final String limit, final String start) {
         if (logger.isTraceEnabled()) {
             logger.trace("step5ReviewUpdates: sort = " + sort + " dir = " + dir + " limit = " + limit + " start = " + start);
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final SortInfo sortInfo = new SortInfo(sort, dir, limit, start);
         final HttpServletRequest request = getRequest(flowRequestContext);
         final Map<String, Object> enteredParams = findEnteredParameters(request);
 
         final ModelMap model = new ModelMap();
 
         final Set<Long> segmentationReportIds = batch.getEntrySegmentationIds();
         final List<Map<String, Object>> rowValues = new ArrayList<Map<String, Object>>();
 
         final Map<String, Object> metaDataMap = tangerineListHelper.initMetaData(sortInfo.getStart(),
                 (sortInfo.getLimit() * 2)); // double the rows because of old & new values will be displayed
 
         BeanWrapper bean = createDefaultEntity(batch);
 
         final Map<String, String> sortInfoMap = new HashMap<String, String>();
         if ( ! bean.isReadableProperty(sortInfo.getSort())) {  // If the sort key is not one of the bean's properties, use the ID as default
             sortInfo.setSort(StringConstants.ID);
         }
         sortInfoMap.put(StringConstants.FIELD, sortInfo.getSort());
         sortInfoMap.put(StringConstants.DIRECTION, sortInfo.getDir());
         metaDataMap.put(StringConstants.SORT_INFO, sortInfoMap);
 
         model.put(StringConstants.META_DATA, metaDataMap);
 
         final List<Map<String, Object>> fieldList = new ArrayList<Map<String, Object>>();
         Map<String, Object> fieldMap = new HashMap<String, Object>();
         fieldMap.put(StringConstants.NAME, StringConstants.TYPE);
         fieldMap.put(StringConstants.MAPPING, StringConstants.TYPE);
         fieldMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_STRING);
         fieldMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.TYPE));
         fieldList.add(fieldMap);
 
         fieldMap = new HashMap<String, Object>();
         fieldMap.put(StringConstants.NAME, StringConstants.DISPLAYED_ID);
         fieldMap.put(StringConstants.MAPPING, StringConstants.DISPLAYED_ID);
         fieldMap.put(StringConstants.TYPE, ExtTypeHandler.EXT_INT);
         fieldMap.put(StringConstants.HEADER, TangerineMessageAccessor.getMessage(StringConstants.ID));
         fieldList.add(fieldMap);
 
         batch.clearUpdateFields();
         // the enteredParam.key/defaultDisplayValue will be the fieldDefinitionId like 'adjustedGift.status' which we need to resolve to the fieldName like 'adjustedGift.adjustedStatus'
         for (String thisKey : enteredParams.keySet()) {
             String fieldDefinitionId = new StringBuilder(batch.getBatchType()).append(".").append(thisKey).toString();
             FieldDefinition fieldDef = fieldService.readFieldDefinition(fieldDefinitionId);
             if (fieldDef != null && bean.isReadableProperty(fieldDef.getFieldName())) {
                 fieldMap = new HashMap<String, Object>();
 
                 // the updateField key will be the fieldDefinitionId (adjustedGift.status) which will need to be resolved to the fieldName (adjustedGift.adjustedStatus)
                 batch.addUpdateField(thisKey, enteredParams.get(thisKey) == null ? null : enteredParams.get(thisKey).toString()); // update the batch
 
                 String escapedFieldName = TangerineForm.escapeFieldName(fieldDef.getFieldName());
                 fieldMap.put(StringConstants.NAME, escapedFieldName);
                 fieldMap.put(StringConstants.MAPPING, escapedFieldName);
 
                 String propertyName = fieldDef.getFieldName();
                 if (bean.getPropertyValue(propertyName) instanceof CustomField) {
                     propertyName += StringConstants.DOT_VALUE;
                 }
                 String extType = ExtTypeHandler.findExtType(bean.getPropertyType(propertyName));
                 fieldMap.put(StringConstants.TYPE, extType);
                 fieldMap.put(StringConstants.HEADER, fieldDef.getDefaultLabel());
 
                 if (ExtTypeHandler.EXT_DATE.equals(extType)) {
                     String format;
 //                        if (FieldType.CC_EXPIRATION.equals(fieldDef.getFieldType()) || FieldType.CC_EXPIRATION_DISPLAY.equals(fieldDef.getFieldType())) {
 //                            format = "Y-m-d"; // TODO: put back CC?
 //                        }
 //                        else {
                         format = "Y-m-d H:i:s";
 //                        }
                     fieldMap.put(StringConstants.DATE_FORMAT, format);
                 }
                 fieldList.add(fieldMap);
             }
         }
         metaDataMap.put(StringConstants.FIELDS, fieldList);
 
         List rows = null;
         if (StringConstants.GIFT.equals(batch.getBatchType())) {
             rows = giftService.readGiftsBySegmentationReportIds(segmentationReportIds, sortInfo, request.getLocale());
         }
         else if (StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
             rows = adjustedGiftService.readAdjustedGiftsBySegmentationReportIds(segmentationReportIds, sortInfo, request.getLocale());
         }
         contrastUpdatedValues(batch, rows, rowValues, enteredParams);
 
         model.put(StringConstants.ROWS, rowValues);
         model.put(StringConstants.TOTAL_ROWS, rowValues.size());
 
         return model;
     }
 
     private void contrastUpdatedValues(final PostBatch batch, final List rows,
                                        final List<Map<String, Object>> rowValues,
                                        final Map<String, Object> enteredParams) {
         if (rows != null) {
             for (Object thisRow : rows) {
                 BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(thisRow);
                 final Map<String, Object> oldRowMap = new HashMap<String, Object>();
                 final Map<String, Object> newRowMap = new HashMap<String, Object>();
                 oldRowMap.put(StringConstants.TYPE, TangerineMessageAccessor.getMessage("before"));
                 newRowMap.put(StringConstants.TYPE, TangerineMessageAccessor.getMessage("after"));
                 oldRowMap.put(StringConstants.ID, "old-" + bw.getPropertyValue(StringConstants.ID));
                 newRowMap.put(StringConstants.ID, "new-" + bw.getPropertyValue(StringConstants.ID));
 
                 oldRowMap.put(StringConstants.DISPLAYED_ID, bw.getPropertyValue(StringConstants.ID));
                 newRowMap.put(StringConstants.DISPLAYED_ID, bw.getPropertyValue(StringConstants.ID));
 
                 for (Map.Entry<String, Object> paramEntry : enteredParams.entrySet()) {
                     String key = paramEntry.getKey();
                     // the batchType + key is the fieldDefinitionId; we need to resolve the fieldName
                     String fieldDefinitionId = new StringBuilder(batch.getBatchType()).append(".").append(key).toString();
                     FieldDefinition fieldDef = fieldService.readFieldDefinition(fieldDefinitionId);
                    if (fieldDef != null) {
                        String fieldName = fieldDef.getFieldName();
                        if (bw.isReadableProperty(fieldName)) {
                            String propertyName = fieldName;
                            if (bw.getPropertyValue(propertyName) instanceof CustomField) {
                                propertyName += StringConstants.DOT_VALUE;
                            }
                            String escapedFieldName = TangerineForm.escapeFieldName(fieldName);
                            oldRowMap.put(escapedFieldName, bw.getPropertyValue(propertyName));
                            newRowMap.put(escapedFieldName, paramEntry.getValue());
                         }
                     }
                 }
                 rowValues.add(oldRowMap); // 1 row for the old value
                 rowValues.add(newRowMap); // 1 row for the new value
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap saveBatch(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("saveBatch:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final PostBatch batch = getBatchFromFlowScope(flowRequestContext);
         final PostBatch savedBatch = postBatchService.maintainBatch(batch);
 
         final ModelMap map = new ModelMap();
         map.put(StringConstants.BATCH_ID, savedBatch.getId());
         map.put(StringConstants.SUCCESS, Boolean.TRUE);
         return map;
     }
 
     @SuppressWarnings("unchecked")
     public ModelMap cancelBatch(final RequestContext flowRequestContext) {
         if (logger.isTraceEnabled()) {
             logger.trace("cancelBatch:");
         }
         tangerineListHelper.checkAccess(getRequest(flowRequestContext), PageType.createBatch);
         final ModelMap map = new ModelMap();
         map.put(StringConstants.SUCCESS, Boolean.TRUE);
         return map;
     }
 
     private Map<String, Object> findEnteredParameters(final HttpServletRequest request) {
         final Enumeration paramNames = request.getParameterNames();
         final Map<String, Object> paramMap = new HashMap<String, Object>();
         while (paramNames.hasMoreElements()) {
             String thisParamName = (String) paramNames.nextElement();
             if (thisParamName.startsWith(PARAM_PREFIX)) {
                 String name = thisParamName.replaceFirst(PARAM_PREFIX, StringConstants.EMPTY);
                 if (StringUtils.hasText(name)) {
                     paramMap.put(name, request.getParameter(thisParamName));
                 }
             }
         }
         return paramMap;
     }
 }
