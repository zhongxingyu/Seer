 package hypermodulesrun;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
 public class HypermodulesHeuristicAlgorithm {
 	
 	private String stat;
 	private ArrayList<String[]> sampleValues;
 	private ArrayList<String[]> clinicalValues;
 	private ArrayList<String[]> network;
 	
 
 	//initClinicalsSurvival
 	private String[] allPatients;
 	private boolean[] status;
 	private double[] followupDays;
 	private double[] censor;
 	
 	//initClinicalsFisher
 	private String[] otherPatients;
 	private HashMap<String, String> clinicalVariableMap;
 	private HashSet<String> clinicalVariableHash;
 	private ArrayList<String> hashArray;
 	private String[] clinicalVariable;
 	
 	private LogRankTest logRankObject;
 	private HashMap<String, String> allGeneSamplesMap;
 	
 	private ArrayList<String> allGenes;
 	private ArrayList<String> allSamples;
 	
 	private HashMap<String, Double> repository;
 	
 	//getAllPaths
 	private Multimap<String, String> networkInteractions;
 	
 	public HypermodulesHeuristicAlgorithm(String stat, 
 				ArrayList<String[]> sampleValues, 
 				ArrayList<String[]> clinicalValues, 
 				ArrayList<String[]> network){
 		
 		this.stat = stat;
 		this.sampleValues = sampleValues;
 		this.clinicalValues = clinicalValues;
 		this.network = network;
 	}
 
 	
 	public void initialize(){
 		try{
 			if (this.stat.toUpperCase().equals("LOGRANK")){
 				initClinicalsSurvival();
 				logRankObject = new LogRankTest(this.followupDays);
 			}
 			
 			else if (this.stat.toUpperCase().equals("FISHER")){
 				initClinicalsFisher();
 			}
 			else{
 				System.out.println("Please enter either LOGRANK or FISHER as the statistical test.");
 				System.exit(0);
 			}
 		}
 		catch(Exception e){
 			System.out.println("Please enter path to a valid input file for the statistical test selected!");
 			System.exit(0);
 		}
 
 		allGeneSamplesMap = new HashMap<String, String>();
 		
 		for (int i=0; i<sampleValues.size(); i++){
 			allGeneSamplesMap.put(sampleValues.get(i)[0], sampleValues.get(i)[1]);
 		}
 		
 		HashSet<String> allNodes = new HashSet<String>();
 		
 		for (int i=0; i<this.network.size(); i++){
 			allNodes.add(this.network.get(i)[0]);
 			allNodes.add(this.network.get(i)[1]);
 		}
 		
 		for (String s : allNodes){
 			if (allGeneSamplesMap.get(s)==null){
 				String[] inconsistency = new String[2];
 				inconsistency[0] = s;
 				inconsistency[1] = "no_sample";
 				sampleValues.add(inconsistency);
 				allGeneSamplesMap.put(sampleValues.get(sampleValues.size()-1)[0], sampleValues.get(sampleValues.size()-1)[1]);
 			}
 		}
 		
 		
 		Multimap<String, String> memoryone = ArrayListMultimap.create();
 		HashSet<String> g2sSamples = new HashSet<String>();
 		for (String[] s : sampleValues){
 			String[] sampleSplit = s[1].split(":");
 			for (String t : sampleSplit){
 				if (!t.equals("no_sample")){
 					g2sSamples.add(t);
 					memoryone.put(t, s[0]);
 				}
 			}
 		}
 		
 		HashSet<String> clinicalSamples = new HashSet<String>();
 		for (String[] s : clinicalValues){
 			clinicalSamples.add(s[0]);
 		}
 		
 		for (String s : g2sSamples){
 			if (!clinicalSamples.contains(s)){
 				for (String z : memoryone.get(s)){
 					allGeneSamplesMap.put(z, "no_sample");
 					System.out.println("The sample " + s + " was not found in your clinical table. All genes corresponding to " + s + " are now assumed to have no sample.");
 				}
 			}
 		}
 
 		allGenes = new ArrayList<String>();
 		allSamples = new ArrayList<String>();
 		
 		for (String key : allGeneSamplesMap.keySet()){
 			allGenes.add(key);
 			allSamples.add(allGeneSamplesMap.get(key));
 		}
 		
 		repository = new HashMap<String, Double>();
 		
 		networkInteractions = ArrayListMultimap.create();
 		for (int i=0; i<this.network.size(); i++){
 			networkInteractions.put(this.network.get(i)[0], this.network.get(i)[1]);
 		}
 		
 	}
 	
 	public void initClinicalsSurvival(){
 		//have a "load (String/Double) Column" method?
 
 		allPatients = new String[this.clinicalValues.size()];
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			allPatients[k]=this.clinicalValues.get(k)[0];
 		}
 		
 		status = new boolean[this.clinicalValues.size()];
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			if (clinicalValues.get(k)[1].toUpperCase().equals("DECEASED")){
 				status[k]=true;
 			}
 			else{
 				status[k]=false;
 			}
 		}
 		
 		followupDays = new double[this.clinicalValues.size()];
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			followupDays[k] = Double.valueOf(clinicalValues.get(k)[2]);
 		}
 		
 
 		censor = new double[this.clinicalValues.size()];
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			if (status[k]==true){
 				censor[k]=1;
 				//System.out.println(censor[k]);
 			}
 			else{
 				censor[k]=0;
 				//System.out.println(censor[k]);
 			}
 			
 		}
 		
 	}
 	
 	public void initClinicalsFisher(){
 		otherPatients = new String[this.clinicalValues.size()];
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			otherPatients[k] = this.clinicalValues.get(k)[0];
 		}
 		
 		clinicalVariableMap = new HashMap<String, String>();
 		clinicalVariableHash = new HashSet<String>();
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			clinicalVariableMap.put(this.clinicalValues.get(k)[0], this.clinicalValues.get(k)[1]);
 			clinicalVariableHash.add(this.clinicalValues.get(k)[1]);
 		}
 		hashArray = new ArrayList<String>();
 		for (String hashElement : clinicalVariableHash){
 			hashArray.add(hashElement);
 		}
 		
 		clinicalVariable = new String[this.clinicalValues.size()];
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			clinicalVariable[k] = this.clinicalValues.get(k)[1];
 		}
 	}
 
 	public HashSet<String> getAllPaths(String seed){
 		HashSet<String> allPaths = new HashSet<String>();
 		for (String s : networkInteractions.get(seed)){
 			for (String t : networkInteractions.get(s)){
 				allPaths.add(seed + ":" + s + ":" + t);
 			}
 		}
 		return allPaths;
 	}
 	
 	public ArrayList<String> compressTokens(HashSet<String> allPaths, String seedName){
 		
 		ArrayList<String> compress = new ArrayList<String>();
 
 		HashMap<String, String> allPatients = new HashMap<String, String>();
 		
 		
 		for (String network : allPaths){
 			String patients = allGeneSamplesMap.get(seedName);
 			String[] genes = network.split(":");
 			for (int i=0; i<genes.length; i++){
 				if (!genes[i].equals(seedName) && !allGeneSamplesMap.get(genes[i]).equals("no_sample")){
 					patients = patients + ":" + allGeneSamplesMap.get(genes[i]);
 				}
 			}
 
 			if (allPatients.get(patients)==null){
 				allPatients.put(patients, network);
 			}
 			else{
 				String[] genes2 = allPatients.get(patients).split(":");
 				if(genes.length<genes2.length){
 					allPatients.put(patients, network);
 				}
 			}
 		}
 
 		for (String list : allPatients.keySet()){
 			compress.add(allPatients.get(list));
 		}
 
 		return compress;
 	}
 	
 	public HashMap<String, Double> mineHublets(ArrayList<String> compressedList){
 		
 		HashMap<String, Double> hubletsTested = new HashMap<String, Double>();
 		
 		String key5;
 		Double value5;
     	for(int i=0; i<compressedList.size(); i++){
     		key5 = compressedList.get(i);
     		value5 = testModuleClinical(compressedList.get(i), 1, false);
     		if (value5!=null){
         		hubletsTested.put(key5, value5);
     		}
     	}
 
     	if (hubletsTested.isEmpty()){
     		return hubletsTested;
     	}
     	
     	HashMap<String[], Double[]> pairwise = new HashMap<String[], Double[]>();
     	HashMap<String, Double> pairwiseConcat = new HashMap<String, Double>();
 		HashMap<String, String[]> pairwiseConcatMemory = new HashMap<String, String[]>();
     	
     	while(true){
         	
 
 			String[] pairwiseKey = new String[2];
     		Double[] pairwiseValue = new Double[2];
     		
     		
     		ArrayList<String> list = new ArrayList<String>();
  
     		for (String cy1 : hubletsTested.keySet()){
     			list.add(cy1);
     		}
     		
     		
     		for (int k=0; k<list.size(); k++){
     			for (int j = k+1; j<list.size(); j++){
     				pairwiseKey = new String[2];
     				pairwiseKey[0]=list.get(k);
     				pairwiseKey[1]=list.get(j);
 
     				pairwiseValue = new Double[2];
     				pairwiseValue[0]=hubletsTested.get(list.get(k));
     				pairwiseValue[1]=hubletsTested.get(list.get(j));
     				
     				pairwise.put(pairwiseKey, pairwiseValue);
     			}
     		}
 
     		String key7;
     		Double value7;
     		//System.out.println("concatenatedNetwork :");
     		
     		String minKey = null;
     		Double minVal=Double.valueOf(2);
     		
     		for (String[] key6 : pairwise.keySet()){
     			//TODO: got rid of concatenate network... why does it still work?
     			key7 = key6[0] + ":" + key6[1];
     			//key7 = concatenateNetwork(key6[0], key6[1]);
     			if (repository.get(key7)!=null){
     				value7 = repository.get(key7);
     			}
     			else{
             			value7 = testModuleClinical(key7, 1, false);
             			repository.put(key7, value7);
 
     			}
 
     			if (value7 < pairwise.get(key6)[0] && value7 < pairwise.get(key6)[1]){
         		pairwiseConcat.put(key7, value7);
         		pairwiseConcatMemory.put(key7, key6);
         			if (value7<minVal){
         				minVal = value7;
         				minKey = key7;
         			}
     			}
     			
     			/*
     			else{
     				hubletsTested.remove(key6[0]);
     				hubletsTested.remove(key6[1]);
     			}
     			*/
     		}
 
     		if (pairwiseConcat.isEmpty()){
     			break;
     		}
     		
     		//here, if there are two keys having the same min value, it may be different from R (R picks arbitrarily anyways)
     		
     		hubletsTested.remove(pairwiseConcatMemory.get(minKey)[0]);
     		hubletsTested.remove(pairwiseConcatMemory.get(minKey)[1]);
     		hubletsTested.put(minKey, pairwiseConcat.get(minKey));
     		
     		if(hubletsTested.size()<2){
     			break;
     		}
     		
     		pairwise.clear();
     		pairwiseConcat.clear();
     		pairwiseConcatMemory.clear();
     		
     	}
     	
     	Double finalTestValue;
     	ArrayList<String> toBeRemoved = new ArrayList<String>();
     	
     	for (String finalTest : hubletsTested.keySet()){
     		//this can be skipped? (flag?)
     		finalTestValue = testModuleClinical(finalTest, 2, true);
     		if (finalTestValue==null){
     			toBeRemoved.add(finalTest);
     		}
     	}
     	
     	for (String remove : toBeRemoved){
     		hubletsTested.remove(remove);
     	}
     	
     	if (hubletsTested.isEmpty()){
     		String emptySet = new String();
     		emptySet = "none";
     		hubletsTested.put(emptySet, Double.valueOf(1));
     		return hubletsTested;
     	}
     	
     	
     	//filter out unique sample hublets (whichUnique)
     	
     	HashMap<String, String> hubletPatientSamples = new HashMap<String, String>();
     	
     	for(String finalFinalTest : hubletsTested.keySet()){
     		String[] nodes = finalFinalTest.split(":");
     		String allSampleString = "";
     		for (int j=0; j<nodes.length; j++){
     			String thesePatientSamples = allGeneSamplesMap.get(nodes[j]);
     			if (!thesePatientSamples.equals("no_sample")){
     				allSampleString = allSampleString + thesePatientSamples + ":";
     			}
     		}
     		
     		//TODO: could cause array out of bounds error:
     		if (allSampleString.length()>=2){
     			allSampleString = allSampleString.substring(0, allSampleString.length()-1);
     		}
     			
     		hubletPatientSamples.put(finalFinalTest, allSampleString);
     	}
     	
     	//this next part is O(n^2) :S
     	
     	
     	HashMap<String, Boolean> shouldKeep = new HashMap<String, Boolean>();
     	for (String hubs : hubletPatientSamples.keySet()){
     		shouldKeep.put(hubs, true);
     	}
     	
     	
     	for (String firstSet : hubletPatientSamples.keySet()){
     		for (String secondSet : hubletPatientSamples.keySet()){
     			if (!firstSet.equals(secondSet)){
     				HashSet<String> compareSet = new HashSet<String>();
     				String[] firstCompare =  hubletPatientSamples.get(firstSet).split(":");
     				for (int i=0; i<firstCompare.length; i++){
     					compareSet.add(firstCompare[i]);
     				}
         			int beforeSize = compareSet.size();
         			
         			String[] secondCompare = hubletPatientSamples.get(secondSet).split(":");
         			for (int i=0; i<secondCompare.length; i++){
         				compareSet.add(secondCompare[i]);
         			}
         			int afterSize = compareSet.size();
         			
         			if (beforeSize==afterSize && hubletsTested.get(secondSet)>hubletsTested.get(firstSet)){
         				shouldKeep.put(secondSet, false);
         			}
     			}
     			
     		}
     	}
     	
     	ArrayList<String> toBeRemoved2 = new ArrayList<String>();
     	
     	for (String ahh : hubletPatientSamples.keySet()){
     		if (shouldKeep.get(ahh)==false){
     			toBeRemoved2.add(ahh);
     		}
     	}
     	
     	
     	for (String omg : toBeRemoved2){
     		hubletsTested.remove(omg);
     	}
 
     	repository.clear();
     	
     	return hubletsTested;
 	}
 	
 	public Double testModuleFisher(String thisNetwork, int limit){
 
		
 		String[] genes = thisNetwork.split(":");
 		
 		ArrayList<String> patients = new ArrayList<String>();
 		String[] thesePatients;
 		
 		for (int i=0; i<genes.length; i++){
 			thesePatients = allGeneSamplesMap.get(genes[i]).split(":");
 			for (int t=0; t<thesePatients.length; t++){
 				patients.add(thesePatients[t]);
 			}
 		}
 		
 		boolean[] var2patients = new boolean[this.clinicalValues.size()];
 		for (int k=0; k<this.clinicalValues.size(); k++){
 			var2patients[k]=false;
 			
 			for (int l=0; l<patients.size(); l++){		
 				if(patients.get(l).equals(clinicalValues.get(k)[0])){
 					var2patients[k]=true;
 				}
 			}
 		}
 		
 		int alpha=0;
 		for (int k=0; k<var2patients.length; k++){
 			if (var2patients[k]==true){
 				alpha++;
 			}
 		}
 		
 		if (alpha<limit){
 			return null;
 		}
 		
 		
 		int[][] matrix = new int[clinicalVariableHash.size()][2];
 		
 		for (int i=0; i<matrix.length; i++){
 			for (int j=0; j<matrix[i].length; j++){
 				matrix[i][j]=0;
 			}
 		}
 		
 		for (int k=0; k<clinicalValues.size(); k++){
 			if (var2patients[k]==true){
 				for (int i=0; i<hashArray.size(); i++){
 					if (clinicalVariableMap.get(otherPatients[k]).equals(hashArray.get(i))){
 						matrix[i][0]++;
 					}
 				}
 			}
 			else{
 				for (int i=0; i<hashArray.size(); i++){
 					if (clinicalVariableMap.get(otherPatients[k]).equals(hashArray.get(i))){
 						matrix[i][1]++;
 					}
 				}
 			}
 		}
 		
 		FishersExact fe = new FishersExact(matrix);
 		return fe.fisher2c();
 	}
 	
 	public Double testModuleClinical(String thisNetwork, int limit, boolean flag){
 		if (this.stat.toUpperCase().equals("FISHER")){
 			return testModuleFisher(thisNetwork, limit);
 		}
 
 		String[] genes = thisNetwork.split(":");
 		HashSet<String> truePatients = new HashSet<String>();
 		String[] thesePatients;
 		
 		for (int i=0; i<genes.length; i++){
 			thesePatients = allGeneSamplesMap.get(genes[i]).split(":");
 			for (int t=0; t<thesePatients.length; t++){
 				if (!thesePatients[t].equals("no_sample"))
 				truePatients.add(thesePatients[t]);
 			}
 		}
 
 		
 		int alpha=truePatients.size();
 		
 		if (alpha<limit){
 			return null;
 		}
 		
 		if (flag){
 			return Double.valueOf(1);
 		}
 		
 		ArrayDeque<Double> time1 = new ArrayDeque<Double>();
 		ArrayDeque<Double> time2 = new ArrayDeque<Double>();
 		ArrayDeque<Double> censor1 = new ArrayDeque<Double>();
 		ArrayDeque<Double> censor2 = new ArrayDeque<Double>();
 		
 		for (int i=0; i<allPatients.length; i++){
 			if (truePatients.contains(allPatients[i])){
 				time1.add(followupDays[i]);
 				censor1.add(censor[i]);
 			}
 			else{
 				time2.add(followupDays[i]);
 				censor2.add(censor[i]);
 			}
 		}
 
 		Double pValue = Double.valueOf(0);
 
 		Double[] result = logRankObject.logRank(time1, time2, censor1, censor2);
 		pValue = result[2];
 		return pValue;
 	}
 	
 	public int testModuleBoolean(String thisNetwork){
 		String[] genes = thisNetwork.split(":");
 		HashSet<String> truePatients = new HashSet<String>();
 		String[] thesePatients;
 		
 		if (genes[0].equals("none")){
 				return 2;
 		}
 		
 		for (int i=0; i<genes.length; i++){
 			thesePatients = allGeneSamplesMap.get(genes[i]).split(":");
 			for (int t=0; t<thesePatients.length; t++){
 				if (!thesePatients[t].equals("no_sample"))
 				truePatients.add(thesePatients[t]);
 			}
 		}
 
 		int alpha=truePatients.size();
 		
 		ArrayDeque<Double> time1 = new ArrayDeque<Double>();
 		ArrayDeque<Double> time2 = new ArrayDeque<Double>();
 		ArrayDeque<Double> censor1 = new ArrayDeque<Double>();
 		ArrayDeque<Double> censor2 = new ArrayDeque<Double>();
 		
 		for (int i=0; i<allPatients.length; i++){
 			if (truePatients.contains(allPatients[i])){
 				time1.add(followupDays[i]);
 				censor1.add(censor[i]);
 			}
 			else{
 				time2.add(followupDays[i]);
 				censor2.add(censor[i]);
 			}
 		}
 		
 		if (logRankObject.logRankSurvivalTest(time1, time2, censor1, censor2)){
 			return 1;
 		}else{
 			return 0;
 		}
 	}
 	
 
 	public void shuffleLabels(){
 		Collections.shuffle(this.allSamples);
 		for (int i=0; i<allGenes.size(); i++){
 			allGeneSamplesMap.put(allGenes.get(i),  allSamples.get(i));
 		}
 	}
 	
 
 	
 }
