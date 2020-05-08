 package sbfp.world;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import sbfp.BlockSB;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockOre extends BlockSB{
 
 	public Icon[] icons = new Icon[256];
 	public static final String[] names = {"oreThorium","oreFluorite","oreMoS2"};
 
 	public BlockOre(int type, int id){
		super(id,Material.rock,"ore");
 		this.setCreativeTab(CreativeTabs.tabBlock);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register){
 		icons[0] = register.registerIcon("sbfp:"+BlockOre.names[0]);
 		icons[1] = register.registerIcon("sbfp:"+BlockOre.names[1]);
 		icons[2] = register.registerIcon("sbfp:"+BlockOre.names[2]);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int meta){
 		return icons[meta];
 	}
 
 	@Override
 	public int damageDropped(int meta){
 		return meta;
 	}
 
 	@Override
 	public void getSubBlocks(int id, CreativeTabs tabs, List list){
 		for(int i = 0; i<3; ++i){
 			list.add(new ItemStack(id,1,i));
 		}
 	}
 
 	@Override
 	public ArrayList<ItemStack> getBlockDropped(World w, int x, int y, int z, int metadata, int fortune){
 		ArrayList<ItemStack> q = new ArrayList<ItemStack>();
 		// maybe put pick code here? I'm not sure...
 		q.add(new ItemStack(this.blockID,1,metadata));
 		return q;
 	}
 }
