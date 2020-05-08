 package eu.play_project.dcep.distributedetalis;
 
 import java.io.Serializable;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import jpl.PrologException;
 
 import org.apache.commons.collections.buffer.CircularFifoBuffer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jtalis.core.event.EtalisEvent;
 import com.jtalis.core.event.JtalisInputEventProvider;
 
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.measurement.MeasurementUnit;
 import fr.inria.eventcloud.api.CompoundEvent;
 
 /**
  * To communicate with ETALIS. Push events to and receive events from ETALIS.
  * 
  * @author sobermeier
  */
 public class JtalisInputProvider implements JtalisInputEventProvider,
 		Serializable {
 
 	private static final long serialVersionUID = 1L;
 	BlockingQueue<EtalisEvent> events = null; // Contains events till ETALIS has consumed them.
 	BlockingQueue<EtalisEvent> meausrementEvents = null; //Contains measurement events. They are preferred to the other events.
 	boolean shutdownEtalis = false; // If true ETALIS will shutdown.
 	private MeasurementUnit measurementUnit;
 	private final PrologSemWebLib semWebLib;
 	public static int eventConsumed = 0;
 	private static Logger logger = LoggerFactory.getLogger(JtalisInputProvider.class);
 	private final CircularFifoBuffer duplicatesCache = new CircularFifoBuffer(32);
 
 	public JtalisInputProvider(PrologSemWebLib semWebLib) {
 		super();
 		this.semWebLib = semWebLib;
 		this.events = new LinkedBlockingQueue<EtalisEvent>();
 	}
 
 	/**
 	 * Push events to Jtalis.
 	 */
 	public void notify(CompoundEvent event) {
 		String eventType = EventCloudHelpers.getEventType(event);
 		String eventId = event.getGraph().toString();
 
 		/*
 		 * Do some checking for duplicates (memorizing a few recently seen
 		 * events)
 		 */
		if (!duplicatesCache.contains(eventId)) {
 			logger.info("DCEP Suppressed Duplicate Event: " +eventId);
 			return;
 		}
 		else {
 			logger.info("DCEP Entry " + eventId);
 			duplicatesCache.add(eventId);
 		}
 		
 		try {
 			// Add RDF payload to Prolog:
 			semWebLib.addEvent(event);
 			// Trigger event in ETALIS:
 			events.put(new EtalisEvent("'" + eventType + "'", eventId));
 		} catch (PrologException e) {
 			logger.error("Error on new event. ", e);
 			semWebLib.removeEvent(eventId);
 		} catch (InterruptedException e) {
 			logger.error("Error adding event to Jtalis queue.", e);
 			semWebLib.removeEvent(eventId);
 		} catch (DistributedEtalisException e) {
 			logger.error("Error on new event. ", e);
 			semWebLib.removeEvent(eventId);
 		}
 	}
 
 	@Override
 	public boolean hasMore() {
 		if (shutdownEtalis) {
 			return false;
 		} else {
 			return true; // Otherwise workerthread of jtalis shuts down.
 		}
 	}
 	
 	/**
 	 * Jtalis gets events from here.
 	 */
 	@Override
 	public EtalisEvent getEvent() {
 		
 		// Return the oldest event (FIFO) blocking if there is none:
 		try {
 				EtalisEvent e = events.take();
 				incrementEventCounter();
 				return e;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@Override
 	public void setup() {
 
 	}
 	
 	public static synchronized void incrementEventCounter(){
 		eventConsumed++;
 	}
 	
 	public static synchronized void resetEventCounter(){
 		eventConsumed = 0;
 	}
 	
 	public static synchronized int getEventCounter(){
 		return eventConsumed;
 	}
 	
 	@Override
 	public void shutdown() {
 		shutdownEtalis = true;
 	}
 	
 	public int getInputQueueSize(){
 		return events.size();
 	}
 }
