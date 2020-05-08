 package me.hammale.Sewer;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.ItemStack;
 
 public class bridge {
 
 	Random gen = new Random();
 	private final AbandonedTunnel tun = new AbandonedTunnel();
 	
 	public void nsbridge(Block set, Material m, BlockFace bf){
 
 		BlockFace bf1 = BlockFace.NORTH;
 		BlockFace bf2 = BlockFace.SOUTH;
 		BlockFace bf3 = BlockFace.EAST;
 		BlockFace bf4 = BlockFace.WEST;
 		
 		int tun1 = tun.nstStraight(set, m, bf);
 		
 		int gap = gen.nextInt(40);
 		
 		if(gap <= 20){
 			gap = 20;
 		}
 		
 		Block otherset = set.getRelative(bf, tun1-2); 
 		int finalgap = gap+tun1;
 		int i = 1;
 		
 		while(i <= finalgap-2){
 			Block set1 = otherset.getRelative(bf, i);
 			Block set2 = set1.getRelative(bf3, 1);
 			Block set3 = set1.getRelative(bf4, 1);
 			Block fence1 = set2.getRelative(BlockFace.UP, 1);
 			Block fence2 = set3.getRelative(BlockFace.UP, 1);
 				
 			int webran1 = gen.nextInt(6);
 			if(webran1 == 1){
 				fence1.setTypeId(0);
 			}else{
 				fence1.setTypeId(85);
 			}
 			int webran2 = gen.nextInt(6);
 			if(webran2 == 1){
 				fence2.setTypeId(0);
 			}else{
 				fence2.setTypeId(85);
 			}
 			
 			int x = 1;
 			while(x <= 2){
 			Block clr1 = fence1.getRelative(BlockFace.UP, x);
 			Block clr2 = clr1.getRelative(bf4, 1);
 			Block clr3 = clr2.getRelative(bf4, 1);
 			Block clr4 = fence1.getRelative(bf4, 1);
 			
 			Block clr5 = clr1.getRelative(bf3, 1);
 			Block clr6 = clr1.getRelative(bf3, 2);
 			Block clr7 = clr3.getRelative(bf4, 1);
 			Block clr8 = clr3.getRelative(bf4, 2);
 			
 			Block clr9 = fence1.getRelative(bf3, 1);
 			Block clr10 = fence2.getRelative(bf4, 1);
 			Block clr11 = fence1.getRelative(bf3, 2);
 			Block clr12 = fence2.getRelative(bf4, 2);
 			
 			clr1.setType(Material.AIR);
 			clr2.setType(Material.AIR);
 			clr3.setType(Material.AIR);
//			if(x == 1 && i == 1 || i == finalgap-2){
 			clr4.setType(Material.AIR);
//			}else{
//				clr4.setType(Material.AIR);
//			}
 			clr5.setType(Material.AIR);
 			clr6.setType(Material.AIR);
 			clr7.setType(Material.AIR);
 			clr8.setType(Material.AIR);
 			clr9.setType(Material.AIR);
 			clr10.setType(Material.AIR);
 			clr11.setType(Material.AIR);
 			clr12.setType(Material.AIR);
 			x++;
 			}
 			x = 1;
 			while(x <= 6){
 			Block clr1 = fence1.getRelative(BlockFace.DOWN, x);
 			Block clr2 = clr1.getRelative(bf4, 1);
 			Block clr3 = clr2.getRelative(bf4, 1);
 			Block clr4 = fence1.getRelative(bf4, 1);
 			
 			Block clr5 = clr1.getRelative(bf3, 1);
 			Block clr6 = clr1.getRelative(bf3, 2);
 			Block clr7 = clr3.getRelative(bf4, 1);
 			Block clr8 = clr3.getRelative(bf4, 2);
 			
 			Block clr9 = fence1.getRelative(bf3, 1);
 			Block clr10 = fence2.getRelative(bf4, 1);
 			Block clr11 = fence1.getRelative(bf3, 2);
 			Block clr12 = fence2.getRelative(bf4, 2);
 			
 			if(x != 5){
 				clr1.setType(Material.AIR);
 				clr2.setType(Material.AIR);
 				clr3.setType(Material.AIR);
 				clr4.setType(Material.AIR);
 				clr5.setType(Material.AIR);
 				clr6.setType(Material.AIR);
 				clr7.setType(Material.AIR);
 				clr8.setType(Material.AIR);
 				clr9.setType(Material.AIR);
 				clr10.setType(Material.AIR);
 				clr11.setType(Material.AIR);
 				clr12.setType(Material.AIR);
 			}else{						
 				clr1.setType(Material.LAVA);
 				clr2.setType(Material.LAVA);
 				clr3.setType(Material.LAVA);
 				clr4.setType(Material.LAVA);
 				clr5.setType(Material.LAVA);
 				clr6.setType(Material.LAVA);
 				clr7.setType(Material.LAVA);
 				clr8.setType(Material.LAVA);
 				clr9.setType(Material.LAVA);
 				clr10.setType(Material.LAVA);
 				clr11.setType(Material.LAVA);
 				clr12.setType(Material.LAVA);
 			}
 			
 			int fran1 = gen.nextInt(6);
 			if(fran1 == 1){
 				set1.setTypeId(0);
 			}else{
 				int ran1 = gen.nextInt(3);
 				set1.setType(m);
 				set1.setData((byte) ran1);
 			}
 			
 			int fran2 = gen.nextInt(6);
 			if(fran2 == 1){
 				set2.setTypeId(0);
 			}else{
 				int ran1 = gen.nextInt(3);
 				set2.setType(m);
 				set2.setData((byte) ran1);
 			}
 			
 			int fran3 = gen.nextInt(6);
 			if(fran3 == 1){
 				set3.setTypeId(0);
 			}else{
 				int ran1 = gen.nextInt(3);
 				set3.setType(m);
 				set3.setData((byte) ran1);
 			}
			if(i == 1 || i == finalgap-2){
			Block east = clr4.getRelative(BlockFace.EAST, 1);
			Block west = clr4.getRelative(BlockFace.WEST, 1);
			clr4.setType(Material.IRON_FENCE);
			east.setType(Material.IRON_FENCE);
			west.setType(Material.IRON_FENCE);
			}
 			x++;
 			}						
 			i++;
 		}
 		
 		Block bridge = set.getRelative(bf, tun1+i-2);
 		
 		int tun2 = tun.nstStraight(bridge, m, bf);
 	}
 }
