 package com.orangeleap.tangerine.dao.ibatis;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.ibatis.sqlmap.client.SqlMapClient;
 import com.orangeleap.tangerine.dao.RecurringGiftDao;
 import com.orangeleap.tangerine.dao.util.QueryUtil;
 import com.orangeleap.tangerine.dao.util.search.SearchFieldMapperFactory;
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.type.EntityType;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 @Repository("recurringGiftDAO")
 public class IBatisRecurringGiftDao extends AbstractPaymentInfoEntityDao<RecurringGift> implements RecurringGiftDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Autowired
     public IBatisRecurringGiftDao(SqlMapClient sqlMapClient) {
         super(sqlMapClient);
     }
     
     private void loadCustomFieldsForEntities(RecurringGift recurringGift) {
         if (recurringGift != null) {
             loadDistributionLinesCustomFields(recurringGift);
             if (recurringGift.getId() != null) {
                 recurringGift.setAssociatedGiftIds(readAssociatedGiftIdsForRecurringGift(recurringGift.getId()));
             }
             loadCustomFields(recurringGift.getPerson());
             loadCustomFields(recurringGift.getSelectedAddress());
             loadCustomFields(recurringGift.getSelectedPhone());
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<RecurringGift> readRecurringGifts(Date date, List<String> statuses) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGifts: date = " + date + " statuses = " + statuses);
         }
         Map<String, Object> params = setupParams();
         params.put("date", date);
         params.put("statuses", statuses);
 
         List<RecurringGift> recurringGifts = getSqlMapClientTemplate().queryForList("SELECT_RECURRING_GIFTS_ON_OR_AFTER_DATE", params);
         if (recurringGifts != null) {
             for (RecurringGift recurringGift : recurringGifts) {
                 loadCustomFieldsForEntities(recurringGift);
             }
         }
         return recurringGifts;
     }
 
     @Override
     public RecurringGift maintainRecurringGift(RecurringGift rg) {
         if (logger.isTraceEnabled()) {
             logger.trace("maintainRecurringGift: recurringGiftId = " + rg.getId());
         }
 		RecurringGift aRecurringGift = (RecurringGift)insertOrUpdate(rg, "RECURRING_GIFT");
 		
 		/* Delete DistributionLines first */
         getSqlMapClientTemplate().delete("DELETE_DISTRO_LINE_BY_RECURRING_GIFT_ID", aRecurringGift.getId()); 
         /* Then Insert DistributionLines */
         insertDistributionLines(aRecurringGift, "recurringGiftId");
         return aRecurringGift;
     }
     
     /**
      * Updates the recurringGift AMOUNT_PAID, AMOUNT_REMAINING, and RECURRING_GIFT_STATUS fields ONLY
      * @param recurringGift
      */
     @Override
     public void maintainRecurringGiftAmountPaidRemainingStatus(RecurringGift recurringGift) {
         if (logger.isTraceEnabled()) {
             logger.trace("maintainRecurringGiftAmountPaidRemainingStatus: recurringGiftId = " + recurringGift.getId() + " amountPaid = " + recurringGift.getAmountPaid() + 
                     " amountRemaining = " + recurringGift.getAmountRemaining() + " recurringGiftStatus = " + recurringGift.getRecurringGiftStatus());
         }
         getSqlMapClientTemplate().update("UPDATE_RECURRING_GIFT_AMOUNT_PAID_REMAINING_STATUS", recurringGift);
     }
    
    /**
     * Updates the recurringGift NEXT_RUN_DATE field ONLY
     * @param recurringGift
     */
    @Override
    public void maintainRecurringGiftNextRunDate(RecurringGift recurringGift) {
        if (logger.isTraceEnabled()) {
            logger.trace("maintainRecurringGiftNextRunDate: recurringGiftId = " + recurringGift.getId() + " nextRunDate = " + recurringGift.getNextRunDate());
        }
        getSqlMapClientTemplate().update("UPDATE_RECURRING_GIFT_NEXT_RUN_DATE", recurringGift);
    }
 
     @Override
     public void removeRecurringGift(RecurringGift rg) {
         if (logger.isTraceEnabled()) {
             logger.trace("removeRecurringGift: id = " + rg.getId());
         }
         Map<String, Object> params = setupParams();
         params.put("id", rg.getId());
         int rows = getSqlMapClientTemplate().delete("DELETE_RECURRING_GIFT", params);
         if (logger.isInfoEnabled()) {
             logger.info("removeRecurringGift: number of rows deleted = " + rows);
         }
     }
     
     @Override
     public RecurringGift readRecurringGiftById(Long recurringGiftId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGiftById: recurringGiftId = " + recurringGiftId);
         }
         Map<String, Object> params = setupParams();
         params.put("id", recurringGiftId);
         RecurringGift recurringGift = (RecurringGift) getSqlMapClientTemplate().queryForObject("SELECT_RECURRING_GIFT_BY_ID", params);
         loadCustomFieldsForEntities(recurringGift);
         return recurringGift;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<RecurringGift> readRecurringGiftsByConstituentId(Long constituentId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGiftsByConstituentIdType: constituentId = " + constituentId);
         }
         Map<String, Object> params = setupParams();
         params.put("constituentId", constituentId);
         return getSqlMapClientTemplate().queryForList("SELECT_RECURRING_GIFTS_BY_CONSTITUENT_ID", params);
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public PaginatedResult readPaginatedRecurringGiftsByConstituentId(Long constituentId, SortInfo sortinfo) {
         if (logger.isTraceEnabled()) {
             logger.trace("readPaginatedRecurringGiftsByConstituentId: constituentId = " + constituentId);
         }
         Map<String, Object> params = setupParams();
         sortinfo.addParams(params);
 
 		params.put("constituentId", constituentId);
 
         List rows = getSqlMapClientTemplate().queryForList("SELECT_RECURRING_GIFTS_BY_CONSTITUENT_ID_PAGINATED", params);
         Long count = (Long)getSqlMapClientTemplate().queryForObject("RECURRING_GIFTS_BY_CONSTITUENT_ID_ROWCOUNT",params);
         PaginatedResult resp = new PaginatedResult();
         resp.setRows(rows);
         resp.setRowCount(count);
         return resp;
     }
 
 
     @SuppressWarnings("unchecked")
     @Override
     public List<RecurringGift> searchRecurringGifts(Map<String, Object> searchParams) {
         if (logger.isTraceEnabled()) {
             logger.trace("searchRecurringGifts: searchParams = " + searchParams);
         }
         Map<String, Object> params = setupParams();
         QueryUtil.translateSearchParamsToIBatisParams(searchParams, params, new SearchFieldMapperFactory().getMapper(EntityType.recurringGift).getMap());
 
         return getSqlMapClientTemplate().queryForList("SELECT_RECURRING_GIFT_BY_SEARCH_TERMS", params);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<DistributionLine> findDistributionLinesForRecurringGifts(List<String> recurringGiftIds) {
         if (logger.isTraceEnabled()) {
             logger.trace("findDistributionLinesForRecurringGifts: recurringGiftIds = " + recurringGiftIds);
         }
         Map<String, Object> params = setupParams();
         params.put("recurringGiftIds", recurringGiftIds);
         return getSqlMapClientTemplate().queryForList("SELECT_DISTRO_LINE_BY_RECURRING_GIFT_ID", params);
     }
     
     @SuppressWarnings("unchecked")
     protected List<Long> readAssociatedGiftIdsForRecurringGift(Long recurringGiftId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readAssociatedGiftIdsForRecurringGift: recurringGiftId = " + recurringGiftId);
         }
         Map<String, Object> paramMap = setupParams();
         paramMap.put("recurringGiftId", recurringGiftId);
         return getSqlMapClientTemplate().queryForList("SELECT_RECURRING_GIFT_GIFT_BY_RECURRING_GIFT_ID", paramMap);
     }
     
     @Override
     public BigDecimal readAmountPaidForRecurringGiftId(Long recurringGiftId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readAmountPaidForRecurringGiftId: recurringGiftId = " + recurringGiftId);
         }
         Map<String, Object> paramMap = setupParams();
         paramMap.put("recurringGiftId", recurringGiftId);
         return (BigDecimal) getSqlMapClientTemplate().queryForObject("SELECT_AMOUNT_PAID_BY_RECURRING_GIFT_ID", paramMap);
     }
 }
