 package clashsoft.mods.morepotions;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Properties;
 
 import clashsoft.brewingapi.BrewingAPI;
 import clashsoft.clashsoftapi.CustomItem;
 import clashsoft.clashsoftapi.CustomPotion;
 import clashsoft.clashsoftapi.util.*;
 import clashsoft.mods.morepotions.block.BlockCauldron2;
 import clashsoft.mods.morepotions.block.BlockMixer;
 import clashsoft.mods.morepotions.item.ItemMortar;
 import clashsoft.mods.morepotions.tileentity.TileEntityCauldron;
 import clashsoft.mods.morepotions.tileentity.TileEntityMixer;
 
 import com.google.common.base.Charsets;
 
 import cpw.mods.fml.common.*;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 import net.minecraft.block.Block;
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.util.ResourceLocation;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.oredict.OreDictionary;
 
 @Mod(modid = "MorePotionsMod", name = "More Potions Mod", version = MorePotionsMod.VERSION)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class MorePotionsMod
 {
	public static final int			REVISION		= 3;
 	public static final String		VERSION			= CSUpdate.CURRENT_VERSION + "-" + REVISION;
 	
 	@Instance("MorePotionsMod")
 	public static MorePotionsMod	INSTANCE;
 	
 	@SidedProxy(clientSide = "clashsoft.mods.morepotions.ClientProxy", serverSide = "clashsoft.mods.morepotions.CommonProxy")
 	public static CommonProxy		proxy;
 	
 	public static String			customEffects	= "gui/potionIcons.png";
 	
 	// Configurables
 	public static int				randomMode		= 0;
 	
 	public static int				MixerTEID		= 12;
 	public static int				Cauldron2TEID	= 13;
 	
 	public static int				MixerID			= 190;
 	public static int				DustID			= 14000;
 	public static int				MortarID		= 14001;
 	
 	public static boolean			cauldronInfo	= false;
 	
 	static
 	{
 		BrewingAPI.expandPotionList();
 	}
 	
 	public static Potion			fire			= new CustomPotion("potion.fire", true, 0xFFE500, false, customEffects, 0, 0);
 	public static Potion			effectRemove	= new CustomPotion("potion.effectRemove", false, 0xFFFFFF, false, customEffects, 1, 0);
 	public static Potion			waterWalking	= new CustomPotion("potion.waterWalking", false, 0x124EFE, false, customEffects, 2, 0);
 	public static Potion			coldness		= new CustomPotion("potion.coldness", false, 0x00DDFF, false, customEffects, 3, 0);
 	public static Potion			ironSkin		= new CustomPotion("potion.ironSkin", false, 0xD8D8D8, false, customEffects, 4, 0);
 	public static Potion			obsidianSkin	= new CustomPotion("potion.obsidianSkin", false, 0x101023, false, customEffects, 5, 0);
 	public static Potion			doubleLife		= new CustomPotion("potion.doubleLife", false, 0xFF2222, false, customEffects, 7, 0, CSUtil.fontColorInt(0, 0, 1, 1));
 	public static Potion			explosiveness	= new CustomPotion("potion.explosiveness", true, 0xCC0000, false, customEffects, 1, 1);
 	public static Potion			random			= new CustomPotion("potion.random", false, 0x000000, randomMode == 0, customEffects, 2, 1, CSUtil.fontColorInt(0, 1, 1, 1));
 	public static Potion			thorns			= new CustomPotion("potion.thorns", false, 0x810081, false, customEffects, 3, 1);
 	public static Potion			greenThumb		= new CustomPotion("potion.greenThumb", false, 0x008100, false, customEffects, 4, 1);
 	public static Potion			projectile		= new CustomPotion("potion.projectile", false, 0x101010, false, customEffects, 5, 1);
 	
 	public static BlockMixer		mixer;
 	public static BlockCauldron2	cauldron2;
 	public static Item				dust;
 	public static ItemStack			dustCoal;
 	public static ItemStack			dustIron;
 	public static ItemStack			dustGold;
 	public static ItemStack			dustObsidian;
 	public static ItemStack			dustDiamond;
 	public static ItemStack			dustEmerald;
 	public static ItemStack			dustQuartz;
 	public static ItemStack			dustWither;
 	public static ItemStack			dustEnderpearl;
 	public static ItemStack			dustClay;
 	public static ItemStack			dustBrick;
 	public static ItemStack			dustFlint;
 	public static ItemStack			dustGlass;
 	public static ItemStack			dustCharcoal;
 	public static ItemStack			dustWoodOak;
 	public static ItemStack			dustWoodBirch;
 	public static ItemStack			dustWoodSpruce;
 	public static ItemStack			dustWoodJungle;
 	public static ItemStack			dustNetherstar;
 	public static ItemStack			dustNetherbrick;
 	public static ItemMortar		mortar;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		
 		MixerTEID = config.get("TileEntityIDs", "MixerTEID", 12).getInt();
 		Cauldron2TEID = config.get("TileEntityIDs", "Cauldron2TEID", 13).getInt();
 		
 		MixerID = config.getBlock("MixerID", 190).getInt();
 		DustID = config.getItem("DustID", 14000).getInt();
 		MortarID = config.getItem("MortarID", 14001).getInt();
 		
 		randomMode = config.get("Potions", "RandomPotionMode", 0, "Determines how the random potion works, if this is 0 the effect is instant and you get a random potion effect when you drink the potion, 1 will give you a new effect every 2 seconds.").getInt();
 		cauldronInfo = config.get("Cauldrons", "CauldronInfo", true).getBoolean(false);
 		
 		config.save();
 	}
 	
 	@EventHandler
 	public void load(FMLInitializationEvent event)
 	{
 		BrewingAPI.registerEffectHandler(new MorePotionsModEffectHandler());
 		BrewingAPI.registerIngredientHandler(new MorePotionsModIngredientHandler());
 		
 		GameRegistry.registerTileEntity(TileEntityMixer.class, "Mixxer");
 		GameRegistry.registerTileEntity(TileEntityCauldron.class, "Cauldron2");
 		
 		Block.blocksList[Block.cauldron.blockID] = null;
 		cauldron2 = (BlockCauldron2) (new BlockCauldron2(Block.cauldron.blockID)).setHardness(2.0F).setUnlocalizedName("cauldron").setTextureName("cauldron");
 		
 		mixer = (BlockMixer) (new BlockMixer(MixerID)).setUnlocalizedName("mixer").setCreativeTab(CreativeTabs.tabBrewing);
 		
 		CSBlocks.addBlock(mixer, "Mixer");
 		CSBlocks.addBlock(cauldron2, "Cauldron");
 		mortar = (ItemMortar) new ItemMortar(MortarID).setCreativeTab(CreativeTabs.tabTools).setMaxDamage(32).setNoRepair().setMaxStackSize(1).setUnlocalizedName("mortar");
 		dust = new CustomItem(DustID, CSArrays.addToAll(new String[] { "dustCoal", "dustIron", "dustGold", "dustDiamond", "dustEmerald", "dustObsidian", "dustQuartz", "dustWither", "dustEnderpearl", "dustClay", "dustBrick", "dustFlint", "dustGlass", "dustCharcoal", "dustWoodOak", "dustWoodBirch", "dustWoodSpruce", "dustWoodJungle", "dustNetherstar", "dustNetherbrick" }, "item.", ".name"), new String[] { "dustCoal", "dustIron", "dustGold", "dustDiamond", "dustEmerald", "dustObsidian", "dustQuartz", "dustWither", "dustEnderpearl", "dustClay", "dustBrick", "dustFlint", "dustGlass", "dustCoal", "dustWoodOak", "dustWoodBirch", "dustWoodSpruce", "dustWoodJungle", "dustNetherstar", "dustNetherbrick" }, new String[] { "C2", "Fe", "Au", "C128", "Be3Al2Si6O18", "MgFeSi2O8", "SiO2", "\u00a7k???", "BeK4N5Cl6", "Na2LiAl2Si2", "Na2LiAl2Si2", "SiO2", "SiO4", "C", "", "", "", "", "", "" }).setCreativeTab(CreativeTabs.tabMaterials);
 		addDusts();
 		
 		BrewingAPI.load();
 		
 		Item.sugar.setCreativeTab(CreativeTabs.tabBrewing);
 		Item.netherStalkSeeds.setCreativeTab(CreativeTabs.tabBrewing);
 		
 		CSCrafting.addCrafting(new ItemStack(mortar), new Object[] { "SfS", " S ", 'S', Block.stone, 'f', Item.flint });
 		CSCrafting.addCrafting(new ItemStack(mixer), new Object[] { "gSg", "g g", "SiS", 'g', Block.thinGlass, 'S', Block.stone, 'i', Item.ingotIron });
 		
 		addLocalizations();
 		
 		NetworkRegistry.instance().registerGuiHandler(INSTANCE, proxy);
 		proxy.registerRenderInformation();
 		proxy.registerRenderers();
 		MinecraftForge.EVENT_BUS.register(new MorePotionsModEventHandler());
 		GameRegistry.registerCraftingHandler(new MorePotionsModCraftingHandler());
 		
 		System.out.println("Initializing MorePotionsMod Brewings");
 		MorePotionsModBrewingList.initializeBaseBrewings_MorePotionsMod();
 		MorePotionsModBrewingList.initializeBrewings_MorePotionsMod();
 		
 		System.out.println("Registering MorePotionsMod Brewings");
 		MorePotionsModBrewingList.registerBaseBrewings_MorePotionsMod();
 		MorePotionsModBrewingList.registerBrewings_MorePotionsMod();
 	}
 	
 	private void addLocalizations()
 	{
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
				loadLanguageFile("lang/mpm_en_US", "en_US");
				loadLanguageFile("lang/mpm_de_DE", "de_DE");
 			}
 		}).start();
 	}
 	
 	public void loadLanguageFile(String fileName, String language)
 	{
 		try
 		{
 			Properties langPack = new Properties();
			ResourceLocation de = new ResourceLocation(fileName);
 			
 			InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(de).getInputStream();
 			Reader reader = new InputStreamReader(stream, Charsets.UTF_8);
 			langPack.load(reader);
 			
 			LanguageRegistry.instance().addStringLocalization(langPack, language);
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 	
 	private void addDusts()
 	{
 		ItemStack mortarStack = new ItemStack(mortar, 1, OreDictionary.WILDCARD_VALUE);
 		
 		dustCoal = CSCrafting.registerOre("dustCoal", new ItemStack(dust, 1, 0));
 		dustIron = CSCrafting.registerOre("dustIron", new ItemStack(dust, 1, 1));
 		dustGold = CSCrafting.registerOre("dustGold", new ItemStack(dust, 1, 2));
 		dustDiamond = CSCrafting.registerOre("dustDiamond", new ItemStack(dust, 1, 3));
 		dustEmerald = CSCrafting.registerOre("dustEmerald", new ItemStack(dust, 1, 4));
 		dustObsidian = CSCrafting.registerOre("dustObsidian", new ItemStack(dust, 1, 5));
 		dustQuartz = CSCrafting.registerOre("dustQuartz", new ItemStack(dust, 1, 6));
 		dustWither = CSCrafting.registerOre("dustWither", new ItemStack(dust, 1, 7));
 		dustEnderpearl = CSCrafting.registerOre("dustEnderpearl", new ItemStack(dust, 1, 8));
 		dustClay = CSCrafting.registerOre("dustClay", new ItemStack(dust, 1, 9));
 		dustBrick = CSCrafting.registerOre("dustBrick", new ItemStack(dust, 1, 10));
 		dustFlint = CSCrafting.registerOre("dustFlint", new ItemStack(dust, 1, 11));
 		dustGlass = CSCrafting.registerOre("dustGlass", new ItemStack(dust, 1, 12));
 		dustCharcoal = CSCrafting.registerOre("dustCharcoal", new ItemStack(dust, 1, 13));
 		dustWoodOak = CSCrafting.registerOre("dustWoodOak", CSCrafting.registerOre("dustWood", new ItemStack(dust, 1, 14)));
 		dustWoodBirch = CSCrafting.registerOre("dustWoodBirch", CSCrafting.registerOre("dustWood", new ItemStack(dust, 1, 15)));
 		dustWoodSpruce = CSCrafting.registerOre("dustWoodSpruce", CSCrafting.registerOre("dustWood", new ItemStack(dust, 1, 16)));
 		dustWoodJungle = CSCrafting.registerOre("dustWoodJungle", CSCrafting.registerOre("dustWood", new ItemStack(dust, 1, 17)));
 		dustNetherstar = CSCrafting.registerOre("dustNetherstar", new ItemStack(dust, 1, 18));
 		dustNetherbrick = CSCrafting.registerOre("dustNetherbrick", new ItemStack(dust, 1, 19));
 		
 		GameRegistry.addShapelessRecipe(dustCoal, new Object[] { Item.coal, mortarStack });
 		GameRegistry.addShapelessRecipe(dustIron, new Object[] { Item.ingotIron, mortarStack });
 		GameRegistry.addShapelessRecipe(dustGold, new Object[] { Item.ingotGold, mortarStack });
 		GameRegistry.addShapelessRecipe(dustDiamond, new Object[] { Item.diamond, mortarStack });
 		GameRegistry.addShapelessRecipe(dustEmerald, new Object[] { Item.emerald, mortarStack });
 		GameRegistry.addShapelessRecipe(dustObsidian, new Object[] { Block.obsidian, mortarStack });
 		GameRegistry.addShapelessRecipe(dustQuartz, new Object[] { Item.netherQuartz, mortarStack });
 		GameRegistry.addShapelessRecipe(dustWither, new Object[] { new ItemStack(Item.skull, 1, 1), dustCoal, dustQuartz });
 		GameRegistry.addShapelessRecipe(dustEnderpearl, new Object[] { Item.enderPearl, mortarStack });
 		GameRegistry.addShapelessRecipe(dustClay, new Object[] { Item.clay, mortarStack });
 		GameRegistry.addShapelessRecipe(dustBrick, new Object[] { Item.brick, mortarStack });
 		GameRegistry.addShapelessRecipe(dustFlint, new Object[] { Item.flint, mortarStack });
 		GameRegistry.addShapelessRecipe(dustGlass, new Object[] { Block.glass, mortarStack });
 		GameRegistry.addShapelessRecipe(dustCharcoal, new Object[] { new ItemStack(Item.coal, 1, 1), mortarStack });
 		GameRegistry.addShapelessRecipe(dustWoodOak, new Object[] { new ItemStack(Block.wood, 1, 0), mortarStack });
 		GameRegistry.addShapelessRecipe(dustWoodBirch, new Object[] { new ItemStack(Block.wood, 1, 2), mortarStack });
 		GameRegistry.addShapelessRecipe(dustWoodSpruce, new Object[] { new ItemStack(Block.wood, 1, 1), mortarStack });
 		GameRegistry.addShapelessRecipe(dustWoodJungle, new Object[] { new ItemStack(Block.wood, 1, 3), mortarStack });
 		GameRegistry.addShapelessRecipe(dustNetherstar, new Object[] { Item.netherStar, mortarStack });
 		GameRegistry.addShapelessRecipe(dustNetherbrick, new Object[] { Item.netherrackBrick, mortarStack });
 		
 		CSCrafting.addSmelting(dustIron, new ItemStack(Item.ingotIron), 0F);
 		CSCrafting.addSmelting(dustGold, new ItemStack(Item.ingotGold), 0F);
 		CSCrafting.addSmelting(dustQuartz, new ItemStack(Item.netherQuartz), 0F);
 		CSCrafting.addSmelting(dustClay, new ItemStack(Item.brick), 0.1F);
 		CSCrafting.addSmelting(dustBrick, new ItemStack(Item.brick), 0F);
 		CSCrafting.addSmelting(dustGlass, new ItemStack(Block.glass), 0F);
 	}
 	
 	public class MorePotionsModCraftingHandler implements ICraftingHandler
 	{
 		@Override
 		public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
 		{
 			for (int i = 0; i < craftMatrix.getSizeInventory(); i++)
 			{
 				if (craftMatrix.getStackInSlot(i) != null)
 				{
 					ItemStack j = craftMatrix.getStackInSlot(i);
 					if (j.getItem() != null && j.getItem() == MorePotionsMod.mortar)
 					{
 						ItemStack k = new ItemStack(MorePotionsMod.mortar, 2, (j.getItemDamage() + 1));
 						if (k.getItemDamage() >= k.getMaxDamage())
 						{
 							k.stackSize--;
 						}
 						craftMatrix.setInventorySlotContents(i, k);
 					}
 				}
 			}
 		}
 		
 		@Override
 		public void onSmelting(EntityPlayer player, ItemStack item)
 		{
 		}
 	}
 }
