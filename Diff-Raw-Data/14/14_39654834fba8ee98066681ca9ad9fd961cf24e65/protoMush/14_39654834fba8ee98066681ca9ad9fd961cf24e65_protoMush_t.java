 package colin.richard.brad;
 
 import java.io.*;
 //import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 //import java.util.Collections;
 //import java.util.Comparator;
 //import java.util.HashMap;
 //import java.util.Map;
 
 import org.paukov.combinatorics.Factory;
 import org.paukov.combinatorics.Generator;
 import org.paukov.combinatorics.ICombinatoricsVector;
 import org.paukov.combinatorics.util.*;
 
 
 
 //that's pretty much it
 public class protoMush{
 	public static ArrayList<Record> dataSet;
 	public static ArrayList<Record> testingDataSet;
 	static File inputTrain;
 	static File inputTest;
 	public protoMush(String in, String inTest) throws IOException{
 		dataSet = parseArff(in.toString());
 		testingDataSet = parseArff(inTest.toString());
 		inputTrain = new File(in);
 		inputTest = new File(inTest);
 	}
 	
 	public static void main (String [] args) throws IOException{
 		//run parseArff/parseAttributes
 		
 		protoMush mushy = new protoMush("mushrooms.train.arff", "mushrooms.test.arff"); 
 		DTI dRunner = new DTI(dataSet, testingDataSet);
 		dRunner.run();
 		//run DTI
 		//run KNN
 		//run ModifiedKNN(just KNN, passed data pruned by DTI)
 		
 	}
 	
 	static ArrayList<Record> parseArff(String fileName) throws IOException {
 		ArrayList<Record> records = new ArrayList<Record>();
 			
 		//@data is the line immediately preceding csv
 		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
 		while (inputStream.readLine().indexOf("@data") == -1);
 		
 		String line;
 		while ((line = inputStream.readLine()) != null && line.indexOf(",") != -1)
 			records.add(new Record(line));
 		inputStream.close();
 		return records;
 	}
 	
 	static ArrayList<ArrayList<String>> parseAttributes(String fileName) throws IOException{
 		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
 		
 		BufferedReader inputStream = new BufferedReader(new FileReader(fileName));
 		//we need the attributes section
 		String in = inputStream.readLine();
 		while (in.indexOf("@attribute") == -1){
 			in = inputStream.readLine();
 		}
 		
 		//while in the attributes section
 		while (in.indexOf("@attribute") != -1){
 			//keep track of spaces on the line
 			int space = in.indexOf(" ");
 			int space2 = in.lastIndexOf(" ");
 			//the attributes name 
 			String attrName = in.substring(space + 1, space2);
 			//the attributes values. space2 + 2 should step over the "{" 
 			//while in.length() - 1 should step behind the "}"
 			String attrVals = in.substring(space2 + 2, in.length() - 1);
 			//each index in toAdd is an attribute
 			//the attribute's name appears in index 0
 			//the attribute's values appear in the following indices
 			ArrayList<String> toAdd = new ArrayList<String>();
 			toAdd.add(attrName);
 			String[] temp = attrVals.split(",");
 			for (int i = 0; i < temp.length; i++){
 				toAdd.add(temp[i]);
 				}
 			results.add(toAdd);
 			in = inputStream.readLine();
 		}	
 		//results will contain all of the arrayLists for the attributes
 		//each attribute's arrayList will contain its name and its values as strings
 		inputStream.close();
 		return results;
 		
 	}
 	static double lgBase(int n, double x){
 		if (x == 0)
 			return 0;
 		else 
 			return Math.log(x)/Math.log(n);
 	}
 }
 
 
 //this might only work for KNN, needs to be modified for DTI
 //this is not anything close to Richard's Records class
 //all this does is keep track of the classname and attributes, which is useful
 //the Tree class and DTI will perform class assignment and everything else
 class Record {
 	String[] attributes;
 	String classname;
 	
 	String getClassname(){return classname;}
 	
 	Record(String line) {
 		String[] fields = line.split(",");
 		attributes = fields;
 		//Globally keep track of class names so we can properly form confusion matrices, and so we know how many graphs to plot
 		classname = fields[0];
 		
 	}
 	
 }
 
 //performs DTI, contains methods which will also prune to later pass to modifiedKNN
 class DTI{
 	//constructor just instantiates fields, which may be redundant at this point
 	ArrayList<Record> dataSet;
 	ArrayList<String[]> attributes;
 	ArrayList<String> classes;
 	ArrayList<Record> testSet;
 	ArrayList<String> evaluations;
 	
 	
 	public DTI(ArrayList<Record> data, ArrayList<Record> tests){
 		dataSet = data;
 		ArrayList<String[]> att = new ArrayList<String[]>();
 		ArrayList<String> eval = new ArrayList<String>();
 		ArrayList<String> c = new ArrayList<String>();
 		for (Record r : dataSet){
 			att.add(r.attributes);
 			c.add(r.classname);
 		}
 		testSet = tests;
 		for (Record r :testSet){
 			eval.add(r.classname);
 		}
 		attributes = att;
 		evaluations = eval;
 		classes = c;
 	}
 
 	void run() throws IOException{
 		ArrayList<ArrayList<String>> allAttributes = protoMush.parseAttributes(protoMush.inputTrain.toString());
 		Tree initial = new Tree(allAttributes, dataSet, new GiniSplit());
 		ArrayList<String> s = predictClasses(testSet, initial);
 		int COUNT_OUR_SUCCESS = 0;
 		for (String s2 : s){
 			if (s2.equals(evaluations.get(s.indexOf(s2)))){
 				COUNT_OUR_SUCCESS++;
 			}
 		}
 		System.out.print(COUNT_OUR_SUCCESS);
 	}
 	
 	//returns the data which a branch applies to
 			public ArrayList<Record> findUsedData(String[] branch){
 				ArrayList<Record> results = new ArrayList<Record>();
 				boolean test = false;
 				for (Record r : dataSet){ //for each record in the trainingSet
 					if(r.attributes == null){break;}
 					for (String s : r.attributes){ //look through r's attributes
 						if(test){break;}//if we've already added the record...
 						for(String s2 : branch){//look through the values in the branch
 							if (s2.equals(s)){//if something matches up
 								results.add(r); //add the record
 								test = true; //note, we can stop looking through this record
 								break; //break the loop for the branch
 							}
 							test = false;//if we reach this point, we haven't added the record, keep checking
 						}
						test = false;
 					}
 				}
 				return results;//yeah
 			}
 	
 	//dunno if I should have this
 	abstract class Analysis{
 		
 		//counts how many times an attribute value occurs
 		int count(String val){
 			if(attributes == null){return 0;}
 			int result = 0;
 			for (String[] s : attributes){
 				for (int i = 0; i < s.length; i++){
 					if (s[i].equals(val)){
 						result++;
 					}
 				}
 			}
 			return result;
 		}
 		
 		abstract double analyze(String[] branch, ArrayList<Record> dataSet);
 		abstract double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs);
 	}
 	//override some math
 	abstract class GiniIndex extends Analysis{
 		double analyze(String[] branch, ArrayList<Record> dataSet){
 			int dataSize = dataSet.size();
 			double sum = 0;
 			int counter = 0;
 			
 			for (int i = 0; i < branch.length; i++){ //parse through that branch
 				counter += count(branch[i]); //and add the count of how many times the ith value of that branch appears in the data
 				sum += Math.pow(((double)counter)/((double)dataSize), 2);	
 			}
 			return 1.0 - sum;
 		}
 		abstract double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs);
 	}
 	
 	class GiniSplit extends GiniIndex{
 		double analyze(ArrayList<String[]> combo, ArrayList<ArrayList<Record>> dataSubs){
 			double GI; //Gini indices
 			double sum = 0.0;
 			int counter = 0;
 			int dataSize = 0; //size of the data we're dealing with at the entire node
 			for (ArrayList<Record> r : dataSubs){
 				dataSize += r.size(); //yeah it's silly, but good for generalization
 			}
 			for (String[] s : combo){//for each branch in the node
 				for (int i = 0; i < s.length; i++){ //iterate through the branch
 					counter += count(s[i]); //count how many records are at the branch
 				}
 				GI = super.analyze(s, dataSubs.get(combo.indexOf(s))); //check the GiniIndex at that branch, for the data subset that branch covers
 				sum += (counter/dataSize) * GI; //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
 			}
 			return sum;//return the sum
 		}
 	}
 	abstract class Entropy extends Analysis{
 		double analyze(String[] branch, ArrayList<Record> dataSet){
 			double dataSize = (double)dataSet.size();
 			double sum = 0;
 			double counter = 0;
 			
 			for (int i = 0; i < branch.length; i++){ //parse through that branch
 				counter += count(branch[i]); //and add the count of how many times the ith value of that branch appears in the data
 				sum += (counter/dataSize)*protoMush.lgBase(2,counter/dataSize);	
 			}
 			return -sum;
 		}
 		abstract double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs);
 	}
 	class InfoGain extends Entropy{
 		double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs){
 			int sizeOfStuff = 0;
 			double counter = 0.0;
 			double E;
 			double sum = 0.0;
 			
 			for (String[] s : combos){
 				sizeOfStuff += s.length;
 			}
 			String[] allData = new String[sizeOfStuff];
 			int placeHolder = 0;
 			for (String [] s : combos){
 				for (int i = 0 + placeHolder; i < s.length; i++){
 					allData[i] = s[i];
 				}
 				placeHolder = s.length - 1;
 			}
 			double parent = super.analyze(allData, dataSet);
 			
 			for (String[] s : combos){//for each branch in the node
 				for (int i = 0; i < s.length; i++){ //iterate through the branch
 					counter += count(s[i]); //count how many records are at the branch
 				}
 				E = super.analyze(s, dataSubs.get(combos.indexOf(s))); //check the GiniIndex at that branch, for the data subset that branch covers
 				sum += (counter/dataSet.size()) * E; //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
 			}
 			
 			return parent - sum;
 		}
 	}
 	
 	class SplitInfo extends Analysis{
 		
 		double analyze(String[] branch, ArrayList<Record> dataSubs){
 			return -1.0;
 		}
 		double analyze(ArrayList<String[]> combos, ArrayList<ArrayList<Record>> dataSubs){
 			double counter = 0.0;
 			double sum = 0.0;
 			double j = 0.0;
 			for (ArrayList<Record> a : dataSubs){
 				j += a.size();
 			}
 			for (String[] s : combos){//for each branch in the node
 				for (int i = 0; i < s.length; i++){ //iterate through the branch
 					counter += count(s[i]); //count how many records are at the branch
 				}
 				sum += (counter/j) * protoMush.lgBase(2,(counter/j)); //sums the sizes of the child nodes divided by the size of the entire dataset times the GINI
 			}
 			return -sum;
 		}
 	}
 	
 	//yeah, establishes hierarchy for Tree class, holy shit this is an ugly method and it doesn't work
 	//I guess, it actually would, if one of the members of each split were pure, but I don't think that'll work
 	ArrayList<String[]> establishHierarchy(Analysis chief, ArrayList<ArrayList<String>> allAttributes, ArrayList<Record> data) throws IOException{ //chief is the analysis we're checking with
 		
 		allAttributes.remove(0); //we don't want to deal with the class
 		ArrayList<Double> analBesties = new ArrayList<Double>(); //this will hold the best analysis for each attribute
 		ArrayList<ArrayList<String[]>> besties = new ArrayList<ArrayList<String[]>>(); //this will hold the best split for each attribute
		for (int i = 0; i <= allAttributes.size(); i++){
 			analBesties.add(null);
 			besties.add(null);
 		}
 		for (ArrayList<String> attr : allAttributes){attr.remove(0);} //we don't need the attribute's name or class
 		
 		for (ArrayList<String> attr : allAttributes){ //for each attribute in allAttributes
 			String [] attrCast = new String[attr.size()]; 
 			attr.toArray(attrCast); //put the attribute's values in the array attrCast
 			
 			ArrayList<ArrayList<String[]>> poss = possibleCombinations (attrCast, 2); //poss contains all possible splits on the attribute
 			
 			double bestAnalysis = -666666.666666; //all hail s@an
 			ArrayList<String[]> bestSplit = new ArrayList<String[]>();
 			
 			for (ArrayList<String[]> p : poss){ //for each possible split on the attribute, we're checking for the best
 				ArrayList<ArrayList<Record>> DDDD = new ArrayList<ArrayList<Record>>();
 				for (String [] s : p){
 					DDDD.add(findUsedData(s));
 				}
 				
 				double dummy = chief.analyze(p, DDDD); //check the analysis of that split
 				if (bestAnalysis == -666666.666666){ //if we're checking for the first time
 					bestAnalysis = dummy;
 					bestSplit = p;
 				}
 				else if (chief instanceof GiniIndex && dummy < bestAnalysis){ //if chief is Gini
 					bestAnalysis = dummy;
 					bestSplit = p;
 				}
 				else if(chief instanceof Entropy || chief instanceof InfoGain && dummy > bestAnalysis){ //if chief is something else
 					bestAnalysis = dummy;
 					bestSplit = p;
 				}
 			}
			int testy = allAttributes.indexOf(attr);
 			analBesties.set(allAttributes.indexOf(attr), bestAnalysis); //store the best analysis for this attribute
 			besties.set(allAttributes.indexOf(attr), bestSplit); //store the split for that attribute, the index will correspond with that of its analysis
 			
 		}
 		
 		ArrayList<ArrayList<String[]>> results = new ArrayList<ArrayList<String[]>>();
 		double best = -666666.666666;
 		int indexOfBest = -1;
 		//while (analBesties.size() > 0){
 			for (double d : analBesties){
 				if (best == -666666.666666){ //if we're checking for the first time
 					best = d;
 					indexOfBest = analBesties.indexOf(d);
 					
 				}
 				else if (chief instanceof GiniIndex && d < best){ //if chief is Gini
 					best = d;
 					indexOfBest = analBesties.indexOf(d);
 				}
 				else if(chief instanceof Entropy || chief instanceof InfoGain && d > best){ //if chief is something else
 					best = d;
 					indexOfBest = analBesties.indexOf(d);
 				}
 			}
 			results.add(besties.get(indexOfBest)); //add the best split
 			//analBesties.remove(indexOfBest); //remove that attribute's analysis 
 			//besties.remove(indexOfBest); //remove that attribute
 		//}
 		
 		//return results;
 			//results is an array of ArrayLists, in order of best analysis
 		return besties.get(indexOfBest);
 			//this is the best split of those remaining attributes
 		
 	}
 	
 	
 	
 	//generates all possible combinations for an array
 	ArrayList<ArrayList<String[]>> possibleCombinations(String [] attr, int n){
 		
 		   ICombinatoricsVector<String> vector = Factory.createVector(attr);
 		   ArrayList<ArrayList<String[]>> results = new ArrayList<ArrayList<String[]>>();
 		   // Create a complex-combination generator
 		   Generator<ICombinatoricsVector<String>> gen = new ComplexCombinationGenerator<String>(vector, n, false, true);
 		   String[] r;
 		   List<String> a1;
 		   // Iterate through the different combinations
		   ArrayList<String[]> results2 = null;
 		   for (ICombinatoricsVector<ICombinatoricsVector<String>> comb : gen) {
			  results2 = new ArrayList<String[]>();
 			  //Iterate through the individual parts of the combination
 			  for (ICombinatoricsVector<String> v : comb){
 				  a1 = v.getVector(); //this might be redundant
 				  r = new String[a1.size()]; //we're going to put it in an array
 				  a1.toArray(r); //here's the array
 				  results2.add(r); //woohoo, add that array to the array of arrays
 			  }
 			  
 			  results.add(results2); //add the array of arrays to another array
			  results2 = null;
 		   }
 		   return results;
 		   //so, basically, what this looks like is, an array with many arrays in it
 		   //each array in 'results' is an array of String[] arrays
 		   //each String[] contains the individual attribute values
 	}
 	
 	//this class does a lot of stuff
 	class Tree{
 		
 		ArrayList<ArrayList<String>> attrs;
 		ArrayList<Record> data;
 		Analysis measure;
 		
 		ArrayList<Tree> nodes = new ArrayList<Tree>();
 		
 		//takes the attributes this tray can possibly cover, the data it covers, and the type of analysis
 		Tree(ArrayList<ArrayList<String>> allAttributes, ArrayList<Record> d, Analysis h) throws IOException{
 			attrs = allAttributes;
 			data = d;
 			measure = h;
 			build(h);
 		}
 		
 		//finds the best attribute to split on and its split
 		//iterates through the children of that split
 		//adds them as trees
 		//this is recursive
 		void build(Analysis w) throws IOException{
 			ArrayList<String[]> split = establishHierarchy(w, attrs, data);
 			for (int i = 0; i < split.size(); i++){ //iterate through each of the children
 				String[] branch = split.get(i); //branch is the array with the attribute values its split on
 				ArrayList<Record> d = findUsedData(branch); //find the data which the split applies to 
 				ArrayList<ArrayList<String>> a = findUnusedAttributes(branch); //find the attributes the split doesn't apply to
 				Tree node = new Tree(a, d, w); //create a child tree which works with the same data and unused attributes
 				nodes.add(node); //nodes holds each tree child
 			}	
 		}
 		
 		
 		
 		//returns attributes unused by the branch
 		ArrayList<ArrayList<String>> findUnusedAttributes(String[] branch){
 			ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
 			boolean test = false;
 			for (ArrayList<String> a : attrs){//for each attribute
 				for (String s1 : a){//for each value for that attribute
 					if(test){break;}//if we've found the attribute, no need to continue looking
 					for (String s2 : branch){//for each value in the array
 						if (s2.equals(s1)){//if an attribute value and array value match
 							test = true;//note we can stop checking, we've found the attribute
 							results.add(a);//add that attribute
 							break;//break
 						}
 					}
 				}
 			}
 			return results;
 		}
 		
 		//checks if a branch generated by a split is pure
 		boolean isPure(String[] branch){
 			ArrayList<Record> d = findUsedData(branch); //we work with the data this branch applies to
 			String c = d.get(0).classname; //check the first class in that data subset
 			for (Record r : d){ //for each of the records in that subset
 				if(!(r.classname.equals(c))){ //if one of the classes doesn't match
 					return false; //the branch isn't pure
 				}
 			}
 			return true; //if we've reached this point, the branch is pure
 		}
 		
 		//returns the class the branch contains, if it is pure
 		String getClass(String[] branch){
 			if(isPure(branch)){//if the branch is pure
 				return findUsedData(branch).get(0).classname; //the classes will all be the same, return the first
 			}
 			return null; //if we've reached this point, the branch isn't pure and will return null
 		}
 		
 		//just accesses establishHierarchy for the tree at this point in the recursion
 		ArrayList<String[]> splitAtThisLevel() throws IOException{
 			return establishHierarchy(measure, attrs, data);
 		}
 		
 		//deletes a child node and its children
 		void prune(int nodeIndex){nodes.remove(nodeIndex);}
 		
 		//same as above but with different arg
 		void prune(ArrayList<Record> node){nodes.remove(node);}
 		
 		//traverses recursively and assigns a class
 		String assignClassTo(Record r) throws IOException{
 			String[] theseAttributes = r.attributes; //get this record's attribute values
 			String result; //prepare the result
 			for (Tree t: nodes){ //for each child node
 				String[] s1 = t.splitAtThisLevel().get(0); //get its first branch
 				String[] s2 = t.splitAtThisLevel().get(1); //get its second branch
 				
 				for(String test : s1){ //for each value in the branch
 					for(String a : theseAttributes){ //for each of these record's values
 						if(a.equals(test)){ //if the values in the branch and record are equal
 							if (isPure(s1)){ //if this branch is pure
 								return getClass(s1); //we've found the class - BASECASE HOORAY
 							}
 							result = t.nodes.get(0).assignClassTo(r); //otherwise, we go deeper on this node
 						}
 					}
 				}//if we reach the second branch, we've passed the first completely and haven't recursed yet
 				for(String test : s2){ //for each value in the branch
 					for(String a : theseAttributes){ //for each of the record's values
 						if(a.equals(test)){ //if the values in the branch and record are equal
 							if(isPure(s2)){ //if this branch is pure
 								return getClass(s2); //we've found the class - BASECASE HOORAY
 							}
 							result = t.nodes.get(1).assignClassTo(r); //otherwise, we go deeper on this node
 						}
 					}
 				}
 				
 			}
 			result = null; //if we get this far, something's gone wrong. 
 			return result; //this will throw an exception, forcing revision of the code
 		}
 	}
 	
 	//pretty much just conglomerates test results
 	ArrayList<String> predictClasses(ArrayList<Record> testSet, Tree decisionTree) throws IOException{
 		ArrayList<String> results = new ArrayList<String>();
 		for (Record r : testSet){
 			results.add(decisionTree.assignClassTo(r));
 			}
 		return results;
 		}
 }
 
 
