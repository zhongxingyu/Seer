 package no.ntnu.ai.boost;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import no.ntnu.ai.data.DataElement;
 import no.ntnu.ai.hypothesis.Generator;
 import no.ntnu.ai.hypothesis.Hypothesis;
 
 public class AdaBoost<T, T2> {
 	private final List<Generator<T, T2>> generators;
 	private final List<DataElement<T, T2>> data;
 	private final Map<Integer, Double> weights;
 	private final List<Double> errorList;
 	private boolean hasRun = false;
 	
 	public AdaBoost(List<Generator<T, T2>> generators, List<DataElement<T, T2>> data){
 		this.generators = generators;
 		this.data = data;
 		weights = new HashMap<Integer, Double>();
 		for(int i = 0; i < data.size(); i++){
 			weights.put(i, 1.0/data.size());
 		}
 		errorList = new ArrayList<Double>(data.size());
 	}
 	
 	public List<Hypothesis<T, T2>> runBooster(){
 		List<Hypothesis<T, T2>> hypotheses = new ArrayList<Hypothesis<T,T2>>();
 		for(Generator<T, T2> g : this.generators){
 			Hypothesis<T, T2> current = g.generateHypothesis(this.data, this.weights);
 			updateWeights(current);
 			hypotheses.add(current);
 		}
 		hasRun = true;
 		return hypotheses;
 	}
 	
 	private void updateWeights(Hypothesis<T, T2> h){
 		double error = 0;
 		for(int i = 0; i < this.weights.size(); i++){
 			DataElement<T, T2> d = data.get(i);
			if(!h.classify(d).equals(d.getClassification())){
 				error += weights.get(i);
 			}
 		}
		for(int i = 0; i < this.weights.size(); i++){
			DataElement<T, T2> d = data.get(i);
			if(h.classify(d).equals(d.getClassification())){
				this.weights.put(i, this.weights.get(i)*(error/(1-error)));
			}
		}
 		double sum = 0;
 		for(Double w : this.weights.values()){
 			sum += w;
 		}
 		for(int classi : weights.keySet()){
 			this.weights.put(classi, weights.get(classi)/sum);
 		}
 		errorList.add(error);
 		h.setWeight(Math.log((1-error)/error)/Math.log(2));
 	}
 	
 	public double getAvg(){
 		if(!hasRun){
 			throw new RuntimeException("Adaboost algorithm must run before " +
 					"calculating average");
 		}
 		double sum = 0.0;
 		for(Double d : errorList){
 			sum += d;
 		}
 		return sum / errorList.size();
 	}
 	
 	public double getStdDev(){
 		if(!hasRun){
 			throw new RuntimeException("Adaboost algorithm must run before " +
 					"calculating average");
 		}
 		double sum = 0.0;
 		double avg = this.getAvg();
 		for(Double d : errorList){
 			sum += Math.pow(d - avg, 2);
 		}
 		return Math.sqrt(sum / errorList.size());
 	}
 
 }
