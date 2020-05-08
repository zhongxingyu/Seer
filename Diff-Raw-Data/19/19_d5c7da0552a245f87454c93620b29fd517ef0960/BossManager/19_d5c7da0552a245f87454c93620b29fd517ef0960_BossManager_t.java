 package com.ne0nx3r0.rareitemhunter.bosses;
 
 import com.ne0nx3r0.rareitemhunter.RareItemHunter;
 import com.ne0nx3r0.rareitemhunter.bosses.skills.*;
 import de.kumpelblase2.remoteentities.api.RemoteEntity;
 import de.kumpelblase2.remoteentities.api.RemoteEntityType;
 import de.kumpelblase2.remoteentities.api.thinking.goals.DesireAttackOnCollide;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.EntityEquipment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class BossManager
 {
     private final RareItemHunter plugin;
     
     Map<String,BossTemplate> bossTemplates;
     
     Map<String,BossEggSpawnPoint> spawnPoints;
     Map<Location,String> bossEggs;
     Map<Integer,Boss> activeBosses;
     
     private saveFileManager saveManager;
     
     //TODO: Periodically clean up bosses that were removed by /killall, etc.
     
     public BossManager(RareItemHunter plugin)
     {
         this.plugin = plugin;
         
         bossEggs = new HashMap<Location,String>();
         
         activeBosses = new HashMap<Integer,Boss>();
         
         spawnPoints = new HashMap<String,BossEggSpawnPoint>();
         
         List<BossSkill> availableBossSkills = new ArrayList<BossSkill>();
         
         availableBossSkills.add(new Burst());
         availableBossSkills.add(new GreaterBurst());
         availableBossSkills.add(new ShootArrow());
         availableBossSkills.add(new ShootFireball());
         //availableBossSkills.add(new FakeWeb());-> Seems to be sort of... crashing the server. Orefuscator conflict, possibly...
         availableBossSkills.add(new Blink());
         availableBossSkills.add(new JumpAttack());
         availableBossSkills.add(new SpawnZombiePig());
         availableBossSkills.add(new SpawnSkeleton());
         availableBossSkills.add(new SpawnZombie());
         availableBossSkills.add(new SpawnCreeper());
         availableBossSkills.add(new SpawnSpider());
         availableBossSkills.add(new SpawnCaveSpider());
         availableBossSkills.add(new Pull());
         availableBossSkills.add(new SpawnSilverfish());
         availableBossSkills.add(new PoisonDart());
         availableBossSkills.add(new Disorient());
         availableBossSkills.add(new LightningStorm());
         availableBossSkills.add(new LightningBolt());
         
         bossTemplates = new HashMap<String,BossTemplate>();
 
         File bossesFile = new File(plugin.getDataFolder(),"bosses.yml");
 
         if(!bossesFile.exists())
         {
             plugin.copy(plugin.getResource("bosses.yml"),bossesFile);
         }
         
         FileConfiguration bossesYml = YamlConfiguration.loadConfiguration(bossesFile);
         
         for(Iterator<String> it = bossesYml.getKeys(false).iterator(); it.hasNext();)
         {
             String sBossName = it.next();
             
             String sType = bossesYml.getString(sBossName+"."+"type");
 
             EntityType entityType = EntityType.fromName(sType);
             
             if(entityType == null)
             {
                 plugin.getLogger().log(Level.WARNING,sBossName+" has an invalid entity type '"+bossesYml.getString(sBossName+"."+"type")+"', skipping this boss.");
                 
                 plugin.getLogger().log(Level.WARNING,"Hint: PigZombie vs pig_zombie");
                 
                 continue;
             }
 
             int hp = bossesYml.getInt(sBossName+".hp");
 
             int attackPower = bossesYml.getInt(sBossName+".attackPower");
 
             int essencesDropped = bossesYml.getInt(sBossName+".essencesDropped");
 
 // Add equipment if it has any
             List<ItemStack> equipment = new ArrayList<ItemStack>();
             
             if(bossesYml.isSet(sBossName+".armor"))
             {
                 List<String> bossEquipmentStrings = (List<String>) bossesYml.getList(sBossName+".armor");
                 
                 for(String sItem : bossEquipmentStrings)
                 {
                     if(equipment.size() < 4)
                     {
                         ItemStack is = this.getItemStackFromEquipmentString(sBossName,sItem);
                         
                         if(is != null)
                         {
                             equipment.add(is);
                         }
                     }
                     else
                     {
                         plugin.getLogger().log(Level.WARNING,sBossName+" has too many armor items, skipping '"+sItem+"'");
                     }
                 }
             }
             
 // Add weapon if boss has one
             ItemStack weapon = null; 
             
             if(bossesYml.isSet(sBossName+".weapon"))
             {
                 // Method will return null if invalid, and handle notification of error
                 weapon = this.getItemStackFromEquipmentString(sBossName,bossesYml.getString(sBossName+".weapon"));
             }
 
 // Create the template
             BossTemplate bossTemplate = new BossTemplate(sBossName,entityType,hp,attackPower,essencesDropped,equipment,weapon);
             
 // Add any skills
             if(bossesYml.isSet(sBossName+".skills"))
             {
                 List<String> skillStrings = (List<String>) bossesYml.getList(sBossName+".skills");
 
                 for(String skillString : skillStrings)
                 {           
                     String[] skillValues = skillString.split(" ");
 
                     String skillName = skillValues[2];
                     int chance = Integer.parseInt(skillValues[0].substring(0,skillString.indexOf("%")));
                     int level = Integer.parseInt(skillValues[4]);
 
                     for(BossSkill bossSkill : availableBossSkills)
                     {
                         if(bossSkill.getYmlName().equalsIgnoreCase(skillName))
                         {
                             bossTemplate.addSkill(bossSkill, level, chance);
                         }
                     }
                 }  
             }
             
 // Add any events
             if(bossesYml.isSet(sBossName+".events"))
             {
                 List<String> eventStrings = (List<String>) bossesYml.getList(sBossName+".events");
 
                 for(String eventString : eventStrings)
                 {           
                     String[] eventValues = eventString.split(" ");
 
                     String sEventType = eventValues[0];
 
                     BossEventType eventType = null;
                     
                     for(BossEventType bet : BossEventType.values())
                     {
                         if(bet.name().equalsIgnoreCase(sEventType))
                         {
                             eventType = BossEventType.valueOf(sEventType);
                         }
                     }
 
                     if(eventType == null)
                     {
                         plugin.getLogger().log(Level.WARNING,"'"+sEventType+"' is not a valid event type on boss '"+sBossName+"'. Skipping.");
 
                         continue;
                     }
 
                     int iEventValue = -1;
 
                     try
                     {
                         iEventValue = Integer.parseInt(eventValues[1]);
                     }
                     catch(Exception e)
                     {
                         plugin.getLogger().log(Level.WARNING,"'"+eventValues[1]+"' is not a valid event value on boss '"+sBossName+"'. Skipping.");
 
                         continue;
                     }                
 
                     String skillName = eventValues[2].replace("_", " ");
                     int level = Integer.parseInt(eventValues[4]);
 
                     for(BossSkill bossSkill : availableBossSkills)
                     {
                         if(bossSkill.getName().equalsIgnoreCase(skillName))
                         {
                             bossTemplate.addEvent(new BossEvent(eventType,iEventValue,bossSkill,level));
                         }
                     }
                 }  
             }
             
 // Save the template
             this.bossTemplates.put(bossTemplate.name,bossTemplate);
         }
        
         this.saveManager = new saveFileManager(plugin,this);
         
 // Schedule random boss spawns
 
         int iTimer = 60 * 20 * plugin.getConfig().getInt("timeBetweenChancesToGenerateBossEgg",60 * 60 * 20);
         int iMaxChance = plugin.getConfig().getInt("maxChanceToGenerateBossEgg",20);
         int iExpiration = 60 * 20 * plugin.getConfig().getInt("bossEggExpiration",15 * 60 * 20);
         
         if(iTimer > 0)
         {
             plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                 plugin,
                 new RandomlyGenerateBossTask(plugin,iMaxChance,iTimer,iExpiration), 
                 iTimer, 
                 iTimer);
         }
         
 // Schedule targetting / garbage collection for activeBosses
         //plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new SelectBossTargetTask(plugin,this), 20*2, 20*2);
     }
     
     public boolean isBoss(Entity entity)
     {        
         if(this.activeBosses.containsKey(entity.getEntityId()))
         {
             return true;
         }
         
         return false;
     }
 
     public Boss getBoss(Entity entity)
     {
         return this.activeBosses.get(entity.getEntityId());
     }
 
     public boolean isBossEgg(Location eggLocation)
     {
         return this.bossEggs.containsKey(eggLocation);
     }
 
     public Boss hatchBoss(Location eggLocation)
     {
         String sBossName = bossEggs.get(eggLocation);
         
         eggLocation.getBlock().setType(Material.AIR);
         
         eggLocation.getBlock().getRelative(BlockFace.DOWN).setType(Material.AIR);
 
         Boss boss = this.spawnBoss(sBossName, eggLocation);
         
         bossEggs.remove(eggLocation);
         
         return boss;
     }
 
     public Boss spawnBoss(String sBossName, Location eggLocation)
     {
         Boss boss = new Boss(this.bossTemplates.get(sBossName));
         
         Entity ent = null;
         
         if(plugin.entityManager != null && 1 == 2)
         {
             RemoteEntity rEntity = plugin.entityManager.createEntity(RemoteEntityType.valueOf(boss.template.entityType.getName()), eggLocation, false);
             
             ent = rEntity.getBukkitEntity();
             
             //rEntity.getMind().addActionDesire(new DesireFindAttackingTarget(rEntity, 30, false, true, true),1);
             //rEntity.getMind().addActionDesire(new DesireFindNearestTarget(rEntity, EntityHuman.class, 30, false, true, 100),1);
             rEntity.getMind().addActionDesire(new DesireAttackOnCollide(rEntity, Player.class, true, 10f), 1);
         }
         else
         {
             ent = eggLocation.getWorld().spawnEntity(eggLocation, boss.getEntityType());
         }
         
         boss.setEntity(ent);
         
         LivingEntity lent = (LivingEntity) ent;
         
         lent.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,9999999,5));
 
         EntityEquipment lequips = lent.getEquipment();
             
         if(boss.template.equipment != null)
         {
             lequips.setArmorContents(boss.template.equipment.toArray(new ItemStack[4]));
 
             lequips.setBootsDropChance(0f);
             lequips.setLeggingsDropChance(0f);
             lequips.setChestplateDropChance(0f);
             lequips.setHelmetDropChance(0f);
         }
             
         if(boss.template.weapon != null)
         {
             lequips.setItemInHand(boss.template.weapon);
 
             lequips.setItemInHandDropChance(0f);
         }
         
         activeBosses.put(ent.getEntityId(), boss);
         
         this.saveManager.save();
         
         return boss;
     }
 
     public boolean hasSpawnPoints()
     {
         return !this.spawnPoints.isEmpty();
     }
     
     public Location spawnBossEgg(String bossName,String sSpawnPointName)
     {
         return spawnBossEgg(this.bossTemplates.get(bossName), sSpawnPointName);
     }  
     
     public boolean spawnBossEgg(String sBossName,Block block)
     { 
         return spawnBossEgg(this.bossTemplates.get(sBossName), block);
     }
 
     public Location spawnBossEgg(BossTemplate bossTemplate,String sSpawnPointName)
     {
         Random random = new Random();
         
         BossEggSpawnPoint spawnPoint = this.spawnPoints.get(sSpawnPointName);
         
         for(int i=0;i<10;i++)
         {
             Location lSpawnPoint = spawnPoint.location.clone().add(
                 random.nextInt(spawnPoint.radius*2)-spawnPoint.radius,
                 0,
                 random.nextInt(spawnPoint.radius*2)-spawnPoint.radius);
 
             Block block = lSpawnPoint.getBlock();
 
             World world = lSpawnPoint.getWorld();
             int mapHeight = world.getMaxHeight();
             int x = lSpawnPoint.getBlockX();
             int y = lSpawnPoint.getBlockY();
             int z = lSpawnPoint.getBlockZ();
 
             for(int j=0;j<spawnPoint.radius && j<mapHeight;j++)
             {
                 if(y+j+1 < mapHeight)
                 {
                     Block up1 = world.getBlockAt(x,y+j,z);
                     Block up2 = world.getBlockAt(x,y+j+1,z);
                     
                     if(up1 != null && up1.getType() != Material.AIR
                     && up2 != null && up2.getType() == Material.AIR
                     && !this.bossEggs.containsKey(up2.getLocation()))
                     {
                         if(this.spawnBossEgg(bossTemplate, up2))
                         {
                             return up2.getLocation();
                         }
                     }
                 }
 
                 if(y-j-1 > 0)
                 {
                     Block down1 = world.getBlockAt(x,y-j,z);
                     Block down2 = world.getBlockAt(x,y-j-1,z);
 
                     if(down1 != null && down1.getType() != Material.AIR
                     && down2 != null && down2.getType() == Material.AIR
                     && !this.bossEggs.containsKey(down1.getLocation()))
                     {
                         if(this.spawnBossEgg(bossTemplate, down1))
                         {
                             return down1.getLocation();
                         }
                     }
                 }
             }
         }
         
         return null;
     }
     
     public boolean spawnBossEgg(BossTemplate bossTemplate,Block block)
     { 
         if(!this.bossEggs.containsKey(block.getLocation()))
         {
             block.getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
 
             block.setType(Material.DRAGON_EGG);
 
             this.bossEggs.put(block.getLocation(), bossTemplate.name);
 
             this.saveManager.save();
             
             return true;
         }
         
         return false;
     }
     
     public Location spawnRandomBossEgg()
     {
         Random r = new Random();
 
         Object[] values = this.spawnPoints.keySet().toArray();
         String sRandomSpawnPointName = (String) values[r.nextInt(values.length)];
 
         Object[] bosses = this.bossTemplates.values().toArray();
         BossTemplate bossTemplate = (BossTemplate) bosses[r.nextInt(bosses.length)];
 
         Location lSpawned = spawnBossEgg(bossTemplate, sRandomSpawnPointName);
 
         if(lSpawned == null)
         {
             plugin.getServer().broadcast("-------------- RareItemHunter ----------------","rareitemhunter.admin");
             plugin.getServer().broadcast("Was unable to find a place to spawn a "+bossTemplate.name+" boss egg at spawn point "+sRandomSpawnPointName,"rareitemhunter.admin");
         }
         
         return lSpawned;
     }
 
     public void addSpawnPoint(String name, Location location, int radius)
     {
         this.spawnPoints.put(name, new BossEggSpawnPoint(name,location,radius));
         
         this.saveManager.save();
     }
 
     public boolean isSpawnPoint(String name)
     {
         return this.spawnPoints.containsKey(name);
     }
 
     public void delSpawnPoint(String name)
     {
         this.spawnPoints.remove(name);
         
         this.saveManager.save();
     }
 
     public Iterable<BossEggSpawnPoint> getSpawnPoints()
     {
         return this.spawnPoints.values();
     }
 
     public boolean isValidLocation(String sPointName)
     {
         return this.spawnPoints.containsKey(sPointName);
     }
 
     public boolean isValidBossName(String sBossName)
     {
         return this.bossTemplates.containsKey(sBossName);
     }
 
     public void spawnBoss(String sBossName, String sPointName)
     {
         throw new UnsupportedOperationException("Not yet implemented");
     }
 
     void removeBossEgg(Location lSpawnedEgg)
     {
         if(this.bossEggs.containsKey(lSpawnedEgg))
         {
             Block block = lSpawnedEgg.getBlock();
             Block blockNeneath = block.getRelative(BlockFace.DOWN);
 
             if(block.getType() == Material.DRAGON_EGG)
             {
                 block.setType(Material.AIR);
             }
             if(blockNeneath.getType() == Material.BEDROCK)
             { 
                 blockNeneath.setType(Material.AIR);
             }
 
             this.bossEggs.remove(lSpawnedEgg);
             
             this.saveManager.save();
         }
     }
 
     public Location getNearestBossEggLocation(Location l)
     {
         Location lClosest = null;
         
         World lWorld = l.getWorld();
         
         double dClosest = -1;
         
         double dCurrent;
         
         for(Location lEgg : bossEggs.keySet())
         {
             if(lEgg.getWorld().equals(lWorld))
             {
                 dCurrent = l.distanceSquared(lEgg);
                 
                 if(dClosest == -1)
                 {
                     dClosest = dCurrent;
                     
                     lClosest = lEgg;
                 }
                 else if(dCurrent < dClosest)
                 {
                     dClosest = dCurrent;
                     
                     lClosest = lEgg;
                 }
             }
         }
         
         return lClosest;
     }
     
 // Misc helper methods
     private ItemStack getItemStackFromEquipmentString(String sBossName,String sItem)
     {
         String[] equipValues = sItem.split(" ");
                     
         Material equipMaterial = Material.matchMaterial(equipValues[0]);
 
         if(equipMaterial != null)
         {
             ItemStack is = new ItemStack(equipMaterial);
 
             if(equipValues.length > 1)
             {
                 for(String sEnchantment : equipValues[1].split(","))
                 {
                     String[] enchantmentPair = sEnchantment.split(":");
 
                     Enchantment en = Enchantment.getByName(enchantmentPair[0]);
                     int level = 0;
 
                     try
                     {
                         level = Integer.parseInt(enchantmentPair[1]);
                     }
                     catch(Exception e)
                     {
                         plugin.getLogger().log(Level.WARNING,"'"+enchantmentPair[1]+"' is not a valid enchantment level on boss '"+sBossName+"'. Skipping.");
 
                         return null;
                     }
 
                     if(en == null)
                     {
                         plugin.getLogger().log(Level.WARNING,"'"+enchantmentPair[0]+"' is not a valid enchantment name on boss '"+sBossName+"'. Skipping.");
 
                         return null;
                     }
 
                     is.addEnchantment(en, level);
                 }
             }
 
             return is;
         }
         else
         {
             plugin.getLogger().log(Level.WARNING,"'"+equipValues[0]+"' is not a valid material on boss '"+sBossName+"'. Skipping.");
         }
         
         return null;
     }
 
     public void destroyBoss(Boss boss)
     {
         this.activeBosses.remove(boss.entity.getEntityId());
         
         boss.entity.remove();
     }
 }
