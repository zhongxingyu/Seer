 package assets.fyresmodjam;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import cpw.mods.fml.common.ICraftingHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import assets.fyresmodjam.EntityStatHelper.EntityStat;
 import assets.fyresmodjam.ItemStatHelper.ItemStatTracker;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.EnumEntitySize;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.boss.EntityDragon;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.nbt.NBTTagString;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.network.packet.Packet5PlayerInventory;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldServer;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 import net.minecraftforge.event.entity.living.LivingDeathEvent;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
 
 public class EntityStatHelper {
 	
 	//There's probably a better way of doing all of this. :P Oh well.
 	
 	public static class EntityStatTracker {
 		public Class[] classes;
 		
 		public boolean instanceAllowed = false;
 		
 		public EntityStatTracker(Class[] classes, boolean instancesAllowed) {this.classes = classes; this.instanceAllowed = instancesAllowed;}
 		public EntityStatTracker(Class c, boolean instancesAllowed) {this(new Class[] {c}, instancesAllowed);}
 
 		//public HashMap<String, String> stats = new HashMap<String, String>();
 		public ArrayList<EntityStat> stats = new ArrayList<EntityStat>();
 		//public StatTracker giveStat(String name, String value) {stats.put(name, value); return this;}
 
 		public void addStat(EntityStat stat) {
 			if(!stats.contains(stat)) {stats.add(stat);}
 		}
 	}
 	
 	public static class EntityStat {
 		public String name;
 		public String value;
 		
 		public EntityStat(String name, String value) {
 			this.name = name;
 			this.value = value;
 		}
 		
 		public Object getNewValue(Random r) {return value;}
 		public String getAlteredEntityName(EntityLiving entity) {return entity.getEntityName();}
 		public void modifyEntity(Entity entity) {}
 	}
 	
 	public static HashMap<Class, EntityStatTracker> statTrackersByClass = new HashMap<Class, EntityStatTracker>();
 	public static ArrayList<EntityStatTracker> genericTrackers = new ArrayList<EntityStatTracker>();
 	
 	public static void addStatTracker(EntityStatTracker statTracker) {
 		if(statTracker.classes != null) {
 			for(Class c : statTracker.classes) {statTrackersByClass.put(c, statTracker);}
 			if(statTracker.instanceAllowed) {genericTrackers.add(statTracker);}
 		}
 	}
 	
 	public static Entity giveStat(Entity entity, String name, Object value) {
 		if(entity != null && name != null && value != null) {entity.getEntityData().setString(name, value.toString());}
 		return entity;
 	}
 	
 	public static Entity setName(EntityLiving entity, String name) {
 		entity.setCustomNameTag(name);
 		//if(entity.getCustomNameTag().equals(entity.getEntityName())) {entity.setCustomNameTag("");}
 		return entity;
 	}
 	
 	public static String getStat(Entity entity, String name) {
 		String s = null;
 		if(entity.getEntityData() != null && entity.getEntityData().hasKey(name)) {s = entity.getEntityData().getString(name);}
 		return s;
 	}
 	
 	public static boolean hasStat(Entity entity, String name) {
 		if(entity.getEntityData() != null && entity.getEntityData().hasKey(name)) {return true;}
 		return false;
 	}
 	
 	@ForgeSubscribe
 	public void entityJoinWorld(EntityJoinWorldEvent event) {
 		if(!event.world.isRemote) {
 			processEntity(event.entity, ModjamMod.r);
 			
 			boolean isClone = true;
 			
			try {
 				isClone = event.entity.getDataWatcher().getWatchableObjectByte(30) != 0;
 			} catch (Exception e) {
 				isClone = false;
 				event.entity.getDataWatcher().addObject(30, (byte) 1);
			}
 			
 			if(CommonTickHandler.worldData != null && CommonTickHandler.worldData.getDisadvantage().equals("Increased Mob Spawn") && (event.entity instanceof EntityMob) && !(event.entity instanceof EntityDragon) && !isClone && ModjamMod.r.nextInt(3) == 0) {
 				
 				Entity entityNew = null;
 				
 				try {
 					Constructor[] constructors = event.entity.getClass().getConstructors();
 					
 					for(int i = 0; i < constructors.length; i++) {
 						Class[] parameters = constructors[i].getParameterTypes();
 						if(parameters.length == 1 && parameters[0].equals(World.class)) {entityNew = (Entity) event.entity.getClass().getConstructors()[i].newInstance(event.world);}
 					}
 					
 				} catch (Exception e) {e.printStackTrace();}
 				
 				if(entityNew != null) {
 					entityNew.setLocationAndAngles(event.entity.posX, event.entity.posY, event.entity.posZ, event.entity.rotationYaw, event.entity.rotationPitch);
					entityNew.getDataWatcher().addObject(30, (byte) 1);
 					entityNew.dimension = event.entity.dimension;
 					CommonTickHandler.addLater.add(entityNew); //event.world.spawnEntityInWorld(entityNew);
 				}
 			}
 			
 			if(event.entity instanceof EntityPlayer) {
 				if(!event.entity.getEntityData().hasKey("Blessing") && CommonTickHandler.worldData.blessingByPlayer.containsKey(event.entity.getEntityName())) {
 					event.entity.getEntityData().setString("Blessing", CommonTickHandler.worldData.blessingByPlayer.get(event.entity.getEntityName()));
 					PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_BLESSING, new Object[] {event.entity.getEntityData().getString("Blessing")}), (Player) event.entity);
 					CommonTickHandler.worldData.blessingByPlayer.remove(event.entity.getEntityName());
 					CommonTickHandler.worldData.markDirty();
 				}
 				
 				if(!event.entity.getEntityData().hasKey("PotionKnowledge") && CommonTickHandler.worldData.potionKnowledgeByPlayer.containsKey(event.entity.getEntityName())) {
 					event.entity.getEntityData().setIntArray("PotionKnowledge", CommonTickHandler.worldData.potionKnowledgeByPlayer.get(event.entity.getEntityName()));
 					PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_POTION_KNOWLEDGE, new Object[] {event.entity.getEntityData().getIntArray("PotionKnowledge")}), (Player) event.entity);
 					CommonTickHandler.worldData.potionKnowledgeByPlayer.remove(event.entity.getEntityName());
 					CommonTickHandler.worldData.markDirty();
 				}
 			}
 		}
 	}
 	
 	public static ArrayList<EntityStatTracker> temp = new ArrayList<EntityStatTracker>();
 	
 	public static boolean b = false;
 	
 	public static void processEntity(Entity entity, Random r) {
 		
 		if(entity == null) {return;}
 		
 		temp.clear();
 		
 		if(statTrackersByClass.containsKey(entity.getClass())) {temp.add(statTrackersByClass.get(entity.getClass()));}
 		
 		for(EntityStatTracker e : genericTrackers) {
 			if(!temp.contains(e)) {
 				for(Class c : e.classes) {if(c.isAssignableFrom(entity.getClass())) {temp.add(e); break;}}
 			}
 		}
 		
 		if(!temp.isEmpty()) {
 			String processed = EntityStatHelper.getStat(entity, "processed");
 			
 			if(processed == null || processed.equals("false")) {
 				EntityStatHelper.giveStat(entity, "processed", "true");
 				
 				for(EntityStatTracker statTracker : temp) {
 					for(EntityStat s : statTracker.stats) {
 						giveStat(entity, s.name, s.getNewValue(r).toString());
 						if(entity instanceof EntityLiving) {setName((EntityLiving) entity, s.getAlteredEntityName((EntityLiving) entity));}
 						s.modifyEntity(entity);
 					}
 				}
 			}
 		}
 		
 		/*if(entity instanceof EntityLiving) {
 			String entityName = ((EntityLiving) entity).hasCustomNameTag() ? ((EntityLiving) entity).getCustomNameTag() : null;
 			
 			if(FyresWorldData.currentDisadvantage != null && FyresWorldData.currentDisadvantage.equals("Illiterate")) {
 				if((entityName == null || !entityName.startsWith("\u00A7k"))) {
 					setName((EntityLiving) entity, "\u00A7k" + (entityName == null ? entity.getEntityName() : entityName));
 				}
 			} else if(entityName != null && entityName.startsWith("\u00A7k")) {
 				setName((EntityLiving) entity, entityName.replace("\u00A7k", ""));
 			}
 		}*/
 	}
 	
 	public void register() {
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	@ForgeSubscribe
 	public void livingDeath(LivingDeathEvent event) {
 		if(!event.entity.worldObj.isRemote) {
 			if(event.entity.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot")) {
 				
 				if(event.entity instanceof EntityLivingBase && event.source != null && event.source.getEntity() != null) {
 					if(event.source.getEntity().getEntityData().hasKey("Blessing")) {
 						String blessing = event.source.getEntity().getEntityData().getString("Blessing");
 						
 						if(blessing.equals("Thief") && ModjamMod.r.nextInt(10) == 0) {
 							if(!event.entity.worldObj.isRemote) {event.entity.dropItem(Item.goldNugget.itemID, 1);}
 							//event.entity.worldObj.playSoundAtEntity(event.entity, "fyresmodjam:coin", 1.0F, 1.0F);
 						}
 					}
 				}
 				
 				int level = 0;
 				if(event.entity.getEntityData().hasKey("Level")) {level = Integer.parseInt(event.entity.getEntityData().getString("Level"));}
 				if(ModjamMod.r.nextInt(10) == 0 || level == 5) {event.entity.entityDropItem(new ItemStack(ModjamMod.mysteryPotion.itemID, 1, ModjamMod.r.nextInt(13)), event.entity.height/2);}
 			
 			}
 			
 			if(CommonTickHandler.worldData.currentTask.equals("Kill") && CommonTickHandler.worldData.validMobs[CommonTickHandler.worldData.currentTaskID].isAssignableFrom(event.entity.getClass())) {
 				CommonTickHandler.worldData.progress++;
 				
 				String name1 = CommonTickHandler.worldData.currentTask.equals("Kill") ? FyresWorldData.validMobNames[CommonTickHandler.worldData.currentTaskID] : new ItemStack(Item.itemsList[CommonTickHandler.worldData.currentTaskID], 1).getDisplayName();
 				
 				if(name1.contains("Block")) {if(name1.contains("Block")) {name1 = name1.replace("Block", "Blocks").replace("block", "blocks");}}
 				else {name1 += "s";}
 				
 				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7fCurrent Goal Progress: " + CommonTickHandler.worldData.progress + "/" + CommonTickHandler.worldData.currentTaskAmount + " " + name1 + " "+ CommonTickHandler.worldData.currentTask + "ed."}));
 				
 				if(CommonTickHandler.worldData.progress >= CommonTickHandler.worldData.currentTaskAmount) {
 					CommonTickHandler.worldData.progress = 0;
 					CommonTickHandler.worldData.tasksCompleted++;
 					
 					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.LEVEL_UP, new Object[] {CommonTickHandler.worldData.rewardLevels}));
 					
 					if(!CommonTickHandler.worldData.enderDragonKilled && event.entity instanceof EntityDragon) {CommonTickHandler.worldData.enderDragonKilled = true;}
 					CommonTickHandler.worldData.giveNewTask();
 					
 					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eA world goal has been completed!" + (!CommonTickHandler.worldData.getDisadvantage().equals("None") ? " World disadvantage has been lifted!": "")}));
 					PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eA new world goal has been set: " + (CommonTickHandler.worldData.currentTask + " " + CommonTickHandler.worldData.currentTaskAmount + " " + (CommonTickHandler.worldData.currentTask.equals("Kill") ? FyresWorldData.validMobNames[CommonTickHandler.worldData.currentTaskID] : new ItemStack(Item.itemsList[CommonTickHandler.worldData.currentTaskID], 1).getDisplayName()) + "s. (" + CommonTickHandler.worldData.progress + " " + CommonTickHandler.worldData.currentTask + "ed)")}));
 					//PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eGoal Reward: " + CommonTickHandler.worldData.rewardLevels + " experience levels"}));
 					
 					CommonTickHandler.worldData.currentDisadvantage = "None";
 				}
 				
 				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {CommonTickHandler.worldData.potionValues, CommonTickHandler.worldData.potionDurations, CommonTickHandler.worldData.getDisadvantage(), CommonTickHandler.worldData.currentTask, CommonTickHandler.worldData.currentTaskID, CommonTickHandler.worldData.currentTaskAmount, CommonTickHandler.worldData.progress, CommonTickHandler.worldData.tasksCompleted, CommonTickHandler.worldData.enderDragonKilled, ModjamMod.spawnTraps, CommonTickHandler.worldData.rewardLevels}));
 			
 				CommonTickHandler.worldData.markDirty();
 			}
 			
 			if(!CommonTickHandler.worldData.enderDragonKilled && event.entity instanceof EntityDragon) {
 				CommonTickHandler.worldData.enderDragonKilled = true;
 				PacketDispatcher.sendPacketToAllPlayers(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {CommonTickHandler.worldData.potionValues, CommonTickHandler.worldData.potionDurations, CommonTickHandler.worldData.getDisadvantage(), CommonTickHandler.worldData.currentTask, CommonTickHandler.worldData.currentTaskID, CommonTickHandler.worldData.currentTaskAmount, CommonTickHandler.worldData.progress, CommonTickHandler.worldData.tasksCompleted, CommonTickHandler.worldData.enderDragonKilled, ModjamMod.spawnTraps, CommonTickHandler.worldData.rewardLevels}));
 				CommonTickHandler.worldData.markDirty();
 			}
 		}
 		
 		if(event.entity instanceof EntityPlayer) {
 			((EntityPlayer) event.entity).triggerAchievement(ModjamMod.losingIsFun);
 			
 			CommonTickHandler.worldData.blessingByPlayer.put(event.entity.getEntityName(), event.entity.getEntityData().getString("Blessing"));
 			CommonTickHandler.worldData.potionKnowledgeByPlayer.put(event.entity.getEntityName(), event.entity.getEntityData().getIntArray("PotionKnowledge"));
 		}
 	}
 	
 	public static void setEntitySize(Entity entity, float par1, float par2) {
 		float f2;
 
         if (par1 != entity.width || par2 != entity.height) {
             f2 = entity.width;
             entity.width = par1;
             entity.height = par2;
             entity.boundingBox.maxX = entity.boundingBox.minX + (double)entity.width;
             entity.boundingBox.maxZ = entity.boundingBox.minZ + (double)entity.width;
             entity.boundingBox.maxY = entity.boundingBox.minY + (double)entity.height;
 
             /*if (entity.width > f2 && !entity.firstUpdate && !entity.worldObj.isRemote) {
                 entity.moveEntity((double)(f2 - entity.width), 0.0D, (double)(f2 - entity.width));
             }*/
         }
 
         f2 = par1 % 2.0F;
 
         if ((double)f2 < 0.375D) {
             entity.myEntitySize = EnumEntitySize.SIZE_1;
         } else if ((double)f2 < 0.75D) {
             entity.myEntitySize = EnumEntitySize.SIZE_2;
         } else if ((double)f2 < 1.0D) {
             entity.myEntitySize = EnumEntitySize.SIZE_3;
         } else if ((double)f2 < 1.375D) {
             entity.myEntitySize = EnumEntitySize.SIZE_4;
         } else if ((double)f2 < 1.75D) {
             entity.myEntitySize = EnumEntitySize.SIZE_5;
         } else {
             entity.myEntitySize = EnumEntitySize.SIZE_6;
         }
 	}
 }
