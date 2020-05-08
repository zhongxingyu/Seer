 package Roma.Cards;
 
 import Roma.PlayArea;
 import Roma.Player;
 import Roma.PlayerInterfaceFiles.CancelAction;
 import Roma.PlayerInterfaceFiles.PlayerInterface;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 19/05/12
  * Desc:
  */
 public class Kat extends CardBase {
 
     public final static String NAME = "Kat";
     final static String TYPE = Card.CHARACTER;
     final static String DESCRIPTION = "Mysterious and revered animal.  " +
             "Its haunting cry lifts the heart of all who hear it.  Has nine lives.";
     final static int COST = 5;
     final static int DEFENCE = 1;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
     private static final int NUMBER_OF_LIVES = 9;
 
     public Kat(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
     }
 
 
     @Override
     public void gatherData(Player player, int position) throws CancelAction {
         PlayerInterface.printOut("Meows when activated", true);
         player.commit();
     }
 
     //activationData: no data required
 
     @Override
     public void activate(Player player, int position) {
         PlayerInterface.printOut("Meow~ o^.^o", true);
     }
 
     @Override
     public void enterPlay(Player player, int position) {
         KatWrapperMaker katWrapperMaker = new KatWrapperMaker();
 
         for (int i = 0; i < NUMBER_OF_LIVES; i++) {
             katWrapperMaker.insertWrapper(cardHolder);
         }
     }
 
     @Override
     public CardHolder makeOne(PlayArea playArea) {
         CardBase card = new Kat(playArea);
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
             card = new Kat(playArea);
             cardHolder = new CardHolder(card, playArea);
             card.setContainer(cardHolder);
             card.setCardHolder(cardHolder);
             set.add(cardHolder);
         }
 
         return set;
     }
 
     @Override
     public String getDescription() {
         return DESCRIPTION + "(" + countLives() + " lives left)";
     }
 
     private int countLives() {
         int count = 0;
         Card container = getContainer();
 
         while (container.isWrapper()) {
             if (container.getName().equalsIgnoreCase(KatWrapper.NAME)) {
                 count++;
             }
         }
         return count;
     }
 
     private class KatWrapper extends Wrapper {
         public final static String NAME = "Kat Wrapper";
 
         public KatWrapper(Card card) {
             super(card);
             name = NAME;
         }
 
         @Override
         public void discarded(CardHolder[] playerActiveCards, int position) {
             deleteThisWrapper();
         }
     }
 
     private class KatWrapperMaker extends WrapperMaker {
         @Override
         public Wrapper insertWrapper(CardHolder card) {
             Wrapper wrapper = new KatWrapper(card);
             return wrapper;
         }
     }
 }
