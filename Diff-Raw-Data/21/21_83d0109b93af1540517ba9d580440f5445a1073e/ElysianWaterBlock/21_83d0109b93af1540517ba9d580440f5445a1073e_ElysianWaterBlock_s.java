 package mods.elysium.block;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import mods.elysium.DefaultProps;
 import mods.elysium.Elysium;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraftforge.fluids.BlockFluidClassic;
 
 public class ElysianWaterBlock extends BlockFluidClassic {
 
 	protected static Icon[] theIcon;
 
 	public ElysianWaterBlock(int id) {
 		super(id, Elysium.elysianWaterFluid, Material.water);
 		Elysium.elysianWaterFluid.setBlockID(id);
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister iconRegister)
     {
        this.theIcon = new Icon[] {iconRegister.registerIcon("elysium:elysian_water_still"), iconRegister.registerIcon("elysium:elysian_water_flow")};
     }
 
 	
     
     @SideOnly(Side.CLIENT)
     @Override
     /**
      * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
      */
    public Icon getIcon(int par1, int par2)
     {
        return par2 != 0 && par2 != 1 ? this.theIcon[1] : this.theIcon[0];
     }
 	
 //	@Override
 //	public int colorMultiplier(IBlockAccess iblockaccess, int x, int y, int z) {
 //		return 0x8CF4FF;
 //	}
 }
