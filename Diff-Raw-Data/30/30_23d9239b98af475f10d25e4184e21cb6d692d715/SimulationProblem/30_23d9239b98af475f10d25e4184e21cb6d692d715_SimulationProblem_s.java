 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mase;
 
 import ec.EvolutionState;
 import ec.Individual;
 import ec.Population;
 import ec.Problem;
 import ec.coevolve.GroupedProblemForm;
 import ec.simple.SimpleProblemForm;
 import ec.util.Parameter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import mase.controllers.HeterogeneousGroupController;
 import mase.controllers.HomogeneousGroupController;
 
 /**
  *
  * @author Jorge Gomes, FC-UL <jorgemcgomes@gmail.com>
  */
 public abstract class SimulationProblem extends Problem implements GroupedProblemForm, SimpleProblemForm {
 
     public static final String P_EVAL_NUMBER = "number-evals";
     public static final String P_EVAL = "eval";
     protected EvaluationFunction[] evalFunctions;
     public static final String P_TRIALS_MERGE = "trials-merge";
     public static final String V_BEST = "best", V_MEAN = "mean", V_MEDIAN = "median";
     public static final int MERGE_BEST = 0, MERGE_MEAN = 1, MERGE_MEDIAN = 2;
     protected int mergeMode;
 
     @Override
     public void setup(EvolutionState state, Parameter base) {
         super.setup(state, base);
         base = defaultBase();
 
         /* evaluation */
         if (!state.parameters.exists(base.push(P_EVAL_NUMBER), null)) {
             state.output.warning("Parameter not exists. Going to use just 1 evaluation.", base.push(P_EVAL_NUMBER));
         }
         int nEvals = state.parameters.getIntWithDefault(base.push(P_EVAL_NUMBER), null, 1);
         evalFunctions = new EvaluationFunction[nEvals];
         for (int i = 0; i < nEvals; i++) {
             evalFunctions[i] = (EvaluationFunction) state.parameters.getInstanceForParameter(base.push(P_EVAL).push("" + i), base.push(P_EVAL), EvaluationFunction.class);
             evalFunctions[i].setup(state, base.push(P_EVAL).push("" + i));
         }
 
         /* trial merge */
         if (!state.parameters.exists(base.push(P_TRIALS_MERGE), null)) {
             mergeMode = MERGE_BEST;
             state.output.warning("Parameter not found. Going with mean.", base.push(P_TRIALS_MERGE));
         } else {
             String val = state.parameters.getString(base.push(P_TRIALS_MERGE), null);
             if (val.equals(V_BEST)) {
                 mergeMode = MERGE_BEST;
             } else if (val.equals(V_MEAN)) {
                 mergeMode = MERGE_MEAN;
             } else if (val.equals(V_MEDIAN)) {
                 mergeMode = MERGE_MEDIAN;
             } else {
                 state.output.fatal("Unknown parameter value.", base.push(P_TRIALS_MERGE));
             }
         }
     }
     
 
     @Override
     public void preprocessPopulation(EvolutionState state, Population pop, boolean[] prepareForFitnessAssessment, boolean countVictoriesOnly) {
         for (int i = 0; i < pop.subpops.length; i++) {
             for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                 pop.subpops[i].individuals[j].fitness.trials = new ArrayList();
             }
         }
     }
 
     @Override
     public void postprocessPopulation(EvolutionState state, Population pop, boolean[] assessFitness, boolean countVictoriesOnly) {
         for (int i = 0; i < pop.subpops.length; i++) {
             for (int j = 0; j < pop.subpops[i].individuals.length; j++) {
                 Individual ind = pop.subpops[i].individuals[j];
                 ExpandedFitness[] trials = Arrays.copyOf(ind.fitness.trials.toArray(), ind.fitness.trials.size(), ExpandedFitness[].class);
                 for (int k = 0; k < trials.length; k++) {
                     trials[k] = (ExpandedFitness) ind.fitness.trials.get(k);
                 }
                 switch (mergeMode) {
                     case MERGE_MEAN:
                         ind.fitness.setToMeanOf(state, trials);
                         break;
                     case MERGE_MEDIAN:
                         ind.fitness.setToMedianOf(state, trials);
                         break;
                     case MERGE_BEST:
                         ind.fitness.setToBestOf(state, trials);
                 }
                 ind.evaluated = true;
                 ((ExpandedFitness) ind.fitness).setCorrespondingSubpop(i);
             }
         }
     }
 
     @Override
     public void evaluate(EvolutionState state, Individual[] ind, boolean[] updateFitness, boolean countVictoriesOnly, int[] subpops, int threadnum) {
         /* Perform simulation */
         AgentController[] acs = new AgentController[ind.length];
         for (int i = 0; i < ind.length; i++) {
             acs[i] = ((AgentControllerIndividual) ind[i]).decodeController();
         }
         HeterogeneousGroupController controller = new HeterogeneousGroupController(acs);
         EvaluationResult[] eval = evaluateSolution(controller, state.random[threadnum].nextLong());
 
         /* Save results */
         for (int i = 0; i < ind.length; i++) {
             if (updateFitness[i]) {
                 ExpandedFitness trial = (ExpandedFitness) ind[i].fitness.clone();
                 trial.setEvaluationResults(eval);
                 trial.setFitness(state, trial.getFitnessScore(), false);
                 trial.setContext(ind);
                 ind[i].fitness.trials.add(trial);
             }
         }
     }
 
     @Override
     public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
         AgentControllerIndividual aci = (AgentControllerIndividual) ind;
         HomogeneousGroupController hc = new HomogeneousGroupController(aci.decodeController());
         EvaluationResult[] eval = evaluateSolution(hc, state.random[threadnum].nextLong());
         ExpandedFitness fit = (ExpandedFitness) ind.fitness;
         fit.setEvaluationResults(eval);
         fit.setFitness(state, fit.getFitnessScore(), false);
         ind.evaluated = true;
     }
 
     public abstract EvaluationResult[] evaluateSolution(GroupController gc, long seed);
 }
