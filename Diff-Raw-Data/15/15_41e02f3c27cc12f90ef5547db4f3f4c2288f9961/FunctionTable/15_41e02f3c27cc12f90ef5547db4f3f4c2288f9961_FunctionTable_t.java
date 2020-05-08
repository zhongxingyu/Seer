 import java.util.HashMap;
 import java.util.LinkedList;
 
 class Function {
 	private String name;
 	private Symbol label;
 	private LinkedList<Instruction> instructions;
 	private HashMap<Integer, Symbol> labels;
 	private LinkedList<Symbol> tempSymbols;
 	private HashMap<String, Symbol> variableSymbolsMap;
 	private LinkedList<Symbol> variableSymbols;
 	private HashMap<String, Symbol> parameterSymbols;
 	private int numParams;
 
 	private int startAddress;
 
 	private int labelCount;
 	
 	public Function(String name, Symbol label) {
 		this.name = name;
 		this.label = label;
 		this.instructions = new LinkedList<Instruction>();
 		this.labels = new HashMap<Integer, Symbol>();
 		this.tempSymbols = new LinkedList<Symbol>();
 		this.variableSymbolsMap = new HashMap<String, Symbol>();
 		this.variableSymbols = new LinkedList<Symbol>();
 		this.startAddress = -1;
 		this.numParams = 0;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public LinkedList<Symbol> getVariables() {
 		return this.variableSymbols;
 	}
 
 	public LinkedList<Symbol> getTemps() {
 		return this.tempSymbols;
 	}
 
 	public Symbol getLabel() {
 		return this.label;
 	}
 
 	public int getNumParams() {
 		return numParams;
 	}
 
 	public void link(SymbolTable st, FunctionTable ft, int startingAddress) {
 		this.startAddress = startingAddress;
 		LinkedList<Instruction> linkedInstructions = new LinkedList<Instruction>();
		HashMap<Integer, Symbol> linkedLabels = new HashMap<Integer, Symbol>();

 		int i = 0;
 		for(Instruction instruction : instructions) {
 			if(labels.containsKey(new Integer(i))) {
				linkedLabels.put(new Integer(linkedInstructions.size()), labels.get(new Integer(i)));
 			}

 			if(instruction instanceof PseudoInstruction) {
 				((PseudoInstruction)instruction).link(st, ft, this, linkedInstructions);
 			}
 			else {
 				linkedInstructions.addLast(instruction);
 			}
 
 			i++;
 		}
 		instructions = linkedInstructions;
 
 		if(label != null) {
 			label.setAddr(startingAddress);
 		}
		labels = linkedLabels;
 		for(Integer key : labels.keySet()) {
 			Symbol thisLabel = labels.get(key);
 			thisLabel.setAddr(key.intValue() + startingAddress);
 		}
 	}
 
 	public int numInstructions() {
 		return instructions.size();
 	}
 
 	public Symbol addTemp() {
 		Symbol newTemp = new Symbol(name + "::T" + tempSymbols.size(), Symbol.UNDEFINED, Symbol.TEMP, Symbol.UNDEFINED);
 		tempSymbols.addLast(newTemp);
 		return newTemp;
 	}
 
 	public Symbol getVariable(String name) {
 		if(variableSymbolsMap.containsKey(name)) {
 			return variableSymbolsMap.get(name);
 		}
 		else {
 			Symbol newVariable = new Symbol(this.name + "::" + name, Symbol.UNDEFINED, Symbol.VARIABLE, Symbol.UNDEFINED);
 			variableSymbolsMap.put(name, newVariable);
 			variableSymbols.addLast(newVariable);
 			return newVariable;
 		}
 	}
 
 	public Symbol addParameter(String name) {
 		if(variableSymbolsMap.containsKey(name)) {
 			return variableSymbolsMap.get(name);
 		}
 		else {
 			numParams++;
 			Symbol newVariable = new Symbol(this.name + "::" + name, Symbol.UNDEFINED, Symbol.VARIABLE, Symbol.UNDEFINED);
 			variableSymbolsMap.put(name, newVariable);
 			variableSymbols.addLast(newVariable);
 			return newVariable;
 		}
 	}
 
 	public void add(Instruction instruction) {
 		instructions.addLast(instruction);
 	}
 
 	public void add(Symbol label, Instruction instruction) {
 		labels.put(new Integer(instructions.size()), label);
 		instructions.addLast(instruction);
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		int j = 0;
 		for(Instruction instruction : instructions) {
 		    	builder.append(instruction);
			if(j == 0 && this.label != null) {
                 		builder.append("\t; " + this.label);
                 	}
             		if(labels.containsKey(new Integer(j))) {
 				builder.append("\t; " + labels.get(new Integer(j)));
			}
 			builder.append("\n");
 			j++;
 		}
 		return builder.toString();
 	}
 }
 
 public class FunctionTable {
 
 	private HashMap<String, Function> functions;
 
 	public FunctionTable() {
 		functions = new HashMap<String, Function>();
 	}
 
         public HashMap<String, Function> getFunctions(){
 	    return functions;
         }
 
         public void addFunction(Function function) {
 	    functions.put(function.getName(), function);
         }
 
         public Function get(String name){
 	    if(functions.containsKey(name)) {
 		return functions.get(name);
 	    }
 	    else{
 		return null;
 	    }
         }
 }
