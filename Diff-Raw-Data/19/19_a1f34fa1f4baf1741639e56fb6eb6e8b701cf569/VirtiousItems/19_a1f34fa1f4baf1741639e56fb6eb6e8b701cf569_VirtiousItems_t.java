 package teamm.mods.virtious.lib;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import teamm.mods.virtious.Config;
 import teamm.mods.virtious.Virtious;
 import teamm.mods.virtious.item.TeleporterItem;
 import teamm.mods.virtious.item.VirtiousItem;
 import net.minecraft.item.Item;
 import net.minecraftforge.common.Property;
 
 public class VirtiousItems 
 {
 	public static Item ingotBrazeum;
	public static Item shardAquieus;
 	public static Item teleportWand;
 	public static Item gemPluthorium;
 	
 	/**
 	 * Loads all item objects
 	 */
 	public VirtiousItems()
 	{
 		Property idIngotBrazeum = Virtious.config.getItem("Brazeum Ingot ID", Config.idIngotBrazeum);
 		int ingotBrazeumId = idIngotBrazeum.getInt();
 		ingotBrazeum = new VirtiousItem(ingotBrazeumId).setUnlocalizedName("BrazeumIngot");
 		LanguageRegistry.addName(ingotBrazeum, "Brazeum Ingot");
 
		Property idShardAquieus = Virtious.config.getItem("Virtious Shard ID", Config.idShardAquieus);
		int shardAquieusId = idShardAquieus.getInt();
		shardAquieus = new VirtiousItem(shardAquieusId).setUnlocalizedName("AquieusShard");
		LanguageRegistry.addName(shardAquieus, "Aquieus Shard");
 		
 		Property idTeleporterWand = Virtious.config.getItem("idTeleporterWand", Config.idTeleporterWand);
 		int TeleporterWandID = idTeleporterWand.getInt();
 		teleportWand = new TeleporterItem(TeleporterWandID).setUnlocalizedName("TeleportWand");
 		LanguageRegistry.addName(teleportWand, "TeleportWand");
 		
 		Property idGemPluthorium = Virtious.config.getItem("Pluthorium Gem", Config.idGemPluthorium);
 		int gemPluthoriumId = idGemPluthorium.getInt();
 		gemPluthorium = new VirtiousItem(gemPluthoriumId).setUnlocalizedName("PluthoriumGem");
 		LanguageRegistry.addName(gemPluthorium, "Pluthorium Gem");
 		
 		//TODO load all item objects
 	}
 }
