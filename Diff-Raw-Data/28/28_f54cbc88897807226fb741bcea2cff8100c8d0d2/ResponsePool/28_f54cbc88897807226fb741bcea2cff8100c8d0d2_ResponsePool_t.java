 package de.hsrm.inspector.gadgets.communication;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import de.hsrm.inspector.gadgets.communication.GadgetEvent.EVENT_TYPE;
 import de.hsrm.inspector.gadgets.intf.Gadget;
 
 /**
  * Pool of processed {@link GadgetRequest} objects. Only holds the last
  * {@link GadgetEvent} of a {@link Gadget} to avoid redundant data transfers.
  */
 public class ResponsePool {
 
 	private ConcurrentHashMap<String, ConcurrentLinkedQueue<GadgetEvent>> mResponsePool;
 	private ConcurrentHashMap<String, ConcurrentHashMap<Gadget, GadgetEvent>> mDataEvents;
 	private ConcurrentHashMap<String, Set<Gadget>> mBrowserInstances;
 
 	public ResponsePool() {
 		mResponsePool = new ConcurrentHashMap<String, ConcurrentLinkedQueue<GadgetEvent>>();
 		mDataEvents = new ConcurrentHashMap<String, ConcurrentHashMap<Gadget, GadgetEvent>>();
 		mBrowserInstances = new ConcurrentHashMap<String, Set<Gadget>>();
 	}
 
 	/**
 	 * 
 	 * @param id
 	 * @param gadget
 	 */
 	public synchronized void addBrowserGadget(String id, Gadget gadget) {
 		if (!mBrowserInstances.containsKey(id)) {
 			mBrowserInstances.put(id, new HashSet<Gadget>());
 		}
 		mBrowserInstances.get(id).add(gadget);
 	}
 
 	/**
 	 * 
 	 * @param gadget
 	 */
 	public synchronized void removeGadget(Gadget gadget) {
 		for (Set<Gadget> set : mBrowserInstances.values()) {
 			set.remove(gadget);
 		}
 	}
 
 	/**
 	 * Add a new {@link GadgetEvent} to the pool. If {@link GadgetEvent} is
 	 * {@link EVENT_TYPE#DATA} only the last added {@link GadgetEvent} will be
 	 * published to web application.
 	 * 
 	 * @param response
 	 *            {@link GadgetEvent}
 	 */
 	public synchronized void add(GadgetEvent response) {
 		for (String id : mBrowserInstances.keySet()) {
 			// Check each browser id, if instance uses this gadget.
 			if (mBrowserInstances.get(id).contains(response.getGadget())) {
 				// If event is data event.
 				if (response.getEvent().equals(EVENT_TYPE.DATA)) {
 					if (!mDataEvents.containsKey(id)) {
 						mDataEvents.put(id, new ConcurrentHashMap<Gadget, GadgetEvent>());
 					}
 					mDataEvents.get(id).put(response.getGadget(), response);
 				} else {
 					mResponsePool.get(id).add(response);
 				}
 			}
 		}
 	}
 
 	public synchronized void addAll(List<GadgetEvent> events) {
 		for (GadgetEvent e : events) {
 			add(e);
 		}
 	}
 
 	/**
 	 * Get all {@link GadgetEvent} objects for one web application.
 	 * 
 	 * @param id
 	 *            {@link String}
 	 * @return {@link List} of {@link GadgetEvent}
 	 */
 	public synchronized List<GadgetEvent> popAll(String id) {
 		ArrayList<GadgetEvent> responses = new ArrayList<GadgetEvent>();
 
 		if (mResponsePool.containsKey(id)) {
 			while (!mResponsePool.get(id).isEmpty()) {
 				responses.add(mResponsePool.get(id).poll());
 			}
 		}
 		if (mDataEvents.containsKey(id)) {
 			for (GadgetEvent e : mDataEvents.get(id).values()) {
 				responses.add(e);
 			}
 			mDataEvents.get(id).clear();
 		}
 
 		return responses;
 	}
 
 	/**
 	 * Returns <code>true</code> if {@link #mResponses} contains a pool for
 	 * given parameter and this pool isn't empty.
 	 * 
 	 * @param id
 	 *            {@link String}
 	 * @return {@link Boolean}
 	 */
 	public synchronized boolean hasItems(String id) {
 		return (mResponsePool.containsKey(id) && !mResponsePool.get(id).isEmpty())
				|| (mDataEvents.containsKey(id) && !mDataEvents.get(id).isEmpty());
 	}
 
 	public synchronized int size(String id) {
 		int size = 0;
 		if (mDataEvents.get(id) != null) {
 			size += mDataEvents.get(id).size();
 		}
 		if (mResponsePool.get(id) != null) {
 			size += mResponsePool.get(id).size();
 		}
 		return size;
 	}
 
 	public synchronized void clear(String id) {
 		if (mDataEvents.containsKey(id)) {
 			mDataEvents.get(id).clear();
 		}
 		if (mResponsePool.containsKey(id)) {
 			mResponsePool.get(id).clear();
 		}
 	}

	public synchronized void clearAll() {
		mDataEvents.clear();
		mResponsePool.clear();
		mBrowserInstances.clear();
	}
 }
