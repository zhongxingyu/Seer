 package symbol_table;
 
 import org.antlr.runtime.tree.Tree;
 
 public class StatementChecker {
	
 	public static void checkStatement(Tree node, SymbolTable symbolTable)
 	{
 		if (node.getText().contentEquals("was"))
 		{
 			String var = node.getChild(0).getText();
 			
 			if (symbolTable.checkVariableScopeInCurrLevel(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() 
 						+ " Multiple declarations of " + node.getText());
 			} else 
 			{
 				DATA_TYPES type = DATA_TYPES.valueOf((node.getChild(1).getText()).toUpperCase());
 				symbolTable.insert(var, new VariableSTValue(type, false));
 			}
 							
 		}
 		
 		if ( node.getText().contentEquals("ate") ||  node.getText().contentEquals("drank"))
 		{	
 			String var = node.getChild(0).getText();
 			
 			if (!symbolTable.checkVariableScopeInAllReleventLevels(null))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + " : "
 						+ var + " out of scope.");
 			}
 
 			else if ( !((VariableSTValue) symbolTable.lookup(node.getChild(0).getText())).isInitialised())
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" not initialised yet" + node.getText());				
 			}
 			
 			else if ((symbolTable.lookup(node.getChild(0).getText())).getType() != DATA_TYPES.NUMBER)
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + " : "
 						+ var + " not a number.");
 			}
 			
 		}		
 
 		if ( node.getText().contentEquals("became"))
 		{
 			String var = node.getChild(0).getText();
 			
 			if (!symbolTable.checkVariableScopeInAllReleventLevels(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" out of scope" + node.getText());
 			}
 			
 			else if ((symbolTable.lookup(node.getChild(0).getText())).getType() != node.getChild(1).getType())    //**************************** Need to find a method for passing expressions 
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + " : Types of "
 						+ var + " and the subcribed value don't match.");
 			}
 			
 			else
 			{
 				((VariableSTValue) symbolTable.lookup(node.getChild(0).getText())).setInitialised(true) ;
 			}
 			
 		}
 		
 		if ( node.getText().contentEquals("spoke") || node.getText().contentEquals("said"))
 		{
 			String var = node.getChild(0).getText();
 
 			if (!symbolTable.checkVariableScopeInAllReleventLevels(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" out of scope" + node.getText());				
 			}
 			
 			if ( !((VariableSTValue) symbolTable.lookup(node.getChild(0).getText())).isInitialised() )
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" not initialised yet" + node.getText());				
 			}
 		}
 		
 	}
 	
 	private static void checkIfTypesMatch(Tree node, SymbolTable symbolTable) {
 		
 		((VariableSTValue)symbolTable.lookup(node.getChild(0).getText())).getType();//type x == number
 		node.getChild(1);//symTable lookup this node?
 		
 	}
 
 }
