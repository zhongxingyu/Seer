 package ui.command;
 
 
 import java.io.IOException;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import spreadsheet.Application;
 import spreadsheet.NoSuchSpreadsheetException;
 import spreadsheet.Position;
 import spreadsheet.arithmetic.*;
 import spreadsheet.logical.*;
 import spreadsheet.textual.*;
 import spreadsheet.*;
 import ui.ErrorStream;
 
 public final class SetCommand extends Command {
 
 	private int argInt1;
 	private int argInt2;
 	private String expType;
 	private String arguments;
 	private int writeOutArg;
 	/**
 	 * @param argInt12
 	 * @param argInt22
 	 * @param type
 	 * @param rest
 	 */
 	public SetCommand(final int argInt1, final int argInt2, 
 			final String type, final String rest) {
 		this.argInt1 = argInt1;
 		this.argInt2 = argInt2;
 		this.expType = type;
 		this.arguments = rest;
 		
 	}
 	@Override
 	public void execute() throws NoSuchSpreadsheetException {
 		Position position = new Position(argInt1, argInt2);
 		Expression expression;
 			expression = getType(expType, arguments);
 		if (expression != null) {
 		Application.instance.getWorksheet().set(position, expression);
 		System.out.println(String.format("set new %s(%s)" +
 				"at Position(%d,%d)", expType, writeOutArg, argInt1, argInt2));
 		}
		else 
			ErrorStream.instance.show("Invalid Expression:");

 
 	}
 	
 	private Expression getType(String str, String arg) {;
 
 		Scanner scan = new Scanner(arg);
 		try {
 		switch (str) {
 		case "AConst" : if (scan.hasNextInt()) {
 							writeOutArg = scan.nextInt();
 							return new AConst(writeOutArg);
 						}
 		case "LConst" : if (scan.hasNextBoolean())
 							return new LConst(scan.nextBoolean());
 		case "TConst" : if (scan.hasNext())
 							return new TConst(scan.next());
 
 		/*
 		case "Add" :  return new Add(getType("Add", arguments),
 				getType("Add", arguments));
 		case "Neg" : return new Neg(getType("Neg", arguments));
 
 */
 		}
 		} catch (NoSuchElementException e) {
 			ErrorStream.instance.show("Invalid input: " + e.toString());
 			
 		}
 		return null;
 		
 	}
 
 }
