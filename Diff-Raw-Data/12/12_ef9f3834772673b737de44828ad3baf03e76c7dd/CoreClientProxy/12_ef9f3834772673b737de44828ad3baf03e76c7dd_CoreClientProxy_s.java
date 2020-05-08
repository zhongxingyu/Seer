 package shadow.mods.metallurgy;
 
 import java.io.File;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.ItemArmor;
 import net.minecraft.src.Item;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import net.minecraftforge.client.MinecraftForgeClient;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import shadow.mods.metallurgy.*;
 import shadow.mods.metallurgy.precious.FC_ChestRenderHelper;
 import shadow.mods.metallurgy.precious.FC_TileEntityChest;
 import shadow.mods.metallurgy.precious.FC_TileEntityChestRenderer;
 
 public class CoreClientProxy extends CoreCommonProxy{
 	
 	public void addNames()
 	{
 		LanguageRegistry.instance().addStringLocalization("container.crusher", "Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.StoneCrusher.name", "Stone Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.CopperCrusher.name", "Copper Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.BronzeCrusher.name", "Bronze Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.IronCrusher.name", "Iron Crusher");
 		LanguageRegistry.instance().addStringLocalization("tile.Crusher.SteelCrusher.name", "Steel Crusher");
 
 		LanguageRegistry.instance().addStringLocalization("tile.VanillaBrick.0.name", "Iron Bricks");
 		LanguageRegistry.instance().addStringLocalization("tile.VanillaBrick.1.name", "Gold Bricks");
 	}
 	
 	public static void addNamesToSet(MetalSet set)
 	{
 		
		String[] level = {" ", "1", "9", "3", "b", "2", "a", "5", "d", "e", "6", "4", "c"};
 		
 		for(int i = 0; i < set.numMetals; i++)
 		{
 			if(!set.info.isAlloy())
 				LanguageRegistry.instance().addStringLocalization("tile." + set.setName + "Ore." + i + ".name", level[set.info.level(i)] + set.info.name(i) + " Ore");
 			LanguageRegistry.instance().addStringLocalization("tile." + set.setName + "Brick." + i + ".name", level[set.info.level(i)] + set.info.name(i) + " Brick");
 			ModLoader.addName(set.Dust[i], level[set.info.level(i)] + set.info.name(i) + " Dust");
 			ModLoader.addName(set.Bar[i], level[set.info.level(i)] + set.info.name(i) + " Bar");
 
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
 		Item.swordWood.setIconIndex(0).setTextureFile("/shadow/Overrides.png");
 		Item.pickaxeWood.setIconIndex(16).setTextureFile("/shadow/Overrides.png");
 		Item.axeWood.setIconIndex(32).setTextureFile("/shadow/Overrides.png");
 		Item.hoeWood.setIconIndex(48).setTextureFile("/shadow/Overrides.png");
 		Item.shovelWood.setIconIndex(64).setTextureFile("/shadow/Overrides.png");
 		Item.swordStone.setIconIndex(1).setTextureFile("/shadow/Overrides.png");
 		Item.pickaxeStone.setIconIndex(17).setTextureFile("/shadow/Overrides.png");
 		Item.axeStone.setIconIndex(33).setTextureFile("/shadow/Overrides.png");
 		Item.hoeStone.setIconIndex(49).setTextureFile("/shadow/Overrides.png");
 		Item.shovelStone.setIconIndex(65).setTextureFile("/shadow/Overrides.png");
 		Item.swordSteel.setIconIndex(2).setTextureFile("/shadow/Overrides.png");
 		Item.pickaxeSteel.setIconIndex(18).setTextureFile("/shadow/Overrides.png");
 		Item.axeSteel.setIconIndex(34).setTextureFile("/shadow/Overrides.png");
 		Item.hoeSteel.setIconIndex(50).setTextureFile("/shadow/Overrides.png");
 		Item.shovelSteel.setIconIndex(66).setTextureFile("/shadow/Overrides.png");
 		Item.swordGold.setIconIndex(3).setTextureFile("/shadow/Overrides.png");
 		Item.pickaxeGold.setIconIndex(19).setTextureFile("/shadow/Overrides.png");
 		Item.axeGold.setIconIndex(35).setTextureFile("/shadow/Overrides.png");
 		Item.hoeGold.setIconIndex(51).setTextureFile("/shadow/Overrides.png");
 		Item.shovelGold.setIconIndex(67).setTextureFile("/shadow/Overrides.png");
 		Item.swordDiamond.setIconIndex(4).setTextureFile("/shadow/Overrides.png");
 		Item.pickaxeDiamond.setIconIndex(20).setTextureFile("/shadow/Overrides.png");
 		Item.axeDiamond.setIconIndex(36).setTextureFile("/shadow/Overrides.png");
 		Item.hoeDiamond.setIconIndex(52).setTextureFile("/shadow/Overrides.png");
 		Item.shovelDiamond.setIconIndex(68).setTextureFile("/shadow/Overrides.png");
 		Item.helmetSteel.setIconIndex(5).setTextureFile("/shadow/Overrides.png");
 		Item.plateSteel.setIconIndex(21).setTextureFile("/shadow/Overrides.png");
 		Item.legsSteel.setIconIndex(37).setTextureFile("/shadow/Overrides.png");
 		Item.bootsSteel.setIconIndex(53).setTextureFile("/shadow/Overrides.png");
 		Item.helmetGold.setIconIndex(6).setTextureFile("/shadow/Overrides.png");
 		Item.plateGold.setIconIndex(22).setTextureFile("/shadow/Overrides.png");
 		Item.legsGold.setIconIndex(38).setTextureFile("/shadow/Overrides.png");
 		Item.bootsGold.setIconIndex(54).setTextureFile("/shadow/Overrides.png");
 
 	}
 	
 	@Override
 	public void registerTileEntitySpecialRenderer() {
 		ClientRegistry.bindTileEntitySpecialRenderer(BC_TileEntityCrusher.class, new BC_TileEntityCrusherRenderer());
 	}
 	
 	@Override
 	public void registerRenderInformation()
 	{
 		RenderingRegistry.registerBlockHandler(new BC_CrusherRenderHelper());
 		MinecraftForgeClient.preloadTexture("/shadow/Overrides.png");
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
