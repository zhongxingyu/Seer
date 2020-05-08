 package Roma.Cards;
 
 import Roma.*;
 import Roma.PlayerInterfaceFiles.CancelAction;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Turris extends CardBase {
     private static int DEFENSE_SHIFT = 1;
     public final static String NAME = "Turris";
     final static String TYPE = Card.BUILDING;
     final static String DESCRIPTION = "As long as the Turris is face-up, the defence value of all the " +
             "player's other face-up cards increases by 1.";
     final static int COST = 6;
     final static int DEFENCE = 6;
     final static boolean ACTIVATE_ENABLED = false;
 
     public final static int OCCURENCES = 2;
     WrapperMaker wrapperMaker;
 
     @Override
     public CardHolder makeOne(PlayArea playArea){
         Card card = new Turris(playArea);
         CardHolder cardHolder = new CardHolder(card);
         card.setContainer(cardHolder);
 
         return cardHolder;
     }
 
     public static ArrayList<CardHolder> playSet(PlayArea playArea){
         ArrayList<CardHolder> set = new ArrayList<CardHolder>();
         CardHolder cardHolder;
         Card card;
 
         for(int i = 0; i < OCCURENCES; i++){
             card = new Turris(playArea);
             cardHolder = new CardHolder(card);
             card.setContainer(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
 
     Turris(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
     }
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         System.err.println("Turris being activated somehow!");
     }
 
     @Override
     public void activate(Player player, int position) {
         System.err.println("Turris being activated somehow!");
     }
 
     @Override
     public void enterPlay(Player player, int position) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         CardHolder[] friendlyCards = diceDiscs.getPlayerActives(player.getPlayerID());
         wrapperMaker = new WrapperMaker();
         wrapperMaker.setDefenseShift(DEFENSE_SHIFT);
 
         for(int i = 0; i < friendlyCards.length; i++){
            if(i != position && friendlyCards[i] != null){
                 wrapperMaker.insertWrapper(friendlyCards[i]);
             }
         }
 
         playArea.addToEnterPlayList(wrapperMaker);
     }
 
     @Override
     public void leavePlay() {
         ArrayList<Wrapper> wrapperList = wrapperMaker.getWrapperList();
 
         for(Wrapper wrapper : wrapperList){
             wrapper.deleteThisWrapper();
         }
 
         playArea.removeFromEnterPlayList(wrapperMaker);
         wrapperMaker.clearWrapperList();
         wrapperMaker = null;
         container.leavePlay();
     }
 
 }
