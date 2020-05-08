 package Roma.History;
 
 
 import Implementers.GameStateImplementer;
 import Roma.Cards.CardFactory;
 import Roma.Cards.CardHolder;
 import Roma.*;
 import framework.cards.Card;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Andrew
  * Date: 5/21/12
  * Time: 11:17 AM
  * To change this template use File | Settings | File Templates.
  */
 public class TimeWarp {
     private final TurnHistory turnHistory;
     private final int timeReverse;
     private final int playerID;
     private final int position;
     private final String cardName;
     private final int lives;
     private final PlayArea playArea;
     private final GameStateImplementer gameStateImplementer;
     private CardFactory cardFactory;
 
     public TimeWarp(TurnHistory turnHistory, int timeReverse, int playerID, int position,
                     String cardName, int lives, PlayArea playArea){
 
         this.turnHistory = turnHistory;
         this.timeReverse = timeReverse;
         this.playerID = playerID;
         this.position = position;
         this.cardName = cardName;
         this.lives = lives;
         this.playArea = playArea;
         gameStateImplementer = new GameStateImplementer(playArea);
         cardFactory = new CardFactory(playArea);
     }
 
     //three things to preserve:
     //1)action dice
     //2)battle die
     //3)deck order
     //card will duplicate itself
     //save number of lives the Kats have
 
     //import Implementers.GameStateImplementer;
     //import framework.cards.Card;
 
     public void warpTime() throws TimeParadox {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         int currentTurn = turnHistory.getCurrentTurnNumber();
         int destinationTurn = currentTurn - timeReverse;
         ArrayList<PlayState> history = turnHistory.getHistory();
         PlayState jumpDestination = history.get(destinationTurn);
         ArrayList<PlayState> timeChunk = new ArrayList<PlayState>();
 
         ArrayList<String> deckData = jumpDestination.getDeckData();
         ArrayList<String> discardData = jumpDestination.getDiscardData();
         ArrayList<ArrayList<String>> hand = jumpDestination.getHand();
         String[][] discs = jumpDestination.getDiscs();
         int[][] lives = jumpDestination.getLives();
         int[] money = jumpDestination.getMoney();
         int[] victory = jumpDestination.getVictory();
         String[][][] oldFromPast = jumpDestination.getFromPast();
         int[][][] oldPastLives = jumpDestination.getPastLives();
         CardHolder[][][] newFromPast =
                 new CardHolder[Dice.MAX_DIE_VALUE][RomaGame.MAX_PLAYERS][DiceDiscs.CARD_POSITIONS];
         int[][][] newPastLives = new int[Dice.MAX_DIE_VALUE][RomaGame.MAX_PLAYERS][DiceDiscs.CARD_POSITIONS];
         CardHolder card;
 
         ArrayList<Card> cardList = new ArrayList<Card>();
         Card[] cardArray = new Card[DiceDiscs.CARD_POSITIONS];
         CardHolder[] playerActives;
 
         playArea.setTurn(destinationTurn);
 
         for(int i = 0; i < RomaGame.MAX_PLAYERS; i++){
             gameStateImplementer.setPlayerVictoryPoints(i, 1);
         }
 
         for(int i = 0; i < RomaGame.MAX_PLAYERS; i++){
             for(String cardName : hand.get(i)){
                 cardList.add(Card.valueOf(cardName.replaceAll(" ", "").toUpperCase()));
             }
             gameStateImplementer.setPlayerHand(i, cardList);
             cardList.clear();
 
             for(int j = 0; j < DiceDiscs.CARD_POSITIONS; j++){
                 cardArray[j] = Card.valueOf(discs[i][j].replaceAll("\\s", "").toUpperCase());
             }
             gameStateImplementer.setPlayerCardsOnDiscs(i, cardArray);
 
             playerActives = diceDiscs.getPlayerActives(i);
             for(int j = 0; j < DiceDiscs.CARD_POSITIONS; j++){
                 if(playerActives[j] != null){
                     while(playerActives[j].countLives() > lives[i][j]){
                         playerActives[j].discarded(i, j);
                     }
                 }
             }
 
             gameStateImplementer.setPlayerSestertii(i, money[i]);
             gameStateImplementer.setPlayerVictoryPoints(i, victory[i]);
         }
 
         for(String cardName : deckData){
             cardList.add(Card.valueOf(cardName.replaceAll(" ", "").toUpperCase()));
         }
         gameStateImplementer.setDeck(cardList);
         cardList.clear();
 
         for(String cardName : discardData){
             cardList.add(Card.valueOf(cardName.replaceAll(" ", "").toUpperCase()));
         }
         gameStateImplementer.setDiscard(cardList);
         cardList.clear();
 
         for(int i = 0; i < Dice.MAX_DIE_VALUE; i++){
             for(int j = 0; j < RomaGame.MAX_PLAYERS; j++){
                 for(int k = 0; k < DiceDiscs.CARD_POSITIONS; k++){
                     if(!oldFromPast[i][j][k].equalsIgnoreCase("")){
                         card = cardFactory.getCard
                                 (Card.valueOf(oldFromPast[i][j][k].replaceAll(" ", "").toUpperCase()).toString());
                     } else {
                         card = null;
                     }
                     newFromPast[i][j][k] = card;
                 }
             }
         }
 
         diceDiscs.setFromPastTime(newFromPast);
 
         for(int i = 0; i < Dice.MAX_DIE_VALUE; i++){
             for(int j = 0; j < RomaGame.MAX_PLAYERS; j++){
                 System.arraycopy(oldPastLives[i][j], 0, newPastLives[i][j], 0, DiceDiscs.CARD_POSITIONS);
             }
         }
         diceDiscs.setTimeLives(newPastLives);
 
         for(int i = 0; i <= timeReverse ; i++){
             timeChunk.add(0, history.remove(history.size() - 1));
         }
 
         timeLapse(currentTurn, timeChunk);
     }
 
 // TODO: Time paradoxes
 //    EMPTY DISC activating an Empty disc during replay causes a time paradox (TP).
 //    AESCULAPINUM/HARUSPEX if the previously chosen card from the discard pile in the replay is no longer in the discard pile then that is a TP
 //    CONSILIARIUS/MACHINA if the set of discs containing a character/building card previously is not the same as the set of discs containing character/building cards in the replay then that is a TP.
 //    SCAENICUS if the disc previously selected does not contain the same card on replay then that is a TP
 //    TEMPLUM if on replay there is a templum next to a forum, and that templum was not activated prior to the time travel, then it is likewise not activated in the replay, and there is no TP.  On the other hand if previously a templum was on a disc next to an activated forum and was used to obtain victory points with an action die, but in the replay a templum is no longer on that disc then that is a TP.
 //    KAT kats are activated every turn, so you can't land on them when time travelling (except with another KAT).
 
     public void timeLapse(int currentTurn, ArrayList<PlayState> timeChunk) throws TimeParadox{
         PlayState theTurn;
         ArrayList<ActionData> actionHistory;
         ActionData currentAction;
         int[] actionDiceValues;
         PlayState newPlayState;
 
         theTurn = timeChunk.remove(0);
         actionDiceValues = theTurn.getActionDice();
         gameStateImplementer.setActionDice(actionDiceValues);
         actionHistory = theTurn.getActionHistory();
         newPlayState = playArea.autoTurnStart();
         insertTimeTraveller();
         while(!actionHistory.isEmpty()){
             currentAction = actionHistory.remove(0);
            //TODO: check for time paradoxes
             playArea.autoAction(newPlayState, currentAction);
         }
         playArea.autoTurnEnd(newPlayState);
 
         while(playArea.getTurn() != currentTurn){
             theTurn = timeChunk.remove(0);
             actionDiceValues = theTurn.getActionDice();
             gameStateImplementer.setActionDice(actionDiceValues);
             actionHistory = theTurn.getActionHistory();
             newPlayState = playArea.autoTurnStart();
             while(!actionHistory.isEmpty()){
                 currentAction = actionHistory.remove(0);
                 //TODO: check for time paradoxes
                 playArea.autoAction(newPlayState, currentAction);
             }
             playArea.autoTurnEnd(newPlayState);
         }
 
         theTurn = timeChunk.remove(0);
         actionDiceValues = theTurn.getActionDice();
         gameStateImplementer.setActionDice(actionDiceValues);
     }
 
     private void insertTimeTraveller() {
         Player player = playArea.getPlayer(playerID);
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         CardHolder card = cardFactory.getCard
                 (Card.valueOf(cardName.replaceAll(" ", "").toUpperCase()).toString());
         if(!diceDiscs.getTargetCard(player, position).getName().equalsIgnoreCase(card.getName())){
             diceDiscs.layCard(player, position, card);
         }
         while(card.countLives() > lives){
             card.discarded(playerID, position);
         }
     }
 }
