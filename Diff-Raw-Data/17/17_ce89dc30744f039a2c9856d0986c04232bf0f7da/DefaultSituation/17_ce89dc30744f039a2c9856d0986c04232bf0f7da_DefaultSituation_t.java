 package org.drooms.impl;
 
import java.util.Collection;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.drooms.api.Collectible;
 import org.drooms.api.Player;
 import org.drooms.api.Situation;
 
 public class DefaultSituation implements
         Situation<DefaultPlayground, DefaultNode, DefaultEdge> {
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(DefaultSituation.class);
 
     private final int turnNo;
     private final DefaultPlayground playground;
     private final Map<Player, PlayerDecisionLogic> players = new LinkedHashMap<Player, PlayerDecisionLogic>();
     private final Map<Player, Integer> lengths = new LinkedHashMap<Player, Integer>();
     private final Map<Player, Deque<DefaultNode>> positions = new LinkedHashMap<Player, Deque<DefaultNode>>();
     private final Map<Collectible, DefaultNode> collectibles = new HashMap<Collectible, DefaultNode>();
 
     private DefaultSituation(final DefaultPlayground playground,
             final int turnNo,
             final Map<PlayerDecisionLogic, Deque<DefaultNode>> players,
             final Map<Player, Integer> lengths,
             final Map<Collectible, DefaultNode> collectibles) {
         this.turnNo = turnNo;
         this.playground = playground;
         for (final Map.Entry<PlayerDecisionLogic, Deque<DefaultNode>> entry : players
                 .entrySet()) {
             final Player p = entry.getKey().getPlayer();
             this.players.put(p, entry.getKey());
             this.positions.put(p, entry.getValue());
         }
         // deep-clone
         for (final Map.Entry<Player, Integer> entry : lengths.entrySet()) {
             if (!this.players.containsKey(entry.getKey())) {
                 // forget information about dead players
                 continue;
             }
             this.lengths.put(entry.getKey(), entry.getValue());
         }
         for (final Map.Entry<Collectible, DefaultNode> entry : collectibles
                 .entrySet()) {
             this.collectibles.put(entry.getKey(), entry.getValue());
         }
     }
 
     public DefaultSituation(final DefaultPlayground playground,
             final List<Player> players, final int defaultLength) {
         this.turnNo = 0;
         this.playground = playground;
         final int playersSupported = this.playground.getStartingPositions()
                 .size();
         final int playersAvailable = players.size();
         if (playersSupported < playersAvailable) {
             throw new IllegalArgumentException(
                     "The playground doesn't support " + playersAvailable
                             + " players, only " + playersSupported + "! ");
         }
         final List<DefaultNode> startingPositions = this.playground
                 .getStartingPositions();
         int i = 0;
         for (final Player player : players) {
             this.players.put(player, new PlayerDecisionLogic(player));
             this.lengths.put(player, defaultLength);
             // determine starting position
             this.positions.put(player, new LinkedList<DefaultNode>());
             this.positions.get(player).push(startingPositions.get(i));
             DefaultSituation.LOGGER.info("Player {} assigned position {}.",
                     new Object[] { player.getName(), i });
             i++;
         }
     }
 
     @Override
     public boolean addCollectible(final Collectible c, final DefaultNode node) {
         if (this.getCollectible(node) != null) {
             return false;
         }
         this.collectibles.put(c, node);
         for (final PlayerDecisionLogic logic : this.players.values()) {
             logic.notifyOfCollectibleAddition(c, node);
         }
         DefaultSituation.LOGGER.info("New collectible {} added at node {}.",
                 new Object[] { c, node });
         return true;
     }
 
     @Override
     public boolean collectCollectible(final Collectible c, final Player p) {
         this.validatePlayer(p);
         if (this.removeCollectible(c)) {
             for (final PlayerDecisionLogic logic : this.players.values()) {
                 logic.notifyOfCollectibleReward(c, p, c.getPoints());
             }
             DefaultSituation.LOGGER.info("Player {} collected {}.",
                     new Object[] { p.getName(), c });
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean collide(final Player p1, final Player p2) {
         if (!this.hasPlayer(p1) || !this.hasPlayer(p2)) {
             return false;
         }
         for (final PlayerDecisionLogic logic : this.players.values()) {
             logic.notifyOfCollision(p1, p2);
         }
         this.players.remove(p1);
         this.players.remove(p2);
         DefaultSituation.LOGGER.info(
                 "Players {} and {} collided, neither will see next turn.",
                 new Object[] { p1.getName(), p2.getName() });
         return true;
     }
 
     @Override
     public boolean crash(final Player p1) {
         if (this.hasPlayer(p1)) {
             for (final PlayerDecisionLogic logic : this.players.values()) {
                 logic.notifyOfDeath(p1);
             }
             DefaultSituation.LOGGER.info(
                     "Player {} crashed and will not see next turn.",
                     new Object[] { p1.getName() });
             this.players.remove(p1);
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public Collectible getCollectible(final DefaultNode node) {
         for (final Map.Entry<Collectible, DefaultNode> entry : this.collectibles
                 .entrySet()) {
             if (entry.getValue() == node) {
                 return entry.getKey();
             }
         }
         return null;
     }
 
     @Override
     public DefaultNode getHeadPosition(final Player p) {
         this.validatePlayer(p);
         return this.positions.get(p).peek();
     }
 
     @Override
     public int getPlayerLength(final Player p) {
         this.validatePlayer(p);
         return this.lengths.get(p);
     }
 
     @Override
     public DefaultPlayground getPlayground() {
         return this.playground;
     }
 
     @Override
    public Collection<DefaultNode> getPositions(final Player p) {
         this.validatePlayer(p);
        return Collections
                 .unmodifiableCollection(this.positions.get(p));
     }
 
     @Override
     public int getTurnNumber() {
         return this.turnNo;
     }
 
     private boolean hasPlayer(final Player p) {
         return this.players.containsKey(p);
     }
 
     @Override
     public DefaultSituation move() {
         DefaultSituation.LOGGER.info("Playground {} starting turn #{}.",
                 new Object[] { this, this.turnNo });
         final Map<PlayerDecisionLogic, Deque<DefaultNode>> positions = new LinkedHashMap<PlayerDecisionLogic, Deque<DefaultNode>>();
         // determine the movements of players
         for (final Map.Entry<Player, PlayerDecisionLogic> entry : this.players
                 .entrySet()) {
             // make the decision
             final Move decision = entry.getValue().decideNextMove();
             final Player player = entry.getKey();
             // move the head of the worm
             final DefaultNode currentHeadPos = this.getHeadPosition(player);
             DefaultNode newHeadPos;
             switch (decision) {
             case UP:
                 newHeadPos = new DefaultNode(currentHeadPos.getX(),
                         currentHeadPos.getY() + 1);
                 break;
             case DOWN:
                 newHeadPos = new DefaultNode(currentHeadPos.getX(),
                         currentHeadPos.getY() - 1);
                 break;
             case LEFT:
                 newHeadPos = new DefaultNode(currentHeadPos.getX() - 1,
                         currentHeadPos.getY());
                 break;
             case RIGHT:
                 newHeadPos = new DefaultNode(currentHeadPos.getX() + 1,
                         currentHeadPos.getY());
                 break;
             case STAY:
                 newHeadPos = currentHeadPos;
                 break;
             default:
                 throw new IllegalStateException("Unknown move!");
             }
             // make sure the snake is as long as it should be
             final Deque<DefaultNode> newPosition = new LinkedList<DefaultNode>(
                     this.positions.get(player));
             newPosition.push(newHeadPos);
             while (newPosition.size() > this.getPlayerLength(player)) {
                 newPosition.removeLast();
             }
             positions.put(entry.getValue(), newPosition);
             // notify
             for (final PlayerDecisionLogic logic : this.players.values()) {
                 logic.notifyOfPlayerMove(player, decision, newHeadPos);
             }
         }
         return new DefaultSituation(this.getPlayground(),
                 this.getTurnNumber() + 1, positions, this.lengths,
                 this.collectibles);
     }
 
     @Override
     public boolean removeCollectible(final Collectible c) {
         if (this.collectibles.containsKey(c)) {
             this.collectibles.remove(c);
             for (final PlayerDecisionLogic logic : this.players.values()) {
                 logic.notifyOfCollectibleRemoval(c);
             }
             DefaultSituation.LOGGER.info(
                     "Collectible {} removed from playground..",
                     new Object[] { c });
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean rewardSurvival(final Player p, final int points) {
         if (this.hasPlayer(p)) {
             for (final PlayerDecisionLogic logic : this.players.values()) {
                 logic.notifyOfSurvivalReward(p, points);
             }
             DefaultSituation.LOGGER.info(
                     "Player {} survived another turn, awarded {} points.",
                     new Object[] { p.getName(), points });
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public void setPlayerLength(final Player p, final int length) {
         this.validatePlayer(p);
         this.lengths.put(p, length);
         for (final PlayerDecisionLogic logic : this.players.values()) {
             logic.notifyOfPlayerLengthChange(p, length);
         }
         DefaultSituation.LOGGER.info("Player {} has length: {}.", new Object[] {
                 p.getName(), length });
     }
 
     private void validatePlayer(final Player p) {
         if (!this.hasPlayer(p)) {
             throw new IllegalArgumentException("Player not in the game: "
                     + p.getName());
         }
     }
 
 }
