 package lcdispenser;
 
 import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
 import net.minecraft.dispenser.IBehaviorDispenseItem;
 import net.minecraft.dispenser.IBlockSource;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityDispenser;
 import net.minecraft.util.EnumFacing;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.liquids.ITankContainer;
 import net.minecraftforge.liquids.LiquidContainerData;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import net.minecraftforge.liquids.LiquidStack;
 
 public class BehaviorContainerDispense implements IBehaviorDispenseItem
 {
 	protected final IBehaviorDispenseItem chain;
 	protected final static IBehaviorDispenseItem defaultDispenseBehavior = new BehaviorDefaultDispenseItem();
 	
 	public BehaviorContainerDispense(IBehaviorDispenseItem chain)
 	{
 		if(chain == null){
 			this.chain = defaultDispenseBehavior;
 		}else{
 			this.chain = chain;
 		}
 	}
 	
 	@Override
 	public ItemStack dispense(IBlockSource blockSource, ItemStack itemstack)
 	{
 		ItemStack result = dispenseStack(blockSource, itemstack);
 		if(result == null){
 			System.out.println("Not processed. Chain to Vanilla.");
 			return chain.dispense(blockSource, itemstack);
 		}
 		if(result.stackSize <= 0){
 			// The result was empty.
 		}else if(addResultToInventory((TileEntityDispenser)blockSource.func_82619_j(), result)){
 			// The result was placed in inventory.
 		}else if(itemstack.stackSize <= 0){
 			itemstack.itemID = result.itemID;
 			itemstack.setItemDamage(result.getItemDamage());
 			itemstack.stackSize = result.stackSize;
 		}else{
 			defaultDispenseBehavior.dispense(blockSource, result);
 		}
 		playDispenseSound(blockSource);
 		spawnDispenseParticles(blockSource);
 		return itemstack;
 	}
 
 	public ItemStack dispenseStack(IBlockSource blockSource, ItemStack itemstack)
 	{
 		EnumFacing facing = EnumFacing.func_82600_a(blockSource.func_82620_h());
 		World world = blockSource.getWorld();
 		int targetX = blockSource.getXInt() + facing.func_82601_c();
 		int targetY = blockSource.getYInt();
 		int targetZ = blockSource.getZInt() + facing.func_82599_e();
 		
 		TileEntity te = world.getBlockTileEntity(targetX, targetY, targetZ);
 		if(te != null && te instanceof ITankContainer){
 			return fillOrEmptyContainer((ITankContainer)te, itemstack, facing);
 		}
 		if(world.isAirBlock(targetX, targetY, targetZ))
 		{
 			TileEntity tileabove = world.getBlockTileEntity(targetX, targetY+1, targetZ);
 			TileEntity tilebelow = world.getBlockTileEntity(targetX, targetY-1, targetZ);
 			if(tileabove != null && LiquidContainerRegistry.isEmptyContainer(itemstack))
 			{
 				return fillContainer((ITankContainer)tileabove, itemstack, EnumFacing.UP);
 			}
 			if(tilebelow != null && LiquidContainerRegistry.isFilledContainer(itemstack))
 			{
				return fillContainer((ITankContainer)tilebelow, itemstack, EnumFacing.DOWN);
 			}
 		}
 		System.out.println("Not matched");
 		
 		return null;
 	}
 	
 	private boolean addResultToInventory(TileEntityDispenser te, ItemStack result)
 	{
 		int size = te.getSizeInventory();
 		int i;
 		ItemStack itemstack;
 		for(i=0; i<size; ++i){
 			itemstack = te.getStackInSlot(i);
 			if(itemstack != null && itemstack.isItemEqual(result) && itemstack.stackSize + result.stackSize <= itemstack.getMaxStackSize()){
 				itemstack.stackSize += result.stackSize;
 				return true;
 			}
 		}
 		for(i=0; i<size; ++i){
 			itemstack = te.getStackInSlot(i);
 			if(itemstack == null || itemstack.itemID == 0){
 				te.setInventorySlotContents(i, result);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected void playDispenseSound(IBlockSource blockSource)
 	{
 		blockSource.getWorld().playAuxSFX(1000, blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt(), 0);
 	}
 	
 	protected void spawnDispenseParticles(IBlockSource blockSource)
 	{
 		EnumFacing facing = EnumFacing.func_82600_a(blockSource.func_82620_h());
 		blockSource.getWorld().playAuxSFX(2000, blockSource.getXInt(), blockSource.getYInt(), blockSource.getZInt(), facing.func_82601_c() + 1 + (facing.func_82599_e() + 1) * 3);
 	}
 	
 	protected ItemStack fillContainer(ITankContainer tank, ItemStack itemstack, EnumFacing facing)
 	{
 		for(LiquidContainerData data : LiquidContainerRegistry.getRegisteredLiquidContainerData()){
 			if(data.container.isItemEqual(itemstack)){
 				int amount = data.stillLiquid.amount;
 				LiquidStack liquid = tank.drain(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), amount, false);
 				if(liquid != null && liquid.isLiquidEqual(data.stillLiquid) && liquid.amount == amount){
 					tank.drain(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), amount, true);
 					itemstack.stackSize--;
 					ItemStack result = data.filled.copy();
 					result.stackSize = 1;
 					return result;
 				}
 			}
 		}
 		return null;
 	}
 	
 	protected ItemStack emptyContainer(ITankContainer tank, ItemStack itemstack, EnumFacing facing)
 	{
 		for(LiquidContainerData data : LiquidContainerRegistry.getRegisteredLiquidContainerData()){
 			if(data.filled.isItemEqual(itemstack)){
 				LiquidStack liquid = data.stillLiquid;
 				int amount = tank.fill(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), liquid, false);
 				if(liquid.amount == amount){
 					tank.fill(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), liquid, true);
 					ItemStack result = itemstack.getItem().getContainerItemStack(itemstack);
 					itemstack.stackSize--;
 					if(result == null){
 						result = data.container.copy();
 						result.stackSize = 0;
 					}
 					return result;
 				}else{
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 	
 	protected ItemStack fillOrEmptyContainer(ITankContainer tank, ItemStack itemstack, EnumFacing facing)
 	{
 		for(LiquidContainerData data : LiquidContainerRegistry.getRegisteredLiquidContainerData()){
 			if(data.container.isItemEqual(itemstack)){
 				int amount = data.stillLiquid.amount;
 				LiquidStack liquid = tank.drain(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), amount, false);
 				if(liquid != null && liquid.isLiquidEqual(data.stillLiquid) && liquid.amount == amount){
 					tank.drain(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), amount, true);
 					itemstack.stackSize--;
 					ItemStack result = data.filled.copy();
 					result.stackSize = 1;
 					return result;
 				}
 			}
 			else if(data.filled.isItemEqual(itemstack)){
 				LiquidStack liquid = data.stillLiquid;
 				int amount = tank.fill(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), liquid, false);
 				if(liquid.amount == amount){
 					tank.fill(ForgeDirection.getOrientation(facing.ordinal()).getOpposite(), liquid, true);
 					ItemStack result = itemstack.getItem().getContainerItemStack(itemstack);
 					itemstack.stackSize--;
 					if(result == null){
 						result = data.container.copy();
 						result.stackSize = 0;
 					}
 					return result;
 				}else{
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 }
