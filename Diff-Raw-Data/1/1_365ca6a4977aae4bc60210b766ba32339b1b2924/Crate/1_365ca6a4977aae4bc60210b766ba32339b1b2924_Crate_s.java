 package si.meansoft.logisticraft.common.core;
 
 import si.meansoft.logisticraft.common.blocks.LCBlocks;
 import net.minecraft.src.Block;
 import net.minecraft.src.Material;
 import net.minecraft.src.World;
 
 public class Crate {
 	
 	/* True == OK, False == NOT OK */
 	
 	public static boolean checkIce(World world, int x, int y, int z) {
 		int size = 3;
 		 for (int var5 = x - size; var5 <= x + size; ++var5) {
 	            for (int var6 = y - size; var6 <= y + size; ++var6) {
 	                for (int var7 = z - size; var7 <= z + size; ++var7) {
 	                    if (world.getBlockId(var5, var6, var7) == Block.ice.blockID || world.getBlockId(var5, var6, var7) == Block.blockSnow.blockID ||  world.getBlockId(var5, var6, var7) == Block.snow.blockID) {
 	                        return true;
 	                    }
 	                }
 	            }
 	        }
 
 	        return false;
 	}
 	
 	public static boolean checkWater(World world, int x, int y, int z) {
 		Material up = world.getBlockMaterial(x, y + 1, z);
 		Material dn = world.getBlockMaterial(x, y - 1, z);
 		Material so = world.getBlockMaterial(x - 1, y, z);
 		Material no = world.getBlockMaterial(x + 1, y, z);
 		Material ea = world.getBlockMaterial(x, y, z - 1);
 		Material we = world.getBlockMaterial(x, y, z + 1);
 		
 		Material ma = Material.water;
 		
 		int md = world.getBlockMetadata(x, y, z);
 		int bl = world.getBlockId(x, y, z);
 		
 		if((bl==LCBlocks.crate.blockID && md!=0) || (bl==LCBlocks.box.blockID && md!=14)) {
 			if((up == ma) || (dn == ma) || (so == ma) || (no == ma) || (ea == ma) || (we == ma)){
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public static boolean checkRain(World world, int x, int y, int z){
 		int bl = world.getBlockId(x, y, z);
 		int md = world.getBlockMetadata(x, y, z);
 		boolean blockAbove = false;
 		y++;
 		
 		if(!world.isRemote && world.isRaining()) {
			System.out.println("It is raining");
 			if((bl==LCBlocks.crate.blockID && (md!=0 || md!=14 || md!=7)) || (bl==LCBlocks.box.blockID && (md!=13 || md!=6 || md!=14))){
 				while (y<256) {
 					if(world.getBlockId(x, y, z) != 0) {
 						y++;
 						blockAbove = true;
 						break;
 					}
 					else {
 						y++;
 					}
 				}
 				return blockAbove;
 			}
 			else {
 				return false;
 			}
 		}
 		else {
 			return false;
 
 		}
 	}
 }
