 package me.hammale.Sewer;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.inventory.ItemStack;
 
 public class hut {
 
 	Random gen = new Random();
 	public int hut1(Block set, Material m, BlockFace bf){
 		int x = 1;
 		int a = 6;
 		int i = 0;
 		while (x < a) {
 			int newx = x-1;
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
 										
 				clr1.setType(Material.AIR);
 				clr2.setType(Material.AIR);
 				clr3.setType(Material.AIR);
 				clr6.setType(Material.AIR);
 				clr4.setType(Material.AIR);
 				clr5.setType(Material.AIR);			
 				clr7.setType(Material.AIR);
 				clr7.setType(Material.AIR);				
 				clr8.setType(Material.AIR);	
 				clr9.setType(Material.AIR);
 
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
 				int ran6 = gen.nextInt(3);
 				set5.setType(m);
 				set5.setData((byte) ran3);
 				set6.setType(m);
 				set6.setData((byte) ran7);
 				set7.setType(m);
 				set7.setData((byte) ran5);
 				set8.setType(m);
 				set8.setData((byte) ran6);
 				//
 				Block set9 = set8.getRelative(BlockFace.WEST, 1);
 				Block set10 = set9.getRelative(BlockFace.WEST, 1);
 				Block set11 = set10.getRelative(BlockFace.WEST, 1);
 				Block set12 = set11.getRelative(BlockFace.WEST, 1);
 				Block set121 = set12.getRelative(BlockFace.WEST, 1);
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
 				set121.setType(m);
 				set121.setData((byte) ran8);
 				//
 				Block set13 = set121.getRelative(BlockFace.DOWN, 1);
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
 				
 				Block clr10 = set12.getRelative(BlockFace.DOWN, 1);
 				Block clr11 = clr10.getRelative(BlockFace.DOWN, 1);
 				Block clr12 = clr11.getRelative(BlockFace.DOWN, 1);
 								
 				clr10.setType(Material.AIR);				
 				clr11.setType(Material.AIR);
 				if (x == 2) {
 					clr12.setTypeId(58);
 				}
 				
 				if (x == 5 || x == 1) {
 					if (x == 5) {
 					clr1.setType(Material.IRON_FENCE);
 					clr2.setType(Material.IRON_FENCE);	
 					}
 					clr3.setType(Material.IRON_FENCE);
 					clr4.setType(Material.IRON_FENCE);
 					clr5.setType(Material.IRON_FENCE);
 					clr6.setType(Material.IRON_FENCE);
 					clr7.setType(Material.IRON_FENCE);			
 					clr8.setType(Material.IRON_FENCE);	
 					clr9.setType(Material.IRON_FENCE);
 					clr10.setType(Material.IRON_FENCE);				
 					clr11.setType(Material.IRON_FENCE);	
 					clr12.setType(Material.IRON_FENCE);
 				}
 				if (x == 1) {
 					Byte data = (byte)(0x8);
 					clr1.setType(Material.WOODEN_DOOR);
 					clr2.setTypeIdAndData(64, data, true);
 				}
 
 				if (x == 3) {
 					clr1.setType(Material.BOOKSHELF);
 					Block book2 = clr1.getRelative(BlockFace.UP,  1);
 					book2.setType(Material.BOOKSHELF);
 				}
 				
 //				if (x == 3 || x ==4) {
 //					if (x==3){
 //						if(bf == BlockFace.SOUTH){
 //							byte direction = ( byte )( 0x2 );
 //					direction = ( byte )( 0x1 );
 //							clr12.setTypeIdAndData( 26, direction, true );
 //						}else{
 //							byte direction = ( byte )( 0x0 );
 //							direction = ( byte )( 0x1 );
 //							clr12.setTypeIdAndData( 26, direction, true );
 //						}
 //					}else{
 //						if(bf == BlockFace.SOUTH){
 //							byte flags = ( byte )8;
 //							flags = ( byte )( flags | 0x2 );
 //							clr12.setTypeIdAndData( 26, flags, true );
 //						}else{
 //							byte flags = ( byte )8;
 //							flags = ( byte )( flags | 0x1 );
 //							clr12.setTypeIdAndData( 26, flags, true );
 //						}
 //					}
 //				}
 				if (x == 3) {
 					clr12.setType(Material.AIR);
 				}
 
 				if (x == 4) {
 					clr12.setType(Material.AIR);
 				}
 				
 				//START CHEST CODE//
 				if (x == 4){
 
					int chestran = gen.nextInt(10);
 								
 					for (i = 1; i <= chestran; i++){
 						int matran = gen.nextInt(10);;
 						Material mat = null;
 						if(matran == 0){
 							mat = Material.BOOK;
 						}
 						if(matran == 1){
 							mat = Material.TNT;
 						}
 						if(matran == 2){
 							mat = Material.ROTTEN_FLESH;
 						}
 						if(matran == 3){
 							mat = Material.ARROW;
 						}
 						if(matran == 4){
 							mat = Material.STONE_SWORD;
 						}
 						if(matran == 5){
 							mat = Material.CAKE;
 						}
 						if(matran == 6){
 							mat = Material.BOW;
 						}
 						if(matran == 7){
 							mat = Material.IRON_PICKAXE;
 						}
 						if(matran == 8){
 							mat = Material.CHAINMAIL_CHESTPLATE;
 						}
 						if(matran == 9){
 							mat = Material.IRON_LEGGINGS;
 						}
 						if(matran == 10){
 							mat = Material.BUCKET;
 						}
 						int amt = gen.nextInt(10);
 	
 						clr1.setTypeId(54);
 						Chest chest = (Chest)clr1.getState();
 						if (matran <= 4) {
 							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amt) });
 						}
 
 						else {
 							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, 0) });
 						}
 					}
 					
 				}
 			    //END CHEST CODE//
 
 				newx++;
 				x++;
 			}
 		return a;
 	  }
 	
 }
