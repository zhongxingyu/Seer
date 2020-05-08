 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.utils;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import monnef.core.api.IIntegerCoordinates;
 import net.minecraft.block.Block;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 
 import static monnef.core.utils.MathHelper.square;
 import static net.minecraft.util.MathHelper.sqrt_float;
 
 public class IntegerCoordinates implements IIntegerCoordinates {
     // do not ever change after construction!
     protected int x;
     protected int y;
     protected int z;
 
     protected String compoundTagName;
     private int dimension;
     private boolean locked = false;
 
     public IntegerCoordinates(TileEntity tile) {
         setWorld(tile.worldObj);
         x = tile.xCoord;
         y = tile.yCoord;
         z = tile.zCoord;
 
         postInit();
     }
 
     public IntegerCoordinates(int x, int y, int z, World world) {
         setWorld(world);
         this.x = x;
         this.y = y;
         this.z = z;
 
         postInit();
     }
 
     public IntegerCoordinates(NBTTagCompound tag, String compoundTagName) {
         this.compoundTagName = compoundTagName;
         loadFrom(tag, compoundTagName);
 
         postInit();
     }
 
     private void postInit() {
         locked = true;
     }
 
     @Override
     public TileEntity getTile() {
         return getWorld().getBlockTileEntity(x, y, z);
     }
 
     @Override
     public World getWorld() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimension);
     }
 
     private void setWorld(World world) {
         if (world == null) {
             throw new NullPointerException("world");
         }
 
         this.dimension = world.provider.dimensionId;
     }
 
     @Override
     public int getX() {
         return x;
     }
 
     @Override
     public int getY() {
         return y;
     }
 
     @Override
     public int getZ() {
         return z;
     }
 
     @Override
     public void saveTo(NBTTagCompound tag, String tagName) {
         NBTTagCompound save = new NBTTagCompound();
         save.setInteger("x", getX());
         save.setInteger("y", getY());
         save.setInteger("z", getZ());
         save.setInteger("dim", getWorld().provider.dimensionId);
         tag.setCompoundTag(tagName, save);
     }
 
     @Override
     public void loadFrom(NBTTagCompound tag, String tagName) {
         if (locked) {
             throw new RuntimeException("locked");
         }
 
         NBTTagCompound save = tag.getCompoundTag(tagName);
         x = save.getInteger("x");
         y = save.getInteger("y");
         z = save.getInteger("z");
         dimension = save.getInteger("dim");
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         IntegerCoordinates that = (IntegerCoordinates) o;
 
         if (x != that.x) return false;
         if (y != that.y) return false;
         if (z != that.z) return false;
         if (dimension != that.dimension) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = x;
         result = 31 * result + y;
         result = 31 * result + z;
         result = 31 * result + dimension;
         return result;
     }
 
     public float computeDistance(IIntegerCoordinates other) {
         return sqrt_float(computeDistanceSquare(other));
     }
 
     public float computeDistanceSquare(IIntegerCoordinates other) {
         return square(getX() - other.getX()) + square(getY() - other.getY()) + square(getZ() - other.getZ());
     }
 
     @Override
     public IIntegerCoordinates shiftInDirectionBy(ForgeDirection dir, int amount) {
         return new IntegerCoordinates(getX() + dir.offsetX * amount, getY() + dir.offsetY * amount, getZ() + dir.offsetZ * amount, getWorld());
     }
 
     @Override
     public TileEntity getBlockTileEntity() {
         return getWorld().getBlockTileEntity(getX(), getY(), getZ());
     }
 
     @Override
     public int getBlockId() {
         return getWorld().getBlockId(getX(), getY(), getZ());
     }
 
     @Override
     public int getBlockMetadata() {
         return getWorld().getBlockMetadata(getX(), getY(), getZ());
     }
 
     @Override
     public int getRedstoneWirePowerLevel() {
         return getBlockId() == Block.redstoneWire.blockID ? getBlockMetadata() : 0;
     }
 
     @Override
     public int getIndirectPowerFromSide(int side) {
         return getWorld().getIndirectPowerLevelTo(getX(), getY(), getZ(), side);
     }
 
 }
