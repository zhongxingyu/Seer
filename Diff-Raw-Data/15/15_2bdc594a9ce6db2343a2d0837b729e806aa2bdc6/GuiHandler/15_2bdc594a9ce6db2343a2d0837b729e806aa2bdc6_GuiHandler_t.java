 package ip.industrialProcessing;
 
 import ip.industrialProcessing.gui.IGuiLayout;
 import ip.industrialProcessing.gui.IGuiLayoutMultiblock;
 import ip.industrialProcessing.gui.container.ContainerIP;
 import ip.industrialProcessing.gui.guiContainer.GuiContainerIP;
 import ip.industrialProcessing.gui3.generating.IGuiBlock;
 import ip.industrialProcessing.gui3.generating.IGuiMultiblock;
 import ip.industrialProcessing.gui3.generating.LayoutGuiBuilder;
 import ip.industrialProcessing.gui3.guide.GuideScreen;
 import ip.industrialProcessing.machines.plants.oilRefinary.AlkylationUnit.ContainerAlkylationUnit;
 import ip.industrialProcessing.machines.plants.oilRefinary.AlkylationUnit.GuiContainerAlkylationUnit;
 import ip.industrialProcessing.machines.plants.oilRefinary.AlkylationUnit.TileEntityAlkylationUnit;
 import ip.industrialProcessing.machines.plants.oilRefinary.asphaltBlower.ContainerAsphaltBlower;
 import ip.industrialProcessing.machines.plants.oilRefinary.asphaltBlower.GuiContainerAsphaltBlower;
 import ip.industrialProcessing.machines.plants.oilRefinary.asphaltBlower.TileEntityAsphaltBlower;
 import ip.industrialProcessing.machines.plants.oilRefinary.gasProcessor.ContainerGasProcessor;
 import ip.industrialProcessing.machines.plants.oilRefinary.gasProcessor.GuiContainerGasProcessor;
 import ip.industrialProcessing.machines.plants.oilRefinary.gasProcessor.TileEntityGasProcessor;
 import ip.industrialProcessing.machines.plants.oilRefinary.isomerizationReactor.ContainerIsomerizationReactor;
 import ip.industrialProcessing.machines.plants.oilRefinary.isomerizationReactor.GuiContainerIsomerizationReactor;
 import ip.industrialProcessing.machines.plants.oilRefinary.isomerizationReactor.TileEntityIsomerizationReactor;
 import ip.industrialProcessing.machines.plants.oilRefinary.sourWaterStripper.ContainerSourWaterStripper;
 import ip.industrialProcessing.machines.plants.oilRefinary.sourWaterStripper.GuiContainerSourWaterStripper;
 import ip.industrialProcessing.machines.plants.oilRefinary.sourWaterStripper.TileEntitySourWaterStripper;
 import ip.industrialProcessing.machines.plants.waste.exhaust.coolingUnit.ContainerCoolingUnit;
 import ip.industrialProcessing.machines.plants.waste.exhaust.coolingUnit.GuiContainerCoolingUnit;
 import ip.industrialProcessing.machines.plants.waste.exhaust.coolingUnit.TileEntityCoolingUnit;
 import ip.industrialProcessing.multiblock.core.block.blastFurnace.ContainerMultiblockBlastFurnace;
 import ip.industrialProcessing.multiblock.core.block.blastFurnace.GuiContainerMultiblockBlastFurnace;
 import ip.industrialProcessing.multiblock.core.block.blastFurnace.TEmultiblockBlastFurnace;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.CatalyticReformer.ContainerCatalyticReformer;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.CatalyticReformer.GuiContainerCatalyticReformer;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.CatalyticReformer.TileEntityCatalyticReformer;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.amineTreater.ContainerAmineTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.amineTreater.GuiContainerAmineTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.amineTreater.TileEntityAmineTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.atmosphericDestilationTower.ContainerAtmosphericDestilationTower;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.atmosphericDestilationTower.GuiContainerAtmosphericDestilationTower;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.atmosphericDestilationTower.TileEntityAtmosphericDestilationTower;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.clausSulfurPlant.ContainerClausSulfurPlant;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.clausSulfurPlant.GuiContainerClausSulfurPlant;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.clausSulfurPlant.TileEntityClausSulfurPlant;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.delayedCoker.ContainerDelayedCoker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.delayedCoker.GuiContainerDelayedCoker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.delayedCoker.TileEntityDelayedCoker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.fluidCatalyticCracker.ContainerFluidCatalyticCracker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.fluidCatalyticCracker.GuiContainerFluidCatalyticCracker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.fluidCatalyticCracker.TileEntityFluidCatalyticCracker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.hydroCracker.ContainerHydroCracker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.hydroCracker.GuiContainerHydroCracker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.hydroCracker.TileEntityHydroCracker;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.hydroTreater.ContainerHydroTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.hydroTreater.GuiContainerHydroTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.hydroTreater.TileEntityHydroTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.meroxTreater.ContainerMeroxTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.meroxTreater.GuiContainerMeroxTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.meroxTreater.TileEntityMeroxTreater;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.vacuumDestilationTower.ContainerVacuumDestilationTower;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.vacuumDestilationTower.GuiContainerVacuumDestilationTower;
 import ip.industrialProcessing.multiblock.core.block.plants.oilRefinary.vacuumDestilationTower.TileEntityVacuumDestilationTower;
 import ip.industrialProcessing.multiblock.core.block.plants.storage.container.ContainerContainer;
 import ip.industrialProcessing.multiblock.core.block.plants.storage.container.GuiContainerContainer;
 import ip.industrialProcessing.multiblock.core.block.plants.storage.container.TileEntityContainer;
 import ip.industrialProcessing.multiblock.dummy.block.toggleButton.GUImultiblockToggleButton;
 import ip.industrialProcessing.multiblock.dummy.block.toggleButton.TEmultiblockToggleButton;
 import ip.industrialProcessing.power.plants.GuiContainerTurbine;
 import ip.industrialProcessing.power.plants.TileEntityTurbine;
import ip.industrialProcessing.subMod.blackSmith.plant.anvil.ContainerAnvil;
import ip.industrialProcessing.subMod.blackSmith.plant.anvil.GuiContainerAnvil;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import cpw.mods.fml.common.network.IGuiHandler;
 
 public class GuiHandler implements IGuiHandler {
 
     public static final int GUIDE_ID = 1;
     public static final int ANVIL_ID = 2;
     public static final int SOLDERINGIRON_ID = 3;
 
     @Override
     public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
 
         TileEntity entity = world.getBlockTileEntity(x, y, z);
         if (ID == 0) {
             if (entity.getBlockType() != null && (entity.getBlockType() instanceof IGuiLayout || entity.getBlockType() instanceof IGuiLayoutMultiblock)) {
                 return new ContainerIP(player, entity);
             }
 
             if (entity.getBlockType() != null && (entity.getBlockType() instanceof IGuiBlock || entity.getBlockType() instanceof IGuiMultiblock)) {
                 return LayoutGuiBuilder.createContainer(player.inventory, entity);
             }
 
             if (entity instanceof TileEntitySourWaterStripper)
                 return new ContainerSourWaterStripper(player.inventory, (TileEntitySourWaterStripper) entity);
             if (entity instanceof TileEntityAlkylationUnit)
                 return new ContainerAlkylationUnit(player.inventory, (TileEntityAlkylationUnit) entity);
             if (entity instanceof TileEntityAsphaltBlower)
                 return new ContainerAsphaltBlower(player.inventory, (TileEntityAsphaltBlower) entity);
             if (entity instanceof TileEntityGasProcessor)
                 return new ContainerGasProcessor(player.inventory, (TileEntityGasProcessor) entity);
             if (entity instanceof TileEntityIsomerizationReactor)
                 return new ContainerIsomerizationReactor(player.inventory, (TileEntityIsomerizationReactor) entity);
             if (entity instanceof TileEntityAtmosphericDestilationTower)
                 return new ContainerAtmosphericDestilationTower(player.inventory, (TileEntityAtmosphericDestilationTower) entity);
             if (entity instanceof TileEntityAmineTreater)
                 return new ContainerAmineTreater(player.inventory, (TileEntityAmineTreater) entity);
             if (entity instanceof TileEntityCatalyticReformer)
                 return new ContainerCatalyticReformer(player.inventory, (TileEntityCatalyticReformer) entity);
             if (entity instanceof TileEntityClausSulfurPlant)
                 return new ContainerClausSulfurPlant(player.inventory, (TileEntityClausSulfurPlant) entity);
             if (entity instanceof TileEntityDelayedCoker)
                 return new ContainerDelayedCoker(player.inventory, (TileEntityDelayedCoker) entity);
             if (entity instanceof TileEntityFluidCatalyticCracker)
                 return new ContainerFluidCatalyticCracker(player.inventory, (TileEntityFluidCatalyticCracker) entity);
             if (entity instanceof TileEntityHydroCracker)
                 return new ContainerHydroCracker(player.inventory, (TileEntityHydroCracker) entity);
             if (entity instanceof TileEntityHydroTreater)
                 return new ContainerHydroTreater(player.inventory, (TileEntityHydroTreater) entity);
             if (entity instanceof TileEntityMeroxTreater)
                 return new ContainerMeroxTreater(player.inventory, (TileEntityMeroxTreater) entity);
             if (entity instanceof TileEntityVacuumDestilationTower)
                 return new ContainerVacuumDestilationTower(player.inventory, (TileEntityVacuumDestilationTower) entity);
 
             if (entity instanceof TEmultiblockBlastFurnace)
                 return new ContainerMultiblockBlastFurnace(player.inventory, (TEmultiblockBlastFurnace) entity);
 
             if (entity instanceof TileEntityCoolingUnit)
                 return new ContainerCoolingUnit(player.inventory, (TileEntityCoolingUnit) entity);
             if (entity instanceof TileEntityContainer)
                 return new ContainerContainer(player.inventory, (TileEntityContainer) entity);
         } else if (ID == GUIDE_ID) {
             return null;
         } else if (ID == ANVIL_ID) {
             return new ContainerAnvil(player.inventory, world, x, y, z);
         }
         return null;
     }
 
     @Override
     public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
 
         TileEntity entity = world.getBlockTileEntity(x, y, z);
         if (ID == 0) {
             if (entity.getBlockType() != null && (entity.getBlockType() instanceof IGuiLayout || entity.getBlockType() instanceof IGuiLayoutMultiblock)) {
                 return new GuiContainerIP(player, entity, new ContainerIP(player, entity));
             }
             if (entity.getBlockType() != null && (entity.getBlockType() instanceof IGuiBlock || entity.getBlockType() instanceof IGuiMultiblock)) {
                 return LayoutGuiBuilder.createGuiContainer(player.inventory, entity);
             }
             if (entity instanceof TileEntityTurbine)
                 return new GuiContainerTurbine(player.inventory, (TileEntityTurbine) entity);
             if (entity instanceof TEmultiblockToggleButton)
                 return new GUImultiblockToggleButton((TEmultiblockToggleButton) entity, player);
             if (entity instanceof TileEntitySourWaterStripper)
                 return new GuiContainerSourWaterStripper(player.inventory, (TileEntitySourWaterStripper) entity);
             if (entity instanceof TileEntityAlkylationUnit)
                 return new GuiContainerAlkylationUnit(player.inventory, (TileEntityAlkylationUnit) entity);
             if (entity instanceof TileEntityAsphaltBlower)
                 return new GuiContainerAsphaltBlower(player.inventory, (TileEntityAsphaltBlower) entity);
             if (entity instanceof TileEntityGasProcessor)
                 return new GuiContainerGasProcessor(player.inventory, (TileEntityGasProcessor) entity);
             if (entity instanceof TileEntityIsomerizationReactor)
                 return new GuiContainerIsomerizationReactor(player.inventory, (TileEntityIsomerizationReactor) entity);
             if (entity instanceof TileEntityAtmosphericDestilationTower)
                 return new GuiContainerAtmosphericDestilationTower(player.inventory, (TileEntityAtmosphericDestilationTower) entity);
             if (entity instanceof TileEntityAmineTreater)
                 return new GuiContainerAmineTreater(player.inventory, (TileEntityAmineTreater) entity);
             if (entity instanceof TileEntityCatalyticReformer)
                 return new GuiContainerCatalyticReformer(player.inventory, (TileEntityCatalyticReformer) entity);
             if (entity instanceof TileEntityClausSulfurPlant)
                 return new GuiContainerClausSulfurPlant(player.inventory, (TileEntityClausSulfurPlant) entity);
             if (entity instanceof TileEntityDelayedCoker)
                 return new GuiContainerDelayedCoker(player.inventory, (TileEntityDelayedCoker) entity);
             if (entity instanceof TileEntityFluidCatalyticCracker)
                 return new GuiContainerFluidCatalyticCracker(player.inventory, (TileEntityFluidCatalyticCracker) entity);
             if (entity instanceof TileEntityHydroCracker)
                 return new GuiContainerHydroCracker(player.inventory, (TileEntityHydroCracker) entity);
             if (entity instanceof TileEntityHydroTreater)
                 return new GuiContainerHydroTreater(player.inventory, (TileEntityHydroTreater) entity);
             if (entity instanceof TileEntityMeroxTreater)
                 return new GuiContainerMeroxTreater(player.inventory, (TileEntityMeroxTreater) entity);
             if (entity instanceof TileEntityVacuumDestilationTower)
                 return new GuiContainerVacuumDestilationTower(player.inventory, (TileEntityVacuumDestilationTower) entity);
 
             if (entity instanceof TEmultiblockBlastFurnace)
                 return new GuiContainerMultiblockBlastFurnace(player.inventory, (TEmultiblockBlastFurnace) entity);
             if (entity instanceof TileEntityCoolingUnit)
                 return new GuiContainerCoolingUnit(player.inventory, (TileEntityCoolingUnit) entity);
             if (entity instanceof TileEntityContainer)
                 return new GuiContainerContainer(player.inventory, (TileEntityContainer) entity);
         } else if (ID == GUIDE_ID) {
             return new GuideScreen(player);
             // return new GuiGuide(player);
         } else if (ID == ANVIL_ID) {
             return new GuiContainerAnvil(player.inventory, world, x, y, z);
         }
         return null;
     }
 }
