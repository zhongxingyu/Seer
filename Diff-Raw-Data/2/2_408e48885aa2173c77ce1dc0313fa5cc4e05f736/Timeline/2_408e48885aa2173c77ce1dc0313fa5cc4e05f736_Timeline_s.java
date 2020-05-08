 package net.hi117.unique;
 
 import java.util.PriorityQueue;
 
 /**
 * An object that incapsulates the timeline and handles the progression of
  * in-game events.
  *
  * @author Yanus Poluektovich (ypoluektovich@gmail.com)
  */
 public class Timeline {
 	private final PriorityQueue<Event> myQueue =
 			new PriorityQueue<>(16, EventComparator.INSTANCE);
 
 	private long myCurrentTime;
 
 	private int myCurrentPriority = Integer.MIN_VALUE;
 
 	public void addEvent(final Event event) throws CausalityViolationException {
 		final long newTime = event.getTime();
 		if (myCurrentTime > newTime) {
 			throw new CausalityViolationException(event, myQueue.peek());
 		}
 		if (myCurrentTime == newTime &&
 				event.getPriority() <= myCurrentPriority) {
 			throw new CausalityViolationException(event, myQueue.peek());
 		}
 		myQueue.add(event);
 	}
 
 	public boolean tick()
 			throws CausalityViolationException, EventException
 	{
 		if (!setCurrentTime()) {
 			return false;
 		}
 		Event event;
 		while ((event = myQueue.peek()) != null) {
 			if (event.getTime() != myCurrentTime) {
 				break;
 			}
 			myCurrentPriority = event.getPriority();
 			event.trigger();
 			myQueue.remove();
 		}
 		return true;
 	}
 
 	private boolean setCurrentTime() {
 		final Event peek = myQueue.peek();
 		if (peek == null) {
 			return false;
 		}
 		myCurrentTime = peek.getTime();
 		return true;
 	}
 
 }
