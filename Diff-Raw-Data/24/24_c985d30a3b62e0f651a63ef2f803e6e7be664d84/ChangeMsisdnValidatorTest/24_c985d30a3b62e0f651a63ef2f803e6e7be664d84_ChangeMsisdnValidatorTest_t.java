 package org.motechproject.ananya.kilkari.subscription.validators;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.obd.domain.Channel;
 import org.motechproject.ananya.kilkari.subscription.domain.Subscription;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionPack;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.subscription.repository.AllSubscriptions;
 import org.motechproject.ananya.kilkari.subscription.service.request.ChangeMsisdnRequest;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class ChangeMsisdnValidatorTest {
 
     @Mock
     private AllSubscriptions allSubscriptions;
 
     private ChangeMsisdnValidator changeMsisdnValidator;
 
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
 
     @Before
     public void setUp() {
         initMocks(this);
         changeMsisdnValidator = new ChangeMsisdnValidator(allSubscriptions);
     }
 
     @Test
     public void shouldValidateChangeMsisdnRequest() {
         String oldMsisdn = "9876543210";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, "9876543211", Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setPacks(Arrays.asList(SubscriptionPack.BARI_KILKARI, SubscriptionPack.NAVJAAT_KILKARI));
 
         Subscription subscription1 = new Subscription(oldMsisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.ACTIVE);
 
 
         Subscription subscription2 = new Subscription(oldMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.SUSPENDED);
 
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2));
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateChangeMsisdnRequestWhenOnePackIsMissing() {
         String oldMsisdn = "9876543210";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, "9876543211", Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setPacks(Arrays.asList(SubscriptionPack.BARI_KILKARI, SubscriptionPack.NAVJAAT_KILKARI, SubscriptionPack.NANHI_KILKARI));
 
         Subscription subscription1 = new Subscription(oldMsisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.ACTIVE);
 
         Subscription subscription2 = new Subscription(oldMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.NEW_EARLY);
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2));
 
         expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("Old msisdn doesn't have an updatable subscription for the requested pack %s", SubscriptionPack.NANHI_KILKARI));
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateChangeMsisdnRequestWhenAllPacksAreMissing() {
         String oldMsisdn = "9876543210";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, "9876543211", Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setPacks(Arrays.asList(SubscriptionPack.BARI_KILKARI, SubscriptionPack.NAVJAAT_KILKARI, SubscriptionPack.NANHI_KILKARI));
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(new ArrayList<Subscription>());
 
         expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("Old msisdn doesn't have an updatable subscription for the requested pack %s", SubscriptionPack.BARI_KILKARI));
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateChangeMsisdnRequestWhenOneOfTheRequestedSubscriptionIsNotUpdatable() {
         String oldMsisdn = "9876543210";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, "9876543211", Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setPacks(Arrays.asList(SubscriptionPack.BARI_KILKARI, SubscriptionPack.NAVJAAT_KILKARI));
 
         Subscription subscription1 = new Subscription(oldMsisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.ACTIVE);
 
         Subscription subscription2 = new Subscription(oldMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.PENDING_ACTIVATION);
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1));
 
         expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("Old msisdn doesn't have an updatable subscription for the requested pack %s", SubscriptionPack.NAVJAAT_KILKARI));
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateChangeMsisdnWhenAllPacksSpecified() {
         String oldMsisdn = "9876543210";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, "9876543211", Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setShouldChangeAllPacks(true);
 
         Subscription subscription1 = new Subscription(oldMsisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.ACTIVE);
 
         Subscription subscription2 = new Subscription(oldMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.SUSPENDED);
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2));
         when(allSubscriptions.findByMsisdn(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2));
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateChangeMsisdnForAllSubscriptionToBeUpdatableWhenAllPacksSpecified() {
         String oldMsisdn = "9876543210";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, "9876543211", Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setShouldChangeAllPacks(true);
 
         Subscription subscription1 = new Subscription(oldMsisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.ACTIVE);
 
         Subscription subscription2 = new Subscription(oldMsisdn, SubscriptionPack.NANHI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.SUSPENDED);
 
         Subscription subscription3 = new Subscription(oldMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription3.setStatus(SubscriptionStatus.PENDING_ACTIVATION);
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2));
         when(allSubscriptions.findByMsisdn(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2, subscription3));
 
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage(String.format("All the subscription for old msisdn are not updatable. SubscriptionId: %s; Pack: %s, Status: %s", subscription3.getSubscriptionId(), subscription3.getPack(), subscription3.getStatus()));
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateForAlteastOneSubscriptionWhenAllPacksIsSpecified() {
         String oldMsisdn = "9876543210";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, "9876543211", Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setShouldChangeAllPacks(true);
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(new ArrayList<Subscription>());
         when(allSubscriptions.findByMsisdn(changeMsisdnRequest.getOldMsisdn())).thenReturn(new ArrayList<Subscription>());
 
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage("Old msisdn has no subscriptions.");
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateIfTheNewMsisdnIsAlreadySubscribedToAnyOfTheGivenPacks() {
         String oldMsisdn = "1111111111";
         String newMsisdn = "1111111112";
         final SubscriptionPack pack = SubscriptionPack.NAVJAAT_KILKARI;
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, newMsisdn, Channel.CONTACT_CENTER, "random reason");
         changeMsisdnRequest.setPacks(new ArrayList<SubscriptionPack>(){{
             add(pack);
         }});
         Subscription oldMsisdnSubscription = new Subscription(oldMsisdn, pack, DateTime.now(), DateTime.now(), null);
         oldMsisdnSubscription.setStatus(SubscriptionStatus.ACTIVE);
         Subscription newMsisdnSubscription1 = new Subscription(newMsisdn, pack, DateTime.now(), DateTime.now(), null);
         newMsisdnSubscription1.setStatus(SubscriptionStatus.SUSPENDED);
         Subscription newMsisdnSubscription2 = new Subscription(newMsisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         newMsisdnSubscription2.setStatus(SubscriptionStatus.ACTIVE);
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(oldMsisdnSubscription));
         when(allSubscriptions.findSubscriptionsInProgress(newMsisdn)).thenReturn(Arrays.asList(newMsisdnSubscription2, newMsisdnSubscription1));
 
         expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("New msisdn already has a in-progress subscription for the requested pack %s.", SubscriptionPack.NAVJAAT_KILKARI));
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 
     @Test
     public void shouldValidateIfTheNewMsisdnIsAlreadySubscribedToAnyOfTheGivenPacksWhenAllSpecified() {
         String oldMsisdn = "9876543210";
         String newMsisdn = "9876543211";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, newMsisdn, Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setShouldChangeAllPacks(true);
 
         Subscription subscription1 = new Subscription(oldMsisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.ACTIVE);
 
         Subscription subscription2 = new Subscription(oldMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.SUSPENDED);
 
         Subscription subscription3 = new Subscription(newMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription3.setStatus(SubscriptionStatus.PENDING_ACTIVATION);
 
         when(allSubscriptions.findUpdatableSubscriptions(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2));
         when(allSubscriptions.findByMsisdn(changeMsisdnRequest.getOldMsisdn())).thenReturn(Arrays.asList(subscription1, subscription2));
         when(allSubscriptions.findSubscriptionsInProgress(newMsisdn)).thenReturn(Arrays.asList(subscription3));
 
         expectedException.expect(ValidationException.class);
        expectedException.expectMessage(String.format("New msisdn already has a in-progress subscription for the requested pack %s.", SubscriptionPack.NAVJAAT_KILKARI));
 
         changeMsisdnValidator.validate(changeMsisdnRequest);
     }
 }
