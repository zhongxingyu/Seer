 package sbfp.world;
 
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 
 public class ItemBlockOre extends ItemBlock{
 
 	public ItemBlockOre(int id){
 		super(id);
 		this.setHasSubtypes(true);
 		this.setMaxDamage(0);
 	}
 
 	@Override
 	public int getMetadata(int damage){
 		return damage;
 	}
 
 	@Override
 	public String getUnlocalizedName(ItemStack is){
		return BlockOre.names[is.getItemDamage()];
 	}
 }
