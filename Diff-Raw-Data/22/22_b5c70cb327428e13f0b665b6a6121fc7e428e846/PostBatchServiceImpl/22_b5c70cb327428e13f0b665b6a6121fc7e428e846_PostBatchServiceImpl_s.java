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
 
 package com.orangeleap.tangerine.service.impl;
 
 import com.orangeleap.tangerine.dao.JournalDao;
 import com.orangeleap.tangerine.dao.PostBatchDao;
 import com.orangeleap.tangerine.domain.AbstractCustomizableEntity;
 import com.orangeleap.tangerine.domain.Journal;
 import com.orangeleap.tangerine.domain.PostBatch;
 import com.orangeleap.tangerine.domain.PostBatchEntry;
 import com.orangeleap.tangerine.domain.Postable;
 import com.orangeleap.tangerine.domain.Segmentation;
 import com.orangeleap.tangerine.domain.customization.Picklist;
 import com.orangeleap.tangerine.domain.customization.PicklistItem;
 import com.orangeleap.tangerine.domain.paymentInfo.AbstractPaymentInfoEntity;
 import com.orangeleap.tangerine.domain.paymentInfo.AdjustedGift;
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.service.AdjustedGiftService;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.service.PicklistItemService;
 import com.orangeleap.tangerine.service.PostBatchService;
 import com.orangeleap.tangerine.service.SiteService;
 import com.orangeleap.tangerine.service.customization.FieldService;
 import com.orangeleap.tangerine.service.customization.PageCustomizationService;
 import com.orangeleap.tangerine.util.OLLogger;
 import com.orangeleap.tangerine.util.StringConstants;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 import com.orangeleap.tangerine.web.common.SortInfo;
 import com.orangeleap.theguru.client.GetSegmentationListByTypeRequest;
 import com.orangeleap.theguru.client.GetSegmentationListByTypeResponse;
 import com.orangeleap.theguru.client.ObjectFactory;
 import com.orangeleap.theguru.client.Theguru;
 import com.orangeleap.theguru.client.WSClient;
 import org.apache.commons.logging.Log;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindException;
 
 import javax.annotation.Resource;
 import java.math.BigDecimal;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 @Service("postBatchService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class PostBatchServiceImpl extends AbstractTangerineService implements PostBatchService {
 
     /**
      * Logger for this class and subclasses
      */
     protected final Log logger = OLLogger.getLog(getClass());
 
     @Resource(name = "postBatchDAO")
     private PostBatchDao postBatchDao;
 
     @Resource(name = "giftService")
     private GiftService giftService;
 
     @Resource(name = "adjustedGiftService")
     private AdjustedGiftService adjustedGiftService;
 
     @Resource(name = "siteService")
     private SiteService siteService;
 
     @Resource(name = "journalDAO")
     private JournalDao journalDao;
 
     @Resource(name = "picklistItemService")
     private PicklistItemService picklistItemService;
 
     @Resource(name = "pageCustomizationService")
     protected PageCustomizationService pageCustomizationService;
 
     @Resource(name = "fieldService")
     private FieldService fieldService;
 
     @Resource(name = "tangerineUserHelper")
     protected TangerineUserHelper tangerineUserHelper;
 
     public final static String DEBIT = "debit";
     public final static String CREDIT = "credit";
     public final static String BANK = "bank";
     public final static String ADJUSTED_GIFT = "adjustedgift";
     public final static String DISTRO_LINE = "distributionline";
     public final static String DEFAULT = "_default";
 
     public final static String SOURCE = "source";
     public final static String STATUS = "status";
     public final static String IDS = "ids";
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public PostBatch maintainBatch(PostBatch batch) {
         if (logger.isTraceEnabled()) {
             logger.trace("maintainBatch: batchId = " + batch.getId());
         }
         batch.setBatchCreatedById(tangerineUserHelper.lookupUserId());
         batch.setBatchCreatedDate(new Date());
         return postBatchDao.maintainPostBatch(batch);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public void deleteBatch(PostBatch batch) {
         if (logger.isTraceEnabled()) {
             logger.trace("deleteBatch: batchId = " + batch.getId());
         }
         if (batch.isExecuted()) {
             throw new RuntimeException("Cannot delete a batch that has already been executed.");
         }
         postBatchDao.deletePostBatch(batch);
     }
 
     @Override
     public List<PostBatch> readBatches(boolean showRanBatches, SortInfo sort, Locale locale) {
         if (logger.isTraceEnabled()) {
             logger.trace("readBatches: showRanBatches = " + showRanBatches + " sort = " + sort);
         }
         return postBatchDao.readBatches(showRanBatches, sort.getSort(), sort.getDir(), sort.getStart(),
                 sort.getLimit(), locale);
     }
 
     @Override
     public PostBatch readBatch(Long batchId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readBatch: batchId = " + batchId);
         }
         return postBatchDao.readPostBatchById(batchId);
     }
 
     @Override
     public PostBatch readBatchCreateIfNull(final Long batchId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readBatchCreateIfNull: batchId = " + batchId);
         }
 
         PostBatch batch;
         if (batchId != null && batchId > 0) {
             batch = readBatch(batchId);
         } else {
             batch = new PostBatch();
         }
         return batch;
     }
 
     @Override
     public List<Map<String, Object>> findSegmentationsForBatchType(PostBatch batch, Set<Long> pickedSegmentationIds, String batchType, String sortField,
                                                                    String sortDirection, int startIndex, int resultCount) {
         if (logger.isTraceEnabled()) {
             logger.trace("findSegmentationsForBatchType: batchType = " + batchType + " sortField = " + sortField +
                     " sortDirection = " + sortDirection + " startIndex = " + startIndex + " resultCount = " + resultCount);
         }
         final List<Segmentation> segmentations = findSegmentations(batchType, sortField, sortDirection, startIndex, resultCount);
         List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
         if (segmentations != null) {
             final SimpleDateFormat sdf = new SimpleDateFormat(StringConstants.EXT_DATE_FORMAT);
             for (Segmentation segmentation : segmentations) {
                 Map<String, Object> returnMap = new HashMap<String, Object>();
                 returnMap.put(StringConstants.ID, segmentation.getId());
                 returnMap.put(StringConstants.NAME, segmentation.getName());
                 returnMap.put("desc", segmentation.getDescription());
                 returnMap.put("count", segmentation.getCount());
                 returnMap.put("lastDt", segmentation.getLastRunDate() == null ? null : sdf.format(segmentation.getLastRunDate()));
                 returnMap.put("lastUser", segmentation.getLastRunByUser());
                 returnMap.put("picked", pickedSegmentationIds.contains(segmentation.getId()));
                 returnList.add(returnMap);
             }
         }
         return returnList;
     }
 
     @Override
     public int findTotalSegmentations(String batchType) {
         if (logger.isTraceEnabled()) {
             logger.trace("findTotalSegmentations: batchType = " + batchType);
         }
         return 2;// TODO: fix
     }
 
     @Override
     public List<Segmentation> findSegmentations(String batchType, String sortField, String sortDirection, int startIndex, int resultCount) {
         if (logger.isTraceEnabled()) {
             logger.trace("findSegmentations: batchType = " + batchType + " sortField = " + sortField +
                     " sortDirection = " + sortDirection + " startIndex = " + startIndex + " resultCount = " + resultCount);
         }
         Theguru theGuru = new WSClient().getTheGuru();
         ObjectFactory objFactory = new ObjectFactory();
         GetSegmentationListByTypeRequest req = objFactory.createGetSegmentationListByTypeRequest();
 
         List<Segmentation> returnSegmentations = new ArrayList<Segmentation>();
         String resolvedType = null;
         if (StringConstants.GIFT.equals(batchType)) {
             resolvedType = StringConstants.GIFT_SEGMENTATION;
         }
         if (resolvedType != null) {
             req.setType(resolvedType);
             req.setSortField(sortField);
             req.setSortDirection(sortDirection);
             req.setStartIndex(startIndex);
             req.setResultCount(resultCount);
 
             GetSegmentationListByTypeResponse resp = theGuru.getSegmentationListByType(req);
             if (resp != null) {
                 List<com.orangeleap.theguru.client.Segmentation> wsSegmentations = resp.getSegmentation();
                 if (wsSegmentations != null) {
                     for (com.orangeleap.theguru.client.Segmentation wsSegmentation : wsSegmentations) {
                         if (wsSegmentation != null) {
                             Segmentation segmentation = new Segmentation(wsSegmentation.getId(), wsSegmentation.getName(),
                                     wsSegmentation.getDescription(), wsSegmentation.getExecutionCount(),
                                     wsSegmentation.getExecutionDate() == null ? null : wsSegmentation.getExecutionDate().toGregorianCalendar().getTime(),
                                     wsSegmentation.getExecutionUser());
                             returnSegmentations.add(segmentation);
                         }
                     }
                 }
             }
         }
         return returnSegmentations;
     }
 
 
     /**
      * ************* the following will be removed ***************
      */
 //    @Override
 //    public Map<String, String> readAllowedGiftSelectFields() {
 //
 //    	Map<String, String> map = new TreeMap<String, String>();
 //        map.put(IDS, "Gift Ref Numbers");
 //        map.put("amountLessThan", "Amount Less Than");
 //        map.put("amountGreaterThan", "Amount Greater Than");
 //        map.put("currencyCode", "Currency Code");
 //        map.put("createdDateBefore", "Created Date Before");
 //        map.put("createdDateAfter", "Created Date After");
 //        map.put("constituentId", "Constituent Id");
 //        map.put(STATUS, "Gift Status");
 //        map.put("paymentType", "Payment Type");
 //        map.put("donationDate", "Donation Date");
 //        map.put("postmarkDate", "Postmark Date");
 //        map.put(SOURCE, "Source");
 //        map.put("designationCode", "Designation Code");
 //        map.put("motivationCode", "Motivation Code");
 //        map.put(POSTED, "Posted");
 //
 //        return map;
 //    }
 
 //    private Map<String, Object> createSearchMap(Map<String, String> map) {
 //
 //    	Map<String, Object> result = new HashMap<String, Object>();
 //        for (Map.Entry<String, String> me : map.entrySet()) {
 //
 //    		String key = me.getKey();
 //            String value = me.getValue();
 //            if (value == null || value.trim().length() == 0) continue;
 //
 //            if (key.equals(POSTED)) {
 //            	boolean posted = false;
 //            	value = value.toLowerCase();
 //            	if (value.equals("true") || value.equals("t") || value.equals("y") || value.equals("yes") || value.equals("1")) {
 //            		posted = true;
 //            	}
 //            	result.put(key, posted);
 //            } else if (key.equals(IDS)) {
 //            	result.put(key, value.split(","));
 //            } else if (key.toLowerCase().contains("date")) {
 //                if (value.length() != PostBatchServiceImpl.DATE_FORMAT.length()) throw new RuntimeException("Invalid Date.");
 //            	DateFormat formatter = new SimpleDateFormat(PostBatchServiceImpl.DATE_FORMAT);
 //            	try {
 //            		Date adate = formatter.parse(value);
 //            		result.put(key, adate);
 //            	} catch (Exception e) {
 //            		throw new RuntimeException("Invalid Date.");
 //            	}
 //            } else if (key.toLowerCase().startsWith("amount")) {
 //            	BigDecimal bd = new BigDecimal(value);
 //        		result.put(key, bd);
 //            } else {
 //                result.put(key, value);
 //            }
 //
 //        }
 //
 //        return result;
 //    }
 
 //    @Override
 //    public Map<String, String> readAllowedGiftUpdateFields() {
 //       Map<String, String> map = new TreeMap<String, String>();
 //       map.put(POSTED_DATE, "Posted Date (Creates Journal Entry)");  // Updating this triggers a post (creates journal entry)
 //       map.put(STATUS, "Status");
 //       map.put(SOURCE, "Source");
 //       return map;
 //    }
 
 //    private void setField(boolean isGift, AbstractPaymentInfoEntity apie, String key, String value) {
 //       if (key.equals(POSTED_DATE)) {
 //           DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
 //           Date postedDate;
 //           try {
 //        	   postedDate = dateFormat.parse(value);
 //           } catch (Exception e) {
 //        	   throw new RuntimeException("Invalid post date: "+value);
 //           }
 //           if (isGift) {
 //               ((Gift)apie).setPostedDate(postedDate);
 //           } else {
 //               ((AdjustedGift)apie).setPostedDate(postedDate);
 //           }
 //       } else if (key.equals(SOURCE)) {
 //    	   apie.setCustomFieldValue(SOURCE, value);
 //       } else {
 //    	   if (isGift) {
 //    		    setGiftField((Gift)apie, key, value);
 //    	   } else {
 //   		    	setAdjustedGiftField((AdjustedGift)apie, key, value);
 //    	   }
 //       }
 //    }
 //
 //    private void setGiftField(Gift gift, String key, String value) {
 //       if (key.equals(STATUS)) {
 //    	   gift.setGiftStatus(value);
 //       } else {
 //    	   throw new RuntimeException("Invalid field "+key);
 //       }
 //    }
 //
 //    private void setAdjustedGiftField(AdjustedGift ag, String key, String value) {
 //        if (key.equals(STATUS)) {
 //     	   ag.setAdjustedStatus(value);
 //        } else {
 //     	   throw new RuntimeException("Invalid field "+key);
 //        }
 //    }
 //
 //    @Override
 //    public List<PostBatch> listBatchs() {
 //        return postBatchDao.listBatches();
 //    }
 
 //    @Override
 //    public PostBatch readBatch(Long batchId) {
 //        logger.debug("readBatch: id = "+batchId);
 //        if (batchId == null) return null;
 //        return postBatchDao.readPostBatchById(batchId);
 //    }
 
 //    // Evaluates criteria to create list of matching gifts (snapshot at this moment in time).
 //    @Override
 //    public List<AbstractPaymentInfoEntity> createBatchSelectionList(PostBatch postbatch) {
 //
 //        boolean isGift = GIFT.equals(postbatch.getBatchType());
 //
 //        postBatchDao.deletePostBatchItems(postbatch.getId());
 //
 ////        Map<String, Object> searchmap = createSearchMap(postbatch.getWhereConditions());
 //
 ////        if (isGift) {
 ////            postBatchDao.insertIntoPostBatchFromGiftSelect(postbatch, searchmap);
 ////        } else {
 ////            postBatchDao.insertIntoPostBatchFromAdjustedGiftSelect(postbatch, searchmap);
 ////        }
 //
 //
 ////        postbatch.setReviewSetGenerated(true);
 ////        postbatch.setBatchCreatedDate(new java.util.Date());
 ////        postbatch.setBatchCreatedById(tangerineUserHelper.lookupUserId());
 ////        postbatch.setReviewSetSize(postBatchDao.getReviewSetSize(postbatch.getId()));
 //        postBatchDao.maintainPostBatch(postbatch);
 //
 //        // Gift list uses json to display a paginated list.
 //        if (isGift) return new ArrayList<AbstractPaymentInfoEntity>();
 //
 //        return getBatchSelectionList(postbatch);
 //    }
     private void saveGift(Gift gift) throws BindException {
         gift.setSuppressValidation(true);
         giftService.editGift(gift);
     }
 
     private void saveAdjustedGift(AdjustedGift adjustedGift) throws BindException {
         adjustedGift.setSuppressValidation(true);
         adjustedGiftService.maintainAdjustedGift(adjustedGift);
     }
 
 //    // Reads previous list of matched gifts. Does not re-evaluate any criteria.
 //    @Override
 //    public List<AbstractPaymentInfoEntity> getBatchSelectionList(PostBatch postbatch) {
 //         List<PostBatchEntry> list = postBatchDao.readPostBatchReviewSetItems(postbatch.getId());
 //         List<AbstractPaymentInfoEntity> result = new ArrayList<AbstractPaymentInfoEntity>();
 //         boolean isGift = GIFT.equals(postbatch.getBatchType());
 //         for (PostBatchEntry item : list) {
 //             if (isGift) {
 //                 result.add(giftService.readGiftById(item.getSegmentationId()));
 //             } else {
 //                 result.add(adjustedGiftService.readAdjustedGiftById(item.getSegmentationId()));
 //             }
 //         }
 //         return result;
 //    }
 
 //    @Override
 //	public PaginatedResult getBatchSelectionList(long postbatchId, SortInfo sortInfo) {
 //        return postBatchDao.readPostBatchReviewSetItems(postbatchId, sortInfo);
 //	}
 
     // THis is really execute
     // Sets fields on gifts/adjusted gifts in reviewed batch list
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public PostBatch executeBatch(PostBatch batch) {
         if (logger.isTraceEnabled()) {
             logger.trace("executeBatch: batchId = " + batch.getId());
         }
         batch.clearUpdateErrors();
         if (StringConstants.GIFT.equals(batch.getBatchType()) || StringConstants.ADJUSTED_GIFT.equals(batch.getBatchType())) {
             return executePostableBatches(batch);
         }
         else {
             return null; //TODO
         }
     }
 
     public PostBatch executePostableBatches(PostBatch batch) {
         if (logger.isTraceEnabled()) {
             logger.trace("executePostableBatches: batchId = " + batch.getId());
         }
         final DateFormat dateFormat = new SimpleDateFormat(StringConstants.MM_DD_YYYY_FORMAT);
         final String postedDateString = batch.getUpdateFieldValue(StringConstants.POSTED_DATE);
         final boolean doPost = postedDateString != null;
 
         Date postedDate = null;
         final Map<String, String> bankMap = new HashMap<String, String>();
         final Map<String, String> codeMap = new HashMap<String, String>();
 
         if (doPost) {
             try {
                 postedDate = dateFormat.parse(postedDateString);
             }
             catch (Exception e) {
                 throw new RuntimeException("Invalid posted date.");
             }
             createMaps(bankMap, codeMap);
         }
         else {
             // Can't update these fields if not posting.
             batch.removeUpdateField(StringConstants.POSTED_DATE);
         }
 
         // Get list to update
         // TODO process in single transactions and support partial batch updates.
         Set<Long> segmentationIds = batch.getEntrySegmentationIds();
         int total = giftService.readCountGiftsBySegmentationReportIds(segmentationIds);
 //        final List<Gift> gifts = giftService.readGiftsBySegmentationReportIds(segmentationIds);
 
         List<PostBatchEntry> list = postBatchDao.readPostBatchReviewSetItems(batch.getId()); // TODO: grab gifts/adjusted gifts based on segmentations
         if (list.size() > 2000) {
             throw new RuntimeException("PostBatch size limit exceeded.");   // TODO remove size limit when supporting partial updates
         }
         for (PostBatchEntry entry : list) {
             try {
                 processUpdateItem(entry, batch, doPost, codeMap, bankMap);
             }
             catch (Exception e) {
                 String msg = new StringBuilder(entry.getSegmentationId().toString()).append(": ").append(e.getMessage()).toString();
                 logger.error(msg, e);
                 batch.addUpdateError(msg);
             }
         }
 
         if ( ! batch.getUpdateErrors().isEmpty()) {
             // rollback entire batch for now.  otherwise we have to support reprocess partial
             throw new PostBatchUpdateException(batch.getUpdateErrors());
         }
 
         if (doPost) {
             batch.setPosted(true);
             batch.setPostedDate(postedDate);
             batch.setPostedById(tangerineUserHelper.lookupUserId());
         }
 
         batch.setExecuted(true);
         batch.setExecutedDate(new java.util.Date());
         batch.setExecutedById(tangerineUserHelper.lookupUserId());
 
         // Update
         batch = postBatchDao.maintainPostBatch(batch);
         return batch;
     }
 
     private void createMaps(final Map<String, String> bankMap, final Map<String, String> codeMap) {
         Picklist bankCodes = picklistItemService.getPicklist("customFieldMap[bank]");
         addItemsToMap(bankCodes, bankMap);
         if (bankMap.isEmpty()) {
             throw new RuntimeException("Posting bank GL account codes not defined.  Go to Manage Picklist Items and set up Bank and Designation Code customizations for GL Accounts.");
         }
 
         Picklist projectCodes = picklistItemService.getPicklist("projectCode");
         addItemsToMap(projectCodes, codeMap);
         if (codeMap.isEmpty()) {
             throw new RuntimeException("No active designation GL codes defined.");
         }
     }
 
     private void addItemsToMap(Picklist picklist, Map<String, String> map) {
         for (PicklistItem item : picklist.getActivePicklistItems()) {
             if (item.getCustomFieldValue(StringConstants.ACCOUNT_STRING_1) == null ||
                     item.getCustomFieldValue(StringConstants.ACCOUNT_STRING_2) == null ||
                     item.getCustomFieldValue(StringConstants.GL_ACCOUNT_CODE) == null) {
                 continue;
             }
             map.put(getKey(item.getItemName(), StringConstants.ACCOUNT_STRING_1), item.getCustomFieldValue(StringConstants.ACCOUNT_STRING_1));
             map.put(getKey(item.getItemName(), StringConstants.ACCOUNT_STRING_2), item.getCustomFieldValue(StringConstants.ACCOUNT_STRING_2));
             map.put(getKey(item.getItemName(), StringConstants.GL_ACCOUNT_CODE), item.getCustomFieldValue(StringConstants.GL_ACCOUNT_CODE));
         }
         if ( ! picklist.getActivePicklistItems().isEmpty()) {
             PicklistItem defaultItem = picklist.getActivePicklistItems().get(0);
             map.put(getKey(StringConstants.EMPTY, StringConstants.ACCOUNT_STRING_1), defaultItem.getCustomFieldValue(StringConstants.ACCOUNT_STRING_1));
             map.put(getKey(StringConstants.EMPTY, StringConstants.ACCOUNT_STRING_2), defaultItem.getCustomFieldValue(StringConstants.ACCOUNT_STRING_2));
             map.put(getKey(StringConstants.EMPTY, StringConstants.GL_ACCOUNT_CODE), defaultItem.getCustomFieldValue(StringConstants.GL_ACCOUNT_CODE));
             map.put(DEFAULT, defaultItem.getItemName());
         }
     }
 
     private void processUpdateItem(PostBatchEntry entry, PostBatch batch, boolean doPost,
                                    Map<String, String> codeMap, Map<String, String> bankMap) throws Exception {
         boolean isGift = StringConstants.GIFT.equals(batch.getBatchType());
         AbstractPaymentInfoEntity entity;
 
         boolean wasPreviouslyPosted;
         if (isGift) {
             entity = giftService.readGiftById(entry.getSegmentationId());
             wasPreviouslyPosted = ((Gift) entity).isPosted();
         }
         else {
             entity = adjustedGiftService.readAdjustedGiftById(entry.getSegmentationId());
             wasPreviouslyPosted = ((AdjustedGift) entity).isPosted();
         }
 
         // Record previous values for audit trail
         siteService.populateDefaultEntityEditorMaps(entity);
 
         if (doPost) {
             // Don't allow double posting.
             if (wasPreviouslyPosted) {
                 String msg = "Item " + entity.getId() + " previously posted - cannot re-doPost.";
                 throw new RuntimeException(msg);
             }
             ((Postable) entity).setPosted(true);
         }
 
         // Set update values.  
         for (Map.Entry<String, String> me : batch.getUpdateFields().entrySet()) {
             // TODO: use a BeanWrapper here instead
 //        	setField(isGift, entity, me.getKey(), me.getValue());
         }
 
         // Update record.
         if (isGift) {
             Gift gift = (Gift) entity;
             saveGift(gift);
             if (doPost) {
                 createJournalEntries(gift, null, batch, codeMap, bankMap);
             }
         }
         else {
             AdjustedGift adjustedGift = (AdjustedGift) entity;
             saveAdjustedGift(adjustedGift);
             Gift gift = giftService.readGiftById(adjustedGift.getOriginalGiftId());
             if (doPost) {
                 createJournalEntries(gift, adjustedGift, batch, codeMap, bankMap);
             }
         }
     }
 
     public final static class PostBatchUpdateException extends RuntimeException {
         private static final long serialVersionUID = 1L;
         private List<String> errors;
 
         public PostBatchUpdateException(List<String> errors) {
             super("Errors exist in batch: " + errors.get(0));
             this.errors = errors;
         }
 
         public List<String> getErrors() {
             return errors;
         }
     }
 
     private void createJournalEntries(Gift gift, AdjustedGift adjustedGift, PostBatch batch,
                                       Map<String, String> codeMap, Map<String, String> bankMap) {
         createJournalEntry(gift, adjustedGift, null, batch, codeMap, bankMap);
         List<DistributionLine> distributionLines = adjustedGift == null ? gift.getDistributionLines() : adjustedGift.getDistributionLines();
         for (DistributionLine line : distributionLines) {
             createJournalEntry(gift, adjustedGift, line, batch, codeMap, bankMap);
         }
     }
 
     private void createJournalEntry(Gift gift, AdjustedGift adjustedGift, DistributionLine distributionLine,
                                     PostBatch batch, Map<String, String> codeMap, Map<String, String> bankMap) {
         boolean isDebit = gift.getAmount().compareTo(new BigDecimal("0")) >= 0;
         boolean isHeader = distributionLine == null;
         boolean isGift = adjustedGift == null;
 
         Journal journal = new Journal();
         journal.setSiteName(getSiteName());
         journal.setPostedDate(new java.util.Date());
         journal.setPostBatchId(batch.getId());
 
         journal.setDonationDate(gift.getDonationDate());
         journal.setPaymentMethod(gift.getPaymentType());
         if (gift.getPaymentSource() != null) {
             journal.setCcType(gift.getPaymentSource().getCreditCardType());
         }
 
         if (isHeader) {
             // Gift or Adjusted Gift
             if (isGift) {
                 journal.setJeType(isDebit ? DEBIT : CREDIT);
                 journal.setEntity(StringConstants.GIFT);
                 journal.setEntityId(gift.getId());
                 journal.setAmount(gift.getAmount());
                 journal.setCode(getBank(gift, bankMap));
                 journal.setDescription("Gift from " + gift.getConstituent().getRecognitionName()); // TODO: message bundle?
             }
             else {
                 journal.setJeType(isDebit ? CREDIT : DEBIT);
                 journal.setEntity(ADJUSTED_GIFT);
                 journal.setEntityId(adjustedGift.getId());
                 journal.setOrigEntity(StringConstants.GIFT);
                 journal.setOrigEntityId(gift.getId());
                 journal.setAmount(adjustedGift.getAdjustedAmount());
                 journal.setCode(getBank(adjustedGift, bankMap));
                 journal.setAdjustmentDate(adjustedGift.getAdjustedTransactionDate());
                 journal.setDescription("Adjustment associated with gift ID " + gift.getId() + " from " + gift.getConstituent().getRecognitionName()); // TODO: message bundle?
             }
             updateJournalCodes(journal, bankMap, journal.getCode(), batch);
         }
         else {
             // Distribution lines
             journal.setEntity(DISTRO_LINE);
             journal.setEntityId(distributionLine.getId());
 
             journal.setMasterEntity(isGift ? StringConstants.GIFT : ADJUSTED_GIFT);
             journal.setMasterEntityId(isGift ? gift.getId() : adjustedGift.getId());
 
             journal.setAmount(distributionLine.getAmount());
             journal.setCode(getProjectCode(distributionLine, codeMap));
 
             if (isGift) {
                 journal.setJeType(isDebit ? CREDIT : DEBIT);
                 journal.setDescription("Associated with gift ID " + gift.getId() + " from " + gift.getConstituent().getRecognitionName());
             }
             else {
                 journal.setJeType(isDebit ? DEBIT : CREDIT);
                 journal.setDescription("Adjusted gift ID " + adjustedGift.getId() + ", associated with original gift ID " + gift.getId() + " from " + gift.getConstituent().getRecognitionName());
                 journal.setAdjustmentDate(adjustedGift.getAdjustedTransactionDate());
                 journal.setOrigEntity(StringConstants.GIFT);
                 journal.setOrigEntityId(gift.getId());
             }
 
             updateJournalCodes(journal, codeMap, journal.getCode(), batch);
         }
         journalDao.maintainJournal(journal);
     }
 
     private String getBank(AbstractCustomizableEntity entity, Map<String, String> bankMap) {
         String defaultbank = bankMap.get(DEFAULT);
         String bank = entity.getCustomFieldValue(BANK);
         if (bank == null) {
             bank = defaultbank;
         }
         bank = (bank == null ? "" : bank.trim());
         if (bank.equalsIgnoreCase(StringConstants.NONE)) {
             bank = StringConstants.EMPTY;
         }
         return bank;
     }
 
     private String getProjectCode(DistributionLine distributionLine, Map<String, String> codeMap) {
         String defaultcode = codeMap.get(DEFAULT);
         String projectCode = distributionLine.getProjectCode();
         if (projectCode == null) {
             projectCode = defaultcode;
         }
         projectCode = (projectCode == null ? "" : projectCode.trim());
         if (projectCode.equalsIgnoreCase(StringConstants.NONE)) {
             projectCode = "";
         }
         return projectCode;
     }
 
     private void updateJournalCodes(Journal journal, Map<String, String> map, String code, PostBatch batch) {
         String glAccount1 = map.get(getKey(code, StringConstants.ACCOUNT_STRING_1));
         if (glAccount1 == null) {
             batch.getUpdateErrors().add("Invalid AccountString1 for " + code);
         }
         journal.setGlAccount1(glAccount1);
 
         String glAccount2 = map.get(getKey(code, StringConstants.ACCOUNT_STRING_2));
         if (glAccount2 == null) {
             batch.getUpdateErrors().add("Invalid AccountString2 for " + code);
         }
         journal.setGlAccount2(glAccount2);
 
         String glCode = map.get(getKey(code, StringConstants.GL_ACCOUNT_CODE));
         if (glCode == null) {
             batch.getUpdateErrors().add("Invalid GL Code for " + code);
         }
         journal.setGlCode(glCode);
     }
 
     private String getKey(String s1, String s2) {
         return new StringBuilder(s1).append(" : ").append(s2).toString();
     }
 }
