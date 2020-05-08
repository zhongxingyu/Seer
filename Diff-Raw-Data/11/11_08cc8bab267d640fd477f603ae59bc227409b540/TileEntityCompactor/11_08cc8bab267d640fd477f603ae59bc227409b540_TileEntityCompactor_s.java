 package compactMobs.TileEntity;
 
 
 
 import java.io.DataInput;
 import java.util.Iterator;
 import java.util.List;
 
 import compactMobs.CompactMobsCore;
 import compactMobs.Utils;
 import compactMobs.Vect;
 import compactMobs.Items.CompactMobsItems;
 import compactMobs.Items.FullMobHolder;
 
 import buildcraft.api.core.Orientations;
 import buildcraft.api.power.IPowerProvider;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerFramework;
 import buildcraft.api.power.PowerProvider;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 
 import net.minecraft.src.AxisAlignedBB;
 import net.minecraft.src.Block;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityAgeable;
 import net.minecraft.src.EntityCreature;
 import net.minecraft.src.EntityDragonBase;
 import net.minecraft.src.EntityList;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntitySheep;
 import net.minecraft.src.EntitySlime;
 import net.minecraft.src.EntityVillager;
 import net.minecraft.src.IInventory;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.NBTBase;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NBTTagList;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 
 public class TileEntityCompactor extends TileEntity implements IInventory, IPowerReceptor, ISidedInventory
 {
 	
 	
 	
 	public ItemStack[] ItemStacks;
 	
 	IPowerProvider provider;
 	boolean drawParticle;
 	
 
 	
 	public TileEntityCompactor() {
 		ItemStacks = new ItemStack[28];
 		if (PowerFramework.currentFramework != null)
 			provider = PowerFramework.currentFramework.createPowerProvider();
 		provider.configure(50, 25, 25, 25, 1000);
 	}
 	
 	@Override
 	public void updateEntity() {
 		
 		if (this instanceof IPowerReceptor) {
 			IPowerReceptor receptor = ((IPowerReceptor) this);
 
 			receptor.getPowerProvider().update(receptor);
 		}
 		//worldObj.spawnParticle("smoke", this.xCoord+1.5D, this.yCoord+.5D, this.zCoord+.5, -.1, 0, 0);
 		//this.worldObj.spawnParticle("smoke", this.xCoord+1.5D, this.yCoord+.5D, this.zCoord+.5, -.1, 0, 0);
 		
 			/*if (ItemStacks[0] != null)
 		{
 			if (ItemStacks[0].getItem() == Item.redstone)
 			{
 				for (int var3 = 1; var3 < 29; ++var3)
 		        {
 					if (ItemStacks[var3] != null)
 					{
 						if (ItemStacks[var3].getItem() == Item.paper){
 							if(ItemStacks[var3].stackSize < 64){
 								ItemStacks[var3] = new ItemStack(Item.paper, ItemStacks[var3].stackSize+1);
 								break;
 							}
 						}
 					} else
 					{
 						ItemStacks[var3] = new ItemStack(Item.paper, 1);
 						break;
 					}
 		        }
 				
 				if (ItemStacks[0].stackSize > 1)
 				{
 					ItemStacks[0] = new ItemStack(ItemStacks[0].getItem(), ItemStacks[0].stackSize-1);
 				} else 
 				{
 					ItemStacks[0] = null;
 				}
 			}
 		}*/
 		return;
 	}
 	
 	
 	
 	@Override
     public int getSizeInventory() {
             return ItemStacks.length;
     }
 
     @Override
     public ItemStack getStackInSlot(int slot) {
             return ItemStacks[slot];
     }
     
     @Override
     public void setInventorySlotContents(int slot, ItemStack stack) {
     		ItemStacks[slot] = stack;
             if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                     stack.stackSize = getInventoryStackLimit();
             }               
     }
 
     @Override
     public ItemStack decrStackSize(int slot, int amt) {
             ItemStack stack = getStackInSlot(slot);
             if (stack != null) {
                     if (stack.stackSize <= amt) {
                             setInventorySlotContents(slot, null);
                     } else {
                             stack = stack.splitStack(amt);
                             if (stack.stackSize == 0) {
                                     setInventorySlotContents(slot, null);
                             }
                     }
             }
             return stack;
     }
 
     @Override
     public ItemStack getStackInSlotOnClosing(int slot) {
             ItemStack stack = getStackInSlot(slot);
             if (stack != null) {
                     setInventorySlotContents(slot, null);
             }
             return stack;
     }
     
     @Override
     public int getInventoryStackLimit() {
             return 64;
     }
 
     @Override
     public boolean isUseableByPlayer(EntityPlayer player) {
             return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
             player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
     }
     
     
 
     @Override
     public void openChest() {}
 
     @Override
     public void closeChest() {}
     
     @Override
     public void readFromNBT(NBTTagCompound tagCompound) 
     {
     	super.readFromNBT(tagCompound);
         NBTTagList var2 = tagCompound.getTagList("compactor");
         this.ItemStacks = new ItemStack[this.getSizeInventory()];
         System.out.println(var2.tagCount());
         for (int var3 = 0; var3 < var2.tagCount(); ++var3)
         {
             NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
             byte var5 = var4.getByte("Slot");
 
             if (var5 >= 0 && var5 < this.ItemStacks.length)
             {
                 this.ItemStacks[var5] = ItemStack.loadItemStackFromNBT(var4);
             }
         }
     }
 
     @Override
     public void writeToNBT(NBTTagCompound tagCompound) {
             super.writeToNBT(tagCompound);
             NBTTagList itemList = new NBTTagList();
             
             for (int i = 0; i < ItemStacks.length; i++) {
                     ItemStack stack = ItemStacks[i];
                     if (stack != null) {
                             NBTTagCompound tag = new NBTTagCompound();
                             tag.setByte("Slot", (byte) i);
                             stack.writeToNBT(tag);
                             itemList.appendTag(tag);
                     }
             }
             tagCompound.setTag("compactor", itemList);
     }
 
 	@Override
 	public String getInvName() {
 		return "Compactor";
 	}
 
 	@Override
 	public void setPowerProvider(IPowerProvider iprovider) {
 		// TODO Auto-generated method stub
 		provider = iprovider;
 	}
 
 	@Override
 	public IPowerProvider getPowerProvider() {
 		// TODO Auto-generated method stub
 		return provider;
 	}
 
 	@Override
 	public void doWork() {
 		if (provider.useEnergy(25, 25, true) < 25)
 			return;
 		World world = worldObj;
 		double radius = 3.0D;
 		List list1 = world.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)this.xCoord-radius, (double)this.yCoord-1.0D, (double)this.zCoord-radius, (double)this.xCoord+radius, (double)this.yCoord+1.0D, (double)this.zCoord+radius));
 		
 		EntityLiving entity = null;
         double var6 = Double.MAX_VALUE;
         Iterator entitys = list1.iterator();
 
         while (entitys.hasNext())
         {
         	
             EntityLiving var9 = (EntityLiving)entitys.next();
 
             if (!(var9 instanceof EntityPlayer) && !(var9 instanceof EntityDragonBase))
             {
 	            double var10 = this.getDistanceSqToEntity(var9);
 	
 	            if (var10 <= var6)
 	            {
 	                entity = var9;
 	                var6 = var10;
 	            }
             }
         }
 		if (ItemStacks[0] != null && entity != null)
 		{
 			int id = EntityList.getEntityID(entity);
 			
 			//CompactMobsCore.instance.cmLog.info("1: " + String.valueOf(id));
 			
 			//var5.getHealth();
 			//world.spawnEntityInWorld(newMob);
 			if (ItemStacks[0].getItem() == CompactMobsItems.mobHolder);
 			{
 				int empty = 0;
 				int var3;
 				for (var3 = 1; var3 < 28; ++var3)
 				{
 					if(ItemStacks[var3] == null)
 					{
 						empty++;
 						break;
 					}
 				}
 				
 				if (empty > 0)
 				{
 					
 					ItemStack holder = new ItemStack(CompactMobsItems.fullMobHolder, 1);
 					//FullMobHolder holder = new FullMobHolder(CompactMobsItems.fullMobHolder.shiftedIndex);
 					NBTTagCompound nbttag = holder.stackTagCompound;
 					if (nbttag == null)
 					{
 						nbttag = new NBTTagCompound();
 					}
 					nbttag.setInteger("entityId", id);
 					
 					
 					nbttag.setInteger("entityHealth", entity.getHealth());
 					
 					if (entity instanceof EntitySlime)
 					{
 						EntitySlime entitySlime = (EntitySlime)entity;
 						nbttag.setInteger("entitySize", entitySlime.getSlimeSize());
 					}
 					if (entity instanceof EntityAgeable)
 					{
 						EntityAgeable entityAge = (EntityAgeable)entity;
 						nbttag.setInteger("entityGrowingAge", entityAge.getGrowingAge());
 					}
 					
 					if (entity instanceof EntitySheep)
 					{
 						EntitySheep entitySheep = (EntitySheep)entity;
 						nbttag.setBoolean("entitySheared", entitySheep.getSheared());
 						nbttag.setInteger("entityColor", entitySheep.getFleeceColor());
 					}
 					if (entity instanceof EntityVillager)
 					{
 						EntityVillager entityVillager = (EntityVillager)entity;
 						nbttag.setInteger("entityProfession", entityVillager.getProfession());
 						//nbttag.setCompoundTag("Offers", entityVillager.get)
 					}
 					if (CompactMobsCore.instance.useFullTagCompound)
 					{
 						NBTTagCompound nbttag2 = new NBTTagCompound();
 						entity.writeEntityToNBT(nbttag2);
 						nbttag.setCompoundTag("compound", entity.getEntityData());
 					}
 					String name = entity.getEntityName();
 					nbttag.setString("name", name);
 					holder.setItemDamage(id);
 					
 					spawnParticles(world, entity.posX, entity.posY, entity.posZ);
 					//spawnParticles(world, entity.posX, entity.posY, entity.posZ);
 					
 					
 					holder.setTagCompound(nbttag);
 					ItemStacks[var3] = holder;
 					world.removeEntity(entity);
 					
 					
 					
 					/*if (true)//holder.hasTagCompound())
 					{
 						NBTTagCompound nbttag = new NBTTagCompound();
 						nbttag.setInteger("entityId", id);
 						holder.writeToNBT(nbttag);//func_77983_a("tag",nbttag);
 						//CompactMobsCore.instance.cmLog.info("2: " + String.valueOf(holder.hasTagCompound()));
 						//nbttag = holder.getTagCompound();
 						
 						NBTBase test = holder.getTagCompound().getTag("entityId");
 						
 						
 						//setId(EntityList.getEntityID(var5));
 						ItemStacks[var3] = holder;
 						CompactMobsCore.instance.cmLog.info("2: " + String.valueOf(test));
 					}*/
 				}
 				
 				/*for (int var3 = 1; var3 < 29; ++var3)
 		        {
 					if (ItemStacks[var3] != null)
 					{
 						if (ItemStacks[var3].getItem() == Item.paper){
 							if(ItemStacks[var3].stackSize < 64){
 								ItemStacks[var3] = new ItemStack(Item.paper, ItemStacks[var3].stackSize+1);
 								break;
 							}
 						}
 					} else
 					{
 						ItemStacks[var3] = new ItemStack(Item.paper, 1);
 						break;
 					}
 		        }*/
 				
 				if (ItemStacks[0].stackSize > 1)
 				{
 					ItemStacks[0] = new ItemStack(ItemStacks[0].getItem(), ItemStacks[0].stackSize-1);
 				} else 
 				{
 					ItemStacks[0] = null;
 				}
 			}
 		}
 		dumpItems();
 		//worldObj.spawnParticle("smoke", this.xCoord+1.5D, this.yCoord+.5D, this.zCoord+.5, -.1, 0, 0);
 		//spawnParticles(this.worldObj, this.xCoord+1, this.yCoord, this.zCoord);
 		//spawnParticles(this.worldObj, this.xCoord+1, this.yCoord, this.zCoord);
 		//this.worldObj.spawnParticle("smoke", this.xCoord+1.5D, this.yCoord+.5D, this.zCoord+.5, -.1, 0, 0);
 		
 	}
 	
	@SideOnly(Side.CLIENT)
 	public void spawnParticles (World world, double x, double y, double z)
 	{
 		//CompactMobsCore.instance.cmLog.info("Got");
 		double xv, yv, zv;
 		if (this.yCoord - y > 0)
 		{
 			yv = (this.yCoord - y)/10;
 		}
 		else
 		{
 			yv = 0;
 		}
 		if (this.xCoord - x > 0)
 		{
 			xv = (this.xCoord - x)/10;
 		}
 		else if(this.xCoord - x < 0)
 		{
 			xv = (this.xCoord - x)/10;
 		}
 		else
 		{
 			xv = 0;
 		}
 		if (this.zCoord - z > 0)
 		{
 			zv = (this.zCoord - z)/10;
 		}
 		else if(this.zCoord - z < 0)
 		{
 			zv = (this.zCoord - z)/10;
 		}
 		else
 		{
 			zv = 0;
 		}
 		CompactMobsCore.instance.proxy.spawnParticle("explode", x+.5D, y+.5D, z+.5D, xv, yv, zv, 10);
 		//this.worldObj.spawnParticle("smoke", this.xCoord+1.5D, this.yCoord+.5D, this.zCoord+.5, -.1, 0, 0);
 		//world.spawnParticle("smoke", x+.5D, y+.5D, z+.5D, xv, yv, zv);
 	}
 
 	@Override
 	public int powerRequest() {
 		// TODO Auto-generated method stub
 		return 25;
 	}
 	
 	public void dumpItems()
 	{
 		
 		Orientations[] pipes = Utils.getPipeDirections(this.worldObj, new Vect(this.xCoord, this.yCoord, this.zCoord), Orientations.XNeg);
 	    if (pipes.length > 0) {
 	      dumpToPipe(pipes);
 	    } else {
 	      IInventory[] inventories = Utils.getAdjacentInventories(this.worldObj, new Vect(this.xCoord, this.yCoord, this.zCoord));
 	      dumpToInventory(inventories);
 	    }
 	}
 	
 	private void dumpToPipe(Orientations[] pipes)
 	{
 		Orientations[] filtered;
 		filtered = Utils.filterPipeDirections(pipes, new Orientations[] { Orientations.YNeg, Orientations.YPos});
 		
 		for (int i = 1; i < 28; i++) {
 			if (ItemStacks[i]!=null)
 			{
 				while ((ItemStacks[i].stackSize>0) && (filtered.length > 0)) {
 					Utils.putFromStackIntoPipe(this, filtered, ItemStacks[i]);
 					
 				}
 				if (this.ItemStacks[i].stackSize <= 0)
 					this.ItemStacks[i] = null;
 			}
 		}
 	}
 	 
 	public void dumpToInventory(IInventory[] inventories)
 	{
 	for (int i = 1; i < 28; i++) {
 		if (ItemStacks[i]!=null && ItemStacks[i].stackSize>0)
 		{
 			
 		 
 			for (int j = 0; j < inventories.length; j++)
 			{
 				if (inventories[j] != null)
 				{
 					IInventory inventory = Utils.getChest(inventories[j]);
 				
 				
 					Utils.stowInInventory(ItemStacks[i], inventory, true);
 					if (this.ItemStacks[i].stackSize <= 0)
 						this.ItemStacks[i] = null;
 					}
 				}
 			}
 		}
 	}
 
 
 	
 	public double getDistanceSqToEntity(Entity par1Entity)
     {
         double var2 = this.xCoord - par1Entity.posX;
         double var4 = this.yCoord - par1Entity.posY;
         double var6 = this.zCoord - par1Entity.posZ;
         return var2 * var2 + var4 * var4 + var6 * var6;
     }
 
 	@Override
 	public int getStartInventorySide(ForgeDirection side) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getSizeInventorySide(ForgeDirection side) {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 	
 }
