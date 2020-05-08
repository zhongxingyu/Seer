 package com.avapir.roguelike.locatable;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 
 import com.avapir.roguelike.battle.Armor;
 import com.avapir.roguelike.battle.Attack;
 import com.avapir.roguelike.core.Game;
 import com.avapir.roguelike.game.Map;
 
 public class Hero extends Mob implements Locatable {
 
 	private static final class DefaultStats {//@formatter:off
 		/* 	STR 	AGI 	VIT 	INT 	DEX 	LUK */
 		static final int[]	PLAYER	= { 3, 		3, 		3, 		3, 		2, 		1 };	// 16
 //		static final int[]	PLAYER	= { 280, 		170,		230, 		90, 		70, 		47 };	// 16
 		static final int[]	NPC		= { 50, 	100, 	100, 	50, 	50, 	10 };	// 360
 		static final int[]	ELDER	= { 290, 	120,	390, 	700,	400, 	100 }; 	// 2000
 		static final int[]	UNDEAD	= { 120, 	40, 	120, 	0, 		40, 	0 };	// 320
 	}//@formatter:on
 
 	public static final class StatsFormulas {
 
 		public static float getMaxHP(final Hero h) {
 			final int baseHP = 2;
 			return baseHP + 4 * h.stats.getStr() + 7 * h.stats.getVit();
 		}
 
 		public static float getMaxMP(final Hero h) {
 			final int baseMP = 1;
 			return baseMP + 7 * h.stats.getInt();
 		}
 
 		public static double addBonusXp(final Hero h, final double xp) {
 			final Random r = new Random();
 			final double L = r.nextInt(h.stats.getLuk() * 2);
 			// up to 100% bonus for each 50 LUK
 			return (int) (xp * (1f + h.stats.getDex() / 300f + L / 100f));
 		}
 
 		public static int getFOVR(final Hero h) {
 			final int baseFOVR = 5;
 			return baseFOVR + h.stats.getInt() / 50;
 		}
 
 		public static int getATKR(final Hero h) {
 			final int AGI = h.stats.getAgi();
 			return AGI == 0 ? 0 : AGI < 50 ? 1 : AGI < 150 ? 2 : 3;
 		}
 
 		public static Attack getAttack(final Hero h) {
 			final float STR = h.stats.getStr();
 			final float DEX = h.stats.getDex();
 			final float INT = h.stats.getInt();
 			final float phy = 1.6f + STR + DEX * 0.4f + INT * 0.2f;
 			final float mag = 1.2f + INT + DEX * 0.4f;
 			System.out.println(STR + " " + DEX + " " + INT + " " + phy);
 			return new Attack(phy, mag);
 		}
 
 		public static Armor getArmor(final Hero h) {
 			final float STR = h.stats.getStr();
 			final float AGI = h.stats.getAgi();
 			final float INT = h.stats.getInt();
 			final float phy = AGI * 0.7f + STR * 0.3f;
 			final float mag = INT * 0.5f;
 			return new Armor(phy, mag);
 		}
 	}
 
 	public static final class PrimaryStats {
 
 		private void ac(final int[] a) {
 			System.arraycopy(a, 0, values, 0, PRIMARY_STATS_AMOUNT);
 		}
 
 		/**
 		 * STRength <br>
 		 * AGIlity <br>
 		 * VITality <br>
 		 * INTelligence <br>
 		 * DEXterity <br>
 		 * LUcK
 		 */
 		public static final int	PRIMARY_STATS_AMOUNT	= 6;
 		public static final int	DEFAULT_STAT_INCREASE	= 5;
 		private final int[]		values					= new int[PRIMARY_STATS_AMOUNT];
 		private int				freeStats				= 0;
 
 		//@formatter:off
 		public PrimaryStats(final String name) {
 			if (name.contains("NPC")) {				ac(DefaultStats.NPC);
 			} else if (name.contains("Elder")) {	ac(DefaultStats.ELDER);
 			} else if (name.contains("Undead")) {	ac(DefaultStats.UNDEAD);
 			} else {								ac(DefaultStats.PLAYER);
 			}
 		}
 
 		public int	 values(final int i) {return values[i];}
 		public int[] getArray() {return values;}
 		public int 	 getStr()   {return values[0];}
 		public int 	 getAgi()   {return values[1];}
 		public int 	 getVit()   {return values[2];}
 		public int 	 getInt()   {return values[3];}
 		public int 	 getDex()   {return values[4];}
 		public int 	 getLuk()   {return values[5];}
 		//@formatter:on
 
 		public boolean isMaxed(int i) {
			return values[i] >= 300;
 		}
 
 		public void decrease(int cursor) {
 			values[cursor]--;
 			freeStats++;
 		}
 
 		public void increase(int cursor) {
 			values[cursor]++;
 			freeStats--;
 		}
 
 		public boolean hasFreeStats() {
 			return freeStats > 0;
 		}
 
 		public int getFreeAmount() {
 			return freeStats;
 		}
 	}
 
 	protected static final class HiddenStats {
 
 		private final int[]	values;
 
 		public HiddenStats() {
 			values = new int[2];
 			setToDefault();
 			// TODO Auto-generated constructor stub
 		}
 
 		protected void setToDefault() {
 			values[0] = 5;
 			values[1] = 1;
 		}
 
 		public int getFOVR() {
 			return values[0];
 		}
 
 		public int getATKR() {
 			return values[1];
 		}
 	}
 
 	private final class Inventory {
 
 		private static final int	SLOTS	= 5;
 
 		Inventory() {}
 
 		private final List<Item>	items		= new ArrayList<>();
 		private final int[]			wearedItems	= new int[SLOTS];
 		private int					storageWeight;
 
 		ListIterator<Item> getIterator() {
 			return items.listIterator();
 		}
 
 		// boolean isWeared(int index) {
 		// boolean b = false;
 		// for (int i = 0; i < SLOTS; i++) {
 		// b |= (index == wearedItems[i]);
 		// }
 		// return b;
 		// }
 
 		Attack getAttack() {
 			final Attack atk = new Attack();
 			for (final int index : wearedItems) {
 				if (index < items.size() - 1) {
 					atk.addAttack(items.get(index).getAttack());
 				}
 			}
 			return atk;
 		}
 
 		Armor getArmor() {
 			final Armor def = new Armor();
 			for (final int index : wearedItems) {
 				if (index < items.size() - 1) {
 					def.addArmor(items.get(index).getArmor());
 				}
 			}
 			return def;
 		}
 
 		boolean isOverweighted() {
 			// TODO formulas mechanics
 			return storageWeight > 10 * stats.getStr();
 		}
 
 		boolean hasTooMuchItems() {
 			// TODO formulas mechanics
 			return items.size() > 3 * stats.getStr() / 2;
 		}
 
 	}
 
 	private static final int[]	XP_TO_LVL	= { 150, 400, 850, 2_000, 3_500, 7_000, 15_000, 25_000,
 			43_000							};
 
 	private final String		name;
 	private final Inventory		inventory	= new Inventory();
 	private final PrimaryStats	stats;
 
 	private int					level		= 0;
 	private int					XP			= 0;
 
 	public Hero(final int x, final int y, final String n, final Map m) {
 		super(x, y, null, n, m);
 		// TODO
 		name = n;
 		stats = new PrimaryStats(name);
 		restore();
 	}
 
 	private void restore() {
 		maxHP = Hero.StatsFormulas.getMaxHP(this);
 		maxMP = Hero.StatsFormulas.getMaxMP(this);
 		HP = maxHP;
 		MP = maxMP;
 		baseAttack.replaceBy(Hero.StatsFormulas.getAttack(this));
 		baseArmor.replaceBy(Hero.StatsFormulas.getArmor(this));
 	}
 
 	public void recomputeStats() {
 		float HPperc = HP / maxHP;
 		float MPperc = MP / maxMP;
 
 		maxHP = Hero.StatsFormulas.getMaxHP(this);
 		maxMP = Hero.StatsFormulas.getMaxMP(this);
 		HP = maxHP * HPperc;
 		MP = maxMP * MPperc;
 		baseAttack.replaceBy(Hero.StatsFormulas.getAttack(this));
 		baseArmor.replaceBy(Hero.StatsFormulas.getArmor(this));
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	public PrimaryStats getStats() {
 		return stats;
 	}
 
 	@Override
 	public Point move(final Point dp, final Game g) {
 		if (inventory.isOverweighted()) {
 			g.log("Вы #2#перегружены!#^#");
 			return null;
 		}
 		if (inventory.hasTooMuchItems()) {
 			g.log("Вы несете #2#слишком много вещей!#^#");
 			return null;
 		}
 
 		return super.move(dp, g);
 	}
 
 	@Override
 	public Armor getArmor() {
 		return super.getArmor().addArmor(inventory.getArmor());
 	}
 
 	@Override
 	public float getArmor(final int i) {
 		return getArmor().getArmor(i);
 	}
 
 	@Override
 	public Attack getAttack() {
 		return super.getAttack().addAttack(inventory.getAttack());
 	}
 
 	@Override
 	public float getAttack(final int i) {
 		return getAttack().getDamageOfType(i);
 	}
 
 	public ListIterator<Item> getInventoryIterator() {
 		return inventory.getIterator();
 	}
 
 	@Override
 	public void doTurnEffects() {
 		super.doTurnEffects();
 	}
 
 	private boolean lvlUp() {
 		return XP >= XP_TO_LVL[level];
 	}
 
 	private void gainLvl(final Game g) {
 		XP = 0;
 		level++;
 		stats.freeStats += PrimaryStats.DEFAULT_STAT_INCREASE;
 		restore();
 		g.log(String.format("%s достиг %s уровня!", name, level));
 		g.getWindowsManager();
 	}
 
 	public void gainXPfromDamage(final float dmg, final Game g) {
 		final int xp = (int) Math.pow(dmg, 6 / 5f);
 		final int gainedXP = (int) StatsFormulas.addBonusXp(this, xp);
 		XP += gainedXP;
 		g.log(String.format("%s получает %s опыта", getName(), gainedXP));
 		while (lvlUp()) {
 			gainLvl(g);
 		}
 
 	}
 
 	@Override
 	protected void onDeath(final Game g) {
 		HP = 0;
 		g.gameOver();
 		g.repaint();
 	}
 
 	public int getXP() {
 		return XP;
 	}
 
 	public int getAdvanceXP() {
 		return XP_TO_LVL[level];
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 }
