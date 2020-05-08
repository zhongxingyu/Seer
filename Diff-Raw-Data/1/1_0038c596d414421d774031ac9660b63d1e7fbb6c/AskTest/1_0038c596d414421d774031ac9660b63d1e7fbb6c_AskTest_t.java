 package org.nohope.akka;
 
 import akka.actor.ActorSystem;
 import akka.actor.Props;
 import akka.testkit.TestActorRef;
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 10/17/12 9:13 PM
  */
 @SuppressWarnings("MethodMayBeStatic")
 public class AskTest {
 
     public static class EchoActor extends MessageTypeMatchingActor {
         @OnReceive
         public Integer echo(final Integer param) {
             return param;
         }
     }
 
     @Test
     public void castingTest() throws Exception {
         final ActorSystem system = org.nohope.test.AkkaUtils.createLocalSystem("test");
         final TestActorRef ref = TestActorRef.apply(new Props(EchoActor.class), system);
         assertEquals(123, (int) Ask.waitReply(Integer.class, ref, 123));
 
         try {
             Ask.waitReply(String.class, ref, 123);
             fail();
         } catch (Exception e) {
             assertTrue(e.getCause() instanceof ClassCastException);
         }
     }
 }
