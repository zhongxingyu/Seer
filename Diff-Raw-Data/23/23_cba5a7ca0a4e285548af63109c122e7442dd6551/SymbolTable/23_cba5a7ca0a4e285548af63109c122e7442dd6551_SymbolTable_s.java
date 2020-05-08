 package intermediate;
 
 import java.util.TreeMap;
 
 public class SymbolTable extends TreeMap<String, SymbolTableEntry> {
     private SymbolTable predecessor;
     private int nestingLevel;
 
     public SymbolTable() {
         nestingLevel = 0;
     }
 
     public SymbolTable(int nestingLevel) {
         this.nestingLevel = nestingLevel;
     }
 
     public SymbolTable getPredecessor() {
         return predecessor;
     }
 
     public void setPredecessor(SymbolTable predecessor) {
         this.predecessor = predecessor;
     }
 
     public int getNestingLevel() {
         return nestingLevel;
     }
 
     public void setNestingLevel(int nestingLevel) {
         this.nestingLevel = nestingLevel;
     }
 
 
     public void addEntry(String name, SymbolTableEntry entry)
     {
         put(name, entry);
     }
 
     public SymbolTableEntry getEntry(String key)
     {
         return get(key);
     }
 
   /* public SymbolTableEntry enter(String name) {
        SymbolTableEntry entry = new SymbolTableEntry(name, this);
         put(name, entry);
 
         return entry;
    }  */
 
     public SymbolTableEntry lookup(String name) {
         return get(name);
     }
 }
