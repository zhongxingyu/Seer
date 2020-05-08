 package com.herocraftonline.dev.heroes.effects;
 
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.Packet41MobEffect;
import net.minecraft.server.Packet42RemoveMobEffect;

 import org.bukkit.Location;
 import org.bukkit.craftbukkit.entity.CraftLivingEntity;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.LivingEntity;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class Effect {
 
     protected final String name;
     protected final Skill skill;
     protected final Heroes plugin;
     protected final Set<EffectType> types = EnumSet.noneOf(EffectType.class);
     protected long applyTime;
     private boolean persistent;
     private Map<MobEffect, Boolean> mobEffects = new HashMap<MobEffect, Boolean>();
 
     public Effect(Skill skill, String name) {
         this.name = name;
         this.skill = skill;
         if (skill != null) {
             this.plugin = skill.plugin;
         } else {
             this.plugin = null;
         }
         this.persistent = false;
     }
 
     public Effect(Skill skill, String name, EffectType... types) {
         this(skill, name);
         if (types != null) {
             for (EffectType type : types) {
                 this.types.add(type);
             }
         }
     }
 
     public void apply(LivingEntity lEntity) {
         this.applyTime = System.currentTimeMillis();
         if (!mobEffects.isEmpty()) {
             EntityLiving eLiving = ((CraftLivingEntity) lEntity).getHandle();
             for (MobEffect mobEffect : mobEffects.keySet()) {
                 eLiving.addEffect(mobEffect);
             }
         }
     }
 
     public void apply(Hero hero) {
         this.applyTime = System.currentTimeMillis();
         if (!mobEffects.isEmpty()) {
             EntityPlayer ePlayer = ((CraftPlayer) hero.getPlayer()).getHandle();
             for (Entry<MobEffect, Boolean> entry : mobEffects.entrySet()) {
                 if (!entry.getValue()) {
                     ePlayer.addEffect(entry.getKey());
                 } else {
                     ePlayer.netServerHandler.sendPacket(new Packet41MobEffect(ePlayer.id, entry.getKey()));
                 }
             }
         }
     }
 
     public void remove(LivingEntity lEntity) {
         if (!mobEffects.isEmpty()) {
             EntityLiving eLiving = ((CraftLivingEntity) lEntity).getHandle();
             for (MobEffect mobEffect : mobEffects.keySet()) {
                eLiving.getEffects().remove(mobEffect);
             }
        }
     }
 
     public void remove(Hero hero) {
         if (!mobEffects.isEmpty()) {
             EntityPlayer ePlayer = ((CraftPlayer) hero.getPlayer()).getHandle();
            Iterator<Entry<MobEffect, Boolean>> iter = mobEffects.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<MobEffect, Boolean> entry = iter.next();
                 if (!entry.getValue()) {
                     ePlayer.getEffects().remove(entry.getKey());
                 }
                ePlayer.netServerHandler.sendPacket(new Packet42RemoveMobEffect(ePlayer.id, entry.getKey()));
             }
         }
     }

     public void broadcast(Location source, String message, Object... args) {
         skill.broadcast(source, message, args);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Effect other = (Effect) obj;
         if (name == null) {
             if (other.name != null) {
                 return false;
             }
         } else if (!name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     /**
      * 
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * 
      * @return the Skill
      */
     public Skill getSkill() {
         return skill;
     }
 
     @Override
     public int hashCode() {
         return name.hashCode();
     }
 
     /**
      * Returns if the effect is persistent or not
      * 
      * @return
      */
     public boolean isPersistent() {
         return persistent;
     }
 
     /**
      * Returns if this Effect is the type specified
      * 
      * @param type
      * @return
      */
     public boolean isType(EffectType type) {
         return types.contains(type);
     }
 
     /*
      * Sets the effects persistence value
      */
     public void setPersistent(boolean persistent) {
         this.persistent = persistent;
     }
 
     public void addMobEffect(int id, int duration, int strength, boolean faked) {
         mobEffects.put(new MobEffect(id, duration, strength), faked);
     }
 
     public void addMobEffect(MobEffect mobEffect, boolean faked) {
         mobEffects.put(mobEffect, faked);
     }
 
     public Map<MobEffect, Boolean> getMobEffects() {
         return Collections.unmodifiableMap(mobEffects);
     }
 }
