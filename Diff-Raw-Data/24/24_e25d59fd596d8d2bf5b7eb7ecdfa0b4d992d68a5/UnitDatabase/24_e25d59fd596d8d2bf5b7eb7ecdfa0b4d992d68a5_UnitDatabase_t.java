 package storage;
 
 import internalformatting.Unit;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 
 /**
  * An arraylist of unit objects that serves as a database for NewtonsToolbox
  * @author Clayven Anderson
  * 
  */
 public class UnitDatabase extends ArrayList<Unit> implements Serializable
 {
 private static final long serialVersionUID = 7363265L;
   
   public UnitDatabase(){
     super(); //creates an Arraylist of capacity 1
     this.clear();
   }
   
   /**
    * Adds a Variable object to Arraylist of Variables
    * @param Var the variable to be added to the database
    */
   public void addUnit(Unit Uni){
    this.ensureCapacity(this.size() + 1);
     this.add(Uni);
   }
   
   /**
    * returns a Unit at a specified index
    * @param i index of the desired unit
    * @return the for variable located at index i
    */
   public Unit getUnit (int i){
     return this.get(i);
   }
   
   /**
    * removes the unit at  index i
    * @param i the index of the unit to be removed
    */
   public void rmUnit(int i){
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
