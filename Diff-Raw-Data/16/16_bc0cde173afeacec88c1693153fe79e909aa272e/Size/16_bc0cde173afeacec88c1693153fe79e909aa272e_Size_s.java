 package model.common;
 
 import java.io.Serializable;
 
 import common.Result;
 
 /**
  * The Size class is a way to store both a magnitude and a unit at once.
  */
 public class Size implements Serializable{
     // This is to be able to set the units with consistency. 
 
     private float _amount;
     private String _unit;
 
     /**
      * Constructor
      */
     public Size(float a, String u){
         assert true;
         this._amount = a;
         this._unit = u;
     }
 
     /**
      * Returns the magnitude of the size.
      */
     public float getAmount(){
         return _amount;
     }
 
     /**
      * Sets the magnitude of the Size. Must be non-negative.
      */
     public Result setAmount(float a){
         float oldAmt = _amount;
         _amount = a;
         Result valid = validate();
         if (!valid.getStatus()){
             _amount = oldAmt;
             return valid;
         }
         return new Result(true, "Amount was set successfully");
     }
 
     /**
      * Returns the unit for this Size.
      */
     public String getUnit(){
         return _unit;
     }
 
     /**
      * Sets the Unit for the Size. Can be found in Size.Unit
      */
     public Result setUnit(String u){
         String oldUnit = _unit;
         _unit = u;
         Result valid = validate();
         if (!valid.getStatus()){
             _unit = oldUnit;
             return valid;
         }
         return new Result(true, "The unit was set successfully");
     }
 
     /**
      * Makes sure the Size is valid.
      */
     public Result validate(){
         if (_amount < 0){
             return new Result(false, "The amount must not be negative.");
         }
        if (_unit.toLowerCase().contentEquals("count") && _amount != 1.0){
         	//TODO:I commented this out because product groups can have a 3month supply with a count > 1 - Brendon Beebe (11/7/2012)
         	// If this creates bugs in other places then we need to come up with a better solution
         	
           //return new Result(false, "If unit=count, amount must be 1");
         }
         return new Result(true, "You've got yourself a valid unit.");
     }
 
     /**
      * Renders the size to a string.
      */
     @Override
     public String toString(){
         return _amount + " " + _unit;
     }
 }
