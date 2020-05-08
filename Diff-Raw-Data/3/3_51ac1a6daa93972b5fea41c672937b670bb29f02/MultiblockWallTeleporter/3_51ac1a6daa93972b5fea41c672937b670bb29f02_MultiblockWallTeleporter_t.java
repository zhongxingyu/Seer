 package unwrittenfun.minecraft.wallteleporters.blocks.multiblocks;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagIntArray;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import unwrittenfun.minecraft.wallteleporters.blocks.tileentities.TileEntityWallTeleporter;
 import unwrittenfun.minecraft.wallteleporters.gui.containers.ContainerWallTeleporter;
 import unwrittenfun.minecraft.wallteleporters.handlers.PacketHandler;
 import unwrittenfun.minecraft.wallteleporters.info.BlockInfo;
 import unwrittenfun.minecraft.wallteleporters.items.WTItems;
 
 import java.util.ArrayList;
 
 /**
  * Mod: Wall Teleporters
  * Author: UnwrittenFun
  * License: Minecraft Mod Public License (Version 1.0.1)
  */
 public class MultiblockWallTeleporter implements IInventory {
     public ArrayList<TileEntityWallTeleporter> teleporters;
     public TileEntityWallTeleporter            controller;
 
     public String destinationWorldName = "";
     public int    destinationWorldId;
     public float  destinationX;
     public float  destinationZ;
     public float  destinationRotation;
     public float destinationY = -1;
 
     private boolean locked      = false;
     private boolean useRotation = true;
 
     public ContainerWallTeleporter container;
 
     private ArrayList<int[]> blocks;
 
     public MultiblockWallTeleporter() {
         teleporters = new ArrayList<TileEntityWallTeleporter>();
     }
 
     public MultiblockWallTeleporter(TileEntityWallTeleporter teleporter) {
         this();
         add(teleporter);
     }
 
     public void setLocked(boolean locked) {
         this.locked = locked;
 
         if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
             PacketHandler.sendLockedOrRotationPacket((byte) 0, this, null);
         }
     }
 
     public boolean isLocked() {
         return locked;
     }
 
     public void setShouldUseRotation(boolean useRotation) {
         this.useRotation = useRotation;
 
         if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
             PacketHandler.sendLockedOrRotationPacket((byte) 1, this, null);
         }
     }
 
     public boolean shouldUseRotation() {
         return useRotation;
     }
 
     public boolean hasDestination() {
         return destinationY >= 0;
     }
 
     public void setDestination(String worldName, int worldId, float x, float y, float z, float r) {
         destinationWorldName = worldName;
         destinationWorldId = worldId;
         destinationX = x;
         destinationY = y;
         destinationZ = z;
         destinationRotation = r;
 
         if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
             PacketHandler.sendDestinationPacket(this, null);
         }
     }
 
     public void clearDestination() {
         setDestination("", 0, 0F, -1F, 0F, 0F);
     }
 
     public void teleportToDestination(EntityPlayerMP player) {
         if (destinationWorldId != player.worldObj.provider.dimensionId) {
             player.travelToDimension(destinationWorldId);
         }
 
         float teleportR = player.rotationYaw;
 
         if (useRotation) teleportR = destinationRotation;
 
         player.playerNetServerHandler.setPlayerLocation(destinationX, destinationY + 0.5F, destinationZ,
                 teleportR, player.rotationPitch);
     }
 
     public boolean hasController() {
         return controller != null;
     }
 
     public boolean isController(TileEntityWallTeleporter teleporter) {
         return controller == teleporter;
     }
 
     public int count() {
         return teleporters.size();
     }
 
     public void add(TileEntityWallTeleporter teleporter) {
         if (!hasController()) {
             controller = teleporter;
         }
 
         if (!teleporters.contains(teleporter)) {
             teleporters.add(teleporter);
         }
 
         teleporter.multiblock = this;
     }
 
     public void recalculate() {
         for (TileEntityWallTeleporter teleporter : teleporters) {
             new MultiblockWallTeleporter(teleporter);
         }
 
         for (TileEntityWallTeleporter teleporter : teleporters) {
             teleporter.notifyNeighboursOfConnectionChanged();
         }
     }
 
     public void writeToNBT(NBTTagCompound compound) {
         NBTTagCompound wtCompound = new NBTTagCompound();
 
         compound.setBoolean("Locked", locked);
 
         if (hasDestination()) {
            wtCompound.setString("destWorldName", destinationWorldName);
             wtCompound.setInteger("destWorldId", destinationWorldId);
             wtCompound.setFloat("destX", destinationX);
             wtCompound.setFloat("destY", destinationY);
             wtCompound.setFloat("destZ", destinationZ);
             wtCompound.setFloat("destRotation", destinationRotation);
         }
 
         NBTTagCompound wtBlocksCompound = new NBTTagCompound();
         for (TileEntityWallTeleporter teleporter : teleporters) {
             wtBlocksCompound.setIntArray("block" + teleporters.indexOf(teleporter),
                     new int[]{ teleporter.xCoord, teleporter.yCoord, teleporter.zCoord });
         }
         wtCompound.setCompoundTag("WTBlocks", wtBlocksCompound);
 
         NBTTagList items = new NBTTagList();
         for (int i = 0; i < getSizeInventory(); i++) {
             ItemStack stack = getStackInSlot(i);
 
             if (stack != null) {
                 NBTTagCompound item = new NBTTagCompound();
                 item.setByte("Slot", (byte) i);
                 stack.writeToNBT(item);
                 items.appendTag(item);
             }
         }
         wtCompound.setTag("Items", items);
         compound.setCompoundTag("WTMultiblock", wtCompound);
     }
 
     public void readFromNBT(NBTTagCompound compound) {
         NBTTagCompound wtCompound = compound.getCompoundTag("WTMultiblock");
 
         locked = compound.getBoolean("Locked");
 
         if (wtCompound.hasKey("destWorldId")) {
            destinationWorldName = wtCompound.getString("destWorldName");
             destinationWorldId = wtCompound.getInteger("destWorldId");
             destinationX = wtCompound.getFloat("destX");
             destinationY = wtCompound.getFloat("destY");
             destinationZ = wtCompound.getFloat("destZ");
             destinationRotation = wtCompound.getFloat("destRotation");
         }
 
         NBTTagCompound wtBlocksCompound = wtCompound.getCompoundTag("WTBlocks");
         blocks = new ArrayList<int[]>();
         for (Object tag : wtBlocksCompound.getTags()) {
             if (tag instanceof NBTTagIntArray) {
                 blocks.add(((NBTTagIntArray) tag).intArray);
             }
         }
 
         NBTTagList items = wtCompound.getTagList("Items");
         for (int i = 0; i < items.tagCount(); i++) {
             NBTTagCompound item = (NBTTagCompound) items.tagAt(i);
             int slot = item.getByte("Slot");
 
             if (slot >= 0 && slot < getSizeInventory()) {
                 setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
             }
         }
     }
 
     public void init(World world) {
         if (blocks != null) {
             for (int[] blockCoords : blocks) {
                 TileEntity tileEntity = world.getBlockTileEntity(blockCoords[0], blockCoords[1], blockCoords[2]);
 
                 if (tileEntity instanceof TileEntityWallTeleporter) {
                     TileEntityWallTeleporter teleporter = (TileEntityWallTeleporter) tileEntity;
 
                     add(teleporter);
                     blocks = null;
                 }
             }
         }
     }
 
     public static void merge(MultiblockWallTeleporter multiblock1, MultiblockWallTeleporter multiblock2) {
         if (multiblock1 != multiblock2) {
             for (TileEntityWallTeleporter teleporter : multiblock2.teleporters) {
                 multiblock1.add(teleporter);
             }
         }
     }
 
     // Inventory implementation
 
     public ItemStack[] items = new ItemStack[2];
 
     @Override
     public int getSizeInventory() {
         return items.length;
     }
 
     @Override
     public ItemStack getStackInSlot(int i) {
         return items[i];
     }
 
     @Override
     public ItemStack decrStackSize(int i, int count) {
         ItemStack stack = getStackInSlot(i);
 
         if (stack != null) {
             if (stack.stackSize <= count) {
                 setInventorySlotContents(i, null);
             } else {
                 stack = stack.splitStack(count);
                 onInventoryChanged();
             }
         }
 
         return stack;
     }
 
     @Override
     public ItemStack getStackInSlotOnClosing(int i) {
         ItemStack stack = getStackInSlot(i);
         setInventorySlotContents(i, null);
         return stack;
     }
 
     @Override
     public void setInventorySlotContents(int i, ItemStack itemstack) {
         items[i] = itemstack;
         onInventoryChanged();
     }
 
     @Override
     public String getInvName() {
         return BlockInfo.WT_NAME;
     }
 
     @Override
     public boolean isInvNameLocalized() {
         return true;
     }
 
     @Override
     public int getInventoryStackLimit() {
         return 1;
     }
 
     @Override
     public boolean isUseableByPlayer(EntityPlayer player) {
         return true;
     }
 
     @Override
     public void openChest() {
     }
 
     @Override
     public void closeChest() {
     }
 
     @Override
     public boolean isItemValidForSlot(int i, ItemStack stack) {
         return (stack != null) && (stack.getItem() == WTItems.gpsChip) && (stack.getItemDamage() == 1);
     }
 
     @Override
     public void onInventoryChanged() {
         if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
             ItemStack stack = getStackInSlot(0);
             if (getStackInSlot(1) == null && stack != null && stack.getItem().itemID == WTItems.gpsChip.itemID &&
                 stack.getItemDamage() == 1 && stack.hasTagCompound()) {
                 NBTTagCompound stackCompound = stack.getTagCompound();
 
                 if (stackCompound.hasKey("LocationData")) {
                     NBTTagCompound locationCompound = stackCompound.getCompoundTag("LocationData");
 
                     String worldName = locationCompound.getString("worldName");
                     int worldId = locationCompound.getInteger("worldId");
                     float x = locationCompound.getFloat("locationX");
                     float y = locationCompound.getFloat("locationY");
                     float z = locationCompound.getFloat("locationZ");
                     float r = locationCompound.getFloat("locationRotation");
 
                     setDestination(worldName, worldId, x, y, z, r);
 
                     setInventorySlotContents(0, null);
                     setInventorySlotContents(1, stack);
 
                     if (container != null) {
                         container.sendSlotContentsToCrafters(36, null);
                         container.sendSlotContentsToCrafters(37, stack);
                     }
                 }
             }
         }
 
         for (TileEntityWallTeleporter teleporter : teleporters) {
             teleporter.onInventoryChanged();
         }
     }
 }
