 package com.adamki11s.npcs;
 
 import java.io.File;
 import java.util.HashSet;
 
 import net.minecraft.server.v1_4_6.Packet;
 import net.minecraft.server.v1_4_6.Packet5EntityEquipment;
 import net.minecraft.server.v1_4_6.WorldServer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.adamki11s.ai.RandomMovement;
 import com.adamki11s.data.ItemStackDrop;
 import com.adamki11s.dialogue.Conversation;
 import com.adamki11s.events.ConversationRegister;
 import com.adamki11s.io.FileLocator;
 import com.adamki11s.npcs.tasks.Fireworks;
 import com.adamki11s.npcs.tasks.TaskManager;
 import com.adamki11s.npcs.tasks.TaskRegister;
 import com.adamki11s.quests.QuestLoader;
 import com.adamki11s.quests.QuestManager;
 import com.adamki11s.quests.QuestTask;
 import com.adamki11s.questx.QuestX;
 import com.adamki11s.sync.io.configuration.SyncConfiguration;
 import com.topcat.npclib.entity.HumanNPC;
 
 //import net.minecraft.server.EntityLiving;
 
 public class SimpleNPC {
 
 	final String name, questName;
 	final ChatColor nameColour;
 	final boolean moveable, attackable, aggressive;
 	final int minPauseTicks, maxPauseTicks, maxVariation, respawnTicks, maxHealth, damageMod;
 	final double retalliationMultiplier;
 	final ItemStackDrop inventory;
 
 	int waitedSpawnTicks = 0;
 
 	RandomMovement randMovement;
 
 	Conversation c;
 
 	HashSet<Integer> completeQuestNodes = new HashSet<Integer>();
 
 	final NPCHandler handle;
 
 	volatile Location fixedLocation, spawnedLocation;
 	boolean isSpawnFixed = false;
 
 	volatile HumanNPC npc;
 	boolean isSpawned = false, underAttack = false;
 
 	final ItemStack[] gear;// boots 1, legs 2, chest 3, head 4, arm 5
 
 	int health;
 
 	public SimpleNPC(NPCHandler handle, String name, ChatColor nameColour, boolean moveable, boolean attackable, boolean aggressive, int minPauseTicks, int maxPauseTicks, int maxVariation,
 			int health, int respawnTicks, ItemStackDrop inventory, ItemStack[] gear, int damageMod, double retalliationMultiplier) {
 		UniqueNameRegister.addNPCName(name);
 		this.name = name;
 		this.nameColour = nameColour;
 		this.moveable = moveable;
 		this.attackable = attackable;
 		this.aggressive = aggressive;
 		this.minPauseTicks = minPauseTicks;
 		this.maxPauseTicks = maxPauseTicks;
 		this.maxVariation = maxVariation;
 		this.maxHealth = health;
 		this.respawnTicks = respawnTicks;
 		this.handle = handle;
 		this.inventory = inventory;
 		this.gear = gear;
 		this.damageMod = damageMod;
 		this.retalliationMultiplier = retalliationMultiplier;
 
 		File fLink = FileLocator.getNPCQuestLinkFile(name);
 		SyncConfiguration cfg = new SyncConfiguration(fLink);
 		cfg.read();
 		this.questName = cfg.getString("QUEST_NAME");
 		String nodes = cfg.getString("NODES");
 		if (nodes != null) {
 			for (String num : nodes.split(",")) {
 				this.completeQuestNodes.add(Integer.parseInt(num));
 			}
 		}
 		QuestX.logMSG("qName = " + this.questName);
 		QuestX.logMSG("nodes = " + cfg.getString("NODES"));
 
 		handle.registerNPC(this);
 	}
 
 	public HashSet<Integer> getCompleteQuestNodes() {
 		return this.completeQuestNodes;
 	}
 
 	public String getQuestName() {
 		return this.questName;
 	}
 
 	public boolean doesLinkToQuest() {
 		return (this.questName != null && !this.questName.equalsIgnoreCase("null") && !this.questName.equalsIgnoreCase("0"));
 	}
 
 	public boolean isMovementScheduled() {
 		return (this.randMovement == null ? false : this.randMovement.isMovementScheduled());
 	}
 
 	public void setFixedLocation(Location l) {
 		this.isSpawnFixed = true;
 		this.fixedLocation = l;
 	}
 
 	public void setNewSpawnLocation(Location l) {
 		// QuestX.logMSG("Setting new spawn location");
 		// QuestX.logMSG(l.toString());
 		this.spawnedLocation = l;
 	}
 
 	public Location getFixedLocation() {
 		return this.fixedLocation;
 	}
 
 	public Location getSpawnedLocation() {
 		return this.spawnedLocation;
 	}
 
 	public boolean isSpawnFixed() {
 		return this.isSpawnFixed;
 	}
 
 	public double getRetalliationMultiplier() {
 		return retalliationMultiplier;
 	}
 
 	public int getDamageMod() {
 		return damageMod;
 	}
 
 	public Conversation getC() {
 		return c;
 	}
 
 	public ItemStack[] getGear() {
 		return gear;
 	}
 
 	public int getMaxHealth() {
 		return maxHealth;
 	}
 
 	public ItemStackDrop getInventory() {
 		return inventory;
 	}
 
 	public int getWaitedSpawnTicks() {
 		return waitedSpawnTicks;
 	}
 
 	public RandomMovement getRandMovement() {
 		return randMovement;
 	}
 
 	public NPCHandler getHandle() {
 		return handle;
 	}
 
 	public HumanNPC getNpc() {
 		return npc;
 	}
 
 	public boolean isSpawned() {
 		return isSpawned;
 	}
 
 	public boolean isConversing() {
 		if (this.c == null) {
 			return false;
 		} else {
 			return this.c.isConversing();
 		}
 	}
 
 	public void setItemInHand(ItemStack item) {
 		this.npc.getInventory().setItemInHand(item);
 		this.updateArmor(0, item);
 	}
 
 	public void setBoots(ItemStack item) {
 		this.npc.getInventory().setBoots(item);
 		this.updateArmor(1, item);
 	}
 
 	public void setLegs(ItemStack item) {
 		this.npc.getInventory().setLeggings(item);
 		this.updateArmor(2, item);
 	}
 
 	public void setChestplate(ItemStack item) {
 		this.npc.getInventory().setChestplate(item);
 		this.updateArmor(3, item);
 	}
 
 	public void setHelmet(ItemStack item) {
 		this.npc.getInventory().setHelmet(item);
 		this.updateArmor(4, item);
 	}
 
 	Player aggressor;
 
 	public Player getAggressor() {
 		return this.aggressor;
 	}
 
 	public void unAggro() {
 		this.aggressor = null;
 		this.underAttack = false;
 	}
 
 	public void damageNPC(Player p, int damage) {
		if(this.isSpawnFixed){
			return;
		}
 		health -= damage;
 		this.aggressor = p;
 		this.underAttack = true;
 		if (this.isConversing()) {
 			ConversationRegister.endPlayerNPCConversation(c.getConvoData().getPlayer());
 		}
 		if (health <= 0) {
 			if (this.isSpawnFixed()) {
 				p.sendMessage("You killed NPC '" + this.getName() + "'. NPC will respawn in " + this.respawnTicks / 20 + " seconds.");
 			} else {
 				p.sendMessage("You killed NPC '" + this.getName() + "'. This NPC may respawn elsewhere now.");
 			}
 			for (ItemStack i : this.inventory.getDrops()) {
 				p.getWorld().dropItemNaturally(this.npc.getBukkitEntity().getLocation(), i);
 			}
 			if (TaskRegister.doesPlayerHaveTask(p.getName())) {
 				TaskManager tm = TaskRegister.getTaskManager(p.getName());
 				tm.trackNPCKill(this.getName());
 			}
 			this.despawnNPC();
 		}
 	}
 
 	public void updateArmor(int slot, org.bukkit.inventory.ItemStack itm) {
 		net.minecraft.server.v1_4_6.ItemStack i = CraftItemStack.asNMSCopy(itm);
 		Packet p = new Packet5EntityEquipment(this.npc.getEntity().id, slot, i);
 		((WorldServer) this.npc.getEntity().world).tracker.a(this.npc.getEntity(), p);
 	}
 
 	public HumanNPC getHumanNPC() {
 		return this.npc;
 	}
 
 	public void updateWaitedSpawnTicks(int ticks) {
 		if (!this.isNPCSpawned()) {
 			this.waitedSpawnTicks += ticks;
 			if (this.waitedSpawnTicks >= this.respawnTicks) {
 				this.spawnNPC();
 			}
 		}
 	}
 
 	boolean canNPCCompleteQuestNode(String player) {
 		int currentNode = QuestManager.getQuestLoader(this.getQuestName()).getCurrentQuestNode(player);
 		return (this.getCompleteQuestNodes().contains(currentNode));
 	}
 
 	public void interact(Player p) {
 		if (!this.isConversing() && !this.isUnderAttack()) {
 
 			if (this.doesLinkToQuest()) {
 				if (QuestManager.doesPlayerHaveQuest(p.getName())) {
 					if (QuestManager.getCurrentQuestName(p.getName()).equalsIgnoreCase(this.getQuestName())) {
 						QuestLoader ql = QuestManager.getQuestLoader(this.getQuestName());
 						QuestTask t = QuestManager.getCurrentQuestTask(p.getName());
 						// run checks
 						if (this.canNPCCompleteQuestNode(p.getName())) {
 							// do complete check
 							if (t.isTaskComplete(p)) {
 								ql.incrementTaskProgress(p.getName());
 								p.sendMessage(t.getCompleteTaskText());
 								if(ql.isQuestComplete(p.getName())){
 									p.sendMessage(ql.getEndText());
 									Fireworks f = new Fireworks(p.getLocation(), 6, 60);
 									f.circularDisplay();
 								}
 							} else {
 								t.sendWhatIsLeftToDo(p);
 							}
 							return;
 						} else {
 							p.sendMessage("Sorry You need to see " + t.getNPCToCompleteName() + " to complete this part of the quest");
 							return;
 						}
 					} else {
 						// doing a different quest
 					}
 				}
 
 				/*QuestTask qt = QuestManager.getCurrentQuestTask(p.getName());
 				if (qt.isTalkNPC()) {
 					String s = qt.getData().toString();
 					if (s.equalsIgnoreCase(this.getName())) {
 						QuestLoader ql = QuestManager.getQuestLoader(QuestManager.getCurrentQuestName(p.getName()));
 						ql.setTaskComplete(p.getName());
 					}
 				}*/
 			} else {
 				
 			}
 
 			if (TaskRegister.doesPlayerHaveTask(p.getName())) {
 				if (TaskRegister.doesPlayerHaveTaskFromNPC(p.getName(), this.getName())) {
 					TaskManager tm = TaskRegister.getTaskManager(p.getName());
 					boolean completed = tm.isTaskCompleted();
 					if (!completed) {
 						QuestX.logChat(p, tm.getIncompleteTaskSpeech());
 						tm.sendWhatIsLeftToDo(p);
 						return;
 					} else {
 						QuestX.logChat(p, tm.getCompleteTaskSpeech());
 						tm.awardPlayer(p);
 						return;
 					}
 				} else {
 					QuestX.logChat(p, "You already have a task, finish or cancel your current task before starting a new one.");
 					return;
 				}
 			}
 			if (!FileLocator.doesNPCDlgFileExist(this.getName())) {
 				p.sendMessage(ChatColor.AQUA + "[QuestX] " + ChatColor.RED + "No dialogue.dlg file found or it is empty!");
 			} else {
 				c = new Conversation(p.getName(), this);
 				c.loadConversation();
 				c.startConversation();
 				Location pl = p.getLocation();
 				this.getHumanNPC().lookAtPoint(new Location(pl.getWorld(), pl.getX(), pl.getY() + 1, pl.getZ()));
 				System.out.println("Conversing = " + this.isConversing());
 			}
 		}
 	}
 
 	public boolean doesNPCIDMatch(String id) {
 
 		return ((HumanNPC) this.handle.getNPCManager().getNPC(id)).getName().equalsIgnoreCase(this.getName());
 	}
 
 	public synchronized void spawnNPC() {
 		if (!isSpawned) {
 
 			this.health = this.maxHealth;
 			this.waitedSpawnTicks = 0;
 			Location toSpawn;
 			if (this.isSpawnFixed()) {
 				toSpawn = this.getFixedLocation();
 			} else {
 				toSpawn = this.getSpawnedLocation();
 			}
 
 			QuestX.logMSG("Spawning NPC " + this.getName());
 			QuestX.logMSG("Log spawn location");
 			QuestX.logMSG(toSpawn.toString());
 
 			this.npc = (HumanNPC) this.handle.getNPCManager().spawnHumanNPC(this.name, toSpawn);
 
 			if (moveable) {
 				this.randMovement = new RandomMovement(this, toSpawn, this.minPauseTicks, this.maxPauseTicks, this.maxVariation);// throwing
 																																	// null
 			}
 
 			this.setBoots(this.gear[0]);
 			this.setLegs(this.gear[1]);
 			this.setChestplate(this.gear[2]);
 			this.setHelmet(this.gear[3]);
 			this.setItemInHand(this.gear[4]);
 
 			isSpawned = true;
 
 			this.handle.registerNPCSpawn(this);
 		}
 	}
 
 	public boolean isNPCSpawned() {
 		return this.isSpawned;
 	}
 
 	public boolean isUnderAttack() {
 		return this.underAttack;
 	}
 
 	public void despawnNPC() {
 		if (isSpawned) {
 			this.isSpawned = false;
 			this.unAggro();
 			this.handle.getNPCManager().despawnHumanByName(this.name);
 			this.randMovement = null;
 			if (!this.isSpawnFixed()) {
 				this.handle.removeNPC(this);
 			}
 		}
 	}
 
 	public void destroyNPCObject() {
 		this.despawnNPC();
 		UniqueNameRegister.removeName(name);
 		this.handle.removeNPC(this);
 	}
 
 	public void moveTick() {
 		if (this.isMoveable() && this.isSpawned() && !this.isMovementScheduled()) {
 			this.randMovement.move();
 		}
 	}
 
 	public void moveTo(Location l) {
 		this.npc.walkTo(l);
 	}
 
 	public void lookAt(Location l) {
 		this.npc.lookAtPoint(l);
 	}
 
 	public int getRespawnTicks() {
 		return this.respawnTicks;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public void setHealth(int health) {
 		this.health = health;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public ChatColor getNameColour() {
 		return nameColour;
 	}
 
 	public boolean isMoveable() {
 		return moveable;
 	}
 
 	public boolean isAttackable() {
 		return attackable;
 	}
 
 	public boolean isAggressive() {
 		return aggressive;
 	}
 
 	public int getMinPauseTicks() {
 		return minPauseTicks;
 	}
 
 	public int getMaxPauseTicks() {
 		return maxPauseTicks;
 	}
 
 	public int getMaxVariation() {
 		return maxVariation;
 	}
 
 }
