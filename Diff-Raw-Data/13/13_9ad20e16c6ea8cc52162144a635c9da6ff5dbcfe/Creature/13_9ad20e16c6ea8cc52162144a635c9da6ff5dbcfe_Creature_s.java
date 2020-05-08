 package rltut;
 
 import java.awt.Color;
 
 /**
  * Base class for a creature
  * 
  * @author Jeremy Rist
  * 
  */
 public class Creature {
 	private World world;
 	public int x;
 	public int y;
 	public int z;
 
 	private String name;
 	private char glyph;
 	private Color color;
 
 	private CreatureAi ai;
 
 	private Inventory inventory;
 
 	private int maxHp;
 	private int hp;
 	private int attackValue;
 	private int defenseValue;
 	private int maxFood;
 	private int food;
 
 	private int visionRadius;
 
 	private Item weapon;
 	private Item armor;
 
 	/**
 	 * @param world
 	 *            World where the creature lives
 	 * @param name
 	 *            Name of the creature
 	 * @param glyph
 	 *            char glyph of the creature
 	 * @param color
 	 *            Color of the creature
 	 * @param maxHp
 	 *            Max Hit points of the creature
 	 * @param attack
 	 *            Attack value of the creature
 	 * @param defense
 	 *            defense value of the creature
 	 */
 	public Creature(World world, String name, char glyph, Color color,
 			int maxHp, int attack, int defense) {
 		this.world = world;
 
 		this.name = name;
 		this.glyph = glyph;
 		this.color = color;
 
 		this.inventory = new Inventory(20);
 
 		this.maxHp = maxHp;
 		this.hp = maxHp;
 		this.attackValue = attack;
 		this.defenseValue = defense;
 
 		this.maxFood = 1000;
 		this.food = maxFood / 3 * 2;
 
 		this.visionRadius = 9;
 	}
 
 	public Item armor() {
 		return armor;
 	}
 
 	/**
 	 * A public getter. Probably ought to be getAttackValue()
 	 * 
 	 * @return attackValue
 	 */
 	public int attackValue() {
 		return attackValue + (weapon == null ? 0 : weapon.attackValue())
 				+ (armor == null ? 0 : armor.attackValue());
 	}
 
 	/**
 	 * Check if the creature can enter a given coordinate
 	 * 
 	 * @param wx
 	 *            world x-coordinate
 	 * @param wy
 	 *            world y-coordinate
 	 * @param wz
 	 *            world z-coordinate
 	 * @return if the creature can enter given coordinate
 	 */
 	public boolean canEnter(int wx, int wy, int wz) {
 		return world.tile(wx, wy, wz).isGround()
 				&& world.creature(wx, wy, wz) == null;
 	}
 
 	/**
 	 * Checks with the creature's AI to see if the creature can see into a given
 	 * coordinate.
 	 * 
 	 * @param wx
 	 *            world x-coordinate
 	 * @param wy
 	 *            world y-coordinate
 	 * @param wz
 	 *            world z-coordinate
 	 * @return if the Creature can see the given coordinate
 	 */
 	public boolean canSee(int wx, int wy, int wz) {
 		return ai.canSee(wx, wy, wz);
 	}
 
 	/**
 	 * Returns the creature's Color
 	 * 
 	 * @return the creture's Color
 	 */
 	public Color color() {
 		return color;
 	}
 
 	/**
 	 * Finds another creature at the given coordinates in the same world as the
 	 * Creature.
 	 * 
 	 * @param wx
 	 *            world x-coordinate
 	 * @param wy
 	 *            world y-coordinate
 	 * @param wz
 	 *            world z-coordinate
 	 * @return Creature at given coordinate
 	 */
 	public Creature creature(int wx, int wy, int wz) {
 		return world.creature(wx, wy, wz);
 	}
 
 	/**
 	 * Returns the defenseValue of the creature.
 	 * 
 	 * Probably ought to be getDefenseValue()
 	 * 
 	 * @return defenseValue
 	 */
 	public int defenseValue() {
 		return defenseValue + (weapon == null ? 0 : weapon.defenseValue())
 				+ (armor == null ? 0 : armor.defenseValue());
 	}
 
 	/**
 	 * Tell the creature to dig in a given coordinate.
 	 * 
 	 * @param wx
 	 *            world x-coordinate
 	 * @param wy
 	 *            world y-coordinate
 	 * @param wz
 	 *            world z-coordinate
 	 */
 	public void dig(int wx, int wy, int wz) {
 		modifyFood(-10);
 		world.dig(wx, wy, wz);
 		doAction("dig");
 	}
 
 	/**
 	 * Push a message from the Creature on to the message queue
 	 * 
 	 * @param message
 	 *            String to display
 	 * @param params
 	 *            params for the string
 	 */
 	public void doAction(String message, Object... params) {
 		int r = 9;
 		for (int ox = -r; ox < r + 1; ox++) {
 			for (int oy = -r; oy < r + 1; oy++) {
 				if (ox * ox + oy * oy > r * r)
 					continue;
 
 				Creature other = world.creature(x + ox, y + oy, z);
 
 				if (other == null)
 					continue;
 
 				if (other == this)
 					other.notify("You " + message + ".", params);
 				else if (other.canSee(x, y, z))
 					other.notify(String.format("The %s %s.", name,
 							makeSecondPerson(message)), params);
 			}
 		}
 	}
 
 	/**
 	 * Remove an Item from the Creature's Inventory and place at or near the
 	 * Creature's current location in the World.
 	 * 
 	 * @param item
 	 *            Item to drop from the Creature's Inventory
 	 */
 	public void drop(Item item) {
if (world.addAtEmptySpace(item, x, y, z)	){
		doAction("drop a " + item.name());
		inventory.remove(item);
		unequip(item);
	} else { notify("There's nowhere to drop the %s.", item.name());}
 
 	/**
 	 * Eat an item.
 	 * 
 	 * @param item
 	 *            Item to eat.
 	 */
 	public void eat(Item item) {
 		if (item.foodValue() < 0)
 			notify("Gross!");
 
 		modifyFood(item.foodValue());
 		inventory.remove(item);
 		unequip(item);
 	}
 
 	public void equip(Item item) {
 		if (item.attackValue() == 0 && item.defenseValue() == 0)
 			return;
 
 		if (item.attackValue() >= item.defenseValue()) {
 			unequip(weapon);
 			doAction("wield a " + item.name());
 			weapon = item;
 		} else {
 			unequip(armor);
 			doAction("put on a " + item.name());
 			weapon = item;
 		}
 	}
 
 	public int food() {
 		return food;
 	}
 
 	/**
 	 * @return the creature's glyph
 	 */
 	public char glyph() {
 		return glyph;
 	}
 
 	/**
 	 * @return The crature's current HP
 	 */
 	public int hp() {
 		return hp;
 	}
 
 	/**
 	 * @return The creature's Inventory object
 	 */
 	public Inventory inventory() {
 		return inventory;
 	}
 
 	public int maxFood() {
 		return maxFood;
 	}
 
 	/**
 	 * @return the creature's Max HP
 	 */
 	public int maxHp() {
 		return maxHp;
 	}
 
 	/**
 	 * Change how sated the Creature is. Kill the player if he has no food in
 	 * his belly.
 	 * 
 	 * @param amount
 	 *            Amount to change the food in the Creature's belly
 	 */
 	public void modifyFood(int amount) {
 		food += amount;
 
 		if (food > maxFood) {
 			maxFood = (maxFood + food) / 2;
 			food = maxFood;
 			notify("You can't believe your stomach can hold that much!");
 		} else if (food < 1 && isPlayer()) {
 			modifyHp(-1000);
 		}
 	}
 
 	/**
 	 * Adds hit points to a creature's hit points. If the creature's hit points
 	 * are reduced below 1, the creature is removed from the world and leave a
 	 * corpse in its place.
 	 * 
 	 * @param amount
 	 *            Amount of HP to add.
 	 */
 	public void modifyHp(int amount) {
 		hp += amount;
 
 		if (hp < 1) {
 			doAction("die");
 			leaveCorpse();
 			world.remove(this);
 		}
 	}
 
 	/**
 	 * Moves the Creature by a given amount
 	 * 
 	 * @param mx
 	 *            x-axis movement
 	 * @param my
 	 *            y-axis movement
 	 * @param mz
 	 *            z-axis movement
 	 */
 	public void moveBy(int mx, int my, int mz) {
 		if (mx == 0 && my == 0 && mz == 0)
 			return;
 
 		Tile tile = world.tile(x + mx, y + my, z + mz);
 
 		if (mz == -1) {
 			if (tile == Tile.STAIRS_DOWN) {
 				doAction("walk up the stairs to level %d", z + mz + 1);
 			} else {
 				doAction("try to go up but are stopped by the cave ceiling");
 				return;
 			}
 		} else if (mz == 1) {
 			if (tile == Tile.STAIRS_UP) {
 				doAction("walk down the stairs to level %d", z + mz + 1);
 			} else {
 				doAction("try to go down but are stopped by the cave floor");
 				return;
 			}
 		}
 
 		Creature other = world.creature(x + mx, y + my, z + mz);
 
 		if (other == null)
 			ai.onEnter(x + mx, y + my, z + mz, tile);
 		else
 			attack(other);
 	}
 
 	/**
 	 * @return name of creature
 	 */
 	public String name() {
 		return name;
 	}
 
 	public void notify(String message, Object... params) {
 		ai.onNotify(String.format(message, params));
 	}
 
 	/**
 	 * Pick up the item at the Creature's current location in the world and add
 	 * to the Creature's inventory.
 	 */
 	public void pickup() {
 		Item item = world.item(x, y, z);
 
 		if (inventory.isFull() || item == null) {
 			doAction("grab at the ground");
 		} else {
 			doAction("pickup a %s", item.name());
 			world.remove(x, y, z);
 			inventory.add(item);
 		}
 	}
 
 	/**
 	 * Set the creature's AI.
 	 * 
 	 * @param ai
 	 *            Ai object to give the creature
 	 */
 	public void setCreatureAi(CreatureAi ai) {
 		this.ai = ai;
 	}
 
 	/**
 	 * Get a tile from the creature's world
 	 * 
 	 * @param wx
 	 *            world x-coordinate
 	 * @param wy
 	 *            world y-coordinate
 	 * @param wz
 	 *            world z-coordinate
 	 * @return Tile object from the given coordinate in the Creature's world
 	 */
 	public Tile tile(int wx, int wy, int wz) {
 		return world.tile(wx, wy, wz);
 	}
 
 	public void unequip(Item item) {
 		if (item == null)
 			return;
 
 		if (item == armor) {
 			doAction("remove a " + item.name());
 			armor = null;
 		} else if (item == weapon) {
 			doAction("put away a " + item.name());
 			weapon = null;
 		}
 	}
 
 	/**
 	 * Run the creature's update routine.
 	 * 
 	 * Take a turn.
 	 */
 	public void update() {
 		modifyFood(-1);
 		ai.onUpdate();
 	}
 
 	/**
 	 * @return visionRadius of the Creature
 	 */
 	public int visionRadius() {
 		return visionRadius;
 	}
 
 	public Item weapon() {
 		return weapon;
 	}
 
 	/**
 	 * Attacks another creature and removes hit points from it.
 	 * 
 	 * @param other
 	 *            Creature being attacked
 	 */
 	private void attack(Creature other) {
 		int amount = Math.max(0, attackValue() - other.defenseValue());
 
 		amount = (int) (Math.random() * amount) + 1;
 
 		other.modifyHp(-amount);
 
 		doAction("attack the %s for %d damage", other.name, amount);
 	}
 
 	/**
 	 * @return if the Creature is the player
 	 */
 	private boolean isPlayer() {
 		return glyph == '@';
 	}
 
 	/**
 	 * Leave a corpse where the Creature is standing.
 	 */
 	private void leaveCorpse() {
 		Item corpse = new Item('%', color, name + " corpse");
 		corpse.modifyFoodValue(maxHp * 3);
 		world.addAtEmptySpace(corpse, x, y, z);
 	}
 
 	/**
 	 * Converts a given string to second person parlance.
 	 * 
 	 * @param text
 	 *            String to convert to second person
 	 * @return Second Personized String
 	 */
 	private String makeSecondPerson(String text) {
 		// TODO String and Grammar parser in Creature class: Yuck.
 		String[] words = text.split(" ");
 		words[0] = words[0] + "s";
 
 		StringBuilder builder = new StringBuilder();
 		for (String word : words) {
 			builder.append(" ");
 			builder.append(word);
 		}
 
 		return builder.toString().trim();
 	}
 
 }
