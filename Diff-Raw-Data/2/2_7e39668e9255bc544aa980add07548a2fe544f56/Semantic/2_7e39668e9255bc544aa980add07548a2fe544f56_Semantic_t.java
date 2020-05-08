 package semantic;
 import parser.Procedure;
 import semanticlib.SymbolTable;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 public class Semantic {
 	
 	private ArrayList<Exception> errors;
 	private Stack<Procedure> currentProcedure;
 	
 	public Semantic() {
 		errors = new ArrayList<Exception>();
 		currentProcedure = new Stack<Procedure>();
 	}
 	
 	public void enterProcedure(Procedure p) {
 		currentProcedure.push(p);
 	}
 	
	public void exitProcedure() {
 		currentProcedure.pop();
 	}
 	
 	public Procedure currentProcedure() {
 		return currentProcedure.peek();
 	}
 	
 	public void addError(Exception e) {
 		errors.add(e);
 	}
 	
 	public int numErrors() { return errors.size(); }
 	public void printAnalysis() {
 		if(errors.isEmpty())
 			System.out.println("No errors!");
 		else {
 			System.out.println("errors found");
 			for(Exception e : errors)
 				System.out.println("Error: " + e);
 		}
 	}
 }
