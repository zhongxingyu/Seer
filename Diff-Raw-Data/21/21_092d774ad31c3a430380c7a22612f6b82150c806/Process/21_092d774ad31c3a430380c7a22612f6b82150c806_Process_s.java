 import java.awt.*;
 import java.util.*;
 
 /**
  * This class contains data associated with processes, and methods for
  * manipulating this data as well as methods for displaying a process in the
  * GUI.
  * 
  * You will probably want to add more methods to this class.
  */
 public class Process implements Constants {
 	/** The ID of the next process to be created */
 	private static long nextProcessId = 1;
 
 	/** The font used by all processes */
 	private static Font font = new Font("Arial", Font.PLAIN, 10);
 
 	/** The ID of this process */
 	private long processId;
 
 	/** The color of this process */
 	private Color color;
 
 	/** The amount of memory needed by this process */
 	private long memoryNeeded;
 
 	/** The amount of CPU time still needed by this process */
 	private long cpuTimeNeeded;
 
 	/** The average time between the need for I/O operations for this process */
 	private long ioInterval;
 
 	/** The time left until the next time this process needs I/O */
 	private long timeToNextIoOperation = 0;
 
 	/** The time that this process has spent waiting in the memory queue */
 	private long timeSpentInMemoryQueue = 0;
 
 	/** The time that this process has spent waiting in the CPU queue */
 	private long timeSpentInCPUQueue = 0;
 
 	/** The time that this process has spent processing */
 	private long timeSpentInCpu = 0;
 
 	/** The time that this process has spent waiting in the I/O queue */
 	private long timeSpentInIoQueue = 0;
 
 	/** The time that this process has spent performing I/O */
 	private long timeSpentInIo = 0;
 
 	/** The number of times that this process has been placed in the CPU queue */
 	private long nofTimesInReadyQueue = 0;
 
 	/** The number of times that this process has been placed in the I/O queue */
 	private long nofTimesInIoQueue = 0;
 
 	/** The global time of the last event involving this process */
 	private long timeOfLastEvent;
 	
 	private long timeAddedToSystem;
 	private long timeCreated;
 	
 	private int PREV_STATE;
 
 	/**
 	 * Creates a new process with given parameters. Other parameters are
 	 * randomly determined.
 	 * 
 	 * @param memorySize The size of the memory unit.
 	 * @param creationTime The global time when this process is created.
 	 */
 	public Process(long memorySize) {
 		timeCreated = SystemClock.getTime();
 		
 		// Memory need varies from 100 kB to 25% of memory size
 		memoryNeeded = 100 + (long) (Math.random() * (memorySize / 4 - 100));
 
 		// CPU time needed varies from 100 to 10000 milliseconds
 		cpuTimeNeeded = 100 + (long) (Math.random() * 9900);
 
 		// Average interval between I/O requests varies from 1% to 25% of CPU
 		// time needed
 		ioInterval = (1 + (long) (Math.random() * 25)) * cpuTimeNeeded / 100;
 		
 		// Time to next IO request
 		timeToNextIoOperation = generateTimeToNextIoOperation();
 		
 		// The first and latest event involving this process is its creation
 		timeOfLastEvent = SystemClock.getTime();
 
 		// Assign a process ID
 		processId = nextProcessId++;
 
 		// Assign a pseudo-random color used by the GUI
 		int red = 64 + (int) ((processId * 101) % 128);
 		int green = 64 + (int) ((processId * 47) % 128);
 		int blue = 64 + (int) ((processId * 53) % 128);
 		color = new Color(red, green, blue);
 		
 		PREV_STATE = MEMORY_QUEUE;
 	}
 	
 	private long generateTimeToNextIoOperation() {
 		return (long) (2 * Math.random() * this.ioInterval);
 	}
 	
 	/**
 	 * Process to String for debugging purposes.
 	 */
 	public String toString() {
 		String res = "process id = '" + processId + "' - ";
 		res += "RAM needed = '" + memoryNeeded + "' - ";
 		res += "CPU time needed = '" + cpuTimeNeeded + "' - ";
 		res += "avg IO interval = '" + ioInterval + "' - ";
 		res += "time of last event = '" + timeOfLastEvent + "'";
 
 		return res;
 	}
 
 	/**
 	 * Draws this process as a colored box with a process ID inside.
 	 * 
 	 * @param g The graphics context.
 	 * @param x The leftmost x-coordinate of the box.
 	 * @param y The topmost y-coordinate of the box.
 	 * @param w The width of the box.
 	 * @param h The height of the box.
 	 */
 	public void draw(Graphics g, int x, int y, int w, int h) {
 		g.setColor(color);
 		g.fillRect(x, y, w, h);
 		g.setColor(Color.black);
 		g.drawRect(x, y, w, h);
 		g.setFont(font);
 		FontMetrics fm = g.getFontMetrics(font);
 		g.drawString("" + processId, x + w / 2 - fm.stringWidth("" + processId)
 				/ 2, y + h / 2 + fm.getHeight() / 2);
 	}
 
 	/**
 	 * Get unique ID for the process
 	 * 
 	 * @return {@code long} unique identificator
 	 */
 	public long getProcessId() {
 		return this.processId;
 	}
 
 	/**
 	 * Get amount of memory needed.
 	 * 
 	 * @return The a {@code long} amount of memory needed by this process.
 	 */
 	public long getMemoryNeeded() {
 		return memoryNeeded;
 	}
 	
 	public void updateProcess(int NEW_STATE) {
 		long timePassed = SystemClock.getTime()-this.timeOfLastEvent;
 		
 		if (NEW_STATE == CPU_QUEUE) {
 			Statistics.processesPlacedInCpuQueue();
 		} else if (NEW_STATE == IO_QUEUE) {
 			Statistics.processesPlacedInIOQueue();
 		} else if (NEW_STATE == FINISHED) {
 			Statistics.processCompleted();
 			Statistics.processesTotalTimeInSystem(SystemClock.getTime()-this.timeAddedToSystem);
 		}
 		
 		if (PREV_STATE == MEMORY_QUEUE) {
 			this.timeAddedToSystem = SystemClock.getTime();
 			this.timeSpentInMemoryQueue += timePassed;
 			Statistics.processMemoryWait(timePassed);
			Statistics.processAccesspted();
 		} else if (PREV_STATE == CPU_ACTIVE) {
 			this.timeSpentInCpu += timePassed;
 			Statistics.cpuActiveTime(timePassed);
 			
 			this.timeToNextIoOperation -= timePassed;
 			this.cpuTimeNeeded -= timePassed;
 			
 			if (timeToNextIoOperation == 0) {
 				this.timeToNextIoOperation = this.generateTimeToNextIoOperation();
 			}
 		} else if (NEW_STATE == CPU_ACTIVE) {
 			this.timeSpentInCPUQueue += timePassed;
 			Statistics.processCPUWait(timePassed);
 		} else if (PREV_STATE == IO_QUEUE) {
 			this.timeSpentInIoQueue += timePassed;
 			Statistics.processIOWait(timePassed);
 		} else if (PREV_STATE == IO_ACTIVE) {
 			this.timeSpentInIo += timePassed;
 			Statistics.ioActiveTime(timePassed);
 		}
 		
 		PREV_STATE = NEW_STATE;
 		this.timeOfLastEvent = SystemClock.getTime();
 	}
 		
 	/**
 	 * 
 	 * @return
 	 */
 	public long getTimeToNextIoOperation() {
 		return timeToNextIoOperation;
 	}
 
 	/**
 	 * Get remaining time needed for this process
 	 * 
 	 * @return Returns a positive {@code long} of remaining time needed
 	 */
 	public long getRemainingCPUTime() {
 		return this.cpuTimeNeeded;
 	}
 }
