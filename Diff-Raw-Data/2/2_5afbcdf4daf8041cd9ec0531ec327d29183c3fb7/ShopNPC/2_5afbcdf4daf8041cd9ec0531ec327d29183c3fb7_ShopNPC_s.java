 package net.loadingchunks.plugins.Leeroy.Types;
 
 import java.util.List;
 import java.util.Random;
 
 import net.loadingchunks.plugins.Leeroy.Leeroy;
 import net.loadingchunks.plugins.Leeroy.LeeroyHomeCommand;
 import net.loadingchunks.plugins.Leeroy.LeeroySQL;
 import net.loadingchunks.plugins.Leeroy.LeeroyUtils;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import com.topcat.npclib.entity.HumanNPC;
 import com.topcat.npclib.nms.NpcEntityTargetEvent;
 
 public class ShopNPC extends BasicNPC
 {	
 	public String type = "leeroy_npcshop";
 	public String hrtype = "shop";
 	public long bcastInitial = 20L;
 	public long bcastBetween = 500L;
 
 	public ShopNPC(Leeroy plugin, String name, Location l, String id, String msg1, String msg2, String msg3, String msg4, boolean isnew, String world)
 	{
 		super(plugin, name, l, id, msg1, msg2, msg3, msg4, isnew, world, "shop", "leeroy_npcshop");
 	}
 
 	@Override
 	public void SetBroadcast(final String msg)
 	{
 		if(msg == null || msg.isEmpty())
 			return;
 
 		final HumanNPC tmp = this.npc;
 		this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, new Runnable() {
 			public void run() {
 				List<Entity> entities = tmp.getBukkitEntity().getNearbyEntities(5,2,5);
 				for(Entity e : entities)
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
 		LeeroyHomeCommand[] commands = this.plugin.sql.GetCommands();
 		
 		p.sendMessage("The following homeworld upgrades are available for purchase: ");
 		
 		for(LeeroyHomeCommand comm : commands)
 		{
			p.sendMessage(ChatColor.GOLD + "/hw " + comm.commandString + " - " + comm.commandDescription);
 		}
 		
 		p.sendMessage("To purchase an upgrade, use " + ChatColor.GOLD + "/upgrade <upgradename>" + ChatColor.WHITE + ", for instance " + ChatColor.GOLD + "/upgrade weather" + ChatColor.WHITE+ " to control your homeworld's weather.");
 	}
 }
