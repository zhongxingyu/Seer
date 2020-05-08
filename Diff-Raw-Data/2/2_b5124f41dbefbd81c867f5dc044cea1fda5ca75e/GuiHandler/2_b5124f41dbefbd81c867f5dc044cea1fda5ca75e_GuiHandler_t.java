 package shadowhax.crystalluscraft.core.handler;
 
 import cpw.mods.fml.common.network.IGuiHandler;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import shadowhax.crystalluscraft.block.Blocks;
 import shadowhax.crystalluscraft.block.tile.TileEntityCrystalChest;
 import shadowhax.crystalluscraft.client.gui.CrystalBookGui;
 import shadowhax.crystalluscraft.client.gui.GuiCrystalChest;
 import shadowhax.crystalluscraft.client.gui.RefinedTableGui;
 import shadowhax.crystalluscraft.core.util.Config;
 import shadowhax.crystalluscraft.inventory.ContainerCrystalChest;
 import shadowhax.crystalluscraft.inventory.ContainerRefiningTable;
 import shadowhax.crystalluscraft.item.Items;
 
 public class GuiHandler implements IGuiHandler {
 
 	@Override
 	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
 
 		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
 		switch(id){
 		
 		case 1: return world.getBlockId(x, y, z) == Blocks.refiningTable.blockID ? new ContainerRefiningTable(player.inventory, world, x, y, z): null;
		case 3: return world.getBlockId(x, y, z) == Blocks.crystalChest.blockID ? new ContainerCrystalChest(player.inventory, (TileEntityCrystalChest) tile_entity): null;
 		default: return null;
 		}
 	}
 
 	@Override
 	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
 
 		TileEntity tile_entity = world.getBlockTileEntity(x, y, z);
 		switch(id){
 		
 		case 1: return world.getBlockId(x, y, z) == Blocks.refiningTable.blockID ? new RefinedTableGui(player.inventory, world, x, y, z) : null;
 		case 3: return world.getBlockId(x, y, z) == Blocks.crystalChest.blockID | player.getHeldItem().itemID == Items.chestLink.itemID ? new GuiCrystalChest(player.inventory, (TileEntityCrystalChest) tile_entity): null; 
 		default: return null;
 		}
 	}
 }
