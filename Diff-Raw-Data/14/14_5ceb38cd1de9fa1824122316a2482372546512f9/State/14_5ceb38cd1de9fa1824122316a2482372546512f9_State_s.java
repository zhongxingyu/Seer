 package cardGame.wizard.bot.uct;
 
 import cardGame.Card;
 import cardGame.wizard.WizardComparator;
 import cardGame.wizard.WizardGame;
 import cardGame.wizard.WizardState;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 public class State {
 
     private static int players;
     private static int round;
     private static int roundStarter;
     private static int trumpSuit;
     private static int[] scores;
     
     private int[] bids;
     private int currentPlayer;
     private int[] playerTricks;
     private ArrayList<Card> tableCards;
     private ArrayList<Card>[] playerHands;
 
     public State(int currentPlayer, int[] playerTricks, ArrayList<Card> tableCards, ArrayList<Card>[] handsByPlayer, int[] bids) {
         this.currentPlayer = currentPlayer;
         this.playerTricks = playerTricks;
         this.tableCards = tableCards;
         this.playerHands = handsByPlayer;
         this.bids = bids;
     }
     
     public State (WizardState state) {
         currentPlayer = state.getCurrentPlayer();
         playerTricks = new int[state.getNumberOfPlayers()];
         tableCards = (ArrayList<Card>) state.getTableCards();
         playerHands = state.getHands();
         initialize(state);
         bids = new int[players];
         for (int i=0; i<players; i++) {
             bids[i] = state.getBid(i);
         }
         
     }
     
     public State makeMove(Move move) {
         if (move.isCard()) {
             return makeMove(move.getCard());
         } else {
             return makeMove(move.getBid());
         }
     }
     public State makeRandomMove() {
         ArrayList<Move> playable = getPossibleMoves();
         Random rand = new Random();
         Move playCard = playable.get(rand.nextInt(playable.size()));
         return makeMove(playCard);
     }
 
     private State makeMove(Card card) {
         ArrayList<Card> hand = playerHands[currentPlayer];
         ArrayList<Card>[] newPlayerHands = playerHands.clone();
         ArrayList<Card> newHand = (ArrayList<Card>) hand.clone();
         ArrayList<Card> newTableCards = null;
         int newCurrentPlayer = currentPlayer;
         int[] newPlayerTricks = playerTricks.clone();
 
         if (WizardGame.cardLegallyPlayable(tableCards, card, hand)) {
             newHand.remove(card);
             newPlayerHands[currentPlayer] = newHand;
             newTableCards = (ArrayList<Card>) tableCards.clone();
             card.setOwner(currentPlayer);
             newTableCards.add(card);
             newCurrentPlayer = (currentPlayer + 1) % players;
 
             if (newTableCards.size() == players) {
                Card highestCard = Collections.min(tableCards, new WizardComparator(trumpSuit, WizardGame.getFollowSuit(tableCards)));
                 int trickWinner = highestCard.getOwner();
                 newPlayerTricks[trickWinner]++;
                 newCurrentPlayer = trickWinner;
                 newTableCards = new ArrayList<>(players);
             }
         } else {
             throw new IllegalArgumentException("Illegal card played!");
         }
         return new State(newCurrentPlayer, newPlayerTricks, newTableCards, newPlayerHands, this.bids);
     }
 
     private State makeMove(int bid) {
         if (bids[currentPlayer] == -1) {
             int[] newBids = bids.clone();
             newBids[currentPlayer] = bid;
             int newCurrentPlayer = (currentPlayer + 1) % players;
             ArrayList<Card> newTableCards = (ArrayList<Card>) this.tableCards.clone();
             ArrayList<Card>[] newPlayerHands = (ArrayList<Card>[]) this.playerHands.clone();
             return new State(newCurrentPlayer, this.playerTricks.clone(), newTableCards, newPlayerHands, newBids);
         }
         throw new IllegalArgumentException("Cannot bid");
     }
     
     public ArrayList<Move> getPossibleMoves() {
         ArrayList<Move> moves = new ArrayList();
         if (bids[currentPlayer] == -1) {
             for (int i=0; i<=round; i++) {
                 if (! (currentPlayer == roundStarter && getTotalBids() +i == round)) {
                     moves.add(new Move(i));
                 }
             }
         } else {
             return getPlayableCards();
         }
         return moves;
     }
     
     private ArrayList<Move> getPlayableCards() {
         ArrayList<Move> playableCards = new ArrayList<>();
         for (Card c : playerHands[currentPlayer]) {
             if (cardLegallyPlayable(c)) {
                 playableCards.add(new Move(c));
             }          
         }   
         return playableCards;
     }
     
     public boolean isFinalState() {
 
         for (int i = 0; i < playerHands.length; i++) {
             if (!playerHands[i].isEmpty()) {
                 return false;
             }
         }
         return true;
     }
     
     public int[] evaluate(){
         if (isFinalState()) {
         int[] reward = new int[players];
         for (int i=0; i<players; i++) {
             reward[i] = WizardGame.calculateScore(playerTricks[i], bids[i]);
         }         
         return reward; }
         throw new UnsupportedOperationException("Cannot evaluate non terminal state!");
     }
     
     public static void initialize(WizardState state) {
         players = state.getNumberOfPlayers();
         round = state.getRound();
         scores = new int[players];
         trumpSuit = state.getTrumpSuit();
     }
 
     public int getTotalBids() {
         int total = 0;
         for (int i = 0; i < players; i++) {
             if (bids[i] > 0) {
                 total += bids[i];
             }
         }
         return total;
     }
     public static int getPlayers() {
         return players;
     }
 
     public static int getRound() {
         return round;
     }
 
     public static int getTrumpSuit() {
         return trumpSuit;
     }
 
     public static int[] getScores() {
         return scores;
     }
 
     public int[] getBids() {
         return bids;
     }
 
     public int getCurrentPlayer() {
         return currentPlayer;
     }
 
     public int[] getPlayerTricks() {
         return playerTricks;
     }
 
     public ArrayList<Card> getTableCards() {
         return tableCards;
     }
 
     public ArrayList<Card>[] getPlayerHands() {
         return playerHands;
     }
     
     public boolean cardLegallyPlayable(Card c) {
         return WizardGame.cardLegallyPlayable(tableCards, c, playerHands[currentPlayer]);
     }
     
 }
