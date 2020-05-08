 package techguy.mods.undead.common;
 
 import java.util.Random;
 
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
 
 public class ModUndeadBlockImmortiumOre extends Block
 {
 	public ModUndeadBlockImmortiumOre (int i, int j)
 	{
 		super(i, j, Material.iron);
 	}
 
 	public int idDropped(int i, Random random, int j)
 	{
 		return ModUndeadMainRegistry.immortiumShard.shiftedIndex;
 	}
 	
 	public int quantityDropped(Random random)
 	{
 		return 1 + random.nextInt(1);
 	}
 	
 	public String getTextureFile()
 	{
 		return "/undeadPlus/terrain.png";
 	}

 }
