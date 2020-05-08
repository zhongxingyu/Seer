 package co.raawr.cah;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class CAH {
 
     // Bot framework
     private static Main cah;
     // Game constants
     private static final int ROUND_LIMIT_MIN = 3;
     private static final int ROUND_LIMIT_MAX = 25;
     private static final int PLAYER_HAND_MAX = 10;
     // Player and card handling lists
     private static ArrayList<Player> players = new ArrayList<>();
     private static ArrayList<Player> playersTemp = new ArrayList<>();
     private static Queue<Player> playerQueue = new LinkedList<>();
     // Black cards are questions, bot shows these
     private static ArrayList<Card> blackDeck = new ArrayList<>();
     // White cards are answers, players use these
     private static ArrayList<Card> whiteDeck = new ArrayList<>();
     // Game variables - do not change
     //private static ArrayList<Card> roundHand = new ArrayList<>();
     private static Card activeCard;
     private static int round = 0;
     private static int rounds = 0;
     private static int czar = 0;
     //private static Player owner;
     // If a game has been initialized with .cah but not started
     private static boolean gamePrepped = false;
     // If the czar is picking a card
     private static boolean pickingCard = false;
 
     public static void init(Main cah) {
         CAH.cah = cah;
     }
 
     public static void addCards() {
         // Scans and adds cards to decks
         // Should only be called once on startup
         addWhiteCards();
         addBlackCards();
     }
 
     public static void addBlackCards() {
         try {
             File f = new File("black.dat");
             Scanner sc = new Scanner(f);
 
             String line;
             String expansion = "default";
             String content;
 
             // Scan and add black cards
             while (sc.hasNextLine()) {
                 line = sc.nextLine();
                 if (line.matches("\\{.*\\}")) {
                     expansion = line.replace("{", "").replace("}", "");
                     continue;
                 }
                 content = line;
                 Card c = new Card("black", content, expansion);
                 blackDeck.add(c);
             }
             // Shuffle
             Collections.shuffle(blackDeck);
             cah.print("Added " + blackDeck.size() + " black cards and shuffled.");
 
         } catch (FileNotFoundException ex) {
             Logger.getLogger(CAH.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static void addWhiteCards() {
         try {
             File f = new File("white.dat");
             Scanner sc = new Scanner(f);
 
             String line;
             String expansion = "default";
             String content;
 
             // Scan and add white cards
             while (sc.hasNextLine()) {
                 line = sc.nextLine();
                 if (line.matches("\\{.*\\}")) {
                     expansion = line.replace("{", "").replace("}", "");
                     continue;
                 }
                 content = line;
                 Card c = new Card("white", content, expansion);
                 whiteDeck.add(c);
             }
             // Shuffle
             Collections.shuffle(whiteDeck);
             cah.print("Added " + whiteDeck.size() + " white cards and shuffled.");
 
         } catch (FileNotFoundException ex) {
             Logger.getLogger(CAH.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public static void addPlayer(Player p) {
 
         if (p == null) {
             return;
         }
 
         if (players.contains(p)) {
             cah.sendMessage("#cah", "You are already in the game.");
             return;
         }
 
         if (round == 0 && gamePrepped) {
             // A game has been prepped and not started
             players.add(p);
             cah.sendMessage("#cah", p.nick + " has joined the game.");
         } else if (round == 0) {
             // A game has not been prepped yet
             cah.sendMessage("#cah", "A game has not been started yet! Use .cah [rounds] to start one.");
         } else {
             // Game is in progress, add at next round
             playerQueue.add(p);
             cah.sendMessage("#cah", "You will be added to the game on the next round.");
         }
 
     }
 
     public static void beginRound() {
 
         round++;
         cah.sendMessage("#cah", "[Round: " + round + "]");
 
         // Handle czar
         if (czar < players.size()) {
             // Set czar
             players.get(czar).isCzar = true;
             cah.sendMessage("#cah", "It is Czar " + players.get(czar).nick + "'s turn.");
         }
 
         // Deal black card
         if (blackDeck.isEmpty()) {
             addBlackCards();
         }
         activeCard = blackDeck.remove(0);
         cah.sendMessage("#cah", activeCard.content);
 
         // Deal white cards to players
         for (int i = 0; i < players.size(); i++) {
             int cardsNeeded = PLAYER_HAND_MAX - players.get(i).hand.size();
             for (int j = 0; j < cardsNeeded; j++) {
                 // Make sure we don't run out of cards
                 if (whiteDeck.isEmpty()) {
                     // We ran out of white cards! Rescan and shuffle
                     addWhiteCards();
                 }
                 players.get(i).addCard(whiteDeck.remove(j));
             }
         }
 
         // Show white cards to players
         String cards = "";
         for (int i = 0; i < players.size(); i++) {
             Player p = players.get(i);
             // Show them the black card in PM
             if (!p.isCzar) {
                 cah.sendMessage(p.nick, "[Round " + round + "]");
                 cah.sendMessage(p.nick, activeCard.content);
             }
             for (int j = 0; j < p.hand.size(); j++) {
                 if (!p.isCzar) {
                     // If message goes over 450, print the cards gotten thus far
                     if (cards.length() > 300) {
                         cah.sendMessage(p.nick, cards);
                         cards = "";
                     }
                     cards += (j + 1) + ": [" + p.hand.get(j).content + "] ";
                 }
             }
             // Do not show cards to czar
             if (p.isCzar) {
                 cah.sendMessage(p.nick, "You are the czar this round!");
             } else {
                 // Show remaining cards to the player
                 cah.sendMessage(p.nick, cards);
                 cards = "";
                 p.awaitingSubmit = true;
             }
         }
 
         // All cards have been dealt and shown
 
     }
 
     public static void czarPickCard(Player p, int card) {
         // Make sure player is actually in game
         if (p == null) {
             return;
         }
         // Make sure the player is actually the czar
         if (!p.isCzar) {
             return;
         }
         // They are the czar, check if ready to pick a card
         if (pickingCard) {
            if (card > 0 && card <= playersTemp.size()) {
                 Player w = playersTemp.get(card - 1);
 
                 w.score++;
                 cah.sendMessage("#cah", w.nick + " has won this round. Current score: " + w.score);
                 pickingCard = false;
                 roundTransistion();
             }
         }
     }
 
     public static void pickCard(Player p, int card) {
         // Make sure that player is actually in-game
         if (p == null) {
             cah.sendMessage(p.nick, "You are not currently in the game!");
             return;
         }
         // Make sure we're actually waiting for a card from this player
         if (p.awaitingSubmit) {
             if (card > 0 && card < 11) {
                 p.playedCardIndex = card - 1;
                 p.awaitingSubmit = false;
                 cah.sendMessage(p.nick, "Card submitted: [" + p.hand.get(p.playedCardIndex).content + "]");
             } else {
                 cah.sendMessage(p.nick, "Please choose a card between 1 and 10.");
             }
         } else {
             cah.sendMessage(p.nick, "You cannot submit a card at this time!");
             return;
         }
         // Now check if all players have submitted
         for (Player pl : players) {
             if (!pl.isCzar && pl.awaitingSubmit) {
                 return;
             }
         }
 
         // If we're here, everyone has submitted. Display picks and begin czar picking
         if (!pickingCard) {
             displayCards();
         }
 
     }
 
     public static void displayCards() {
 
         String cards = "";
         HashMap<Player, Card> czarHand = new HashMap<>();
 
         // Get each player's card and add it to hashmap (other than czar's)
         for (int i = 0; i < players.size(); i++) {
             Player p = players.get(i);
             if (!p.isCzar) {
                 czarHand.put(p, p.getPlayedCard());
             }
         }
 
         // Randomize the order in which the player's cards appear
         playersTemp.addAll(czarHand.keySet());
         Collections.shuffle(playersTemp);
 
         // Display and remove the card from their hand
         for (int i = 0; i < playersTemp.size(); i++) {
             Card c = czarHand.get(playersTemp.get(i));
             cards += "[" + (i + 1) + " : " + c.content + "] ";
         }
         cah.sendMessage("#cah", "Black card: [" + activeCard.content + "]");
         cah.sendMessage("#cah", cards);
         cah.sendMessage("#cah", "Choose a card, Czar " + players.get(czar).nick + ".");
         pickingCard = true;
         // Wait on czar to pick card
     }
 
     public static Player createPlayer(String nick) {
         return lookupPlayer(nick) != null ? lookupPlayer(nick) : new Player(nick);
     }
 
     public static Player lookupPlayer(String nick) {
         for (Player p : players) {
             if (p.nick.equalsIgnoreCase(nick)) {
                 return p;
             }
         }
         return null;
     }
 
     public static void removePlayer(Player p) {
 
         if (p == null) {
             // Player was not found
             cah.sendMessage("#cah", "You are not currently in a game.");
             return;
         }
 
         if (!gamePrepped && round == 0) {
             // No game has been started; therefore you cannot leave the game
         } else if (round == 0) {
             // A game has been started but still in joining period
             // Remove player with no ill effects
             players.remove(p);
             cah.sendMessage("#cah", p.nick + " has left the game.");
         } else {
             // Check if there are enough players to continue the game
             if (players.size() < 3) {
                 // Not enough players, end game
                 endGame();
             }
 
             if (p.isCzar) {
                 // Player was the czar, restart round
                 // TODO
             }
             // Remove player and add his cards into the deck
             // TODO
 
         }
     }
 
     public static void roundTransistion() {
         if (!playerQueue.isEmpty()) {
             // Add players that are waiting to join
             for (Player p : playerQueue) {
                 players.add(p);
             }
         }
 
         // Clean out the player-hand list
         playersTemp.clear();
 
         // Remove player's played cards
         for (Player p : players) {
             if (!p.isCzar) {
                 p.removePlayedCard();
             }
         }
 
         // Make sure the czar isn't the czar anymore
         players.get(czar).isCzar = false;
 
         // Onto the next czar
         czar = (czar == players.size() - 1) ? 0 : czar + 1;
 
         // Onto the next round
         if (round < rounds) {
             beginRound();
         } else {
             // The game is over
             endGame();
         }
     }
 
     private static void endGame() {
         cah.sendMessage("#cah", "The game is over!");
         // Declare winners, obviously
         declareWinners();
 
         // Clean up!
         round = 0;
         rounds = 0;
         players.clear();
         whiteDeck.clear();
         addWhiteCards();
         blackDeck.clear();
         addBlackCards();
         playerQueue.clear();
         czar = 0;
         activeCard = null;
         pickingCard = false;
 
     }
 
     private static void declareWinners() {
         int highscore = 0;
         ArrayList<Player> winners = new ArrayList<>();
         // First, determine the high score
         for (Player p : players) {
             if (p.score > highscore) {
                 highscore = p.score;
             }
         }
         // Now, determine the winners
         for (Player p : players) {
             if (p.score == highscore) {
                 winners.add(p);
             }
         }
         // Print the winner(s)
         String w = "";
         if (winners.size() == 1) {
             cah.sendMessage("#cah", winners.get(0).nick + " has won with a score of " + highscore + "!");
         } else if (winners.size() == 2) {
             cah.sendMessage("#cah", winners.get(0).nick + " and " + winners.get(1) + " tied for first with a score of " + highscore + "!");
         } else {
             for (int i = 0; i < winners.size(); i++) {
                 Player p = winners.get(i);
                 if (i < winners.size() - 2) {
                     w += p.nick + ", ";
                 } else if (i == winners.size() - 2) {
                     w += p.nick + ", and ";
                 } else if (i == winners.size() - 1) {
                     w += p.nick;
                 }
             }
             w += " tied for first with a score of " + highscore + "!";
             cah.sendMessage("#cah", w);
         }
     }
 
     public static void endGame(Player p) {
         if (p.isOwner) {
             endGame();
             cah.sendMessage("#cah", "The owner " + p.nick + " has ended the game.");
         } else {
             cah.sendMessage("#cah", "You may not end the game.");
         }
     }
 
     public static void prepGame(int rounds, Player owner) {
 
         if (!(round == 0)) {
             // Game is already in progress
             cah.sendMessage("#cah", "There is already a game in progress.");
             return;
         }
 
         if (!(rounds >= ROUND_LIMIT_MIN && rounds <= ROUND_LIMIT_MAX)) {
             // Invalid rounds
             cah.sendMessage("#cah", "Number of rounds must range from " + ROUND_LIMIT_MIN + " to " + ROUND_LIMIT_MAX + ".");
             return;
         }
 
         //CAH.owner = owner;
         CAH.rounds = rounds;
 
         // Wait for players to join
         gamePrepped = true;
         cah.sendMessage("#cah", owner.nick + " has started a game! Type .join to join.");
 
         // Designate game owner and add him to game
         owner.isOwner = true;
         addPlayer(owner);
     }
 
     public static void begin(Player p) {
         if (players.size() < 3) {
             cah.sendMessage("#cah", "At least 3 players are required to start a round.");
             return;
         }
         if (p.isOwner) {
             cah.sendMessage("#cah", "The game has started!");
             gamePrepped = false;
             beginRound();
         } else {
             cah.sendMessage("#cah", "You cannot start the game because you are not the owner.");
         }
     }
 }
