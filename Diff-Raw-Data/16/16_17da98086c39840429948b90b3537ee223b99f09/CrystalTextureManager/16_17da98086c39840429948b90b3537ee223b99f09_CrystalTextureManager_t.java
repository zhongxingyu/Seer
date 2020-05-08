 package shadowhax.crystalluscraft.block.renderer;
 
 import net.minecraft.util.ResourceLocation;
 
 public class CrystalTextureManager {
 
     private static String[] crystalNames = new String[] {"lux", "ros", "natura", "obitus", "ignis", "zephyr"};
 
     public static ResourceLocation getRLForCrystal(int growthStage, int meta) {
 
        return new ResourceLocation("crystalluscraft:textures/crystals/stage" + growthStage + "/" + crystalNames[meta] + ".png");
     }
 }
