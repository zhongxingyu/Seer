 package example.suspendable;
 
 import monterey.actor.ActorRef;
 import monterey.actor.ActorSpec;
 import monterey.actor.factory.ActorFactoryRegistry;
 import monterey.actor.factory.pojo.PojoFactory;
 import monterey.test.ActorProdder;
 import monterey.test.TestUtils;
 import monterey.venue.Venue;
 import monterey.venue.jms.activemq.ActiveMqAdmin;
 import monterey.venue.spi.ActorState;
 
 import org.apache.activemq.broker.BrokerService;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.google.common.base.Predicates;
 import com.google.common.base.Supplier;
 
 /** Test the Suspend / Resume example code. */
 public class SuspendResumeTest {
     
     private static final int TIMEOUT_MS = 5*1000;
     
     private ActorRef counterActor;
     private ActiveMqAdmin admin;
     private Venue venue;
     private BrokerService activeMqBroker;
     private String activeMqUrl;
     private ActorProdder prodder;
 
     @BeforeMethod
     public void setup() throws Exception {
         setupBroker();
         setupVenue();
 
         counterActor =  venue.newActor(new ActorSpec(SuspendResumeActor.class.getName(), "counter-actor"));
 
         prodder = new ActorProdder(admin, "prodder");
         prodder.subscribe("count");
     }
 
     @AfterMethod(alwaysRun = true)
     public void tearDown() throws Exception {
        prodder.close();
         if (venue != null) venue.shutdown();
         if (activeMqBroker != null) activeMqBroker.stop();
     }
 
     private void setupBroker() throws Exception {
         activeMqUrl = "tcp://localhost:61616";
         activeMqBroker = new BrokerService();
         activeMqBroker.addConnector(activeMqUrl);
         activeMqBroker.start();
         activeMqBroker.waitUntilStarted();
         activeMqBroker.deleteAllMessages();
     }
 
     private void setupVenue() throws Exception {
         ActorFactoryRegistry actors = new ActorFactoryRegistry();
         actors.addFactory(PojoFactory.POJO, new PojoFactory());
 
         admin = new ActiveMqAdmin();
         admin.setBrokerUrl(activeMqUrl);
         
         venue = new Venue("test-venue", "test-venue", actors);
         venue.setJmsAdmin(admin);
         venue.start();
     }
 
     @Test(enabled=false)
     public void testStateIncrementedOnMessage() throws Exception {
         prodder.sendTo(counterActor, "");
         assertLastCountEquals(1);
     }
 
     @Test(enabled=false)
     public void testHasCountAfterResume() throws Exception {
         // Setup some state
         prodder.sendTo(counterActor, "");
         assertLastCountEquals(1);
 
         // Suspend and resume
         ActorState counterState = venue.suspendActor(counterActor.getId());
         venue.resumeActor(counterActor.getId(), counterState);
         
         // Expect the state to continue increasing from where it left off
         prodder.sendTo(counterActor, "");
         assertLastCountEquals(2);
     }
     
     @Test(enabled=false)
     public void testHasSubscriptionAfterResume() throws Exception {
         // Demonstrate subscription works pre-suspend
         prodder.publish("topic1", "");
         assertLastCountEquals(1);
 
         // Suspend and resume
         ActorState counterState = venue.suspendActor(counterActor.getId());
         venue.resumeActor(counterActor.getId(), counterState);
         
         // Expect the subscription to still be in place after resume
         prodder.publish("topic1", "");
         assertLastCountEquals(2);
     }
     
     private void assertLastCountEquals(int expected) {
         TestUtils.assertEventually(
                 new Supplier<Object>() {
                     public Object get() { return prodder.getLastMessage(); }
                 }, 
                 Predicates.<Object>equalTo(expected), 
                 TIMEOUT_MS);
     }
 }
