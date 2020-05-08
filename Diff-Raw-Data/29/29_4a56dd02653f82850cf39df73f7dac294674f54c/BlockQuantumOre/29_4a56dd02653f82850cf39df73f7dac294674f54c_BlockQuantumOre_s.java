 package sammko.quantumCraft.blocks;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.item.Item;
 import cpw.mods.fml.common.registry.GameRegistry;
 import sammko.quantumCraft.core.QuantumCraftSettings;
 public class BlockQuantumOre extends BlockOre {
 
 	public BlockQuantumOre(int par1, int par2) {
 		super(par1, par2);
 		this.setTextureFile(QuantumCraftSettings.BLOCK_PNG);
 		GameRegistry.registerBlock(this);
 	}
 	
 	//Yay, crystal now drop 3 - 6 crystals of the type
 
 	@Override
 	public int idDropped(int par1, Random par2Random, int par3)
     {
 		if (this.blockID == QuantumCraftSettings.OrePositroniumID) return QuantumCraftSettings.CrystalPositroniumID+256; else
 		if (this.blockID == QuantumCraftSettings.OreRadiumID) return QuantumCraftSettings.CrystalRadiumID+256; else
 		if (this.blockID == QuantumCraftSettings.OreGammatroniumID) return QuantumCraftSettings.CrystalGammatroniumID+256; else
		if (this.blockID == QuantumCraftSettings.OreDepletedID) return QuantumCraftSettings.OreDepletedID+256; else
 		if (this.blockID == QuantumCraftSettings.OreNeutriniumID) return QuantumCraftSettings.CrystalNeutriniumID+256; else
 		if (this.blockID == QuantumCraftSettings.OrePlutoniumID) return QuantumCraftSettings.OrePlutoniumID+256; else
 			return -1;
 	
     }
 
 	public int quantityDropped(Random par1Random)
     {
         if (this.blockID == QuantumCraftSettings.OrePlutoniumID) return 1; else
         	return 3+par1Random.nextInt(3);
     }
 }
