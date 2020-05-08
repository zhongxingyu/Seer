 package com.norcode.bukkit.schematica;
 
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import org.bukkit.util.BlockVector;
 import org.junit.Ignore;
 
 import java.util.Random;
 
 public class ClipboardTest extends TestCase {
     private static Random rand = new Random();
     public ClipboardTest(String name) {
         super(name);
 
     }
 
     public void testRotateSize() throws Exception {
         Clipboard clipboard = new Clipboard(new BlockVector(3,4,5));
         BlockVector originalSize = clipboard.getSize();
         for (int x=0;x<clipboard.getSize().getBlockX();x++) {
             for (int y=0;y<clipboard.getSize().getBlockY();y++) {
                 for (int z=0;z<clipboard.getSize().getBlockZ();z++) {
                     clipboard.setBlock(new BlockVector(x,y,z), new ClipboardBlock(rand.nextInt(10), (byte) 0));
                 }
             }
         }
         clipboard.rotate2D(90);
         Assert.assertEquals(clipboard.getSize().getBlockX(), originalSize.getBlockZ());
         Assert.assertEquals(clipboard.getSize().getBlockY(), originalSize.getBlockY());
         Assert.assertEquals(clipboard.getSize().getBlockZ(), originalSize.getBlockX());
         clipboard.rotate2D(90);
         Assert.assertEquals(clipboard.getSize().getBlockX(), originalSize.getBlockX());
         Assert.assertEquals(clipboard.getSize().getBlockY(), originalSize.getBlockY());
         Assert.assertEquals(clipboard.getSize().getBlockZ(), originalSize.getBlockZ());
         clipboard.rotate2D(180);
         Assert.assertEquals(clipboard.getSize().getBlockX(), originalSize.getBlockX());
         Assert.assertEquals(clipboard.getSize().getBlockY(), originalSize.getBlockY());
         Assert.assertEquals(clipboard.getSize().getBlockZ(), originalSize.getBlockZ());
     }
 
     @Ignore
     public ClipboardBlock[][][] cloneData(Clipboard clipboard) {
         BlockVector s = clipboard.getSize();
         ClipboardBlock[][][] data = new ClipboardBlock[s.getBlockX()][s.getBlockY()][s.getBlockZ()];
         for (int x=0;x<s.getBlockX();x++) {
             for (int y=0;y<s.getBlockY();y++) {
                 for (int z=0;z<s.getBlockZ();z++) {
                     data[x][y][z] = new ClipboardBlock(clipboard.getBlock(x,y,z));
                 }
             }
         }
         return data;
     }
 
     public void testRotateBlocks() throws Exception {
         Clipboard clipboard = new Clipboard(new BlockVector(3,4,5));
         BlockVector originalSize = clipboard.getSize();
         for (int x=0;x<clipboard.getSize().getBlockX();x++) {
             for (int y=0;y<clipboard.getSize().getBlockY();y++) {
                 for (int z=0;z<clipboard.getSize().getBlockZ();z++) {
                     clipboard.setBlock(new BlockVector(x,y,z), new ClipboardBlock(rand.nextInt(10), (byte) 0));
                 }
             }
         }
         ClipboardBlock[][][] originalData = cloneData(clipboard);
         clipboard.rotate2D(180);
         for (int x=0;x<clipboard.getSize().getBlockX();x++) {
             for (int y=0;y<clipboard.getSize().getBlockY();y++) {
                 for (int z=0;z<clipboard.getSize().getBlockZ();z++) {
                     Assert.assertNotNull(clipboard.getBlock(x,y,z));
                 }
             }
         }
 
     }
 }
