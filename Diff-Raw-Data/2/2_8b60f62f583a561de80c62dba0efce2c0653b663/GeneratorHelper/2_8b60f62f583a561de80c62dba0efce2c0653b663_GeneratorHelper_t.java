 package vb.eindopdracht.helpers;
 
 import java.util.Arrays;
 
 import vb.eindopdracht.symboltable.*;
 
 public class GeneratorHelper extends CrimsonCodeHelper {
 	// Keep track of the Stack size
 	private int size;
 	// Label for the next output
 	private String nextLabel;
 	// Identifier for labels (in case of nested (if) statements)
 	private int labelNumber;
 	// If in constant scope, operands should not output
 	private boolean constantScope;
 	// If in a routine, routinecalls should use static link LB
 	private int routineLevel;
 
 	/**
 	 * Stel constantScope in, dit heeft gevolgen voor het printen van TAM
 	 * statements mbt constante waardes
 	 * 
 	 * @param b
 	 *            - De constantScope waarde
 	 */
 	public void setConstantScope(boolean b) {
 		constantScope = b;
 	}
 
 	/**
 	 * Check of de Generator zich in een constantScope bevindt
 	 */
 	public boolean isConstantScope() {
 		return constantScope;
 	}
 	
 	/**
 	 * Opent een scope in de symbolTable
 	 */
 	public void openScope() {
 		symbolTable.openScope();
 	}
 	
 	/**
 	 * Sluit een scope en popt de achtergelaten variabelen
 	 * Laat een resultaat van grootte result intact.
 	 * @param result
 	 */
 	public void closeScope(int result) throws Exception {
 		int scopeSize = symbolTable.getCurrentLocalBaseSize();
 		if(!symbolTable.isFunctionalScope(symbolTable.currentLevel())) {
 			printTAM("POP(" + result + ")", scopeSize + "", "Pop " + scopeSize + " local variables");
 			this.size -= scopeSize;
 		}
 		symbolTable.closeScope();
 	}
 	
 	/**
 	 * printTAM print een mooi opgemaakte TAM instructie
 	 * 
 	 * @param lbl
 	 *            - Label voor de instructie
 	 * @param cmd
 	 *            - De instructie zelf
 	 * @param arg
 	 *            - Het argument van de instructie
 	 * @param cmt
 	 *            - Eventueel commentaar op bij de instructie
 	 */
 	private void printTAM(String lbl, String cmd, String arg, String cmt) {
 		if (!lbl.equals("") && lbl != null)
 			lbl = lbl + ":";
 		System.out.format("%-9s %-9s %9s ; %s%n", lbl, cmd, arg, cmt);
 	}
 
 	/**
 	 * printTAM print een mooi opgemaakte TAM instructie de aanroep zonder label
 	 * veld gebruikt het nextLabel veld voor de aanroep.
 	 * 
 	 * @param cmd
 	 *            - De instructie
 	 * @param arg
 	 *            - Het argument van de instructie
 	 * @param cmt
 	 *            - Eventueel commentaar bij de instructie
 	 */
 	private void printTAM(String cmd, String arg, String cmt) {
 		printTAM(nextLabel, cmd, arg, cmt);
 		nextLabel = "";
 	}
 
 	/**
 	 * Clears the stack and sends a Halt signal to the virtual machine.
 	 */
 	public void endProgram() {
 		printTAM("POP(0)", String.valueOf(size), "Pop " + size + " variables");
 		printTAM("HALT", "", "End of program");
 	}
 	
 	/**
 	 * Debug method to compare expected stack size to actual stack size
 	 */
 	public void printStackSize() {
 		printPrimitiveRoutine("puteol", "");
 		printTAM("LOADL", size + "", "");
 		printPrimitiveRoutine("putint", "");
 		printTAM("LOADL", encode("|"), "");
 		printPrimitiveRoutine("put", "");
 		printTAM("LOADA", 0 + "[ST]", "");
 		printPrimitiveRoutine("putint", "");
 		printPrimitiveRoutine("puteol", "");
 	}
 
 	/**
 	 * Definieer een constante waarde (variabele die niet in het geheugen is
 	 * opgeslagen).
 	 * 
 	 * @param id
 	 * @param value
 	 * @throws Exception
 	 */
 	public void defineConstant(String id, String value) throws Exception {
 		IdEntry entry = processEntry(id);
 		entry.setType(IdEntry.Type.CONST);
 		entry.setConstant(true);
 		entry.setValue(value);
 	}
 
 	/**
 	 * Definieer een variabele met de naam id
 	 *  
 	 * @param id
 	 * @throws Exception
 	 */
 	public void defineVariable(String id) throws Exception {
 		IdEntry entry = processEntry(id);
 		String register;
 		if(routineLevel > 0)
 			register = "[LB]";
 		else
 			register = "[SB]";
 		entry.setAddress(size + register);
 		entry.setType(IdEntry.Type.VAR);
 		if(id.endsWith("Array")) {
 			size = ((ArrayEntry) entry).getArraySize();
 		} else {
 			size = 1;
 		}
 		printTAM("PUSH", String.valueOf(size), "Push variable " + id);
 		size++;
 	}
 	
 	/**
 	 * Definieer een parameter met de naam id op locatie offset[LB]
 	 * 
 	 * @param id
 	 * @throws Exception
 	 */
 	public void defineParameters(String id, int offset) throws Exception {
 		boolean varparam = false;
 		String[] splitted = CrimsonCodeHelper.splitString(id);
 		if("Var".equals(splitted[splitted.length-1])) {
 			varparam = true;
 		}
 		IdEntry entry = processEntry(id.substring(0, (varparam?id.length()-3:id.length())));
 		entry.setAddress(offset + "[LB]");
 		entry.setType(IdEntry.Type.VAR);
 		entry.setVarparam(varparam);
 	}
 
 	/**
 	 * Print een routineaanroep (een aanroep naar de VM met CALL)
 	 * 
 	 * @param cmd
 	 * @param comment
 	 * @param statlink
 	 */
 	public void printRoutine(String cmd, String comment, String statlink) {
 		printTAM("CALL(" + statlink + ")", cmd, comment);
 	}
 	
 	public void printRoutine(String cmd, String comment) {
 		String statlink = "SB";
 		if(routineLevel > 0)
 			statlink = "LB";
 		printRoutine(cmd, comment, statlink);
 	}
 
 	/**
 	 * Print een primitive routine (een aanroep naar de VM met CALL)
 	 * met SB als static link
 	 * 
 	 * @param cmd
 	 * @param comment
 	 */
 	public void printPrimitiveRoutine(String cmd, String comment) {
 		printRoutine(cmd, comment, "SB");
 	}
 
 	/**
 	 * Laadt een literal waarde op de stack
 	 * 
 	 * @param literal
 	 */
 	public void loadLiteral(String literal) {
 		literal = encode(literal);
 		printTAM("LOADL", literal, "Load literal value '" + literal + "'");
 	}
 
 	/**
 	 * Sla een waarde op in een variabele 'id'
 	 * 
 	 * @param id
 	 * @param value
 	 * @param size
 	 */
 	public void storeValue(String id, String value) {
 		int size = 1;
 		IdEntry entry = symbolTable.retrieve(id);
 		boolean isArray = false;
 		if(entry instanceof ArrayEntry) {
 			size = ((ArrayEntry) entry).getArraySize();
 			isArray = true;
 		}
 		if(entry.isVarparam()) {
 			printTAM("LOAD(" + size + ")", entry.getAddress(), "Load the variable parameter address " + id);
 			printTAM("STOREI(" + size + ")", "", "Store in the variable parameter " + id);
 			printTAM("LOAD(" + size + ")", entry.getAddress(), "Load the variable parameter address " + id);
 			printTAM("LOADI(1)", "", "Load stored variable " + id + " on the stack.");
 			
 		}
 		else {
 			printTAM("STORE(" + size + ")", symbolTable.retrieve(id).getAddress(),
 					"Store in variable " + id);
 			if(!isArray) printTAM("LOAD(" + size + ")", symbolTable.retrieve(id).getAddress(),
 					"Load stored variable " + id + " on the stack.");
			if(!isArray) symbolTable.retrieve(id).setValue(value);
 		}
 	}
 
 	/**
 	 * Geef de waarde van een variabele terug
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public String getValue(String id) {
 		return getValue(id, "0");
 	}
 	
 	/**
 	 * Geeft de waarde van een variabele (met offset) terug
 	 * @param id
 	 * @param offset
 	 * @return
 	 */
 	public String getValue(String id, String offset) {
 		IdEntry entry = symbolTable.retrieve(id);
 		String val = entry.toString();
 		if(entry.isFunctional()) {
 //			printTAM("JUMP", entry.getAddress(), "Jump to the process " + id);
 			printStatementCall(id);
 			if(entry instanceof FuncEntry)
 				val = "1";	// Dummy value
 		}
 		else if(entry.isVarparam()) {
 			printTAM("LOAD(1)", entry.getAddress(), "Load the variable parameter address " + id);
 			printTAM("LOADI(1)", "", "Load the variable parameter");
 		}
 		else if(entry instanceof ArrayEntry) {
 			ArrayEntry arr = (ArrayEntry) entry;
 			if(arr.isConstant()) {
 				loadLiteral(arr.get(offset));
 			}
 			else {
 				printTAM("LOAD(1)", arr.getOffsetAddress(offset), "Load the array value");
 			}
 			val = "1"; // Dummy;
 		}
 		else if(entry.getAddress() == null) {
 			printTAM("LOADL", encode(val), "Load the constant " + id);
 		}
 		else
 			printTAM("LOAD(1)", entry.getAddress(),	"Load the variable " + id);
 		return val;
 	}
 	
 	/**
 	 * Geef het adres van een variabele terug
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public String getAddress(String id) {
 		IdEntry entry = symbolTable.retrieve(id);
 		printTAM("LOADA", entry.getAddress(),	"Load variable address for " + id);
 		return entry.getAddress();
 	}
 
 	/**
 	 * Encode een literal value (int of character) naar een integer value
 	 * 
 	 * @param str
 	 * @return
 	 */
 	public String encode(String str) {
 		String result = null;
 		try {
 			Integer.parseInt(str);
 			result = str;
 		} catch (NumberFormatException e) {
 			String[] encodings = { "", "", "", "", "", "", "", "", "", "", // 00-09
 					"", "", "", "", "", "", "", "", "", "", // 10-19
 					"", "", "", "", "", "", "", "", "", "", // 20-29
 					"", "", " ", "!", "\"", "#", "$", "%", "&", "'", // 30-39
 					"(", ")", "*", "+", ",", "-", ".", "/", "0", "1", // 40-49
 					"2", "3", "4", "5", "6", "7", "8", "9", ":", ";", // 50-59
 					"<", "=", ">", "?", "@", "A", "B", "C", "D", "E", // 60-69
 					"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", // 70-79
 					"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", // 80-89
 					"Z", "[", "\\", "]", "^", "_", "`", "a", "b", "c", // 90-99
 					"d", "e", "f", "g", "h", "i", "j", "k", "l", "m", // 100-109
 					"n", "o", "p", "q", "r", "s", "t", "u", "v", "w", // 110-119
 					"x", "y", "z", "{", "|", "}", "", "", "", "" // 120-129
 			};
 			result = String.valueOf(Arrays.asList(encodings).indexOf(str));
 		}
 		return result;
 	}
 
 	// //////////////////////////////////////////////////////////
 	// / Procedure
 	// //////////////////////////////////////////////////////////
 
 	/**
 	 * Definieer een procedure
 	 * 
 	 * @param id
 	 * @throws Exception
 	 */
 	public int defineProcedure_Start(String id) throws Exception {
 		ProcEntry proc = new ProcEntry(id);
 		int thisLabelNo = labelNumber++;
 		printTAM("JUMP", "End" + thisLabelNo + "[CB]", "Skip procedure " + id + " body.");
 		nextLabel = "Proc" + thisLabelNo;
 		proc.setAddress(nextLabel + "[CB]");
 		symbolTable.enter(id, proc);
 		symbolTable.openScope(true);
 		symbolTable.goDeeper();
 		routineLevel++;
 		return thisLabelNo;
 	}
 
 	/**
 	 * Einde van defineProcedure
 	 */
 	public void defineProcedure_End(int thisLabelNo, int parameters) throws Exception {
 		closeScope(0);
 		printTAM("RETURN(0)", "" + parameters, "Return from the procedure");
 		symbolTable.goShallower();
 		nextLabel = "End" + thisLabelNo;
 		routineLevel--;
 	}
 
 	// //////////////////////////////////////////////////////////
 	// / Functie
 	// //////////////////////////////////////////////////////////
 
 	/**
 	 * Definieer een functie
 	 * 
 	 * @param id
 	 * @throws Exception
 	 */
 	public int defineFunction_Start(String id) throws Exception {
 		FuncEntry func = new FuncEntry(id);
 		int thisLabelNo = labelNumber++;
 		printTAM("JUMP", "End" + thisLabelNo + "[CB]", "Skip function " + id + " body.");
 		nextLabel = "Func" + thisLabelNo;
 		func.setAddress(nextLabel + "[CB]");
 		symbolTable.enter(id, func);
 		symbolTable.openScope(true);
 		symbolTable.goDeeper();
 		routineLevel++;
 		return thisLabelNo;
 	}
 
 	/**
 	 * Einde van defineFunction
 	 */
 	public void defineFunction_End(int thisLabelNo, int parameters) throws Exception {
 		closeScope(1);
 		printTAM("RETURN(1)", "" + parameters, "Return from the function");
 		symbolTable.goShallower();
 		nextLabel = "End" + thisLabelNo;
 		routineLevel--;
 	}
 	
 	/**
 	 * Call naar functie
 	 * @param id
 	 */
 	public String printStatementCall(String id) {
 		IdEntry ie = symbolTable.retrieve(id);
 		printPrimitiveRoutine(ie.getAddress(), "Call function " + id);
 		if(ie.getValue() == null)
 			return null;
 		else
 			return ie.getValue().toString();
 	}
 
 
 	// //////////////////////////////////////////////////////////
 	// / IF Statement
 	// //////////////////////////////////////////////////////////
 
 	/**
 	 * Start voor een if statement (test de stack en jumpt naar label)
 	 */
 	public int printStatementIf_Start() {
 		int thisLabelNo = labelNumber++;
 		printTAM("JUMPIF(0)", "Else" + thisLabelNo + "[CB]", "Jump to ELSE");
 		return thisLabelNo;
 	}
 
 	/**
 	 * Print de instructies voor de else clause
 	 * 
 	 * @param thisLabelNo
 	 */
 	public void printStatementIf_Else(int thisLabelNo) {
 		printTAM("JUMP", "End" + thisLabelNo + "[CB]", "Jump over ELSE");
 		nextLabel = "Else" + thisLabelNo;
 	}
 
 	/**
 	 * Print de instructies voor het einde van het if statement
 	 * 
 	 * @param thisLabelNo
 	 */
 	public void printStatementIf_End(int thisLabelNo) {
 		if (!nextLabel.equals(""))
 			printTAM("JUMP", "End" + thisLabelNo + "[CB]",
 					"Jump to End, no Else clause");
 		nextLabel = "End" + thisLabelNo;
 	}
 
 	// //////////////////////////////////////////////////////////
 	// / WHILE Statement
 	// //////////////////////////////////////////////////////////
 	/**
 	 * Print het begin van een while statement
 	 * 
 	 * @return WhileInfo object met informatie voor de End mehode
 	 */
 	public WhileInfo printStatementWhile_Start() {
 		int thisLabelNo = labelNumber++;
 		// Wanneer while label al ingevuld is, geef deze door
 		if (nextLabel.equals(""))
 			nextLabel = "While" + thisLabelNo;
 		return new WhileInfo(thisLabelNo, nextLabel);
 	}
 
 	/**
 	 * Print het begin van het DO statement.
 	 * 
 	 * @param info
 	 */
 	public void printStatementWhile_Do(WhileInfo info) {
 		printTAM("JUMPIF(0)", "End" + info.thisLabelNo + "[CB]",
 				"Jump past body");
 	}
 
 	/**
 	 * Print het einde van een while statement
 	 * 
 	 * @param info
 	 *            - De teruggegeven info van de start
 	 */
 	public void printStatementWhile_End(WhileInfo info) {
 		printTAM("JUMP", info.nextLabel + "[CB]", "Jump to WHILE-expression");
 		nextLabel = "End" + info.thisLabelNo;
 	}
 	
 	public void printStatementWhile_Cleanup() {
 		printTAM("POP(0)", symbolTable.getCurrentLocalBaseSize() + "", "Pop variables leftover from while conditional ");
 	}
 	
 	// //////////////////////////////////////////////////////////
 	// / ARRAY Statements
 	// //////////////////////////////////////////////////////////
 	
 	public void defineArray_Type(String identifier, String start, String end) throws Exception {
 		processDynamicType(identifier, start, end);
 	}
 	
 	// //////////////////////////////////////////////////////////
 	// / PRINT Statement
 	// //////////////////////////////////////////////////////////
 	/**
 	 * Print de put statements, bekijkt of het een Integer betreft (voor
 	 * putint).
 	 * 
 	 * @param ex
 	 */
 	public void printStatementPrint(String ex) {
 		if(ex != null)
 		try {
 			Integer.parseInt(ex);
 			printTAM("LOADA", -1+"[ST]", "Load address of int on top of stack");
 			printTAM("LOADI(1)", "", "Duplicate int on top of stack");
 			printPrimitiveRoutine("putint", "Print the int value on top of the stack");
 		} catch (NumberFormatException e) {
 			if (ex.equals("\n")) {
 				printPrimitiveRoutine("puteol", "Print a newline to the stdout");
 			} else {
 				printTAM("LOADA", -1+"[ST]", "Load address of char on top of stack");
 				printTAM("LOADI(1)", "", "Duplicate char on top of stack");
 				printPrimitiveRoutine("put", "Print the char value on top of the stack");
 			}
 		}
 	}
 
 	// //////////////////////////////////////////////////////////
 	// / READ Statement
 	// //////////////////////////////////////////////////////////
 	/**
 	 * Print de get statements, bekijkt of het een Integer betreft (voor
 	 * getint).
 	 * 
 	 * @param ex
 	 */
 	public void printStatementRead(String id) {
 		printTAM("LOADA", symbolTable.retrieve(id).getAddress(),
 				"Load variable address for " + id);
 		if (symbolTable.retrieve(id).isNumeric()) {
 			printPrimitiveRoutine("getint", "Get a numeric value for " + id);
 		} else {
 			printPrimitiveRoutine("get", "Get a value for " + id);
 		}
 		printTAM("LOAD(1)", symbolTable.retrieve(id).getAddress(), "Load resulting value");
 	}
 	
 	/**
 	 * Cleant de stack als een resultaat niet meer nodig is.
 	 */
 	public void printStatementCleanup(String name) {
 		printTAM("POP(0)", "1", "Pop resulting value of " + name);
 	}
 
 	/**
 	 * Instantieer een GeneratorHelper
 	 */
 	public GeneratorHelper() {
 		super();
 		this.size = 0;
 		this.nextLabel = "";
 		this.labelNumber = 0;
 		this.constantScope = false;
 		this.routineLevel = 0;
 	}
 }
