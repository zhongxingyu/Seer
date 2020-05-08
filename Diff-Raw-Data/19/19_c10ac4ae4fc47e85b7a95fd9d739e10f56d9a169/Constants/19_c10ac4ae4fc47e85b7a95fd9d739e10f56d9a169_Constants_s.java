 import java.util.ArrayList;
 
 
 public class Constants {
 	/**
 	 * Most of these values can be specified at runtime by passing certain arguments
 	 * See class ArgCheck for more information
 	 */
 	public static int _numnodes = 500;
 	public static int _avgdegree = 5;
 	public static int _edges = (_numnodes * _avgdegree)/2;
	public static int _groups = _numnodes / 50;
 	
 	public double _SIM_epsilon_start = .025;
 	public double _SIM_epsilon_final = 1;
 	public static double _SIM_epsilon_step = .0025;
 	
 	public static final int _iterations = 100;
 	public static int _trials = 50;
 	public double _epsilon = _SIM_epsilon_start;
 	public static boolean _murand = false;
	public static final double _mu = .5;
 	
	public static double _p_ext = (double)_avgdegree / (_numnodes-1);
 	public static boolean Repulsive = false;
 	public static int repuslivePer = 50;
 	public static boolean ConstantEp = false;
	public static int numThreads = 4;
 	public static boolean verbose = false; //Specifies if any threads are verbose
 	public static int threadVerbose = -1; // Specifies a specific thread num to be verbose(only one)
 	public static int numThreadsVerbose = 1; // Specifies a number of threads to be verbose
 	/**NumThreadsVerbose will make the first n threads verbose, I.E. 2 will make 0 and 1
 	 verbose **/
 	
 	//stuff for data collection
 	public static String _OUTPUT_PATH = "test\\";
	public final double threshold = _epsilon * _mu / 10;
	public final static boolean debug = true;
 	public static ArrayList<String> files = new ArrayList<String>();
 	public static double randMuStart = 0;
 	public static double randMuEnd = 1;
 	public static double muIncS = 0;
 	public static double muIncUp = .01;
 	public static boolean muCheck = false;
 	public static boolean migrateSwitch = true;
 	
 	public static void resetVals(int nodes, int trials) {
 		_numnodes = nodes;
 		_edges = (_numnodes * _avgdegree)/2;
 		_groups = _numnodes / 50;
 		_p_ext = (double)_avgdegree / (_numnodes-1);
 		_trials = trials;
 	}
 }
