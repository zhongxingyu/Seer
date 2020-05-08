 package nacorpio.mod.temperature;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 
 public class ItemManualThermometer extends Item {
 
 	BiomeTemperature biomeTemperature;
 	boolean T = false;
 	
 	public ItemManualThermometer() {
 		super(1566);
 	}
 
 	public final ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		this.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
 		if (T == false){
 			BiomeGenBase biome = par2World.getBiomeGenForCoords((int)par3EntityPlayer.posX, (int)par3EntityPlayer.posZ);
 			switch (biome.biomeID){
 			case 0 :
 				biomeTemperature = new TemperatureOcean();
 			case 1 :
 				biomeTemperature = new TemperaturePlain();
 			case 2 :
 				biomeTemperature = new TemperatureDesert();
 			case 3 :
 				biomeTemperature = new TemperatureExtremeHills();
 			case 4 :
 				biomeTemperature = new TemperatureForest();
 			case 5 :
 				biomeTemperature = new TemperatureTaiga();
 			case 6 :
 				biomeTemperature = new TemperatureSwampLand();
 			case 7 :
 				biomeTemperature = new TemperatureRiver();
 			case 8 :
 				biomeTemperature = new TemperatureHell();
 			case 9 : 
 				biomeTemperature = new TemperatureSky();
 			case 10 : 
 				biomeTemperature = new TemperatureFrozenOcean();
 			case 11 :
 				biomeTemperature = new TemperatureFrozenRiver();
 			case 12 :
 				biomeTemperature = new TemperatureIcePlains();
 			case 13 : 
 				biomeTemperature = new TemperatureIceMountains();
 			case 14 :
 				biomeTemperature = new TemperatureMushroomIsland();
 			case 15 :
 				biomeTemperature = new TemperatureMushroomIslandShore();
 			case 16 :
 				biomeTemperature = new TemperatureBeach();
 			case 17 :
 				biomeTemperature = new TemperatureDesertHills();
 			case 18 :
 				biomeTemperature = new TemperatureForestHills();
 			case 19 :
 				biomeTemperature = new TemperatureTaigaHills();
 			case 20 :
 				biomeTemperature = new TemperatureHillsEdge();
 			case 21 : 
 				biomeTemperature = new TemperatureJungle();
 			case 22 : 
 				biomeTemperature = new TemperatureJungleHills();
 			}
 			biomeTemperature.setRandomizedTemperature((int)par2World.getTotalWorldTime(), par2World.isRaining(), par2World.isThundering());
 			par3EntityPlayer.sendChatToPlayer("Starting measurement of the temperature in the current biome...");
 			this.T = true;
 		} else {
 			par3EntityPlayer.sendChatToPlayer("The current temperature of the air is " + biomeTemperature.getCurrentTemperature());
 		}
 		return par1ItemStack;
 	}
 	
 }
