 package mediastore;
 
 /**
  * A class that encapsulates an audio book.
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  */
 public class Audiobook extends Media {
 
     public Audiobook( String author, String title, int duration, String genre, int rating, int totalReviews, double price, int numSold ) {
 
         super( author, title, duration, genre, rating, totalReviews, price, numSold );
 
     }
 
     public String toTextDB() {
        return author + "\n " + title + "\n " + duration + "\n " + genre + "\n " + rating + "\n " + totalReviews + "\n " + price + "\n " + numSold;
     }
 }
