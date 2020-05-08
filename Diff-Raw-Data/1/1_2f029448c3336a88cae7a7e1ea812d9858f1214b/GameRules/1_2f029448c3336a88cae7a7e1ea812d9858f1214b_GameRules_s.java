 package Roma;
 
 import Roma.Cards.Card;
 import Roma.Cards.CardHolder;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 
 /**
  * File Name: GameRules.java
  * Creator: Varun Nayyar
  * Date: 12/05/12
  * Desc: All the initial and regular global rules are run by this object
  */
 public class GameRules {
 
     private static final int CANCEL = PlayerInterface.CANCEL;
 
     private PlayArea playArea;
     Player[] players;
 
     public GameRules(PlayArea playArea){
         this.playArea = playArea;
         players =  playArea.getAllPlayers();
     }
 
     public void getAndSwapCards(){
         CardManager cardManager = playArea.getCardManager();
 
         ArrayList<CardHolder> initialSet = new ArrayList<CardHolder>();
         //stores the initial cards for the game
 
         for(int i = 0; i<Roma.MAX_PLAYERS;i++){
             initialSet.addAll(0, cardManager.drawNCards(Roma.NUM_INIT_CARDS));
         } //gets all the cards needed
 
         //cardManager.shuffle(initialSet);
         //To ensure some extra randomness?
 
         ArrayList<CardHolder> choices = new ArrayList<CardHolder>();
         //stores the choices of the previous player
 
         for (int i =0; i<Roma.MAX_PLAYERS;i++){
             choices.clear();
             //add prev choices
             ArrayList<CardHolder> individualHand = new ArrayList<CardHolder>();
 
             individualHand.addAll(initialSet.subList(
                     (i * Roma.NUM_INIT_CARDS), (i + 1) * Roma.NUM_INIT_CARDS));
             //extracts the first num-init-cards from the initial set
 
             PlayerInterface.printOut(players[i].getName() +
                     ", these are the " + Roma.NUM_INIT_CARDS + " cards dealt to you.\n" +
                     "You must choose " + Roma.NUM_CARDS_SWAPPED + " to give to your opponent.\n" +
                     "Choose the first Card", true);
             //Prompt: move printing to player interface?
 
 
             for(int j = 0, input = PlayerInterface.CANCEL; j<Roma.NUM_CARDS_SWAPPED;j++, input = PlayerInterface.CANCEL){
                 while(input == PlayerInterface.CANCEL){

                     try {
                         input = players[i].getCardIndex(individualHand,"");
                         choices.add(individualHand.remove(input));
                     } catch (CancelAction cancelAction) {
                         PlayerInterface.printOut("You must choose a card: ", true);
                     }
                 }
                 if(j!=Roma.NUM_CARDS_SWAPPED-1) PlayerInterface.printOut("Choose the next card:", true);
             }
             players[i].addCardListToHand(individualHand);
             players[(i+1)%Roma.MAX_PLAYERS].addCardListToHand(choices);
 
         }
     }
 
     public void layAllCardsInHand() {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
 
         Player activePlayer = null;
         ArrayList<CardHolder> hand = null;
         CardHolder[][] activeCards = diceDiscs.getActiveCards();
 
         int chosenCardIndex = CANCEL;
         int targetDisc;
 
         for(int i = 0; i < Roma.MAX_PLAYERS; i++){
             activePlayer = players[i];
             hand = activePlayer.getHand();
 
             while(!hand.isEmpty()){
                 playArea.printStats();
                 PlayerInterface.printOut(activePlayer.getName() + ", please lay all your cards", true);
                 chosenCardIndex = CANCEL;
                 while(chosenCardIndex == CANCEL){
                     try {
                         chosenCardIndex = activePlayer.getCardIndex(hand,"");
                     } catch (CancelAction cancelAction) {
                         PlayerInterface.printOut("You must choose a card", true);
                     }
                 }
                 targetDisc = CANCEL; // cancel value
                 while(targetDisc == CANCEL){
                     try {
                         targetDisc = activePlayer.getDiceDiscIndex(activeCards, false, false);
                     } catch (CancelAction cancelAction) {
                         PlayerInterface.printOut("You must choose a disc", true);
                     }
                 }
                 diceDiscs.layCard(activePlayer, targetDisc, hand.remove(chosenCardIndex));
             }
         }
     }
 
     public void deductVictoryTokens(int playerID) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         VictoryTokens victoryTokens = playArea.getVictoryTokens();
 
         Card[] friendlyCards = diceDiscs.getPlayerActives(playerID);
         int countNull = 0;
         for(Card card : friendlyCards){
             if(card == null){
                 countNull++;
             }
         }
         PlayerInterface.printOut("There are " + countNull + " empty slots, losing that many players", true);
         victoryTokens.playerToPool(playerID, countNull);
     }
 
 }
