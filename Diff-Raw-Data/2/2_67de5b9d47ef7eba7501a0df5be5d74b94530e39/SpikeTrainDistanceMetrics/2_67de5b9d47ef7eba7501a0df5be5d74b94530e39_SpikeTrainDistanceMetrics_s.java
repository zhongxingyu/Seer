 package test;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class SpikeTrainDistanceMetrics {
 
 	private double activationThreshold;
 	private double kTimeStep;
 	private ArrayList<Integer> phenotypeSpikePositions;
 	private ArrayList<Integer> targetSpikePositions;
 	private ArrayList<Double> trainingSpikeTrain;
 	private ArrayList<Double> phenotypeSpikeTrain;
 	
 	public SpikeTrainDistanceMetrics(){
 		activationThreshold = 0;
 		kTimeStep = 5;
 		
 		readTrainingData(1);
 		findTargetSpikePosition();
 	}
 	
 	public void findPhenotypeSpikePosition(ArrayList<Double> phenotypeSpikeTrain){
 		phenotypeSpikePositions = new ArrayList<Integer>();
 		phenotypeSpikePositions = findSpikePositions(phenotypeSpikePositions,phenotypeSpikeTrain);
 		this.phenotypeSpikeTrain = phenotypeSpikeTrain;
 	}
 	
 	private void readTrainingData(int trainingDataSet){
 		trainingSpikeTrain = new ArrayList<Double>();
 		Scanner sc = new Scanner("");
 		try {
 			if (trainingDataSet == 1) {	
 				sc = new Scanner(new File("src/trainingData/izzy-train1.dat"));
 			}else if (trainingDataSet == 2) {
 				sc = new Scanner(new File("src/trainingData/izzy-train2.dat"));
 			}else if (trainingDataSet == 3) {
 				sc = new Scanner(new File("src/trainingData/izzy-train3.dat"));
 			}else if (trainingDataSet == 4) {
 				sc = new Scanner(new File("src/trainingData/izzy-train4.dat"));
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		while (sc.hasNext()) {
 			trainingSpikeTrain.add(sc.nextDouble());
 		}
 		
 	}
 	
 	public void findTargetSpikePosition(){
 		targetSpikePositions = new ArrayList<Integer>();
 		targetSpikePositions = findSpikePositions(targetSpikePositions,trainingSpikeTrain);
 	}
 	
 	private ArrayList<Integer> findSpikePositions(ArrayList<Integer> newList, ArrayList<Double> phenotypeSpikeTrain){
 		int trainLength = (int) (phenotypeSpikeTrain.size()-kTimeStep+1);
 		for (int i = 0; i < trainLength; i++) {
 			double maxValue = activationThreshold -1;
 			double index = -1;
 			for (int j = 0; j < kTimeStep; j++) {
 				
 				// find max value in the k-timestep 
 				if (phenotypeSpikeTrain.get(j+i)> maxValue){
 					 maxValue = phenotypeSpikeTrain.get(j+i);
 					 index = j;
 				}		 
 				
 			}
 			
 			// check if the middle value is maximum and above threshold
 			int middleIndex = (int) (kTimeStep/2);
 			if (index == middleIndex && maxValue > activationThreshold){
 				newList.add(i+middleIndex);
 			}
 			
 			
 		}
 		
 		return newList;
 	}
 	
 	
 	public double spikeTimeDistanceMetrics(){
 		int p = 2;
 		double distanceValue = 0;
 		double minimumSpikes = Math.min(phenotypeSpikePositions.size(), targetSpikePositions.size());
 		
 		for (int i = 0; i < minimumSpikes; i++) {
 			double temp = Math.abs(phenotypeSpikePositions.get(i)-targetSpikePositions.get(i));
 			distanceValue += Math.pow(temp, 2);
 		}
 		distanceValue = Math.pow(distanceValue, 1/p);
 		distanceValue += spikeCountDifferancePenalty();
 		distanceValue = (1/minimumSpikes)*distanceValue;
 		
 		return distanceValue;
 	}
 	
 	public double spikeIntervalDistanceMetrics(ArrayList<Double> neuronSpikeTrain, ArrayList<Double> targetSpikeTrain){
 		double fitnessValue = 0;
 		double p = 2;
 		double N = Math.min(phenotypeSpikePositions.size(), targetSpikePositions.size());
 		double sumOfDifferences = 0;
 		
 		for (int i = 1; i < N; i++) {
 			sumOfDifferences += Math.abs((targetSpikePositions.get(i)- targetSpikePositions.get(i-1)) - (phenotypeSpikePositions.get(i) - phenotypeSpikePositions.get(i-1)));
 			sumOfDifferences = Math.pow(sumOfDifferences, p);
 		}
 		
 		fitnessValue = Math.pow(sumOfDifferences, (1/p));
		fitnessValue += spikeCountDifferancePenalty;
 		
 		fitnessValue = (1/(N-1)) * fitnessValue;
 		
 		return fitnessValue;
 	}
 	
 	public double waveFormDistanceMetrics(){
 		double fitnessValue = 0;
 		double M = trainingSpikeTrain.size();
 		double p = 2;
 		
 		double sumOfDifferenses = 0;
 		
 		for (int i = 0; i < M; i++) {
 			sumOfDifferenses += Math.abs(trainingSpikeTrain.get(i) - phenotypeSpikeTrain.get(i));
 			sumOfDifferenses = Math.pow(sumOfDifferenses, p);
 		}
 		fitnessValue = (1 / M) * Math.pow(sumOfDifferenses, (1/p));
 		
 		return fitnessValue;
 	}
 	
 	public double spikeCountDifferancePenalty(){
 		double penelty = 0;
 		double spikeDifferense = 0;
 		double minimumSpikes = 0;
 		if (phenotypeSpikePositions.size()<targetSpikePositions.size()){
 			spikeDifferense = targetSpikePositions.size() - phenotypeSpikePositions.size();
 			minimumSpikes = phenotypeSpikePositions.size();
 		}else{
 			spikeDifferense = phenotypeSpikePositions.size()- targetSpikePositions.size();
 			minimumSpikes = targetSpikePositions.size();
 		}
 		
 		penelty = (spikeDifferense*phenotypeSpikeTrain.size())/(2*minimumSpikes);
 		
 		return penelty;
 	}
 	
 	public double getFintesss(){
 		return 0;
 	}
 	
 	public String toString(){
 		String newString ="";
 		
 		newString +="spikeTrain\n";
 		for (int i = 0; i < phenotypeSpikeTrain.size(); i++) {
 			newString += phenotypeSpikeTrain.get(i)+", ";
 		}
 		
 		newString +="\n\nspikeTrainPositions\n";
 		for (int i = 0; i < phenotypeSpikePositions.size(); i++) {
 			newString += phenotypeSpikePositions.get(i)+", ";
 		}
 		newString +="\n\n";
 		
 		return newString;
 	}
 }
