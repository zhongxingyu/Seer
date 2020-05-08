 package ch.uzh.ddis.katts.bolts.aggregate;
 
 import java.sql.Date;
 import java.util.ArrayList;
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
 import ch.uzh.ddis.katts.query.stream.Stream;
 import ch.uzh.ddis.katts.query.stream.Variable;
 
 /**
  * A partition bolt builds aggregates on variables (fields) by building windows that slides over the time.
  * 
  * For each slide (step) an aggregate is created. Additionally the aggregate can be grouped (partitioned) by a variable.
  * This is similar to the group by in SQL like languages.
  * 
  * The slides can be smaller, equal or greater than the windows.
  * 
  * @author Thomas Hunziker
  * 
  */
 public class PartitionerBolt extends AbstractSynchronizedBolt {
 
 	private Logger logger = LoggerFactory.getLogger(PartitionerBolt.class);
 
 	/**
 	 * Map which contains for each partitioned by value a bucket list. Each bucket contains a Map of storages objects
 	 * for each component.
 	 */
 	private Map<Object, List<Map<String, Object>>> bucketsStorage = new HashMap<Object, List<Map<String, Object>>>();
 
 	/**
 	 * The last used bucket index is used to determine, if a bucket is finished and therefore the elements need to be
 	 * emitted and the new bucket has to be reset.
 	 */
 	private Map<Object, Long> lastUsedBucketIndexStorage = new HashMap<Object, Long>();
 
 	/**
 	 * When we emit the bucket value as the aggregation value, we join the event to this bucket. But we emit the bucket,
 	 * when we receive an event for the next bucket. So when we emit the bucket, we do not have an event of the bucket
 	 * to join. This implies that we join the buckets with a total wrong event. Additionally, we emit the wrong start
 	 * date, when we have hole in the time.
 	 */
 	private Map<Object, Event> lastBucketEventStorage = new HashMap<Object, Event>();
 
 	private List<PartitionerComponent> components = new ArrayList<PartitionerComponent>();
 
 	private long slideSizeInMilliSeconds;
 	private long windowSizeInMilliSeconds;
 
 	/** Number of buckets per window */
 	private int bucketsPerWindow;
 
 	private static final long serialVersionUID = 1L;
 	private PartitionerConfiguration configuration;
 
 	/**
 	 * The field on which we build the partition. The partition is similar to the group by in SQL like languages.
 	 */
 	private Variable partitionOnField;
 
 	/**
 	 * The field on which the aggregation is done. This is similar to the field in the aggregation function in SQL like
 	 * languages.
 	 */
 	private Variable aggregateOnField;
 
 	@Override
 	public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context, OutputCollector collector) {
 		super.prepare(stormConf, context, collector);
 
 		// TODO: Move the state to the shared storage engine.
 
 		components = this.getConfiguration().getComponents();
 
 		slideSizeInMilliSeconds = this.getConfiguration().getSlideSize().getTimeInMillis(new Date(0));
 		windowSizeInMilliSeconds = this.getConfiguration().getWindowSize().getTimeInMillis(new Date(0));
 		bucketsPerWindow = (int) Math.ceil((double) windowSizeInMilliSeconds / slideSizeInMilliSeconds);
 
 		partitionOnField = this.getConfiguration().getPartitionOn();
 		aggregateOnField = getConfiguration().getAggregateOn();
 
 	}
 
 	@Override
 	public String getId() {
 		return this.getConfiguration().getId();
 	}
 
 	@Override
 	public void execute(Event event) {
 
 		String partitionFieldValue = event.getVariableValue(partitionOnField).toString().intern();
 
 		synchronized (partitionFieldValue) {
 			// Fix Bucket (initialize or reset)
 			List<Map<String, Object>> buckets = this.getBuckets(event, partitionFieldValue);
 
 			// Update Bucket (for each component update the bucket value)
 			this.updateBuckets(event, buckets, partitionFieldValue);
 
 			// Update the last used global index
 			this.lastUsedBucketIndexStorage.put(partitionFieldValue, this.getBucketGlobalIndex(event));
 		}
 
 		event.ack();
 	}
 
 	/**
 	 * This method checks if the given event with the given partitionFieldValue is inside the same bucket as the last
 	 * event for the same partition.
 	 * 
 	 * @param event
 	 * @param partitionFieldValue
 	 * @return
 	 */
 	private boolean hasBucketNumberIncreased(Event event, Object partitionFieldValue) {
 		Long bucketGlobalIndex = this.getBucketGlobalIndex(event);
 
 		Long lastIndex = this.lastUsedBucketIndexStorage.get(partitionFieldValue);
 		if (lastIndex == null || lastIndex.compareTo(bucketGlobalIndex) < 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * This method returns the buckets for the current partition value. If the buckets are not initialized or they need
 	 * to be reset, this method resets it.
 	 * 
 	 * @param event
 	 * @param partitionFieldValue
 	 * @return
 	 */
 	protected synchronized List<Map<String, Object>> getBuckets(Event event, Object partitionFieldValue) {
 		List<Map<String, Object>> buckets = this.bucketsStorage.get(partitionFieldValue);
 		boolean hasWindowChanged = false;
 		if (buckets == null) {
 			buckets = new ArrayList<Map<String, Object>>();
 			for (int i = 0; i < bucketsPerWindow; i++) {
 				buckets.add(new HashMap<String, Object>());
 			}
 			hasWindowChanged = true;
 			this.bucketsStorage.put(partitionFieldValue, buckets);
 		}
 
 		if (this.hasBucketNumberIncreased(event, partitionFieldValue)) {
 
 			// If we jump more than one slide, we need to reset the intermidate slides, because they may have incorrect
 			// storage values in it.
 			Long lastIndex = this.lastUsedBucketIndexStorage.get(partitionFieldValue);
 			if (lastIndex != null) {
 				long bucketGlobalIndex = this.getBucketGlobalIndex(event);
 				long firstIntermediateGlobalSlideId = lastIndex + 1;
 				for (long slideIndex = firstIntermediateGlobalSlideId; slideIndex < bucketGlobalIndex; slideIndex++) {
 					Map<String, Object> storages = new HashMap<String, Object>();
 					for (PartitionerComponent component : this.getComponents()) {
 						Object storage = component.resetBucket();
 						storages.put(component.getName(), storage);
 					}
 					buckets.set(this.getBucketWindowIndex(slideIndex), storages);
 				}
 			}
 
 			// Check if the bucket has to be reset, because the window
 			// has been changed. If so, then emit the aggregates:
 			if (hasWindowChanged == false) {
 				this.emitAggregates(event, partitionFieldValue, buckets);
 			}
 
 			// Since we have emit the event, we can update the last bucket event:
 			this.lastBucketEventStorage.put(partitionFieldValue, event);
 
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
 	 * This method updates the component storages for depending on the given event.
 	 * 
 	 * @param number
 	 * @param buckets
 	 * @param bucketIndex
 	 */
 	protected void updateBuckets(Event event, List<Map<String, Object>> buckets, Object partitionFieldValue) {
 		int bucketWindowIndex = this.getBucketWindowIndex(event);
 
 		// Check if the event is inside the given window. In case the window
 		// size is smaller as the slide size, we need to check, if the event
 		// is in the current window.
 		if (this.isEventInsideWindow(event)) {
 			double aggregateValue = (Double) event.getVariableValue(aggregateOnField);
 
 			Map<String, Object> storages = buckets.get(bucketWindowIndex);
 			for (PartitionerComponent component : this.getComponents()) {
 				Object storage = storages.get(component.getName());
 				storages.put(component.getName(), component.updateBucket(storage, aggregateValue));
 			}
 			buckets.set(bucketWindowIndex, storages);
 		}
 	}
 
 	/**
 	 * This method checks if the given event is inside the window. The case that it is outside the window can only
 	 * happen, when the window size is smaller as the slide size.
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
 	protected void emitAggregates(Event event, Object partitionFieldValue, List<Map<String, Object>> buckets) {
 
 		// When no data is aggregate, then do not emit anything
 		if (buckets == null) {
 			return;
 		}
 
 		long bucketNumber = this.getBucketGlobalIndex(event);
 
 		// Try to get the last event:
 		Event eventToJoinWith = this.lastBucketEventStorage.get(partitionFieldValue);
 
 		if (eventToJoinWith == null) {
 			eventToJoinWith = event;
 		}
 
 		for (Stream stream : this.getStreams()) {
 			VariableBindings bindings = getEmitter().createVariableBindings(stream, eventToJoinWith);
 
			// Copy all configured variables that exist in the original tuple (inherited and others)
			for (Variable variable : stream.getAllVariables()) {
				if (eventToJoinWith.getTuple().contains(variable.getName())) {
 					bindings.add(variable, eventToJoinWith.getVariableValue(variable));
 				}
 			}
 
 			boolean aggreationMissed = false;
 			for (PartitionerComponent component : this.getComponents()) {
 
 				// Build component specific bucket list:
 				List<Object> componentBuckets = new ArrayList<Object>();
 				for (Map<String, Object> storage : buckets) {
 					componentBuckets.add(storage.get(component.getName()));
 				}
 
 				Double aggregate = component.calculateAggregate(componentBuckets);
 
 				if (aggregate == null) {
					logger.warn("An aggreate could not be built, "
							+ "because all buckets seem to be empty or null, respectively.");
 					aggreationMissed = true;
 					break;
 				}
 
 				bindings.add(component.getName(), aggregate);
 
 			}
 
 			if (!aggreationMissed) {
 				long startTime = (bucketNumber - bucketsPerWindow + 1) * slideSizeInMilliSeconds;
 				// long endTime = bucketNumber * slideSizeInMilliSeconds;
 				long endTime = eventToJoinWith.getEndDate().getTime();
 				bindings.setStartDate(new Date(startTime));
 				bindings.setEndDate(new Date(endTime));
 				bindings.emit();
 			}
 		}
 	}
 
 	/**
 	 * This method returns the index for the bucket inside the window. The window consists of a list of buckets. This
 	 * index identifies exactly one such bucket depending on the events start date.
 	 * 
 	 * @param event
 	 * @return
 	 */
 	protected int getBucketWindowIndex(Event event) {
 		return getBucketWindowIndex(getBucketGlobalIndex(event));
 	}
 
 	/**
 	 * This method returns the index for the bucket inside the window. The window consists of a list of buckets. This
 	 * index identifies exactly one such bucket depending on the given global index.
 	 * 
 	 * @param globalIndex
 	 * @return
 	 */
 	protected int getBucketWindowIndex(Long globalIndex) {
 		return (int) (globalIndex % bucketsPerWindow);
 	}
 
 	/**
 	 * This method returns the global (over the whole time) an index for the given event. The index identifies a certain
 	 * bucket exactly in the whole time span.
 	 * 
 	 * @param event
 	 * @return
 	 */
 	protected Long getBucketGlobalIndex(Event event) {
 		long eventTime = event.getStartDate().getTime();
 		return Long.valueOf((long) (Math.floor(eventTime / (double) slideSizeInMilliSeconds)));
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
