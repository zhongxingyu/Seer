 package machinitech.common.block;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import machinitech.common.core.MachiniTechCore;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 
 public abstract class MachiniTechBlockContainer extends BlockContainer {
 	private static int BlockContainer_ID_Def;
 	private static int Num_Machines = 0;
 	public static void prepareBlocks() {
		BlockContainer_ID_Def = MachiniTechCore.config.get("Block ID", "FirstBlockContainer", 600).getInt();
 		MachiniTechCore.config.save();
 	}
 	protected MachiniTechBlockContainer() {
 		super(Num_Machines + BlockContainer_ID_Def, Material.circuits);
 		Num_Machines++;
 	}
 	public static int getNumMachines () {
 		return Num_Machines;
 	}
 }
