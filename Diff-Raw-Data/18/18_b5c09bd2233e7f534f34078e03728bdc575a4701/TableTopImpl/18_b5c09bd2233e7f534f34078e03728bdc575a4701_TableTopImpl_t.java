 
 package oodpcourseworkpoker;
 
 /**
  *@author Anna Taylor
  * @author Greg Marshall
  */
 
 import javax.swing.JOptionPane;
 public class TableTopImpl implements TableTop {
     private Player humanPlayer;
     private Player dealerPlayer; 
     private Deck deck;
 
 
     @Override
     public void prepareTable() { // creates human and dealer players and takes user's name, and sets off the game
 
         String userName = JOptionPane.showInputDialog("Please enter your name: ");
         if (userName == null) { //In the event that the user clicks 'cancel'
                 userName = "Player";
         }
         humanPlayer = new HumanPlayer(userName);
         dealerPlayer = new DealerPlayer();
         System.out.println("**************************************************************************************");
         System.out.println("**************************************************************************************");
         System.out.println("******************WELCOME TO ANNA AND GREG'S 5 CARD STUD CHALLENGE********************");
         System.out.println("**************************************************************************************");
         System.out.println("**************************************************************************************");
         System.out.println("");
         System.out.println("");
         pause();
         System.out.println("Hi " + userName + ". Hope you're ready to get the cards in the air! Please wait....");
         pause();
         commenceGame();
     }
 
     @Override
     public void pause() {
         try {
         Thread.sleep(1800);
         }
         catch (InterruptedException ie)
         {
             ie.printStackTrace();
         }
     }
 
     @Override
     public void commenceGame() { // introduction to game and dealer (only at startup)
 
         System.out.println("Executing.....");
         pause();
         System.out.println("Ok let's play 5 Card Stud. If you don't know the rules.....");
         pause();
         System.out.println("You're probably going to lose a lot of money!");
         pause();
         System.out.println("Your dealer today is called " + dealerPlayer.getName() + ". " + dealerPlayer.getName() + 
                     " says hello and good luck.");
         pause();
         mainGameCycle();
 
     }
 
     @Override
     public void mainGameCycle(){
         boolean finished = false;
         while(!finished){  // main cycle runs on a loop until player opts to quit (below)
             deck = new DeckImpl(); // a new deck object is created at the start of each hand and shuffled
             clearHands(); // clears both hands of cards so they're ready for the new game
             deck.shuffleCards();
             dealCards();
 
             System.out.println("***********************************************************");
             System.out.println("Ok let's shuffle up and deal!");
             pause();                
             System.out.println("shuffling deck....");
             pause();
             System.out.println("dealing cards....");
             pause();
 
             System.out.println("Your hand is: ");
             humanPlayer.getHand().evaluateHand();
             System.out.println(humanPlayer.getHand().displayHand());
             System.out.println("You have: " + humanPlayer.getHand().getHandValue());
             pause();
             pause();
 
             discardAndReplace(); // discard and replace duties pulled out to another method
             pause();
             System.out.println("Your final 5-card hand is: ");
             humanPlayer.getHand().evaluateHand();
             System.out.println(humanPlayer.getHand().displayHand());
             System.out.println("You have: " + humanPlayer.getHand().getHandValue());
             pause();
             System.out.println("Finished player discard....computer is thinking");
             pause();
 
             dealerTurn(); //Initiates the program-controlled dealer's turn
             System.out.println("****SHOWDOWN!!**** ");
             pause();
             System.out.println(dealerPlayer.getHand().displayHand());
             System.out.println(dealerPlayer.getName() + "'s hand is: " + dealerPlayer.getHand().getHandValue());
             pause();
             System.out.println("Your hand is: " + humanPlayer.getHand().getHandValue());
             pause();
             pause();
 
             HandComparator handComparator = new HandComparatorImpl();
             handComparator.compareHands(humanPlayer, dealerPlayer); //Compares the hands of both players
             System.out.println(handComparator.getResult()); //Prints the result of the comparison (announces winner)
             pause();
 
             String cont = JOptionPane.showInputDialog("Would you like to play another hand? Enter Y to continue or N to exit: ");
             if (cont == null) {
                 cont = "N"; //N is assumed if the user clicks cancel
             }
             if (cont.contains("N")|| cont.equals("n")) {
                 finished = true;
                 System.out.println("Thanks for playing!");
             }
             else if (!cont.contains("Y") || !cont.contains("y")) { //If the user enters something other than Y or N
                 System.out.println("   I'll take that as a yes, then!   ");
             }
         }
     }
 
     @Override
     public void dealCards() { //Deals cards into the players' hands
         for (int i = 0; i < 5; i++) {
                 humanPlayer.receiveCard(deck.popCard());
                 dealerPlayer.receiveCard(deck.popCard());
             }
     }
 
     @Override
     public void discardAndReplace() { //Allows user to exchange cards
        int cardsToBin;
        try {
        cardsToBin = Integer.parseInt(JOptionPane.showInputDialog("How many cards would you like to discard? [Maximum 3] "));
        }
        catch (NumberFormatException numEx) { //If user clicks cancel, default is 0
                    cardsToBin = 0;
                    System.out.println("I'll assume from that you mean zero!");
         }
         while (cardsToBin > 3 || cardsToBin < 0) {
             System.out.println("You may only enter 0-3 cards to discard, please try again: ");
            cardsToBin = Integer.parseInt(JOptionPane.showInputDialog("How many cards would you like to discard? [Maximum 3] "));
         }
 
         /**
          * At this point whilst 'cardsToBin' is still positive, discardCard is called and passed the integer entered by the user. 
          * The hand is not resorted during this process so that the numbers of the cards does not change during the discard.
          */
         int cardDis = 0;
         int cardsToReplace = cardsToBin;
         while(cardsToBin > 0) {                    
             try {
                     cardDis = Integer.parseInt(JOptionPane.showInputDialog("Which card would you like to discard next? [Enter 1,2,3,4 or 5]"));
             }
             catch (NumberFormatException numEx) { //If user clicks cancel, default is 0
                     cardDis = 0;
                     break;
             }
             if(cardDis > 0 && cardDis < 6) {
                 humanPlayer.getHand().discardCard(cardDis);
                 cardsToBin--;
             }
             else {
                 System.out.println("You may only enter 1,2,3,4 or 5");
             }
         }
 
             for (int i = cardsToReplace; i > 0;i--) {
                     humanPlayer.receiveCard(deck.popCard());
             }
             if (cardDis > 0) { //Only print message below if user has opted to swap out cards
                     System.out.println("dealing your replacement cards......");
             }
     }
 
     @Override
     public void dealerTurn(){
         int dealerReplace = dealerPlayer.chooseDiscard();  //the dealer's automated discard decision begins
         for(int i = dealerReplace;i > 0;i--) {
             dealerPlayer.receiveCard(deck.popCard());
         }
         dealerPlayer.getHand().evaluateHand();
         System.out.println(dealerPlayer.getName() + " has discarded " + dealerReplace + " cards");
         pause();
         System.out.println(dealerPlayer.getName() + " has finished changing cards....and has: ");
         pause();
     }
 
     @Override
     public void clearHands(){ //For use at the end of each game cycle
         humanPlayer.getHand().clearHand();
         dealerPlayer.getHand().clearHand();
     } 
 
 
 
     public static void main(String[] args) {
 
         TableTopImpl test = new TableTopImpl();
         test.prepareTable();
     }
 
 }
