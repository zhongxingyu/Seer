 package assets.electrolysm.electro.crafting.items;
 
 import java.util.List;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import assets.electrolysm.electro.electrolysmCore;
 import assets.electrolysm.electro.common.CommonProxy;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 public class grindStone extends Item {
 
 	@SideOnly(Side.CLIENT)
	private Icon[] grindStoneIcon = new Icon[3];
 	
 	public grindStone(int id) {
 		super(id);
 		
 		this.hasSubtypes = true;
 		this.setCreativeTab(electrolysmCore.TabElectrolysm);
 	}
 	
 	public String getUnlocalizedName(ItemStack stack)
     {
         int dmg = stack.getItemDamage();
         return "grindStone_" + dmg;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister reg)
     {
         for (int i = 0; i < grindStoneIcon.length; i ++)
         {
         	grindStoneIcon[i] = reg.registerIcon("electrolysm:" + "grindStone-" + i);
         }
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int dmg)
     {
         return grindStoneIcon[dmg];
     }
 
     public void getSubItems(int id, CreativeTabs creativeTab, List list)
     {
        for (int i = 0; i < grindStoneIcon.length; i++)
         {
             list.add(new ItemStack(electrolysmCore.grindStone, 1, i));
         }
     }
 
 }
