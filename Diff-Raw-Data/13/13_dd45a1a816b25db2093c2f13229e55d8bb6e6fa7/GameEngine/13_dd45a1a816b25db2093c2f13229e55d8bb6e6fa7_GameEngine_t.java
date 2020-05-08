 package com.game.pileon;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.util.Log;
 import android.view.Gravity;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * GameEngine
  * 
  * This is the default game. 4 colors (R G B Y) and ten cards of each (1 through
  * 10)
  * 
  * @author nscross, breeze4
  * 
  */
 public class GameEngine {
     
     public Deck Deck;
     
     private final static int NUMBEROFCOLORS = 4;
     private final static int NUMBEROFCARDSPERCOLOR = 10;
     private final static int NUMBEROFPILES = 3;
     private final static int NUMBEROFHANDS = 5;
     public final static int STARTINGTIMEBONUS = 10000;
     public final static int STARTINGBONUSMULTIPLIER = 1;
     
     public boolean isGameOver = false;
     
     private final static String CARDCOLORS[] = { "red", "green", "blue",
     "yellow" };
     
     private PointTracker mPointTracker;
     
     public ArrayList<Pile> Piles;
     public ArrayList<Hand> Hands;
     
     public GameEngine() {
         createDeck();
         setupGame();
         addPlaceholderCardsToDeck();
         isGameOver = false;
     }
     
     public GameEngine(SavedGame savedGameState) {
         Deck = savedGameState.saveDeck;
        Piles = new ArrayList<Pile>();
         
         for (int pileNdx = 0; pileNdx < NUMBEROFPILES; pileNdx++) {
             Piles.add(pileNdx, savedGameState.savePiles.get(pileNdx));
         }
        Hands = new ArrayList<Hand>();
         for (int handNdx = 0; handNdx < NUMBEROFHANDS; handNdx++) {
             Hands.add(handNdx, savedGameState.saveHands.get(handNdx));
             Hands.get(handNdx).setDeck(Deck);
             Hands.get(handNdx).setGameEngine(this);
         }
         
         setPointTracker(savedGameState.savePointTracker);
         
         isGameOver = false;
     }
     
     public Deck createDeck() {
         Deck = new Deck();
         String cardID = "";
         for (int colorNdx = 0; colorNdx < NUMBEROFCOLORS; colorNdx++) {
             for (int cardNdx = 0; cardNdx < NUMBEROFCARDSPERCOLOR; cardNdx++) {
                 cardID = CARDCOLORS[colorNdx] + Integer.toString(cardNdx + 1);
                 DefaultGameCard cardToAdd = new DefaultGameCard(
                         CARDCOLORS[colorNdx], cardNdx + 1, cardID);
                 Deck.AddCard(cardToAdd);
                 Log.i("PO CreateDeck", cardToAdd.toString());
             }
         }
         
         Deck.Shuffle();
         return Deck;
     }
     
     public void addPlaceholderCardsToDeck() {
         for (int cardNdx = 0; cardNdx < NUMBEROFHANDS; cardNdx++) {
             Deck.addToEnd(createPlaceholderCard());
         }
         
     }
     
     public Card createPlaceholderCard() {
         DefaultGameCard placeholder = new DefaultGameCard();
         Log.i("PO CreateDeck",
                 "adding placeholder card: " + placeholder.toString());
         return placeholder;
     }
     
     public void createPiles() {
         Piles = new ArrayList<Pile>();
         
         for (int pileNdx = 0; pileNdx < NUMBEROFPILES; pileNdx++) {
             Piles.add(pileNdx, new Pile(Deck.dealTopCard()));
         }
     }
     
     public void createHands() {
         Hands = new ArrayList<Hand>();
         
         for (int handNdx = 0; handNdx < NUMBEROFHANDS; handNdx++) {
             Hands.add(new Hand(Deck.dealTopCard()));
             Hands.get(handNdx).setDeck(Deck);
             Hands.get(handNdx).setGameEngine(this);
         }
     }
     
     public void setupGame() {
         createPiles();
         createHands();
     }
     
     public Card dealTopCard() {
         Card topCard = Deck.dealTopCard();
         
         return topCard;
     }
     
     private boolean moveAvailable() {
         if ((Piles == null) || (Hands == null) || (Piles.size() == 0)
                 || (Hands.size() == 0)) {
             return true;
         }
         
         boolean moveAvailable = false;
         
         for (int i = 0; i < NUMBEROFPILES; i++) {
             for (int j = 0; j < NUMBEROFHANDS; j++) {
                 if (Piles.get(i).isMoveLegal(Hands.get(j).mCard)) {
                     Log.i("PO Game Over", "comparing pile: " + Piles.get(i)
                             + " to hand: " + Hands.get(j));
                     moveAvailable = true;
                 }
                 
             }
         }
         Log.i("PO Game Over",
                 "any moves left?: " + Boolean.toString(moveAvailable));
         return moveAvailable;
     }
     
     @SuppressLint("ShowToast")
     public boolean isGameOver() {
         if (Deck.isEmpty() || !moveAvailable()) {
             isGameOver = true;
             
             CharSequence text = "No more moves available";
             
             MainGame.mToast = Toast.makeText(MainGame.getAppContext(), text, Toast.LENGTH_SHORT);
             MainGame.mToast.setGravity(Gravity.CENTER, 0, 0);
             makeLongToast();
         }
         
         Log.i("PO Game Over", "is game over?: " + Boolean.toString(isGameOver));
         
         return isGameOver;
     }
     
     public void setPointTracker(PointTracker pointTracker) {
         mPointTracker = pointTracker;
         
         for (int pileNdx = 0; pileNdx < NUMBEROFPILES; pileNdx++) {
             Piles.get(pileNdx).setPointTracker(mPointTracker);
         }
     }
     
     public ArrayList<Pile> getPileList() {
         return Piles;
     }
     
     public ArrayList<Hand> getHandList() {
         return Hands;
     }
     
     private void makeLongToast() {
         Thread t = new Thread(){
             public void run() {
                 int count = 0;
                 try {
                     while( MainGame.showToast && count < 5){
                         MainGame.mToast.show();
                         sleep(1850);
                         count++;
                     }
                     
                 } catch (Exception e){
                     Log.e("LongToast", "", e);
                 }
             }
         };
         t.start();
     }
     
 }
