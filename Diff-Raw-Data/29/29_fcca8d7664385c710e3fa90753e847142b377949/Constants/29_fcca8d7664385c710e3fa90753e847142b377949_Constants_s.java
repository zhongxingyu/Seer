 import java.util.ArrayList;
 
 
 public class Constants {
 	/**
 	 * Most of these values can be specified at runtime by passing certain arguments
 	 * See class ArgCheck for more information
 	 */
 	public static int _numnodes = 5000;
 	public static int _avgdegree = 5;
 	public static int _edges = (_numnodes * _avgdegree)/2;
 	public static int groupRatio = 50;
 	public static int _groups = _numnodes / groupRatio;
 	public static boolean DynamicGroups = false;
 	
 	public double _SIM_epsilon_start = .025;
 	public double _SIM_epsilon_final = 1;
 	public static double _SIM_epsilon_step = .0025;
 	
 	public static final int _iterations = 100;
 	public static int _trials = 50;
 	public double _epsilon = _SIM_epsilon_start;
 	public static boolean _murand = false;
 	public static double _mu = .5;
 	
 	public static double _p_ext = (double)_avgdegree / (_numnodes-1) * 2;
 	public static boolean Repulsive = false;
 	public static int repuslivePer = 50;
 	public static boolean ConstantEp = false;
 	public static int numThreads = 3;
 	public static boolean verbose = false; //Specifies if any threads are verbose
 	public static int threadVerbose = -1; // Specifies a specific thread num to be verbose(only one)
 	public static int numThreadsVerbose = 1; // Specifies a number of threads to be verbose
 	/**NumThreadsVerbose will make the first n threads verbose, I.E. 2 will make 0 and 1
 	 verbose **/
 	
 	//stuff for data collection
	public static String _OUTPUT_PATH = "test\\";
	public final double threshold = _epsilon * _mu / 100;
 	public static int minAgents = 5;
 	public final static boolean debug = false;
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
 	
 	public boolean increment() {
 		return true;
 	}
 	
 	private abstract class indpVar {
 		protected boolean initialized = false;
 		protected double indpVal = 0;
 		public abstract void initialize(double start, double fin, double step);
 		public indpVar() {}
 		
 		public abstract boolean increment();
 		/*public abstract double getStart();
 		public abstract double getFinal();
 		public abstract double getStep();//*/
 	}
 	
 	private class indpEpsilon extends indpVar {
 		private double _epsilon_start = .025;
 		private double _epsilon_final = 1;
 		private double _epsilon_step = .0025;
 		
 		public void initialize(double start, double fin, double step) {
 			if(initialized) return;
 			initialized = true;
 			_epsilon_start = start;
 			_epsilon_final = fin;
 			_epsilon_step = step;
 			indpVal = start;
 		}
 		
 		public boolean increment() {
 			if(indpVal >= _epsilon_final) return false;
 			indpVal += _epsilon_step;
 			
 			return true;
 		}
 	}
 	
 	private class indpMu extends indpVar {
 		private double _mu_start = 0;
 		private double _mu_final =  0.98;
 		private double _mu_step = 0.01;
 		
 		public void initialize(double start, double fin, double step) {
 			if(initialized) return;
 			initialized = true;
 			_mu_start = start;
 			_mu_final = fin;
 			_mu_step = step;
 		}
 		
 		public boolean increment() {
 			if(indpVal >= _mu_final) return false;
 			indpVal += _mu_step;
 			_mu = indpVal;
 			return true;
 		}
 	}
 	
 	private class indpAvgDegree extends indpVar {
 		private double _avgdegree_start = 0;
 		private double _avgdegree_final = 50;
 		private double _avgdegree_step = 5;
 		
 		public void initialize(double start, double fin, double step) {
 			if(initialized) return;
 			initialized = true;
 			_avgdegree_start = start;
 			_avgdegree_final = fin;
 			_avgdegree_step = step;
 			
 			indpVal = _avgdegree_start;
 		}
 		
 		public boolean increment() {
 			if(indpVal >= _avgdegree_final) return false;
 			_avgdegree += _avgdegree_step;
 			return true;
 		}
 	} //*/
 	
 	
 }
