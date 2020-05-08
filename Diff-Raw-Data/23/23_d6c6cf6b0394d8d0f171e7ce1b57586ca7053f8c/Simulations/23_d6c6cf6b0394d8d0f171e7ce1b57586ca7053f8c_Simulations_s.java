 package com.jsravn.mc;
 
 /**
  * Factory class for generating simulation related objects.
  */
 public final class Simulations {
     private Simulations() { }
 
     /**
      * Returns a new simulator that will run the given simulation.
      * @param sim the Simulation the simulator will run.
      */
    public static Simulator newSimulator(Simulation sim);
 }
