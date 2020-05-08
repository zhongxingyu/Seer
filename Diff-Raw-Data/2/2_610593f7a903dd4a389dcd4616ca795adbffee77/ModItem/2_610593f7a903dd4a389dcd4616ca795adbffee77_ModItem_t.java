 package com.cyntain.Fm.Item;
 import com.cyntain.Fm.lib.ItemIDs;
 
 
 //import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 /*Fuels Mod
  * 
  * @Authors: Cyntain and Paronamixxe
  * 
  * FuelsMod
  * 
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  * @credit: Visual_Argonian, Pahimar (enim mihi inspirante) and Plenty_of_Fish
  * */
 
 
 public class ModItem {
  
   /* Mod item instances */
     public static Item copperIngot;
     public static Item ChunkCopper;  
     public static Item ClusterCopper;
     public static Item osmiumIngot;
     public static Item ChunkOsmium;
     public static Item berylliumIngot;
     public static Item ChunkBeryllium;
     public static Item clusterBeryllium;
     public static Item zeoliteDust;
     public static Item zeoliteDustVial;
     
     public static void init(){
       
   /* Initialise each mod item individually */
         
         // COPPER ITEMs
       copperIngot = new itemCopperIngot(ItemIDs.COPPER_INGOT_DEFAULT); 
       LanguageRegistry.addName(copperIngot, "Copper Ingot");
       ChunkCopper = new itemCopperOreChunk(ItemIDs.COPPER_CHUNK_DEFAULT); 
       LanguageRegistry.addName(ChunkCopper, "Copper Ore Chunk");
       ClusterCopper = new itemClusterCopper(ItemIDs.COPPER_CLUSTER_DEFAULT);
       LanguageRegistry.addName(ClusterCopper, "Copper Ore Cluster");
      
       
       // OSMIUM ITEMs
       osmiumIngot = new itemOsmiumIngot(ItemIDs.OSMIUM_INGOT_DEFAULT);
       LanguageRegistry.addName(osmiumIngot, "Osmium Ingot");
       ChunkOsmium = new itemOsmiumChunk(ItemIDs.OSMIUM_CHUNK_DEFAULT);
       LanguageRegistry.addName(ChunkOsmium, "Osmium Ore Chunk");
       
       
       // BERYLLIUM ITEMs
       berylliumIngot = new itemBerylliumIngot(ItemIDs.BERYLLIUM_INGOT_DEFAULT);
       LanguageRegistry.addName(berylliumIngot, "Beryllium Ingot");
       
       ChunkBeryllium = new itemChunkBeryllium(ItemIDs.BERYLLIUM_CHUNK_DEFAULT);
       LanguageRegistry.addName(ChunkBeryllium, "Beryllium Ore Chunk");
       
       clusterBeryllium = new itemClusterBeryllium(ItemIDs.BERYLLIUM_CLUSTER_DEFAULT);
       LanguageRegistry.addName(clusterBeryllium, "Beryllium Ore Cluster");
       
       
       // ZEOLITE ITEMs
       zeoliteDust = new itemZeoliteDust(ItemIDs.ZEOLITE_DUST);
       LanguageRegistry.addName(zeoliteDust, "Zeolite Dust");
      zeoliteDustVial = new itemZeoliteVial(ItemIDs.ZEOLITE_DUST_VIAL);
       LanguageRegistry.addName(zeoliteDustVial, "Vial of Zeolite Dust");
       
       /* Register + Initialise Smelting Recipes */
       GameRegistry.addSmelting(ModItem.ChunkCopper.itemID, new ItemStack(copperIngot), 5.0f);
       GameRegistry.addSmelting(ModItem.ChunkOsmium.itemID, new ItemStack(osmiumIngot), 5.0f);
       GameRegistry.addSmelting(ModItem.ChunkBeryllium.itemID, new ItemStack(berylliumIngot), 5.0f);
       GameRegistry.addSmelting(ModItem.ClusterCopper.itemID, new ItemStack(copperIngot, 9), 10.0f);
       GameRegistry.addSmelting(ModItem.clusterBeryllium.itemID, new ItemStack(berylliumIngot, 9), 10.0f);
     
       /* Shaped Crafting Recipe*/
       GameRegistry.addRecipe(new ItemStack(ModItem.ClusterCopper, 1), "XXX", "XXX", "XXX", Character.valueOf('X'), ModItem.ChunkCopper);
       GameRegistry.addRecipe(new ItemStack(ModItem.clusterBeryllium, 1), "XXX", "XXX", "XXX", Character.valueOf('X'), ModItem.ChunkBeryllium);
       GameRegistry.addRecipe(new ItemStack(ModItem.zeoliteDustVial, 1), "XXX", "XYX", "XXX", Character.valueOf('X'), ModItem.zeoliteDust, Character.valueOf('Y'), Item.glassBottle);
    }
 }
