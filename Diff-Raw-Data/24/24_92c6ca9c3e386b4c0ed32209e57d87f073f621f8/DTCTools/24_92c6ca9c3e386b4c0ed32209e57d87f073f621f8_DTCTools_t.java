 package no.ntnu.ai.classifiers.dtc;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import no.ntnu.ai.data.DataElement;
 
 
 public class DTCTools<T extends Number, T2> {
 	
 	public double entropy(List<DataElement<T, T2>> list){
 		Map<T2, Integer> occurs = new HashMap<T2, Integer>();
 		for(DataElement<T, T2> dat : list){
 			T2 classi = dat.getClassification();
 			if(!occurs.containsKey(classi)){
 				occurs.put(classi, 0);
 			}
 			occurs.put(classi, occurs.get(classi) + 1);
 		}
 		double sum = 0.0;
 		for(T2 classi : occurs.keySet()){
 			sum += this.calc(occurs.get(classi), list.size());
 		}
 		return -sum;
 	}
 	
 	private double calc(double occurs, double total){
 		double tmp = occurs / total;
		return tmp*(Math.log10(tmp)/Math.log10(2));
 	}
 
 }
