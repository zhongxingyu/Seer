 package com.randombit.uskoci.game;
 
 import com.randombit.uskoci.card.dao.CardDAO;
 import com.randombit.uskoci.card.model.Card;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public interface GameController {
     public int getCurrentPlayerId();
 
     public int getNextPlayerId();
 
     public void setNextPlayersTurn(int playerId) throws ActionNotAllowedException;
 
     public List<Card> getCardsInTheDeck();
 
     public String resetGame();
 
     public boolean startGame(int numberOfPlayers);
 
     public void setCardDAO(CardDAO cardDAO);
 
     public List<Card> getPlayerCards(int playedId);
 
    public Card drawCard(int playerId) throws ActionNotAllowedException;
 
     public boolean getBeginningCardDrawn();
 
     public boolean isGameStarted();
 
     public int getNumberOfPlayersJoined();
 
     public List<Card> getDiscardPile();
 
     public List<Card> getResources(int playerId);
 
     public int getPlayersPoints(int playerId);
 
     public void playCard(int playerId, int cardId) throws ActionNotAllowedException;
 
     public boolean isResourceCardPlayed();
     
     public void discardCardFromPlayersHand(int cardId, int playerId);
     
     public void discardCardFromResourcePile(int cardId, int playerId);
 
     public void removeMultiplierFromResourcePile(int playerId, int cardId ) throws ActionNotAllowedException;
 
     public Card flipCardFaceUp();
 
     public LinkedList<Card> getCardStack();
 
 }
