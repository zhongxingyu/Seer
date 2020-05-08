 package src.core;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import src.WaveGenerator;
 
 public class Game {
 	private static final int creepDelay = 25; // delay, in ticks, between creeps
 	private static final int waveTime = 1000; // number of ticks between each wave (about 30 seconds)
 	
 	private Map map;
 	private ArrayList<Creep> creeps;
 	private ArrayList<Tower> towers;
 	private Player player;
 	private int elapsedTime; // the total game time, in "ticks"
 	private int wavesSent; // the total number of waves sent
 	private int lastWaveTime; // the elapsedTime when the last wave was sent
 	
 	private LinkedList<Creep> creepQueue; // the creeps that are waiting to be sent out onto the map
 	private int lastCreepTime; // the time the last creep was sent
 
 	public Game() {
 		creeps = new ArrayList<Creep>();
 		creepQueue = new LinkedList<Creep>();
 	}
 	
 	/**
 	 * Performs all of the actions associated with one "tick" during the game.
 	 * This includes updating creep positions and applying damage to creeps,
 	 * among other things.
 	 */
 	public void tick() {
 		elapsedTime++;
 		
 		// time to send another wave
 		if (elapsedTime - lastWaveTime >= waveTime) {
 			sendNextWave();
 		}
 		
 		// time to send another creep, if there is one...
 		if (creepQueue.size() != 0 && elapsedTime - lastCreepTime > creepDelay) {
 			lastCreepTime = elapsedTime;
 			
 			Creep toSend = creepQueue.poll();
 			CreepPath path = toSend.isFlying() ? map.getFlyingPath() : map.getPath();
 			
 			toSend.setPath(path);
 			toSend.setPosition(path.getPoint(0));
 			
 			synchronized (creeps) {
 				creeps.add(toSend);
 			}
 		}
 
 		// do a bunch of stuff to update the state of the game
 		stepCreeps();
 		doTowerAttacks();
 	}
 
 	/**
 	 * Sends the next wave of creeps.
 	 */
 	public void sendNextWave(){
 		lastWaveTime = elapsedTime;
 		
 		sendWave(WaveGenerator.generateWave(wavesSent + 1)); // wavesSent + 1 == wave number	
 	}
 	
 	/**
 	 * Moves each of the creeps on the board along their specified CreepPath.  When creeps
 	 * are destroyed, a reward is issued to the player.  When creeps reach the end of the
 	 * path unchecked, the player's health is reduced.
 	 */
 	private void stepCreeps() {
 		double speed = .05; // tiles per tick
 
 		Iterator<Creep> it = creeps.iterator();
 
 		while (it.hasNext()) {
 			Creep c = it.next();
 			
 			if (c.getHealthFraction() <= 0) {
 				synchronized (creeps) {
 					it.remove();
 				}
 				
 				player.reapReward(c.getReward());
 				continue;
 			}
 			
 			Point2D.Double direction = c.getNextDirection();
 
 			if (direction == null) { // the creep reached the end of the path
 				player.decreaseHealth(c.getDamageToBase());
 				
 				synchronized (creeps) {
 					it.remove();
 				}
 				
 				continue;
 			}
 
 			double realSpeed = speed * c.getSpeed();
 			c.setPosition(new Point2D.Double(
 					direction.getX() * realSpeed + c.getPosition().getX(), 
 					direction.getY() * realSpeed + c.getPosition().getY()));
 			
 			c.handleTimedDamage(elapsedTime);
 		}
 	}
 
 	/**
 	 * Sets the creeps targeted by each tower, and 
 	 */
 	private void doTowerAttacks() {
 		for (Tower t : towers) {
 			for (Creep c : creeps) {
 				if (c.getPosition().distance(t.getX(), t.getY()) < t.getRadius() &&
 					(!c.isFlying() || t.getTargeting().isHitsFlying())) {
 					c.applyDamage(t.getDamage(), t, elapsedTime);
 					break;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Adds creeps to the creep queue.  As time goes on, creeps are released
 	 * from the map queue one by one onto the map, at some fixed spacing interval.
 	 * @param wave A collection of creeps to add to the creep queue.
 	 */
 	public void sendWave(Collection<Creep> wave) {
 		wavesSent++;
 		
 		for (Creep c : wave) {
 			creepQueue.add(c);
 		}
 	}
 	
 	/**
 	 * @return The number of "ticks" until the next wave will be released.
 	 */
 	public int getTicksUntilNextWave() {
 		return waveTime - (elapsedTime - lastWaveTime);
 	}
 
 	public Map getMap() {
 		return map;
 	}
 
 	public void setMap(Map map) {
 		this.map = map;
 	}
 
 	public Collection<Creep> getCreeps() {
 		return creeps;
 	}
 
 	public void setCreeps(ArrayList<Creep> creeps) {
 		this.creeps = creeps;
 	}
 
 	public Collection<Tower> getTowers() {
 		return towers;
 	}
 
 	public void setTowers(ArrayList<Tower> towers) {
 		this.towers = towers;
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 	
 	public int getLastWaveTime(){
 		return lastWaveTime;
 	}
 	
 	public void setLastWaveTime(int lastWaveTime) {
 		this.lastWaveTime = lastWaveTime;
 	}
 	
 	public int getElapsedTime() {
 		return elapsedTime;
 	}
 
 	public void setElapsedTime(int elapsedTime) {
 		this.elapsedTime = elapsedTime;
 	}
 	
 	public int getWavesSent() {
 		return wavesSent;
 	}
 }
