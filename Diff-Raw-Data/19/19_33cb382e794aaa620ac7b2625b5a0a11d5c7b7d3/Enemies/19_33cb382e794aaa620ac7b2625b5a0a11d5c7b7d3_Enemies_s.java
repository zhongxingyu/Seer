 package com.jaxelson;
 
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 
 import navigation.ExtendedBot;
 import robocode.RobotDeathEvent;
 import robocode.ScannedRobotEvent;
 /**
  * Holds the information about all the enemies in the game
  * @author jason
  */
 public class Enemies {
 	private Hashtable<String,BotInfo> _enemyTable = new Hashtable<String,BotInfo>();
 	private ExtendedBot _robot;
 	private BotInfo _target;
 	private int _debug = 0;
 	
 	/**
 	 * 
 	 */
 	public Enemies(ExtendedBot robot) {
 		_robot = robot;
 	}
 	
 	public Collection<BotInfo> getEnemies() {
 		return _enemyTable.values();
 	}
 	
 	/**
 	 * Updates records of enemies
 	 * @param e ScannedRobotEvent info about a robot
 	 * @param enemies list of all known enemies
 	 */
 	public void update(ScannedRobotEvent e) {
 		// Hack because of asterisk error
 		String enemyName = e.getName().replace(" (", "* (");
 		
 		BotInfo enemyBot = new BotInfo(e, _robot);
 		if(_target == null) {
 			_target = enemyBot;
 		}
 
 		// Don't track team-mates
 		if (_robot.isTeammate(enemyName)) {
 			return;
 		}
 		
 		// Update enemy database
 		if(_enemyTable.containsKey(enemyName)) {
 			_enemyTable.get(enemyName).update(e);
 		} else {
 			_enemyTable.put(enemyName, enemyBot);
 		}
 	}
 	
 	public void update(RobotDeathEvent e) {
 		// Hack because of asterisk error
 		String enemyName = e.getName().replace(" (", "* (");
 		if(_debug >= 1) System.out.println("Removing: "+ enemyName);
 		_enemyTable.remove(enemyName);
 		
 		if(_target.getName().equals(enemyName)) {
 			_target = pickRandomTarget();
 			if(_target != null) System.out.println("Target is now: "+ _target.getName());
 		}
 	}
 	
 	public BotInfo pickRandomTarget() {
 		if(_enemyTable.size() >= 1) {
 			Enumeration<String> enemyNames = _enemyTable.keys();
 			BotInfo target = _enemyTable.get(enemyNames.nextElement()); 
 			return target;
 		} else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Chooses target with lowest energy
	 * @return EnemyBot with lowest energy, or null if no known enemies
 	 */
 	public BotInfo pickByLowestEnergy() {
 		List<BotInfo> enemies = new ArrayList<BotInfo>(_enemyTable.values());
 		Collections.sort(enemies);
 		for(BotInfo enemy : enemies) {
 			if(_debug >= 1) System.out.println("Enemy "+ enemy.getName() + ": "+ enemy.getEnergy());
 		}
 		if(enemies.size() > 0) {
 			return enemies.get(0);
 		} else {
 			return null;
 		}
 	}
 
 	public BotInfo get(ScannedRobotEvent e) {
 		return get(BotUtility.fixName(e.getName()));
 	}
 	
 	public BotInfo get(String enemyName) {
 		return _enemyTable.get(enemyName);
 	}
 	
 	public void remove(RobotDeathEvent e) {
 		_enemyTable.remove(BotUtility.fixName(e.getName()));
 	}
 	
 	public void setTarget(BotInfo target) {
 		_target = target;
 	}
 	
 	public BotInfo getTarget() {
 		return _target;
 	}
 	
 	public int size() {
 		return _enemyTable.size();
 	}
 	
 	/**
 	 * Paint all enemies on the battlefield
 	 * @param g GraphicsObject
 	 * @param enemies All known enemies
 	 */
 	public void paintAll(Graphics2D g) {
 		for(BotInfo enemy : _enemyTable.values()) {
 			enemy.paintTrackingRectangle(_robot,g);
 		}
 	}
 	
 
 	public boolean isCurrentTarget(ScannedRobotEvent e) {
 		String targetName = getTarget().getName();
 		String enemyName = BotUtility.fixName(e.getName());
 		return (targetName.equals(enemyName));
 	}
 }
