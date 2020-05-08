 package codeGeneration;
 
 
 import java.util.ArrayList;
 
 import org.antlr.runtime.tree.Tree;
 
 import symbol_table.DATA_TYPES;
 import symbol_table.FunctionSTValue;
 import symbol_table.SymbolTable;
 
 public class Expression
 {
 	
 	public static String getResultReg(Tree node, SymbolTable table, LabelGenerator gen)
 	{
 		if (node==null) return null;
 		if (node.getChildCount()==0) return makeVar(node, table,gen);
 		//AFTER CHECKING FOR NO CHILD
 		OPERATOR op = getOperator(node.getText());
 		
 		//checking for single arity functions would be checked before this point
 		String arg1 = getResultReg(node.getChild(0), table, gen);
 		String arg2 = getResultReg(node.getChild(1), table, gen);
 
 		try{
 			int i = Integer.parseInt(arg1);
 			if( op==OPERATOR.NOT || op==OPERATOR.BWNOT) {
 				return calculateUanary(op, i) + "";
 			}
 			int j = Integer.parseInt(arg2);
 			return calculateExpr(op, i, j) + "";
 		} catch (NumberFormatException e)
 		{ }
 		String uniqueRegisterID = gen.getUniqueRegisterID();
 		if(op==null)
 		{
			System.out.println("in func");
 			String id = writeCodeForFunctionCall(node, table,gen);
 			return id ;
 		}
 		switch(op)
 		{
 			case EQ: 
 				uniqueRegisterID = 
 					writeComparisonStatements(uniqueRegisterID, "eq", arg1, arg2,gen);
 				break;
 			case ADD:
 				writeOperationExpressions(uniqueRegisterID, "add", arg1, arg2);
 				break;
 			case OR:
 				writeOrStatement(arg1, arg2, uniqueRegisterID,gen);
 				break;
 			case AND:
 				uniqueRegisterID = writeAndStatement(arg1, arg2, uniqueRegisterID, gen);
 				break;
 			case BWOR:
 				writeOperationExpressions(uniqueRegisterID, "or", arg1, arg2);
 				break;
 			case BWXOR:
 				writeOperationExpressions(uniqueRegisterID, "xor", arg1, arg2);
 				break;
 			case BWAND:
 				writeOperationExpressions(uniqueRegisterID, "and", arg1, arg2);
 				break;
 			case NE:
 				uniqueRegisterID = writeComparisonStatements(uniqueRegisterID, "ne", arg1, arg2, gen);
 				break;
 			case LTE:
 				uniqueRegisterID = writeComparisonStatements(uniqueRegisterID, "sle", arg1, arg2,gen);
 				break;
 			case LT:
 				uniqueRegisterID = writeComparisonStatements(uniqueRegisterID, "slt", arg1, arg2,gen);
 				break;
 			case GT:
 				uniqueRegisterID = writeComparisonStatements(uniqueRegisterID, "sgt", arg1, arg2, gen);
 				break;
 			case GTE:
 				uniqueRegisterID = writeComparisonStatements(uniqueRegisterID, "sge", arg1, arg2, gen);
 				break;
 			case SUB:
 				writeOperationExpressions(uniqueRegisterID, "sub", (arg2==null)? "0" : arg2 , arg1);
 				break;
 			case MUL:
 				writeOperationExpressions(uniqueRegisterID, "mul", arg1, arg2);
 				break;
 			case DIV:
 				writeOperationExpressions(uniqueRegisterID, "sdiv", arg1, arg2);
 				break;
 			case MOD:
 				writeOperationExpressions(uniqueRegisterID, "srem", arg1, arg2);
 				break;
 			case BWNOT:
 				writeOperationExpressions(uniqueRegisterID, "xor", arg1, "-1");
 				break;
 			case NOT:
 				uniqueRegisterID = writeNotStatement(uniqueRegisterID,arg1, gen);
 				break;
 		}
 		return uniqueRegisterID;
 	}
 	private static int calculateExpr(OPERATOR op, int i, int j)
 	{
 		switch(op)
 		{
 			case EQ:
 				return (i==j)? 1 : 0 ;
 			case ADD:
 				return i+j;
 			case BWOR:
 				return i|j;
 			case BWXOR:
 				return i^j;
 			case BWAND:
 				return i&j;
 			case NE:
 				return (i!=j)? 1 : 0 ;
 			case LTE:
 				return (i<=j)? 1 : 0 ;
 			case LT:
 				return (i<j)? 1 : 0 ;
 			case GT:
 				return (i>j)? 1 : 0 ;
 			case GTE:
 				return (i>=j)? 1 : 0 ;
 			case SUB:
 				return i-j;
 			case MUL:
 				return i*j;
 			case DIV:
 				return i/j;
 			case MOD:
 				return i%j;
 		}
 		return -1;
 	}
 	private static int calculateUanary(OPERATOR op, int i)
 	{
 		switch(op)
 		{
 			case NOT:
 				return (i==0)? 1 : 0 ;
 			case BWNOT:
 				return ~i;
 		}
 		return -1;
 	}
 	private static String writeNotStatement(String uniqueRegisterID, String arg1, LabelGenerator gen)
 	{
 		writeComparison(uniqueRegisterID, "eq", arg1, "0");
 //		String prev = uniqueRegisterID;
 //		uniqueRegisterID = gen.getUniqueRegisterID();
 //		writeExtensionIns(uniqueRegisterID, prev);
 		return uniqueRegisterID;
 	}
 	private static String writeOrStatement(String arg1, String arg2,
 			String uniqueRegisterID,LabelGenerator gen)
 	{
 		CodeGenerator.addInstruction(uniqueRegisterID + " = or i32 " + arg2 + ", " + arg1);
 		String prev = uniqueRegisterID;
 		uniqueRegisterID = gen.getUniqueRegisterID();
 		writeComparison(uniqueRegisterID, "ne", prev, "0");
 //		prev = uniqueRegisterID;
 //		uniqueRegisterID = gen.getUniqueRegisterID();
 //		writeExtensionIns(uniqueRegisterID, prev);
 		return uniqueRegisterID;
 	}
 	private static String writeAndStatement(String arg1, String arg2,
 			String uniqueRegisterID, LabelGenerator gen)
 	{
 		writeComparison(uniqueRegisterID, "ne", arg2, "0");
 		String a1 = uniqueRegisterID;
 		uniqueRegisterID = gen.getUniqueRegisterID();
 		writeComparison(uniqueRegisterID, "ne", arg1, "0");
 		String a2 = uniqueRegisterID;
 		uniqueRegisterID = gen.getUniqueRegisterID();
 		CodeGenerator.addInstruction(uniqueRegisterID + " = and il " + a1 + ", " + a2);
 //		String ans = uniqueRegisterID;
 //		uniqueRegisterID = gen.getUniqueRegisterID();
 //		writeExtensionIns(uniqueRegisterID, ans);
 		return uniqueRegisterID;
 	}
 	
 	static void writeOperationExpressions(String uniqueRegisterID, 
 			String operation, String arg1, String arg2)
 	{
 		CodeGenerator.addInstruction(uniqueRegisterID + " = " + operation + " nsw i32 " 
 				+ arg1 + ", " + arg2);
 	}
 	private static String writeComparisonStatements(String uniqueRegisterID, 
 			String operation, String arg1, String arg2, LabelGenerator gen)
 	{
 		writeComparison(uniqueRegisterID, operation, arg1, arg2);
 //		String prev = uniqueRegisterID;
 //		uniqueRegisterID = gen.getUniqueRegisterID();
 //		writeExtensionIns(uniqueRegisterID, prev);
 		return uniqueRegisterID;
 	}
 	private static void writeComparison(String uniqueRegisterID,
 			String operation, String arg1, String arg2)
 	{
 		CodeGenerator.addInstruction(uniqueRegisterID + " = " + "icmp " + operation + " i32 " 
 				+ arg1 + ", " + arg2);
 	}
 //	private static void writeExtensionIns(String uniqueRegisterID, String reg)
 //	{
 //		CodeGenerator.addInstruction(uniqueRegisterID + " = zest il " + reg 
 //				+ " to i32");
 //	}
 	
 	
 	private static String makeVar(Tree leaf, SymbolTable table, LabelGenerator gen)
 	{
 		if (leaf.getChildCount()!=0)
 		{
 			return writeCodeForFunctionCall(leaf, table,gen);
 		}
 		if (table.checkItemWasDeclaredBefore(leaf.getText())) 
 		{
 			//THIS NEEDS TO BE THE NEW METHOD WHICH WOULD RETURN THE NAME OF 
 			//THE EXPRESSIONS
 			return table.lookup(leaf.getText()).getLocationReg();
 		}
 		return leaf.getText();
 	}
 	private static String writeCodeForFunctionCall(Tree leaf, SymbolTable table,LabelGenerator gen)
 	{
 		String id = gen.getUniqueRegisterID();
 		String returnType = Utils.getReturnTypeOfFunction(table.lookup
 				(leaf.getText()).getType());
 		CodeGenerator.addInstruction(id + " = call " 
 				+ returnType + " "
 				+ table.lookup(leaf.getText()).getLocationReg() 
 				+ "(" 
 				+ Expression.getParamsToFunction(leaf, table)
 				+ ")" );
 		return id;
 	}
 	
 	private static OPERATOR getOperator(String op)
 	{
 		if (op.contentEquals("==")) return OPERATOR.EQ;
 		if (op.contentEquals("&&")) return OPERATOR.AND;
 		if (op.contentEquals("|")) return OPERATOR.BWOR;
 		if (op.contentEquals("^")) return OPERATOR.BWXOR;
 		if (op.contentEquals("!=")) return OPERATOR.NE;
 		if (op.contentEquals("<=")) return OPERATOR.LTE;
 		if (op.contentEquals("<")) return OPERATOR.LT;
 		if (op.contentEquals(">")) return OPERATOR.GT;
 		if (op.contentEquals(">=")) return OPERATOR.LTE;
 		if (op.contentEquals("+")) return OPERATOR.ADD;
 		if (op.contentEquals("-")) return OPERATOR.SUB;
 		if (op.contentEquals("*")) return OPERATOR.MUL;
 		if (op.contentEquals("/")) return OPERATOR.DIV;
 		if (op.contentEquals("%")) return OPERATOR.MOD;
 		if (op.contentEquals("~")) return OPERATOR.BWNOT;
 		if (op.contentEquals("!")) return OPERATOR.NOT;
 		if (op.contentEquals("||")) return OPERATOR.OR;
 		if (op.contentEquals("&")) return OPERATOR.BWAND;
 		return null;
 			
 	}
 	private enum OPERATOR
 	{
 		OR, AND, BWOR, BWXOR, BWAND, EQ, NE, LTE, LT, GT, GTE, ADD,
 		SUB, MUL, MOD, BWNOT, NOT, DIV
 	}
 	public static String getParamsToFunction(Tree leaf, SymbolTable table)
 	{
 		if (leaf.getChildCount()==0) return "";
 		String params = "" ;
 		FunctionSTValue fVal = (FunctionSTValue) table.lookup(leaf.getText());
 		ArrayList<DATA_TYPES> args = fVal.getArgs();
 		for (int i=1 ; i<leaf.getChildCount()-2; i++)
 		{
 			params += Utils.getReturnTypeOfFunction(args.get(i-1)) 
 					  + " " 
 					  + table.lookup(leaf.getChild(i).getText()).getLocationReg()
 					  + ", ";
 		}
 		
 		return params 
 			   + Utils.getReturnTypeOfFunction(args.get(leaf.getChildCount()-2)) 
 			   + " "
 			   + table.lookup(leaf.getChild(leaf.getChildCount()-2).getText())
 					.getLocationReg();
 	}
 }
