 package roy.firstmod;
 
 //This Import list will grow longer with each additional tutorial.
 //It's not pruned between full class postings, unlike other tutorial code.
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid = "GeorgesMod", name = "GeorgesMod", version = "0.0.1")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true, channels = { "GeorgesModRandom" })
 public class GeorgesMod {
 	
 	public static Item blackdiamond = (new Item(605)).setUnlocalizedName("blackdiamond")
 			.setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureName("firstmod:blackdiamond");
 	static {
 		
 		GameRegistry.registerItem(blackdiamond, "blackdiamond");
 		LanguageRegistry.addName(blackdiamond, "Black Diamond");
 	}
 	
 	public static final Block oreBlackDiamond = (new BlockOreDropper(606))
 			.setDropItem(605)
 			.setHardness(3.0F).setResistance(5.0F)
 			.setStepSound(Block.soundStoneFootstep)
			.setUnlocalizedName("blackdiamondore").setTextureName("firstmod:black_diamond_ore");
     static {
 
 		GameRegistry.registerBlock(oreBlackDiamond, "blackdiamondore");
 		LanguageRegistry.addName(oreBlackDiamond, "Black Diamond Ore");
     }
     
  @Instance("GeorgesMod")
  public static GeorgesMod instance;
 
  @SidedProxy(clientSide = "roy.firstmod.client.ClientProxy", serverSide = "roy.firstmod.CommonProxy")
  public static CommonProxy proxy;
 
  @EventHandler
  public void preInit (FMLPreInitializationEvent event) {
      // Stub Method
  }
 
  @EventHandler
  public void load (FMLInitializationEvent event) {
      proxy.registerRenderers();
 
      ItemStack dirtStack = new ItemStack(Block.dirt);
      ItemStack diamondsStack = new ItemStack(Item.diamond, 64);
      ItemStack leatherStack = new ItemStack(Item.leather);
      ItemStack stringStack = new ItemStack(Item.silk);
      ItemStack IronStack = new ItemStack(Item.ingotIron);
      ItemStack SaddleStack = new ItemStack(Item.saddle);
      ItemStack GoldStack = new ItemStack(Item.ingotGold);
      ItemStack HopperStack = new ItemStack(Block.hopperBlock);
      ItemStack IronBlockStack = new ItemStack(Block.blockIron);
      ItemStack dragonEggStack = new ItemStack(Block.dragonEgg);
      ItemStack endPortalFrameStack = new ItemStack(Block.endPortalFrame);
      ItemStack gunpowderStack = new ItemStack(Item.gunpowder);
      ItemStack tntStack = new ItemStack(Block.tnt);
      ItemStack endstone = new ItemStack(Block.whiteStone);
      ItemStack torchRedstoneActiveStack = new ItemStack(Block.torchRedstoneActive);
      ItemStack enderPearlStack = new ItemStack(Item.enderPearl);
 
 //     GameRegistry.addShapelessRecipe(diamondsStack, dirtStack, dirtStack,
 //             dirtStack, dirtStack, dirtStack, dirtStack, new ItemStack(
 //                     Block.sand), gravelStack, cobbleStack);
 //
 //     GameRegistry.addRecipe(new ItemStack(Block.cobblestone), "xy", "yx",
 //             'x', dirtStack, 'y', gravelStack);
 //
      GameRegistry.addRecipe(new ItemStack(Item.saddle), 
     		 "x x", 
     		 "xxx", 
     		 " y ",
              'x', leatherStack, 'y', stringStack);
      
      GameRegistry.addRecipe(new ItemStack(Item.helmetChain),
     		 " x ",
     		 "x x",
     		 "   ",
     		 'x', IronStack);
      GameRegistry.addRecipe(new ItemStack(Item.plateChain),
     		 "x x",
     		 " x ",
     		 "x x",
     		 'x', IronStack);
      GameRegistry.addRecipe(new ItemStack(Item.legsChain),
     		 " x ",
     		 "x x",
     		 "x x",
     		 'x', IronStack);
      GameRegistry.addRecipe(new ItemStack(Item.bootsChain),
     		 "   ",
     		 "x x",
     		 "   ",
     		 'x', IronStack);
      GameRegistry.addRecipe(new ItemStack(Item.horseArmorDiamond),
     		 "xyx",
     		 "xxx",
     		 "zzz",
     		 'x', diamondsStack, 'y', SaddleStack, 'z', leatherStack);
      GameRegistry.addRecipe(new ItemStack(Item.horseArmorIron),
     		 "xyx",
     		 "xxx",
     		 "zzz",
     		 'x', IronStack, 'y', SaddleStack, 'z', leatherStack);
      GameRegistry.addRecipe(new ItemStack(Item.horseArmorGold),
     		 "xyx",
     		 "xxx",
     		 "zzz",
     		 'x', GoldStack, 'y', SaddleStack, 'z', leatherStack);
 	GameRegistry.addRecipe(new ItemStack(Item.minecartHopper),
     		 "xyx",
     		 "xxx",
     		 "zyz",
     		 'x', IronStack, 'y', HopperStack, 'z', IronBlockStack);
 	GameRegistry.addRecipe(new ItemStack(Item.netherStar),
 			"xxx",
 			"xyx",
 			"xxx",
 			'x', endstone, 'y', endPortalFrameStack);
 	GameRegistry.addRecipe(new ItemStack(Item.minecartTnt),
 			"xyx",
 			"xxx",
 			"zyz",
 			'x', gunpowderStack, 'y', tntStack, 'z', torchRedstoneActiveStack);
 	GameRegistry.addRecipe(new ItemStack(Block.endPortalFrame),
 			"xxx",
 			"xyx",
 			"xxx",
 			'x', endstone, 'y', enderPearlStack);
 	
 //     GameRegistry.addSmelting(Block.stone.blockID, new ItemStack(
 //             Block.stoneBrick), 0.1f);
 //
 //     FurnaceRecipes.smelting().addSmelting(Block.cloth.blockID, 15,
 //             new ItemStack(Block.cloth, 1, 0), 0.1f);
 
  }
 
  @EventHandler
  public void postInit (FMLPostInitializationEvent event) {
  }
 }
