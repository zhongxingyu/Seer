 //import java stack utility
 import java.util.Stack;
 
 //declare stack class
 public class SymbolHashStack {
 
 	//instantiate stack object and scope tracker 
 	private static Stack<SymbolHash> symbolHashStack = new Stack<SymbolHash>();
 	private static Stack<SymbolHash> symbolHashStackHandler = new Stack<SymbolHash>();
 	private static int scope = 1;
 	private static int globaltraversaltracker = 0;
 	
 	//***********************
 	//	methods 
 	//
 	//***********************
 
 	//push to global hash
 	public static void newGlobal() {
 
 		symbolHashStack.push(new SymbolHash("GLOBAL"));
 	}
 	
 	//push to function hash
 	public static void newFunction(String name) {
 
 		symbolHashStack.push(new SymbolHash(name));
 	}
 	
 	//push to block hash
 	public static void newBlock() {
 
 		symbolHashStack.push(new SymbolHash("BLOCK " + Integer.toString(scope++)));
 	}
 	
 	//add new symbol to current stack hash
 	public static void newSymbol(String type, String name, String value) {
 
 		SymbolHash tempHash = symbolHashStack.pop();
 		tempHash.newSymbol(type, name, value);
 		symbolHashStack.push(tempHash);
 	}
 
 	//print ";tiny code" header
 	public static void printTinyCode() {
 
 		SymbolHash tempHash = symbolHashStack.pop();
 		tempHash.printTinyCode();
 		symbolHashStack.push(tempHash);
 	}
 
 	//print ";IR code" header
 	public static void printIRCode() {
 
 		SymbolHash tempHash = symbolHashStack.pop();
 		tempHash.printIRCode();
 		symbolHashStack.push(tempHash);
 	}	
 	
 	//pop hash and return to last
 	public static void popHash() {
 
 		SymbolHash tempHash = symbolHashStack.pop();
 	}
 	
 	//check global symbol type
 	public static String checkGlobal(String name) {
 
 		String type = "";
 		String[] names;	
 		names = name.split(",");
 		SymbolHash tempHash = symbolHashStack.pop();
 		symbolHashStackHandler.push(tempHash);
 		globaltraversaltracker++;
 		while (tempHash.checkScope().contains("GLOBAL")!=true) {
 		
 			tempHash = symbolHashStack.pop();
			System.out.println(";Stack handling: " + tempHash.checkScope());
 			symbolHashStackHandler.push(tempHash);
 			globaltraversaltracker++;
 		}
 		for (int j=0;j<names.length;j++) {
 			//System.out.println(names[j]);
 			type = tempHash.checkType(names[j]);
 			//System.out.println(type);
 		}
 		for (int i=0; i < globaltraversaltracker; globaltraversaltracker--) {
 		
 			tempHash = symbolHashStackHandler.pop();
			System.out.println(";Stack handling: " + tempHash.checkScope());
 			symbolHashStack.push(tempHash);
 		}
 		return type;
 	}
 
 	//check id's type
 	public static String checkType(String name) {
 				
 		//splitArgs		
 		String type = "";
 		String[] names;
 		names = name.split(",");
 		SymbolHash tempHash = symbolHashStack.pop();
 		for (int i=0;i<names.length;i++) {
 			//System.out.println(names[i]);
 			type = tempHash.checkType(names[i]);
 			//System.out.println(type);
 			symbolHashStack.push(tempHash);
 		}
 		if (type == "E") {
 			type = checkGlobal(name);
 		}
 		return type;
 	}
 }
