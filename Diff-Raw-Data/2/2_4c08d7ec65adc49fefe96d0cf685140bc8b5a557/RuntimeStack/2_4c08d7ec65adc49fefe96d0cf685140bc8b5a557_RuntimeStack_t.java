 package proj;
 
 import java.util.LinkedList;
 import java.util.HashMap;
 
 import proj.AbstractSyntax.Variable;
 import proj.AbstractSyntax.Value;
 
 public class RuntimeStack{
 
     public HashMap<Variable, Value> globalVarValues;
     public LinkedList<ActivationRecord> activationRecords;
 
     public RuntimeStack(){
         globalVarValues = new HashMap<Variable, Value>();
         activationRecords = new LinkedList<ActivationRecord>();
     }
 
     public void setGlobal(Variable var, Value val){
         globalVarValues.put(var, val);
     }
 
     public Value getGlobal(Variable var){
         return globalVarValues.get(var);
     }
 
     public void addRecord(ActivationRecord ar){
         activationRecords.addFirst(ar);
     }
 
     //remove when the function is done running
     public void removeRecord(){
         activationRecords.removeFirst();
     }
 
     public ActivationRecord getRecord(){
         try{
             return activationRecords.getFirst();
         }
         catch(java.util.NoSuchElementException e){
             System.out.println("that shit was null yo");
             return null;
         }
     }
 
     public void printStack(){
         printGlobals();
         for(ActivationRecord ar : activationRecords){
             System.out.println(ar.toString());
         }
     }
 
     public void printGlobals(){
         System.out.println("++++++++++++++++++++");
         System.out.println("Global Variables");
         System.out.println("++++++++++++++++++++");
         for(Variable var : globalVarValues.keySet()){
             System.out.println("var: " + var.toString() +
                    "   val: " + ((globalVarValues.get(var).toString() == null) ? globalVarValues.get(var).toString() : "null" ));
         }
     }
 
 }
