 package me.hammale.Sewer;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 public class AbandonedTunnel {
 
 	Random gen = new Random();
 	public int ewtStraight(Block set, Material m, BlockFace bf){
 		int x = 1;
 		int a = gen.nextInt(40);
 		if (a < 12){
 			a = 12;
 		}
 		while (x < a) {
 				int newx = x-1;
 				//START//
 				Block otherset = set.getRelative(bf, newx);
 				Block set1 = otherset.getRelative(BlockFace.SOUTH, 1);
 				Block set2 = set1.getRelative(BlockFace.SOUTH, 1);
 				Block set3 = otherset.getRelative(BlockFace.NORTH, 1);
 				Block set4 = set3.getRelative(BlockFace.NORTH, 1);
 				Block clr1 = set3.getRelative(BlockFace.UP, 1);
 				Block clr2 = clr1.getRelative(BlockFace.UP, 1);
 				Block clr3 = clr2.getRelative(BlockFace.UP, 1);
 				Block clr4 = set1.getRelative(BlockFace.UP, 1);
 				Block clr5 = clr4.getRelative(BlockFace.UP, 1);
 				Block clr6 = clr5.getRelative(BlockFace.UP, 1);
 				Block clr7 = otherset.getRelative(BlockFace.UP, 1);
 				Block clr8 = clr7.getRelative(BlockFace.UP, 1);
 				Block clr9 = clr8.getRelative(BlockFace.UP, 1);
 								
 				clr1.setType(Material.WATER);
 				clr2.setType(Material.AIR);
 				clr6.setType(Material.AIR);
 				int i1 = gen.nextInt(10);
 				if (i1 == 3){
 					clr3.setType(Material.WEB);	
 				}else if (i1 == 2){
 					Block otherside = clr3.getRelative(BlockFace.SOUTH, 2);
 					otherside.setType(Material.WEB);
 					clr3.setType(Material.AIR);
 				}else {
 					clr3.setType(Material.AIR);
 				}
 				clr4.setType(Material.WATER);
 				clr5.setType(Material.AIR);				
 				int i3 = gen.nextInt(10);
 				int i4 = gen.nextInt(5);
 				if (i3 == 2){
 					if (i4 == 2){
 						byte flags = (byte) (0x2);
 						clr7.setTypeIdAndData(97, flags, true);
 					}
 					else {
 						int ran16 = gen.nextInt(3);
 						clr7.setType(m);
 						clr7.setData((byte) ran16);
 					}
 				}
 				else {
 					clr7.setType(Material.WATER);
 				}				
 				clr8.setType(Material.AIR);	
 				clr9.setType(Material.AIR);
 				
 				Block side1 = clr7.getRelative(BlockFace.NORTH, 1);
 				int side1ran = gen.nextInt(3);
 				side1.setType(m);
 				side1.setData((byte) side1ran);
 
 //				Block side2 = clr7.getRelative(BlockFace.SOUTH, 1);
 //				int side2ran = gen.nextInt(3);
 //				side2.setType(m);
 //				side2.setData((byte) side2ran);
 				
 				int other = gen.nextInt(3);
 				otherset.setType(m);
 				otherset.setData((byte) other);
 				
 				int ran2 = gen.nextInt(3);
 				set3.setType(m);
 				set3.setData((byte) ran2);
 				
 				int ran4 = gen.nextInt(3);
 				set4.setType(m);
 				set4.setData((byte) ran4);
 				
 				int ran = gen.nextInt(3);
 				set1.setType(m);
 				set1.setData((byte) ran);
 				
 				int ran1 = gen.nextInt(3);
 				set2.setType(m);
 				set2.setData((byte) ran1);
 					
 				//
 				Block set5 = set4.getRelative(BlockFace.UP, 1);
 				Block set6 = set5.getRelative(BlockFace.UP, 1);
 				Block set7 = set6.getRelative(BlockFace.UP, 1);
 				Block set8 = set7.getRelative(BlockFace.UP, 1);
 				int ran3 = gen.nextInt(3);
 				int ran7 = gen.nextInt(3);
 				int ran5 = gen.nextInt(3);
 				int ran6 =  gen.nextInt(3);
 				set5.setType(m);
 				set5.setData((byte) ran3);
 				set6.setType(m);
 				set6.setData((byte) ran7);
 				set7.setType(m);
 				int rtorch1 = gen.nextInt(15);
 				if (rtorch1 == 1){
 					byte flags = (byte) (0x4);
 					Block torch = set7.getRelative(BlockFace.SOUTH, 1);
					torch.setTypeIdAndData(76, flags, true);
 				}
 				set7.setData((byte) ran5);
 				set8.setType(m);
 				set8.setData((byte) ran6);
 				//
 				Block set9 = set8.getRelative(BlockFace.SOUTH, 1);
 				Block set10 = set9.getRelative(BlockFace.SOUTH, 1);
 				Block set11 = set10.getRelative(BlockFace.SOUTH, 1);
 				Block set12 = set11.getRelative(BlockFace.SOUTH, 1);
 				int ran11 = gen.nextInt(3);
 				int ran8 = gen.nextInt(3);
 				int ran9 = gen.nextInt(3);
 				int ran10 = gen.nextInt(3);
 				set9.setType(m);
 				set9.setData((byte) ran11);
 				set10.setType(m);
 				set10.setData((byte) ran8);
 				set11.setType(m);
 				set11.setData((byte) ran9);
 				set12.setType(m);
 				set12.setData((byte) ran10);
 				//
 				Block set13 = set12.getRelative(BlockFace.DOWN, 1);
 				Block set14 = set13.getRelative(BlockFace.DOWN, 1);
 				Block set15 = set14.getRelative(BlockFace.DOWN, 1);
 				Block set16 = set15.getRelative(BlockFace.DOWN, 1);
 				int ran12 = gen.nextInt(3);
 				int ran13 = gen.nextInt(3);
 				int ran14 = gen.nextInt(3);
 				int ran15 = gen.nextInt(3);
 				set13.setType(m);
 				set13.setData((byte) ran12);
 				set14.setType(m);
 				set14.setData((byte) ran13);
 				set15.setType(m);
 				set15.setData((byte) ran14);
 				set16.setType(m);
 				set16.setData((byte) ran15);
 				
 				if (x == a-1) {
 					int ran16 = gen.nextInt(3);
 					clr1.setType(m);
 					clr1.setData((byte) ran16);
 					
 					clr2.setType(Material.IRON_FENCE);
 					clr3.setType(Material.IRON_FENCE);
 					clr6.setType(Material.IRON_FENCE);
 
 					int ran17 = gen.nextInt(3);
 					clr4.setType(m);
 					clr4.setData((byte) ran17);
 					
 					clr5.setType(Material.IRON_FENCE);			
 					clr7.setType(Material.IRON_FENCE);
 					clr7.setType(Material.AIR);				
 					clr8.setType(Material.AIR);	
 					clr9.setType(Material.IRON_FENCE);
 				}
 				newx++;
 				x++;
 			}
 		return a;
 	  }
 	
 	public int nstStraight(Block set, Material m, BlockFace bf){
 		int x = 1;
 		int a = gen.nextInt(40);
 		if (a < 12){
 			a = 12;
 		}
 		while (x < a) {
 			int newx = x-1;
 			//START//
 				Block otherset = set.getRelative(bf, newx);
 				Block set1 = otherset.getRelative(BlockFace.WEST, 1);
 				Block set2 = set1.getRelative(BlockFace.WEST, 1);
 				Block set3 = otherset.getRelative(BlockFace.EAST, 1);
 				Block set4 = set3.getRelative(BlockFace.EAST, 1);
 				Block clr1 = set3.getRelative(BlockFace.UP, 1);
 				Block clr2 = clr1.getRelative(BlockFace.UP, 1);
 				Block clr3 = clr2.getRelative(BlockFace.UP, 1);
 				Block clr4 = set1.getRelative(BlockFace.UP, 1);
 				Block clr5 = clr4.getRelative(BlockFace.UP, 1);
 				Block clr6 = clr5.getRelative(BlockFace.UP, 1);
 				Block clr7 = otherset.getRelative(BlockFace.UP, 1);
 				Block clr8 = clr7.getRelative(BlockFace.UP, 1);
 				Block clr9 = clr8.getRelative(BlockFace.UP, 1);
 								
 				clr1.setType(Material.WATER);
 				clr2.setType(Material.AIR);
 				int i1 = gen.nextInt(10);
 				if (i1 == 3){
 					clr3.setType(Material.WEB);	
 				}else if (i1 == 4){
 					Block otherside = clr3.getRelative(BlockFace.WEST, 2);
 					otherside.setType(Material.WEB);
 					clr3.setType(Material.AIR);
 				}else {
 					clr3.setType(Material.AIR);
 				}
 				clr6.setType(Material.AIR);
 				clr4.setType(Material.WATER);
 				clr5.setType(Material.AIR);				
 				int i3 = gen.nextInt(10);
 				int i4 = gen.nextInt(5);
 				if (i3 == 2){
 					if (i4 == 2){
 						byte flags = (byte) (0x2);
 						clr7.setTypeIdAndData(97, flags, true);
 					}
 					else {
 						int ran16 = gen.nextInt(3);
 						clr7.setType(m);
 						clr7.setData((byte) ran16);
 					}
 				}
 				else {
 					clr7.setType(Material.WATER);
 				}				
 				clr8.setType(Material.AIR);	
 				clr9.setType(Material.AIR);
 
 				Block side1 = clr7.getRelative(BlockFace.EAST, 1);
 				int side1ran = gen.nextInt(3);
 				side1.setType(m);
 				side1.setData((byte) side1ran);
 
 //				Block side2 = clr7.getRelative(BlockFace.WEST, 1);
 //				int side2ran = gen.nextInt(3);
 //				side2.setType(m);
 //				side2.setData((byte) side2ran);
 				
 				int other = gen.nextInt(3);
 				otherset.setType(m);
 				otherset.setData((byte) other);
 				
 				int ran2 = gen.nextInt(3);
 				set3.setType(m);
 				set3.setData((byte) ran2);
 				
 				int ran4 = gen.nextInt(3);
 				set4.setType(m);
 				set4.setData((byte) ran4);
 				
 				int ran = gen.nextInt(3);
 				set1.setType(m);
 				set1.setData((byte) ran);
 				
 				int ran1 = gen.nextInt(3);;
 				set2.setType(m);
 				set2.setData((byte) ran1);
 				//
 				Block set5 = set4.getRelative(BlockFace.UP, 1);
 				Block set6 = set5.getRelative(BlockFace.UP, 1);
 				Block set7 = set6.getRelative(BlockFace.UP, 1);
 				Block set8 = set7.getRelative(BlockFace.UP, 1);
 				int ran3 = gen.nextInt(3);
 				int ran7 = gen.nextInt(3);
 				int ran5 = gen.nextInt(3);
 				int ran6 = gen.nextInt(3);
 				set5.setType(m);
 				set5.setData((byte) ran3);
 				set6.setType(m);
 				set6.setData((byte) ran7);
 				set7.setType(m);
 				int rtorch1 = gen.nextInt(15);
 				if (rtorch1 == 1){
 					byte flags = (byte) (0x1);
 					Block torch = set7.getRelative(BlockFace.WEST, 1);
					torch.setTypeIdAndData(76, flags, true);
 				}
 				set7.setData((byte) ran5);
 				set8.setType(m);
 				set8.setData((byte) ran6);
 				//
 				Block set9 = set8.getRelative(BlockFace.WEST, 1);
 				Block set10 = set9.getRelative(BlockFace.WEST, 1);
 				Block set11 = set10.getRelative(BlockFace.WEST, 1);
 				Block set12 = set11.getRelative(BlockFace.WEST, 1);
 				int ran11 = gen.nextInt(3);
 				int ran8 = gen.nextInt(3);
 				int ran9 = gen.nextInt(3);
 				int ran10 = gen.nextInt(3);
 				set9.setType(m);
 				set9.setData((byte) ran11);
 				set10.setType(m);
 				set10.setData((byte) ran8);
 				set11.setType(m);
 				set11.setData((byte) ran9);
 				set12.setType(m);
 				set12.setData((byte) ran10);
 				//
 				Block set13 = set12.getRelative(BlockFace.DOWN, 1);
 				Block set14 = set13.getRelative(BlockFace.DOWN, 1);
 				Block set15 = set14.getRelative(BlockFace.DOWN, 1);
 				Block set16 = set15.getRelative(BlockFace.DOWN, 1);
 				int ran12 = gen.nextInt(3);
 				int ran13 = gen.nextInt(3);
 				int ran14 = gen.nextInt(3);
 				int ran15 = gen.nextInt(3);
 				set13.setType(m);
 				set13.setData((byte) ran12);
 				set14.setType(m);
 				set14.setData((byte) ran13);
 				set15.setType(m);
 				set15.setData((byte) ran14);
 				set16.setType(m);
 				set16.setData((byte) ran15);
 						
 				if (x == a-1) {
 					
 					int ran16 = gen.nextInt(3);
 					clr1.setType(m);
 					clr1.setData((byte) ran16);
 					
 					clr2.setType(Material.IRON_FENCE);
 					clr3.setType(Material.IRON_FENCE);
 					clr6.setType(Material.IRON_FENCE);
 
 					int ran17 = gen.nextInt(3);
 					clr4.setType(m);
 					clr4.setData((byte) ran17);
 					
 					clr5.setType(Material.IRON_FENCE);			
 					clr7.setType(Material.IRON_FENCE);
 					clr7.setType(Material.AIR);				
 					clr8.setType(Material.AIR);	
 					clr9.setType(Material.IRON_FENCE);
 				}
 				newx++;
 				x++;
 			}
 		return a;
 	  }
 
 }
