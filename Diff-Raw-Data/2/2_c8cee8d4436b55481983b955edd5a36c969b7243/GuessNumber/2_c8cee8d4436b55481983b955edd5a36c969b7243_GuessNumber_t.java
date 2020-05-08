 import java.util.Random;
 import java.util.Scanner;
 
 public class GuessNumber {
 
   /**
    * The max range in the randomize method.
    */
   protected static int maxRange = 10;
 
   /**
    * The scanner.
    */
   private static Scanner scanner;
 
   public static void main(String[] args) {
 
     String userName = getUserName();
 
     System.out.println("Welcome " + userName
         + ", lets play a game. Guess a number bwtween 1 and 10.");
 
     int randomInt = getRandomNumber(maxRange);
 
     // Control if the user have entered the correct value.
     boolean passed = false;
     int tries = 0;
 
     while (tries < 3 && !passed) {
       int userGuess;
       try {
         userGuess = getUserGuess();
       }
       catch (Exception e) {
         System.out.println("Your guess must be a number between 1 and 10");
         continue;
       }
 
       if (userGuess == randomInt) {
         System.out.println("Correct " + userName + "! Random number = "
             + randomInt);
         passed = true;
         break;
       }
       else if (userGuess > randomInt) {
         System.out.println("Wrong! The random number is lower.");
       }
       else {
         System.out.println("Wrong! The random number is higher.");
       }
 
       scanner.next();
       tries++;
     }
 
     // If the user havent found the correct number after three tries,
     // print Game Over and what the random number were.
     if (!passed) {
       System.out.println("Game over!");
      System.out.println("The random number was " + randomInt);
     }
 
     // Close the scanner.
     scanner.close();
 
     System.exit(0);
   }
 
   /**
    * Get a random number (int) between 0 and [max]
    * 
    * @param max
    *          The highest number in the range.
    * 
    * @return An int with a random number in the specific range.
    */
   private static int getRandomNumber(int max) {
     Random r = new Random();
     int randomInt = r.nextInt(max);
     randomInt++;
 
     return randomInt;
   }
 
   /**
    * Get the user input.
    * 
    * @param text
    *          The text to print before the user input.
    * @return A Scanner object.
    */
   private static Scanner getUserInput(String text) {
     System.out.print(text);
 
     if (scanner == null) {
       scanner = new Scanner(System.in);
     }
 
     return scanner;
   }
 
   /**
    * Get the users guess.
    * 
    * @throws Exception
    */
   private static int getUserGuess() throws Exception {
     Scanner scanner = getUserInput("Guess a number: ");
 
     // Validate that we have an int.
     if (scanner.hasNextInt()) {
       int guess = scanner.nextInt();
       // Validate the range.
       if (guess > maxRange || guess < 1) {
         throw new Exception();
       }
 
       return guess;
     }
     else {
       throw new Exception();
     }
   }
 
   /**
    * Get the users name.
    */
   private static String getUserName() {
     // Scanner scanner = getUserInput("What is your name? ");
     // return scanner.nextLine();
 
     return System.getProperty("user.name");
   }
 
 }
