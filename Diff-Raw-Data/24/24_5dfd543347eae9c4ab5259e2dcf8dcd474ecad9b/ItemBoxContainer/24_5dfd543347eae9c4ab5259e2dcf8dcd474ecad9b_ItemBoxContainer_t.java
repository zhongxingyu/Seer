 package ml.boxes.data;
 
 import ml.boxes.inventory.ContainerBox;
 import ml.boxes.item.ItemBox;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 
 public class ItemBoxContainer implements IBoxContainer {
 
 	public final ItemStack stack;
 	private final Box data;
 	
 	public ItemBoxContainer(ItemStack stack) {
 		this.stack = stack;
 		data = new Box(ItemBox.getBoxNBT(stack), this);
 	}
 	
 	@Override
 	public void saveData() {
 		ItemBox.saveBoxData(stack, getBox());
 	}
 
 	@Override
 	public void boxOpen() {}
 
 	@Override
 	public void boxClose() {}
 
 	@Override
 	public Box getBox() {
 		return data;
 	}
 
 	@Override
 	public void ejectItem(ItemStack is) {} //Should never be called
 
 	@Override
 	public boolean slotPreClick(ContainerBox cb, int slotNum, int mouseBtn, int action,
 			EntityPlayer par4EntityPlayer) {
 		
 		if (slotNum>=0 && cb.getSlot(slotNum).inventory == par4EntityPlayer.inventory && cb.getSlot(slotNum).getSlotIndex() == par4EntityPlayer.inventory.currentItem) { //((ItemIBox)box).stack == getSlot(slotNum).getStack()){
 			return false;
 		}
 		
 		if (action == 2 && mouseBtn == par4EntityPlayer.inventory.currentItem)
 			return false;
 		
 		return true;
 	}
 	
 	@Override
	public boolean boxUseableByPlayer(EntityPlayer epl) {
 		return data.isUseableByPlayer(epl);
 	}
 
 }
