 package com.cluedoassist;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.io.Serializable;
import java.util.Collections;
 
 public class Cluedo implements Serializable {
     private ArrayList<String> players;
     private int playerCount;
 
     private static int ENV_COL = 0;
     private static int OUT_COL = 1;
     private static int ME_COL = 2;
 
     public static String ENVELOPE = "Env";
     public static String OUT = "Out";
     public static String ME = "Me";
 
     private int currentPlayer;
 
     private Resolution[][] table;
 
     private int[] cardCountPerPlayer;
 
     public int cardCount = Card.values().length;
 
     ArrayList<LogEntry> log;
 
    public List<LogEntry> getLog() {
        return Collections.unmodifiableList(log);
    }

     public List<String> getCompartments() {
         ArrayList<String> result = new ArrayList<String>();
         result.add(ENVELOPE);
         result.add(OUT);
         result.addAll(getPlayers());
         return result;
     }
 
     public List<String> getPlayers() {
         return new ArrayList<String>(players);
     }
 
     public Cluedo(List<String> players) {
         this.playerCount = players.size() + 1;
         this.players = new ArrayList<String>();
         this.players.add(ME);
         this.players.addAll(players);
         this.currentPlayer = 0;
         table = new Resolution[cardCount][];
         for (int i = 0; i < cardCount; ++i) {
             table[i] = new Resolution[playerCount + 2];
             for (int j = 0; j < table[i].length; ++j) {
                 table[i][j] = Resolution.Unknown;
             }
         }
         log = new ArrayList<LogEntry>();
 
         setCardCount();
 
         solvePlayerHasAllCards(OUT_COL);
     }
 
     private void setCardCount() {
         int cardCountPerOnePlayer = (cardCount - 3) / playerCount;
         int outCount = (cardCount - 3) % playerCount;
 
         cardCountPerPlayer = new int[playerCount + 2];
         cardCountPerPlayer[ENV_COL] = 3;
         cardCountPerPlayer[OUT_COL] = outCount;
         for (int i = 2; i < cardCountPerPlayer.length; ++i) {
             cardCountPerPlayer[i] = cardCountPerOnePlayer;
         }
     }
 
     public String[][] getTable() {
         String[][] result = new String[table.length + 1][];
         for (int i = 0; i < result.length; ++i) {
             result[i] = new String[playerCount + 3];
         }
         // write titles
         result[0][ENV_COL + 1] = "Env";
         result[0][OUT_COL + 1] = "Out";
         for (int j = 0; j < playerCount; ++j) {
             result[0][j + OUT_COL + 2] = players.get(j);
         }
         for (int i = 0; i < table.length; ++i) {
             result[i + 1][0] = Card.values()[i].toString();
             for (int j = 0; j < table[i].length; ++j) {
                 switch (table[i][j]) {
                 case Plus : result[i + 1][j + 1] = "+"; break;
                 case Minus : result[i + 1][j + 1] = "-"; break;
                 case Unknown : result[i + 1][j + 1] = "?"; break;
                 }
             }
         }
         return result;
     }
 
     private void setPlus(int cardNumber, int playerNumber) {
         table[cardNumber][playerNumber] = Resolution.Plus;
         for (int i = 0; i < table[cardNumber].length; ++i) {
             if (i != playerNumber) {
                 table[cardNumber][i] = Resolution.Minus;
             }
         }
     }
 
     public void setCard(String asker, Card card) throws UnknownPlayerException {
         //log.add(new SetCard(asker, card));
         int playerNumber = playerOrd(asker);
         int cardNumber = card.cardNumber();
         setPlus(cardNumber, playerNumber);
         rectifyTable();
     }
 
     public void makeTurn(String asker, List<Card> askedCards, List<Reply> replies) throws UnknownPlayerException {
         log.add(new LogEntry(asker, askedCards, replies));
         rectifyTable();
     }
 
     public void makeTurn(LogEntry l) throws UnknownPlayerException {
         log.add(l);
         rectifyTable();
     }
 
     private void processLog() throws UnknownPlayerException {
         for (LogEntry logEntry : log) {
             solveRepliersHave(logEntry);
             solveReplierHasNoCards(logEntry);
             solveOnlyOneUnknown(logEntry);
         }
     }
 
     private void rectifyTable() throws UnknownPlayerException {
         processLog();
         for (int i = 0; i < table[0].length; ++i) {
             solvePlayerHasAllCards(i);
         }
     }
 
     public int playerOrd(String player) throws UnknownPlayerException {
         if (ENVELOPE.equals(player)) {
             return ENV_COL;
         } else if (OUT.equals(player)) {
             return OUT_COL;
         } else {
             for (int i = 0; i < playerCount; ++i) {
                 if (player.equals(players.get(i))) {
                     return i + 2;
                 }
             }
             throw new UnknownPlayerException("Unknown player : " + player);
         }
     }
 
     private void solveRepliersHave(LogEntry le) throws UnknownPlayerException {
         for (Reply r : le.replies) {
             int playerNumber = playerOrd(r.replier);
 
             int cardNumber = r.cardReply.cardNumber();
             if (cardNumber < 0) {
                 continue;
             }
             setPlus(cardNumber, playerNumber);
         }
     }
 
     private void solveReplierHasNoCards(LogEntry le) throws UnknownPlayerException {
         for (Reply r : le.replies) {
             int playerNumber = playerOrd(r.replier);
 
             if (r.cardReply == CardReply.NoCard()) {
                 for (Card c : le.askedCards) {
                     int cardNumber = c.cardNumber();
                     table[cardNumber][playerNumber] = Resolution.Minus;
                 }
             }
         }
     }
 
     private void solveOnlyOneUnknown(LogEntry le) throws UnknownPlayerException {
         for (Reply r : le.replies) {
             int playerNumber = playerOrd(r.replier);
 
             if (r.cardReply != CardReply.NoCard()) {
                 List<Card> s = allPlusCards(playerNumber);
                 s.addAll(allUnknownCards(playerNumber));
                 s.retainAll(le.askedCards);
                 if (s.size() == 1) {
                     int cardNumber = s.get(0).cardNumber();
                     setPlus(cardNumber, playerNumber);
                 }
             }
         }
     }
 
     private int plusCountOfPlayer(int playerNumber) {
         int count = 0;
         for (int i = 0; i < cardCount; ++i) {
             if (table[i][playerNumber] == Resolution.Plus) {
                 ++count;
             }
         }
         return count;
     }
 
     private void solvePlayerHasAllCards(int playerNumber) {
         if (plusCountOfPlayer(playerNumber)
                     == cardCountPerPlayer[playerNumber]) {
 
             for (int i = 0; i < cardCount; ++i) {
                 if (table[i][playerNumber] == Resolution.Unknown) {
                     table[i][playerNumber] = Resolution.Minus;
                 }
             }
         }
     }
 
     private List<Card> allPlusCards(int playerNumber) {
         ArrayList<Card> result = new ArrayList<Card>();
         for (int i = 0; i < cardCount; ++i) {
             if (table[i][playerNumber] == Resolution.Plus) {
                 result.add(Card.values()[i]);
             }
         }
         return result;
     }
 
     private List<Card> allUnknownCards(int playerNumber) {
         ArrayList<Card> result = new ArrayList<Card>();
         for (int i = 0; i < cardCount; ++i) {
             if (table[i][playerNumber] == Resolution.Unknown) {
                 result.add(Card.values()[i]);
             }
         }
         return result;
     }
 
 }
