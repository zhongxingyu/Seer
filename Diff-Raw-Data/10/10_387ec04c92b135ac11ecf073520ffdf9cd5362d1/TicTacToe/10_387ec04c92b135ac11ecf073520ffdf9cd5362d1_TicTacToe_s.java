 package com.github.TrungLam.TicTacToe;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Line;
 
 
 public class TicTacToe extends BasicGame{
 	
 	Line line, line2, line3, line4;
 	static int screenWidth, screenHeight;
 	int mousex, mousey;
 	String mouseXY = "";
 	Square squares[][] = new Square[3][3];
 	MouseBlock mb;
 	int squareX, squareY;
 	float squareWidth, squareHeight;
 	int index;
 	String playerMarker[] = {"X", "O"};
 	boolean win;
 	
 
 
 	public TicTacToe() {
 		super("Tic Tac Toe");
 	}
 
 	@Override
 	public void render(GameContainer arg0, Graphics arg1) throws SlickException {
 		arg1.draw(line);
 		arg1.draw(line2);
 		arg1.draw(line3);
 		arg1.draw(line4);
 		arg1.drawString(mouseXY, 0, 100);
 		
 		for (int i = 0; i < 3; i++) {
 			for (int k = 0; k < 3; k++)	{
 				squares[i][k].draw();
 			}
 		}
 	}
 
 	@Override
 	public void init(GameContainer arg0) throws SlickException {
 		line = new Line(screenWidth * .33f,0.f, screenWidth *.33f,screenHeight);
 		line2 = new Line(screenWidth *.66f, 0, screenWidth *.66f, screenHeight);
 		line3 = new Line(0, screenHeight * .33f, screenWidth, screenHeight * .33f);
 		line4 = new Line(0, screenHeight * .66f, screenWidth, screenHeight * .66f);
 		mousex = Mouse.getX();
 		mousey = screenHeight - Mouse.getY();
 		
 		mb = new MouseBlock(mousex, mousey);
 		win = false;
 		index = 0;
 
 
 		initilizeSquares();
 	}
 
 	@Override
 	public void update(GameContainer arg0, int arg1) throws SlickException {
 		mousex = Mouse.getX();
 		mousey = screenHeight -Mouse.getY();
 		mouseXY = "x: " + mousex + "\ty: " + mousey;
 		mb.setX(mousex);
 		mb.setY(mousey);
 		Input input = arg0.getInput();
 		
 		if (input.isMousePressed(1)) {
 			initilizeSquares();
 			win = false;
 		}
 		
 		if (input.isMousePressed(0) && !win) {
 			
 			Square clickedSquare = null;
 			int found = 0;
 			
 			for (int i = 0; i < 3; i++) {
 				for (int k = 0; k < 3; k++) {
 					if (found > 1)
 						break;
 					if (mb.getRect().intersects(squares[i][k].getRectangle()) && !squares[i][k].getMark()) {
 						if (clickedSquare == null)
 							clickedSquare = squares[i][k];
 						found++;
 					}
 				}
 			}
 			
 			if (found == 1) {
 				clickedSquare.setC(playerMarker[index]);
 				clickedSquare.setMark(true);
 				
 				win = checkWin();
 				
 				if (win)
 					System.out.println("win");
 				
 				index = (index == 0) ? 1 : 0;
 			}
 		}
 		
 	}
 	
 	void initilizeSquares() {
 		squareX = 0;
 		squareY = 0;
 		squareWidth = (float)screenWidth - screenWidth*.66f;
 		squareHeight = (float)screenHeight - screenHeight*.66f;
 		
 		for (int i = 0; i < 3; i++) {
 			for (int k = 0; k < 3; k++) {
 				squares[i][k] = new Square(squareX, squareY, squareWidth, squareHeight);
 				squareX += squareWidth;
 			}
 			squareX = 0;
 			squareY += squareHeight;
 		}
 	}
 	
 	boolean checkWin() {
 		int counter = 0;
 		
 		//check horizontal
 		for (int i = 0; i < 3; i++) {
 			for (int k =0; k < 3; k++) {
 				if (squares[i][k].getMark() && squares[i][k].getC() == playerMarker[index]) {
 					counter++;
 				}
 			}
 			if (counter == 3)
 				return true;
 			counter = 0;
 		}
 		//check vertically
 		for (int i = 0; i < 3; i++) {
 			for (int k = 0; k < 3; k++) {
 				if (squares[k][i].getMark() && squares[k][i].getC() == playerMarker[index]) {
 					counter++;
 				}
 			}
 			if (counter == 3)
 				return true;
 			counter = 0;
 		}
 		//check diagonally
 		for (int i = 0; i < 3; i++){
 			if (squares[i][i].getMark() && squares[i][i].getC() == playerMarker[index]) {
 				counter++;
 			}
 			
 		}
 		if (counter == 3)
 			return true;
 		
 		return false;
 	}
 	
 	public static void main(String[] args) throws SlickException {
 		AppGameContainer app = new AppGameContainer(new TicTacToe());
 		screenHeight = 225;
 		screenWidth = 300;
 		
 		app.setDisplayMode(screenWidth, screenHeight, false);
 		app.start();
 	}
 	
 
 }
