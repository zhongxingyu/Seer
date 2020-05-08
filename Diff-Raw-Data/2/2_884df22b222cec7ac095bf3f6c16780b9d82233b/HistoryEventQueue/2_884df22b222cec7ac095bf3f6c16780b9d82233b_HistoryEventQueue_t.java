 package fortune.sweep.events;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fortune.sweep.Algorithm;
 import fortune.sweep.events.EventQueueModification.Type;
 
 public class HistoryEventQueue extends EventQueue
 {
 
 	private List<EventQueueModification> modifications = new ArrayList<EventQueueModification>();
 
 	private Algorithm algorithm;
 
 	public HistoryEventQueue(Algorithm algorithm)
 	{
 		this.algorithm = algorithm;
 	}
 
 	@Override
 	public synchronized void insert(EventPoint eventPoint)
 	{
 		modifications.add(new EventQueueModification(algorithm.getSweepX(),
 				Type.ADD, eventPoint));
 		super.insert(eventPoint);
 	}
 
 	@Override
 	public synchronized boolean remove(EventPoint eventPoint)
 	{
 		boolean remove = super.remove(eventPoint);
 		if (remove) {
 			modifications.add(new EventQueueModification(algorithm.getSweepX(),
 					Type.REMOVE, eventPoint));
 		}
 		return remove;
 	}
 
 	@Override
 	public synchronized EventPoint pop()
 	{
 		EventPoint eventPoint = top();
		modifications.add(new EventQueueModification(eventPoint.getX(),
 				Type.REMOVE, eventPoint));
 		return super.pop();
 	}
 
 	public synchronized boolean hasModification()
 	{
 		return modifications.size() > 0;
 	}
 
 	public synchronized EventQueueModification getLatestModification()
 	{
 		if (modifications.size() == 0) {
 			return null;
 		}
 		return modifications.get(modifications.size() - 1);
 	}
 
 	public synchronized EventQueueModification revertModification()
 	{
 		if (modifications.size() == 0) {
 			return null;
 		}
 		EventQueueModification modification = modifications
 				.remove(modifications.size() - 1);
 		if (modification.getType() == Type.ADD) {
 			super.remove(modification.getEventPoint());
 		} else if (modification.getType() == Type.REMOVE) {
 			super.insert(modification.getEventPoint());
 		}
 
 		return modification;
 	}
 
 }
