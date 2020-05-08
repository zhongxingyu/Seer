 package gameEngine;
 
 import java.util.Random;
 
 import screen.GamePanel;
 
 public class Controller {
 	private int color;
 	private int gameStatus;
 	protected Board board;
 	protected Player[] player;
 
 	public Controller() {
 		this.gameStatus = 0;
 		System.out.println("gameEngine.Controller");
 		rand = new Random();
 	}
 
 	public int getGameStatus() {
 		return this.gameStatus;
 	}
 
 	public void start(int width, int height, int maxEdges, int p1, int p2) {
 		this.color=1;
 		this.gameStatus = 1;
 		board = new Board(width, height, maxEdges);
 		//creating two players
 		player = new Player[2];
 		switch(p1) {
 		case 0: player[0] = new Human(2); break;
 		case 1: player[0] = new ComputerAdam(2); break;
 		case 2: player[0] = new Computer(2); break;
 		case 3: player[0] = new Computer(2); break;
 		}
 		switch(p2) {
 		case 0: player[1] = new Human(1); break;
 		case 1: player[1] = new ComputerAdam(1); break;
 		case 2: player[1] = new Computer(1); break;
 		case 3: player[1] = new Computer(1); break;
 		}
 		System.out.println("gameEngine.Controller start");
 		System.out.println(p2);
 		
 		if (player[(this.color+1)%2].getClass() != new Human(-1).getClass()) {
 			int mv = makeMove(0,0);
 			GamePanel.message(mv);
 		}
 	}
 
 	public void stop() {
 		this.gameStatus = 0;
 	}
 	
 	public void undo(int flag) {
 		// -2 force redo, -1 normal redo, >-1 loop redo
         int tmpColor = this.board.removeEdge(-1); 
         if (tmpColor >= 0) {
             this.color = tmpColor;
             while (flag >= 0 && ((tmpColor = this.board.removeEdge(tmpColor)) == this.color)) {
             	continue;
             }
         }
         if (flag != -2 && player[(this.color+1)%2].getClass() != new Human(-1).getClass()) {
			int mv = makeMove(0,0);
			GamePanel.message(mv);
 		}
 	}
 
 	Random rand;
 
 	public int getBoardHeight() {
 		return this.board.getHeight();
 	}
 	
 	public int getCurrentPlayer() {
 		return this.color+1;
 	}
 	
 	public void updatePos(int x,int y) {
 		this.board.setPos(x,y);
 	}
 
 	public int getBoardWidth() {
 		return this.board.getWidth();
 	}
 	
 	public void updateLinks(int y, int x, int k, int val) {
 		this.board.addEdge(y, x, k, val);
 	}
 	
 	public int boardLinks(int i, int j, int k) {
 		return this.board.getLinks(i, j, k);
 	}
 
 	public int returnPosX() {
 		return this.board.getX();
 	}
 	
 	public int returnPosY() {
 		return this.board.getY();
 	}
 
 	public int countNeighbours(int x, int y) {
 		return this.board.countNeighbours(x,y);
 	}
 	
 	/*
 	 * makeMove return statement: 
 	 * 0 - no winner 
 	 * 1 - player 1 wins
 	 * 2 - player 2 wins
 	 */
 	
 	public int makeMove(int x, int y) {
 		boolean endOfTurn=false;
 		
 		while (!endOfTurn) {
 			Player curPlayer;
 			if (this.color == 1) curPlayer = player[0];
 			else curPlayer = player[1];			
 			
 			int mv = curPlayer.makeMove(x,y,board);
 			if(mv==-1) {
 				System.out.println("iimpossible");
 				return 0;
 			}
 			else if(mv==3) {
 				System.out.println("top player has lost");
 				return 1;
 			}
 			else if(mv==4) {
 				System.out.println("top player has win");
 				return 2;
 			}
 			else if(mv==5) { 
 				System.out.println("dead end");
 				if (this.color == 1)
 					return 3;
 				else
 					return 4;
 			}
 			else if(mv==1) {
 				System.out.println("rreflection");
 				return 0;
 			}
 			else if(mv==0) {
 				System.out.println("cchange player");
 				this.color+=1;
 				this.color%=2;
 				
 				if (player[(this.color+1)%2].getClass() != new Human(-1).getClass()) {
 					//System.out.println("From " + board.getX() + "," + board.getY());
 					//return makeMove(0,0);
 				}
 				else {
 					endOfTurn = true;
 				}
 			}
 		}		
 		System.out.println("Wychodzę");
 		return 0;
 	}
 }
