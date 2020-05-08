 package shadow.mods.metallurgy;
 
 import java.io.File;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.Item;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import net.minecraftforge.client.MinecraftForgeClient;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import shadow.mods.metallurgy.*;
 
 public class CoreClientProxy extends CoreCommonProxy{
 
 	public static void addArmorToSet(MetalSet set)
 	{
 		for(int i = 0; i < set.info.numMetals(); i++)
 		{
 			if(set.info.isCatalyst(i))
 				continue;
 			((MetallurgyArmor) (set.Helmet[i])).setTexture(RenderingRegistry.addNewArmourRendererPrefix(set.info.name(i)));
 			((MetallurgyArmor) (set.Plate[i])).setTexture(RenderingRegistry.addNewArmourRendererPrefix(set.info.name(i)));
 			((MetallurgyArmor) (set.Legs[i])).setTexture(RenderingRegistry.addNewArmourRendererPrefix(set.info.name(i)));
 			((MetallurgyArmor) (set.Boots[i])).setTexture(RenderingRegistry.addNewArmourRendererPrefix(set.info.name(i)));
 		}
 	}
 	
 	public void addNames()
 	{
 		LanguageRegistry.instance().addStringLocalization("container.crusher", "Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.StoneCrusher.name", "Stone Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.CopperCrusher.name", "Copper Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.BronzeCrusher.name", "Bronze Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.IronCrusher.name", "Iron Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.SteelCrusher.name", "Steel Crusher");
 	}
 	
 	public static void addNamesToSet(MetalSet set)
 	{
 		
 		for(int i = 0; i < set.numMetals; i++)
 		{
 			if(!set.info.isAlloy())
 				LanguageRegistry.instance().addStringLocalization("tile." + set.setName + "Ore." + i + ".name", set.info.name(i) + " Ore");
 			LanguageRegistry.instance().addStringLocalization("tile." + set.setName + "Brick." + i + ".name", set.info.name(i) + " Brick");
 			ModLoader.addName(set.Dust[i], set.info.name(i) + " Dust");
 			ModLoader.addName(set.Bar[i], set.info.name(i) + " Bar");
 
 			if(!set.info.isCatalyst(i))
 			{
 				ModLoader.addName(set.Pickaxe[i], set.info.name(i) + " Pickaxe");
 				ModLoader.addName(set.Shovel[i], set.info.name(i) + " Shovel");
 				ModLoader.addName(set.Axe[i], set.info.name(i) + " Axe");
 				ModLoader.addName(set.Hoe[i], set.info.name(i) + " Hoe");
 				ModLoader.addName(set.Sword[i], set.info.name(i) + " Sword");
 				ModLoader.addName(set.Helmet[i], set.info.name(i) + " Helmet");
 				ModLoader.addName(set.Plate[i], set.info.name(i) + " Plate");
 				ModLoader.addName(set.Legs[i], set.info.name(i) + " Legs");
 				ModLoader.addName(set.Boots[i], set.info.name(i) + " Boots");
 			}
 		}
 	}
 	
 	@Override
 	public void addTextureOverrides()
 	{
 		Item.swordWood.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmWoodSword.png"));
 		Item.pickaxeWood.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmWoodPickaxe.png"));
 		Item.axeWood.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmWoodAxe.png"));
 		Item.hoeWood.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmWoodHoe.png"));
 		Item.shovelWood.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmWoodShovel.png"));
 		Item.swordStone.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmStoneSword.png"));
 		Item.pickaxeStone.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmStonePickaxe.png"));
 		Item.axeStone.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmStoneAxe.png"));
 		Item.hoeStone.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmStoneHoe.png"));
 		Item.shovelStone.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmStoneShovel.png"));
 		Item.swordDiamond.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmDiamondSword.png"));
 		Item.pickaxeDiamond.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmDiamondPickaxe.png"));
 		Item.axeDiamond.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmDiamondAxe.png"));
 		Item.hoeDiamond.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmDiamondHoe.png"));
 		Item.shovelDiamond.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmDiamondShovel.png"));
 		Item.pickaxeGold.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmGoldPickaxe.png"));
 		Item.shovelGold.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmGoldShovel.png"));
 		Item.axeGold.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmGoldAxe.png"));
 		Item.hoeGold.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmGoldHoe.png"));
 		Item.swordGold.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmGoldSword.png"));
 		Item.pickaxeSteel.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmIronPickaxe.png"));
 		Item.shovelSteel.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmIronShovel.png"));
 		Item.axeSteel.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmIronAxe.png"));
 		Item.hoeSteel.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmIronHoe.png"));
 		Item.swordSteel.setIconIndex(ModLoader.addOverride("/gui/items.png", "/shadow/ItmIronSword.png"));
 	}
 	
 	@Override
 	public void registerRenderInformation()
 	{
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyTerrain.png");
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyFurnaces.png");
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyBaseMetals.png");
 		MinecraftForgeClient.preloadTexture("/shadow/MetallurgyBaseAlloys.png");
 	}
 	
 	@Override
 	public World getClientWorld() {
 		return FMLClientHandler.instance().getClient().theWorld;
 	}
 
 	@Override
 	public File getMinecraftDir()
 	{
 		return Minecraft.getMinecraftDir();
 	}
 }
