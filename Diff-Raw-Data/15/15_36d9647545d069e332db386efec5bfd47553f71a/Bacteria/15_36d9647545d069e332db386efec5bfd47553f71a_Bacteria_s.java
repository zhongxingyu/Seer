 package mods.Electrolysm.electro.biology.bacteria;
 
 import mods.Electrolysm.api.bacteria.BacteriaBaseClass;
 import net.minecraft.item.Item;
 
 public class Bacteria {
 	
 	public static Item bacteriaFusoR;
 	public static Item bacteriaFusoNR;
 	public static Item nitrospiraeR;
 	public static Item nitrospiraeNR;
 	public static String[] posBac = {"bacteriaFuso", "nitrospirae"};
 	public static String[] reBac = {"R", "RN"};
 	
 	public static void loadBacteria(){
 		
 		//Tier 1
 		//Fusobacteria
		bacteriaFusoR = new BacteriaBaseClass(BacIDHandler.tier1.bacteriaFuso).setUnlocalizedName(posBac[1] + reBac[1]);
		bacteriaFusoNR = new BacteriaBaseClass(BacIDHandler.tier1.bacteriaFuso + 1).setUnlocalizedName(posBac[1] + reBac[2]);
 		//Nitrospirae
		nitrospiraeR = new BacteriaBaseClass(BacIDHandler.tier1.nitrospirae).setUnlocalizedName(posBac[2] + reBac[1]);
		nitrospiraeNR = new BacteriaBaseClass(BacIDHandler.tier1.nitrospirae + 1).setUnlocalizedName(posBac[2] + reBac[2]);
 		
 		 
 		//Tier 2
 		
 		//Tier 3
 		
 		//Tier 4
 		
 		//Tier 5
 		
 		//Tier 6
 	}
 }
