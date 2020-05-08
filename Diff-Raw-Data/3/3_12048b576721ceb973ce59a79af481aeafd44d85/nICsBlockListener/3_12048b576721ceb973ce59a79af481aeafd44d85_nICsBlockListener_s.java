 package com.niccholaspage.nICs;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
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
 		if (!(plugin.ics.containsKey(text))) return;
 		if (plugin.powers.contains(event.getBlock())){
 			plugin.powers.remove(event.getBlock());
 			return;
 		}else {
 			plugin.powers.add(event.getBlock());
 		}
 		Boolean success = plugin.ics.get(text).run(plugin, event);
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
 			if (!(plugin.permissions.has(event.getPlayer(), "nICs.ics." + text.toLowerCase()))){
 				event.getPlayer().sendMessage(ChatColor.RED + "You cannot create the IC " + text +"!");
 				event.setCancelled(true);
 				return;
 			}
 		}
 		String[] lines = event.getLines();
 		String canplace = plugin.ics.get(text).canPlace(lines); 
 		if (!(canplace == null)){
 			event.getPlayer().sendMessage(ChatColor.RED + canplace);
 			event.setCancelled(true);
 			return;
 		}
 		for (int i = 0; i< lines.length; i++){
 			event.setLine(i, lines[i]);
 		}
 		event.setLine(0, plugin.ics.get(text).getName());
 		event.getPlayer().sendMessage(ChatColor.GREEN + plugin.ics.get(text).getName() + " has been created!");
 	}
 }
