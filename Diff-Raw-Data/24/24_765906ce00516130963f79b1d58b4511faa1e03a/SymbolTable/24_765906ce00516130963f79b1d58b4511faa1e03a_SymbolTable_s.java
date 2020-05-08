 /*
   SNARL/SymbolTable
 
   James Current
   Date: 3/20/12
   Time: 7:50 PM
  */
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.LinkedList;
 
 //Implements a symbol table using a table of stacks.
 
 public class SymbolTable {
     protected Hashtable<String, LinkedList<Scope>> table;   //The symbol table
     protected int level;                                    //The current scope level
 
     //Constructor. Returns a new SymbolTable that has no scopes.
 
     public SymbolTable(){
         table = new Hashtable<String, LinkedList<Scope>>();
         level = 0;
     }
 
     //isEmpty. Test if the SymbolTable has no scopes.
 
     public boolean isEmpty(){
         return level == 0;
     }
 
     //push. Push a new empty scope to the top of the SymbolTable.
     
     public void push(){
         level++;
     }
 
     //pop. Pop a scope off the top of the SymbolTable. If SymbolTable is empty, throw a RunTimeException.
     
     public void pop(){
         if(isEmpty()){
             throw new RuntimeException("Unable to pop, table is empty.");
        }

        if(!table.isEmpty()){
             for(Enumeration<String> names = table.keys();names.hasMoreElements();){
                 String name = names.nextElement();
                 LinkedList<Scope> list = table.get(name);
                 
                 if(list.peek().level == level){
                    Scope ignore = list.pop();
                     
                     if(list.isEmpty()){
                         table.remove(name);
                     }
                 }
             }
         }
 
         level--;
     }
 
     //isDeclared. Test if name appears in some scope.
     
     public boolean isDeclared(String name){
         return table.containsKey(name);
     }
 
     /*
         getDescriptor. Return the first Descriptor associated with name, if name is not in any scope, throw a
         SnarlCompilerException. If SymbolTable is empty, throw a RunTimeException.
      */
 
     public Descriptor getDescriptor(String name){
         if(isEmpty()){
             throw new RuntimeException("Unable to get Descriptor " + name + ", table is empty.");
         }
         
         if(!isDeclared(name)){
             throw new SnarlCompilerException(name + "is not declared.");
         }
         
         return table.get(name).peek().descriptor;
     }
 
     /*
         setDescriptor. If name is not in the topmost scope add it to the scope, if it is throw a
         SnarlCompilerException. If SymbolTable is empty, throw a RunTimeException.
      */
     
     public void setDescriptor(String name, Descriptor descriptor){
         if(isEmpty()){
             throw new RuntimeException("Unable to set Descriptor " + name + ", table is empty.");
         }
        
        if(isDeclared(name) && table.get(name).peek().level == level){
            throw new SnarlCompilerException(name + "is declared twice.");
        }
        
        if(!isDeclared(name)){
             LinkedList<Scope> list = new LinkedList<Scope>();
             list.push(new Scope(level,descriptor));
             table.put(name,list);
        } else {
            table.get(name).push(new Scope(level,descriptor));
         }
     }
 }
