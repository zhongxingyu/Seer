 package net.insomniacraft.codeex.InsomniaDOTA;
 
 import java.util.Random;
 
 import net.insomniacraft.codeex.InsomniaDOTA.structures.nexus.IDNexus;
 import net.insomniacraft.codeex.InsomniaDOTA.structures.turrets.IDTurret;
 import net.insomniacraft.codeex.InsomniaDOTA.structures.turrets.IDTurretManager;
 import net.insomniacraft.codeex.InsomniaDOTA.teams.IDTeamManager;
 import net.insomniacraft.codeex.InsomniaDOTA.teams.IDTeam.Colour;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.BlockIterator;
 
 public class IDListener implements Listener {
 	
 	Plugin pl;
 	
 	public IDListener(Plugin p) {
 		this.pl = p;
 	}
 
 	@EventHandler
 	public void onPlayerJoin(PlayerJoinEvent e) {
 		Player p = e.getPlayer();
 		IDTeamManager.setTeam(Colour.NEUTRAL, p);
 	}
 	@EventHandler
 	public void onPlayerSpawn(PlayerRespawnEvent e) {
 		final Player p = e.getPlayer();
 
 		Bukkit.getServer().getScheduler()
 				.scheduleSyncDelayedTask(pl, new Runnable() {
 					public void run() {
 						p.giveExp(4625);
 					}
 				}, 20L);
 	
 		Colour col = IDTeamManager.getTeam(p);
 		Location l = IDTeamManager.getSpawn(col);
 		if (l != null){
 			p.teleport(l);
 		}
 	}
 	@EventHandler
 	public void onPlayerHit (EntityDamageByEntityEvent evt){
		//If both involved are not players then we are not interested
		if (!(evt.getDamager() instanceof Player) || !(evt.getEntity() instanceof Player)){
 			return;
 		}
		//If its not either an attack or projectile we dont care
		if (!(evt.getCause().toString().equals("ENTITY_ATTACK")) || !(evt.getCause().toString().equals("PROJECTILE"))) {
			return;
		}
		Player player = (Player) evt.getEntity();
		Player damager = (Player) evt.getDamager();
 		Colour pCol = IDTeamManager.getTeam(player);
		Colour dCol = IDTeamManager.getTeam(damager);
 		Random r = new Random();
 		int num = r.nextInt(4);
 		if (pCol.equals(dCol)){
 			switch (num){
 				case 0: 
 					damager.sendMessage(ChatColor.DARK_RED + "That's a friendly!");
 					break;
 				case 1: 
 					damager.sendMessage(ChatColor.DARK_RED + "Watch your fire!");
 					break;
 				case 2: 
 					damager.sendMessage(ChatColor.DARK_RED + "It's not that dark outside!");
 					break;
 				case 3: 
 					damager.sendMessage(ChatColor.DARK_RED + "What, are you blind? I'm a friendly!");
 					break;
 			}
 			evt.setCancelled(true);
 		} else {
			player.sendMessage("[DEBUG] You have been hit!");
			damager.sendMessage("[DEBUG] You have hit an enemy!");
 		}
 	}
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent e) {
 		Player p = e.getPlayer();
 		if (p.getName().equalsIgnoreCase(IDCommands.setupPlayer)) {
 			pl.getServer().dispatchCommand(pl.getServer().getConsoleSender(), "setup");
 		}
 		IDTeamManager.removeFromTeam(p);
 	}
 	
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		if (!IDCommands.setup) {
 			return;
 		}
 		if (!(IDCommands.setupPlayer.equalsIgnoreCase(e.getPlayer().getName()))) {
 			return;
 		}
 		Block b = e.getClickedBlock();
 		if (b == null) {
 			return;
 		}
 		//Check if already in use w/ turrets
 		if (IDTurretManager.getTurret(b) != null) {
 			e.getPlayer().sendMessage("Block already in use!");
 			return;
 		}
 		if (IDGameManager.getNexus(b) != null) {
 			e.getPlayer().sendMessage("Block already in use!");
 			return;
 		}
 		IDBlockSelector.addBlock(b);
 		e.getPlayer().sendMessage(IDBlockSelector.getSize()+" block(s) selected.");
 	}
 	
 	@EventHandler
 	public void onPlayerChat(PlayerChatEvent e) {
 		Player p = e.getPlayer();
 		String m = e.getMessage();
 		Colour col = IDTeamManager.getTeam(p);
 		if (col.toString().equals("BLUE")) {
 			e.setFormat(ChatColor.BLUE+"<"+p.getName()+"> "+m);
 		}
 		else if (col.toString().equals("RED")) {
 			e.setFormat(ChatColor.RED+"<"+p.getName()+"> "+m);
 		}
 		else if (col.toString().equals("NEUTRAL")) {
 			e.setFormat(ChatColor.GRAY+"<"+p.getName()+"> "+m);
 		}
 	}
 	
 	@EventHandler
 	public void onArrowShot(ProjectileHitEvent e) {
 		Entity projectile = e.getEntity();
 		//Check if not arrow
 		if (!(projectile instanceof Arrow)) {
 			return;
 		}
 		Arrow arrow = (Arrow) projectile;
 		//Check if not player
 		if (!(arrow.getShooter() instanceof Player)) {
 			return;
 		}
 		World w = arrow.getWorld();
 		//Find block arrow hit
 		BlockIterator bi = new BlockIterator(w, arrow.getLocation().toVector(), arrow.getVelocity().normalize(), 0, 4);
 		Block hit = null;
 		while (bi.hasNext()) {
 			hit = bi.next();
 			if (hit.getTypeId() != 0)
 			{
 				break;
 			}
 		}
 		if (IDTurretManager.getHit(hit, arrow)) {
 			IDTurret turret = IDTurretManager.getTurret(hit);
 			turret.doDamage();
 			pl.getServer().broadcastMessage(String.valueOf(turret.getHealth()));
 		}
 		if (IDGameManager.getNexusHit(hit, arrow)) {
 			IDNexus nex = IDGameManager.getNexus(hit);
 			nex.doDamage();
 			pl.getServer().broadcastMessage(String.valueOf(nex.getHealth()));
 		}
 	}
 }
