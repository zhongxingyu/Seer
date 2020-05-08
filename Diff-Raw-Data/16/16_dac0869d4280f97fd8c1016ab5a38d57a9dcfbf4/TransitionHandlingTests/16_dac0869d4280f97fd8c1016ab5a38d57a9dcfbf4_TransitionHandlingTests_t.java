 package de.reneruck.tcd.ipp.database;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.junit.Before;
 import org.junit.Test;
 
import de.reneruck.tcd.ipp.datamodel.Airport;
 import de.reneruck.tcd.ipp.datamodel.Booking;
 import de.reneruck.tcd.ipp.datamodel.transition.NewBookingTransition;
 import de.reneruck.tcd.ipp.datamodel.transition.TemporalTransitionsStore;
 import de.reneruck.tcd.ipp.datamodel.transition.Transition;
 import de.reneruck.tcd.ipp.datamodel.transition.TransitionState;
 
 public class TransitionHandlingTests {
 
 	private Queue<Transition> dbQueue;
 	private TemporalTransitionsStore transitionsQueue;
 
 	@Before
 	public void setUp() throws Exception {
 		this.dbQueue = new LinkedBlockingQueue<Transition>(new LinkedList<Transition>());
 		this.transitionsQueue = new TemporalTransitionsStore();
 		
		DatabaseQueryHandler queryHandler = new DatabaseQueryHandler(this.dbQueue, this.transitionsQueue);
 		queryHandler.setRunning(true);
 		queryHandler.start();
 	}
 
 	@Test
 	public void testPendingTransition() {
		Booking booking = new Booking("test", new Date(1361728800000L), Airport.city);
 		NewBookingTransition transition = new NewBookingTransition(booking);
 		this.dbQueue.add(transition);
 		
 		sleep();
 		
 		Transition transitionById = this.transitionsQueue.getTransitionById(transition.getTransitionId());
 
 		assertTrue("Transition was added to the overall transition queue", 
 				this.transitionsQueue.containsTransition(transition));
 		assertEquals("Transition was added to the overall transition queue", 
 				transitionById, transition);
 		assertTrue("Transition Process was set to PROCESSED", 
 				TransitionState.PROCESSED.equals(transitionById.getTransitionState()));
 		assertTrue("Processed Date was correctly set", 
 				transitionById.getProcessingDate().before(new Date(System.currentTimeMillis())));
 	}
 	
 	@Test
 	public void testAcnowlegedTransition() {
		Booking booking = new Booking("test", new Date(1361728800000L), Airport.city);
 		NewBookingTransition transition = new NewBookingTransition(booking);
 		this.dbQueue.add(transition);
 		
 		sleep();
 		
 		NewBookingTransition transition2 = new NewBookingTransition(booking);
 		transition2.setTransitionState(TransitionState.ACKNOWLEGED);
 		this.dbQueue.add(transition2);
 		
 		sleep();
 		
 		assertFalse(this.transitionsQueue.containsTransition(transition));
 		assertFalse(this.transitionsQueue.containsTransition(transition2));
 	}
 
 	@Test
 	public void testProcessedTransition() {
		Booking booking = new Booking("test", new Date(1361728800000L), Airport.city);
 		NewBookingTransition transition = new NewBookingTransition(booking);
 		transition.setTransitionState(TransitionState.PROCESSED);
 		transition.setHandlingDate(new Date(System.currentTimeMillis()));
 		this.dbQueue.add(transition);
 		
 		sleep();
 		
 		assertFalse(this.transitionsQueue.containsTransition(transition));
 	}
 	
 	
 	private void sleep() {
 		try {
			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		};
 	}
 }
