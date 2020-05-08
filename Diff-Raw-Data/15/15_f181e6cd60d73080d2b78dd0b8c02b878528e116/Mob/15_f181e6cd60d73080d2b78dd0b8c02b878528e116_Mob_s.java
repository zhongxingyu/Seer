 package com.archmageinc.RandomEncounters;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 /**
  * Represents an instance of a Mob configuration
  * 
  * @author ArchmageInc
  */
 public class Mob{
     
     /**
      * The static set of singlton instances.
      */
     protected static HashSet<Mob> instances =   new HashSet();
     
     /**
      * The unique name of the Mob.
      */
     protected String name;
     
     /**
      * The name each creature will be given.
      */
     protected String tagName;
     
     /**
      * The type of entity spawned by this Mob.
      */
     protected EntityType type;
     
     /**
      * The minimum number of creatures to spawn.
      */
     protected Long min;
     
     /**
      * The maximum number of creatures to spawn.
      */
     protected Long max;
     
     /**
      * The probability of additional creatures.
      */
     protected Double probability;
     
     /**
      * Is this Mob enabled for spawning.
      */
     protected Boolean enabled;
     
     /**
      * The equipment configuration to be placed on spawned creatures.
      */
     protected JSONObject equipment;
     
     /**
      * The mob to spawn when the creature dies.
      */
     protected Mob deathSpawn;
     
     /**
      * The unique name of the death spawn mob.
      */
     protected String deathSpawnName;
     
     /**
      * The set of Treasures which will be dropped by spawned creatures.
      */
     protected Set<Treasure> treasures       =   new HashSet();
     
     /**
      * The set of potion effects that will be paced on spawned creatures.
      */
     protected Set<MobPotionEffect> effects  =   new HashSet();
     
     
     /**
      * Get the instance of the Mob based on JSON configuration
      * 
      * @param jsonConfiguration The JSON configuration of the Mob.
      * @return 
      */
     public static Mob getInstance(JSONObject jsonConfiguration){
         try{
             for(Mob instance : instances){
                 if(instance.getName().equals((String) jsonConfiguration.get("name"))){
                     return instance;
                 }
             }
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid Mob configuration: "+e.getMessage());
         }
         return new Mob(jsonConfiguration);
     }
     
     
     /**
      * Get the instance of the Mob based on the unique name
      * @param name The unique name of the Mob.
      * @return Returns the Mob if found, null otherwise.
      */
     public static Mob getInstance(String name){
         for(Mob instance : instances){
             if(instance.getName().equals(name)){
                 return instance;
             }
         }
         return null;
     }
     
     /**
      * Constructor for the Mob based on JSON configuration.
      * @param jsonConfiguration 
      */
     protected Mob(JSONObject jsonConfiguration){
         try{
             name                        =   (String) jsonConfiguration.get("name");
             type                        =   EntityType.fromName((String) jsonConfiguration.get("type"));
             min                         =   ((Number) jsonConfiguration.get("min")).longValue();
             max                         =   ((Number) jsonConfiguration.get("max")).longValue();
             enabled                     =   (Boolean) jsonConfiguration.get("enabled");
             probability                 =   ((Number) jsonConfiguration.get("probability")).doubleValue();
             equipment                   =   (JSONObject) jsonConfiguration.get("equipment");
             tagName                     =   (String) jsonConfiguration.get("tagName");
             deathSpawnName              =   (String) jsonConfiguration.get("deathSpawn");
             JSONArray jsonTreasures     =   (JSONArray) jsonConfiguration.get("treasures");
             JSONArray jsonEffects       =   (JSONArray) jsonConfiguration.get("potionEffects");
             if(jsonTreasures!=null){
                 for(int i=0;i<jsonTreasures.size();i++){
                     treasures.add(new Treasure((JSONObject) jsonTreasures.get(i)));
                 }
             }
             
             if(jsonEffects!=null){
                 for(int i=0;i<jsonEffects.size();i++){
                     effects.add(new MobPotionEffect((JSONObject) jsonEffects.get(i)));
                 }
             }
             if(type==null){
                 RandomEncounters.getInstance().logError("Invalid mob type: "+(String) jsonConfiguration.get("type"));
             }
             instances.add(this);
         }catch(ClassCastException e){
             RandomEncounters.getInstance().logError("Invalid Mob configuration: "+e.getMessage());
         }
     }
     
     /**
      * Spawn the creatures and create a PlacedMob at a given location for a given PlacedEncounter.
      * @param encounter The PlacedEncounter that the creature will belong to.
      * @param location The location to spawn the creature.
      * @return Returns the newly created PlacedMob
      */
     public PlacedMob placeMob(PlacedEncounter encounter,Location location){
         return PlacedMob.create(this,encounter, location);
         
     }
     
     /**
      * Gets an Equipment item based on the equipment type name.
      * 
      * @param type The type name of the equipment
      * @return Returns the equipment item if found, null otherwise.
      * @see org.bukkit.inventory.EntityEquipment
      */
     protected ItemStack getEquipmentItem(String type){
         ItemStack item      =   null;
         try{
             if(equipment!=null){
                 JSONObject object   =   (JSONObject) equipment.get(type);
                 if(object!=null){
                     Treasure treasure  =   new Treasure(object);
                     item               =   treasure.getOne();
                 }
             }
             
         }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid mob equipment configuration for "+name+" "+type);
         }
         return item;
     }
     
     /**
      * Gets the drop probability of the equipment based on the type.
      * 
      * @param type The name of the equipment type
      * @return Returns the drop probability of the equipment.
      * @see org.bukkit.inventory.EntityEquipment
      */
     protected Float getDropProbability(String type){
         Float chance    =   ((Integer) 0).floatValue();
         try{
             if(equipment!=null){
                JSONObject object   =   (JSONObject) equipment.get(type);
                 if(object!=null){
                    chance  =   ((Long) object.get("dropProbability")).floatValue();
                 } 
             }
         }catch(ClassCastException e){
            RandomEncounters.getInstance().logError("Invalid mob equipment configuration for "+name+" "+type);
         }
         return chance;
     }
     
     /**
      * Sets the equipment based on this configuration for the given LivingEntity
      * 
      * @param entity The entity on which to place the equipment.
      * @see org.bukkit.inventory.EntityEquipment
      */
     public void setEquipment(LivingEntity entity){
         ItemStack hand      =   getEquipmentItem("hand");
         if(hand!=null){
              entity.getEquipment().setItemInHand(hand);
              entity.getEquipment().setItemInHandDropChance(getDropProbability("hand"));
         }
         ItemStack helmet    =   getEquipmentItem("helmet");
         if(helmet!=null){
              entity.getEquipment().setHelmet(helmet);
              entity.getEquipment().setHelmetDropChance(getDropProbability("helmet"));
         }
         ItemStack chestplate    =   getEquipmentItem("chestplate");
         if(chestplate!=null){
              entity.getEquipment().setChestplate(chestplate);
              entity.getEquipment().setChestplateDropChance(getDropProbability("chestplate"));
         }
         ItemStack leggings    =   getEquipmentItem("leggings");
         if(leggings!=null){
              entity.getEquipment().setLeggings(leggings);
              entity.getEquipment().setLeggingsDropChance(getDropProbability("leggings"));
         }
         ItemStack boots    =   getEquipmentItem("boots");
         if(boots!=null){
              entity.getEquipment().setBoots(boots);
              entity.getEquipment().setBootsDropChance(getDropProbability("boots"));
         }
         
     }
     
     /**
      * Sets the potion effects based on this configuration for the given entity.
      * 
      * @param entity The LivingEntity on which to place the effects.
      */
     public void setEffects(LivingEntity entity){
         for(MobPotionEffect effect : effects){
             effect.checkApply(entity);
         }
     }
     
     /**
      * Get the randomly generated number of creatures to place
      * 
      * @return 
      */
     public Long getCount(){
         Long count   =   min;
         for(int i=min.intValue();i<max;i++){
             if(Math.random()<probability)
                 count++;
         }
         return count;
     }
     
     /**
      * Get the randomly generated list of items to drop upon the creatures death.
      * 
      * @return 
      */
     public List<ItemStack> getDrop(){
         List<ItemStack> list    =   new ArrayList();
         for(Treasure treasure : treasures){
             List<ItemStack> loot    =   treasure.get();
             if(!loot.isEmpty()){
                 list.addAll(loot);
             }
         }
         return list;
     }
     
     /**
      * Return the unique name of this Mob configuration
      * 
      * @return 
      */
     public String getName(){
         return name;
     }
     
     /**
      * Get the EntityType associated with this Mob Configuration.
      * @return 
      */
     public EntityType getType(){
         return type;
     }
     
     /**
      * Get the tagName for the creature.
      * @return 
      */
     public String getTagName(){
         return tagName;
     }
     
     public Mob getDeathSpawn(){
         if(deathSpawn==null && deathSpawnName!=null){
             deathSpawn  =   Mob.getInstance(deathSpawnName);
         }
         return deathSpawn;
     }
 }
