 import java.util.HashMap;
 
 public class SymbolTable {
 
 	private HashMap<String, Symbol> symbols;
 
 	private int numConsts = 0;
 	private int numVars = 0;
 	private int numTemps = 0;
 
         private String initialMemory;
 
 	public SymbolTable() {
 		symbols = new HashMap<String, Symbol>();
 	}
 
         public HashMap<String, Symbol> getSymbols(){
 	    return symbols;
         }
 
         public Symbol getSymbol(String name){
 	    if(symbols.containsKey(name)) {
 		return symbols.get(name);
 	    }
 	    else{
 		return null;
 	    }
         }
 
         public String getName(Symbol symbol){
 	    for(String name : symbols.keySet()){
 		if(symbols.get(name).equals(symbol)){
 		    return name;
 		}
 	    }
 	    return null;
 	}
 
         public String getInitialMemory() {
 	    return initialMemory;
         }
 
 	public void addConstant(Integer c) {
 		if (symbols.get("C" + c.toString()) == null) {
 			symbols.put("C" + c.toString(), new Symbol(c, Symbol.CONSTANT,
 					Symbol.UNDEFINED));
 			numConsts++;
 		}
 	}
 
 	public void addVariable(String name) {
 		if (symbols.get(name) == null) {
 			symbols.put(name, new Symbol(Symbol.UNDEFINED, Symbol.VARIABLE,
 					Symbol.UNDEFINED));
 			numVars++;
 		}
 	}
 
 	/**
 	 * Add a temporary variable to the symbol table.
 	 * 
 	 * @return The name of the temporary variable symbol added to the table.
 	 */
 	public String addTemp() {
 		String tempName = "T" + numTemps;
 		symbols.put(tempName, new Symbol(Symbol.UNDEFINED, Symbol.TEMP,
 				Symbol.UNDEFINED));
 		numTemps++;
 		return tempName;
 	}
 
         public Symbol addLabel(int label) {
 	    if (symbols.get("L" + label) == null) {
 		Symbol labelSymbol = new Symbol(label, Symbol.LABEL, Symbol.UNDEFINED);
 		symbols.put("L" + label, labelSymbol);
 		return labelSymbol;
 	    }
 	    else{
 		return symbols.get("L" + label);
 	    }
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 		b.append("Symbol Table:\n");
 		b.append("Num Constants: " + numConsts + ", Num Vars: " + numVars
 				+ ", Num Temps: " + numTemps + "\n");
 		b.append(symbols);
 		return b.toString();
 	}
 
         public void link(){
 	    StringBuilder b = new StringBuilder();
 
 	    int currAddr = 1;
 
 	    // Constants
 	    int currKeyNum = 0;
 	    int index = 0;
 	    while(index < numConsts){
 		String key = "C"+currKeyNum;
 		if(symbols.containsKey(key)){
 		    b.append(currAddr + " " + symbols.get(key).getValue() + "\n");
 		    symbols.get(key).setAddr(currAddr++);
 		    index++;
 		}
 		currKeyNum++;
 	    }
 
 	    // Variables
 	    for(String key : symbols.keySet()){
 		if(symbols.get(key).getType() == Symbol.VARIABLE){
 		    b.append(currAddr + " 0\n");
 		    symbols.get(key).setAddr(currAddr++); 
 		}
 	    }
 

 	    // Temps
 	    for(int i = 0; i < numTemps; i++){
 		b.append(currAddr + " 0\n");
 		symbols.get("T"+i).setAddr(currAddr++);
 	    }
 
 	    initialMemory = b.toString();
         }
 }
