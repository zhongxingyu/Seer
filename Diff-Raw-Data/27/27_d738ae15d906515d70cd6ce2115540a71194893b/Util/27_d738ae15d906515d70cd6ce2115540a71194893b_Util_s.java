 package net.sqdmc.bubbleshield;
 
 import java.text.NumberFormat;
 import java.text.ParsePosition;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.Faction;
 
 import net.sqdmc.bubbleshield.Shield;
 import net.sqdmc.bubbleshield.ShieldOwner;
 
 public class Util {
 	
 	public static boolean isNumeric(String str) {
 	  NumberFormat formatter = NumberFormat.getInstance();
 	  ParsePosition pos = new ParsePosition(0);
 	  formatter.parse(str, pos);
 	  return str.length() == pos.getIndex();
 	}
 
 	public static Shield getShield(ShieldOwner owner, ShieldStorage shieldstorage) {
 		Shield shield = shieldstorage.getShields().get(owner);
 		if (shield == null) {
 			shield =  new Shield(owner);
 			shieldstorage.addShield(owner,shield);
 		}
 		return shield;
 	}
 	
 	public static ShieldBase getShieldBase(Location targetLoc) {
 		Block signBlock = targetLoc.getBlock();
 		if (signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST || signBlock.getType() == Material.WALL_SIGN){
 			
 			Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
 			if (ShieldBlock.getType() == Material.SPONGE) {				
 				Faction faction = Board.getFactionAt(targetLoc);	    						   
 			    
 			    ShieldOwnerFaction fSheildowner = new ShieldOwnerFaction(faction);
 				Shield shield = new Shield(fSheildowner);
 				ShieldBase shieldBase = new ShieldBase(ShieldBlock, signBlock, shield, targetLoc.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
 				
 				return shieldBase;
 			}
 		}
 		return null;
 	}
 	
 	public static int getShieldCount(ShieldStorage shieldstorage, String owner) {
 		int count = 0;
 		for (ShieldBase shieldbase : shieldstorage.GetShieldBases()) {
 			if (shieldbase.shield.getShieldOwner().getOwner() == owner) {
 				count++;
 			}
 		}
 
 		return count;
 	}
 	
 	public static int getShieldPowerFromBlock(Block block) {
 		if (block.getType() == Material.SPONGE)
			return 100;
 		else if (block.getType() == Material.EMERALD_BLOCK)
			return 75;
 		else if (block.getType() == Material.DIAMOND_BLOCK)
			return 50;
 		else if (block.getType() == Material.GOLD_BLOCK)
			return 25;
 		else if (block.getType() == Material.IRON_BLOCK)
 			return 10;
 
 		return 0;
 	}
 }
