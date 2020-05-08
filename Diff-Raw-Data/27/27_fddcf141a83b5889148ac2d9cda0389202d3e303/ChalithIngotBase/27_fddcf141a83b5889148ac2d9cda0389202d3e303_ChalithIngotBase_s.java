 package tektor.minecraft.chalith.items;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 public class ChalithIngotBase extends Item {
 
	@SideOnly(Side.CLIENT)
 	private Icon[] icon = new Icon[2];
 	
 	public ChalithIngotBase(int id) {
 		super(id);
 		setMaxStackSize(64);
 		setCreativeTab(CreativeTabs.tabMaterials);
 		setHasSubtypes(true);
 	}
 	
 	@Override
 	public Icon getIcon(ItemStack stack,
 	           int pass)
 	{
 		int id = stack.getItemDamage();
 		return icon[id];
 	}
 	
 	@Override
 	public Icon getIconFromDamageForRenderPass(int par1,
             int par2)
 	{
 		return icon[par1];
 	}
 	
 	@Override
 	public Icon getIconFromDamage(int par1) 
 	{
 		return icon[par1];
 	}
 	
 	@Override
 	public String getUnlocalizedName(ItemStack stack)
 	{
 		switch(stack.getItemDamage())
 		{
 		case 0: return "lorynIngot";
 		case 1: return "sorfynOre";
 		default: return "??";
 		}
 		
 	}
 	
 	@Override
  	public String getItemDisplayName(ItemStack stack)
  	{
  		switch(stack.getItemDamage())
  		{
  		case 0: return "Loryn Ingot";
 		case 1: return "Sorfyn Ingot";
 		default: return "??";
  		}
  	}
 	
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		icon[0] = par1IconRegister.registerIcon("chalith:lorynIngot");
 		icon[1] = par1IconRegister.registerIcon("chalith:sorfynIngot");
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubItems(int par1, CreativeTabs tab, List subItems) {
 		
 			subItems.add(new ItemStack(this, 1, 0));
 			subItems.add(new ItemStack(this, 1, 1));
 	}
 
 }
