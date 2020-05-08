 package forrest.main;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ForrestWriter {
 	
 	/** Output method */
 	private FileWriter fileWriter;
 	
 	private ArrayList<String> instructions = new ArrayList<String>();
 	
 	//Store for storing the variables and address locations
 	private IdStore store = new IdStore();
 	
 	private int nIf = 0;
 	
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
 	
 	public void print() {
 		try {
 			for(String s: instructions){
				fileWriter.append(s + "\n");
 			}
 			fileWriter.flush();
 			fileWriter.close();
 		} catch (IOException e) {
 			System.err.println("Error while writing to file: " + e.getMessage());
 		}
 	}
 	
 	public void putStore(String id, Type type){
 		store.declare(id, type);
 	}
 	
 	public int getFreeIf() {
 		nIf++;
 		return nIf;
 	}
 	
 	//Used to reserve room for the error code on the stack
 	public void startProgram(){
 		instructions.add("PUSH 1");
     	store.openScope();
     	store.declare("7ERROR", Type.INT);
 	}
 
 	/**
 	 * Write the code after a program-rule
 	 * @throws IOException
 	 */
 	public void writeProgram() {
 		instructions.add("POP(0) " + store.getCurrentScopeSize());
 		instructions.add("HALT");
 		instructions.add("ERROR: LOAD(1) " + store.getAddress("7ERROR") + "[SB]");
 		instructions.add("CALL putint");
 		instructions.add("HALT");
 		
 		print();
 	}
 	
 	public void openScope() {
 		store.openScope();
 	}
 	
 	public void closeScope() {
 		instructions.remove(instructions.size()-1);
 		instructions.add("POP(1) " + ((store.getCurrentScopeSize() - 1) > 0 ? (store.getCurrentScopeSize() - 1) : 0));
 		store.closeScope();
 	}
 	
 	public void writeLine() {
 		instructions.add("POP(0) 1"); 
 	}
 	
 	public void writeDeclarationVar(ForrestTree t) {
 		instructions.add("PUSH 1");
 		String id = t.getChild(0).getText();
 		Type type = Type.valueOf(t.getChild(1).getText().toUpperCase());
 		putStore(id, type);
 	}
 	
 	public void writeDeclarationConstant(ForrestTree t) {
 		instructions.add("PUSH 1");
 		String id = t.getChild(0).getText();
 		Type type = ((ForrestTree)t.getChild(1)).getReturnType();
 		putStore(id, type);
 	}
 	
 	public void writeBecomes(ForrestTree t){
 		instructions.add("STORE(1) " + store.getAddress(t.getChild(0).getText()) + "[SB]");
 		instructions.add("LOAD(1) " + store.getAddress(t.getChild(0).getText()) + "[SB]");
 	}
 	
 	public void writeIf(int label){
 		instructions.add("JUMPIF(0) LIFFALSE" + label + "[CB]");
 	}
 	
 	public void writeElse(int label){
 		instructions.add("JUMP LIFFINALLY" + label + "[CB]");
 		instructions.add("LIFFALSE" + label + ":");
 	}
 	
 	public void writeIfFinally(int label){
 		instructions.add("LIFFINALLY" + label + ":");
 	}
 	
 	public void writeIfFalse(int label){
 		instructions.add("LIFFALSE" + label + ":");
 	}
 	
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
 		
 		if (call.equals("ne") || call.equals("eq")) {
 			instructions.add("LOADL 1");
 		}
 		
 		instructions.add("CALL " + call);
 	}
 	
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
 	
 	public void writeIdentifier(ForrestTree t){
 		instructions.add("LOAD(1) " + store.getAddress(t.getText()) + "[SB]");
 	}
 	
 	public void writeNumber(ForrestTree t){
 		instructions.add("LOADL " + t.getText());
 	}
 	
 	public void writeBoolean(ForrestTree t){
 		String value = "1";
 		if (t.getText().equals("false")) {
 			value = "0";
 		}
 		
 		instructions.add("LOADL " + value);
 	}
 	
 	public void writeCharacter(ForrestTree t){
 		instructions.add("LOADL " + Character.getNumericValue(t.getText().charAt(0)));
 	}
 	
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
 			}
 		}
 		instructions.add("LOAD(1) " + store.getAddress(t.getChild(0).getText()) + "[SB]");
 	}
 	
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
 			}
 		}
 		instructions.add("POP(0) " + (t.getChildCount() - 1));
 	}
 	
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
 }
