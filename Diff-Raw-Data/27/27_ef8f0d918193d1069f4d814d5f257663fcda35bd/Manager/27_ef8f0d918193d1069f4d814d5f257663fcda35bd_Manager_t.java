 package mediastore;
 
 import java.util.Scanner;
 
 /**
  *
  * @author Cole Arnold and Ryan Smith
  */
 public class Manager {
 
     String password;
  
     public void add(){
         
         //Command Line prompt for manager to add desired item to Media store
         System.out.println("What media type do you want to add?");
         System.out.println("1. Album");
         System.out.println("2. Movie");
         System.out.println("3. Audiobook");
         Scanner input = new Scanner(System.in); //Scanner oject
         
         int choice = input.nextInt();
         switch(choice) {
             case 1:
          System.out.println("Enter the artist, album title, album duration, genre, and price respectively.");
          String artist = input.next();
          String albumTitle = input.next();
          int albumDuration = input.nextInt();
          String albumGenre = input.next();
          double albumPrice = input.nextDouble();
          Media newAlbum = new Album(artist, albumTitle, albumDuration, albumGenre, albumPrice);
          //store this new album in appropriate place (ask milton)
          
             break;
             case 2:   
          System.out.println("Enter the director, movie title, movie duration, genre, price, and release year respectively.");
          String director = input.next();
          String movieTitle = input.next();
          int movieDuration = input.nextInt();
          String movieGenre = input.next();
          double moviePrice = input.nextDouble();
          int releaseYear = input.nextInt();
          Media newMovie = new Movie(director, movieTitle, movieDuration, movieGenre, moviePrice, releaseYear);
          //sore this new movie in appropriate place (ask milton)
          
             break;
             case 3:
          System.out.println("Enter the author, book title, book duration, genre, and price respectively.");
          String author = input.next();
          String bookTitle = input.next();
          int bookDuration = input.nextInt();
          String bookGenre = input.next();
          double bookPrice = input.nextDouble();
          Media newAudioBooks = new AudioBooks(author, bookTitle, bookDuration, bookGenre, bookPrice);
          //store this new album in appropriate place (ask milton)
          
             break;
         }
     }   
 }
