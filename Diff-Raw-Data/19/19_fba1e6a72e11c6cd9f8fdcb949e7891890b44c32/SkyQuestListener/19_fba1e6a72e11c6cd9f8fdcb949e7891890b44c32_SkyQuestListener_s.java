 package net.skycraftmc.SkyQuest;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class SkyQuestListener implements Listener
 {
 	SkyQuestPlugin plugin;
 	QuestManager qm;
 	public SkyQuestListener(SkyQuestPlugin plugin)
 	{
 		this.plugin = plugin;
 		qm = plugin.getQuestManager();
 	}
 	@EventHandler
 	public void killObj(EntityDeathEvent ev)
 	{
 		LivingEntity ent = ev.getEntity();
 		if(ent.getKiller() == null)return;
 		Player player = ent.getKiller();
 		PlayerQuestLog log = qm.getQuestLog(player.getName());
 		for(Quest q:log.getAssigned())
 		{
 			QuestData qd = log.getProgress(q);
 			for(Objective o:q.getObjectives())
 			{
 				if(o.getType() != ObjectiveType.KILL)continue;
 				if(!qd.isAssigned(o.getID()))continue;
 				String prog = qd.getProgress(o.getID());
 				String targ = o.getTarget();
 				if(!ObjectiveType.KILL.isSimilarType(targ, prog))continue;
 				int p = ObjectiveType.KILL.getKills(prog) + 1;
 				int t = ObjectiveType.KILL.getKills(targ);
 				player.sendMessage(ChatColor.GREEN + o.getName() + " (" + p + "/" + t + ")");
 				qd.setProgress(o.getID(), p + " " + ent.getType().name());
 			}
 		}
 	}
 	@EventHandler
 	public void join(PlayerJoinEvent event)
 	{
 		String name = event.getPlayer().getName();
 		if(qm.getQuestLog(name) == null)
 		{
 			PlayerQuestLog log = new PlayerQuestLog(name);
 			qm.addQuestLog(log);
 			
 			//Testing
 			log.assign(qm.getQuest("test"));
 		}
 	}
 	@EventHandler
 	public void interact(PlayerInteractEvent event)
 	{
 		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)return;
 		//TODO: Add ItemStack check
 		plugin.getQuestLogManager().openQuestLog(event.getPlayer());
 	}
 }
