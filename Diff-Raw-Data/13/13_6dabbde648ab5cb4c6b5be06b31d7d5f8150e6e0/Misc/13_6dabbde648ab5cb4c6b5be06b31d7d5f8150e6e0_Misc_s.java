 package com.vildaberper.DefaultCommands;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet29DestroyEntity;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.CommandSender;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Ghast;
 import org.bukkit.entity.Giant;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Slime;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Squid;
 import org.bukkit.entity.Wolf;
 import org.bukkit.entity.Zombie;
 import org.bukkit.inventory.ItemStack;
 
 import com.vildaberper.DefaultCommands.Class.DCAfkPlayer;
 import com.vildaberper.DefaultCommands.Class.DCArmor;
 import com.vildaberper.DefaultCommands.Class.DCBan;
 import com.vildaberper.DefaultCommands.Class.DCBlock;
 import com.vildaberper.DefaultCommands.Class.DCCommand;
 import com.vildaberper.DefaultCommands.Class.DCConfiguration;
 import com.vildaberper.DefaultCommands.Class.DCHome;
 import com.vildaberper.DefaultCommands.Class.DCHover;
 import com.vildaberper.DefaultCommands.Class.DCInventoryPlayer;
 import com.vildaberper.DefaultCommands.Class.DCItem;
 import com.vildaberper.DefaultCommands.Class.DCKit;
 import com.vildaberper.DefaultCommands.Class.DCPlayer;
 import com.vildaberper.DefaultCommands.Class.DCPortal;
 import com.vildaberper.DefaultCommands.Class.DCReply;
 import com.vildaberper.DefaultCommands.Class.DCSelection;
 import com.vildaberper.DefaultCommands.Class.DCString;
 import com.vildaberper.DefaultCommands.Class.DCTask;
 import com.vildaberper.DefaultCommands.Class.DCUndo;
 import com.vildaberper.DefaultCommands.Class.DCWarp;
 import com.vildaberper.DefaultCommands.Class.DCWorld;
 import com.vildaberper.DefaultCommands.Enum.Instrument;
 
 public class Misc{
 	public static void removePlayerFromLists(Player player){
 		setGod(player.getEntityId(), false);
 		setFly(player.getEntityId(), false);
 		setCreative(player.getEntityId(), false);
 		setIgnoremob(player.getEntityId(), false);
 		setNopickup(player.getEntityId(), false);
 		setInstakill(player.getEntityId(), false);
 		setLastCommand(player.getName(), null);
 		setSelection(player.getName(), null, null);
 		setReply(player.getName(), null);
 		setAfk(player.getEntityId(), false);
 	}
 
 	@SuppressWarnings("deprecation")
 	public static void chat(Player player, String message){
 		String send = "";
 		String hp = "";
 		List<Player> players = new LinkedList<Player>();
 
 		for(int i = 0; i < player.getHealth(); i++){
 			hp += "|";
 		}
 		hp += ChatColor.DARK_GRAY;
 		for(int i = 0; i < 20 - player.getHealth(); i++){
 			hp += "|";
 		}
 		if(player.getHealth() > 10){
 			hp = ChatColor.GREEN + hp;
 		}else if(player.getHealth() <= 10 && player.getHealth() > 5){
 			hp = ChatColor.GOLD + hp;
 		}else if(player.getHealth() <= 5){
 			hp = ChatColor.RED + hp;
 		}
 		hp += ChatColor.WHITE;
 		send = V.chat
 		.replace("<player>", getName(player.getName()))
 		.replace("<message>", message)
 		.replace("<hp>", hp)
 		.replace("<world>", player.getWorld().getName())
 		.replace("<x>", Double.toString(Math.round(player.getLocation().getX())))
 		.replace("<y>", Double.toString(Math.round(player.getLocation().getY())))
 		.replace("<z>", Double.toString(Math.round(player.getLocation().getZ())));
 		if(Perm.PermissionsHandler != null){
 			send = send
 			.replace("<prefix>", Perm.PermissionsHandler.getGroupPrefix(player.getWorld().getName(), Perm.PermissionsHandler.getGroup(player.getWorld().getName(), player.getName())))
 			.replace("<suffix>", Perm.PermissionsHandler.getGroupSuffix(player.getWorld().getName(), Perm.PermissionsHandler.getGroup(player.getWorld().getName(), player.getName())))
 			.replace("<group>", Perm.PermissionsHandler.getGroup(player.getWorld().getName(), player.getName()));
 		}else{
 			send = send
 			.replace("<prefix>", "")
 			.replace("<suffix>", "")
 			.replace("<group>", "");
 		}
 		send = Util.replaceColor(send);
 		if(getConfig(player).getBoolean("separate_chat")){
 			for(Player p : player.getWorld().getPlayers()){
 				if(getConfig(player).getBoolean("local_chat")){
 					if(Util.getDistance(player.getLocation(), p.getLocation()) <= getConfig(player).getDouble("local_chat_distance")){
 						players.add(p);
 					}
 				}else{
 					players.add(p);
 				}
 			}
 		}else{
 			for(Player p : V.plugin.getServer().getOnlinePlayers()){
 				players.add(p);
 			}
 		}
 		for(Player p : players){
 			sendMessage(p, send);
 		}
 	}
 
 	public static void afkMove(Player player){
 		if(Misc.isAfk(player.getEntityId()) && Perm.hasPermissionSilent(player, "dc.afk.self")){
 			Misc.setAfk(player.getEntityId(), false);
 			Misc.getAfkPlayer(player.getEntityId()).resetTime();
 			L.log(Misc.getSenderCmdMsg("c_afk", player, Misc.getPlayers(player, player.getName()), Misc.isAfk(Misc.getPlayers(player, player.getName()).get(0).getEntityId())));
 			Misc.sendMessage(player, Misc.getSenderCmdMsg("c_afk", player, Misc.getPlayers(player, player.getName()), Misc.isAfk(Misc.getPlayers(player, player.getName()).get(0).getEntityId())));
 		}
 	}
 
 	public static void longGrassDestroyCreative(Player player, Block block){
 		if(block.getType().equals(Material.LONG_GRASS)){
 			if((block.getData() == (byte) 1 || block.getData() == (byte) 2) && Math.random() <= getConfig(block).getDouble("long_grass_drop_seed_rate")){
 				Util.addIfNotInInventory(player, new ItemStack(Material.SEEDS, 1));
 			}
 			block.setType(Material.AIR);
 		}
 	}
 
 	public static void longGrassDestroy(Block block){
 		if(block.getType().equals(Material.LONG_GRASS)){
 			if((block.getData() == (byte) 1 || block.getData() == (byte) 2) && Math.random() <= getConfig(block).getDouble("long_grass_drop_seed_rate")){
 				block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.SEEDS, 1));
 			}
 			block.setType(Material.AIR);
 		}
 	}
 
 	public static void leafDestroyCreative(Player player, Block block){
 		if(block.getType().equals(Material.LEAVES)){
 			if(getConfig(block).getBoolean("leaf_drop_apple") && Math.random() <= getConfig(block).getDouble("leaf_drop_apple_rate")){
 				Util.addIfNotInInventory(player, new ItemStack(Material.APPLE, 1));
 			}
 			if(getConfig(block).getBoolean("leaf_drop_sapling") && Math.random() <= getConfig(block).getDouble("leaf_drop_sapling_rate")){
 				if(block.getData() == (byte) 1 || block.getData() == (byte) 9){
 					Util.addIfNotInInventory(player, new ItemStack(Material.SAPLING, 1, (short) 1, Byte.parseByte("1")));
 				}else if(block.getData() == (byte) 2 || block.getData() == (byte) 10){
 					Util.addIfNotInInventory(player, new ItemStack(Material.SAPLING, 1, (short) 1, Byte.parseByte("2")));
 				}else{
 					Util.addIfNotInInventory(player, new ItemStack(Material.SAPLING, 1, (short) 1, Byte.parseByte("0")));
 				}
 			}
 			if(getConfig(block).getBoolean("leaf_drop_leaf") && Math.random() <= getConfig(block).getDouble("leaf_drop_leaf_rate")){
 				if(block.getData() == (byte) 1 || block.getData() == (byte) 9){
 					Util.addIfNotInInventory(player, new ItemStack(Material.LEAVES, 1, (short) 1, Byte.parseByte("1")));
 				}else if(block.getData() == (byte) 2 || block.getData() == (byte) 10){
 					Util.addIfNotInInventory(player, new ItemStack(Material.LEAVES, 1, (short) 1, Byte.parseByte("2")));
 				}else{
 					Util.addIfNotInInventory(player, new ItemStack(Material.LEAVES, 1, (short) 1, Byte.parseByte("0")));
 				}
 			}
 			block.setType(Material.AIR);
 		}
 	}
 
 	public static void leafDestroy(Block block, boolean shears){
 		if(block.getType().equals(Material.LEAVES)){
 			if(getConfig(block).getBoolean("leaf_drop_apple") && Math.random() <= getConfig(block).getDouble("leaf_drop_apple_rate")){
 				block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.APPLE, 1));
 			}
 			if(getConfig(block).getBoolean("leaf_drop_sapling") && Math.random() <= getConfig(block).getDouble("leaf_drop_sapling_rate")){
 				if(block.getData() == (byte) 1 || block.getData() == (byte) 9){
 					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.SAPLING, 1, (short) 1, Byte.parseByte("1")));
 				}else if(block.getData() == (byte) 2 || block.getData() == (byte) 10){
 					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.SAPLING, 1, (short) 1, Byte.parseByte("2")));
 				}else{
 					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.SAPLING, 1, (short) 1, Byte.parseByte("0")));
 				}
 			}
 			if(shears && getConfig(block).getBoolean("leaf_drop_leaf") && Math.random() <= getConfig(block).getDouble("leaf_drop_leaf_rate")){
 				if(block.getData() == (byte) 1 || block.getData() == (byte) 9){
 					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.LEAVES, 1, (short) 1, Byte.parseByte("1")));
 				}else if(block.getData() == (byte) 2 || block.getData() == (byte) 10){
 					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.LEAVES, 1, (short) 1, Byte.parseByte("2")));
 				}else{
 					block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.LEAVES, 1, (short) 1, Byte.parseByte("0")));
 				}
 			}
 			block.setType(Material.AIR);
 		}
 	}
 
 	public static String getMotd(String player){
 		return getString("motd")
 		.replace("<player>", player)
 		.replace("<online>", Integer.toString(getPlayerList(V.plugin.getServer().getPlayer(player)).size()))
 		.replace("<max>", Integer.toString(V.plugin.getServer().getMaxPlayers()))
 		.replace("<players>", getPlayerList(V.plugin.getServer().getPlayer(player)).toString().substring(1, getPlayerList(V.plugin.getServer().getPlayer(player)).toString().length() - 1).replace(",", "&2,"));
 	}
 
 	public static String getLatestVersion(){
 		FileManager.downloadFile("http://dl.dropbox.com/u/19411765/Release/DefaultCommands/Version.txt", new File(V.plugin.getDataFolder(), "temp"));
 
 		String string = FileManager.readTextFile(new File(V.plugin.getDataFolder(), "temp")).get(0), latest = "";
 		List<Character> chars = new LinkedList<Character>();
 
 		new File(V.plugin.getDataFolder(), "temp").delete();
 		chars.add('.');
 		for(int i = 0; i <= 9; i++){
 			chars.add(Integer.toString(i).charAt(0));
 		}
 		for(int i = 0; i < string.length(); i++){
 			if(chars.contains(string.charAt(i))){
 				latest += string.charAt(i);
 			}
 		}
 		return latest;
 	}
 
 	public static boolean isRemoveFloat(World world){
 		for(DCTask dctask : V.removefloats){
 			if(dctask.getWorld().equals(world)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static void setBan(String player, String message){
 		while(getBan(player) != null){
 			V.bans.remove(getBan(player));
 		}
 		if(message != null){
 			V.bans.add(new DCBan(player, message));
 		}
 	}
 
 	public static DCBan getBan(String player){
 		for(DCBan dcban : V.bans){
 			if(dcban.getPlayer().equals(player)){
 				return dcban;
 			}
 		}
 		return null;
 	}
 
 	public static DCAfkPlayer getAfkPlayer(int id){
 		for(DCAfkPlayer dcafkplayer : V.afkplayers){
 			if(dcafkplayer.getId() == id){
 				return dcafkplayer;
 			}
 		}
 		return null;
 	}
 
 	public static void setAfkPlayer(int id, DCAfkPlayer dcafkplayer){
 		while(getAfkPlayer(id) != null){
 			V.afkplayers.remove(getAfkPlayer(id));
 		}
 		if(dcafkplayer != null){
 			V.afkplayers.add(dcafkplayer);
 		}
 	}
 
 	public static void giveKit(Player player, DCKit kit){
 		for(ItemStack itemstack : kit.getItems()){
 			player.getInventory().addItem(itemstack);
 		}
 	}
 
 	public static DCKit getKit(String name){
 		for(DCKit dckit : V.kits){
 			if(dckit.getName().equalsIgnoreCase(name)){
 				return dckit;
 			}
 		}
 		return null;
 	}
 
 	public static void setInventory(String name, ItemStack[] itemstack, String world){
 		if(!getConfig(world).getBoolean("separate_inventory")){
 			while(getInventory(name, world) != null){
 				V.inventories.remove(getInventory(name, world));
 			}
 			if(itemstack != null){
 				V.inventories.add(new DCInventoryPlayer(name, itemstack));
 			}
 		}else{
 			getConfig(world).setInventory(name, itemstack);
 		}
 	}
 
 	public static DCInventoryPlayer getInventory(String name, String world){
 		if(!getConfig(world).getBoolean("separate_inventory")){
 			for(DCInventoryPlayer dcinventory : V.inventories){
 				if(dcinventory.getName().equals(name)){
 					return dcinventory;
 				}
 			}
 		}else{
 			return getConfig(world).getInventory(name);
 		}
 		return null;
 	}
 
 	public static void setArmor(String name, ItemStack[] itemstack, String world){
 		if(!getConfig(world).getBoolean("separate_armor")){
 			while(getArmor(name, world) != null){
 				V.armors.remove(getArmor(name, world));
 			}
 			if(itemstack != null){
 				V.armors.add(new DCArmor(name, itemstack));
 			}
 		}else{
 			getConfig(world).setArmor(name, itemstack);
 		}
 	}
 
 	public static DCArmor getArmor(String name, String world){
 		if(!getConfig(world).getBoolean("separate_armor")){
 			for(DCArmor dcarmor : V.armors){
 				if(dcarmor.getName().equals(name)){
 					return dcarmor;
 				}
 			}
 		}else{
 			return getConfig(world).getArmor(name);
 		}
 		return null;
 	}
 
 	public static void playMessageSound(final Player player){
 		V.plugin.getServer().getScheduler().scheduleAsyncDelayedTask(
 				V.plugin,
 				new Runnable(){
 					public void run(){
 						playNote(player, Instrument.PIANO, 5);
 						Util.sleep(100);
 						playNote(player, Instrument.PIANO, 6);
 						Util.sleep(100);
 						playNote(player, Instrument.PIANO, 8);
 					}
 				}
 		);
 	}
 
 	public static void playNote(final Player player, final Instrument instrument, final int note){
 		final Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
 		final Byte data = block.getData();
 		final Material material = block.getType();
 
 		player.sendBlockChange(block.getLocation(), Material.NOTE_BLOCK, instrument.getType());
 		V.plugin.getServer().getScheduler().scheduleSyncDelayedTask(
 				V.plugin,
 				new Runnable(){
 					public void run(){
 						player.playNote(block.getLocation(), instrument.getType(), (byte) note);
 						player.sendBlockChange(block.getLocation(), material, data);
 					}
 				},
 				1
 		);
 	}
 
 	public static DCUndo getUndo(String name){
 		for(DCUndo dcundo : V.undos){
 			if(dcundo.getName().equals(name)){
 				return dcundo;
 			}
 		}
 		return null;
 	}
 
 	public static void setUndo(String name, List<Block> blocks){
 		for(int i = 0; i < V.undos.size(); i++){
 			if(V.undos.get(i).getName().equals(name)){
 				V.undos.remove(i);
 				i--;
 			}
 		}
 		if(blocks != null){
 			List<DCBlock> dcblocks = new LinkedList<DCBlock>();
 
 			for(Block block : blocks){
 				dcblocks.add(new DCBlock(block));
 			}
 			V.undos.add(new DCUndo(name, dcblocks));
 		}
 	}
 
 	public static void setSelection(String name, Block block1, Block block2){
 		for(int i = 0; i < V.selections.size(); i++){
 			if(V.selections.get(i).getName().equals(name)){
 				if(block1 != null){
 					V.selections.get(i).setBlock1(block1);
 				}
 				if(block2 != null){
 					V.selections.get(i).setBlock2(block2);
 				}
 				if(block1 == null && block2 == null){
 					V.selections.remove(i);
 				}
 				return;
 			}
 		}
 		V.selections.add(new DCSelection(name, block1, block2));
 	}
 
 	public static DCSelection getSelection(String name){
 		for(DCSelection dcs : V.selections){
 			if(dcs.getName().equals(name) && dcs.getBlock1() != null && dcs.getBlock2() != null && dcs.getBlock1().getWorld() .equals(dcs.getBlock2().getWorld())){
 				return dcs;
 			}
 		}
 		return null;
 	}
 
 	public static DCPortal getPortal(Location location){
 		int x = location.getBlock().getX(), y = location.getBlock().getY(), z = location.getBlock().getZ();
 
 		for(DCPortal dcp : V.portals){
 			if(dcp.getWorld().equals(location.getWorld().getName())){
 				int x1, x2, y1, y2, z1, z2;
 
 				if(dcp.getBlock1().getX() < dcp.getBlock2().getX()){
 					x1 = dcp.getBlock1().getX();
 					x2 = dcp.getBlock2().getX();
 				}else{
 					x2 = dcp.getBlock1().getX();
 					x1 = dcp.getBlock2().getX();
 				}
 				if(dcp.getBlock1().getY() - 1 < dcp.getBlock2().getY() - 1){
 					y1 = dcp.getBlock1().getY() - 2;
 					y2 = dcp.getBlock2().getY() - 2;
 				}else{
 					y2 = dcp.getBlock1().getY();
 					y1 = dcp.getBlock2().getY();
 				}
 				if(dcp.getBlock1().getZ() < dcp.getBlock2().getZ()){
 					z1 = dcp.getBlock1().getZ();
 					z2 = dcp.getBlock2().getZ();
 				}else{
 					z2 = dcp.getBlock1().getZ();
 					z1 = dcp.getBlock2().getZ();
 				}
 				for(int i1 = x1; i1 <= x2; i1++){
 					for(int i2 = y1; i2 <= y2; i2++){
 						for(int i3 = z1; i3 <= z2; i3++){
 							if(i1 == x && i2 == y && i3 == z){
 								return dcp;
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public static DCPortal getPortal(String name){
 		for(DCPortal dcportal : V.portals){
 			if(dcportal.getName().equals(name)){
 				return dcportal;
 			}
 		}
 		return null;
 	}
 
 	public static void setPortal(String name, Block block1, Block block2, String target){
 		while(getPortal(name) != null){
 			V.portals.remove(getPortal(name));
 		}
 		if(block1 != null && block2 != null && target != null){
 			V.portals.add(new DCPortal(name, block1, block2, target));
 		}
 	}
 
 	public static void cancelRemovefloat(double id){
 		for(DCTask dctask : V.removefloats){
 			if(dctask.getId() == id){
 				V.plugin.getServer().getScheduler().cancelTask(dctask.getTask());
 				if(V.plugin.getServer().getPlayer(dctask.getPlayer()) != null){
 
 					V.plugin.getServer().getPlayer(dctask.getPlayer()).sendMessage("Removefloat finished.");
 				}
 				V.removefloats.remove(dctask);
 				return;
 			}
 		}
 	}
 
 	public static void broadcastJoin(Player player, String world, boolean hide){
 		for(Player p : V.plugin.getServer().getOnlinePlayers()){
 			if(!player.equals(p) && (!isHide(player.getName()) || Perm.hasPermissionSilent(p, "dc.hide.see"))){
 				sendMessage(p,
 						getColoredString("join_world")
 						.replace("<player>", player.getDisplayName())
 						.replace("<world>", world)
 				);
 			}
 		}
 		if(!hide){
 			L.log(getColoredString("join_world")
 					.replace("<player>", player.getDisplayName())
 					.replace("<world>", world));
 		}
 	}
 
 	public static void broadcastDisconnect(Player player, boolean hide){
 		for(Player p : V.plugin.getServer().getOnlinePlayers()){
 			if(!player.equals(p) && (!isHide(player.getName()) || Perm.hasPermissionSilent(p, "dc.hide.see"))){
 				sendMessage(
 						p,
 						Util.replaceColor(
 								getString("disconnect_message")
 								.replace("<player>", player.getDisplayName())
 								.replace("<world>", player.getWorld().getName())
 								.replace("<ip>", Util.getIp(player))
 						)
 				);
 			}
 		}
 		if(!hide){
 			L.log(Util.replaceColor(
 					getString("disconnect_message")
 					.replace("<player>", player.getDisplayName())
 					.replace("<world>", player.getWorld().getName())
 					.replace("<ip>", player.getAddress().toString().substring(1).split(":")[0])
 			));
 			getConfig(player).setInventory(player.getName(), player.getInventory().getContents());
 			setPlayer(new DCPlayer(player.getName(), Util.getIp(player), Util.getDateTime()));
 			removePlayerFromLists(player);
 		}
 	}
 
 	public static void broadcastConnect(Player player, boolean hide){
 		String message = "connect_message";
 
 
 		if(isFirstTimer(player.getName())){
 			player.teleport(getConfig(player).getSpawn());
 			message = "connect_message_first_time";
 		}
 		for(Player p : V.plugin.getServer().getOnlinePlayers()){
 			if(!player.equals(p) && (!isHide(player.getName()) || Perm.hasPermission(p, "dc.hide.see"))){
 				sendMessage(
 						p,
 						Util.replaceColor(
 								getColoredString(message)
 								.replace("<player>", player.getDisplayName())
 								.replace("<world>", player.getWorld().getName())
 								.replace("<ip>", Util.getIp(player))
 						)
 				);
 			}
 		}
 		if(!hide){
 			if(isHide(player.getName())){
 				player.sendMessage("Note: You are still hidden.");
 			}
 			L.log(getColoredString(message)
 					.replace("<player>", player.getDisplayName())
 					.replace("<world>", player.getWorld().getName())
 					.replace("<ip>", Util.getIp(player))
 			);
 			if(getConfig(player).getInventory(player.getName()) != null){
 				player.getInventory().setContents(Util.convertItemStack(getConfig(player).getInventory(player.getName()).getContents()));
 			}
 			player.setDisplayName(Util.replaceColor(player.getName()));
 			setPlayer(new DCPlayer(player.getName(), Util.getIp(player), Util.getDateTime()));
 			updateHide();
 		}
 	}
 
 	public static ItemStack[] getNiceItemStack(ItemStack[] itemstack){
 		ItemStack[] is = new ItemStack[36];
 
 		for(int i = 27; i < 36; i++){
 			is[i] = itemstack[i - 27];
 		}
 		for(int i = 0; i < 27; i++){
 			is[i] = itemstack[i + 9];
 		}
 		return is;
 	}
 
 	public static ItemStack[] getItemStackFromString(String string){
 		ItemStack[] itemstack = new ItemStack[string.split(";").length];
 
 		for(int i = 0; i < string.split(";").length; i++){
 			if(Integer.parseInt(string.split(";")[i].split(":")[0]) != 0){
 				itemstack[i] = new ItemStack(Integer.parseInt(string.split(";")[i].split(":")[0]), Integer.parseInt(string.split(";")[i].split(":")[2]), Short.parseShort(string.split(";")[i].split(":")[1]));
 			}else{
 				itemstack[i] = null;
 			}
 		}
 		return itemstack;
 	}
 
 	public static String getStringFromItemStack(ItemStack[] itemstack){
 		String string = "";
 
 		for(ItemStack is : itemstack){
 			if(is != null){
 				string += is.getTypeId() + ":" + is.getDurability() + ":" + is.getAmount() + ";";
 			}else{
 				string += "0:0:0:0;";
 			}
 		}
 		return string;
 	}
 
 	public static List<World> getNotSeparateInventoryWorlds(){
 		List<World> worlds = new LinkedList<World>();
 
 		for(DCWorld dcworld : V.worlds){
 			if(!dcworld.getBoolean("separate_inventory")){
 				worlds.add(V.plugin.getServer().getWorld(dcworld.getName()));
 			}
 		}
 		return worlds;
 	}
 
 	public static void updateHide(){
 		for(Player player : V.plugin.getServer().getOnlinePlayers()){
 			if(isHide(player.getName())){
 				hidePlayerFromAll(player);
 			}
 		}
 	}
 
 	public static void hidePlayerFromAll(Player hide){
 		for(Player player : hide.getServer().getOnlinePlayers()){
 			hidePlayer(hide, player);
 		}
 	}
 
 	public static void appearPlayerFromAll(Player appear){
 		for(Player player : appear.getServer().getOnlinePlayers()){
 			appearPlayer(appear, player);
 		}
 	}
 
 	public static void hidePlayer(Player hide, Player player){
 		if(hide != player && !Perm.hasPermissionSilent(player, "dc.hide.see")){
 			((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(hide.getEntityId()));
 		}
 	}
 
 	public static void appearPlayer(Player appear, Player player){
 		if(appear != player){
 			((CraftPlayer) player).getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(((org.bukkit.craftbukkit.entity.CraftPlayer) appear).getHandle()));
 		}
 	}
 
 	public static boolean isRideAble(Entity entity){
 		if(!(entity instanceof Pig) && !(entity instanceof Giant)){
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isAnimal(Entity entity){
 		if(
 				entity instanceof Chicken
 				|| entity instanceof Cow
 				|| entity instanceof Pig
 				|| entity instanceof Sheep
 				|| entity instanceof Squid
 				|| entity instanceof Wolf
 				|| entity instanceof Spider && ((Spider) entity).getTarget() == null
 		){
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isMonster(Entity entity){
 		if(
 				entity instanceof Creeper
 				|| entity instanceof Ghast
 				|| entity instanceof Monster
 				|| entity instanceof PigZombie
 				|| entity instanceof Skeleton
 				|| entity instanceof Slime
 				|| entity instanceof Spider && ((Spider) entity).getTarget() != null
 				|| entity instanceof Zombie
 				|| entity instanceof Giant
 		){
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isFirstTimer(String player){
 		for(World world : V.plugin.getServer().getWorlds()){
 			if(new File(world.getName() + File.separator + "players", player + ".dat").exists()){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static void setTime(String world, long time){
 		V.plugin.getServer().getWorld(world).setTime(time);
 		if(getConfig(world).getBoolean("sync_time")){
 			for(DCWorld dcworld : V.worlds){
 				if(!dcworld.equals(getConfig(world)) && dcworld.getBoolean("sync_time")){
 					V.plugin.getServer().getWorld(dcworld.getName()).setTime(time);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public static List<Player> getPlayers(Player p, String string){
 		List<Player> players = new LinkedList<Player>();
 
 		for(String s : string.split(",")){
 			if(s.equals(V.all)){
 				for(Player player : V.plugin.getServer().getOnlinePlayers()){
 					for(int i = 0; i < players.size(); i++){
 						if(players.get(i).equals(player)){
 							players.remove(i);
 							i--;
 						}
 					}
 					if(p == null || p.equals(player) || !isHide(player.getName()) || isHide(player.getName()) && Perm.hasPermissionSilent(p, "dc.hide.see")){
 						players.add(player);
 					}
 				}
 			}else if(s.startsWith("w:") && V.plugin.getServer().getWorld(s.substring(2)) != null){
 				for(Player player : V.plugin.getServer().getWorld(s.substring(2)).getPlayers()){
 					for(int i = 0; i < players.size(); i++){
 						if(players.get(i).equals(player)){
 							players.remove(i);
 							i--;
 						}
 					}
 					if(p == null || p.equals(player) || !isHide(player.getName()) || isHide(player.getName()) && Perm.hasPermissionSilent(p, "dc.hide.see")){
 						players.add(player);
 					}
 				}
 			}else if(s.startsWith("g:")){
 				for(World world : V.plugin.getServer().getWorlds()){
 					if(Perm.PermissionsHandler != null && Perm.PermissionsHandler.getGroup(world.getName(), s.substring(2)) != null){
 						for(Player player : world.getPlayers()){
 							if(Perm.PermissionsHandler.inGroup(world.getName(), player.getName(), s.substring(2))){
 								for(int i = 0; i < players.size(); i++){
 									if(players.get(i).equals(player)){
 										players.remove(i);
 										i--;
 									}
 								}
 								if(p == null || p.equals(player) || !isHide(player.getName()) || isHide(player.getName()) && Perm.hasPermissionSilent(p, "dc.hide.see")){
 									players.add(player);
 								}
 							}
 						}
 					}
 				}
 			}else if(V.plugin.getServer().getPlayer(s) != null){
 				for(int i = 0; i < players.size(); i++){
 					if(players.get(i).equals(V.plugin.getServer().getPlayer(s))){
 						players.remove(i);
 						i--;
 					}
 				}
 				if(p == null || p.equals(V.plugin.getServer().getPlayer(s)) || !isHide(V.plugin.getServer().getPlayer(s).getName()) || isHide(V.plugin.getServer().getPlayer(s).getName()) && Perm.hasPermissionSilent(p, "dc.hide.see")){
 					players.add(V.plugin.getServer().getPlayer(s));
 				}
 			}
 		}
 		return players;
 	}
 
 	public static List<Player> getPlayers(CommandSender sender, String string){
 		if(sender instanceof Player){
 			return getPlayers((Player) sender, string);
 		}
 		return getPlayers(null, string);
 	}
 
 	public static List<String> getPlayerList(Player player){
 		List<String> names = new LinkedList<String>();
 
 		for(Player p : getPlayers(player, V.all)){
 			if(isAfk(p.getEntityId())){
 				names.add("&8" + p.getName());
 			}else{
 				names.add("&7" + p.getName());
 			}
 		}
 		return names;
 	}
 
 	public static List<String> getPlayerList(CommandSender sender){
 		if(sender instanceof Player){
 			return getPlayerList((Player) sender);
 		}
 		return getPlayerList(null);
 	}
 
 	public static List<Material> getItems(String string){
 		List<Material> materials = new LinkedList<Material>();
 
 		for(String s : string.split(" ")){
 			if(s.equals(V.all)){
 				materials.clear();
 				for(Material m : Material.values()){
 					materials.add(m);
 				}
 				return materials;
 			}
 			if(Material.matchMaterial(s) != null){
 				materials.add(Material.matchMaterial(s));
 			}
 		}
 		return materials;
 	}
 
 	public static String getSenderName(CommandSender sender){
 		if(sender instanceof Player){
 			return ((Player) sender).getName();
 		}
 		return V.console_name;
 	}
 
 	public static String getPlayerNames(List<Player> players){
 		if(players.size() == 0){
 			return "";
 		}
 		if(players.size() > 1){
 			return players.size() + " players";
 		}
 		return players.get(0).getName();
 	}
 
 	public static String getSenderCmdMsg(String id, CommandSender sender, List<Player> players, boolean enabled){
 		String s = "disable";
 
 		if(enabled){
 			s = "enable";
 		}
 		return getColoredString(id).replace("<sender>", getSenderName(sender)).replace("<player>", getPlayerNames(players)).replace("<players>", getPlayerNames(players)).replace("<status>", s);
 	}
 
 	public static String getSenderCmdMsg(String id, CommandSender sender, List<Player> players){
 		return getColoredString(id).replace("<sender>", getSenderName(sender)).replace("<player>", getPlayerNames(players)).replace("<players>", getPlayerNames(players)).replace("<status>", "toggle");
 	}
 
 	public static String getSenderCmdMsg(String id, CommandSender sender, List<Player> players, List<ItemStack> itemstacks){
 		int amount = 0;
 
 		for(ItemStack itemstack : itemstacks){
 			amount += itemstack.getAmount();
 		}
 		return getSenderCmdMsg(id, sender, players).replace("<item>", Util.getItemName(itemstacks.get(0).getType())).replace("<amount>", String.valueOf(amount));
 	}
 
 	public static List<ItemStack> getItemStacks(String string, String amount){
 		if(string == null || amount == null){
 			return new LinkedList<ItemStack>();
 		}
 		if(!Util.isValidInt(amount)){
 			return new LinkedList<ItemStack>();
 		}
 
 		String material = getItem(string).split(":")[0], data = "0";
 		List<ItemStack> itemstacks = new LinkedList<ItemStack>();
 		int a = Integer.parseInt(amount);
 
 		if(getItem(string).split(":").length < 3){
 			if(getItem(string).split(":").length == 2){
 				data = getItem(string).split(":")[1];
 			}
 			if(Material.matchMaterial(material) != null && Material.matchMaterial(material) != Material.AIR && Util.isValidInt(data) && Integer.parseInt(data) > -1 && Util.isValidInt(amount) && Integer.parseInt(amount) > 0){
 				while(a > 0){
 					if(a - 64 > 0){
 						itemstacks.add(new ItemStack(Material.matchMaterial(material), 64, (short) 0, Byte.parseByte(data)));
 						a -= 64;
 					}else{
 						itemstacks.add(new ItemStack(Material.matchMaterial(material), a, (short) 0, Byte.parseByte(data)));
 						a -= a;
 					}
 				}
 			}
 		}
 		return itemstacks;
 	}
 
 	public static String getGive(String[] args, String get){
 		if(V.give.split(" ").length == args.length){
 			for(int i = 0; i < args.length; i++){
 				if(V.give.split(" ")[i].equals(get)){
 					return args[i];
 				}
 			}
 		}
 		return null;
 	}
 
 	public static void broadcastMessage(String message){
 		for(Player player : V.plugin.getServer().getOnlinePlayers()){
 			sendMessage(player, message);
 		}
 	}
 
 	public static void sendMessage(Player player, String message){
 		if(Util.removeColor(message).length() > 0){
 			for(String string : message.split("<br>")){
 				player.sendMessage(Util.replaceColor(string));
 			}
 		}
 	}
 
 	public static void sendMessage(CommandSender sender, String message){
 		if(Util.removeColor(message).length() > 0){
 			for(String string : message.split("<br>")){
 				sender.sendMessage(Util.replaceColor(string));
 			}
 		}
 	}
 
 	public static void sendString(Player player, String id){
 		for(String string : getColoredString(id).split("<br>")){
 			player.sendMessage(string);
 		}
 	}
 
 	public static void sendString(CommandSender sender, String id){
 		for(String string : getColoredString(id).split("<br>")){
 			sender.sendMessage(string);
 		}
 	}
 
 	public static void addWorld(String world, Environment environment){
 		if(getConfig(world) == null){
 			delWorld(world);
 			V.worlds.add(new DCWorld(world, environment, 0));
 			V.plugin.getServer().createWorld(world, environment);
 			getConfig(world).setSeed(V.plugin.getServer().getWorld(world).getSeed());
 			setConfig(world, Reset.resetDCConfiguration(world));
 		}
 	}
 
 	public static void addWorld(String world, Environment environment, long seed){
 		if(getConfig(world) == null){
 			delWorld(world);
 			V.worlds.add(new DCWorld(world, environment, seed));
 			V.plugin.getServer().createWorld(world, environment, seed);
 			setConfig(world, Reset.resetDCConfiguration(world));
 		}
 	}
 
 	public static void delWorld(String world){
 		if(getConfig(world) != null){
 			while(V.worlds.contains(getConfig(world))){
 				V.worlds.remove(getConfig(world));
 			}
 		}
 	}
 
 	public static void removeWorld(String world, boolean save){
 		if(V.plugin.getServer().getWorld(world) != null){
 			for(Player player : V.plugin.getServer().getWorld(world).getPlayers()){
 				player.kickPlayer("The world you were in was removed!");
 			}
 			delWorld(world);
 			V.plugin.getServer().unloadWorld(V.plugin.getServer().getWorld(world), true);
 			FileManager.deleteDirectory(new File(V.plugin.getDataFolder(), "Worlds" + File.separator + world));
 			if(!save){
 				FileManager.deleteDirectory(new File(world));
 			}
 		}
 	}
 
 	public static void setConfig(String world, List<DCConfiguration> configuration){
 		for(int i = 0; i < V.worlds.size(); i++){
 			if(V.worlds.get(i).getName().equals(world)){
 				V.worlds.get(i).setConfiguration(configuration);
 			}
 		}
 	}
 
 	public static void setConfig(String world, String configuration, Object value){
 		for(DCWorld dcworld : V.worlds){
 			if(dcworld.getName().equals(world)){
 				for(int i = 0; i < dcworld.getConfiguration().size(); i++){
 					if(dcworld.getConfiguration().get(i).getConfiguration().equals(configuration)){
 						dcworld.getConfiguration().get(i).setValue(value);
 						return;
 					}
 				}
 			}
 		}
 	}
 
 	public static DCWorld getConfig(String world){
 		for(DCWorld dcworld : V.worlds){
 			if(dcworld.getName().equals(world)){
 				return dcworld;
 			}
 		}
 		return null;
 	}
 
 	public static DCWorld getConfig(World world){
 		return getConfig(world.getName());
 	}
 
 	public static DCWorld getConfig(Entity entity){
 		return getConfig(entity.getWorld().getName());
 	}
 
 	public static DCWorld getConfig(Player player){
 		return getConfig(player.getWorld().getName());
 	}
 
 	public static DCWorld getConfig(Block block){
 		return getConfig(block.getWorld().getName());
 	}
 
 	public static String getCommand(String alias){
 		for(DCCommand dccommand : V.commands){
 			for(String dcalias : dccommand.getAlias()){
 				if(dcalias.equalsIgnoreCase(alias)){
 					return dccommand.getCommand();
 				}
 			}
 		}
 		return null;
 	}
 
 	public static List<String> getAlias(String command){
 		for(DCCommand dccommand : V.commands){
 			if(dccommand.getCommand().equalsIgnoreCase(command)){
 				return dccommand.getAlias();
 			}
 		}
 		return null;
 	}
 
 	public static void setAlias(String command, List<String> alias){
 		for(DCCommand dccommand : V.commands){
 			if(dccommand.getCommand().equals(command)){
 				dccommand.setAlias(alias);
 				return;
 			}
 		}
 		V.commands.add(new DCCommand(command, alias));
 	}
 
 	public static void addAlias(String command, String alias){
 		List<String> old = new LinkedList<String>();
 		for(int i = 0; i < V.commands.size(); i++){
 			if(V.commands.get(i).getCommand().equals(command)){
 				old = V.commands.get(i).getAlias();
 				for(String a : old){
 					if(a.equalsIgnoreCase(alias)){
 						return;
 					}
 				}
 				old.add(alias);
 				V.commands.get(i).setAlias(old);
 				return;
 			}
 		}
 		old.add(alias);
 		V.commands.add(new DCCommand(command, old));
 	}
 
 	public static void setItemAlias(String id, List<String> alias){
 		for(int i = 0; i < V.items.size(); i++){
 			if(V.items.get(i).getId().equals(id)){
 				V.items.get(i).setAlias(alias);
 				return;
 			}
 		}
 		V.items.add(new DCItem(id, alias));
 	}
 
 	public static String getItem(String string){
 		for(DCItem item : V.items){
 			for(String alias : item.getAlias()){
 				if(string.equalsIgnoreCase(alias)){
 					return item.getId();
 				}
 			}
 		}
 		return string;
 	}
 
 	public static void addItemAlias(String id, String alias){
 		List<String> na = new LinkedList<String>();
 
 		for(DCItem item : V.items){
 			if(item.getId().equals(id)){
 				for(String a : item.getAlias()){
 					if(alias.equalsIgnoreCase(a)){
 						return;
 					}
 				}
 				item.getAlias().add(alias);
 				return;
 			}
 		}
 		na.add(alias);
 		V.items.add(new DCItem(id, na));
 	}
 
 	public static void setString(String id, String value){
 		for(int i = 0; i < V.strings.size(); i++){
 			if(V.strings.get(i).getId().equals(id)){
 				V.strings.get(i).setValue(value);
 			}
 		}
 	}
 
 	public static String getString(String id){
 		for(int i = 0; i < V.strings.size(); i++){
 			if(V.strings.get(i).getId().equals(id)){
 				return V.strings.get(i).getValue();
 			}
 		}
 		return "";
 	}
 
 	public static String getColoredString(String id){
 		return Util.replaceColor(getString(id));
 	}
 
 	public static void setLastCommand(String player, String command){
 		for(int i = 0; i < V.last_commands.size(); i++){
 			if(V.last_commands.get(i).getId().equals(player)){
 				V.last_commands.remove(i);
 				i--;
 			}
 		}
 		if(command != null){
 			V.last_commands.add(new DCString(player, command));
 		}
 	}
 
 	public static String getLastCommand(String player){
 		for(int i = 0; i < V.last_commands.size(); i++){
 			if(V.last_commands.get(i).getId().equals(player)){
 				return V.last_commands.get(i).getValue();
 			}
 		}
 		return null;
 	}
 
 	public static void setName(String name, String displayname){
 		for(int i = 0; i < V.names.size(); i++){
 			if(V.names.get(i).getId().equals(name)){
 				V.names.remove(i);
 				i--;
 			}
 		}
 		if(displayname != null && !name.equals(displayname)){
 			V.names.add(new DCString(name, displayname));
 		}
 		if(V.plugin.getServer().getPlayer(name) != null){
 			V.plugin.getServer().getPlayer(name).setDisplayName(getName(name));
 		}
 	}
 
 	public static String getName(String name){
 		for(int i = 0; i < V.names.size(); i++){
 			if(V.names.get(i).getId().equals(name)){
 				return Util.replaceColor(V.names.get(i).getValue());
 			}
 		}
 		return name;
 	}
 
 	public static void setPlayer(DCPlayer dcplayer){
 		while(getPlayer(dcplayer.getName()) != null){
 			V.players.remove(getPlayer(dcplayer.getName()));
 		}
 		V.players.add(dcplayer);
 	}
 
 	public static DCPlayer getPlayer(String name){
 		for(DCPlayer dcplayer : V.players){
 			if(dcplayer.getName().equals(name)){
 				return dcplayer;
 			}
 		}
 		for(DCPlayer dcplayer : V.players){
 			if(dcplayer.getName().startsWith(name)){
 				return dcplayer;
 			}
 		}
 		return null;
 	}
 
 	public static DCHome getHome(String name){
 		for(DCHome dchome : V.homes){
 			if(dchome.getName().equals(name)){
 				return dchome;
 			}
 		}
 		return null;
 	}
 
 	public static DCHome getHome(Player player){
 		return getHome(player.getName());
 	}
 
 	public static void setHome(String name, Location location){
 		while(getHome(name) != null){
 			V.homes.remove(getHome(name));
 		}
 		if(location != null){
 			V.homes.add(new DCHome(name, location));
 		}
 	}
 
 	public static DCWarp getWarp(String name){
 		for(DCWarp dcwarp : V.warps){
 			if(dcwarp.getName().equals(name)){
 				return dcwarp;
 			}
 		}
 		return null;
 	}
 
 	public static void setWarp(String name, Location location){
 		while(getWarp(name) != null){
 			V.warps.remove(getWarp(name));
 		}
 		if(location != null){
 			V.warps.add(new DCWarp(name, location));
 		}
 	}
 
 	public static Player getPlayer(int id){
 		for(Player player : V.plugin.getServer().getOnlinePlayers()){
 			if(player.getEntityId() == id){
 				return player;
 			}
 		}
 		return null;
 	}
 
 	public static void setReply(String sender, String reciever){
 		for(int i = 0; i < V.replies.size(); i++){
 			if(V.replies.get(i).getReciever().equals(reciever)){
 				V.replies.remove(i);
 				i--;
 			}
 		}
 		if(reciever != null){
 			V.replies.add(new DCReply(sender, reciever));
 		}
 	}
 
 	public static String getReply(String reciever){
 		for(int i = 0; i < V.replies.size(); i++){
 			if(V.replies.get(i).getReciever().equals(reciever)){
 				return V.replies.get(i).getSender();
 			}
 		}
 		return null;
 	}
 
 	public static void setGod(int id, boolean enabled){
 		V.god.remove((Object) id);
 		if(enabled){
 			V.god.add(id);
 		}
 	}
 
 	public static boolean isGod(int id){
 		return V.god.contains(id);
 	}
 
 	public static void setFly(int id, boolean enabled){
 		V.fly.remove((Object) id);
 		V.hovers.remove(getHover(id));
 		if(enabled){
 			V.fly.add(id);
 			V.hovers.add(new DCHover(id));
 		}
 	}
 
 	public static boolean isFly(int id){
 		return V.fly.contains(id);
 	}
 
 	public static void setCreative(int id, boolean enabled){
 		V.creative.remove((Object) id);
 		if(enabled){
 			V.creative.add(id);
 		}
 	}
 
 	public static boolean isCreative(int id){
 		return V.creative.contains(id);
 	}
 
 	public static void setIgnoremob(int id, boolean enabled){
 		V.ignoremob.remove((Object) id);
 		if(enabled){
 			V.ignoremob.add(id);
 		}
 	}
 
 	public static boolean isIgnoremob(int id){
 		return V.ignoremob.contains(id);
 	}
 
 	public static void setNopickup(int id, boolean enabled){
 		V.nopickup.remove((Object) id);
 		if(enabled){
 			V.nopickup.add(id);
 		}
 	}
 
 	public static boolean isNopickup(int id){
 		return V.nopickup.contains(id);
 	}
 
 	public static void setInstakill(int id, boolean enabled){
 		V.instakill.remove((Object) id);
 		if(enabled){
 			V.instakill.add(id);
 		}
 	}
 
 	public static boolean isInstakill(int id){
 		return V.instakill.contains(id);
 	}
 
 	public static void setAfk(int id, boolean enabled){
 		V.afk.remove((Object) id);
 		if(enabled){
 			V.afk.add(id);
 		}
 	}
 
 	public static boolean isAfk(int id){
 		return V.afk.contains(id);
 	}
 
 	public static void setFreeze(String name, boolean enabled){
 		V.freeze.remove(name);
 		if(enabled){
 			V.freeze.add(name);
 		}
 	}
 
 	public static boolean isFreeze(String name){
 		return V.freeze.contains(name);
 	}
 
 	public static void setMute(String name, boolean enabled){
 		V.mute.remove(name);
 		if(enabled){
 			V.mute.add(name);
 		}
 	}
 
 	public static boolean isMute(String name){
 		return V.mute.contains(name);
 	}

 	public static void setHide(String name, boolean enabled){
 		if(!enabled){
 			if(V.plugin.getServer().getPlayer(name) != null){
 				appearPlayerFromAll(V.plugin.getServer().getPlayer(name));
 			}
 			V.hide.remove(name);
 		}else{
 			if(V.plugin.getServer().getPlayer(name) != null){
 				hidePlayerFromAll(V.plugin.getServer().getPlayer(name));
 			}
 			V.hide.add(name);
 		}
 	}
 
 	public static boolean isHide(String name){
 		return V.hide.contains(name);
 	}
 
 	public static void setWhitelist(String name, boolean enabled){
 		V.whitelist_.remove(name);
 		if(enabled){
 			V.whitelist_.add(name);
 		}
 	}
 
 	public static boolean isWhitelist(String name){
 		return V.whitelist_.contains(name);
 	}
 
 	public static void setHover(int id, Block block){
 		if(getHover(id) != null){
 			getHover(id).setBlock(block);
 		}
 	}
 
 	public static DCHover getHover(int id){
 		for(DCHover dchover : V.hovers){
 			if(dchover.getId() == id){
 				return dchover;
 			}
 		}
 		return null;
 	}
 }
