 package edu.ucsb.cs56.S13.lab04.Daized;
 
 /** Movie class which represents a film with its title and year of release
 
  */
 
 
 public class Movie{
 
     private String title; // name of movie
     private int yearOfRelease; //year that movie was released
 
     public Movie(){
	title = "";
 	yearOfRelease = 0;
     }
 
     public Movie(String a, int b){
 	title = a;
 	yearOfRelease = b;
     }
 
     public String getTitle(){
 	return title;
     }
 
     public int getYearOfRelease(){
 	return yearOfRelease;
     }
 
     public void setTitle(String title){
 	this.title = title;
     }
 
     public void setYearOfRelease(int yearOfRelease){
 	this.yearOfRelease = yearOfRelease;
     }
 
     public String toString(){
	return title + " (" + yearOfRelease + ")";
     }
 
     public boolean equals(Object o){
 
 	// boiler-plate for an equals method:
	if (o==null) {return false;}
 	if (! (o instanceof Movie) ) {return false;}
 	Movie m = (Movie) o;
 
 	if ( !(m.getTitle().equals(this.title)))
 	    return false;
 
 	if ( this.yearOfRelease != m.getYearOfRelease() )
 	    return false ;
 
 	return true;
     }
 
     public static void main(String [] args){
 
 	Movie ragingBull = new Movie("Raging Bull", 1980);
 
 	String c = ragingBull.toString();
 	System.out.println(c);
 
     }
 
 
 
 
 
 
 }
