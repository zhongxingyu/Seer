 package spazzysmod.world;
 
 import net.minecraft.util.Vec3;
 import net.minecraft.world.WorldProvider;
 import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.chunk.IChunkProvider;
 import spazzysmod.world.biome.SpazzysBiomes;
 import spazzysmod.world.chunk.ChunkManagerMars;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class WorldProviderMars extends WorldProvider {
 
     /**
      * creates a new world chunk manager for WorldProvider
      */
     @Override
     public void registerWorldChunkManager() {
         this.worldChunkMgr = new WorldChunkManagerHell(SpazzysBiomes.marsBiome,
                 1.0F, 0.0F);
         this.isHellWorld = true;
         this.hasNoSky = true;
         this.dimensionId = SpazzysDimensions.marsDimensionID;
     }
 
     
     @Override
    public boolean canDoLightning(Chunk chunk)
    {
        return false;
    }
    
    @Override
     @SideOnly(Side.CLIENT)
     /**
      * Return Vec3D with biome specific fog color
      */
     public Vec3 getFogColor(float par1, float par2) {
         return this.worldObj.getWorldVec3Pool().getVecFromPool(
                 0.20000000298023224D, 0.029999999329447746D,
                 0.029999999329447746D);
     }
 
     /**
      * Creates the light to brightness table
      */
     @Override
     protected void generateLightBrightnessTable() {
         float f = 0.1F;
 
         for (int i = 0; i <= 15; ++i) {
             float f1 = 1.0F - i / 15.0F;
             this.lightBrightnessTable[i] = (1.0F - f1) / (f1 * 3.0F + 1.0F)
                     * (1.0F - f) + f;
         }
     }
 
     @Override
     public IChunkProvider createChunkGenerator() {
         return new ChunkManagerMars(this.worldObj, this.worldObj.getSeed(), hasNoSky);
     }
 
     /**
      * Returns 'true' if in the "main surface world", but
      * 'false' if in the Nether or End dimensions.
      */
     @Override
     public boolean isSurfaceWorld() {
         return false;
     }
 
     /**
      * Will check if the x, z position specified is alright
      * to be set as the map spawn point
      */
     @Override
     public boolean canCoordinateBeSpawn(int par1, int par2) {
         return false;
     }
 
     /**
      * Calculates the angle of sun and moon in the sky
      * relative to a specified time (usually worldTime)
      */
     @Override
     public float calculateCelestialAngle(long par1, float par3) {
         return 0.5F;
     }
 
     /**
      * True if the player can respawn in this dimension
      * (true = overworld, false = nether).
      */
     @Override
     public boolean canRespawnHere() {
         return false;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     /**
      * Returns true if the given X,Z coordinate should show environmental fog.
      */
     public boolean doesXZShowFog(int par1, int par2) {
         return true;
     }
 
     /**
      * Returns the dimension's name, e.g. "The End",
      * "Nether", or "Overworld".
      */
     @Override
     public String getDimensionName() {
         return "Mars";
     }
 }
