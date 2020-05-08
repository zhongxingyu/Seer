 package FuzzCraft.Base;
 
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import FuzzCraft.Blocks.*;
 import FuzzCraft.Handlers.*;
 import FuzzCraft.Items.*;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="fuzzcraft", name="fuzzcraft", version="0.1.0a")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 
 public class fuzzcraft {
     
     private static Property blockIdFlower, blockIdStone, blockIdBrick, blockIdChisBrick, blockIdColorizor,
         blockIdRep, itemIdCharge0, itemIdCharge1, itemIdCharge2, itemIdCharge3, itemIdCharge4, itemIdCharge5,
         itemIdCharge6, itemIdCharge7, itemIdCharge8, itemIdCharge9, itemIdCharge10, itemIdCharge11, itemIdCharge12,
         itemIdCharge13, itemIdCharge14;
     
     public static int repulsorPower = 15;
     
     private Colorizor_Handler guiHandler = new Colorizor_Handler();
     
     public static ColorCharge colorCharge0, colorCharge1, colorCharge2, colorCharge3, 
     colorCharge4, colorCharge5, colorCharge6, colorCharge7, colorCharge8, colorCharge9, 
     colorCharge10, colorCharge11, colorCharge12, colorCharge13, colorCharge14;
     
     public static EnderFlower enderFlower;
     public static StoneBlock stoneBlock;
     public static BrickBlock brickBlock;
     public static ChisBrickBlock chisbrickBlock;
     public static Colorizor colorizorBlock;
     public static RepulsionBlock repulsionBlock;
     
         @Instance("fuzzcraft")
         public static fuzzcraft instance;
        
         @SidedProxy(clientSide=modinfo.CLIENT_PROXY, serverSide=modinfo.SERVER_PROXY)
         public static CommonProxy proxy;
         
         @PreInit
         public void preInit(FMLPreInitializationEvent event) {
             
             Configuration fc_config = new Configuration(event.getSuggestedConfigurationFile());
             
             try {
                 fc_config.load();
                 
                 blockIdStone = fc_config.getBlock("ID.Stone", 1500);
                 blockIdFlower = fc_config.getBlock("ID.Flower", 1501);
                 blockIdBrick = fc_config.getBlock("ID.Brick", 1502);
                 blockIdChisBrick = fc_config.getBlock("ID.Chiseled_Brick", 1503);
                 blockIdColorizor = fc_config.getBlock("ID.Colorizor", 1504);
                 blockIdRep = fc_config.getBlock("ID.Repulsor_Block", 1505);
                 itemIdCharge0 = fc_config.getItem("ID.White_Charge", 10000);
                 itemIdCharge1 = fc_config.getItem("ID.Orange_Charge", 10001);
                 itemIdCharge2 = fc_config.getItem("ID.Magenta_Charge", 10002);
                 itemIdCharge3 = fc_config.getItem("ID.LightBlue_Charge", 10003);
                 itemIdCharge4 = fc_config.getItem("ID.Yellow_Charge", 10004);
                 itemIdCharge5 = fc_config.getItem("ID.Lime_Charge", 10005);
                 itemIdCharge6 = fc_config.getItem("ID.Pink_Charge", 10006);
                 itemIdCharge7 = fc_config.getItem("ID.DarkGrey_Charge", 10007);
                 itemIdCharge8 = fc_config.getItem("ID.Cyan_Charge", 10008);
                 itemIdCharge9 = fc_config.getItem("ID.Purple_Charge", 10009);
                 itemIdCharge10 = fc_config.getItem("ID.Blue_Charge", 10010);
                 itemIdCharge11 = fc_config.getItem("ID.Brown_Charge", 10011);
                 itemIdCharge12 = fc_config.getItem("ID.Green_Charge", 10012);
                 itemIdCharge13 = fc_config.getItem("ID.Red_Charge", 10013);
                 itemIdCharge14 = fc_config.getItem("ID.Black_Charge", 10014);
    
                 Property rP = fc_config.get(Configuration.CATEGORY_GENERAL, "repulsion_power", 15);
                 rP.comment = "Set the power of Repulsion Blocks (Between 1 - 30, Default 15)"; 
                 repulsorPower = rP.getInt();
                 
                 if (repulsorPower == 0) {
                     repulsorPower = 1;
                 }
                 else if  (repulsorPower > 30) {
                     repulsorPower = 30;
                 }
             }
             catch (Exception e) {
                 FMLLog.log(Level.SEVERE, e, "Error loading FuzzCraft configuration file!");
             }
             finally {
                 FMLLog.log(Level.INFO, "FuzzCraft configuration loaded sucessfully! ");
                 fc_config.save();
             }
         }
        
         @Init
         public void load(FMLInitializationEvent event) {
             
             proxy.registerRenderers();
          
             // Init blocks
             enderFlower = new EnderFlower(blockIdFlower.getInt());
             stoneBlock = new StoneBlock(blockIdStone.getInt());
             brickBlock = new BrickBlock(blockIdBrick.getInt());
             chisbrickBlock = new ChisBrickBlock(blockIdChisBrick.getInt());
             colorizorBlock = new Colorizor(blockIdColorizor.getInt());
             repulsionBlock = new RepulsionBlock(blockIdRep.getInt(), repulsorPower);
             
            
             // Init Items
             colorCharge0 = new ColorCharge(itemIdCharge0.getInt());
             colorCharge1 = new ColorCharge(itemIdCharge1.getInt());
             colorCharge2 = new ColorCharge(itemIdCharge2.getInt());
             colorCharge3 = new ColorCharge(itemIdCharge3.getInt());
             colorCharge4 = new ColorCharge(itemIdCharge4.getInt());
             colorCharge5 = new ColorCharge(itemIdCharge5.getInt());
             colorCharge6 = new ColorCharge(itemIdCharge6.getInt());
             colorCharge7 = new ColorCharge(itemIdCharge7.getInt());
             colorCharge8 = new ColorCharge(itemIdCharge8.getInt());
             colorCharge9 = new ColorCharge(itemIdCharge9.getInt());
             colorCharge10 = new ColorCharge(itemIdCharge10.getInt());
             colorCharge11 = new ColorCharge(itemIdCharge11.getInt());
             colorCharge12 = new ColorCharge(itemIdCharge12.getInt());
             colorCharge13 = new ColorCharge(itemIdCharge13.getInt());
             colorCharge14 = new ColorCharge(itemIdCharge14.getInt());
             
             // Register Blocks //
             
             // Standard Blocks
             LanguageRegistry.addName(enderFlower, "Ender Flower");
             GameRegistry.registerBlock(enderFlower, "enderFlower");
             
             LanguageRegistry.addName(colorizorBlock, "Colorizor");
             GameRegistry.registerBlock(colorizorBlock, "colorizor");
             MinecraftForge.setBlockHarvestLevel(colorizorBlock, "Pick", 3);
             
             LanguageRegistry.addName(repulsionBlock, "Repulsion Block");
             GameRegistry.registerBlock(repulsionBlock, "repulsionBlock");
             MinecraftForge.setBlockHarvestLevel(repulsionBlock, "Pick", 0);
             
             // Color Charges
             GameRegistry.registerItem(colorCharge0, "colorCharge0");
             LanguageRegistry.addName(colorCharge0, "White Color Charge"); 
             
             GameRegistry.registerItem(colorCharge1, "colorCharge1");
             LanguageRegistry.addName(colorCharge1, "Orange Color Charge"); 
             
             GameRegistry.registerItem(colorCharge2, "colorCharge2");
             LanguageRegistry.addName(colorCharge2, "Magenta Color Charge"); 
             
             GameRegistry.registerItem(colorCharge3, "colorCharge3");
             LanguageRegistry.addName(colorCharge3, "Light Blue Color Charge"); 
             
             GameRegistry.registerItem(colorCharge4, "colorCharge4");
             LanguageRegistry.addName(colorCharge4, "Yellow Color Charge"); 
             
             GameRegistry.registerItem(colorCharge5, "colorCharge5");
             LanguageRegistry.addName(colorCharge5, "Lime Color Charge"); 
             
             GameRegistry.registerItem(colorCharge6, "colorCharge6");
             LanguageRegistry.addName(colorCharge6, "Pink Color Charge"); 
             
             GameRegistry.registerItem(colorCharge7, "colorCharge7");
             LanguageRegistry.addName(colorCharge7, "Dark Grey Color Charge"); 
             
             GameRegistry.registerItem(colorCharge8, "colorCharge8");
             LanguageRegistry.addName(colorCharge8, "Cyan Color Charge"); 
             
             GameRegistry.registerItem(colorCharge9, "colorCharge9");
             LanguageRegistry.addName(colorCharge9, "Purple Color Charge"); 
             
             GameRegistry.registerItem(colorCharge10, "colorCharge10");
             LanguageRegistry.addName(colorCharge10, "Blue Color Charge"); 
             
             GameRegistry.registerItem(colorCharge11, "colorCharge11");
             LanguageRegistry.addName(colorCharge11, "Brown Color Charge"); 
             
             GameRegistry.registerItem(colorCharge12, "colorCharge12");
             LanguageRegistry.addName(colorCharge12, "Green Color Charge"); 
             
             GameRegistry.registerItem(colorCharge13, "colorCharge13");
             LanguageRegistry.addName(colorCharge13, "Red Color Charge"); 
             
             GameRegistry.registerItem(colorCharge14, "colorCharge14");
             LanguageRegistry.addName(colorCharge14, "Black Color Charge"); 
             
            
             // Colored Stone
             
             MinecraftForge.setBlockHarvestLevel(stoneBlock, "Pick", 0);
             GameRegistry.registerBlock(stoneBlock, StoneBlockItem.class, "stoneBlock");
             for (int i = 0; i < 15; i++) {
                 LanguageRegistry.addName(new ItemStack(stoneBlock, 1, i),
                        StoneBlock.stoneBlockNames[i]); }
             
             // Colored Brick
             
             MinecraftForge.setBlockHarvestLevel(brickBlock, "Pick", 0);
             GameRegistry.registerBlock(brickBlock, BrickBlockItem.class, "brickBlock");
             for (int i = 0; i < 15; i++){
                 LanguageRegistry.addName(new ItemStack(brickBlock, 1, i),
                         BrickBlock.brickBlockNames[i]); }
             
             // Colored Chiseled Brick
             
             MinecraftForge.setBlockHarvestLevel(chisbrickBlock, "Pick", 0);
             GameRegistry.registerBlock(chisbrickBlock, ChisBrickBlockItem.class, "chisbrickBlock");
             for (int i = 0; i < 15; i++){
                 LanguageRegistry.addName(new ItemStack(chisbrickBlock, 1, i),
                         ChisBrickBlock.chisbrickBlockNames[i]); }
             
            // Brick Recipes
             
             for (int i = 0; i < 15; i++) {
                 ItemStack stoneStack = new ItemStack(stoneBlock, 1, i);
                 GameRegistry.addRecipe(new ItemStack(brickBlock, 1, i),
                        "xx", "xx", 'x', stoneStack); }
             
            // Chiseled Brick Recipes
             
             for (int i = 0; i <15; i++) {
                 ItemStack brickStack = new ItemStack(brickBlock, 1, i);
                 GameRegistry.addRecipe(new ItemStack(chisbrickBlock, 1, i),
                         "xx", "xx", 'x', brickStack); }
             
             // Temp Colorizor Recipes
             
            ItemStack stoneStack  = new ItemStack(Block.stone);
            
            for (int i = 0; i < 15; i++){
                
            }
            
             
             ItemStack colorizorStack0 = new ItemStack(fuzzcraft.colorCharge0, 64);
             ItemStack colorizorStack1 = new ItemStack(fuzzcraft.colorCharge1, 64);
             ItemStack colorizorStack2 = new ItemStack(fuzzcraft.colorCharge2, 64);
             ItemStack colorizorStack3 = new ItemStack(fuzzcraft.colorCharge3, 64);
             ItemStack colorizorStack4 = new ItemStack(fuzzcraft.colorCharge4, 64);
             ItemStack colorizorStack5 = new ItemStack(fuzzcraft.colorCharge5, 64);
             ItemStack colorizorStack6 = new ItemStack(fuzzcraft.colorCharge6, 64);
             ItemStack colorizorStack7 = new ItemStack(fuzzcraft.colorCharge7, 64);
             ItemStack colorizorStack8 = new ItemStack(fuzzcraft.colorCharge8, 64);
             ItemStack colorizorStack9 = new ItemStack(fuzzcraft.colorCharge9, 64);
             ItemStack colorizorStack10 = new ItemStack(fuzzcraft.colorCharge10, 64);
             ItemStack colorizorStack11 = new ItemStack(fuzzcraft.colorCharge11, 64);
             ItemStack colorizorStack12 = new ItemStack(fuzzcraft.colorCharge12, 64);
             ItemStack colorizorStack13 = new ItemStack(fuzzcraft.colorCharge13, 64);
             ItemStack colorizorStack14 = new ItemStack(fuzzcraft.colorCharge14, 64); 
             
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 0), stoneStack, colorizorStack0);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 1), stoneStack, colorizorStack1);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 2), stoneStack, colorizorStack2);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 3), stoneStack, colorizorStack3);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 4), stoneStack, colorizorStack4);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 5), stoneStack, colorizorStack5);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 6), stoneStack, colorizorStack6);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 7), stoneStack, colorizorStack7);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 8), stoneStack, colorizorStack8);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 9), stoneStack, colorizorStack9);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 10), stoneStack, colorizorStack10);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 11), stoneStack, colorizorStack11);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 12), stoneStack, colorizorStack12);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 13), stoneStack, colorizorStack13);
             GameRegistry.addShapelessRecipe(new ItemStack(fuzzcraft.stoneBlock, 1, 14), stoneStack, colorizorStack14); 
                 
             NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
             GameRegistry.registerTileEntity(FuzzCraft.TileEntity.colorizor_tileEntity.class,"colorizor_tileEntity");
             
         }
             
         
         @PostInit
         public void postInit(FMLPostInitializationEvent event) {
         }
   }
