 package com.purplefrog.minecraftExplorer;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: thoth
  * Date: 4/8/13
  * Time: 5:18 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FloatingIsland
 {
 
     public final int excavationLimit;
     public DepthMap bottom;
     protected final boolean[] lampMap;
 
     public FloatingIsland(int xSize, int zSize, int excavationLimit, int lampExclusionRadius)
     {
         this.excavationLimit = excavationLimit;
         bottom = computeDepthMap(xSize, zSize, this.excavationLimit);
 
         lampMap = computeLampSpots(bottom, lampExclusionRadius);
 
     }
 
     public static void main(String[] argv)
         throws IOException
     {
         FloatingIsland island = new FloatingIsland(100, 50, 9, 3);
 
 
         File saveDir = WorldPicker.pickSaveDir();
 
         BlockEditor editor = new BlockEditor(new MinecraftWorld(saveDir));
 
         int x0= 81;
         int z0= 81;
         int y0 = 100;
 
         if (false) {
             island.renderClicheWithLamps(editor, x0, y0, z0, y0 + island.excavationLimit+3);
         } else {
             island.render(editor, x0, y0, z0, y0 + island.excavationLimit + 3, new DoomChipCookie());
         }
 
         editor.relight();
 
         editor.save();
     }
 
     public void renderClicheWithLamps(BlockEditor editor, int x0, int y0, int z0, int y2)
         throws IOException
     {
 
        render(editor, x0, y0, z0, y2, new BlockChooser());
     }
 
     public void render(BlockEditor editor, int x0, int y0, int z0, int y2, int airType, int ceilingType, int lampType)
         throws IOException
     {
         BlockChooser chooser = new BlockChooser(airType,  ceilingType, lampType);
         render(editor, x0, y0, z0, y2, chooser);
     }
 
     public void render(BlockEditor editor, int x0, int y0, int z0, int y2, BlockChooser chooser)
         throws IOException
     {
         for (int x=0; x<bottom.w; x++) {
             for (int z=0; z<bottom.h; z++) {
                 int k = (int) bottom.get(x, z);
                 for (int y= 0; y+y0< y2; y++) {
 
                     int bt = chooser.choose(lampMap[z * bottom.w + x] && y == k, y >= k);
 
                     editor.setBlock(x+x0,y+y0,z+z0, bt);
                 }
             }
         }
     }
 
     public static DepthMap computeDepthMap(int xSize, int zSize, int excavationLimit)
     {
         DepthMap bottom = new DepthMap(xSize, zSize);
 
         Random rand = new Random();
 
         while (true) {
             RandomCoord rc = new RandomCoord(rand);
             for (int x=0; x<bottom.w; x++) {
                 for (int y=0; y<bottom.h; y++) {
                     if (bottom.get(x,y) == 0)
                         rc.addCoord(x,y);
                 }
             }
 
             if (rc.n <= 0) {
                 break;
             }
 
             excavateCone(bottom, rand, rc.x, rc.y, excavationLimit);
 
             if (false)
                 break;
         }
 
         if (false) {
             for (int y=0; y<bottom.h; y++) {
                 for (int x=0; x<bottom.w; x++) {
                     System.out.print(bottom.get(x,y)+" ");
                 }
                 System.out.println();
             }
 
             return null;
         }
         return bottom;
     }
 
     public static boolean[] computeLampSpots(DepthMap bottom, int exclusionRadius)
     {
         boolean [] lampMap = new boolean[bottom.w*bottom.h];
 
         for (int x=0; x<bottom.w; x++) {
             for (int z=0; z<bottom.h; z++) {
                 if (localPeak(bottom, x,z, exclusionRadius)) {
                     lampMap[z*bottom.w + x] = true;
                 }
             }
         }
         return lampMap;
     }
 
     public static boolean localPeak(DepthMap bottom, int x, int z, int radius)
     {
         float y=bottom.get(x,z);
         for (int a=Math.max(0, x- radius); a<bottom.w && a<=x+ radius; a++) {
             for (int b=Math.max(0, z- radius); b<bottom.h && b<=z+ radius; b++) {
                 if (a==0 && b==0)
                     continue;
                 if (bottom.get(a,b) < y)
                     return false;
             }
         }
 
         return true;
     }
 
     public static void excavateCone(DepthMap bottom, Random rand, int cx, int cy, int excavationLimit)
     {
         int wallaby = rand.nextInt(excavationLimit)+1;
         System.out.println("cone[" +wallaby+"] @" +cx+ "," + cy);
         int hippo = wallaby*4;
         DepthMap cone = new DepthMap(hippo*2+1, hippo*2+1);
         sloppyCone(cone, rand, hippo, hippo, wallaby);
 
         if (false) {
             for (int y=hippo-4; y<=hippo+4; y++) {
                 for (int x=hippo-4; x<=hippo+4; x++) {
                     System.out.print(cone.get(x,y)+" ");
                 }
                 System.out.println();
             }
         }
 
         for (int x=Math.max(0, hippo-cx); x<Math.min(cone.w, bottom.w +hippo-cx) ; x++) {
             for (int y=Math.max(0, hippo-cy); y<Math.min(cone.h, bottom.h +hippo-cy); y++) {
                 int x2 = x + cx - hippo;
                 int y2 = y + cy - hippo;
                 bottom.set(x2, y2,
                     Math.max(bottom.get(x2, y2), cone.get(x, y)));
             }
         }
     }
 
     private static void sloppyCone(DepthMap map, Random rand, int cx, int cy, int height)
     {
         map.set(cx, cy, height);
         for (int r = 1; ; r++) {
 
             boolean dirty = false;
 
             dirty = maybeUpdate(map, rand, cx, cy - r, map.get(cx, cy - r + 1), null) || dirty;
             dirty = maybeUpdate(map, rand, cx - r, cy, map.get(cx - r + 1, cy), null) || dirty;
             dirty = maybeUpdate(map, rand, cx + r, cy, map.get(cx + r - 1, cy), null) || dirty;
             dirty = maybeUpdate(map, rand, cx, cy + r, map.get(cx, cy + r - 1), null) || dirty;
             for (int q=1; q<r; q++) {
                 int x = cx-q;
                 int y = cy-r+q;
                 dirty = maybeUpdate(map, rand, x, y, map.get(x, y + 1), map.get(x + 1, y)) || dirty;
                 y = cy+r-q;
                 dirty = maybeUpdate(map, rand, x, y, map.get(x, y - 1), map.get(x + 1, y)) || dirty;
                 x = cx+q;
                 dirty = maybeUpdate(map, rand, x, y, map.get(x, y - 1), map.get(x - 1, y)) || dirty;
                 y = cy-r+q;
                 dirty = maybeUpdate(map, rand, x, y, map.get(x, y + 1), map.get(x - 1, y)) || dirty;
             }
             if (!dirty)
                 break;
         }
     }
 
     public static boolean maybeUpdate(DepthMap bottom, Random rand, int x, int y, Float d1, Float d2)
     {
         double newD;
         if (d1==null) {
             if (d2==null) {
                 return false;
             } else {
                 newD = d2 - erosion(rand);
             }
         } else {
             if (d2==null) {
                 newD = d1-erosion(rand);
             } else {
                 newD = Math.max(d2-erosion(rand), d1-erosion(rand));
             }
         }
         if (newD > bottom.get(x,y)) {
             bottom.set(x,y,newD);
             return true;
         } else {
             return false;
         }
     }
 
     private static double erosion(Random rand)
     {
         double q = rand.nextDouble();
         return q*q *2 +0.5;
 
 
     }
 
     private static int[][] array2d(int dx, int dy)
     {
         int[][] rval = new int[dx][];
         for (int i = 0; i < rval.length; i++) {
             rval[i] = new int[dy];
         }
         return rval;
     }
 
     public static class DepthMap
     {
         final public int w, h;
 
         protected float[] data;
 
         public DepthMap(int w, int h)
         {
             this.w = w;
             this.h = h;
 
             data = new float[w*h];
         }
 
         public float get(int x, int y)
         {
             if (x<0 || x>=w || y<0 || y>=h)
                 throw new IllegalArgumentException();
             return data[y*w +x];
         }
 
         public Float get_(int x, int y)
         {
             if (x<0 || x>=w || y<0 || y >=h)
                 return null;
             return get(x,y);
         }
 
         public void set(int x, int y, double val)
         {
             data[y*w+x] = (float) val;
         }
     }
 
     public static class BlockChooser
     {
 
         public final int airType;
         public final int ceilingType;
         public final int lampType;
 
        public BlockChooser()
        {
            this(0,1,89);
        }

         public BlockChooser(int airType, int ceilingType, int lampType)
         {
 
             this.airType = airType;
             this.ceilingType = ceilingType;
             this.lampType = lampType;
         }
 
         public int choose(boolean lampSpot, boolean above)
         {
             return lampSpot ? lampType : above ? ceilingType : airType;
         }
     }
 
     public static class DoomChipCookie
         extends BlockChooser
     {
         Random rand = new Random();
 
         public DoomChipCookie(int airType, int ceilingType, int lampType)
         {
             super(airType, ceilingType, lampType);
         }
 
         public DoomChipCookie()
         {
            super(0, 1, 89);
         }
 
         @Override
         public int choose(boolean lampSpot, boolean above)
         {
             if (lampSpot)
                 return lampType;
             if (above) {
                 if (rand.nextInt(20)==0)
                     return 46;
                 else
                     return ceilingType;
             }
             return airType;
         }
     }
 }
