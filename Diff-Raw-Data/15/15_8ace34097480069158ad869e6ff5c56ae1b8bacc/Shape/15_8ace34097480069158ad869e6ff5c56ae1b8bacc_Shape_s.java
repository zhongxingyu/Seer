 
 package edu.ucsb.cs56.S13.lab04.gordoncheung;
 /**
    A class to represent a shape. Has variables for the name of the shape and the number of sides the shape has.
  */	
 public class Shape{
     public String name;
     public int sides;
     
 
     /**
        No-arg constructor creates a shape with null string name and 0 sides 
      */
     public Shape(){
     }
 
     /**
        Constructor with arguments
      */
     public Shape(String name, int sides){
 	this.name=name;
 	this.sides=sides;
     }
 
     /**
        setName method to set the name variable
      */
     public void setName(String name){
 	this.name=name;
     }
 
     /**
        setSides method to set the sides variable
      */
     public void setSides(int sides){
 	this.sides=sides;
     }
     
     /**
    getName returns the name 
      */
     public String getName(){
 	return this.name;
     }
     
     /**
        getSides returns the sides
      */
     public int getSides(){
 	return this.sides;
     }
 
     /**
       toString returns a string like "Name: Square, Sides: 4"
      */
     public String toString(){
 	return "Name: " + name + ", Sides: " + sides;
     }
 
     /** 
 	equals method compares two shape objects
      */
     public boolean equals(Object o) {
 
         if (!(o instanceof Shape))
             return false;
         Shape temp = (Shape) o;
 	if(temp.getName()==this.getName() && temp.getSides()==this.getSides())return true;
 	return false;
     }
 
     /**
        main creates an instance of the shape using the two-arg constructor and calls the toString method
      */
     public static void main(String args[]){
 	Shape square= new Shape("Square", 4);
 	square.toString();
     }
 
 
 
 
 }
