 /**
  * The Character class provides stats for all characters and monsters.
  */
 
 package yuuki.entity;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import yuuki.action.Action;
 import yuuki.buff.Buff;
 
 public abstract class Character {
 
 	/**
 	 * The multiplier used in calculating required experience.
 	 */
 	protected static final int XP_MULTIPLIER = 50;
 	
 	/**
 	 * The base used in calculating required experience.
 	 */
 	protected static final double XP_BASE = 2.0;
 	
 	/**
 	 * The moves that this Character can perform.
 	 */
 	protected Action[] moves;
 	
 	/**
 	 * The experience of this Character.
 	 */
 	protected int xp;
 
 	/**
 	 * The level of this Character.
 	 */
 	protected int level;
 	
 	/**
 	 * The Name of the Character.
 	 */
 	private String name;	
 
 	/**
 	 * The hit point stat.
 	 */
 	private VariableStat hp;
 
 	/**
 	 * The mana points stat.
 	 */
 	private VariableStat mp;
 
 	/**
 	 * The physical damage dealt by the character.
 	 */
 	private Stat strength;
 
 	/**
 	 * Modifies damage taken.
 	 */
 	private Stat defense;
 
 	/**
 	 * The magical damage dealt by the character.
 	 */
 	private Stat magic;
 
 	/**
 	 * Modifies percent chance to avoid damage.
 	 */
 	private Stat agility;
 
 	/**
 	 * Modifies percent chance to hit.
 	 */
 	private Stat accuracy;
 
 	/**
 	 * Modifies critical strike percent.
 	 */
 	private Stat luck;
 	
 	/**
 	 * The Buffs that this Character has on it.
 	 */
 	private ArrayList<Buff> buffs;
 	
 	/**
 	 * The buffs that have just ended on this Character.
 	 */
 	private ArrayList<Buff> expiredBuffs;
 	
 	/**
 	 * The ID of this Character within its team during a battle.
 	 */
 	private int fighterId;
 	
 	/**
 	 * The ID of this Character's team during a battle.
 	 */
 	private int teamId;
 	
 	/**
 	 * The sprite for this Character in the GUI.
 	 * 
 	 * TODO: Make type more specific to get around type-casting.
 	 */
 	private javax.swing.JComponent sprite = null;
 	
 	/**
 	 * Calculates the experience required to be at a level.
 	 *
 	 * @param level The level to get the required experience for.
 	 *
 	 * @return The experience required to be the given level.
 	 */
 	public static final int getRequiredXP(int level) {
 		if (level == 1) {
 			return 0;
 		} else {
 			double power = Math.pow(XP_BASE, level - 1);
 			return (int) Math.floor(XP_MULTIPLIER * power);
 		}
 	}
 
 	/**
 	 * Allocates a new Character. Most stats are set manually, but experience
 	 * is automatically calculated from the starting level. All stats are the
 	 * base stats; all actual stats are calculated by multiplying the stat gain
 	 * by the level and adding the base stat.
 	 *
 	 * @param name The name of the Character.
 	 * @param level The level of the new Character. XP is set to match this.
 	 * @param moves The moves that this Character knows.
 	 * @param hp The health stat of the new Character.
 	 * @param mp The mana stat of the new Character.
 	 * @param strength The physical strength of the Character.
 	 * @param defense The Character's resistance to damage.
 	 * @param agility The Character's avoidance of hits.
 	 * @param accuracy The Character's ability to hit.
 	 * @param magic The magical ability of the Character.
 	 * @param luck The ability of the Character to get a critical hit.
 	 */
 	public Character(String name, int level, Action[] moves, VariableStat hp,
 					VariableStat mp, Stat strength, Stat defense, Stat agility,
 					Stat accuracy, Stat magic, Stat luck) {
 		if (level < 1) {
 			throw new IllegalArgumentException("Character level too low.");
 		}
 		hp.restore(level);
 		mp.restore(level);
 		this.name = name;
 		this.level = level;
 		this.moves = moves;
 		this.hp = hp;
 		this.mp = mp;
 		this.strength = strength;
 		this.defense = defense;
 		this.accuracy = accuracy;
 		this.agility = agility;
 		this.magic = magic;
 		this.luck = luck;
 		this.xp = Character.getRequiredXP(level);
 		this.fighterId = -1;
 		this.teamId = -1;
 		for (int i = 0; i < moves.length; i++) {
 			moves[i].setOrigin(this);
 		}
 	}
 	
 	/**
 	 * Gets the String version of this Character.
 	 *
 	 * @return the String version.
 	 */
 	public String toString() {
 		String strVer = getName() + " lv." + getLevel();
 		if (getFighterId() != -1) {
			strVer += " on team " + getTeamId();
 		}
 		return strVer;
 	}
 	
 	/**
 	 * Adds experience points to this Character. Adding enough XP to level up
 	 * this Character will not make it level up, but it will make the
 	 * canLevelUp method return true. That method should be checked after
 	 * adding experience to determine whether levelUp should then be called.
 	 *
 	 * @param amount
 	 * The amount of XP to add.
 	 */
 	public void addXP(int amount) {
 		xp += amount;
 	}
 	
 	/**
 	 * Checks whether this Character has enough experience points to advance to
 	 * the next level.
 	 *
 	 * @return True if the Character has enough XP to level up; otherwise
 	 * false.
 	 */
 	public boolean canLevelUp() {
 		int required = Character.getRequiredXP(level + 1);
 		return (xp >= required);
 	}
 	
 	/**
 	 * Levels up this Character. The level is increased by one and base stats
 	 * are increased by the given amounts. The current HP and MP are reset to
 	 * their maximum value.
 	 *
 	 * @param hp The amount to increase base HP by.
 	 * @param mp The amount to increase base MP by.
 	 * @param str The amount to increase base strength by.
 	 * @param def The amount to increase base defense by.
 	 * @param agt The amount to increase base agility by.
 	 * @param acc The amount to increase base accuracy by.
 	 * @param mag The amount to increase base magic by.
 	 * @param luck The amount to increase base luck by.
 	 */
 	public void levelUp(int hp, int mp, int str, int def, int agt, int acc,
 						 int mag, int luck) {
 		level++;
 		this.hp.increaseBase(hp);
 		this.mp.increaseBase(mp);
 		this.strength.increaseBase(str);
 		this.defense.increaseBase(def);
 		this.agility.increaseBase(agt);
 		this.accuracy.increaseBase(acc);
 		this.magic.increaseBase(mag);
 		this.luck.increaseBase(luck);
 		this.hp.restore(level);
 		this.mp.restore(level);
 	}
 	
 	/**
 	 * Checks whether this Character is alive.
 	 *
 	 * @return True if this Character is alive; otherwise, false.
 	 */
 	public boolean isAlive() {
 		return (hp.getCurrent() >= 1);
 	}
 	
 	/**
 	 * Gets the name of this Character.
 	 * 
 	 * @return The name of the Character.
 	 */
 	public String getName() {
 			return name;
 	}
         
 	/**
 	 * Gets the level of this Character.
 	 *
 	 * @return The level of this Character.
 	 */
 	public int getLevel() {
 		return level;
 	}
 
 	/**
 	 * Gets the HP stat of this Character.
 	 *
 	 * @return The HP stat.
 	 */
 	public VariableStat getHPStat() {
 		return hp;
 	}
 	
 	/**
 	 * Gets the max HP of this Character.
 	 * 
 	 * @return The max HP.
 	 */
 	public int getMaxHP() {
 		return hp.getMax(level);
 	}
 	
 	/**
 	 * Gets the current HP of this Character.
 	 * 
 	 * @return The current HP.
 	 */
 	public int getHP() {
 		return hp.getCurrent();
 	}
 	
 	/**
 	 * Causes the HP to decrease by the specified amount.
 	 * 
 	 * @param amount The amount to decrease by.
 	 * 
 	 * @return The actual amount that HP decreased by.
 	 */
 	public int loseHP(int amount) {
 		int old = hp.getCurrent();
 		hp.lose(amount);
 		int actual = old - hp.getCurrent();
 		return actual;
 	}
 	
 	/**
 	 * Causes the HP increase by the specified amount.
 	 * 
 	 * @param amount the amount to increase by.
 	 * 
 	 * @return The actual amount that HP increased by.
 	 */
 	public int gainHP(int amount) {
 		int old = hp.getCurrent();
 		hp.gain(amount, level);
 		int actual = hp.getCurrent() - old;
 		return actual;
 	}
 	
 	/**
 	 * Immediately sets HP to 0.
 	 */
 	public void drainHP() {
 		hp.drain();
 	}
 	
 	/**
 	 * Immediately sets HP to max.
 	 */
 	public void restoreHP() {
 		hp.restore(level);
 	}
 	
 	/**
 	 * Sets a modifier on HP.
 	 * 
 	 * @param mod The amount of the modifier to add.
 	 */
 	public void addHPMod(double mod) {
 		hp.addModifier(mod, level);
 	}
 	
 	/**
 	 * Removes a modifier from HP.
 	 * 
 	 * @param mod The amount of the modifier to remove.
 	 */
 	public void removeHPMod(double mod) {
 		hp.removeModifier(mod, level);
 	}
 
 	/**
 	 * Gets the MP stat of this Character.
 	 *
 	 * @return The MP stat.
 	 */
 	public VariableStat getMPStat() {
 		return mp;
 	}
 	
 	/**
 	 * Gets the max MP of this Character.
 	 * 
 	 * @return The max MP.
 	 */
 	public int getMaxMP() {
 		return mp.getMax(level);
 	}
 	
 	/**
 	 * Gets the amount of MP for this character.
 	 * 
 	 * @return The current amount of MP.
 	 */
 	public int getMP() {
 		return mp.getEffective(level);
 	}
 	
 	/**
 	 * Causes the MP to decrease by the specified amount.
 	 * 
 	 * @param amount The amount to decrease by.
 	 * 
 	 * @return The actual amount that MP decreased by.
 	 */
 	public int loseMP(int amount) {
 		int old = mp.getCurrent();
 		mp.lose(amount);
 		int actual = old - mp.getCurrent();
 		return actual;
 	}
 	
 	/**
 	 * Causes the MP increase by the specified amount.
 	 * 
 	 * @param amount the amount to increase by.
 	 * 
 	 * @return The actual amount that MP increased by.
 	 */
 	public int gainMP(int amount) {
 		int old = mp.getCurrent();
 		mp.gain(amount, level);
 		int actual = mp.getCurrent() - old;
 		return actual;
 	}
 	
 	/**
 	 * Immediately sets MP to 0.
 	 */
 	public void drainMP() {
 		mp.drain();
 	}
 	
 	/**
 	 * Immediately sets MP to max.
 	 */
 	public void restoreMP() {
 		mp.restore(level);
 	}
 	
 	/**
 	 * Sets a modifier on MP.
 	 * 
 	 * @param mod The amount of the modifier to add.
 	 */
 	public void addMPMod(double mod) {
 		mp.addModifier(mod, level);
 	}
 	
 	/**
 	 * Removes a modifier from MP.
 	 * 
 	 * @param mod The amount of the modifier to remove.
 	 */
 	public void removeMPMod(double mod) {
 		mp.removeModifier(mod, level);
 	}
 	
 	/**
 	 * Gets this Character's experience points.
 	 *
 	 * @return The number of experience points of this Character.
 	 */
 	public int getXP() {
 		return xp;
 	}
 
 	/**
 	 * Gets the strength Stat of this Character.
 	 *
 	 * @return The strength Stat.
 	 */
 	public Stat getStrengthStat() {
 		return strength;
 	}
 	
 	/**
 	 * Gets the current effective strength of this Character.
 	 * 
 	 * @return The effective value of the stat.
 	 */
 	public int getStrength() {
 		return strength.getEffective(level);
 	}
 	
 	/**
 	 * Adds a modifier to the strength stat.
 	 * 
 	 * @param mod The amount of modifier to add.
 	 */
 	public void addStrengthMod(double mod) {
 		strength.addModifier(mod);
 	}
 	
 	/**
 	 * Removes a modifier from the strength stat.
 	 * 
 	 * @param mod The amount of modifier to remove.
 	 */
 	public void removeStrengthMod(double mod) {
 		strength.removeModifier(mod);
 	}
 
 	/**
 	 * Gets the defense Stat of this Character.
 	 *
 	 * @return The defense Stat.
 	 */
 	public Stat getDefenseStat() {
 		return defense;
 	}
 	
 	/**
 	 * Gets the current effective defense of this Character.
 	 * 
 	 * @return The effective value of the stat.
 	 */
 	public int getDefense() {
 		return defense.getEffective(level);
 	}
 	
 	/**
 	 * Adds a modifier to the defense stat.
 	 * 
 	 * @param mod The amount of modifier to add.
 	 */
 	public void addDefenseMod(double mod) {
 		defense.addModifier(mod);
 	}
 	
 	/**
 	 * Removes a modifier from the defense stat.
 	 * 
 	 * @param mod The amount of modifier to remove.
 	 */
 	public void removeDefenseMod(double mod) {
 		defense.removeModifier(mod);
 	}
 
 	/**
 	 * Gets the magic Stat of this Character.
 	 *
 	 * @return The magic Stat.
 	 */
 	public Stat getMagicStat() {
 		return magic;
 	}
 	
 	/**
 	 * Gets the current effective magic of this Character.
 	 * 
 	 * @return The effective value of the stat.
 	 */
 	public int getMagic() {
 		return magic.getEffective(level);
 	}
 	
 	/**
 	 * Adds a modifier to the magic stat.
 	 * 
 	 * @param mod The amount of modifier to add.
 	 */
 	public void addMagicMod(double mod) {
 		magic.addModifier(mod);
 	}
 	
 	/**
 	 * Removes a modifier from the magic stat.
 	 * 
 	 * @param mod The amount of modifier to remove.
 	 */
 	public void removeMagicMod(double mod) {
 		magic.removeModifier(mod);
 	}
 
 	/**
 	 * Gets the agility Stat of this Character.
 	 *
 	 * @return The agility Stat.
 	 */
 	public Stat getAgilityStat() {
 		return agility;
 	}
 	
 	/**
 	 * Gets the current effective agility of this Character.
 	 * 
 	 * @return The effective value of the stat.
 	 */
 	public int getAgility() {
 		return agility.getEffective(level);
 	}
 	
 	/**
 	 * Adds a modifier to the agility stat.
 	 * 
 	 * @param mod The amount of modifier to add.
 	 */
 	public void addAgilityMod(double mod) {
 		agility.addModifier(mod);
 	}
 	
 	/**
 	 * Removes a modifier from the agility stat.
 	 * 
 	 * @param mod The amount of modifier to remove.
 	 */
 	public void removeAgilityMod(double mod) {
 		agility.removeModifier(mod);
 	}
 
 	/**
 	 * Gets the accuracy Stat of this Character.
 	 *
 	 * @return The accuracy Stat.
 	 */
 	public Stat getAccuracyStat() {
 		return accuracy;
 	}
 	
 	/**
 	 * Gets the current effective accuracy of this Character.
 	 * 
 	 * @return The effective value of the stat.
 	 */
 	public int getAccuracy() {
 		return accuracy.getEffective(level);
 	}
 	
 	/**
 	 * Adds a modifier to the accuracy stat.
 	 * 
 	 * @param mod The amount of modifier to add.
 	 */
 	public void addAccuracyMod(double mod) {
 		accuracy.addModifier(mod);
 	}
 	
 	/**
 	 * Removes a modifier from the accuracy stat.
 	 * 
 	 * @param mod The amount of modifier to remove.
 	 */
 	public void removeAccuracyMod(double mod) {
 		accuracy.removeModifier(mod);
 	}
 
 	/**
 	 * Gets the luck Stat of this Character.
 	 *
 	 * @return The luck Stat.
 	 */
 	public Stat getLuckStat() {
 		return luck;
 	}
 	
 	/**
 	 * Gets the current effective luck of this Character.
 	 * 
 	 * @return The effective value of the stat.
 	 */
 	public int getLuck() {
 		return luck.getEffective(level);
 	}
 	
 	/**
 	 * Adds a modifier to the luck stat.
 	 * 
 	 * @param mod The amount of modifier to add.
 	 */
 	public void addLuckMod(double mod) {
 		luck.addModifier(mod);
 	}
 	
 	/**
 	 * Removes a modifier from the luck stat.
 	 * 
 	 * @param mod The amount of modifier to remove.
 	 */
 	public void removeLuckMod(double mod) {
 		luck.removeModifier(mod);
 	}
 	
 	/**
 	 * Gets a modified stat list for this character. Each item will be a stat
 	 * with a mod on it. The stats are cloned so that they may not be modified.
 	 * 
 	 * @return An array with the modified stats as the first index and the
 	 * unmodified stats as the second index.
 	 */
 	public ArrayList<ArrayList<Stat>> getStatModList() {
 		ArrayList<ArrayList<Stat>> list = new ArrayList<ArrayList<Stat>>(2);
 		ArrayList<Stat> modded = new ArrayList<Stat>();
 		ArrayList<Stat> unmodded = new ArrayList<Stat>();
 		list.add(modded);
 		list.add(unmodded);
 		(hp.hasModifier() ? modded : unmodded).add(hp.clone());
 		(mp.hasModifier() ? modded : unmodded).add(mp.clone());
 		(strength.hasModifier() ? modded : unmodded).add(strength.clone());
 		(defense.hasModifier() ? modded : unmodded).add(defense.clone());
 		(agility.hasModifier() ? modded : unmodded).add(agility.clone());
 		(accuracy.hasModifier() ? modded : unmodded).add(accuracy.clone());
 		(magic.hasModifier() ? modded : unmodded).add(magic.clone());
 		(luck.hasModifier() ? modded : unmodded).add(luck.clone());
 		return list;
 	}
 	
 	/**
 	 * Gets the Buffs currently on this Character.
 	 *
 	 * @return The Buffs.
 	 */
 	public ArrayList<Buff> getBuffs() {
 		return buffs;
 	}
 	
 	/**
 	 * Gets the expired buffs on this Character.
 	 *
 	 * @return The expired buffs.
 	 */
 	public ArrayList<Buff> getExpiredBuffs() {
 		return expiredBuffs;
 	}
 	
 	/**
 	 * Gets the fighter ID of this Character.
 	 *
 	 * @return The ID if this Character is in a Battle, otherwise -1.
 	 */
 	public int getFighterId() {
 		return fighterId;
 	}
 	
 	/**
 	 * Gets the team ID of this Character.
 	 *
 	 * @return The ID if this Character is in a Battle, otherwise -1.
 	 */
 	public int getTeamId() {
 		return teamId;
 	}
 	
 	/**
 	 * Puts a buff on this Character.
 	 *
 	 * @param b The buff to add.
 	 */
 	public void addBuff(Buff b) {
 		b.setTarget(this);
 		buffs.add(b);
 	}
 	
 	/**
 	 * Sets the sprite for this Character.
 	 * 
 	 * @param sprite The sprite to set it to.
 	 */
 	public void setSprite(javax.swing.JComponent sprite) {
 		this.sprite = sprite;
 	}
 	
 	/**
 	 * Gets the sprite for this Character.
 	 * 
 	 * @return The sprite that this Character is set to use.
 	 */
 	public javax.swing.JComponent getSprite() {
 		return sprite;
 	}
 	
 	/**
 	 * Removes buffs that are no longer active.
 	 */
 	public void removeExpiredBuffs() {
 		Iterator<Buff> it = buffs.iterator();
 		while (it.hasNext()) {
 			Buff b = it.next();
 			if (b.isExpired()) {
 				it.remove();
 				expiredBuffs.add(b);
 			}
 		}
 	}
 	
 	/**
 	 * Clears the expired buffs list.
 	 */
 	public void emptyExpiredBuffs() {
 		expiredBuffs.clear();
 	}
 	
 	/**
 	 * Applies all current buffs to this Character.
 	 */
 	public void applyBuffs() {
 		for (Buff b: buffs) {
 			b.apply();
 		}
 	}
 	
 	/**
 	 * Decides what move to do next based on the states of other players in the
 	 * battle.
 	 *
 	 * @param fighters The states of the other players.
 	 *
 	 * @return The move that this Character wishes to perform.
 	 */
 	public Action getNextAction(
 				ArrayList<ArrayList<Character>> fighters) {
 		Action m = selectAction(fighters);
 		if (m.getTargets().isEmpty()) {
 			m.addTarget(selectTarget(fighters));
 		}
 		return m;
 	}
 	
 	/**
 	 * Selects the Action to do based on the other players. The action's
 	 * target is not set.
 	 *
 	 * @param fighters The states of the other players.
 	 *
 	 * @return The selected Action without a target.
 	 */
 	protected abstract Action selectAction(
 				ArrayList<ArrayList<Character>> fighters);
 	
 	/**
 	 * Selects the target of an action based on the other players.
 	 *
 	 * @param fighters The states of the other players.
 	 *
 	 * @return The target.
 	 */
 	protected abstract Character selectTarget(
 				ArrayList<ArrayList<Character>> fighters);
 	
 	/**
 	 * Gets the moves that this Character knows.
 	 *
 	 * @return The moves.
 	 */
 	public Action[] getMoves() {
 		return moves;
 	}
 	
 	/**
 	 * Sets up the properties needed for fighting.
 	 *
 	 * @param id The fighter ID of this Character in the battle.
 	 * @param team The team ID of this Character in the battle.
 	 */
 	public void startFighting(int id, int team) {
 		fighterId = id;
 		teamId = team;
 		buffs = new ArrayList<Buff>();
 		expiredBuffs = new ArrayList<Buff>();
 	}
 	
 	/**
 	 * Resets the properties needed for fighting to their default values.
 	 */
 	public void stopFighting() {
 		fighterId = -1;
 		teamId = -1;
 		buffs = null;
 		expiredBuffs = null;
 	}
 	
 	/**
 	 * Sets team Id.
 	 *
 	 * @param Id The new team ID.
 	 */
 	public void setTeamId(int id) {
 		teamId = id;
 	}
 	
 	/**
 	 * Sets fighter Id.
 	 *
 	 * @param Id The new fighter ID.
 	 */
 	public void setFighterId(int id) {
 		fighterId = id;
 	}
 }
