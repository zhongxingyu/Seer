 package org.drooms.api;
 
import java.util.Deque;
 
 /**
  * A situation is a point in time before any of the worms have made their moves.
  * Terminated worms are no longer present, all collectibles have been resolved.
  * 
  * @param <P>
  *            Playground for the situation.
  * @param <N>
  *            Node type for the playground.
  * @param <E>
  *            Edge type for the playground.
  */
 public interface Situation<P extends Playground<N, E>, N extends Node, E extends Edge<N>> {
 
     /**
      * Add a collectible at a specified position. Will only be added at a
      * position where there is nothing else - no wall, no worm, no other
      * collectible.
      * 
      * @param c
      *            The collectible to be added.
      * @param node
      *            The position in the playground.
      * 
      * @return True if added, false if it couldn't be added.
      */
     public boolean addCollectible(Collectible c, N node);
 
     /**
      * Mark collectible as having been collected by a given player. Also remove
      * it.
      * 
      * @param c
      *            Collectible in question.
      * @param p
      *            Player to have collected the collectible.
      * @return True if collected, false if there was no such collectible.
      */
     public boolean collectCollectible(Collectible c, Player p);
 
     /**
      * Mark two worms as having collided with each other.
      * 
      * @param p1
      *            Player in question.
      * @param p2
      *            Other player in question.
      * @return True of both the worms existed, false otherwise.
      */
     public boolean collide(Player p1, Player p2);
 
     /**
      * Mark the worm as having crashed into something.
      * 
      * @param p1
      *            Player in question.
      * @return True if the worm existed, false otherwise.
      */
     public boolean crash(Player p1);
 
     /**
      * A collectible found on the given position.
      * 
      * @param node
      *            The position to look at.
      * @return A collectible specified, or null if none.
      */
     public Collectible getCollectible(N node);
 
     /**
      * Get a player's head position in the situation.
      * 
      * @param p
      *            Player in question.
      * @return The node that the player is in. Null when crashed to the wall.
      */
     public N getHeadPosition(Player p);
 
     /**
      * Get the length of the player.
      * 
      * @param p
      *            Player in question.
      * @return Length of the player to be used in the next iteration.
      */
     public int getPlayerLength(Player p);
 
     /**
      * Retrieve the playground for this particular situation.
      * 
      * @return The playground.
      */
     public P getPlayground();
 
     /**
      * Return positions of all player's parts.
      * 
      * @param p
      *            Player in question.
      * @return First element is the head as retrieved by (
      *         {@link Situation#getHeadPosition(Player)}, last is the tail.
      */
    public Deque<N> getPositions(Player p);
 
     /**
      * Return the number of the current turn.
      * 
      * @return How many times the {@link #move()} method had been called.
      */
     public int getTurnNumber();
 
     /**
      * Make all players make their more. Will increse turn number (see
      * {@link #getTurnNumber()}.
      * 
      * @return The situation created by that move.
      */
     public Situation<P, N, E> move();
 
     /**
      * Remove a collectible from the game.
      * 
      * @param c
      *            Collectible in question.
      * @return True if removed, false if there was nothing to remove.
      */
     public boolean removeCollectible(Collectible c);
 
     /**
      * Reward the survival of a worm with a particular amount of points.
      * 
      * @param p
      *            Player in question.
      * @return True if player exists.
      */
     public boolean rewardSurvival(Player p, int points);
 
     /**
      * Change the length of the player's worm.
      * 
      * @param p
      *            Player in question.
      * @param length
      *            New length of the player, valid after the next {@link #move()}
      *            .
      */
     public void setPlayerLength(Player p, int length);
 
 }
