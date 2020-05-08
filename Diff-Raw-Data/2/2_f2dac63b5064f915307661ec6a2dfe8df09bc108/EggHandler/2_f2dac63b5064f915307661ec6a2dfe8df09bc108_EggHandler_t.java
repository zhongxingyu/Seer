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
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Horse.Color;
 import org.bukkit.entity.Horse.Variant;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.PigZombie;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
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
 	public static HashMap<String, SheepCache> SheepCache = new HashMap<String, SheepCache>();
 	public static HashMap<String, PigCache> PigCache = new HashMap<String, PigCache>();
 	public static HashMap<String, CowCache> CowCache = new HashMap<String, CowCache>();
 	public static HashMap<String, HorseCache> HorseCache = new HashMap<String, HorseCache>();
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
 				}else if(e.getType().equals(EntityType.SHEEP)) {
 					Sheep s = (Sheep) e;
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".isbaby", !s.isAdult());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".sheared", s.isSheared());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".agelock", s.getAgeLock());
 				}else if(e.getType().equals(EntityType.PIG)) {
 					Pig p = (Pig) e;
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".isbaby", !p.isAdult());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".saddled", p.hasSaddle());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".agelock", p.getAgeLock());
 				}else if(e.getType().equals(EntityType.COW)) {
 					Cow c = (Cow) e;
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".isbaby", !c.isAdult());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".agelock", c.getAgeLock());
 				}else if(e.getType().equals(EntityType.HORSE)) {
 					Horse h = (Horse) e;
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".isbaby", !h.isAdult());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".agelock", h.getAgeLock());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".tamed", h.isTamed());
 					if(h.isTamed()) eggsyml.set("Eggs.id." + e.getEntityId() + ".tamer", h.getOwner().getName());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".carryingchest", h.isCarryingChest());
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".inventory", InventorySerializer.tobase64(h.getInventory()));
 					eggsyml.set("Eggs.id." + e.getEntityId() + ".varient", ParseHorseVarient(h.getVariant()));
 					if(h.getVariant() == Variant.HORSE) {
 						eggsyml.set("Eggs.id." + e.getEntityId() + ".color", ParseHorseColor(h.getColor()));
 					}
 				}
 			}else{
 				//Egg already existent
 			}
 		}
 		eggs.save();
 	}
 	private static String ParseHorseColor(Color color) {
 		if(color.equals(Color.BLACK)) {
 			return "Black";
 		}else if(color.equals(Color.BROWN)) {
 			return "Brown";
 		}else if(color.equals(Color.CHESTNUT)) {
 			return "Chestnut";
 		}else if(color.equals(Color.CREAMY)) {
 			return "Creamy";
 		}else if(color.equals(Color.DARK_BROWN)) {
 			return "Dark_Brown";
 		}else if(color.equals(Color.GRAY)) {
 			return "Gray";
 		}else if(color.equals(Color.WHITE)) {
 			return "White";
 		}else{
 			return "Black";
 		}
 	}
 	private static Color ParseHorseColorString(String color) {
 		if(color.equals("Black")) {
 			return Color.BLACK;
 		}else if(color.equals("Brown")) {
 			return Color.BROWN;
 		}else if(color.equals("Chestnut")) {
 			return Color.CHESTNUT;
 		}else if(color.equals("Creamy")) {
 			return Color.CREAMY;
 		}else if(color.equals("Dark_Brown")) {
 			return Color.DARK_BROWN;
 		}else if(color.equals("Gray")) {
 			return Color.GRAY;
 		}else if(color.equals("White")) {
 			return Color.WHITE;
 		}else if(color.equals("Black")) {
 			return Color.BLACK;
 		}else{
 			return Color.BLACK;
 		}
 	}
 	private static String ParseHorseVarient(Variant variant) {
 		if(variant.equals(Variant.HORSE)) {
 			return "Horse";
 		}else if(variant.equals(Variant.DONKEY)) {
 			return "Donkey";
 		}else if(variant.equals(Variant.MULE)) {
 			return "Mule";
 		}else if(variant.equals(Variant.SKELETON_HORSE)) {
 			return "Skeleton_Horse";
 		}else if(variant.equals(Variant.UNDEAD_HORSE)) {
 			return "Undead_Horse";
 		}else{
 			return "Horse";
 		}
 	}
 	private static Variant ParseHorseString(String s) {
 		if(s.equals("Horse")) {
 			return Variant.HORSE;
 		}else if(s.equals("Donkey")) {
 			return Variant.DONKEY;
 		}else if(s.equals("Mule")) {
 			return Variant.MULE;
 		}else if(s.equals("Skeleton_Horse")) {
 			return Variant.SKELETON_HORSE;
 		}else if(s.equals("Undead_Horse")) {
 			return Variant.UNDEAD_HORSE;
 		}else{
 			return Variant.HORSE;
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
 			PigZombieCache.put(id, new PigZombieCache(Armorstackinv.getContents(),iteminv.getItem(0),isbaby));
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
 	}else if(etype.equals(EntityType.SHEEP) && !SheepCache.containsKey(id)) {
 		CommonEntity entity = CommonEntity.create(etype);
 		String entityLoc = "Eggs.id." + id + ".";
 		Boolean isbaby = eggs.getBoolean(entityLoc + "isbaby");
 		Boolean sheared = eggs.getBoolean(entityLoc + "sheared");
 		Boolean agelock = eggs.getBoolean(entityLoc + "agelock");
 		Entity bukkitentity = entity.getEntity();
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		if(etype.equals(EntityType.SHEEP)) {
 			Sheep s = (Sheep) EntityUtil.getEntity(loc.getWorld(), entid);
 			if(isbaby.equals(true)) s.setBaby(); else s.setAdult();
 			if(sheared.equals(true)) s.setSheared(true); else s.setSheared(false);
 			if(agelock.equals(true)) s.setAgeLock(true); else s.setAgeLock(false);
 			SheepCache.put(id, new SheepCache(isbaby,sheared,agelock));
 			return s;
 		}else{
 			return bukkitentity;
 		}
 	}else if(etype.equals(EntityType.SHEEP) && SheepCache.containsKey(id)) {
 		SheepCache set = SheepCache.get(id);
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		Sheep s = (Sheep) EntityUtil.getEntity(loc.getWorld(), entid);
 		if(set.isbaby) s.setBaby(); else s.setAdult();
 		if(set.sheared) s.setSheared(true); else s.setSheared(false);
 		if(set.agelock) s.setAgeLock(true); else s.setAgeLock(false);
 		return s;
 	}else if(etype.equals(EntityType.PIG) && !PigCache.containsKey(id)) {
 		CommonEntity entity = CommonEntity.create(etype);
 		String entityLoc = "Eggs.id." + id + ".";
 		Boolean isbaby = eggs.getBoolean(entityLoc + "isbaby");
 		Boolean saddled = eggs.getBoolean(entityLoc + "saddled");
 		Boolean agelock = eggs.getBoolean(entityLoc + "agelock");
 		Entity bukkitentity = entity.getEntity();
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		if(etype.equals(EntityType.PIG)) {
 			Pig p = (Pig) EntityUtil.getEntity(loc.getWorld(), entid);
 			if(isbaby.equals(true)) p.setBaby(); else p.setAdult();
 			if(saddled.equals(true)) p.setSaddle(true); else p.setSaddle(false);
 			if(agelock.equals(true)) p.setAgeLock(true); else p.setAgeLock(false);
 			PigCache.put(id, new PigCache(isbaby,saddled,agelock));
 			return p;
 		}else{
 			return bukkitentity;
 		}
 	}else if(etype.equals(EntityType.PIG) && PigCache.containsKey(id)) {
 		PigCache set = PigCache.get(id);
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		Pig p = (Pig) EntityUtil.getEntity(loc.getWorld(), entid);
 		if(set.isbaby) p.setBaby(); else p.setAdult();
 		p.setSaddle(set.saddled);
 		p.setAgeLock(set.agelock);
 		return p;
 	}else if(etype.equals(EntityType.COW) && !CowCache.containsKey(id)) {
 		CommonEntity entity = CommonEntity.create(etype);
 		String entityLoc = "Eggs.id." + id + ".";
 		Boolean isbaby = eggs.getBoolean(entityLoc + "isbaby");
 		Boolean agelock = eggs.getBoolean(entityLoc + "agelock");
 		Entity bukkitentity = entity.getEntity();
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		if(etype.equals(EntityType.COW)) {
 			Cow c = (Cow) EntityUtil.getEntity(loc.getWorld(), entid);
 			if(isbaby.equals(true)) c.setBaby(); else c.setAdult();
 			CowCache.put(id, new CowCache(isbaby,agelock));
 			return c;
 		}else{
 			return bukkitentity;
 		}
 	}else if(etype.equals(EntityType.COW) && CowCache.containsKey(id)) {
 		CowCache set = CowCache.get(id);
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		Cow c = (Cow) EntityUtil.getEntity(loc.getWorld(), entid);
 		if(set.isbaby) c.setBaby(); else c.setAdult();
 		c.setAgeLock(set.agelock);
 		return c;
 	}else if(etype.equals(EntityType.HORSE) && !HorseCache.containsKey(id)) {
		CommonEntity entity = CommonEntity.create(EntityType.fromId(100));
 		String entityLoc = "Eggs.id." + id + ".";
 		Boolean isbaby = eggs.getBoolean(entityLoc + "isbaby");
 		Boolean agelock = eggs.getBoolean(entityLoc + "agelock");
 		Boolean tamed = eggs.getBoolean(entityLoc + "tamed");
 		Boolean chest = eggs.getBoolean(entityLoc + "carryingchest");
 		Variant variant = ParseHorseString(eggs.getString(entityLoc + "varient"));
 		Inventory horseinv = InventorySerializer.frombase64(eggs.getString(entityLoc + "inventory"));
 		Player horsetamer = null;
 		Color color = null;
 		if(tamed) {
 			String tamer = eggs.getString(entityLoc + "tamer");
 			horsetamer = (Player) Bukkit.getOfflinePlayer(tamer);
 		}
 		if(variant.equals(Variant.HORSE)) {
 			color = ParseHorseColorString(entityLoc + "color");
 		}
 		Entity bukkitentity = entity.getEntity();
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		if(etype.equals(EntityType.HORSE)) {
 			Horse h = (Horse) EntityUtil.getEntity(loc.getWorld(), entid);
 			if(isbaby.equals(true)) h.setBaby(); else h.setAdult();
 			h.setTamed(tamed);
 			if(tamed) h.setOwner(horsetamer);
 			h.setVariant(variant);
 			if(variant.equals(Variant.HORSE)) h.setColor(color);
 			h.getInventory().setContents(horseinv.getContents());
 			HorseCache.put(id, new HorseCache(isbaby,agelock,tamed,chest,variant,horseinv,horsetamer,color));
 			return h;
 		}else{
 			return bukkitentity;
 		}
 	}else if(etype.equals(EntityType.HORSE) && HorseCache.containsKey(id)) {
 		HorseCache set = HorseCache.get(id);
 		UUID entid = loc.getWorld().spawnEntity(loc, etype).getUniqueId();
 		Horse h = (Horse) EntityUtil.getEntity(loc.getWorld(), entid);
 		if(set.isbaby) h.setBaby(); else h.setAdult();
 		h.setAgeLock(set.agelock);
 		h.setVariant(set.variant);
 		h.setTamed(set.tamed);
 		if(set.variant.equals(Variant.HORSE)) {
 			h.setColor(set.color);
 		}
 		if(set.tamed) {
 			h.setOwner(set.tamer);
 		}
 		h.getInventory().setContents(set.horseinv.getContents());
 		return h;
 	}
 	return CommonEntity.create(etype).getEntity();
 	}
 }
