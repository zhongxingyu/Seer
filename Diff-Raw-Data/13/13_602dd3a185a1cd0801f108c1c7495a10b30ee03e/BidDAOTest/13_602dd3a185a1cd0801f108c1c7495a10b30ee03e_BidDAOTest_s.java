 
 package com.kilo.dao;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.junit.Assert;
 import org.junit.Test;
import org.mybatis.spring.MyBatisSystemException;
 import org.springframework.dao.DataIntegrityViolationException;
 
 import com.kilo.dao.mapper.BidDAO;
 import com.kilo.domain.Bid;
 import com.kilo.domain.BidItemCategory;
 import com.kilo.domain.dto.BidItemCategoryAvgBidPrice;
 
 public class BidDAOTest extends BaseDAOTest {
 
     @Resource(name = "bidDAO")
     private BidDAO bidDAO;
 
     @Test
     public void testGetBidsForBidIds() {
         List<Long> bidIds = Arrays.asList(1L, 1L, 2L, 3L, 4L);
         List<Bid> bids = bidDAO.getBidsForBidIds(bidIds);
         Assert.assertNotNull(bids);
         Assert.assertTrue(bids.size() == 4);
     }
 
    @Test(expected = MyBatisSystemException.class)
     public void testGetBidsForUniqueBidIds() {
         Set<Long> bidIds = new HashSet<>(Arrays.asList(1L, 1L, 2L, 3L, 4L));
         List<Bid> bids = bidDAO.getBidsForUniqueBidIds(bidIds);
         Assert.assertNotNull(bids);
         Assert.assertTrue(bids.size() == 4);
     }
 
     @Test
     public void testGetAverageBidPricePerCategoryForBidIds() {
         List<Long> bidIds = Arrays.asList(1L, 2L, 3L, 4L);
         List<BidItemCategoryAvgBidPrice> averageBidPricePerCategoryForBidIds = bidDAO
                 .getAverageBidPricePerCategoryForBidIds(bidIds);
         Assert.assertNotNull(averageBidPricePerCategoryForBidIds);
     }
 
     @Test
     public void testGetAverageBidPricePerCategoryForBidIdsAsMap() {
         List<Long> bidIds = Arrays.asList(1L, 2L, 3L, 4L);
         Map<BidItemCategory, BigDecimal> averageBidPricePerCategoryMap = bidDAO
                 .getAverageBidPricePerCategoryForBidIdsAsMap(bidIds);
         Assert.assertNotNull(averageBidPricePerCategoryMap);
     }
 
     @Test(expected = DataIntegrityViolationException.class)
     public void testGetAverageBidPricePerCategoryForBidIdsFailure() {
         // An huge number of bids
         List<Long> bidIds = new ArrayList<>();
         for (long i = 0; i < 2100; i++) {
             bidIds.add(i);
         }
         List<BidItemCategoryAvgBidPrice> averageBidPricePerCategoryForBidIds = bidDAO
                 .getAverageBidPricePerCategoryForBidIds(bidIds);
         Assert.assertNotNull(averageBidPricePerCategoryForBidIds);
     }
 
     @Test(expected = DataIntegrityViolationException.class)
     public void testGetAverageBidPricePerCategoryForBidIdsAsMapFailure() {
         // An huge number of bids
         List<Long> bidIds = new ArrayList<>();
         for (long i = 0; i < 2100; i++) {
             bidIds.add(i);
         }
         Map<BidItemCategory, BigDecimal> averageBidPricePerCategoryMap = bidDAO
                 .getAverageBidPricePerCategoryForBidIdsAsMap(bidIds);
         Assert.assertNotNull(averageBidPricePerCategoryMap);
     }
 }
