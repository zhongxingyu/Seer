 package com.runetooncraft.plugins.EasyMobArmory.egghandler;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Zombie;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.bergerkiller.bukkit.common.entity.CommonEntity;
 import com.bergerkiller.bukkit.common.utils.EntityUtil;
 import com.runetooncraft.plugins.EasyMobArmory.EMA;
 import com.runetooncraft.plugins.EasyMobArmory.MobCache.*;
 import com.runetooncraft.plugins.EasyMobArmory.core.Config;
 import com.runetooncraft.plugins.EasyMobArmory.core.InventorySerializer;
 
 public class EggHandler {
 	public static Eggs eggs = EMA.eggs;
 	public static HashMap<String, ZombieCache> ZombieCache = new HashMap<String, ZombieCache>();
 	public static HashMap<String, PigZombieCache> PigZombieCache = new HashMap<String, PigZombieCache>();
 	public static HashMap<String, SkeletonCache> SkeletonCache = new HashMap<String, SkeletonCache>();
 	public static ItemStack GetEggitem(Entity e,String name, String lore) {
 		ItemStack egg = new ItemStack(Material.MONSTER_EGG, 1, (short) e.getEntityId());
 		return renameItem(egg, name, lore);
 	}
 	public static ItemStack renameItem(ItemStack is, String newName, String lore){
 		  ItemMeta meta = is.getItemMeta();
 		  meta.setDisplayName(newName);
 		  List<String> lorelist = new ArrayList<String>();
 		  lorelist.add(lore);
 		  meta.setLore(lorelist);
 		  is.setItemMeta(meta);
 		  return is;
 	}
 	public static void addegg(Entity e) {
 		YamlConfiguration eggsyml = eggs.GetConfig();
 		if(eggsyml.getList("Eggs.List") != null) {
 			if(!eggsyml.getList("Eggs.List").contains(Integer.toString(e.getEntityId()))) {
 				eggs.addtolist("Eggs.List", Integer.toString(e.getEntityId()));
 				eggsyml.set("Eggs.id." + e.getEntityId() + ".Type", e.getType().getTypeId());
 				if(e.getType().equals(EntityType.ZOMBIE)) {
 					Zombie z = (Zombie) e;
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".Armor", InventorySerializer.tobase64(InventorySerializer.getArmorEntityInventory(z.getEquipment())));
 					Inventory HandItem = Bukkit.getServer().createInventory(null, InventoryType.PLAYER);
 					HandItem.setItem(0, z.getEquipment().getItemInHand());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".Hand", InventorySerializer.tobase64(HandItem));
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".isbaby", z.isBaby());
 					eggs.save();
 				}else if(e.getType().equals(EntityType.SKELETON)) {
 					Skeleton s = (Skeleton) e;
 					Inventory HandItem = Bukkit.getServer().createInventory(null, InventoryType.PLAYER);
 					HandItem.setItem(0, s.getEquipment().getItemInHand());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".Armor", InventorySerializer.tobase64(InventorySerializer.getArmorEntityInventory(s.getEquipment())));
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".Hand", InventorySerializer.tobase64(HandItem));
 				}else if(e.getType().equals(EntityType.PIG_ZOMBIE)) {
 					PigZombie pz = (PigZombie) e;
 					Inventory HandItem = Bukkit.getServer().createInventory(null, InventoryType.PLAYER);
 					HandItem.setItem(0, pz.getEquipment().getItemInHand());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".Armor", InventorySerializer.tobase64(InventorySerializer.getArmorEntityInventory(pz.getEquipment())));
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".Hand", InventorySerializer.tobase64(HandItem));
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".isbaby", pz.isBaby());
 				}
 			}else{
 				//Egg already existent
 			}
 		}
 	}
 	public static List<String> GetEggList() {
 		return (List<String>) eggs.getList("Eggs.List");
 	}
 	public static Entity Loadentity(String id, Location loc) {
 		int Entityid = eggs.getInt("Eggs.id." + id + ".Type");
 		EntityType etype = EntityType.fromId(Entityid);
 	if(etype.equals(EntityType.ZOMBIE) && !ZombieCache.containsKey(id)) {
 		CommonEntity entity = CommonEntity.create(etype);
 		String entityLoc = "Eggs.id." + id + ".";
 		Inventory Armorstackinv = InventorySerializer.frombase64(eggs.getString(entityLoc + "Armor"));
 		Inventory iteminv = InventorySerializer.frombase64(eggs.getString(entityLoc +"Hand"));
 		Boolean isbaby = eggs.getBoolean(entityLoc + "isbaby");
 		Entity bukkitentity = entity.getEntity();
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		if(etype.equals(EntityType.ZOMBIE)) {
 			Zombie z = (Zombie) EntityUtil.getEntity(loc.getWorld(), entid);
 			z.getEquipment().setArmorContents(Armorstackinv.getContents());
 			z.getEquipment().setItemInHand(iteminv.getItem(0));
 			z.setBaby(isbaby);
 			ZombieCache.put(id, new ZombieCache(Armorstackinv.getContents(),iteminv.getItem(0),isbaby));
 			return z;
 		}else{
 			return bukkitentity;
 		}
 	}else if(etype.equals(EntityType.ZOMBIE) && ZombieCache.containsKey(id)) {
 			ZombieCache set = ZombieCache.get(id);
 			UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 			Zombie z = (Zombie) EntityUtil.getEntity(loc.getWorld(), entid);
 			z.getEquipment().setArmorContents(set.Equip);
 			z.getEquipment().setItemInHand(set.handitem);
 			z.setBaby(set.isbaby);
 			return z;
 	}else if(etype.equals(EntityType.SKELETON) && !SkeletonCache.containsKey(id)) {
 		CommonEntity entity = CommonEntity.create(etype);
 		String entityLoc = "Eggs.id." + id + ".";
 		Inventory Armorstackinv = InventorySerializer.frombase64(eggs.getString(entityLoc + "Armor"));
 		Inventory iteminv = InventorySerializer.frombase64(eggs.getString(entityLoc +"Hand"));
 		Entity bukkitentity = entity.getEntity();
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		if(etype.equals(EntityType.SKELETON)) {
 			Skeleton s = (Skeleton) EntityUtil.getEntity(loc.getWorld(), entid);
 			s.getEquipment().setArmorContents(Armorstackinv.getContents());
 			s.getEquipment().setItemInHand(iteminv.getItem(0));
 			SkeletonCache.put(id, new SkeletonCache(Armorstackinv.getContents(),iteminv.getItem(0)));
 			return s;
 		}else{
 			return bukkitentity;
 		}
 	}else if(etype.equals(EntityType.SKELETON) && SkeletonCache.containsKey(id)) {
 		SkeletonCache set = SkeletonCache.get(id);
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		Skeleton s = (Skeleton) EntityUtil.getEntity(loc.getWorld(), entid);
 		s.getEquipment().setArmorContents(set.Equip);
 		s.getEquipment().setItemInHand(set.handitem);
 		return s;
 	}else if(etype.equals(EntityType.PIG_ZOMBIE) && !PigZombieCache.containsKey(id)) {
 		CommonEntity entity = CommonEntity.create(etype);
 		String entityLoc = "Eggs.id." + id + ".";
 		Inventory Armorstackinv = InventorySerializer.frombase64(eggs.getString(entityLoc + "Armor"));
 		Inventory iteminv = InventorySerializer.frombase64(eggs.getString(entityLoc +"Hand"));
 		Boolean isbaby = eggs.getBoolean(entityLoc + "isbaby");
 		Entity bukkitentity = entity.getEntity();
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
		if(etype.equals(EntityType.PIG_ZOMBIE)) {
 			PigZombie pz = (PigZombie) EntityUtil.getEntity(loc.getWorld(), entid);
 			pz.getEquipment().setArmorContents(Armorstackinv.getContents());
 			pz.getEquipment().setItemInHand(iteminv.getItem(0));
 			pz.setBaby(isbaby);
 			ZombieCache.put(id, new ZombieCache(Armorstackinv.getContents(),iteminv.getItem(0),isbaby));
 			return pz;
 		}else{
 			return bukkitentity;
 		}
 	}else if(etype.equals(EntityType.PIG_ZOMBIE) && PigZombieCache.containsKey(id)) {
 		PigZombieCache set = PigZombieCache.get(id);
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		PigZombie pz = (PigZombie) EntityUtil.getEntity(loc.getWorld(), entid);
 		pz.getEquipment().setArmorContents(set.Equip);
 		pz.getEquipment().setItemInHand(set.handitem);
 		pz.setBaby(set.isbaby);
 		return pz;
 	}
 	return CommonEntity.create(etype).getEntity();
 	}
 }
