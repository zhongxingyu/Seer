 package net.loadingchunks.plugins.Leeroy.Types;
 
 import net.loadingchunks.plugins.Leeroy.Leeroy;
 import net.loadingchunks.plugins.Leeroy.LeeroyUtils;
 
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import com.topcat.npclib.entity.HumanNPC;
 import com.topcat.npclib.nms.NpcEntityTargetEvent;
 
 public class PortNPC extends BasicNPC
 {	
 	public String type = "leeroy_npcport";
 	public String hrtype = "port";
 	public long bcastInitial = 20L;
 	public long bcastBetween = 500L;
 
 	public PortNPC(Leeroy plugin, String name, Location l, String id, String msg1, String msg2, String msg3, String msg4, boolean isnew, String world)
 	{
 		super(plugin, name, l, id, msg1, msg2, msg3, msg4, isnew, world, "port", "leeroy_npcport");
 	}
 
 	@Override
 	public void SetBroadcast(final String msg)
 	{
 		if(msg == null || msg.isEmpty())
 			return;
 
 		final HumanNPC tmp = this.npc;
 		this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(this.plugin, new Runnable() {
 			public void run() {
 				for(Entity e : tmp.getBukkitEntity().getNearbyEntities(10,5,10))
 				{
 					if(e instanceof Player)
 					{
 						String fmsg;
 						Player p = (Player)e;
 						fmsg = msg.replaceAll("<player>", p.getDisplayName());
 						fmsg = fmsg.replaceAll("<npc>", npc.getName());
 						p.sendMessage(fmsg);
 					}
 				}
 			}
 		}, this.bcastInitial, this.bcastBetween);
 	}
 
 	// Player bounces (doesn't seem to work)
 	@Override
 	public void onBounce(Player player, EntityTargetEvent event)
 	{
 		return;
 	}
 
 	// Player near
 	@Override
 	public void onNear(Player player)
 	{
 		this.npc.lookAtPoint(player.getEyeLocation());
 		return;
 	}
 
 	// Player right clicks
 	public void onRightClick(Player player, NpcEntityTargetEvent event)
 	{
 		final Player p = player;
 		Location l = null;
 		Boolean making = false;
 
 		if(!this.plugin.mvcore.getMVWorldManager().loadWorld("homeworld_" + player.getName()))
 		{
 			l = this.makeWorld(player);
 			making = true;
 		}
 
 		if(l == null && making)
 		{
 			player.sendMessage("<" + this.name + "> We're just building your homeworld, right click me again to go there!");
 			return;
 		}
 
 		player.sendMessage("<" + this.name + "> You'll be transported to your homeworld in 5 seconds");
 		player.sendMessage("<" + this.name + "> Thank you for using the Chunky Transport System!");
 
 		this.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
 			public void run() {
 				if(plugin.mvcore.getMVWorldManager().isMVWorld("homeworld_" + p.getName()) && plugin.mvcore.getMVWorldManager().loadWorld("homeworld_" + p.getName()))
 					p.teleport(plugin.mvcore.getMVWorldManager().getMVWorld("homeworld_" + p.getName()).getSpawnLocation());
 				else
 					plugin.mvcore.getLogger().warning("[LEEROY] Something is odd! " + p.getName() + "'s homeworld isn't loading!");
 			}
 		},100L);
 	}
 
 	// When a player attacks
 	@Override
 	public void onPlayer(Player assailant, EntityDamageByEntityEvent event)
 	{
 		return;
 	}
 
 	// When a monster attacks
 	@Override
 	public void onMonster(Monster monster, EntityDamageByEntityEvent event)
 	{
 		this.broadcast("We're under attack!");
 		this.npc.lookAtPoint(monster.getLocation());
 		this.npc.animateArmSwing();
 		
 		if(this.IsNearby(monster.getLocation(), event.getDamager().getLocation(), 2, 2))
 			monster.damage(5);
 	}
 
 	public Location makeWorld(Player p)
 	{
 		
 		if(this.plugin.getServer().getWorld("homeworld_" + p.getName()) == null && !this.plugin.mvcore.getMVWorldManager().loadWorld("homeworld_" + p.getName()))
 		{
 			this.plugin.log.info("[LEEROY] Player " + p.getName() + " has no home world, let's make them one!");
 			this.plugin.log.info("[LEEROY] Creating world file from template...");
 			
 			LeeroyUtils.DuplicateWorld(this.plugin.getServer().getWorld("homeworld"), this.plugin, "homeworld_" + p.getName());
 			
 			this.plugin.log.info("[LEEROY] Adding to WM...");
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "mv import homeworld_" + p.getName() + " normal");
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "mvm set monsters false homeworld_" + p.getName());
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "mvm set pvp false homeworld_" + p.getName());
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "mvm set hidden true homeworld_" + p.getName());
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "mvm set animals false homeworld_" + p.getName());
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "mvm set hunger false homeworld_" + p.getName());
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "mvm set autoload false homeworld_" + p.getName());
 			this.plugin.getServer().dispatchCommand((CommandSender) (this.plugin.getServer().getConsoleSender()), "pex user " + p.getName() + " add AntiGuest.* homeworld_" + p.getName());
 			if(!this.plugin.getMVCore().getMVWorldManager().loadWorld("homeworld_" + p.getName()))
 			{
 				p.sendMessage("<" + this.name + "> Something went wrong! Please alert an admin and provide Error Code: 404");
 				return null;
 			}
 		}
 
 		return null;
 	}
 }
