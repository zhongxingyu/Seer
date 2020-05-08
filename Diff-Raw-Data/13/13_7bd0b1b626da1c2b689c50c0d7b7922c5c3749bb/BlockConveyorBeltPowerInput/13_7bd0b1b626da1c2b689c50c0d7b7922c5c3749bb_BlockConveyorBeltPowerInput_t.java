 package ip.industrialProcessing.transport.items.ConveyorBeltPowerInput;
 
 import ip.industrialProcessing.config.ConfigMachineBlocks;
 import ip.industrialProcessing.config.ISetupCreativeTabs;
 import ip.industrialProcessing.machines.BlockMachine;
 import ip.industrialProcessing.transport.items.conveyorBelt.TileEntityConveyorConnectionsBase;
 import ip.industrialProcessing.utils.IDescriptionBlock;
 import ip.industrialProcessing.utils.registry.HandlerRegistry;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public class BlockConveyorBeltPowerInput extends BlockMachine implements IDescriptionBlock {
 
 	public BlockConveyorBeltPowerInput() {
 		super(ConfigMachineBlocks.getBlockConveyorBeltPowerInputID(), Material.iron, 1F, Block.soundMetalFootstep, "BlockConveyorBeltPowerInput", ISetupCreativeTabs.tabPower);
 	}
 
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new TileEntityConveyorBeltPowerInput();
 	}
 
 	@Override
 	public String getDescription() {
 		return "Power connector for the conveyor belt.";
 	}
 
 	@Override
 	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float clickX, float clickY, float clickZ) {
		
		TileEntityConveyorBeltPowerInput te = (TileEntityConveyorBeltPowerInput) world.getBlockTileEntity(x, y, z);
		System.out.println(te.conveyorLine);
 		return super.onBlockActivated(world, x, y, z, player, metadata, clickX, clickY, clickZ);
 	}
 
 	@Override
 	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
 		TileEntityConveyorBeltPowerInput me = (TileEntityConveyorBeltPowerInput) world.getBlockTileEntity(x, y, z);
 		me.unregister();
 		super.breakBlock(world, x, y, z, par5, par6);
 	}
 
 	@Override
 	public void onNeighborBlockChange(World world, int x, int y, int z, int par5) {
 		world.scheduleBlockUpdate(x, y, z, this.blockID, 5);
 		super.onNeighborBlockChange(world, x, y, z, par5);
 	}
 	
 	@Override
 	public void updateTick(World world, int x, int y, int z, Random par5Random) {
 		TileEntityConveyorBeltPowerInput me = (TileEntityConveyorBeltPowerInput) world.getBlockTileEntity(x, y, z);
 		TileEntity te = world.getBlockTileEntity(x, y + 1, z);
 		if (me.hasLine()) {
 			int id = me.conveyorLine;
 			if (te instanceof TileEntityConveyorConnectionsBase) {
 				if(id == ((TileEntityConveyorConnectionsBase) te).getConveyorLineID()){
 					//all is well in the world
 				}else{
 					me.unregister();
 					me.register();
 				}
 			}
 			else{
 				me.unregister();
 			}
 		} else {
 			me.register();
 		}
 		super.updateTick(world, x, y, z, par5Random);
 	}
 
 }
