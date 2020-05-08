 package compactMobs.TileEntity;
 
 import java.util.Random;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityList;
 import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityWither;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.DamageSource;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import buildcraft.api.power.IPowerProvider;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerFramework;
 
 import compactMobs.CompactMobsCore;
 import compactMobs.Utils;
 import compactMobs.Vect;
 import compactMobs.Items.CompactMobsItems;
 
 public class TileEntityCatalyst extends TileEntity implements IInventory, IPowerReceptor, ISidedInventory {
 
     public ItemStack[] ItemStacks;
     IPowerProvider provider;
     //protected Random rand;
 
     public TileEntityCatalyst() {
         ItemStacks = new ItemStack[31];
         if (PowerFramework.currentFramework != null) {
             provider = PowerFramework.currentFramework.createPowerProvider();
         }
         provider.configure(50, 25, 25, 25, 1000);
     }
 
 
     @Override
     public void updateEntity() {
         if (this instanceof IPowerReceptor) {
             IPowerReceptor receptor = ((IPowerReceptor) this);
 
             receptor.getPowerProvider().update(receptor);
         }
         /*
          * if (ItemStacks[0] != null) { if (ItemStacks[0].getItem() ==
          * Item.redstone) { for (int var3 = 1; var3 < 29; ++var3) { if
          * (ItemStacks[var3] != null) { if (ItemStacks[var3].getItem() ==
          * Item.paper){ if(ItemStacks[var3].stackSize < 64){ ItemStacks[var3] =
          * new ItemStack(Item.paper, ItemStacks[var3].stackSize+1); break; } } }
          * else { ItemStacks[var3] = new ItemStack(Item.paper, 1); break; } }
          *
          * if (ItemStacks[0].stackSize > 1) { ItemStacks[0] = new
          * ItemStack(ItemStacks[0].getItem(), ItemStacks[0].stackSize-1); } else
          * { ItemStacks[0] = null; } } }
          */
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
         return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
                 && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
     }
 
     @Override
     public void openChest() {
     }
 
     @Override
     public void closeChest() {
     }
 
     @Override
     public void readFromNBT(NBTTagCompound tagCompound) {
         super.readFromNBT(tagCompound);
         NBTTagList var2 = tagCompound.getTagList("compactor");
         this.ItemStacks = new ItemStack[this.getSizeInventory()];
         System.out.println(var2.tagCount());
         for (int var3 = 0; var3 < var2.tagCount(); ++var3) {
             NBTTagCompound var4 = (NBTTagCompound) var2.tagAt(var3);
             byte var5 = var4.getByte("Slot");
 
             if (var5 >= 0 && var5 < this.ItemStacks.length) {
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
         if (provider.useEnergy(25, 25, true) < 25) {
             return;
         }
         World world = worldObj;
         //double radius = 3.0D;
         //List list1 = world.getEntitiesWithinAABB(EntityCreature.class, AxisAlignedBB.getAABBPool().addOrModifyAABBInPool((double)this.xCoord-radius, (double)this.yCoord-1.0D, (double)this.zCoord-radius, (double)this.xCoord+radius, (double)this.yCoord+1.0D, (double)this.zCoord+radius));
 
         //EntityCreature entity = null;
         //double var6 = Double.MAX_VALUE;
         //Iterator entitys = list1.iterator();
 
         /*
          * while (entitys.hasNext()) { EntityCreature var9 =
          * (EntityCreature)entitys.next();
          *
          *
          * double var10 = this.getDistanceSqToEntity(var9);
          *
          * if (var10 <= var6) { entity = var9; var6 = var10; }
          *
          * }
          */
         ItemStack stack = null;
         int stackNum;
         for (stackNum = 0; stackNum < 27; stackNum++) {
             if (ItemStacks[stackNum] != null) {
                 if (ItemStacks[stackNum].getItem() == CompactMobsItems.fullMobHolder) {
                     stack = ItemStacks[stackNum];
                     break;
                 }
             }
         }
 
         int outStackNum;
         boolean out = false;
         for (outStackNum = 27; outStackNum < 30; outStackNum++) {
             if (ItemStacks[outStackNum] != null) {
                 if (ItemStacks[outStackNum].getItem() == CompactMobsItems.mobHolder) {
                     if (ItemStacks[outStackNum].stackSize < 64) {
                         out = true;
                         break;
                     }
                 }
             }
             if (ItemStacks[outStackNum] == null) {
                 out = true;
                 break;
             }
         }
 
 
         if (stack != null && out && ItemStacks[30]!= null) {
 
         	if (ItemStacks[30].getItem()==CompactMobsItems.catalystCore)
         	{
 	            NBTTagCompound nbttag = stack.getTagCompound();
 	            if (nbttag != null) {
 	                int dir = world.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
 	
 	                EntityLiving entity = null;
 	                if (CompactMobsCore.instance.useFullTagCompound && nbttag.hasKey("entityTags")) {
 	                    //CompactMobsCore.instance.cmLog.info(String.valueOf(nbttag.hasKey("compound")));
 	
 	
 	
 	                    NBTTagCompound newCompound = nbttag.getCompoundTag("entityTags");
 	                    int id = nbttag.getInteger("entityId");
 	                    entity = (EntityLiving) EntityList.createEntityByID(id, world);
 	                    //entity = (EntityLiving) EntityList.createEntityFromNBT(newCompound, world);
 	                    entity.readFromNBT(newCompound);
 	                    
 	                    //entity = (EntityLiving) EntityList.createEntityFromNBT(newCompound, world);
 	
 	                    //CompactMobsCore.instance.cmLog.info("dir: "+String.valueOf(dir));
 	
 	                    Random random = new Random();
 	                    if (dir == 2) {
 	                        entity.setPosition(this.xCoord + .5D, this.yCoord, this.zCoord - .5D);
 	                        CompactMobsCore.instance.proxy.spawnParticle("largesmoke", this.xCoord + .5D, this.yCoord + .5D, this.zCoord, 0, 0, -.1, 10);
 	                    } else if (dir == 3) {
 	                        entity.setPosition(this.xCoord + .5D, this.yCoord, this.zCoord + 1.5D);
 	                        CompactMobsCore.instance.proxy.spawnParticle("largesmoke", this.xCoord + .5D, this.yCoord + .5D, this.zCoord + 1D, 0, 0, .1, 10);
 	                    } else if (dir == 4) {
 	                        entity.setPosition(this.xCoord - .5D, this.yCoord, this.zCoord + .5D);
 	                        CompactMobsCore.instance.proxy.spawnParticle("largesmoke", this.xCoord, this.yCoord + .5D, this.zCoord + .5D, -.1, 0, 0, 10);
 	                    } else if (dir == 5) {
 	                        entity.setPosition(this.xCoord + 1.5D, this.yCoord, this.zCoord + .5D);
 	                        CompactMobsCore.instance.proxy.spawnParticle("largesmoke", this.xCoord - 1D, this.yCoord + .5D, this.zCoord + .5D, .1, 0, 0, 10);
 	                    } else {
 	                        entity.setPosition(this.xCoord + .5D, this.yCoord, this.zCoord - .5D);
 	                        CompactMobsCore.instance.proxy.spawnParticle("largesmoke", this.xCoord + .5D, this.yCoord + .5D, this.zCoord, 0, 0, -.1, 10);
 	                    }
	                    
	                    if (entity instanceof EntityWither)
	                    {
	                    	world.spawnEntityInWorld(entity);
	                    }
	                    else
	                    {	                    
 	                    entity.onDeath(DamageSource.generic);
 	                    entity.onDeath(DamageSource.generic);
	                    }
 	                }
 	
 	
 	
 	            }
 	            //int id = EntityList.getEntityID(entity);
 	
 	            //CompactMobsCore.instance.cmLog.info("1: " + String.valueOf(id));
 	
 	            //var5.getHealth();
 	            //world.spawnEntityInWorld(newMob);
 	            if (ItemStacks[stackNum].stackSize > 1) {
 	                ItemStacks[stackNum] = new ItemStack(ItemStacks[stackNum].getItem(), ItemStacks[stackNum].stackSize - 1);
 	            } else {
 	                ItemStacks[stackNum] = null;
 	            }
 	
 	            if (worldObj.rand.nextInt(10) != 0) {
 	                if (ItemStacks[outStackNum] != null) {
 	                    ItemStacks[outStackNum] = new ItemStack(ItemStacks[outStackNum].getItem(), ItemStacks[outStackNum].stackSize + 1);
 	                } else {
 	                    ItemStacks[outStackNum] = new ItemStack(CompactMobsItems.mobHolder, 1);
 	                }
 	            }
 	            
 	            ItemStacks[30].setItemDamage(ItemStacks[30].getItemDamage()+1);
 	            if (ItemStacks[30].getItemDamage() > 150)
 	            {
 	            	ItemStacks[30]=null;
 	            }
 	
 	        }
         }
         dumpItems();
 
     }
 
     public void dumpItems() {
 
         ForgeDirection[] pipes = Utils.getPipeDirections(this.worldObj, new Vect(this.xCoord, this.yCoord, this.zCoord), ForgeDirection.WEST);
         if (pipes.length > 0) {
             dumpToPipe(pipes);
         } else {
             IInventory[] inventories = Utils.getAdjacentInventories(this.worldObj, new Vect(this.xCoord, this.yCoord, this.zCoord));
             dumpToInventory(inventories);
         }
     }
 
     private void dumpToPipe(ForgeDirection[] pipes) {
         ForgeDirection[] filtered;
         filtered = Utils.filterPipeDirections(pipes, new ForgeDirection[]{ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.SOUTH});
 
         for (int i = 27; i < 30; i++) {
             if (ItemStacks[i] != null) {
                 while ((ItemStacks[i].stackSize > 0) && (filtered.length > 0)) {
                     Utils.putFromStackIntoPipe(this, filtered, ItemStacks[i]);
 
                 }
                 if (this.ItemStacks[i].stackSize <= 0) {
                     this.ItemStacks[i] = null;
                 }
             }
         }
     }
 
     public void dumpToInventory(IInventory[] inventories) {
         for (int i = 27; i < 30; i++) {
             if (ItemStacks[i] != null && ItemStacks[i].stackSize > 0) {
 
 
                 for (int j = 0; j < inventories.length; j++) {
                     if (inventories[j] != null) {
                         IInventory inventory = Utils.getChest(inventories[j]);
 
 
                         Utils.stowInInventory(ItemStacks[i], inventory, true);
                         if (this.ItemStacks[i].stackSize <= 0) {
                             this.ItemStacks[i] = null;
                         }
                     }
                 }
             }
         }
     }
 
     @Override
     public int powerRequest() {
         // TODO Auto-generated method stub
         return 25;
     }
 
     public double getDistanceSqToEntity(Entity par1Entity) {
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
         return 27;
     }
 }
 
