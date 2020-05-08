 package com.madpcgaming.mt.core.proxy;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 import com.madpcgaming.mt.MadTech;
 import com.madpcgaming.mt.client.gui.ElectrolyserGUI;
 import com.madpcgaming.mt.helpers.LogHelper;
 import com.madpcgaming.mt.inventory.ElectrolyserContainer;
import com.madpcgaming.mt.lib.GuiIds;
import com.madpcgaming.mt.tileentitys.ContainerIndustrialFurnace;
 import com.madpcgaming.mt.tileentitys.TileElectrolyser;
import com.madpcgaming.mt.tileentitys.TileEntityIndustrialFurnaceCore;
 
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 
 public class CommonProxy implements IGuiHandler
 {
 	
 	public void registerRenderings()
 	{
 		NetworkRegistry.instance().registerGuiHandler(MadTech.instance, this);
 	}
 	
 	@Override
 	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		LogHelper.info("Getting Server GUI!");
 		TileEntity t = world.getBlockTileEntity(x, y, z);
 		if (ID == 0 && t instanceof TileElectrolyser)
 		{
 			return new ElectrolyserContainer(player.inventory, (TileElectrolyser) t);
 		}
		else if (ID == GuiIds.FURNACE_CORE)
		{
			TileEntityIndustrialFurnaceCore tileIndustrialFurnaceCore = (TileEntityIndustrialFurnaceCore) world.getBlockTileEntity(x, y, z);
			return new ContainerIndustrialFurnace(player.inventory, tileIndustrialFurnaceCore);
		}
 		return null;
 	}
 	
 	@Override
 	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
 	{
 		LogHelper.info("Getting CLient GUI!");
 		TileEntity t = world.getBlockTileEntity(x, y, z);
 		if (ID == 0 && t instanceof TileElectrolyser)
 		{
 			return new ElectrolyserGUI(new ElectrolyserContainer(player.inventory, (TileElectrolyser) t), (TileElectrolyser) t);
 		}
		else if (ID == GuiIds.FURNACE_CORE)
		{
			TileEntityIndustrialFurnaceCore tileIndustrialFurnaceCore = (TileEntityIndustrialFurnaceCore) world.getBlockTileEntity(x, y, z);
			return new ContainerIndustrialFurnace(player.inventory, tileIndustrialFurnaceCore);
		}
 		return null;
 	}
 }
