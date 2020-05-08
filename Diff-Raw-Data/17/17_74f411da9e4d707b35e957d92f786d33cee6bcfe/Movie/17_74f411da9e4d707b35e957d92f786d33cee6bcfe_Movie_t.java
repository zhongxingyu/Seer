 package mediastore;
 
 /**
  * A class that encapsulates a movie.
  *
  * @author Ryan Smith
  */
 public class Movie extends Media {
 
     private int releaseYear; //year of movie's release
    private String director;
 
     public Movie() {
         //POST: Initializes data members to "" or 0
         super();
         releaseYear = 0;
     }
 
     public Movie( String author, String title, int duration, String genre, int ranking, double price, String director, int releaseYear ) {
         //PRE: Takes an author, title, duration, genre, ranking, price ( > 0 ), director of movie, and release year ( > 0 )
         //POST: Sets data members to respective values
         super( author, title, duration, genre, price );
         this.director = director;
         this.releaseYear = releaseYear;
     }
 
     //****************************************************************************
     //*                            Set and Get Methods                           *
     //****************************************************************************
     public void setDirector( String director ) {
         //PRE: Parameter takes a director name
         //POST: Sets director to indicated string
         this.director = director;
     }
 
     public String getDirector() {
         //POST: returns movie's director
         return director;
     }
     
     public Movie(String author, String title, int duration, String genre, double price, int releaseYear){
       //PRE: Takes an author (director), title, duration, genre, ranking, price ( > 0 ), and release year ( > 0 )
       //POST: Sets data members to respective values
       super(author, title, duration, genre, price);
       this.releaseYear = releaseYear;
     }
 
  //****************************************************************************
  //*                            Set and Get Methods                           *
  //****************************************************************************
     
     
     public void setReleaseYear(int releaseYear){
       //PRE: Parameter takes in year of movie release
       //POST: Release year is set to given value
         this.releaseYear = releaseYear;
     }
     
     
     public int getReleaseYear(){
       //POST: returns movie's release year
         return releaseYear;
     }
 
     protected Movie getFromID() { // only media should call this method
         return null;
     }
 }
