 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase.neat;
 
 import ec.EvolutionState;
 import ec.Individual;
 import ec.Population;
import ec.simple.SimpleBreeder;
 import java.util.HashMap;
 import java.util.Map;
 import org.neat4j.neat.core.NEATChromosome;
 import org.neat4j.neat.ga.core.Chromosome;
 
 /**
  *
  * @author jorge
  */
public class NEATBreeder extends SimpleBreeder {
 
     @Override
     public Population breedPopulation(EvolutionState state) {
         Population newpop = (Population) state.population.emptyClone();
         for (int i = 0 ; i < state.population.subpops.length ; i++) {
             NEATSubpop sub = (NEATSubpop) state.population.subpops[i];
             // Get scores from individuals
             Map<Chromosome, Float> scores = new HashMap<Chromosome, Float>(sub.individuals.length * 2);
             for(Individual ind : sub.individuals) {
                 NEATIndividual ni = (NEATIndividual) ind;
                 scores.put(ni.getChromosome(), ni.fitness.fitness());
             }
 
             // Create fitness function with the scores and plug in
             PreEvaluatedFitnessFunction fit = new PreEvaluatedFitnessFunction(scores);
             sub.getNEAT().pluginFitnessFunction(fit);
 
             // Run Epoch
             sub.getNEAT().runEpoch();
             
             // Create new population
             for(int j = 0 ; j < sub.individuals.length ; j++) {
                 newpop.subpops[i].individuals[j] = sub.species.newIndividual(state, 0);
                 ((NEATIndividual) newpop.subpops[i].individuals[j])
                         .setChromosome((NEATChromosome) sub.getNEAT().population().genoTypes()[j]);
             }
         }
         return newpop;
     }
 }
