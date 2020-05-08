 package com.dasanjos.java.zebraPuzzle;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.dasanjos.java.util.Property;
 import com.dasanjos.java.util.file.CSVReader;
 import com.dasanjos.java.util.math.PermutationIterator;
 import com.dasanjos.java.util.math.PermutationWithRepetitionIterator;
 import com.dasanjos.java.zebraPuzzle.model.PuzzleRule;
 import com.dasanjos.java.zebraPuzzle.model.PuzzleSolution;
 
 /**
  * <p>
  * Java Brute Force implementation of <a href="http://en.wikipedia.org/wiki/Zebra_Puzzle"> Zebra Puzzle</a> (also called Einstein's Puzzle)
  * </p>
  * 
  * <b>Example input file (input.csv)</b> <br />
  * 
  * <pre>
  * 5
  * SAME,nationality,English,color,Red
  * SAME,nationality,Spaniard,pet,Dog
  * SAME,drink,Coffee,color,Green
  * SAME,drink,Tea,nationality,Ukrainian
  * LEFT,color,Ivory,color,Green
  * SAME,smoke,Old gold,pet,Snails
  * SAME,smoke,Kools,color,Yellow
  * SAME,drink,Milk,position,3
  * SAME,nationality,Norwegian,position,1
  * NEXT,smoke,Chesterfields,pet,Fox
  * NEXT,smoke,Kools,pet,Horse
  * SAME,smoke,Lucky strike,drink,Orange juice
  * SAME,smoke,Parliaments,nationality,Japanese
  * NEXT,color,Blue,nationality,Norwegian
  * SAME,drink,Water
  * SAME,pet,Zebra
  * </pre>
  * 
  * <b>Example output file (output.xml)</b><br />
  * 
  * <pre>
  * &lt;solutions>
  *   &lt;solution>
  *     &lt;house position="1" color="Yellow" nationality="Norwegian" drink="Water"        smoke="Kools"         pet="Fox"/>
  *     &lt;house position="2" color="Blue"   nationality="Ukrainian" drink="Tea"          smoke="Chesterfields" pet="Horse"/>
  *     &lt;house position="3" color="Red"    nationality="English"   drink="Milk"         smoke="Old gold"      pet="Snails"/>
  *     &lt;house position="4" color="Ivory"  nationality="Spaniard"  drink="Orange juice" smoke="Lucky strike"  pet="Dog"/>
  *     &lt;house position="5" color="Green"  nationality="Japanese"  drink="Coffee"       smoke="Parliaments"   pet="Zebra"/>
  *   &lt;/solution>
  * &lt;/solutions>
  * </pre>
  */
 public class BruteForceSolver {
 
 	protected int houses;
 
 	protected List<Property> properties;
 
 	protected List<PuzzleRule> rules;
 
 	public BruteForceSolver(File input) throws FileNotFoundException {
 		this(new CSVReader(input, ","));
 	}
 
 	public BruteForceSolver(CSVReader reader) {
 		properties = new ArrayList<Property>();
 		rules = new ArrayList<PuzzleRule>();
 		parseInputCSV(reader);
 	}
 
 	/**
 	 * Parse input values and generate internal lists of unique Properties for solution generation and Rules for solution validation
 	 * 
 	 * @param input CSV File with Zebra Puzzle input content
 	 */
 	private void parseInputCSV(CSVReader reader) {
 		// Read Number of Houses
 		List<String> values = reader.readLine();
 		this.houses = Integer.parseInt(values.get(0));
 
 		// Read Rules and Calculate Unique Properties
 		while ((values = reader.readLine()) != null) {
 			Property property1 = new Property(values.get(1), values.get(2));
 			if (!"position".equals(property1.getKey()) && !properties.contains(property1)) {
 				properties.add(property1);
 			}
 			if (values.size() == 5) {
 				Property property2 = new Property(values.get(3), values.get(4));
				if (!"position".equals(property1.getKey()) && !properties.contains(property2)) {
 					properties.add(property2);
 				}
 				rules.add(new PuzzleRule(values.get(0), property1, property2));
 			}
 		}
 	}
 
 	/**
 	 * Generate all possible solutions based on the number of houses (rows) and number of unique properties (columns)
 	 */
 	public List<PuzzleSolution> generateSolutions() {
 		List<PuzzleSolution> solutions = new ArrayList<PuzzleSolution>();
 		List<String> keys = Property.getUniqueKeys(properties);
 
 		Integer[] permIndex = new Integer[houses]; // Initialize array of possible permutations indexes
 		for (int nr = 0; nr < houses; nr++) {
 			permIndex[nr] = nr;
 		}
 		// Generate all permutations without repetition of properties (propNr!)
 		PermutationIterator<Integer> propPermutator = new PermutationIterator<Integer>(permIndex);
 		List<Integer[]> propPermutations = new ArrayList<Integer[]>();
 		while (propPermutator.hasNext()) {
 			propPermutations.add(propPermutator.next());
 		}
 
 		Integer[] solutionIndex = new Integer[propPermutations.size()]; // Initialize array of possible combinations indexes
 		for (int nr = 0; nr < propPermutations.size(); nr++) {
 			solutionIndex[nr] = nr;
 		}
 		// Generate all permutations with repetition of previous permutations
 		PermutationWithRepetitionIterator<Integer> solPermutator = new PermutationWithRepetitionIterator<Integer>(solutionIndex, houses);
 		while (solPermutator.hasNext()) {
 			// Map this combination of Permutations to a Solution
 			Integer[] solPermutation = solPermutator.next();
 
 			// Initialize solution (Nr. of houses is same for each solution)
 			PuzzleSolution solution = new PuzzleSolution(houses);
 			for (int nr = 0; nr < houses; nr++) {
 				Integer[] propPermutation = propPermutations.get(solPermutation[nr]);
 				for (int k = 0; k < propPermutation.length; k++) {
 					String key = keys.get(nr);
 					solution.getHouse(k).putProperty(key, Property.getValues(key, properties).get(propPermutation[k]));
 				}
 			}
 			solutions.add(solution);
 		}
 		return solutions;
 	}
 
 	// Validate all possible solutions with all rules and return valid solutions
 	public List<PuzzleSolution> getValidSolutions(List<PuzzleSolution> possibleSolutions) {
 		List<PuzzleSolution> solutions = new ArrayList<PuzzleSolution>();
 		for (PuzzleSolution solution : possibleSolutions) {
 			boolean valid = true;
 			Iterator<PuzzleRule> iterator = rules.iterator();
 			while (valid && iterator.hasNext()) {
 				PuzzleRule rule = iterator.next();
 				valid = rule.isValidSolution(solution);
 			}
 			if (valid) {
 				solutions.add(solution);
 			}
 		}
 		return solutions;
 	}
 }
