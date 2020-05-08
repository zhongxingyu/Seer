 package com.ainast.morepowerfulmobsreloaded;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.potion.PotionType;
 
 public class BossMob implements Runnable, Listener{
 	
 	LivingEntity boss;
 	EntityType type = EntityType.ZOMBIE;
 	String name = "Testificate";
 	ItemStack weaponInHand;
 	ItemStack[] armor;	
 	ItemStack[] drops;
 	int dropChance = 0;
 	int experience = 0;
 	String deathMessage = "Mob is dead";
 	int deathMessageRadius = 10;
 	double maxHealth = 100;
 	String worldName = "world";
 	double x = 0;
 	double y = 70;
 	double z = 0;
 	boolean potThrower = false;
 	boolean leader = false;
 	int potTask = 0;
 	int leaderTask = 0;
 	
 	public BossMob(){
 		MPMTools.plugin.getServer().getPluginManager().registerEvents(this,  MPMTools.plugin);
 	}
 	
 	@SuppressWarnings("deprecation")
 	@Override
 	public void run() {
 		if (!isAlive()){
 			if (potTask!=0){
 				MPMTools.plugin.getServer().getScheduler().cancelTask(potTask);
 				potTask=0;
 			}
 			if (leaderTask!=0){
 				MPMTools.plugin.getServer().getScheduler().cancelTask(leaderTask);
 				leaderTask=0;
 			}
 			boss = getLocation().getWorld().spawnCreature(getLocation(), type);
 			if (getName()!=null) boss.setCustomName(getName());
 			boss.setCustomNameVisible(true);
 			if (getArmor()!=null) boss.getEquipment().setArmorContents(getArmor());
 			if (getWeaponInHand()!=null) boss.getEquipment().setItemInHand(getWeaponInHand());
 			boss.setMaxHealth(getMaxHealth());
 			if (potThrower){
 				potTask = MPMTools.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(MPMTools.plugin, new PotThrower(boss), 20, 40);
 			}
 		}
 	}
 	
 	public void setLeader(boolean leader){
 		this.leader = true;
 	}
 	
 	public boolean getLeader(){
 		return this.leader;
 	}
 	
 	public void setPotThower(boolean potThower){
 		this.potThrower = potThrower;
 	}
 	
 	public boolean getPotThrower(){
 		return this.potThrower;
 	}
 	
 	public void setLocation(String worldName, double x, double y, double z){
 		this.worldName = worldName;
 		this.x = x;
 		this.y = y;
 		this.z = z;
 	}
 	
 	public Location getLocation(){
 		World world = MPMTools.plugin.getServer().getWorld(worldName);
 		
 		if (world==null) throw new IllegalArgumentException("WORLD CAN NOT BE NULL, CHECK IF MULTIVERSE IS LOADED.");
 		
 		Location location = new Location(world, x, y, z);
 		return location;
 	}
 	
 	public void setMaxHealth(double maxHealth){
 		this.maxHealth = maxHealth;
 	}
 	
 	public double getMaxHealth(){
 		return this.maxHealth;
 	}
 	
 	public void setDeathMessageRadius(int deathMessageRadius){
 		this.deathMessageRadius = deathMessageRadius;
 	}
 	
 	public void setDeathMessage(String deathMessage){
 		this.deathMessage = deathMessage;
 	}
 	
 	public String getDeathMessage(){
 		return this.deathMessage;
 	}
 	public void setExperience(int experience){
 		this.experience = experience;
 	}
 	
 	public void setDropChance(int chance){
 		this.dropChance = chance;
 	}
 	
 	public int getDropChance(){
 		return this.dropChance;
 	}
 	
 	public void setDrops(ItemStack[] drops){
 		this.drops = drops;
 	}
 	
 	public ItemStack[] getDrops(){
 		return this.drops;
 	}
 	
 	public void setArmor(ItemStack[] armor){
 		this.armor = armor;
 	}
 	
 	public ItemStack[] getArmor(){
 		return this.armor;
 	}
 	
 	public void setWeaponInHand(ItemStack weaponInHand){
 		this.weaponInHand = weaponInHand;
 	}
 	
 	public ItemStack getWeaponInHand(){
 		return this.weaponInHand;
 	}
 	
 	public boolean isAlive(){
 		if (boss!=null){
 			if (!boss.isDead()){
 				return true;
 			}
 		}
 		return false; //if none of the above match the boss is not alive.
 	}
 	
 	public boolean isDead(){
 		return !isAlive();
 	}
 	
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	public String getName(){
 		return this.name;
 	}
 	
 	public void setType(EntityType type){
 		this.type = type;
 	}
 	
 	public EntityType getType(){
 		return type;
 	}
 
 	public int getExperience(){
 		return this.experience;
 	}
 	
 	public void dropBossItems(Location location){
 		for (ItemStack item : getDrops()){
 			Bukkit.getWorld(location.getWorld().getName()).dropItem(location, item);
 		}
 	}
 	
 	public void sendMassDeathMessage(Entity entity){
 		for (Entity e : entity.getNearbyEntities(getDeathMessageRadius(), getDeathMessageRadius(), getDeathMessageRadius())){
 			if (e instanceof Player){
 				((Player) e).sendMessage(ChatColor.GOLD + getDeathMessage());
 			}
 		}
 	}
 	
 	private double getDeathMessageRadius() {
 		// TODO Auto-generated method stub
 		return deathMessageRadius;
 	}
 
 	@EventHandler
 	public void onBossDeathEvent(EntityDeathEvent event){
 		LivingEntity entity = event.getEntity();
 		if (entity.equals(boss)){
 			event.setDroppedExp(getExperience());
 			event.getDrops().clear();
 			dropBossItems(entity.getLocation());
 			sendMassDeathMessage(entity);
 			}	
 		}
 }
 
 class Leader implements Runnable, Listener{
 	LivingEntity boss;
 	EntityType subordinateType;
 	List<LivingEntity> subordinates = new ArrayList<LivingEntity>();
 	static HashMap<LivingEntity, ArrayList<LivingEntity>> subordinateList = new HashMap<LivingEntity, ArrayList<LivingEntity>>();
 	
 	public Leader(Entity entity, EntityType subordinateType){
 		boss = (LivingEntity) entity;	
 		this.subordinateType = subordinateType;
 		subordinateList.put(boss,  new ArrayList<LivingEntity>());
 	}
 	
 	@Override
 	public void run() {
 		if (this.boss!=null){
 			int chance = MPMTools.generator.nextInt(100)+1;
 			if (chance<10 || subordinateList.get(boss).size()>5){
 				ArrayList<LivingEntity> el = subordinateList.get(boss);
 				el.add(boss.getLocation().getWorld().spawnCreature(boss.getLocation(), subordinateType));
 				subordinateList.put(boss, el);
 			}
 			if (chance<20){
 				ArrayList<LivingEntity> el = subordinateList.get(boss);
 				el.add(boss.getLocation().getWorld().spawnCreature(boss.getLocation(), subordinateType));
 				subordinateList.put(boss, el);
 			}
 			if (chance<30){
 				ArrayList<LivingEntity> el = subordinateList.get(boss);
 				el.add(boss.getLocation().getWorld().spawnCreature(boss.getLocation(), subordinateType));
 				subordinateList.put(boss, el);}			
 		}
 	}
 	
 	@EventHandler
 	public void onEntityDeathEvent(EntityDeathEvent event){
 		List<LivingEntity> subs = subordinateList.get(boss);
 		
 		for (LivingEntity entity : subs){
 			entity.damage(10000);
 		}
 				
 	}
 }
 
 class PotThrower implements Runnable{
 	LivingEntity boss;
 	public PotThrower(Entity entity){
 		boss = (LivingEntity) entity;
 	}
 	
 	@Override
 	public void run(){
 		if (this.boss!=null){
 			System.out.println("Throw Potion");
 			int chance = MPMTools.generator.nextInt(100)+1;
 			if (chance<101){
 				System.out.println("Throw Potion at Entity");
 				List<Entity> entityList = boss.getNearbyEntities(10, 10, 10);
 				
 				for (Entity entity : entityList){
 					chance = MPMTools.generator.nextInt(100)+1;
 					System.out.println(chance);
 					if (chance<=20){
 						
 						ThrownPotion potion = boss.launchProjectile(ThrownPotion.class);
 						potion.setShooter(boss);
 						potion.getEffects().add(new PotionEffect(PotionEffectType.HARM, 50, 20));
 						potion.setVelocity(boss.getLocation().getDirection().multiply(2));
 						boss.launchProjectile(ThrownPotion.class).setMetadata("HARM", new FixedMetadataValue(MPMTools.plugin, true));
 					}
 				}	
 			}
 		}else{
 
 		}
 	}
 }
