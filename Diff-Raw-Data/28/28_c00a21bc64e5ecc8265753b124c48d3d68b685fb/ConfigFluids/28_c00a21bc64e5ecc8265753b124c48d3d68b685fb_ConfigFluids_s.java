 package ip.industrialProcessing.config;
 
 import ip.industrialProcessing.IndustrialProcessing;
 import ip.industrialProcessing.api.config.ConfigCategories;
 import ip.industrialProcessing.fluids.ContainerFluid;
 import ip.industrialProcessing.items.ItemIPBucket;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.fluids.Fluid;
 import net.minecraftforge.fluids.FluidContainerRegistry;
 import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
 import net.minecraftforge.fluids.FluidRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class ConfigFluids {
 
 	private static ConfigFluids instance = new ConfigFluids();
 
 	public static int blockFluidBaseDirtyWaterID() {
 		return getInstance().blockFluidBaseDirtyWaterID;
 	}
 
 	public static int blockFluidBaseExhaustGasID() {
 		return getInstance().blockFluidBaseExhaustGasID;
 	}
 
 	public static int blockFluidBasefiltyWaterID() {
 		return getInstance().blockFluidBasefiltyWaterID;
 	}
 
 	public static int blockFluidBaseGritSludgeID() {
 		return getInstance().blockFluidBaseGritSludgeID;
 	}
 
 	public static int blockFluidBaseHotSlagID() {
 		return getInstance().blockFluidBaseHotSlagID;
 	}
 
 	public static int blockFluidBaseHotSteelID() {
 		return getInstance().blockFluidBaseHotSteelID;
 	}
 
 	public static int blockFluidBaseMicrobialWaterID() {
 		return getInstance().blockFluidBaseMicrobialWaterID;
 	}
 
 	public static int blockFluidBaseMixedSludgeChromiteID() {
 		return getInstance().blockFluidBaseMixedSludgeChromiteID;
 	}
 
 	public static int blockFluidBaseMixedSludgeCopperID() {
 		return getInstance().blockFluidBaseMixedSludgeCopperID;
 	}
 
 	public static int blockFluidBaseMixedSludgeGalenaID() {
 		return getInstance().blockFluidBaseMixedSludgeGalenaID;
 	}
 
 	public static int blockFluidBaseMixedSludgeGoldID() {
 		return getInstance().blockFluidBaseMixedSludgeGoldID;
 	}
 
 	public static int blockFluidBaseMixedSludgeIronID() {
 		return getInstance().blockFluidBaseMixedSludgeIronID;
 	}
 
 	public static int blockFluidBaseMixedSludgeTaliaID() {
 		return getInstance().blockFluidBaseMixedSludgeTaliaID;
 	}
 
 	public static int blockFluidBaseMixedSludgeTinID() {
 		return getInstance().blockFluidBaseMixedSludgeTinID;
 	}
 
 	public static int blockFluidBaseOreSludgeChromiteID() {
 		return getInstance().blockFluidBaseOreSludgeChromiteID;
 	}
 
 	public static int blockFluidBaseOreSludgeCopperID() {
 		return getInstance().blockFluidBaseOreSludgeCopperID;
 	}
 
 	public static int blockFluidBaseOreSludgeGalenaID() {
 		return getInstance().blockFluidBaseOreSludgeGalenaID;
 	}
 
 	public static int blockFluidBaseOreSludgeGoldID() {
 		return getInstance().blockFluidBaseOreSludgeGoldID;
 	}
 
 	public static int blockFluidBaseOreSludgeIronID() {
 		return getInstance().blockFluidBaseOreSludgeIronID;
 	}
 
 	public static int blockFluidBaseOreSludgeTaliaID() {
 		return getInstance().blockFluidBaseOreSludgeTaliaID;
 	}
 
 	public static int blockFluidBaseOreSludgeTinID() {
 		return getInstance().blockFluidBaseOreSludgeTinID;
 	}
 
 	public static int blockFluidBasePigIronID() {
 		return getInstance().blockFluidBasePigIronID;
 	}
 
 	public static int blockFluidBaseResiduID() {
 		return getInstance().blockFluidBaseResiduID;
 	}
 
 	public static int blockFluidBaseSteamID() {
 		return getInstance().blockFluidBaseSteamID;
 	}
 
 	public static int blockFluidBaseUncleanWaterID() {
 		return getInstance().blockFluidBaseUncleanWaterID;
 	}
 
 	public static int BucketDirtyWaterID() {
 		return getInstance().BucketDirtyWaterID;
 	}
 
 	public static int BucketfiltyWaterID() {
 		return getInstance().BucketfiltyWaterID;
 	}
 
 	public static int BucketGritSludgeID() {
 		return getInstance().BucketGritSludgeID;
 	}
 
 	public static int BucketHotSlagID() {
 		return getInstance().BucketHotSlagID;
 	}
 
 	public static int BucketHotSteelID() {
 		return getInstance().BucketHotSteelID;
 	}
 
 	public static int BucketMicrobialWaterID() {
 		return getInstance().BucketMicrobialWaterID;
 	}
 
 	public static int bucketOreSludgeChromiteID() {
 		return getInstance().bucketOreSludgeChromiteID;
 	}
 
 	public static int BucketOreSludgeCopperID() {
 		return getInstance().BucketOreSludgeCopperID;
 	}
 
 	public static int bucketOreSludgeGalenaID() {
 		return getInstance().bucketOreSludgeGalenaID;
 	}
 
 	public static int bucketOreSludgeGoldID() {
 		return getInstance().bucketOreSludgeGoldID;
 	}
 
 	public static int BucketOreSludgeIronID() {
 		return getInstance().BucketOreSludgeIronID;
 	}
 
 	public static int bucketOreSludgeTaliaID() {
 		return getInstance().bucketOreSludgeTaliaID;
 	}
 
 	public static int BucketOreSludgeTinID() {
 		return getInstance().BucketOreSludgeTinID;
 	}
 
 	public static int bucketMixedSludgeChromiteID() {
 		return getInstance().bucketMixedSludgeChromiteID;
 	}
 
 	public static int BucketMixedSludgeCopperID() {
 		return getInstance().BucketMixedSludgeCopperID;
 	}
 
 	public static int bucketMixedSludgeGalenaID() {
 		return getInstance().bucketMixedSludgeGalenaID;
 	}
 
 	public static int bucketMixedSludgeGoldID() {
 		return getInstance().bucketMixedSludgeGoldID;
 	}
 
 	public static int BucketMixedSludgeIronID() {
 		return getInstance().BucketMixedSludgeIronID;
 	}
 
 	public static int bucketMixedSludgeTaliaID() {
 		return getInstance().bucketMixedSludgeTaliaID;
 	}
 
 	public static int BucketMixedSludgeTinID() {
 		return getInstance().BucketMixedSludgeTinID;
 	}
 
 	public static int BucketPigIronID() {
 		return getInstance().BucketPigIronID;
 	}
 
 	public static int BucketResiduID() {
 		return getInstance().BucketResiduID;
 	}
 
 	public static int BucketUncleanWaterID() {
 		return getInstance().BucketUncleanWaterID;
 	}
 
 	public static int getAirFluidBlockID() {
 		return getInstance().blockFluidBaseAirID;
 	}
 
 	public static int getAlkylateFluidBlockID() {
 		return getInstance().blockFluidBaseAlkylateID;
 	}
 
 	public static int getAsphaltSludgeFluidBlockID() {
 		return getInstance().blockFluidBaseAsphaltSludgeID;
 	}
 
 	public static int getAsphaltSludgeFluidBucketID() {
 		return getInstance().BucketAsphaltSludgeID;
 	}
 
 	public static int getButaneFluidBlockID() {
 		return getInstance().blockFluidBaseButaneID;
 	}
 
 	public static int getCO2FluidBlockID() {
 		return getInstance().blockFluidBaseCO2ID;
 	}
 
 	public static int getFuelGasFluidBlockID() {
 		return getInstance().blockFluidBaseFuelGasID;
 	}
 
 	public static int getGasMixFluidBlockID() {
 		return getInstance().blockFluidBaseGasMixID;
 	}
 
 	public static int getH2FluidBlockID() {
 		return getInstance().blockFluidBaseH2ID;
 	}
 
 	public static int getH2SFluidBlockID() {
 		return getInstance().blockFluidBaseH2SID;
 	}
 
 	public static ConfigFluids getInstance() {
 		return instance;
 	}
 
 	public static int getIsoButaneFluidBlockID() {
 		return getInstance().blockFluidBaseIsoButaneID;
 	}
 
 	public static int getIsomerateFluidBlockID() {
 		return getInstance().blockFluidBaseIsomerateID;
 	}
 
 	public static int getLatexFluidBlockID() {
 		return getInstance().blockFluidBaseLatexID;
 	}
 
 	public static int getLatexFluidBucketID() {
 		return getInstance().bucketLatexID;
 	}
 
 	public static int getParaffinicNaphthaFluidBlockID() {
 		return getInstance().blockFluidBaseParaffinicNaphthaID;
 	}
 
 	public static int getSourWaterFluidBlockID() {
 		return getInstance().blockFluidBaseSourWaterID;
 	}
 
 	public static int getSourWaterFluidBucketID() {
 		return getInstance().BucketSourWaterID;
 	}
 
 	public static int getUnsweetenedButaneFluidBlockID() {
 		return getInstance().blockFluidBaseUnsweetenedButaneID;
 	}
 
 	public static int getUnsweetenedLPGFluidBlockID() {
 		return getInstance().blockFluidBaseUnsweetenedLPGID;
 	}
 
 	public static int getAtmoshepicBottomsFluidBlock() {
 		return getInstance().blockFluidBaseAtmosphericBottoms;
 	}
 
 	public static int getAtmoshepicBottomsFluidBucket() {
 		return getInstance().bucketAtmosphericBottoms;
 	}
 
 	public static int getGasOilFluidBlock() {
 		return getInstance().blockFluidBaseGasOil;
 	}
 
 	public static int getGasOilFluidBucket() {
 		return getInstance().bucketGasOil;
 	}
 
 	public static int getDieselOilFluidBlock() {
 		return getInstance().blockFluidBaseDieselOil;
 	}
 
 	public static int getDieselOilFluidBucket() {
 		return getInstance().bucketDieselOil;
 	}
 
 	public static int getKeroseneFluidBlock() {
 		return getInstance().blockFluidBaseKerosene;
 	}
 
 	public static int getKeroseneFluidBucket() {
 		return getInstance().bucketKerosene;
 	}
 
 	public static int getHeavyNaphthaFluidBlock() {
 		return getInstance().blockFluidBaseHeavyNaphtha;
 	}
 
 	public static int getHeavyNaphthaFluidBucket() {
 		return getInstance().bucketHeavyNaphtha;
 	}
 
 	public static int getLightNaphthaFluidBlock() {
 		return getInstance().blockFluidBaseLightNaphtha;
 	}
 
 	public static int getLightNaphthaFluidBucket() {
 		return getInstance().bucketLightNaphtha;
 	}
 
 	public static int getUntreatedKeroseneFluidBlock() {
 		return getInstance().blockFluidBaseUntreatedKerosene;
 	}
 
 	public static int getUntreatedKeroseneFluidBucket() {
 		return getInstance().bucketUntreatedKerosene;
 	}
 
 	public static int getUntreatedDieselOilFluidBlock() {
 		return getInstance().blockFluidBaseUntreatedDieselOil;
 	}
 
 	public static int getUntreatedDieselOilFluidBucket() {
 		return getInstance().bucketUntreatedDieselOil;
 	}
 
 	public static int getHeavyvacuumGasOilFluidBlock() {
 		return getInstance().blockFluidBaseHeavyvacuumGasOil;
 	}
 
 	public static int getHeavyvacuumGasOilFluidBucket() {
 		return getInstance().bucketHeavyvacuumGasOil;
 	}
 
 	public static int getLightvacuumGasOilFluidBlock() {
 		return getInstance().blockFluidBaseLightvacuumGasOil;
 	}
 
 	public static int getLightvacuumGasOilFluidBucket() {
 		return getInstance().bucketLightvacuumGasOil;
 	}
 
 	public static int getHeavyVirginNaphthaFluidBlock() {
 		return getInstance().blockFluidBaseHeavyVirginNaphtha;
 	}
 
 	public static int getHeavyVirginNaphthaFluidBucket() {
 		return getInstance().bucketHeavyVirginNaphtha;
 	}
 
 	public static int getParaffinicNaphthaFluidBlock() {
 		return getInstance().blockFluidBaseParaffinicNaphtha;
 	}
 
 	public static int getParaffinicNaphthaFluidBucket() {
 		return getInstance().bucketParaffinicNaphtha;
 	}
 
 	public static int getResinFluidBlockID() {
 		return getInstance().blockFluidBaseResin;
 	}
 
 	public static int getResinFluidBucketID() {
 		return getInstance().bucketResin;
 	}
 
 	public static int getBlockFluidBaseThickTinSludgeID() {
 		return getInstance().blockFluidBaseThickTinSludgeID;
 	}
 
 	public static int getBucketThickTinSludgeID() {
 		return getInstance().bucketThickTinSludgeID;
 	}
 
 	public static int getBlockFluidBaseThickCopperSludgeID() {
 		return getInstance().blockFluidBaseThickCopperSludgeID;
 	}
 
 	public static int getBucketThickCopperSludgeID() {
 		return getInstance().bucketThickCopperSludgeID;
 	}
 
 	public static int getBlockFluidBaseThickTaliaSludgeID() {
 		return getInstance().blockFluidBaseThickTaliaSludgeID;
 	}
 
 	public static int getBucketThickTaliaSludgeID() {
 		return getInstance().bucketThickTaliaSludgeID;
 	}
 
 	public static int getBlockFluidBaseThickGoldSludgeID() {
 		return getInstance().blockFluidBaseThickGoldSludgeID;
 	}
 
 	public static int getBucketThickGoldSludgeID() {
 		return getInstance().bucketThickGoldSludgeID;
 	}
 
 	public static int getBlockFluidBaseThickGalenaSludgeID() {
 		return getInstance().blockFluidBaseThickGalenaSludgeID;
 	}
 
 	public static int getBucketThickGalenaSludgeID() {
 		return getInstance().bucketThickGalenaSludgeID;
 	}
 
 	public static int getBlockFluidBaseThickChromiteSludgeID() {
 		return getInstance().blockFluidBaseThickChromiteSludgeID;
 	}
 
 	public static int getBucketThickChromiteSludgeID() {
 		return getInstance().bucketThickChromiteSludgeID;
 	}
 
 	public static int getBlockFluidBaseThickIronSludgeID() {
 		return getInstance().blockFluidBaseThickIronSludgeID;
 	}
 
 	public static int getBucketThickIronSludgeID() {
 		return getInstance().bucketThickIronSludgeID;
 	}
 
 	public static int getBlockFluidBaseFlotationTinSedimentID() {
 		return getInstance().blockFluidBaseFlotationTinSedimentID;
 	}
 
 	public static int getBucketFlotationTinSedimentID() {
 		return getInstance().bucketFlotationTinSedimentID;
 	}
 
 	public static int getBlockFluidBaseFlotationCopperSedimentID() {
 		return getInstance().blockFluidBaseFlotationCopperSedimentID;
 	}
 
 	public static int getBucketFlotationCopperSedimentID() {
 		return getInstance().bucketFlotationCopperSedimentID;
 	}
 
 	public static int getBlockFluidBaseFlotationTaliaSedimentID() {
 		return getInstance().blockFluidBaseFlotationTaliaSedimentID;
 	}
 
 	public static int getBucketFlotationTaliaSedimentID() {
 		return getInstance().bucketFlotationTaliaSedimentID;
 	}
 
 	public static int getBlockFluidBaseFlotationGoldSedimentID() {
 		return getInstance().blockFluidBaseFlotationGoldSedimentID;
 	}
 
 	public static int getBucketFlotationGoldSedimentID() {
 		return getInstance().bucketFlotationGoldSedimentID;
 	}
 
 	public static int getBlockFluidBaseFlotationGalenaSedimentID() {
 		return getInstance().blockFluidBaseFlotationGalenaSedimentID;
 	}
 
 	public static int getBucketFlotationGalenaSedimentID() {
 		return getInstance().bucketFlotationGalenaSedimentID;
 	}
 
 	public static int getBlockFluidBaseFlotationChromiteSedimentID() {
 		return getInstance().blockFluidBaseFlotationChromiteSedimentID;
 	}
 
 	public static int getBucketFlotationChromiteSedimentID() {
 		return getInstance().bucketFlotationChromiteSedimentID;
 	}
 
 	public static int getBlockFluidBaseFlotationIronSedimentID() {
 		return getInstance().blockFluidBaseFlotationIronSedimentID;
 	}
 
 	public static int getBucketFlotationIronSedimentID() {
 		return getInstance().bucketFlotationIronSedimentID;
 	}
 
 	public static int getBlockFluidBaseCycloneTinSedimentID() {
 		return getInstance().blockFluidBaseCycloneTinSedimentID;
 	}
 
 	public static int getBucketCycloneTinSedimentID() {
 		return getInstance().bucketCycloneTinSedimentID;
 	}
 
 	public static int getBlockFluidBaseCycloneCopperSedimentID() {
 		return getInstance().blockFluidBaseCycloneCopperSedimentID;
 	}
 
 	public static int getBucketCycloneCopperSedimentID() {
 		return getInstance().bucketCycloneCopperSedimentID;
 	}
 
 	public static int getBlockFluidBaseCycloneTaliaSedimentID() {
 		return getInstance().blockFluidBaseCycloneTaliaSedimentID;
 	}
 
 	public static int getBucketCycloneTaliaSedimentID() {
 		return getInstance().bucketCycloneTaliaSedimentID;
 	}
 
 	public static int getBlockFluidBaseCycloneGoldSedimentID() {
 		return getInstance().blockFluidBaseCycloneGoldSedimentID;
 	}
 
 	public static int getBucketCycloneGoldSedimentID() {
 		return getInstance().bucketCycloneGoldSedimentID;
 	}
 
 	public static int getBlockFluidBaseCycloneGalenaSedimentID() {
 		return getInstance().blockFluidBaseCycloneGalenaSedimentID;
 	}
 
 	public static int getBucketCycloneGalenaSedimentID() {
 		return getInstance().bucketCycloneGalenaSedimentID;
 	}
 
 	public static int getBlockFluidBaseCycloneChromiteSedimentID() {
 		return getInstance().blockFluidBaseCycloneChromiteSedimentID;
 	}
 
 	public static int getBucketCycloneChromiteSedimentID() {
 		return getInstance().bucketCycloneChromiteSedimentID;
 	}
 
 	public static int getBlockFluidBaseCycloneIronSedimentID() {
 		return getInstance().blockFluidBaseCycloneIronSedimentID;
 	}
 
 	public static int getBucketCycloneIronSedimentID() {
 		return getInstance().bucketCycloneIronSedimentID;
 	}
 
 	public static int getBlockFluidBaseThickTinSedimentID() {
 		return getInstance().blockFluidBaseThickTinSedimentID;
 	}
 
 	public static int getBucketThickTinSedimentID() {
 		return getInstance().bucketThickTinSedimentID;
 	}
 
 	public static int getBlockFluidBaseThickCopperSedimentID() {
 		return getInstance().blockFluidBaseThickCopperSedimentID;
 	}
 
 	public static int getBucketThickCopperSedimentID() {
 		return getInstance().bucketThickCopperSedimentID;
 	}
 
 	public static int getBlockFluidBaseThickTaliaSedimentID() {
 		return getInstance().blockFluidBaseThickTaliaSedimentID;
 	}
 
 	public static int getBucketThickTaliaSedimentID() {
 		return getInstance().bucketThickTaliaSedimentID;
 	}
 
 	public static int getBlockFluidBaseThickGoldSedimentID() {
 		return getInstance().blockFluidBaseThickGoldSedimentID;
 	}
 
 	public static int getBucketThickGoldSedimentID() {
 		return getInstance().bucketThickGoldSedimentID;
 	}
 
 	public static int getBlockFluidBaseThickGalenaSedimentID() {
 		return getInstance().blockFluidBaseThickGalenaSedimentID;
 	}
 
 	public static int getBucketThickGalenaSedimentID() {
 		return getInstance().bucketThickGalenaSedimentID;
 	}
 
 	public static int getBlockFluidBaseThickChromiteSedimentID() {
 		return getInstance().blockFluidBaseThickChromiteSedimentID;
 	}
 
 	public static int getBucketThickChromiteSedimentID() {
 		return getInstance().bucketThickChromiteSedimentID;
 	}
 
 	public static int getBlockFluidBaseThickIronSedimentID() {
 		return getInstance().blockFluidBaseThickIronSedimentID;
 	}
 
 	public static int getBucketThickIronSedimentID() {
 		return getInstance().bucketThickIronSedimentID;
 	}
 
 	public static int getBlockFluidBaseLightTinSedimentID() {
 		return getInstance().blockFluidBaseLightTinSedimentID;
 	}
 
 	public static int getBucketLightTinSedimentID() {
 		return getInstance().bucketLightTinSedimentID;
 	}
 
 	public static int getBlockFluidBaseLightCopperSedimentID() {
 		return getInstance().blockFluidBaseLightCopperSedimentID;
 	}
 
 	public static int getBucketLightCopperSedimentID() {
 		return getInstance().bucketLightCopperSedimentID;
 	}
 
 	public static int getBlockFluidBaseLightTaliaSedimentID() {
 		return getInstance().blockFluidBaseLightTaliaSedimentID;
 	}
 
 	public static int getBucketLightTaliaSedimentID() {
 		return getInstance().bucketLightTaliaSedimentID;
 	}
 
 	public static int getBlockFluidBaseLightGoldSedimentID() {
 		return getInstance().blockFluidBaseLightGoldSedimentID;
 	}
 
 	public static int getBucketLightGoldSedimentID() {
 		return getInstance().bucketLightGoldSedimentID;
 	}
 
 	public static int getBlockFluidBaseLightGalenaSedimentID() {
 		return getInstance().blockFluidBaseLightGalenaSedimentID;
 	}
 
 	public static int getBucketLightGalenaSedimentID() {
 		return getInstance().bucketLightGalenaSedimentID;
 	}
 
 	public static int getBlockFluidBaseLightChromiteSedimentID() {
 		return getInstance().blockFluidBaseLightChromiteSedimentID;
 	}
 
 	public static int getBucketLightChromiteSedimentID() {
 		return getInstance().bucketLightChromiteSedimentID;
 	}
 
 	public static int getBlockFluidBaseLightIronSedimentID() {
 		return getInstance().blockFluidBaseLightIronSedimentID;
 	}
 
 	public static int getBucketLightIronSedimentID() {
 		return getInstance().bucketLightIronSedimentID;
 	}
 
 	public static int getBlockFluidBaseConcentratedTinSludgeID() {
 		return getInstance().blockFluidBaseConcentratedTinSludgeID;
 	}
 
 	public static int getBucketConcentratedTinSludgeID() {
 		return getInstance().bucketConcentratedTinSludgeID;
 	}
 
 	public static int getBlockFluidBaseConcentratedCopperSludgeID() {
 		return getInstance().blockFluidBaseConcentratedCopperSludgeID;
 	}
 
 	public static int getBucketConcentratedCopperSludgeID() {
 		return getInstance().bucketConcentratedCopperSludgeID;
 	}
 
 	public static int getBlockFluidBaseConcentratedTaliaSludgeID() {
 		return getInstance().blockFluidBaseConcentratedTaliaSludgeID;
 	}
 
 	public static int getBucketConcentratedTaliaSludgeID() {
 		return getInstance().bucketConcentratedTaliaSludgeID;
 	}
 
 	public static int getBlockFluidBaseConcentratedGoldSludgeID() {
 		return getInstance().blockFluidBaseConcentratedGoldSludgeID;
 	}
 
 	public static int getBucketConcentratedGoldSludgeID() {
 		return getInstance().bucketConcentratedGoldSludgeID;
 	}
 
 	public static int getBlockFluidBaseConcentratedGalenaSludgeID() {
 		return getInstance().blockFluidBaseConcentratedGalenaSludgeID;
 	}
 
 	public static int getBucketConcentratedGalenaSludgeID() {
 		return getInstance().bucketConcentratedGalenaSludgeID;
 	}
 
 	public static int getBlockFluidBaseConcentratedChromiteSludgeID() {
 		return getInstance().blockFluidBaseConcentratedChromiteSludgeID;
 	}
 
 	public static int getBucketConcentratedChromiteSludgeID() {
 		return getInstance().bucketConcentratedChromiteSludgeID;
 	}
 
 	public static int getBlockFluidBaseConcentratedIronSludgeID() {
 		return getInstance().blockFluidBaseConcentratedIronSludgeID;
 	}
 
 	public static int getBucketConcentratedIronSludgeID() {
 		return getInstance().bucketConcentratedIronSludgeID;
 	}
 	
 	public static int getBlockFluidBasePasteTinSedimentID() {
 		return getInstance().blockFluidBasePasteTinSedimentID;
 	}
 
 	public static int getBucketPasteTinSedimentID() {
 		return getInstance().bucketPasteTinSedimentID;
 	}
 
 	public static int getBlockFluidBasePasteCopperSedimentID() {
 		return getInstance().blockFluidBasePasteCopperSedimentID;
 	}
 
 	public static int getBucketPasteCopperSedimentID() {
 		return getInstance().bucketPasteCopperSedimentID;
 	}
 
 	public static int getBlockFluidBasePasteTaliaSedimentID() {
 		return getInstance().blockFluidBasePasteTaliaSedimentID;
 	}
 
 	public static int getBucketPasteTaliaSedimentID() {
 		return getInstance().bucketPasteTaliaSedimentID;
 	}
 
 	public static int getBlockFluidBasePasteGoldSedimentID() {
 		return getInstance().blockFluidBasePasteGoldSedimentID;
 	}
 
 	public static int getBucketPasteGoldSedimentID() {
 		return getInstance().bucketPasteGoldSedimentID;
 	}
 
 	public static int getBlockFluidBasePasteGalenaSedimentID() {
 		return getInstance().blockFluidBasePasteGalenaSedimentID;
 	}
 
 	public static int getBucketPasteGalenaSedimentID() {
 		return getInstance().bucketPasteGalenaSedimentID;
 	}
 
 	public static int getBlockFluidBasePasteChromiteSedimentID() {
 		return getInstance().blockFluidBasePasteChromiteSedimentID;
 	}
 
 	public static int getBucketPasteChromiteSedimentID() {
 		return getInstance().bucketPasteChromiteSedimentID;
 	}
 
 	public static int getBlockFluidBasePasteIronSedimentID() {
 		return getInstance().blockFluidBasePasteIronSedimentID;
 	}
 
 	public static int getBucketPasteIronSedimentID() {
 		return getInstance().bucketPasteIronSedimentID;
 	}
 
 	private int offset = 2500;
 
 	private int blockFluidBaseDirtyWaterID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "DirtyWaterID", offset).getInt();
 
 	private int BucketDirtyWaterID = offset + 1;
 
 	private int blockFluidBaseOreSludgeIronID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "IronSludgeID", offset + 2).getInt();
 
 	private int blockFluidBaseOreSludgeCopperID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "CopperSludgeID", offset + 3).getInt();
 
 	private int blockFluidBaseOreSludgeTinID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "TinSludgeID", offset + 4).getInt();
 
 	private int BucketOreSludgeIronID = offset + 5;
 
 	private int BucketOreSludgeCopperID = offset + 6;
 
 	private int BucketOreSludgeTinID = offset + 7;
 
 	private int BucketHotSlagID = offset + 8;
 
 	private int BucketPigIronID = offset + 9;
 
 	private int BucketHotSteelID = offset + 10;
 
 	private int blockFluidBaseHotSlagID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "HotSlagID", offset + 11).getInt();
 
 	private int blockFluidBasePigIronID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "PigIronID", offset + 12).getInt();
 
 	private int blockFluidBaseHotSteelID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "HotSteelID", offset + 13).getInt();
 
 	private int blockFluidBaseExhaustGasID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "ExhaustGasID", offset + 14).getInt();
 
 	private int blockFluidBaseSteamID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "SteamID", offset + 15).getInt();
 
 	private int bucketOreSludgeChromiteID = offset + 16;
 
 	private int bucketOreSludgeGalenaID = offset + 17;
 
 	private int bucketOreSludgeGoldID = offset + 18;
 
 	private int bucketOreSludgeTaliaID = offset + 19;
 
 	private int blockFluidBaseOreSludgeChromiteID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "ChromiteSludgeID", offset + 20).getInt();
 
 	private int blockFluidBaseOreSludgeGalenaID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "GalenaSludgeID", offset + 21).getInt();
 
 	private int blockFluidBaseOreSludgeGoldID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "GoldSludgeID", offset + 22).getInt();
 
 	private int blockFluidBaseOreSludgeTaliaID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "TaliaSludgeID", offset + 23).getInt();
 
 	private int blockFluidBasefiltyWaterID = offset + 24;
 
 	private int BucketfiltyWaterID = offset + 25;
 
 	private int blockFluidBaseGritSludgeID = offset + 26;
 
 	private int BucketGritSludgeID = offset + 27;
 
 	private int blockFluidBaseUncleanWaterID = offset + 28;
 
 	private int BucketUncleanWaterID = offset + 29;
 
 	private int blockFluidBaseResiduID = offset + 30;
 
 	private int BucketResiduID = offset + 31;
 
 	private int blockFluidBaseMicrobialWaterID = offset + 32;
 
 	private int BucketMicrobialWaterID = offset + 33;
 
 	private int blockFluidBaseLatexID = offset + 34;
 
 	private int bucketLatexID = offset + 35;
 
 	private int blockFluidBaseAirID = offset + 36;
 
 	private int blockFluidBaseCO2ID = offset + 37;
 
 	private int blockFluidBaseH2ID = offset + 38;
 
 	private int blockFluidBaseH2SID = offset + 39;
 
 	private int blockFluidBaseSourWaterID = offset + 40;
 
 	private int BucketSourWaterID = offset + 41;
 
 	private int blockFluidBaseAsphaltSludgeID = offset + 42;
 
 	private int BucketAsphaltSludgeID = offset + 43;
 
 	private int blockFluidBaseFuelGasID = offset + 44;
 
 	private int blockFluidBaseUnsweetenedLPGID = offset + 45;
 
 	private int blockFluidBaseUnsweetenedButaneID = offset + 46;
 
 	private int blockFluidBaseGasMixID = offset + 47;
 
 	private int blockFluidBaseParaffinicNaphthaID = offset + 48;
 
 	private int blockFluidBaseIsomerateID = offset + 49;
 
 	private int blockFluidBaseIsoButaneID = offset + 50;
 
 	private int blockFluidBaseButaneID = offset + 51;
 
 	private int blockFluidBaseAlkylateID = offset + 52;
 
 	private int blockFluidBaseMixedSludgeChromiteID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "ChromiteMixedSludgeID", offset + 53).getInt();
 
 	private int blockFluidBaseMixedSludgeGalenaID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "GalenaMixedSludgeID", offset + 54).getInt();
 
 	private int blockFluidBaseMixedSludgeGoldID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "GoldMixedSludgeID", offset + 55).getInt();
 
 	private int blockFluidBaseMixedSludgeTaliaID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "TaliaMixedSludgeID", offset + 56).getInt();
 
 	private int blockFluidBaseMixedSludgeIronID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "IronMixedSludgeID", offset + 57).getInt();
 
 	private int blockFluidBaseMixedSludgeCopperID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "CopperMixedSludgeID", offset + 58).getInt();
 
 	private int blockFluidBaseMixedSludgeTinID = IndustrialProcessing.config.get(ConfigCategories.fluids.toString(), "TinMixedSludgeID", offset + 59).getInt();
 
 	private int BucketMixedSludgeIronID = offset + 60;
 
 	private int BucketMixedSludgeCopperID = offset + 61;
 
 	private int BucketMixedSludgeTinID = offset + 62;
 
 	private int bucketMixedSludgeChromiteID = offset + 63;
 
 	private int bucketMixedSludgeGalenaID = offset + 64;
 
 	private int bucketMixedSludgeGoldID = offset + 65;
 
 	private int bucketMixedSludgeTaliaID = offset + 66;
 
 	private int blockFluidBaseLightNaphtha = offset + 67;
 	private int bucketLightNaphtha = offset + 68;
 
 	private int blockFluidBaseHeavyNaphtha = offset + 69;
 	private int bucketHeavyNaphtha = offset + 70;
 
 	private int blockFluidBaseKerosene = offset + 71;
 	private int bucketKerosene = offset + 72;
 
 	private int blockFluidBaseDieselOil = offset + 73;
 	private int bucketDieselOil = offset + 74;
 
 	private int blockFluidBaseGasOil = offset + 75;
 	private int bucketGasOil = offset + 76;
 
 	private int blockFluidBaseAtmosphericBottoms = offset + 77;
 	private int bucketAtmosphericBottoms = offset + 78;
 
 	private int blockFluidBaseUntreatedKerosene = offset + 79;
 	private int bucketUntreatedKerosene = offset + 80;
 
 	private int blockFluidBaseUntreatedDieselOil = offset + 81;
 	private int bucketUntreatedDieselOil = offset + 82;
 
 	private int blockFluidBaseHeavyvacuumGasOil = offset + 83;
 	private int bucketHeavyvacuumGasOil = offset + 84;
 
 	private int blockFluidBaseLightvacuumGasOil = offset + 85;
 	private int bucketLightvacuumGasOil = offset + 86;
 
 	private int blockFluidBaseHeavyVirginNaphtha = offset + 87;
 	private int bucketHeavyVirginNaphtha = offset + 88;
 
 	private int blockFluidBaseParaffinicNaphtha = offset + 89;
 	private int bucketParaffinicNaphtha = offset + 90;
 
 	private int blockFluidBaseResin = offset + 91;
 	private int bucketResin = offset + 92;
 
 	private int blockFluidBaseThickTinSludgeID = offset + 93;
 	private int bucketThickTinSludgeID = offset + 94;
 
 	private int blockFluidBaseThickCopperSludgeID = offset + 95;
 	private int bucketThickCopperSludgeID = offset + 96;
 
 	private int blockFluidBaseThickTaliaSludgeID = offset + 97;
 	private int bucketThickTaliaSludgeID = offset + 98;
 
 	private int blockFluidBaseThickGoldSludgeID = offset + 99;
 	private int bucketThickGoldSludgeID = offset + 100;
 
 	private int blockFluidBaseThickGalenaSludgeID = offset + 101;
 	private int bucketThickGalenaSludgeID = offset + 102;
 
 	private int blockFluidBaseThickChromiteSludgeID = offset + 103;
 	private int bucketThickChromiteSludgeID = offset + 104;
 
 	private int blockFluidBaseThickIronSludgeID = offset + 105;
 	private int bucketThickIronSludgeID = offset + 106;
 
 	private int blockFluidBaseFlotationTinSedimentID = offset + 107;
 	private int bucketFlotationTinSedimentID = offset + 108;
 
 	private int blockFluidBaseFlotationCopperSedimentID = offset + 109;
 	private int bucketFlotationCopperSedimentID = offset + 110;
 
 	private int blockFluidBaseFlotationTaliaSedimentID = offset + 111;
 	private int bucketFlotationTaliaSedimentID = offset + 112;
 
 	private int blockFluidBaseFlotationGoldSedimentID = offset + 113;
 	private int bucketFlotationGoldSedimentID = offset + 114;
 
 	private int blockFluidBaseFlotationGalenaSedimentID = offset + 115;
 	private int bucketFlotationGalenaSedimentID = offset + 116;
 
 	private int blockFluidBaseFlotationChromiteSedimentID = offset + 117;
 	private int bucketFlotationChromiteSedimentID = offset + 118;
 
 	private int blockFluidBaseFlotationIronSedimentID = offset + 119;
 	private int bucketFlotationIronSedimentID = offset + 120;
 
 	private int blockFluidBaseCycloneTinSedimentID = offset + 121;
 	private int bucketCycloneTinSedimentID = offset + 122;
 
 	private int blockFluidBaseCycloneCopperSedimentID = offset + 123;
 	private int bucketCycloneCopperSedimentID = offset + 124;
 
 	private int blockFluidBaseCycloneTaliaSedimentID = offset + 125;
 	private int bucketCycloneTaliaSedimentID = offset + 126;
 
 	private int blockFluidBaseCycloneGoldSedimentID = offset + 127;
 	private int bucketCycloneGoldSedimentID = offset + 128;
 
 	private int blockFluidBaseCycloneGalenaSedimentID = offset + 129;
 	private int bucketCycloneGalenaSedimentID = offset + 130;
 
 	private int blockFluidBaseCycloneChromiteSedimentID = offset + 131;
 	private int bucketCycloneChromiteSedimentID = offset + 132;
 
 	private int blockFluidBaseCycloneIronSedimentID = offset + 133;
 	private int bucketCycloneIronSedimentID = offset + 134;
 
 	private int blockFluidBaseThickTinSedimentID = offset + 135;
 	private int bucketThickTinSedimentID = offset + 136;
 
 	private int blockFluidBaseThickCopperSedimentID = offset + 137;
 	private int bucketThickCopperSedimentID = offset + 138;
 
 	private int blockFluidBaseThickTaliaSedimentID = offset + 139;
 	private int bucketThickTaliaSedimentID = offset + 140;
 
 	private int blockFluidBaseThickGoldSedimentID = offset + 141;
 	private int bucketThickGoldSedimentID = offset + 142;
 
 	private int blockFluidBaseThickGalenaSedimentID = offset + 143;
 	private int bucketThickGalenaSedimentID = offset + 144;
 
 	private int blockFluidBaseThickChromiteSedimentID = offset + 145;
 	private int bucketThickChromiteSedimentID = offset + 146;
 
 	private int blockFluidBaseThickIronSedimentID = offset + 147;
 	private int bucketThickIronSedimentID = offset + 148;
 
 	private int blockFluidBaseLightTinSedimentID = offset + 149;
 	private int bucketLightTinSedimentID = offset + 150;
 
 	private int blockFluidBaseLightCopperSedimentID = offset + 151;
 	private int bucketLightCopperSedimentID = offset + 152;
 
 	private int blockFluidBaseLightTaliaSedimentID = offset + 153;
 	private int bucketLightTaliaSedimentID = offset + 154;
 
 	private int blockFluidBaseLightGoldSedimentID = offset + 155;
 	private int bucketLightGoldSedimentID = offset + 156;
 
 	private int blockFluidBaseLightGalenaSedimentID = offset + 157;
 	private int bucketLightGalenaSedimentID = offset + 158;
 
 	private int blockFluidBaseLightChromiteSedimentID = offset + 159;
 	private int bucketLightChromiteSedimentID = offset + 160;
 
 	private int blockFluidBaseLightIronSedimentID = offset + 161;
 	private int bucketLightIronSedimentID = offset + 162;
 
 	private int blockFluidBaseConcentratedTinSludgeID = offset + 163;
 	private int bucketConcentratedTinSludgeID = offset + 164;
 
 	private int blockFluidBaseConcentratedCopperSludgeID = offset + 165;
 	private int bucketConcentratedCopperSludgeID = offset + 166;
 
 	private int blockFluidBaseConcentratedTaliaSludgeID = offset + 167;
 	private int bucketConcentratedTaliaSludgeID = offset + 168;
 
 	private int blockFluidBaseConcentratedGoldSludgeID = offset + 169;
 	private int bucketConcentratedGoldSludgeID = offset + 170;
 
 	private int blockFluidBaseConcentratedGalenaSludgeID = offset + 171;
 	private int bucketConcentratedGalenaSludgeID = offset + 172;
 
 	private int blockFluidBaseConcentratedChromiteSludgeID = offset + 173;
 	private int bucketConcentratedChromiteSludgeID = offset + 174;
 
 	private int blockFluidBaseConcentratedIronSludgeID = offset + 175;
 	private int bucketConcentratedIronSludgeID = offset + 176;
 	
 	private int blockFluidBasePasteTinSedimentID = offset + 177;
 	private int bucketPasteTinSedimentID = offset + 178;
 
 	private int blockFluidBasePasteCopperSedimentID = offset + 179;
 	private int bucketPasteCopperSedimentID = offset + 180;
 
 	private int blockFluidBasePasteTaliaSedimentID = offset + 181;
 	private int bucketPasteTaliaSedimentID = offset + 182;
 
 	private int blockFluidBasePasteGoldSedimentID = offset + 183;
 	private int bucketPasteGoldSedimentID = offset + 184;
 
 	private int blockFluidBasePasteGalenaSedimentID = offset + 185;
 	private int bucketPasteGalenaSedimentID = offset + 186;
 
 	private int blockFluidBasePasteChromiteSedimentID = offset + 187;
 	private int bucketPasteChromiteSedimentID = offset + 188;
 
 	private int blockFluidBasePasteIronSedimentID = offset + 189;
 	private int bucketPasteIronSedimentID = offset + 190;
 
 	public ConfigFluids() {
 
 	}
 
 	private void registerFluid(Block block, Fluid fluid, ItemIPBucket bucket, String displayName) {
 		GameRegistry.registerBlock(block, "BlockFluid" + fluid.getName());
 		LanguageRegistry.addName(block, displayName);
 		if (bucket != null) {
 			FluidContainerRegistry.registerFluidContainer(new FluidContainerData(FluidRegistry.getFluidStack(fluid.getName(), FluidContainerRegistry.BUCKET_VOLUME), new ItemStack(bucket), new ItemStack(Item.bucketEmpty)));
 		}
 		// FluidRegistry.registerFluid(fluid) is done in constructor of
 		// itemFluid;
 	}
 
 	public void registerFluids() {
 		registerFluid(ISetupFluids.blockFluidIsoButane, ISetupFluids.itemFluidIsoButane, null, "Isobutane");
 		registerFluid(ISetupFluids.blockFluidButane, ISetupFluids.itemFluidButane, null, "Butane");
 		registerFluid(ISetupFluids.blockFluidAlkylate, ISetupFluids.itemFluidAlkylate, null, "Alkylate");
 		registerFluid(ISetupFluids.blockFluidIsomerate, ISetupFluids.itemFluidIsomerate, null, "Isomerate");
 		registerFluid(ISetupFluids.blockFluidUnsweetenedLPG, ISetupFluids.itemFluidUnsweetenedLPG, null, "Unsweetened LPG");
 		registerFluid(ISetupFluids.blockFluidUnsweetenedButane, ISetupFluids.itemFluidUnsweetenedButane, null, "Unsweetened Butane");
 		registerFluid(ISetupFluids.blockFluidFuelGas, ISetupFluids.itemFluidFuelGas, null, "Fuel gas");
 		registerFluid(ISetupFluids.blockFluidGasMix, ISetupFluids.itemFluidGasMix, null, "Gas mix");
 
 		registerFluid(ISetupFluids.blockFluidResin, ISetupFluids.itemFluidResin, ISetupFluids.bucketResin, "Resin");
 		LanguageRegistry.addName(ISetupFluids.bucketResin, "Resin Bucket");
 
 		registerFluid(ISetupFluids.blockFluidParaffinicNaphtha, ISetupFluids.itemFluidParaffinicNaphtha, ISetupFluids.bucketParaffinicNaphtha, "Paraffinic naphtha");
 		LanguageRegistry.addName(ISetupFluids.bucketParaffinicNaphtha, "Paraffinic naphtha Bucket");
 
 		registerFluid(ISetupFluids.blockFluidHeavyVirginNaphtha, ISetupFluids.itemFluidHeavyVirginNaphtha, ISetupFluids.bucketHeavyVirginNaphtha, "Heavy virgin naphtha");
 		LanguageRegistry.addName(ISetupFluids.bucketHeavyVirginNaphtha, "Heavy virgin naphtha Bucket");
 
 		registerFluid(ISetupFluids.blockFluidLightVacuumGasOil, ISetupFluids.itemFluidLightVacuumGasOil, ISetupFluids.bucketLightVacuumGasOil, "Light vacuum gasOil");
 		LanguageRegistry.addName(ISetupFluids.bucketLightVacuumGasOil, "Light vacuum gasOil Bucket");
 
 		registerFluid(ISetupFluids.blockFluidHeavyvacuumGasOil, ISetupFluids.itemFluidHeavyvacuumGasOil, ISetupFluids.bucketHeavyvacuumGasOil, "Heavy vacuum gasOil");
 		LanguageRegistry.addName(ISetupFluids.bucketHeavyvacuumGasOil, "Heavy vacuum gasOil Bucket");
 
 		registerFluid(ISetupFluids.blockFluidUntreatedDieselOil, ISetupFluids.itemFluidUntreatedDieselOil, ISetupFluids.bucketUntreatedDieselOil, "Untreated diesel Oil");
 		LanguageRegistry.addName(ISetupFluids.bucketUntreatedDieselOil, "Untreated diesel Oil Bucket");
 
 		registerFluid(ISetupFluids.blockFluidUntreatedKerosene, ISetupFluids.itemFluidUntreatedKerosene, ISetupFluids.bucketUntreatedKerosene, "Untreated kerosene");
 		LanguageRegistry.addName(ISetupFluids.bucketUntreatedKerosene, "Untreated kerosene Bucket");
 
 		registerFluid(ISetupFluids.blockFluidLightNaphtha, ISetupFluids.itemFluidLightNaphtha, ISetupFluids.bucketLightNaphtha, "Light naphtha");
 		LanguageRegistry.addName(ISetupFluids.bucketLightNaphtha, "Light naphtha Bucket");
 
 		registerFluid(ISetupFluids.blockFluidHeavyNaphtha, ISetupFluids.itemFluidHeavyNaphtha, ISetupFluids.bucketHeavyNaphtha, "Heavy naphtha");
 		LanguageRegistry.addName(ISetupFluids.bucketHeavyNaphtha, "Heavy naphtha Bucket");
 
 		registerFluid(ISetupFluids.blockFluidKerosene, ISetupFluids.itemFluidKerosene, ISetupFluids.bucketKerosene, "Kerosene");
 		LanguageRegistry.addName(ISetupFluids.bucketKerosene, "Kerosene Bucket");
 
 		registerFluid(ISetupFluids.blockFluidDieselOil, ISetupFluids.itemFluidDieselOil, ISetupFluids.bucketDieselOil, "Diesel oil");
 		LanguageRegistry.addName(ISetupFluids.bucketDieselOil, "Diesel oil Bucket");
 
 		registerFluid(ISetupFluids.blockFluidGasOil, ISetupFluids.itemFluidGasOil, ISetupFluids.bucketGasOil, "Gasoil");
 		LanguageRegistry.addName(ISetupFluids.bucketGasOil, "Gasoil Bucket");
 
 		registerFluid(ISetupFluids.blockFluidAtmosphericBottoms, ISetupFluids.itemFluidAtmosphericBottoms, ISetupFluids.bucketAtmosphericBottoms, "Atmospheric bottoms");
 		LanguageRegistry.addName(ISetupFluids.bucketAtmosphericBottoms, "Atmospheric bottoms Bucket");
 
 		registerFluid(ISetupFluids.blockFluidAsphaltSludge, ISetupFluids.itemFluidAsphaltSludge, ISetupFluids.bucketAsphaltSludge, "Asphalt sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketAsphaltSludge, "Asphalt sludge Bucket");
 
 		registerFluid(ISetupFluids.blockFluidH2S, ISetupFluids.itemFluidH2S, null, "H2S");
 
 		registerFluid(ISetupFluids.blockFluidSourWater, ISetupFluids.itemFluidSourWater, ISetupFluids.bucketSourWater, "Sour water");
 		LanguageRegistry.addName(ISetupFluids.bucketSourWater, "Sour water Bucket");
 
 		registerFluid(ISetupFluids.blockFluidH2, ISetupFluids.itemFluidH2, null, "H2");
 		registerFluid(ISetupFluids.blockFluidCO2, ISetupFluids.itemFluidCO2, null, "CO2");
 		registerFluid(ISetupFluids.blockFluidAir, ISetupFluids.itemFluidAir, null, "Air");
 
 		registerFluid(ISetupFluids.blockFluidLatex, ISetupFluids.itemFluidLatex, ISetupFluids.bucketFluidLatex, "Latex");
 		LanguageRegistry.addName(ISetupFluids.bucketFluidLatex, "Latex Bucket");
 
 		registerFluid(ISetupFluids.blockFluidMicrobialWater, ISetupFluids.itemFluidMicrobialWater, ISetupFluids.bucketMicrobialWater, "Microbial water");
 		LanguageRegistry.addName(ISetupFluids.bucketMicrobialWater, "Microbial water Bucket");
 
 		registerFluid(ISetupFluids.blockFluidUncleanWater, ISetupFluids.itemFluidUncleanWater, ISetupFluids.bucketUncleanWater, "Unclean water");
 		LanguageRegistry.addName(ISetupFluids.bucketUncleanWater, "Unclean water Bucket");
 
 		registerFluid(ISetupFluids.blockFluidResidu, ISetupFluids.itemFluidResidu, ISetupFluids.bucketResidu, "Residu");
 		LanguageRegistry.addName(ISetupFluids.bucketResidu, "Residu Bucket");
 
 		registerFluid(ISetupFluids.blockFluidGritSludge, ISetupFluids.itemFluidGritSludge, ISetupFluids.bucketGritSludge, "Grit sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketGritSludge, "Grit sludge Bucket");
 
 		registerFluid(ISetupFluids.blockFluidDirtyWater, ISetupFluids.itemFluidDirtyWater, ISetupFluids.bucketDirtyWater, "Dirty water");
 		LanguageRegistry.addName(ISetupFluids.bucketDirtyWater, "Dirty water Bucket");
 
 		registerFluid(ISetupFluids.blockFluidfiltyWater, ISetupFluids.itemFluidfiltyWater, ISetupFluids.bucketfiltyWater, "Filty water");
 		LanguageRegistry.addName(ISetupFluids.bucketfiltyWater, "Filty water Bucket");
 
 		registerFluid(ISetupFluids.blockFluidOreSludgeIron, ISetupFluids.itemFluidOreSludgeIron, ISetupFluids.bucketOreSludgeIron, "Iron ore sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketOreSludgeIron, "Iron sludge Bucket");
 
 		registerFluid(ISetupFluids.blockFluidOreSludgeCopper, ISetupFluids.itemFluidOreSludgeCopper, ISetupFluids.bucketOreSludgeCopper, "Copper ore sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketOreSludgeCopper, "Copper sludge Bucket");
 
 		registerFluid(ISetupFluids.blockFluidOreSludgeTin, ISetupFluids.itemFluidOreSludgeTin, ISetupFluids.bucketOreSludgeTin, "Tin ore sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketOreSludgeTin, "Tin sludge Bucket");
 
 		registerFluid(ISetupFluids.blockFluidHotSlag, ISetupFluids.itemFluidHotSlag, ISetupFluids.bucketHotSlag, "Hot slag");
 		LanguageRegistry.addName(ISetupFluids.bucketHotSlag, "Hot slag Bucket");
 
 		registerFluid(ISetupFluids.blockFluidPigIron, ISetupFluids.itemFluidPigIron, ISetupFluids.bucketPigIron, "Pig iron");
 		LanguageRegistry.addName(ISetupFluids.bucketPigIron, "Pig iron Bucket");
 
 		registerFluid(ISetupFluids.blockFluidHotSteel, ISetupFluids.itemFluidHotSteel, ISetupFluids.bucketHotSteel, "Molten steel");
 		LanguageRegistry.addName(ISetupFluids.bucketHotSteel, "Molten steel Bucket");
 
 		registerFluid(ISetupFluids.blockFluidExhaustGas, ISetupFluids.itemFluidExhaustGas, null, "Exhaust gas");
 
 		registerFluid(ISetupFluids.blockFluidSteam, ISetupFluids.itemFluidSteam, null, "Steam");
 
 		registerFluid(ISetupFluids.blockFluidOreSludgeChromite, ISetupFluids.itemFluidOreSludgeChromite, ISetupFluids.bucketOreSludgeChromite, "Chromite ore sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketOreSludgeChromite, "Chromite sludge Bucket");
 		registerFluid(ISetupFluids.blockFluidOreSludgeGalena, ISetupFluids.itemFluidOreSludgeGalena, ISetupFluids.bucketOreSludgeGalena, "Galena ore sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketOreSludgeGalena, "Galena sludge Bucket");
 		registerFluid(ISetupFluids.blockFluidOreSludgeGold, ISetupFluids.itemFluidOreSludgeGold, ISetupFluids.bucketOreSludgeGold, "Gold ore sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketOreSludgeGold, "Gold sludge Bucket");
 		registerFluid(ISetupFluids.blockFluidOreSludgeTalia, ISetupFluids.itemFluidOreSludgeTalia, ISetupFluids.bucketOreSludgeTalia, "Thalium ore sludge");
 		LanguageRegistry.addName(ISetupFluids.bucketOreSludgeTalia, "Thalium sludge Bucket");
 
 		registerFluid(ISetupFluids.blockFluidMixedSludgeIron, ISetupFluids.itemFluidMixedSludgeIron, ISetupFluids.bucketMixedSludgeIron, "Iron ore mixedSludge");
 		LanguageRegistry.addName(ISetupFluids.bucketMixedSludgeIron, "Iron mixedSludge Bucket");
 		registerFluid(ISetupFluids.blockFluidMixedSludgeCopper, ISetupFluids.itemFluidMixedSludgeCopper, ISetupFluids.bucketMixedSludgeCopper, "Copper ore mixedSludge");
 		LanguageRegistry.addName(ISetupFluids.bucketMixedSludgeCopper, "Copper mixedSludge Bucket");
 		registerFluid(ISetupFluids.blockFluidMixedSludgeTin, ISetupFluids.itemFluidMixedSludgeTin, ISetupFluids.bucketMixedSludgeTin, "Tin ore mixedSludge");
 		LanguageRegistry.addName(ISetupFluids.bucketMixedSludgeTin, "Tin mixedSludge Bucket");
 		registerFluid(ISetupFluids.blockFluidMixedSludgeChromite, ISetupFluids.itemFluidMixedSludgeChromite, ISetupFluids.bucketMixedSludgeChromite, "Chromite ore mixedSludge");
 		LanguageRegistry.addName(ISetupFluids.bucketMixedSludgeChromite, "Chromite mixedSludge Bucket");
 		registerFluid(ISetupFluids.blockFluidMixedSludgeGalena, ISetupFluids.itemFluidMixedSludgeGalena, ISetupFluids.bucketMixedSludgeGalena, "Galena ore mixedSludge");
 		LanguageRegistry.addName(ISetupFluids.bucketMixedSludgeGalena, "Galena mixedSludge Bucket");
 		registerFluid(ISetupFluids.blockFluidMixedSludgeGold, ISetupFluids.itemFluidMixedSludgeGold, ISetupFluids.bucketMixedSludgeGold, "Gold ore mixedSludge");
 		LanguageRegistry.addName(ISetupFluids.bucketMixedSludgeGold, "Gold mixedSludge Bucket");
 		registerFluid(ISetupFluids.blockFluidMixedSludgeTalia, ISetupFluids.itemFluidMixedSludgeTalia, ISetupFluids.bucketMixedSludgeTalia, "Thalium ore mixedSludge");
 		LanguageRegistry.addName(ISetupFluids.bucketMixedSludgeTalia, "Thalium mixedSludge Bucket");
 
 	}
 
 }
