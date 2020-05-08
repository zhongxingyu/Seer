 
 package com.kilo.dao.mapper;
 
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
 
 import com.kilo.domain.Bid;
 import com.kilo.domain.BidItemCategory;
 import com.kilo.domain.dto.BidItemCategoryAvgBidPrice;
 
 public interface BidDAO {
 
     List<Bid> getBidsForBidIds(List<Long> bidIds);
 
    List<Bid> getBidsForUniqueBidIds(@Param("set") Set<Long> bidIds);
 
     List<BidItemCategoryAvgBidPrice> getAverageBidPricePerCategoryForBidIds(
             List<Long> bidIds);
 
     @MapKey("bidItemCategory")
     Map<BidItemCategory, BigDecimal> getAverageBidPricePerCategoryForBidIdsAsMap(
             List<Long> bidIds);
 
 }
