 package se.chalmers.TowerDefence;
 
 import org.newdawn.slick.tiled.TiledMap;
 
 public class GameBoard {
 
 	private final int[][] gameBoard;
 	
 	public GameBoard(){
 	 gameBoard = new int[][]{
 				new int[]{1,0,0,0,0},
 				new int[]{1,1,1,1,0},
 				new int[]{1,1,0,1,0},
 				new int[]{1,1,0,1,0},
 				new int[]{1,1,0,1,0},
 				new int[]{0,0,0,0,0}};
 
 	}
 	public GameBoard(TiledMap map){
		gameBoard = new int[map.getWidth() + 1 ][map.getHeight() + 1 ];
 		for (int xAxis=0;xAxis<map.getWidth(); xAxis++){
 		             for (int yAxis=0;yAxis<map.getHeight(); yAxis++){
 		                 int tileID = map.getTileId(xAxis, yAxis, 0);
 		                 String value = map.getTileProperty(tileID, "road", "false");
 		                 if ("true".equals(value)){
 		                     gameBoard[xAxis][yAxis] = 1;
 		                 }
 		             }
 		 }
 	}
 	
 	private int[][] findGameBoard(){
 		//TODO
 		//Get a gameBoard from a file
 		return null;
 	}
 	public int[][] getGameBoard(){
 		//TODO;
 		return gameBoard;
 	}
 
 	public int getTile(int mouseX) {		
 		return mouseX/40;
 	}
 	public boolean isBlocked(int x, int y) {
		return (gameBoard[x][y]==1 || gameBoard[x][y]==2);
	}
	public void addTower(int x, int y) {
		gameBoard[x][y]=2;
 	}
 	
 //	public int[] getFirstRoadSquare(){
 //		return null;
 //	}
 	
 	/*
 	@Override
 	public String toString(){
 		String temp=new String();
 		for(int i=0; i<gameBoard.length;i++){
 			for(int j=0; j<gameBoard.length;j++){
 				temp+=gameBoard[i][j]+" ";
 			}
 			temp+="\n";
 		}
 		
 		return temp;	
 	}
 	*/
 }
