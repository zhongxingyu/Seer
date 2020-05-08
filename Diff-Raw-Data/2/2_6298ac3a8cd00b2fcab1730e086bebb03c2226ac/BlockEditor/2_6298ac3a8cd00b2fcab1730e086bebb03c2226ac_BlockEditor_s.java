 package com.purplefrog.minecraftExplorer;
 
 import com.mojang.nbt.*;
 import com.purplefrog.jwavefrontobj.*;
 import net.minecraft.world.chunk.storage.*;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: thoth
  * Date: 4/5/13
  * Time: 12:27 PM
  * To change this template use File | Settings | File Templates.
  */
 public class BlockEditor
 {
     protected final ChunkTagCache chunkTagCache;
     private MinecraftWorld world;
     private Map<Point3D, Anvil.Section> sectionCache = new HashMap<Point3D, Anvil.Section>();
 
     public BlockEditor(MinecraftWorld world)
     {
         this.world = world;
         chunkTagCache = new ChunkTagCache(this.world);
     }
 
     public void setBlock(int x, int y, int z, int bt)
         throws IOException
     {
         Anvil.Section s = getSection(x, y, z);
 
         if (s==null) {
             throw new NullPointerException("There is no chunk/section at "+x+","+y+","+z+", and I don't know how to invoke the terrain generator.  You're going to have to log on to minecraft and visit that location before I can edit it.");
         }
         s.getBlocks().set(x,y,z, bt);
     }
 
     public void setBlock(int x, int y, int z, BlockPlusData block)
         throws IOException
     {
         Anvil.Section s = getSection(x, y, z);
 
         if (s==null) {
             throw new NullPointerException("There is no chunk/section at "+x+","+y+","+z+", and I don't know how to invoke the terrain generator.  You're going to have to log on to minecraft and visit that location before I can edit it.");
         }
         s.getBlocks().set(x,y,z, block.blockType);
         s.getData().set(x,y,z, block.data);
     }
 
     protected Anvil.Section getSection(int x, int y, int z)
         throws IOException
     {
         return getSectionChunk(x >> 4, y >> 4, z >> 4);
     }
 
     public Anvil.Section getSectionChunk(int chunkX, int chunkY, int chunkZ)
         throws IOException
     {
         Point3D key = new Point3D(chunkX, chunkY, chunkZ);
         Anvil.Section rval = sectionCache.get(key);
 
         if (null==rval) {
             CompoundTag chunk = chunkTagCache.getForChunk(chunkX, chunkZ);
             if (chunk == null)
                 return null;
             Anvil anvil = new Anvil(chunk);
 
             rval = anvil.getOrCreateSectionForRaw(chunkY<<4);
             sectionCache.put(key, rval);
         }
 
         return rval;
     }
 
     public void save()
         throws IOException
     {
         for (Map.Entry<Point, CompoundTag> en : chunkTagCache.cache.entrySet()) {
             Point chunkXz = en.getKey();
             RegionFile rf = world.getRegionFile(chunkXz.x, chunkXz.y);
 
             CompoundTag tag = en.getValue();
             byte[] chunkBytes = NbtIo.deflate(tag);
             System.out.println("chunk "+chunkXz+" ["+chunkBytes.length+"]");
             rf.write(chunkXz.x&0x1f, chunkXz.y&0x1f, chunkBytes, chunkBytes.length);
         }
     }
 
     public void relight()
     {
         for (Map.Entry<Point, CompoundTag> en : chunkTagCache.cache.entrySet()) {
 
             Anvil anvil = new Anvil(en.getValue());
             for (Anvil.Section section : anvil.getSections()) {
 
                 section.getBlockLight().zero();
 
                 section.getSkyLight().zero();
             }
 
         }
 
         for (Map.Entry<Point, CompoundTag> en : chunkTagCache.cache.entrySet()) {
 
             Anvil anvil = new Anvil(en.getValue());
             Point chunkPos = en.getKey();
             System.out.println("relighting x="+chunkPos.x+"<<4 , z="+chunkPos.y+"<<4");
 
             scanChunkForRelight(anvil, chunkPos);
         }
 
         Set<Point3D> notFinished = new HashSet<Point3D>();
         for (Map.Entry<Point, CompoundTag> en : chunkTagCache.cache.entrySet()) {
 
             Anvil anvil = new Anvil(en.getValue());
 
             Point chunkPos = en.getKey();
 
             for (Anvil.Section section : anvil.getSections()) {
                 Point3D sectionPos = new Point3D(chunkPos.x, section.getY(), chunkPos.y);
 
                 lightingOverpass(notFinished, sectionPos, section);
             }
         }
 
         while (!notFinished.isEmpty()) {
 
             System.out.println("light bleed for "+notFinished.size() + " sections");
 
             Set<Point3D> needsRecompute = notFinished;
             notFinished= new HashSet<Point3D>();
 
             for (Point3D sCoord : needsRecompute) {
 
                 try {
                     Anvil.Section s =getSectionChunk((int) sCoord.getX(), (int)sCoord.getY(), (int)sCoord.getZ());
 
                     lightingOverpass(notFinished, sCoord, s);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
 
 
     }
 
     /**
      *  Propagate sky and block light values throughout the cube.
      *  Identify adjacent sections that the light bleeds into and add them to the notFinished set.
      * @param notFinished will be updated with the section coordinates of cubes that will need further processing.
      * @param sCoord
      * @param s
      */
     public void lightingOverpass(Set<Point3D> notFinished, Point3D sCoord, Anvil.Section s)
     {
         boolean dirty = false;
         while (oneLightingPass(s.getSkyLight(), s.getBlocks(), true)) {
             dirty = true;
         }
         while (oneLightingPass(s.getBlockLight(), s.getBlocks(), false)) {
             dirty = true;
         }
 
         if (dirty) {
             propagateToAdjacentBlocks(notFinished, sCoord, s.getSkyLight(), s.getBlocks(), new GetSkyLightCube());
             propagateToAdjacentBlocks(notFinished, sCoord, s.getBlockLight(), s.getBlocks(), new GetBlockLightCube());
         }
     }
 
     private void propagateToAdjacentBlocks(Set<Point3D> notFinished, Point3D sectionPos, NibbleCube light, ByteCube blocks, GetLightingCube lightFlavor)
     {
         int strideY = 1<<8;
         int strideZ = 1<<4;
         int strideX = 1;
 
         interBlockPropagate(notFinished, light,
             new Point3D(sectionPos.getX()-1, sectionPos.getY(), sectionPos.getZ()),
             strideY, strideZ, strideX, 0, 15, blocks, lightFlavor);
         interBlockPropagate(notFinished, light,
             new Point3D(sectionPos.getX(), sectionPos.getY()-1, sectionPos.getZ()),
             strideZ, strideX, strideY, 0, 15, blocks, lightFlavor);
         interBlockPropagate(notFinished, light,
             new Point3D(sectionPos.getX(), sectionPos.getY(), sectionPos.getZ()-1),
             strideY, strideX, strideZ, 0, 15, blocks, lightFlavor);
 
         interBlockPropagate(notFinished, light,
             new Point3D(sectionPos.getX()+1, sectionPos.getY(), sectionPos.getZ()),
             strideY, strideZ, strideX, 15, 0, blocks, lightFlavor);
         interBlockPropagate(notFinished, light,
             new Point3D(sectionPos.getX(), sectionPos.getY()+1, sectionPos.getZ()),
             strideZ, strideX, strideY, 15, 0, blocks, lightFlavor);
         interBlockPropagate(notFinished, light,
             new Point3D(sectionPos.getX(), sectionPos.getY(), sectionPos.getZ()+1),
             strideY, strideX, strideZ, 15, 0, blocks, lightFlavor);
     }
 
     public boolean oneLightingPass(NibbleCube light, ByteCube blocks, boolean notFromSolid)
     {
         boolean[] dirty = { false };
         for (int y=0; y<16; y++) {
             for (int z=0; z<16; z++) {
                 for (int x=0; x<16; x++) {
                    if (false && ! BlockDatabase.transparent(blocks.get(x,y,z)))
                         continue; // skip this solid block
 
                     int s0 = light.get(x,y,z);
                     int newS = s0;
                     if (x>0) {
                         newS = maybeUpdateLight(light, x-1, y, z, newS, dirty, blocks, notFromSolid);
                     }
                     if (y>0) {
                         newS = maybeUpdateLight(light, x, y-1, z, newS, dirty, blocks, notFromSolid);
                     }
                     if (z>0) {
                         newS = maybeUpdateLight(light, x, y, z-1, newS, dirty, blocks, notFromSolid);
                     }
                     if (x<15) {
                         newS = maybeUpdateLight(light, x+1, y, z, newS, dirty, blocks, notFromSolid);
                     }
                     if (y<15) {
                         newS = maybeUpdateLight(light, x, y+1, z, newS, dirty, blocks, notFromSolid);
                     }
                     if (z<15) {
                         newS = maybeUpdateLight(light, x, y, z+1, newS, dirty, blocks, notFromSolid);
                     }
 
                     light.set(x, y, z, newS);
                 }
             }
         }
         return dirty[0];
     }
 
     public static int maybeUpdateLight(NibbleCube light, int x, int y, int z, int currLight, boolean[] dirty, ByteCube blocks, boolean notFromSolid)
     {
         if (notFromSolid && !BlockDatabase .transparent(blocks.get(x,y,z)))
             return currLight;
 
         int sw = light.get(x, y, z) -1;
         if (sw>currLight) {
             currLight = sw;
             dirty[0] = true;
         }
         return currLight;
     }
 
     /**
      *
      * @param lightFlavor
      * @param notFinished a set of {@link com.purplefrog.minecraftExplorer.Anvil.Section}s that we determine need recomputation.
      * @param srcLight the skylight cube for the "source" block
      * @param adjacentSectionPos the chunk-position of the destination Section
      * @param strideU one of the axes of the section-section boundary plane
      * @param strideV the other axis of the section-section boundary plane
      * @param strideQ the axis perpendicular to the section-section boundary plane
      * @param srcQ the coordinate of the boundary plane in the source block (either 0 or 15)
      * @param dstQ the coordinate of the bondary plane in the destination block (either 15 or 0)
      */
     public void interBlockPropagate(Set<Point3D> notFinished, NibbleCube srcLight, Point3D adjacentSectionPos, int strideU, int strideV, int strideQ, int srcQ, int dstQ, ByteCube srcBlocks, GetLightingCube lightFlavor)
     {
         Anvil.Section s2 = sectionCache.get(adjacentSectionPos);
         if (s2==null)
             return;
         NibbleCube light2 = lightFlavor.getLightLevels(s2);
         boolean dirty2 = false;
         for (int u=0; u<16; u++) {
             for (int v=0; v<16; v++) {
                 int p1 = u*strideU + v*strideV;
 
                 if (!BlockDatabase.transparent(srcBlocks.get_(p1+srcQ*strideQ)))
                     continue; // this block is solid
 
                 int s0 = srcLight.get_(p1 + srcQ * strideQ);
                 int s1 = light2.get_(p1 + dstQ * strideQ);
                 if (s1 < s0-1) {
                     light2.set_(p1 + dstQ * strideQ, s0 - 1);
                     dirty2 = true;
                 }
             }
         }
         if (dirty2) {
 //            System.out.println("light bleeds into "+adjacentSectionPos);
             notFinished.add(adjacentSectionPos);
         }
     }
 
     /**
      * scan the blocks in a chunk for light sources and recompute their light contribution.
      * @param anvil
      * @param chunkPos
      */
     public void scanChunkForRelight(Anvil anvil, Point chunkPos)
     {
         for (Anvil.Section section : anvil.getSections()) {
             ByteCube blocks = section.getBlocks();
             int pos=0;
             for (int y=0; y<16; y++) {
                 for (int z=0; z<16; z++) {
                     for (int x=0; x<16; x++, pos++) {
 //                            int pos = BlockVoxels.encodePos(x,y,z);
                         int light = BlockDatabase.lightLevel(blocks.get_(pos));
                         if (light>0) {
                             if (false) {
                                 updateLightMultiblock(x, y, z, light, chunkPos.x, section.getY(), chunkPos.y);
                             } else {
                                 section.getBlockLight().set(x,y,z, light);
                             }
                         }
                     }
                 }
             }
         }
 
         skyLightOpenSky(anvil);
     }
 
     public static void skyLightOpenSky(Anvil anvil)
     {
         int highest=-1;
         for (Anvil.Section section : anvil.getSections()) {
             if (section.getY() > highest) {
                 highest = section.getY();
             }
         }
 
         IntSquare heightMap = anvil.getHeightMap();
 
         boolean[] occluded = new boolean[16*16];
         for (int cy = highest; cy>=0; cy--) {
             Anvil.Section section = anvil.getSectionForChunkY(cy);
             if (section==null)
                 continue;
             int pos = 0;
             NibbleCube skyLight = section.getSkyLight();
             ByteCube blocks = section.getBlocks();
 
             for (int y=15; y>=0; y--){
                 for (int z=0; z<16; z++) {
                     for (int x=0; x<16; x++, pos++) {
                         int skyPtr = z << 4 | x;
                         skyLight.set(x,y,z, occluded[skyPtr] ? 0:15);
                         if (!occluded[skyPtr]) {
 
                             int bt = blocks.get(x,y,z);
                             if (!BlockDatabase.transparent(bt)) {
                                 int adj = 1; // XXX still trying to figure out what to do.
                                 heightMap.set(x,cy*16 + y +adj,z);
                                 occluded[skyPtr] = true;
                             }
                         }
 
                     }
                 }
             }
         }
     }
 
     /**
      * Update the chunk's lighting based on a new light source at x,y,z inside the chunk.
      * Possibly update adjacent sections (but only if they are in our cache).
      *
      * @param x position of light within the chunk
      * @param y position of light within the chunk
      * @param z position of light within the chunk
      * @param light intensity of the light
      * @param chunkX0 position of the chunk containing the light (in chunk coordinates, raw>>4)
      * @param chunkY0 position of the chunk containing the light (in chunk coordinates, raw>>4)
      * @param chunkZ0 position of the chunk containing the light (in chunk coordinates, raw>>4)
      */
     public void updateLightMultiblock(int x, int y, int z, int light, int chunkX0, int chunkY0, int chunkZ0)
     {
         for (int b=(y-light)>>4; b <=(y+light)>>4; b++) {
             for (int c=(z-light)>>4; c<= (z+light)>>4; c++) {
                 for (int a=(x-light)>>4; a<= (x+light)>>4; a++) {
                     Anvil.Section s2 = maybeGetSection(a+ chunkX0, b+ chunkY0, c+ chunkZ0);
                     if (s2 != null)
                         updateLight( x-a*16, y-b*16, z-c*16, s2.getBlockLight(), light);
                 }
             }
 //                                updateLight( x, y, z, blockLight, light);
         }
     }
 
     private Anvil.Section maybeGetSection(int chunkX, int chunkY, int chunkZ)
     {
         try {
             CompoundTag t = chunkTagCache.maybeGetForChunk(chunkX, chunkZ);
             if (t==null)
                 return null;
 
             Anvil anvil = new Anvil(t);
 
             return anvil.getSectionForChunkY(chunkY);
         } catch (IOException e) {
             return null;
         }
     }
 
     public void updateLight(int cx, int cy, int cz, NibbleCube blockLight, int light)
     {
         int pos=0;
         for (int y=0; y<16; y++) {
             for (int z=0; z<16; z++) {
                 for (int x=0; x<16; x++) {
                     int d = Math.abs(x-cx) + Math.abs(y-cy) + Math.abs(z-cz);
                     int old = blockLight.get(x,y,z);
                     int updated = Math.max(old, NibbleCube.nibbleClamp(light - d));
                     blockLight.set(x,y,z, updated);
                 }
             }
         }
     }
 
     public void fillCube(int blockType, int x0, int y0, int z0, int dx, int dy, int dz)
         throws IOException
     {
         for (int x=0; x<dx; x++) {
             for (int y=0; y<dy; y++) {
                 for (int z=0; z<dz; z++) {
                     setBlock(x + x0, y + y0, z + z0, blockType);
                 }
             }
         }
     }
 
     public void fillCubeByCorners(BlockTemplate template, int x1, int y1, int z1, int x2, int y2, int z2)
         throws IOException
     {
         for (int x=x1; x<x2; x++) {
             for (int y=y1; y<y2; y++) {
                 for (int z=z1; z<z2; z++) {
                     setBlock(x, y, z, template.getBlock(x - x1, y - y1, z - z1));
                 }
             }
         }
     }
 
     public void drawBorderedRectangle(int x1, int y0, int z1, int x2, int z2, BlockTemplate north, BlockTemplate south, BlockTemplate east, BlockTemplate west, BlockTemplate meadow)
         throws IOException
     {
         for (int x=0; x+x1 < x2; x++) {
             for (int z=0; z+z1 < z2; z++) {
 
                 int n=2;
                 int d=1;
                 ColumnRef col=null;
 
                 if (null != west) {
                     n = x;
                     d = west.depth;
                     col = west.referenceColumn(z, x);
                 }
 
                 if (null != east) {
                     int n2 = x2-(x+x1)-1;
                     int d2 = east.depth;
                     if ( n2*d <= n*d2) {
                         col = east.referenceColumn(z, n2);
                         n = n2;
                         d = d2;
                     }
                 }
 
                 if (null != south) {
                     int n2 = z2-(z+z1)-1;
                     int d2 = south.depth;
                     if ( n2*d <= n*d2) {
                         col = south.referenceColumn(x,n2);
                         n = n2;
                         d = d2;
                     }
                 }
 
                 if (null != north) {
                     int n2 = z;
                     int d2 = north.depth;
                     if ( n2*d <= n*d2) {
                         col = north.referenceColumn(x,z);
                         n = n2;
                         d = d2;
                     }
                 }
 
                 if (n>=d)
                     col = meadow.referenceColumn(x,z);
 
                 col.renderColumn(this, x+x1, y0, z+z1);
             }
         }
     }
 
     public interface GetLightingCube
     {
         public NibbleCube getLightLevels(Anvil.Section s);
     }
 
     public static class GetSkyLightCube
     implements GetLightingCube
     {
         @Override
         public NibbleCube getLightLevels(Anvil.Section s)
         {
             return s.getSkyLight();
         }
     }
 
     public static class GetBlockLightCube
         implements GetLightingCube
     {
         @Override
         public NibbleCube getLightLevels(Anvil.Section s)
         {
             return s.getBlockLight();
         }
     }
 
 }
