 package Implementers;
 
 import Roma.*;
 import Roma.Cards.*;
 import Roma.History.ActionData;
 import Roma.VictoryTokens;
 import framework.cards.Card;
 import framework.interfaces.GameState;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 14/05/12
  * Desc:
  */
 public class GameStateImplementer implements GameState{
     PlayArea playArea = new PlayArea("testing");
 
     /**
      * Get the current turn's player number
      * <p/>
      * <p>
      * This method will return an integer between 0 and
      * ({@link framework.Rules#NUM_PLAYERS NUM_PLAYERS} - 1), as
      * specified in the GameState interface.
      * </p>
      *
      * @return the number of the current player
      */
     @Override
     public int getWhoseTurn() {
         return playArea.getTurn()%Roma.MAX_PLAYERS;
     }
 
     /**
      * Set the current player.
      * <p/>
      * <p>
      * This method sets which player is currently having a turn. Valid
      * inputs are between 0 and ({@link framework.Rules#NUM_PLAYERS
      * NUM_PLAYERS} - 1) inclusive.
      * </p>
      *
      * @param player the player whose turn it will be
      */
     @Override
     public void setWhoseTurn(int player) {

     }
 
     /**
      * Gets the GameState's current deck.
      * <p/>
      * <p>
      * The current deck of the GameState is to be returned as a List of
      * Cards. The first item in the list is the next card that would be
      * drawn from the deck, and so on.
      * </p>
      *
      * @return the current GameState deck
      */
     @Override
     public List<Card> getDeck() {
         ArrayList<CardHolder> deck = playArea.getCardManager().getPlayingDeck();
         return convertToCardList(deck);
     }
 
 
     /**
      * Sets the GameState's current deck.
      * <p/>
      * <p>
      * The new deck of the GameState is to be given as a List of Cards.
      * The first item in the list is the next card that would be
      * drawn from the deck, and so on.
      * </p>
      *
      * @param deck the new deck of the GameState
      */
     @Override
     public void setDeck(List<Card> deck) {
         //TODO: Implement
     }
 
     /**
      * Gets the GameState's current discard pile.
      * <p/>
      * <p>
      * The current discard pile of the GameState is to be returned as a
      * List of Cards. The first item in the list is the most recently
      * discarded card, and so on.
      * </p>
      *
      * @return the current GameState discard pile
      */
     @Override
     public List<Card> getDiscard() {
         ArrayList<CardHolder> discard = playArea.getCardManager().getDiscardPile();
         return convertToCardList(discard);
     }
 
     /**
      * Sets the GameState's current discard pile.
      * <p/>
      * <p>
      * The current discard pile of the GameState is to be given as a
      * List of Cards. The first item in the list is the most recently
      * discarded card, and so on.
      * </p>
      *
      * @param discard the new discard pile of the GameState
      */
     @Override
     public void setDiscard(List<Card> discard) {
         //TODO: Implement
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * Gets a player's current Sestertii.
      * <p/>
      * <p>
      * The current Sestertii (money) of the specified player is returned
      * as an integer. Correct player indexing is discussed in the
      * GameState interface header.
      * </p>
      *
      * @param playerNum which player's Sestertii to return
      * @return the player's Sestertii
      */
     @Override
     public int getPlayerSestertii(int playerNum) {
         return playArea.getMoneyManager().getPlayerMoney(playerNum);
 
     }
 
     /**
      * Sets a player's current Sestertii.
      * <p/>
      * <p>
      * The new Sestertii (money) of the specified player is given
      * as an integer. Correct player indexing is discussed in the
      * GameState interface header.
      * </p>
      *
      * @param playerNum which player's Sestertii to set
      * @param amount    the quantity of Sestertii for the player to have
      */
     @Override
     public void setPlayerSestertii(int playerNum, int amount) {
         MoneyManager moneyManager = playArea.getMoneyManager();
         moneyManager.setStartingMoney(playerNum, amount);
     }
 
     /**
      * Gets a player's current Victory Points.
      * <p/>
      * <p>
      * The current Victory Points of the specified player are returned as
      * an integer. Correct player indexing is discussed in the
      * GameState interface header.
      * </p>
      *
      * @param playerNum which player's Victory Points to get
      * @return the player's Victory Points
      */
     @Override
     public int getPlayerVictoryPoints(int playerNum) {
         return playArea.getVictoryTokens().getPlayerTokens(playerNum);
     }
 
     /**
      * Gives a player VPs from the stockpile or give the stockpile VPs from a player.
      * <p/>
      * <p>
      * The new Victory Points of the specified player are given as an
      * integer. Correct player indexing is discussed in the GameState
      * interface header.
      * </p>
      * <p>
      * If the given amount is more than what the player already has,
      * then points need to be removed from the stockpile and given
      * to the player and vice versa.
      * </p>
      *
      * @param playerNum which player's Victory Points to set
      * @param points    the player's Victory Points
      */
     @Override
     public void setPlayerVictoryPoints(int playerNum, int points) {
         VictoryTokens victoryTokens = playArea.getVictoryTokens();
         victoryTokens.setPlayerTokens(playerNum, points);
     }
 
     /**
      * Gets the contents of a player's current Hand.
      * <p/>
      * <p>
      * The contents of the hand of the specified player is returned as an
      * unordered collection of Cards. Correct player indexing is
      * discussed in the GameState interface header.
      * </p>
      *
      * @param playerNum which player's hand cards to get
      * @return the contents of the player's hand
      */
     @Override
     public Collection<Card> getPlayerHand(int playerNum) {
         Player player= playArea.getPlayer(playerNum);
         return convertToCardList(player.getHand());
     }
 
     /**
      * Sets the contents of a player's current Hand.
      * <p/>
      * <p>
      * The contents of the hand of the specified player is given as an
      * unordered collection of Cards. Correct player indexing is
      * discussed in the GameState interface header.
      * </p>
      *
      * @param playerNum which player's hand cards to set
      * @param hand      the contents of the the player's hand
      */
     @Override
     public void setPlayerHand(int playerNum, Collection<Card> hand) {
         //TODO: Implement
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * Gets the cards currently laid on a player's dice discs.
      * <p/>
      * <p>
      * The cards on the specified player's dice discs are returned in an
      * array of length {@link framework.Rules#NUM_DICE_DISCS
      * NUM_DICE_DISCS}. The 0th index in the array represents the dice
      * disc of value 1. Dice discs with no card are returned with
      * Card.NOT_A_CARD as their value. Correct player indexing is
      * discussed in the GameState interface header.
      * </p>
      *
      * @param playerNum which player's dice disc contents to get
      * @return the cards currently on the player's dice discs
      */
     @Override
     public Card[] getPlayerCardsOnDiscs(int playerNum) {
         ArrayList<CardHolder> myDiscs = playArea.getDiceDiscs().toList(playerNum);
         return convertToCardArray(myDiscs);
     }
 
     /**
      * Sets the contents of a player's dice discs.
      * <p/>
      * <p>
      * The cards on the specified player's dice discs are given in an
      * array of length {@link framework.Rules#NUM_DICE_DISCS
      * NUM_DICE_DISCS}. The 0th index in the array represents the dice
      * disc of value 1. Dice discs with no card are returned with
      * Card.NOT_A_CARD as their value. Correct player indexing is
      * discussed in the GameState interface header.
      * </p>
      *
      * @param playerNum which player's cards to set
      * @param discCards the cards to be placed on the dice discs
      */
     @Override
     public void setPlayerCardsOnDiscs(int playerNum, Card[] discCards) {
         //TODo: Implement
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * Gets the current player's action dice values.
      * <p/>
      * <p>
      * The values of the current player's action dice are returned in an
      * array in unspecified order. Dice are to be referred to by their
      * value, <i>not</i> by their position in this array.
      * </p>
      *
      * @return the current player's dice
      */
     @Override
     public int[] getActionDice() {
         //TODO: Get this sorted out
         // currently implementing as get free die
         int currPlayer = getWhoseTurn();
         Player player = playArea.getPlayer(currPlayer);
         ArrayList<Dice> diceList = player.getFreeDice();
         int[] diceNums = new int[diceList.size()];
         int i = 0;
         for(Dice dice: diceList){
             diceNums[i] = dice.getValue();
             i++;
         }
         return diceNums;
     }
 
     /**
      * Sets the current player's action dice values.
      * <p/>
      * <p>
      * The values of the current player's action dice are given in an
      * array in unspecified order. Dice are to be referred to by their
      * value, <i>not</i> by their position in this array.
      * </p>
      *
      * @param dice the new values of the current player's dice
      */
     @Override
     public void setActionDice(int[] dice) {
         //todo: There is no function to do this
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * Returns the number of Victory Points not currently held by a
      * player.
      * <p/>
      * <p>
      * The number of victory points not held by any player are returned.
      * This method is included so that the total number of Victory
      * Points in a game can be tested.
      * </p>
      *
      * @return the number of Victory Points not held by any player
      */
     @Override
     public int getPoolVictoryPoints() {
         VictoryTokens victoryTokens = playArea.getVictoryTokens();
         return victoryTokens.getPoolTokens();  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * Returns true iff a game has been started AND the game has come to completion
      * otherwise return false.
      *
      * @return whether a game has come to completion
      */
     @Override
     public boolean isGameCompleted() {
         return playArea.isGameOver();
     }
 
     private Card[] convertToCardArray(ArrayList<CardHolder> cardDeck){
         ArrayList<Card> converted = convertToCardList(cardDeck);
         Card[] forReturn = new Card[converted.size()];
         int i=0;
         for(Card card: converted){
             forReturn[i] = card;
             i++;
         }
         return forReturn;
     }
 
     private ArrayList<Card> convertToCardList(ArrayList<CardHolder> cardDeck){
         ArrayList<Card> forAnswer = new ArrayList<Card>();
         for(CardHolder cardHolder: cardDeck){
             Card card = Card.valueOf(cardHolder.getName().toUpperCase());
             forAnswer.add(card);
         }
         return forAnswer;
     }
 }
