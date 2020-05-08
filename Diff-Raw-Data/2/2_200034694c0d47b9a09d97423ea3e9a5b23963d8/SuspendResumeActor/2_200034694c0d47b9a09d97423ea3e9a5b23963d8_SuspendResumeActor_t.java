 package example.suspendable;
 
 import java.io.Serializable;
 
 import monterey.actor.Actor;
 import monterey.actor.ActorContext;
 import monterey.actor.MessageContext;
 import monterey.actor.trait.Suspendable;
 
 /** 
  * A very simple example of implementing a suspendable Actor.
  *
  * This shows how an actor can preserve its state when being suspended and resumed 
  * (including when being moved to a different location in between). Any subscriptions
  * done in start() will be automatically re-subscribed.
  * 
  * On resume, it will be a different instance of the actor so its fields must be 
  * set on resume.
  * 
  * In this class, the state is a simple counter of the number of messages received.
  */
 public class SuspendResumeActor implements Actor, Suspendable {
     private ActorContext context;
     private long count;
 
     @Override
     public void init(ActorContext context) {
         this.context = context;
     }
 
     @Override
     public void onMessage(Object payload, MessageContext messageContext) {
         count++;
         
        // Publish the latest count (primarily for the purpose of testing this actor).
         context.publish("count", count);
     }
 
     @Override
     public void start(Object state) {
         count = 0;
         context.subscribe("topic1");
     }
 
     @Override
     public Serializable suspend() {
         return count;
     }
     
     @Override
     public void resume(Object state) {
         count = (Long) state;
     }
 }
