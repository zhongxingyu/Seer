 package denoflionsx.minefactoryreloaded.modhelpers.forestry.leaves;
 
 import denoflionsx.minefactoryreloaded.modhelpers.forestry.trees.PlantableForestryTree;
 import denoflionsx.minefactoryreloaded.modhelpers.forestry.utils.ForestryUtils;
 import forestry.api.arboriculture.EnumGermlingType;
 import forestry.api.arboriculture.ITree;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import powercrystals.minefactoryreloaded.api.HarvestType;
 import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
 
 public class HarvestableLeaves implements IFactoryHarvestable {
 
     private int id;
 
     public HarvestableLeaves(int id) {
 	this.id = id;
     }
 
     @Override
     public int getPlantId() {
 	return this.id;
     }
 
     @Override
     public HarvestType getHarvestType() {
 	return HarvestType.TreeLeaf;
     }
 
     @Override
     public boolean breakBlock() {
 	return false;
     }
 
     @Override
     public boolean canBeHarvested(World world, Map<String, Boolean> harvesterSettings, int x, int y, int z) {
 	return true;
     }
 
     @Override
     public List<ItemStack> getDrops(World world, Random rand, Map<String, Boolean> harvesterSettings, int x, int y, int z) {
 	ArrayList<ItemStack> prod = new ArrayList();
 	TileEntity tile = world.getBlockTileEntity(x, y, z);
 	if (tile == null) {
 	    return prod;
 	}
 	if (PlantableForestryTree.getTree(tile) == null) {
 	    return prod;
 	}
 	// Add saplings
	float modifier = 1.0f;
	if (harvesterSettings.get("silkTouch") != null && harvesterSettings.get("silkTouch")) {
	    modifier = 100f;
	}
	ITree[] saplings = PlantableForestryTree.getTree(tile).getSaplings(world, x, y, z, modifier);
 	for (ITree sapling : saplings) {
 	    if (sapling != null) {
		prod.add(ForestryUtils.root.getMemberStack(sapling, EnumGermlingType.SAPLING.ordinal()));
 	    }
 	}
 	// Add fruits
 	if (ForestryUtils.getFruitBearer(tile).hasFruit()) {
 	    for (ItemStack stack : PlantableForestryTree.getTree(tile).produceStacks(world, x, y, z, this.getRipeTime(world, x, y, z))) {
		prod.add(stack);
 	    }
 	}
 	return prod;
     }
 
     private int getRipeTime(World world, int x, int y, int z) {
 	try {
 	    return (Integer) Class.forName("forestry.arboriculture.gadgets.TileLeaves").getMethod("getRipeningTime", new Class[0]).invoke(world.getBlockTileEntity(x, y, z), new Object[0]);
 	} catch (Throwable ex) {
 	    ex.printStackTrace();
 	}
 	return 0;
     }
 
     @Override
     public void preHarvest(World world, int x, int y, int z) {
     }
 
     @Override
     public void postHarvest(World world, int x, int y, int z) {
 	world.removeBlockTileEntity(x, y, z);
 	world.playAuxSFXAtEntity(null, 2001, x, y, z, world.getBlockId(x, y, z) + (world.getBlockMetadata(x, y, z) << 12));
 	world.setBlockToAir(x, y, z);
     }
 }
