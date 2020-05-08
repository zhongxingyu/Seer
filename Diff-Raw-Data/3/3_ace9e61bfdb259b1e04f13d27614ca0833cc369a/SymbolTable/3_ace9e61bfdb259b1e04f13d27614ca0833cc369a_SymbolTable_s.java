 package malice;
 
 import java.util.Map;
 
 public class SymbolTable {
     
     private Map<String, Symbol> symbolTable;
     
     public SymbolTable() {
     }
     
     public void addVariable(String variableName, Type variableType) {
         symbolTable.put(variableName, new Symbol(variableType));
     }
     
     public boolean containsVariable(String variableName) {
         return symbolTable.containsKey(variableName);
     }
     
     public Type getVariableType(String variableName) {
         return symbolTable.get(variableName).getType();
     }
     
     public Register getVariableRegister(String variableName) {
         return symbolTable.get(variableName).getRegister();
     }
 }
