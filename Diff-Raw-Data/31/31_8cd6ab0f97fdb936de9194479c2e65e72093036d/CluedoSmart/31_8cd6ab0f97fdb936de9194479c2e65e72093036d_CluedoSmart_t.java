 /*
  * Copyright 2013 Victor Denisov
  *
  * This file is part of Cluedo Assistant.
  *
  * Cluedo Assistant is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Cluedo Assistant is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Cluedo Assistant.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.cluedoassist;
 
 import java.util.*;
 
 import java.io.Serializable;
 
 import java.util.Collections;
 
 public class CluedoSmart extends CluedoDumb {
 
     static final long serialVersionUID = 1L;
 
     private Resolution[][] backupTable;
 
     private ArrayList<LogEntry> backupLog;
 
     public CluedoSmart(CardSet cs, List<String> players) {
         super(cs, players);
 
         try {
             solvePlayerHasAllCards(OUT_COL);
         } catch (ContradictionException e) {
             // no contradiction is possible in this case
         }
     }
 
     public void setCard(String asker, Card card) throws UnknownPlayerException
                                                       , UnknownCardException
                                                       , ContradictionException {
         beginTransaction();
         try {
             super.setCard(asker, card);
             inferenceCycle();
         } catch (UnknownPlayerException upe) {
             rollBackTransaction();
             throw upe;
         } catch (UnknownCardException uce) {
             rollBackTransaction();
             throw uce;
         } catch (ContradictionException ce) {
             rollBackTransaction();
             throw ce;
         }
         commitTransaction();
     }
 
     public void makeTurn(Suggestion l) throws UnknownPlayerException
                                             , UnknownCardException
                                             , ContradictionException {
         beginTransaction();
         try {
             super.makeTurn(l);
             inferenceCycle();
         } catch (UnknownPlayerException upe) {
             rollBackTransaction();
             throw upe;
         } catch (UnknownCardException uce) {
             rollBackTransaction();
             throw uce;
         } catch (ContradictionException ce) {
             rollBackTransaction();
             throw ce;
         }
         commitTransaction();
     }
 
     public void makeAccusation(Accusation a) throws UnknownPlayerException
                                                   , UnknownCardException
                                                   , ContradictionException {
         beginTransaction();
 
         try {
             super.makeAccusation(a);
             inferenceCycle();
         } catch (UnknownPlayerException upe) {
             rollBackTransaction();
             throw upe;
         } catch (UnknownCardException uce) {
             rollBackTransaction();
             throw uce;
         } catch (ContradictionException ce) {
             rollBackTransaction();
             throw ce;
         }
         commitTransaction();
     }
 
     public List<CardReply> possibleCardReplies( String replier
                                               , Card[] askedCards
                                               ) throws UnknownPlayerException
                                                      , UnknownCardException {
         int playerNumber = playerOrd(replier);
         ArrayList<CardReply> result = new ArrayList<CardReply>();
         if (hasNoneOf(playerNumber, askedCards)) {
             result.add(CardReply.NoCard());
         }
         if (hasNonNegativeOf(playerNumber, askedCards)) {
             result.add(CardReply.UnknownCard());
         }
         for (Card c : askedCards) {
             if (table[cardSet.ordinal(c)][playerNumber] != Resolution.Minus) {
                 result.add(CardReply.ActualCard(c));
             }
         }
         return Collections.unmodifiableList(result);
     }
 
     private boolean hasNoneOf(int playerNumber, Card[] cards)
                                                   throws UnknownCardException {
         for (Card c : cards) {
             if (table[cardSet.ordinal(c)][playerNumber] == Resolution.Plus) {
                 return false;
             }
         }
         return true;
     }
 
     private boolean hasNonNegativeOf(int playerNumber, Card[] cards)
                                                 throws UnknownCardException {
         for (Card c : cards) {
             if (table[cardSet.ordinal(c)][playerNumber] != Resolution.Minus) {
                 return true;
             }
         }
         return false;
     }
 
     private void beginTransaction() {
         backupTable = new Resolution[table.length][];
         for (int i = 0; i < table.length; ++i) {
             backupTable[i] = new Resolution[table[i].length];
             for (int j = 0; j < table[i].length; ++j) {
                 backupTable[i][j] = table[i][j];
             }
         }
         backupLog = new ArrayList<LogEntry>(log);
     }
 
     private void commitTransaction() {
         backupTable = null;
         backupLog = null;
     }
 
     private void rollBackTransaction() {
         table = new Resolution[backupTable.length][];
         for (int i = 0; i < backupTable.length; ++i) {
             table[i] = new Resolution[backupTable[i].length];
             for (int j = 0; j < backupTable[i].length; ++j) {
                 table[i][j] = backupTable[i][j];
             }
         }
         backupTable = null;
         log = new ArrayList<LogEntry>(backupLog);
         backupLog = null;
     }
 
     private boolean setPlus(int cardNumber, int playerNumber)
                                                 throws ContradictionException {
         if (table[cardNumber][playerNumber] == Resolution.Minus) {
             Card card = cardSet.cards.get(cardNumber);
             String player = getCompartments().get(playerNumber);
             throw new ContradictionException("Card : " + card
                                          + ", Player : " + player
                                          + ". Expected Unknown or Plus, "
                                            + "encountered Minus.");
         }
         boolean tableModified = false;
         if (table[cardNumber][playerNumber] == Resolution.Unknown) {
             tableModified = true;
         }
         table[cardNumber][playerNumber] = Resolution.Plus;
         for (int i = 0; i < table[cardNumber].length; ++i) {
             if (i != playerNumber) {
                 boolean setMinusValue = setMinus(cardNumber, i);
                 tableModified = tableModified || setMinusValue;
             }
         }
         return tableModified;
     }
 
     private boolean setMinus(int cardNumber, int playerNumber)
                                                 throws ContradictionException {
         if (table[cardNumber][playerNumber] == Resolution.Plus) {
             Card card = cardSet.cards.get(cardNumber);
             String player = getCompartments().get(playerNumber);
             throw new ContradictionException("Card : " + card
                                          + ", Player : " + player
                                          + ". Expected Unknown or Minus, "
                                            + "encountered Plus.");
         }
 
         boolean tableModified = false;
         if (table[cardNumber][playerNumber] == Resolution.Unknown) {
             tableModified = true;
         }
         table[cardNumber][playerNumber] = Resolution.Minus;
         return tableModified;
     }
 
     private boolean processLog() throws UnknownPlayerException
                                       , UnknownCardException
                                       , ContradictionException {
         boolean tableModified = false;
         for (LogEntry logEntry : log) {
             if (logEntry instanceof Accusation) {
                 boolean doAccusation = solveAskerHasNoCardsFromAccusation((Accusation)logEntry);
                 boolean solveAccusationValue = solveUnsuccessfulAccusation((Accusation)logEntry);
                 tableModified = tableModified || solveAccusationValue;
                 tableModified = tableModified || doAccusation;
             } else if (logEntry instanceof SetCard) {
                 SetCard s = (SetCard) logEntry;
                 int playerNumber = playerOrd(s.player);
                 int cardNumber = cardSet.ordinal(s.card);
                 setPlus(cardNumber, playerNumber);
             } else {
                 Suggestion suggestion = (Suggestion) logEntry;
                 boolean solveRepliersHaveValue = solveRepliersHave(suggestion);
                 boolean solveReplierHasNoCardsValue = solveReplierHasNoCards(suggestion);
                 boolean solveOnlyOneUnknownValue = solveOnlyOneUnknown(suggestion);
                 boolean solveThreeCardsReplied = solveThreeCardsReplied(suggestion);
 
                 tableModified = tableModified || solveRepliersHaveValue;
                 tableModified = tableModified || solveReplierHasNoCardsValue;
                 tableModified = tableModified || solveOnlyOneUnknownValue;
                 tableModified = tableModified || solveThreeCardsReplied;
             }
         }
         return tableModified;
     }
 
     private boolean rectifyTable() throws ContradictionException {
         boolean tableModified = false;
         for (int i = 0; i < table[0].length; ++i) {
             boolean solvePlayerHasAllCardsValue = solvePlayerHasAllCards(i);
             tableModified = tableModified || solvePlayerHasAllCardsValue;
         }
         for (int i = 0; i < table[0].length; ++i) {
             boolean solveCountOfNonnegativeEqualsCardCountValue =
                                       solveCountOfNonnegativeEqualsCardCount(i);
             tableModified = tableModified || solveCountOfNonnegativeEqualsCardCountValue;
         }
         for (int i = 0; i < table.length; ++i) {
             boolean solveLineOneNonNegativeValue = solveLineOneNonNegative(i);
             tableModified = tableModified || solveLineOneNonNegativeValue;
         }
         boolean solveEnvHasOneUnknownCardInGroup06Value =
                                         solveEnvHasOneUnknownCardInGroup
                                                        ( 0
                                                        , cardSet.suspectCount);
         tableModified = tableModified || solveEnvHasOneUnknownCardInGroup06Value;
 
         boolean solveEnvHasOneUnknownCardInGroup612Value =
                                        solveEnvHasOneUnknownCardInGroup
                                                ( cardSet.suspectCount
                                                , cardSet.suspectCount
                                                + cardSet.weaponCount);
         tableModified = tableModified || solveEnvHasOneUnknownCardInGroup612Value;
 
         boolean solveEnvHasOneUnknownCardInGroup12LengthValue =
                             solveEnvHasOneUnknownCardInGroup
                                    ( cardSet.suspectCount
                                    + cardSet.weaponCount
                                    , table.length);
         tableModified = tableModified || solveEnvHasOneUnknownCardInGroup12LengthValue;
 
        boolean solvePlusInGroup06Value = solvePlusInGroup( 0
                                                          , cardSet.suspectCount);
         tableModified = tableModified || solvePlusInGroup06Value;
 
        boolean solvePlusInGroup612Value = solvePlusInGroup( cardSet.suspectCount
                                                           , cardSet.suspectCount
                                                           + cardSet.weaponCount);
         tableModified = tableModified || solvePlusInGroup612Value;
 
        boolean solvePlusInGroup12LengthValue = solvePlusInGroup
                                                    ( cardSet.suspectCount
                                                    + cardSet.weaponCount
                                                    , table.length);
         tableModified = tableModified || solvePlusInGroup12LengthValue;
         return tableModified;
     }
 
     private boolean solveAskerHasNoCardsFromAccusation(Accusation a)
                                                throws UnknownPlayerException
                                                     , UnknownCardException
                                                     , ContradictionException {
         int playerNumber = playerOrd(a.asker);
         boolean tableModified = false;
         for (Card c : a.cards) {
             int cardNumber = cardSet.ordinal(c);
             boolean result = setMinus(cardNumber, playerNumber);
             tableModified = tableModified || result;
         }
         return tableModified;
     }
 
     private boolean solveUnsuccessfulAccusation(Accusation a)
                                             throws UnknownCardException
                                                  , ContradictionException {
         if (plusCountOfPlayer(ENV_COL) == 2) {
             for (Card c : a.cards) {
                 int cardNumber = cardSet.ordinal(c);
                 if (table[cardNumber][ENV_COL] == Resolution.Unknown) {
                     setMinus(cardNumber, ENV_COL);
                     return true;
                 }
             }
         }
         return false;
     }
 
     private void inferenceCycle() throws UnknownPlayerException
                                        , UnknownCardException
                                        , ContradictionException {
         for (;;) {
             boolean tableModified = false;
 
             boolean processLogValue = processLog();
             tableModified = tableModified || processLogValue;
 
             boolean rectifyTableValue = rectifyTable();
             verifyEveryLineHasQuestionOrPlus();
             tableModified = tableModified || rectifyTableValue;
             if (!tableModified) {
                 break;
             }
         }
     }
 
     private void verifyEveryLineHasQuestionOrPlus()
                                         throws ContradictionException {
         for (int i = 0; i < table.length; ++i) {
             verifyLineHasQuestionOrPlus(i);
         }
     }
 
     private void verifyLineHasQuestionOrPlus(int lineNum)
                                         throws ContradictionException {
         for (int i = 0; i < table[lineNum].length; ++i) {
             if (table[lineNum][i] != Resolution.Minus) {
                 return;
             }
         }
         throw new ContradictionException("Line has minuses only");
     }
 
     public void replaceLog(List<LogEntry> l) throws UnknownPlayerException
                                                   , UnknownCardException
                                                   , ContradictionException {
         beginTransaction();
         try {
             super.replaceLog(l);
             inferenceCycle();
         } catch (UnknownPlayerException upe) {
             rollBackTransaction();
             throw upe;
         } catch (UnknownCardException uce) {
             rollBackTransaction();
             throw uce;
         } catch (ContradictionException ce) {
             rollBackTransaction();
             throw ce;
         }
         commitTransaction();
     }
 
     /* Processes the situation when replier shows known card. */
     private boolean solveRepliersHave(Suggestion le)
                                                 throws UnknownPlayerException
                                                      , UnknownCardException
                                                      , ContradictionException {
         boolean tableModified = false;
         for (Reply r : le.replies) {
             int playerNumber = playerOrd(r.replier);
 
             int cardNumber = r.cardReply.ordinal(cardSet);
             if (cardNumber < 0) {
                 continue;
             }
             boolean setPlusValue = setPlus(cardNumber, playerNumber);
             tableModified = tableModified || setPlusValue;
         }
         return tableModified;
     }
 
     /* Processes the situation when replier shows no cards. */
     private boolean solveReplierHasNoCards(Suggestion le)
                                                 throws UnknownPlayerException
                                                      , UnknownCardException
                                                      , ContradictionException {
         boolean tableModified = false;
         for (Reply r : le.replies) {
             int playerNumber = playerOrd(r.replier);
 
             if (r.cardReply.isNoCard()) {
                 for (Card c : le.askedCards) {
                     int cardNumber = cardSet.ordinal(c);
                     boolean setMinusValue = setMinus(cardNumber, playerNumber);
                     tableModified = tableModified || setMinusValue;
                 }
             }
         }
         return tableModified;
     }
 
     private boolean solveOnlyOneUnknown(Suggestion le)
                                                 throws UnknownPlayerException
                                                      , UnknownCardException
                                                      , ContradictionException {
         boolean tableModified = false;
         for (Reply r : le.replies) {
             int playerNumber = playerOrd(r.replier);
 
             if (r.cardReply.isUnknown()) {
                 List<Card> s = allPlusCards(playerNumber);
                 s.addAll(allUnknownCards(playerNumber));
                 s.retainAll(le.askedCards);
                 if (s.size() == 1) {
                     int cardNumber = cardSet.ordinal(s.get(0));
                     boolean setPlusValue = setPlus(cardNumber, playerNumber);
                     tableModified = tableModified || setPlusValue;
                 }
             }
         }
         return tableModified;
     }
 
     private boolean solveThreeCardsReplied(Suggestion le)
                                                 throws UnknownPlayerException
                                                      , UnknownCardException
                                                      , ContradictionException {
         boolean tableModified = false;
         int countCardReplies = 0;
         for (Reply r : le.replies) {
             if (!r.cardReply.isNoCard()) {
                 ++countCardReplies;
             }
         }
         if (countCardReplies == 3) {
             for (Card c : le.askedCards) {
                 int cardNumber = cardSet.ordinal(c);
                 boolean value = setMinus(cardNumber, ENV_COL);
                 tableModified = tableModified || value;
             }
         }
         return tableModified;
     }
 
     private int plusCountOfPlayer(int playerNumber) {
         int count = 0;
         for (int i = 0; i < cardSet.cardCount; ++i) {
             if (table[i][playerNumber] == Resolution.Plus) {
                 ++count;
             }
         }
         return count;
     }
 
     private int nonNegativeCountOfCard(int cardNumber) {
         int nonNegativeCount = 0;
         for (int i = 0; i < table[cardNumber].length; ++i) {
             if (table[cardNumber][i] != Resolution.Minus) {
                 ++nonNegativeCount;
             }
         }
         return nonNegativeCount;
     }
 
     private int nonNegativeCountOfPlayer(int playerNumber) {
         int nonNegativeCount = 0;
         for (int i = 0; i < table.length; ++i) {
             if (table[i][playerNumber] != Resolution.Minus) {
                 ++nonNegativeCount;
             }
         }
         return nonNegativeCount;
     }
 
     private boolean solveLineOneNonNegative(int cardNumber)
                                                 throws ContradictionException {
         boolean tableModified = false;
         if (nonNegativeCountOfCard(cardNumber) == 1) {
             for (int i = 0; i < table[cardNumber].length; ++i) {
                 if (table[cardNumber][i] == Resolution.Unknown) {
                     boolean setPlusValue = setPlus(cardNumber, i);
                     tableModified = tableModified || setPlusValue;
                 }
             }
         }
         return tableModified;
     }
 
     private boolean solveCountOfNonnegativeEqualsCardCount(int playerNumber)
                                                 throws ContradictionException {
         boolean tableModified = false;
         if (nonNegativeCountOfPlayer(playerNumber)
                                     == cardCountPerPlayer[playerNumber]) {
             for (int i = 0; i < table.length; ++i) {
                 if (table[i][playerNumber] == Resolution.Unknown) {
                     setPlus(i, playerNumber);
                     tableModified = true;
                 }
             }
         }
         return tableModified;
     }
 
     private boolean solvePlusInGroup(int l, int r)
                                         throws ContradictionException {
         boolean tableModified = false;
         int pluses = 0;
         for (int i = l; i < r; ++i) {
             if (table[i][ENV_COL] == Resolution.Plus) {
                 ++pluses;
             }
         }
         if (pluses == 1) {
             for (int i = l; i < r; ++i) {
                 if (table[i][ENV_COL] != Resolution.Plus) {
                     boolean setMinusValue = setMinus(i, ENV_COL);
                     tableModified = tableModified || setMinusValue;
                 }
             }
         }
         return tableModified;
     }
 
     private boolean solveEnvHasOneUnknownCardInGroup(int l, int r)
                                                 throws ContradictionException {
         boolean tableModified = false;
         int minuses = 0;
         for (int i = l; i < r; ++i) {
             if (table[i][ENV_COL] == Resolution.Minus) {
                 ++minuses;
             }
         }
         if (minuses == (r - l - 1)) {
             for (int i = l; i < r; ++i) {
                 if (table[i][ENV_COL] != Resolution.Minus) {
                     boolean setPlusValue = setPlus(i, ENV_COL);
                     tableModified = tableModified || setPlusValue;
                 }
             }
         }
         return tableModified;
     }
 
     private boolean solvePlayerHasAllCards(int playerNumber)
                                                 throws ContradictionException {
         boolean tableModified = false;
         if (plusCountOfPlayer(playerNumber)
                     == cardCountPerPlayer[playerNumber]) {
 
             for (int i = 0; i < cardSet.cardCount; ++i) {
                 if (table[i][playerNumber] == Resolution.Unknown) {
                     tableModified = true;
                     setMinus(i, playerNumber);
                 }
             }
         }
         return tableModified;
     }
 
     private List<Card> allPlusCards(int playerNumber) {
         ArrayList<Card> result = new ArrayList<Card>();
         for (int i = 0; i < cardSet.cardCount; ++i) {
             if (table[i][playerNumber] == Resolution.Plus) {
                 result.add(cardSet.cards.get(i));
             }
         }
         return result;
     }
 
     private List<Card> allUnknownCards(int playerNumber) {
         ArrayList<Card> result = new ArrayList<Card>();
         for (int i = 0; i < cardSet.cardCount; ++i) {
             if (table[i][playerNumber] == Resolution.Unknown) {
                 result.add(cardSet.cards.get(i));
             }
         }
         return result;
     }
 
 }
