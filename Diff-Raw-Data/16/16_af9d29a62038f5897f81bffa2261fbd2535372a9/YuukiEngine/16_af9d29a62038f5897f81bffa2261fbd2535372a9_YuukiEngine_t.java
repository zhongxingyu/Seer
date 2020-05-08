 /**
  * The game engine for the Yuuki JRPG project. This class may be executed
  * directly to run Yuuki.
  */
 
 package yuuki;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;

import yuuki.ui.GraphicalInterface;
 import yuuki.ui.Interactable;
 import yuuki.ui.StreamInterface;
 import yuuki.battle.Battle;
 import yuuki.action.*;
 import yuuki.buff.*;
 import yuuki.entity.Character;
 import yuuki.entity.*;
 
 public class YuukiEngine implements Runnable {
 
 	/**
 	 * The user interface.
 	 */
 	private Interactable ui;
 	
 	/**
 	 * The player character.
 	 */
 	private PlayerCharacter player;
 	
 	/**
 	 * Program execution hook. Creates a new thread in which to execute the
 	 * game engine in and then starts the thread.
 	 *
 	 * @param args Command line arguments. Not used.
 	 */
 	public static void main(String[] args) {
		YuukiEngine gameEngine = new YuukiEngine();
 		Thread gameThread = new Thread(gameEngine, "GameEngine");
 		gameThread.start();
 	}
 	
 	/**
 	 * Creates a new YuukiEngine with a UI that uses only streams.
 	 *
 	 * @param in The input stream.
 	 * @param out The output stream.
 	 * @param error The error stream.
 	 */
 	public YuukiEngine(InputStream in, OutputStream out, OutputStream error) {
 		ui = new StreamInterface(in, out, error);
 	}
 	
 	/**
	 * Creates a new YuukiEngine with a Swing-based GUI.
	 */
	public YuukiEngine() {
		ui = new GraphicalInterface();
	}
	
	/**
 	 * Runs the engine.
 	 */
 	public void run() {
 		boolean stillFighting = true;
 		ui.initialize();
 		ui.switchToIntroScreen();
 		createPlayer(10);
 		ui.switchToOverworldScreen();
 		while (stillFighting) {
 			battleOneOnOne(player, createJill());
 			stillFighting = ui.confirm("Battle again?", "Yes", "No");
 		}
 		ui.switchToEndingScreen();
 		ui.destroy();
 	}
 	
 	/**
 	 * Starts a battle between two characters. Switches to the battle screen,
 	 * runs the battle, then switches to the overworld screen.
 	 *
 	 * @param f1 The first fighter.
 	 * @param f2 The second fighter.
 	 *
 	 * @return The winner of the battle.
 	 */
 	private Character battleOneOnOne(Character f1, Character f2) {
 		Character[] t1 = {f1};
 		Character[] t2 = {f2};
 		Character[][] fighters = new Character[2][1];
 		fighters[0] = t1;
 		fighters[1] = t2;
 		ui.display(null, "Oh no! Random monsters!");
 		Battle b = new Battle(fighters);
 		showBattle(fighters, b);
 		Character winner = b.getFighters(0).get(0);
 		return winner;
 	}
 	
 	/**
 	 * Switches to the battle screen, runs through a battle, then switches to
 	 * the overworld screen.
 	 *
 	 * @param fighters The characters in the battle.
 	 * @param battle The battle to show.
 	 */
 	private void showBattle(Character[][] fighters, Battle b) {
 		ui.switchToBattleScreen(fighters);
 		runBattle(b);
 		ui.switchToOverworldScreen();
 	}
 	
 	/**
 	 * Runs a battle to completion. If the UI is switched to the battle screen,
 	 * it is displayed there; otherwise it is not displayed at all.
 	 *
 	 * @param battle The battle to run
 	 */
 	private void runBattle(Battle battle) {
 		while (battle.advance()) {
 			switch (battle.getLastState()) {
 				case STARTING_TURN:
 					outputTurnStart(battle);
 					break;
 					
 				case GETTING_ACTION:
 					outputActionGet(battle);
 					break;
 					
 				case APPLYING_ACTION:
 					outputActionApplication(battle);
 					break;
 					
 				case APPLYING_BUFFS:
 					outputBuffApplication(battle);
 					break;
 					
 				case CHECKING_DEATH:
 					outputDeathCheck(battle);
 					break;
 					
 				case ENDING_TURN:
 					outputTeamDeathCheck(battle);
 					break;
 					
 				case CHECKING_VICTORY:
 					// not sure we care about this state
 					break;
 					
 				case LOOTING:
 					outputLoot(battle);
 					break;
 					
 				default:
 					break;
 			}
 			if (battle.getState() == Battle.State.ENDING) {
 				outputVictory(battle);
 			}
 		}
 	}
 	
 	/**
 	 * Creates the player character.
 	 *
 	 * @param level The level of the player.
 	 */
 	private void createPlayer(int level) {
 		String name = ui.getString("Enter player name");
 		VariableStat hp = createHealthStat(0, 1);
 		VariableStat mp = createManaStat(0, 1);
 		Stat str = createStrengthStat(5, 1);
 		Stat def = createDefenseStat(5, 1);
 		Stat agi = createAgilityStat(5, 1);
 		Stat acc = createAccuracyStat(5, 1);
 		Stat mag = createMagicStat(5, 1);
 		Stat luk = createLuckStat(5, 1);
 		Action moves[] = createPlayerMoveSet();
 		this.player = new PlayerCharacter(name, level, moves, hp, mp, str, def,
 											agi, acc, mag, luk, ui);
 	}
 	
 	/**
 	 * Creates the move set for the player.
 	 *
 	 * @return The move set.
 	 */
 	private Action[] createPlayerMoveSet() {
 		Action[] moves = new Action[2];
 		moves[0] = new BasicAttack(3.0);
 		moves[1] = new BasicDefense(1);
 		return moves;
 	}
 	
 	/**
 	 * Creates the health stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private VariableStat createHealthStat(int base, int gain) {
 		return new VariableStat("health", base, gain);
 	}
 	
 	/**
 	 * Creates the mana stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private VariableStat createManaStat(int base, int gain) {
 		return new VariableStat("mana", base, gain);
 	}
 	
 	/**
 	 * Creates the strength stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private Stat createStrengthStat(int base, int gain) {
 		return new VariableStat("strength", base, gain);
 	}
 	
 	/**
 	 * Creates the defense stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private Stat createDefenseStat(int base, int gain) {
 		return new VariableStat("defense", base, gain);
 	}
 	
 	/**
 	 * Creates the agility stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private Stat createAgilityStat(int base, int gain) {
 		return new VariableStat("agility", base, gain);
 	}
 	
 	/**
 	 * Creates the accuracy stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private Stat createAccuracyStat(int base, int gain) {
 		return new VariableStat("accuracy", base, gain);
 	}
 	
 	/**
 	 * Creates the magic stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private Stat createMagicStat(int base, int gain) {
 		return new VariableStat("magic", base, gain);
 	}
 	
 	/**
 	 * Creates the luck stat.
 	 *
 	 * @param base The base value.
 	 * @param gain The gain per level.
 	 *
 	 * @return The new stat.
 	 */
 	private Stat createLuckStat(int base, int gain) {
 		return new VariableStat("luck", base, gain);
 	}
 	
 	/**
 	 * Creates the NPC Jack.
 	 *
 	 * @return Jack, the NPC.
 	 */
 	private NonPlayerCharacter createJack() {
 		VariableStat hp, mp;
 		Stat str, def, agi, acc, mag, luk;
 		hp = createHealthStat(0, 1);
 		mp = createManaStat(10, 3);
 		str = createStrengthStat(5, 1);
 		def = createDefenseStat(5, 1);
 		agi = createAgilityStat(5, 1);
 		acc = createAccuracyStat(5, 1);
 		mag = createMagicStat(5, 1);
 		luk = createLuckStat(5, 1);
 		Action[] moves = createBasicMoveSet();
 		int lvl = 5;
 		int xpb = 5;
 		NonPlayerCharacter c = null;
 		c = new NonPlayerCharacter("Jack", lvl, moves, hp, mp, str, def, agi,
 									acc, mag, luk, xpb);
 		return c;
 	}
 	
 	/**
 	 * Creates the NPC Jill.
 	 *
 	 * @return Jill, the NPC.
 	 */
 	private NonPlayerCharacter createJill() {
 		VariableStat hp, mp;
 		Stat str, def, agi, acc, mag, luk;
 		hp = createHealthStat(0, 1);
 		mp = createManaStat(10, 3);
 		str = createStrengthStat(5, 1);
 		def = createDefenseStat(5, 1);
 		agi = createAgilityStat(5, 1);
 		acc = createAccuracyStat(5, 1);
 		mag = createMagicStat(5, 1);
 		luk = createLuckStat(5, 1);
 		Action[] moves = createBasicMoveSet();
 		int lvl = 5;
 		int xpb = 5;
 		NonPlayerCharacter c = null;
 		c = new NonPlayerCharacter("Jill", lvl, moves, hp, mp, str, def, agi,
 									acc, mag, luk, xpb);
 		return c;
 	}
 	
 	/**
 	 * Creates the basic move set.
 	 *
 	 * @return The basic move set.
 	 */
 	private Action[] createBasicMoveSet() {
 		Action[] moves = new Action[2];
 		moves[0] = new BasicAttack(1.0);
 		moves[1] = new BasicDefense(1);
 		return moves;
 	}
 	
 	/**
 	 * Outputs the results of a battle's turn start phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputTurnStart(Battle battle) {
 		Character c = battle.getCurrentFighter();
 		int recoveredMana = battle.getRegeneratedMana();
 		ui.display(c, "It looks like I'm up next.");
 		ArrayList<Buff> expiredBuffs = c.getExpiredBuffs();
 		for (Buff expired: expiredBuffs) {
 			ui.showBuffDeactivation(expired);
 		}
 		if (recoveredMana != 0) {
 			ui.showRecovery(c, c.getMPStat(), recoveredMana);
 		}
 		ui.showStatUpdate(c);
 	}
 	
 	/**
 	 * Outputs the results of a battle's action get phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputActionGet(Battle battle) {
 		Action a = battle.getLastAction();
 		ui.showActionPreperation(a);
 	}
 	
 	/**
 	 * Outputs the results of a battle's action application phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputActionApplication(Battle battle) {
 		Action a = battle.getLastAction();
 		if (a.wasSuccessful()) {
 			if (a.getCostStat() != null) {
 				outputActionCost(a);
 			}
 			ui.showActionUse(a);
 			if (a.getEffectStat() != null) {
 				outputActionEffects(a);
 			}
 			if (a.getOriginBuff() != null) {
 				ui.showBuffActivation(a.getOriginBuff());
 			}
 			if (a.getTargetBuff() != null) {
 				ui.showBuffActivation(a.getTargetBuff());
 			}
 		} else {
 			ui.showActionFailure(a);
 		}
 	}
 	
 	/**
 	 * Outputs the results of an action cost to the user interface.
 	 *
 	 * @param action The Action to output.
 	 */
 	private void outputActionCost(Action a) {
 		ui.showDamage(a.getOrigin(), a.getCostStat(), (int)a.getCost());
 		ui.showStatUpdate(a.getOrigin());
 	}
 	
 	/**
 	 * Outputs the effects of an action to the user interface.
 	 *
 	 * @param a The Action to output.
 	 */
 	private void outputActionEffects(Action a) {
 		int[] effects = a.getActualEffects();
 		ArrayList<Character> targets = a.getTargets();
 		for (int i = 0; i < effects.length; i++) {
 			Character t = targets.get(i);
 			int damage = effects[i];
 			ui.showDamage(t, a.getEffectStat(), damage);
 			ui.showStatUpdate(t);
 		}
 	}
 	
 	/**
 	 * Outputs the results of a battle's buff application phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputBuffApplication(Battle battle) {
 		Character currentFighter = battle.getCurrentFighter();
 		ArrayList<Buff> buffs = currentFighter.getBuffs();
 		for (Buff b: buffs) {
 			ui.showBuffApplication(b);
 			ui.showStatUpdate(currentFighter);
 		}
 	}
 	
 	/**
 	 * Outputs the results of a battle's death check phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputDeathCheck(Battle battle) {
 		ArrayList<Character> removed = battle.getRemovedFighters();
 		for (Character c: removed) {
 			ui.showCharacterRemoval(c);
 		}
 	}
 	
 	/**
 	 * Outputs the results of a battle's team death check phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputTeamDeathCheck(Battle battle) {}
 	
 	/**
 	 * Outputs the results of a battle's loot calculation phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputLoot(Battle battle) {}
 	
 	/**
 	 * Outputs the results of a battle's victory check phase to the user
 	 * interface.
 	 *
 	 * @param battle The battle to output the state of.
 	 */
 	private void outputVictory(Battle battle) {}
 }
