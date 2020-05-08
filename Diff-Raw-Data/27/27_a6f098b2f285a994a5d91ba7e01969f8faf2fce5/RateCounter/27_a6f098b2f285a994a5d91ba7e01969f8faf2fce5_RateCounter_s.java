 package ddth.dasp.framework.stats;
 
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLongArray;
 
 /**
  * This class encapsulates a rate (number of things/amount of time) counter.
  * 
  * @author NBThanh <btnguyen2k@gmail.com>
  */
 public class RateCounter {
 	private final static int NUM_SLOTS_BUFFER = 2;
 
 	/*
 	 * Number of slots should be power of 2 (e.g. 2, 4, 8, 16, etc)
 	 */
 	private int numSlots;
 	private int numSlotsReturns;
 	private int slotMask;
 	private Timer timer;
 	private TimerTask task;
 
 	/*
 	 * Resolution is best to be power of 2 (e.g. 2, 4, 8, 16, etc)
 	 */
 	private int slotResolution;
 	private long slotResolutionNanoseconds;
 	private int resolutionShift;
 
 	private int numSlotsLast10secs = -1;
 	private int numSlotsLast20secs = -1;
 	private int numSlotsLast30secs = -1;
 	private int numSlotsLast40secs = -1;
 	private int numSlotsLast50secs = -1;
 	private int numSlotsLast60secs = -1;
 
 	private String name;
 
 	private AtomicLongArray slots;
 	private AtomicInteger slotNumber = new AtomicInteger(0);
 
 	private volatile long lastAccessTimestamp = System.nanoTime();
 
 	/**
 	 * Constructs a new {@link RateCounter} instance.
 	 */
 	public RateCounter() {
 	}
 
 	/**
 	 * Setter for {@link #timer}.
 	 * 
 	 * @param timer
 	 *            Timer
 	 */
 	public void setTimer(Timer timer) {
 		this.timer = timer;
 	}
 
 	/**
 	 * Getter for {@link #timer}.
 	 * 
 	 * @return Timer
 	 */
 	protected Timer getTimer() {
 		return timer;
 	}
 
 	public void destroy() {
 		if (task != null) {
 			task.cancel();
 			task = null;
 		}
 	}
 
 	/**
 	 * Initialzing method.
 	 */
 	public void init() {
 		int numSlots = 4; // min number of slots is 4
 		while (numSlots < this.numSlots) {
 			numSlots <<= 1;
 		}
 		this.numSlots = numSlots;
 		this.slots = new AtomicLongArray(this.numSlots);
 		this.numSlotsReturns = this.numSlots - NUM_SLOTS_BUFFER;
 		this.slotMask = this.numSlots - 1;
 
 		if (this.slotResolution < 1) {
 			this.slotResolution = 1;
 		}
 		this.resolutionShift = 20;
 		this.slotResolutionNanoseconds = 1 << this.resolutionShift;
 		long temp = this.slotResolution * 1000000L;
 		while (this.slotResolutionNanoseconds < temp) {
 			this.resolutionShift++;
 			this.slotResolutionNanoseconds <<= 1;
 		}
 
 		long nanoLast10secs = 10 * 1000000000L;
 		long nanoLast20secs = 20 * 1000000000L;
 		long nanoLast30secs = 30 * 1000000000L;
 		long nanoLast40secs = 40 * 1000000000L;
 		long nanoLast50secs = 50 * 1000000000L;
 		long nanoLast60secs = 60 * 1000000000L;
 		numSlotsLast10secs = -1;
 		numSlotsLast20secs = -1;
 		numSlotsLast30secs = -1;
 		numSlotsLast40secs = -1;
 		numSlotsLast50secs = -1;
 		numSlotsLast60secs = -1;
 		long nanoCounter = this.slotResolutionNanoseconds;
 		int slotCounter = 1;
 		while (slotCounter <= this.numSlotsReturns) {
 			if (numSlotsLast10secs == -1 && nanoCounter >= nanoLast10secs) {
 				numSlotsLast10secs = slotCounter;
 			}
 			if (numSlotsLast20secs == -1 && nanoCounter >= nanoLast20secs) {
 				numSlotsLast20secs = slotCounter;
 			}
 			if (numSlotsLast30secs == -1 && nanoCounter >= nanoLast30secs) {
 				numSlotsLast30secs = slotCounter;
 			}
 			if (numSlotsLast40secs == -1 && nanoCounter >= nanoLast40secs) {
 				numSlotsLast40secs = slotCounter;
 			}
 			if (numSlotsLast50secs == -1 && nanoCounter >= nanoLast50secs) {
 				numSlotsLast50secs = slotCounter;
 			}
 			if (numSlotsLast60secs == -1 && nanoCounter >= nanoLast60secs) {
 				numSlotsLast60secs = slotCounter;
 			}
 			slotCounter++;
 			nanoCounter += this.slotResolutionNanoseconds;
 		}
 
 		slotNumber.set(calcSlotNumber(System.nanoTime()));
 
 		if (timer != null) {
 			long delay = (numSlots / 4) * slotResolution;
 			task = new IdleUpdateTask(this);
 			timer.scheduleAtFixedRate(task, delay, delay);
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getNumSlots() {
 		return numSlots;
 	}
 
 	public void setNumSlots(int numSlots) {
 		this.numSlots = numSlots;
 	}
 
 	/**
 	 * Gets number of slots that covers the last 10 seconds.
 	 * 
 	 * @param numSlots
 	 *            int
 	 */
 	public int getNumSlotsLast10secs() {
 		return numSlotsLast10secs;
 	}
 
 	/**
 	 * Gets number of slots that covers the last 20 seconds.
 	 * 
 	 * @param numSlots
 	 *            int
 	 */
 	public int getNumSlotsLast20secs() {
 		return numSlotsLast20secs;
 	}
 
 	/**
 	 * Gets number of slots that covers the last 30 seconds.
 	 * 
 	 * @param numSlots
 	 *            int
 	 */
 	public int getNumSlotsLast30secs() {
 		return numSlotsLast30secs;
 	}
 
 	/**
 	 * Gets number of slots that covers the last 40 seconds.
 	 * 
 	 * @param numSlots
 	 *            int
 	 */
 	public int getNumSlotsLast40secs() {
 		return numSlotsLast40secs;
 	}
 
 	/**
 	 * Gets number of slots that covers the last 50 seconds.
 	 * 
 	 * @param numSlots
 	 *            int
 	 */
 	public int getNumSlotsLast50secs() {
 		return numSlotsLast50secs;
 	}
 
 	/**
 	 * Gets number of slots that covers the last 60 seconds.
 	 * 
 	 * @param numSlots
 	 *            int
 	 */
 	public int getNumSlotsLast60secs() {
 		return numSlotsLast60secs;
 	}
 
 	/**
 	 * Gets slot's resolution in nanoseconds.
 	 * 
 	 * @return long the resolution in nanoseconds
 	 */
 	public long getSlotResolutionNano() {
 		return this.slotResolutionNanoseconds;
 	}
 
 	/**
 	 * Gets slot's resolution.
 	 * 
 	 * @return long the resolution in milliseconds
 	 */
 	public int getSlotResolution() {
 		return this.slotResolution;
 	}
 
 	/**
 	 * Sets slot's resolution.
 	 * 
 	 * @param resolution
 	 *            int slot's resolution in milliseconds
 	 */
 	public void setSlotResolution(int resolution) {
 		this.slotResolution = resolution;
 	}
 
 	/**
 	 * Gets the counter's last access timestamp.
 	 * 
 	 * @return long last access timestamp in nanoseconds
 	 */
 	public long getLastAccessTimestamp() {
 		return this.lastAccessTimestamp;
 	}
 
 	/**
 	 * The "hash" function to calculate the slot number from the timestamp.
 	 * 
 	 * @param timestamp
 	 *            timestamp in nanoseconds
 	 * @return int the calculated slot number
 	 */
 	private int calcSlotNumber(long timestamp) {
 		return (int) ((timestamp >> resolutionShift) & slotMask);
 	}
 
 	/**
 	 * Calculates the next slot number from the current one.
 	 * 
 	 * @param currentSlotNumber
 	 *            int
 	 * @return int
 	 */
 	private int nextSlotNumber(int currentSlotNumber) {
 		return (currentSlotNumber + 1) & slotMask;
 	}
 
 	/**
 	 * Calculates the previous slot number from the current one.
 	 * 
 	 * @param currentSlotNumber
 	 *            int
 	 * @return int
 	 */
 	private int prevSlotNumber(int currentSlotNumber) {
 		return (currentSlotNumber - 1) & slotMask;
 	}
 
 	/**
 	 * Gets the counter value of the current slot.
 	 * 
 	 * @return long
 	 */
 	public long getCounter() {
 		return slots.get(slotNumber.get());
 	}
 
 	/**
 	 * Gets the last 'n' counter values from the the current slot (inclusive).
 	 * 
 	 * @param numCounters
 	 *            int number of slots to retrieve (current slot inclusive)
 	 * @return long
 	 */
 	public long[] getCounters(int numCounters) {
 		if (numCounters < 1) {
 			numCounters = 1;
 		}
 		if (numCounters > numSlotsReturns) {
 			numCounters = numSlotsReturns;
 		}
 		long[] result = new long[numCounters];
 		int currentSlotNumber = slotNumber.get();
 		for (int temp = numCounters - 1; temp >= 0; temp--) {
 			result[temp] = slots.get(currentSlotNumber);
 			currentSlotNumber = prevSlotNumber(currentSlotNumber);
 		}
 		return result;
 	}
 
 	/**
 	 * Gets counter values that cover last 10 seconds.
 	 * 
 	 * @return long[]
 	 */
 	public long[] getCountersLast10secs() {
 		return numSlotsLast10secs > 0 ? getCounters(numSlotsLast10secs) : null;
 	}
 
 	/**
 	 * Gets counter values that cover last 20 seconds.
 	 * 
 	 * @return long[]
 	 */
 	public long[] getCountersLast20secs() {
 		return numSlotsLast20secs > 0 ? getCounters(numSlotsLast20secs) : null;
 	}
 
 	/**
 	 * Gets counter values that cover last 30 seconds.
 	 * 
 	 * @return long[]
 	 */
 	public long[] getCountersLast30secs() {
 		return numSlotsLast30secs > 0 ? getCounters(numSlotsLast30secs) : null;
 	}
 
 	/**
 	 * Gets counter values that cover last 40 seconds.
 	 * 
 	 * @return long[]
 	 */
 	public long[] getCountersLast40secs() {
 		return numSlotsLast40secs > 0 ? getCounters(numSlotsLast40secs) : null;
 	}
 
 	/**
 	 * Gets counter values that cover last 50 seconds.
 	 * 
 	 * @return long[]
 	 */
 	public long[] getCountersLast50secs() {
 		return numSlotsLast50secs > 0 ? getCounters(numSlotsLast50secs) : null;
 	}
 
 	/**
 	 * Gets counter values that cover last 60 seconds.
 	 * 
 	 * @return long[]
 	 */
 	public long[] getCountersLast60secs() {
 		return numSlotsLast60secs > 0 ? getCounters(numSlotsLast60secs) : null;
 	}
 
 	/**
 	 * Increases counter by 1 and returns the post-inc value.
 	 * 
 	 * @return
 	 */
 	public long incCounter() {
 		return incCounter(1);
 	}
 
 	/**
 	 * Increases counter by a specific value and returns the post-inc value.
 	 * 
 	 * @param value
 	 *            long
 	 * @return
 	 */
	public long incCounter(long value) {
 		long timestamp = System.nanoTime();
 		int currentSlotNumber = slotNumber.get();
 		int newSlotNumber = calcSlotNumber(timestamp);
 		slotNumber.set(newSlotNumber);
 
		long result = 1;
 		if (currentSlotNumber != newSlotNumber) {
 			// set value to slot number
 			slots.set(newSlotNumber, value);
 		} else {
 			// add value to slot number
 			result = slots.addAndGet(newSlotNumber, value);
 		}
 
		// reset idle slots
		while (currentSlotNumber != newSlotNumber) {
			currentSlotNumber = nextSlotNumber(currentSlotNumber);
			slots.set(currentSlotNumber, 0);
		}

 		// update last access timestamp
 		lastAccessTimestamp = timestamp;
 		return result;
 	}
 
 	static class IdleUpdateTask extends TimerTask {
 		private RateCounter rateCounter;
 
 		public IdleUpdateTask(RateCounter rateCounter) {
 			this.rateCounter = rateCounter;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void run() {
 			rateCounter.incCounter(0);
 		}
 	}
 
 	public static void main(String[] atgv) throws InterruptedException {
 		Timer timer = new Timer(true);
 		RateCounter counter = new RateCounter();
 		counter.setTimer(timer);
 		counter.setNumSlots(128);
 		counter.setSlotResolution(100);
 		counter.init();
 		for (int i = 0; i < 10000; i++) {
 			System.out.println(i);
 			counter.incCounter();
 			Thread.sleep(3);
 		}
 		long resolutionNano = counter.getSlotResolutionNano();
 		double resolutionMillis = resolutionNano / 1000000.0;
 		System.out.println("Resolution Nano  :" + resolutionNano);
 		System.out.println("Resolution Millis:" + resolutionMillis);
 		System.out.println("Rate: " + counter.slots);
 		// System.out.print("Rate: ");
 		// for (int i = 0; i < counter.slots.length(); i++) {
 		// System.out.print((double) counter.slots.get(i) / resolutionMillis);
 		// System.out.print(",");
 		// }
 	}
 }
