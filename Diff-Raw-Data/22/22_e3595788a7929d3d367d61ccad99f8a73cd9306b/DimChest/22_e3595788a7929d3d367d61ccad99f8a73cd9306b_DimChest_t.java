 package mods.firstspring.dimchest;
 
 import java.util.Iterator;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemMultiTextureTile;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStopping;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStoppingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid="DimChest", name="DimensionalChest", version="Build 2")
 //パケットハンドラの登録
 @NetworkMod(channels = {"DCClient","DCServer"}, clientSideRequired=true, serverSideRequired=true, packetHandler = PacketHandler.class)
 public class DimChest {
 
 	public static Block dimChest;
 	public static int dimChestID;
 	public static Item linkCard;
 	public static int linkCardID;
 	public static Item endPanel;
 	public static int endPanelID;
 	public static Item endCircuitBoard;
 	public static int endCircuitBoardID;
 	//使用されていないレンダリングタイプを取得
 	public static int dimChestRenderID = RenderingRegistry.getNextAvailableRenderId();
 	//DimChest
 	public static boolean doBreak;
 	public static boolean recipeBlock;
 	public static boolean recipeLinkChest;
 	//LinkChest
 	public static boolean recipeEmerald;
 	public static boolean isEnableModInventory;
 	public static final String[] dyeName = new String[] {"dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"};
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event){
 		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
 		cfg.load();
 		dimChestID = cfg.get(Configuration.CATEGORY_BLOCK, "DimChest_ID", 1845).getInt();
 		doBreak = cfg.get(Configuration.CATEGORY_GENERAL, "Do_chest_break_like_a_EnderChest", true).getBoolean(true);
 		recipeBlock = cfg.get(Configuration.CATEGORY_GENERAL, "Recipe_Iron_Block", false).getBoolean(true);
 		recipeLinkChest = cfg.get(Configuration.CATEGORY_GENERAL, "Recipe_Use_LinkChest", true).getBoolean(true);
 		linkCardID = cfg.get(Configuration.CATEGORY_ITEM, "LinkCard_ID", 18450).getInt();
 		endPanelID = cfg.get(Configuration.CATEGORY_ITEM, "EndPanel_ID", 18451).getInt();
 		endCircuitBoardID = cfg.get(Configuration.CATEGORY_ITEM, "EndCircuitBoard_ID", 18452).getInt();
 		isEnableModInventory = cfg.get(Configuration.CATEGORY_GENERAL, "Enable_MOD_Inventory", false).getBoolean(true);
 		recipeEmerald = cfg.get(Configuration.CATEGORY_GENERAL, "Recipe_Emerald", false).getBoolean(true);
 		cfg.save();
 		//TileEntityの登録
 		GameRegistry.registerTileEntity(TileEntityDimChest.class, "DimChest");
 		//クライアント側でレンダーの登録
 		CommonProxy.proxy.registerRenderer();
 		//GUIの表示名を登録
 		LanguageRegistry.instance().addStringLocalization("container.linkchest", "Link Chest");
 		LanguageRegistry.instance().addStringLocalization("container.linkchest", "ja_JP", "接続チェスト");
 		LanguageRegistry.instance().addStringLocalization("container.dimchest", "Dimensional Chest");
 		LanguageRegistry.instance().addStringLocalization("container.dimchest", "ja_JP", "次元チェスト");
 		//ブロックを登録
 		dimChest = new BlockDimChest(dimChestID).setCreativeTab(CreativeTabs.tabDecorations).setUnlocalizedName("dimchest").setHardness(1.0F);
 		new ItemMultiTextureTile(dimChestID - 256, dimChest, new String[]{"linkchest", "dimchest"});
 		//ブロックに名前を登録
 		//LanguageRegistry.addName(dimChest, "Link Chest");
 		//LanguageRegistry.instance().addNameForObject(dimChest, "ja_JP", "次元チェスト");
 		LanguageRegistry.instance().addStringLocalization("tile.dimchest.linkchest.name", "Link Chest");
 		LanguageRegistry.instance().addStringLocalization("tile.dimchest.linkchest.name", "ja_JP", "接続チェスト");
 		LanguageRegistry.instance().addStringLocalization("tile.dimchest.dimchest.name", "Dimensional Chest");
 		LanguageRegistry.instance().addStringLocalization("tile.dimchest.dimchest.name", "ja_JP", "次元チェスト");
 		//Tickハンドラを登録
 		TickRegistry.registerTickHandler(new TickHandler(), Side.SERVER);
 		
 		linkCard = new ItemLinkCard(linkCardID).setUnlocalizedName("linkcard").setCreativeTab(CreativeTabs.tabTransport);
 		LanguageRegistry.addName(linkCard, "Link Card");
 		LanguageRegistry.instance().addNameForObject(linkCard, "ja_JP", "リンクカード");
 		
 		endPanel = new Item(endPanelID).setUnlocalizedName("firstspring/dimchest:endpanel").setCreativeTab(CreativeTabs.tabMaterials);
 		LanguageRegistry.addName(endPanel, "End Panel");
 		LanguageRegistry.instance().addNameForObject(endPanel, "ja_JP", "終焉の外装");
 		
 		endCircuitBoard = new Item(endCircuitBoardID).setUnlocalizedName("firstspring/dimchest:endcircuit").setCreativeTab(CreativeTabs.tabMaterials);
 		LanguageRegistry.addName(endCircuitBoard, "End Circuit Board");
 		LanguageRegistry.instance().addNameForObject(endCircuitBoard, "ja_JP", "終焉の回路基板");
 
 	}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event){
 		List recipes = CraftingManager.getInstance().getRecipeList();
 		InventoryCrafting ic = new InventoryCrafting(new Container(){
 			@Override
 			public boolean canInteractWith(EntityPlayer entityplayer) {
 				return true;
 			}
 			
 			@Override
 			public void onCraftMatrixChanged(IInventory par1IInventory){}
 		}, 3, 3);
 		for(int i = 0; i < 8; i++){
 			if(i != 4)
 				ic.setInventorySlotContents(i, new ItemStack(Item.ingotIron));
 		}
 		for(Iterator<IRecipe> i = recipes.iterator(); i.hasNext();){
 			IRecipe r = i.next();
 			if(r.matches(ic, null)){
 				i.remove();
 			}
 		}
 		ItemStack craftbase1;
 		if(this.recipeBlock)
			craftbase1 = new ItemStack(Block.blockIron);
 		else
 			craftbase1 = new ItemStack(Item.ingotIron);
 		ItemStack craftbase2;
 		if(this.recipeEmerald)
 			craftbase2 = new ItemStack(Item.emerald);
 		else
 			craftbase2 = new ItemStack(Item.enderPearl);
 		//レシピ追加
 		if(recipeLinkChest)
 			GameRegistry.addRecipe(new ItemStack(dimChest, 1, 1), new Object[]{"III",
 																			"IEI",
 																			"III",
 																			'I', craftbase1, 'E', new ItemStack(dimChest)});
 		else
 			GameRegistry.addRecipe(new ItemStack(dimChest, 1, 1), new Object[]{"III",
 																			"I I",
 																			"III",
 																			'I', craftbase1});
 		
 		for(int i = 1;i<16;i++){
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(linkCard, 1, i), new Object[]{dyeName[i], new ItemStack(linkCard, 1, 32767)}));
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(linkCard, 1, i), new Object[]{dyeName[i], new ItemStack(endCircuitBoard, 1, 32767)}));
 		}
 		GameRegistry.addRecipe(new ItemStack(endPanel, 16), new Object[]{"III", "IEI", "III", 'I', Item.ingotIron, 'E', craftbase2});
 		GameRegistry.addRecipe(new ItemStack(dimChest, 1), new Object[]{"EEE", " C ", "EEE", 'E', endPanel, 'C', Block.chest});
 		GameRegistry.addRecipe(new ItemStack(dimChest, 1), new Object[]{"E E", "ECE", "E E", 'E', endPanel, 'C', Block.chest});
 		GameRegistry.addRecipe(new ItemStack(endCircuitBoard, 8), new Object[]{"RLR", "LEL", "RLR", 'R', Item.redstone, 'L', new ItemStack(Item.dyePowder, 1, 4), 'E', endPanel});
 		GameRegistry.addRecipe(new ItemStack(endCircuitBoard, 8), new Object[]{"LRL", "RER", "LRL", 'R', Item.redstone, 'L', new ItemStack(Item.dyePowder, 1, 4), 'E', endPanel});
 	}
 	
 	//サーバーが停止する時に呼ばれる
 	//全インベントリの読み込みを解除
 	//
 	@ServerStopping
 	public void unloadAll(FMLServerStoppingEvent event){
 		InventoryManager.unloadAll();
 	}
 }
 
