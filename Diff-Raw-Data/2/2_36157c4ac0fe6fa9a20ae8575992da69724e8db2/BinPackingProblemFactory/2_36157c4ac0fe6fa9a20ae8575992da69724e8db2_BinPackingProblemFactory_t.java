 package edu.upenn.cis350.algoviz;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.xmlpull.v1.XmlPullParser;
 
 import edu.upenn.cis350.algoviz.R;
 
 import android.content.Context;
 import android.content.res.XmlResourceParser;
 
 /**
  * Generates the Bins and BinObjects for each Bin Packing problem difficulty, 
  * as specified in res/xml/problems.xml. Also calculates optimal solutions.
  */
 public class BinPackingProblemFactory {
 	
 	///// Instance variables /////
 	private ArrayList<String> _problems;
 	private HashMap<String, Collection<Bin>> _bins;
 	private HashMap<String, Collection<BinObject>> _objects;
 	private HashMap<String, Double> _optimalSolutions;
 	//TODO make BinPackingProblem class that encompasses the above variables?
 	private XmlResourceParser _parser; //TODO ugly instance variable - find a way to remove?
 	
 	
 	///// Constructor /////
 	public BinPackingProblemFactory(Context context) {
 		_parser = context.getResources().getXml(R.xml.problems);
 		
 		_problems = new ArrayList<String>();
 		_bins = new HashMap<String, Collection<Bin>>();
 		_objects = new HashMap<String, Collection<BinObject>>();
 		parseProblemsFile();
 		
 		_optimalSolutions = new HashMap<String, Double>();
 		calculateOptimalSolutions();
 	}
 	
 	
 	///// Public methods /////
 	/**
 	 * @return An ordered Collection of all the problem names specified in problems.xml.
 	 */
 	public Collection<String> getProblemNames() {
 		return _problems;
 	}
 	
 	/**
 	 * @param problemName The name of the problem as specified in problems.xml.
 	 * @return a Collection of all BinObjects for the specified problem, or null if the
 	 * problem is unspecified in problems.xml.
 	 */
 	public Collection<BinObject> getBinObjects(String problemName) {
 		return _objects.get(problemName);
 	}
 	
 	/**
 	 * @param problemName The name of the problem as specified in problems.xml.
 	 * @return a Collection of all Bins for the specified problem, or null if the
 	 * problem is unspecified in problems.xml.
 	 */
 	public Collection<Bin> getBins(String problemName) {
 		return _bins.get(problemName);
 	}
 	
 	/**
 	 * @param problemName The name of the problem as specified in problems.xml.
 	 * @return the value of the optimal solution for the specified problem, or null if the
 	 * problem is unspecified in problems.xml.
 	 */
 	public Double getOptimalSolution(String problemName) {
 		return _optimalSolutions.get(problemName);
 	}
 	
 	
 	///// Private methods /////
 	private void parseProblemsFile() {
 		try {
 			int event = _parser.next();
 			while (!doneParsingBinPacking(event)) {
 				if (isStartTag(event)) parseTag();
 				event = _parser.next();
 			}
 		} catch (Exception e) {
 			System.err.println("Problem parsing res/xml/problems.xml - please check file format");
 			System.exit(-1);
 		} finally {
 			_parser.close();
 		}
 	}
 	
 	private boolean doneParsingBinPacking(int event) {
 		return "item".equalsIgnoreCase(_parser.getName()) && event == XmlPullParser.END_TAG;
 	}
 	
 	private boolean isStartTag(int event) {
 		return event == XmlPullParser.START_TAG;
 	}
 	
 	private void parseTag() {
 		String element = _parser.getName();
 		if ("problem".equalsIgnoreCase(element)) instantiateNewProblem();
 		else if ("bin".equalsIgnoreCase(element)) parseBin();				
 		else if ("object".equalsIgnoreCase(element)) parseObject();
 	}
 	
 	private void instantiateNewProblem() {
 		String problemName = _parser.getAttributeValue(null, "name");
 		_problems.add(problemName);
 		_bins.put(problemName, new ArrayList<Bin>());
 		_objects.put(problemName, new ArrayList<BinObject>());
 	}
 	
 	private String lastProblem() {
 		return _problems.get(_problems.size()-1);
 	}
 	
 	private void parseBin() {
 		Double capacity = new Double(_parser.getAttributeValue(null, "capacity"));
 		Bin bin = new Bin(capacity);
 		
 		Collection<Bin> bins = _bins.get(lastProblem());
 		bins.add(bin);
 		
 		_bins.put(lastProblem(), bins);
 	}
 	
 	private void parseObject() {
 		Double weight = new Double(_parser.getAttributeValue(null, "weight"));
 		Double value = new Double(_parser.getAttributeValue(null, "value"));
 		String type = _parser.getAttributeValue(null, "type");
 		BinObject object = new BinObject(weight, value, type);
 		
 		Collection<BinObject> objects = _objects.get(lastProblem());
 		objects.add(object);
 		
 		_objects.put(lastProblem(), objects);
 	}
 	
 	private void calculateOptimalSolutions() {
 		for (String problemName : getProblemNames()) {
 			Collection<Bin> bins = getBins(problemName);
 			Collection<BinObject> objects = getBinObjects(problemName);
 			Double optimalSolution = calculateSolution(bins, objects);
 			_optimalSolutions.put(problemName, optimalSolution);
 		}
 	}
 	
 	/**
 	 * Calculates the optimal solution to the problem of packing the highest value of BinObjects into
 	 * the given collection of Bins. Note that if there's more than 1 Bin in the bins collection, the 
 	 * problem of calculating an optimal solution becomes NP-Hard. In this case the method only returns 
 	 * an approximation to the optimal solution.
 	 * @param bins The collection of bins objects can be packed into.
 	 * @param objects The collection of objects to choose from.
 	 * @return The value of the optimal solution to the Bin-Packing problem.
 	 */
 	private double calculateSolution(Collection<Bin> bins, Collection<BinObject> objects) {
 		double sol = 0;
 		
 		ArrayList<BinObject> tempObjects = new ArrayList<BinObject>();
 		tempObjects.addAll(objects);
 		
 		for (Bin bin : bins) {
 			boolean[] chosenObjectIndex = knapsack(bin, tempObjects);
 			ArrayList<BinObject> chosenObjects = new ArrayList<BinObject>();
 			for (int i=0; i<tempObjects.size(); i++) {
 				if (chosenObjectIndex[i]) chosenObjects.add(tempObjects.get(i));
 			}
 			for (BinObject obj : chosenObjects) {
 				sol += obj.getValue();
 				tempObjects.remove(obj);
 			}
 		}
 		
 		return sol;
 	}
 	
 	/**
 	 * Takes a single Bin and a collection of BinObjects to calculate the optimal solution to the
 	 * Bin-Packing problem. The Bin-Packing problem consists of maximizing the sum of the value of
 	 * the objects in a bin, where the bin can only carry a limited weight-capacity, and each object
 	 * has a specified weight and value.
 	 * @param bin The bin to be packed.
 	 * @param objects The collection of objects the algorithm can choose from.
 	 * @return An array indicating which objects are to be included in the optimal solution. If the ith
 	 * element in the array is true, then the ith object in the objects collection was included in the
 	 * optimal solution. If the ith element of the array is false, then the ith object in the collection
 	 * was not included in the optimal solution.
 	 */
 	private boolean[] knapsack(Bin bin, Collection<BinObject> objects) {
 		Object[] objs = objects.toArray();
 		int numObjs = objs.length;
 		int capacity = (int) Math.floor(bin.getCapacity()); //Bins are allowed to hold decimal-value weight 
 		//capacities; however, for the sake of computing this algorithm in a reasonable amount of time, we 
 		//work with the rounded-down bin capacity.
 
 		double[][] optSol = new double[numObjs+1][capacity+1]; //This array contains the optimal solution value 
 		//for packing objects from the set of objs[0] to objs[n] into a bin with capacity from 0 to j
 		
 		for (int c=0; c<capacity+1; c++) optSol[0][c] = 0; //Since packing from the choice set of 0 items has 
 		//0 value regardless of the bin's capacity
 		
 		boolean[][] solChoice = new boolean[numObjs+1][capacity+1]; //Array keeping track of whether the corresponding
 		//optSol[][] value includes item n
 		
 		for (int c=0; c<capacity+1; c++) solChoice[0][c] = false; //Since you are packing no items when you pick from the
 		//set of 0 objects
 
 		for (int n=1; n<numObjs+1; n++) {
 			//Get the object's weight and value, rounding weight to the corresponding integer once again
 			int itemWeight = (int) Math.ceil(((BinObject)objs[n-1]).getWeight());
 			double itemValue = ((BinObject)objs[n-1]).getValue();
 
 			for (int c=0; c<capacity+1; c++) {
				double leaveItemSolVal = optSol[n-1][c]; //Solution value if we were to leave item n
 
 				double takeItemSolVal = 0; //Solution value if we were to take item n
 				if (itemWeight <= c) takeItemSolVal = itemValue + optSol[n-1][c-itemWeight];
 
 				//Select optimal choice: take or leave the item
 				if (takeItemSolVal > leaveItemSolVal) {
 					optSol[n][c] = takeItemSolVal;
 					solChoice[n][c] = true; //Since we took the item
 				} else {
 					optSol[n][c] = leaveItemSolVal;
 					solChoice[n][c] = false; //Since we left the item
 				}
 			}
 
 		}
 		
 		//Now we calculate which items were chosen in the optimal solution
 		boolean[] choices = new boolean[numObjs]; //choices[i]=true indicates we took the ith item
 		for (int n=numObjs, c=capacity; n>0; n--) {
 			if (solChoice[n][c]) {
 				choices[n-1] = true;
 				c = c - (int) Math.ceil(((BinObject)objs[n-1]).getWeight());
 			} else choices[n-1] = false;
 		}
 		
 		return choices;
 	}
 	
 }
