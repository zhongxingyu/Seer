 package com.runetooncraft.plugins.EasyMobArmory;
 
 
 
 import java.util.HashMap;
 
 import net.minecraft.server.v1_6_R2.Item;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import net.minecraft.server.v1_6_R2.TileEntityChest;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftInventory;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Skeleton;
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
 import com.runetooncraft.plugins.EasyMobArmory.core.Config;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.EggHandler;
 
 
 public class EMAListener implements Listener {
 	Config config;
 	public static HashMap<Player, Entity> PlayerMobDataMap = new HashMap<Player, Entity>();
 	public static HashMap<Player, Boolean> Armoryenabled = new HashMap<Player, Boolean>();
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
 				inv.setItem(8, EggHandler.GetEggitem(e,ChatColor.GOLD + "Get Mob Egg"));
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
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, pig);
 			}
 		}else if(e.getType().equals(EntityType.COW)) {
 			ItemStack i = p.getItemInHand();
 			Cow cow = (Cow) e;
 			if(i.getType().equals(Material.BONE)) {
 				Inventory inv = Bukkit.createInventory(p, 9, "cowinv");
 				if(!cow.isAdult()) inv.setItem(5, new ItemStack(Material.REDSTONE));
 				p.openInventory(inv);
 				PlayerMobDataMap.put(p, cow);
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
 				if(h.isCarryingChest()) inv.setItem(8, new ItemStack(Material.CHEST));
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
 		}
 		else if(event.getInventory().getName().equals("cowinv")) {
 			Inventory i = event.getInventory();
 			Cow cow = (Cow) PlayerMobDataMap.get(event.getPlayer());
 			if(i.contains(Material.REDSTONE)) {
 				cow.setBaby();
 			}else{
 				cow.setAdult();
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
 		if(name.equals("zombieinv") || name.equals("skeletoninv") || name.equals("pigzombieinv") || name.equals("sheepinv") || name.equals("piginv") || name.equals("cowinv") || name.equals("horseinv")) {
 			if(event.getCurrentItem().getType() == Material.MONSTER_EGG && event.getCurrentItem().getItemMeta().hasDisplayName() && event.getInventory().getItem(8).equals(event.getCurrentItem())){
 				Player p = (Player) event.getWhoClicked();
 				Entity e = PlayerMobDataMap.get(p);
 				EggHandler.addegg(e);
 				p.getInventory().addItem(EggHandler.GetEggitem(e, "EMA Egg id: " + e.getEntityId()));
 			}
 		}
 	}
 	@EventHandler
 	public void OnPlayerInteract(PlayerInteractEvent event) {
 		if(event.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGG)) {
 			Player p = event.getPlayer();
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
 		}
 	}
 }
