 package net.skycraftmc.SkyQuest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.skycraftmc.SkyQuest.event.ObjectiveCompleteEvent;
 import net.skycraftmc.SkyQuest.quest.Objective.ObjectiveType;
 import net.skycraftmc.SkyQuest.quest.Quest;
 import net.skycraftmc.SkyQuest.quest.KillObjective;
 import net.skycraftmc.SkyQuest.util.SkyQuestUtil;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.sun.swing.internal.plaf.metal.resources.metal;
 
 public class SkyQuestListener implements Listener
 {
 	private SkyQuestMain plugin;
 	public SkyQuestListener(SkyQuestMain plugin)
 	{
 		this.plugin = plugin;
 	}
 	@EventHandler
 	public void SignInteract(PlayerInteractEvent event)
 	{
 		if(event.getAction() == Action.LEFT_CLICK_BLOCK && SkyQuestUtil.isSign(event.getClickedBlock()))
 		{
 			
 		}
 	}
 	@EventHandler
 	public void onSignChange(SignChangeEvent event)
 	{
 		if(event.getLine(0).toLowerCase().equalsIgnoreCase("[accept quest]"))
 		{
 			event.setLine(0, ChatColor.GOLD + "[Accept Quest]");
 			if(!event.getPlayer().hasPermission("skyquest.createsign"))
 			{
 				event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to create quest signs!");
 				event.setCancelled(true);
 			}
 		}
 		if(event.getLine(0).toLowerCase().equalsIgnoreCase("[finish quest]"))
 		{
 			event.setLine(0, ChatColor.GOLD + "[Finish Quest]");
 			if(!event.getPlayer().hasPermission("skyquest.createsign"))
 			{
 				event.getPlayer().sendMessage(ChatColor.RED + "You are not allowed to create quest signs!");
 				event.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler
 	public void KillObjective(EntityDeathEvent event)
 	{
 		CreatureType c = SkyQuestUtil.getTypeFromEntity(event.getEntity());
 		if(c == null)return;
 		if(event.getEntity().getLastDamageCause() == null)return;
 		if(!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent))return;
 		EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event.getEntity().getLastDamageCause();
 		Player player = null;
 		if(e.getDamager() instanceof Arrow)
 		{
 			Arrow a = (Arrow)e.getDamager();
 			if(a.getShooter() == null)return;
 			if(a.getShooter() instanceof Player)player = (Player)a.getShooter();
 		}
 		else if(e.getDamager()instanceof Player)player = (Player)e.getDamager();
		else return;
 		if(plugin.qm.getQuests(player) == null)return;
 		for(Quest q:plugin.qm.getQuests(player))
 		{
 			if(q.getCurrentObjective().getType() != ObjectiveType.KILL)continue;
 			KillObjective ko = (KillObjective)q.getCurrentObjective();
 			if(ko.isComplete())return;
 			if(ko.getTargetType() == null)return;
 			if(c != ko.getTargetType())return;
 			((KillObjective)q.getCurrentObjective()).setProgress(ko.getProgress() + 1);
 			player.sendMessage(ChatColor.GREEN + "Progress: " + ko.getProgressAsString());
 			if(ko.getProgress() > ko.getRawTarget())
 			{
 				ObjectiveCompleteEvent oce = new ObjectiveCompleteEvent(player, q, q.getCurrentObjective());
 				((KillObjective)q.getCurrentObjective()).setComplete(true);
 				plugin.getServer().getPluginManager().callEvent(oce);
 			}
 		}
 	}
 	@EventHandler
 	public void ObjectiveComplete(ObjectiveCompleteEvent event)
 	{
 		Player player = event.getPlayer();
 		List<String> reward = event.getObjective().getRewards();
 		ArrayList<ItemStack>r = new ArrayList<ItemStack>();
 		for(String s:reward)
 		{
 			ItemStack i = SkyQuestUtil.parseItemReward(s);
 			if(i != null)r.add(i);
 		}
 		for(ItemStack i:r)player.getInventory().addItem(i);
 	}
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent event)
 	{
 		plugin.cm.loadData(event.getPlayer());
 	}
 	@EventHandler
 	public void onPlayerLeave(PlayerQuitEvent event)
 	{
 		plugin.cm.saveData(event.getPlayer());
 	}
 }
