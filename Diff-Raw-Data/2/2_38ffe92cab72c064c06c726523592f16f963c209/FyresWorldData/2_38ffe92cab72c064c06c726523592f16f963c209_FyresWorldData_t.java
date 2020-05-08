 package assets.fyresmodjam;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import net.minecraft.block.Block;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.boss.EntityDragon;
 import net.minecraft.entity.boss.EntityWither;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldSavedData;
 import net.minecraft.world.WorldServer;
 import net.minecraft.world.storage.MapStorage;
 
 public class FyresWorldData extends WorldSavedData {
 
 	public static String[] validDisadvantages = {/*"Illiterate",*/ "Tougher Mobs", "Weak", "Explosive Traps"};
 	public static String[] disadvantageDescriptions = {/*"Item names are unreadable",*/ "-25% damage to hostile enemies", "-25% melee damage", "Traps trigger explosions on failed disarms"};
 	
 	public static String[] validTasks = {"Kill", "Collect"};
 	
 	public static String key = "FyresWorldData";
 	
 	public int[] potionValues = null;
 	public int[] potionDurations = null;
 	
 	public String currentDisadvantage = null;
 	
 	public String currentTask = null;
 	public int currentTaskID = -1;
 	public int currentTaskAmount = 0;
 	public int progress = 0;
 	public int tasksCompleted = 0;
 	
 	public static Class[] validMobs = {EntityDragon.class, EntityWither.class};
 	public static String[] validMobNames = {"Ender Dragon", "Wither"};
 	public static int[][] mobNumbers = {new int[] {1, 1} , new int[] {1, 3}};
 	
 	public static int[] validItems = {Block.blockDiamond.blockID, Block.blockGold.blockID, Block.blockEmerald.blockID, Block.blockLapis.blockID, Item.diamond.itemID, Item.emerald.itemID, Item.ingotGold.itemID, Item.netherStar.itemID, Item.ghastTear.itemID};
 
 	public FyresWorldData() {
 		super(key);
 	}
 
 	public FyresWorldData(String key) {
 		super(key);
 	}
 
 	public static FyresWorldData forWorld(World world) {
 		
 		MapStorage storage = world.perWorldStorage;
 		FyresWorldData result = (FyresWorldData) storage.loadData(FyresWorldData.class, key);
 		
 		if(result == null) {result = new FyresWorldData(); storage.setData(key, result); result.checkWorldData();}
 		
 		return result; 
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound nbttagcompound) {
 		if(nbttagcompound.hasKey("values")) {potionValues = nbttagcompound.getIntArray("values");}
 		if(nbttagcompound.hasKey("durations")) {potionDurations = nbttagcompound.getIntArray("durations");}
 		if(nbttagcompound.hasKey("currentDisadvantage")) {currentDisadvantage = nbttagcompound.getString("currentDisadvantage");}
 		
 		if(nbttagcompound.hasKey("currentTask")) {currentTask = nbttagcompound.getString("currentTask");}
 		if(nbttagcompound.hasKey("currentTaskID")) {currentTaskID = nbttagcompound.getInteger("currentTaskID");}
 		if(nbttagcompound.hasKey("currentTaskAmount")) {currentTaskAmount = nbttagcompound.getInteger("currentTaskAmount");}
 		if(nbttagcompound.hasKey("progress")) {progress = nbttagcompound.getInteger("progress");}
 		if(nbttagcompound.hasKey("tasksCompleted")) {tasksCompleted = nbttagcompound.getInteger("tasksCompleted");}
 		
 		checkWorldData();
 	} 
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbttagcompound) {
 		checkWorldData();
 		
 		nbttagcompound.setIntArray("values", potionValues);
 		nbttagcompound.setIntArray("durations", potionDurations);
 		nbttagcompound.setString("currentDisadvantage", currentDisadvantage);
 		
 		nbttagcompound.setString("currentTask", currentTask);
 		nbttagcompound.setInteger("currentTaskID", currentTaskID);
 		nbttagcompound.setInteger("currentTaskAmount", currentTaskAmount);
 		nbttagcompound.setInteger("progress", progress);
 		nbttagcompound.setInteger("tasksCompleted", tasksCompleted);
 	}
 	
 	private void checkWorldData() {
 		if(potionValues == null) {
 			potionValues = new int[12];
 			
 			for(int i = 0; i < 12; i++) {
 				int i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);
 				while(Potion.potionTypes[i2] == null) {i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);}
 				
 				//boolean skip = false;
 				//for(int i3 = 0; i3 < potionValues.length; i3++) {if(potionValues[i] == i3) {skip = true; break;}}
 				//if(skip) {continue;}
 						
 				potionValues[i] = i2;// break;
 			}
 		} else {
 			for(int i = 0; i < 12; i++) {
 				if(Potion.potionTypes[potionValues[i]] == null) {
 					int i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);
 					while(Potion.potionTypes[i2] == null) {i2 = ModjamMod.r.nextInt(Potion.potionTypes.length);}
 					potionValues[i] = i2;
 				}
 			}
 		}
 		
 		if(potionDurations == null) {potionDurations = new int[12];}
 		for(int i = 0; i < 12; i++) {if(potionDurations[i] != 0) {continue;} potionDurations[i] = 5 + ModjamMod.r.nextInt(26);}
 		
 		boolean changeDisadvantage = currentDisadvantage == null;
 		
 		if(!changeDisadvantage) {
 			boolean valid = false;
 			for(String s : validDisadvantages) {if(s.equals(currentDisadvantage)) {valid = true; break;}}
 			changeDisadvantage = !valid;
 		}
 		
 		if(changeDisadvantage) {
 			currentDisadvantage = validDisadvantages[ModjamMod.r.nextInt(validDisadvantages.length)];
 			
 			/*MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
 			
 			for(int i = 0; i < server.worldServers.length; i++) {
 				WorldServer s = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[i];
 				
 				if(s == null) {continue;}
 				
 				for(Object o : s.loadedEntityList) {
 					if(o == null || o instanceof EntityPlayer) {continue;}
 					EntityStatHelper.processEntity((Entity) o, ModjamMod.r);
 				}
 			}*/
 		}
 		
 		if(currentTask == null) {
 			giveNewTask();
 		} else {
 			boolean changeTask = true;
 			for(String s : validTasks) {if(s.equals(currentTask)) {changeTask = false; break;}}
 			if(changeTask) {giveNewTask();} else {if(currentTask.equals("Kill")) {currentTaskID %= validMobs.length;}}
 		}
 	}
 
 	public void giveNewTask() {
 		progress = 0;
 		
 		currentTask = validTasks[ModjamMod.r.nextInt(validTasks.length)];
 		
 		if(currentTask.equals("Kill")) {
 			currentTaskID = ModjamMod.r.nextInt(validMobs.length);
 			currentTaskAmount = mobNumbers[currentTaskID][0] + ModjamMod.r.nextInt(mobNumbers[currentTaskID][1]);
 		} else if(currentTask.equals("Collect")) {
 			currentTaskID = validItems[ModjamMod.r.nextInt(validItems.length)];
 			currentTaskAmount = 5 + ModjamMod.r.nextInt(28);
 		}
		
		markDirty();
 	}
 }
