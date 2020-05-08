 /* This file is part of Schematica.
  * Copyright (C) 2013 metalhedd <https://github.com/andrepl/>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Schematica.  If not, see <http://www.gnu.org/licenses/>.
  *
  * This code is based heavily on WorldEdit by sk89q <http://www.sk89q.com>
  */
 
 package com.norcode.bukkit.schematica;
 
 import net.minecraft.server.v1_5_R3.NBTBase;
 import net.minecraft.server.v1_5_R3.NBTCompressedStreamTools;
 import net.minecraft.server.v1_5_R3.NBTTagCompound;
 import net.minecraft.server.v1_5_R3.NBTTagInt;
 import net.minecraft.server.v1_5_R3.NBTTagList;
 import net.minecraft.server.v1_5_R3.TileEntity;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
 import org.bukkit.util.BlockVector;
 import org.bukkit.util.Vector;
 
 import com.norcode.bukkit.schematica.exceptions.SchematicLoadException;
 import com.norcode.bukkit.schematica.exceptions.SchematicSaveException;
 
 import org.bukkit.Material;
 import org.bukkit.block.BlockState;
 
 import java.util.*;
 
 /**
  * Temporary storage for cuboid regions.
  *
  */
 public class Clipboard {
     private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;
 
     ClipboardBlock[][][] data;
     BlockVector origin;
     BlockVector offset;
     BlockVector size;
 
     private static HashSet<Integer> onlyAir = new HashSet<Integer>();
     static {
         onlyAir.add(0);
     }
     public Clipboard(BlockVector size) {
         this.size = new BlockVector(size);
         this.offset = new BlockVector();
         this.origin = new BlockVector();
         this.data = new ClipboardBlock[this.size.getBlockX()][this.size.getBlockY()][this.size.getBlockZ()];
     }
 
     /**
      * Gets the origin point of the clipboard
      *
      * The origin is the point in the world where the clipboards offset point originally came from in the world.
      *
      * @return a BlockVector representing the origin point of the clipboard.
      */
     public BlockVector getOrigin() {
         return origin;
     }
 
     /**
      * Sets the origin point of the clipboard
      *
      * @param origin the origin BlockVector
      */
     public void setOrigin(BlockVector origin) {
         this.origin = origin;
     }
 
 
     /**
      * Gets the offset point of the clipboard
      *
      * @return the point from which the clipboard is copied/pasted and around which it is rotated
      */
     public BlockVector getOffset() {
         return offset;
     }
 
     /**
      * Sets the offset point of the clipboard.
      *
      * @param offset the new offset point for the clipboard
      */
     public void setOffset(BlockVector offset) {
         this.offset = offset;
     }
 
     /**
      * Gets the size of the clipboard
      *
      * @return a BlockVector representing the size of the current clipboard
      */
     public BlockVector getSize() {
         return size;
     }
 
     /**
      * Sets the clipboards size.
      *
      * Do NOT use this to resize an existing clipboard.  It is only intended for use during initialization of a new clipboard.
      *
      * @param size a BlockVector representing the size of the clipboard
      */
     public void setSize(BlockVector size) {
         this.size = size;
     }
 
 
     /**
      * Load an MCEdit Format Schematic
      *
      * @param input a byte[] of raw schematic contents.
      *
      * @return a new Clipboard containing the schematic.
      *
      * @throws SchematicLoadException (actually doesn't yet)
      */
     public static Clipboard fromSchematic(byte[] input) throws SchematicLoadException {
         NBTTagCompound tag = NBTCompressedStreamTools.a(input);
         
         short width = tag.getShort("Width");
         short length = tag.getShort("Length");
         short height = tag.getShort("Height");
         BlockVector size = new BlockVector(width, height, length);
         int originX = tag.getInt("WEOriginX");
         int originY = tag.getInt("WEOriginY");
         int originZ = tag.getInt("WEOriginZ");
         BlockVector origin = new BlockVector(originX, originY, originZ);
 
         int offsetX = tag.getInt("WEOffsetX");
         int offsetY = tag.getInt("WEOffsetY");
         int offsetZ = tag.getInt("WEOffsetZ");
         BlockVector offset = new BlockVector(offsetX, offsetY, offsetZ);
         
         byte[] blockIds = tag.getByteArray("Blocks");
         byte[] data = tag.getByteArray("Data");
         byte[] addId = new byte[0];
         short[] blocks = new short[blockIds.length];
 
         if (tag.hasKey("AddBlocks")) {
             addId = tag.getByteArray("AddBlocks");
         }
         // Combine the AddBlocks data with the first 8-bit block ID
         for (int index = 0; index < blockIds.length; index++) {
             if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                 blocks[index] = (short) (blockIds[index] & 0xFF);
             } else {
                 if ((index & 1) == 0) {
                     blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockIds[index] & 0xFF));
                 } else {
                     blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockIds[index] & 0xFF));
                 }
             }
         }
         displayArray(blocks);
         // Need to pull out tile entities
         NBTTagList tileEntities = tag.getList("TileEntities");
 
         Map<BlockVector, NBTTagCompound> tileEntitiesMap =
                 new HashMap<BlockVector, NBTTagCompound>();
 
         NBTTagCompound cTag;
         for (int index=0;index<tileEntities.size();index++) {
             try {
                 cTag = (NBTTagCompound) tileEntities.get(index);
             } catch (ClassCastException ex) {
                 continue;
             }
             int x = 0;
             int y = 0;
             int z = 0;
             for (NBTBase v: (Collection<NBTBase>) cTag.c()) {
                 if (v instanceof NBTTagInt) {
                     NBTTagInt intTag = (NBTTagInt) v;
                     if (v.getName().equals("x")) {
                         x = intTag.data;
                     } else if (v.getName().equals("y")) {
                         y = intTag.data;
                     } else if(v.getName().equals("z")) {
                         z = intTag.data;
                     }
                 }
             }
             tileEntitiesMap.put(new BlockVector(x,y,z), cTag);
         }
 
         Clipboard cb = new Clipboard(size);
         cb.origin = origin;
         cb.offset = offset;
         int idx;
         BlockVector vec;
         ClipboardBlock block;
         for (int x=0;x<width;++x) {
             for (int y=0;y<height;++y) {
                 for (int z=0;z<length;++z) {
                     idx = y*width*length+z*width+x;
                     vec = new BlockVector(x,y,z);
                     block = new ClipboardBlock(blocks[idx], data[idx]);
                     if (tileEntitiesMap.containsKey(vec)) {
                         block.setTag(tileEntitiesMap.get(vec));
                     }
                     cb.data[x][y][z] = block;
                 }
             }
         }
         return cb;
     }
 
     private static void displayArray(short[] blocks) {
         String s = "";
         for (short b: blocks) {
             s += Short.toString(b) + ",";
         }
     }
 
     /**
      * Saves the current clipboard contents into a new schematic.
      *
      * @return a byte[] in MCEdit schematic format.
      *
      * @throws SchematicSaveException if the Width, Height or Length of the clipboard exceed Short.MAX_VALUE
      */
     public byte[] toSchematic() throws SchematicSaveException {
         int width = getWidth();
         int height = getHeight();
         int length = getLength();
         if (width > MAX_SIZE) {
             throw new SchematicSaveException("Width " + width + " exceeds max size of "  + MAX_SIZE);
         }
         if (height > MAX_SIZE) {
             throw new SchematicSaveException("Height " + height + " exceeds max size of "  + MAX_SIZE);
         }
         if (length > MAX_SIZE) {
             throw new SchematicSaveException("Length " + length + " exceeds max size of "  + MAX_SIZE);
         }
 
         NBTTagCompound tag = new NBTTagCompound("Schematic");
         tag.setShort("Width",  (short) width);
         tag.setShort("Length", (short) length);
         tag.setShort("Height", (short) height);
         tag.setString("Materials",  "Alpha");
         tag.setInt("WEOriginX", getOrigin().getBlockX());
         tag.setInt("WEOriginY", getOrigin().getBlockY());
         tag.setInt("WEOriginZ", getOrigin().getBlockZ());
         tag.setInt("WEOffsetX", getOffset().getBlockX());
         tag.setInt("WEOffsetY", getOffset().getBlockY());
         tag.setInt("WEOffsetZ", getOffset().getBlockZ());
 
         // Copy
         byte[] blocks = new byte[width * height * length];
         byte[] addBlocks = null;
         byte[] blockData = new byte[width * height * length];
         NBTTagList tileEntities = new NBTTagList();
         for (int x = 0; x < width; ++x) {
             for (int y = 0; y < height; ++y) {
                 for (int z = 0; z < length; ++z) {
                     int index = y * width * length + z * width + x;
                     ClipboardBlock block = getBlock(x, y, z);
 
                     // Save 4096 IDs in an AddBlocks section
                     if (block.getType() > 255) {
                         if (addBlocks == null) { // Lazily create section
                             addBlocks = new byte[(blocks.length >> 1) + 1];
                         }
 
                         addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                                 addBlocks[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
                                 : addBlocks[index >> 1] & 0xF | ((block.getType() >> 8) & 0xF) << 4);
                     }
 
                     blocks[index] = (byte) block.getType();
                     blockData[index] = (byte) block.getData();
 
                     if (block.hasTag()) {
                         NBTTagCompound rawTag = (NBTTagCompound) block.getTag().clone();
                         rawTag.setInt("x", x);
                         rawTag.setInt("y", y);
                         rawTag.setInt("z", z);
                         tileEntities.add(rawTag);
                     }
                 }
             }
         }
 
 
         tag.setByteArray("Blocks", blocks);
         tag.setByteArray("Data", blockData);
         tag.set("Entities", new NBTTagList("Entities"));
         tag.set("TileEntities", tileEntities);
         if (addBlocks != null) {
             tag.setByteArray("AddBlocks", addBlocks);
         }
         // Build and output
         tag.setName("Schematic");
         return NBTCompressedStreamTools.a(tag);
     }
 
     /**
      * rotates a BlockVector in 90 degree increments
      *
      * @param v a BlockVector to be rotated
      * @param angle degrees to rotate
      * @return a new rotated BlockVector
      */
     public static BlockVector transformBlockVector(BlockVector v, double angle, int aboutX, int aboutZ, int translateX, int translateZ) {
         angle = Math.toRadians(angle);
         double x = v.getBlockX() - aboutX;
         double z = v.getBlockZ() - aboutZ;
         double x2 = x * Math.cos(angle) - z * Math.sin(angle);
         double z2 = x * Math.sin(angle) + z * Math.cos(angle);
 
         return new BlockVector(
                 (int)Math.round(x2 + aboutX + translateX),
                 v.getBlockY(),
                 (int)Math.round(z2 + aboutZ + translateZ)
         );
     }
 
     /**
      * Rotates the entire clipboard in 90 degree increments.
      *
      * @param angle rotation
      */
     public void rotate2D(int angle) {
         angle = angle % 360;
         if (angle % 90 != 0) {
             return;
         }
         boolean reverse = angle < 0;
         int numRotations = Math.abs((int) Math.floor(angle / 90.0));
 
         int width = getWidth();
         int length = getLength();
         int height = getHeight();
         BlockVector sizeRotated = transformBlockVector(size, angle, 0,0,0,0);
         int shiftX = sizeRotated.getBlockX() < 0 ? -sizeRotated.getBlockX() - 1 : 0;
         int shiftZ = sizeRotated.getBlockZ() < 0 ? -sizeRotated.getBlockZ() - 1 : 0;
 
         ClipboardBlock newData[][][] = new ClipboardBlock
                 [Math.abs(sizeRotated.getBlockX())]
                 [Math.abs(sizeRotated.getBlockY())]
                 [Math.abs(sizeRotated.getBlockZ())];
 
         for (int x = 0; x < width; ++x) {
             for (int z = 0; z < length; ++z) {
                 BlockVector v = transformBlockVector(new BlockVector(x, 0, z), angle, 0, 0, 0, 0);
                 int newX = v.getBlockX();
                 int newZ = v.getBlockZ();
                 for (int y = 0; y < height; ++y) {
                     ClipboardBlock block = data[x][y][z];
                     newData[shiftX + newX][y][shiftZ + newZ] = block;
                     if (reverse) {
                         for (int i = 0; i < numRotations; ++i) {
                             block.rotate90Reverse();
                         }
                     } else {
                         for (int i = 0; i < numRotations; ++i) {
                             block.rotate90();
                         }
                     }
                 }
             }
         }
 
         data = newData;
         size = new BlockVector(Math.abs(sizeRotated.getBlockX()),
                           Math.abs(sizeRotated.getBlockY()),
                           Math.abs(sizeRotated.getBlockZ()));
         Vector tmpOff = transformBlockVector(offset, angle,0,0,0,0).subtract(new org.bukkit.util.BlockVector(shiftX, 0, shiftZ));
 
         offset = new BlockVector(
                 (int) Math.round(tmpOff.getX()),
                 (int) Math.round(tmpOff.getY()),
                 (int) Math.round(tmpOff.getZ()));
     }
 
     /**
      * Gets the {@link ClipboardBlock} at the specified clipboard position
      *
      * @param x clipboard x position
      * @param y clipboard y position
      * @param z clipboard z position
      *
      * @return
      */
     public ClipboardBlock getBlock(int x, int y, int z) {
         return data[x][y][z];
     }
 
     /**
      * Gets the {@link ClipboardBlock} at the specified clipboard position
      *
      * @param position BlockVector - clipboard position
      * @return
      */
     public ClipboardBlock getBlock(BlockVector position) {
         return getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ());
     }
 
     /**
      * Sets a the block at the specified clipboard position.
      *
      * @param x clipboard x position
      * @param y clipboard y position
      * @param z clipboard z position
      * @param block {@link ClipboardBlock}
      */
     public void setBlock(int x, int y, int z, ClipboardBlock block) {
         data[x][y][z] = block;
     }
 
     /**
      * Sets a the block at the specified clipboard position.
      *
      * @param position Clipboard position.
      * @param block {@link ClipboardBlock}
      */
     public void setBlock(BlockVector position, ClipboardBlock block) {
         setBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ(), block);
     }
 
     /**
      * Gets the height (Y value) of the clipboard
      *
      * @return clipboard height
      */
     private int getHeight() {
         return size.getBlockY();
     }
 
     /**
      * Gets the length (Z value) of the clipboard
      *
      * @return clipboard length
      */
     private int getLength() {
         return size.getBlockZ();
     }
 
     /**
      * Gets the width (X value) of the clipboard
      *
      * @return clipboard width
      */
     private int getWidth() {
         return size.getBlockX();
     }
 
     /**
      * Copies the specified block to the clipboard at x, y, z.
      *
      * @param block a bukkit Block object
      * @param x clipboard x position
      * @param y clipboard y position
      * @param z clipboard z position
      */
     public void copyBlockFromWorld(Block block, int x, int y, int z) {
         ClipboardBlock cbb = new ClipboardBlock(block.getTypeId(), block.getData());
         if (MaterialID.isTileEntityBlock(block.getTypeId())) {
             CraftWorld cw = (CraftWorld) block.getWorld();
             TileEntity te = cw.getTileEntityAt(block.getX(), block.getY(), block.getZ());
            if (te != null) {
                NBTTagCompound tag = new NBTTagCompound(MaterialID.getTileEntityId(block.getTypeId()));
                te.b(tag);
                tag.setInt("x", x);
                tag.setInt("y", y);
                tag.setInt("z", z);
                cbb.setTag(tag);
            }
         }
         data[x][y][z] = cbb;
     }
 
     /**
      * Copies the specified block to the clipboard at position.
      *
      * @param block a bukkit Block object
      * @param position clipboard position
      */
     public void copyBlockFromWorld(Block block, BlockVector position) {
         copyBlockFromWorld(block, position.getBlockX(), position.getBlockY(), position.getBlockZ());
     }
 
     /**
      * Copies the specified block to the clipboard.
      * the position in the clipboard is determinted by the clipboard's current origin.
      */
     public void copyBlockFromWorld(Block block) {
         int x = block.getLocation().getBlockX() - this.getOrigin().getBlockX();
         int y = block.getLocation().getBlockY() - this.getOrigin().getBlockY();
         int z = block.getLocation().getBlockZ() - this.getOrigin().getBlockZ();
         copyBlockFromWorld(block, x, y, z);
 
     }
 
     /**
      * Gets the Location in the specified world that corresponds to the specified
      * clipboard point using the clipboards current origin point.
      *
      * @param clipboardPoint the position in the clipboard
      * @param world the world
      * @return a Location in the world that corresponds to the given clipboard position
      */
     public Location getWorldLocationFor(BlockVector clipboardPoint, World world) {
         return new Location(world,
                 getOrigin().getBlockX() + clipboardPoint.getBlockX(),
                 getOrigin().getBlockY() + clipboardPoint.getBlockY(),
                 getOrigin().getBlockZ() + clipboardPoint.getBlockZ());
     }
 
     /**
      *
      * @param shuffle whether or not to shuffle the list so as to look more natural during slow builds.
      * @param ignoreTypes a set of block ID's to be left out of the list. if null, only air is ignored.
      * @return a list of clipboard points in an order suitable for being safely added to the world.
      */
     public List<BlockVector> getPasteQueue(boolean shuffle, Set<Integer> ignoreTypes) {
         if (ignoreTypes == null) {
             ignoreTypes = onlyAir;
         }
         List<BlockVector> queue = new ArrayList<BlockVector>(this.getWidth()*this.getHeight()*this.getLength());
         List<BlockVector> layerQueue = new ArrayList<BlockVector>(this.getWidth()*this.getLength());
         List<BlockVector> yBuffer = new ArrayList<BlockVector>();
         List<BlockVector> finishBuffer = new ArrayList<BlockVector>();
         ClipboardBlock cb;
         for (int y=0;y<getHeight();y++) {
             for (int z=0;z<getSize().getBlockZ();z++) {
                 for (int x=0;x<getSize().getBlockX();x++) {
                     cb = data[x][y][z];
                     if (ignoreTypes.contains(cb.getType())) continue;
                     switch (cb.getType()) {
                         case MaterialID.TORCH:
                         case MaterialID.BED_BLOCK:
                         case MaterialID.COCOA:
                         case MaterialID.WOOD_BUTTON:
                         case MaterialID.STONE_BUTTON:
                         case MaterialID.SKULL:
                         case MaterialID.REDSTONE_TORCH_OFF:
                         case MaterialID.REDSTONE_TORCH_ON:
                         case MaterialID.TRAP_DOOR:
                         case MaterialID.WALL_SIGN:
                         case MaterialID.TRIPWIRE_HOOK:
                             yBuffer.add(new BlockVector(x,y,z));
                             break;
                         case MaterialID.PISTON_EXTENSION:
                         case MaterialID.VINE:
                         case MaterialID.IRON_DOOR_BLOCK:
                         case MaterialID.WOODEN_DOOR:
                         case MaterialID.LEVER:
                             finishBuffer.add(new BlockVector(x,y,z));
                             break;
                         default:
                             layerQueue.add(new BlockVector(x,y,z));
                     }
                 }
             }
             if (shuffle) {
                 Collections.shuffle(layerQueue);
                 Collections.shuffle(yBuffer);
             }
             queue.addAll(layerQueue);
             if (!yBuffer.isEmpty()) {
                 queue.addAll(yBuffer);
                 yBuffer.clear();
             }
             layerQueue.clear();
         }
         if (shuffle) {
             Collections.shuffle(finishBuffer);
         }
         queue.addAll(finishBuffer);
         return queue;
     }
 
 
     /**
      * Copies the block at the specified clipboard position into the
      * world location determined by the clipboard's current origin.
      *
      */
      public void copyBlockToWorld(BlockVector position, World world) {
          copyBlockToWorld(getBlock(position), getWorldLocationFor(position, world));
      }
 
 
      /**
      * Copies the specified {@link ClipboardBlock} to the specified world location.
      *
      * @param block a {@link ClipboardBlock} to be placed in the world.
      * @param loc a org.bukkit.Location object at which to place the block.
      */
     public void copyBlockToWorld(ClipboardBlock block, Location loc) {
         BlockState state = loc.getBlock().getState();
         Material old = state.getType();
 
         state.setTypeId(block.getType());
         state.setRawData(block.getData());
         state.update(true, false);
 
         if (block.hasTag()) {
             TileEntity te;
             NBTTagCompound btag = (NBTTagCompound) block.getTag().clone();
             btag.setName(MaterialID.getTileEntityId(block.getType()));
             btag.setInt("x", loc.getBlockX());
             btag.setInt("y", loc.getBlockY());
             btag.setInt("z", loc.getBlockZ());
             te = ((CraftWorld) loc.getWorld()).getTileEntityAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
             te.a(btag);
             te.update();
         }
     }
 
     /**
      *
      *
      * @param shuffle whether or not to shuffle the list so as to look more natural during slow builds.
      * @param ignoreTypes a set of block ID's to be left out of the list. if null, only air is ignored.
      * @return a list of clipboard points in an order suitable for being safely removed from the world.
      */
     public List<BlockVector> getCutQueue(boolean shuffle, HashSet<Integer> ignoreTypes) {
         List<BlockVector> queue = getPasteQueue(shuffle, ignoreTypes);
         Collections.reverse(queue);
         return queue;
     }
 
     @Override
     public String toString() {
         BlockVector s = getSize();
         return "{Clipboard[" + s.getBlockX() + "x" + s.getBlockY() + "x" + s.getBlockZ() + "]}";
     }
 
     @Override
     public Clipboard clone() {
         Clipboard cb = new Clipboard(this.size.clone());
         cb.setOrigin(this.getOrigin().clone());
         cb.setOffset(this.getOffset().clone());
         for (int x=0;x<cb.getWidth();x++) {
             for (int y=0; y<cb.getHeight();y++) {
                 for (int z=0;z<cb.getLength();z++) {
                     cb.setBlock(x,y,z, getBlock(x,y,z).clone());
                 }
             }
         }
         return cb;
     }
 }
