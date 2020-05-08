 package com.orangeleap.tangerine.dao;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 public interface RecurringGiftDao {
 
     public RecurringGift readRecurringGiftById(Long recurringGiftId);
     
     public List<RecurringGift> readRecurringGiftsByConstituentId(Long constituentId);
     
     public List<RecurringGift> readRecurringGifts(Date date, List<String> statuses);
 
     public RecurringGift maintainRecurringGift(RecurringGift rg);
     
     public void maintainRecurringGiftAmountPaidRemainingStatus(RecurringGift recurringGift);
 
     public void removeRecurringGift(RecurringGift rg);
     
     public List<RecurringGift> searchRecurringGifts(Map<String, Object> searchParams);
 
 	public PaginatedResult readPaginatedRecurringGiftsByConstituentId(Long constituentId, SortInfo sortinfo);
 	
 	public List<DistributionLine> findDistributionLinesForRecurringGifts(List<String> recurringGiftIds);
 	
 	public BigDecimal readAmountPaidForRecurringGiftId(Long recurringGiftId);

	void maintainRecurringGiftNextRunDate(RecurringGift recurringGift);
 }
