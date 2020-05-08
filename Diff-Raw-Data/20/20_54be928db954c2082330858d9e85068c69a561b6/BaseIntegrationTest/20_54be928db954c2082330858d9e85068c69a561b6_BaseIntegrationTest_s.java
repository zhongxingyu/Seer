 package org.motechproject.ghana.mtn.integration;
 
 import org.motechproject.ghana.mtn.BaseSpringTestContext;
 import org.motechproject.ghana.mtn.billing.repository.AllBillAccounts;
 import org.motechproject.ghana.mtn.controller.SubscriptionController;
import org.motechproject.ghana.mtn.domain.ProgramType;
import org.motechproject.ghana.mtn.domain.ShortCode;
import org.motechproject.ghana.mtn.domain.Subscription;
import org.motechproject.ghana.mtn.domain.SubscriptionStatus;
 import org.motechproject.ghana.mtn.domain.builder.ProgramTypeBuilder;
 import org.motechproject.ghana.mtn.domain.builder.ShortCodeBuilder;
 import org.motechproject.ghana.mtn.domain.dto.SubscriptionRequest;
 import org.motechproject.ghana.mtn.matchers.ProgramTypeMatcher;
 import org.motechproject.ghana.mtn.repository.AllProgramTypes;
 import org.motechproject.ghana.mtn.repository.AllShortCodes;
 import org.motechproject.ghana.mtn.repository.AllSubscribers;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.vo.Money;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public abstract class BaseIntegrationTest extends BaseSpringTestContext {
 
     @Autowired
     protected SubscriptionController subscriptionController;
     @Autowired
     protected AllSubscriptions allSubscriptions;
     @Autowired
     protected AllSubscribers allSubscribers;
     @Autowired
     protected AllProgramTypes allProgramTypes;
     @Autowired
     protected AllBillAccounts allBillAccounts;
     @Autowired
     protected AllShortCodes allShortCodes;
 
     protected ShortCode shortCode = new ShortCodeBuilder().withCodeKey(ShortCode.RELATIVE).withShortCode("R").build();
    public final ProgramType childCarePregnancyType = new ProgramTypeBuilder().withFee(new Money(0.60D)).withMinWeek(1).withMaxWeek(52).withProgramName("Child Care").withShortCode("C").withShortCode("c").build();
    public final ProgramType pregnancyProgramType = new ProgramTypeBuilder().withFee(new Money(0.60D)).withMinWeek(5).withMaxWeek(35).withProgramName("Pregnancy").withShortCode("P").withShortCode("p").build();
 
     protected SubscriptionRequest createSubscriptionRequest(String inputMessage, String subscriberNumber) {
         SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
         subscriptionRequest.setInputMessage(inputMessage);
         subscriptionRequest.setSubscriberNumber(subscriberNumber);
         return subscriptionRequest;
     }
 
     protected void enroll(String subscriberNumber, String inputMessage) {
         SubscriptionRequest subscriptionRequest = createSubscriptionRequest(inputMessage, subscriberNumber);
         subscriptionController.handle(subscriptionRequest);
 
         assertEquals(1, allSubscriptions.getAllActiveSubscriptionsForSubscriber(subscriberNumber));
     }
 
     protected void assertEnrollmentDetails(Subscription subscription) {

         ProgramType programType = allProgramTypes.findByCampaignShortCode(subscription.getProgramType().getShortCodes().get(0));
         assertThat(programType, new ProgramTypeMatcher(subscription.getProgramType()));
         assertThat(subscription.getStatus(), is(SubscriptionStatus.ACTIVE));
         assertTrue(subscription.getStartWeekAndDay().getWeek().getNumber() >= programType.getMinWeek());
         assertTrue(subscription.getStartWeekAndDay().getWeek().getNumber() <= programType.getMaxWeek());
         assertNotNull(subscription.getSubscriber());
 
         assertCampaignSchedule(subscription);
         assertBillingSchedule(subscription);
     }
 
     private void assertBillingSchedule(Subscription subscription) {
     }
 
     protected void assertCampaignSchedule(Subscription subscription) {
 
     }
 }
