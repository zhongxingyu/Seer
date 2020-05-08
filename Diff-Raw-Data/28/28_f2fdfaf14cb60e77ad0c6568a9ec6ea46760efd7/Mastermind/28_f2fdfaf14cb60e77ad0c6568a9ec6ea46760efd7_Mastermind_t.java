 package com.waldm.mastermind;
 
 import java.io.IOException;
 
 public class Mastermind {
     private final int codeLength;
     private final char[] alphabet;
     private final int maximumNumberOfGuesses;
     private final Player guesser;
     private final CodeCreator codeCreator;
 
     public Mastermind() {
         System.out.println("Welcome to Mastermind!");
         codeLength = askForCodeLength();
         alphabet = askForAlphabet();
         maximumNumberOfGuesses = askForMaximumNumberOfGuesses();
         codeCreator = askForCodeCreator();
         guesser = askForGuesser();
 
         play();
     }
 
     private Player askForGuesser() {
         System.out.println("Who will be guessing the code?");
         System.out.println("Enter H for human or C for computer");
         String guesserString;
         try {
             guesserString = Utils.readStringFromConsole();
         } catch (IOException e) {
             throw new RuntimeException("Unable to read from console");
         }
 
         if (guesserString.equals("H")) {
             return new HumanPlayer();
         } else {
             return new ComputerPlayer(codeLength, alphabet);
         }
     }
 
     private CodeCreator askForCodeCreator() {
         System.out.println("Who will be creating the code?");
         System.out.println("Enter H for human or C for computer");
         String codeCreatorString;
         try {
             codeCreatorString = Utils.readStringFromConsole();
         } catch (IOException e) {
             throw new RuntimeException("Unable to read from console");
         }
 
         if (codeCreatorString.equals("H")) {
             return new HumanCodeCreator();
         } else {
             return new ComputerCodeCreator(codeLength, alphabet);
         }
     }
 
     private int askForMaximumNumberOfGuesses() {
         System.out.println("What is the maximum number of guesses allowed? ");
         try {
             return Integer.parseInt(Utils.readStringFromConsole());
         } catch (IOException e) {
             throw new RuntimeException("Unable to read from console");
         }
     }
 
     private char[] askForAlphabet() {
         System.out.println("What letters/numbers can the code contain?");
         System.out.println("Enter the letters/numbers as a continuous string, with no spaces or commas");
         String alphabetString = null;
         try {
             alphabetString = Utils.readStringFromConsole();
         } catch (IOException e) {
             throw new RuntimeException("Unable to read from console");
         }
 
         char[] newAlphabet = new char[alphabetString.length()];
         for (int i = 0; i < alphabetString.length(); i++) {
             newAlphabet[i] = alphabetString.charAt(i);
         }
 
         return newAlphabet;
     }
 
     private int askForCodeLength() {
         System.out.println("How long should the code be? ");
         try {
             return Integer.parseInt(Utils.readStringFromConsole());
         } catch (IOException e) {
             throw new RuntimeException("Unable to read from console");
         }
     }
 
     private void play() {
        System.out.println("Game starting!");

        int numberOfGuessesLeft = maximumNumberOfGuesses;
        while (numberOfGuessesLeft > 0) {
            guesser.informNumberOfGuessesLeft(numberOfGuessesLeft);
             String guess = guesser.requestGuess(codeLength, alphabet);
 
             if (guess.length() != codeLength) {
                 guesser.informIncorrectLength(codeLength);
                 continue;
             }
 
             if (codeCreator.guessWasCorrect(guess)) {
                 guesser.informCorrectGuess();
                 System.exit(0);
             }
 
             guesser.informResult(codeCreator.calculateResult(guess));
            numberOfGuessesLeft--;
         }
 
         guesser.informGameOver(codeCreator.getCode());
     }
 
     public static void main (String[] args) {
         new Mastermind();
     }
 }
