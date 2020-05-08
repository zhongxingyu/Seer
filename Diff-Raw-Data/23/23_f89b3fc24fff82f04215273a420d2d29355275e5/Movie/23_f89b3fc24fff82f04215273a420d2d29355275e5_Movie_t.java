 /*
     Program: Movie.java
    Purpose: Provide an object that can be usd to represent a movie.
             A child class of the parent AbstractFilm.
 */
 
 public class Movie extends AbstractFilm {
 
     private int myOscars;
 
     public Movie(String name, String producer, String director, int year,
                 String[] actors, int length, int oscars) {
         super(name, producer, director, year, actors, length);
         myOscars = oscars;
     }
     public Movie(){
         super();
         myOscars = 0;
     }
 
     public void setOscars(int oscars){myOscars = oscars;}
     public int getOscars(){return myOscars;}
 
     public String toString(){
         return "The Movie " + super.toString() +
         "\tOscars: " + getOscars() + "\n";
     }
     public int awards(){
         return getOscars();
     }
 }
