 package kaijin.InventoryStocker;
 
 import net.minecraft.src.*;
 import net.minecraft.src.forge.*;
 import kaijin.InventoryStocker.*;
 
 public class TileEntityInventoryStocker extends TileEntity implements IInventory, ISidedInventory
 {
     //ItemStack privates
     private ItemStack contents[];
     private ItemStack remoteSnapshot[];
 
     //Boolean privates
     private boolean previousPoweredState = false;
     private boolean hasSnapshot = false;
     private boolean tileLoaded = false;
     private boolean guiTakeSnapshot = false;
 
     //other privates
     private TileEntity lastTileEntity = null;
     public TileEntity tileFrontFace = null;
     private String targetTileName = "none";
     private int remoteNumSlots = 0;
 
     @Override
     public boolean canUpdate()
     {
         return true;
     }
 
     public TileEntityInventoryStocker()
     {
         this.contents = new ItemStack [this.getSizeInventory()];
         this.clearSnapshot();
     }
 
     public void setSnapshotState(boolean state)
     {
         if(!Utils.isClient(worldObj))
         {
             this.hasSnapshot = state;
         }
         else
         {
             //send packet to server asking for it to take a snapshot
         }
     }
     
     public boolean validSnapshot()
     {
         return hasSnapshot;
     }
     
     public void guiTakeSnapshot()
     {
         if(!Utils.isClient(worldObj))
         {
             guiTakeSnapshot = true;
         }
         else
         {
             //send packet to server asking for it to take a snapshot            
         }
     }
 
     public void guiClearSnapshot()
     {
         if(!Utils.isClient(worldObj))
         {
             clearSnapshot();
         }
         else
         {
             //send packet to server asking for it to clear the snapshot
         }
     }
     
     public void clearSnapshot()
     {
         lastTileEntity = null;
         hasSnapshot = false;
         targetTileName = "none";
         remoteSnapshot = null;
         remoteNumSlots = 0;
     }
 
     public int getStartInventorySide(int i)
     {
         // Sides (0-5) are: Front, Back, Top, Bottom, Right, Left
         int side = getRotatedSideFromMetadata(i);
 
         if (side == 1)
         {
             return 9;    // access output section, 9-17
         }
 
         return 0; // access input section, 0-8
     }
 
     public int getSizeInventorySide(int i)
     {
         // Sides (0-5) are: Top, Bottom, Front, Back, Left, Right
         int side = getRotatedSideFromMetadata(i);
 
         if (side == 0)
         {
             return 0;    // Front has no inventory access
         }
 
         return 9;
     }
 
     public int getRotatedSideFromMetadata(int side)
     {
         int dir = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 7;
         return Utils.lookupRotatedSide(side, dir);
     }
 
     public TileEntity getTileAtFrontFace()
     {
         int dir = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 7;
         /**
          *      0: -Y (bottom side)
          *      1: +Y (top side)
          *      2: -Z (west side)
          *      3: +Z (east side)
          *      4: -X (north side)
          *      5: +x (south side)
          */
         int x = xCoord;
         int y = yCoord;
         int z = zCoord;
 
         switch (dir)
         {
             case 0:
                 y--;
                 break;
 
             case 1:
                 y++;
                 break;
 
             case 2:
                 z--;
                 break;
 
             case 3:
                 z++;
                 break;
 
             case 4:
                 x--;
                 break;
 
             case 5:
                 x++;
                 break;
         }
         return worldObj.getBlockTileEntity(x, y, z);
     }
 
     public int getSizeInventory()
     {
         return 18;
     }
 
     public ItemStack getStackInSlot(int i)
     {
         return contents[i];
     }
 
     public ItemStack decrStackSize(int par1, int par2)
     {
         if (this.contents[par1] != null)
         {
             ItemStack var3;
 
             if (this.contents[par1].stackSize <= par2)
             {
                 var3 = this.contents[par1];
                 this.contents[par1] = null;
                 this.onInventoryChanged();
                 return var3;
             }
             else
             {
                 var3 = this.contents[par1].splitStack(par2);
 
                 if (this.contents[par1].stackSize == 0)
                 {
                     this.contents[par1] = null;
                 }
 
                 this.onInventoryChanged();
                 return var3;
             }
         }
         else
         {
             return null;
         }
     }
 
     public ItemStack getStackInSlotOnClosing(int var1)
     {
         if (this.contents[var1] == null)
         {
             return null;
         }
 
         ItemStack stack = this.contents[var1];
         this.contents[var1] = null;
         return stack;
     }
 
     public void setInventorySlotContents(int i, ItemStack itemstack)
     {
         this.contents[i] = itemstack;
 
         if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
         {
             itemstack.stackSize = getInventoryStackLimit();
         }
     }
 
     public String getInvName()
     {
         return "Stocker";
     }
 
     /**
      * Reads a tile entity from NBT.
      */
     public void readFromNBT(NBTTagCompound nbttagcompound)
     {
         if(!Utils.isClient(worldObj))
         {
             super.readFromNBT(nbttagcompound);
             //read extra NBT stuff here
             targetTileName = nbttagcompound.getString("targetTileName");
             remoteNumSlots = nbttagcompound.getInteger("remoteSnapshotSize");
             
             System.out.println("ReadNBT: "+targetTileName+" remoteInvSize:"+remoteNumSlots);
             
             NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
             NBTTagList nbttagremote = nbttagcompound.getTagList("remoteSnapshot");
             
             this.contents = new ItemStack[this.getSizeInventory()];
             this.remoteSnapshot = null;
             if (remoteNumSlots != 0)
             {
                 this.remoteSnapshot = new ItemStack[remoteNumSlots];
             }
 
             //our inventory
             for (int i = 0; i < nbttaglist.tagCount(); ++i)
             {
                 NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
                 int j = nbttagcompound1.getByte("Slot") & 255;
 
                 if (j >= 0 && j < this.contents.length)
                 {
                     this.contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
                 }
             }
 
             //remote inventory
             System.out.println("ReadNBT tagRemoteCount: "+nbttagremote.tagCount());
             if (nbttagremote.tagCount() != 0)
             {
                 for (int i = 0; i < nbttagremote.tagCount(); ++i)
                 {
                     NBTTagCompound remoteSnapshot1 = (NBTTagCompound)nbttagremote.tagAt(i);
                     int j = remoteSnapshot1.getByte("Slot") & 255;
 
                     if (j >= 0 && j < this.remoteSnapshot.length)
                     {
                         this.remoteSnapshot[j] = ItemStack.loadItemStackFromNBT(remoteSnapshot1);
                         System.out.println("ReadNBT Remote Slot: "+j+" ItemID: "+this.remoteSnapshot[j].itemID);
                     }
                 }
             }
         }
     }
 
     /**
      * Writes a tile entity to NBT.
      */
     public void writeToNBT(NBTTagCompound nbttagcompound)
     {
         if(!Utils.isClient(worldObj))
         {
             super.writeToNBT(nbttagcompound);
             NBTTagList nbttaglist = new NBTTagList();
             NBTTagList nbttagremote = new NBTTagList();
             
             //our inventory
             for (int i = 0; i < this.contents.length; ++i)
             {
                 if (this.contents[i] != null)
                 {
                     NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                     nbttagcompound1.setByte("Slot", (byte)i);
                     this.contents[i].writeToNBT(nbttagcompound1);
                     nbttaglist.appendTag(nbttagcompound1);
                 }
             }
             
             //remote inventory
             if (this.remoteSnapshot != null)
             {
                 System.out.println("writeNBT Target: "+targetTileName+" remoteInvSize:"+this.remoteSnapshot.length);
                 for (int i = 0; i < this.remoteSnapshot.length; i++)
                 {
                     if (this.remoteSnapshot[i] != null)
                     {
                         System.out.println("writeNBT Remote Slot: "+i+" ItemID: "+this.remoteSnapshot[i].itemID+" StackSize: "+this.remoteSnapshot[i].stackSize+" meta: "+this.remoteSnapshot[i].getItemDamage());
                         NBTTagCompound remoteSnapshot1 = new NBTTagCompound();
                         remoteSnapshot1.setByte("Slot", (byte)i);
                         this.remoteSnapshot[i].writeToNBT(remoteSnapshot1);
                         nbttagremote.appendTag(remoteSnapshot1);
                     }
                 }
             }
             else
             {
                 System.out.println("writeNBT Remote Items is NULL!");
             }
                         
             //write stuff to NBT here
             nbttagcompound.setTag("Items", nbttaglist);
             nbttagcompound.setTag("remoteSnapshot", nbttagremote);
             nbttagcompound.setString("targetTileName", targetTileName);
             nbttagcompound.setInteger("remoteSnapshotSize", remoteNumSlots);
         }
     }
 
     public void onLoad()
     {
         /*
          * This function fires only once on first load of an instance of our tile and attempts to see
          * if we should have a valid inventory or not. It will set the lastTileEntity and
          * hasSnapshot state. The actual remoteInventory object will be loaded (or not) via the NBT calls.
          */
         if(!Utils.isClient(worldObj))
         {
             tileLoaded = true;
             System.out.println("onLoad, remote inv size = " + remoteNumSlots);
             TileEntity tile = getTileAtFrontFace();
             if (tile == null)
             {
                 System.out.println("onLoad tile = null");
                 clearSnapshot();
             }
             else
             {
                 String tempName = tile.getClass().getName();
                 if (tempName.equals(targetTileName) && ((IInventory)tile).getSizeInventory() == remoteNumSlots)
                 {
                     System.out.println("onLoad, target name="+tempName+" stored name="+targetTileName+" MATCHED!");
                     lastTileEntity = tile;
                     hasSnapshot = true;
                 }
                 else
                 {
                     System.out.println("onLoad, target name="+tempName+" stored name="+targetTileName+" NOT matched.");
                     clearSnapshot();
                 }
             }
         }
     }
         
     public int getInventoryStackLimit()
     {
         return 64;
     }
 
     public boolean isUseableByPlayer(EntityPlayer entityplayer)
     {
         if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
         {
             return false;
         }
 
         return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
     }
 
     public void openChest()
     {
         // TODO Auto-generated method stub
     }
 
     public void closeChest()
     {
         // TODO Auto-generated method stub
     }
 
     public ItemStack[] takeSnapShot(TileEntity tile)
     {
         /*
          * This function will take a snapshot the IInventory of the TileEntity passed to it.
          * This will be a copy of the remote inventory as it looks when this function is called.
          *
          * It will check that the TileEntity passed to it actually implements IInventory and
          * return doing false if it does not.
          * 
          * Will return true if it successfully took a snapshot
          */
         if (!(tile instanceof IInventory))
         {
             return null;
         }
 
         // Get number of slots in the remote inventory
         this.remoteNumSlots = ((IInventory)tile).getSizeInventory();
         ItemStack tempCopy;
         ItemStack returnCopy[] = new ItemStack[this.remoteNumSlots];
 
         // Iterate through remote slots and make a copy of it
         for (int i = 0; i < this.remoteNumSlots; i++)
         {
             tempCopy = ((IInventory)tile).getStackInSlot(i);
 
             if (tempCopy == null)
             {
                 returnCopy[i] = null;
             }
             else
             {
                 returnCopy[i] = new ItemStack(tempCopy.itemID, tempCopy.stackSize, tempCopy.getItemDamage());
 
                 if (tempCopy.stackTagCompound != null)
                 {
                     returnCopy[i].stackTagCompound = (NBTTagCompound)tempCopy.stackTagCompound.copy();
                 }
             }
         }
         /*
          *  get remote entity class name and store it as targetTile, which also ends up being stored in our
          *  own NBT tables so our tile will remember what was there being chunk unloads/restarts/etc
          */
         this.targetTileName = tile.getClass().getName();
         return returnCopy;
     }
 
     public boolean inputGridIsEmpty()
     {
         for (int i=0; i<9; i++)
         {
             if (contents[i] != null)
             {
                 return false;
             }
         }
         return true;
     }
 
     protected void stockInventory(IInventory tile)
     {
         int startSlot = 0;
         int endSlot = startSlot + tile.getSizeInventory();
 
         boolean workDone;
         // Now makes two passes through the target and snapshot to help with 'item in wrong slot' adjustments
         do
         {
             workDone = false;
             for (int slot = startSlot; slot < endSlot; slot++)
             {
                 ItemStack i = tile.getStackInSlot(slot);
                 ItemStack s = remoteSnapshot[slot];
                 if (i == null)
                 {
                     if (s == null)
                         continue; // Slot is and should be empty. Next!
     
                     // Slot is empty but shouldn't be. Add what belongs there.
                     workDone = addItemToRemote(slot, tile, remoteSnapshot[slot].stackSize);
                 }
                 else
                 {
                     // Slot is occupied. Figure out if contents belong there.
                     if (s == null)
                     {
                         // Nope! Slot should be empty. Need to remove this.
                         // Call helper function to do that here, and then
                         workDone = removeItemFromRemote(slot, tile, tile.getStackInSlot(slot).stackSize);
                         continue; // move on to next slot!
                     }
                     
                     // Compare contents of slot between remote inventory and snapshot.
                     if (checkItemTypesMatch(i, s))
                     {
                         // Matched. Compare stack sizes. Try to ensure there's not too much or too little.
                         int amtNeeded = remoteSnapshot[slot].stackSize - tile.getStackInSlot(slot).stackSize;
                         if (amtNeeded > 0)
                         {
                             workDone = addItemToRemote(slot, tile, amtNeeded);
                         }
                         else if (amtNeeded < 0)
                         {
                             workDone = removeItemFromRemote(slot, tile, -amtNeeded);
                         }
                         // else the size is already the same and we've nothing to do. Hooray!
                     }
                     else
                     {
                         // Wrong item type in slot! Try to remove what doesn't belong and add what does.
                         workDone = removeItemFromRemote(slot, tile, tile.getStackInSlot(slot).stackSize);
                         if (tile.getStackInSlot(slot) == null)
                             workDone = addItemToRemote(slot, tile, remoteSnapshot[slot].stackSize);
                     }
                 } // else
             } // for slot
         } while (workDone);
     }
 
     // Test if two item stacks' types match, while ignoring damage level if needed.  
     protected boolean checkItemTypesMatch(ItemStack a, ItemStack b)
     {
         // System.out.println("checkItemTypesMatch: a: "+ a +" b: "+ b +"");
         // System.out.println("checkItemTypesMatch: .isStackable() a: "+ a.isStackable() +" b: "+ b.isStackable() +"");
         // System.out.println("checkItemTypesMatch: .getItemDamage() a: "+ a.getItemDamage() +" b: "+ b.getItemDamage() +"");
         // System.out.println("checkItemTypesMatch: .isItemStackDamageable() a: "+ a.isItemStackDamageable() +" b: "+ b.isItemStackDamageable() +"");
 
         if (a.itemID == b.itemID)
         {        
             // Ignore damage value of damageable items while testing for match!
             if (a.isItemStackDamageable())
                 return true;
 
             // Already tested ItemID, so a.isItemEqual(b) would be partially redundant.
             if (a.getItemDamage() == b.getItemDamage())
                 return true;
         }
         return false;
     }
     
     protected boolean removeItemFromRemote(int slot, IInventory remote, int amount)
     {
         // Find room in output grid
         // Use checkItemTypesMatch on any existing contents to see if the new output will stack
         // If all existing ItemStacks become full, and there is no room left for a new stack,
         // leave the untransferred remainder in the remote inventory.
         
         boolean partialMove = false;
         ItemStack remoteStack = remote.getStackInSlot(slot);
         if (remoteStack == null)
             return false;
         int max = remoteStack.getMaxStackSize();
         int amtLeft = amount;
         if (amtLeft > max)
             amtLeft = max;
 
         int delayedDestination = -1;
         for (int i = 9; i < 18; i++) // Pull only into the Output section
         {
             if (contents[i] == null)
             {
                 if (delayedDestination == -1) // Remember this parking space in case we don't find a matching partial slot. 
                     delayedDestination = i; // Remember to car-pool, boys and girls!
             }
             else if (checkItemTypesMatch(contents[i], remoteStack))
             {
                 int room = max - contents[i].stackSize;
                 if (room >= amtLeft)
                 {
                     // Space for all, so toss it in.
                     contents[i].stackSize += amtLeft;
                     remoteStack.stackSize -= amtLeft;
                     if (remoteStack.stackSize <= 0)
                         remote.setInventorySlotContents(slot, null);
                     return true;
                 }
                 else
                 {
                     // Room for some of it, so add what we can, then keep looking.
                     contents[i].stackSize += room;
                     remoteStack.stackSize -= room;
                     amtLeft -= room;
                     partialMove = true;
                 }
             }
         }
         
         if (amtLeft > 0 && delayedDestination >= 0)
         {
             // Not enough room in existing stacks, so transfer whatever's left to a new one.
             contents[delayedDestination] = remoteStack;
             remote.setInventorySlotContents(slot, null);
             return true;
         }
         return partialMove;
     }
 
     protected boolean addItemToRemote(int slot, IInventory remote, int amount)
     {
         boolean partialMove = false;
         int max = remoteSnapshot[slot].getMaxStackSize();
         int amtNeeded = amount;
         if (amtNeeded > max)
             amtNeeded = max;
 
         for (int i = 17; i >= 0; i--) // Scan Output section as well in case desired items were removed for being in the wrong slot
         {
             if (contents[i] != null && checkItemTypesMatch(contents[i], remoteSnapshot[slot]))
             {
                 if (remote.getStackInSlot(slot) == null)
                 {
                     // It's currently empty, so toss what we can into remote slot.
                     if (contents[i].stackSize > amtNeeded)
                     {
                         // Found more than enough to meet the quota, so shift it on over.
                         ItemStack extra = contents[i].splitStack(amtNeeded);
                         remote.setInventorySlotContents(slot, extra);
                         return true;
                     }
                     else
                     {
                         amtNeeded -= contents[i].stackSize;
                         remote.setInventorySlotContents(slot, contents[i]);
                         contents[i] = null;
                         if (amtNeeded <= 0)
                             return true;
                         partialMove = true;
                     }
                 }
                 else
                 {
                     // There's already some present, so transfer from one stack to the other.
                     if (contents[i].stackSize > amtNeeded)
                     {
                         // More than enough here, just add and subtract.
                         contents[i].stackSize -= amtNeeded;
                         remote.getStackInSlot(slot).stackSize += amtNeeded;
                         return true;
                     }
                     else
                     {
                         // This stack matches or is smaller than what we need. Consume it entirely.
                         amtNeeded -= contents[i].stackSize;
                         remote.getStackInSlot(slot).stackSize += contents[i].stackSize;
                         contents[i] = null;
                         if (amtNeeded <= 0)
                             return true;
                         partialMove = true;
                     }
                 } // else
             } // if
         } // for
         return partialMove;
     }
 
     public boolean checkInvalidSnapshot()
     {
         /*
          * Will check if our snapshot should be invalidated, returns true if snapshot is invalid
          * false otherwise.
          */
 
         TileEntity tile = getTileAtFrontFace();
         if (tile == null)
         {
             System.out.println("Invalid: Tile = null");
             return true;
         }
         else
         {
             String tempName = tile.getClass().getName();
             if (!tempName.equals(targetTileName))
             {
                 System.out.println("Invalid: TileName Mismatched, detected TileName="+tempName+" expected TileName="+targetTileName);
                 return true;
             }
             else if (tile != lastTileEntity)
             {
                 System.out.println("Invalid: tileEntity does not match lastTileEntity");
                 return true;
             }
             else if (((IInventory)tile).getSizeInventory() != this.remoteNumSlots)
             {
                 System.out.println("Invalid: tileEntity inventory size has changed");
                 return true;
             }
         }
         return false;
     }
 
     public void onUpdate()
     {
         if(!Utils.isClient(worldObj))
         {
             if (checkInvalidSnapshot())
                 clearSnapshot();
         }
     }
     
     @Override
     public void updateEntity()
     {
         super.updateEntity();
         if(!Utils.isClient(worldObj))
         {
             // See if this tileEntity instance has ever loaded, if not, do some onLoad stuff to restore prior state
             if (!tileLoaded)
             {
                 System.out.println("tileLoaded false, running onLoad");
                 this.onLoad();
             }
 
             /*
              * Check if the GUI is asking us to take a snapshot, if so, clear the existing snapshot
              * and take a new one
              */
             if (guiTakeSnapshot)
             {
                 guiTakeSnapshot = false;
                 System.out.println("GUI take snapshot request");
                 TileEntity tile = getTileAtFrontFace();
                 if (tile != null && tile instanceof IInventory)
                 {
                     System.out.println("GUI: No snapshot-taking snapshot");
                     clearSnapshot();
                     remoteSnapshot = takeSnapShot(tile);
                     lastTileEntity = tile;
                     hasSnapshot = true;
                 }
             }
             
             // Check if one of the blocks next to us or us is getting power from a neighboring block. 
             boolean isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
            
            if (isPowered)
            {
                worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
            }
            
             // If we're not powered, set the previousPoweredState to false
             if (!isPowered && previousPoweredState)
             {
                 // Lost power.
                 previousPoweredState = false;
 
                 // Shut off glowing light textures.
                 int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord); // Grab current meta data
                 meta &= 7; // Clear bit 4
                 worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta); // And store it
                worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
             }
             
             /* If we are powered and the previous power state is false, it's time to go to
              * work. We test it this way so that we only trigger our work state once
              * per redstone power state cycle (pulse).
              */
             
             if (isPowered && !previousPoweredState)
             {
                 // We're powered now, set the state flag to true
                 previousPoweredState = true;
                 
                 // Turn on das blinkenlights!
                 int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord); // Grab current meta data
                 meta |= 8; // Set bit 4
                 worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta); // And store it
                worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
 
                 // grab TileEntity at front face
                 TileEntity tile = getTileAtFrontFace();
                 
                 // Verify that the tile we got back exists and implements IInventory            
                 if (tile != null && tile instanceof IInventory)
                 {
                     // Code here deals with the adjacent inventory
                     System.out.println("Chest Found!");
 
                     // Check if our snapshot is considered valid and/or the tile we just got doesn't
                     // match the one we had prior.
                     if (!hasSnapshot || checkInvalidSnapshot())
                     {
                         System.out.println("Redstone pulse: No snapshot-taking snapshot");
                         clearSnapshot();
                         remoteSnapshot = takeSnapShot(tile);
                         lastTileEntity = tile;
                         hasSnapshot = true;
 
                     }
                     else
                     {
                         // If we've made it here, it's time to stock the remote inventory
                         stockInventory((IInventory)tile);
                     }
                 }
                 else
                 {
                     /*
                      * This code deals with us not getting a valid tile entity from
                      * the getTileAtFrontFace code. This can happen because there is no
                      * detected tileentity (returned false), or the tileentity that was returned
                      * does not implement IInventory. We will clear the last snapshot.
                      */
                     clearSnapshot();
                     System.out.println("entityUpdate snapshot clear");
                 }
             }
         }
     }
 }
