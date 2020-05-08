 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase.neat;
 
 import ec.EvolutionState;
 import ec.Individual;
 import ec.util.Parameter;
 import mase.AgentController;
 import mase.AgentControllerIndividual;
 import org.neat4j.neat.core.NEATChromosome;
 import org.neat4j.neat.core.NEATNetDescriptor;
 import org.neat4j.neat.core.NEATNeuralNet;
 
 /**
  * NEATGenome wrapper
  *
  * @author jorge
  */
 public class NEATIndividual extends Individual implements AgentControllerIndividual {
 
     private NEATChromosome genome;
     private NEATNeuralNet network;
 
     @Override
     public void setup(EvolutionState state, Parameter base) {
         super.setup(state, base);
     }
 
     public void setChromosome(NEATChromosome genome) {
         this.genome = genome;
         NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
         descr.updateStructure(this.genome);
         this.network = new NEATNeuralNet();
         this.network.createNetStructure(descr);
         this.network.updateNetStructure();
     }
 
     public NEATChromosome getChromosome() {
         return genome;
     }
     
     public NEATNeuralNet getNeuralNet() {
         return network;
     }
 
     @Override
     public boolean equals(Object ind) {
         return genome.equals(genome);
     }
 
     @Override
     public int hashCode() {
         return genome.hashCode();
     }
 
     @Override
     public Parameter defaultBase() {
         return defaultBase();
     }
     
     @Override
    public AgentController decodeController() {
         NEATAgentController nac = new NEATAgentController(network);
         return nac;
     }
 }
