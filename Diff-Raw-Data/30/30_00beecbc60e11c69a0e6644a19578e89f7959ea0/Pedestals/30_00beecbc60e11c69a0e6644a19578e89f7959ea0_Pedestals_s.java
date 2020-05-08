 package au.com.mineauz.SkyQuest.pedestals;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import net.minecraft.server.NBTTagCompound;
 import net.minecraft.server.NBTTagList;
 
 import org.bukkit.Location;
 
 /**
  * This class keeps track of current pedestals
  * @author Schmoller
   */
 public class Pedestals 
 {
 	static
 	{
 		mPedestalTypes = new HashMap<String, Class<? extends PedestalBase>>();
 		mPedestalTypesReverse = new HashMap<Class<? extends PedestalBase>, String>();
 		
 		// Register this type with the pedestal manager
 		registerPedestalType("Debug", DebugPedestal.class);
 		registerPedestalType("Save", SavePedestal.class);
 		registerPedestalType("Quest", QuestPedestal.class);
 	}
 	/**
 	 * Registers a pedestal type
 	 * @param typeName The name of the type
 	 * @param typeClass The class of the type
 	 */
 	public static void registerPedestalType(String typeName, Class<? extends PedestalBase> typeClass)
 	{
 		mPedestalTypes.put(typeName.toLowerCase(), typeClass);
 		mPedestalTypesReverse.put(typeClass, typeName.toLowerCase());
 	}
 	/**
 	 * Adds a new pedestal to the map
 	 */
 	public static void addPedestal(PedestalBase pedestal)
 	{
 		mPedestals.put(pedestal.getLocation(), pedestal);
 	}
 	/**
 	 * Removes a pedestal from the map
 	 * @return True if it was removed
 	 */
 	public static boolean removePedestal(Location pedestalLocation)
 	{
 		return (mPedestals.remove(pedestalLocation) != null);
 	}
 	
 	/**
 	 * Compiles the pedestals into an NBTTagCompound to be saved to disk
 	 */
 	public static void savePedestals(NBTTagCompound root)
 	{
 		NBTTagList list = new NBTTagList();
 		
 		for(Entry<Location, PedestalBase> pedestal : mPedestals.entrySet())
 		{
 			NBTTagCompound tag = new NBTTagCompound();
 			tag.setString("Type", mPedestalTypesReverse.get(pedestal.getValue().getClass()));
 			pedestal.getValue().writeToNBT(tag);
 			list.add(tag);
 		}
 		
 		root.set("Pedestals", list);
 	}
 	/**
 	 * Rebuilds the list of pedestals from a TagCompound
 	 */
 	public static void loadPedestals(NBTTagCompound root)
 	{
 		NBTTagList list = root.getList("Pedestals");
 		
 		unloadPedestals();
 		
 		if(list == null)
 			return;
 		
 		for(int i = 0; i < list.size(); ++i)
 		{
 			NBTTagCompound tag = (NBTTagCompound)list.get(i);
 			
 			PedestalBase pedestal;
 			try 
 			{
 				pedestal = mPedestalTypes.get(tag.getString("Type").toLowerCase()).newInstance();
 				pedestal.readFromNBT(tag);
 				mPedestals.put(pedestal.getLocation(), pedestal);
 			} 
 			catch (InstantiationException | IllegalAccessException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void unloadPedestals()
 	{
 		// Unload all current pedestals
 		for(Entry<Location, PedestalBase> ent : mPedestals.entrySet())
 		{
 			ent.getValue().onRemove();
 		}
 		mPedestals.clear();
 	}
 
 	private static HashMap<Location, PedestalBase> mPedestals = new HashMap<Location, PedestalBase>();
 	
 	private static HashMap<String, Class<? extends PedestalBase>> mPedestalTypes;
 	private static HashMap<Class<? extends PedestalBase>, String> mPedestalTypesReverse;
 }
