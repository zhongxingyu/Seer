 package pegasus.eventbus.amqp;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.junit.Test;
 
 import pegasus.eventbus.client.EventHandler;
 import pegasus.eventbus.client.Subscription;
 import pegasus.eventbus.client.SubscriptionToken;
 
 public class AmqpEventManager_EnvelopeSubscribeTest extends AmqpEventManager_BasicSubscribeWithSubscriptionObjectTest {
 
	TestEnvelopeHandler envelopeHandler = new TestEnvelopeHandler();
 
     @Override
     @Test
     public void aSubscriptionThatSpecifiesAQueueNameShouldBeDurable() {
         // This test does not apply to this envelope subscriptions.
     }
 
     @Override
     @Test
     public void unsubscribingShouldSendtInteruptRequestsToAllHandlerThreadsOfThatSubscription() throws Exception {
         // TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
     }
 
     @Override
     @Test
     public void unsubscribingShouldWaitForAnyHandlerThreadsForThatSubscriptionWhichAreCurrentyProcessingAnEventToCompleteProcesing() throws Exception {
         // TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
     }
 
     @Override
     @Test
     public void closingTheManagerShouldSendtInteruptRequestsToAllHandlerThreadsOfThatSubscription() throws Exception {
         // TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
     }
 
     @Override
     @Test
     public void closingTheManagerShouldWaitForAnyHandlerThreadsWhichAreCurrentyProcessingAnEventToCompleteProcesing() throws Exception {
         // TODO: Refactor all test to be based on envelope handler and then test EventEnvelopeHandlerSeparately.
     }
 
     @Override
     protected SubscriptionToken subscribe(Subscription subscription) {
         return manager.subscribe(subscription);
     }
 
     @Override
     protected SubscriptionToken subscribe() {
         envelopeHandler.setEventSetName(NAMED_EVENT_SET_NAME);
         Subscription subscription = new Subscription(envelopeHandler);
         return super.subscribe(subscription);
     }
 
     @Override
     protected SubscriptionToken subscribe(EventHandler<?> handler) {
         throw new NotImplementedException();
         // return super.subscribe(handler);
     }
 
     @Override
     protected void subscribe(String queueName) {
         envelopeHandler.setEventSetName(NAMED_EVENT_SET_NAME);
         Subscription subscription = new Subscription(envelopeHandler);
         subscription.setQueueName(queueName);
         super.subscribe(subscription);
     }
 
     @Override
     protected SubscriptionToken subscribeWithANonDurableQueue() {
         envelopeHandler.setEventSetName(NAMED_EVENT_SET_NAME);
         Subscription subscription = new Subscription(envelopeHandler);
         subscription.setQueueName(NON_DURABLE_QUEUE_NAME);
         subscription.setIsDurable(false);
         return super.subscribe(subscription);
     }
 
     @Override
     protected SubscriptionToken subscribeWithADurableQueue() {
         envelopeHandler.setEventSetName(NAMED_EVENT_SET_NAME);
         Subscription subscription = new Subscription(envelopeHandler);
         subscription.setQueueName(DURABLE_QUEUE_NAME);
         subscription.setIsDurable(true);
         return super.subscribe(subscription);
     }
     
 	@Override
 	protected RoutingInfo[] getExpectedRoutes() {
 		return routesForNamedEventSet;
 	}
 }
