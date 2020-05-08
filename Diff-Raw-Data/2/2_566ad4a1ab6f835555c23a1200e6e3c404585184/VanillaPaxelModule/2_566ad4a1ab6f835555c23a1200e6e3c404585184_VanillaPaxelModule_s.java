 package ictrobot.gems.module;
 
 import ictrobot.core.helper.tool.ToolMaterials;
 import ictrobot.core.tool.Paxel;
 import ictrobot.core.helper.config.ConfigHelper;
 import ictrobot.core.helper.register.Register;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class VanillaPaxelModule {
   
   //Define IDs - Vanilla Paxel
   public static int woodPaxelID;
   public static int stonePaxelID;
   public static int ironPaxelID;
   public static int goldPaxelID;
   public static int diamondPaxelID;
   //Define Items - Vanilla Paxel
   public static Item woodPaxel;
   public static Item stonePaxel;
   public static Item ironPaxel;
   public static Item goldPaxel;
   public static Item diamondPaxel;
     
     public static void Config(int ID) {
       ConfigHelper.file("VanillaPaxel", ID);
       woodPaxelID = ConfigHelper.item("woodPaxelID");
       stonePaxelID = ConfigHelper.item("stonePaxelID");
       ironPaxelID = ConfigHelper.item("ironPaxelID");
       goldPaxelID = ConfigHelper.item("goldPaxelID");
       diamondPaxelID = ConfigHelper.item("diamondPaxelID");
       
       ConfigHelper.save();
     }
 
     public static void Settings() {
       //Tools - Vanilla Paxel
       woodPaxel = (new Paxel(woodPaxelID, ToolMaterials.Wood));
       stonePaxel = (new Paxel(stonePaxelID, ToolMaterials.Stone));
       ironPaxel = (new Paxel(ironPaxelID, ToolMaterials.Iron));
       goldPaxel = (new Paxel(goldPaxelID, ToolMaterials.Gold));
       diamondPaxel = (new Paxel(diamondPaxelID, ToolMaterials.Diamond));
       //Register Recipes - Vanilla Paxel
       GameRegistry.addRecipe(new ItemStack(woodPaxel), "spa", " t ", " t ", 's', new ItemStack(Item.shovelWood, 1, 0), 'p', new ItemStack(Item.pickaxeWood, 1, 0), 'a', new ItemStack(Item.axeWood, 1, 0), 't', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(stonePaxel), "spa", " t ", " t ", 's', new ItemStack(Item.shovelStone, 1, 0), 'p', new ItemStack(Item.pickaxeStone, 1, 0), 'a', new ItemStack(Item.axeStone, 1, 0), 't', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(ironPaxel), "spa", " t ", " t ", 's', new ItemStack(Item.shovelIron, 1, 0), 'p', new ItemStack(Item.pickaxeIron, 1, 0), 'a', new ItemStack(Item.axeIron, 1, 0), 't', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(goldPaxel), "spa", " t ", " t ", 's', new ItemStack(Item.shovelGold, 1, 0), 'p', new ItemStack(Item.pickaxeGold, 1, 0), 'a', new ItemStack(Item.axeGold, 1, 0), 't', new ItemStack(Item.stick));
       GameRegistry.addRecipe(new ItemStack(diamondPaxel), "spa", " t ", " t ", 's', new ItemStack(Item.shovelDiamond, 1, 0), 'p', new ItemStack(Item.pickaxeDiamond, 1, 0), 'a', new ItemStack(Item.axeDiamond, 1, 0), 't', new ItemStack(Item.stick));
     }
     
     public static void Register(){
       //Items - Vanilla Paxel
       Register.Item(woodPaxel, "Wooden Paxel");
      Register.Item(stonePaxel, "Stone paxel");
       Register.Item(ironPaxel, "Iron Paxel");
       Register.Item(goldPaxel, "Gold Paxel");
       Register.Item(diamondPaxel, "Diamond Paxel");
     }
 }
