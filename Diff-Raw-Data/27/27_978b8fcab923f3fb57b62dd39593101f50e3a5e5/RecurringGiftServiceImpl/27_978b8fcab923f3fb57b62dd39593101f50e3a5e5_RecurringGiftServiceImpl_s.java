 package com.orangeleap.tangerine.service.impl;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.DateMidnight;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StringUtils;
 
 import com.orangeleap.tangerine.dao.RecurringGiftDao;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.paymentInfo.Commitment;
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.RecurringGiftService;
 import com.orangeleap.tangerine.type.EntityType;
 import com.orangeleap.tangerine.web.common.PaginatedResult;
 import com.orangeleap.tangerine.web.common.SortInfo;
 
 @Service("recurringGiftService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class RecurringGiftServiceImpl extends AbstractCommitmentService<RecurringGift> implements RecurringGiftService {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name = "recurringGiftDAO")
     private RecurringGiftDao recurringGiftDao;
 
     @Transactional(propagation = Propagation.REQUIRED)
     @Override
     public List<RecurringGift> readRecurringGiftsByDateStatuses(Date date, List<String> statuses) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGifts: date = " + date + " statuses = " + statuses);
         }
         return recurringGiftDao.readRecurringGifts(date, statuses);
     }
 
     @Override
     public RecurringGift readRecurringGiftById(Long recurringGiftId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGiftById: recurringGiftId = " + recurringGiftId);
         }
         return recurringGiftDao.readRecurringGiftById(recurringGiftId);
     }
     
     @Override
     public RecurringGift readRecurringGiftByIdCreateIfNull(String recurringGiftId, Person constituent) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGiftByIdCreateIfNull: recurringGiftId = " + recurringGiftId + " constituentId = " + (constituent == null ? null : constituent.getId()));
         }
         RecurringGift recurringGift = null;
         if (recurringGiftId == null) {
             if (constituent != null) {
                 recurringGift = this.createDefaultRecurringGift(constituent);
             }
         } 
         else {
             recurringGift = this.readRecurringGiftById(Long.valueOf(recurringGiftId));
         }
         return recurringGift;
     }
     
     @Override
     public RecurringGift createDefaultRecurringGift(Person constituent) {
         if (logger.isTraceEnabled()) {
             logger.trace("createDefaultRecurringGift: constituent = " + (constituent == null ? null : constituent.getId()));
         }
         RecurringGift recurringGift = new RecurringGift();
         createDefault(constituent, recurringGift, EntityType.recurringGift, "recurringGiftId");
 
         return recurringGift;
     }
     
     @Transactional(propagation = Propagation.REQUIRED)
     @Override
     public RecurringGift maintainRecurringGift(RecurringGift recurringGift) {
         if (logger.isTraceEnabled()) {
             logger.trace("maintainRecurringGift: recurringGift = " + recurringGift);
         }
 
         recurringGift.setAutoPay(true);
        recurringGift.setNextRunDate(getNextGiftDate(recurringGift));
 
         recurringGift.filterValidDistributionLines();
         return save(recurringGift);
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED)
     public RecurringGift editRecurringGift(RecurringGift recurringGift) {
         if (logger.isTraceEnabled()) {
             logger.trace("editRecurringGift: recurringGiftId = " + recurringGift.getId());
         }
         return save(recurringGift);
     }
     
     private RecurringGift save(RecurringGift recurringGift) {
         maintainEntityChildren(recurringGift, recurringGift.getPerson());
         recurringGift = recurringGiftDao.maintainRecurringGift(recurringGift);
         auditService.auditObject(recurringGift, recurringGift.getPerson());
         return recurringGift;
     }
     
     @Override
     public List<RecurringGift> readRecurringGiftsForConstituent(Person constituent) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGifts: constituent = " + constituent);
         }
         return readRecurringGiftsForConstituent(constituent.getId());
     }
 
     @Override
     public List<RecurringGift> readRecurringGiftsForConstituent(Long constituentId) {
         if (logger.isTraceEnabled()) {
             logger.trace("readRecurringGifts: constituentId = " + constituentId);
         }
         return recurringGiftDao.readRecurringGiftsByConstituentId(constituentId);
     }
     
     @Override
     public PaginatedResult readPaginatedRecurringGiftsByConstituentId(Long constituentId, SortInfo sortinfo) {
         if (logger.isTraceEnabled()) {
             logger.trace("readPaginatedRecurringGiftsByConstituentId: constituentId = " + constituentId);
         }
         return recurringGiftDao.readPaginatedRecurringGiftsByConstituentId(constituentId, sortinfo);
     }
 
     @Override
     public List<RecurringGift> searchRecurringGifts(Map<String, Object> params) {
         if (logger.isTraceEnabled()) {
             logger.trace("searchRecurringGifts: params = " + params);
         }
         return recurringGiftDao.searchRecurringGifts(params);
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public Map<String, List<RecurringGift>> findGiftAppliableRecurringGiftsForConstituent(Long constituentId, String selectedRecurringGiftIds) {
         if (logger.isTraceEnabled()) {
             logger.trace("findGiftAppliableRecurringGiftsForConstituent: constituentId = " + constituentId + " selectedRecurringGiftIds = " + selectedRecurringGiftIds);
         }
         List<RecurringGift> rGifts = recurringGiftDao.readRecurringGiftsByConstituentId(constituentId);
         
         Set<String> selectedRecurringGiftIdsSet = StringUtils.commaDelimitedListToSet(selectedRecurringGiftIds);
         List<RecurringGift> notSelectedRecurringGifts = filterApplicableRecurringGiftsForConstituent(rGifts, Calendar.getInstance().getTime());
         List<RecurringGift> selectedRecurringGifts = new ArrayList<RecurringGift>();
         
         if (selectedRecurringGiftIdsSet.isEmpty() == false) {
             for (Iterator<RecurringGift> iter = notSelectedRecurringGifts.iterator(); iter.hasNext();) {
                 RecurringGift aRecurringGift = iter.next();
                 if (selectedRecurringGiftIdsSet.contains(aRecurringGift.getId().toString())) {
                     selectedRecurringGifts.add(aRecurringGift);
                     iter.remove();
                 }
             }
         }
         Map<String, List<RecurringGift>> recurringGiftMap = new HashMap<String, List<RecurringGift>>();
         recurringGiftMap.put("selectedRecurringGifts", selectedRecurringGifts);
         recurringGiftMap.put("notSelectedRecurringGifts", notSelectedRecurringGifts);
         return recurringGiftMap;
     }
     
     @Override
     public List<RecurringGift> filterApplicableRecurringGiftsForConstituent(List<RecurringGift> gifts, Date nowDt) {
         DateMidnight now = new DateMidnight(nowDt);
         for (Iterator<RecurringGift> recIter = gifts.iterator(); recIter.hasNext();) {
             RecurringGift recurringGift = recIter.next();
             if (Commitment.STATUS_EXPIRED.equals(recurringGift.getRecurringGiftStatus()) || Commitment.STATUS_CANCELLED.equals(recurringGift.getRecurringGiftStatus())) {
                 recIter.remove();
             }
             else {
                 DateMidnight startDt = new DateMidnight(recurringGift.getStartDate());
                 if (startDt.isAfter(now)) {
                     recIter.remove();
                 }
                 else if (recurringGift.getEndDate() != null) {
                     if (new DateMidnight(recurringGift.getEndDate()).isBefore(now)) {
                         recIter.remove();
                     }
                 }
             }
         }
         return gifts;
     }
     
     @Override
     public List<DistributionLine> findDistributionLinesForRecurringGifts(Set<String> recurringGiftIds) {
         if (logger.isTraceEnabled()) {
             logger.trace("findDistributionLinesForRecurringGifts: recurringGiftIds = " + recurringGiftIds);
         }
         if (recurringGiftIds != null && recurringGiftIds.isEmpty() == false) {
             return recurringGiftDao.findDistributionLinesForRecurringGifts(new ArrayList<String>(recurringGiftIds));
         }
         return null;
     }
     
     @Override
     public boolean canApplyPayment(RecurringGift recurringGift) {
         if (logger.isTraceEnabled()) {
             logger.trace("canApplyPayment: recurringGift.id = " + recurringGift.getId() + " status = " + recurringGift.getRecurringGiftStatus());
         }
         List<RecurringGift> rGifts = new ArrayList<RecurringGift>(1);
         rGifts.add(recurringGift);
         return recurringGift.getId() != null && recurringGift.getId() > 0 && filterApplicableRecurringGiftsForConstituent(rGifts, Calendar.getInstance().getTime()).size() == 1;
     }
 
     @Transactional(propagation = Propagation.REQUIRED)
     @Override
     // TODO: fix IBatisRecurringDao Status, map to RecurringStatus, fix in general
     public void processRecurringGifts() {
         if (logger.isTraceEnabled()) {
             logger.trace("processRecurringGifts:");
         }
         List<RecurringGift> recurringGifts = recurringGiftDao.readRecurringGifts(Calendar.getInstance().getTime(), Arrays.asList(new String[] { Commitment.STATUS_ACTIVE /*, Commitment.STATUS_FULFILLED*/ }));
         if (recurringGifts != null) {
             for (RecurringGift recurringGift : recurringGifts) {
                 logger.debug("processRecurringGifts: id =" + recurringGift.getId() + ", nextRun =" + recurringGift.getNextRunDate());
                 Date nextDate = null;
                 if (recurringGift.getEndDate() == null || recurringGift.getEndDate().after(getToday().getTime())) {
                     createAutoGift(recurringGift);
                     nextDate = getNextGiftDate(recurringGift);
                     if (nextDate != null) {
 
                         recurringGift.setNextRunDate(nextDate);
                         recurringGiftDao.maintainRecurringGift(recurringGift);
                     }
                 }
                 // TODO: is this necessary?
 //                if (nextDate == null) {
 //                    recurringGiftDao.removeRecurringGift(recurringGift);
 //                    recurringGift = null;
 //                }
             }
         }
     }
 }
