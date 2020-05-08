 package de.Lathanael.SimpleCalc.Parser;
 
 import de.Lathanael.SimpleCalc.SimpleCalc;
 import de.Lathanael.SimpleCalc.Exceptions.MathSyntaxMismatch;
 
 public class UnaryFunctions extends Operator {
 	private String name;
 	private int countArg;
 	private String playerName;
 
 	/**
 	* Class to create a function with a name and a list of arguments
 	*
 	* @param name
 	* @param args
 	*/
 	public UnaryFunctions (String name, String playerName){
 		super("unFUNCTION");
 		this.name = name;
 		this.playerName = playerName;
 		countArg = 0;
 	}
 
 	public double compute (double[] args) throws MathSyntaxMismatch {
 		if (name.equalsIgnoreCase("ans")) {
 			try {
 				double value = SimpleCalc.answer.get(playerName);
 				return value;
 			} catch (NullPointerException e) {
 				throw new MathSyntaxMismatch("Could not retrieve 'answer' variable.");
 			}
 		} else {
			throw new MathSyntaxMismatch("Object(" + name + ") was declared as a function but could not be matched to any known function.");
 		}
 	}
 
 	public void incArgCount () {
 		countArg++;
 	}
 
 	public void setArgCount (int numArgs) {
 		this.countArg = numArgs;
 	}
 
 	public int getArgCount () {
 		return countArg;
 	}
 
 }
