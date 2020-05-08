 package net.bpiwowar.kqp; 
 /**
  * @author B. Piwowarski <benjamin@bpiwowar.net>
  * @date 19/4/12
  */
 import net.bpiwowar.kqp.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.Properties;
 
 public class example_1 {
   
    static public void main(String [] args) {
         // Dimension of the problem
         int dim = 10;
 
         // Feature space
         DenseSpaceDouble fs = new DenseSpaceDouble(dim);
         
         // Creating an incremental builder
         System.err.format("Creating a KEVD builder%n");
         KEVDAccumulatorDouble kevd = new KEVDAccumulatorDouble(fs);
 
         // Add 10 vectors with $\alpha_i=1$
         System.err.format("Adding 10 vectors%n");
         for(int i = 0; i < 10; i++) {
             // Adds a random $\varphi_i$
             EigenMatrixDouble m = new EigenMatrixDouble(dim,1);
             m.randomize();
             kevd.add(new DenseDouble(m));
         }
 
         // Get the result $\rho \approx X Y D Y^\dagger X^\dagger$
         System.err.println("Getting the result");
         DecompositionDouble d = kevd.getDecomposition();
 
         // --- Compute a kEVD for a subspace
 
         System.err.format("Creating a KEVD builder (event)%n");
 
         KEVDAccumulatorDouble kevd_event = new KEVDAccumulatorDouble(fs);
 
         for(int i = 0; i < 3; i++) {
             // Adds a random $\varphi_i$
             EigenMatrixDouble m = new EigenMatrixDouble(dim,1);
             m.randomize();
             kevd_event.add(new DenseDouble(m));
         }
 
 
         // --- Compute some probabilities
 
 
         // Setup densities and events
         d = kevd.getDecomposition();
         System.err.println("Creating the density rho and event E");
         DensityDouble rho = new DensityDouble(kevd);
         rho.normalize();
         EventDouble event = new EventDouble(kevd_event);
         System.err.println("Computing some probabilities");
 
         // Compute the probability
         System.out.format("Probability = %g%n", rho.probability(event));
 
         // Conditional probability
        DensityDouble rho_cond = event.project(rho);
         System.out.format("Entropy of rho/E = %g%n", rho_cond.entropy());
 
         // Conditional probability (orthogonal event)
         DensityDouble rho_cond_orth = event.project(rho, true);
         System.out.format("Entropy of rho/not E = %g%n", rho_cond.entropy());
 
     }
 
 }
