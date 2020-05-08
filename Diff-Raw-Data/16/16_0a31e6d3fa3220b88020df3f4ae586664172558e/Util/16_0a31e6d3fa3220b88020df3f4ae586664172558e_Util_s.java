 package environment;
 
 /**
  * Static variables referenced in all parts of the code.
  */
 public class Util {
     /* Task 1 */
 	public final static int DIM = 11;               /* Dimension of the grid world. */
 	public final static double PREYREWARD = 10.0;   /* Reward assigned to the Prey actor. */
     public static final int EPISODE_COUNT = 100;
     /* Task 2 */
 	public static double epsilon = 0.1;             /* Used in the Epsilon-Greedy implementation. */
     public final static double tau = 4;             /* Softmax action selection temperature */
 
     public final static double[] alphas = {0.1, 0.2, 0.3, 0.4, 0.5};    /* Q-Learning learning rates Task 2.1. */
     public final static double[] gammas = {0.1, 0.5, 0.7, 0.9};         /* Q-Learning discount factors Task 2.1. */
 }
