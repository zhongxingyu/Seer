 package com.orangeleap.tangerine.test.service.impl;
 
 import java.lang.reflect.Method;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.validation.BindException;
 import org.testng.annotations.Test;
 
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.PaymentSource;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.paymentInfo.Commitment;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.NightlyBatchService;
 import com.orangeleap.tangerine.service.RecurringGiftService;
 import com.orangeleap.tangerine.service.impl.AbstractCommitmentService;
 import com.orangeleap.tangerine.test.BaseTest;
 import com.orangeleap.tangerine.test.dataprovider.PaymentSourceDataProvider;
 
 public class RecurringGiftServiceImplTest extends BaseTest {
     @Autowired
     private RecurringGiftService recurringGiftService;
  
     @Autowired
     private NightlyBatchService nightlyBatchService;
 
     private List<RecurringGift> setupRecurringGifts() throws Exception {
         final List<RecurringGift> rGifts = new ArrayList<RecurringGift>();
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        rGifts.add(new RecurringGift(1L, Commitment.STATUS_CANCELLED, sdf.parse("10/31/2009"), null));
        rGifts.add(new RecurringGift(2L, Commitment.STATUS_ACTIVE, sdf.parse("03/15/2009"), sdf.parse("04/15/2009")));
        rGifts.add(new RecurringGift(3L, Commitment.STATUS_IN_PROGRESS, sdf.parse("03/15/2009"), null));
        rGifts.add(new RecurringGift(4L, Commitment.STATUS_FULFILLED, sdf.parse("11/25/2009"), sdf.parse("11/30/2009")));
        rGifts.add(new RecurringGift(5L, Commitment.STATUS_EXPIRED, sdf.parse("03/15/2009"), sdf.parse("04/15/2009")));
        rGifts.add(new RecurringGift(6L, Commitment.STATUS_PENDING, sdf.parse("01/01/2009"), null));
         return rGifts;
     }
     
     @Test
     public void testFilterApplicableRecurringGiftsForConstituent() throws Exception {
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         
         List<RecurringGift> returnList = recurringGiftService.filterApplicableRecurringGiftsForConstituent(setupRecurringGifts(), sdf.parse("12/01/2008"));
         assert returnList.isEmpty();
         
         returnList = recurringGiftService.filterApplicableRecurringGiftsForConstituent(setupRecurringGifts(), sdf.parse("01/02/2009"));
         assert returnList.size() == 1;
         for (RecurringGift aGift : returnList) {
             assert aGift.getId() == 6L;
         }
 
         returnList = recurringGiftService.filterApplicableRecurringGiftsForConstituent(setupRecurringGifts(), sdf.parse("03/15/2009"));
         assert returnList.size() == 3;
         for (RecurringGift aGift : returnList) {
             assert aGift.getId() == 2L || aGift.getId() == 3L || aGift.getId() == 6L;
         }
 
         returnList = recurringGiftService.filterApplicableRecurringGiftsForConstituent(setupRecurringGifts(), sdf.parse("11/30/2009"));
         Assert.assertEquals("expected size = " + returnList.size(), 3, returnList.size());
         for (RecurringGift aGift : returnList) {
             assert aGift.getId() == 4L || aGift.getId() == 3L || aGift.getId() == 6L;
         }
 
         returnList = recurringGiftService.filterApplicableRecurringGiftsForConstituent(setupRecurringGifts(), sdf.parse("08/08/2012"));
         assert returnList.size() == 2;
         for (RecurringGift aGift : returnList) {
             assert aGift.getId() == 3L || aGift.getId() == 6L;
         }
     }
 
     Method findCalculateNextRunDateMethod() {
         Method methods[] = AbstractCommitmentService.class.getDeclaredMethods();
 
         for (int i = 0; i < methods.length; i++)
             if (methods[i].getName().equals("calculateNextRunDate")) return methods[i];
 
         return null;
     }
     
                /*
     @Test(dataProvider = "setupCCPaymentSource", dataProviderClass = PaymentSourceDataProvider.class)
     public void testNextRunDate(Site site, Constituent c, PaymentSource ps) throws NoSuchMethodException,IllegalAccessException, InvocationTargetException
     {
         RecurringGiftService service = new RecurringGiftServiceImpl();
         RecurringGift recurringGift  = null;
         Calendar startCal = Calendar.getInstance();
         startCal.add(Calendar.MONTH,-4);
 
         Calendar endCal = Calendar.getInstance();
 
         Method getNextRunDate = findCalculateNextRunDateMethod();
         getNextRunDate.setAccessible(true);
 
         recurringGift = new RecurringGift(1L,Commitment.STATUS_PENDING,startCal.getTime(),endCal.getTime());
         recurringGift.setFrequency(Commitment.FREQUENCY_WEEKLY);
         Date d = (Date) getNextRunDate.invoke(service,recurringGift);
 
         recurringGift = new RecurringGift(1L,Commitment.STATUS_PENDING,startCal.getTime(),endCal.getTime());
         recurringGift.setFrequency(Commitment.FREQUENCY_QUARTERLY);
         recurringGift.setConstituent(c);
         recurringGift.setPaymentSource(ps);
      
         d = (Date) getNextRunDate.invoke(service,recurringGift);
     }
                  */
 
     @Test(dataProvider = "setupCCPaymentSource", dataProviderClass = PaymentSourceDataProvider.class)
     public void testWeekly(Site site, Constituent c, PaymentSource ps) throws BindException
     {
 
         Calendar startCal = Calendar.getInstance();
         startCal.add(Calendar.MONTH,-4);
 
         Calendar endCal = Calendar.getInstance();
                                      
 /*
         RecurringGift recurringGift = new RecurringGift(100L,Commitment.STATUS_PENDING,startCal.getTime(),endCal.getTime());
         recurringGift.setAmountPerGift(new BigDecimal(10.00));
         recurringGift.setFrequency(RecurringGift.FREQUENCY_WEEKLY);
         recurringGift.setActivate(true);
         recurringGift.setConstituent(c);
         recurringGift.setPaymentSource(ps);
 
         recurringGiftService.maintainRecurringGift(recurringGift);
 */
         nightlyBatchService.processRecurringGifts();
     }
 }
