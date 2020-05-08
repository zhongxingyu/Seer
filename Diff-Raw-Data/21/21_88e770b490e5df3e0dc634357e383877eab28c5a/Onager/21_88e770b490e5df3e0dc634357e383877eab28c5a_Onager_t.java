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
 public class Onager extends CardBase {
 
     public final static String NAME = "Onager";
     final static String TYPE = Card.BUILDING;
     final static String DESCRIPTION = "This Roman catapult attacks any opposing building. " +
             "The battle die is thrown once.";
     final static int COST = 5;
     final static int DEFENCE = 4;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
 
     @Override
     public CardHolder makeOne(PlayArea playArea){
         Card card = new Onager(playArea);
         CardHolder cardHolder = new CardHolder(card);
         card.setContainer(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea){
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         Card card;
 
         for(int i = 0; i < OCCURENCES; i++){
             card = new Onager(playArea);
             cardHolder = new CardHolder(card);
             card.setContainer(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
 
     Onager(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
 
     }
 
 
 //    public boolean activate(Player player, int position) {
 //        //TODO: refactor input to interface or player
 //        Scanner input = new Scanner(System.in);
 //        ArrayList<Integer> validInput = new ArrayList<Integer>();
 //        boolean inputValid = false;
 //        int chosenInput = -1;
 //
 //        boolean activated = true;
 //        DiceDiscs diceDiscs = playArea.getDiceDiscs();
 //        BattleManager battleManager = playArea.getBattleManager();
 //        int targetPlayerID = player.getOtherPlayerID();
 //
 //        CardHolder[] enemyCards = diceDiscs.getPlayerActives(targetPlayerID);
 //
 //        PlayerInterface.printOut("Available Targets:", true);
 //        for(int i = 0; i < enemyCards.length; i++){
 //            if(enemyCards[i] != null && enemyCards[i].getType() == Card.BUILDING){
 //                PlayerInterface.printOut((i + 1) + ") " + enemyCards[i].getName(), true);
 //                validInput.add(i + 1);
 //            } else {
 //                PlayerInterface.printOut((i + 1) + ") #", true);
 //            }
 //        }
 //
 //        while(!inputValid){
 //            chosenInput = input.nextInt();
 //            for(int number : validInput){
 //                if(chosenInput == number){
 //                    inputValid = true;
 //                }
 //            }
 //        }
 //        chosenInput--;
 //
 //        battleManager.battle(targetPlayerID, chosenInput);
 //
 //        return activated;
 //    }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         //TODO: fill in
     }
 
     //activationData: [targetIndex]
 
     @Override
     public void activate(Player player, int position) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
        BattleManager battleManager = playArea.getBattleManager();
         int targetPlayerID = player.getOtherPlayerID();
         ArrayList<Integer> activationData = player.getActivationData();
         int targetIndex = activationData.remove(0);
 
        battleManager.battle(targetPlayerID, targetIndex);
     }
 
     @Override
     public void clearWrappers() {
         //do nothing when discarded
     }
 }
