 import java.util.*;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  * 
  * @author Jeff
  */
 public class CombatEngine {
 	private Random randomNumberGenerator = new Random();
 	int accumulatedExp = 0;
 	Entity actor;
 	ArrayList<Monster> enemies;
 	String event;
 	boolean combatOver;
 	Stack<Entity> turnStack;
 	int combatResult;
 	ArrayList<Entity> combatants;
 	ArrayList<Character> characters;
 	CombatGUI gui;
 
 	public CombatEngine(ArrayList<Character> charactersIn) {
 		characters = charactersIn;
 		enemies = initializeEnemies(0);
 		actor = null;
 		event = null;
 		combatOver = false;
 		turnStack = new Stack<Entity>();
 		combatResult = 0;
 		combatants = new ArrayList<Entity>();
 
 		for (Character c : characters) {
 			combatants.add(c);
 		}
 		for (Monster m : enemies) {
 			combatants.add(m);
 		}
 
 		// determine turn order
 		combatants = sortTurnOrderArray(combatants);
 
 		// populate turn stack for first round
 		for (Entity e : combatants) {
 			turnStack.push(e);
 		}
 
 		// gui = new CombatGUI(this);
 
 	}
 
 	public void endTurn() {
 		System.out.println(event);
 		gui.info.setText(event);
 
 		// clean out dead combatants
 		characters = cleanCharacterList(characters);
 		enemies = cleanMonsterList(enemies);
 		combatants = mergeCombatantArrays(enemies, characters);
 
 		if (enemies.size() < 1) {
 			combatOver = true;
 			combatResult = 1;
 		}
 
 		if (characters.size() < 1) {
 			combatOver = true;
 			combatResult = -1;
 		}
 		// peek at next Entity in stack to make sure they are alive
 		if (!turnStack.empty() && !turnStack.peek().alive()) {
 			turnStack.pop();
 		}
 
 		// if stack is empty, refill it
 		if (turnStack.empty()) {
 			for (Entity e : combatants) {
 				turnStack.push(e);
 			}
 		}
 
 		if (combatOver) {
 			endCombat(characters, accumulatedExp, combatResult);
 			System.out.println("Combat Over");
 		}
 
 		gui.update();
 	}
 
 	public void monsterTurn (Monster m) throws InterruptedException {
 		
 		Thread.sleep(1000);
 		String action = m.takeTurn();
 		if (action.equals("attack")) {
 			event = executeAttack(m, characters.get(0));
 		} else {
 			event = executeAbility(m, 0, enemies, characters,
 					actor.abilities.get(0));
 		}
 
 		turnStack.pop();
 		endTurn();
 	}
 
 	public void playerTurn() {
 		String action = gui.attacks.getSelectedItem().toString();
 		int target = gui.targets.getSelectedIndex();
 
 		actor = turnStack.pop();
 
 		// start character turn
 
 		// actions for character
 		if (action.equals("Attack")) {
 			event = executeAttack(actor, combatants.get(target));
 		}
 
 		else if (action.equals("Wait")) {
 			event = actor.getName() + " waits.";
 		}
 
 		else if (action.equals("Flee")) {
 			if (attemptToFlee(characters.get(0))) {
 				event = "You fled successfully!";
 				combatOver = true;
 				combatResult = 0;
 			} else {
 				event = "Your attempt to flee failed!";
 			}
 		}
 
 		else {
 			Ability activeAbility = actor.getAbilityByName(action);
 			if (activeAbility.friendly()) {
				event = executeAbility(actor, 0, enemies, characters,
 						activeAbility);
 			} else {
				event = executeAbility(actor, 0, enemies, characters,
 						activeAbility);
 			}
 		}
 
 		endTurn();
 
 	}
 
 	public void combat() throws InterruptedException{
 		while (!combatOver) {
 			Entity a = turnStack.peek();
 			if (a instanceof Character) {
 				;
 			}
 			if (a instanceof Monster) {
 				monsterTurn((Monster) a);
 			}
 		}
 	}
 
 	/**
 	 * public ArrayList<Character> combat() throws InterruptedException {
 	 * 
 	 * String action = null;
 	 * 
 	 * // while (!combatOver) {
 	 * 
 	 * // display health messages System.out.println(); for (Entity e :
 	 * combatants) { System.out.println(e.getName() + "'s health: " +
 	 * e.getCurrentHealth() + "/" + e.getMaxHealth()); }
 	 * 
 	 * actor = turnStack.pop();
 	 * 
 	 * // start character turn if (actor instanceof Character) { action =
 	 * ((Character) actor).takeTurn();
 	 * 
 	 * // actions for character if (action.equals("attack")) { event =
 	 * executeAttack(actor, enemies.get(0)); }
 	 * 
 	 * else if (action.equals("wait")) { event = actor.getName() + " waits."; }
 	 * 
 	 * else if (action.equals("flee")) { if (attemptToFlee(characters.get(0))) {
 	 * event = "You fled successfully!"; combatOver = true; combatResult = 0; }
 	 * else { event = "Your attempt to flee failed!"; } }
 	 * 
 	 * else { Ability activeAbility = actor.getAbilityByName(action); if
 	 * (activeAbility.friendly()) { event = executeAbility(actor, 0, enemies,
 	 * characters, activeAbility); } else { event = executeAbility(actor, 0,
 	 * enemies, characters, activeAbility); } } // end character turn
 	 * 
 	 * // start monster turn } else if (actor instanceof Monster) { action =
 	 * ((Monster) actor).takeTurn(); if (action.equals("attack")) { event =
 	 * executeAttack(actor, characters.get(0)); } else { event =
 	 * executeAbility(actor, 0, enemies, characters, actor.abilities.get(0)); }
 	 * }
 	 * 
 	 * // end monster turn
 	 * 
 	 * System.out.println(event);
 	 * 
 	 * // clean out dead combatants characters = cleanCharacterList(characters);
 	 * enemies = cleanMonsterList(enemies); combatants =
 	 * mergeCombatantArrays(enemies, characters);
 	 * 
 	 * if (enemies.size() < 1) { combatOver = true; combatResult = 1; }
 	 * 
 	 * if (characters.size() < 1) { combatOver = true; combatResult = -1; } //
 	 * peek at next Entity in stack to make sure they are alive if
 	 * (!turnStack.empty() && !turnStack.peek().alive()) { turnStack.pop(); }
 	 * 
 	 * // if stack is empty, refill it if (turnStack.empty()) { for (Entity e :
 	 * combatants) { turnStack.push(e); } } // }
 	 * 
 	 * if (combatOver) { endCombat(characters, accumulatedExp, combatResult);
 	 * System.out.println("Combat Over"); } return characters; }
 	 **/
 
 	public String executeAttack(Entity source, Entity target) {
 		int calculatedDamage = 0;
 		int attackVal = source.getAttack() + randomNumberGenerator.nextInt(6);
 		int defenseVal = target.getDefense() + randomNumberGenerator.nextInt(6);
 
 		calculatedDamage = attackVal - (defenseVal / 2);
 		if (calculatedDamage < 0) {
 			calculatedDamage = 0;
 		}
 
 		target.setCurrentHealth(target.getCurrentHealth() - calculatedDamage);
 		return (source.getName() + "'s attack hit " + target.getName()
 				+ " for " + calculatedDamage + " damage!");
 	}
 
 	/*
 	 * TYPE 0 -- damage 1 -- heal 2 -- statusmod
 	 * 
 	 * SCOPE 0 -- single target 1 -- splash
 	 */
 	public String executeAbility(Entity source, int targetID,
 			ArrayList<? extends Entity> enemies,
 			ArrayList<? extends Entity> allies, Ability ability) {
 
 		String result = null;
 
 		switch (ability.getType()) {
 
 		// damage ability
 		case 0:
 
 			// single target
 			if (ability.getScope() == 0) {
 				int calculatedDamage = 0;
 				int attackVal = source.getAttack()
 						+ randomNumberGenerator.nextInt(6);
 				int defenseVal = enemies.get(targetID).getDefense()
 						+ randomNumberGenerator.nextInt(6);
 
 				calculatedDamage = attackVal - (defenseVal / 2);
 				if (calculatedDamage < 0) {
 					calculatedDamage = 0;
 				}
 
 				enemies.get(targetID).setCurrentHealth(
 						enemies.get(targetID).getCurrentHealth()
 								- calculatedDamage);
 				result = (source.getName() + "'s " + ability.getName()
 						+ " hit " + enemies.get(targetID).getName() + " for "
 						+ calculatedDamage + " damage!");
 			}
 
 			// splash
 			else {
 
 				int avgDefense = 0;
 				// average enemies defense values
 				for (Entity e : enemies) {
 					avgDefense += e.getDefense();
 				}
 
 				avgDefense /= enemies.size();
 
 				int calculatedDamage = 0;
 				int attackVal = source.getAttack()
 						+ randomNumberGenerator.nextInt(6);
 				int defenseVal = avgDefense + randomNumberGenerator.nextInt(6);
 
 				calculatedDamage = attackVal - (defenseVal / 2);
 				if (calculatedDamage < 0) {
 					calculatedDamage = 0;
 				}
 
 				for (Entity e : enemies) {
 					e.setCurrentHealth(e.getCurrentHealth() - calculatedDamage);
 					result = (source.getName() + "'s " + ability.getName()
 							+ " hit all enemies for " + calculatedDamage + " damage!");
 				}
 			}
 
 			break;
 
 		// healing ability
 		case 1:
 			int amountHealed = source.getAttack()
 					+ randomNumberGenerator.nextInt(6);
 			combatants.get(targetID).setCurrentHealth(
 					combatants.get(targetID).getCurrentHealth() + amountHealed);
 			if (combatants.get(targetID).getCurrentHealth() > combatants.get(
 					targetID).getMaxHealth()) {
 				combatants.get(targetID).setCurrentHealth(
 						combatants.get(targetID).getMaxHealth());
 				return (combatants.get(targetID).getName()
 						+ " was fully healed by " + source.getName() + "'s "
 						+ ability.getName() + "!");
 			} else {
 				return (combatants.get(targetID).getName() + " was healed for "
 						+ amountHealed + " damage by " + source.getName()
 						+ "'s " + ability.getName() + "!");
 			}
 
 		}
 
 		return result;
 
 	}
 
 	public boolean attemptToFlee(Character character) {
 		boolean result;
 
 		int roll = randomNumberGenerator.nextInt(3);
 		if (roll == 2) {
 			result = true;
 		} else {
 			result = false;
 		}
 
 		return result;
 	}
 
 	// returns array of type Entity sorted by speed. Lowest speed is first.
 	public ArrayList<Entity> sortTurnOrderArray(ArrayList<Entity> combatants) {
 		Collections.sort(combatants);
 
 		return combatants;
 	}
 
 	public ArrayList<Monster> initializeEnemies(int zoneID) {
 		ArrayList<Monster> monsterArray = new ArrayList<Monster>();
 		int numberOfEnemies = randomNumberGenerator.nextInt(5) + 1;
 		Monster m;
 
 		for (int i = 1; i <= numberOfEnemies; i++) {
 			m = new Monster("Koopa Troopa", 10, 10, 5, 5, 1, 3);
 			Ability cure = new Ability("heal", 1, 0, 0);
 			m.abilities.add(cure);
 			m.setCombatID(i);
 			m.setName(m.getName() + " " + i);
 			monsterArray.add(m);
 		}
 		return monsterArray;
 	}
 
 	private boolean endCombat(ArrayList<Character> characters, int experience,
 			int result) {
 		if (result == 0) {
 			// code to handle running away (nothing happens)
 			return true;
 		} else {
 			if (result == 1) {
 				System.out.println("Victory!\n");
 				for (Character character : characters) {
 					character.setExp(character.getExp() + experience);
 					System.out.println(character.getName() + " has earned "
 							+ experience + " experience!");
 				}
 				System.out.println();
 				return true;
 			} else {
 				System.out.println("Defeat!");
 				return false;
 			}
 		}
 	}
 
 	private ArrayList<Monster> cleanMonsterList(ArrayList<Monster> entities) {
 		ArrayList<Monster> newArray = new ArrayList<Monster>();
 
 		for (Monster e : entities) {
 			if (e.getCurrentHealth() > 0) {
 				newArray.add(e);
 			} else {
 				System.out.println(e.getName() + " was slain!");
 				accumulatedExp += e.getExpValue();
 			}
 		}
 		return newArray;
 	}
 
 	private ArrayList<Character> cleanCharacterList(
 			ArrayList<Character> entities) {
 		ArrayList<Character> newArray = new ArrayList<Character>();
 
 		for (Character e : entities) {
 			if (e.getCurrentHealth() > 0) {
 				newArray.add(e);
 			} else {
 				System.out.println(e.getName() + " was slain!");
 			}
 		}
 		return newArray;
 	}
 
 	private ArrayList<Entity> mergeCombatantArrays(
 			ArrayList<? extends Entity> a, ArrayList<? extends Entity> b) {
 		ArrayList<Entity> newArray = new ArrayList<Entity>();
 		for (Entity e : a) {
 			newArray.add(e);
 		}
 		for (Entity e : b) {
 			newArray.add(e);
 		}
 
 		return newArray;
 	}
 }
