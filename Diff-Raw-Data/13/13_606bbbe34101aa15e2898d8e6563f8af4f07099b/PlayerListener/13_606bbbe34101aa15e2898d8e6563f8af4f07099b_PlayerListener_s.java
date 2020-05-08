 package net.endercraftbuild.ac.listeners;
 
 
 import java.util.ArrayList;
 
 import net.endercraftbuild.ac.ACMain;
 import net.endercraftbuild.ac.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Sound;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerExpChangeEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.Vector;
 
 public class PlayerListener implements Listener {
 
 
 	public ArrayList<String> Jump = new ArrayList<String>();
 
 	private ACMain plugin;
 
 	public PlayerListener(ACMain plugin) {
 		this.plugin = plugin;
 	}
 	//Just throwing down ideas, will become more classes and stuff later ;P
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerHitPlayer(EntityDamageByEntityEvent event) { 
 		if (!(event.getEntity() instanceof Player))
 			return;
 		//If they are in the shadows? Hidden assasination?
 		//If they have x amount of mana?
 		//If they are holding an assasination weapon? (Hidden blade, dagger)
 		//set damage to higher amount? Silent assasination?
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void SneakFall(EntityDamageEvent event) {
 
 		if(event.getEntity() instanceof Player)
 		{
 		Player player = (Player) event.getEntity();
 		Integer playerexp = player.getTotalExperience();
 		
			if(playerexp < 2)
				player.sendMessage(plugin.prefix + ChatColor.RED + "You need more energy!");
			else {
 				if(event.getCause() == DamageCause.FALL) 
 				{
 					//check if they have enough mana (small amount)
 					if(player.isSneaking() && event.getDamage() > 1)	
 					{
						Utils.setEnergy(player, playerexp -= 2);
 						event.setCancelled(true);
 						player.sendMessage(plugin.prefix + ChatColor.RED + "**ROLL**");
 					}
 				}
 			}
 		}
 	}
 	@EventHandler(ignoreCancelled = true)
 	public void Cloak(PlayerMoveEvent event) { //Make them vanish in the shadows...
 
 
 		Player p = event.getPlayer();
 		Integer playerexp = p.getTotalExperience();
 
 		Location l = p.getLocation();
 
 
 		Location blockaboveplayer = new Location(l.getWorld(), l.getX(),
 				l.getY() + 1, l.getZ());
 		
 		if(p.hasPotionEffect(PotionEffectType.INVISIBILITY))
 			return;
 		if(playerexp < 6) 
 			return;
 		
 		if(blockaboveplayer != null)
 		{
 			int lightlevel = blockaboveplayer.getBlock().getLightLevel();
 			if(lightlevel < 1.5 && p.isSneaking())
 			{
 				
 					Utils.setEnergy(p, playerexp -= 6);
 					p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 240, 1));
 
 					p.sendMessage(plugin.prefix + ChatColor.RED + "You are now hidden in the shadows");
 				}
 			}
 		}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void JoinStartTask(PlayerLoginEvent event) { //player join - Start the EXP giving task for new joiners
 		Player player = event.getPlayer();
 		Integer playerexp = player.getTotalExperience();
 		if(playerexp == 16)
 			return;
 		Utils.setEnergy(player, playerexp += 1);
 		//Give kit on players first join or something
 	}
 	@EventHandler(ignoreCancelled = true)
 	public void Respawn(PlayerRespawnEvent event) { //player dies and looses energy - start task
 		Player player = event.getPlayer();
 		Integer playerexp = player.getTotalExperience();
		if(playerexp == 16)
 			return;
 		Utils.setEnergy(player, playerexp += 1);
 		//Give kit when they respawn (The Feather for double jumps, etc.)
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void PreventNaturalGain(PlayerExpChangeEvent event) { //Prevent getting exp naturaly
 		//event.setCancelled(true); This event isn't cancellable... 
 		//TODO : Cancel all world exp drops
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void DoubleJump(final PlayerMoveEvent event) { //Jump
 
 		Player player = event.getPlayer();
 		Integer playerexp = player.getTotalExperience();
 		Location loc = player.getLocation();
 		Vector jump = player.getLocation().getDirection().multiply(0.2).setY(1);
 		if(playerexp >= 4)
 		{
 
 			if(Jump.contains(player.getName()))
 				return;
 			if(!player.hasPermission("ac.doublejump"))
 					return;
 			if (event.getTo().getBlockY() > event.getFrom().getY() && player.getItemInHand().getType() == Material.FEATHER)
 			{
 				if (!event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(Material.AIR))
 				{
 					player.setVelocity(player.getVelocity().add(jump));
 					player.playSound(loc, Sound.IRONGOLEM_THROW, 10, -10);
 					player.sendMessage(plugin.prefix + ChatColor.RED + "**WOOSH**");
 					Jump.add(player.getName());
 
 					Utils.setEnergy(player, playerexp -= 4);
 				}
 					else if(!(playerexp >= 4)) {
 						player.sendMessage(plugin.prefix + ChatColor.RED + "You need more energy!");
 					}
 					final PlayerListener self = this;
 
 					Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
 						public void run() {
 							if(!self.Jump.contains(event.getPlayer().getName()))
 								return;
 							self.Jump.remove(event.getPlayer().getName());
 						}
 					}, 25L);
 
 				}
 			}
 		}
 	}
 
