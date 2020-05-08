 package halvors.mods.lightningrod;
 
 import java.util.List;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ICrafting;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 
 public class ContainerLightningRodGenerator extends Container {
 	private final TileEntityLightningRodGenerator tileEntity;
 	private final IInventory playerInventory;
 	
 	private boolean canLightningStrike;
 	private int energy;
 	
 	public ContainerLightningRodGenerator(IInventory playerInventory, TileEntityLightningRodGenerator tileEntity) {
 		this.tileEntity = tileEntity;
 		this.playerInventory = playerInventory;
 
 		addSlotToContainer(new Slot(tileEntity, 0, 65, 17));
 		
 		for (int inventoryRow = 0; inventoryRow < 3; inventoryRow++) {
 			for (int inventoryColumn = 0; inventoryColumn < 9; inventoryColumn++) {
 				addSlotToContainer(new Slot(playerInventory, inventoryColumn + inventoryRow * 9 + 9, 8 + inventoryColumn * 18, 84 + inventoryRow * 18));
 			}
 		}
 
 		for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
 			addSlotToContainer(new Slot(playerInventory, hotbarSlot, 8 + hotbarSlot * 18, 142));
 		}
 	}
 	
 	@Override
 	public void detectAndSendChanges() {
 		super.detectAndSendChanges();
 		
 		List<ICrafting> crafters = this.crafters;
 		
 		for (ICrafting crafter : crafters) {
			if (canLightningStrike != tileEntity.canLightningStrike()) {
 				crafter.sendProgressBarUpdate(this, 0, tileEntity.canLightningStrike ? 1 : 0);
 			}
 			
 			if (energy != tileEntity.getStored()) {
 				crafter.sendProgressBarUpdate(this, 1, tileEntity.getStored() & 65535);
 				crafter.sendProgressBarUpdate(this, 2, tileEntity.getStored() >>> 16);
             }
 		}
 		
 		this.canLightningStrike = tileEntity.canLightningStrike();
 		this.energy = tileEntity.getStored();
 	}
 
 	@Override
 	public void updateProgressBar(int i, int j) {
 		switch (i) {
             case 0:
             	tileEntity.setCanLightningStrike(j == 1);
             	break;
             	
             case 1:
                 tileEntity.setStored(tileEntity.getStored() & -65536 | j);
                 break;
 
             case 2:
             	tileEntity.setStored(tileEntity.getStored() & 65535 | j << 16);
             	break;
         }
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer entityPlayer) {
 		return tileEntity.isUseableByPlayer(entityPlayer);
 	}
 	
 	public TileEntityLightningRodGenerator getTileEntity() {
 		return tileEntity;
 	}
 }
