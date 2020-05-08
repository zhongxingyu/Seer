 package forrest.main;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class ForrestWriter {
 	
 	/** Output method */
 	private FileWriter fileWriter;
 	
 	/** List of instructions kept while running */
 	private ArrayList<String> instructions = new ArrayList<String>();
 	
 	/** Store for storing the variables and address locations */
 	private IdStore store = new IdStore();
 	
 	/** Amount of if-statements encountered */
 	private int nIf = 0;
 	/** Amount of while-statements encountered */
 	private int nWhile = 0;
 	
 	/** Constructor. Sets the output file */
 	public ForrestWriter(){
 		this.setFile(ForrestOptions.tasmFileLocation);
 	}
 	
 	/**
 	 * Change the file location
 	 * @param fileLocation
 	 */
 	public void setFile(String fileLocation){
 		try {
 			fileWriter = new FileWriter(new File(fileLocation));
 		} catch (IOException e) {
 			System.err.println("Error in creating file: " + fileLocation);
 		}
 	}
 	
 	/**
 	 * Print the list of instructions to the file.
 	 */
 	public void print() {
 		try {
 			for(String s: instructions){
 				fileWriter.write(s + "\n");
 			}
 			fileWriter.flush();
 			fileWriter.close();
 		} catch (IOException e) {
 			System.err.println("Error while writing to file: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * Place an identifier in the store
 	 * @param id the id
 	 * @param type the type of the variable/constant
 	 */
 	public void putStore(String id, Type type){
 		store.declare(id, type);
 	}
 	
 	/** @return a previously unused number for the if labels */
 	public int getFreeIf() {
 		nIf++;
 		return nIf;
 	}
 	
 	/** @return a previously unused number for the while labels */
 	public int getFreeWhile() {
 		nWhile++;
 		return nWhile;
 	}
 	
 	/** Setup extra variables for program running. Reserves 1 place on the stack */
 	public void startProgram(){
 		instructions.add("PUSH 1");
     	store.openScope();
     	store.declare("7ERROR", Type.INT);
 	}
 
 	/**
 	 * Write the code after a program-rule
 	 */
 	public void writeProgram() {
 		// Clean up the stack
 		instructions.add("POP(0) " + store.getCurrentScopeSize());
 		instructions.add("HALT");
 		// Divide by zero error
 		instructions.add("ERRORZERODIV: LOADL 1");
 		instructions.add("STORE(1) " + store.getAddress("7ERROR") + "[SB]"); 
 		instructions.add("JUMP ERROR[CB]");
 		// Incorrect user input
 		instructions.add("ERRORINPUT: LOADL 2");
 		instructions.add("STORE(1) " + store.getAddress("7ERROR") + "[SB]"); 
 		instructions.add("JUMP ERROR[CB]");
 		// Error label: output the error code
 		instructions.add("ERROR: LOAD(1) " + store.getAddress("7ERROR") + "[SB]");
 		this.printString("Error: ");
 		instructions.add("CALL putint");
 		instructions.add("HALT");
 	}
 	
 	/** Open a new scope */
 	public void openScope() {
 		store.openScope();
 	}
 	
 	/** 
 	 * Close the current scope and remove all identifiers associated with that 
 	 * scope from the stack.
 	 */
 	public void closeScope() {
 		instructions.add("POP(1) " + ((store.getCurrentScopeSize() - 1) > 0 ? (store.getCurrentScopeSize() - 1) : 0));
 		store.closeScope();
 	}
 	
 	/** Write the end of a program line: leave the last value on the stack */
 	public void writeProgramLines() {
 		instructions.remove(instructions.size()-1);
 	}
 	
 	/** Upon a semi-colon, remove the last value from the stack */
 	public void writeLine() {
 		instructions.add("POP(0) 1"); 
 	}
 	
 	/** Declare a variable */
 	public void writeDeclarationVar(ForrestTree t) {
 		instructions.add("PUSH 1");
 		String id = t.getChild(0).getText();
 		Type type = Type.valueOf(t.getChild(1).getText().toUpperCase());
 		putStore(id, type);
 	}
 	
 	/** Declare a constant */
 	public void writeDeclarationConstant(ForrestTree t) {
 		instructions.add("PUSH 1");
 		String id = t.getChild(0).getText();
 		Type type = ((ForrestTree)t.getChild(1)).getReturnType();
 		putStore(id, type);
 	}
 	
 	/** Assignment */
 	public void writeBecomes(ForrestTree t){
 		instructions.add("STORE(1) " + store.getAddress(t.getChild(0).getText()) + "[SB]");
 		instructions.add("LOAD(1) " + store.getAddress(t.getChild(0).getText()) + "[SB]");
 	}
 	
 	/** Opening to a while-loop */
 	public void writeWhileStart(int label) {
 		this.openScope();
 		instructions.add("LWHILESTART" + label + ":");
 	}
 	
 	/** Possible break from the while-loop */
 	public void writeWhileCheck(int label) {
 		instructions.add("JUMPIF(0) LWHILEFINALLY" + label + "[CB]");
 		this.openScope();
 	}
 	
 	/** End of while-loop */
 	public void writeWhileFinally(int label) {
 		instructions.add("JUMP LWHILESTART" + label + "[CB]");
 		this.closeScope();
 		this.closeScope();
 		instructions.add("LWHILEFINALLY" + label + ":");
 	}
 	
 	/** Opening of an if statement */
 	public void writeIfStart() {
 		this.openScope();
 	}
 	
 	/** Check of if statement */
 	public void writeIfCheck(int label){
 		instructions.add("JUMPIF(0) LIFFALSE" + label + "[CB]");
 		this.openScope();
 	}
 	
 	/** Code after completing then part of the then statement */
 	public void writeIfAfterThen() {
 		this.closeScope();
 		this.openScope();
 	}
 	
 	/** Else part of if statement */
 	public void writeIfBeforeElse(int label){
 		instructions.add("JUMP LIFFINALLY" + label + "[CB]");
 		instructions.add("LIFFALSE" + label + ":");
 	}
 	
 	/** Extra code after the complete if statement */
 	public void writeIfFinally(int label){
 		this.closeScope();
 		instructions.add("LIFFINALLY" + label + ":");
 	}
 	
 	/** Code to jump to if the if-statement returns false and no else exists */
 	public void writeIfFalse(int label){
 		this.closeScope();
 		instructions.add("LIFFALSE" + label + ":");
 	}
 	
 	/** Code to execute a binary operation */
 	public void writeBinaryOp(ForrestTree t) {
 		String text = t.getText();
 		String call = "";
 		switch(text) {
 		case "||":
 			call = "or";
 			break;
 		case "&&":
 			call = "and";
 			break;
 		case ">":
 			call = "gt";
 			break;
 		case "<":
 			call = "lt";
 			break;
 		case ">=":
 			call = "ge";
 			break;
 		case "<=":
 			call = "le";
 			break;
 		case "==":
 			call = "eq";
 			break;
 		case "!=":
 			call = "ne";
 			break;
 		case "+":
 			call = "add";
 			break;
 		case "-":
 			call = "sub";
 			break;
 		case "*":
 			call = "mult";
 			break;
 		case "/":
 			call = "div";
 			break;
 		case "%":
 			call = "mod";
 			break;
 		}
 		
 		// Check divide by zero
 		if (call.equals("div") || call.equals("mod")) {
 			instructions.add("JUMPIF(0) ERRORZERODIV [CB]");
 		}
 		
 		if (call.equals("ne") || call.equals("eq")) {
 			instructions.add("LOADL 1");
 		}
 		
 		instructions.add("CALL " + call);
 	}
 	
 	/** Code to execute a unary operation */
 	public void writeUnaryOp(ForrestTree t) {
 		String text = t.getText();
 		String call = "";
 		switch(text) {
 		case "!":
 			call = "not";
 			break;
 		case "+":
 			call = "id";
 			break;
 		case "-":
 			call = "neg";
 			break;
 		}
 		
 		instructions.add("CALL " + call);
 	}
 	
 	/** Load a variable/constant */
 	public void writeIdentifier(ForrestTree t){
 		instructions.add("LOAD(1) " + store.getAddress(t.getText()) + "[SB]");
 	}
 	
 	/** Load a number */
 	public void writeNumber(ForrestTree t){
 		instructions.add("LOADL " + t.getText());
 	}
 	
 	/** Load a boolean */
 	public void writeBoolean(ForrestTree t){
 		String value = "1";
 		if (t.getText().equals("false")) {
 			value = "0";
 		}
 		
 		instructions.add("LOADL " + value);
 	}
 	
 	/** Load a character */
 	public void writeCharacter(ForrestTree t){
 		instructions.add("LOADL " + Character.getNumericValue(t.getText().charAt(0)));
 	}
 	
 	/** Ask the user for input for a series of variables */
 	public void writeRead(ForrestTree t) {
 		for (Object child: t.getChildren()) {
 			ForrestTree tree = (ForrestTree) child;
 			StoreItem item = store.getItem(tree.getText());
 			Type type = item.getType();
 			int address = store.getAddress(tree.getText());
 			instructions.add("LOADA " + address + "[SB]");
 			switch(type) {
 			case INT:
 				printString("Input int: ");
 				instructions.add("CALL getint");
 				break;
 			case CHAR:
 				printString("Input char: ");
 				instructions.add("CALL get");
 				break;
 			case BOOL:
 				printString("Input bool (0/1): ");
 				instructions.add("CALL getint");
 				break;
 			default:
 				break;
 			}
 		}
 		instructions.add("LOAD(1) " + store.getAddress(t.getChild(0).getText()) + "[SB]");
 	}
 	
 	/** Print a series of expressions */
 	public void writePrint(ForrestTree t) {
 		for (int i = 0; i < t.getChildCount(); i ++) {
 			ForrestTree tree = (ForrestTree) t.getChild(i);
 			Type type = tree.getReturnType();
 			instructions.add("LOAD(1) " + (i - t.getChildCount()) + "[ST]");
 			switch(type) {
 			case INT:
 				instructions.add("CALL putint");
 				break;
 			case CHAR:
 				instructions.add("CALL put");
 				break;
 			case BOOL:
 				instructions.add("CALL putint");
 				break;
 			default:
 				break;
 			}
 		}
 		instructions.add("POP(0) " + (t.getChildCount() - 1));
 		instructions.add("CALL puteol");
 	}
 	
 	/** Print a string */
 	public void printString(String output) {
 		for (Character c: output.toCharArray()) {
 			if (c == '\n') {
 				instructions.add("CALL puteol");
 			} else {
 				instructions.add("LOADL " + (int) c);
 				instructions.add("CALL put");
 			}
 		}
 	}
 	
 	/** Checks whether a boolean input is correct */
 	public void printBooleanCheck() {
 		instructions.add("LOAD(1) -1[ST]");
 		instructions.add("LOADL 0");
 		instructions.add("LOADL 1");
 		instructions.add("CALL eq");
 		instructions.add("LOAD(1) -2[ST]");
 		instructions.add("LOADL 1");
 		instructions.add("LOADL 1");
 		instructions.add("CALL eq");
 		instructions.add("CALL or");
 		instructions.add("JUMPIF(0) ERRORINPUT");
 	}
 }
