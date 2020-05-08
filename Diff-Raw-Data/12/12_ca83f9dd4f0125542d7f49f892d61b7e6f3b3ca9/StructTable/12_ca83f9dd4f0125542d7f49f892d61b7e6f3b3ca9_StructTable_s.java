 import java.util.Hashtable;
 
 public class StructTable {
    private Hashtable<String, SymTable> structs = null;
    
   private boolean DEBUG = true;
    
    public StructTable() {
       structs = new Hashtable<String, SymTable>();
    }
    
    public SymTable getSymbolTable(String id) {
       return structs.get(id);
    }
 
    public boolean addStruct(String id) {
       if (structs.containsKey(id))
          return false;
       if(DEBUG) System.out.println("j> adding struct '" + id + "'");
       // SymTable doesn't matter, we're gonna overwrite it in update
       structs.put(id, new SymTable());
       return true;
    }
 /*
    public boolean addStruct(String id, SymTable nestedDecls) {
       if (structs.containsKey(id))
          return false;
       structs.put(id, nestedDecls);
       return true;
    }
 */
    public boolean updateStruct(String id, SymTable nestedDecls) {
       if (!structs.containsKey(id))
          return false;
       structs.put(id, nestedDecls);
       return true;
    }
 
    public boolean isDefined(String id) {
       return structs != null && structs.containsKey(id);
    }
    
   public boolean isField(String struct, String field) {
      return structs != null && ((SymTable)structs.get(struct)).isDefined(field);
    }
 
   public String getType(String struct, String field) {
      return ((SymTable)structs.get(struct)).getType(field);
    }
    
    public void print() {
       print("");
    }
    
    public void print(String tabs) {
       for(String key : structs.keySet()) {
          System.out.println(tabs + key + "\t:\t" + structs.get(key));
          //structs.get(key).print("\t\t");
       }
    }
 }
