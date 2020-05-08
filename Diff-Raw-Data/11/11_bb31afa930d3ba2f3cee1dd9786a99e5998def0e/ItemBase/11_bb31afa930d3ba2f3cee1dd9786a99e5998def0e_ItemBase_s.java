 package mods.CountryGamer_Possession.Possession.Items;
 
 import mods.CountryGamer_Possession.Possession.PosMain;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemBase extends Item {
 
 	public ItemBase(int id) {
 		super(id);
 		this.setCreativeTab(PosMain.tabsPion);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
		itemIcon = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1));
 	}
 
 	
 	
 	
 }
