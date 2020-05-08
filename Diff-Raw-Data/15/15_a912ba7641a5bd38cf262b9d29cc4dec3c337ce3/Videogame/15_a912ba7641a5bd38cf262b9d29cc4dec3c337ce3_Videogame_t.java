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
 	this.name = "";
 	this.yearReleased = 0;
     }
     
     /**
        Constructor
        @param name Name of the Videogame
        @param yearReleased The year the Videogame was released
     */
     public Videogame(String name, int yearReleased) {
 	this.name = name;
 	this.yearReleased = yearReleased;
     }
 
     /**
        get the name of the Videogame
        @return the name of the Videogame
     */
     public String getName() {
 	return this.name;
     }
     
     /**
        get the year the Videogame was released
        @return the year the Videogame was released
     */
     public int getYearReleased() {
 	return this.yearReleased;
     }
 
     /**
        set the name of the Videogame
        @param name The name of the Videogame
     */
     public void setName(String name) {
 	this.name = name;
     }
     
     /**
        set the year the Videogame was released
        @param yearReleased the year the Videogame was released
     */
     public void setYearReleased(int yearReleased) {
 	this.yearReleased = yearReleased;
     }
 
     /**
        return String representation of Videogame
        it will be in this format: "name, yearReleased"
        @return the String representation of Videogame
     */
     public String toString() {
 	String output = this.name + ", " + this.yearReleased;
 	return output;
     }
     
     /**
        check if two Videogames are equivalent, that is, if their name and yearReleased are the same
        @return whether or not two videogames are equivalent
     */
    public boolean equals(Object o) {
        if (! (o instanceof Videogame) )
            return false;
        Videogame other = (Videogame) o;
        
	if (!this.name.equals(other.name)) {
	    return false;
	}
	if (this.yearReleased != other.yearReleased) {
	    return false;
	}
	
	return true;
     }
 
     /**
        execute the program
     */
     public static void main(String[] args) {
 	// stub
     }
     
 }
