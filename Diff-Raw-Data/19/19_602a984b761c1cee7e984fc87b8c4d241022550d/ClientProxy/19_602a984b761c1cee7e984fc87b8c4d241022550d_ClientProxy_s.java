 package MysticWorld.Client;
 
 import net.minecraft.src.ModLoader;
 import net.minecraft.world.World;
 import net.minecraftforge.client.MinecraftForgeClient;
 import MysticWorld.CommonProxy;
 import MysticWorld.Blocks.BlockHandler;
 import MysticWorld.Client.FX.LightCubeFX;
 import MysticWorld.Client.FX.PowerAirFX;
 import MysticWorld.Client.FX.PowerEarthFX;
 import MysticWorld.Client.FX.PowerEnergyFX;
 import MysticWorld.Client.FX.PowerFireFX;
 import MysticWorld.Client.FX.PowerWaterFX;
 import MysticWorld.Entity.EntityChargeEarth;
 import MysticWorld.Entity.EntityChargeEnergy;
 import MysticWorld.Entity.EntityChargeFire;
 import MysticWorld.Entity.EntityChargeWater;
 import MysticWorld.Items.ItemHandler$1;
 import MysticWorld.Lib.RenderIds;
 import MysticWorld.Renderer.BlockPillarInsertRenderer;
 import MysticWorld.Renderer.ItemOrbAirRenderer;
 import MysticWorld.Renderer.ItemOrbEarthRenderer;
 import MysticWorld.Renderer.ItemOrbEnergyRenderer;
 import MysticWorld.Renderer.ItemOrbFireRenderer;
 import MysticWorld.Renderer.ItemOrbWaterRenderer;
 import MysticWorld.Renderer.ItemStaffAirRenderer;
 import MysticWorld.Renderer.ItemStaffEarthRenderer;
 import MysticWorld.Renderer.ItemStaffEnergyRenderer;
 import MysticWorld.Renderer.ItemStaffFireRenderer;
 import MysticWorld.Renderer.ItemStaffWaterRenderer;
 import MysticWorld.Renderer.RenderStaffPower;
 import MysticWorld.Renderer.TileEntityPillarInsertRenderer;
 import MysticWorld.TileEntity.TileEntityPillarInsert;
 import cpw.mods.fml.client.registry.ClientRegistry;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 
 public class ClientProxy extends CommonProxy {
 	
 	@Override
 	public void registerRenderIDs()
 	{
 		RenderIds.RENDER_PILLAR_INSERT = RenderingRegistry.getNextAvailableRenderId();
 	}
 	
 	@Override
 	public void registerRenders()
 	{
 		MinecraftForgeClient.registerItemRenderer(BlockHandler.pillarInsert.blockID, new BlockPillarInsertRenderer());
 		
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.fireStaff.itemID, new ItemStaffFireRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.earthStaff.itemID, new ItemStaffEarthRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.airStaff.itemID, new ItemStaffAirRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.energyStaff.itemID, new ItemStaffEnergyRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.waterStaff.itemID, new ItemStaffWaterRenderer());
 
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.fireOrb.itemID, new ItemOrbFireRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.earthOrb.itemID, new ItemOrbEarthRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.airOrb.itemID, new ItemOrbAirRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.energyOrb.itemID, new ItemOrbEnergyRenderer());
 		MinecraftForgeClient.registerItemRenderer(ItemHandler$1.waterOrb.itemID, new ItemOrbWaterRenderer());
 		
 		RenderingRegistry.registerEntityRenderingHandler(EntityChargeFire.class, new RenderStaffPower(0));
 		RenderingRegistry.registerEntityRenderingHandler(EntityChargeWater.class, new RenderStaffPower(1));
 		RenderingRegistry.registerEntityRenderingHandler(EntityChargeEarth.class, new RenderStaffPower(2));
		RenderingRegistry.registerEntityRenderingHandler(EntityChargeEarth.class, new RenderStaffPower(3));
 		RenderingRegistry.registerEntityRenderingHandler(EntityChargeEnergy.class, new RenderStaffPower(4));
 		
 		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPillarInsert.class, new TileEntityPillarInsertRenderer());
 	}
 	
 	@Override
 	public int addArmor(String armor)
 	{
 		return RenderingRegistry.addNewArmourRendererPrefix(armor);
 	}
 	
 	//particles
 	@Override
 	public void airFeetFX(World worldObj, double x, double y, double z, float scale)
 	{
 		PowerAirFX airFX = new PowerAirFX(worldObj, x, y, z, scale);
 		ModLoader.getMinecraftInstance().effectRenderer.addEffect(airFX);
 	}
 	
 	@Override
 	public void earthFX(World worldObj, double x, double y, double z, float scale)
 	{
 		PowerEarthFX earthFX = new PowerEarthFX(worldObj, x, y, z, scale);
 		ModLoader.getMinecraftInstance().effectRenderer.addEffect(earthFX);
 	}
 	
 	@Override
 	public void energyFX(World worldObj, double x, double y, double z, float scale)
 	{
 		PowerEnergyFX energyFX = new PowerEnergyFX(worldObj, x, y, z, scale);
 		ModLoader.getMinecraftInstance().effectRenderer.addEffect(energyFX);
 	}
 	
 	@Override
 	public void fireFX(World worldObj, double x, double y, double z, float scale)
 	{
 		PowerFireFX fireFX = new PowerFireFX(worldObj, x, y, z, scale);
 		ModLoader.getMinecraftInstance().effectRenderer.addEffect(fireFX);
 	}
 	
 	@Override
 	public void waterFX(World worldObj, double x, double y, double z, float scale)
 	{
 		PowerWaterFX waterFX = new PowerWaterFX(worldObj, x, y, z, scale);
 		ModLoader.getMinecraftInstance().effectRenderer.addEffect(waterFX);
 	}
 	
 	@Override
 	public void lightCubeFX(World worldObj, double x, double y, double z, double motionX, double motionY, double motionZ, float scale)
 	{
 		LightCubeFX lightCubeFX = new LightCubeFX(worldObj, x, y, z, motionX, motionY, motionZ, scale);
 		ModLoader.getMinecraftInstance().effectRenderer.addEffect(lightCubeFX);
 	}
 	
 }
