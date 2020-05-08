 
 package hawksmachinery.common.tileentity;
 
 import java.util.Random;
 import hawksmachinery.common.HMInventoryCrafting;
 import hawksmachinery.common.api.HMRecipes;
 import hawksmachinery.common.block.HMBlock;
 import hawksmachinery.common.block.HMBlockStarForge;
 import net.minecraft.block.Block;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.prefab.multiblock.IMultiBlock;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class HMTileEntityStarForge extends HMTileEntityMachine implements IMultiBlock
 {
 	/**
 	 * The item this Star Forge is going to spit out.
 	 */
 	public ItemStack output;
 	public HMInventoryCrafting matrix = new HMInventoryCrafting("Star Forge", 3, 3, this);
 	
 	/**
 	 * Used internally to make sure the block drops properly.
 	 */
 	private boolean canDrop = true;
 	
 	public HMTileEntityStarForge()
 	{
 		super();
 		ELECTRICITY_REQUIRED = 20;
 		TICKS_REQUIRED = 100;
 		ELECTRICITY_LIMIT = 2000;
 		containingItems = new ItemStack[10];
 		VOLTAGE = 120;
 		isProcessor = true;
 		
 	}
 	
 	@Override
 	public void updateEntity()
 	{
 		super.updateEntity();
 		
 		if (this.canWork())
 		{
 			this.electricityStored -= this.ELECTRICITY_REQUIRED;
 			
 			if (this.workTicks == 0)
 			{
 				this.workTicks = this.TICKS_REQUIRED;
 				
 			}
 			else
 			{
 				--this.workTicks;
 				
 				if (this.workTicks == 1)
 				{
 					this.forgeItem();
 					this.workTicks = 0;
 					
 				}
 				
 			}
 			
 		}
 		else
 		{
 			this.workTicks = 0;
 			
 		}
 		
 	}
 	
 	public boolean canWork()
 	{
 		this.output = HMRecipes.getForgeResult(this.matrix, this.worldObj);
 		return this.output != null && !this.isDisabled() && (this.electricityStored >= (this.ELECTRICITY_REQUIRED * 2)) && (this.containingItems[9] == null || (this.output.isItemEqual(this.containingItems[9]) && this.output.stackSize + this.containingItems[9].stackSize <= this.output.getMaxStackSize()));
 	}
 	
 	public void forgeItem()
 	{
 		if (this.containingItems[9] == null)
 		{
 			this.containingItems[9] = this.output.copy();
 			
 		}
 		else
 		{
 			if (this.containingItems[9].isItemEqual(this.output))
 			{
 				this.containingItems[9].stackSize += this.output.copy().stackSize;
 				
 			}
 			
 		}
 		
 		for (int counter = 0; counter < 9; ++counter)
 		{
 			if (this.containingItems[counter] != null)
 			{
 				this.decrStackSize(counter, 1);
 				
 			}
 			
 		}
 		
 		this.randomlyDamageSelf();
 		
 	}
 	
 	@Override
 	public boolean onActivated(EntityPlayer player)
 	{
 		if (player.isSneaking()) return false;
		return ((HMBlockStarForge)Block.blocksList[this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord)]).onMachineActivated(this.worldObj, this.xCoord, this.yCoord, this.zCoord, player, 0, player.serverPosX, player.serverPosY, player.serverPosZ);
 	}
 	
 	@Override
 	public void onCreate(Vector3 placedPosition)
 	{
 		for (int x = -1; x < 2; ++x)
 		{
 			for (int z = -1; z < 2; ++z)
 			{
 				if (x != 0 || z != 0)
 				{
 					HMBlock.starForgeTechnical.makeFakeBlock(this.worldObj, new Vector3(this.xCoord + x, this.yCoord, this.zCoord + z), placedPosition);
 					
 				}
 				
 			}
 			
 		}
 		
 	}
 	
 	public void onDestroy(TileEntity callingBlock, boolean dropItem)
 	{
 		for (int x = -1; x < 2; ++x)
 		{
 			for (int z = -1; z < 2; ++z)
 			{
 				if (x != 0 || z != 0)
 				{
 					if (dropItem && this.canDrop)
 					{
 						Block.blocksList[this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord)].dropBlockAsItem(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord), this.getBlockMetadata());
 						this.canDrop = false;
 						
 					}
 					else if (!dropItem)
 					{
 						this.canDrop = false;
 						
 					}
 					
 					this.worldObj.setBlockWithNotify(this.xCoord + x, this.yCoord, this.zCoord + z, 0);
 					
 				}
 				
 			}
 			
 		}
 		
 		for (ItemStack item : this.containingItems)
 		{
 			if (item != null)
 			{
 				Random random = new Random();
 				float var8 = random.nextFloat() * 0.8F + 0.1F;
 				float var9 = random.nextFloat() * 0.8F + 0.1F;
 				float var10 = random.nextFloat() * 0.8F + 0.1F;
 				
 				while (item.stackSize > 0)
 				{
 					int var11 = random.nextInt(21) + 10;
 					
 					if (var11 > item.stackSize)
 					{
 						var11 = item.stackSize;
 					}
 
 					item.stackSize -= var11;
 					
 					EntityItem var12 = new EntityItem(this.worldObj, (this.xCoord + var8), (this.yCoord + var9), (this.zCoord + var10), new ItemStack(item.itemID, var11, item.getItemDamage()));
 
 					if (item.hasTagCompound())
 					{
 						var12.func_92014_d().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
 					}
 					
 					float var13 = 0.05F;
 					
 					var12.motionX = ((float)random.nextGaussian() * var13);
 					var12.motionY = ((float)random.nextGaussian() * var13 + 0.2F);
 					var12.motionZ = ((float)random.nextGaussian() * var13);
 					
 					this.worldObj.spawnEntityInWorld(var12);
 					
 				}
 				
 			}
 			
 		}
 		
 		this.selfVec.selfTerminate();
 		this.invalidate();
 		
 	}
 	
 	@Override
 	public void onDestroy(TileEntity callingBlock)
 	{
 		this.onDestroy(callingBlock, true);
 		
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound NBTTag)
 	{
 		super.readFromNBT(NBTTag);
 		if (NBTTag.hasKey("output")) this.output = ItemStack.loadItemStackFromNBT(NBTTag.getCompoundTag("output"));
 		
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound NBTTag)
 	{
 		super.writeToNBT(NBTTag);
 		if (this.output != null) NBTTag.setCompoundTag("output", this.output.writeToNBT(new NBTTagCompound()));
 		
 	}
 	
 }
