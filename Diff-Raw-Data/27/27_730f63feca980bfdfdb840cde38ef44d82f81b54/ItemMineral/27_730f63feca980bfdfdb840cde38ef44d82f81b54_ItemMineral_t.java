 package slimevoid.tmf.minerals.items;
 
 import slimevoid.tmf.core.creativetabs.CreativeTabTMF;
 import slimevoid.tmf.core.lib.ConfigurationLib;
 import slimevoid.tmf.core.lib.SpriteLib;
 import slimevoid.tmf.fuel.IFuelHandlerTMF;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class ItemMineral extends Item implements IFuelHandlerTMF {
 	private int burnTime = 1600;
 	private int burnSpeed = 200;
 	private int burnWidth = 1;
 
 	public ItemMineral(int id) {
 		super(id - ConfigurationLib.ITEMID_OFFSET);
		this.setNoRepair();
 		this.setCreativeTab(CreativeTabTMF.tabTMF);
 	}
 
 	@Override
 	public boolean isFull3D() {
 		return true;
 	}
 	
 	@Override
 	public String getTextureFile() {
 		return SpriteLib.RESOURCE_SPRITE_PATH;
 	}
 	
 	public Item setBurnSettings(int time, int speed, int width) {
 		setBurnTime(time);
 		setBurnSpeed(speed);
 		setBurnWidth(width);
 		return this;
 	}
 
 	@Override
 	public int getBurnTime(ItemStack stack) {
 		return burnTime;
 	}
 	public Item setBurnTime(int burnTime) {
 		this.burnTime = burnTime;
 		return this;
 	}
 
 	@Override
 	public int getBurnSpeed(ItemStack stack) {
 		return burnSpeed;
 	}
 	public Item setBurnSpeed(int burnSpeed) {
 		this.burnSpeed = burnSpeed;
 		return this;
 	}
 
 	@Override
 	public int getBurnWidth(ItemStack stack) {
 		return burnWidth;
 	}
 	public Item setBurnWidth(int burnWidth) {
 		this.burnWidth = burnWidth;
 		return this;
 	}
 }
