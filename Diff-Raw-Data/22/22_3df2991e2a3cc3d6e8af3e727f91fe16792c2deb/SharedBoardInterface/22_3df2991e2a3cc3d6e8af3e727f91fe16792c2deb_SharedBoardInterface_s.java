 /*
 * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
 *
 * Licensed under the Academic Free License v. 3.0.
 * http://www.opensource.org/licenses/afl-3.0.php
 */
 
 
 /**
  * @author awaterma@ecosur.mx
  */
 package mx.ecosur.multigame.ejb.interfaces;
 
 import java.util.Collection;
 
 import mx.ecosur.multigame.exception.InvalidMoveException;
 import mx.ecosur.multigame.exception.InvalidSuggestionException;
 import mx.ecosur.multigame.model.interfaces.ChatMessage;
 import mx.ecosur.multigame.model.interfaces.Game;
 import mx.ecosur.multigame.model.interfaces.Move;
 import mx.ecosur.multigame.model.interfaces.Suggestion;
 
 public interface SharedBoardInterface {
     /**
      * Gets a game by its id
      *
      * @param gameId
      * @return Game
      */
     public Game getGame(int gameId);
 
     /**
     * Shares a game interfaces for use by connected clients.
     *
     * @param gameImpl
     */
    public void shareGame (Game gameImpl);

    /**
      * Makes the specified move on the game grid and invokes the set of game
      * rule's affiliated with the Move's game.
      *
      * @param game
      * @param move
      * @throws InvalidMoveException
      * @return Move
      */
     public Move doMove (Game game, Move move) throws InvalidMoveException;
 
     /**
      * Gets all the moves for a given game.
      *
      * @param gameId
      * @return a list of all moves made in the game in the order that they were
      *         made
      */
     public Collection<Move> getMoves(int gameId);
 
     /**
      * Adds a chat message
      *
      * @param chatMessage
      */
     public ChatMessage addMessage(ChatMessage chatMessage);
 
     /**
      * Updates a move (with user input information, for example)
      *
      * @param move
      * @return Move
      */
     public Move updateMove (Move move);
 
     /**
      * Makes a suggestion to be evaluated by playing members of the game.
      *
      *
      */
     public Suggestion makeSuggestion (Game game, Suggestion suggestion) throws InvalidSuggestionException;
 
 }
