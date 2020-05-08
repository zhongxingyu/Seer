 package com.prsc.gs.model.comp.mob;
 
import java.util.Timer;
 
import java.util.TimerTask;
 
 
 import com.prsc.config.Formulae;
 import com.prsc.gs.event.SingleEvent;
 import com.prsc.gs.event.impl.FightEvent;
 import com.prsc.gs.external.EntityHandler;
 import com.prsc.gs.external.GameObjectLoc;
 import com.prsc.gs.model.Bubble;
 import com.prsc.gs.model.ChatMessage;
 import com.prsc.gs.model.GameObject;
 import com.prsc.gs.model.InvItem;
 import com.prsc.gs.model.Item;
 import com.prsc.gs.model.MenuHandler;
 import com.prsc.gs.model.Mob;
 import com.prsc.gs.model.Npc;
 import com.prsc.gs.model.Path;
 import com.prsc.gs.model.Player;
 import com.prsc.gs.model.Point;
 import com.prsc.gs.model.Player.SkillType;
 import com.prsc.gs.plugins.Quest;
 import com.prsc.gs.states.Action;
 import com.prsc.gs.states.CombatState;
 import com.prsc.gs.tools.DataConversions;
 import com.prsc.gs.world.Shop;
 import com.prsc.gs.world.World;
 
 public class Scriptable {
 	
 	private Player player = null;
 	private Npc activeNpc = null;
 	private Quest activeQuest = null;
 	
	private final Timer timer = new Timer();
 	
 	private static final byte BEGINNING = 0x0;
 	private static final byte COMPLETE = -1;
 	
 	public Scriptable(Player player) {
 		this.player = player;
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public int getQuestStage() {
 		return player.getQuestStage(activeQuest);
 	}
 	
 	public void setQuestStage(int stage) {
 		player.setQuestStage(activeQuest, stage);
 	}
 	
 	public void setQuestCompleted() {
 		player.sendQuestComplete(activeQuest.getQuestId());
 		setQuestStage(COMPLETE);
 	}
 	
 	public void setActiveNpc(Npc npc) {
 		this.activeNpc = npc;
 	}
 	
 	public void setActiveQuest(Quest q) {
 		this.activeQuest = q;
 	}
 	
 	public void displayMessage(String... messages) {
 		displayMessage(messages, 500);
 	}
 	
 	public void displayMessage(String[] messages, int delay) {
 		for(String message : messages) {
 			player.getActionSender().sendMessage(message);
 			if(delay > 0)
 				sleep(delay);
 		}
 	}
 
 	public void displayAlert(String message) {
 		displayAlert(message, false);
 	}
 
 	public void displayAlert(String message, boolean big) {
 		player.getActionSender().sendAlert(message, big);
 	}
 
 	public void sendChat(Mob speaker, Mob receiver, String... messages) {
 		for(String message : messages) {
 			if(speaker instanceof Player) {
 				player.informGroupOfChatMessage(new ChatMessage(speaker, message, receiver));
 			} else {
 				player.informGroupOfNpcMessage(new ChatMessage(speaker, message, receiver));
 			}
 			sleep(2200);
 		}
 	}
 
 	public void sendChat(Mob speaker, String... messages) {
 		for(String message : messages) {
 			if(speaker instanceof Player) {
 				player.informGroupOfChatMessage(new ChatMessage(speaker, message, activeNpc));
 			} else {
 				player.informGroupOfNpcMessage(new ChatMessage(speaker, message, player));
 			}
 			sleep(2200);
 		}
 	}
 
 	public void sendNpcChat(String... messages) {
 		for(String message : messages) {
 			player.informGroupOfNpcMessage(new ChatMessage(activeNpc, message, player));
 			sleep(2200);
 		}
 	}
 
 	public void sendPlayerChat(String... messages) {
 		for(String message : messages) {
 			player.informGroupOfChatMessage(new ChatMessage(player, message, activeNpc));
 			sleep(2200);
 		}
 	}
 	
 	public int pickOption(String[] strs, boolean repeat) {
 		try {
 			long time = System.currentTimeMillis();
 			player.setBusy(false);
 			player.lastOption = -2;
 			player.setMenuHandler(new MenuHandler(strs) {
 				public void handleReply(int option, String reply) {
 					if (option < 0 || option >= getOptions().length || option == 30) {
 						activeNpc.unblock();
 						player.setBusy(false);
 						owner.lastOption = -1;
 						return;
 					} else {
 						owner.lastOption = option;
 					}
 				}
 			});
 			player.getActionSender().sendMenu(strs);
 			while (player.lastOption == -2 && System.currentTimeMillis() - time < 20000) { // timeout
 				sleep(12);
 			}
 			if (player.lastOption == -1 || player.lastOption == -2) {
 				player.setBusy(false);
 				activeNpc.unblock();
 				return -1;
 			} 
 			player.setBusy(true);
 			int newOpt = player.lastOption;
 			player.lastOption = -2;
 			if(repeat) {
 				sendChat(player, strs[newOpt]);
 			}
 			return newOpt;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return -1;
 		}
 	}
 
 	public int pickOption(String[] strs) {
 		return pickOption(strs, true);
 	}
 
 	public int getRemainingSpace() {
 		return 30 - player.getInventory().size();
 	}
 
 	public void removeItem(int id, int amount) {
 		if (EntityHandler.getItemDef(id).isStackable()) {
 			player.getInventory().remove(id, amount);
 		} else {
 			for (int i = 0; i < amount; i++) {
 				player.getInventory().remove(id, 1);
 			}
 		}
 		player.getActionSender().sendInventory();
 	}
 
 	public void removeAllItem(int id) {
 		removeItem(id, countItem(id));
 	}
 
 	public void spawnItem(int x, int y, int id, int amount) {
 		spawnItem(x, y, id, amount, 500);
 	}
 
 	public void spawnItem(final int x, final int y, final int id, final int amount, final int delayFor) {
 		final InvItem item = new InvItem(id, amount);
 		sleep(delayFor);
 		World.getWorld().registerItem(new Item(item.getID(), x, y, item.getAmount(), player));
 	}
 
 	public void addItem(int id, int amount) {
 		player.getInventory().add(new InvItem(id, amount));
 		player.getActionSender().sendInventory();
 	}
 	
 	public void addItem(int id) {
 		addItem(id, 1);
 	}
 	
 	public boolean isWielding(int itemId) {
 		return player.getInventory().wielding(itemId);
 	}
 	
 	public InvItem getItem(int itemId) {
 		return new InvItem(itemId, 1);
 	}
 
 	public boolean hasItem(int id) {
 		return hasItem(id, 1);
 	}
 	
 	public int rand(int low, int high) {
 		return Formulae.Rand(low, high);
 	}
 
 	public boolean hasItem(int id, int amount) {
 		if (EntityHandler.getItemDef(id).isStackable()) {
 			for (InvItem i : player.getInventory().getItems()) {
 				if (i.getID() == id && i.getAmount() >= amount)
 					return true;
 			}
 		} else {
 			int count = 0;
 			for (InvItem i : player.getInventory().getItems()) {
 				if (i.getID() == id)
 					count++;
 			}
 			if (count >= amount)
 				return true;
 		}
 		return false;
 	}
 
 	public int countItem(int id) {
 		return player.getInventory().countId(id);
 	}
 
 	public int getMaxLevel(SkillType skill) {
 		return player.getMaxStat(skill.getSkill());
 	}
 
 	public int getCurrentLevel(SkillType skill) {
 		return player.getCurStat(skill.getSkill());
 	}
 	
 	public void advanceStat(SkillType skillToAdvance, double experienceAmount, boolean useFatigue) {
 		player.incExp(skillToAdvance.getSkill(), experienceAmount, useFatigue, false, false);
 		player.getActionSender().sendStat(skillToAdvance.getSkill());
 	}
 	
 	public void advanceStat(SkillType skillToAdvance, int experienceAmount) {
 		player.incExp(skillToAdvance.getSkill(), experienceAmount, false, false, false);
 		player.getActionSender().sendStat(skillToAdvance.getSkill());
 	}
 
 	public void advanceStat(SkillType skillToAdvance, int experienceAmount, boolean useFatigue) {
 		player.incExp(skillToAdvance.getSkill(), experienceAmount, useFatigue, false, false);
 		player.getActionSender().sendStat(skillToAdvance.getSkill());
 	}
 	
 	public void restoreStat(SkillType skillToRestore, int levelToRestoreTo) {
 		player.setCurStat(skillToRestore.getSkill(), levelToRestoreTo);
 		player.getActionSender().sendStat(skillToRestore.getSkill());
 	}
 	
 	public void updateStat(SkillType skillToAdvance) {
 		player.getActionSender().sendStat(skillToAdvance.getSkill());
 	}
 
 	public void addQuestPoints(int points) {
 		player.incQuestPoints(points);
 		player.getActionSender().sendQuestPoints();
 	}
 	
 	public void occupy() {
 		player.setBusy(true);
 		if(activeNpc != null)
 			activeNpc.blockedBy(player);
 	}
 	
 	public void release() {
 		player.setBusy(false);
 		if(activeNpc != null)
 			activeNpc.unblock();
 	}
 	
 	public Npc getNpc(int npcId) {
 		return World.getWorld().getNpcById(npcId);
 	}
 	
 	public Npc getNearestNpc(int npcId, int radius) {
 		Iterable<Npc> npcsInView = player.getViewArea().getNpcsInView();;
 		Npc closestNpc = null;
 		for(int next = radius; next >= radius; next--) {
 			for(Npc n : npcsInView) {
 				if(n.getID() == npcId && n.withinRange(player.getLocation(), next) && !n.isBusy()) {
 					closestNpc = n;
 				}
 			}
 		}
 		return closestNpc;
 	}
 
 	public Npc spawnNpc(int npcId, int x, int y, boolean respawn) {
 		return spawnNpc(npcId, x, y, 0, respawn, false);
 	}
 	
 	// Fix
 	public Npc spawnNpc(int npcId, int x, int y, int time, boolean persist, boolean aggressive) {
 		if (EntityHandler.getNpcDef(npcId) != null) {
 			final Npc n = new Npc(npcId, x, y, x - 5, x + 5, y - 5, y + 5);
 			n.setRespawn(persist);
 			n.getDef().aggressive = true;
 			World.getWorld().registerNpc(n);
 			if(!persist) {
 				World.getWorld().getDelayedEventHandler().add(new SingleEvent(null, time == 0 ? 300000 : time) {
 					public void action() {
 						Mob opponent = n.getOpponent();
 						if (opponent != null) {
 							opponent.resetCombat(CombatState.ERROR);
 						}
 						n.resetCombat(CombatState.ERROR);
 						world.unregisterNpc(n);
 						n.remove();
 					}
 				});
 			}
 			return n;
 		}
 		return null;
 	}
 	
 	public boolean isNpcNearby(int id) {
 		for(Npc npc : player.getViewArea().getNpcsInView()) {
 			if(npc.getID() == id) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void moveNpc(Npc npc, int[] positions) {
 		npc.setPath(new Path(0, 0, 0, 0));
 	}
 
 	public void removeNpc(Npc npc) {
 		World.getWorld().unregisterNpc(npc);
 		npc.remove();
 	}
 	
 	public void faceNpc(Npc n) {
 		int dir = Formulae.getDirection(player, n);
 		if (dir != -1) {
 			player.setSprite(Formulae.getDirection(n, player));
 			n.setSprite(Formulae.getDirection(player, n));
 		}
 	}
 	
 	public void damagePlayer(final int damage) {
 		player.setLastDamage(damage);
 		player.setHits(player.getHits() - damage);
 		player.getActionSender().sendStat(SkillType.HITS.getSkill());
 		player.informOfModifiedHits(player);
 	}
 
 	public void attackPlayer(final Npc npc) { 
 		npc.attack(player);
 	}
 	
 	public FightEvent fightNpc(final Npc npc) {
 		return fightNpc(npc, false);
 	}
 	
 	public FightEvent fightNpc(final Npc npc, boolean invincibleMode) {
 		player.resetAll();
 		player.setStatus(Action.FIGHTING_MOB);
 		npc.resetPath();
 		
 		player.setLocation(npc.getLocation(), true);
 
 		for (Player p : player.getViewArea().getPlayersInView()) {
 			p.removeWatchedPlayer(player);
 		}
 	
 		player.setBusy(true);
 		player.setSprite(9);
 		player.setOpponent(npc);
 		player.setCombatTimer();
 		npc.setBusy(true);
 		npc.setSprite(8);
 		npc.setOpponent(player);
 		npc.setCombatTimer();
 		FightEvent fighting = new FightEvent(player, npc);
 		fighting.setLastRun(0);
 		fighting.setOpponentInvincible(invincibleMode);
 		World.getWorld().getDelayedEventHandler().add(fighting);
 		return fighting;
 	}
 	
 	public void showBubble(int itemId) {
 		player.informGroupOfBubble(new Bubble(player, itemId));
 	}
 	
 	public void sendSound(String soundName) {
 		player.getActionSender().sendSound(soundName);
 	}
 	
 	public void sendSleep(boolean isBed) {
 		player.getActionSender().sendEnterSleep();
 		player.startSleepEvent(isBed);
 	}
 	
 	public void movePlayer(int x, int y, boolean showBubble) {
 		player.teleport(x, y, showBubble);
 	}
 	
 	public boolean objectExists(GameObject object) {
 		return World.getWorld().getTile(object.getX(), object.getY()).hasGameObject();
 	}
 	
 	public GameObject createNewObject(GameObject object) {
 		return World.getWorld().getTile(object.getX(), object.getY()).getGameObject();
 	}
 	
 	public void spawnObject(int x, int y, int id, int direction, int type, boolean delay, GameObjectLoc loc, int respawnTime) {
 		spawnObject(new Point(x,y), id, direction, type, delay, loc, respawnTime);
 	}
 	
 	public void spawnObject(Point location, int id, int direction, int type, boolean delayRespawn, final GameObjectLoc loc, final int respawnTime) {
 		World.getWorld().registerGameObject(new GameObject(location, id, direction, type));
 		if(delayRespawn) {
 			World.getWorld().delayedSpawnObject(loc, respawnTime);
 		}
 	}
 	
 	public int getRandom(int start, int n) {
 		return DataConversions.random(start, n);
 	}
 	
 	public int getTotalPlayers() {
 		return World.getWorld().countPlayers();
 	}
 	
 	public int getTotalNpcs() {
 		return World.getWorld().countNpcs();
 	}
 	
 	// Shops
 	public Shop[] getShopFromDatabase(String... name) {
 		return null;
 	}
 	
 	public void sleep(final int milliseconds) {
 		try {
 			Thread.sleep(milliseconds);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		} 
 	} 
 
 }
