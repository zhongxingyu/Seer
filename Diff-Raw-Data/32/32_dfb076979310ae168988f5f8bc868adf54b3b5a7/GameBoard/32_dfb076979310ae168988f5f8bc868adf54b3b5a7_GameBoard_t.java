 package no.ntnu.tdt4240.models;
 
import no.ntnu.tdt4240.views.GridView;
import no.ntnu.tdt4240.views.PlayerView;
 import android.text.style.ForegroundColorSpan;
 
 public class GameBoard {
 	
 	private Cell[][] gameBoard;
	private GridView view;
 	
 	GameBoard(int height, int width){
 		
 		this.gameBoard= new Cell[height][width];
 		
 	}
 	
 	public void addGold(int gold){
 		while(gold>0){
 			
 			int xValue=(int) (1+Math.random()*(gameBoard.length-1));
 			int yValue=(int) (1+Math.random()*(gameBoard[0].length-1));
 			
 			if(gameBoard[xValue][yValue]==null){
 				gameBoard[xValue][yValue]= new Gold(null);
 				gold--;
 			}
 		}
 	}
 	
 	public void addMines(int mines){
 		while(mines>0){
 			
 			int xValue=(int) (1+Math.random()*(gameBoard.length-1));
 			int yValue=(int) (1+Math.random()*(gameBoard[0].length-1));
 			
 			if(gameBoard[xValue][yValue]==null){
 				gameBoard[xValue][yValue]= new Mine(null);
 				mines--;
 			}
 			
 		}	
 	}
 	
 	public void addBlanks(){
 	
 		for(int a=1; a<gameBoard.length-1;a++){
 			for(int b=1; b<gameBoard[0].length-1; b++){
 
 				if(gameBoard[a][b]==null){
 					int nearGold=0;
 					int nearMines=0;
 				
 					for(int c=a-1; c<a+1;c++){
 						for(int d=b-1; d<b+1; d++){
 							
 							if(gameBoard[c][d].getType()=="gold"){
 								nearGold++;
 							}
 							if(gameBoard[c][d].getType()=="Mine"){
 								nearMines++;
 							}		
 					gameBoard[a][b]=new Blank(null, nearGold, nearMines);
 						}
 				
 					}
 					
 				}
 			}	
 		 }
 	}
 	public void createBoard(int gold, int mines){
 		
 			addGold(gold);
 			addMines(mines);
 			addBlanks();
 		
 	}

	public Cell[][] getGridArray() {
		return gameBoard;
	}
 }
