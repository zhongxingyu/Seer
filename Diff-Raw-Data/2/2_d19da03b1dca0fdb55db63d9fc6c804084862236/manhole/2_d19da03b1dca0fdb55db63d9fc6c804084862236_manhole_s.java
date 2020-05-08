 package me.hammale.Sewer;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 public class manhole {
 
 	Random gen = new Random();
 
 	int i = 1;
 	
 	public int man1(Block set, Material m, BlockFace bf){
 		int x = 1;
 		int a = gen.nextInt(10) * 2;
 		if (a < 12){
 			a = 12;
 		}
 		BlockFace up = BlockFace.UP;
 		Block setter = set.getRelative(BlockFace.UP, 1);
 
 		while (setter.getType() != (Material.AIR)) {
 				int newx = x-1;
 				setter = set.getRelative(BlockFace.UP, x);
 				//START//
 				Block otherset = set.getRelative(up, newx);
 				Block set1 = otherset.getRelative(BlockFace.NORTH, 1);
 				Block set2 = set1.getRelative(BlockFace.NORTH, 1);
 				Block set3 = set2.getRelative(BlockFace.WEST, 1);
 				Block set4 = set3.getRelative(BlockFace.WEST, 1);
 				Block set5 = set4.getRelative(BlockFace.SOUTH, 1);
 				Block set6 = set5.getRelative(BlockFace.SOUTH, 1);
 				Block set8 = set6.getRelative(BlockFace.EAST, 1);
 				Block set9 = set8.getRelative(BlockFace.EAST, 1);
 								
 				Block clr1 = set1.getRelative(BlockFace.WEST, 1);
 								
 				clr1.setType(Material.AIR);
 
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
 				int ran3 = gen.nextInt(3);
 				int ran7 = gen.nextInt(3);
 				int ran5 = gen.nextInt(3);
 				int ran6 = gen.nextInt(3);
 				set5.setType(m);
 				int rtorch1 = gen.nextInt(5);
 				if (rtorch1 == 1){
					byte flags = (byte)4;
 					set5.setType(Material.TORCH);
 					set5.setData(flags);
 				}
 				set5.setData((byte) ran3);
 				set6.setType(m);
 				set6.setData((byte) ran7);
 				set8.setType(m);
 				set8.setData((byte) ran6);
 				//
 				int ran11 = gen.nextInt(3);
 				int ran8 = gen.nextInt(3);
 				int ran9 = gen.nextInt(3);
 				int ran10 = gen.nextInt(3);
 				set1.setType(m);
 				set1.setData((byte) ran11);
 				set2.setType(m);
 				set2.setData((byte) ran8);
 				set3.setType(m);
 				set3.setData((byte) ran9);
 				set4.setType(m);
 				set4.setData((byte) ran10);
 				//
 				int ran12 = gen.nextInt(3);
 				set9.setType(m);
 				set9.setData((byte) ran12);
 				
 				Block setter2 = setter.getRelative(BlockFace.UP, 1);
 				
 				if (setter2.getType() == (Material.AIR)) {
 					
 					if (i == 2){
 					clr1.setType(Material.IRON_FENCE);
 					i = 1;
 					}else{
 						byte flags = ( byte )3;
 						clr1.setType(Material.LADDER);
 						clr1.setData(flags);
 						i++;
 					}
 					
 				} else {
 					byte flags = ( byte )3;
 					clr1.setType(Material.LADDER);
 					clr1.setData(flags);
 				}
 				
 								
 				newx++;
 				x++;
 			}
 		return a;
 	  }
 	
 	
 	public int man2(Block set, Material m, BlockFace bf){
 		int x = 1;
 		int a = gen.nextInt(35);
 		if (a < 20){
 			a = 20;
 		}
 		int d = 1;
 		while (d < a) {
 				//START//
 				Block otherset = set.getRelative(BlockFace.DOWN, d);
 				Block set1 = otherset.getRelative(BlockFace.NORTH, 1);
 				Block set2 = set1.getRelative(BlockFace.NORTH, 1);
 				Block set3 = set2.getRelative(BlockFace.WEST, 1);
 				Block set4 = set3.getRelative(BlockFace.WEST, 1);
 				Block set5 = set4.getRelative(BlockFace.SOUTH, 1);
 				Block set6 = set5.getRelative(BlockFace.SOUTH, 1);
 				Block set8 = set6.getRelative(BlockFace.EAST, 1);
 				Block set9 = set8.getRelative(BlockFace.EAST, 1);
 								
 				Block clr1 = set1.getRelative(BlockFace.WEST, 1);
 								
 				clr1.setType(Material.AIR);
 
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
 				int ran3 = gen.nextInt(3);
 				int ran7 = gen.nextInt(3);
 				int ran5 = gen.nextInt(3);
 				int ran6 = gen.nextInt(3);
 				set5.setType(m);
 				int rtorch1 = (int) gen.nextInt(5);
 				if (rtorch1 == 1){
 					byte flags = (byte)5;
 					set5.setType(Material.TORCH);
 					set5.setData(flags);
 				}
 				set5.setData((byte) ran3);
 				set6.setType(m);
 				set6.setData((byte) ran7);
 				set8.setType(m);
 				set8.setData((byte) ran6);
 				//
 				int ran11 = gen.nextInt(3);
 				int ran8 = gen.nextInt(3);
 				int ran9 = gen.nextInt(3);
 				int ran10 = gen.nextInt(3);
 				set1.setType(m);
 				set1.setData((byte) ran11);
 				set2.setType(m);
 				set2.setData((byte) ran8);
 				set3.setType(m);
 				set3.setData((byte) ran9);
 				set4.setType(m);
 				set4.setData((byte) ran10);
 				//
 				int ran12 = gen.nextInt(3);
 				set9.setType(m);
 				set9.setData((byte) ran12);
 										
 					if (d == 1){
 					clr1.setType(Material.IRON_FENCE);					
 					}else{
 						byte flags = ( byte )3;
 						clr1.setType(Material.LADDER);
 						clr1.setData(flags);
 						i++;
 					}						
 				x++;
 				d++;
 			}
 		return a;
 	  }
 	
 }
