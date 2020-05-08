 
 package test1;
 
 import java.util.Random;
 
 /**
  * @author cesarcruz
  *
  */
 public class RandomBoat {
 
 	private Random rand;
 	private boolean check1 = false, check2 = false;
 	private int[] available;
 	private int boatSerial;
 	private int boatAlignment;
 	public RandomBoat(){
 		rand = new Random();
 	}
 
 	/**
 	 * Given the number of ships, places random ships in the current Grid at play.
 	 * @param k Number of ships
 	 * @param rows Number of Rows in a grid
 	 * @param columns Number of Columns in a grid
 	 * @param currentGrid Currently selected grid
 	 * @param level Currently selected level
 	 */
 	public void placeRandomBoats(int k, int rows, int columns, TheGrid currentGrid, boolean level){
 		available = new int[k];
 		for(int i = 0; i < k; i++){
 			if(!level)
 				boatAlignment = rand.nextInt(2) + 1;
 			else
 				boatAlignment = rand.nextInt(3) + 1;
 			/**
 			 * Places a boat with random coordinates horizontally 
 			 */
			for (int j = 0; j < available.length; j++) 
				System.out.print(available[j] + " ");
			System.out.println();
 			if(boatAlignment == 1){
 
 				while(!check1){
 
 					int x = rand.nextInt(rows);
 					int y1 = rand.nextInt(columns);
 					int y2;
 					do{
 						if(y1 >= columns/2){
 							y1 = y1 - (columns/2);
 							y2 = rand.nextInt(k + 2) + y1;
 						}
 
 						else{
 							y2 = rand.nextInt(k + 2) + y1;
 						}
 						for(int j = 0; j < available.length; j++){
 							if(((y2 - y1)+1) == available[j] && ((y2 - y1)+1) > 1){
 								check2 = false;
 								break;
 							}
 							check2 = true;
 						}
					}while(!check2 && y2-y1 >= 2);
 
 					boatSerial = (y2 - y1) + 1;
 					currentGrid.setSerial(boatSerial);
 					check1 = currentGrid.addBoatHorizontal(x, y1, y2);
 
 				}
 				check1 = false;
 				check2 = false;
 				available[i] = boatSerial;
 			}
 			/**
 			 * Places a boat with random coordinates vertically
 			 */
 			else if(boatAlignment == 2){
 
 				while(!check1){
 					int y = rand.nextInt(columns);
 					int x1 = rand.nextInt(rows);
 					int x2;
 					do{
 						if(x1 >= rows/2){
 							x1 = x1 - (rows/2);
 							x2 = rand.nextInt(k + 2) + x1;
 						}
 
 						else{
 							x2 = rand.nextInt(k + 2) + x1;
 						}
 						for(int j = 0; j < available.length; j++){
 							if(((x2 - x1)+1) == available[j] && ((x2 - x1)+1) > 1){
 								check2 = false;
 								break;
 							}
 							check2 = true;
 						}
					}while(!check2 && x2-x1 >= 2);
 
 					boatSerial = (x2 - x1) + 1;
 
 					currentGrid.setSerial(boatSerial);
 					check1 = currentGrid.addBoatVertical(y, x1, x2);
 				}
 				check1 = false;
 				check2 = false;
 				available[i] = boatSerial;
 			}
 			/**
 			 * Places a boat with random coordinates diagonally
 			 */
 			else if(boatAlignment == 3){
 
 				while(!check1){
 
 					while(!check2){
 
 						boatSerial = rand.nextInt((k + 1))+2;
 
 						for(int j = 0; j < available.length; j++){
 							if(boatSerial == available[j] && boatSerial > 1){
 								check2 = false;
 								break;
 							}
 							check2 = true;
 						}
 					}
 
 
 					int x;
 					int y;
 					if(rows == boatSerial)
 						x = 0;
 					else
 						x = rand.nextInt(rows - boatSerial);
 					if(columns == boatSerial)
 						y = 0;
 					else
 						y = rand.nextInt(columns - boatSerial);
 
 					currentGrid.setSerial(boatSerial);
 					check1 = currentGrid.addBoatDiagonal(x, y, true);
 				}
 
 				check1 = false;
 				check2 = false;
 				available[i] = boatSerial;
 			}
 		}
 	}
 }
 
 
