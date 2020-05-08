 package NetherStuffs.Items;
 
 import java.util.List;
 
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 import net.minecraft.src.CreativeTabs;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 
 public class NetherStonePotionBowl extends Item {
 
 	public static String[] itemNames = new String[] { "NetherStonePotionBowlHellfire", "NetherStonePotionBowlAcid", "NetherStonePotionBowlDeath" };
	public static String[] itemDisplayNames = new String[] { "Hellfire Bowl", "Acid Bowl", "Death Bowl" };
 
 	public static final int hellfire = 0;
 	public static final int acid = 1;
 	public static final int death = 2;
 
 	public NetherStonePotionBowl(int par1) {
 		super(par1);
 		this.setMaxStackSize(1);
 		this.setHasSubtypes(true);
 		this.setMaxDamage(0);
 		this.setCreativeTab(CreativeTabs.tabBrewing);
 	}
 
 	public String getTextureFile() {
 		return "/items.png";
 	}
 
 	public int getIconFromDamage(int par1) {
 		switch (par1) {
 		case hellfire:
 			return 48;
 		case acid:
 			return 49;
 		case death:
 			return 50;
 		default:
 			return 19;
 		}
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean hasEffect(ItemStack par1ItemStack) {
 		return true;
 	}
 	
 	public static int getMetadataSize() {
 		return itemNames.length;
 	}
 
 	public String getItemNameIS(ItemStack is) {
 		String name = "";
 		if (is.getItemDamage() < getMetadataSize() && is.getItemDamage() >= 0)
 			name = itemNames[is.getItemDamage()];
 		else
 			name = itemNames[0];
 
 		return getItemName() + "." + name;
 	}
 
 	public int getMetadata(int meta) {
 		return meta;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public void getSubItems(int par1, CreativeTabs tab, List list) {
 		for (int metaNumber = 0; metaNumber < getMetadataSize(); metaNumber++) {
 			list.add(new ItemStack(par1, 1, metaNumber));
 		}
 	}
 
 }
