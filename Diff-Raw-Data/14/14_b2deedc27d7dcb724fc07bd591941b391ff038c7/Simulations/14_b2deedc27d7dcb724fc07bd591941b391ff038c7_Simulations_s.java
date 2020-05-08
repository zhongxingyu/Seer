 package com.jsravn.mc;
 
 /**
  * Factory class for generating simulation related objects.
  */
 public final class Simulations {
     private Simulations() { }
 
     /**
      * Returns a new Simulator that will run the given simulation.
      *
      * @param sim is the Simulation the Simulator will run.
      */
     public static Simulator newSimulator(Simulation simulation) {
 	return new StandardSimulator(simulation);
     }
 
     /**
      * Returns a new Simulator that runs the given simulation using threads.
      *
      * A threaded simulator will only be faster than a non-threaded simulator
      * when the simulations themselves take a long time to run. This is very
      * subjective, and should be compared to the non threaded simulator if
      * performance matters.
      *
      * @param sim is the Simulation the Simulator will run
      */
     public static Simulator newThreadedSimulator(Simulation simulation) {
 	return new ThreadedSimulator(simulation);
     }
 
     /**
      * Returns a new texed based Runner that will run the given simulator and
      * output the results on the command line. Use this instead of a Simulator
      * directly if you don't need the extra flexibility. It is much less work
      * to use a Runner.
      *
      * @param args is an array of arguments, usually passed on the command
      * line. Their interpretation is handled by the Runner, and a help menu
      * will be output to the console if arguments are omitted.
      * @param sim is the Simulator to run
      */
     public static Runner newTextRunner(String[] args, Simulator simulator) {
 	return new SimpleRunner(args, simulator);
     }
 
     /**
      * Returns a new text based Runner using the standard simulator returned
     * by newSimulator. This is equivalent to:
     * <code>newTextRunner(args, Simulations.newSimulator(simulation))</code>
      *
      * @param sim is the Simulation
      */
     public static Runner newTextRunner(String[] args, Simulation simulation) {
 	return new SimpleRunner(args, newSimulator(simulation));
     }
 
     /**
      * Returns a new text based Runner using the threaded simulator returned
     * by newThreadedSimulator. This is equivalent to:
     * <code>newTextRunner(args, Simulations.newThreadedSimulator(simulation))
     * </code>
      *
      * @param sim is the Simulation
      */
     public static Runner newThreadedTextRunner(String[] args,
 					       Simulation simulation) {
 	return new SimpleRunner(args, newThreadedSimulator(simulation));
     }
 }
