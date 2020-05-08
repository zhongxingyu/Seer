 package ml.boxes.data;
 
 import ml.boxes.inventory.ContainerBox;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 
 public interface IBoxContainer {
 
 	public void saveData();
 	
 	public void boxOpen();
 	
 	public void boxClose();
 	
 	public Box getBox();
 	
 	public void ejectItem(ItemStack is);
 	
 	public boolean slotPreClick(ContainerBox cb, int slotNum, int mouseBtn, int action,
 			EntityPlayer par4EntityPlayer);
 	
	public boolean isUseableByPlayer(EntityPlayer epl);
 }
