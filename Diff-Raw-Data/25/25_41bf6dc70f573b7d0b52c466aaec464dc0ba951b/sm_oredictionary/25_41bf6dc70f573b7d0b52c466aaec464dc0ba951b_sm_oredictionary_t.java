 package mods.storemore;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 public class sm_oredictionary extends StoreMore{
 
 	//oredictionary recipes
 	public static void registerOres()
 	{
 
 		OreDictionary.registerOre("packedObsidianI", new ItemStack(packedblocksIV,1,0));
 		OreDictionary.registerOre("packedObsidianII", new ItemStack(packedblocksIV,1,1));
 		OreDictionary.registerOre("packedObsidianIII", new ItemStack(packedblocksIV,1,2));
 		OreDictionary.registerOre("packedObsidianIV", new ItemStack(packedblocksIV,1,3));
 		OreDictionary.registerOre("packedGlowstoneV", new ItemStack(packedblocksIV,1,4));
 		OreDictionary.registerOre("packedGlowstoneVI", new ItemStack(packedblocksIV,1,5));
 		OreDictionary.registerOre("packedGlowstoneVII", new ItemStack(packedblocksIV,1,6));
 		OreDictionary.registerOre("packedGlowstoneVIII", new ItemStack(packedblocksIV,1,7));
 		OreDictionary.registerOre("packedNetherracklI", new ItemStack(packedblocksIV,1,8));
 		OreDictionary.registerOre("packedNetherracklII", new ItemStack(packedblocksIV,1,9));
 		OreDictionary.registerOre("packedNetherracklIII", new ItemStack(packedblocksIV,1,10));
 		OreDictionary.registerOre("packedNetherracklIV", new ItemStack(packedblocksIV,1,11));
 		OreDictionary.registerOre("packedIronI", new ItemStack(packedblocksV,1,0));
 		OreDictionary.registerOre("packedIronII", new ItemStack(packedblocksV,1,1));
 		OreDictionary.registerOre("packedIronIII", new ItemStack(packedblocksV,1,2));
 		OreDictionary.registerOre("packedIronIV", new ItemStack(packedblocksV,1,3));
 		OreDictionary.registerOre("packedGoldI", new ItemStack(packedblocksV,1,4));
 		OreDictionary.registerOre("packedGoldII", new ItemStack(packedblocksV,1,5));
 		OreDictionary.registerOre("packedGoldIII", new ItemStack(packedblocksV,1,6));
 		OreDictionary.registerOre("packedGoldIV", new ItemStack(packedblocksV,1,7));
 		OreDictionary.registerOre("packedDiamondlI", new ItemStack(packedblocksV,1,8));
 		OreDictionary.registerOre("packedDiamondlII", new ItemStack(packedblocksV,1,9));
 		OreDictionary.registerOre("packedDiamondlIII", new ItemStack(packedblocksV,1,10));
 		OreDictionary.registerOre("packedDiamondlIV", new ItemStack(packedblocksV,1,11));
 		OreDictionary.registerOre("packedEmeraldlI", new ItemStack(packedblocksV,1,12));
 		OreDictionary.registerOre("packedEmeraldlII", new ItemStack(packedblocksV,1,13));
 		OreDictionary.registerOre("packedEmeraldlIII", new ItemStack(packedblocksV,1,14));
 		OreDictionary.registerOre("packedEmeraldlIV", new ItemStack(packedblocksV,1,15));
 		OreDictionary.registerOre("packedSticksI", new ItemStack(packedblocksVI,1,0));
 		OreDictionary.registerOre("packedSticksII", new ItemStack(packedblocksVI,1,1));
 		OreDictionary.registerOre("packedSticksIII", new ItemStack(packedblocksVI,1,2));
 		OreDictionary.registerOre("packedSticksIV", new ItemStack(packedblocksVI,1,3));
 		OreDictionary.registerOre("packedTorchesI", new ItemStack(packedblocksVI,1,4));
 		OreDictionary.registerOre("packedTorchesII", new ItemStack(packedblocksVI,1,5));
 		OreDictionary.registerOre("packedTorchesIII", new ItemStack(packedblocksVI,1,6));
 		OreDictionary.registerOre("packedTorchesIV", new ItemStack(packedblocksVI,1,7));
 		OreDictionary.registerOre("packedGunpowderlI", new ItemStack(packedblocksVI,1,8));
 		OreDictionary.registerOre("packedGunpowderlII", new ItemStack(packedblocksVI,1,9));
 		OreDictionary.registerOre("packedGunpowderlIII", new ItemStack(packedblocksVI,1,10));
 		OreDictionary.registerOre("packedGunpowderlIV", new ItemStack(packedblocksVI,1,11));
 		OreDictionary.registerOre("packedFlintlI", new ItemStack(packedblocksVI,1,12));
 		OreDictionary.registerOre("packedFlintlII", new ItemStack(packedblocksVI,1,13));
 		OreDictionary.registerOre("packedFlintlIII", new ItemStack(packedblocksVI,1,14));
 		OreDictionary.registerOre("packedFlintlIV", new ItemStack(packedblocksVI,1,15));
 		OreDictionary.registerOre("packedWoodI", new ItemStack(packedblocksVII,1,0));
 		OreDictionary.registerOre("packedWoodII", new ItemStack(packedblocksVII,1,1));
 		OreDictionary.registerOre("packedWoodIII", new ItemStack(packedblocksVII,1,2));
 		OreDictionary.registerOre("packedWoodIV", new ItemStack(packedblocksVII,1,3));
 		OreDictionary.registerOre("packedPlanksI", new ItemStack(packedblocksVIII,1,0));
 		OreDictionary.registerOre("packedPlanksII", new ItemStack(packedblocksVIII,1,1));
 		OreDictionary.registerOre("packedPlanksIII", new ItemStack(packedblocksVIII,1,2));
 		OreDictionary.registerOre("packedPlanksIV", new ItemStack(packedblocksVIII,1,3));
 		OreDictionary.registerOre("packedRedstoneI", new ItemStack(packedblocksIX,1,0));
 		OreDictionary.registerOre("packedRedstoneII", new ItemStack(packedblocksIX,1,1));
 		OreDictionary.registerOre("packedRedstoneIII", new ItemStack(packedblocksIX,1,2));
 		OreDictionary.registerOre("packedRedstoneIV", new ItemStack(packedblocksIX,1,3));
 		OreDictionary.registerOre("packedQuartzI", new ItemStack(packedblocksIX,1,4));
 		OreDictionary.registerOre("packedQuartzII", new ItemStack(packedblocksIX,1,5));
 		OreDictionary.registerOre("packedQuartzIII", new ItemStack(packedblocksIX,1,6));
 		OreDictionary.registerOre("packedQuartzIV", new ItemStack(packedblocksIX,1,7));
 		OreDictionary.registerOre("packedSoulsandlI", new ItemStack(packedblocksIX,1,8));
 		OreDictionary.registerOre("packedSoulsandlII", new ItemStack(packedblocksIX,1,9));
 		OreDictionary.registerOre("packedSoulsandlIII", new ItemStack(packedblocksIX,1,10));
 		OreDictionary.registerOre("packedSoulsandlIV", new ItemStack(packedblocksIX,1,11));
 		OreDictionary.registerOre("packedClaylI", new ItemStack(packedblocksIX,1,12));
 		OreDictionary.registerOre("packedClaylII", new ItemStack(packedblocksIX,1,13));
 		OreDictionary.registerOre("packedClaylIII", new ItemStack(packedblocksIX,1,14));
 		OreDictionary.registerOre("packedClaylIV", new ItemStack(packedblocksIX,1,15));
 		OreDictionary.registerOre("packedCoalI", packedcoalI);
 		OreDictionary.registerOre("packedCoalII", packedcoalII);
 		OreDictionary.registerOre("packedCoalIII", packedcoalIII);
 		OreDictionary.registerOre("packedCoalIV", packedcoalIV);
 		OreDictionary.registerOre("packedCharcoalI", packedcharcoalI);
 		OreDictionary.registerOre("packedCharcoalII", packedcharcoalII);
 		OreDictionary.registerOre("packedCharcoalIII", packedcharcoalIII);
 		OreDictionary.registerOre("packedCharcoalIV", packedcharcoalIV);
 	}
 
 
 
 
 	public static void addOreRecipes()
 	{	 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVII,1,0), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "logWood",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVII,1,1), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "packedWoodI",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVII,1,2), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "packedWoodII",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVII,1,3), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "packedWoodIII",}));
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVIII,1,0), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "plankWood",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVIII,1,1), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "packedPlanksI",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVIII,1,2), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "packedPlanksII",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVIII,1,3), new Object[]{"XXX","XXX","XXX",Character.valueOf('X'), "packedPlanksIII",}));
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,4,0), new Object[]{"P","P",Character.valueOf('P'), "packedPlanksI",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,4,1), new Object[]{"P","P",Character.valueOf('P'), "packedPlanksII",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,4,2), new Object[]{"P","P",Character.valueOf('P'), "packedPlanksIII",}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,4,3), new Object[]{"P","P",Character.valueOf('P'), "packedPlanksIV",}));
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,4), new Object[]{"C","S",Character.valueOf('C'), "packedCoalI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,5), new Object[]{"C","S",Character.valueOf('C'), "packedCoalII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,6), new Object[]{"C","S",Character.valueOf('C'), "packedCoalIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,7), new Object[]{"C","S",Character.valueOf('C'), "packedCoalIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,4), new Object[]{"C","S",Character.valueOf('C'), "packedCharcoalI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,5), new Object[]{"C","S",Character.valueOf('C'), "packedCharcoalII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,6), new Object[]{"C","S",Character.valueOf('C'), "packedCharcoalIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.packedblocksVI,6,7), new Object[]{"C","S",Character.valueOf('C'), "packedCharcoalIV", Character.valueOf('S'), "packedSticksIV"}));
 
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodPickaxeI), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedPlanksI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodPickaxeII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedPlanksII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodPickaxeIII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedPlanksIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodPickaxeIV), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedPlanksIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodAxeI), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedPlanksI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodAxeII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedPlanksII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodAxeIII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedPlanksIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodAxeIV), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedPlanksIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodHoeI), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedPlanksI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodHoeII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedPlanksII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodHoeIII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedPlanksIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodHoeIV), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedPlanksIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodShovelI), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedPlanksI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodShovelII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedPlanksII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodShovelIII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedPlanksIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodShovelIV), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedPlanksIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodSwordI), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedPlanksI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodSwordII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedPlanksII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodSwordIII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedPlanksIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedWoodSwordIV), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedPlanksIV", Character.valueOf('S'), "packedSticksIV"}));
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStonePickaxeI), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedCobbleI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStonePickaxeII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedCobbleII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStonePickaxeIII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedCobbleIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStonePickaxeIV), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedCobbleIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneAxeI), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedCobbleI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneAxeII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedCobbleII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneAxeIII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedCobbleIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneAxeIV), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedCobbleIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneHoeI), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedCobbleI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneHoeII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedCobbleII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneHoeIII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedCobbleIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneHoeIV), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedCobbleIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneShovelI), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedCobbleI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneShovelII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedCobbleII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneShovelIII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedCobbleIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneShovelIV), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedCobbleIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneSwordI), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedCobbleI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneSwordII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedCobbleII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneSwordIII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedCobbleIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedStoneSwordIV), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedCobbleIV", Character.valueOf('S'), "packedSticksIV"}));
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronPickaxeI), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedIronI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronPickaxeII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedIronII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronPickaxeIII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedIronIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronPickaxeIV), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedIronIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronAxeI), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedIronI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronAxeII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedIronII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronAxeIII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedIronIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronAxeIV), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedIronIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronHoeI), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedIronI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronHoeII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedIronII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronHoeIII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedIronIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronHoeIV), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedIronIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronShovelI), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedIronI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronShovelII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedIronII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronShovelIII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedIronIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronShovelIV), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedIronIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronSwordI), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedIronI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronSwordII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedIronII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronSwordIII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedIronIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedIronSwordIV), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedIronIV", Character.valueOf('S'), "packedSticksIV"}));
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldPickaxeI), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedGoldI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldPickaxeII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedGoldII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldPickaxeIII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedGoldIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldPickaxeIV), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedGoldIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldAxeI), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedGoldI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldAxeII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedGoldII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldAxeIII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedGoldIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldAxeIV), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedGoldIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldHoeI), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedGoldI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldHoeII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedGoldII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldHoeIII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedGoldIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldHoeIV), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedGoldIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldShovelI), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedGoldI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldShovelII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedGoldII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldShovelIII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedGoldIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldShovelIV), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedGoldIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldSwordI), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedGoldI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldSwordII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedGoldII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldSwordIII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedGoldIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedGoldSwordIV), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedGoldIV", Character.valueOf('S'), "packedSticksIV"}));
 
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondPickaxeI), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedDiamondI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondPickaxeII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedDiamondII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondPickaxeIII), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedDiamondIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondPickaxeIV), new Object[]{"PPP"," S "," S ",Character.valueOf('P'), "packedDiamondIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondAxeI), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedDiamondI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondAxeII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedDiamondII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondAxeIII), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedDiamondIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondAxeIV), new Object[]{"PP ","PS "," S ",Character.valueOf('P'), "packedDiamondIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondHoeI), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedDiamondI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondHoeII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedDiamondII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondHoeIII), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedDiamondIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondHoeIV), new Object[]{"PP "," S "," S ",Character.valueOf('P'), "packedDiamondIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondShovelI), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedDiamondI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondShovelII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedDiamondII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondShovelIII), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedDiamondIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondShovelIV), new Object[]{" P "," S "," S ",Character.valueOf('P'), "packedDiamondIV", Character.valueOf('S'), "packedSticksIV"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondSwordI), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedDiamondI", Character.valueOf('S'), "packedSticksI"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondSwordII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedDiamondII", Character.valueOf('S'), "packedSticksII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondSwordIII), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedDiamondIII", Character.valueOf('S'), "packedSticksIII"}));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(StoreMore.hardenedDiamondSwordIV), new Object[]{" P "," P "," S ",Character.valueOf('P'), "packedDiamondIV", Character.valueOf('S'), "packedSticksIV"}));
 
 	}
 
 }
