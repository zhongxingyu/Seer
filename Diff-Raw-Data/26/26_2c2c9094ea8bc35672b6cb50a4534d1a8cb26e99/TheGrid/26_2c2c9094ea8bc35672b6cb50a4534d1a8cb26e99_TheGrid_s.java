 package test1;
 
 
 /**
  * @author cesarcruz
  *
  */
 public class TheGrid {
 
 		public Tiles[][]  theGrid = new Tiles[10][10];
 		private static int numberOfGrids = 2;
 		private static final int WIDTH_OF_GRID = 600;
 		private static final int HEIGHT_OF_GRID = 300;
 		private static int numberOfColumns = 10;
 		private static int numberOfRows = 10;
 		private static int gridObjects = 0;
 		
 		/**
 		 * Creates a Grid object that in itself is an array of Tiles, each contains its location. 
 		 */
 		public TheGrid(){
 			int topX = ((WIDTH_OF_GRID/numberOfGrids)/(numberOfRows));
 			int topY = (HEIGHT_OF_GRID)/(numberOfColumns);
 			int offSet = (WIDTH_OF_GRID/numberOfGrids)*gridObjects;
 			for(int i = 0; i < 10; i++){
 				for(int j = 0; j < 10; j++){
 					theGrid[i][j] = new Tiles();
 					theGrid[i][j].addLocation(i, j);
 					theGrid[i][j].setRectangleBounds((i+1)*topX + offSet, ((j+1)*topY) - 15);
 					System.out.println((i+1)*topX + offSet);
 				}
 			}
			//gridObjects++;
 		}
 		
 		/**
 		 * @param x will be the size of the matrix (x*x)
 		 */
 		public TheGrid(int x){
 			int topX = ((WIDTH_OF_GRID/numberOfGrids)/(numberOfRows));
 			int topY = (HEIGHT_OF_GRID)/(numberOfColumns);
 			int offSet = (WIDTH_OF_GRID/numberOfGrids)*gridObjects;
 			for(int i = 0; i < x; i++){
 				for(int j = 0; j < x; j++){
 					theGrid[i][j] = new Tiles();
 					theGrid[i][j].addLocation(i, j);
 					theGrid[i][j].setRectangleBounds((i+1)*topX + offSet, ((j+1)*topY) - 15);
 				}
 			}
 			gridObjects++;
 		}
 		
 		/**
 		 * 
 		 * @param x1 starting point of the boat in the x coordinate
 		 * @param x2 ending point of the boat in the x coordinate
 		 * @param y the number of the row
 		 */
 		public void addBoatVertical(int x1, int x2, int y){
 			int boatSerial = x2 - x1;
 			for(int i = 0; i < 10; i++){
 				for(int j = 0; j < 10; j++){
 					if(x1 >= i && x2 <= i && j == y)
 					theGrid[i][j].placeBoat(i, j);
 					theGrid[i][j].setSerial(boatSerial);
 				}
 			}
 		}
 		/**
 		 * @param x the number of the column
 		 * @param y1 the starting point of the boat in the y coordinate
 		 * @param y2 the ending point of the boat in the y coordinate
 		 */
 		public void addBoatHorizontal(int x, int y1, int y2){
 			int boatSerial = y2 - y1; 
 			for(int i = 0; i < 10; i++){
 				for(int j = 0; j < 10; j++){
 					if(y1 >= j && y2 <= j && x == i)
 						theGrid[i][j].placeBoat(i, j);
 						theGrid[i][j].setSerial(boatSerial);
 				}
 			}
 		}
 		public void numberOfGrids(int x){
 			numberOfGrids = x;
 		}
 }
