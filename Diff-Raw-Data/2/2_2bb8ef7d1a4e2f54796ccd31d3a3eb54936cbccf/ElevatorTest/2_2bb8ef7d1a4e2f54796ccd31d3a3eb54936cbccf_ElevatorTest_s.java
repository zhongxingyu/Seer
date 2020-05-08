 package org.antbear.statemachine;
 
 import org.antbear.statemachine.elevator.*;
 import org.junit.Before;
 import org.junit.Test;
 
import static junit.framework.Assert.*;
 
 public class ElevatorTest implements Announcer, TransitionListener<State, Event>, TransitionExceptionListener<State, Event> {
 
     private Elevator elevator;
 
     private String announcedMessages = "";
     private int transitionEventsReceived = 0;
     private int transitionExceptionsReceived = 0;
 
     @Before
     public void beforeTest() {
         // inject a passive state machine so that the tests are single-threaded and
         // no synchronization/signals are needed
         final PassiveStateMachine<State, Event> stateMachine = new PassiveStateMachine<State, Event>();
         stateMachine.addBeforeTransitionListener(this);
         stateMachine.addExceptionListener(this);
         this.elevator = new Elevator(stateMachine, this);
         this.elevator.start();
     }
 
     @Override
     public void announce(final String message) {
         this.announcedMessages += message;
     }
 
     @Override
     public void onTransition(final TransitionEvent<State, Event> transitionEvent) {
         ++this.transitionEventsReceived;
     }
 
     @Override
     public void onException(final TransitionExceptionEvent<State, Event> transitionExceptionEvent) {
         ++this.transitionExceptionsReceived;
     }
 
     @Test
     public void moveUp() {
         this.elevator.moveUp();
         this.elevator.stop();
 
         // check that the state machine has made to correct call on the dependency
         // do not check on internal state
         assertEquals(Messages.STOPPED_MOVING_UP, this.announcedMessages);
         assertEquals(2, this.transitionEventsReceived);
     }
 
     @Test
     public void moveDown() {
         this.elevator.moveDown();
         this.elevator.stop();
 
         // do not check on internal state
         assertEquals(Messages.STOPPED_MOVING_DOWN, this.announcedMessages);
         assertEquals(2, this.transitionEventsReceived);
     }
 
     @Test
     public void moveDownMoveUp() {
         this.elevator.moveDown();
         this.elevator.stop();
         this.elevator.moveUp();
         this.elevator.stop();
 
         // do not check on internal state
         assertEquals(Messages.STOPPED_MOVING_DOWN + Messages.STOPPED_MOVING_UP, this.announcedMessages);
         assertEquals(4, this.transitionEventsReceived);
     }
 }
