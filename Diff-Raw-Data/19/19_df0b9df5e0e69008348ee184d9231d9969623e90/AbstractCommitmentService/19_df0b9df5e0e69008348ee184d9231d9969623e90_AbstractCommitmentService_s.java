 package com.orangeleap.tangerine.service.impl;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.BeanWrapper;
 import org.springframework.beans.PropertyAccessorFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.orangeleap.tangerine.dao.SiteDao;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.customization.EntityDefault;
 import com.orangeleap.tangerine.domain.paymentInfo.Commitment;
 import com.orangeleap.tangerine.domain.paymentInfo.DistributionLine;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.GiftService;
 import com.orangeleap.tangerine.type.EntityType;
 
 @Service("commitmentService")
 @Transactional(propagation = Propagation.REQUIRED)
 public abstract class AbstractCommitmentService<T extends Commitment> extends AbstractPaymentService {
 
     /** Logger for this class and subclasses */
     protected final static Log logger = LogFactory.getLog(AbstractCommitmentService.class);
 
     @Resource(name = "giftService")
     protected GiftService giftService;
 
     @Resource(name = "siteDAO")
     private SiteDao siteDao;
 
     public void createDefault(Person constituent, T commitment, EntityType entityType, String lineIdProperty) {
         if (logger.isTraceEnabled()) {
             logger.trace("createDefaultCommitment: constituent = " + (constituent == null ? null : constituent.getId()));
         }
         // get initial commitment with built-in defaults
         BeanWrapper commitmentWrapper = PropertyAccessorFactory.forBeanPropertyAccess(commitment);
 
         List<EntityDefault> entityDefaults = siteDao.readEntityDefaults(Arrays.asList(new EntityType[] { entityType }));
         for (EntityDefault ed : entityDefaults) {
             commitmentWrapper.setPropertyValue(ed.getEntityFieldName(), ed.getDefaultValue());
         }
         List<DistributionLine> lines = new ArrayList<DistributionLine>(1);
         DistributionLine line = new DistributionLine(constituent);
         line.setDefaults();
         BeanWrapper lineWrapper = PropertyAccessorFactory.forBeanPropertyAccess(line);
         lineWrapper.setPropertyValue(lineIdProperty, commitment.getId());
         lines.add(line);
         commitment.setDistributionLines(lines);
         commitment.setPerson(constituent);
     }
     
     // TODO: refactor; this method is a mess!!!
     public List<Calendar> getCommitmentGiftDates(T commitment) {
         if (logger.isTraceEnabled()) {
             logger.trace("getCommitmentGiftDates: commitment = " + commitment);
         }
         List<Calendar> giftDates = null;
         if (commitment.getStartDate() == null) {
             logger.debug("Commitment start date is null");
             return giftDates;
         }
         if (commitment.getEndDate() == null) {
             logger.debug("Commitment end date is null");
             return giftDates;
         }
         giftDates = new ArrayList<Calendar>();
         Calendar startDateCal = new GregorianCalendar();
         startDateCal.setTimeInMillis(commitment.getStartDate().getTime());
         Calendar firstGiftCal = new GregorianCalendar(startDateCal.get(Calendar.YEAR), startDateCal.get(Calendar.MONTH), startDateCal.get(Calendar.DAY_OF_MONTH));
         giftDates.add(createGiftDate(firstGiftCal));
 
         if (Commitment.FREQUENCY_TWICE_MONTHLY.equals(commitment.getFrequency())) {
             Calendar secondGiftCal = new GregorianCalendar();
             secondGiftCal.setTimeInMillis(firstGiftCal.getTimeInMillis() + (1000 * 60 * 60 * 24 * 15));
             if (isPastEndDate(commitment, secondGiftCal.getTime())) {
                 return giftDates;
             } else {
                 giftDates.add(createGiftDate(secondGiftCal));
             }
             boolean pastEndDate = false;
             int i = 0;
             while (!pastEndDate) {
                 i++;
                 Calendar payment1 = getBimonthlyCalendar(firstGiftCal.get(Calendar.YEAR), firstGiftCal.get(Calendar.MONTH) + i, firstGiftCal.get(Calendar.DAY_OF_MONTH));
                 if (isPastEndDate(commitment, payment1.getTime())) {
                     pastEndDate = true;
                 } else {
                     giftDates.add(createGiftDate(payment1));
                 }
                 Calendar payment2 = getBimonthlyCalendar(secondGiftCal.get(Calendar.YEAR), secondGiftCal.get(Calendar.MONTH) + i, secondGiftCal.get(Calendar.DAY_OF_MONTH));
                 if (isPastEndDate(commitment, payment2.getTime())) {
                     pastEndDate = true;
                 } else {
                     giftDates.add(createGiftDate(payment2));
                 }
             }
         } else {
             Calendar giftCal = firstGiftCal;
             boolean pastEndDate = false;
             while (!pastEndDate) {
                 if (Commitment.FREQUENCY_WEEKLY.equals(commitment.getFrequency())) {
                     giftCal.add(Calendar.WEEK_OF_MONTH, 1);
                 } else if (Commitment.FREQUENCY_MONTHLY.equals(commitment.getFrequency())) {
                     giftCal.add(Calendar.MONTH, 1);
                 } else if (Commitment.FREQUENCY_QUARTERLY.equals(commitment.getFrequency())) {
                     giftCal.add(Calendar.MONTH, 3);
                 } else if (Commitment.FREQUENCY_TWICE_ANNUALLY.equals(commitment.getFrequency())) {
                     giftCal.add(Calendar.MONTH, 6);
                 } else if (Commitment.FREQUENCY_ANNUALLY.equals(commitment.getFrequency())) {
                     giftCal.add(Calendar.YEAR, 1);
                 } else {
                     logger.debug("Unknown frequency");
                     return giftDates;
                 }
                 if (isPastEndDate(commitment, giftCal.getTime())) {
                     pastEndDate = true;
                 } else {
                     giftDates.add(createGiftDate(giftCal));
                 }
             }
         }
         return giftDates;
     }
     
     public Calendar createGiftDate(Calendar giftCal) {
         return new GregorianCalendar(giftCal.get(Calendar.YEAR), giftCal.get(Calendar.MONTH), giftCal.get(Calendar.DAY_OF_MONTH));
     }
 
     public boolean isPastEndDate(T commitment, Date date) {
         return commitment.getEndDate() == null ? false : date.after(commitment.getEndDate());
     }
 
     public Calendar getBimonthlyCalendar(int year, int month, int day) {
         Calendar next = new GregorianCalendar();
         next.clear();
         if (month > 11) {
             next.set(year + 1, month - 12, 1);
         } else {
             next.set(year, month, 1);
         }
         int maxMonthDay = next.getActualMaximum(Calendar.DAY_OF_MONTH);
         if (maxMonthDay >= day) {
             next.set(Calendar.DAY_OF_MONTH, day);
         } else {
             next.set(Calendar.DAY_OF_MONTH, maxMonthDay);
         }
         logger.debug("getBimonthlyCalendar() = " + next.getTime() + " millis=" + next.getTimeInMillis());
         return next;
     }
 
     protected Date getNextGiftDate(T commitment) {
         Date nextGiftDate = new Date();
         String status = commitment instanceof Pledge ? ((Pledge)commitment).getPledgeStatus() : ((RecurringGift)commitment).getRecurringGiftStatus();
         if (commitment.getEndDate() != null && commitment.getEndDate().before(Calendar.getInstance().getTime())) {
             nextGiftDate = null;
         } 
         else if (Commitment.STATUS_ACTIVE.equals(status)) {
             nextGiftDate = calculateNextRunDate(commitment);
         }
         else {
             nextGiftDate = null;
         }
 
         if (nextGiftDate != null && logger.isDebugEnabled()) {
             logger.debug("it is currently, " + Calendar.getInstance().getTime() + ", running again at " + nextGiftDate);
         }
         return nextGiftDate;
     }
     
     protected Date calculateNextRunDate(T commitment) {
         Calendar nextRun = new GregorianCalendar();
         nextRun.setTimeInMillis(commitment.getStartDate().getTime());
         logger.debug("start date = " + nextRun.getTime() + " millis = " + nextRun.getTimeInMillis());
         Calendar today = getToday();
         
         if (Commitment.FREQUENCY_TWICE_MONTHLY.equals(commitment.getFrequency())) {
             Calendar startDateCal = new GregorianCalendar();
             startDateCal.setTimeInMillis(commitment.getStartDate().getTime());
             Calendar firstGiftCal = new GregorianCalendar(startDateCal.get(Calendar.YEAR), startDateCal.get(Calendar.MONTH), startDateCal.get(Calendar.DAY_OF_MONTH));
             boolean found = false;
             boolean pastEndDate = false;
             if (firstGiftCal.after(today)) {
                 nextRun.setTimeInMillis(firstGiftCal.getTimeInMillis());
                 pastEndDate = isPastEndDate(commitment, nextRun.getTime());
                 found = !pastEndDate;
             }
             if (!found && !pastEndDate) {
                 Calendar secondGiftCal = new GregorianCalendar();
                 secondGiftCal.setTimeInMillis(firstGiftCal.getTimeInMillis() + (1000 * 60 * 60 * 24 * 15));
                 if (secondGiftCal.after(today)) {
                     nextRun.setTimeInMillis(secondGiftCal.getTimeInMillis());
                     pastEndDate = isPastEndDate(commitment, nextRun.getTime());
                     found = !pastEndDate;
                 }
                 int i = 0;
                 while (!found && !pastEndDate) {
                     i++;
                     Calendar payment1 = getBimonthlyCalendar(firstGiftCal.get(Calendar.YEAR), firstGiftCal.get(Calendar.MONTH) + i, firstGiftCal.get(Calendar.DAY_OF_MONTH));
                     if (payment1.after(today)) {
                         nextRun.setTimeInMillis(payment1.getTimeInMillis());
                         pastEndDate = isPastEndDate(commitment, nextRun.getTime());
                         found = !pastEndDate;
                     }
                     Calendar payment2 = getBimonthlyCalendar(secondGiftCal.get(Calendar.YEAR), secondGiftCal.get(Calendar.MONTH) + i, secondGiftCal.get(Calendar.DAY_OF_MONTH));
                     if (payment2.after(today)) {
                         nextRun.setTimeInMillis(payment2.getTimeInMillis());
                         pastEndDate = isPastEndDate(commitment, nextRun.getTime());
                         found = !pastEndDate;
                     }
                 }
             }
         } else {
                 if (Commitment.FREQUENCY_WEEKLY.equals(commitment.getFrequency())) {
                     nextRun.add(Calendar.WEEK_OF_MONTH, 1);
                 } else if (Commitment.FREQUENCY_MONTHLY.equals(commitment.getFrequency())) {
                     nextRun.add(Calendar.MONTH, 1);
                 } else if (Commitment.FREQUENCY_QUARTERLY.equals(commitment.getFrequency())) {
                     nextRun.add(Calendar.MONTH, 3);
                 } else if (Commitment.FREQUENCY_TWICE_ANNUALLY.equals(commitment.getFrequency())) {
                     nextRun.add(Calendar.MONTH, 6);
                 } else if (Commitment.FREQUENCY_ANNUALLY.equals(commitment.getFrequency())) {
                     nextRun.add(Calendar.YEAR, 1);
                 } else {
                     nextRun = null;
                 }
         }
         if (nextRun == null || (commitment.getLastEntryDate() != null && !nextRun.getTime().after(commitment.getLastEntryDate()))) {
             nextRun = null;
             logger.debug("no next run scheduled");
             return null;
         }
         return nextRun.getTime();
     }
     
     protected void setCommitmentStatus(T commitment, String statusPropertyName) {
        if (commitment.getAmountPaid() != null && commitment.getAmountTotal() != null) {
             BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(commitment);
             if (bw.isWritableProperty(statusPropertyName)) {
                if (commitment.getAmountPaid().compareTo(commitment.getAmountTotal()) == 0 || commitment.getAmountPaid().compareTo(commitment.getAmountTotal()) == 1) {
                    bw.setPropertyValue(statusPropertyName, Commitment.STATUS_FULFILLED);
                 }
                else if (commitment.getAmountPaid().compareTo(BigDecimal.ZERO) == 1) {
                     bw.setPropertyValue(statusPropertyName, Commitment.STATUS_IN_PROGRESS);
                 }
             }
         }
     }
 
     protected Calendar getToday() {
         Calendar now = Calendar.getInstance();
         Calendar today = new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
         logger.trace("getToday() = " + today.getTime() + " millis=" + today.getTimeInMillis());
         return today;
     }
 }
