 package com.fullwall.Citizens.NPCTypes.Questers;
 
 import java.util.ArrayDeque;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import com.fullwall.Citizens.Interfaces.Clickable;
 import com.fullwall.Citizens.Interfaces.Toggleable;
 import com.fullwall.Citizens.NPCTypes.Questers.Quests.QuestManager;
 import com.fullwall.Citizens.NPCTypes.Questers.Rewards.QuestReward;
 import com.fullwall.Citizens.Properties.PropertyManager;
 import com.fullwall.Citizens.Utils.PageUtils;
 import com.fullwall.Citizens.Utils.PageUtils.PageInstance;
 import com.fullwall.Citizens.Utils.StringUtils;
 import com.fullwall.resources.redecouverte.NPClib.HumanNPC;
 import com.iConomy.util.Messaging;
 
 public class QuesterNPC implements Toggleable, Clickable {
 	private final HumanNPC npc;
 	private PageInstance display;
 	private Player previous;
 	private final ArrayDeque<String> quests = new ArrayDeque<String>();
 
 	/**
 	 * Quester NPC object
 	 * 
 	 * @param npc
 	 */
 	public QuesterNPC(HumanNPC npc) {
 		this.npc = npc;
 	}
 
 	/**
 	 * Add a quest
 	 * 
 	 * @param quest
 	 */
 	public void addQuest(String quest) {
 		quests.push(quest);
 	}
 
 	public void removeQuest(String quest) {
 		quests.removeFirstOccurrence(quest);
 	}
 
 	@Override
 	public void toggle() {
 		npc.setQuester(!npc.isQuester());
 	}
 
 	@Override
 	public boolean getToggle() {
 		return npc.isQuester();
 	}
 
 	@Override
 	public String getName() {
 		return npc.getStrippedName();
 	}
 
 	@Override
 	public String getType() {
 		return "quester";
 	}
 
 	@Override
 	public void saveState() {
 		PropertyManager.get(getType()).saveState(npc);
 	}
 
 	@Override
 	public void register() {
 		PropertyManager.get(getType()).register(npc);
 	}
 
 	@Override
 	public void onLeftClick(Player player, HumanNPC npc) {
 		previous = player;
 		cycle(player);
 	}
 
 	private void cycle(Player player) {
 		quests.addLast(quests.pop());
 		Quest quest = getQuest(quests.peek());
 		display = PageUtils.newInstance(player);
 		display.setSmoothTransition(true);
 		display.header(ChatColor.GREEN + "======= Quest %x/%y - "
 				+ StringUtils.wrap(quest.getName()) + " =======");
 		for (String push : quest.getDescription().split("<br>")) {
 			display.push(push);
 			if (display.elements() % 8 == 0 && display.maxPages() == 1) {
 				display.push(ChatColor.GOLD
 						+ "Right click to continue description.");
 			} else if (display.elements() % 9 == 0) {
 				display.push(ChatColor.GOLD
 						+ "Right click to continue description.");
 			}
 		}
 		if (display.maxPages() == 1)
 			player.sendMessage(ChatColor.GOLD + "Right click to accept.");
 		display.process(1);
 	}
 
 	private Quest getQuest(String name) {
 		return QuestManager.getQuest(name);
 	}
 
 	@Override
 	public void onRightClick(Player player, HumanNPC npc) {
 		if (QuestManager.getProfile(player.getName()).hasQuest()) {
 			PlayerProfile profile = QuestManager.getProfile(player.getName());
			if (profile.getProgress().fullyCompleted()
					&& profile.getProgress().getQuesterUID() == this.npc
							.getUID()) {
 				Quest quest = QuestManager.getQuest(profile.getProgress()
 						.getQuestName());
 				Messaging.send(quest.getCompletedText());
 				for (Reward reward : quest.getRewards()) {
 					if (reward instanceof QuestReward)
 						((QuestReward) reward).grantQuest(player, npc);
 					else
 						reward.grant(player);
 				}
 				profile.setProgress(null);
 				QuestManager.setProfile(player.getName(), profile);

 			}
 		} else {
 			if (previous == null
 					|| !previous.getName().equals(player.getName())) {
 				previous = player;
 				cycle(player);
 			}
 			if (display.currentPage() != display.maxPages()) {
 				display.displayNext();
 				if (display.currentPage() == display.maxPages()) {
 					player.sendMessage(ChatColor.GOLD
 							+ "Right click to accept.");
 				}
 			} else {
 				QuestManager.assignQuest(this.npc, player, quests.peek());
 			}
 		}
 	}
 
 	public ArrayDeque<String> getQuests() {
 		return quests;
 	}
 }
