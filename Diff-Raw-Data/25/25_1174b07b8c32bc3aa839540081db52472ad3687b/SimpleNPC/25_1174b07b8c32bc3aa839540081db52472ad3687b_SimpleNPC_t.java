 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.crimsonrpg.personas.personas.npc;
 
 import com.crimsonrpg.personas.personas.PersonasPlugin;
 import com.crimsonrpg.personas.personasapi.Personas;
 import com.crimsonrpg.personas.personasapi.npc.NPC;
 import com.crimsonrpg.personas.personasapi.npc.Trait;
 import com.crimsonrpg.personas.personasapi.persona.Persona;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionAttachment;
 import org.bukkit.permissions.PermissionAttachmentInfo;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 import org.martin.bukkit.npclib.NPCEntity;
 
 /**
  * Represents a simple NPC.
  */
 public class SimpleNPC implements NPC {
     private String id;
     private String name;
     private Map<Class, Trait> traits = new HashMap<Class, Trait>();
     private Persona persona;
     
     private NPCEntity handle;
     private HumanEntity bukkitHandle;
 
     public SimpleNPC(String id, String name, List<Trait> traits, Persona persona, NPCEntity handle) {
         this.id = id;
         this.name = name;
         this.persona = persona;
         this.handle = handle;
         
         //Add in the traits
         for (Trait trait : traits) {
             this.traits.put(trait.getClass(), trait);
         }
         
         //Add the bukkit handle
         bukkitHandle = (HumanEntity) handle.getBukkitEntity();
     }
     
     public String getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public <T extends Trait> T getTrait(Class<T> type) {
         Trait trait = traits.get(type);
         if (trait != null) {
             return type.cast(trait);
         }
         
         //Creates a default trait if it is not found
         try {
             T theTrait = type.newInstance();
             theTrait.reset();
             this.setTrait(theTrait);
             return theTrait;
         } catch (InstantiationException ex) {
             PersonasPlugin.LOGGER.severe("[Personas] Could not instantiate a new Trait for '" + type.getName() + "' in the NPC " + id + ".");
         } catch (IllegalAccessException ex) {
             PersonasPlugin.LOGGER.severe("[Personas] Could not access a new Trait for '" + type.getName() + "' in the NPC " + id + ".");
         }
         
         return null;
     }
     
     public void setTrait(Trait trait) {
         traits.put(trait.getClass(), trait);
     }
     
     public Persona getPersona() {
        return persona;
     }
 
     public void despawn() {
        //Bye bye
        Personas.getNPCManager().despawnNPC(id);
     }
     
     /////////////////////////////////////
     // BUKKIT STUFF IS BELOW HERE
    // DO NOT MESS WITH IT UNLESS BUKKIT CHANGES
     /////////////////////////////////////
     
     public void setOp(boolean bln) {
         bukkitHandle.setOp(bln);
     }
 
     public boolean isOp() {
         return bukkitHandle.isOp();
     }
 
     public boolean teleport(Entity entity) {
         return bukkitHandle.teleport(entity);
     }
 
     public boolean teleport(Location lctn) {
         return bukkitHandle.teleport(lctn);
     }
 
     public void setVelocity(Vector vector) {
         bukkitHandle.setVelocity(vector);
     }
 
     public void setTicksLived(int i) {
         bukkitHandle.setTicksLived(i);
     }
 
     public boolean setPassenger(Entity entity) {
         return bukkitHandle.setPassenger(entity);
     }
 
     public void setLastDamageCause(EntityDamageEvent ede) {
         bukkitHandle.setLastDamageCause(ede);
     }
 
     public void setFireTicks(int i) {
         bukkitHandle.setFireTicks(i);
     }
 
     public void setFallDistance(float f) {
         bukkitHandle.setFallDistance(f);
     }
 
     public void remove() {
         bukkitHandle.remove();
     }
 
     public boolean isEmpty() {
         return bukkitHandle.isEmpty();
     }
 
     public boolean isDead() {
         return bukkitHandle.isDead();
     }
 
     public World getWorld() {
         return bukkitHandle.getWorld();
     }
 
     public Vector getVelocity() {
         return bukkitHandle.getVelocity();
     }
 
     public UUID getUniqueId() {
         return bukkitHandle.getUniqueId();
     }
 
     public int getTicksLived() {
         return bukkitHandle.getTicksLived();
     }
 
     public Server getServer() {
         return bukkitHandle.getServer();
     }
 
     public Entity getPassenger() {
         return bukkitHandle.getPassenger();
     }
 
     public List<Entity> getNearbyEntities(double d, double d1, double d2) {
         return bukkitHandle.getNearbyEntities(d, d1, d2);
     }
 
     public int getMaxFireTicks() {
         return bukkitHandle.getMaxFireTicks();
     }
 
     public Location getLocation() {
         return bukkitHandle.getLocation();
     }
 
     public EntityDamageEvent getLastDamageCause() {
         return bukkitHandle.getLastDamageCause();
     }
 
     public int getFireTicks() {
         return bukkitHandle.getFireTicks();
     }
 
     public float getFallDistance() {
         return bukkitHandle.getFallDistance();
     }
 
     public int getEntityId() {
         return bukkitHandle.getEntityId();
     }
 
     public boolean eject() {
         return bukkitHandle.eject();
     }
 
     public void removeAttachment(PermissionAttachment pa) {
         bukkitHandle.removeAttachment(pa);
     }
 
     public void recalculatePermissions() {
         bukkitHandle.recalculatePermissions();
     }
 
     public boolean isPermissionSet(Permission prmsn) {
         return bukkitHandle.isPermissionSet(prmsn);
     }
 
     public boolean isPermissionSet(String string) {
         return bukkitHandle.isPermissionSet(string);
     }
 
     public boolean hasPermission(Permission prmsn) {
         return bukkitHandle.hasPermission(prmsn);
     }
 
     public boolean hasPermission(String string) {
         return bukkitHandle.hasPermission(string);
     }
 
     public Set<PermissionAttachmentInfo> getEffectivePermissions() {
         return bukkitHandle.getEffectivePermissions();
     }
 
     public PermissionAttachment addAttachment(Plugin plugin, int i) {
         return bukkitHandle.addAttachment(plugin, i);
     }
 
     public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln, int i) {
         return bukkitHandle.addAttachment(plugin, string, bln, i);
     }
 
     public PermissionAttachment addAttachment(Plugin plugin) {
         return bukkitHandle.addAttachment(plugin);
     }
 
     public PermissionAttachment addAttachment(Plugin plugin, String string, boolean bln) {
         return bukkitHandle.addAttachment(plugin, string, bln);
     }
 
     public Snowball throwSnowball() {
         return bukkitHandle.throwSnowball();
     }
 
     public Egg throwEgg() {
         return bukkitHandle.throwEgg();
     }
 
     public Arrow shootArrow() {
         return bukkitHandle.shootArrow();
     }
 
     public void setRemainingAir(int i) {
         bukkitHandle.setRemainingAir(i);
     }
 
     public void setNoDamageTicks(int i) {
         bukkitHandle.setNoDamageTicks(i);
     }
 
     public void setMaximumNoDamageTicks(int i) {
         bukkitHandle.setMaximumNoDamageTicks(i);
     }
 
     public void setMaximumAir(int i) {
         bukkitHandle.setMaximumAir(i);
     }
 
     public void setLastDamage(int i) {
         bukkitHandle.setLastDamage(i);
     }
 
     public void setHealth(int i) {
         bukkitHandle.setHealth(i);
     }
 
     public boolean leaveVehicle() {
         return bukkitHandle.leaveVehicle();
     }
 
     public boolean isInsideVehicle() {
         return bukkitHandle.isInsideVehicle();
     }
 
     public Vehicle getVehicle() {
         return bukkitHandle.getVehicle();
     }
 
     public Block getTargetBlock(HashSet<Byte> hs, int i) {
         return bukkitHandle.getTargetBlock(hs, i);
     }
 
     public int getRemainingAir() {
         return bukkitHandle.getRemainingAir();
     }
 
     public int getNoDamageTicks() {
         return bukkitHandle.getNoDamageTicks();
     }
 
     public int getMaximumNoDamageTicks() {
         return bukkitHandle.getMaximumNoDamageTicks();
     }
 
     public int getMaximumAir() {
         return bukkitHandle.getMaximumAir();
     }
 
     public List<Block> getLineOfSight(HashSet<Byte> hs, int i) {
         return bukkitHandle.getLineOfSight(hs, i);
     }
 
     public List<Block> getLastTwoTargetBlocks(HashSet<Byte> hs, int i) {
         return bukkitHandle.getLastTwoTargetBlocks(hs, i);
     }
 
     public int getLastDamage() {
         return bukkitHandle.getLastDamage();
     }
 
     public int getHealth() {
         return bukkitHandle.getHealth();
     }
 
     public Location getEyeLocation() {
         return bukkitHandle.getEyeLocation();
     }
 
     public double getEyeHeight(boolean bln) {
         return bukkitHandle.getEyeHeight(bln);
     }
 
     public double getEyeHeight() {
         return bukkitHandle.getEyeHeight();
     }
 
     public void damage(int i, Entity entity) {
         bukkitHandle.damage(i, entity);
     }
 
     public void damage(int i) {
         bukkitHandle.damage(i);
     }
 
     public void setItemInHand(ItemStack is) {
         bukkitHandle.setItemInHand(is);
     }
 
     public void setGameMode(GameMode gm) {
         bukkitHandle.setGameMode(gm);
     }
 
     public boolean isSleeping() {
         return bukkitHandle.isSleeping();
     }
 
     public int getSleepTicks() {
         return bukkitHandle.getSleepTicks();
     }
 
     public ItemStack getItemInHand() {
         return bukkitHandle.getItemInHand();
     }
 
     public PlayerInventory getInventory() {
         return bukkitHandle.getInventory();
     }
 
     public GameMode getGameMode() {
         return bukkitHandle.getGameMode();
     }
 }
