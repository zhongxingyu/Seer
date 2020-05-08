 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domini;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author Daniel Albarral
  */
 public class RestriccioTemps {
     
     private ArrayList<Integer> dilluns;
     private ArrayList<Integer> dimarts;
     private ArrayList<Integer> dimecres;
     private ArrayList<Integer> dijous;
     private ArrayList<Integer> divendres;
     
     
     /**
      * 
      * @return  Arraylist amb tots els dies i hores disponibles L->dia, R->hora
      */
     public ArrayList<Pair<String,Integer>> disponibilitat(){
         ArrayList<Pair<String,Integer>> disponibilitat = new ArrayList();
         return disponibilitat; 
     }
 }
