 package pegasus.eventbus.integration_tests;
 
 import static com.jayway.awaitility.Awaitility.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.*;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventHandler;
 import pegasus.eventbus.client.EventResult;
 import pegasus.eventbus.client.Subscription;
 import pegasus.eventbus.client.SubscriptionToken;
 import pegasus.eventbus.testsupport.TestSendEvent;
 import pegasus.eventbus.testsupport.TestSendEvent2;
 
 public class SubscriptionTest extends IntegrationTestBase {
 
     TestSendEvent                       receivedEvent;
     SubscriptionToken                   subscription;
     private HandlerThatHandlesExactType handler;
 
     public TestSendEvent getReceivedEvent() {
         return receivedEvent;
     }
 
     @Test
     public void aHandlerThatAcceptsTheSubscribedTypeShouldReceiveEventsOfThatType() throws Exception {
         subscription = subscribe();
 
         manager.publish(sendEvent);
 
         waitAtMost(5, TimeUnit.SECONDS).untilCall(to(this).getReceivedEvent(), notNullValue());
         sendEvent.assertIsEquevalentTo(receivedEvent);
     }
 
     @Test
     public void aHandlerThatAcceptsASuperTypeOfTheSubscribedTypeShouldReceiveEventsOfThatType() throws Exception {
         subscription = manager.subscribe(new HandlerThatHandlesSuperType(this));
 
         manager.publish(sendEvent);
 
         waitAtMost(5, TimeUnit.SECONDS).untilCall(to(this).getReceivedEvent(), notNullValue());
         sendEvent.assertIsEquevalentTo(receivedEvent);
     }
 
     @Test
     public void aHandlerThatAcceptsMultipleTypesShouldReceiveEventsOfEachSuchType() throws Exception {
         final HandlerThatHandlesMultipleTypes handler = new HandlerThatHandlesMultipleTypes();
         subscription = manager.subscribe(handler);
 
         manager.publish(sendEvent);
         manager.publish(new TestSendEvent2());
 
         waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler.getReceivedEvents()).size(), equalTo(2));
     }
 
     @Test
     public void subscribingShouldCreateTheExchangeTheQueueAndTheBindingBetweenThemIfNotAlreadyPresent() throws Exception {
 
         resetVirtualHost();
 
         subscription = subscribe();
 
         assertExchangeExists();
         assertQueueExists();
         assertQeueuHasBindingFor(TestSendEvent.class);
     }
 
     @Test
     public void aSubscribedHandlerShouldNotReceiveFurtherEventsAfterBeingUnsubscribed() throws Exception {
         subscription = subscribe();
 
         manager.publish(sendEvent);
 
         waitAtMost(5, TimeUnit.SECONDS).untilCall(to(this).getReceivedEvent(), notNullValue());
 
         manager.unsubscribe(subscription);
         receivedEvent = null;
 
         manager.publish(sendEvent);
 
         Thread.sleep(2000);
 
         assertNull(receivedEvent);
     }
 
     @Test
     public void anEnvelopeSubscriptionShouldReceiveEvents() throws Exception {
         TestEnvelopeHandler handler = new TestEnvelopeHandler();
 
         handler.setEventSetName("all-test-events");
         Subscription subscription = new Subscription(handler);
         manager.subscribe(subscription);
 
         manager.publish(sendEvent);
 
         waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler).eventWasReceived(), equalTo(true));
     }
 
     protected SubscriptionToken subscribe() {
         handler = new HandlerThatHandlesExactType(this);
         return manager.subscribe(handler);
     }
 
     public class HandlerThatHandlesExactType implements EventHandler<TestSendEvent> {
 
         private SubscriptionTest testFixture;
 
         public HandlerThatHandlesExactType(SubscriptionTest testFixture) {
             super();
             this.testFixture = testFixture;
         }
 
         @Override
         public EventResult handleEvent(TestSendEvent event) {
             testFixture.receivedEvent = event;
             return EventResult.Handled;
         }
 
         @SuppressWarnings({ "unchecked", "rawtypes" })
         @Override
         public Class<? extends TestSendEvent>[] getHandledEventTypes() {
             Class[] classes = { TestSendEvent.class };
             return classes;
         }
     }
 
     public class HandlerThatHandlesSuperType implements EventHandler<Object> {
 
         private SubscriptionTest testFixture;
 
         public HandlerThatHandlesSuperType(SubscriptionTest testFixture) {
             super();
             this.testFixture = testFixture;
         }
 
         @Override
         public EventResult handleEvent(Object event) {
             testFixture.receivedEvent = (TestSendEvent) event;
             return EventResult.Handled;
         }
 
         @SuppressWarnings("rawtypes")
         @Override
         public Class<?>[] getHandledEventTypes() {
             Class[] classes = { TestSendEvent.class };
             return classes;
         }
     }
 
     public class HandlerThatHandlesMultipleTypes implements EventHandler<Object> {
 
         private final ArrayList<Object> receivedEvents = new ArrayList<Object>();
 
         public ArrayList<Object> getReceivedEvents() {
             return receivedEvents;
         }
 
         @Override
         public EventResult handleEvent(Object event) {
             receivedEvents.add(event);
             return EventResult.Handled;
         }
 
         @SuppressWarnings("rawtypes")
         @Override
         public Class<?>[] getHandledEventTypes() {
             Class[] classes = { TestSendEvent.class, TestSendEvent2.class };
             return classes;
         }
     }
 
     public class TestEnvelopeHandler implements EnvelopeHandler {
 
         private boolean wasReveived;
         private String  eventSetName;
 
         @Override
         public EventResult handleEnvelope(Envelope envelope) {
             wasReveived = true;
             return EventResult.Handled;
         }
 
         public boolean eventWasReceived() {
             return wasReveived;
         }
 
         @Override
         public String getEventSetName() {
             return eventSetName;
         }
 
        @Override
         public void setEventSetName(String eventSetName) {
             this.eventSetName = eventSetName;
         }
     }
 }
