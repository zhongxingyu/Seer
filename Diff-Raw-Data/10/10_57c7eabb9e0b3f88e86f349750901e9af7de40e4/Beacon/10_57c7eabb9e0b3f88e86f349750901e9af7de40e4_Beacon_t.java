 /*
  * This file is part of MyPet
  *
  * Copyright (C) 2011-2013 Keyle
  * MyPet is licensed under the GNU Lesser General Public License.
  *
  * MyPet is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyPet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.Keyle.MyPet.skill.skills.implementation;
 
 import de.Keyle.MyPet.entity.types.MyPet;
 import de.Keyle.MyPet.entity.types.MyPet.PetState;
 import de.Keyle.MyPet.skill.skills.ISkillActive;
 import de.Keyle.MyPet.skill.skills.ISkillStorage;
 import de.Keyle.MyPet.skill.skills.implementation.beacon.ContainerBeacon;
 import de.Keyle.MyPet.skill.skills.implementation.beacon.MyPetCustomBeaconInventory;
 import de.Keyle.MyPet.skill.skills.implementation.beacon.TileEntityBeacon;
 import de.Keyle.MyPet.skill.skills.implementation.inventory.ItemStackNBTConverter;
 import de.Keyle.MyPet.skill.skills.info.BeaconInfo;
 import de.Keyle.MyPet.skill.skills.info.ISkillInfo;
 import de.Keyle.MyPet.util.BukkitUtil;
 import de.Keyle.MyPet.util.IScheduler;
 import de.Keyle.MyPet.util.Util;
 import de.Keyle.MyPet.util.locale.Locales;
 import net.minecraft.server.v1_6_R2.*;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
 import org.bukkit.craftbukkit.v1_6_R2.event.CraftEventFactory;
 import org.bukkit.entity.Player;
 import org.spout.nbt.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Beacon extends BeaconInfo implements ISkillInstance, IScheduler, ISkillStorage, ISkillActive
 {
     public static int HUNGER_DECREASE_TIME = 60;
 
     private TileEntityBeacon tileEntityBeacon;
 
     private static Map<Integer, String> buffNames = new HashMap<Integer, String>();
     private Map<Integer, Boolean> primaryActive = new HashMap<Integer, Boolean>();
     private int[] primaryBuffs = {0, 1, 3, 11, 8, 5};
     private Map<Integer, Boolean> secundaryActive = new HashMap<Integer, Boolean>();
     private int[] secundaryBuffs = {0, 1, 3, 11, 8, 5, 10};
     private int primaryEffectId = 0;
     private int secondaryEffectId = 0;
     private int hungerDecreaseTimer;
     private boolean active = false;
     private int level = 0;
     private MyPetCustomBeaconInventory beaconInv;
     public ItemStack tributeItem;
     private MyPet myPet;
 
     static
     {
         buffNames.put(1, "Speed");
         buffNames.put(3, "Haste");
         buffNames.put(5, "Strength");
         buffNames.put(8, "JumpBoost");
         buffNames.put(10, "Regeneration");
         buffNames.put(11, "Resistance");
     }
 
     public Beacon(boolean addedByInheritance)
     {
         super(addedByInheritance);
         beaconInv = new MyPetCustomBeaconInventory(this);
         primaryActive.put(1, false);
         primaryActive.put(3, false);
         primaryActive.put(5, false);
         primaryActive.put(8, false);
         primaryActive.put(11, false);
         secundaryActive.put(1, false);
         secundaryActive.put(3, false);
         secundaryActive.put(5, false);
         secundaryActive.put(8, false);
         secundaryActive.put(10, false);
         secundaryActive.put(11, false);
     }
 
     public void setMyPet(MyPet myPet)
     {
         this.myPet = myPet;
     }
 
     public MyPet getMyPet()
     {
         return myPet;
     }
 
     public boolean isActive()
     {
         return level > 0;
     }
 
     public int getLevel()
     {
         return level;
     }
 
     public ItemStack getTributeItem()
     {
         return tributeItem;
     }
 
     public void setTributeItem(ItemStack itemStack)
     {
         beaconInv.setItem(0, itemStack);
     }
 
     public void upgrade(ISkillInfo upgrade, boolean quiet)
     {
         if (upgrade instanceof BeaconInfo)
         {
             level = 4;
             for (int primaryBuffId : primaryBuffs)
             {
                 if (upgrade.getProperties().getValue().containsKey("1_" + primaryBuffId))
                 {
                     boolean active = ((ByteTag) upgrade.getProperties().getValue().get("1_" + primaryBuffId)).getBooleanValue();
                     primaryActive.put(primaryBuffId, active);
                 }
             }
 
             for (int secundaryBuffId : secundaryBuffs)
             {
                 if (upgrade.getProperties().getValue().containsKey("2_" + secundaryBuffId))
                 {
                     if (secundaryBuffId == 10)
                     {
                         boolean active = ((ByteTag) upgrade.getProperties().getValue().get("2_" + secundaryBuffId)).getBooleanValue();
                         secundaryActive.put(secundaryBuffId, active);
                     }
                     else
                     {
                         if (primaryActive.get(secundaryBuffId))
                         {
                             boolean active = ((ByteTag) upgrade.getProperties().getValue().get("2_" + secundaryBuffId)).getBooleanValue();
                             secundaryActive.put(secundaryBuffId, active);
                         }
                         else
                         {
                             secundaryActive.put(secundaryBuffId, false);
                         }
                     }
                 }
             }
             if (upgrade.getProperties().getValue().containsKey("duration"))
             {
                 if (!upgrade.getProperties().getValue().containsKey("addset_duration") || ((StringTag) upgrade.getProperties().getValue().get("addset_duration")).getValue().equals("add"))
                 {
                     duration += ((IntTag) upgrade.getProperties().getValue().get("duration")).getValue();
                 }
                 else
                 {
                     duration = ((IntTag) upgrade.getProperties().getValue().get("duration")).getValue();
                 }
             }
             if (upgrade.getProperties().getValue().containsKey("range"))
             {
                 if (!upgrade.getProperties().getValue().containsKey("addset_range") || ((StringTag) upgrade.getProperties().getValue().get("addset_range")).getValue().equals("add"))
                 {
                     range += ((DoubleTag) upgrade.getProperties().getValue().get("range")).getValue();
                 }
                 else
                 {
                     range = ((DoubleTag) upgrade.getProperties().getValue().get("range")).getValue();
                 }
             }
             if (!quiet)
             {
                 myPet.sendMessageToOwner(Util.formatText(Locales.getString("Message.Skill.Beacon.Upgrade", myPet.getOwner().getLanguage()), myPet.getPetName(), String.format("%1.2f", range), duration));
                 myPet.sendMessageToOwner("  " + getFormattedValue());
             }
         }
     }
 
     public String getFormattedValue()
     {
         String availableBuffs = "";
         for (int primaryBuffId : primaryBuffs)
         {
             if (primaryBuffId != 0 && primaryActive.get(primaryBuffId))
             {
                 if (!availableBuffs.equalsIgnoreCase(""))
                 {
                     availableBuffs += ", ";
                 }
                 availableBuffs += ChatColor.GOLD + Locales.getString("Name." + buffNames.get(primaryBuffId), myPet.getOwner().getLanguage());
                 if (secundaryActive.get(primaryBuffId))
                 {
                     availableBuffs += " (II)";
                 }
                 availableBuffs += ChatColor.RESET;
             }
         }
         if (secundaryActive.get(10))
         {
             if (!availableBuffs.equalsIgnoreCase(""))
             {
                 availableBuffs += ", ";
             }
             availableBuffs += ChatColor.GOLD + Locales.getString("Name." + buffNames.get(10), myPet.getOwner().getLanguage()) + ChatColor.RESET;
         }
         return availableBuffs;
     }
 
     public void reset()
     {
         stop(true);
     }
 
     public boolean activate()
     {
         if (level > 0)
         {
             if (myPet.getOwner().isInExternalGames())
             {
                 myPet.sendMessageToOwner(Locales.getString("Message.No.AllowedHere", myPet.getOwner()).replace("%petname%", myPet.getPetName()));
                 return false;
             }
             openBeacon(myPet.getOwner().getPlayer());
             return true;
         }
         else
         {
             myPet.sendMessageToOwner(Util.formatText(Locales.getString("Message.No.Skill", myPet.getOwner().getLanguage()), myPet.getPetName(), this.getName()));
             return false;
         }
     }
 
     public void activate(Player player)
     {
         if (level > 0)
         {
             openBeacon(player);
         }
         else
         {
             player.sendMessage(Util.formatText(Locales.getString("Message.No.Skill", myPet.getOwner().getLanguage()), myPet.getPetName(), this.getName()));
         }
     }
 
     public boolean activate(boolean primary, int effectId)
     {
         if (level > 0)
         {
             if (primary)
             {
                 if (!primaryActive.containsKey(effectId))
                 {
                     return false;
                 }
                 else if (primaryActive.get(effectId))
                 {
                     setPrimaryEffectId(effectId);
                     return true;
                 }
                 else
                 {
                     myPet.sendMessageToOwner(Util.formatText(Locales.getString("Message.Skill.Beacon.BuffNotActive", myPet.getOwner().getLanguage()), Locales.getString("Name." + buffNames.get(effectId), myPet.getOwner().getLanguage())));
                     return false;
                 }
             }
             else
             {
                 if (!secundaryActive.containsKey(effectId))
                 {
                     return false;
                 }
                 else if (secundaryActive.get(effectId))
                 {
                     setSecondaryEffectId(effectId);
                     return true;
                 }
                 else
                 {
                     if (effectId != 10)
                     {
                         myPet.sendMessageToOwner(Util.formatText(Locales.getString("Message.Skill.Beacon.ImprovedBuffNotActive", myPet.getOwner().getLanguage()), Locales.getString("Name." + buffNames.get(effectId), myPet.getOwner().getLanguage())));
                     }
                     else
                     {
                         myPet.sendMessageToOwner(Util.formatText(Locales.getString("Message.Skill.Beacon.BuffNotActive", myPet.getOwner().getLanguage()), Locales.getString("Name." + buffNames.get(effectId), myPet.getOwner().getLanguage())));
                     }
                     return false;
                 }
             }
         }
         else
         {
             myPet.sendMessageToOwner(Util.formatText(Locales.getString("Message.No.Skill", myPet.getOwner().getLanguage()), myPet.getPetName(), this.getName()));
         }
         return false;
     }
 
     public void schedule()
     {
         if (myPet.getStatus() == PetState.Here && level > 0 && this.active && this.primaryEffectId > 0)
         {
             byte amplification = 0;
 
             if (this.primaryEffectId == this.secondaryEffectId)
             {
                 amplification = 1;
             }
             double range = this.range * myPet.getHungerValue() / 100.;
 
             BukkitUtil.playParticleEffect(myPet.getLocation().add(0, 1, 0), "witchMagic", 0.2F, 0.2F, 0.2F, 0.1F, 5, 20);
 
             for (Object entityObj : this.myPet.getCraftPet().getHandle().world.a(EntityHuman.class, myPet.getCraftPet().getHandle().boundingBox.grow(range, range, range)))
             {
                 EntityHuman entityHuman = (EntityHuman) entityObj;
 
                 if (!entityHuman.getBukkitEntity().equals(Bukkit.getPlayer(entityHuman.getName())))
                 {
                     continue;
                 }
 
                 entityHuman.addEffect(new MobEffect(this.primaryEffectId, duration * 20, amplification, true));
 
                 if (level > 3 && this.primaryEffectId != this.secondaryEffectId && this.secondaryEffectId > 0)
                 {
                     BukkitUtil.playParticleEffect(entityHuman.getBukkitEntity().getLocation().add(0, 1, 0), "instantSpell", 0.2F, 0.2F, 0.2F, 0.1F, 5, 20);
                     entityHuman.addEffect(new MobEffect(this.secondaryEffectId, duration * 20, 0, true));
                 }
             }
 
             if (HUNGER_DECREASE_TIME > 0 && hungerDecreaseTimer-- < 0)
             {
                 myPet.setHungerValue(myPet.getHungerValue() - 1);
                 hungerDecreaseTimer = HUNGER_DECREASE_TIME;
             }
         }
     }
 
     public void load(CompoundTag compound)
     {
         if (compound.getValue().containsKey("Primary"))
         {
             this.primaryEffectId = ((IntTag) compound.getValue().get("Primary")).getValue();
         }
         if (compound.getValue().containsKey("Secondary"))
         {
             this.secondaryEffectId = ((IntTag) compound.getValue().get("Secondary")).getValue();
         }
         if (compound.getValue().containsKey("Active"))
         {
             this.active = ((ByteTag) compound.getValue().get("Active")).getBooleanValue();
         }
         if (compound.getValue().containsKey("Item"))
         {
             setTributeItem(ItemStackNBTConverter.CompundToItemStack((CompoundTag) compound.getValue().get("Item")));
         }
     }
 
     public CompoundTag save()
     {
 
         CompoundTag nbtTagCompound = new CompoundTag(getName(), new CompoundMap());
         nbtTagCompound.getValue().put("Primary", new IntTag("Primary", this.primaryEffectId));
         nbtTagCompound.getValue().put("Secondary", new IntTag("Secondary", this.secondaryEffectId));
         nbtTagCompound.getValue().put("Active", new ByteTag("Active", this.active));
         if (tributeItem != null)
         {
             nbtTagCompound.getValue().put("Item", ItemStackNBTConverter.ItemStackToCompund(tributeItem));
         }
         return nbtTagCompound;
     }
 
     public void openBeacon(Player p)
     {
         EntityPlayer entityPlayer = ((CraftPlayer) p).getHandle();
 
         if (tileEntityBeacon == null)
         {
             tileEntityBeacon = new TileEntityBeacon(this);
         }
         Container container = CraftEventFactory.callInventoryOpenEvent(entityPlayer, new ContainerBeacon(entityPlayer.inventory, beaconInv, tileEntityBeacon, this));
         if (container == null)
         {
             return;
         }
 
         int containerCounter = entityPlayer.nextContainerCounter();
        entityPlayer.playerConnection.sendPacket(new Packet100OpenWindow(containerCounter, 7, "MyPet Beacon", beaconInv.getSize(), true));
         entityPlayer.activeContainer = container;
         entityPlayer.activeContainer.windowId = containerCounter;
         entityPlayer.activeContainer.addSlotListener(entityPlayer);
     }
 
 
     public int getPrimaryEffectId()
     {
         return primaryEffectId;
     }
 
     public void setPrimaryEffectId(int effectId)
     {
         if (effectId > 0)
         {
             this.primaryEffectId = effectId;
             active = true;
             hungerDecreaseTimer = HUNGER_DECREASE_TIME;
         }
         else
         {
             this.primaryEffectId = 0;
             if (secondaryEffectId == 0)
             {
                 active = false;
             }
         }
     }
 
     public int getSecondaryEffectId()
     {
         return secondaryEffectId;
     }
 
     public void setSecondaryEffectId(int effectId)
     {
         if (effectId > 0)
         {
             this.secondaryEffectId = effectId;
             active = true;
             hungerDecreaseTimer = HUNGER_DECREASE_TIME;
         }
         else
         {
             this.secondaryEffectId = 0;
             if (primaryEffectId == 0)
             {
                 active = false;
             }
         }
     }
 
     public void stop(boolean reset)
     {
         this.active = false;
         if (reset)
         {
             primaryEffectId = 0;
             secondaryEffectId = 0;
         }
     }
 
     @Override
     public ISkillInstance cloneSkill()
     {
         Beacon newSkill = new Beacon(this.isAddedByInheritance());
         newSkill.setProperties(getProperties());
         return newSkill;
     }
 }
