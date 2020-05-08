 package edu.ucsb.cs56.S13.lab04.mlindsay56;
 
 /** Coffee class
 
 @author Marc Lindsay
 @version CS56, S13, lab04
 
Coffee represents a coffee beverage.
 */
 public class Coffee {
 	
 
     private String typeOfCoffee; //the name of the type of coffee
     private int height_inches; //the height of coffee drink served
 
     /** Default Constructor creates a Coffee with type Regular, height 10 inches */
     
     public Coffee(){
 	this.typeOfCoffee = "Regular";
 	this.height_inches = 10; 
     }
     
     /** 
		@param type of Coffee, (e.g. Latte, Espresso)
		@param height of beverage in inches (e.g. 10, 11, 5)
      */
     public Coffee(String typeOfCoffee, int height_inches){
     this.typeOfCoffee = typeOfCoffee;
 	this.height_inches = height_inches;
     }
     
 	/**
 		get the type of this coffee 
 		@return type of this coffee beverage
 	*/
     public String getTypeOfCoffee(){
 	return this.typeOfCoffee;
     }
     
 	/**
 		get the height of this coffee
 		@return height of this coffee beverage in inches
 	*/
     public int getHeight_inches(){
 	return this.height_inches;
     }
     
 	/**
 		set the type of this coffee
		@param type of coffee beverage
 	*/    
     public void setTypeOfCoffee(String typeOfCoffee){
 	this.typeOfCoffee = typeOfCoffee;
     }
 	
 	/**
 		set the height of this coffee
		@param height of coffee beverage in inches
 	*/
     public void setHeight_inches(int height_inches){
 	this.height_inches = height_inches;
     }
     
     /**
        Convert to a string representation.  Examples: "12 inch Latte", "5 inch Americano"
        @return String represntation of a coffee beverage.
      */
     public String toString(){
 	return String.format("%d inch %s",this.height_inches,this.typeOfCoffee);
      }
      
      /**
        Are the coffee beverages equal?
        @param o another object to compare 
        @return true if this object is the same this coffee object
      */
     public boolean equals(Object o){
 	double tol = 1; 
 	if (! (o instanceof Coffee) )
 	    return false;
 	Coffee other = (Coffee) o;
 	return (other.getTypeOfCoffee().equals(this.getTypeOfCoffee()) &
 	Math.abs(other.getHeight_inches() - this.getHeight_inches()) < tol); 
 	}
 	
 	/** 
 	A sample main that uses the class.
     */
 	public static void main(String [] args) { 
 	Coffee c = new Coffee("Latte",12);
 	System.out.println(c);
     }
 
 }
