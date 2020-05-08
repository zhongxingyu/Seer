 package proj;
 
 import java.util.LinkedList;
 import java.util.HashMap;
 
 import proj.AbstractSyntax.Variable;
 import proj.AbstractSyntax.Value;
 
 public class RuntimeStack{
 
     private HashMap<Variable, Value> globalVarValues;
     private LinkedList<ActivationRecord> activationRecords;
 
     public RuntimeStack(){
         globalVarValues = new HashMap<Variable, Value>();
         activationRecords = new LinkedList<ActivationRecord>();
     }
 
     public void addRecord(ActivationRecord ar){
         activationRecords.addFirst(ar);
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
        System.out.println("!!!begin!!!");
         for(ActivationRecord ar : activationRecords){
             System.out.println(ar.toString());
         }
        System.out.println("!!!!end!!!!");
     }
 
 }
