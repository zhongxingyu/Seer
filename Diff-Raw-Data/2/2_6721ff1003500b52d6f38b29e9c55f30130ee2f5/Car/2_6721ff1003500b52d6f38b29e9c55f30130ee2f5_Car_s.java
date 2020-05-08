 package edu.ucsb.cs56.S13.lab04.sdowell;
 /** Car is a class to represent a Car
  @author Sam Dowell
  @version 05/03/2013 for lab04, cs56, S13
 */
 
class Car{
 
     String name; // Name of a car (e.g. Dodge Challenger)
     int year; // Year of car model (e.g. 2006)
     /** Default constructor
 	 Sets default dummy values to 2010 Dodge Challenger
     */
     public Car(){
 	name = "Dodge Challenger";
 	year = 2010;
     }
     /** Constructor
 	@param name Name of the car (e.g. Dodge Challenger)
 	@param year Year of car model (e.g. 2006)
     */
     public Car(String name, int year){
 	this.name = name;
 	this.year = year;
     }
     /**
 	@return name of car
     */
     public String getName(){
 
 	return name;
     }
     /**
 	@return year of car model
     */
     public int getYear(){
 	return year;
     }
     /** Setter for name
 	@param name Name of the car
     */
     public void setName(String name){
 	this.name = name;
     }
     /** Setter for year
 	@param year Year of car model
     */
     public void setYear(int year){
 	this.year = year;
     }
     /**
 	@return formatted string representation of attributes (e.g. 2006 Dodge Challenger)
     */
     public String toString(){
 	return this.year + " " + this.name;
     }
     /**
 	@param o Object to be compared to this
 	@return true if the name and year of o are equal to this
     */
     public boolean equals(Object o){
 	if(o == null)
 		return false;
 	else if(!(o instanceof Car))
 		return false;
 
 	Car c = (Car) o;
 	if(c.getName().equals(this.name) && c.getYear()==this.year)
 	    return true;
 	return false;
 	
     }
     public static void main(String args[]){
 	Car c = new Car("Honda CRV", 2004);
 	System.out.println(c.toString());
 	
     }
 } // class Car
