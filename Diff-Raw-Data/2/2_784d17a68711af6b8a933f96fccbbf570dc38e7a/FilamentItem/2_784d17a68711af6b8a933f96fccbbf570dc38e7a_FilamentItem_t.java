 package monoxide.Lanterns;
 
 import java.util.List;
 
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 
 public class FilamentItem extends Item {
 	
 	public static final int PLAIN = 0;
 	public static final int FORGED = 1;
 	public static final int WATERPROOF = 2;
 	
 	public FilamentItem(int i) {
 		super(i);
 		setMaxStackSize(16);
		this.setCreativeTab(CreativeTabs.tabMaterials);
 	}
 	
 	@Override
 	public String getTextureFile() {
 		return CommonProxy.ITEMS_PNG;
 	}
 	
 	@Override
 	public int getIconFromDamage(int damage) {
 		return damage;
 	}
 	
 	@Override
 	public String getItemDisplayName(ItemStack itemStack) {
 		switch(itemStack.getItemDamage()) {
 		case PLAIN:
 			return "Filament";
 		case FORGED:
 			return "Fired Filament";
 		case WATERPROOF:
 			return "Waterproof Filament";
 		}
 		return super.getItemDisplayName(itemStack);
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubItems(int par1, CreativeTabs tabs, List subItems) {
 		for (int i = 0; i < 3; i++) {
 			subItems.add(new ItemStack(this, 1, i));
 		}
 	}
 }
