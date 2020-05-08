 package simplex;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  * Data class to hold tableaus.
  * If we have an array 
  * 		test[0][1] = 1;
  *		test[1][0] = 2;
  *		test[1][2] = 3;
  * then the first dimension is the column and the 2nd the row ((x,y) coordinates).
  * Note that the topmost row is the the 0th column.
  *
  */
 public class Tableau {
 	public double[][] tableau;
 	
 	public Tableau(double[][] tableau){
 		this.tableau = tableau;
		checkSoundness();
 	}
 	
 	
 	public void print(){
 		for(double[] innerArray : tableau ){
 			for(double i : innerArray){
 				System.out.print(i);
 			}
 			System.out.println();
 		}
 	}
 	
	public static enum PivotResult{OPTIMAL, INFINITE_COSTS, BASIS_CHANGED}
 	/**
 	 * Performs a pivot operation. This means it tries to decrease te cost function.
 	 * The result can be 
 	 * 	- The tableau is optimal
 	 *  - The optimal cost is -infinity
 	 *  - The cost is decreased by a change of basis represented in the tableau. 
 	 */
 	public PivotResult pivot (){
 		checkSoundness();
 		/* 
 		 * first find a variable with negative reduced costs.
 		 * If we do not find one, we already have an optimal solution
 		 */
 		int enteringVariable = Util.NOTHING;
 		for (int i = 0; i < tableau.length; i++) {
 			if(Util.smaller(tableau[i][0], 0)){
 				enteringVariable = i;
 				break;
 			}
 		}
 		if(enteringVariable == Util.NOTHING)
 			return PivotResult.OPTIMAL;
 		/*
 		 * Then if the j'th variable has column vector <= 0 the optimal cost is
 		 * -infinite and we terminate.
 		 */
 		if(Util.isNonPositive(tableau[enteringVariable]))
 			return PivotResult.INFINITE_COSTS;
 
 		/*
 		 * Then we collect all rows that have a ratio equal to the minRatio
 		 * And determine the lex-smallest of those rows.
 		 */
 		double minRatio = getMinRatio(enteringVariable);
 		List<Integer> minRatioRows = getMinRatioRows(minRatio, enteringVariable);
		int leavingVariable = getLexSmallestRow(minRatioRows);
 		/*
 		 * Then column i leaves the basis and column j enters it.
 		 * Make it such, that the column j has at row i ((j,i) is pivot element) 
 		 * a 1 and every else 0.
 		 */
		multiplyBy(1 / tableau[enteringVariable][leavingVariable], leavingVariable);
		for (int row = 0; row < nrOfColumns(); row++) {
			if(row != leavingVariable){
				addMultiple(-tableau[enteringVariable][row], leavingVariable, row);
			}
		}
		return PivotResult.BASIS_CHANGED;
 	}
 	/**
 	 * For a list of vectors decides which vector is the lexicographically
 	 * smallest vector
 	 */
 	public int getLexSmallestRow(List<Integer> indices){
 		for (int i = 1; i < nrOfColumns(); i++) {
 			double[] vector = new double[indices.size()];
 			/*
 			 * Create the columns to be compared
 			 */
 			for (int k = 0; k < indices.size(); k++) {
 				vector[k] = tableau[i][indices.get(k)];
 			}
 			List<Integer> smallest = Util.smallestIndices(vector);
 			if(smallest.size() == 1)
 				return indices.get(smallest.get(0));
 			else
 				indices = smallest;
 		}
 		throw new RuntimeException("Probably only reachable if the lex-smallest is not unique, which we do assume to be.");
 	}
 	
 	/**
 	 * Gets the rows that have the minRatio
 	 */
 	private List<Integer> getMinRatioRows(double minRatio, int pivotColumn) {
 		List<Integer> result = new ArrayList<>();
 		for (int i = 0; i < tableau[pivotColumn].length; i++) {
 			double currentRatio = tableau[0][i] / tableau[pivotColumn][i];
 			if(Util.areEqual(currentRatio, minRatio))
 				result.add(i);
 		}
 		return result;
 	}
 
 
 	/**
 	 * Calculates the minimum ratio x_{B(i)} / u_i for all rows i and the given column
 	 * note that <code>x_{B(i)} = tableau[0][i]</code> 
 	 * and <code>u_i = tableau[0][pivotColumn]</code>   
 	 */
	private double getMinRatio(int pivotColumn){
 		double minRatio = Double.NaN;
 		for (int i = 0; i < tableau[pivotColumn].length; i++) {
 			double currentRatio = tableau[0][i] / tableau[pivotColumn][i];
 			if(Util.smaller(currentRatio, minRatio))
 				minRatio = currentRatio;
 		}
 		return minRatio;
 	}
 	
 	/**
 	 * Multiplies the defined the row by a constant
 	 * @param multiplier the constant to multiply the row by
 	 * @param row the row to multiply
 	 */
 	public void multiplyBy(double multiplier, int row){
 		addMultiple(multiplier, row, row);
 	}
 	/**
 	 * Adds a multiplicative of a row to another row.
 	 * Be careful that <code>toAdd = addTo</code> may be the case.
 	 * 
 	 * @param multiplier the constant to multiply <code>toAdd</code> by
 	 * @param toAdd the row to add to another row
 	 * @param addTo the row which should be changed
 	 */
 	public void addMultiple(double multiplier, int toAdd, int addTo){
 		for(int i = 0; i < nrOfColumns(); i++){
			tableau[i][addTo] += tableau[i][toAdd] * multiplier; 
 		}
 	}
	
	
 	/**
 	 * Checks the soundness of the current tableau. 
 	 * Makes sure, that we have the correct number of unitVectors (one for each basic variable)
 	 * and that the reduced cost of each basic variable 
 	 * is 0
 	 * 
 	 */
 	public void checkSoundness(){
 		Util.checkIsMatrix(tableau);
 		
 		/*
 		 * This part ensures there are m linear independent unit vectors.
 		 * m is the (number of rows - 1) in the tableau
 		 */
 		List<Integer> indices = new ArrayList<>(nrOfColumns() - 1);
 		/*
 		 * First we calculate the nr of unitVectors and store the position of the 1s
 		 * Note that we start from 1 cause the first row and first column are different 
 		 */
 		for (int i = 1; i < nrOfColumns(); i++) {
 			if(Util.isUnitVector(tableau[i]) >= 0){
 				indices.add(Util.isUnitVector(tableau[i]));
 				
 				if(!Util.areEqual(tableau[i][0], 0))
 					throw new IllegalArgumentException(
 							"The reduced costs of the basic varaibles have to be 0!");
 			}
 		}
 		if(indices.size() != nrOfColumns() -1)
 			throw new IllegalArgumentException("Tableau should have m basic variables!");
 		/*
 		 * Now we ensure that the unit-vectors are linearly independend (no two are equal)
 		 */
 		if(indices.size() != new HashSet<Integer>(indices).size())
 			throw new IllegalArgumentException("There are some unitvectors that have 1 at the same position!");
 	}
 	
 	public int nrOfColumns(){
 		return tableau[0].length;
 }
 
 	public int nrOfRows(double[][] tableau){
 		return tableau.length;
 	}
 }
