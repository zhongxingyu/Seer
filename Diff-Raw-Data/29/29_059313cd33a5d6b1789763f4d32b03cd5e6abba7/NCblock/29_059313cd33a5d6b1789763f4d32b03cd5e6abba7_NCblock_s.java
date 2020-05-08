 package MMC.neocraft.block;
 
 import MMC.neocraft.NeoCraftTab;
 import MMC.neocraft.lib.Reference;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 
 public class NCblock extends Block
 {
<<<<<<< HEAD
	public static Block orangeWood, orangeLeaves, blockTest, saplingOrange, teaSteeper, kilnCore;
=======
	public static Block orangeWood, orangeLeaves, blockTest, saplingOrange, teaSteeper, magicSteeper;
>>>>>>> 1c6f3f25b592ba7010d20a775c1e1964f41a5477
 	
 	public NCblock(int id, Material material)
 	{
 		super(id, material);
 		setCreativeTab(NeoCraftTab.neoCraftTab);
 	}
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register) { this.blockIcon = register.registerIcon(Reference.MOD_ID + ":" + (this.getUnlocalizedName().substring(5))); }
 	
 	public static void init()
 	{
 		orangeWood = new BlockOrangeWood(253, Material.wood).setHardness(2.0F).setStepSound(soundWoodFootstep).setUnlocalizedName("orangeWood");
 		orangeLeaves = new BlockOrangeLeaves(254).setHardness(0.2F).setLightOpacity(1).setStepSound(soundGrassFootstep).setUnlocalizedName("orangeLeaves");
 	    saplingOrange = new SaplingOrange(255, Material.leaves).setHardness(0.0F).setStepSound(soundGrassFootstep).setUnlocalizedName("orangeSapling");
 
 	    teaSteeper = new BlockSteeper(1615, Material.glass).setHardness(1.5F).setStepSound(soundGlassFootstep).setUnlocalizedName("teaSteeper");
<<<<<<< HEAD
	    kilnCore = new KilnCore(1615, Material.glass).setHardness(1.5F).setStepSound(soundGlassFootstep).setUnlocalizedName("kilnCore");
=======
 		blockTest = new BlockTest(1616, Material.glass).setHardness(0.5F).setUnlocalizedName("blockTest");
 	    magicSteeper = new MagicSteeper(1617, Material.glass).setHardness(1.5F).setStepSound(soundGlassFootstep).setUnlocalizedName("magicSteeper");
>>>>>>> 1c6f3f25b592ba7010d20a775c1e1964f41a5477
 	}
 }
