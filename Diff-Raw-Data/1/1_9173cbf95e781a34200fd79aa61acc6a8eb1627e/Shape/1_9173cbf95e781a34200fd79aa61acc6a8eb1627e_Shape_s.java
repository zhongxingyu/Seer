 
 package edu.ucsb.cs56.S13.lab04.gordoncheung;
 
 	
 public class Shape{
     public String name;
     public int sides;
     
     public Shape(){
     }
     public Shape(String name, int sides){
 	this.name=name;
 	this.sides=sides;
     }
 
     public void setName(String name){
 	this.name=name;
     }
     public void setSides(int sides){
 	this.sides=sides;
     }
     public String getName(){
 	return name;
     }
     public int getSides(){
 	return sides;
     }
 
     public String toString(){
 	return "Name: " + name + ", Sides: " + sides;
     }
     public boolean equals(Object o) {
 
         if (!(o instanceof Shape))
             return false;
         Shape temp = (Shape) o;
 	if(temp.getName()==this.getName() && temp.getSides()==this.getSides())return true;
 	return false;
     }
 
 
     public static void main(String args[]){
     }
 
 
 
 
 }
