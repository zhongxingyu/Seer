 package com.orangeleap.tangerine.dao;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 public interface PledgeDao {
 
     public Pledge maintainPledge(Pledge pledge);
 
     public Pledge readPledgeById(Long pledgeId);
 
     public List<Pledge> readPledgesByConstituentId(Long constituentId);
 
     public List<Pledge> findNotCancelledPledges(Long constituentId);
 
     public List<DistributionLine> findDistributionLinesForPledges(List<String> pledgeIds);
     
     public List<Pledge> searchPledges(Map<String, Object> params);
 
 	public PaginatedResult readPaginatedPledgesByConstituentId(Long constituentId, SortInfo sortinfo);
 	
 	public BigDecimal readAmountPaidForPledgeId(Long pledgeId);
 	
	public void maintainPledgeAmountPaidRemaining(Pledge pledge);
 }
