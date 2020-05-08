 package github.com.cp149.disruptor;
 
 import github.com.cp149.BaseAppender;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.logging.log4j.core.async.RingBufferLogEvent;
 
 import ch.qos.logback.classic.spi.ILoggingEvent;
 
 import com.lmax.disruptor.EventFactory;
 import com.lmax.disruptor.EventHandler;
 import com.lmax.disruptor.RingBuffer;
 import com.lmax.disruptor.Sequence;
 import com.lmax.disruptor.SequenceReportingEventHandler;
 import com.lmax.disruptor.SleepingWaitStrategy;
 import com.lmax.disruptor.YieldingWaitStrategy;
 import com.lmax.disruptor.dsl.Disruptor;
 import com.lmax.disruptor.dsl.ProducerType;
 
 public class DisruptorAppender extends BaseAppender {
 	private static final int RINGBUFFER_DEFAULT_SIZE = 256 * 1024;
 	private static final int HALF_A_SECOND = 500;
 	private static final int MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN = 20;
 
 	private final class LogEventHandler implements EventHandler<ValueEvent> {
 		// event will eventually be recycled by the Disruptor after it wraps
 		public void onEvent(final ValueEvent event, final long sequence, final boolean endOfBatch) throws Exception {
 			aai.appendLoopOnAppenders(event.getEvent());		
			event.setEvent(null);
 		}			
 	}
 
 	final ExecutorService exec = Executors.newSingleThreadExecutor();	
 	Disruptor<ValueEvent> disruptor = new Disruptor<ValueEvent>(DisruptorAppender.EVENT_FACTORY, RINGBUFFER_DEFAULT_SIZE, exec,ProducerType.MULTI, new SleepingWaitStrategy());
 
 	final LogEventHandler handler = new LogEventHandler();
 
 	public final static EventFactory<ValueEvent> EVENT_FACTORY = new EventFactory<ValueEvent>() {
 		public ValueEvent newInstance() {
 			return new ValueEvent();
 		}
 	};
 	private RingBuffer<ValueEvent> ringBuffer;
 
 	@Override
 	public void start() {
 
 		super.start();
 		disruptor.handleEventsWith(handler);
 		ringBuffer = disruptor.start();
 
 	}
 
 	@Override
 	public void stop() {
 		if (!isStarted())
 			return;
 
 		this.started = false;
 		try {			 ;
 
 			// Must guarantee that publishing to the RingBuffer has stopped
 			// before we call disruptor.shutdown()
 //			disruptor = null; // client code fails with NPE if log after stop =
 								// OK
 			 disruptor.shutdown();
 
 			// wait up to 10 seconds for the ringbuffer to drain
 			RingBuffer<ValueEvent> ringBuffer = disruptor.getRingBuffer();
 			for (int i = 0; i < MAX_DRAIN_ATTEMPTS_BEFORE_SHUTDOWN; i++) {
 				if (ringBuffer.hasAvailableCapacity(ringBuffer.getBufferSize())) {
 					break;
 				}
 				try {
 					// give ringbuffer some time to drain...
 					Thread.sleep(HALF_A_SECOND);
 				} catch (InterruptedException e) {
 					// ignored
 				}
 			}
 			disruptor.shutdown();
 			disruptor = null;			
 			exec.shutdown(); // finally, kill the processor thread
 			try {
 				// give ringbuffer some time to drain...
 				Thread.sleep(HALF_A_SECOND);
 			} catch (InterruptedException e) {
 				// ignored
 			}finally{
 				exec.shutdownNow();
 			}
 			detachAndStopAllAppenders();
 		} catch (Exception e) {
 			addError(e.getMessage());
 		}
 		super.stop();
 	}
 
 	@Override
 	protected void append(ILoggingEvent eventObject) {
 		try {
 			if (includeCallerData) {
 				eventObject.prepareForDeferredProcessing();
 				eventObject.getCallerData();
 			}
 
 			long seq = ringBuffer.next();
 			try {
 				ValueEvent valueEvent = ringBuffer.get(seq);
 				valueEvent.setEvent(eventObject);
 			} finally {
 				ringBuffer.publish(seq);
 			}
 		} catch (Exception e) {
 			addError(e.getMessage());
 		}
 
 	}
 
 }
