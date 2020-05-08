 package monterey.example.echo;
 
 import monterey.actor.ActorRef;
 import monterey.actor.ActorSpec;
 import monterey.venue.testharness.VenueTestHarness;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import static org.testng.Assert.*;
 
 public class EchoTest {
 
     VenueTestHarness harness;
 
     @BeforeMethod
     public void setupHarness() {
         harness = VenueTestHarness.Factory.newInstance("activemq");
     }
 
     @AfterMethod(alwaysRun=true)
     public void tearDownHarness() {
         harness.shutdown();
     }
 
     @Test
     public void testActorIsInstantiated() throws Exception {
         ActorRef actorRef = harness.newActor(new ActorSpec(EchoActor.class.getName(), "Echo test actor"));
         assertTrue(harness.getActorInstance(actorRef) instanceof EchoActor);
     }
 
     @Test//(enabled=false)
     public void testEchoedResponse() {
         ActorRef actorRef = harness.newActor(new ActorSpec(EchoActor.class.getName(), "Echo Actor"));
         harness.sendTo(actorRef, "echo!");
        // TODO Test requires manual inspection that the actor did its println
     }
 
 }
