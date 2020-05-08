 package engine;
 
 public class Options {
	public final boolean SEQUENTIAL_LEARNING = true;
 	public final int LEARN_TIME = 10 * 10 * 30;
 	public final int LAYERS = 1;
	public final int SENSORS = 512;
 	public final int CELLS = 3;
 
	public final double SENSOR_BOOST = 0.3;
	public final int SENSOR_WINNERS = 8;
 	
	public final int NEW_SYNAPSES = 10;
	public final int ACTIVATION_THRESHOLD = 5;
 	public final int MIN_THRESHOLD = 2;
 	
	public final int MIN_OVERLAP = 500;
	public final int PERMANENCE_INC = 10;
	public final int PERMANENCE_DEC = 5;
 	public final int PERMANENCE_CONNECTED = 10;
 	public final int PERMANENCE_INITIAL = 30;
 }
