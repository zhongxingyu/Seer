 package edu.ucsb.cs56.S13.lab04.RapidRage;
 
 /** Videogame represents a videogame
  @author Chris Atanasian
  @version 2013/04/30
 */
 
 public class Videogame {
     private String name; // name of the videogame
     private int yearReleased; // year that the videogame was released
 
     /**
        default Constructor
     */
     public Videogame() {
 	// stub
     }
     
     /**
        Constructor
        @param name Name of the Videogame
        @param yearReleased The year the Videogame was released
     */
     public Videogame(String name, int yearReleased) {
	this.name = "abc"; // stub
	yearReleased = -42; // stub
     }
 
     /**
        get the name of the Videogame
        @return the name of the Videogame
     */
     public String getName() {
 	return null; // stub
     }
     
     /**
        get the year the Videogame was released
        @return the year the Videogame was released
     */
     public int getYearReleased() {
 	return -42; // stub
     }
 
     /**
        set the name of the Videogame
        @param name The name of the Videogame
     */
     public void setName(String name) {
	this.name = "abc"; // stub
     }
     
     /**
        set the year the Videogame was released
        @param yearReleased the year the Videogame was released
     */
     public void setYearReleased(int yearReleased) {
	this.yearReleased = -42; // stub
     }
 
     /**
        return String representation of Videogame
        it will be in this format: "name, yearReleased"
        @return the String representation of Videogame
     */
     public String toString() {
	return "lol"; // stub
     }
     
     /**
        check if two Videogames are equivalent, that is, if their name and yearReleased are the same
        @return whether or not two videogames are equivalent
     */
     public boolean equals() {
 	return false; // stub
     }
 
     /**
        execute the program
     */
     public static void main(String[] args) {
 	// stub
     }
     
 }
