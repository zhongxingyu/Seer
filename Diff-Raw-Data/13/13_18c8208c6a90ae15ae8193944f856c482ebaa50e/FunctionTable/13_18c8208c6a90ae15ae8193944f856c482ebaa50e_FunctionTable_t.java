 import java.util.HashMap;
 import java.util.LinkedList;
 
 class Function {
 	private String name;
 	private Symbol label;
 	private LinkedList<Instruction> instructions;
 	private HashMap<Integer, Symbol> labels;
 	private LinkedList<Symbol> tempSymbols;
 	private HashMap<String, Symbol> variableSymbols;
 	private HashMap<String, Symbol> parameterSymbols;
 	private int startAddress;
 
 	private int labelCount;
 	
 	public Function(String name, Symbol label) {
 		this.name = name;
 		this.label = label;
 		this.instructions = new LinkedList<Instruction>();
 		this.labels = new HashMap<Integer, Symbol>();
 		this.tempSymbols = new LinkedList<Symbol>();
 		this.variableSymbols = new HashMap<String, Symbol>();
 		this.parameterSymbols = new HashMap<String, Symbol>();
 		this.startAddress = -1;
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public HashMap<String, Symbol> getVars() {
 		return this.variableSymbols;
 	}
 	public HashMap<String, Symbol> getParams() {
 		return this.parameterSymbols;
 	}
 	public LinkedList<Symbol> getTemps() {
 		return this.tempSymbols;
 	}
 
 	public Symbol getLabel() {
 		return this.label;
 	}
 
 	public void setStartingAddress(int address) {
 		this.startAddress = address;
 		label.setAddr(address);
 		for(Integer key : labels.keySet()) {
 			Symbol thisLabel = labels.get(key);
 			thisLabel.setAddr(key.intValue() + address);
 		}
 	}
 
 	public int numInstructions() {
                 int count = 0;
                 for(Instruction instruction : instructions) {
 			if(!instruction.isNOP()) {
 				count ++;
 			}
                 }
 		return count;
 	}
 
 	public Symbol addTemp() {
 		Symbol newTemp = new Symbol(name + "::T" + tempSymbols.size(), Symbol.UNDEFINED, Symbol.TEMP, Symbol.UNDEFINED);
 		tempSymbols.addLast(newTemp);
 		return newTemp;
 	}
 
 	public Symbol getVariable(String name) {
 		if(variableSymbols.containsKey(name)) {
 			return variableSymbols.get(name);
 		}
 		else {
 			Symbol newVariable = new Symbol(this.name + "::" + name, Symbol.UNDEFINED, Symbol.VARIABLE, Symbol.UNDEFINED);
 			variableSymbols.put(name, newVariable);
 			return newVariable;
 		}
 	}
 
 	public void addParameter(String name) {
 		if(!parameterSymbols.containsKey(name)) {
 			parameterSymbols.put(name, new Symbol(this.name + "::" + name, Symbol.UNDEFINED, Symbol.VARIABLE, Symbol.UNDEFINED));
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
                 int i = startAddress;
		int j = 0;
 		for(Instruction instruction : instructions) {
			if(j == 0 && this.label != null) {
                        	builder.append(this.label + ":\n");
                        }
                    	if(labels.containsKey(new Integer(j))) {
				builder.append(labels.get(new Integer(j)) + ":\n");
			}
                         if(!instruction.isNOP()) {
			    	builder.append(instruction + "\n");
                         }
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
