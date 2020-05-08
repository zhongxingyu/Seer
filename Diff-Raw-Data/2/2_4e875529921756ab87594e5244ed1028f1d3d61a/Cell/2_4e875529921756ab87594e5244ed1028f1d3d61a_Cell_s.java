 package model;
 
 import java.util.regex.Pattern;
 import com.eteks.parser.CompilationException;
 import com.eteks.parser.CompiledExpression;
 import com.graphbuilder.math.Expression;
 import com.graphbuilder.math.ExpressionParseException;
 import com.graphbuilder.math.ExpressionTree;
 import com.graphbuilder.math.FuncMap;
 import com.graphbuilder.math.VarMap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.TreeMap;
 
 public class Cell {
 
 	private int row;
 	private int column;
 	private String rawExpression;
 	private Object calculatedValue;
 	private Sheet sheet;
 	private HashMap<String,Cell> cellsReferingToMe;
 	private HashMap<String,Cell> cellsIReferTo;
 	private Expression cellExpression;
 
 	public Cell(Sheet sheet, int row, int col, Object value) {
 		this.sheet = sheet;
 		this.row = row;
 		this.column = col;
 		this.calculatedValue = value;
 		this.cellsIReferTo = new HashMap<String,Cell>();
 	}
 
 	/**
 	 * Raw meaning before being processed by our parser.
 	 * 
 	 * @return The expression that is being held by this cell. The expression
 	 *         raw expression is not only the expression that our parser deals
 	 *         with. It could also be any value that it is given.
 	 */
 	public String getRawExpression() {
 		return rawExpression;
 	}
 
 	/**
 	 * Checks if the rawExpression starts with an '='. If it does then it does
 	 * nothing (right now). If it doesn't start with that then it sets our
 	 * calculatedValue to the rawExpression (since no calculations are needed)
 	 * and then sets new rawExpression.
 	 * 
 	 * @param rawExpression
 	 */
 	public void setRawExpression(String rawExpression) {
 
 		this.rawExpression = rawExpression;
 
 		if(rawExpression.length() == 0){
 			calculatedValue = rawExpression;
 			return;
 		}
 
 		boolean startsWithEquals = rawExpression.substring(0,1).equals("=");
 
 		if (startsWithEquals) {
 			String rawExpressionWithoutEquals = rawExpression.substring(1, rawExpression.length());
 			calculatedValue = compileRawExpression(rawExpressionWithoutEquals);
 
 		} else {
 			calculatedValue = rawExpression;
 
 
 		}
 
 	}
 
 	// return the value to be displayed after compilation
 	private Object compileRawExpression(String rawExpression){
 
 		System.out.println("compiling raw expression...");
 
 		// Handle Incomplete Expression exception ExpressionParseException
 		try{
 			cellExpression = ExpressionTree.parse(rawExpression);
 		} catch (ExpressionParseException e){
 			return "ERR";
 		}
 		String[] cellsReferencedInExpression = cellExpression.getVariableNames();
 
 		System.out.println("\t" + cellsReferencedInExpression.length + " variable(s) found");
 
 		for(String cellName : cellsReferencedInExpression){
 			System.out.println("\tLooking up '" + cellName + "':" );
 			Cell thisReferencedCell = sheet.getCells().get(cellName);
 
 			// failed to lookup cell by name
 			// the user probably entered an expression with a bad cell name
 			if(thisReferencedCell == null){
 				return "ERR";
 			}
 			// successfuly found cell by name
 			// add this cell to cellsIReferTo
 			else{
 				System.out.println("\t\tAbout to add cell to 'cells i refer to': " + thisReferencedCell);
 				System.out.println("\t\t" + thisReferencedCell.toString());
 				cellsIReferTo.put(cellName, thisReferencedCell);
 			}
 		}
 
 		// now that 'cellsIReferTo' is populated with verified cell objects,
 		// get and return the value of this cell (start recursive call)
 		try {
 			Stack<Cell> previouslyVisitedCells = new Stack<Cell>();
 			try{
 				calculatedValue = getValueRecursively(previouslyVisitedCells);
 			} catch (NumberFormatException e){
 				return "ERR: non-number references";
 			}
 		} catch (CircularityException e) {
 			return "ERR: Circularity!";
 		}
 		
 		Double thisValue = (Double)calculatedValue;
 		Integer thisIntValue = thisValue.intValue();
 		
 		// if casting this cell's value to an integer doesn't change its
 		// value, return the integer version
 		// otherwise, return the double version
 		if(Double.compare(thisValue, thisIntValue.doubleValue()) == 0){
 			return thisIntValue;
 		}
 		else{
 			return calculatedValue;
 		}
 		
 	}
 
 	private double getValueRecursively(Stack<Cell> visitedCells) throws CircularityException{
 
 		// if this cell is already in the stack, throw CircilarityException
 		if(visitedCells.search(this) != -1){
 			throw new CircularityException();
 		}
 		else{
 			
 			visitedCells.push(this);
 			
 			// if this cell contains an Expression object
 			if(cellExpression != null){
 				VarMap varmap = new VarMap();
 				
 				String[] cellNamesInExpression = cellExpression.getVariableNames();
 				
 				for(String cellName : cellNamesInExpression){
 					double value = cellsIReferTo.get(cellName).getValueRecursively(visitedCells);
 					varmap.setValue(cellName, value);
 				}
 				
 				// now that the varmap is populated, use it to evaluate the expression
 				return cellExpression.eval(varmap, new FuncMap());
 			}
 			else{
 				//System.out.println(calculatedValue.getClass());
 				return Double.parseDouble((String)this.calculatedValue);
 			}
 		}
 	}
 
 	/**
 	 * Value calculated when we set the raw expression. No additional
 	 * calculation or work is necessary here because will already be calculated.
 	 * 
 	 * @return The object that we ended up with at the end of the
 	 *         setRawExpression method. Could be String, Int, Double, etc.
 	 */
 	public Object getCalculatedValue() {
 
 		return calculatedValue;
 	}
 
 	public String toString(){
 		String returnMe = new String();
 
 		returnMe += "Cell at " + this.row + "," + this.column + " (row,column). Value = " + this.getRawExpression();
 
 		return returnMe;
	}f
 
 }
