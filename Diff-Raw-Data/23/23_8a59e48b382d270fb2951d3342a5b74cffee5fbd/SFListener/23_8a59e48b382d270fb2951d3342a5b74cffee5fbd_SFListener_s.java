 /*  
  *  SkyFall, Minecraft bukkit plugin
  *  (c)2012, fromgate, fromgate@gmail.com
  *  http://dev.bukkit.org/server-mods/skyfall/
  *    
  *  This file is part of Dogtags.
  *  
  *  SkyFall is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  SkyFall is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with WeatherMan.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package fromgate.skyfall;
 
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 
 public class SFListener implements Listener{
 	SkyFall plg;
 	FGUtil u;
 
 	public SFListener (SkyFall plg){
 		this.plg = plg;
 		this.u = plg.u;
 	}
 
 
 
 	@EventHandler (priority = EventPriority.NORMAL)
 	public void onPlayerJoin (PlayerJoinEvent event){
 		u.UpdateMsg(event.getPlayer());
		if (plg.pset.containsKey(event.getPlayer().getName()))	plg.pset.remove(event.getPlayer().getName());
 	}
 
 
 	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = true)
 	public void onPlayerMove (PlayerMoveEvent event){
 		Player p = event.getPlayer();
 		if ((plg.worlds.size()>0)&&((p.getLocation().getY()<=0)||(p.getLocation().getY()>=256))){
 			String wn = p.getWorld().getName();
 			
 
 
 			if ((p.getLocation().getY()<0)&&((!plg.worlds.get(wn).world_under.isEmpty()))){
 				// Падение
 				Location loc = plg.getFallLocation(p.getLocation(), 255, plg.worlds.get(wn).world_under, plg.fall_random_radius);
 
 
 
 				while (!loc.getChunk().isLoaded())
 					loc.getChunk().load();
 				p.teleport(loc, TeleportCause.PLUGIN);
 
 				if (plg.showmsg) u.PrintMSG (p, "msg_fallmsg",plg.worlds.get(wn).world_under);  
 
 			} else if ((p.getLocation().getY()>=256)&&((!plg.worlds.get(wn).world_above.isEmpty()))){
 				// Взлёт ;)
 				Location loc = plg.getFallLocation(p.getLocation(), 1, plg.worlds.get(wn).world_above, 0);
 				while (!loc.getChunk().isLoaded())
 					loc.getChunk().load();
 
 				p.teleport(loc, TeleportCause.PLUGIN);
 				if (plg.showmsg) u.PrintMSG (p, "msg_climbmsg",plg.worlds.get(wn).world_under);
 
 
 				///////////////////////////////////////////////////////////////////////
 				// Нужно продумать как наиболее эффективно сделать "подставку" 
 				// для поднимающегося игрока
 				///////////////////////////////////////////////////////////////////////
 				/*loc.getBlock().getRelative(BlockFace.DOWN).setType(Material.GLASS);
 
 				if (loc.getBlock().getRelative(BlockFace.DOWN).getType()==Material.AIR) p.sendBlockChange(loc.add(0,-1,0), Material.GLASS, (byte) 0);
 				if (loc.getBlock().getType()!=Material.AIR) p.sendBlockChange(loc, Material.AIR, (byte) 0);
 				if (loc.getBlock().getRelative(BlockFace.UP).getType()!=Material.AIR) p.sendBlockChange(loc.add(0,1,0), Material.AIR, (byte) 0);  */
 
 
 
 				/*				final Location l = loc;
 				final Player pp = p;
 
 				plg.getServer().getScheduler().scheduleSyncDelayedTask(plg, new Runnable() {
 					public void run() {
 						if (l.getBlock().getType()!=Material.AIR) pp.sendBlockChange(l, Material.AIR, (byte) 0);
 						if (l.getBlock().getRelative(BlockFace.UP).getType()!=Material.AIR) pp.sendBlockChange(l.add(0,1,0), Material.AIR, (byte) 0);
 						if (l.getBlock().getRelative(BlockFace.DOWN).getType()==Material.AIR) pp.sendBlockChange(l.add(0,-1,0), Material.GLASS, (byte) 0);
 						pp.teleport(l);
 					}
 				}, 10L); */
 
 
 			}
 
 		}
 
 
 	}
 
 
 
 
 
 }
 
 
 
 
