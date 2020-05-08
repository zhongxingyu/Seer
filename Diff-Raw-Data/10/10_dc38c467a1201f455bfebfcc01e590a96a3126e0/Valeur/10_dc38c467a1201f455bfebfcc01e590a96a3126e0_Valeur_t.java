 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package AST;
 
 /**
  *
  * @author hubert
  */
 public class Valeur extends Expression {
 
     int v;
 
     public Valeur(String v) {
        int val = Integer.parseInt(v);
         this.v = val;
     }
 
     @Override
     public String toString() {
 
         return "Valeur[" + v + "]";
     }
 
     @Override
     public String toAsm() {
        String s ;
        s = "\tmov\teax, " + v + "\n";

         return s;
     }
 }
