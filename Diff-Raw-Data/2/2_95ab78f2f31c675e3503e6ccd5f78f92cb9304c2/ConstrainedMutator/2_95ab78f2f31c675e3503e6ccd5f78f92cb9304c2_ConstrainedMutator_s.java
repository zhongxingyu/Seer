 package org.andrill.conop.search.mutators;
 
 import java.util.List;
 import java.util.Random;
 
 import org.andrill.conop.search.Event;
 import org.andrill.conop.search.Solution;
 
 import com.google.common.collect.Lists;
 
 /**
  * Moves a random event within its before and after constraints.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class ConstrainedMutator extends AbstractMutator {
 	protected Random random = new Random();
 
 	@Override
 	public Solution internalMutate(final Solution solution) {
 		List<Event> events = Lists.newArrayList(solution.getEvents());
 
 		// pick a random event and calculate the valid position range
 		Event e = events.remove(random.nextInt(events.size()));
 		int min = e.getAfterConstraint() == null ? 0 : solution.getPosition(e.getAfterConstraint());
 		int max = e.getBeforeConstraint() == null ? events.size() : solution.getPosition(e.getBeforeConstraint());
 
 		// add in the event at a random valid position
		events.add(min + random.nextInt(max - min), e);
 		return new Solution(solution.getRun(), events);
 	}
 
 	@Override
 	public String toString() {
 		return "Constrained";
 	}
 }
