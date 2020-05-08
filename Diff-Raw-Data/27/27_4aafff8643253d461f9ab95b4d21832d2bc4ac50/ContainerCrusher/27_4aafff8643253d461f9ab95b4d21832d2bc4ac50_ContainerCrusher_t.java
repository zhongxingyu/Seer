 package ip.industrialProcessing.machines.crusher;
 
 import ip.industrialProcessing.machines.ContainerMachine;
 import ip.industrialProcessing.utils.containers.ContainerUtils;
 import ip.industrialProcessing.utils.containers.IContainerAdd;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
public class ContainerCrusher extends ContainerMachine {
 
	
 	protected TileEntityCrusher tileEntityCrusher;
 	
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		return tileEntityCrusher.isUseableByPlayer(entityplayer);
 	}
 	
 	public ContainerCrusher(InventoryPlayer inventoryPlayer, TileEntityCrusher tileEntityCrusher) {
 		super(inventoryPlayer, tileEntityCrusher);
 		this.tileEntityCrusher = tileEntityCrusher;
 		
        addSlotToContainer(new Slot(tileEntityCrusher, 0, 8, 16));
        addSlotToContainer(new Slot(tileEntityCrusher, 1, 8, 48));	
 	}
 }
