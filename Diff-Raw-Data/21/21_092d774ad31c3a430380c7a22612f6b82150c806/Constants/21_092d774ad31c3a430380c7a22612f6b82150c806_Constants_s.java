 public interface Constants 
 {
 	public static final boolean TESTING_ENABLED = true;
 	public static final long TESTING_MEMORY_SIZSE = 2048;
	public static final long TESTING_CPU_TIME = 500;
	public static final long TESTING_IO_TIME = 225;
 	public static final long TESTING_SIMULATION_LENGTH = 250000;
	public static final long TESTING_AVG_ARRIVAL_INTERVAL = 5000;
 	
 	/** Event type describing the arrival of a new process */
     public static final int NEW_PROCESS = 1;
 	/** Event type describing the completion of the active process */
     public static final int END_PROCESS = 2;
 	/** Event type describing a process switch due to the completion of a RR time quant */
     public static final int SWITCH_PROCESS = 3;
 	/** Event type describing the need for the active process to perform I/O */
     public static final int IO_REQUEST = 4;
 	/** Event type describing the end of the current I/O operation */
     public static final int END_IO = 5;
 	/** The orientation SOUTH */
 	public final static int SOUTH = 0;
 	/** The orientation WEST */
 	public final static int WEST = 1;
 	/** The orientation NORTH */
 	public final static int NORTH = 2;
 	/** The orientation EAST */
 	public final static int EAST = 3;
 	
 	public static final int MEMORY_QUEUE = 1;
 	public static final int CPU_ACTIVE = 2;
 	public static final int CPU_QUEUE = 3;
 	public static final int IO_QUEUE = 4;
 	public static final int IO_ACTIVE = 5;
 	public static final int FINISHED = 6;
 }
