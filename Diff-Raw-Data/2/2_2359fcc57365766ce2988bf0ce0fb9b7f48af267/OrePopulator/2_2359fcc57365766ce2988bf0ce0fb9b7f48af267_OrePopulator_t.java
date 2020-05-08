 package com.gmail.jaquadro.oreplus;
 
 import org.bukkit.Chunk;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.block.Block;
 import org.bukkit.generator.BlockPopulator;
 
 import java.util.List;
 import java.util.Random;
 
 public class OrePopulator extends BlockPopulator {
     private OrePlus _plugin;
 
     public OrePopulator (OrePlus plugin)
     {
         _plugin = plugin;
     }
 
     @Override
     public void populate(World world, Random random, Chunk chunk)
     {
         applyClearRules(world, chunk);
         applyGenerateRules(world, random, chunk);
     }
 
     private void applyGenerateRules (World world, Random random, Chunk chunk)
     {
         List<OreRule> rules = _plugin.GetOreRules(world);
         if (rules == null)
             return;
 
         for (OreRule rule : rules) {
             if (!rule.isEnabled())
                 continue;
 
             OPMaterial material = rule.getMaterial();
             if (!material.isBlockValid())
                 continue;
 
             for (int i = 0; i < rule.getRounds(); i++) {
                 if (rule.getProbability() < random.nextDouble())
                     continue;
 
                 int x = chunk.getX() * 16 + random.nextInt(16);
                 int y = rule.getMinHeight() + random.nextInt(rule.getMaxHeight() - rule.getMinHeight());
                 int z = chunk.getZ() * 16 + random.nextInt(16);
 
                 if (rule.getIncludedBiomes().size() > 0) {
                     Biome biome = world.getBiome(x, z);
                     if (!rule.getIncludedBiomes().contains(biome))
                         continue;
                 }
 
                 generate(world, random, x, y, z, rule.getSize(), material);
             }
         }
     }
 
     private void applyClearRules (World world, Chunk chunk)
     {
         List<ClearRule> rules = _plugin.GetClearRules(world);
         if (rules == null)
             return;
 
         for (ClearRule rule : rules) {
             if (!rule.isEnabled())
                 continue;
 
             OPMaterial material = rule.getMaterial();
             if (!material.isBlockValid())
                 continue;
 
             OPMaterial replacement = new OPMaterial(Material.STONE.getId());
             if (rule.getClearMaterial().isBlockValid())
                 replacement = rule.getClearMaterial();
 
             int starty = rule.getMinHeight();
             int endy = rule.getMaxHeight();
 
             for (int x = 0; x < 16; x++) {
                 for (int z = 0; z < 16; z++) {
                     if (rule.getIncludedBiomes().size() > 0) {
                         Biome biome = world.getBiome(x, z);
                         if (!rule.getIncludedBiomes().contains(biome))
                             continue;
                     }
 
                     for (int y = starty; y < endy; y++) {
                         Block block = chunk.getBlock(x, y, z);
                         if (block.getTypeId() == material.getBlockId()) {
                             if (material.isDataValid() && block.getData() != (byte)material.getBlockData())
                                 continue;
 
                             block.setTypeId(replacement.getBlockId());
                             if (replacement.isDataValid())
                                 block.setData((byte)replacement.getBlockData());
                         }
                     }
                 }
             }
         }
     }
 
     private void generate (World world, Random rand, int x, int y, int z, int size, OPMaterial material)
     {
         double rpi = rand.nextDouble() * Math.PI;
 
         double x1 = x + 8 + Math.sin(rpi) * size / 8.0F;
         double x2 = x + 8 - Math.sin(rpi) * size / 8.0F;
         double z1 = z + 8 + Math.cos(rpi) * size / 8.0F;
         double z2 = z + 8 - Math.cos(rpi) * size / 8.0F;
 
         double y1 = y + rand.nextInt(3) + 2;
         double y2 = y + rand.nextInt(3) + 2;
 
         for (int i = 0; i <= size; i++) {
             double xPos = x1 + (x2 - x1) * i / size;
             double yPos = y1 + (y2 - y1) * i / size;
             double zPos = z1 + (z2 - z1) * i / size;
 
             double fuzz = rand.nextDouble() * size / 16.0D;
             double fuzzXZ = (Math.sin((float) (i * Math.PI / size)) + 1.0F) * fuzz + 1.0D;
             double fuzzY = (Math.sin((float) (i * Math.PI / size)) + 1.0F) * fuzz + 1.0D;
 
             int xStart = (int)Math.floor(xPos - fuzzXZ / 2.0D);
             int yStart = (int)Math.floor(yPos - fuzzY / 2.0D);
             int zStart = (int)Math.floor(zPos - fuzzXZ / 2.0D);
 
             int xEnd = (int)Math.floor(xPos + fuzzXZ / 2.0D);
             int yEnd = (int)Math.floor(yPos + fuzzY / 2.0D);
             int zEnd = (int)Math.floor(zPos + fuzzXZ / 2.0D);
 
             for (int ix = xStart; ix <= xEnd; ix++) {
                 double xThresh = (ix + 0.5D - xPos) / (fuzzXZ / 2.0D);
                 if (xThresh * xThresh < 1.0D) {
                     for (int iy = yStart; iy <= yEnd; iy++) {
                         double yThresh = (iy + 0.5D - yPos) / (fuzzY / 2.0D);
                         if (xThresh * xThresh + yThresh * yThresh < 1.0D) {
                             for (int iz = zStart; iz <= zEnd; iz++) {
                                 double zThresh = (iz + 0.5D - zPos) / (fuzzXZ / 2.0D);
                                 if (xThresh * xThresh + yThresh * yThresh + zThresh * zThresh < 1.0D) {
                                    Block block = world.getBlockAt(ix, iy, iz);
                                     if (block.getType() == Material.STONE) {
                                         block.setTypeId(material.getBlockId());
                                         if (material.isDataValid())
                                             block.setData((byte)material.getBlockData());
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 }
