 package Roma.Cards;
 
 import Roma.*;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Scaenicus extends CardBase {
     public final static String NAME = "Scaenicus";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "He performs no action of his own but can copy the action of any of " +
             "the player's own face-up character cards, and the next time round that of another.";
     final static int COST = 8;
     final static int DEFENCE = 3;
     final static boolean ACTIVATE_ENABLED = true;
 
 
     public final static int OCCURENCES = 2;
 
     @Override
     public CardHolder makeOne(PlayArea playArea){
         Card card = new Scaenicus(playArea);
         CardHolder cardHolder = new CardHolder(card);
         card.setContainer(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea){
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         Card card;
 
         for(int i = 0; i < OCCURENCES; i++){
             card = new Scaenicus(playArea);
             cardHolder = new CardHolder(card);
             card.setContainer(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
     Scaenicus(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
 
     }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         ArrayList<Integer> activationData = player.getActivationData();
         CardHolder[][] activeCards = diceDiscs.getActiveCards();
         int playerID = player.getPlayerID();
         CardHolder card;
         int targetIndex;
 
         for(int i = 0; i < DiceDiscs.CARD_POSITIONS; i++){
             card = activeCards[playerID][i];
             if(card != null && card.getType().equalsIgnoreCase(Card.CHARACTER)){
                 card.setPlayable(true);
             }
         }
         PlayerInterface.printOut("Imitate which other character card of yours in play?", true);
         targetIndex = player.getDiceDiscIndex(activeCards, true, false);
         activationData.add(targetIndex);
        activeCards[playerID][targetIndex].gatherData(player, position);
     }
 
     //activationData: [targetIndex] + extra activation data for copied card
 
     @Override
     public void activate(Player player, int position) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         CardHolder[] friendlyCards = diceDiscs.getPlayerActives(player.getPlayerID());
         ArrayList<Integer> activationData = player.getActivationData();
         int targetIndex = activationData.remove(0);
 
         friendlyCards[targetIndex].activate(player, position);
     }
 }
