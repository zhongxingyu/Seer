 package hunternif.mc.rings.item;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
 
 public class FlyingRing extends PoweredRing {
 	public FlyingRing(int id) {
 		super(id);
 		MinecraftForge.EVENT_BUS.register(this);
 	}
 	
 	private int ticksFlying = 0;
 	private boolean hadThisItemLastTime = false;
 	@ForgeSubscribe
 	public void onPlayerUpdate(LivingUpdateEvent event) {
 		if (event.entityLiving instanceof EntityPlayer && !event.entity.worldObj.isRemote) {
 			EntityPlayer player = (EntityPlayer) event.entityLiving;
 			ItemStack stack = findThisItem(player.inventory);
 			if (stack != null) {
 				if (!hadThisItemLastTime) {
 					hadThisItemLastTime = true;
 					if (hasFuel(stack, player)) {
 						player.capabilities.allowFlying = true;
 						player.sendPlayerAbilities();
 					}
 				}
				if (!hasFuel(stack, player)) {
 					player.capabilities.allowFlying = false;
 					player.capabilities.isFlying = false;
 					player.sendPlayerAbilities();
 				} else if (!player.capabilities.allowFlying) {
 					player.capabilities.allowFlying = true;
 					player.sendPlayerAbilities();
 				}
 				if (player.capabilities.isFlying) {
 					ticksFlying++;
					if (ticksFlying % 10 == 0 && !player.capabilities.isCreativeMode) { // 20 ticks * 5 seconds
 						consumeFuel(stack, player);
 					}
 				} else {
 					ticksFlying = 0;
 				}
 			} else {
 				if (hadThisItemLastTime) {
					player.capabilities.allowFlying = false;
					player.capabilities.isFlying = false;
					player.sendPlayerAbilities();
 					hadThisItemLastTime = false;
 				}
 			}
 		}
 	}
 	
 	protected ItemStack findThisItem(IInventory inventory) {
 		for (int i = 0; i < inventory.getSizeInventory(); i++) {
 			ItemStack stack = inventory.getStackInSlot(i);
 			if (stack != null && stack.getItem() == this) {
 				return stack;
 			}
 		}
 		return null;
 	}
 }
