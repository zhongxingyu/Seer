 
 
 import java.util.LinkedList;
 import java.util.Random;
 
 /**
  * Generates a board
  * Randomly assigns values, checks they work, reassigns if required
  * @author laura
  *
  */
 
 public class BoardFiller {
 	
 	public BoardFiller(Board b) 
 	{
 		this.b = b;
 	}
 	
 	/*
 	public boolean fillBoardOld(){
 		int i, j, k;
 		int x = 0;
 		Random r = new Random();  
 		LinkedList<Integer> l = new LinkedList<Integer>();
 		int resetPoint;
 		int counter = 0;
 		
 		
 		for(i = 0; i < 9; i++){
 			counter = 0;
 			j = 0;
 			while(j < 9){				
 				k = squareNo(i, j);
 				// TODO THIS IS A NICE PLACE TO WATCH FROM IF YOU WANT TO =)
 				//System.out.printf("\n\n\n\n");
 				//printBoard();
 				//System.out.printf("\n\n\n");
 				//remove to here
 				for(int m = 1; m < 10; m++){
 					l.add(m);
 				}
 				do{
 					x = l.remove(r.nextInt(l.size()));
 				} while(!l.isEmpty() && (b.rowHas(i+1, x) || b.columnHas(j+1, x) || b.squareHas(k+1, x)));
 				
 				if(l.isEmpty()){
 					if(i%3 == 2){
 						resetPoint = ((int)Math.floor(j/3)*3);
 					} else {
 						resetPoint = 0;
 					}
 					for(int p = resetPoint; p < 9; p++){
 						b.removeCellValue(i+1, p+1);
 					}
 					j = resetPoint;
 					counter++;
 					if (counter > 50){
 						b.clear();
 						return false;
 					}
 					continue;
 				}
 				b.setCellValue(i+1, j+1, x);
 				j++;
 			}
 		}
 		printBoard();
 		return true;
 	}*/
 	
 	public boolean fillBoard(){
 		int row, col, square;
 		int x = 0;
 		Random r = new Random();  
 		LinkedList<Integer> l = new LinkedList<Integer>();
 		int counter = 0;
 		
 		row = 0;
 		
 		while(row < 9){
 			counter = 0;
 			col = 0;
 			while(col < 9){				
 				square = squareNo(row, col);
 				// TODO THIS IS A NICE PLACE TO WATCH FROM IF YOU WANT TO =)
 				//System.out.printf("\n\n\n\n");
 				//printBoard();
 				//System.out.printf("\n\n\n");
 				//remove to here
 				for(int m = 1; m < 10; m++){
 					l.add(m);
 				}
 				do{
 					x = l.remove(r.nextInt(l.size()));
 				} while(!l.isEmpty() && (b.rowHas(row+1, x) || b.columnHas(col+1, x) || b.squareHas(square+1, x)));
 				
 				if(l.isEmpty()){
 					col = reset(row, col);
 					counter++;
 					if (counter > ATTEMPT_LIMIT){
 						//TODO test to make sure this still works
 						b.clear();
						row = 0;
 						break;
 					}
 				} else {
 					b.setCellValue(row+1, col+1, x);
 					col++;
 				}
 			}
 			row++;
 		}
 		return true;
 	}
 	
 	/** 
 	 * Unsets the values in the cells up to a reset point based on current progress at board filling
 	 * @param row row the filler got stuck on
 	 * @param col column the filler got stuck on
 	 * @return column the filler has reset to
 	 */
 	private int reset(int row, int col){
 		int resetPoint;
 		if(row%3 == 2){
 			resetPoint = ((int)Math.floor(col/3)*3);
 		} else {
 			resetPoint = 0;
 		}
 		for(int p = resetPoint; p < 9; p++){
 			b.removeCellValue(row+1, p+1);
 		}	
 		return resetPoint;
 	}
 	
 	/**
 	 * The number of the square the given cell is in
 	 * @param row row of cell
 	 * @param column column of cell
 	 * @return square number
 	 */
 	private int squareNo(int row, int column){
 		return ((int)Math.floor(column/3) + (int)Math.floor(row/3)*3);
 	}
 	
 	//FOR DEBUGGING PURPOSES ONLY DO NOT USE
 	//TODO remove after debugging is finished
 	public void printBoard(){
 		int i,j;
 		for(i = 0; i < 9; i++){
 			for(j = 0; j < 9; j++){
 				//System.out.println("checking if " + i + " " + j + " is initially visible");
 				if(b.hasInput(i+1, j+1)){
 					System.out.printf("%d" , b.getCellValue(i+1, j+1));
 				} else {
 					System.out.printf("%d", 0);
 				}
 			}
 			System.out.printf("\n");
 		}
 	}
 	
 	
 	private Board b;
 	private static final int ATTEMPT_LIMIT = 20;
 }
