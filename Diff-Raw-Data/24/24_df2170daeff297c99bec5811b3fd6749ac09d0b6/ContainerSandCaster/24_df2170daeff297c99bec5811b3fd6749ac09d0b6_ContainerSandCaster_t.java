 package ip.industrialProcessing.machines.plants.metalProcessing.sandCaster;
 
 import ip.industrialProcessing.machines.TileEntityPoweredFluidWorkerMachine;
 import ip.industrialProcessing.machines.containers.ContainerPoweredFluidWorkerMachine;
 import ip.industrialProcessing.slots.SlotBase;
 import ip.industrialProcessing.utils.containers.ContainerUtils;
 import net.minecraft.entity.player.InventoryPlayer;
import ip.industrialProcessing.slots.SlotOutput;
 
 public class ContainerSandCaster   extends ContainerPoweredFluidWorkerMachine {
 
 	private TileEntityPoweredFluidWorkerMachine tileEntity;
 	private SlotBase inputSlot;
 	private SlotOutput outputSlot;
 
 	public ContainerSandCaster(InventoryPlayer inventoryPlayer, TileEntityPoweredFluidWorkerMachine tileEntity) {
 		super(inventoryPlayer, tileEntity);
 		this.tileEntity = tileEntity;
 
 		inputSlot = new SlotBase(tileEntity, 0, 44, 36);
 		outputSlot = new SlotOutput(tileEntity, 1, 116, 36);
 		
 		addSlotToContainer(inputSlot);
 		addSlotToContainer(outputSlot);
 
 		ContainerUtils.BindPlayerInventory(inventoryPlayer, this, 0);
 		
 		addTankToContainer(0);
         
         addWorkerToContainer(tileEntity.getWorker());
         addPowerToContainer(tileEntity.getMainPowerStorage());
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return 2;
 	}
 
 }
