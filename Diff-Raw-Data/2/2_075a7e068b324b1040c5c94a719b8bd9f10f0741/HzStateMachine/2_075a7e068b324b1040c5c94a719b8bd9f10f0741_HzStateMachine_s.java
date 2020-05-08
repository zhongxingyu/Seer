 package ies.retry.spi.hazelcast;
 
 import ies.retry.Retry;
 import ies.retry.spi.hazelcast.util.HzUtil;
 
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import com.hazelcast.core.HazelcastInstance;
 import com.hazelcast.core.LifecycleEvent;
 import com.hazelcast.core.LifecycleEvent.LifecycleState;
 import com.hazelcast.core.LifecycleListener;
 
 public class HzStateMachine implements LifecycleListener {
 
 	private HzState hzState = HzState.RUNNING;
 	private long hzStatePollPeriod; // in seconds
 	private TimeUnit pollUnit = TimeUnit.SECONDS;
 	private final ScheduledThreadPoolExecutor executor;
 	private HazelcastInstance inst;
 	private final HazelcastRetryImpl coordinator;
 	
 	public HzStateMachine(ScheduledThreadPoolExecutor executor,HazelcastRetryImpl coordinator,long pollPeriod) {
 		this.executor = executor;
 		this.coordinator = coordinator;
		//executor.scheduleAtFixedRate(new HzCheckTask(), 0L, 0L, TimeUnit.DAYS);
 		
 		hzState =  inst.getLifecycleService().isRunning()== true ? HzState.RUNNING:HzState.INACTIVE_UNGRACEFUL;
 		this.inst = coordinator.getHzInst();
 		
 		inst.getLifecycleService().addLifecycleListener(this);
 	}
 	
 	@Override
 	public void stateChanged(LifecycleEvent event) {
 		if (event.getState().equals(LifecycleState.SHUTDOWN) && hzState != HzState.INACTIVE_GRACEFUL) {
 			hzState = HzState.INACTIVE_UNGRACEFUL;
 		}
 		
 	}
 
 	public synchronized void stopHz() {
 				
 		coordinator.getHzInst().getLifecycleService().shutdown();
 		hzState = HzState.INACTIVE_GRACEFUL;
 	}
 	
 	public synchronized void startHz() {
 		
 		if (coordinator.getHzInst().getLifecycleService().isRunning())  {
 			throw new IllegalStateException();
 		}
 		hzState = HzState.INACTIVE_STARTING;
 		HazelcastInstance inst = HzUtil.loadHzConfig();
 		//counld have checks in here to determine the size that the instance would have before
 		//a swap
 		//we should really block until hazelcast is fully up... 
 		//have to figure out the way through the life cycle listener signal
 		//until we have that we shouldn't put it in running state.
 		coordinator.setH1(inst);
 		hzState = HzState.RUNNING;
 		//retry.setH1( HzUtil.loadHzConfig() );
 	}
 
 	public HzState getHzState() {
 		return hzState;
 	}
 	
 	
 	
 	
 	
 
 }
