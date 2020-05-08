 package adanaran.mods.bfr.items;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.Item;
 
 public class ItemCocoaPowder extends Item {
 
 	public ItemCocoaPowder(int par1) {
 		super(par1);
 	}
 
 	@Override
 	public void registerIcons(IconRegister iRegister) {
 		iRegister.registerIcon("bfr:itemCocoaPowder");
 	}
	
 }
