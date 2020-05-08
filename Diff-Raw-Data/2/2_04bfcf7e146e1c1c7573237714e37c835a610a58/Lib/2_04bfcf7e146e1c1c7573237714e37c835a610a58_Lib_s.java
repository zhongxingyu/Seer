 package ml.boxes;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemDye;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 
 public class Lib {
 	
 	private static String[] suffixes = {"k", "M", "G", "T", "P"};
 	public static String[] DyeOreNames = {"dyeBlack", "dyeRed", "dyeGreen", "dyeBrown", "dyeBlue", "dyePurple", "dyeCyan", "dyeLightGray", "dyeGray", "dyePink", "dyeLime", "dyeYellow", "dyeLightBlue", "dyeMagenta", "dyeOrange", "dyeWhite"};
 	
 	public static boolean isRealPlayer(EntityPlayer pl){ //TODO Doesn't work once compiled; NPE
 		Package pkg = pl.getClass().getPackage();
		if (pkg.getName() == null ||!pkg.getName().contains(".") || pkg.getName().contains("net.minecraft"))
 			return true;
 		return false;
 	}
 
 	public static ItemStack getEquivVanillaDye(ItemStack is){
 		for (int i=0; i<ItemDye.dyeColorNames.length; i++){
 			if (OreDictionary.getOreID(is) == OreDictionary.getOreID(new ItemStack(Item.dyePowder, 1, i))){
 				return new ItemStack(Item.dyePowder, 1, i);
 			}
 		}
 		return null;
 	}
 	
 	public static String toGroupedString(float n, int p){
 		String suffix = "";
 		for (int i=suffixes.length; i>0; i--){
 			if (n / Math.pow(1000, i) >= 1){
 				suffix = suffixes[i-1];
 				n /= Math.pow(1000, i);
 				break;
 			}
 		}
 		n = (float)(Math.round(n*Math.pow(10, p))/Math.pow(10, p));
 		String ns = ""+n;
 		return ""+(ns.replaceAll("\\.?[0]*$", ""))+suffix;
 	}
 }
