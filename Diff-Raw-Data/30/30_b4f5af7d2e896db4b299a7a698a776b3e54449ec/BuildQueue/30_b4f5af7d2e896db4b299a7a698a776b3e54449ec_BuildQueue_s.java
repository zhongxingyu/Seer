 package riskyspace.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import riskyspace.services.Event;
 import riskyspace.services.EventBus;
 import riskyspace.services.EventText;
 
 public class BuildQueue {
 	
 	private Map<Position,ArrayList<QueueItem>> colonyQueue = null;
 	private int queueMaxSize = 0;
 
 	public BuildQueue(int queueMaxSize) {
 		this.queueMaxSize = queueMaxSize;
 		colonyQueue = new HashMap<Position,ArrayList<QueueItem>>();
 	}
 	
 	/**
 	 * A class for describing queued items.
 	 * This class is needed for assigning queued items a buildtime that can be reduced.
 	 * @author rapp
 	 *
 	 */
 	private class QueueItem {
 			
 			private int buildTime;
 			private BuildAble item;
 			
 			public QueueItem (BuildAble buildAble) {
 				this.item = buildAble;
 				this.buildTime = buildAble.getBuildTime();
 			}
 			
 			public BuildAble getItem() {
 				return item;
 			}
 			
 			public int getBuildTime() {
 				return buildTime;
 			}
 			
 			public void subtractBuildTime(int time) {
 				buildTime -= time;
 			}
 		}
 	
 	/**
 	 * Adds the BuildAble item to the build queue at the provided position.
 	 * @param buildAble - the BuildAble object that you wish to add.
 	 * @param pos - the position of the colony whose build queue you want to interact with.
 	 * @return returns true if the item was successfully added to the queue, false if queue is full.
 	 */
 	public boolean add(BuildAble buildAble, Position pos) {
 		if (!colonyQueue.containsKey(pos)) {
 			colonyQueue.put(pos, new ArrayList<QueueItem>());
 		}
 		if (colonyQueue.get(pos).size() >= this.queueMaxSize) {
 			EventText et = new EventText("Build queue is full!", pos);
 			Event event = new Event(Event.EventTag.EVENT_TEXT, et);
 			EventBus.INSTANCE.publish(event);
 			return false;
 		}
 		colonyQueue.get(pos).add(new QueueItem(buildAble));
 		return true;
 	}
 	
 	/**
 	 * Adds all the BuildAble items in the provided list to a specific build queue if there is capacity to do so.
 	 * @param buildAbles - a list containing the BuildAbles you want added to the build queue.
 	 * @param pos - the position of the colony whose build queue you want to interact with.
 	 * @return returns true if all the items in the list were successfully added to the queue, false if there was not enough capacity.
 	 */
 	public boolean add(List<BuildAble> buildAbles, Position pos) {
 		if (!colonyQueue.containsKey(pos)) {
 			colonyQueue.put(pos, new ArrayList<QueueItem>());
 		}
 		if (colonyQueue.get(pos).size() + buildAbles.size() > this.queueMaxSize) {
 			EventText et = new EventText("Build queue does not have space for this!", pos);
 			Event event = new Event(Event.EventTag.EVENT_TEXT, et);
 			EventBus.INSTANCE.publish(event);
 			return false;
 		}
 		for (BuildAble ba : buildAbles) {
 			add(ba, pos);
 		}
 		return true;
 	}
 	/**
 	 * Processes the build queue simulating one game round.
 	 * @return returns a list containing the BuildAbles that are to be build this round.
 	 */
 	public Map<Position, BuildAble> processQueue() {
 		Map<Position, BuildAble> finishedBuilding = new HashMap<Position, BuildAble>();
 		for (Position pos : colonyQueue.keySet()) {
 			if (!colonyQueue.get(pos).isEmpty()) {
 				if (colonyQueue.get(pos).get(0).getBuildTime() == 1) {
 					finishedBuilding.put(pos, removeFirst(pos));
 				} else {
 					colonyQueue.get(pos).get(0).subtractBuildTime(1);
 				}
 			}
 		}
 		return finishedBuilding;
 	}
 	
 	/**
 	 * Clears the build queue at a specified position.
 	 * @param pos - the position of the colony whose build queue you want to interact with.
 	 * @return returns a list containing the BuildAbles removed.
 	 */
 	public List<BuildAble> clear(Position pos) {
 		List<BuildAble> removedItems = new ArrayList<BuildAble>();
 		if (colonyQueue.containsKey(pos)) {
 			while (!colonyQueue.get(pos).isEmpty()) {
 					removedItems.add(removeFirst(pos));
 			}
 		}
 		return removedItems;
 	}
 	
 	/**
 	 * Clears all the build queues for all the player's colonies.
 	 * @return returns a list containing the BuildAbles removed.
 	 */
 	public List<BuildAble> clearAll() {
 		List <BuildAble> allRemovedItems = new ArrayList<BuildAble>();
 		List<BuildAble> removedItems = new ArrayList<BuildAble>();
 		for (Position pos : colonyQueue.keySet()) {
 			removedItems = clear(pos);
 			for (BuildAble ba : removedItems) {
 				allRemovedItems.add(ba);
 			}
 		}
 		return allRemovedItems;
 	}
 	
 	/**
 	 * Clears every BuildAble that occupies supply from the build queue.
 	 * @param saveFirst - if true the first BuildAble objects in queues will not be removed, even if they cost supply.
 	 * @return returns a list containing the BuildAbles removed.
 	 */
 	public List<BuildAble> clearSupply(boolean saveFirst) {
 		List <BuildAble> removedItems = new ArrayList<BuildAble>();
 		for (Position pos : colonyQueue.keySet()) {
 			for (QueueItem QI : colonyQueue.get(pos)) {
 				if (QI.getItem().getSupplyCost() > 0) {
 					if (colonyQueue.get(pos).indexOf(QI) > 0 || !(saveFirst)) {
 						removedItems.add(QI.getItem());
 						colonyQueue.get(pos).remove(QI);
 					}
 				}
 			}
 		}
 		return removedItems;
 	}
 	
 	/**
 	 * Removes a specific BuildAble item from the build queue at a specified index.
 	 * @param pos - the position of the colony at which the build queue is found.
 	 * @param index - the index in the build queue of the item that you wish to remove.
 	 * @return returns the BuildAble item that has been removed.
 	 */
 	public BuildAble remove(Position pos, int index) {
 		QueueItem removed = colonyQueue.get(pos).remove(index);
 		return removed.getItem();
 	}
 	
 	/**
 	 * Removes the first BuildAble object from the build queue at specified position.
 	 * @param pos - the position of the colony at which the build queue is found.
 	 * @return returns the BuildAble item that has been removed.
 	 */
 	public BuildAble removeFirst(Position pos) {
 		return remove(pos, 0);
 	}
 	
 	/**
 	 * Removes the last BuildAble object from the build queue at specified position.
 	 * @param pos- the position of the colony at which the build queue is found.
 	 * @return returns the BuildAble item that has been removed.
 	 */
 	public BuildAble removeLast(Position pos) {
 		return remove(pos, queueSize(pos));
 	}
 	
 	/**
 	 * Tells you the amount of items queued at all colonies.
 	 * @return returns the number of elements currently in the build queue.
 	 */
 	public int queueSize() {
 		int queuedItems = 0;
 		for (Position pos : colonyQueue.keySet()) {
 			queuedItems += queueSize(pos);
 		}
 		return queuedItems;
 	}
 	
 	/**
 	 * Tells you the size of the queue at a given position.
 	 * @param pos - the position of the colony at which the build queue is found.
 	 * @return returns the number of elements in the build queue.
 	 */
 	public int queueSize(Position pos) {
 		if (!colonyQueue.containsKey(pos)) {
 			colonyQueue.put(pos, new ArrayList<QueueItem>());
 		}
 		return colonyQueue.get(pos).size();
 	}
 	
 	/**
 	 * Peek at all the build queues.
 	 * @return returns a Map containing the non-empty build queues from all colonies.
 	 */
 	// TODO
 	// QI.getItem() is mutable! Need to implement toClone() or similar in BuildAble interface.
 	public Map<Position, List<BuildAble>> peek() {
 		Map<Position, List<BuildAble>> copiedQueues = new HashMap<Position, List<BuildAble>>();
 		for (Position pos : colonyQueue.keySet()) {
 			if (!colonyQueue.get(pos).isEmpty()) {
 				copiedQueues.put(pos, peek(pos));
 			}
 		}
 		return copiedQueues;
 	}
 
 	public List<BuildAble> peek(Position pos) {
 		List<BuildAble> copiedList = new ArrayList<BuildAble>();
 		if (!colonyQueue.get(pos).isEmpty()) {
 			for (QueueItem QI : colonyQueue.get(pos)) {
 				copiedList.add(QI.getItem());
 			}
 		}
 		return copiedList;
 	}
 	
 	public BuildAble peek(Position pos, int index) throws IllegalArgumentException {
 		if (colonyQueue.get(pos).isEmpty()) {
 			throw new IllegalArgumentException("Requested position is empty");
 		}
 		if (queueSize(pos) < index) {
 			throw new IllegalArgumentException("Requested index in build queue is empty");
 		}
 		return colonyQueue.get(pos).get(index).getItem();
 	}
 	
 	public Set <Position> activePositions() {
 		Set<Position> activePositions = new HashSet<Position>();
 		for (Position pos : colonyQueue.keySet()) {
 			if (!colonyQueue.get(pos).isEmpty()) {
 				activePositions.add(pos);
 			}
 		}
 		return activePositions;
 	}
 	
 	public int numberOfActivePositions() {
 		return activePositions().size();
 	}
 }
