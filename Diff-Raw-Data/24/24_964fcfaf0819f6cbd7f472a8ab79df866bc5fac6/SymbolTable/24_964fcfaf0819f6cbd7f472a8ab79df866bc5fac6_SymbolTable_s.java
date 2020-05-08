 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 
 public class SymbolTable {
    protected Map<String, Type> globals;
    protected Map<String, Type> locals;
    protected Map<String, Type> structs;
    protected Map<String, Type> functions;
 
    public SymbolTable() {
      globals = new HashMap<String, Type>(); 
      locals = new HashMap<String, Type>(); 
      structs = new HashMap<String, Type>(); 
      functions = new HashMap<String, Type>(); 
    }
 
    public String getTableName(Map table) {
       if (table == globals) return "global";
       if (table == locals) return "local";
       if (table == structs) return "structs";
       if (table == functions) return "functions";
       return "";
    }
 
    public void bind(Symbol s) {
       bind(s, false);
    }
 
    public void bind(Symbol s, boolean isGlobal) {
       if (isGlobal) {
          bind(s, globals);
       } else {
          bind (s, locals);
       }
    }
 
    public void bindStruct(Symbol s) {
       bind(s, structs);
    }
 
    public void bindFunction(Symbol fun) {
       bind(fun, functions);
    }
 
    public void bind(Symbol s, Map<String, Type> table) {
       if (table.get(s.getName()) == null) {
 
          table.put(s.getName(), s.getType());
          Evil.debug("Bound " + s + " in " + getTableName(table) + " table");
 
       } else {
         Evil.error("Cannot redeclare " + s 
          + " as " + getTableName(table));
       }
    }
 
    public Type get(String name) {
       if (locals.get(name) != null) {
          return locals.get(name);
       }
 
       if (globals.get(name) != null) {
          return globals.get(name);
       }
 
      Evil.error("Reference to undeclared variable " + name);

       return null;
    }
 
    public StructType getStruct(String name) {
       StructType s = (StructType)structs.get(name);

      if (s == null) {
         Evil.error("Struct " + name + " is undeclared");
      }

       return s;
    }
 
    public FuncType getFunction(String name) {
       FuncType fun = (FuncType)functions.get(name);
 
       if (fun == null) {
         Evil.error("Function " + name + " is undeclared");
       }
 
       return fun;
    }
 
    public void clearLocals() {
       locals.clear();
    }
 
    public void bindParameters(FuncType func) {
       for (Symbol s : func.getParams()) {
          bind(s);
       }
    }
 
    public void bindDeclarations(List<Symbol> symbols) {
       bindDeclarations(symbols, false);
    }
 
    public void bindDeclarations(List<Symbol> symbols, boolean isGlobal) {
       for (Symbol s : symbols) {
          bind(s, isGlobal);
       }
    }
 
    public void bindStructs(List<Symbol> symbols) {
       for (Symbol s : symbols) {
          bindStruct(s);
       }
    }
 
    public void bindFunctions(List<Symbol> symbols) {
       for (Symbol s : symbols) {
          bindFunction(s);
       }
    }
 }
