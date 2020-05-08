 package teamm.mods.virtious.block;
 
 import teamm.mods.virtious.Virtious;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 import net.minecraftforge.fluids.BlockFluidClassic;
 import net.minecraftforge.fluids.Fluid;
 
 public class VirtiousAcid extends BlockFluidClassic
 {
 	protected Icon[] iconArray;
 	
 	public VirtiousAcid(int id) 
 	{
 		super(id, Virtious.virtiousFluid, Material.water);
 		Virtious.virtiousFluid.setBlockID(id);
 	}
 	
 	public void registerIcons(IconRegister r)
 	{
 		this.iconArray = new Icon[]
 				{
					r.registerIcon("virtious:VirtiousAcidStill"),
					r.registerIcon("virtious:VirtiousAcidFlowing")
 				};
 	}
 	
 	public Icon getIcon(int side, int meta)
 	{
 		return meta == 1 ? iconArray[0] : iconArray[1];		
 	}
 }
