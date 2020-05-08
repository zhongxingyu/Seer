 package com.mpower.dao;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import com.mpower.domain.Gift;
 
 public interface GiftDao {
 
     public Gift maintainGift(Gift gift);
 
     public Gift readGift(Long giftId);
 
     public List<Gift> readGifts(Long personId);
 
    public List<Gift> readGifts(Long personId, Map<String, String> params);
 
     public double analyzeMajorDonor(Long personId, Date beginDate, Date currentDate);
 }
