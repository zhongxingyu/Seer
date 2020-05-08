 package main;
 
 public class CheckNumberCommand implements Command{
     static boolean loggedIn = false;
     static String savedLibraryNumber = "";
 
     private static boolean loggedIn() {
         return loggedIn;
     }
 
     public void excute() {
         if (loggedIn()) {
             System.out.println("Your library number is " + savedLibraryNumber);
         } else {
             System.out.println("Please talk to Librarian. Thank you.");
         }
     }
 }
