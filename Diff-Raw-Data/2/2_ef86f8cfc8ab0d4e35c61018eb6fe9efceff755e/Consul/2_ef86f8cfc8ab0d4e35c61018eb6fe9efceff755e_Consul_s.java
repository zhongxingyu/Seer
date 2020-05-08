 package Roma.Cards;
 
 import Roma.Dice;
 import Roma.PlayArea;
 import Roma.Player;
 import Roma.PlayerInterface;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Consul extends Card {
    public final static String NAME = "Gladiator";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "The score on an action die which has not yet been " +
             "used can be " +
             "increased or decreased by 1 point.";
     final static int COST = 3;
     final static int DEFENCE = 3;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
 
     public Consul(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
 
     }
 
 
     public boolean activate(Player player, int position) {
         PlayerInterface playerInterface = player.getPlayerInterface();
         final String strPrompt = "Would you like to...";
         final String strOption1 = "Increase the die?";
         final String strOption2 = "Decrease the die?";
         final String strOption3 = "Cancel";
         final int INCREASE = 1;
         final int DECREASE = 2;
         final int CANCEL = 3;
 
         int choice = 3;
         boolean validChoice = false;
 
         boolean activated = true;
         ArrayList<Dice> freeDice = player.getFreeDice();
         Dice chosenDice = null;
 
         if(freeDice.size() != 0){
             chosenDice = player.chooseDie(freeDice);
             if(chosenDice == null){
                 activated = false;
             } else {
                 while(!validChoice){
                     choice = playerInterface.readInput(strPrompt, strOption1, strOption2, strOption3);
                     if(choice == INCREASE){
                         if(chosenDice.getValue() == Dice.MAX_DIE_VALUE){
                             System.out.println("Can't increase die value over" + Dice.MAX_DIE_VALUE + "!");
                         } else {
                             chosenDice.incrementValue();
                             validChoice = true;
                         }
                     } else if(choice == DECREASE){
                         if(chosenDice.getValue() == Dice.MIN_DIE_VALUE){
                             System.out.println("Can't decrease die value over" + Dice.MIN_DIE_VALUE + "!");
                         } else {
                             chosenDice.decrementValue();
                             validChoice = false;
                         }
                     } else if(choice == CANCEL){
                         validChoice = true;
                         activated = false;
                     }
                 }
             }
         }
 
         return activated;
     }
 }
