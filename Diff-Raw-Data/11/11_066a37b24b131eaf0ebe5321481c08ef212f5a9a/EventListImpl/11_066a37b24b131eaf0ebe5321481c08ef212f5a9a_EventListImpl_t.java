 /*
  * Created on 21/10/2005
  */
 package events.eventslist;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.lang.Validate;
 
 import basic.HardwareClock;
 import basic.SystemClock;
 import core.events.eventslist.EventTransaction;
 import events.PersistenceManager;
 import events.Processor;
 import events.persistence.MustBeCalledInsideATransaction;
 
 public class EventListImpl implements EventList{
 
 	private static final long serialVersionUID = 1L;
 	private final PersistenceManager persistenceManager;
 	private final HardwareClock machineClock;
 	private final SystemClock clock;
 	private final List<Processor<Serializable>> processors;
 	private final Queue<EventTransaction> transactionsQueue;
 	public final AtomicBoolean isDead = new AtomicBoolean(false);
 	private final Censor censor;
 
 
 
 	public EventListImpl(final PersistenceManager persistenceManager,  HardwareClock machineClock, SystemClock systemClock) {
 		this(persistenceManager, machineClock, systemClock, new AcceptAllCensor());
 	}
 
 	public EventListImpl(PersistenceManager persistenceManager,
 			HardwareClock machineClock, SystemClock systemClock,
 			Censor censor) {
 		this.censor = censor;
 		Validate.notNull(censor);
 		
 		this.persistenceManager = persistenceManager;
 		this.machineClock = machineClock;
 		clock = systemClock;
 		processors = new ArrayList<Processor<Serializable>>();
 		transactionsQueue = new LinkedList<EventTransaction>();
 	}
 
 	public synchronized void add(final Serializable elementAdded) {		
 		final EventTransaction transaction = new EventTransaction(machineClock.getTime().getTime(), elementAdded);
 		transactionsQueue.add(transaction);
 		executeAndWrite();
 	}
 
 	private synchronized void executeAndWrite(){
 		EventTransaction transaction = null;
 		
 		if (isDead.get())
 			throw new IllegalStateException("Events system is dead");
 		
 		while ((transaction = transactionsQueue.poll()) != null){
 			try {
 				execute(transaction);
 			} catch (Exception e){
 				isDead.set(true);
 				throw new RuntimeException(e);
 			}
 			
 			persistenceManager.writeEvent(transaction);
 		}
 		
 	}
 
 	private void execute(final EventTransaction transaction) throws MustBeCalledInsideATransaction {
 		
 		if (!censor.accept(transaction))
 			return;
 		
 		clock.setTime(transaction.getTime());
 		
 		boolean executed = false;
 		for (final Processor<Serializable> processor : processors){
 			if (processor.eventType() == transaction.getEvent().getClass()){
 				processor.execute(transaction.getEvent());
 				executed = true;
 			}
 		}
 		
 		
 		if (!executed)
 			throw new RuntimeException("No processor found for '" + transaction.getEvent().getClass().getName() + "' event.");
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public void addProcessors(final Processor... processors) {
 		
 		for (final Processor processor : processors){
 			if (this.processors.contains(processor))
 				throw new RuntimeException("You can't add the same processor twice " + processor.getClass().getName());
 			this.processors.add(processor);
 		}
 		
 		for (final EventTransaction transaction : persistenceManager.getEventTransactions()){			
 			try {
 				execute(transaction);
 			} catch (Exception e) {
				Exception exceptionWithTimestamp = new Exception("Error in transaction with timestamp : "+transaction.getTime(),e);
				exceptionWithTimestamp.printStackTrace();
 			}
 			
 		
 		}
 	}
 
 	public int getEventCount() {
 		return persistenceManager.getEventTransactions().size();
 	}
 
 
 	
 
 
 }
