 package com.adamki11s.npcs.tasks;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.adamki11s.commands.QuestXCMDExecutor;
 import com.adamki11s.extras.inventory.ExtrasInventory;
 import com.adamki11s.io.FileLocator;
 import com.adamki11s.questx.QuestX;
 import com.adamki11s.reputation.ReputationManager;
 
 public class TaskManager {
 
 	final String pName;
 
 	TaskLoader currentTask;
 
 	public TaskManager(String playerName, TaskLoader task) {
 		this.pName = playerName;
 		this.currentTask = task;
 		QuestX.logMSG("TaskManager instantitated");
 	}
 
 	public TaskLoader getTaskLoader() {
 		return this.currentTask;
 	}
 
 	public boolean isTrackingEntityKills() {
 		return this.getTaskLoader().isKillEntities();
 	}
 
 	public boolean isTrackingNPCKills() {
 		return this.getTaskLoader().isKillNPCS();
 	}
 
 	public boolean doesNeedToFetchItems() {
 		return this.getTaskLoader().isFetchItems();
 	}
 
 	public void trackEntityKill(EntityType e) {
 		this.getTaskLoader().getEKT().trackKill(e);
 	}
 
 	public void trackNPCKill(String npcName) {
		if (this.isTrackingNPCKills()) {
			this.getTaskLoader().getNKT().trackKill(npcName);
		}
 	}
 
 	public boolean isTaskCompleted() {
 		boolean kills = true, items = true, npcKills = true;
 		if (this.isTrackingEntityKills()) {
 			kills = this.areEntityKillsCompleted();
 		}
 		if (this.doesNeedToFetchItems()) {
 			items = this.areRequiredItemsGathered();
 		}
 		if (this.isTrackingNPCKills()) {
 			npcKills = this.areNPCKillsCompleted();
 		}
 		return (kills && items && npcKills);
 	}
 
 	boolean areEntityKillsCompleted() {
 		return this.getTaskLoader().getEKT().areRequiredEntitiesKilled();
 	}
 
 	boolean areNPCKillsCompleted() {
 		return this.getTaskLoader().getNKT().areRequiredNPCSKilled();
 	}
 
 	boolean areRequiredItemsGathered() {
 		ItemStack[] req = this.getTaskLoader().getRequiredItems();
 		Player p = Bukkit.getServer().getPlayer(pName);
 		if (p != null) {
 			ItemStack[] pContents = p.getInventory().getContents();
 			for (ItemStack is : req) {
 				int currentIsSum = 0;
 				for (ItemStack contained : pContents) {
 					if (contained != null) {
 						if (contained.getTypeId() == is.getTypeId() && contained.getData().getData() == is.getData().getData()) {
 							currentIsSum += contained.getAmount();
 						}
 					}
 				}
 				if (currentIsSum < is.getAmount()) {
 					return false;
 				}
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public String getPlayerName() {
 		return this.pName;
 	}
 
 	public String getIncompleteTaskSpeech() {
 		return this.getTaskLoader().getIncompleteTaskSpeech();
 	}
 
 	public void sendWhatIsLeftToDo(Player p) {
 		boolean kills = true, items = true, npcKills = true;
 		if (this.isTrackingEntityKills()) {
 			kills = this.areEntityKillsCompleted();
 		}
 		if (this.doesNeedToFetchItems()) {
 			items = this.areRequiredItemsGathered();
 		}
 		if (this.isTrackingNPCKills()) {
 			npcKills = this.areNPCKillsCompleted();
 		}
 		if (!kills) {
 			QuestX.logChat(p, this.getTaskLoader().getEKT().sendEntitiesToKill());
 		}
 		if (!items) {
 			QuestX.logChat(p, this.sendItemsToGather());
 		}
 		if (!npcKills) {
 			QuestX.logChat(p, this.getTaskLoader().nkt.sendNPCSToKill());
 		}
 	}
 
 	String sendItemsToGather() {
 		ItemStack[] req = this.getTaskLoader().getRequiredItems();
 		Player p = Bukkit.getServer().getPlayer(pName);
 		StringBuilder buff = new StringBuilder();
 		buff.append(ChatColor.RED).append("Collect : ").append(ChatColor.RESET);
 		if (p != null) {
 			ItemStack[] pContents = p.getInventory().getContents();
 			for (ItemStack is : req) {
 				int currentIsSum = 0;
 				for (ItemStack contained : pContents) {
 					if (contained != null) {
 						if (contained.getTypeId() == is.getTypeId() && contained.getData().getData() == is.getData().getData()) {
 							currentIsSum += contained.getAmount();
 						}
 					}
 				}
 				if (currentIsSum < is.getAmount()) {
 					buff.append(is.getAmount() - currentIsSum).append(" ").append(is.getType().toString()).append(", ");
 				}
 			}
 			return buff.toString();
 		} else {
 			return "";
 		}
 	}
 
 	public String getCompleteTaskSpeech() {
 		return this.getTaskLoader().getCompleteTaskSpeech();
 	}
 
 	public void awardPlayer(Player p) {
 
 		ExtrasInventory ei = new ExtrasInventory();
 
 		if (this.doesNeedToFetchItems()) {
 			for (ItemStack is : this.getTaskLoader().getRequiredItems()) {
 				ei.removeFromInventory(p, is.getTypeId(), is.getAmount());
 			}
 		}
 
 		if (this.getTaskLoader().isAwardItems()) {
 			ItemStack[] rewardItems = this.getTaskLoader().getRewardItems();
 			for (ItemStack i : rewardItems) {
 				if (i != null) {
 					int empty = -1;
 					if ((empty = p.getInventory().firstEmpty()) != -1) {
 						p.getInventory().addItem(i);
 					} else {
 						p.getWorld().dropItemNaturally(p.getLocation(), i);
 					}
 				}
 			}
 		}
 
 		if (this.getTaskLoader().isAwardExp()) {
 			for (int exp = 1; exp <= this.getTaskLoader().getRewardExp(); exp++) {
 				ExperienceOrb orb = p.getWorld().spawn(p.getLocation().add(0.5, 10, 0.5), ExperienceOrb.class);
 				orb.setExperience(1);
 			}
 		}
 
 		if (this.getTaskLoader().isAwardRep()) {
 			int awardRep = this.getTaskLoader().getRewardRep();
 			ReputationManager.updateReputation(pName, awardRep);
 			// adjust rep
 		}
 
 		if (this.getTaskLoader().isAwardGold()) {
 			if (QuestX.economy.hasAccount(p.getName())) {
 				QuestX.economy.bankDeposit(p.getName(), this.getTaskLoader().getRewardGold());
 			} else {
 				// can't award, no account
 			}
 		}
 
 		if (this.getTaskLoader().isAwardingAddPerms()) {
 			for (String perm : this.getTaskLoader().getAddPerms()) {
 				QuestX.permission.playerAdd(p, perm);
 			}
 		}

 		if (this.getTaskLoader().isAwardingRemPerms()) {
 			for (String perm : this.getTaskLoader().getRemPerms()) {
				if (QuestX.permission.has(p, perm)) {
 					QuestX.permission.playerRemove(p, perm);
 				}
 			}
 		}

		if (this.getTaskLoader().isExecutingPlayerCmds()) {
 			QuestXCMDExecutor.executeAsPlayer(p.getName(), this.getTaskLoader().getPlayerCmds());
 		}

		if (this.getTaskLoader().isExecutingServerCmds()) {
 			QuestXCMDExecutor.executeAsServer(this.getTaskLoader().getServerCmds());
 		}

 		if (this.getTaskLoader().fireWorks) {
 			Location pL = p.getLocation();
 			Fireworks display = new Fireworks(pL, this.getTaskLoader().fwRadius, this.getTaskLoader().fwSectors);
 			display.circularDisplay();
 		}
 
 		TaskRegister.unRegisterTask(this);
 
 		FileLocator.createPlayerNPCProgressionFile(this.getTaskLoader().getNpcName(), this.getPlayerName());
 		// display.showDisplay();
 
 	}
 }
