 package net.rageland.ragemod.npcentities;
 
 import net.minecraft.server.ItemInWorldManager;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.World;
 import net.rageland.ragemod.RageMod;
 import net.rageland.ragemod.Util;
 import net.rageland.ragemod.data.PlayerData;
 import net.rageland.ragemod.quest.Quest;
 
 import org.bukkit.entity.Player;
 
 public class QuestEndNPCEntity extends NPCEntity
 {
 	private Quest quest;
 
 	public QuestEndNPCEntity(MinecraftServer minecraftserver, World world,
 			String name, ItemInWorldManager iteminworldmanager, RageMod plugin,
 			Quest quest)
 	{
 		super(minecraftserver, world, name, iteminworldmanager, plugin);
 		this.quest = quest;
 	}
 
 	/**
 	 * Method called when a right click action on the NPC is performed by a
 	 * player. *
 	 * 
 	 * @param player
 	 *            Player that right clicked the entity
 	 */
 	public void rightClickAction(Player player)
 	{
		Util.message(player, "Quest: " + quest.getQuestName());
		Util.message(player, quest.getQuestText());
		Util.message(player, "[Left click npc to accept]");
 	}
 
 	/**
 	 * Method called when a left click action on the NPC is performed by a
 	 * player. *
 	 * 
 	 * @param player
 	 *            Player that left clicked the entity
 	 */
 	public void leftClickAction(Player player)
 	{
 		PlayerData playerData = plugin.players.get(player.getName());
 		if (playerData != null && playerData.activeQuestData.quest == quest)
 		{
 			if (quest.isQuestFinished(playerData))
 			{
 				quest.questEnd(player, playerData);
 			}
 			else
 			{
				Util.message(player,
						"You havent finished your quest. What are you waiting for?");
 			}
 		}
 		else
 		{
			Util.message(player, "Sorry, I can't help you with anything.");
 		}
 
 	}
 }
