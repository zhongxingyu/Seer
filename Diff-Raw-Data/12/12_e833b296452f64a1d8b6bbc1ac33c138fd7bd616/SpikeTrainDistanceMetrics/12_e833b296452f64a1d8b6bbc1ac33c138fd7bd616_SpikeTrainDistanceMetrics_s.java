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
 		phenotypeSpikePositions = new ArrayList<Integer>();
 		phenotypeSpikeTrain = new ArrayList<Double>();
 		readTrainingData(1);
 		findTargetSpikePosition();
 		printTargetSpikesPosition();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void findPhenotypeSpikePosition(ArrayList<Double> newPhenotypeSpikeTrain){
 		phenotypeSpikePositions.clear();
 		phenotypeSpikeTrain.clear();
 		phenotypeSpikeTrain = (ArrayList<Double>) newPhenotypeSpikeTrain.clone();
 		phenotypeSpikePositions = findSpikePositions(phenotypeSpikePositions,phenotypeSpikeTrain);
 
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
 	
 	public double spikeIntervalDistanceMetrics(){
 		double distanceValue = 0;
 		double p = 2;
 		double N = Math.min(phenotypeSpikePositions.size(), targetSpikePositions.size());
 		double sumOfDifferences = 0;
 		
 		for (int i = 1; i < N; i++) {
 			double temp = 0;
 			temp = Math.abs((targetSpikePositions.get(i)- targetSpikePositions.get(i-1)) - (phenotypeSpikePositions.get(i) - phenotypeSpikePositions.get(i-1)));
 			temp = Math.pow(temp, p);
 			sumOfDifferences = temp;
 		}
 		distanceValue = Math.pow(sumOfDifferences, (1/p));
 		distanceValue += spikeCountDifferancePenalty();
 		
 		distanceValue = (1/(N-1)) * distanceValue;
 		
 		return distanceValue;
 	}
 	
 	public double waveFormDistanceMetrics(){
 		double distanceValue = 0;
 		double M = trainingSpikeTrain.size();
 		double p = 2;
 		double sumOfDifferenses = 0;
 		
 		for (int i = 0; i < M; i++) {
 			sumOfDifferenses += Math.abs(trainingSpikeTrain.get(i) - phenotypeSpikeTrain.get(i));
 			sumOfDifferenses = Math.pow(sumOfDifferenses, p);
 		}
 		distanceValue = (1 / M) * Math.pow(sumOfDifferenses, (1/p));
 		
 		return distanceValue;
 	}
 	
 	public double spikeCountDifferancePenalty(){
 		double penalty = 0;
 		double spikeDifferense = 0;
 		double minimumSpikes = 0;
 		if (phenotypeSpikePositions.size()<targetSpikePositions.size()){
 			spikeDifferense = targetSpikePositions.size() - phenotypeSpikePositions.size();
 			minimumSpikes = phenotypeSpikePositions.size();
 		}else{
 			spikeDifferense = phenotypeSpikePositions.size()- targetSpikePositions.size();
 			minimumSpikes = targetSpikePositions.size();
 		}
 		
 		
 		penalty = (spikeDifferense*phenotypeSpikeTrain.size())/(2*minimumSpikes);
 		
 		return penalty;
 	}
 	
 	public double getFintesss(int method){
 		double fitness = 0;
 		if(method==1 ){
 			fitness = spikeTimeDistanceMetrics();
 		}else if(method==2){
 			fitness = spikeIntervalDistanceMetrics();
 		}else{
 			fitness = waveFormDistanceMetrics();
 		}
 		
 		fitness = 1/(fitness+1);
 		return fitness;
 	}
 	
 	public ArrayList<Integer> getSpikePosition(){
 		return phenotypeSpikePositions;
 	}
 	
 	public void printTargetSpikesPosition(){
 		String newString ="";
 		newString +="\n\nTargetspikeTrainPositions\n";
 		for (int i = 0; i < targetSpikePositions.size(); i++) {
 			newString += targetSpikePositions.get(i)+", ";
 		}
 		System.out.println(newString);
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
