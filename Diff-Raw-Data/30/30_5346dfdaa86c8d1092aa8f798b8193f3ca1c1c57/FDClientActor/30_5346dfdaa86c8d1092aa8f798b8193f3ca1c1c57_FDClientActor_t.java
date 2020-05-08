 package br.ufmg.dcc.vod.spiderpig.distributed.fd;
 
 import java.util.HashMap;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.ReentrantLock;
 
 import br.ufmg.dcc.vod.spiderpig.distributed.RemoteMessageSender;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Fd.PingPong;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.ServiceID;
 import br.ufmg.dcc.vod.spiderpig.queue.Actor;
 import br.ufmg.dcc.vod.spiderpig.queue.QueueProcessor;
 import br.ufmg.dcc.vod.spiderpig.queue.serializer.MessageLiteSerializer;
 
 import com.google.common.base.Stopwatch;
 
 /**
  * A really basic failure detector. Basically it has two parameters, a pingTime 
  * and a timeout. It sends ping to services at every pingTime and considers 
 * services as dead if the timeout has passed. Upon calling the start method
 * this class initiates a thread for sending pings. We check for timeouts at the
 * same time we send pings, thus the implementation does not guarantee that
 * notifications will be sent exactly at timeouts. This is good enough for 
 * the crawler.
  * 
  * When adding ServiceIDs to be monitored this class guarantees that 
  * for each id added at least one answer will be send to the listener 
  * (up or down) eventually. After the first answer, subsequent ones are only 
  * triggered on state change, that is, if the monitored id was down and became 
  * up or was up and became down.
  * 
  * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
  */
 public class FDClientActor extends Actor<PingPong> 
 		implements QueueProcessor<PingPong>, Runnable {
 
 	/**
 	 * Maintains the state of a monitored ServiceID. 
 	 */
 	private class FDStruct {
 		
 		final Stopwatch stopwatch;
 		boolean down;
 		long previd;
		boolean forceNotify; //Flag used to guarantee that at least one 
							 //notification is sent
 
 		public FDStruct(Stopwatch stopwatch) {
 			this.stopwatch = stopwatch;
 			this.down = true;
 			this.forceNotify = true;
 			this.previd = 0;
 		}
 		
 	}
 
 	public static final String HANDLE = "FDClient";
 	
 	private final ReentrantLock lock;
 	private final HashMap<ServiceID, FDStruct> monitoring;
 	private final long timeout;
 	private final long pingTime;
 	private final TimeUnit unit;
 	private final RemoteMessageSender sender;
 	private final AtomicBoolean shutdown;
 	private final FDListener listener;
 	
 	private PingPong ping;
 	private Thread thread;
 
 	/**
 	 * Creates the failure detector.
 	 * 
 	 * @param timeout Timeout for detection
 	 * @param pingTime Time between ping messages
 	 * @param unit Unit of time for both timeout and pingTime
 	 * @param listener The listener which will receive updates on monitored Ids
 	 * @param sender A message sender to send the pings
 	 */
 	public FDClientActor(long timeout, long pingTime, TimeUnit unit, 
 			FDListener listener, RemoteMessageSender sender) {
 		super(HANDLE);
 		this.timeout = timeout;
 		this.pingTime = pingTime;
 		this.unit = unit;
 		this.listener = listener;
 		this.sender = sender;
 		this.lock = new ReentrantLock();
 		this.monitoring = new HashMap<>();
 		this.shutdown = new AtomicBoolean(false);
 		this.ping = null;
 		this.thread = null;
 	}
 	
 	private PingPong getMsg() {
 		if (ping == null)
 			ping = PingPong.newBuilder()
 					.setCallBackID(getServiceID())
 					.setSessionID(service.getSessionID())
 					.build();
 		return ping;
 	}
 	
 	/**
 	 * Adds ServiceID to watch map. If ServiceID is added more than once, 
 	 * at each add after the first will simply trigger a new ping message.
 	 * Moreover, the code guarantees that for each add, be it a repeated id
 	 * or not, at least one answer will be send to the listener (up or down)
 	 * eventually. After the first answer, subsequent ones are only triggered
 	 * on state change, that is, if the monitored id was down and became up
 	 * or was up and became down.
 	 * 
 	 * @param serviceID ServiceID to monitor
 	 */
 	public void watch(ServiceID serviceID) {
 		try {
 			this.lock.lock();
 			FDStruct struct = this.monitoring.get(serviceID);
 			if (struct == null) {
 				Stopwatch stopwatch = new Stopwatch();
 				struct = new FDStruct(stopwatch);
 				this.monitoring.put(serviceID, struct);
 				stopwatch.start();
 			}
 			struct.forceNotify = true;
 			this.sender.send(serviceID, getMsg());
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@Override
 	public void run() {
 		while(!this.shutdown.get()) {
 			try {
 				this.lock.lock();
 				for (ServiceID sid : this.monitoring.keySet()) {
 					FDStruct struct = this.monitoring.get(sid);
 					long elapsedMillis = struct.stopwatch.elapsedMillis();

 					if (elapsedMillis > unit.toMillis(timeout)) {
 						if (!struct.down || struct.forceNotify) {
							setDown(sid, struct);
 						}
 					}
 					this.sender.send(sid, getMsg());
 				}
 			} finally {
 				this.lock.unlock();
 			}
 			
 			try {
 				unit.sleep(pingTime);
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 
 	@Override
 	public QueueProcessor<PingPong> getQueueProcessor() {
 		return this;
 	}
 
 	@Override
 	public MessageLiteSerializer<PingPong> newMsgSerializer() {
 		return new MessageLiteSerializer<>(PingPong.newBuilder());
 	}
 
 	@Override
 	public void process(PingPong t) {
 		try {
 			this.lock.lock();
 			FDStruct struct = this.monitoring.get(t.getCallBackID());
 			
 			if (struct != null) { 
 				if (struct.down || struct.forceNotify) {
 					setUp(t, struct);
 				//If ids changed then service died and rebourned
 				} else if (struct.previd != t.getSessionID()) {
 					setDown(t.getCallBackID(), struct);
 					setUp(t, struct);
				} else if (!struct.down) {
					struct.stopwatch.reset(); //still alive, re-start counter
					struct.stopwatch.start();
 				}
 			}
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	private void setUp(PingPong pingPong, FDStruct struct) {
 		struct.previd = pingPong.getSessionID();
 		struct.down = false;
 		struct.stopwatch.reset();
 		struct.stopwatch.start();
 		struct.forceNotify = false;
 		listener.isUp(pingPong.getCallBackID());
 	}
 
 	private void setDown(ServiceID sid, FDStruct struct) {
 		struct.stopwatch.stop();
 		struct.down = true;
 		struct.forceNotify = false;
 		listener.isSuspected(sid);
 	}
 	
 	/**
 	 * Starts the failure detector.
 	 */
 	public void startTimer() {
 		try {
 			this.lock.lock();
 			
 			if (this.thread != null)
 				return;
 			
 			this.thread = new Thread(this);
 			this.thread.start();
 		} finally {
 			this.lock.unlock();
 		}
 	}
 	
 	/**
 	 * Stops the failure detector.
 	 */
 	public void stopTimer() throws InterruptedException {
 		this.shutdown.set(true);
 		this.thread.join();
 	}	
 }
