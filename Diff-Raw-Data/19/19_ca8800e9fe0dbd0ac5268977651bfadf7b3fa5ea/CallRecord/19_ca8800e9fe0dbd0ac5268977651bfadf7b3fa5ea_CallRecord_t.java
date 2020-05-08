 package com.callcenter.domain;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.Set;
 import javax.persistence.Entity;
 
 import com.callcenter.util.Constants;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Example;
 import org.springframework.roo.addon.entity.RooEntity;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 
 @Entity
 @RooJavaBean
 @RooEntity
 public class CallRecord {
 
     private String callerId;
 
     private String displayInfo;
 
     private Calendar callTime;
 
     private String calledId;
 
     private String targetId;
 
     private String ipAddress;
 
     private String callingPartyName;
 
     private String calledPartyName;
 
     private Boolean outgoing;
 
     private Boolean internal;
 
     private String waveFileName;
 
     public static List<CallRecord> findAllByExample(final CallRecord callRecord) {
         return getSearchCriteria(callRecord).list();
     }
 
     public static List<CallRecord> findAllByExampleAndRestrictions(final CallRecord callRecord,
                                                                    final Restrictions restrictions) {
         final Criteria searchCriteria = getSearchCriteria(callRecord);
         restrictions.applyOn(searchCriteria);
         return searchCriteria.list();
     }
 
     public void prepareValuesForPartialSearch() {
        if(null != ipAddress) setIpAddress(Constants.Query.LIKE_OPERATOR + getIpAddress()
                + Constants.Query.LIKE_OPERATOR);
        if(null != targetId) setTargetId(Constants.Query.LIKE_OPERATOR + getTargetId()
                + Constants.Query.LIKE_OPERATOR);
        if(null != calledId) setCalledId(Constants.Query.LIKE_OPERATOR  + getCalledId()
                + Constants.Query.LIKE_OPERATOR);
        if(null != calledPartyName) setCalledPartyName(Constants.Query.LIKE_OPERATOR + getCalledPartyName()
                + Constants.Query.LIKE_OPERATOR);
        if(null != callerId) setCallerId(Constants.Query.LIKE_OPERATOR + getCallerId()
                + Constants.Query.LIKE_OPERATOR);
        if(null != callingPartyName) setCallingPartyName(Constants.Query.LIKE_OPERATOR + getCallingPartyName()
                + Constants.Query.LIKE_OPERATOR);
     }
 
    private static Criteria getSearchCriteria(final CallRecord callRecord) {
         final Session session = (Session)entityManager().getDelegate();
         return session.createCriteria(CallRecord.class).add(Example.create(callRecord).enableLike().ignoreCase());
     }
 }
