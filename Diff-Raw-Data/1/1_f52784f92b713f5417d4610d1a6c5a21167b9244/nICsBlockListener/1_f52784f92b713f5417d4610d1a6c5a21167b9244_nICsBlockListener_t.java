 package com.niccholaspage.nICs;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class nICsBlockListener extends BlockListener {
 	public static nICs plugin;
 	public nICsBlockListener(nICs instance){
 		plugin = instance;
 	}
 	@Override
 	public void onBlockRedstoneChange(BlockRedstoneEvent event){
 		if (!(event.getBlock().getState() instanceof Sign)) return;
 		Sign sign = (Sign)event.getBlock().getState();
 		if (!(plugin.getBlockFrontOfSign(sign).getType().equals(Material.REDSTONE_WIRE))) return;
		if (!(plugin.getBlockFrontOfSign(sign).getData() > 0x0)) return;
 		String text = sign.getLine(1).replace("[", "").replace("]", "").toUpperCase();
 		Boolean power = (event.getNewCurrent() > 1);
 		World world = event.getBlock().getWorld();
 		if (!(plugin.ics.containsKey(text))) return;
 		Boolean success = plugin.ics.get(text).run(plugin, power, world, sign);
 		if (!(success == null)) plugin.setLever(sign, success);
 	}
 	@Override
 	public void onSignChange(SignChangeEvent event){
 		String text = event.getLine(1).replace("[", "").replace("]", "").toUpperCase();;
 		if (!(plugin.ics.containsKey(text))) return;
 		if (!(event.getBlock().getType().equals(Material.WALL_SIGN))){
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(ChatColor.RED + "IC signs can only be created on a wall.");
 			return;
 		}
 		if (!(plugin.permissions == null)){
 			if (!(plugin.permissions.has(event.getPlayer(), "nICs." + text.toLowerCase()))){
 				event.getPlayer().sendMessage(ChatColor.RED + "You cannot create the IC " + text +"!");
 				event.setCancelled(true);
 				return;
 			}
 		}
 		String canplace = plugin.ics.get(text).canPlace(event.getLines()); 
 		if (canplace.equals("") == false){
 			event.getPlayer().sendMessage(ChatColor.RED + canplace);
 			event.setCancelled(true);
 			return;
 		}
 		event.setLine(0, plugin.ics.get(text).getName());
 		event.getPlayer().sendMessage(ChatColor.GREEN + plugin.ics.get(text).getName() + " has been created!");
 	}
 }
