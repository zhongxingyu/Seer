 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ForkJoinPool.ManagedBlocker;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.PriorityBlockingQueue;
 import java.util.concurrent.SynchronousQueue;
 import java.util.concurrent.TimeUnit;
 
 import javax.activity.InvalidActivityException;
 
 import org.omg.CORBA.DynAnyPackage.Invalid;
 
 public class ResourcePool<R> {
 
 	
 	private BlockingQueue<R> availableResourceQueue = new LinkedBlockingQueue<R>();
 	private ConcurrentMap<Integer,R> acquiredResourceMap = new ConcurrentHashMap<Integer,R>();
 	private Set<Integer> ManagedIds = new TreeSet<Integer>(); 
 	
 	public ResourcePool(){
 	}
 	private boolean _isOpen=false;
 	
 	public void open(){
 		_isOpen=true;
 	}
 	
 	public boolean isOpen(){
 		return _isOpen;
 	}
 	
 	public void close(){
 		while(!acquiredResourceMap.isEmpty());
 		closeNow();
 	}
 	
 	public void closeNow(){
 		_isOpen=false;
 	}
 	
 	public boolean add(R resource) throws InterruptedException
 	{
 		int index = System.identityHashCode(resource);
 		synchronized (ManagedIds) {
 			if(ManagedIds.contains(index))
 				return false;
 			else
 				ManagedIds.add(index);
 		}
 		availableResourceQueue.put(resource);
 		return true;
 	}
 	
 	public boolean remove(R resource){
 		int index = System.identityHashCode(resource);
 		while(acquiredResourceMap.containsKey(index));
 		return removeNow(resource, index);
 	}
 	
	public boolean removeNow(R resource){
 		int index = System.identityHashCode(resource);
 		return removeNow(resource, index);
 	}
 	
 	private boolean removeNow(R resource, int index){
 		synchronized(ManagedIds){
			BlockingQueue<R>  resultQueue= new PriorityBlockingQueue<R>();
			R tmpResource;
 			if(!ManagedIds.remove(index))
 				return false;
		
 			synchronized (availableResourceQueue) {
				while(!availableResourceQueue.isEmpty())
				{
					tmpResource=availableResourceQueue.remove();
					if(tmpResource!=resource)
						resultQueue.add(tmpResource);
				}
				availableResourceQueue=resultQueue;
 			}
 		}
 		return true;
 	}
 	
 	public R acquire() throws InvalidActivityException, InterruptedException{
 		if(!isOpen())throw new InvalidActivityException("The ResourcePool is closed. You cannot acquire from a closed ResourcePool");
 		R result ;
 		result= availableResourceQueue.take();
 
 		acquiredResourceMap.put(System.identityHashCode(result),result);
 		return result;
 	}
 	
 	public R acquire(long timeout,	TimeUnit unit) throws InvalidActivityException, InterruptedException{
 		if(!isOpen())throw new InvalidActivityException("The ResourcePool is closed. You cannot acquire from a closed ResourcePool");
 		R result ;
 		
 		try {
 			result= availableResourceQueue.poll(timeout,unit);
 		} catch (InterruptedException e) {
 			throw e;
 		}
 		if(result==null)return null;
 		
 		acquiredResourceMap.put(System.identityHashCode(result),result);
 		
 		return result;
 	}
 	
 	public void release(R resource) throws InvalidActivityException{
 		int index = System.identityHashCode(resource);
 		
 		if(acquiredResourceMap.containsKey(index))
 			acquiredResourceMap.remove(index);
 		else
 			throw new InvalidActivityException("The ResourcePool has not been used to acquire the specified resource.  You can only release resources that were acquired through the pool");
 	
 		availableResourceQueue.add(resource);
 	}
 }
