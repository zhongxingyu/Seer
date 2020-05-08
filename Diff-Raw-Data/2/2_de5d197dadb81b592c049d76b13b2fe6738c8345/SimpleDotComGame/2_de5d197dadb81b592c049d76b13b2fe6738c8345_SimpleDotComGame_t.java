 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DotCom;
 
 /**
  *
  * @author halvorconsulting
  */
 public class SimpleDotComGame {
     
     public static void main(String[] args) {
 //        declare int to hold number of user guesses
         int numberOfGuesses = 0;
         
         GameHelper helper = new GameHelper();
         
 //        instantiate SimpleDotCom object
         SimpleDotCom theDotCom = new SimpleDotCom();
         
 //        compute a random number between 0 and 4 that will be the starting location cell position
         int randomNumber = (int) (Math.random() * 5);
 //        set location array with 3 consective numbers starting with random number
        int[] locations = {randomNumber, randomNumber + 1, randomNumber + 2};
 //        invoke setLocationCells() method on the SimpleDotCom 
         theDotCom.setLocationCells(locations);
 //        declare boolean variable representing state fo the game name isAlive, set to true
         boolean isAlive = true;
 //        while the dot com is still alive(isAlive == true)
         while(isAlive == true)
         {
             
         
 //          get user input from the command line
             String guess = helper.getUserInput("enter a number");
 //          check user guess
 //          invoke the checkYOurself() method on the on the SimpleDotCom instance
             String result = theDotCom.checkYourself(guess);
 //          increment numberOfGuesses variable
             numberOfGuesses++;
 //          check for dot com death
 //          if result is "kill"
             if(result.equals("kill"))
             {
 //            set isAlive to false (which means we won't enter the loop again)
                 isAlive = false;
 //            print the number of user guesses
                 System.out.println("You took " + numberOfGuesses + " guesses");
             }
 //          end if
 //        end while
         }
 //      
                 
     }
     
 }
