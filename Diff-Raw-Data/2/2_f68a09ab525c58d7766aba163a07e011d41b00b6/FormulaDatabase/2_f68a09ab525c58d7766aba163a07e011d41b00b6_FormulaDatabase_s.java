 package storage;
 import internalformatting.Formula;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 public class FormulaDatabase extends ArrayList<Formula> implements Serializable
 {
   
   @SuppressWarnings("unused")
  private static final long serialVersionID = 4101660L;
   
   /** default constructor*/
   public FormulaDatabase(){
     super(1); //creates an Arraylist of capacity 1
     this.add(0,null);
   }
   
   /**
    * Adds a formula object to Arraylist of formulas
    * @param Form the formula to be added to the database
    */
   public void AddFormula(Formula Form){
     this.add(Form);
   }
   
   /**
    * removes the formula at  index i
    * @param i the index of the formula to be removed
    */
   public void rmFormula(int i){
     this.remove(i);
   }
   /**
    * return the size of the database as an int
    * @return size of the Database
    */
   public int getSize(){
     return this.size();
   }
 }
