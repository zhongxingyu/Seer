 package Roma.Cards;
 
 import Roma.Dice;
 import Roma.PlayArea;
 import Roma.Player;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Consul extends CardBase {
     public final static String NAME = "Consul";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "The score on an action die which has not yet been " +
             "used can be increased or decreased by 1 point.";
     final static int COST = 3;
     final static int DEFENCE = 3;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
 
     @Override
     public CardHolder makeOne(PlayArea playArea) {
         CardBase card = new Consul(playArea);
         CardHolder cardHolder = new CardHolder(card, playArea);
         card.setContainer(cardHolder);
         card.setCardHolder(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea) {
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         CardBase card;
 
         for (int i = 0; i < OCCURENCES; i++) {
             card = new Consul(playArea);
             cardHolder = new CardHolder(card, playArea);
             card.setContainer(cardHolder);
             card.setCardHolder(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
     Consul(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
 
     }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         final int INCREASE = 1;
         final int DECREASE = 2;
         final String strPrompt = "Would you like to...";
         final String strOption1 = "increase die value?";
         final String strOption2 = "decrease die value?";
         PlayerInterface playerInterface = playArea.getPlayerInterface();
         ArrayList<Integer> activationData = player.getActivationData();
         ArrayList<Dice> freeDice = new ArrayList<Dice>();
         freeDice.addAll(player.getFreeDice());
         int dieIndex = player.getCurrentAction().getActionDiceIndex();
         int option = CANCEL;
         int chosenDieValue;
         boolean validInput = false;
 
         freeDice.remove(dieIndex);
         PlayerInterface.printOut("Which die do you want to change?", true);
         if (freeDice.isEmpty()) {
             PlayerInterface.printOut("No free action dice!", true);
             player.cancel();
         }
         dieIndex = player.getDieIndex(freeDice);
         chosenDieValue = freeDice.get(dieIndex).getValue();
         while (!validInput) {
             option = playerInterface.readInput(strPrompt, true, strOption1, strOption2);
             if (option == CANCEL) {
                 player.cancel();
             } else if (option == INCREASE && chosenDieValue < 6) {
                 option = 1;
             } else if (option == DECREASE && chosenDieValue > 1) {
                 option = -1;
             } else {
                 PlayerInterface.printOut("Dice values have to stay between 1 and 6", true);
             }
         }
 
         activationData.add(dieIndex);
         activationData.add(option);
         player.commit();
     }
 
     //activation data: [dieIndex][+1/-1]
 
     @Override
     public void activate(Player player, int position) {
         ArrayList<Integer> activationData = player.getActivationData();
         int dieIndex = activationData.remove(0);
         int modValue = activationData.remove(0);
         ArrayList<Dice> freeDice = player.getFreeDice();
         if (modValue == 1) {
             freeDice.get(dieIndex).incrementValue();
         } else if (modValue == -1) {
             freeDice.get(dieIndex).decrementValue();
         } else {
             System.err.println("Error with Consul data");
             assert (false);
         }
     }
 }
