 package com.runetooncraft.plugins.EasyMobArmory;
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 
 import net.minecraft.server.v1_6_R2.Item;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import net.minecraft.server.v1_6_R2.TileEntityChest;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventory;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
 import org.bukkit.entity.Chicken;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Spider;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.DoubleChestInventory;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 
 import com.bergerkiller.bukkit.common.entity.CommonEntity;
 import com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler.SpawnerCache;
 import com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler.SpawnerHandler;
 import com.runetooncraft.plugins.EasyMobArmory.core.Config;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.EggHandler;
 
 
 public class EMAListener implements Listener {
 	Config config;
 	public static HashMap<Player, Entity> PlayerMobDataMap = new HashMap<Player, Entity>();
 	public static HashMap<Player, Boolean> Armoryenabled = new HashMap<Player, Boolean>();
 	public static HashMap<Player, SpawnerCache> SpawnerSelected = new HashMap<Player, SpawnerCache>();
 	public EMAListener(Config config) {
 		this.config = config;
 	}
 	@EventHandler
 	public void OnPlayerEntityInteract(PlayerInteractEntityEvent event) {
 		Entity e = event.getRightClicked();
 		Player p = event.getPlayer();
 	if(Armoryenabled.get(p) != null){ if(Armoryenabled.get(p)) {
 		if(e.getType().equals(EntityType.ZOMBIE)) {
 			ItemStack i = p.getItemInHand();
 			Zombie z = (Zombie) e;
 			if(EMA.Helmets.contains(i)) {
 				z.getEquipment().setHelmet(i);
 			}else if(EMA.Chestplates.contains(i)) {
 				z.getEquipment().setChestplate(i);
 			}else if(EMA.Leggings.contains(i)) {
 				z.getEquipment().setLeggings(i);
 			}else if(EMA.Boots.contains(i)) {
 				z.getEquipment().setBoots(i);
 			}else if(i.getType().equals(Material.BONE)){
 				Inventory inv = Bukkit.createInventory(p, 9, "zombieinv");
 				ItemStack[] zombieinv = z.getEquipment().getArmorContents();
 				inv.setContents(zombieinv);
 				inv.setItem(4, z.getEquipment().getItemInHand());
 				if(z.isBaby()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, z);
 			}else{
 				z.getEquipment().setItemInHand(i);
 			}
 		}else if(e.getType().equals(EntityType.SKELETON)) {
 			ItemStack i = p.getItemInHand();
 			Skeleton s = (Skeleton) e;
 			if(EMA.Helmets.contains(i)) {
 				s.getEquipment().setHelmet(i);
 			}else if(EMA.Chestplates.contains(i)) {
 				s.getEquipment().setChestplate(i);
 			}else if(EMA.Leggings.contains(i)) {
 				s.getEquipment().setLeggings(i);
 			}else if(EMA.Boots.contains(i)) {
 				s.getEquipment().setBoots(i);
 			}else if(i.getType().equals(Material.BONE)){
 				Inventory inv = Bukkit.createInventory(p, 9, "skeletoninv");
 				ItemStack[] skeletoninv = s.getEquipment().getArmorContents();
 				inv.setContents(skeletoninv);
 				inv.setItem(4, s.getEquipment().getItemInHand());
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, s);
 			}else{
 				s.getEquipment().setItemInHand(i);
 			}
 		}else if(e.getType().equals(EntityType.PIG_ZOMBIE)) {
 			ItemStack i = p.getItemInHand();
 			PigZombie pz = (PigZombie) e;
 			if(EMA.Helmets.contains(i)) {
 				pz.getEquipment().setHelmet(i);
 			}else if(EMA.Chestplates.contains(i)) {
 				pz.getEquipment().setChestplate(i);
 			}else if(EMA.Leggings.contains(i)) {
 				pz.getEquipment().setLeggings(i);
 			}else if(EMA.Boots.contains(i)) {
 				pz.getEquipment().setBoots(i);
 			}else if(i.getType().equals(Material.BONE)){
 				Inventory inv = Bukkit.createInventory(p, 9, "pigzombieinv");
 				ItemStack[] pigzombieinv = pz.getEquipment().getArmorContents();
 				inv.setContents(pigzombieinv);
 				inv.setItem(4, pz.getEquipment().getItemInHand());
 				if(pz.isBaby()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, pz);
 			}else{
 				pz.getEquipment().setItemInHand(i);
 			}
 		}else if(e.getType().equals(EntityType.SHEEP)) {
 			ItemStack i = p.getItemInHand();
 			Sheep sh = (Sheep) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "sheepinv");
 				if(!sh.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(sh.isSheared()) inv.setItem(6, new ItemStack(Material.SHEARS));
 				if(sh.getAgeLock()) inv.setItem(7, new ItemStack(Material.GLOWSTONE_DUST));
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, sh);
 			}
 		}else if(e.getType().equals(EntityType.PIG)) {
 			ItemStack i = p.getItemInHand();
 			Pig pig = (Pig) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "piginv");
 				if(!pig.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(pig.hasSaddle()) inv.setItem(6, new ItemStack(Material.SADDLE));
 				if(pig.getAgeLock()) inv.setItem(7, new ItemStack(Material.GLOWSTONE_DUST));
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, pig);
 			}
 		}else if(e.getType().equals(EntityType.CHICKEN)) {
 			ItemStack i = p.getItemInHand();
 			Chicken c = (Chicken) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "chickeninv");
 				if(!c.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(c.getAgeLock()) inv.setItem(7, new ItemStack(Material.GLOWSTONE_DUST));
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, c);
 			}
 		}else if(e.getType().equals(EntityType.COW)) {
 			ItemStack i = p.getItemInHand();
 			Cow cow = (Cow) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "cowinv");
 				if(!cow.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(cow.getAgeLock()) inv.setItem(7, new ItemStack(Material.GLOWSTONE_DUST));
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, cow);
 			}
 		}else if(e.getType().equals(EntityType.CREEPER)) {
 			ItemStack i = p.getItemInHand();
 			Creeper c = (Creeper) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "creeperinv");
 				if(c.isPowered()) inv.setItem(0, new ItemStack(Material.REDSTONE));
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, c);
 			}
 		}else if(e.getType().equals(EntityType.HORSE)) {
 			ItemStack i = p.getItemInHand();
 			Horse h = (Horse) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "horseinv");
 				if(!h.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				if(h.isTamed()) inv.setItem(6, new ItemStack(Material.HAY_BLOCK));
 				if(h.isTamed()) {
 					Player owner = (Player) h.getOwner();
 					inv.setItem(7, setOwner(new ItemStack(Material.SKULL_ITEM, 1, (short)3), p.getName()));
 				}
 				if(h.isCarryingChest()) inv.setItem(7, new ItemStack(Material.CHEST));
 //				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg",ChatColor.AQUA + e.getType().getName()));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, h);
 			}
 		}
 		}}
 	}
 	@EventHandler
 	public void OnInventoryCloseEvent(InventoryCloseEvent event) {
 	if(Armoryenabled.get(event.getPlayer()) != null){  if(Armoryenabled.get(event.getPlayer())) {
 		if(event.getInventory().getName().equals("zombieinv")) {
 			Inventory i = event.getInventory();
 			Zombie z = (Zombie) PlayerMobDataMap.get(event.getPlayer());
 			z.getEquipment().setHelmet(i.getItem(3));
 			z.getEquipment().setChestplate(i.getItem(2));
 			z.getEquipment().setLeggings(i.getItem(1));
 			z.getEquipment().setBoots(i.getItem(0));
 			z.getEquipment().setItemInHand(i.getItem(4));
 			if(i.contains(Material.REDSTONE)) { 
 				z.setBaby(true);
 			}else{
 				z.setBaby(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("skeletoninv")) {
 			Inventory i = event.getInventory();
 			Skeleton s = (Skeleton) PlayerMobDataMap.get(event.getPlayer());
 			s.getEquipment().setHelmet(i.getItem(3));
 			s.getEquipment().setChestplate(i.getItem(2));
 			s.getEquipment().setLeggings(i.getItem(1));
 			s.getEquipment().setBoots(i.getItem(0));
 			s.getEquipment().setItemInHand(i.getItem(4));
 		}
 		else if(event.getInventory().getName().equals("pigzombieinv")) {
 			Inventory i = event.getInventory();
 			PigZombie pz = (PigZombie) PlayerMobDataMap.get(event.getPlayer());
 			pz.getEquipment().setHelmet(i.getItem(3));
 			pz.getEquipment().setChestplate(i.getItem(2));
 			pz.getEquipment().setLeggings(i.getItem(1));
 			pz.getEquipment().setBoots(i.getItem(0));
 			pz.getEquipment().setItemInHand(i.getItem(4));
 			if(i.contains(Material.REDSTONE)) { 
 				pz.setBaby(true);
 			}else{
 				pz.setBaby(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("sheepinv")) {
 			Inventory i = event.getInventory();
 			Sheep sh = (Sheep) PlayerMobDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				sh.setBaby();
 			}else{
 				sh.setAdult();
 			}
 			if(i.contains(Material.SHEARS)) {
 				sh.setSheared(true);
 			}else{
 				sh.setSheared(false);
 			}
 			if(i.contains(Material.GLOWSTONE_DUST)) {
 				sh.setAgeLock(true);
 			}else{
 				sh.setAgeLock(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("piginv")) {
 			Inventory i = event.getInventory();
 			Pig pig = (Pig) PlayerMobDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				pig.setBaby();
 			}else{
 				pig.setAdult();
 			}
 			if(i.contains(Material.SADDLE)) {
 				pig.setSaddle(true);
 			}else{
 				pig.setSaddle(false);
 			}
 			if(i.contains(Material.GLOWSTONE_DUST)) {
 				pig.setAgeLock(true);
 			}else{
 				pig.setAgeLock(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("cowinv")) {
 			Inventory i = event.getInventory();
 			Cow cow = (Cow) PlayerMobDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				cow.setBaby();
 			}else{
 				cow.setAdult();
 			}
 			if(i.contains(Material.GLOWSTONE_DUST)) {
 				cow.setAgeLock(true);
 			}else{
 				cow.setAgeLock(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("chickeninv")) {
 			Inventory i = event.getInventory();
 			Chicken c = (Chicken) PlayerMobDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				c.setBaby();
 			}else{
 				c.setAdult();
 			}
 			if(i.contains(Material.GLOWSTONE_DUST)) {
 				c.setAgeLock(true);
 			}else{
 				c.setAgeLock(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("creeperinv")) {
 			Inventory i = event.getInventory();
 			Creeper c = (Creeper) PlayerMobDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				c.setPowered(true);
 			}else{
 				c.setPowered(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("horseinv")) {
 			Inventory i = event.getInventory();
 			Horse h = (Horse) PlayerMobDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				h.setBaby();
 			}else{
 				h.setAdult();
 			}
 			if(i.contains(Material.HAY_BLOCK)) {
 				h.setTamed(true);
 				if(i.contains(Material.SKULL_ITEM)) {
 					ItemStack head = i.getItem(i.first(Material.SKULL_ITEM));
 					Player owner = getOwner(head);
 					if(owner == null) {
 						h.setOwner(event.getPlayer());
 					}else{
 						h.setOwner(owner);
 					}
 				}else{
 					h.setOwner(event.getPlayer());
 				}
 			}else{
 				h.setTamed(false);
 			}
 		}
 		else if(event.getInventory().getName().equals("Spawnerinv")) {
 			Inventory i = event.getInventory();
 			SpawnerCache sc = SpawnerSelected.get(event.getPlayer());
 			ItemStack[] InvItems = i.getContents();
 			Inventory NewInv = Bukkit.createInventory(event.getPlayer(), 54, "Spawnerinv");
 			for(ItemStack is : InvItems) {
				if(is.getType().equals(Material.MONSTER_EGG) && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().contains(":")) {
 					NewInv.addItem(is);
 				}
 			}
 			
 		}
 	}}
 	}
 	public ItemStack setOwner(ItemStack item, String owner) {
 		  SkullMeta meta = (SkullMeta) item.getItemMeta();
 		  meta.setOwner(owner);
 		  item.setItemMeta(meta);
 		  return item;
 		}
 	public Player getOwner(ItemStack item) {
 		  SkullMeta meta = (SkullMeta) item.getItemMeta();
 		  if(meta.getOwner() !=null) {
 		  return (Player) Bukkit.getOfflinePlayer(meta.getOwner());
 		  }else{
 			  return null;
 		  }
 		}
 	@EventHandler
 	public void OnInventoryClick(InventoryClickEvent event) {
 		String name = event.getInventory().getName();
 		if(name.equals("zombieinv") || name.equals("skeletoninv") || name.equals("pigzombieinv") || name.equals("sheepinv") || name.equals("piginv") || name.equals("cowinv") || name.equals("horseinv") || name.equals("chickeninv") || name.equals("creeperinv")) {
 			if(event.getSlot() == 8 && event.getCurrentItem().getType() == Material.MONSTER_EGG && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getInventory().getItem(8).equals(event.getCurrentItem())){
 				Player p = (Player) event.getWhoClicked();
 				Entity e = PlayerMobDataMap.get(p);
 				EggHandler.addegg(e);
 				ItemStack eggitem = EggHandler.GetEggitem(e, "EMA Egg id: " + e.getEntityId(),ChatColor.AQUA + e.getType().getName());
 				event.getCurrentItem().setItemMeta(eggitem.getItemMeta());
 			}
 		}
 	}
 	@EventHandler
 	public void OnPlayerInteract(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 	if(Armoryenabled.get(p) != null && Armoryenabled.get(p)) {
 		if(event.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGG) && event.hasBlock()) {
 			ItemStack egg = p.getItemInHand();
 			ItemMeta eggmeta = egg.getItemMeta();
 		if(eggmeta.hasDisplayName() && eggmeta.getDisplayName().contains(": ")) {
 			String[] name = eggmeta.getDisplayName().split(": ");
 			if(name.length == 2) {
 				if(EggHandler.GetEggList().contains(name[1])) {
 					Location loc = event.getClickedBlock().getLocation();
 					loc.setY(loc.getY() + 1);
 					Entity entity = EggHandler.Loadentity(name[1],loc);
 				}else{
 					
 				}
 			}
 		}
 		}else if(event.getPlayer().getItemInHand().getType().equals(Material.BONE) && event.hasBlock() && event.getClickedBlock().getTypeId() == 52) {
 				Block b = event.getClickedBlock();
 				Location BlockLocation = b.getLocation();
 			if(SpawnerHandler.IsEMASpawner(BlockLocation).equals(false)) {
 					Messenger.playermessage("EMASpawer created", p);
 					SpawnerHandler.NewEMASpawner(b, p);
 					SpawnerHandler.OpenSpawnerInventory(b, p);
 			}else{
 				SpawnerHandler.OpenSpawnerInventory(b, p);
 			}
 			SpawnerSelected.put(p, SpawnerHandler.getSpawner(b.getLocation()));
 		}
 	}
 	}
 }
