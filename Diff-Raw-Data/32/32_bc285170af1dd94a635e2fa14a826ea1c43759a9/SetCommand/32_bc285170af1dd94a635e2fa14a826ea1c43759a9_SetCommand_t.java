 package ui.command;
 
 import java.util.Scanner;
 import java.util.regex.MatchResult;
 import java.util.regex.Pattern;
 
 import spreadsheet.arithmetic.*;
 import spreadsheet.logical.*;
 import spreadsheet.textual.*;
 import spreadsheet.*;
 import ui.ErrorStream;
 import ui.SaveFile;
 
 public final class SetCommand extends Command {
 
 
 	private String expType;
 	private String arguments;
 	private String fatTest = "";
 	private String[] argSplit;
 	private Position spreadPos;
 	// Regex of constant Expressions.
 	private final static String constRegex ="((AConst)|(LConst)|(TConst))";
 	
 	/**
 	 * 
 	 * @param argInt1 column in Position to set.
 	 * @param argInt2 row in Position to set.
 	 * @param type String of Expression type
 	 * @param rest String of Arguments for type Expression.
 	 */
 	public SetCommand(final int argInt1, final int argInt2, 
 			final String type, final String rest) {
 		spreadPos = new Position(argInt1, argInt2);
 		this.expType = type;
 		this.arguments = rest;	
 	}
 	/**
 	 * Set Expression gotten from <expType> and <arguments>
 	 * uses method getExpression.
 	 * if Expression is legal sets in current workshop at <spreadPos>
 	 */
 	@Override
 	public void execute() {
 		Expression expression;
 			expression = getExpression(expType, arguments);
 		if (expression != null) {
 			arguments = arguments.replaceAll(fatTest, "");
 			Application.instance.getWorksheet().set(spreadPos, expression);
 			System.out.println(String.format("set new %s(%s) at Position(%d,%d)"
 					, expType, arguments, spreadPos.getColumn(), spreadPos.getRow(),expType));
 			SaveFile.saveFile.add(String.format("set %d %d %s %s"
 					,  spreadPos.getColumn(), spreadPos.getRow(),expType, arguments));
 		}
 		else {
 			ErrorStream.instance.show("Invalid Expression: " + 
 					String.format("new %s(%s) ", expType, arguments));
 		}
 	}
 	
 	/**
 	 * Evaluate polish notation to an Expression
 	 * @param expType String of Expression name
 	 * @param arg String of arguments in polish notation
 	 * @return the Expression
 	 */
 	private Expression getExpression(String expType, String arg) {
 		Pattern constPattern = Pattern.compile(constRegex);
 
 		Scanner scan = new Scanner(arg);
 		
 		try {
 		switch (expType) { 
 		case "AConst" : if (scan.hasNextInt()) {
 							int tal = scan.nextInt();
 							if (scan.hasNextLine())
 								fatTest = scan.nextLine();
 							return new AConst(tal);
 						}
 						return null;
 		case "LConst" : if (scan.hasNextBoolean()) {
 							boolean bool = scan.nextBoolean();
 							if (scan.hasNextLine())
 								fatTest = scan.nextLine();
 							return new LConst(bool);
 						}
 						return null;
 		case "TConst" : if (scan.hasNext()) {
 							String str = scan.next();
 							if (scan.hasNextLine())
 								fatTest = scan.nextLine();
 							return new TConst(str);
 						}
 						return null;
 		case "CellReference" : 
 						if (scan.findInLine("(\\w+) (\\d+) (\\d+)") != null) {
 							MatchResult result = scan.match();
 							String sheetName = result.group(1);
 							int arguInt1 = Integer.parseInt(result.group(2));
 							int arguInt2 = Integer.parseInt(result.group(3));
 							Position pos = new Position(arguInt1, arguInt2);
 							Spreadsheet sheet = Application.instance.
 									getSpreadsheet(sheetName);
 							
 							if (sheet.get(pos) == null)
 								sheet.getCyclic().getCellList().add(pos);
 								sheet.getCyclic().getPosList().add(spreadPos);
 							return new CellReference(sheet, pos);
 							}
 						return null;
 			
 		case "Neg" : if (scan.findInLine(constPattern) != null ) {
 						    MatchResult result = scan.match();
 						    String type2 = result.group(1);
 						    if (scan.hasNextLine())
 						return new Neg(getExpression(type2, scan.nextLine()));
 					 	}
 					 
 						return null;
 		case "Not" : if (scan.findInLine(constPattern) != null ) {
 						MatchResult result = scan.match();
 						String type2 = result.group(1);
 					    if (scan.hasNextLine())
 					    	return new Not(getExpression(type2, scan.nextLine()));
 				 	 }
 					 return null;
 		case "Add" : if (scan.findInLine(constPattern) != null ) {
 					 argSplit = splitTwoArg(arg);
 					 return new Add(getExpression(argSplit[0],argSplit[1]),
 							 getExpression(argSplit[2],  argSplit[3]));
 					}
 					return null;
 		case "Conjunct" : if (scan.findInLine(constPattern) != null ) {
 							argSplit = splitTwoArg(arg);
 						 return new Conjunct(getExpression(argSplit[0],argSplit[1]),
 								  getExpression(argSplit[2],  argSplit[3]));
 						}
 						return null;
 		case "Disjunct" : if (scan.findInLine(constPattern) != null ) {
 						argSplit = splitTwoArg(arg);
 						  return new Disjunct(getExpression(argSplit[0],argSplit[1]),
 								  getExpression(argSplit[2],  argSplit[3]));
 						}
 						return null;
 						  
 		case "Concat" : if (scan.findInLine(constPattern) != null ) {
 						argSplit = splitTwoArg(arg);
 						return new Concat(getExpression(argSplit[0],argSplit[1]),
 							getExpression(argSplit[2],  argSplit[3]));
 						}
  						return null;
 						
 		}
 		} catch (Exception e ) {
 			ErrorStream.instance.show("Invalid input: " + e.toString());
 		}
 		scan.close();
 		return null;
 		
 	}
 	
 
 	/**
 	 * Method for Concat, Add, Disjunct, Conjunct. = double expressions
 	 * Splits a String of polish notation, 
 	 * in to a list of first argument and second.
 	 * This is needed because Concat and Add takes two arguments.
 	 * @param str an console input for double expression
 	 * @return should return String[] of length() == 4 
 	 *         index 0 = expression type of argument 1 of double expression
 	 * 		   index 1 = index 0's arguments
 	 * 		   index 2 = expression type of argument 2 of double expression
 	 * 		   index 3 = index 2's arguments
 	 */
 	private static String[] splitTwoArg(String str) {
 		String[] list = str.split(" ");
 		int countConst = 0;
 		int countDuo = 1;
 		final String constRegex ="((AConst)|(LConst)|(TConst))";
 		final String duoRegex ="(Concat)|(Add)|(Disjunct)|(Conjunct)";
 		String firstArg = "";
 		String firstExp = "";
 
 		int i;
 		for (i = 0; countDuo > countConst && i < list.length; i++) {
 			if (list[i].matches(constRegex)) {
 				countConst++;
 			}
 			if (list[i].matches(duoRegex)) {
 				countDuo++;
 			}
 			if (i == 0) 
 			firstExp = list[i];	
 			else
 			firstArg += list[i] + " ";	
 
 		}
 		firstArg = firstArg + list[i];
 		String secondArg = "";
 		for (i = i+1; i < list.length; i++) {
 			secondArg += " " + list[i];
 		}
 		secondArg = secondArg.replaceFirst(" ", "");
 
 		String secondExp = secondArg.substring(0, secondArg.indexOf(" "));
 		secondArg = secondArg.substring(secondArg.indexOf(" "), secondArg.length());
 		secondArg = secondArg.replaceFirst(" ", "");
 
 		String[] split = {firstExp, firstArg ,secondExp ,secondArg};
 		return split;
 	}
 
 }
