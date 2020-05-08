 package org.andrill.conop.search.mutators;
 
 import java.util.List;
 import java.util.Random;
 
 import org.andrill.conop.search.Event;
 import org.andrill.conop.search.Solution;
 
 import com.google.common.collect.Lists;
 
 public class FooMutator extends AbstractMutator {
 	protected int pos = 0;
 	protected Random random = new Random();
 
 	@Override
 	public Solution internalMutate(final Solution solution) {
 		List<Event> events = Lists.newArrayList(solution.getEvents());
 
 		// pick a random event and calculate the valid position range
 		pos = (pos + 1) % events.size();
 		Event e = events.remove(pos);
		int range = Math.abs(solution.getMin(e) - solution.getMax(e)) + 5;
 		int newPos = Math.max(0, Math.min(events.size(), pos + (random.nextInt(range) - range / 2)));
 
 		// add in the event at a random valid position
 		events.add(newPos, e);
 		return new Solution(solution.getRun(), events);
 	}
 
 	@Override
 	public String toString() {
 		return "Foo";
 	}
 }
