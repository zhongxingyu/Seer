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
 import com.orangeleap.tangerine.dao.GiftDao;
 import com.orangeleap.tangerine.dao.util.QueryUtil;
 import com.orangeleap.tangerine.dao.util.search.SearchFieldMapperFactory;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.type.EntityType;
 
 @Repository("giftDAO")
 public class IBatisGiftDao extends AbstractPaymentInfoEntityDao<Gift> implements GiftDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Autowired
     public IBatisGiftDao(SqlMapClient sqlMapClient) {
         super(sqlMapClient);
     }
 
     @Override
     public Gift maintainGift(final Gift gift) {
         if (logger.isDebugEnabled()) {
             logger.debug("maintainGift: gift = " + gift);
         }
         Gift aGift = (Gift)insertOrUpdate(gift, "GIFT");
         
         /* Delete DistributionLines first */
         getSqlMapClientTemplate().delete("DELETE_DISTRO_LINE_BY_GIFT_ID", aGift.getId());
         /* Then insert DistributionLines */
         insertDistributionLines(aGift, "giftId");
         return aGift;
     }
 
     @Override
     public Gift readGiftById(Long giftId) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGiftById: giftId = " + giftId);
         }
         Map<String, Object> params = setupParams();
         params.put("id", giftId);
         Gift gift = (Gift)getSqlMapClientTemplate().queryForObject("SELECT_GIFT_BY_ID", params);
         
         loadDistributionLinesCustomFields(gift);
        loadCustomFields(gift.getPerson());
         return gift;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Gift> readMonetaryGiftsByConstituentId(Long constituentId) {
         if (logger.isDebugEnabled()) {
             logger.debug("readMonetaryGiftsByConstituentId: constituentId = " + constituentId);
         }
         Map<String, Object> params = setupParams();
         params.put("constituentId", constituentId);
         return getSqlMapClientTemplate().queryForList("SELECT_GIFTS_BY_CONSTITUENT_ID", params);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Gift> searchGifts(Map<String, Object> searchparams) {
     	Map<String, Object> params = setupParams();
     	QueryUtil.translateSearchParamsToIBatisParams(searchparams, params, new SearchFieldMapperFactory().getMapper(EntityType.gift).getMap());
     	
     	List<Gift> gifts = getSqlMapClientTemplate().queryForList("SELECT_GIFT_BY_SEARCH_TERMS", params);
     	return gifts;
     }
     
 
     @Override
     public double analyzeMajorDonor(Long constituentId, Date beginDate, Date currentDate) {
         if (logger.isDebugEnabled()) {
             logger.debug("analyzeMajorDonor: constituentId = " + constituentId + " beginDate = " + beginDate + " currentDate = " + currentDate);
         }
         Map<String, Object> params = setupParams();
         params.put("constituentId", constituentId);
         params.put("beginDate", beginDate);
         params.put("currentDate", currentDate);
         BigDecimal bd = (BigDecimal)getSqlMapClientTemplate().queryForObject("ANALYZE_FOR_MAJOR_DONOR", params);
         if (bd == null) {
             return 0.00d;
         }
         return bd.doubleValue();
     }
     
 	@SuppressWarnings("unchecked")
     @Override
     public List<Person> analyzeLapsedDonor(Date beginDate, Date currentDate) {
         if (logger.isDebugEnabled()) {
             logger.debug("analyzeLapsedDonor:  beginDate = " + beginDate + " currentDate = " + currentDate);
         }
         Map<String, Object> params = setupParams();
         params.put("beginDate", beginDate);
         params.put("currentDate", currentDate);
         return getSqlMapClientTemplate().queryForList("ANALYZE_FOR_LAPSED_DONOR", params);
     }
 
     @SuppressWarnings("unchecked")
 	@Override
 	public List<Gift> readAllGiftsBySite() {
         if (logger.isDebugEnabled()) {
             logger.debug("readAllGiftsBySite:");
         }
         Map<String, Object> params = setupParams();
         return getSqlMapClientTemplate().queryForList("SELECT_ALL_GIFTS_BY_SITE", params);
 	}
     
     @SuppressWarnings("unchecked")
 	@Override
 	public List<Gift> readAllGiftsByDateRange(Date fromDate, Date toDate) {
         if (logger.isDebugEnabled()) {
             logger.debug("readAllGiftsByDateRange:");
         }
         Map<String, Object> params = setupParams();
         params.put("fromDate", fromDate);
         params.put("toDate", toDate);
         return getSqlMapClientTemplate().queryForList("SELECT_ALL_GIFTS_BY_DATE_RANGE", params);
 	}
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Gift> readGiftsByCommitmentId(Long commitmentId) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGiftsByCommitmentId: commitmentId = " + commitmentId);
         }
         Map<String, Object> params = setupParams();
         params.put("commitmentId", commitmentId);
         return getSqlMapClientTemplate().queryForList("SELECT_GIFTS_BY_COMMITMENT_ID", params);
     }
 
     @Override
     public BigDecimal readGiftsReceivedSumByCommitmentId(Long commitmentId) {
         if (logger.isDebugEnabled()) {
             logger.debug("readGiftsReceivedSumByCommitmentId: commitmentId = " + commitmentId);
         }
         Map<String, Object> params = setupParams();
         params.put("commitmentId", commitmentId);
         BigDecimal result = (BigDecimal) getSqlMapClientTemplate().queryForObject("READ_GIFTS_RECEIVED_SUM_BY_COMMITMENT_ID", params);
         if (result == null) {
             result = BigDecimal.ZERO;
         }
         return result;
     }
 }
