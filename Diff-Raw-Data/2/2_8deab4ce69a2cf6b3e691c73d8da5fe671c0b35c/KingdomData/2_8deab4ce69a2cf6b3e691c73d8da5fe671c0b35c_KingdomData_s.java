 /**
  * This class was created by <Vazkii>. It's distributed as
  * part of the Kings and Peasants Mod.
  *
  * Kings and Peasants is Open Source and distributed under a
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
  * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
  *
  * File Created @ [15 Jul 2013, 16:05:47 (GMT)]
  */
 package vazkii.kap.util.storage;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.nbt.NBTTagString;
 import net.minecraft.world.World;
 import vazkii.kap.block.ModBlocks;
 import vazkii.kap.tile.TileEntityThrone;
 import vazkii.kap.util.nbt.NBTManaged;
 import vazkii.kap.util.nbt.NBTManager;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class KingdomData implements Serializable {
 
 	private static final long serialVersionUID = 8858112042694418941L;
 
 	@SideOnly(Side.CLIENT)
 	public static KingdomData clientKingdom;
 
 	@NBTManaged("crest") public CrestData crest = new CrestData(0xFFFFFF, 0xFFFFFF, (short) -1);
 	@NBTManaged("name") public String name;
 	@NBTManaged("owner") public String owner;
 	@NBTManaged("tier") public int tier = 0;
 
 	public List<String> vassals = new ArrayList();
 	public List<String> population = new ArrayList();
 
 	@NBTManaged("throneCoords") public Coordinates throneCoords = new Coordinates(0, Integer.MAX_VALUE, 0);
 
 	public KingdomData(CrestData crest, String name, String owner) {
 		this.crest = crest;
 		this.name = name;
 		this.owner = owner;
 	}
 
 	public boolean hasThrone(World world) {
		return throneCoords.y >= 256 && throneCoords.y < 0 && throneCoords.getBlockID(world) == ModBlocks.throne.blockID && ((TileEntityThrone) throneCoords.getTileEntity(world)).kingdom.equals(name);
 	}
 
 	public void writeToNBT(NBTTagCompound cmp) {
 		NBTManager.writeType(cmp, this);
 
 		NBTTagList vassalsList = new NBTTagList();
 		for(String s : vassals)
 			vassalsList.appendTag(new NBTTagString("", s));
 
 		NBTTagList populationList = new NBTTagList();
 		for(String s : population)
 			vassalsList.appendTag(new NBTTagString("", s));
 
 		cmp.setTag("vassals", vassalsList);
 		cmp.setTag("population", populationList);
 	}
 
 	public void readFromNBT(NBTTagCompound cmp) {
 		NBTManager.loadType(cmp, this);
 
 		vassals.clear();
 		population.clear();
 
 		if(cmp.hasKey("vassals")) {
 			NBTTagList vassalsList = cmp.getTagList("vassals");
 			for(int i = 0; i < vassalsList.tagCount(); i++)
 				vassals.add(((NBTTagString) vassalsList.tagAt(i)).data);
 		}
 
 		if(cmp.hasKey("population")) {
 			NBTTagList populationList = cmp.getTagList("population");
 			for(int i = 0; i < populationList.tagCount(); i++)
 				population.add(((NBTTagString) populationList.tagAt(i)).data);
 		}
 	}
 }
