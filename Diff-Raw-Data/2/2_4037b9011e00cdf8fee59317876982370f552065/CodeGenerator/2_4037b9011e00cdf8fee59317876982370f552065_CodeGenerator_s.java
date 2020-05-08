 package assembler;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Stack;
 import java.util.regex.Pattern;
 
 import Lexical.SymbolTable;
 
 
 /**
  * CodeGenerator will iterate through the rpn translating the intermediate code
  * to the actual assembler code
  * @author Inti
  *
  */
 public class CodeGenerator {
 	
 	public static Stack<String> operandStack;
 	public static String check; 
 	public static String context; 
 	private HashMap<String, String> labels;
 	public static ArrayList<String> assembler;
 	
 	public CodeGenerator() {		
 		check = "";
 		context = "";
 		assembler = new ArrayList<String>();
 		operandStack = new Stack<String>();
 		this.labels = new HashMap<String,String>();
 	}
 	/**
 	 * Generates the assembler code given the rpn
 	 * @param rpn the intermediate code in Reverse Polish Notation
 	 * @throws IOException 
 	 */
 	public void generate(HashMap<String,ArrayList<String>> rpn) {
 		this.addHeader();
 		this.addDeclarations();
 		this.parseIntCode(rpn, "MAIN");
 		this.addExitDeclarations();
 		this.addFunctions(rpn);
 		printCode(assembler);
 	}
 	
 	public static void printCode(ArrayList<String> assembler){
 		for(String codeItem: assembler){
 			System.out.println(codeItem);
 		}
 	}
 	
 	/**
 	 * Add to the file the necessary libraries for the MASM
 	 * @throws IOException 
 	 */
 	private void addHeader() {
 		
 		assembler.add(".386" ); 
 		assembler.add(".model flat, stdcall" );
 		assembler.add("option casemap :none" );
 		assembler.add("include \\masm32\\include\\windows.inc" );
 		assembler.add("include \\masm32\\include\\kernel32.inc" );
 		assembler.add("include \\masm32\\include\\user32.inc" );
 		assembler.add("includelib \\masm32\\lib\\kernel32.lib" );
 		assembler.add("includelib \\masm32\\lib\\user32.lib" );
 		
 	}
 	/**
 	 * Takes the Symbol Table, and adds the declaration statements, given the variables
 	 * defined in it.
 	 * @throws IOException 
 	 */
 	private void addDeclarations() {
 		assembler.add(".data");
 		 
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		Set<String> idNames =  symbolTable.keySet();
 		for(String id : idNames){
 			if(symbolTable.get(id).getUse()=="VAR"){
 				assembler.add(id +" DD ?");
 			} else if(symbolTable.get(id).getType()=="CTE") {
				assembler.add("_"+id +" DD ?");
 			}
 		}
 		assembler.add(".code");
 		assembler.add("start:");
 	}
 	
 	/**
 	 * Adds the exit declarations to the assembler code
 	 */
 	private void addExitDeclarations() {
 		assembler.add("invoke ExitProcess, 0");
 		assembler.add("end start");
 	}
 	
 	/**
 	 * Generates and stores the labels for each jump
 	 * @param rpn The main RPN intermediate code
 	 */
 	private void generateLabels(ArrayList<String> rpn, String context) {
 		for(int i = 0; i< rpn.size();i++){
 			//If it's a jump instruction
 			if(Pattern.matches("\\[BF\\]|\\[JMP\\]",rpn.get(i))) {
 				this.labels.put(rpn.get(i-1), context+"_label_"+rpn.get(i-1));
 			}
 		}
 	}
 	/**
 	 * Adds function declarations to the assembler code
 	 * @param rpn
 	 */
 	private void addFunctions(HashMap<String,ArrayList<String>> rpn){
 		for(String func: rpn.keySet()) {
 			if(func != "MAIN") {
 				assembler.add("function_"+func+":");
 				this.parseIntCode(rpn,func);
 			}
 		}
 	}
 	/**
 	 * Parses the intermediate code, stacking the operands, and executing the operators
 	 * @param rpn the intermediate code in Reverse Polish Notation
 	 * @throws IOException 
 	 */
 	private void parseIntCode(HashMap<String,ArrayList<String>> rpn, String context) {
 		ArrayList<String> intermediateCode = rpn.get(context);
 		this.generateLabels(intermediateCode, context);
 		CodeGenerator.context = context;
 		OperatorFactory factory = new OperatorFactory();
 		String codeItem = new String();
 		for(int i=0; i< intermediateCode.size();i++) {
 			codeItem = intermediateCode.get(i);
 			System.out.println(codeItem);
 			//add label to assembler code
 			if(labels.containsKey(Integer.toString(i))) {
 				assembler.add(labels.get(Integer.toString(i))+":");
 				labels.remove(Integer.toString(i));
 			}
 			
 			//if it's not an operator
 			if(Pattern.matches("\\w+|^'",codeItem)) {
 				//prepend _ if operand is a constant
 				if(Pattern.matches("\\d+",codeItem)) {
 					codeItem = "_"+codeItem;
 				}
 				operandStack.push(codeItem);
 			} else {
 				factory.create(codeItem).operate(operandStack);
 			} 
 			
 		} 
 		if(labels.values().size() > 0) {
 			ArrayList<String> remainingLabels = new ArrayList<String>(labels.values());
 			String lastLabel = remainingLabels.get(0);
 			assembler.add(lastLabel+":");
 		}
 		
 	}
 	
 }
 
