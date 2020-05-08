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
    public void testVectorRotate() {
        for (int x=0;x<3;x++) {
            for (int y=0;y<4;y++) {
                for (int z=0;z<5;z++) {
                    com.sk89q.worldedit.BlockVector weVec = new com.sk89q.worldedit.BlockVector(x,y,z);
                    BlockVector bVec = new BlockVector(x,y,z);
                    com.sk89q.worldedit.BlockVector weVec2 = weVec.transform2D(180,0,0,0,0).toBlockVector();
                    BlockVector bVec2 = Clipboard.transformBlockVector(bVec, 180, 0, 0, 0, 0);

                    System.out.println(weVec2 + "==" + bVec2);
                    System.out.println(weVec2.getBlockX() + "," + weVec.getBlockY() + "," + weVec.getBlockZ() + "==" + bVec2.getBlockX() + "," + bVec.getBlockY() + "," + bVec.getBlockZ());
                    Assert.assertEquals(weVec2.getBlockX(), bVec2.getBlockX());
                    Assert.assertEquals(weVec2.getBlockY(), bVec2.getBlockY());
                    Assert.assertEquals(weVec2.getBlockZ(), bVec2.getBlockZ());
                }
            }
        }

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
        System.out.println(originalSize + "=>" + clipboard.getSize());
         clipboard.rotate2D(90);
         Assert.assertEquals(clipboard.getSize().getBlockX(), originalSize.getBlockX());
         Assert.assertEquals(clipboard.getSize().getBlockY(), originalSize.getBlockY());
         Assert.assertEquals(clipboard.getSize().getBlockZ(), originalSize.getBlockZ());
        System.out.println(originalSize + "=>" + clipboard.getSize());
         clipboard.rotate2D(180);
         Assert.assertEquals(clipboard.getSize().getBlockX(), originalSize.getBlockX());
         Assert.assertEquals(clipboard.getSize().getBlockY(), originalSize.getBlockY());
         Assert.assertEquals(clipboard.getSize().getBlockZ(), originalSize.getBlockZ());
        System.out.println(originalSize + "=>" + clipboard.getSize());

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
