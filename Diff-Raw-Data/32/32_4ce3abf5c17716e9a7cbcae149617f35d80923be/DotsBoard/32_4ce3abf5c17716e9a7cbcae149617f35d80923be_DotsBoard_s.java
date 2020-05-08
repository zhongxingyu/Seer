 package edu.ycp.cs481.ycpgames;
 
 /**
  * Created by brian on 10/28/13.
  */
 public class DotsBoard extends Board {
 	/*
 	 * Origin (0,0)is set at bottom left
 	 */
 	DotsNode [][] grid;
     private int playerOneBoxes;
     private int playerTwoBoxes;
 
     public DotsBoard(){
 		reset();
 	}
 
     public int getPlayerOneBoxes() {
         return playerOneBoxes;
     }
 
     public int getPlayerTwoBoxes() {
         return playerTwoBoxes;
     }
 
     public void reset(){
 		DotsLine sharedLine;
 		grid = new DotsNode[settings.getGridSize()][settings.getGridSize()];
 		for(int x = 0; x < settings.getGridSize(); x++){
 			for(int y = 0; y < settings.getGridSize(); y++){
 				//initialize a grid of DotsNodes
 				grid[x][y] = new DotsNode();
 			}
 		}
 		for(int x = 0; x < settings.getGridSize()-1; x++){
 			for(int y = 0; y < settings.getGridSize()-1; y++){
 				sharedLine = new DotsLine();
                 grid[x+1][y].setLeft(sharedLine);
 				grid[x][y].setRight(sharedLine);
 
 				sharedLine = new DotsLine();
                 grid[x][y+1].setDown(sharedLine);
 				grid[x][y].setUp(sharedLine);
 
 			}
 		}
         playerOneBoxes = 0;
         playerTwoBoxes = 0;
 	}
 
 	public void drawLine(int x, int y, Direction d, GameVal v){
 		grid[x][y].setVal(d,v);
 	}
 
 	public GameVal getLineAt(int x, int y, Direction d){
 		if((x <0) || (x>grid.length) || (y<0) || (y>grid[0].length)){
 			return GameVal.ERROR;
 		}
 		return grid[x][y].getVal(d);
 	}
 
 	public DotsNode[][] getDotsGrid(){
 		return grid;
 	}
 
 	public GameVal isGameOver(){
 		playerOneBoxes = 0;
 		playerTwoBoxes = 0;
 		GameVal tempVal;
 		for(int x = 0; x < settings.getGridSize(); x++){
 			for(int y = 0; y < settings.getGridSize(); y++){
 				tempVal = grid[x][y].isNodeFilled();
 				if(tempVal == GameVal.EMPTY){
					return GameVal.EMPTY;
 				}
 				if(tempVal == GameVal.PLAYER_ONE){
 					playerOneBoxes++;
 				}
 				if(tempVal == GameVal.PLAYER_TWO){
 					playerTwoBoxes++;
 				}
 			}
 		}
 		if(playerOneBoxes > playerTwoBoxes){
 			return GameVal.PLAYER_ONE;
 		}
 		if(playerOneBoxes < playerTwoBoxes){
 			return GameVal.PLAYER_TWO;
 		}
 		if(playerOneBoxes == playerTwoBoxes){
 			return GameVal.DRAW;
 		}
 		return GameVal.IN_PROGRESS;
 	}
 	/**
 	 *
 	 * @return grid height
 	 */
 	@Override
 	public int getGridHeight(){
 		return grid.length;
 	}
 
 	/**
 	 *
 	 * @return grid width
 	 */
 	@Override
 	public int getGridWidth(){
 		return grid[0].length;
 	}
 }
