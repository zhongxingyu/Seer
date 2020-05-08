 package spazzysmod.world;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.entity.Entity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.WorldProvider;
 import net.minecraft.world.biome.WorldChunkManagerHell;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.chunk.IChunkProvider;
 import spazzysmod.world.biome.SpazzysBiomes;
 import spazzysmod.world.chunk.ChunkManagerMoon;
 
 public class WorldProviderMoon extends WorldProvider {
 
 
 
     /*
      * public String getSunTexture() { return
      * "/path/to/your/sun/texture.png"; }
      * 
      * public String getMoonTexture() { return
      * "/path/to/your/moon/texture.png"; }
      */
 
 

     @Override
     public IChunkProvider createChunkGenerator() {
         return new ChunkManagerMoon(this.worldObj, this.worldObj.getSeed(),
                 hasNoSky);
     }
 
 
     @Override
     public String getDimensionName() {
         return "Moon";
     }
     @SideOnly(Side.CLIENT)
 
     /**
      * the y level at which clouds are rendered.
      */
     public float getCloudHeight()
     {
         return -1000F;
     }
 
 
     @Override
     protected void generateLightBrightnessTable()
     {
         final float var1 = 0.0F;
 
 
         for (int var2 = 0; var2 <= 15; ++var2)
         {
             final float var3 = 1.0F - var2 / 15.0F;
             this.lightBrightnessTable[var2] = (1.0F - var3) / (var3 * 3.0F + 1.0F) * (1.0F - var1) + var1;
         }
     }
 
 
     @Override
     public float[] calcSunriseSunsetColors(float var1, float var2)
     {
         return null;
     }
 
     
     @Override
     public Vec3 getSkyColor(Entity cameraEntity, float partialTicks)
     {
         return this.worldObj.getWorldVec3Pool().getVecFromPool(0, 0, 0);
     }
 
 
     @Override
     public float calculateCelestialAngle(long par1, float par3)
     {
         final int var4 = (int) (par1 % 24000L);
         float var5 = (var4 + par3) / 24000.0F - 0.25F;
 
 
         if (var5 < 0.0F)
         {
             ++var5;
         }
 
 
         if (var5 > 1.0F)
         {
             --var5;
         }
 
 
         final float var6 = var5;
         var5 = 1.0F - (float) ((Math.cos(var5 * Math.PI) + 1.0D) / 2.0D);
         var5 = var6 + (var5 - var6) / 3.0F;
         return var5;
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     public float getStarBrightness(float par1)
     {
         final float var2 = this.worldObj.getCelestialAngle(par1);
         float var3 = 1.0F - (MathHelper.cos(var2 * (float) Math.PI * 2.0F) * 2.0F + 0.25F);
 
 
         if (var3 < 0.0F)
         {
             var3 = 0.0F;
         }
 
 
         if (var3 > 1.0F)
         {
             var3 = 1.0F;
         }
 
 
         return var3 * var3 * 0.5F + 0.3F;
     }
 
     
     @Override
     public void updateWeather()
     {
         this.worldObj.getWorldInfo().setRainTime(0);
         this.worldObj.getWorldInfo().setRaining(false);
         this.worldObj.getWorldInfo().setThunderTime(0);
         this.worldObj.getWorldInfo().setThundering(false);
     }
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Return Vec3D with biome specific fog color
      */
     public Vec3 getFogColor(float par1, float par2)
     {
         int i = 10518688;
         float f2 = MathHelper.cos(par1 * (float)Math.PI * 2.0F) * 2.0F + 0.5F;
 
         if (f2 < 0.0F)
         {
             f2 = 0.0F;
         }
 
         if (f2 > 1.0F)
         {
             f2 = 1.0F;
         }
 
         float f3 = (float)(i >> 16 & 255) / 255.0F;
         float f4 = (float)(i >> 8 & 255) / 255.0F;
         float f5 = (float)(i & 255) / 255.0F;
         f3 *= f2 * 0.0F + 0.15F;
         f4 *= f2 * 0.0F + 0.15F;
         f5 *= f2 * 0.0F + 0.15F;
         return this.worldObj.getWorldVec3Pool().getVecFromPool((double)f3, (double)f4, (double)f5);
     }
 
     @SideOnly(Side.CLIENT)
     public boolean isSkyColored()
     {
         return false;
     }
 
 
     @SideOnly(Side.CLIENT)
 
     /**
      * Returns true if the given X,Z coordinate should show environmental fog.
      */
     public boolean doesXZShowFog(int par1, int par2)
     {
         return true;
     }
 
 
     @Override
     public boolean canCoordinateBeSpawn(int var1, int var2)
     {
         return true;
     }
 
 
     @Override
     public boolean canRespawnHere()
     {
         return false;
     }
 
     @Override
     public boolean canSnowAt(int x, int y, int z)
     {
         return false;
     }
 
 
     @Override
     public boolean canBlockFreeze(int x, int y, int z, boolean byWater)
     {
         return false;
     }
 
 
     @Override
     public boolean canDoLightning(Chunk chunk)
     {
         return false;
     }
 
 
     @Override
     public boolean canDoRainSnowIce(Chunk chunk)
     {
         return false;
     }
 
     
     @Override
     public void registerWorldChunkManager() 
     {
         this.worldChunkMgr = new WorldChunkManagerHell(SpazzysBiomes.moonBiome,1.0F, 0.0F);
         this.dimensionId = SpazzysDimensions.moonDimensionID;
     }
 }
