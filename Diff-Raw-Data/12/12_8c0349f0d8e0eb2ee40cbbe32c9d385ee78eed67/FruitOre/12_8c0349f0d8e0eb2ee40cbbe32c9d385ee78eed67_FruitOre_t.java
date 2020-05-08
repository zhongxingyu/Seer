 package ayamitsu.fruitore;
 
 import java.io.IOException;
 
 import ayamitsu.fruitore.block.BlockFruitOre;
 import ayamitsu.fruitore.block.TileEntityFruitOre;
 import ayamitsu.fruitore.item.ItemFruitOreSapling;
 import ayamitsu.fruitore.item.RecipeFruitOreSapling;
 import ayamitsu.fruitore.network.PacketHandler;
 import ayamitsu.util.io.Configuration;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(
 	modid = "ayamitsu.fruitore",
 	name = "FruitOre",
 	version = "1.0.0"
 )
 @NetworkMod(
 	clientSideRequired = true,
 	serverSideRequired = false,
 	packetHandler = PacketHandler.class,
 	channels = "fruitore.te"
 )
 public class FruitOre {
 
 	@Mod.Instance("ayamitsu.fruitore")
 	public static FruitOre instance;
 
 	@SidedProxy(clientSide = "ayamitsu.fruitore.client.ClientProxy", serverSide = "ayamitsu.fruitore.server.ServerProxy")
 	public static Proxy proxy;
 
 	public static Block fruitOreBlock;
 
 	public static int fruitOreBlockId;
 
 	public static Item fruitOreSaplingItem;
 
 	public static int fruitOreSaplingItemId;
 
 
 	/** the flag, add all fruits to creative tabs **/
 	public static boolean addAllFruitsToCreativeTabs;
 
 	@Mod.PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		Configuration conf = new Configuration(event.getSuggestedConfigurationFile());
 
 		try {
 			conf.load();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
		this.fruitOreBlockId = conf.getProperty("fruitOreBlockId", 180).getInt();
 		this.fruitOreSaplingItemId = conf.getProperty("fruitOreSaplingItemId", 14000).getInt();
 
 		this.addAllFruitsToCreativeTabs = conf.getProperty("addAllFruitsToCreativeTabs", false).getBoolean();
 
 		try {
 			conf.save();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Mod.Init
 	public void init(FMLInitializationEvent event) {
 		this.fruitOreBlock = new BlockFruitOre(this.fruitOreBlockId, Material.rock).setUnlocalizedName("ayamitsu/fruitore:fruitore").setCreativeTab(CreativeTabs.tabBlock);
 		GameRegistry.registerBlock(this.fruitOreBlock, "ayamitsu/fruitore:fruitore");
 		LanguageRegistry.instance().addNameForObject(this.fruitOreBlock, "en_US", "Fruit Ore Block");
 
 		//this.fruitOreSaplingItem = new ItemFruitOreSapling(this.fruitOreSaplingItemId - 256, this.fruitOreBlock.blockID).setUnlocalizedName("ayamitsu.fruitore.fruitoresapling").setCreativeTab(CreativeTabs.tabBlock);
 		Item.itemsList[this.fruitOreBlock.blockID] = null;
 		Item.itemsList[this.fruitOreBlock.blockID] = this.fruitOreSaplingItem = new ItemFruitOreSapling(this.fruitOreBlock.blockID - 256);
 
 		GameRegistry.addRecipe(new RecipeFruitOreSapling());
 		GameRegistry.addShapelessRecipe(new ItemStack(this.fruitOreSaplingItem),
 				new Object[] {
 				new ItemStack(Block.sapling),
 				new ItemStack(Block.stone)
 			}
 		);
 
 		LanguageRegistry.instance().addStringLocalization("ayamitsu.fruitore.fruit", "en_US", "Fruit");
 		LanguageRegistry.instance().addStringLocalization("ayamitsu.fruitore.fruit", "ja_JP", "果実");
 
 		this.proxy.load();
 	}
 
 }
