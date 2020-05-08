 package edu.ntnu.EASY.blotto;
 
 import edu.ntnu.EASY.Environment;
 import edu.ntnu.EASY.Evolution;
 import edu.ntnu.EASY.FitnessCalculator;
 import edu.ntnu.EASY.incubator.Incubator;
 import edu.ntnu.EASY.selection.adult.AdultSelector;
 import edu.ntnu.EASY.selection.adult.FullGenerationalReplacement;
 import edu.ntnu.EASY.selection.parent.FitnessProportionateSelector;
 import edu.ntnu.EASY.selection.parent.ParentSelector;
 
 public class Blotto {
 
 	Environment env;
 	
 	public Blotto(){
 		env = new Environment();
 		env.populationSize = 20;
 		env.maxGenerations = 500;
 		env.fitnessThreshold = 2.0;
 		env.mutationRate = 0.005;
 		env.crossoverRate = 0.005;
 		env.numChildren = 20;
 		env.numParents = 10;
 		env.elitism = 10;
 	}
 	
 	public BlottoReport runBlottoEvolution(int B, double Rf, double Lf) {
 		FitnessCalculator<double[]> fitCalc = new BlottoFitnessCalculator(Rf,Lf);
 		AdultSelector<double[]> adultSelector = new FullGenerationalReplacement<double[]>(env.elitism);
 		ParentSelector<double[]> parentSelector = new FitnessProportionateSelector<double[]>(env.numParents);
		Incubator<double[], double[]> incubator = new BlottoIncubator(new BlottoReplicator(B), env.numChildren);	
 		Evolution<double[],double[]> evo = new Evolution<double[], double[]>(fitCalc, adultSelector, parentSelector, incubator);
 
 	
 		BlottoReport report = new BlottoReport(env.maxGenerations);
 		evo.runEvolution(env, report);
 		return report;
 	}
 }
