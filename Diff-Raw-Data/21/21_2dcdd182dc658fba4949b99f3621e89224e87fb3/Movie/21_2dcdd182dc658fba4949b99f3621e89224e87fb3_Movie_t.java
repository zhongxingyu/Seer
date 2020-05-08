 package mediastore;
 
 /**
  * A class that encapsulates a movie.
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  */
 public class Movie extends Media {
 
     protected int releaseYear;
 
     //Default constructor
     public Movie() {
         //POST: Initializes data members to "" or 0
         super();
     }
 
     /**
      * Initializer constructor
      *
      * @param author director of the movie
      * @param title title of the movie
      * @param duration length of the movie (in seconds)
      * @param genre genre of the movie
      * @param price price (in dollars) of the movie
      * @param releaseYear year movie was released
      * @param numSold numbers sold (initialized to 0)
      */
     public Movie( String author, String title, int duration, String genre, int rating, int totalReviews, double price, int numSold, int releaseYear ) {
         //PRE: Takes an author (director), title, duration, genre, ranking, price ( > 0 ), and release year ( > 0 )
         //POST: Sets data members to respective values
         super( author, title, duration, genre, rating, totalReviews, price, numSold );
         this.releaseYear = releaseYear;
     }
 
     //****************************************************************************
     //*                            Set and Get Methods                           *
     //****************************************************************************
     /**
      * Sets the release year of a movie
      *
      * @param releaseYear year of release
      */
     public void setReleaseYear( int releaseYear ) {
         //PRE: Parameter takes in year of movie release
         //POST: Release year is set to given value
         this.releaseYear = releaseYear;
     }
 
     /**
      * Returns the release year of the movie
      *
      * @return releaseYear
      */
     public int getReleaseYear() {
         //POST: returns movie's release year
         return releaseYear;
     }
 
     /**
      * Returns movie object when called from Media class
      *
      * @return null
      */
     protected Movie getFromID() { // only media should call this method
         return null;
     }
 
     /**
      * toString method to properly display in a text file
      *
      * @return toString
      */
     public String toTextDB() {
        return author + "\n" + title + "\n" + duration + "\n" + genre + "\n" + rating + "\n" + totalReviews + "\n" + price + "\n" + numSold + "\n" + releaseYear;
     }
 }
