 package Roma.Cards;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 30/03/12
  * Desc:
  */
 
 import Roma.*;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 
 public class Mercator extends CardBase {
     public final static String NAME = "Mercator";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "For 2 sestertii each, the player can buy 1 victory point" +
             " from their opponent as long as there are money and victory points left!" +
             "The opponent gets the money.";
     final static int COST = 7;
     final static int DEFENCE = 2;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 1;
 
     @Override
     public CardHolder makeOne(PlayArea playArea){
         Card card = new Mercator(playArea);
         CardHolder cardHolder = new CardHolder(card);
         card.setContainer(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea){
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         Card card;
 
         for(int i = 0; i < OCCURENCES; i++){
             card = new Mercator(playArea);
             cardHolder = new CardHolder(card);
             card.setContainer(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
     Mercator(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
     }
 
 //    public boolean activate(Player player, int position) {
 //        final String STR_PROMPT = "How many Victory Points would you like from your opponent? (max 3 or 0 to cancel): ";
 //        final int MAX_PURCHASE = 3;
 //        final int MIN_PURCHASE = 1;
 //
 //        boolean activated = true;
 //        Scanner input = new Scanner(System.in);
 //        MoneyManager moneyManager = playArea.getMoneyManager();
 //        VictoryTokens victoryTokens = playArea.getVictoryTokens();
 //        boolean validInput = false;
 //
 //        //playArea.getMoneyManager().loseMoney(player, COST);//or super.getCost;
 //        int numTokensRead;
 //
 //        PlayerInterface.printOut(STR_PROMPT, true);
 //
 //        while(!validInput){
 //            numTokensRead = input.nextInt();
 //            if(numTokensRead <= MAX_PURCHASE && numTokensRead >= MIN_PURCHASE){
 //                if (
 //                }
 //            } else if (numTokensRead == 0) {
 //                validInput = true;
 //                activated = false;
 //            } else {
 //                PlayerInterface.printOut("Please give a valid number", true);
 //            }
 //        }
 //
 //        return activated;
 //    }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         int numberOfTokens;
         PlayerInterface playerInterface = playArea.getPlayerInterface();
 
         PlayerInterface.printOut("Buy tokens from your opponent for 2 money each", true);
        numberOfTokens = playerInterface.getIntegerInput()
         //TODO: fill in
     }
 
     //activationData: [numberOfTokens]
 
     @Override
     public void activate(Player player, int position) {
         MoneyManager moneyManager = playArea.getMoneyManager();
         VictoryTokens victoryTokens = playArea.getVictoryTokens();
         ArrayList<Integer> activationData = player.getActivationData();
         int numTokens = activationData.remove(0);
         int targetPlayerID = player.getOtherPlayerID();
 
         moneyManager.transferMoney(player.getPlayerID(), targetPlayerID, 2 * numTokens);
         victoryTokens.playerToPlayer(targetPlayerID, player.getPlayerID(), numTokens);
     }
 
     @Override
     public void enterPlay(Player player, int position) {
         //no enter play action
     }
 
     @Override
     public void leavePlay() {
         //do nothing when leaving play
     }
 }
