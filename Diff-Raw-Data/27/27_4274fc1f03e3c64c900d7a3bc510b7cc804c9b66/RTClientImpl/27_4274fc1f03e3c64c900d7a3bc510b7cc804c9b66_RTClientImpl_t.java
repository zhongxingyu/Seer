 package li.rudin.rt.core.client;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import li.rudin.rt.api.RTClient;
 import li.rudin.rt.api.annotations.ChangeId;
 import li.rudin.rt.api.filter.RTFilter;
 import li.rudin.rt.api.listener.RTListener;
 import li.rudin.rt.core.container.ObjectContainer;
 import li.rudin.rt.core.filter.NoOpFilter;
 import li.rudin.rt.core.handler.RTHandlerImpl;
 import li.rudin.rt.core.queue.list.NodeTracker;
 
 public class RTClientImpl implements RTClient
 {
 
 	public RTClientImpl(RTHandlerImpl rt, String clientId)
 	{
 		this.clientId = clientId;
 		tracker = new NodeTracker<>(rt.getEventList());
 	}
 	
 	private final String clientId;
 	
 	/**
 	 * Local sent events
 	 */
 	private final List<ObjectContainer> localEvents = new LinkedList<>();
 	private final ReadWriteLock localEventLock = new ReentrantReadWriteLock();
 	
 	private final NodeTracker<ObjectContainer> tracker;
 	
 	private long count = 0;
 
 	
 	/**
 	 * Waits for changes
 	 * @param delay
 	 */
 	public void waitForChange(long delay)
 	{
 		synchronized(this)
 		{
 			try
 			{
 				this.wait(delay);
 			}
 			catch (Exception e)
 			{}
 		}
 	}
 	
 	private boolean closed = false;
 	
 	/**
 	 * Invoked on RTHandlerImpl upon client close
 	 */
 	public void onClose()
 	{
 		closed = true;
 	}
 
 	public boolean available()
 	{
 		
 		if (tracker.available())
 			return true;
 
 		boolean available;
 		localEventLock.readLock().lock();
 		available = !localEvents.isEmpty();
 		localEventLock.readLock().unlock();
 
 		return available;
 	}
 
 	public List<ObjectContainer> copyAndClear()
 	{
 		return copyAndClear(new NoOpFilter());
 	}
 
 	public List<ObjectContainer> copyAndClear(RTFilter filter)
 	{
 		//Copy events and clear queue local
 
 		List<ObjectContainer> ret = new ArrayList<>();
 
 		for (ObjectContainer o: tracker.track())
 			if (filter.accept(o.getType(), o.getData()))
 				ret.add(o);
 		
 		localEventLock.writeLock().lock();
 		
 		for (ObjectContainer o: localEvents)
 			if (filter.accept(o.getType(), o.getData()))
 				ret.add(o);
 		
 		localEvents.clear();
 		localEventLock.writeLock().unlock();
 
 		return ret;
 	}
 
 	@Override
 	public void send(String type, Object o)
 	{
 		if (closed)
 			throw new IllegalArgumentException("client closed");
 				
 		localEventLock.writeLock().lock();
 		localEvents.add(new ObjectContainer(type, o));
 		localEventLock.writeLock().unlock();
 		
 		count++;
 
 		synchronized(this)
 		{
 			//TODO: defer
 			this.notifyAll();
 		}
 	}
 
 	@Override
 	public void send(Object o)
 	{
 		if (o == null)
 			throw new IllegalArgumentException("null payload");
 			
 		ChangeId changeId = o.getClass().getAnnotation(ChangeId.class);
 		
 		if (changeId == null)
 			throw new IllegalArgumentException("Payload of type: " + o.getClass() + " is not annotated with @ChangeId");
 		
 		send(changeId.value(), o);
 	}
 
 	@Override
 	public long getCount()
 	{
 		return count;
 	}
 
 	@Override
 	public String getId()
 	{
 		return clientId;
 	}
 
 	@Override
 	public boolean isActive()
 	{
 		return !closed;
 	}
 
 	
 	/**
 	 * Returns the current queue in the session
 	 * @param req
 	 * @return
 	 */
 	public static RTClientImpl getOrCreate(RTHandlerImpl handler, int clientId)
 	{
 		
 		RTClientImpl queue = handler.get(clientId);
 		
 		if (queue == null)
 		{
 			queue = new RTClientImpl(handler, "" + clientId);
 			
 			handler.register(clientId, queue);
 			
 			//Fire listeners
 			for (RTListener l: handler.getListeners())
				//Check if weak listener is gc'ed
				if (l != null)
					l.onConnect(queue, handler);
 		}
 		
 		return queue;
 	}
 }
