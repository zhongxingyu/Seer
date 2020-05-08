 package ip.industrialProcessing.machines.plants.metalProcessing.extruder;
 
import buildcraft.core.gui.slots.SlotOutput;
 import ip.industrialProcessing.machines.TileEntityPoweredFluidWorkerMachine;
 import ip.industrialProcessing.machines.containers.ContainerFluidMachine;
 import ip.industrialProcessing.machines.containers.ContainerPoweredFluidWorkerMachine;
 import ip.industrialProcessing.slots.SlotBase;
 import ip.industrialProcessing.slots.SlotLiquid;
 import ip.industrialProcessing.slots.SlotLiquidOutput;
 import ip.industrialProcessing.utils.containers.ContainerUtils;
 import net.minecraft.entity.player.InventoryPlayer;
 
 public class ContainerExtruder   extends ContainerPoweredFluidWorkerMachine {
 
 	private TileEntityPoweredFluidWorkerMachine tileEntity;
 	private SlotOutput outputSlot;
 
 	public ContainerExtruder(InventoryPlayer inventoryPlayer, TileEntityPoweredFluidWorkerMachine tileEntity) {
 		super(inventoryPlayer, tileEntity);
 		this.tileEntity = tileEntity;
 
 		outputSlot = new SlotOutput(tileEntity, 0, 116, 36);
 		
 		addSlotToContainer(outputSlot);
 
 		ContainerUtils.BindPlayerInventory(inventoryPlayer, this, 0);
 		
 		addTankToContainer(0);
         
         addWorkerToContainer(tileEntity.getWorker());
         addPowerToContainer(tileEntity.getMainPowerStorage());
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return 1;
 	}
 
 }
