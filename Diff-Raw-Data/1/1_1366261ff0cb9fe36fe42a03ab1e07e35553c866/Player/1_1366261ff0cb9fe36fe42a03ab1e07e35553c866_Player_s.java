 package game.entities.superentities;
 
 import static game.features.Stat.*;
 import game.Main;
 import game.entities.NPC;
 import game.entities.Portal;
 import game.entities.item.EquipItem;
 import game.entities.item.EquipItem.EquipType;
 import game.entities.item.Item;
 import game.entities.item.UsableItem;
 import game.features.Quest;
 import game.structure.Map;
 import game.structure.Slot;
 import game.ui.MsgBoxManager;
 import game.ui.Shop;
 import game.ui.UserInterface;
 import game.util.Timer;
 import game.util.Util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.util.Point;
 
 /**
  * The main Entity of the game, which is controlled by the user and perform most
  * of the actions of the game.
  */
 public class Player extends SuperEntity {
 
 	public static final int INV_LIMIT = 30, MAX_LEVEL = 8, BASE = 0x10,
 			EXTRA = 0x20, TOTAL = 0x30, HELMET = 0, TOPWEAR = 1,
 			BOTTOMWEAR = 2, SHOES = 3, WEAPON = 4;
 	private static final int REGEN = 3;
 	private int level = 1, exp = 0, gold = 0, mp;
 	private volatile int hp;
 	private volatile java.util.Map<Integer, Integer> stats = new HashMap<Integer, Integer>();
 	private java.util.Map<EquipType, EquipItem> equips = new HashMap<EquipType, EquipItem>();
 	private ArrayList<Item> items = new ArrayList<Item>();
 	private ArrayList<Quest> quests = new ArrayList<Quest>();
 	private static int invincibleRenderCounter = 0;
 	private long nextAtk = 0, invincibleTimer = 0, nextMove = 0;
 	private boolean invincible = false, invincibleRender = false;
 
 	public Player(int id, Point pos) {
 		super(id);
 		addSkill(1792);
 
 		// TODO player file with base stats, stats per level, damage formula
 		// parameters, etc.
 
 		stats.put(BASE + MAXHP.ID, 100);
 		stats.put(BASE + MAXMP.ID, 100);
 		stats.put(BASE + ATK.ID, 1);
 		stats.put(BASE + STR.ID, 10);
 		stats.put(BASE + DEF.ID, 5);
 
 		stats.put(EXTRA + MAXHP.ID, 0);
 		stats.put(EXTRA + MAXMP.ID, 0);
 		stats.put(EXTRA + ATK.ID, 0);
 		stats.put(EXTRA + STR.ID, 0);
 		stats.put(EXTRA + DEF.ID, 0);
 
 		setHP(getStat(TOTAL + MAXHP.ID));
 		setMP(getStat(TOTAL + MAXMP.ID));
 
 		Timer timer = new Timer(this, "regen", 10000);
 		timer.start();
 
 	}
 
 	public void regen() {
 		setHP(getHP() + REGEN);
 		setMP(getMP() + REGEN);
 	}
 
 	public void input() {
 
 		if (Keyboard.getEventKeyState()) {
 			switch (Keyboard.getEventKey()) {
 			case Keyboard.KEY_M:
 				for (Slot s : getMap().getAllSlots()) {
 					Monster monster = s.getMonster();
 					if (monster != null) {
 						monster.die();
 						break;
 					}
 				}
 				break;
 			case Keyboard.KEY_SPACE:
 				action(Util.addRelPoints(position(), new Point(0, 1),
 						getFacingDir()));
 				break;
 			case Keyboard.KEY_Z:
 				attack(1792);
 				break;
 			case Keyboard.KEY_X:
 				Portal portal = getMap().get(position()).getPortal();
 				if (portal != null) {
 					portal.run();
 				}
 				break;
 			case Keyboard.KEY_P:
 				Shop shop = new Shop(0x0a00);
 				shop.buy(0x6300, this);
 			}
 		}
 	}
 
 	public void move(int key) {
 
 		boolean moveCamera = false;
 		int dir = 0;
 
 		// movement handled by update due to movement based on key down not key
 		// press
 
 		switch (key) {
 		case Keyboard.KEY_UP:
 			dir = UP;
 			moveCamera = getPositionInGrid().getY() - 1 < Map.VIEW_LIMIT
 					&& getMap().isPointInMap(
 							new Point(0, -1 + getMap().getOffSet().getY()));
 			break;
 		case Keyboard.KEY_RIGHT:
 			dir = RIGHT;
 			moveCamera = getPositionInGrid().getX() + 1 >= Main.GRIDSIZE
 					.getWidth() - Map.VIEW_LIMIT
 					&& getMap().isPointInMap(
 							new Point(Main.GRIDSIZE.getWidth()
 									+ getMap().getOffSet().getX(), 0));
 			break;
 		case Keyboard.KEY_DOWN:
 			dir = DOWN;
 			moveCamera = getPositionInGrid().getY() + 1 >= Main.GRIDSIZE
 					.getHeight() - Map.VIEW_LIMIT
 					&& getMap().isPointInMap(
 							new Point(0, Main.GRIDSIZE.getHeight()
 									+ getMap().getOffSet().getY()));
 			break;
 		case Keyboard.KEY_LEFT:
 			dir = LEFT;
 			moveCamera = getPositionInGrid().getX() - 1 < Map.VIEW_LIMIT
 					&& getMap().isPointInMap(
 							new Point(getMap().getOffSet().getX() - 1, 0));
 			break;
 		default:
 			return;
 		}
 
 		if (System.currentTimeMillis() < nextMove)
 			return;
 
 		Point oldPos = position();
 
 		face(dir);
 
 		if (!canMove(dir))
 			return;
 
 		setPosition(Util.addRelPoints(position(), new Point(0, 1), dir));
 		nextMove = System.currentTimeMillis() + 150;
 
 		if (moveCamera)
 			getMap().moveView(getX() - oldPos.getX(), getY() - oldPos.getY());
 
 		List<Item> items = getMap().get(position()).getItems();
 
 		if (!items.isEmpty()) {
 			for (ListIterator<Item> l = items.listIterator(); l.hasNext();) {
 				if (addItem(l.next()))
 					l.remove();
 			}
 		}
 
 	}
 
 	private void action(Point target) {
 		if (!getMap().isPointInMap(target))
 			return;
 		NPC npc = getMap().get(target).getNPC();
 		List<Item> items = getMap().get(target).getItems();
 		if (npc != null) {
 			npc.run();
 		} else if (!items.isEmpty()) {
 			for (ListIterator<Item> i = items.listIterator(); i.hasNext();) {
 				if (addItem(i.next()))
 					i.remove();
 			}
 		}
 	}
 
 	protected void attack(int skill) {
 		if (System.currentTimeMillis() < nextAtk)
 			return;
 		if (getMP() < 2) {
 			UserInterface
 					.sendNotification("You don't have enough MP to use this skill.");
 			return;
 		}
 		super.attack(skill);
 		delayAttack(getSkill(skill).getDelay());
 		useMP(2);
 	}
 
 	public double getAverageDamage() {
 		return getStat(TOTAL + ATK.ID) * 2 + getStat(TOTAL + STR.ID);
 	}
 
 	public void delayAttack(int mili) {
 		nextAtk = System.currentTimeMillis() + mili;
 	}
 
 	public void die() {
 		UserInterface.sendNotification("You died.");
 		super.die();
 	}
 
 	public void update() {
 
 		super.update();
 
 		if (System.currentTimeMillis() > invincibleTimer)
 			setInvincible(false);
 
 		if (MsgBoxManager.isActive())
 			return;
 
 		// handles movement
 		int moveKeys[] = { Keyboard.KEY_UP, Keyboard.KEY_RIGHT,
 				Keyboard.KEY_DOWN, Keyboard.KEY_LEFT };
 
 		int keysDown = 0;
 		int keyDown = 0;
 		for (int key : moveKeys) {
 			if (Keyboard.isKeyDown(key)) {
 				keyDown = key;
 				keysDown++;
 			}
 		}
 
 		if (keysDown == 1) {
 			if (System.currentTimeMillis() > nextMove) {
 				move(keyDown);
 				nextMove = System.currentTimeMillis() + 200;
 			}
 		}
 
 	}
 
 	public void render() {
 		if (isInvincible()) {
 			if (invincibleRenderCounter == 7) {
 				invincibleRender = !invincibleRender;
 				invincibleRenderCounter = 0;
 			}
 			invincibleRenderCounter++;
 			if (invincibleRender)
 				return;
 		}
 
 		super.render();
 
 	}
 
 	public boolean hit(int damage) {
 		damage -= getStat(TOTAL+DEF.ID)/10;
 		if(damage < 0)
 			damage = 0;
 		if (!isInvincible()) {
 			invincible = true;
 			invincibleTimer = System.currentTimeMillis() + 1000;
 			return super.hit(damage);
 		}
 		return false;
 	}
 
 	public void useItem(Item item) {
 		if (item != null) {
 			if (item instanceof UsableItem) {
 				UsableItem i = (UsableItem) (item);
 				i.use();
 				i.setQuantity(i.getQuantity() - 1);
 				if (item.getQuantity() == 0) {
 					removeItem(item);
 				}
 			}
 		}
 	}
 
 	public void removeItem(Item item) {
 		items.remove(item);
 	}
 
 	public Item getItem(int id) {
 		for (Item i : items) {
 			if (i.id() == id)
 				return i;
 		}
 		return null;
 	}
 
 	public boolean addItem(Item i) {
 
 		Item item = getItem(i.id());
 		if (item != null && !(item instanceof EquipItem)) {
 			item.add(i.getQuantity());
 			if (i.getQuantity() > 0)
 				UserInterface.sendNotification("You got an item: "
 						+ i.getName() + " x" + i.getQuantity() + ".");
 			return true;
 		}
 		if (items.size() < INV_LIMIT) {
 			items.add(i);
 			if (i.getQuantity() > 0)
 				UserInterface.sendNotification("You got an item: "
 						+ i.getName() + " x" + i.getQuantity() + ".");
 			return true;
 		}
 		return false;
 	}
 
 	public void loseItem(int id, int amount) {
 		if (hasItem(id, amount)) {
 			getItem(id).setQuantity(getItem(id).getQuantity() - amount);
 			if (amount > 0)
 				UserInterface.sendNotification("You lost an item: "
 						+ getItem(id).getName() + " x" + amount + ".");
 			if (getItem(id).getQuantity() <= 0) {
 				items.remove(getItem(id));
 			}
 		}
 	}
 
 	public boolean hasItem(int ID, int quantity) {
 		if (getItem(ID) != null) {
 			if (getItem(ID).getQuantity() >= quantity)
 				return true;
 		}
 		return false;
 	}
 
 	public void gainExp(int amount) {
 		int expToLevel = getExpReq() - exp;
 		if (amount >= expToLevel) {
 			exp = amount - expToLevel;
 			levelUp();
 		} else if (exp + amount < 0) {
 			exp = 0;
 		} else
 			exp += amount;
 		if (amount > 0)
 			UserInterface.sendNotification("You gained " + amount + " EXP.");
 	}
 
 	private void levelUp() {
 		raiseStat(BASE + ATK.ID, 1);
 		raiseStat(BASE + STR.ID, 2);
 		raiseStat(BASE + DEF.ID, 1);
 		level++;
 		UserInterface.sendNotification("LEVEL UP! You are now level "
 				+ getLevel());
 		stats.put(BASE + MAXHP.ID, getStat(BASE + MAXHP.ID) + 5);
 		stats.put(BASE + MAXMP.ID, getStat(BASE + MAXMP.ID) + 5);
 		setHP(getStat(TOTAL + MAXHP.ID));
 		setMP(getStat(TOTAL + MAXHP.ID));
 	}
 
 	public void useMP(int amount) {
 		setMP(getMP() - amount);
 		if (getMP() <= 0)
 			setMP(0);
 	}
 
 	public void gainGold(int amount) {
 		gold += amount;
 		if (amount > 0)
 			UserInterface.sendNotification("You gained " + amount + " gold.");
 	}
 
 	public int getGold() {
 		return gold;
 	}
 
 	public int getExp() {
 		return exp;
 	}
 
 	public int getExpReq() {
 		return level * 10;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public int getItemCount() {
 		return items.size();
 	}
 
 	public ArrayList<Item> getItems() {
 		return items;
 	}
 
 	public void addQuest(Quest quest) {
 		quests.add(quest);
 		UserInterface.sendNotification("Quest accepted: " + quest.getName()
 				+ ".");
 	}
 
 	public Quest getQuest(int id) {
 		for (Quest q : quests) {
 			if (q.id() == id) {
 				return q;
 			}
 		}
 		return null;
 	}
 
 	public boolean hasQuest(int id) {
 		return getQuest(id) != null;
 	}
 
 	public ArrayList<Quest> getActiveQuests() {
 		return quests;
 	}
 
 	public void setInvincible(boolean b) {
 		invincible = b;
 	}
 
 	public boolean isInvincible() {
 		return invincible;
 	}
 
 	public int getStat(int stat) {
 		if (stat >= 0x30) {
 			return getStat(stat - EXTRA) + getStat(stat - BASE);
 		}
 		return stats.get(stat);
 	}
 
 	public void raiseStat(int stat, int amount) {
 		if (stats.containsKey(stat)) {
 			stats.put(stat, getStat(stat) + amount);
 		}
 		return;
 	}
 
 	public void addEquip(EquipItem equip) {
 		if (equips.get(equip.getType()) != null)
 			return;
 		equips.put(equip.getType(), equip);
 		raiseStat(EXTRA + MAXHP.ID, equip.getStat(MAXHP));
 		raiseStat(EXTRA + MAXMP.ID, equip.getStat(MAXMP));
 		raiseStat(EXTRA + ATK.ID, equip.getStat(ATK));
 		raiseStat(EXTRA + STR.ID, equip.getStat(STR));
 	}
 
 	public void removeEquip(EquipType type) {
 
 		if (equips.get(type) == null)
 			return;
 
 		EquipItem equip = equips.get(type);
 
 		if (!addItem(equip))
 			return;
 
 		raiseStat(EXTRA + MAXHP.ID, -equip.getStat(MAXHP));
 		raiseStat(EXTRA + MAXMP.ID, -equip.getStat(MAXMP));
 		raiseStat(EXTRA + ATK.ID, -equip.getStat(ATK));
 		raiseStat(EXTRA + STR.ID, -equip.getStat(STR));
 
 		equips.put(type, null);
 	}
 
 	public java.util.Map<EquipType, EquipItem> getEquips() {
 		return equips;
 	}
 
 	/*
 	 * public EquipItem getEquip(int type) { return equips.get(type); }
 	 */
 
 	public void setHP(int hp) {
 		this.hp = hp;
 		if (getHP() > getStat(TOTAL + MAXHP.ID))
 			setHP(getStat(TOTAL + MAXHP.ID));
 	}
 
 	public int getHP() {
 		return hp;
 	}
 
 	public void setMP(int mp) {
 		this.mp = mp;
 		if (getMP() > getStat(TOTAL + MAXMP.ID))
 			setMP(getStat(TOTAL + MAXMP.ID));
 	}
 
 	public int getMP() {
 		return mp;
 	}
 
 }
