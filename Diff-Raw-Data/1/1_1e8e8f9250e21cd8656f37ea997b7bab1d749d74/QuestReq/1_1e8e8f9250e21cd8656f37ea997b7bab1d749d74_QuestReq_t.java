 package com.theminequest.MineQuest.Quest;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.theminequest.MineQuest.MineQuest;
 import com.theminequest.MineQuest.API.Managers;
 import com.theminequest.MineQuest.API.Group.QuestGroup;
 import com.theminequest.MineQuest.API.Quest.QuestRequirement;
 import com.theminequest.MineQuest.API.Tracker.QuestStatistic;
 import com.theminequest.MineQuest.API.Quest.QuestDetails;
 
 public class QuestReq implements QuestRequirement {
 
 	private Type type;
 	private String details;
 	private QuestDetails quest;
 	
 	public QuestReq(Type type, QuestDetails quest, String details){
 		this.type = type;
 		this.details = details;
 		this.quest = quest;
 	}
 
 	@Override
 	public Type getType() {
 		return type;
 	}
 
 	@Override
 	public String getDetails() {
 		return details;
 	}
 	
 	@Override
 	public QuestDetails getQuest() {
 		return quest;
 	}
 
 	@Override
 	public boolean isSatisfied(Player player) {
 		switch(type){
 		case NEVERDONE:
 			QuestStatistic stat = Managers.getStatisticManager().getStatistic(player.getName(),QuestStatistic.class);
 			for (String q : stat.getCompletedQuests())
 				if (q.equals(quest.getProperty(QuestDetails.QUEST_NAME)))
 					return false;
			break;
 		case GIVE:
 			int id = Integer.parseInt(details);
 			if (player.getItemInHand().getTypeId()!=id)
 				return false;
 			break;
 		case GROUPSIZE:
 			QuestGroup gsg = Managers.getQuestGroupManager().get(player);
 			if (gsg!=null){
 				int size = Integer.parseInt(details);
 				if (gsg.getMembers().size()>size)
 					return false;
 			}
 			break;
 		case ITEM:
 			String[] ivalues = details.split(":");
 			int qty = Integer.parseInt(ivalues[0]);
 			int ivalue = Integer.parseInt(ivalues[1]);
 			short idamage = Short.parseShort(ivalues[2]);
 			if (!player.getInventory().contains(new ItemStack(ivalue,qty,idamage)))
 				return false;
 			break;
 		case BELOWLEVEL:
 			int level1 = Integer.parseInt(details);
 			if (player.getLevel()>level1)
 				return false;
 			break;
 		case ABOVELEVEL:
 			int level2 = Integer.parseInt(details);
 			if (player.getLevel()<level2)
 				return false;
 			break;
 		case MONEY:
 			if (MineQuest.economy!=null){
 				double moneyvalue = Double.parseDouble(details);
 				if (!MineQuest.economy.has(player.getName(),moneyvalue))
 					return false;
 			}
 			break;
 		case PERMISSION:
 			if (!player.hasPermission(details))
 				return false;
 			break;
 		case PLAYER:
 			String[] playernames = details.split(",");
 			for (String s : playernames){
 				if (s.equalsIgnoreCase(player.getName()))
 					return true;
 			}
 			return false;
 		case PREREQ:
 			break;
 		case TIME:
 			break;
 		case WEATHER:
 			break;
 		case WORLD:
 			break;
 		default:
 			break;
 		}
 		return true;
 	}
 
 
 
 }
