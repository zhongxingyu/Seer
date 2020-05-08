 package mediastore;
 
 /**
  * A class that encapsulates a music album.
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  */
 public class Album extends Media {
 
     //Default contructor
     public Album() {
         super();
     }
 
     /**
      * Initializer constructor
      *
      * @param author author of the album
      * @param title name of the album
      * @param duration length of the album (in seconds)
      * @param genre genre of the album
      * @param price price of the album
      * @param numSold number of albums sold (initialized to 0)
      */
     public Album( String author, String title, int duration, String genre, int rating, int totalReviews, double price, int numSold ) {
 
         super( author, title, duration, genre, rating, totalReviews, price, numSold );
 
     }
 
     public String toTextDBRepresentation() {
        return author + '\n' + title + '\n' + duration + '\n' + genre + '\n' + rating + '\n' + totalReviews + '\n' + price + '\n' + numSold;
     }
 }
