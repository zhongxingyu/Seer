 package adanaran.mods.bfr.items;
 
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.util.Icon;
 
 /**
  * 
  * @author Adanaran
  * @author Demitreus
  */
 public abstract class ItemCookwareBase extends Item {
 
 	protected EnumToolMaterial enumMat;
 	protected Icon itemIconIron;
 	protected Icon itemIconStone;
 	protected Icon itemIconGold;
 	protected Icon itemIconDiamond;
 
 	public ItemCookwareBase(int id, EnumToolMaterial material) {
 		super(id);
 		enumMat = material;
 		this.setMaxDamage(enumMat.getMaxUses());
 	}
 
 	@Override
 	public Icon getIconFromDamage(int damage) {
 		Icon icon = itemIconIron;
 		switch (enumMat) {
 		case STONE:
 			icon = itemIconStone;
 			break;
 		case IRON:
 			icon = itemIconIron;
 			break;
 		case GOLD:
 			icon = itemIconGold;
 			break;
 		case EMERALD:
 			icon = itemIconDiamond;
 			break;
 		case WOOD:
 			break;
 		default:
 			break;
 		}
 		return icon;
 	}
 
 }
