 package NaturalBiomes2.Biomes;
 
 import java.util.Random;
 
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
 
    @SideOnly(Side.CLIENT)
     public int getBiomeGrassColor()
     {
        double d0 = (double)this.getFloatTemperature();
        double d1 = (double)this.getFloatRainfall();
        return ((ColorizerGrass.getGrassColor(d0, d1) & 16711422) + 5115470) / 2;
     }
 
    @SideOnly(Side.CLIENT)
     public int getBiomeFoliageColor()
     {
        double d0 = (double)this.getFloatTemperature();
        double d1 = (double)this.getFloatRainfall();
        return ((ColorizerFoliage.getFoliageColor(d0, d1) & 16711422) + 5115470) / 2;
     }
 }
 
