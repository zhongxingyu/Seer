 package net.loadingchunks.plugins.Leeroy.Types;
 
 import net.loadingchunks.plugins.Leeroy.Leeroy;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityCombustByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 import com.topcat.npclib.NPCManager;
 import com.topcat.npclib.entity.HumanNPC;
 import com.topcat.npclib.entity.NPC;
 import com.topcat.npclib.nms.NpcEntityTargetEvent;
 import com.topcat.npclib.nms.NpcEntityTargetEvent.NpcTargetReason;
 
 /* When adding new NPC:
  *  Add handling to LeeroyNPCHandler.java
  *  Add handling of events in LeeroyNPCListener.java
  */
 
 public class BasicNPC
 {
 	public NPCManager manager;
 	public String name;
 	public HumanNPC npc;
 	public Leeroy plugin;
 	public String message1 = "";
 	public String message2 = "";
 	public String message3 = "";
 	public String message4 = "";
 	public Location original;
 
 	public BasicNPC(Leeroy plugin, String name, Location l, String id, String msg1, String msg2, String msg3, String msg4, boolean isnew, String world, String hrtype, String type)
 	{
 		this.plugin = plugin;
 		this.original = l;
 		this.manager = new NPCManager(plugin);
 		this.name = name;
 		this.npc = (HumanNPC)manager.spawnHumanNPC(name, l);
 		FixedMetadataValue meta = new FixedMetadataValue(this.plugin, type);
 		FixedMetadataValue hash = new FixedMetadataValue(this.plugin, id);
 		this.npc.getBukkitEntity().setMetadata("leeroy_type", meta);
 		this.npc.getBukkitEntity().setMetadata("leeroy_id", hash);
 		
 		if(isnew)
 			this.plugin.sql.AddNPC(id, name, hrtype, l, world);
 		
		this.npc.lookAtPoint(l);
 		
 		this.message1 = msg1;
 		this.message2 = msg2;
 		this.message3 = msg3;
 		this.message4 = msg4;
 		
 		this.SetBroadcast(this.message4);
 	}
 	
 	public void SetBroadcast(final String msg)
 	{
 		// Doesn't do anything in basic.
 	}
 	
 	public void onTarget(Player player, NpcEntityTargetEvent event)
 	{
 		if(event.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED)
 			this.onRightClick(player, event);
 		else if(event.getNpcReason() == NpcTargetReason.NPC_BOUNCED)
 			this.onBounce(player, event);
 	}
 
 	public void onRightClick(Player player, NpcEntityTargetEvent event)
 	{
 		// Doesn't do anything in basic.
 	}
 	
 	public void onBounce(Player player, EntityTargetEvent event)
 	{
 		// Doesn't do anything in basic.
 	}
 	
 	public void onNear(Player player)
 	{
 		// Doesn't do anything in basic.
 	}
 	
 	public void onHit(Entity assailant, EntityDamageByEntityEvent event)
 	{
 		if(event.getDamager() instanceof Player)
 			this.onPlayer((Player)event.getDamager(), event);
 		else if(event.getDamager() instanceof Monster)
 			this.onMonster((Monster)event.getDamager(), event);
 	}
 
 	public void onPlayer(Player player, EntityDamageByEntityEvent event)
 	{
 		// Doesn't do anything in basic.
 	}
 	
 	public void onMonster(Monster monster, EntityDamageByEntityEvent event)
 	{
 		// Doesn't do anything in basic.
 	}
 	
 	public void broadcast(String msg)
 	{
 		if(msg == null || msg.isEmpty())
 			return;
 
 		HumanNPC tmp = this.npc;
 
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
 
 	public boolean IsNearby(Location e, Location l, Integer r, Integer h)
 	{	
 		if( ((l.getX() + r) > e.getX() && (l.getX() - r) < e.getX()) &&
 				((l.getY() + h) > e.getY() && (l.getY() - h) < e.getY()) &&
 				((l.getZ() + r) > e.getY() && (l.getY() - r) < e.getY()))
 		{
 			return true;
 		} else
 			return false;
 	}
 }
