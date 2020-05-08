 
 package symbol_table;
 
 import org.antlr.runtime.tree.Tree;
 
 import semantics_checks.SemanticsUtils;
 
 public class StatementChecker
 {
 
 	public static Tree checkAllStatements(Tree node, SymbolTable table) 
 	{
 		Tree current = node ;
 		boolean end_of_statements = false;
 		while(current != null && !end_of_statements)
 		{
 			end_of_statements = checkStatement(current, table);
 			if (end_of_statements) return current ;
 			current = SemanticsUtils.getNextChild(current) ;
 		}
 
 		return current;
 
 	}
 
 
 	public static boolean checkStatement(Tree node, SymbolTable symbolTable)
 	{
 		if (node.getChildCount()==0) { return true; }
 
 
 		if (node.getText().contentEquals("was"))
 		{
 			String var = node.getChild(0).getText();
 
 			if (symbolTable.checkVariableIsInCurrentScopeLevel(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() 
 						+ " Multiple declarations of " + var);
 			}
 
 			else if (node.getChildCount() > 2 &&
 					( ExpressionChecker.getExpressionType(node.getChild(2), symbolTable) !=
 						(SemanticsUtils.getReturnType(node.getChild(1)))))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() 
 						+ "Data types of " + var + " and subscribed value don't match.");
 			}
 
 			else 
 			{
 				DATA_TYPES type = ExpressionChecker.getExpressionType(node.getChild(0), symbolTable);
 				symbolTable.insert(var, new VariableSTValue(type, false));
 			}
 
 			return false;
 
 		}
 
 		else if ( node.getText().contentEquals("ate") ||  node.getText().contentEquals("drank"))
 		{	
 			String var = node.getChild(0).getText();
			if (!symbolTable.checkItemWasDeclaredBefore(var))
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
 
 			return false;
 
 		}		
 
 		else if ( node.getText().contentEquals("became"))
 		{
 			String var = node.getChild(0).getText();
			if (!symbolTable.checkItemWasDeclaredBefore(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" out of scope" + node.getText());
 			}
 
 			else if ((symbolTable.lookup(node.getChild(0).getText())).getType() !=
 				ExpressionChecker.getExpressionType(node.getChild(node.getChildCount() - 1), symbolTable))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + " : Types of "
 						+ var + " and the subcribed value don't match.");
 			}
 
 			else if (node.getChildCount() > 2 &&
 					ExpressionChecker.getExpressionType(node.getChild(1), symbolTable) != DATA_TYPES.NUMBER)
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + " : Length of the Array" + var + " should be an Integer");
 			}
 
 			else
 			{
 
 				((VariableSTValue) symbolTable.lookup(node.getChild(0).getText())).setInitialised(true) ;
 
 			}
 
 			return false;
 		}
 
 		else if ( node.getText().contentEquals("spoke") || node.getText().contentEquals("said"))
 
 		{
 			String var = node.getChild(0).getText();
			if (!symbolTable.checkItemWasDeclaredBefore(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" out of scope" + node.getText());				
 			}
 
 			else if ( !((VariableSTValue) symbolTable.lookup(node.getChild(0).getText())).isInitialised() )
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" not initialised yet" + node.getText());				
 
 			}
 
 			return false;
 		}
 
 		else if ( node.getText().contentEquals("what"))
 
 		{
 			String var = node.getChild(0).getText();
			if (!symbolTable.checkItemWasDeclaredBefore(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" out of scope" + node.getText());				
 			}
 
 			else
 			{
 				((VariableSTValue) symbolTable.lookup(node.getChild(0).getText())).setInitialised(true);
 			}
 
 			return false;
 		}
 
 		else if ( node.getText().contentEquals("found"))
 
 		{
 			String var = node.getChild(0).getText();
			if (!symbolTable.checkItemWasDeclaredBefore(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + ": "
 						+ var +" out of scope" + node.getText());				
 			}
 
 			else if ( !((VariableSTValue) symbolTable.lookup(node.getChild(0).getText())).isInitialised() )
 			{
 				System.err.println("Line "+ node.getLine()+ ": "
 						+ node.getCharPositionInLine() + ": "
 						+ var +" not initialised yet" + node.getText());				
 			}
 			return false;
 		}
 
 		else if ( node.getText().contentEquals("had"))
 		{
 			String var = node.getChild(0).getText();
 
 			if (symbolTable.checkVariableIsInCurrentScopeLevel(var))
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() 
 						+ " Multiple declarations of " + node.getText());
 			}
 
 			if ((symbolTable.lookup(node.getChild(1).getText())).getType() != DATA_TYPES.NUMBER)
 			{
 				System.err.println("Line "+ node.getLine()+ ": " 
 						+ node.getCharPositionInLine() + " : "
 						+ var + " not a number.");
 			}
 			return false;
 
 		}
 
 		return true;
 	}
 
 }
