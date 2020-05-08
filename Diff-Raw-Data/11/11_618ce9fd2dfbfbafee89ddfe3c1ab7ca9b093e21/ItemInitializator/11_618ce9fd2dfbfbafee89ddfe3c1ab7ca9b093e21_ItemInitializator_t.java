 package sammko.quantumCraft.items;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemSword;
 import net.minecraft.world.World;
 import net.minecraftforge.common.EnumHelper;
 import sammko.quantumCraft.core.QuantumCraft;
 import sammko.quantumCraft.core.QuantumCraftSettings;
 import sammko.quantumCraft.core.TabQuantumCraft;
 import sammko.quantumCraft.blocks.BlockEmpty;
 import sammko.quantumCraft.blocks.BlockQuantumOre;
 import sammko.quantumCraft.machine.BlockMachine;
 import sammko.quantumCraft.machine.TileEntityExtractor;
 import sammko.quantumCraft.machine.TileEntityMachine;
 import sammko.quantumCraft.machine.gui.GuiGenerator;
 import sammko.quantumCraft.render.RenderBlockEmpty;
 import sammko.quantumCraft.render.RenderBlockMachine;
 import sammko.quantumCraft.resources.BlockTextureMatrix;
 import sammko.quantumCraft.resources.ItemTextureMatrix;
 import cpw.mods.fml.client.registry.RenderingRegistry;
 import cpw.mods.fml.common.network.IGuiHandler;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ItemInitializator implements IGuiHandler{
 
 	public ItemInitializator() {
 		
 	}
 	
 		//Creative tabs
 		public static CreativeTabs tabQC;
 		//GUI IDs
 		public static int GuiGeneratorID = 1;
 		//Render IDs
 		public static int BlockEmptyRenderID;
 		public static int BlockMachineRenderID;
 		//Ores
 		public static BlockQuantumOre OrePositronium;
 		public static BlockQuantumOre OrePlutonium;
 		public static BlockQuantumOre OreRadium;
 		public static BlockQuantumOre OreGammatronium;
 		public static BlockQuantumOre OreNeutrinium;
 		public static BlockQuantumOre OreDepleted;
 		
 		//Blocks
 		public static BlockEmpty EmptyBlock;
 		//Machines
 		public static BlockMachine MachineBlock;
 		//Items
 		public static ItemPlutoniumIngot ItemIngotPlutonium;
 		public static ItemCrystal ItemPositroniumCrystal;
 		public static ItemCrystal ItemRadiumCrystal;
 		public static ItemCrystal ItemGammatroniumCrystal;
 		public static ItemCrystal ItemNeutriniumCrystal;
 		public static ItemCrystal ItemDepletedCrystal;
 		
 		public static ItemPickaxe ItemCrystalPickaxe;
 		public static ItemSword ItemCrystalSword;
 		public static EnumToolMaterial DEPLETEDCRYSTAL = EnumHelper.addToolMaterial("DEPLETEDCRYSTAL", 2, 500, 7.0F, 6, 10);
 		public static EnumToolMaterial DepletedCrystalSword = EnumHelper.addToolMaterial("DepletedCrystalSword", 4, 500, 15.0F, 500, 25);
 		
 		public static ItemEnergyPacket ItemEmptyEnergyPacket;
 		public static ItemEnergyPacket ItemPositroniumEnergyPacket;
 		public static ItemEnergyPacket ItemRadiumEnergyPacket;
 		public static ItemEnergyPacket ItemGammatroniumEnergyPacket;
 		public static ItemEnergyPacket ItemNeutriniumEnergyPacket;
 		//InitCode
 		public static void initOres()
 		{ //implemented my Creative Tab
 			OrePositronium = (BlockQuantumOre) new BlockQuantumOre(QuantumCraftSettings.OrePositroniumID, BlockTextureMatrix.getIndex(BlockTextureMatrix.OrePositronium)).setBlockName("orePositronium").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(tabQC);
 			OrePlutonium = (BlockQuantumOre) new BlockQuantumOre(QuantumCraftSettings.OrePlutoniumID, BlockTextureMatrix.getIndex(BlockTextureMatrix.OrePlutonium)).setBlockName("orePlutonium").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(tabQC);
 			OreRadium = (BlockQuantumOre) new BlockQuantumOre(QuantumCraftSettings.OreRadiumID, BlockTextureMatrix.getIndex(BlockTextureMatrix.OreRadium)).setBlockName("oreRadium").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(tabQC);
 			OreGammatronium = (BlockQuantumOre) new BlockQuantumOre(QuantumCraftSettings.OreGammatroniumID, BlockTextureMatrix.getIndex(BlockTextureMatrix.OreGammatronium)).setBlockName("oreGammatronium").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(tabQC);
 			OreNeutrinium = (BlockQuantumOre) new BlockQuantumOre(QuantumCraftSettings.OreNeutriniumID, BlockTextureMatrix.getIndex(BlockTextureMatrix.OreNeutrinium)).setBlockName("oreNeutrinium").setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(tabQC);
 			OreDepleted = (BlockQuantumOre) new BlockQuantumOre(QuantumCraftSettings.OreDepletedID, BlockTextureMatrix.getIndex(BlockTextureMatrix.OreDepleted)).setBlockName("OreDepleted").setHardness(5.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setCreativeTab(tabQC);
 			
 			LanguageRegistry.addName(OrePositronium, "Positronium Ore");
 			LanguageRegistry.addName(OrePlutonium, "Plutonium Ore");
 			LanguageRegistry.addName(OreRadium, "Radium Ore");
 			LanguageRegistry.addName(OreGammatronium, "Gammatronium Ore");
 			LanguageRegistry.addName(OreNeutrinium, "Neutrinium Ore");
 			LanguageRegistry.addName(OreDepleted, "Depleted Crystal Ore");
 		}
 		public static void initBlocks()
 		{
 			EmptyBlock = new BlockEmpty(QuantumCraftSettings.EmptyBlockID, BlockTextureMatrix.getIndex(BlockTextureMatrix.EBlockBackSingle), BlockEmptyRenderID);
 			MachineBlock = new BlockMachine(QuantumCraftSettings.MachineBlockID, BlockMachineRenderID);
 		}
 
 		public static void initItems()
 		{
 			ItemIngotPlutonium = new ItemPlutoniumIngot(QuantumCraftSettings.PlutoniumIngotID);
 			ItemPositroniumCrystal = new ItemCrystal(QuantumCraftSettings.CrystalPositroniumID);
 			ItemRadiumCrystal = new ItemCrystal(QuantumCraftSettings.CrystalRadiumID);
 			ItemGammatroniumCrystal = new ItemCrystal(QuantumCraftSettings.CrystalGammatroniumID);
 			ItemNeutriniumCrystal = new ItemCrystal(QuantumCraftSettings.CrystalNeutriniumID);
 			ItemDepletedCrystal = new ItemCrystal(QuantumCraftSettings.CrystalDepletedID);
 			
 			ItemEmptyEnergyPacket = new ItemEnergyPacket(QuantumCraftSettings.EmptyEnergyPacketID);
 			ItemPositroniumEnergyPacket = new ItemEnergyPacket(QuantumCraftSettings.PositroniumEnergyPacketID);
 			ItemRadiumEnergyPacket = new ItemEnergyPacket(QuantumCraftSettings.RadiumEnergyPacketID);
 			ItemGammatroniumEnergyPacket = new ItemEnergyPacket(QuantumCraftSettings.GammatroniumEnergyPacketID);
 			ItemNeutriniumEnergyPacket = new ItemEnergyPacket(QuantumCraftSettings.NeutriniumEnergyPacketID);
 			// also moved to my Tab
 			ItemCrystalPickaxe = (ItemPickaxe) new CrystalPickaxe(QuantumCraftSettings.CrystalPickaxeID, DEPLETEDCRYSTAL).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.CrystalPickaxe)).setItemName("CrystalPickaxe").setCreativeTab(tabQC);
 			ItemCrystalSword = (ItemSword) new CrystalSword(QuantumCraftSettings.CrystalSwordID, DepletedCrystalSword).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.CrystalSword)).setItemName("CrystalSword").setCreativeTab(tabQC);
 			
 			ItemIngotPlutonium.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.PlutoniumIngot)).setItemName("plutoniumIngot");
 			ItemPositroniumCrystal.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.PositroniumCrystal)).setItemName("positroniumCrystal");
 			ItemRadiumCrystal.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.RadiumCrystal)).setItemName("radiumCrystal");
 			ItemGammatroniumCrystal.setMaxStackSize(64).setCreativeTab(tabQC) .setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.GammatroniumCrystal)).setItemName("gammatroniumCrystal");
 			ItemNeutriniumCrystal.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.NeutriniumCrystal)).setItemName("neutriniumCrystal");
 			ItemDepletedCrystal.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.DepletedCrystal)).setItemName("depletedCrystal");
 			
 			ItemEmptyEnergyPacket.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.EmptyEPacket)).setItemName("emptyEP");
 			ItemPositroniumEnergyPacket.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.PositroniumEPacket)).setItemName("positroniumEP");
 			ItemRadiumEnergyPacket.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.RadiumEPacket)).setItemName("radiumEP");
 			ItemGammatroniumEnergyPacket.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.GammatroniumEPacket)).setItemName("gammatroniumEP");
 			ItemNeutriniumEnergyPacket.setMaxStackSize(64).setCreativeTab(tabQC).setIconIndex(ItemTextureMatrix.getIndex(ItemTextureMatrix.NeutriniumEPacket)).setItemName("neutroniumEP");
 			
 			LanguageRegistry.addName(ItemIngotPlutonium, "Plutonium Ingot");
 			LanguageRegistry.addName(ItemPositroniumCrystal, "Positronium Crystal");
 			LanguageRegistry.addName(ItemRadiumCrystal, "Radium Crystal");
 			LanguageRegistry.addName(ItemGammatroniumCrystal, "Gammatronium Crystal");
 			LanguageRegistry.addName(ItemNeutriniumCrystal, "Neutrinium Crystal");
 			LanguageRegistry.addName(ItemDepletedCrystal, "Depleted Crystal");
 			
 			LanguageRegistry.addName(ItemEmptyEnergyPacket, "Empty EPacket");
 			LanguageRegistry.addName(ItemPositroniumEnergyPacket, "Positronium EPacket");
 			LanguageRegistry.addName(ItemRadiumEnergyPacket, "Radium EPacket");
 			LanguageRegistry.addName(ItemGammatroniumEnergyPacket, "Gammatronium EPacket");
 			LanguageRegistry.addName(ItemNeutriniumEnergyPacket, "Neutrinium EPacket");
 			LanguageRegistry.addName(ItemCrystalPickaxe, "Crystal Pickaxe");
			LanguageRegistry.addName(ItemCrystalSword, "Crystal Sword");
 		}
 		public static void initTEntities()
 		{
 			GameRegistry.registerTileEntity(TileEntityMachine.class, "MachineTE");
 			GameRegistry.registerTileEntity(TileEntityExtractor.class, "ExtractorTE");
 		}
 		public static void initRenderers()
 		{
 			BlockEmptyRenderID = RenderingRegistry.getNextAvailableRenderId();
 			BlockMachineRenderID = RenderingRegistry.getNextAvailableRenderId();
 			RenderingRegistry.registerBlockHandler(new RenderBlockEmpty(BlockEmptyRenderID,0));
 			RenderingRegistry.registerBlockHandler(new RenderBlockMachine(BlockMachineRenderID));
 		}
 		public static void initCT()
 		{
 			
 			tabQC = new TabQuantumCraft();
 			
 		}
 		public static void initAll()
 		{
 			initCT();
 			initTEntities();
 			initRenderers();
 			initBlocks();
 			initItems();
 			initOres();
 		
 		}
 		@Override
 		public Object getServerGuiElement(int ID, EntityPlayer player,
 				World world, int x, int y, int z) {
 			
 			return null;
 		}
 		@Override
 		public Object getClientGuiElement(int ID, EntityPlayer player,
 				World world, int x, int y, int z) {
 			
 			return null;
 		}
 
 }
