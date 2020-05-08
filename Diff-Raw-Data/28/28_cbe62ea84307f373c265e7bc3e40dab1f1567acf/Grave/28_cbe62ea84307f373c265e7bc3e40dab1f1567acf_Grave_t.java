 package me.hammale.Sewer;
 
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.ItemStack;
 
 public class Grave {
 	Random gen = new Random();
 	public int grave(Block set, Material m, BlockFace bf){
 		int a = 18;
 		for (int x = 0; x < a; x++) {
 
 			//Define the blocks.
 
 			Block otherset = set.getRelative(bf, x);
 			Block set1 = otherset.getRelative(BlockFace.WEST, 1);
 			Block set2 = otherset.getRelative(BlockFace.WEST, 2);
 			Block set3 = otherset.getRelative(BlockFace.WEST, 3);
 			Block set4 = otherset.getRelative(BlockFace.WEST, 4);
 			Block set5 = otherset.getRelative(BlockFace.WEST, 5);
 			Block set6 = otherset.getRelative(BlockFace.WEST, 6);
 			Block set7 = otherset.getRelative(BlockFace.EAST, 1);
 			Block set8 = otherset.getRelative(BlockFace.EAST, 2);
 			Block set9 = otherset.getRelative(BlockFace.EAST, 3);
 			Block set10 = otherset.getRelative(BlockFace.EAST, 4);
 			Block set11 = otherset.getRelative(BlockFace.EAST, 5);
 			Block set12 = otherset.getRelative(BlockFace.EAST, 6);
 			Block set13 = set6.getRelative(BlockFace.UP, 1);
 			Block set14 = set6.getRelative(BlockFace.UP, 2);
 			Block set15 = set6.getRelative(BlockFace.UP, 3);
 			Block set16 = set6.getRelative(BlockFace.UP, 4);
 			Block set17 = set6.getRelative(BlockFace.DOWN, 1);
 			Block set18 = set6.getRelative(BlockFace.DOWN, 2);
 			Block set19 = set6.getRelative(BlockFace.DOWN, 3);
 			Block set20 = set19.getRelative(BlockFace.EAST, 1);
 			Block set21 = set19.getRelative(BlockFace.EAST, 2);
 			Block set22 = set19.getRelative(BlockFace.EAST, 3);
 			Block set23 = set19.getRelative(BlockFace.EAST, 4);
 			Block set24 = set19.getRelative(BlockFace.EAST, 5);
 			Block set25 = set19.getRelative(BlockFace.EAST, 6);
 			Block set26 = set19.getRelative(BlockFace.EAST, 7);
 			Block set27 = set19.getRelative(BlockFace.EAST, 8);
 			Block set28 = set19.getRelative(BlockFace.EAST, 9);
 			Block set29 = set19.getRelative(BlockFace.EAST, 10);
 			Block set30 = set19.getRelative(BlockFace.EAST, 11);
 			Block set31 = set19.getRelative(BlockFace.EAST, 12);
 			Block set32 = set18.getRelative(BlockFace.EAST, 1);
 			Block set33 = set18.getRelative(BlockFace.EAST, 2);
 			Block set34 = set18.getRelative(BlockFace.EAST, 3);
 			Block set35 = set18.getRelative(BlockFace.EAST, 4);
 			Block set36 = set18.getRelative(BlockFace.EAST, 5);
 			Block set37 = set18.getRelative(BlockFace.EAST, 6);
 			Block set38 = set18.getRelative(BlockFace.EAST, 7);
 			Block set39 = set18.getRelative(BlockFace.EAST, 8);
 			Block set40 = set18.getRelative(BlockFace.EAST, 9);
 			Block set41 = set18.getRelative(BlockFace.EAST, 10);
 			Block set42 = set18.getRelative(BlockFace.EAST, 11);
 			Block set43 = set18.getRelative(BlockFace.EAST, 12);
 			Block set44 = set17.getRelative(BlockFace.EAST, 1);
 			Block set45 = set17.getRelative(BlockFace.EAST, 2);
 			Block set46 = set17.getRelative(BlockFace.EAST, 3);
 			Block set47 = set17.getRelative(BlockFace.EAST, 4);
 			Block set48 = set17.getRelative(BlockFace.EAST, 5);
 			Block set49 = set17.getRelative(BlockFace.EAST, 6);
 			Block set50 = set17.getRelative(BlockFace.EAST, 7);
 			Block set51 = set17.getRelative(BlockFace.EAST, 8);
 			Block set52 = set17.getRelative(BlockFace.EAST, 9);
 			Block set53 = set17.getRelative(BlockFace.EAST, 10);
 			Block set54 = set17.getRelative(BlockFace.EAST, 11);
 			Block set55 = set17.getRelative(BlockFace.EAST, 12);
 			Block set56 = set13.getRelative(BlockFace.EAST, 1);
 			Block set57 = set13.getRelative(BlockFace.EAST, 2);
 			Block set58 = set13.getRelative(BlockFace.EAST, 3);
 			Block set59 = set13.getRelative(BlockFace.EAST, 4);
 			Block set60 = set13.getRelative(BlockFace.EAST, 5);
 			Block set61 = set13.getRelative(BlockFace.EAST, 6);
 			Block set62 = set13.getRelative(BlockFace.EAST, 7);
 			Block set63 = set13.getRelative(BlockFace.EAST, 8);
 			Block set64 = set13.getRelative(BlockFace.EAST, 9);
 			Block set65 = set13.getRelative(BlockFace.EAST, 10);
 			Block set66 = set13.getRelative(BlockFace.EAST, 11);
 			Block set67 = set13.getRelative(BlockFace.EAST, 12);
 			Block set68 = set14.getRelative(BlockFace.EAST, 1);
 			Block set69 = set14.getRelative(BlockFace.EAST, 2);
 			Block set70 = set14.getRelative(BlockFace.EAST, 3);
 			Block set71 = set14.getRelative(BlockFace.EAST, 4);
 			Block set72 = set14.getRelative(BlockFace.EAST, 5);
 			Block set73 = set14.getRelative(BlockFace.EAST, 6);
 			Block set74 = set14.getRelative(BlockFace.EAST, 7);
 			Block set75 = set14.getRelative(BlockFace.EAST, 8);
 			Block set76 = set14.getRelative(BlockFace.EAST, 9);
 			Block set77 = set14.getRelative(BlockFace.EAST, 10);
 			Block set78 = set14.getRelative(BlockFace.EAST, 11);
 			Block set79 = set14.getRelative(BlockFace.EAST, 12);
 			Block set80 = set15.getRelative(BlockFace.EAST, 1);
 			Block set81 = set15.getRelative(BlockFace.EAST, 2);
 			Block set82 = set15.getRelative(BlockFace.EAST, 3);
 			Block set83 = set15.getRelative(BlockFace.EAST, 4);
 			Block set84 = set15.getRelative(BlockFace.EAST, 5);
 			Block set85 = set15.getRelative(BlockFace.EAST, 6);
 			Block set86 = set15.getRelative(BlockFace.EAST, 7);
 			Block set87 = set15.getRelative(BlockFace.EAST, 8);
 			Block set88 = set15.getRelative(BlockFace.EAST, 9);
 			Block set89 = set15.getRelative(BlockFace.EAST, 10);
 			Block set90 = set15.getRelative(BlockFace.EAST, 11);
 			Block set91 = set15.getRelative(BlockFace.EAST, 12);
 			Block set92 = set16.getRelative(BlockFace.EAST, 1);
 			Block set93 = set16.getRelative(BlockFace.EAST, 2);
 			Block set94 = set16.getRelative(BlockFace.EAST, 3);
 			Block set95 = set16.getRelative(BlockFace.EAST, 4);
 			Block set96 = set16.getRelative(BlockFace.EAST, 5);
 			Block set97 = set16.getRelative(BlockFace.EAST, 6);
 			Block set98 = set16.getRelative(BlockFace.EAST, 7);
 			Block set99 = set16.getRelative(BlockFace.EAST, 8);
 			Block set100 = set16.getRelative(BlockFace.EAST, 9);
 			Block set101 = set16.getRelative(BlockFace.EAST, 10);
 			Block set102 = set16.getRelative(BlockFace.EAST, 11);
 			Block set103 = set16.getRelative(BlockFace.EAST, 12);
 
 			//Assign the moss stone and sign direction.
 
 			byte moss = (byte) (0x1);
 			byte sdir = (byte) (0xC);
 
 			//Start bottom floor.
 
 			set19.setTypeIdAndData(98, moss, true);
 			set20.setTypeIdAndData(98, moss, true);
 			set21.setTypeIdAndData(98, moss, true);
 			set22.setTypeIdAndData(98, moss, true);
 			set23.setTypeIdAndData(98, moss, true);
 			set24.setTypeIdAndData(98, moss, true);
 			set25.setTypeIdAndData(98, moss, true);
 			set26.setTypeIdAndData(98, moss, true);
 			set27.setTypeIdAndData(98, moss, true);
 			set28.setTypeIdAndData(98, moss, true);
 			set29.setTypeIdAndData(98, moss, true);
 			set30.setTypeIdAndData(98, moss, true);
 			set31.setTypeIdAndData(98, moss, true);
 
 			//Start casket floor.
 
 			if (x == 2 || x == 3 || x == 5 || x == 6 ||x == 8 || x == 9 ||x == 11 || x == 12 || x == 14 || x ==15) {
 
 				set18.setTypeIdAndData(98, moss, true);
 				set32.setTypeIdAndData(98, moss, true);
 				set33.setType(Material.WOOD);
 				set34.setTypeIdAndData(98, moss, true);
 				set35.setType(Material.WOOD);
 				set36.setTypeIdAndData(98, moss, true);
 				set37.setType(Material.WOOD);
 				set38.setTypeIdAndData(98, moss, true);
 				set39.setType(Material.WOOD);
 				set40.setTypeIdAndData(98, moss, true);
 				set41.setType(Material.WOOD);
 				set42.setTypeIdAndData(98, moss, true);
 				set43.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start casket floor level.
 			
 			else {
 
 				set18.setTypeIdAndData(98, moss, true);
 				set32.setTypeIdAndData(98, moss, true);
 				set33.setTypeIdAndData(98, moss, true);
 				set34.setTypeIdAndData(98, moss, true);
 				set35.setTypeIdAndData(98, moss, true);
 				set36.setTypeIdAndData(98, moss, true);
 				set37.setTypeIdAndData(98, moss, true);
 				set38.setTypeIdAndData(98, moss, true);
 				set39.setTypeIdAndData(98, moss, true);
 				set40.setTypeIdAndData(98, moss, true);
 				set41.setTypeIdAndData(98, moss, true);
 				set42.setTypeIdAndData(98, moss, true);
 				set43.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start casket inside.
 
 			if (x == 2 || x == 3 || x == 5 || x == 6 ||x == 8 || x == 9 ||x == 11 || x == 12 || x == 14 || x ==15) {
 
 				set17.setTypeIdAndData(98, moss, true);
 				set44.setType(Material.WOOD);
 				int ran = gen.nextInt(10);
 
 				if (ran == 0 || ran == 1 || ran == 2) {
 					set45.setType(Material.LAVA);
 				}
 
 				if (ran == 3 || ran == 4 || ran == 5 || ran == 6 ||ran == 7 || ran ==8) {
 					set45.setType(Material.AIR);
 				}
 
 				else {
 					int chestran = gen.nextInt(10);
 
 					for (int i = 1; i <= chestran; i++){
 						int matran = gen.nextInt(11);;
 						Material mat = null;
 						if(matran == 0){
 							mat = Material.COOKED_BEEF;
 						}
 						if(matran == 1){
 							mat = Material.COOKED_CHICKEN;
 						}
 						if(matran == 2){
 							mat = Material.COOKED_FISH;
 						}
 						if(matran == 3){
 							mat = Material.GOLD_INGOT;
 						}
 						if(matran == 4){
 							mat = Material.FLINT_AND_STEEL;
 						}
 						if(matran == 5){
 							mat = Material.SADDLE;
 						}
 						if(matran == 6){
 							mat = Material.BOOKSHELF;
 						}
 						if(matran == 7){
 							mat = Material.CHAINMAIL_HELMET;
 						}
 						if(matran == 8){
 							mat = Material.CHAINMAIL_LEGGINGS;
 						}
 						if(matran == 9){
 							mat = Material.DIAMOND;
 						}
 						if(matran == 10){
 							mat = Material.BUCKET;
 						}
 						int amt = gen.nextInt(10);
 
 						set45.setTypeId(54);
 						Chest chest = (Chest)set45.getState();
 						if (matran <= 4) {
 							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amt) });
 						}
 
 						else {
							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, 1) });
 						}
 					}
 				}
 				set46.setType(Material.WOOD);
 				int ran1 = gen.nextInt(3);
 				if (ran1 == 0) {
 					set47.setType(Material.AIR);
 				}
 
 				else if (ran1 == 1) {
 					set47.setType(Material.LAVA);
 				}
 
 				else {
 					int chestran = gen.nextInt(10);
 
 					for (int i = 1; i <= chestran; i++){
 						int matran = gen.nextInt(11);;
 						Material mat = null;
 						if(matran == 0){
 							mat = Material.COOKED_BEEF;
 						}
 						if(matran == 1){
 							mat = Material.COOKED_CHICKEN;
 						}
 						if(matran == 2){
 							mat = Material.COOKED_FISH;
 						}
 						if(matran == 3){
 							mat = Material.GOLD_INGOT;
 						}
 						if(matran == 4){
 							mat = Material.FLINT_AND_STEEL;
 						}
 						if(matran == 5){
 							mat = Material.SADDLE;
 						}
 						if(matran == 6){
 							mat = Material.BOOKSHELF;
 						}
 						if(matran == 7){
 							mat = Material.CHAINMAIL_HELMET;
 						}
 						if(matran == 8){
 							mat = Material.CHAINMAIL_LEGGINGS;
 						}
 						if(matran == 9){
 							mat = Material.DIAMOND;
 						}
 						if(matran == 10){
 							mat = Material.BUCKET;
 						}
 						int amt = gen.nextInt(10);
 
 						set47.setTypeId(54);
 						Chest chest = (Chest)set47.getState();
 						if (matran <= 4) {
 							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amt) });
 						}
 
 						else {
							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, 1) });
 						}
 					}
 				}
 				set48.setType(Material.WOOD);
 				int ran2 = gen.nextInt(3);
 				if (ran2 == 0) {
 					set49.setType(Material.AIR);
 				}
 
 				else if (ran2 == 1) {
 					set49.setType(Material.LAVA);
 				}
 
 				else {
 					int chestran = gen.nextInt(10);
 
 					for (int i = 1; i <= chestran; i++){
 						int matran = gen.nextInt(11);;
 						Material mat = null;
 						if(matran == 0){
 							mat = Material.COOKED_BEEF;
 						}
 						if(matran == 1){
 							mat = Material.COOKED_CHICKEN;
 						}
 						if(matran == 2){
 							mat = Material.COOKED_FISH;
 						}
 						if(matran == 3){
 							mat = Material.GOLD_INGOT;
 						}
 						if(matran == 4){
 							mat = Material.FLINT_AND_STEEL;
 						}
 						if(matran == 5){
 							mat = Material.SADDLE;
 						}
 						if(matran == 6){
 							mat = Material.BOOKSHELF;
 						}
 						if(matran == 7){
 							mat = Material.CHAINMAIL_HELMET;
 						}
 						if(matran == 8){
 							mat = Material.CHAINMAIL_LEGGINGS;
 						}
 						if(matran == 9){
 							mat = Material.DIAMOND;
 						}
 						if(matran == 10){
 							mat = Material.BUCKET;
 						}
 						int amt = gen.nextInt(10);
 
 						set49.setTypeId(54);
 						Chest chest = (Chest)set49.getState();
 						if (matran <= 4) {
 							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amt) });
 						}
 
 						else {
							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, 1) });
 						}
 					}
 				}
 				set50.setType(Material.WOOD);
 				int ran3 = gen.nextInt(3);
 				if (ran3 == 0) {
 					set51.setType(Material.AIR);
 				}
 
 				else if (ran3 == 1) {
 					set51.setType(Material.LAVA);
 				}
 
 				else {
 					int chestran = gen.nextInt(10);
 
 					for (int i = 1; i <= chestran; i++){
 						int matran = gen.nextInt(11);;
 						Material mat = null;
 						if(matran == 0){
 							mat = Material.COOKED_BEEF;
 						}
 						if(matran == 1){
 							mat = Material.COOKED_CHICKEN;
 						}
 						if(matran == 2){
 							mat = Material.COOKED_FISH;
 						}
 						if(matran == 3){
 							mat = Material.GOLD_INGOT;
 						}
 						if(matran == 4){
 							mat = Material.FLINT_AND_STEEL;
 						}
 						if(matran == 5){
 							mat = Material.SADDLE;
 						}
 						if(matran == 6){
 							mat = Material.BOOKSHELF;
 						}
 						if(matran == 7){
 							mat = Material.CHAINMAIL_HELMET;
 						}
 						if(matran == 8){
 							mat = Material.CHAINMAIL_LEGGINGS;
 						}
 						if(matran == 9){
 							mat = Material.DIAMOND;
 						}
 						if(matran == 10){
 							mat = Material.BUCKET;
 						}
 						int amt = gen.nextInt(10);
 
 						set51.setTypeId(54);
 						Chest chest = (Chest)set51.getState();
 						if (matran <= 4) {
 							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amt) });
 						}
 
 						else {
							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, 1) });
 						}
 					}
 				}
 				set52.setType(Material.WOOD);
 				int ran4 = gen.nextInt(3);
 				if (ran4 == 0) {
 					set53.setType(Material.AIR);
 				}
 
 				else if (ran == 1) {
 					set53.setType(Material.LAVA);
 				}
 
 				else {
 					int chestran = gen.nextInt(10);
 				
 					for (int i = 1; i <= chestran; i++){
 						int matran = gen.nextInt(11);;
 						Material mat = null;
 						if(matran == 0){
 							mat = Material.COOKED_BEEF;
 						}
 						if(matran == 1){
 							mat = Material.COOKED_CHICKEN;
 						}
 						if(matran == 2){
 							mat = Material.COOKED_FISH;
 						}
 						if(matran == 3){
 							mat = Material.GOLD_INGOT;
 						}
 						if(matran == 4){
 							mat = Material.FLINT_AND_STEEL;
 						}
 						if(matran == 5){
 							mat = Material.SADDLE;
 						}
 						if(matran == 6){
 							mat = Material.BOOKSHELF;
 						}
 						if(matran == 7){
 							mat = Material.CHAINMAIL_HELMET;
 						}
 						if(matran == 8){
 							mat = Material.CHAINMAIL_LEGGINGS;
 						}
 						if(matran == 9){
 							mat = Material.DIAMOND;
 						}
 						if(matran == 10){
 							mat = Material.BUCKET;
 						}
 						int amt = gen.nextInt(10);
 
 						set53.setTypeId(54);
 						Chest chest = (Chest)set53.getState();
 						if (matran <= 4) {
 							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, amt) });
 						}
 
 						else {
							chest.getInventory().addItem(new ItemStack[] { new ItemStack(mat, 1) });
 						}
 					}
 				}
 			set54.setType(Material.WOOD);
 			set55.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start casket inside level.
 
 			else {
 				set17.setTypeIdAndData(98, moss, true);
 				set44.setTypeIdAndData(98, moss, true);
 				set45.setTypeIdAndData(98, moss, true);
 				set46.setTypeIdAndData(98, moss, true);
 				set47.setTypeIdAndData(98, moss, true);
 				set48.setTypeIdAndData(98, moss, true);
 				set49.setTypeIdAndData(98, moss, true);
 				set50.setTypeIdAndData(98, moss, true);
 				set51.setTypeIdAndData(98, moss, true);
 				set52.setTypeIdAndData(98, moss, true);
 				set53.setTypeIdAndData(98, moss, true);
 				set54.setTypeIdAndData(98, moss, true);
 				set55.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start casket ceiling.
 
 			if (x == 2 || x == 3 || x == 5 || x == 6 ||x == 8 || x == 9 ||x == 11 || x == 12 || x == 14 || x ==15) {
 			set6.setTypeIdAndData(98, moss, true);
 			set5.setTypeIdAndData(98, moss, true);
 			set4.setType(Material.WOOD);
 			set3.setTypeIdAndData(98, moss, true);
 			set2.setType(Material.WOOD);
 			set1.setTypeIdAndData(98, moss, true);
 			otherset.setType(Material.WOOD);
 			set7.setTypeIdAndData(98, moss, true);
 			set8.setType(Material.WOOD);
 			set9.setTypeIdAndData(98, moss, true);
 			set10.setType(Material.WOOD);
 			set11.setTypeIdAndData(98, moss, true);
 			set12.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start ground.
 
 			else {
 				set6.setTypeIdAndData(98, moss, true);
 				set5.setTypeIdAndData(98, moss, true);
 				set4.setTypeIdAndData(98, moss, true);
 				set3.setTypeIdAndData(98, moss, true);
 				set2.setTypeIdAndData(98, moss, true);
 				set1.setTypeIdAndData(98, moss, true);
 				otherset.setTypeIdAndData(98, moss, true);
 				set7.setTypeIdAndData(98, moss, true);
 				set8.setTypeIdAndData(98, moss, true);
 				set9.setTypeIdAndData(98, moss, true);
 				set10.setTypeIdAndData(98, moss, true);
 				set11.setTypeIdAndData(98, moss, true);
 				set12.setTypeIdAndData(98, moss, true);
 			}
 
 
 
 			//Generate front and back walls.
 
 			if (x == 0 || x == 17) {
 				set13.setTypeIdAndData(98, moss, true);
 				set56.setTypeIdAndData(98, moss, true);
 				set57.setTypeIdAndData(98, moss, true);
 				set58.setTypeIdAndData(98, moss, true);
 				set59.setTypeIdAndData(98, moss, true);
 				set60.setTypeIdAndData(98, moss, true);
 				set61.setTypeIdAndData(98, moss, true);
 				set62.setTypeIdAndData(98, moss, true);
 				set63.setTypeIdAndData(98, moss, true);
 				set64.setTypeIdAndData(98, moss, true);
 				set65.setTypeIdAndData(98, moss, true);
 				set66.setTypeIdAndData(98, moss, true);
 				set67.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start air.
 
 			else {
 				set13.setTypeIdAndData(98, moss, true);
 				set56.setType(Material.AIR);
 				set57.setType(Material.AIR);
 				set58.setType(Material.AIR);
 				set59.setType(Material.AIR);
 				set60.setType(Material.AIR);
 				set61.setType(Material.AIR);
 				set62.setType(Material.AIR);
 				set63.setType(Material.AIR);
 				set64.setType(Material.AIR);
 				set65.setType(Material.AIR);
 				set66.setType(Material.AIR);
 				set67.setTypeIdAndData(98, moss, true);
 			}
 
 			//Generate front and back walls.
 
 			if (x == 0 || x == 17) {
 				set14.setTypeIdAndData(98, moss, true);
 				set68.setTypeIdAndData(98, moss, true);
 				set69.setTypeIdAndData(98, moss, true);
 				set70.setTypeIdAndData(98, moss, true);
 				set71.setTypeIdAndData(98, moss, true);
 				set72.setTypeIdAndData(98, moss, true);
 				set73.setTypeIdAndData(98, moss, true);
 				set74.setTypeIdAndData(98, moss, true);
 				set75.setTypeIdAndData(98, moss, true);
 				set76.setTypeIdAndData(98, moss, true);
 				set77.setTypeIdAndData(98, moss, true);
 				set78.setTypeIdAndData(98, moss, true);
 				set79.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start air 2.
 
 			else {
 				set14.setTypeIdAndData(98, moss, true);
 				set68.setType(Material.AIR);
 				set69.setType(Material.AIR);
 				set70.setType(Material.AIR);
 				set71.setType(Material.AIR);
 				set72.setType(Material.AIR);
 				set73.setType(Material.AIR);
 				set74.setType(Material.AIR);
 				set75.setType(Material.AIR);
 				set76.setType(Material.AIR);
 				set77.setType(Material.AIR);
 				set78.setType(Material.AIR);
 				set79.setTypeIdAndData(98, moss, true);
 			}
 
 			//Generate front and back walls.
 
 			if (x == 0 || x == 17) {
 				set15.setTypeIdAndData(98, moss, true);
 				set80.setTypeIdAndData(98, moss, true);
 				set81.setTypeIdAndData(98, moss, true);
 				set82.setTypeIdAndData(98, moss, true);
 				set83.setTypeIdAndData(98, moss, true);
 				set84.setTypeIdAndData(98, moss, true);
 				set85.setTypeIdAndData(98, moss, true);
 				set86.setTypeIdAndData(98, moss, true);
 				set87.setTypeIdAndData(98, moss, true);
 				set88.setTypeIdAndData(98, moss, true);
 				set89.setTypeIdAndData(98, moss, true);
 				set90.setTypeIdAndData(98, moss, true);
 				set91.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start air 3.
 
 			else {
 				set15.setTypeIdAndData(98, moss, true);
 				set80.setType(Material.AIR);
 				set81.setType(Material.AIR);
 				set82.setType(Material.AIR);
 				set83.setType(Material.AIR);
 				set84.setType(Material.AIR);
 				set85.setType(Material.AIR);
 				set86.setType(Material.AIR);
 				set87.setType(Material.AIR);
 				set88.setType(Material.AIR);
 				set89.setType(Material.AIR);
 				set90.setType(Material.AIR);
 				set91.setTypeIdAndData(98, moss, true);
 			}
 
 			//Start ceiling.
 
 			set16.setTypeIdAndData(98, moss, true);
 			set92.setTypeIdAndData(98, moss, true);
 			set93.setTypeIdAndData(98, moss, true);
 			set94.setTypeIdAndData(98, moss, true);
 			set95.setTypeIdAndData(98, moss, true);
 			set96.setTypeIdAndData(98, moss, true);
 			set97.setTypeIdAndData(98, moss, true);
 			set98.setTypeIdAndData(98, moss, true);
 			set99.setTypeIdAndData(98, moss, true);
 			set100.setTypeIdAndData(98, moss, true);
 			set101.setTypeIdAndData(98, moss, true);
 			set102.setTypeIdAndData(98, moss, true);
 			set103.setTypeIdAndData(98, moss, true);
 			
 			
 			
 			//Start signs.
 
 			if (x == 4 || x == 7  || x == 10 || x == 13 || x ==16) {
 
 			System.out.println("starting signs...");
 			set13.setTypeIdAndData(98, moss, true);
 			set56.setType(Material.AIR);
 			set57.setTypeIdAndData(63, sdir, true);
 			set58.setType(Material.AIR);
 			set59.setTypeIdAndData(63, sdir, true);
 			set60.setType(Material.AIR);
 			set61.setTypeIdAndData(63, sdir, true);
 			set62.setType(Material.AIR);
 			set63.setTypeIdAndData(63, sdir, true);
 			set64.setType(Material.AIR);
 			set65.setTypeIdAndData(63, sdir, true);
 			set66.setType(Material.AIR);
 			set67.setTypeIdAndData(98, moss, true);
 
 			Sign sign = (Sign)set57.getState();
 			Sign sign1 = (Sign)set59.getState();
 			Sign sign2 = (Sign)set61.getState();
 			Sign sign3 = (Sign)set63.getState();
 			Sign sign4 = (Sign)set65.getState();
 
 			//give text to the signs
 
 			String name = SignText();
 			String name1 = SignText();
 			String name2 = SignText();
 			String name3 = SignText();
 			String name4 = SignText();
 
 			//give signs text
 
 			sign.setLine(0, name);
 			sign1.setLine(0, name1);
 			sign2.setLine(0, name2);
 			sign3.setLine(0, name3);
 			sign4.setLine(0, name4);
 
 			}
 			
 			
 			
 		}
 		return a;
 	}
 
 	public String SignText() {
 		
 		//varible for string
 		String stext = " ";
 
 		//assign random
 
 		int ran = gen.nextInt(30);
 
 		//Assign text to varible
 
 		switch(ran) {
 
 			case 0 : stext = "Dead"; break;
 
 			case 1 : stext = "Billy Bob Joe"; break;
 
 			case 2 : stext = "Gyroninja"; break;
 
 			case 3 : stext = "Hammale"; break;
 
 			case 4 : stext = "Th3 Controller"; break;
 
 			case 5 : stext = "Old man Jenkins"; break;
 
 			case 6 : stext = "You"; break;
 
 			case 7 : stext = "Ima Fail"; break;
 
 			case 8 : stext = "Alive"; break;
 
 			case 9 : stext = "Abby Normal"; break;
 
 			case 10 : stext = "Frank"; break;
 
 			case 11 : stext = "Your mom"; break;
 
 			case 12 : stext = "Jimmy Joe"; break;
 
 			case 13 : stext = "Kamen"; break;
 
 			case 14 : stext = "Wesley"; break;
 
 			case 15 : stext = "Finn"; break;
 
 			case 16 : stext = "Matt"; break;
 
 			case 17 : stext = "Taylor"; break;
 
 			case 18 : stext = "Alex"; break;
 
 			case 19 : stext = "Max"; break;
 
 			case 20 : stext = "Michael"; break;
 
 			case 21 : stext = "Philip"; break;
 
 			case 22 : stext = "Robert"; break;
 
 			case 23 : stext = "Notch"; break;
 
 			case 24 : stext = "Rover"; break;
 
 			case 25 : stext = "Jakob"; break;
 
 			case 26 : stext = "Brandon"; break;
 
 			case 27 : stext = "James"; break;
 
 			case 28 : stext = "Aaron"; break;
 
 			case 29 : stext = "Lucky"; break;
 
 			default : stext = "EROOR"; break;
 
 		}
 
 		return stext;
 	}
 
 }
