 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of DirectorPlugin.
  * 
  * DirectorPlugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * DirectorPlugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with DirectorPlugin.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.director.threading;
 
 import java.sql.ResultSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftWorld;
 
 import de.minestar.director.Core;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class BlockSetThread implements Runnable {
 
    private final static int BPT = 1;
 
     private ResultSet blocks;
     private World world;
 
     private int id = Integer.MAX_VALUE;
 
     private int x, y, z = 0;
     private int newId, oldId = 0;
     private byte newData, oldData = 0;
     private String date = "";
     private char type = ' ';
 
     private Block block = null;
 
     private List<Block> setBlocks = new LinkedList<Block>();
 
     public BlockSetThread(World world, ResultSet blocks) {
         this.blocks = blocks;
         this.world = world;
     }
 
     @Override
     public void run() {
         primitiveSetting();
     }
 
     public void setID(int id) {
         if (this.id == Integer.MAX_VALUE)
             this.id = id;
         else
             ConsoleUtils.printError(Core.NAME, "id was already set for thread!");
     }
 
     private void primitiveSetting() {
         try {
             for (int i = 0; i < BPT; ++i) {
                 if (!blocks.next()) {
                     Bukkit.getScheduler().cancelTask(id);
                     return;
                 }
                 x = blocks.getInt(1);
                 y = blocks.getInt(2);
                 z = blocks.getInt(3);
                 newId = blocks.getInt(4);
                 newData = blocks.getByte(5);
                 oldId = blocks.getInt(6);
                 oldData = blocks.getByte(7);
                 date = blocks.getString(8);
                 type = blocks.getString(9).charAt(0);
                 block = world.getBlockAt(x, y, z);
                 block.setTypeIdAndData(newId, newData, false);
                 setBlocks.add(block);
                // BlockX, BlockY, BlockZ, NewBlockId, NewBlockData, OldBlockId,
                // OldBlockData, DateTime, EventType

             }
             net.minecraft.server.World nativeWorld = ((CraftWorld) world).getHandle();
             for (Block setBlock : setBlocks) {
                 x = setBlock.getX();
                 y = setBlock.getY();
                 z = setBlock.getZ();
                 nativeWorld.notify(x, y, z);
                 nativeWorld.applyPhysics(x, y, z, setBlock.getTypeId());
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't read ResultSet!");
         }
     }
 }
