 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 //import java.util.ArrayList;
 import java.util.ArrayList;
 
 public class Board {
 
     //0 = blank
     //1 = black - X
     //2 = white - O
     private int grid[][] = {
         {0, 0, 0},
         {0, 0, 0},
         {0, 0, 0},};
     private int tileWidth = 100;
     private int tileHeight = tileWidth;
     private boolean gameOver = false;
     
     //new button variables
     private int nx = 343;
     private int ny = 218;
     private int nw = 124;
     private int nh = 30;
     
     //clear button variables
     private int cx = 343;
     private int cy = 270;
     private int cw = 124;
     private int ch = 30;
    
     public Board() {
         // TODO Auto-generated constructor stub
     }
 
     /**
      * * SET AND GET METHOD **
      */
     public int getTileWidth() {
         return this.tileWidth;
     }
 
     public int getTileHeight() {
         return this.tileHeight;
     }
 
     public int[][] getGrid() {
         return this.grid;
     }
     
     public void setGrid(int value[][]){
     	this.grid = value;
     }
 
     public boolean getGameOver(){
     	return this.gameOver;
     }
     
     public void setGameOver(boolean value){
     	this.gameOver = value;
     }
     public int getNX(){
     	return this.nx;
     }
     
     public int getNY(){
     	return this.ny;
     }
     
     public int getNW(){
     	return this.nw;
     }
     
     public int getNH(){
     	return this.nh;
     }
     
     public int getCX(){
     	return this.cx;
     }
     
     public int getCY(){
     	return this.cy;
     }
     
     public int getCW(){
     	return this.cw;
     }
     
     public int getCH(){
     	return this.ch;
     }
     
     
     public void clearBoard()
     {
     	for(int y = 0; y < grid.length; y++){
 			for(int x = 0; x < grid[y].length; x++){
 					grid[x][y] = 0;
 			}
 			}
     }
     
     
     
     
     
     public void updateGrid(int x, int y, int value)
     {
     	grid[y][x] = value;
     }
     
     
     public boolean isValid(int x, int y)
     {
    	 	for (int q = 0; q < grid.length; q++) { 
             for (int w = 0; w < grid[q].length; w++) { 
             	if(grid[y][x] == 1 || grid[y][x] == 2)
             	{
             		return false;
             	}
             }
    	 	}
    	 	return true;
     }
     
     
     public boolean whoWin(int value)
     {
     	 int counter = 0;
     	 
     	if(grid[0][0] == value && grid[1][1] == value && grid[2][2] == value)   //dec
     		return true;
 
     		if(grid[0][2] == value && grid[1][1] == value && grid[2][0] == value) //inc
     			return true;
     		
     		//row
     		for(int y = 0; y < grid.length; y++){
     			for(int x = 0; x < grid[y].length; x++){
     				if(grid[x][y] == value){
     					counter++;
     				}
     					if(counter == 3)
     					{
     						return true;
     					}
     				}
     			counter = 0;
     			}
     		
     		//col
     		for(int y = 0; y < grid.length; y++){
     			for(int x = 0; x < grid[y].length; x++){
     				if(grid[y][x] == value){
     					counter++;
     				}
     					if(counter == 3)
     					{
     						return true;
     					}
     				}
     			counter = 0;
     			}
     		
     		
     		return false;
     }
     
     
     
     //draw
     public boolean draw()
     {
     	for(int y = 0; y < grid.length; y++){
 			for(int x = 0; x < grid[y].length; x++){
 				if(grid[x][y] == 0){
 					return false;
 				}
 			}
     	}
     	
     	return true;
     }
     
     
      /**
      * * Paint method **
      */
     public void paint(Graphics g, Player playerObj) {
         //Background Color
         g.setColor(Color.DARK_GRAY);
         g.fillRect(0, 0, Main.WINDOW_WIDTH, Main.WINDOW_HEIGHT);
 
         //load board
         for (int y = 0; y < grid.length; y++) { 
             for (int x = 0; x < grid[y].length; x++) { 
             	   //box - shadow
             		g.setColor(Color.black);
             		g.fillRect(x * (tileWidth + 5), y * (tileHeight + 5), tileWidth+2, tileHeight+2);
 
             		//box
                     g.setColor(Color.red);
                     g.fillRect(x * (tileWidth + 5), y * (tileHeight + 5), tileWidth, tileHeight);
                     
                     //box - white light
                     g.setColor(Color.white);
                     g.fillRect(x*(tileWidth+5), y*(tileHeight+5), 1, 100);
             }
         }// end of main for loop
         
         //score box
         g.setColor(Color.gray);
         g.fill3DRect(330, 10, 150, 80,false);
         
         //Score
         Font scoreFont = new Font("Segoe Script", Font.BOLD, 18);
         g.setFont(scoreFont);
         g.setColor(Color.black);
         g.drawString("Player X: " + playerObj.getPlayerXScore(), 350, 35);
         g.setColor(Color.white);
         g.drawString("Player O: " + playerObj.getPlayerOScore(), 350, 75);
 
         //turn box
         g.setColor(Color.gray);
         g.fill3DRect(330, 120, 150, 80,false);
         
         //whose turn
         if(playerObj.getTurn() == 1){
         	 g.setColor(Color.black);
          	g.drawString("Player 1", 350, 150);
         }
         else{
         	 g.setColor(Color.white);
         	g.drawString("Player 2", 350, 150);
         }
         
         //new game button
         if(gameOver){
 	        g.setFont(scoreFont);
 	        g.setColor(Color.DARK_GRAY);
 	        g.fill3DRect(nx, ny, nw, nh, true);
 	        g.setColor(Color.red);
 	        g.drawString(" New Game", 350, 240);
         }
         
         //clear game button
         g.setFont(scoreFont);
         g.setColor(Color.DARK_GRAY);
         g.fill3DRect(cx, cy, cw, ch, true);
         g.setColor(Color.red);
         g.drawString("Clear Game", 350, 290);
     }// end of paint method
 }
