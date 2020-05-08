 package tcc.MotherOfPearl.items;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
 import tcc.MotherOfPearl.ModInfo;
 import tcc.MotherOfPearl.ModMain;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
public class mopItem10 extends ItemFood {
 
	public mopItem10(int par1, boolean alwaysEdible, int saturationModifier) {
		super(par1, saturationModifier, alwaysEdible);
 		// TODO Auto-generated constructor stub
 		this.setMaxStackSize(64);
 		this.setCreativeTab(ModMain.tabMoP);
 		this.setUnlocalizedName("oysterMeat");
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		itemIcon = par1IconRegister.registerIcon(ModInfo.MOD_ID.toLowerCase() + ":" + this.getUnlocalizedName().substring(5));
 	}
 	
 }
