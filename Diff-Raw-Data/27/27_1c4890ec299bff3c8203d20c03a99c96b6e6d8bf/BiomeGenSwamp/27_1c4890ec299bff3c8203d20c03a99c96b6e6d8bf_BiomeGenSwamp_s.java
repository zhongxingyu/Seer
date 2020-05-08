 package NaturalBiomes2.Biomes;
 
 import java.util.Random;
 
 import net.minecraft.world.ColorizerFoliage;
 import net.minecraft.world.ColorizerGrass;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.gen.feature.WorldGenerator;
 
 public class BiomeGenSwamp extends BiomeGenBase
 {
     public BiomeGenSwamp(int id, int treesChunk)
     {
         super(id);
         this.theBiomeDecorator.treesPerChunk = treesChunk;
         this.theBiomeDecorator.flowersPerChunk = -999;
         this.theBiomeDecorator.deadBushPerChunk = 1;
         this.theBiomeDecorator.mushroomsPerChunk = 8;
         this.theBiomeDecorator.reedsPerChunk = 10;
         this.theBiomeDecorator.clayPerChunk = 1;
         this.theBiomeDecorator.waterlilyPerChunk = 4;
         this.theBiomeDecorator.bigMushroomsPerChunk = 1;
         this.waterColorMultiplier = 14745518;
     }
 
    public WorldGenerator getRandomWorldGenForTrees(Random var1)
    {
        return this.worldGeneratorSwamp;
    }

     public int getBiomeGrassColor()
     {
        double var1 = (double)this.getFloatTemperature();
        double var3 = (double)this.getFloatRainfall();
        return ((ColorizerGrass.getGrassColor(var1, var3) & 16711422) + 5115470) / 2;
     }
 

     public int getBiomeFoliageColor()
     {
        double var1 = (double)this.getFloatTemperature();
        double var3 = (double)this.getFloatRainfall();
        return ((ColorizerFoliage.getFoliageColor(var1, var3) & 16711422) + 5115470) / 2;
     }
 }
 
