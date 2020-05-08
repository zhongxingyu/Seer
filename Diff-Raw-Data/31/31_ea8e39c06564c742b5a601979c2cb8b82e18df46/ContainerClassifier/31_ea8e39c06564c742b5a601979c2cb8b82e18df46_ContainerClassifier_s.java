 package ip.industrialProcessing.machines.classifier;
 
import ip.industrialProcessing.machines.containers.ContainerFluidMachine;
 import ip.industrialProcessing.slots.SlotBase;
 import ip.industrialProcessing.slots.SlotLiquid;
 import ip.industrialProcessing.slots.SlotLiquidOutput;
 import ip.industrialProcessing.slots.SlotOutput;
 import ip.industrialProcessing.utils.containers.ContainerUtils;
 import net.minecraft.entity.player.InventoryPlayer;
 
public class ContainerClassifier extends ContainerFluidMachine {
 
 	private SlotBase inputSlot;
 	private SlotLiquid inputWaterFullInput;
 	private SlotLiquidOutput inputWaterEmptyOutput;
 	private SlotLiquid inputMixedEmptyInput;
 	private SlotLiquidOutput inputMixedFullOutput;
 	private SlotOutput outputSlot;
 
 	public ContainerClassifier(InventoryPlayer inventoryPlayer, TileEntityClassifier tileEntity) {
 		super(inventoryPlayer, tileEntity);
 
 		inputSlot = new SlotBase(tileEntity, 0, 44, 35);
 		outputSlot = new SlotOutput(tileEntity, 1, 116, 35);
 
 		inputWaterFullInput = new SlotLiquid(tileEntity, 2, 8, 19);
 		inputWaterEmptyOutput = new SlotLiquidOutput(tileEntity, 3, 8, 53);
 
 		inputMixedEmptyInput = new SlotLiquid(tileEntity, 4, 152, 19);
 		inputMixedFullOutput = new SlotLiquidOutput(tileEntity, 5, 152, 53);
 
 		addSlotToContainer(inputSlot);
 		addSlotToContainer(outputSlot);
 		addSlotToContainer(inputWaterFullInput);
 		addSlotToContainer(inputWaterEmptyOutput);
 		addSlotToContainer(inputMixedEmptyInput);
 		addSlotToContainer(inputMixedFullOutput);
 
 		ContainerUtils.BindPlayerInventory(inventoryPlayer, this, 0);
 		
 		addTankToContainer(0);
 		addTankToContainer(1);
 	}
 
 }
