 package clashsoft.mods.morefood;
 
 import java.util.Random;
 
 import clashsoft.clashsoftapi.CustomItem;
 import clashsoft.clashsoftapi.util.CSBlocks;
 import clashsoft.clashsoftapi.util.CSCrafting;
 import clashsoft.clashsoftapi.util.CSItems;
 import clashsoft.mods.morefood.block.BlockPlantMoreFood;
 import clashsoft.mods.morefood.block.BlockSaltOre;
 import clashsoft.mods.morefood.item.*;
 import clashsoft.mods.morefood.world.WorldGenGardener;
 import cpw.mods.fml.common.*;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemAppleGold;
 import net.minecraft.item.ItemBucketMilk;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenOcean;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 
 @Mod(modid = "MoreFoodMod", name = "More Food Mod", version = "1.6.2")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 public class MoreFoodMod
 {
 	@Instance("MoreFoodMod")
 	public static MoreFoodMod			instance;
 	
 	@SidedProxy(modId = "MoreFoodMod", clientSide = "clashsoft.mods.morefood.ClientProxy", serverSide = "clashsoft.mods.morefood.CommonProxy")
 	public static CommonProxy			proxy;
 	
 	public static String				items			= "/mod_MoreFood/gui/food.png";
 	public static String				terrain			= "/mod_MoreFood/terrain.png";
 	
 	public static int					itemsID			= 13000;
 	public static int					cucumberPlantID	= 510;
 	public static int					tomatoPlantID	= 511;
 	public static int					pepperPlantID	= 512;
 	public static int					saladPlantID	= 513;
 	public static int					onionPlantID	= 514;
 	public static int					chiliPlantID	= 515;
 	public static int					paprikaPlantID	= 516;
 	public static int					ricePlantID		= 517;
 	public static int					saltOreID		= 518;
 	public static int					cornPlantID		= 519;
 	
 	public static ItemMoreFood			salt;
 	public static ItemMoreFood			pepper;
 	public static ItemMoreFood			cinnamon;
 	public static ItemMoreFood			vanilla;
 	public static ItemMoreFood			honeyDrop;
 	public static ItemFoodMoreFood		chocolateCookie;
 	public static ItemFoodMoreFood		chocolateBar;
 	public static ItemFoodMoreFood		butter;
 	public static CustomItem			cereals;
 	public static ItemJuice				juice;
 	public static ItemBucketMilk		chocolateBucket;
 	public static ItemFoodMoreFood		cookedCarrot;
 	public static ItemAppleGold			goldenPotato;
 	public static ItemDiamondFood		diamondCarrot;
 	public static ItemDiamondFood		diamondPotato;
 	public static ItemDiamondFood		diamondApple;
 	public static ItemFoodMoreFood		stompedCarrot;
 	public static ItemFoodMoreFood		stompedPotato;
 	public static ItemFoodMoreFood		stompedApple;
 	public static ItemEdibles			edibles;
 	public static ItemFertilizer		fertilizer;
 	public static ItemMilkBowls			milkBowls;
 	public static ItemSoupBowls			soupBowls;
 	
 	public static BlockPlantMoreFood	cucumberPlant;
 	public static BlockPlantMoreFood	tomatoPlant;
 	public static BlockPlantMoreFood	pepperPlant;
 	public static BlockPlantMoreFood	saladPlant;
 	public static BlockPlantMoreFood	onionPlant;
 	public static BlockPlantMoreFood	chiliPlant;
 	public static BlockPlantMoreFood	paprikaPlant;
 	public static BlockPlantMoreFood	ricePlant;
 	public static BlockPlantMoreFood	cornPlant;
 	public static Block					saltOre;
 	
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		
 		cucumberPlantID = config.getBlock("Cucumber Plant ID", 510).getInt();
 		tomatoPlantID = config.getBlock("Tomato Plant ID", 511).getInt();
 		pepperPlantID = config.getBlock("Pepper Plant ID", 512).getInt();
 		saladPlantID = config.getBlock("Salad Plant ID", 513).getInt();
 		onionPlantID = config.getBlock("Onion Plant ID", 514).getInt();
 		chiliPlantID = config.getBlock("Chili Plant ID", 515).getInt();
 		paprikaPlantID = config.getBlock("Paprika Plant ID", 516).getInt();
 		ricePlantID = config.getBlock("Rice Plant ID", 517).getInt();
 		cornPlantID = config.getBlock("Corn Plant ID", 518).getInt();
 		saltOreID = config.getBlock("Salt Ore ID", 519).getInt();
 		
 		itemsID = config.getItem("Items ID", 13000).getInt();
 		
 		config.save();
 	}
 	
 	@EventHandler
 	public void init(FMLInitializationEvent e)
 	{
 		GameRegistry.registerWorldGenerator(new WorldGenHandler());
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 		
 		addItems();
 		addBlocks();
 		addCraftingRecipes();
 		addSmeltingRecipes();
 		
 		MinecraftForge.addGrassSeed(new ItemStack(edibles, 1, 19), 10);
 		MinecraftForge.setBlockHarvestLevel(saltOre, "pickaxe", 1);
 	}
 	
 	private void addBlocks()
 	{
 		cucumberPlant = new BlockPlantMoreFood(cucumberPlantID, 3, new ItemStack(edibles, 1, 1), new ItemStack(edibles, 1, 1), "cucumber");
 		tomatoPlant = new BlockPlantMoreFood(tomatoPlantID, 3, new ItemStack(edibles, 1, 5), new ItemStack(edibles, 1, 5), "tomato");
 		pepperPlant = new BlockPlantMoreFood(pepperPlantID, 3, new ItemStack(edibles, 1, 18), new ItemStack(edibles, 1, 18), "pepper");
 		saladPlant = new BlockPlantMoreFood(saladPlantID, 3, new ItemStack(edibles, 1, 0), new ItemStack(edibles, 1, 0), "salad");
 		onionPlant = new BlockPlantMoreFood(onionPlantID, 4, new ItemStack(edibles, 1, 9), new ItemStack(edibles, 1, 9), "onion");
 		chiliPlant = new BlockPlantMoreFood(chiliPlantID, 6, new ItemStack(edibles, 1, 4), new ItemStack(edibles, 1, 4), "chili");
 		paprikaPlant = new BlockPlantMoreFood(paprikaPlantID, 6, new ItemStack(edibles, 1, 6), new ItemStack(edibles, 1, 6), "paprika");
 		ricePlant = new BlockPlantMoreFood(ricePlantID, 6, new ItemStack(edibles, 1, 3), new ItemStack(edibles, 1, 3), "rice");
 		cornPlant = new BlockPlantMoreFood(cornPlantID, 6, new ItemStack(edibles, 1, 24), new ItemStack(edibles, 1, 24), "corn");
 		saltOre = (new BlockSaltOre(saltOreID)).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("saltOre").func_111022_d("saltore");
 		
 		CSBlocks.addBlock(saltOre, "Salt Ore");
 	}
 	
 	private void addItems()
 	{
 		salt = (ItemMoreFood) new ItemMoreFood(itemsID).setUnlocalizedName("salt");
 		pepper = (ItemMoreFood) new ItemMoreFood(itemsID + 1).setUnlocalizedName("pepper");
 		cinnamon = (ItemMoreFood) new ItemMoreFood(itemsID + 2).setUnlocalizedName("cinnamon");
 		vanilla = (ItemMoreFood) new ItemMoreFood(itemsID + 3).setUnlocalizedName("vanilla");
 		honeyDrop = (ItemMoreFood) new ItemMoreFood(itemsID + 4).setUnlocalizedName("honeyDrop").func_111206_d("honeydrop");
 		chocolateCookie = (ItemFoodMoreFood) new ItemFoodMoreFood(itemsID + 5, 2, 1.0F).setUnlocalizedName("chocolateCookie").func_111206_d("chocolatecookie");
 		chocolateBar = (ItemFoodMoreFood) new ItemFoodMoreFood(itemsID + 6, 4, 1.0F).setUnlocalizedName("chocolateBar").func_111206_d("chocolatebar");
 		butter = (ItemFoodMoreFood) new ItemFoodMoreFood(itemsID + 7, 3, 1F).setUnlocalizedName("butter");
 		cereals = (CustomItem) new CustomItem(itemsID + 8, new String[] { "Cereals", "Cereals" }, new String[] { "cereals_1", "cereals_2" }).setUnlocalizedName("cereals");
 		juice = (ItemJuice) new ItemJuice(itemsID + 10).setUnlocalizedName("juice");
 		chocolateBucket = (ItemBucketMilk) new ItemBucketMilk(itemsID + 11).setUnlocalizedName("chocolateBucket").func_111206_d("chocolatebucket");
 		cookedCarrot = (ItemFoodMoreFood) new ItemFoodMoreFood(itemsID + 12, 4, 1.0F).setUnlocalizedName("cookedCarrot").func_111206_d("carrot_cooked");
 		goldenPotato = (ItemAppleGold) new ItemAppleGold(itemsID + 13, 5, 1.0F, false).setUnlocalizedName("goldenPotato").func_111206_d("potato_gold");
 		diamondCarrot = (ItemDiamondFood) new ItemDiamondFood(itemsID + 14, 6, 1.2F, false).setUnlocalizedName("diamondCarrot").func_111206_d("carrot_diamond");
 		diamondPotato = (ItemDiamondFood) new ItemDiamondFood(itemsID + 15, 7, 1.3F, false).setUnlocalizedName("diamondPotato").func_111206_d("potato_diamond");
 		diamondApple = (ItemDiamondFood) new ItemDiamondFood(itemsID + 16, 8, 1.5F, false).setUnlocalizedName("diamondApple").func_111206_d("apple_diamond");
 		stompedCarrot = (ItemFoodMoreFood) new ItemFoodMoreFood(itemsID + 17, 4, 1.0F).setUnlocalizedName("stompedCarrot").func_111206_d("carrot_stomped");
 		stompedPotato = (ItemFoodMoreFood) new ItemFoodMoreFood(itemsID + 18, 5, 1.0F).setUnlocalizedName("stompedPotato").func_111206_d("potato_stomped");
 		stompedApple = (ItemFoodMoreFood) new ItemFoodMoreFood(itemsID + 19, 6, 1.0F).setUnlocalizedName("stompedApple").func_111206_d("apple_stomped");
 		edibles = (ItemEdibles) new ItemEdibles(itemsID + 20, 3, 1.0F).setUnlocalizedName("edibleIgredient");
 		fertilizer = (ItemFertilizer) new ItemFertilizer(itemsID + 21).setUnlocalizedName("fertilizer");
 		milkBowls = (ItemMilkBowls) new ItemMilkBowls(itemsID + 22, 4).setUnlocalizedName("cerealsWithMilk");
 		soupBowls = (ItemSoupBowls) new ItemSoupBowls(itemsID + 23, 6).setUnlocalizedName("soups");
 		
 		CSItems.addItem(salt, "Salt");
 		CSItems.addItemWithShapelessRecipe(pepper, "Pepper", 4, new Object[] { new ItemStack(edibles, 1, 18) });
 		CSItems.addItemWithShapelessRecipe(cinnamon, "Cinnamon", 3, new Object[] { new ItemStack(Item.dyePowder, 1, 3), Item.sugar, Item.sugar });
 		CSItems.addItem(vanilla, "Vanilla");
 		CSItems.addItem(honeyDrop, "Honey Drop");
 		CSItems.addItemWithRecipe(chocolateCookie, "Chocolate Cookie", 8, new Object[] { "wcw", 'w', Item.wheat, 'c', chocolateBar });
 		CSItems.addItem(chocolateBar, "Chocolate Bar");
 		CSItems.addItem(butter, "Butter");
 		CSItems.addItem(cereals, "Cereals");
 		CSItems.addItemWithShapelessRecipe(chocolateBucket, "Chocolate Bucket", 1, new Object[] { new ItemStack(Item.dyePowder, 1, 3), new ItemStack(Item.dyePowder, 1, 3), Item.sugar, Item.bucketMilk });
 		CSItems.addItem(cookedCarrot, "Cooked Carrot");
 		CSItems.addItemWithRecipe(goldenPotato, "Golden Potato", 1, new Object[] { "xxx", "xXx", "xxx", 'x', Item.goldNugget, 'X', Item.potato });
 		CSItems.addItemWithRecipe(diamondCarrot, "Diamond Carrot", 1, new Object[] { "xxx", "xXx", "xxx", 'x', Item.diamond, 'X', Item.carrot });
 		CSItems.addItemWithRecipe(diamondPotato, "Diamond Potato", 1, new Object[] { "xxx", "xXx", "xxx", 'x', Item.diamond, 'X', Item.potato });
 		CSItems.addItemWithRecipe(diamondApple, "Diamond Apple", 1, new Object[] { "xxx", "xXx", "xxx", 'x', Item.diamond, 'X', Item.appleRed });
 		CSItems.addItemWithShapelessRecipe(stompedCarrot, "Stomped Carrot", 1, new Object[] { Item.carrot });
 		CSItems.addItemWithShapelessRecipe(stompedPotato, "Stomped Potato", 1, new Object[] { Item.potato });
 		CSItems.addItemWithShapelessRecipe(stompedApple, "Stomped Apple", 1, new Object[] { Item.appleRed });
 		CSItems.addItemWithRecipe(fertilizer, "Fertilizer", 16, new Object[] { " w ", "sDs", " w ", 'w', Item.wheat, 's', Item.seeds, 'D', Block.dirt });
 	}
 	
 	private void addCraftingRecipes()
 	{
 		CSCrafting.addCrafting(true, new ItemStack(cereals, 1, 0), Item.wheat);
 		CSCrafting.addCrafting(true, new ItemStack(cereals, 1, 1), Item.wheat, new ItemStack(Item.dyePowder, 1, 3));
 		
 		CSCrafting.addCrafting(new ItemStack(goldenPotato, 1, 1), new Object[] { "xxx", "xXx", "xxx", 'x', Block.blockGold, 'X', Item.potato });
 
 		for (int i = 0; i < 7; i++)
 		{
 			ItemStack theSoup = new ItemStack(soupBowls, 1, i);
 			CSCrafting.addCrafting(true, ItemSoupBowls.addModifierToItemStack(theSoup.copy(), true, ItemSoupBowls.isPeppered(theSoup)), new Object[] { theSoup, salt });
 			CSCrafting.addCrafting(true, ItemSoupBowls.addModifierToItemStack(theSoup.copy(), ItemSoupBowls.isSalted(theSoup), true), new Object[] { theSoup, pepper });
			CSCrafting.addCrafting(true, ItemSoupBowls.addModifierToItemStack(theSoup.copy(), true, true), new Object[] { theSoup, pepper, salt });
 			CSCrafting.addCrafting(true, ItemSoupBowls.addModifierToItemStack(theSoup.copy(), true, ItemSoupBowls.isPeppered(theSoup)), new Object[] { theSoup, salt });
 			CSCrafting.addCrafting(true, ItemSoupBowls.addModifierToItemStack(theSoup.copy(), ItemSoupBowls.isSalted(theSoup), true), new Object[] { theSoup, pepper });
 			CSCrafting.addCrafting(true, ItemSoupBowls.addModifierToItemStack(theSoup.copy(), true, true), new Object[] { theSoup, pepper, salt });
 		}
 		
 		CSCrafting.addCrafting(true, new ItemStack(soupBowls, 1, 0), new Object[] { Item.bowlEmpty, Item.bucketWater });
 		for (int i = 0; i <= 7; i += 7)
 		{
 			CSCrafting.addCrafting(true, new ItemStack(soupBowls, 1, 1 + i), new Object[] { new ItemStack(soupBowls, 1, 0 + i), Item.bakedPotato });
 			CSCrafting.addCrafting(true, new ItemStack(soupBowls, 1, 2 + i), new Object[] { new ItemStack(soupBowls, 1, 0 + i), cookedCarrot });
 			CSCrafting.addCrafting(true, new ItemStack(soupBowls, 1, 3 + i), new Object[] { new ItemStack(soupBowls, 1, 0 + i), cookedCarrot, Item.bakedPotato });
 			CSCrafting.addCrafting(true, new ItemStack(soupBowls, 1, 4 + i), new Object[] { new ItemStack(soupBowls, 1, 0 + i), new ItemStack(edibles, 1, 5) });
 			CSCrafting.addCrafting(true, new ItemStack(soupBowls, 1, 5 + i), new Object[] { new ItemStack(soupBowls, 1, 4 + i), new ItemStack(edibles, 1, 3) });
 			CSCrafting.addCrafting(true, new ItemStack(soupBowls, 1, 6 + i), new Object[] { new ItemStack(soupBowls, 1, 0 + i), new ItemStack(edibles, 1, 2) });
 		}
 		
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 0), new Object[] { Item.bowlEmpty, Item.bucketMilk });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 1), new Object[] { new ItemStack(milkBowls, 1, 0), cereals });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 2), new Object[] { new ItemStack(milkBowls, 1, 0), new ItemStack(cereals, 1, 1) });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 3), new Object[] { new ItemStack(milkBowls, 1, 0), cereals, new ItemStack(cereals, 1, 1) });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 4), new Object[] { new ItemStack(milkBowls, 1, 0), new ItemStack(edibles, 1, 3) });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 5), new Object[] { new ItemStack(milkBowls, 1, 0), new ItemStack(edibles, 1, 3), cinnamon });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 5), new Object[] { new ItemStack(milkBowls, 1, 4), cinnamon });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 6), new Object[] { new ItemStack(milkBowls, 1, 0), new ItemStack(edibles, 1, 3), vanilla });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 6), new Object[] { new ItemStack(milkBowls, 1, 4), vanilla });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 7), new Object[] { new ItemStack(milkBowls, 1, 0), new ItemStack(Item.dyePowder, 1, 1) });
 		CSCrafting.addCrafting(true, new ItemStack(milkBowls, 1, 8), new Object[] { new ItemStack(milkBowls, 1, 0), new ItemStack(Item.dyePowder, 1, 10) });
 		
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 8, 2), new Object[] { Item.wheat, Item.wheat, salt });
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 2, 8), new Object[] { Item.porkCooked, Item.beefRaw });
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 2, 10), new Object[] { Item.bread });
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 1, 13), new Object[] { Item.bucketMilk, butter });
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 4, 14), new Object[] { new ItemStack(edibles, 1, 13) });
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 3, 15), new Object[] { Item.porkRaw });
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 1, 26), new Object[] { new ItemStack(edibles, 1, 25), Item.sugar });
 		CSCrafting.addCrafting(true, new ItemStack(edibles, 1, 27), new Object[] { new ItemStack(edibles, 1, 25), salt });
 		
 		CSCrafting.addCrafting(new ItemStack(edibles, 4, 7), new Object[] { "bb", 'b', Item.beefRaw });
 		CSCrafting.addCrafting(new ItemStack(edibles, 2, 11), new Object[] { "bb", 'b', Item.bread });
 		CSCrafting.addCrafting(new ItemStack(edibles, 1, 16), new Object[] { " b ", "smc", " b ", 'b', new ItemStack(edibles, 1, 10), 's', new ItemStack(edibles, 1, 0), 'm', new ItemStack(edibles, 1, 8), 'c', new ItemStack(edibles, 1, 14) });
 		CSCrafting.addCrafting(new ItemStack(edibles, 1, 17), new Object[] { "tst", "www", 't', new ItemStack(edibles, 1, 5), 's', new ItemStack(edibles, 1, 7), 'w', Item.wheat });
 		CSCrafting.addCrafting(new ItemStack(edibles, 1, 19), new Object[] { "c", "t", 'c', new ItemStack(edibles, 1, 14), 't', new ItemStack(edibles, 1, 12) });
 		CSCrafting.addCrafting(new ItemStack(edibles, 1, 20), new Object[] { "s", "t", 's', new ItemStack(edibles, 1, 7), 't', new ItemStack(edibles, 1, 12) });
 		CSCrafting.addCrafting(new ItemStack(edibles, 1, 22), new Object[] { "eee", 'e', new ItemStack(edibles, 1, 23) });
 		CSCrafting.addCrafting(new ItemStack(edibles, 1, 28), new Object[] { "s  ", " s ", "  s", 's', Item.sugar });
 		
 		CSCrafting.addCrafting(true, new ItemStack(juice, 1, 0), new Object[] { Item.glassBottle, stompedApple });
 		CSCrafting.addCrafting(true, new ItemStack(juice, 1, 0), new Object[] { Item.glassBottle, new ItemStack(edibles, 1, 5) });
 	}
 	
 	private void addSmeltingRecipes()
 	{
 		CSCrafting.addSmelting(new ItemStack(chocolateBucket), new ItemStack(chocolateBar), 0.2F);
 		CSCrafting.addSmelting(new ItemStack(Item.bucketMilk), new ItemStack(butter), 0.2F);
 		CSCrafting.addSmelting(new ItemStack(Item.carrot), new ItemStack(cookedCarrot), 0.1F);
 		CSCrafting.addSmelting(new ItemStack(edibles, 1, 11), new ItemStack(edibles, 1, 12), 0.1F);
 		CSCrafting.addSmelting(new ItemStack(edibles, 1, 15), new ItemStack(edibles, 1, 21), 0.1F);
 		CSCrafting.addSmelting(new ItemStack(Item.egg), new ItemStack(edibles, 1, 23), 0.1F);
 		CSCrafting.addSmelting(new ItemStack(edibles, 1, 24), new ItemStack(edibles, 4, 25), 0.1F);
 		CSCrafting.addSmelting(new ItemStack(soupBowls, 1, 0), new ItemStack(soupBowls, 1, 7), 0F);
 	}
 	
 	public static void generate(Random random, int chunkX, int chunkZ, World world)
 	{
 		for (int i = 0; i < 10; i++)
 		{
 			int randPosX = chunkX * 16 + random.nextInt(16);
 			int randPosY = random.nextInt(48);
 			int randPosZ = chunkZ * 16 + random.nextInt(16);
 			if (world.getBiomeGenForCoords(randPosX, randPosZ) instanceof BiomeGenOcean)
 				(new WorldGenMinable(saltOre.blockID, 6)).generate(world, random, randPosX, randPosY, randPosZ);
 		}
 		if (random.nextInt(100) == 0)
 		{
 			int randPosX = chunkX * 16 + random.nextInt(16);
 			int randPosY = 128;
 			int randPosZ = chunkZ * 16 + random.nextInt(16);
 			for (int j = randPosY; j > 0; j--)
 			{
 				if (Block.blocksList[world.getBlockId(randPosX, randPosY, randPosZ)] != null && Block.blocksList[world.getBlockId(randPosX, randPosY, randPosZ)].isBlockSolid(world, randPosX, randPosY, randPosZ, 0) && (world.getBlockId(randPosX, randPosY, randPosZ) == Block.grass.blockID || world.getBlockId(randPosX, randPosY, randPosZ) == Block.dirt.blockID))
 				{
 					randPosY = j;
 					break;
 				}
 				else
 				{
 					randPosY--;
 				}
 			}
 			if (world.getBlockId(randPosX, randPosY, randPosZ) == Block.grass.blockID || world.getBlockId(randPosX, randPosY, randPosZ) == Block.dirt.blockID)
 			{
 				(new WorldGenGardener()).generate(world, random, randPosX, randPosY, randPosZ);
 			}
 		}
 	}
 	
 	public class WorldGenHandler implements IWorldGenerator
 	{
 		@Override
 		public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
 		{
 			if (world.provider.isSurfaceWorld())
 			{
 				MoreFoodMod.generate(random, chunkX, chunkZ, world);
 			}
 		}
 	}
 }
