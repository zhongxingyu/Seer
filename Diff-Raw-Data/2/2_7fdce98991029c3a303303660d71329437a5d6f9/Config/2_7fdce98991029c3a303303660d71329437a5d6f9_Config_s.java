 package hunternif.mc.rings.config;
 
 import hunternif.mc.rings.item.FireRing;
 import hunternif.mc.rings.item.FlyingRing;
 import hunternif.mc.rings.item.HarvestRing;
 import hunternif.mc.rings.item.IceRing;
 import hunternif.mc.rings.item.ModItem;
 import hunternif.mc.rings.item.TeleportRing;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 
 public class Config {
 	public static CfgInfo<ModItem> commonRing = new CfgInfo<ModItem>(26950, "Common Ring");
 	public static CfgInfo<TeleportRing> tpRing = new CfgInfo<TeleportRing>(26951, "Teleport Ring").setCoreItem(Item.enderPearl);
 	public static CfgInfo<FireRing> fireRing = new CfgInfo<FireRing>(26952, "Fire Ring").setCoreItem(Item.flintAndSteel);
	public static CfgInfo<IceRing> iceRing = new CfgInfo<IceRing>(26953, "Ice Ring").setCoreItem(Block.snow);
 	public static CfgInfo<HarvestRing> harvestRing = new CfgInfo<HarvestRing>(26954, "Harvest Ring")
 			.setCoreItem(Item.seeds).setCoreItem(Item.melonSeeds).setCoreItem(Item.pumpkinSeeds).setCoreItem(Block.sapling);
 	public static CfgInfo<FlyingRing> flyRing = new CfgInfo<FlyingRing>(26955, "Flying Ring").setCoreItem(Item.feather);
 }
