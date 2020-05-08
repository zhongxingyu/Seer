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
 		int i = 0;
 		for(Instruction instruction : instructions) {
 			if(labels.containsKey(new Integer(i))) {
 				labels.put(new Integer(linkedInstructions.size()), labels.remove(new Integer(i)));
 			}
 
 			// Fix load instructions
 			if(instruction.getOperator().equals("LD")) {
 				Symbol symbol = instruction.getSymbol();
 
 				int position = 0;
 				if(symbol.getType() == Symbol.VARIABLE) {
 					position = variableSymbols.indexOf(symbol);
 				}
 				else if(symbol.getType() == Symbol.TEMP) {
 					position = variableSymbols.size() + tempSymbols.indexOf(symbol);
 				}
 				else {
 					linkedInstructions.addLast(Instruction.Loadd(symbol));
 					continue;
 				}
 				// Get the proper space in memory
 				linkedInstructions.addLast(Instruction.Loadd(st.getFP()));
 				if(position > 0) {
 					linkedInstructions.addLast(Instruction.Add(st.addConstant(new Integer(position))));
 				}
 				linkedInstructions.addLast(Instruction.Stored(st.getScratch1()));
 
 				// Load proper space in memory
 				linkedInstructions.addLast(Instruction.Loadi(st.getScratch1()));
 			}
 			// Fix Store instructions
 			else if(instruction.getOperator().equals("ST")) {
 				Symbol symbol = instruction.getSymbol();
 				int position = 0;
 				if(symbol.getType() == Symbol.VARIABLE) {
 					position = variableSymbols.indexOf(symbol);
 				}
 				else if(symbol.getType() == Symbol.TEMP) {
 					position = variableSymbols.size() + tempSymbols.indexOf(symbol);
 				}
 				else {
 					linkedInstructions.addLast(Instruction.Stored(symbol));
 					continue;
 				}
 
 				// Store current value in a scratch register
 				linkedInstructions.addLast(Instruction.Stored(st.getScratch1()));
 
 				// Calculate proper place to store
 				linkedInstructions.addLast(Instruction.Loadd(st.getFP()));
 				if(position > 0) {
 					linkedInstructions.addLast(Instruction.Add(st.addConstant(new Integer(position))));
 				}
 
 				linkedInstructions.addLast(Instruction.Stored(st.getScratch2()));
 				linkedInstructions.addLast(Instruction.Loadd(st.getScratch1()));
 				linkedInstructions.addLast(Instruction.Storei(st.getScratch2()));
 			}
 			// Fix function call instructions
 			else if(instruction.getOperator().equals("CALu")) {
 				Symbol symbol = instruction.getSymbol();
 				if(symbol.getType() == Symbol.FUNCTION) {
 					Function callFunction = ft.get(symbol.getName());
 					LinkedList<Symbol> symbols = instruction.getSymbols();
 
 					if (callFunction.getNumParams() != symbols.size()) {
 						System.out.println("Syntax Error: Param count does not match");
 						System.exit(1);
 					}
 
 					// Place all the parameters at the start of the record
 					Symbol constant1 = st.addConstant(new Integer(1));
 					Symbol constant0 = st.addConstant(new Integer(0));
 
 					for (Symbol paramSymbol : symbols) {
 						int position = 0;
 						if(paramSymbol.getType() == Symbol.VARIABLE) {
 							position = variableSymbols.indexOf(paramSymbol);
 						}
 						else if(paramSymbol.getType() == Symbol.TEMP) {
 							position = variableSymbols.size() + tempSymbols.indexOf(paramSymbol);
 						}
 						else {
 							linkedInstructions.addLast(Instruction.Loadd(paramSymbol));
 							continue;
 						}
 						// Get the proper space in memory
 						linkedInstructions.addLast(Instruction.Loadd(st.getFP()));
 						if(position > 0) {
 							linkedInstructions.addLast(Instruction.Add(st.addConstant(new Integer(position))));
 						}
 						linkedInstructions.addLast(Instruction.Stored(st.getScratch1()));
 
 						// Load proper space in memory
 						linkedInstructions.addLast(Instruction.Loadi(st.getScratch1()));
 
 						linkedInstructions.addLast(Instruction.Storei(st.getSP()));
 						linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
 						linkedInstructions.addLast(Instruction.Add(constant1));
 						linkedInstructions.addLast(Instruction.Stored(st.getSP()));
 					}
 					if(symbols.size() == 0) {
 						linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
 					}
 
 					// Allocate space for the variables and temporaries and return val
 					Symbol constantVarSize = st.addConstant(new Integer(callFunction.getVariables().size() + callFunction.getTemps().size() + 1));
 					linkedInstructions.addLast(Instruction.Add(constantVarSize));
 					linkedInstructions.addLast(Instruction.Stored(st.getSP()));
 
 					// Save previous FP
 					linkedInstructions.addLast(Instruction.Loadd(st.getFP()));
 					linkedInstructions.addLast(Instruction.Storei(st.getSP()));
 		
 					// Update FP to be the start of this record
 					Symbol constantActivationSize = st.addConstant(new Integer(callFunction.getVariables().size() + callFunction.getTemps().size() + 1));
 					linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
 					linkedInstructions.addLast(Instruction.Subtract(constantActivationSize));
 					linkedInstructions.addLast(Instruction.Stored(st.getFP()));
 
 					// Set the return address
 					linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
 					linkedInstructions.addLast(Instruction.Add(constant1));
 					linkedInstructions.addLast(Instruction.Stored(st.getSP()));
 
 					// Call the Function
 					linkedInstructions.addLast(Instruction.Call(callFunction.getLabel()));
 
 					// Set the return value symbol
 					Symbol returnTemp = instruction.getReturnSymbol();
 					linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
					linkedInstructions.addLast(Instruction.Subtract(constant1));
					linkedInstructions.addLast(Instruction.Stored(st.getSP()));
					linkedInstructions.addLast(Instruction.Loadi(st.getSP()));
 
 					int position = variableSymbols.size() + tempSymbols.indexOf(returnTemp);
 
 					// Store current value in a scratch register
 					linkedInstructions.addLast(Instruction.Stored(st.getScratch1()));
 
 					// Calculate proper place to store
 					linkedInstructions.addLast(Instruction.Loadd(st.getFP()));
 					if(position > 0) {
 						linkedInstructions.addLast(Instruction.Add(st.addConstant(new Integer(position))));
 					}
 
 					linkedInstructions.addLast(Instruction.Stored(st.getScratch2()));
 					linkedInstructions.addLast(Instruction.Loadd(st.getScratch1()));
 					linkedInstructions.addLast(Instruction.Storei(st.getScratch2()));
 					// Return value has now been stored in temporary symbol
 
 					// Revert the FP
 					linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
 					linkedInstructions.addLast(Instruction.Subtract(constant1));
 					linkedInstructions.addLast(Instruction.Stored(st.getSP()));
 					linkedInstructions.addLast(Instruction.Loadi(st.getSP()));
 					linkedInstructions.addLast(Instruction.Stored(st.getFP()));
 
 					// Revert the SP
 					Symbol constantSize = st.addConstant(new Integer(callFunction.getVariables().size() + callFunction.getTemps().size()));
 					linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
 					linkedInstructions.addLast(Instruction.Subtract(constantSize));
 					linkedInstructions.addLast(Instruction.Stored(st.getSP()));
 				}
 				else {
 					System.err.println("Fatal Exception!");
 					System.exit(1);
 				}
 			}
 			else if(instruction.getOperator().equals("RET")) {
 				Symbol returnSymbol = instruction.getSymbol();
 
 				int position = 0;
 				if(returnSymbol.getType() == Symbol.VARIABLE) {
 					position = variableSymbols.indexOf(returnSymbol);
 				}
 				else if(returnSymbol.getType() == Symbol.TEMP) {
 					position = variableSymbols.size() + tempSymbols.indexOf(returnSymbol);
 				}
 				else {
 					linkedInstructions.addLast(Instruction.Loadd(returnSymbol));
 				}
 				// Get the proper space in memory
 				linkedInstructions.addLast(Instruction.Loadd(st.getFP()));
 				if(position > 0) {
 					linkedInstructions.addLast(Instruction.Add(st.addConstant(new Integer(position))));
 				}
 				linkedInstructions.addLast(Instruction.Stored(st.getScratch1()));
 
 				// Load proper space in memory
 				linkedInstructions.addLast(Instruction.Loadd(st.getSP()));
 				Symbol constant2 = st.addConstant(new Integer(2));
 				linkedInstructions.addLast(Instruction.Subtract(constant2));
 				linkedInstructions.addLast(Instruction.Stored(st.getScratch2()));
 				linkedInstructions.addLast(Instruction.Loadi(st.getScratch1()));
 				linkedInstructions.addLast(Instruction.Storei(st.getScratch2()));
 
 				// TODO: jump back to return address
 				linkedInstructions.addLast(Instruction.JumpIndirect(st.getSP()));
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
 		for(Integer key : labels.keySet()) {
 			Symbol thisLabel = labels.get(key);
 			thisLabel.setAddr(key.intValue() + startingAddress);
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
                         if(!instruction.isNOP()) {
 			    	builder.append(instruction);
 				if(j == 0 && this.label != null) {
                         		builder.append("\t; " + this.label);
                         	}
                     		if(labels.containsKey(new Integer(j))) {
 					builder.append("\t; " + labels.get(new Integer(j)));
 				}
 				builder.append("\n");
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
