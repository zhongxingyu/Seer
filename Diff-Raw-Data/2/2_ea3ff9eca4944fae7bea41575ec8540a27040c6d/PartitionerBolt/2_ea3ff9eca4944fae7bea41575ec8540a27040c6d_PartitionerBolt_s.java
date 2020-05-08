 package ch.uzh.ddis.katts.bolts.aggregate;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import backtype.storm.task.OutputCollector;
 import backtype.storm.task.TopologyContext;
 import ch.uzh.ddis.katts.bolts.AbstractSynchronizedBolt;
 import ch.uzh.ddis.katts.bolts.Event;
 import ch.uzh.ddis.katts.bolts.VariableBindings;
 import ch.uzh.ddis.katts.persistence.Storage;
 import ch.uzh.ddis.katts.persistence.StorageFactory;
 import ch.uzh.ddis.katts.query.stream.Stream;
 import ch.uzh.ddis.katts.query.stream.Variable;
 
 /**
  * A partition bolt builds aggregates on variables (fields) by building windows
  * that slides over the time.
  * 
  * For each slide (step) an aggregate is created. Additionally the aggregate can
  * be grouped (partitioned) by a variable. This is similar to the group by in
  * SQL like languages.
  * 
  * The slides can be smaller, equal or greater than the windows.
  * 
  * @author Thomas Hunziker
  * 
  */
 public class PartitionerBolt extends AbstractSynchronizedBolt {
 
 	private Logger logger = LoggerFactory.getLogger(PartitionerBolt.class);
 
 	/**
 	 * Map which contains for each partitioned by value a bucket list. Each
 	 * bucket contains a Map of storages objects for each component.
 	 */
 	private Storage<Object, List<Map<String, Object>>> bucketsStorage;
 
 	/**
 	 * The last used bucket index is used to determine, if a bucket is finished
 	 * and therefore the elements need to be emitted and the new bucket has to
 	 * be reset.
 	 */
 	private Storage<Object, Long> lastUsedBucketIndexStorage;
 	private List<PartitionerComponent> components = new ArrayList<PartitionerComponent>();
 
 	private long slideSizeInMilliSeconds;
 	private long windowSizeInMilliSeconds;
 
 	/** Number of buckets per window */
 	private int bucketsPerWindow;
 
 	private static final long serialVersionUID = 1L;
 	private PartitionerConfiguration configuration;
 
 	/**
 	 * The field on which we build the partition. The partition is similar to
 	 * the group by in SQL like languages.
 	 */
 	private Variable partitionOnField;
 
 	/**
 	 * The field on which the aggregation is done. This is similar to the field
 	 * in the aggregation function in SQL like languages.
 	 */
 	private Variable aggregateOnField;
 
 	private int counter = 0;
 
 	@Override
 	public void prepare(Map stormConf, TopologyContext context,
 			OutputCollector collector) {
 		super.prepare(stormConf, context, collector);
 
 		try {
 			/*
 			 * TODO: Lorenz - this.getConfiguration().getId() wird wohl nicht
 			 * über alle machinen unique sein. das heisst, dass dies nur
 			 * funktioniert, solange wir kein storage sharing über
 			 * machinengrenzen hinaushaben. kann das sein? warum hast du hier
 			 * nicht get() verwendet?
 			 * 
 			 * Thomas: this.getConfiguration().getId() muss nicht unique sein,
 			 * da wir ja genau wollen, dass wenn der Bolt an andere Stelle hoch
 			 * fährt, dann wieder den gleichen Speicher hat. Das was wir hier
 			 * zurück bekommen ist eine Art HashMap. D.h. das
 			 * partitionFieldValue muss unique sein. Das ist zutreffend da ja
 			 * partitionFieldValue = Wert des Ticker Symbols (also z.B. CSGN)
 			 * ist.
 			 */
 			bucketsStorage = StorageFactory.createDefaultStorage(this
 					.getConfiguration().getId() + "_buckets");
 			lastUsedBucketIndexStorage = StorageFactory
 					.createDefaultStorage(this.getId() + "_last_buckets");
 		} catch (InstantiationException e) {
 			logger.error("Could not load storage object.", e);
 			System.exit(0);
 		} catch (IllegalAccessException e) {
 			logger.error("Could not load storage object.", e);
 			System.exit(0);
 		}
 
 		components = this.getConfiguration().getComponents();
 
 		slideSizeInMilliSeconds = this.getConfiguration().getSlideSize()
 				.getTimeInMillis(new Date(0));
 		windowSizeInMilliSeconds = this.getConfiguration().getWindowSize()
 				.getTimeInMillis(new Date(0));
 		/*
 		 * TODO: Lorenz - Bei einer Division von longs werden alle
 		 * Nachkommastellen abgeschnitten. Du musst mindestens eine der beiden
 		 * Zahlen in einen double konvertieren bevor Du die Division
 		 * durchführst. Bei der Division wird der "kleinere" Typ in den
 		 * "grösseren" umgewandelt. Siehe auch
 		 * http://mathbits.com/MathBits/Java/DataBasics/Mathoperators.htm
 		 * 
 		 * Da hast du natürlich Recht. Allerdings ist eigentlich dies so oder so
 		 * überflüssig, da eine Voraussetzung ist, dass windowSizeInMilliSeconds
 		 * % slideSizeInMilliSeconds == 0 ist. Daher eigentlich alles
 		 * überflüssig...
 		 */
 		bucketsPerWindow = (int) Math.ceil((double) windowSizeInMilliSeconds
 				/ slideSizeInMilliSeconds);
 
 		partitionOnField = this.getConfiguration().getPartitionOn();
 		aggregateOnField = getConfiguration().getAggregateOn();
 
 	}
 
 	@Override
 	public String getId() {
 		return this.getConfiguration().getId();
 	}
 
 	@Override
 	public void execute(Event event) {
 
 		String partitionFieldValue = event.getVariableValue(partitionOnField)
 				.toString();
 
 		synchronized (partitionFieldValue) {
 			// Fix Bucket (initialize or reset)
 			List<Map<String, Object>> buckets = this.getBuckets(event,
 					partitionFieldValue);
 
 			// Update Bucket (for each component update the bucket value)
 			this.updateBuckets(event, buckets, partitionFieldValue);
 
 			// Update the last used global index
 			this.lastUsedBucketIndexStorage.put(partitionFieldValue,
 					this.getBucketGlobalIndex(event));
 		}
 
 		event.ack();
 	}
 
 	/**
 	 * This method checks if the given event with the given partitionFieldValue
 	 * is inside the same bucket as the last event for the same partition.
 	 * 
 	 * @param event
 	 * @param partitionFieldValue
 	 * @return
 	 */
 	private boolean hasBucketNumberIncreased(Event event,
 			Object partitionFieldValue) {
 		Long bucketGlobalIndex = this.getBucketGlobalIndex(event);
 
 		Long lastIndex = this.lastUsedBucketIndexStorage
 				.get(partitionFieldValue);
 		if (lastIndex == null || lastIndex.compareTo(bucketGlobalIndex) < 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * This method returns the buckets for the current partition value. If the
 	 * buckets are not initialized or they need to be reset, this method resets
 	 * it.
 	 * 
 	 * @param event
 	 * @param partitionFieldValue
 	 * @return
 	 */
 	protected synchronized List<Map<String, Object>> getBuckets(Event event,
 			Object partitionFieldValue) {
 		List<Map<String, Object>> buckets = this.bucketsStorage
 				.get(partitionFieldValue);
 		boolean initialized = false;
 		if (buckets == null) {
 			buckets = new ArrayList<Map<String, Object>>();
 			for (int i = 0; i < bucketsPerWindow; i++) {
 				buckets.add(new HashMap<String, Object>());
 			}
 			initialized = true;
 			this.bucketsStorage.put(partitionFieldValue, buckets);
 		}
 
 		if (this.hasBucketNumberIncreased(event, partitionFieldValue)) {
 
 			// Check if the bucket has to be reset, because the window
 			// has been changed. If so, then emit the aggregates:
 			if (initialized == false) {
 				this.emitAggregates(event, partitionFieldValue, buckets);
 			}
 
 			Map<String, Object> storages = new HashMap<String, Object>();
 			for (PartitionerComponent component : this.getComponents()) {
 				Object storage = component.resetBucket();
 				storages.put(component.getName(), storage);
 			}
 			buckets.set(this.getBucketWindowIndex(event), storages);
 		}
 
 		return buckets;
 	}
 
 	/**
 	 * This method updates the component storages for depending on the given
 	 * event.
 	 * 
 	 * @param number
 	 * @param buckets
 	 * @param bucketIndex
 	 */
 	protected void updateBuckets(Event event,
 			List<Map<String, Object>> buckets, Object partitionFieldValue) {
 		int bucketWindowIndex = this.getBucketWindowIndex(event);
 
 		// Check if the event is inside the given window. In case the window
 		// size is smaller as the slide size, we need to check, if the even
 		// is in the current window.
 		if (this.isEventInsideWindow(event)) {
			double aggregateValue = event.getVariableValue(aggregateOnField);
 
 			Map<String, Object> storages = buckets.get(bucketWindowIndex);
 			for (PartitionerComponent component : this.getComponents()) {
 				Object storage = storages.get(component.getName());
 				storages.put(component.getName(),
 						component.updateBucket(storage, aggregateValue));
 			}
 			buckets.set(bucketWindowIndex, storages);
 		}
 	}
 
 	/**
 	 * This method checks if the given event is inside the window. The case that
 	 * it is outside the window can only happen, when the window size is smaller
 	 * as the slide size.
 	 * 
 	 * @param event
 	 * @return
 	 */
 	protected boolean isEventInsideWindow(Event event) {
 		if (slideSizeInMilliSeconds < windowSizeInMilliSeconds) {
 			return true;
 		} else {
 			long eventTime = event.getStartDate().getTime();
 			long residual = eventTime % slideSizeInMilliSeconds;
 			if (residual > windowSizeInMilliSeconds) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 	}
 
 	/**
 	 * This method emits the aggregates build on the buckets.
 	 * 
 	 * @param event
 	 * @param buckets
 	 */
 	protected void emitAggregates(Event event, Object partitionFieldValue,
 			List<Map<String, Object>> buckets) {
 
 		// When no data is aggregate, then do not emit anything
 		if (buckets == null) {
 			return;
 		}
 
 		long bucketNumber = this.getBucketGlobalIndex(event);
 
 		for (Stream stream : this.getStreams()) {
 			VariableBindings bindings = getEmitter().createVariableBindings(
 					stream, event);
 
 			// Copy Variables from the stream we inherit our variables from
 			if (stream.getInheritFrom() != null) {
 				for (Variable variable : stream.getInheritFrom()
 						.getAllVariables()) {
 					bindings.add(variable, event.getVariableValue(variable));
 				}
 			}
 
 			for (PartitionerComponent component : this.getComponents()) {
 
 				// Build component specific bucket list:
 				List<Object> componentBuckets = new ArrayList<Object>();
 				for (Map<String, Object> storage : buckets) {
 					componentBuckets.add(storage.get(component.getName()));
 				}
 
 				double aggregate = component
 						.calculateAggregate(componentBuckets);
 				bindings.add(component.getName(), new Double(aggregate));
 
 			}
 
 			long startTime = bucketNumber * slideSizeInMilliSeconds;
 			long endTime = (bucketNumber + bucketsPerWindow)
 					* slideSizeInMilliSeconds;
 			bindings.setStartDate(new Date(startTime));
 			bindings.setEndDate(new Date(endTime));
 
 			bindings.emit();
 		}
 	}
 
 	/**
 	 * This method returns the index for the bucket inside the window. The
 	 * window consists of a list of buckets. This index identifies exactly one
 	 * such bucket depending on the events start date.
 	 * 
 	 * @param event
 	 * @return
 	 */
 	protected int getBucketWindowIndex(Event event) {
 		return getBucketWindowIndex(getBucketGlobalIndex(event));
 	}
 
 	/**
 	 * This method returns the index for the bucket inside the window. The
 	 * window consists of a list of buckets. This index identifies exactly one
 	 * such bucket depending on the given global index.
 	 * 
 	 * @param globalIndex
 	 * @return
 	 */
 	protected int getBucketWindowIndex(Long globalIndex) {
 		return (int) (globalIndex % bucketsPerWindow);
 	}
 
 	/**
 	 * This method returns the global (over the whole time) an index for the
 	 * given event. The index identifies a certain bucket exactly in the whole
 	 * time span.
 	 * 
 	 * @param event
 	 * @return
 	 */
 	protected Long getBucketGlobalIndex(Event event) {
 		long eventTime = event.getStartDate().getTime();
 		return Long.valueOf((long) (Math.floor(eventTime
 				/ (double) slideSizeInMilliSeconds)));
 	}
 
 	public PartitionerConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(PartitionerConfiguration configuration) {
 		this.configuration = configuration;
 	}
 
 	public List<PartitionerComponent> getComponents() {
 		return components;
 	}
 
 }
