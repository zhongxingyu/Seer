 package edu.ntnu.EASY.neuron;
 
 import java.io.PrintStream;
 import java.util.Arrays;
 
 import edu.ntnu.EASY.Population;
 import edu.ntnu.EASY.Report;
 import edu.ntnu.EASY.individual.Individual;
 
 public final class NeuronReport implements Report<double[],double[]>{
 
 	private Entry[] entries;
 	
 	public NeuronReport(int generations){
 		entries = new Entry[generations + 1];
 	}
 	
 	@Override
 	public void log(int generation, Population<double[], double[]> population) {
 		double average = 0.0;
 		double bestFitness = population.get(0).getFitness();
 		double[] bestGenome = population.get(0).getGenome();
 		double[] bestPhenome = population.get(0).getPhenome();
 		for(Individual<double[], double[]> individual : population){
 			average += individual.getFitness();
 			if(individual.getFitness() > bestFitness){
 				bestFitness = individual.getFitness();
 				bestGenome = individual.getGenome();
 				bestPhenome = individual.getPhenome();
 			}
 		}
		System.out.printf("%d/%d - %.2f%n",generation,entries.length,bestFitness);
 		entries[generation] = new Entry(bestGenome,bestPhenome,average / population.size());		
 	}
 
 	@Override
 	public void writeToStream(PrintStream out) {
 		for (int i = 1; i < entries.length; i++) {
 			out.printf("%4d | %.3f | %s%n",i,entries[i].averageFitness,Arrays.toString(entries[i].bestGenome));
 		}
 	}
 
 	public double[] getBestPhenome() {
 		return entries[entries.length - 1].bestPhenome;
 	}
 	
 	public static class Entry {
 		
 		private double[] bestGenome;
 		private double[] bestPhenome;
 		private double averageFitness;
 		
 		Entry(double[] bestGenome,double[] bestPhenome, double averageFitness){
 			this.bestGenome = bestGenome;
 			this.bestPhenome = bestPhenome;
 			this.averageFitness = averageFitness;
 		}
 		
 	}
 
 
 
 }
