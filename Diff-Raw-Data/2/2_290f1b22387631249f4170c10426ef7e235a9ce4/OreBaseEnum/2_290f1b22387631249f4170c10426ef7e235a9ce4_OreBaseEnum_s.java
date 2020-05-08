 package shadow.mods.metallurgy.base;
 
 import shadow.mods.metallurgy.IMetalSetEnum;
 import shadow.mods.metallurgy.MetallurgyEnumToolMaterial;
 
 public class OreBaseEnum implements IMetalSetEnum{
 
 	public static final int numMetals = 3;
 	public static final String[] names = {"Copper", "Tin", "Manganese"};
 	private final int[] expValues = {1, 1, 3};
 	private final int[] harvestLevels = {2, 2, 3};
	private final int[] pickLevels = {1, 0, 0};
 	private final int[] dungeonLootChances = {150, 130, 65};
 	private final int[] dungeonLootAmounts = {6, 4, 2};
 
 	public static int[] defaultVeinCount = {12, 10, 4};
 	public static int[] defaultOreCount = {6, 6, 4};
 	public static int[] defaultOreHeight = {128, 128, 128};
 	
 	public static int[] numRails = {4, 0, 0};
 	
 	@Override
 	public int numMetals() {
 		return numMetals;
 	}
 
 	@Override
 	public int startID() {
 		return BaseConfig.ItemStartID;
 	}
 
 	@Override
 	public String name(int i) {
 		return names[i];
 	}
 
 	@Override
 	public int expValue(int i) {
 		return expValues[i];
 	}
 
 	@Override
 	public int oreHarvestLevel(int i) {
 		return harvestLevels[i];
 	}
 
 	@Override
 	public int brickHarvestLevel(int i) {
 		return harvestLevels[i];
 	}
 
 	@Override
 	public int pickLevel(int i) {
 		return pickLevels[i];
 	}
 
 	@Override
 	public String image() {
 		return "/shadow/MetallurgyBaseMetals.png";
 	}
 
 	@Override
 	public boolean isAlloy() {
 		return false;
 	}
 
 	@Override
 	public int veinCount(int i) {
 		return BaseConfig.VeinCount[i];
 	}
 
 	@Override
 	public int oreCount(int i) {
 		return BaseConfig.OreCount[i];
 	}
 
 	@Override
 	public int oreHeight(int i) {
 		return BaseConfig.OreHeight[i];
 	}
 
 	@Override
 	public int oreID() {
 		return BaseConfig.baseMetalsVeinID;
 	}
 
 	@Override
 	public int brickID() {
 		return BaseConfig.baseMetalsBrickID;
 	}
 
 	@Override
 	public String getSetName() {
 		return "BaseMetal";
 	}
 
 	@Override
 	public MetallurgyEnumToolMaterial toolEnum(int i) {
 		switch(i)
 		{
 			case(0):
 				return MetallurgyEnumToolMaterial.Copper;
 		}
 		return MetallurgyEnumToolMaterial.Copper;
 	}
 
 	@Override
 	public boolean isCatalyst(int i) {
 		if(i == 1 || i == 2)
 			return true;
 		return false;
 	}
 
 	@Override
 	public int dungeonLootChance(int i) {
 		return dungeonLootChances[i];
 	}
 
 	@Override
 	public int dungeonLootAmount(int i) {
 		return dungeonLootAmounts[i];
 	}
 
 	@Override
 	public int numRails(int i) {
 		return numRails[i];
 	}
 
 	@Override
 	public int getDimension()
 	{
 		return 0;
 	}
 
 }
