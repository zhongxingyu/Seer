 package logicrepository.plugins.srs;
 
 import java.util.ArrayList;
 
 public class Sequence extends ArrayList<Symbol> implements AbstractSequence {
 
   public Sequence(int size){
     super(size);
   }
 
   public Sequence(){
     super();
   }
 
   public Sequence(ArrayList<Symbol> symbols){
     for(Symbol s : symbols){
       if(s == null) continue;
       add(s);
     }
   }
 
   public String toString(){
    if(size() == 0) return "\\epsilon";
     StringBuilder sb = new StringBuilder();
     for(Symbol s : this){
       sb.append(s.toString());
       sb.append(" ");
     }
     return sb.toString();
   }
 
   public Sequence copy(){
     Sequence ret = new Sequence(size());
     for(Symbol s : this){
       ret.add(s);
     }
     return ret;
   }
 
   public static void printSequenceArray(Sequence[] arr){
     for(Sequence s : arr){
       System.out.print(s);
       System.out.print("; ");
     }
     System.out.println();
   }
 }
