 import java.util.HashMap;
 import java.util.Map;
 import java.io.*;
 
 public class NaiveBayes {
 
 	/**
 	 * @param args
 	 */
	class pair{
 		public String name;
 		public float val;
 	}
 
	protected void naiveBayes(String[] dt, String[] dr, String[] l, String[] test,File[] testnames, Map<String,String> results){
 		pair[] DTFeature = featureSet(dt);
 		pair[] DRFeature = featureSet(dr);
 		pair[] LFeature = featureSet(l);
 
 		int featureLen = DTFeature.length;
 		String ckey = "cProb";
 
 		Map<String, Double> DTProb = new HashMap<String, Double>();
 		Map<String, Double> DRProb = new HashMap<String, Double>();
 		Map<String, Double> LProb = new HashMap<String, Double>();
 
 		docBools(DTProb, dt,DTFeature,ckey);
 		docBools(DRProb, dr,DRFeature,ckey);
 		docBools(LProb, l,LFeature,ckey);
 
 		for(int i = 0; i < test.length; i++){
 			String[] d = test[i].split(" ");
 			Map<String, Double> Prob = new HashMap<String, Double>();
 
 			for(int j = 0; j < featureLen; j++){
 				boolean dtTruth = false;
 				boolean drTruth = false;
 				boolean lTruth = false;
 				for(int k = 0; k < d.length; k++){
 					if(d[k].equals(DTFeature[j].name) && dtTruth == false){
 						probInc(Prob, j, DTFeature, "dt",DTProb);
 						dtTruth = true;
 
 					}else if(d[k].equals(DRFeature[j].name) && drTruth == false){
 						probInc(Prob, j, DRFeature,"dr",DRProb);
 						drTruth = true;
 
 					}else if(d[k].equals(LFeature[j].name) && lTruth == false){
 						probInc(Prob, j, LFeature,"l",LProb);
 						lTruth = true;
 
 					}
 				}
 			}
 			double dtVal = Prob.get("dt") / DTProb.get(ckey);
 			double drVal = Prob.get("dr") / DRProb.get(ckey);
 			double lVal = Prob.get("l") / LProb.get(ckey);
 
 			if((dtVal > drVal) && (dtVal > lVal)){
 				results.put(testnames[i].getName(), "DT");
 			}else if((dtVal > drVal) && (dtVal > lVal)){
 				results.put(testnames[i].getName(), "DR");
 			}else if((dtVal > drVal) && (dtVal > lVal)){
 				results.put(testnames[i].getName(), "L");
 			}
 		}
 
 	}
	protected void probInc(Map<String,Double> a, int j, pair[] feature,String key, Map<String,Double> prob){
 		Double val = a.get(key);
 		if(val == null){ val = 0.0;}
 		Double probVal = prob.get(feature[j].name);
 		val *= probVal;
 		a.put(key, val);
 	}
 
	protected void docBools(Map<String,Double> a, String[] docs,pair[] feature,String ckey){
 		Map<String, Integer> bools = new HashMap<String, Integer>();
 
 		for(int i = 0; i < docs.length; i++){
 			String[] d = docs[i].split(" ");
 
 			for(int j = 0; j < feature.length; j++){
 				for(int k = 0; k < d.length; k++){
 					if((feature[j].name).equals(d[k])){
 						break;
 					}
 				}
 				inc(bools,feature[j].name);
 			}
 		}
 		int docNum = docs.length;
 		for(int i = 0; i < feature.length; i++){
 			Integer value = bools.get(feature[i].name);
 			if(value == 0){
 				value = 1;
 			}else{
 				Double val = a.get(ckey);
 				if(val == null){ val = 0.0;}
 				val +=1;
 				a.put(ckey, val);
 
 			}
 			double prob = value / docNum;
 			a.put(feature[i].name, prob);
 		}
 		double cProbability = a.get(ckey) / docNum;
 		a.put(ckey, cProbability);
 	}
	private void inc(Map<String, Integer> c, String key)
     {
         Integer value = c.get(key);
         if (value == null)
             value = 0;
         value++;
         c.put(key, value);
     }
 
	protected pair[] featureSet(String[] allDocs){
 		Map<String, Integer> dict = new HashMap<String, Integer>(); //The Hashmap that stores key and value pair
 		int totalWords = 0; 	//Holds the total number of words in all documents
 
 		//Create a class called pair that will hold the name and value of each word.
 
 
 		//Goes through each document and counts the occurances for each word
 		for(int i = 0; i < allDocs.length; i++){
 			String[] doc = allDocs[i].split(" ");//Split the doc into an array of each word
 			for(int j = 0; j < doc.length; j++){
 				if(dict.get(doc[j]) == null){	//If the key does not exist
 					dict.put(doc[j],1);			//Add it to the Map
 					totalWords++;
 				}else{
 					int count = dict.get(doc[j]);
 					dict.put(doc[j], count+1);
 				}
 			}
 
 		}
 
 		pair[] arr = new pair[dict.size()]; //Create the array of classes so that name/val pairs can be sorted
 		int index = 0;
 		for(String key : dict.keySet()){
 			arr[index].name = key;
 			arr[index].val = dict.get(key) / totalWords;
 			index++;
 		}
 
 		//Selection sort to sort from highest value to lowest value
 		for(int i = 0; i < arr.length-1; i++){
 			for(int j = i+1; j < arr.length; j++){
 				if(arr[i].val < arr[j].val){
 					pair temp = arr[i];
 					arr[i] = arr[j];
 					arr[j] = temp;
 				}else if(arr[i].val == arr[j].val){
 					if(arr[i].name.compareTo(arr[j].name) >0){
 						pair temp = arr[i];
 						arr[i] = arr[j];
 						arr[j] = temp;
 					}
 				}
 			}
 		}
		pair[] feature = new pair[20];
 		for(int k = 0; k < feature.length; k++){
 			feature[k] = arr[k];
 		}
 		return feature;
 
 	}
 
 }
