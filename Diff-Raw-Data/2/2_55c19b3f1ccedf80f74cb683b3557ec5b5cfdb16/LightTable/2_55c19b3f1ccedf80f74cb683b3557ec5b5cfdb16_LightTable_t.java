 package de.htwg.wzzrd.model;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import de.htwg.wzzrd.model.tableComponent.impl.Card;
 import de.htwg.wzzrd.model.tableComponent.impl.CardNames;
 import de.htwg.wzzrd.model.tableComponent.impl.Player;
 
 public class LightTable extends LightTableInterface {
     private Map<Player, Integer> playerMadeTricks = new HashMap<Player, Integer>();
     private Set<Player> playerLaidCard = new HashSet<Player>();
     private Player[] players;
     private int roundnumber;
     private int roundsleft;
     private int subroundnumber;
 
     private boolean subRoundFinished = true;
     private boolean roundFinished = true;
 
     private Player trickplayer = null;
     private Card trickcard = null;
     private int trump;
 
     @Override
     public void initTable(Player[] players) {
         this.players = Arrays.copyOf(players, players.length);
         playerMadeTricks.clear();
         for (Player player : players) {
             playerMadeTricks.put(player, 0);
         }
 
         roundnumber = 0;
         subroundnumber = 0;
 
         int cardcount = CardNames.getCardsPerNation() * CardNames.getNationCount();
         if ((cardcount % playerMadeTricks.size()) == 0) {
             roundsleft = (cardcount / playerMadeTricks.size() + 1);
         } else {
             throw new IllegalArgumentException("Illegal combination of CardsCount(" + cardcount + ") and Playercount(" + playerMadeTricks.size() + ")");
         }
     }
 
     @Override
     public int getTrickCount(Player player) throws RoundNotFinished {
         if (!playerMadeTricks.containsKey(player)) {
             throw new IllegalArgumentException(String.format("Player %s not in internal list!", player.getName()));
         }
         checkRoundNotFinished();
 
         return playerMadeTricks.get(player);
     }
 
     @Override
     public int getRoundNumber() {
         return roundnumber;
     }
 
     @Override
     public int getRoundsLeft() {
         return roundsleft;
     }
 
     @Override
     public void startSubRound() throws SubRoundNotFinished, RoundAlreadyFinished {
         checkSubroundNotFinished();
         checkRoundAlreadyFinished();
 
         playerLaidCard.clear();
         trickplayer = null;
         trickcard = null;
         subRoundFinished = false;
         subroundnumber++;
     }
 
     @Override
     public void startRound(int trump) throws GameIsFinished, RoundNotFinished {
         checkRoundNotFinished();
         checkGameIsFinished();
 
         roundsleft--;
         this.trump = trump;
         roundnumber++;
         subroundnumber = 0;
         playerMadeTricks.clear();
         playerLaidCard.clear();
         for (Player player : players) {
             playerMadeTricks.put(player, 0);
         }
         roundFinished = false;
         subRoundFinished = false;
     }
 
     @Override
     public void addCard(Player player, Card card) throws SubRoundAlreadyFinished, PlayerAlreadyLaidCard {
         checkSubroundAlreadyFinished();
         checkPlayerAlreadyLaidCard(player);
 
         if (trickcard == null || trickcard.isLessThan(card, trump)) {
             trickplayer = player;
             trickcard = card;
         }
         playerLaidCard.add(player);
 
         if (playerLaidCard.size() == players.length) {
             subRoundFinished = true;
             playerMadeTricks.put(trickplayer, playerMadeTricks.get(trickplayer) + 1);
             playerLaidCard.clear();
         }
         if (subRoundFinished && subroundnumber + 1 == roundnumber) {
             roundFinished = true;
         }
 
     }
 
     @Override
     public Player getSubRoundWinner() throws SubRoundNotFinished {
         checkSubroundNotFinished();
         return trickplayer;
     }
 
     @Override
     public Card getSubRoundWinnerCard() throws SubRoundNotFinished {
         checkSubroundNotFinished();
         return trickcard;
     }
 
     private void checkPlayerAlreadyLaidCard(Player player) throws PlayerAlreadyLaidCard {
         if (playerLaidCard.contains(player)) {
             throw new PlayerAlreadyLaidCard();
         }
     }
 
     private void checkRoundAlreadyFinished() throws RoundAlreadyFinished {
         if (roundFinished) {
             throw new RoundAlreadyFinished();
         }
     }
 
     private void checkRoundNotFinished() throws RoundNotFinished {
         if (!roundFinished) {
             throw new RoundNotFinished();
         }
     }
 
     private void checkSubroundAlreadyFinished() throws SubRoundAlreadyFinished {
         if (subRoundFinished) {
             throw new SubRoundAlreadyFinished();
         }
     }
 
     private void checkSubroundNotFinished() throws SubRoundNotFinished {
         if (!subRoundFinished) {
             throw new SubRoundNotFinished();
         }
     }
 
     private void checkGameIsFinished() throws GameIsFinished {
        if (roundsleft <= 1) {
             throw new GameIsFinished();
         }
     }
 
     @Override
     public boolean isSubRoundFinished() {
         return subRoundFinished;
     }
 
     @Override
     public boolean isRoundFinished() {
         return roundFinished;
     }
 }
