 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferStrategy;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 import javax.swing.JViewport;
 import javax.swing.Timer;
 
 public class GameWindow extends JFrame {
 	private static final long serialVersionUID = 1L; //To suppress warning
 	JFrame frame;
 	BufferStrategy bufferStrat;
 	Graphics graphics;
 	Rectangle rec;
 	int xGridMin = 40;
 	int yGridMin = 70;
 	int xGridMax;
 	int yGridMax;
 	int xMax;
 	int yMax;
 	int xSpacing;
 	int ySpacing;
 	int xBoardDim;
 	int yBoardDim;
 	int xClick;
 	int yClick;
 	int xLastCoord = -1;
 	int yLastCoord = -1;
 	int xLastPoint = -1;
 	int yLastPoint = -1;
 	int radius;
 	boolean alternateColors = false;
 	/* True if there was a click */
 	boolean clicked = false;
 	/* True if the timer was fired */
 	boolean ticked = false;
 	/* Used to force window updates independent of events */
 	boolean forceUpdate = true;
 	/* Controls the visibility of the advance and withdraw buttons */
 	boolean advWithVisible = false;
 	/* Controls the visibility of the board and related buttons */
 	boolean gameVisible = false;
 	/* Controls the visibility of the client/server/local game type */
 	boolean clientServerVisible = true;
 	/* Controls the visibility of the client options screen */
 	boolean clientScreenVisible = false;
 	/* Controls the visibility of the server options screen */
 	boolean serverScreenVisible = false;
 	/* Controls the visibility of the local options screen */
 	boolean localScreenVisible = false;
 	
 	String address = "127.0.0.1";
 	int port = 1024;
 	boolean isPlayer1White = true;
 	boolean isPlayer1Human = false;
 	boolean isPlayer2Human = false;
 	int tempRows = 5;
 	int tempColumns = 9;
 	int focusedField = 1;
 	char clientT = 'B';
 	
 	int gameType = 0;
 	
 	
 	/* Game Variables */
 	Fanorona game;
 	int turn;
 	int turnLimit;
 	int numberOfBlackMovesThisTurn;
 	boolean moveTurn;
 	Piece.Type humanPlayer = Piece.Type.WHITE;
 	Piece.Type otherPlayer;
 	long time1;
 	long time2;
 	int responseTime = 5000;
 	
 	private enum selectionStates {
 		NONE,
 		FIRSTPIECE,
 		SECONDCOORD;
 	}
 	selectionStates currentSelectState = selectionStates.NONE;
 	
 	private enum pieceType {
 		BLACK,
 		WHITE,
 		SACRIFICED
 	}
 	
 	final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
 	
 	public GameWindow(int _xBoardDim, int _yBoardDim) {
 		xBoardDim = _xBoardDim;
 		yBoardDim = _yBoardDim;
 		createWindow();
 	}
 	
 	private final void createWindow() {
 		setTitle("Fanorona");
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(600, 600);
 		setLocationRelativeTo(null);
 		setVisible(true);
 		this.createBufferStrategy(2);
 		
 		/* Set window dimensions used elsewhere */
 		rec = new Rectangle();
 		if (getParent() instanceof JViewport) {
 	        JViewport vp = (JViewport) getParent();
 	        rec = vp.getViewRect();
 	    } else {
 	        rec = new Rectangle(0, 0, getWidth(), getHeight());
 	    }
 		xMax = (int) rec.getMaxX();
 		yMax = (int) rec.getMaxY();
 
 
 		
         ActionListener timerListener = new ActionListener()  
         {
             public void actionPerformed(ActionEvent e)  
             {
             	ticked = true;
             	updateScreen();
             }
         };
         Timer timer = new Timer(1000, timerListener);   
         timer.start();
 		
 		addMouseListener(new MouseAdapter() {
 			@Override
             public void mousePressed(MouseEvent event) {
                 if (event.getButton() == MouseEvent.BUTTON1) {
                 	clicked = true;
                 	xClick = event.getX();
                 	yClick = event.getY();
                 	updateScreen();
                 }
 
                 if (event.getButton() == MouseEvent.BUTTON3) {
 
                 }
 			}
 		});
 	}
 	
 	private void calculateDimensions() {
 		xSpacing = (xMax - 2 * xGridMin)/(xBoardDim - 1);
 		ySpacing = ((yMax - 30) - 2 * xGridMin)/(yBoardDim - 1);
 		if (xSpacing >= ySpacing) {
 			radius = (int) (0.375 * (double) ySpacing);
 		} else {
 			radius = (int) (0.375 * (double) xSpacing);
 		}
 		xGridMax = xGridMin + ((xBoardDim - 1) * xSpacing);
 		yGridMax = yGridMin + ((yBoardDim - 1) * ySpacing);
 	}
 	
 	/* TODO Implement this function */
 	public void processMove() {
 		
 	}
 	
 	private boolean checkButtonClicks() {
 		if (gameVisible) {
 			return checkGameButtons();
 		} else if (clientServerVisible) {
 			return checkClientServerButtons();
 		} else if (clientScreenVisible) {
 			return checkClientButtons();
 		} else if (serverScreenVisible) {
 			return checkServerButtons();
 		} else if (localScreenVisible) {
 			return checkLocalButtons();
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean checkGameButtons() {
 		if (checkButtonClick(40, yMax - 30, 20, 80)) {
 			//Reset clicked
 			System.out.println("RESET");
 			return true;
 		} else if (checkButtonClick(480, yMax - 30, 20, 80)) {
 			//Quit clicked
 			System.out.println("QUIT");
 			return true;
 		} else if (advWithVisible) {
 			if (checkButtonClick(200, yMax - 30, 20, 80)) {
 				//Advance clicked
 				advWithVisible = false;
 				forceUpdate = true;
 				return true;
 			} else if (checkButtonClick(315, yMax - 30, 20, 80)) {
 				//Withdraw clicked
 				advWithVisible = false;
 				forceUpdate = true;
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean checkClientServerButtons() {
 		if (checkButtonClick(xMax / 4, yMax / 3, yMax / 6, xMax / 5)) {
 			//Client clicked
 			System.out.println("CLIENT");
 			clientServerVisible = false;
 			clientScreenVisible = true;
 			forceUpdate = true;
 			updateScreen();
 			return true;
 		} else if (checkButtonClick(xMax / 2 + xMax / 20, yMax /3, yMax / 6,
 				xMax / 5)) {
 			//Server clicked
 			System.out.println("SERVER");
 			clientServerVisible = false;
 			serverScreenVisible = true;
 			forceUpdate = true;
 			updateScreen();
 			return true;
 		} else if (checkButtonClick(4 * (xMax / 10), yMax / 3 + yMax / 4,
 				yMax / 6, xMax / 5)) {
 			//Local clicked
 			System.out.println("LOCAL");
 			clientServerVisible = false;
 			localScreenVisible = true;
 			forceUpdate = true;
 			updateScreen();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean checkButtonClick(int x, int y, int height, int width) {
 		if ((xClick > x && xClick < (x + width))
 				&& (yClick > y && yClick < y + height)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private void processClick(int x, int y) {
 		if (checkButtonClicks()) {
 			if (!forceUpdate) {
 				forceUpdate = true;
 				updateScreen();
 			}
 			return;
 		}
 		if (gameVisible) {
 			xClick = -1;
 			yClick = -1;
 			int xTemp;
 			int yTemp;
 			if (x <= xGridMin) {
 				xTemp = 0;
 			} else if (x >= xGridMax) {
 				xTemp = xBoardDim - 1;
 			} else {
 				double xDouble = (double) x;
 				xTemp = (int) Math.round(((xDouble -
 						(double)xGridMin) / (double)xSpacing));
 			}
 			if (y <= yGridMin) {
 				yTemp = 0;
 			} else if (y >= yGridMax) {
 				yTemp = yBoardDim - 1;
 			} else {
 				double yDouble = (double) y;
 				yTemp = (int) Math.round(((yDouble -
 						(double)yGridMin) / (double)ySpacing));
 			}
 			processCoordinate(xTemp, yTemp, x, y);
 		}
 
 		/*graphics.drawString("Point nearest click: " +
 				Integer.toString(xTemp), 20, 40);
 		graphics.drawString("Point nearest click: " +
 				Integer.toString(yTemp), 20, 60);*/
 	}
 
 	private void processCoordinate(int xCoord, int yCoord, int xActual,
 			int yActual) {
 		int xTemp = xGridMin + xSpacing * xCoord;
 		int yTemp = yGridMin + ySpacing * yCoord;
 
 		double distance = Math.sqrt(((xTemp - xActual) * (xTemp - xActual)) +
 				((yTemp - yActual) * (yTemp - yActual)));
 		
 		/* Is the click near a coordinate? */
 		if (distance <= radius) {
 			if (currentSelectState == selectionStates.NONE) {
 				/* TODO Check to see if the selected coordinate has a
 				 * selectable piece in it, otherwise take no action */
 				xLastCoord = xCoord;
 				yLastCoord = yCoord;
 				currentSelectState = selectionStates.FIRSTPIECE;
 				drawSelection(xTemp, yTemp);
 				return;
 			} else if (currentSelectState == selectionStates.FIRSTPIECE) {
 				if (xCoord == xLastCoord && yCoord == yLastCoord) {
 					/* Currently selected piece is selected again. This is
 					 * interpreted as a deselect action */
 					currentSelectState = selectionStates.NONE;
 					xLastCoord = -1;
 					yLastCoord = -1;
 					return;
 				} else {
 					/* A location different from the first is selected. This
 					 * is interpreted as an attempted move action. */
 					currentSelectState = selectionStates.SECONDCOORD;
 					drawSelection(xTemp, yTemp);
 					return;
 				}
 			}
 		} else {
 			/* Clicked point not near a coordinate */
 			drawSelection(-2, -2);
 			return;
 		}
 	}
 	
 	private void drawSelection(int xPoint, int yPoint) {
 		if (forceUpdate) {
 			return;
 		}
 		if (xPoint == -2 || yPoint == -2) {
 			xPoint = xLastPoint;
 			yPoint = yLastPoint;
 		} else {
 			xLastPoint = xPoint;
 			yLastPoint = yPoint;
 		}
 		if (currentSelectState == selectionStates.FIRSTPIECE) {
 			graphics.setColor(Color.RED);
 		} else {
 			graphics.setColor(Color.BLUE);
 		}
 		Graphics2D graphics2D = (Graphics2D) graphics;
 	    graphics2D.setStroke(new BasicStroke(2F));
 	    drawBox(xPoint - radius, yPoint - radius, radius * 2, radius * 2);
 		graphics.setColor(Color.BLACK);
 		graphics2D.setStroke(new BasicStroke(0F));
 		if (currentSelectState == selectionStates.SECONDCOORD) {
 			currentSelectState = selectionStates.NONE;
 			/* TODO Possibly update x/yLastCoord/Point here */
 			processMove();
 		}
 		
 		/* REMOVE THIS LINE */
 		drawPiece(pieceType.WHITE, xPoint, yPoint);
 	}
 	
 	private void clearWindow() {
 		graphics.setColor(Color.lightGray);
 		/* +20 is to work around the either getting bad dimensions from the
 		 * viewport or rendering problems with Swing or the window manager */
         graphics.fillRect(0, 0, xMax + 20, yMax);
         graphics.setColor(Color.BLACK);
 	}
 	
 	private void drawTime() {
 		if (gameVisible) {
 			graphics.setColor(Color.lightGray);
 	        graphics.fillRect(xMax - 250, 0, 240, 42);
 	        graphics.setColor(Color.BLACK);
 			Date date = new Date();
 			String time = timeFormat.format(date);
 	        graphics.drawString("Remaining move time: ", xMax - 250, 40);
 			graphics.drawString(time + " sec", xMax - 100, 40);
 		}
 	}
 
 	private void drawGrid() {
 		/* TODO Add special processing for 1xX and Xx1 board sizes */
 		if ((xBoardDim == 1) || (yBoardDim == 1)) {
 			return;
 		}
 
 		int xCurrent = xGridMin;
 		int yCurrent = yGridMin;
 
 		Graphics2D graphics2D = (Graphics2D) graphics;
 		graphics2D.setStroke(new BasicStroke(7F));
 
 		boolean altLeft = true;
 		boolean flipAlt = false;
 		if (xBoardDim % 2 == 0) {
 			flipAlt = true;
 		}
 		while(true) {
 			if (yCurrent != yGridMax) {
 				//Draw down line
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent,
 						yCurrent + ySpacing);
 			}
 			if ((xCurrent != xGridMin) && (yCurrent != yGridMax) && altLeft) {
 				//Draw left diagonal
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent - xSpacing,
 						yCurrent + ySpacing);
 			}
 			if ((xCurrent != xGridMax) && (yCurrent != yGridMax) && altLeft) {
 				//Draw right diagonal
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent + xSpacing,
 						yCurrent + ySpacing);
 			}
 			if (xCurrent != xGridMax) {
 				//Draw right line
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent + xSpacing,
 						yCurrent);
 			}
 			if ((xCurrent >= xGridMax) && (yCurrent >= yGridMax)) {
 				break;
 			}
 			if (xCurrent == xGridMax) {
 				if (flipAlt) {
 					altLeft = !altLeft;
 				}
 				xCurrent = xGridMin;
 				yCurrent += ySpacing;
 			} else {
 				xCurrent += xSpacing;
 			}
 			altLeft = !altLeft;
 		}
 		graphics2D.setStroke(new BasicStroke(0F));
 	}
 
 	private void drawPiece(pieceType pT, int xPoint, int yPoint) {
 		if (pT == pieceType.WHITE) {
 			graphics.setColor(Color.WHITE);
 		} else if (pT == pieceType.BLACK) {
 			graphics.setColor(Color.BLACK);
 		} else {
 			graphics.setColor(Color.GRAY);
 		}
 		Graphics2D graphics2D = (Graphics2D) graphics;
 		int x = (int) (((double) xPoint) - ((double) radius));
 		int y = (int) (((double) yPoint) - ((double) radius));
 		Ellipse2D.Double circle = new Ellipse2D.Double(x, y,
 				radius * 2, radius * 2);
 		graphics2D.fill(circle);
 		graphics.setColor(Color.BLACK);
 	}
 	
 	
 	public void drawPieces() {
 		/* Iterate over pieces here */
 		/* xCoord and yCoord points start at the top left corner with (0,0)
 		 * and go to the bottom left corner with (n,m) where n and m are the
 		 * x and y dimensions of the board, respectively */
 		if (game != null) {
			for (int i = 1; i < xBoardDim; i++) {
				for (int j = 1; j < yBoardDim; j++) {
 					Piece.Type pT = game.board.array[i][j].type;
					int xOutputPoint = xGridMin + (xSpacing * (i - 1));
					int yOutputPoint = yGridMin + (ySpacing * (yBoardDim - j));
 					if (pT == Piece.Type.WHITE) {
 						drawPiece(pieceType.WHITE, xOutputPoint, yOutputPoint);
 					} else if (pT == Piece.Type.BLACK) {
 						drawPiece(pieceType.BLACK, xOutputPoint, yOutputPoint);
 					} else if ((pT == Piece.Type.BLACKSACRIFICE) ||
 							(pT == Piece.Type.WHITESACRIFICE)) {
 						drawPiece(pieceType.SACRIFICED, xOutputPoint, yOutputPoint);
 					} else {
 						continue;
 					}
 				}
 			}
 		}
 	}
 	
 	public void drawInfo() {
 		graphics.drawString("Current Turn: XX", xMax - 196, 52);
 		graphics.drawString("Current Player: XX", xMax - 196, 62);
 	}
 	
 	private void drawButton(int x, int y, Color color) {
 		Graphics2D graphics2D = (Graphics2D) graphics;
 		graphics2D.setStroke(new BasicStroke(3F));
 		//Reset button
 		graphics.setColor(color);
 		drawBox(x, y, 20, 80);
 		graphics.setColor(Color.BLACK);
 		graphics2D.setStroke(new BasicStroke(0F));
 	}
 	
 	private void drawButtons() {
 		if (gameVisible) {
 			//Reset button
 			drawButton(40, yMax - 30, Color.ORANGE);
 			graphics.drawString("Reset", 60, yMax - 15);
 			//Quit button
 			drawButton(480, yMax - 30, Color.ORANGE);
 			graphics.drawString("Quit", 505, yMax - 15);
 			if (advWithVisible) {
 				//Advance button
 				drawButton(200, yMax - 30, Color.GRAY);
 				graphics.drawString("Advance", 214, yMax - 15);
 				//Withdraw button
 				drawButton(315, yMax - 30, Color.GRAY);
 				graphics.drawString("Withdraw", 327, yMax - 15);
 			}
 		}
 	}
 	
 	private void drawFlashingBox(int x, int y, int height, int width) {
 		Graphics2D graphics2D = (Graphics2D) graphics;
 	    graphics2D.setStroke(new BasicStroke(1.5F));
 		if (alternateColors) {
 			graphics.setColor(Color.RED);
 			if (ticked) {
 				alternateColors = false;
 			}
 		} else {
 			graphics.setColor(Color.YELLOW);
 			if (ticked) {
 				alternateColors = true;
 			}
 		}
 		drawBox(x, y, height, width);
 		graphics.setColor(Color.BLACK);
 		graphics2D.setStroke(new BasicStroke(0F));
 	}
 	
 	private void drawBox(int x, int y, int height, int width) {
 		graphics.drawLine(x, y + height, x + width, y + height); //Bottom
 		graphics.drawLine(x + width, y + height, x + width, y); //Right
 		graphics.drawLine(x, y, x + width, y); //Top
 		graphics.drawLine(x, y, x, y + height); //Left
 	}
 	
 	private void drawClientServer() {
 		Graphics2D graphics2D = (Graphics2D) graphics;
 		graphics2D.setStroke(new BasicStroke(3F));
 		graphics.setColor(Color.BLACK);
 		//Client button
 		drawBox(xMax / 4, yMax / 3, yMax / 6, xMax / 5);
 		graphics.drawString("Client", xMax / 4 + xMax / 10 - xMax / 30,
 				yMax / 3 + yMax / 10);
 		//Server button
 		drawBox(xMax / 2 + xMax / 20, yMax /3, yMax / 6, xMax / 5);
 		graphics.drawString("Server", xMax / 4 + 2 * xMax / 10 - xMax / 30 + 
 				xMax / 5,
 				yMax / 3 + yMax / 10);
 		//Local button
 		drawBox(4 * (xMax / 10), yMax / 3 + yMax / 4, yMax / 6, xMax / 5);
 		graphics.drawString("Local", xMax / 2 - xMax / 30, 2 * yMax / 3);
 	}
 	
 	private void drawClientScreen() {
 		//Address field
 		drawBox(xMax / 2, 2 * yMax / 10, yMax / 12, xMax / 5);
 		graphics.drawString("Address:", xMax / 4, 2 * yMax / 10 + yMax / 24);
 		graphics.drawString(address, xMax / 2, 2 * yMax / 10 + yMax / 24);
 		//Port field
 		drawBox(xMax / 2, 2 * yMax / 10 + yMax / 12 + yMax / 20, yMax / 12, xMax / 5);
 		graphics.drawString("Port:", xMax / 4, 2 * yMax / 10 + yMax / 24  + yMax / 12 + yMax / 20);
 		graphics.drawString(Integer.toString(port), xMax / 2, 2 * yMax / 10 + yMax / 24  + yMax / 12 + yMax / 20);
 		//Number pad
 		drawNumberPad();
 		//Play button
 		drawBox(7 * xMax / 10, 4 * yMax / 5, yMax / 12, xMax / 5);
 		graphics.drawString("Play...", 7 * xMax / 10 + xMax / 15, 4 * yMax / 5 + yMax / 24);
 	}
 	
 	private boolean checkClientButtons() {
 		if (checkButtonClick(xMax / 2, 2 * yMax / 10, yMax / 12, xMax / 5)) {
 			//Address field clicked
 			focusedField = 1;
 			System.out.println("Address clicked");
 			return true;
 		} else if (checkButtonClick(xMax / 2,
 				2 * yMax / 10 + yMax / 12 + yMax / 20, yMax / 12, xMax / 5)) {
 			//Port field clicked
 			focusedField = 2;
 			System.out.println("Port clicked");
 			return true;
 		} else if (checkNumberPadButtons()) {
 			return true;
 		} else if (checkButtonClick(7 * xMax / 10, 4 * yMax / 5, yMax / 12, xMax / 5)) {
 			gameType = 1;
 			startGame();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean checkServerButtons() {
 		int boxHeight = yMax / 13;
 		int ySpacing = yMax / 40;
 		int xSelectorShift = xMax / 50;
 		int ySelectorShift = yMax / 50;
 		if (checkButtonClick(xMax / 2, yMax / 10, boxHeight, xMax / 5)) {
 			//Row field clicked
 			focusedField = 1;
 			System.out.println("Row clicked");
 			return true;
 		} else if (checkButtonClick(xMax / 2, yMax / 10 + ySpacing + boxHeight, boxHeight, xMax / 5)) {
 			//Columns field clicked
 			focusedField = 2;
 			System.out.println("Column clicked");
 			return true;
 		} else if (checkButtonClick(xMax / 2, yMax / 10 + 2 * ySpacing + 2 * boxHeight, boxHeight, xMax / 5)) {
 			//Port field clicked
 			focusedField = 3;
 			System.out.println("Port clicked");
 			return true;
 		} else if (checkButtonClick(xMax / 2, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 10)) {
 			//Player 1 color black clicked
 			isPlayer1White = false;
 			System.out.println("Black selected");
 			return true;
 		} else if (checkButtonClick(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 10)) {
 			//Player 1 color white clicked
 			isPlayer1White = true;
 			System.out.println("White selected");
 			return true;
 		} else if (checkNumberPadButtons()) {
 			return true;
 		} else if (checkButtonClick(7 * xMax / 10, 4 * yMax / 5, yMax / 12, xMax / 5)) {
 			gameType = 2;
 			startGame();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean checkLocalButtons() {
 		int boxHeight = yMax / 16;
 		int ySpacing = yMax / 40;
 		int xSelectorShift = xMax / 50;
 		int ySelectorShift = yMax / 50;
 		if (checkButtonClick(xMax / 2, yMax / 10, boxHeight, xMax / 5)) {
 			//Rows field clicked
 			focusedField = 1;
 			System.out.println("Row clicked");
 			return true;
 		} else if (checkButtonClick(xMax / 2, yMax / 10 + ySpacing + boxHeight, boxHeight, xMax / 5)) {
 			//Columns field clicked
 			System.out.println("Column clicked");
 			focusedField = 2;
 			return true;
 		} else if (checkButtonClick(xMax / 2, yMax / 10 + 2 * ySpacing + 2 * boxHeight, boxHeight - ySelectorShift, xMax / 10)) {
 			//Player 1 color black clicked
 			isPlayer1White = false;
 			System.out.println("Black selected");
 			return true;
 		} else if (checkButtonClick(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 2 * ySpacing + 2 * boxHeight, boxHeight - ySelectorShift, xMax / 10)) {
 			//Player 1 color white clicked
 			isPlayer1White = true;
 			System.out.println("White selected");
 			return true;
 		} else if (checkButtonClick(xMax / 2, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 10)) {
 			//Player 1 human clicked
 			isPlayer1Human = true;
 			System.out.println("Player 1 Human selected");
 			return true;
 		} else if (checkButtonClick(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 8)) {
 			//Player 1 computer clicked
 			isPlayer1Human = false;
 			System.out.println("Player 1 Computer selected");
 			return true;
 		} else if (checkButtonClick(xMax / 2, yMax / 10 + 4 * ySpacing + 4 * boxHeight, boxHeight - ySelectorShift, xMax / 10)) {
 			//Player 2 human clicked
 			isPlayer2Human = true;
 			System.out.println("Player 2 Human selected");
 			return true;
 		} else if (checkButtonClick(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 4 * ySpacing + 4 * boxHeight, boxHeight - ySelectorShift, xMax / 8)) {
 			//Player 2 computer clicked
 			isPlayer2Human = false;
 			System.out.println("Player 2 Computer selected");
 			return true;
 		} else if (checkNumberPadButtons()) {
 			return true;
 		} else if (checkButtonClick(7 * xMax / 10, 4 * yMax / 5, yMax / 12, xMax / 5)) {
 			gameType = 3;
 			startGame();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private void drawServerScreen() {
 		int boxHeight = yMax / 13;
 		int ySpacing = yMax / 40;
 		int xSelectorShift = xMax / 50;
 		int ySelectorShift = yMax / 50;
 		//Rows field
 		drawBox(xMax / 2, yMax / 10, boxHeight, xMax / 5);
 		graphics.drawString("Rows:", xMax / 4, yMax / 10 + boxHeight / 2);
 		graphics.drawString(Integer.toString(tempRows), xMax / 2, yMax / 10 + boxHeight / 2);
 		//Columns field
 		drawBox(xMax / 2, yMax / 10 + ySpacing + boxHeight, boxHeight, xMax / 5);
 		graphics.drawString("Columns:", xMax / 4, yMax / 10 + ySpacing + 3 * boxHeight / 2);
 		graphics.drawString(Integer.toString(tempColumns), xMax / 2, yMax / 10 + ySpacing + 3 * boxHeight / 2);
 		//Port field
 		drawBox(xMax / 2, yMax / 10 + 2 * ySpacing + 2 * boxHeight, boxHeight, xMax / 5);
 		graphics.drawString("Port:", xMax / 4, yMax / 10 + 2 * ySpacing + 5 * boxHeight / 2);
 		graphics.drawString(Integer.toString(port), xMax / 2, yMax / 10 + 2 * ySpacing + 5 * boxHeight / 2);
 		//Color selector
 		graphics.drawString("Player 1 Color:", xMax / 4, yMax / 10 + 3 * ySpacing + 7 * boxHeight / 2);
 		graphics.drawString("Black", xMax / 2 + xSelectorShift, yMax / 10 + 3 * ySpacing + 7 * boxHeight / 2);
 		graphics.drawString("White", xMax / 2 + xMax / 9 + xSelectorShift, yMax / 10 + 3 * ySpacing + 7 * boxHeight / 2);
 		if (!isPlayer1White) {
 			drawBox(xMax / 2, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 10);
 		} else {
 			drawBox(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 10);
 		}
 		//Number pad
 		drawNumberPad();
 		//Play button
 		drawBox(7 * xMax / 10, 4 * yMax / 5, yMax / 12, xMax / 5);
 		graphics.drawString("Play...", 7 * xMax / 10 + xMax / 15, 4 * yMax / 5 + yMax / 24);
 		
 	}
 	
 	private void drawLocalScreen() {
 		int boxHeight = yMax / 16;
 		int ySpacing = yMax / 40;
 		int xSelectorShift = xMax / 50;
 		int ySelectorShift = yMax / 50;
 		//Rows field
 		drawBox(xMax / 2, yMax / 10, boxHeight, xMax / 5);
 		graphics.drawString("Rows:", xMax / 4, yMax / 10 + boxHeight / 2);
 		graphics.drawString(Integer.toString(tempRows), xMax / 2, yMax / 10 + boxHeight / 2);
 		//Columns field
 		drawBox(xMax / 2, yMax / 10 + ySpacing + boxHeight, boxHeight, xMax / 5);
 		graphics.drawString("Columns:", xMax / 4, yMax / 10 + ySpacing + 3 * boxHeight / 2);
 		graphics.drawString(Integer.toString(tempColumns), xMax / 2, yMax / 10 + ySpacing + 3 * boxHeight / 2);
 		//Color selector
 		graphics.drawString("Player 1 Color:", xMax / 4, yMax / 10 + 2 * ySpacing + 5 * boxHeight / 2);
 		graphics.drawString("Black", xMax / 2 + xSelectorShift, yMax / 10 + 2 * ySpacing + 5 * boxHeight / 2);
 		graphics.drawString("White", xMax / 2 + xMax / 9 + xSelectorShift, yMax / 10 + 2 * ySpacing + 5 * boxHeight / 2);
 		if (!isPlayer1White) {
 			drawBox(xMax / 2, yMax / 10 + 2 * ySpacing + 2 * boxHeight, boxHeight - ySelectorShift, xMax / 10);
 		} else {
 			drawBox(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 2 * ySpacing + 2 * boxHeight, boxHeight - ySelectorShift, xMax / 10);
 		}
 		//Player 1 type
 		graphics.drawString("Player 1 Type:", xMax / 4, yMax / 10 + 3 * ySpacing + 7 * boxHeight / 2);
 		graphics.drawString("Human", xMax / 2 + xSelectorShift, yMax / 10 + 3 * ySpacing + 7 * boxHeight / 2);
 		graphics.drawString("Computer", xMax / 2 + xMax / 9 + xSelectorShift, yMax / 10 + 3 * ySpacing + 7 * boxHeight / 2);
 		if (isPlayer1Human) {
 			drawBox(xMax / 2, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 10);
 		} else {
 			drawBox(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 3 * ySpacing + 3 * boxHeight, boxHeight - ySelectorShift, xMax / 8);
 		}
 		//Player 2 type
 		graphics.drawString("Player 2 Type:", xMax / 4, yMax / 10 + 4 * ySpacing + 9 * boxHeight / 2);
 		graphics.drawString("Human", xMax / 2 + xSelectorShift, yMax / 10 + 4 * ySpacing + 9 * boxHeight / 2);
 		graphics.drawString("Computer", xMax / 2 + xMax / 9 + xSelectorShift, yMax / 10 + 4 * ySpacing + 9 * boxHeight / 2);
 		if (isPlayer2Human) {
 			drawBox(xMax / 2, yMax / 10 + 4 * ySpacing + 4 * boxHeight, boxHeight - ySelectorShift, xMax / 10);
 		} else {
 			drawBox(xMax / 2 + xMax / 10 + xSelectorShift, yMax / 10 + 4 * ySpacing + 4 * boxHeight, boxHeight - ySelectorShift, xMax / 8);
 		}
 		drawNumberPad();
 		//Play button
 		drawBox(7 * xMax / 10, 4 * yMax / 5, yMax / 12, xMax / 5);
 		graphics.drawString("Play...", 7 * xMax / 10 + xMax / 15, 4 * yMax / 5 + yMax / 24);
 	}
 	
 	private void drawNumberPad() {
 		int yOffset = yMax / 6;
 		int xShift = 5;
 		int yShift = -2;
 		drawBox(xMax / 3, yMax / 3 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("7", xMax / 3 + xMax / 18 - xShift,
 				yMax / 3 + yMax / 18 - yShift +  + yOffset);
 		drawBox(xMax / 3 + xMax / 9, yMax / 3 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("8", xMax / 3 + 3 * xMax / 18 - xShift,
 				yMax / 3 + yMax / 18 - yShift + yOffset);
 		drawBox(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("9", xMax / 3 + 5 * xMax / 18 - xShift,
 				yMax / 3 + yMax / 18 - yShift + yOffset);
 
 		drawBox(xMax / 3, yMax / 3 + yMax / 9 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("4", xMax / 3 + xMax / 18 - xShift,
 				yMax / 3 + 3 * yMax / 18 - yShift + yOffset);
 		drawBox(xMax / 3 + xMax / 9, yMax / 3 + yMax / 9 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("5", xMax / 3 + 3 * xMax / 18 - xShift,
 				yMax / 3 + 3 * yMax / 18 - yShift + yOffset);
 		drawBox(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + yMax / 9 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("6", xMax / 3 + 5 * xMax / 18 - xShift,
 				yMax / 3 + 3 * yMax / 18 - yShift + yOffset);
 
 		drawBox(xMax / 3, yMax / 3 + 2 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("1", xMax / 3 + xMax / 18 - xShift,
 				yMax / 3 + 5 * yMax / 18 - yShift + yOffset);
 		drawBox(xMax / 3 + xMax / 9, yMax / 3 + 2 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("2", xMax / 3 + 3 * xMax / 18 - xShift,
 				yMax / 3 + 5 * yMax / 18 - yShift + yOffset);
 		drawBox(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + 2 * yMax / 9 - 1 + yOffset, yMax / 9 , xMax / 9);
 		graphics.drawString("3", xMax / 3 + 5 * xMax / 18 - xShift,
 				yMax / 3 + 5 * yMax / 18 - yShift + yOffset);
 
 		if (clientScreenVisible) {
 			drawBox(xMax / 3, yMax / 3 + 3 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9);
 			graphics.drawString(".", xMax / 3 + xMax / 18 - xShift,
 					yMax / 3 + 7 * yMax / 18 - yShift + yOffset);
 		}
 		drawBox(xMax / 3 + xMax / 9, yMax / 3 + 3 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("0", xMax / 3 + 3 * xMax / 18 - xShift,
 				yMax / 3 + 7 * yMax / 18 - yShift + yOffset);
 		drawBox(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + 3 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9);
 		graphics.drawString("<-", xMax / 3 + 5 * xMax / 18 - xShift,
 				yMax / 3 + 7 * yMax / 18 - yShift + yOffset);
 	}
 	
 	private boolean checkNumberPadButtons() {
 		if (forceUpdate) {
 			/* To handle clicks "going through" to the next menu screen */
 			return false;
 		}
 		String tempString = "";
 		boolean add = true;
 		boolean returnVal = false;
 		int yOffset = yMax / 6;
 		if (checkButtonClick(xMax / 3, yMax / 3 + yOffset, yMax / 9, xMax / 9)) {
 			//7 pressed
 			tempString += "7";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + xMax / 9, yMax / 3 + yOffset, yMax / 9, xMax / 9)) {
 			//8 pressed
 			tempString += "8";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + yOffset, yMax / 9, xMax / 9)) {
 			//9 pressed
 			tempString += "9";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3, yMax / 3 + yMax / 9 + yOffset, yMax / 9, xMax / 9)) {
 			//4 pressed
 			tempString += "4";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + xMax / 9, yMax / 3 + yMax / 9 + yOffset, yMax / 9, xMax / 9)) {
 			//5 pressed
 			tempString += "5";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + yMax / 9 + yOffset, yMax / 9, xMax / 9)) {
 			//6 pressed
 			tempString += "6";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3, yMax / 3 + 2 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9)) {
 			//1 pressed
 			tempString += "1";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + xMax / 9, yMax / 3 + 2 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9)) {
 			//2 pressed
 			tempString += "2";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + 2 * yMax / 9 - 1 + yOffset, yMax / 9 , xMax / 9)) {
 			//1 pressed
 			tempString += "3";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3, yMax / 3 + 3 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9) && clientScreenVisible) {
 			//. pressed
 			tempString += ".";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + xMax / 9, yMax / 3 + 3 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9)) {
 			//0 pressed
 			tempString += "0";
 			returnVal = true;
 		} else if (checkButtonClick(xMax / 3 + 2 * xMax / 9 - 1, yMax / 3 + 3 * yMax / 9 - 1 + yOffset, yMax / 9, xMax / 9)) {
 			//Backspace pressed
 			add = false;
 			returnVal = true;
 		} else {
 			returnVal = false;
 		}
 		if (clientScreenVisible) {
 			if (focusedField == 1) {
 				if (add) {
 					address += tempString;
 				} else {
 					if (address.length() > 0) {
 						address = address.substring(0, address.length() - 1);
 					}
 					System.out.println(address);
 				}
 			} else if (focusedField == 2) {
 				if (add) {
 					port = lengthenInt(port, tempString);
 				} else {
 					port = shortenInt(port);
 				}
 			}
 		} else if (serverScreenVisible) {
 			if (focusedField == 1) {
 				if (add) {
 					tempRows = lengthenInt(tempRows, tempString);
 				} else {
 					tempRows = shortenInt(tempRows);
 				}
 			} else if (focusedField == 2) {
 				if (add) {
 					tempColumns = lengthenInt(tempColumns, tempString);
 				} else {
 					tempColumns = shortenInt(tempColumns);
 				}
 			} else if (focusedField == 3) {
 				if (add) {
 					port = lengthenInt(port, tempString);
 				} else {
 					port = shortenInt(port);
 				}
 			}
 		} else if (localScreenVisible) {
 			if (focusedField == 1) {
 				if (add) {
 					tempRows = lengthenInt(tempRows, tempString);
 				} else {
 					tempRows = shortenInt(tempRows);
 				}
 			} else if (focusedField == 2) {
 				if (add) {
 					tempColumns = lengthenInt(tempColumns, tempString);
 				} else {
 					tempColumns = shortenInt(tempColumns);
 				}
 			}
 		}
 		return returnVal;
 	}
 	
 	private int shortenInt(int i) {
 		String tempString = Integer.toString(i);
 		if (tempString.length() >= 1) {
 			tempString = tempString.substring(0, tempString.length() - 1);
 		}
 		if (tempString.length() == 0) {
 			return 0;
 		} else {
 			return Integer.parseInt(tempString);
 		}
 	}
 	
 	private int lengthenInt(int i, String addition) {
 		String tempString = Integer.toString(i);
 		tempString += addition;
 		return Integer.parseInt(tempString);
 	}
 	
 	private void startGame() {
 		clientScreenVisible = false;
 		serverScreenVisible = false;
 		localScreenVisible = false;
 		gameVisible = true;
 		if (gameType == 1) {
 			//Client
 			clientMode(true, address, port);
 		} else if (gameType == 2) {
 			//Server
 			serverMode(true);
 		} else if (gameType == 3) {
 			//Local
 			
 		}
 	}
 	
 	private void quit() {
 		
 	}
 	
 	private void updateScreen() {
 		bufferStrat = getBufferStrategy();
 		graphics = null;
 
 		try {
 			graphics = bufferStrat.getDrawGraphics();
 			Graphics2D graphics2D = (Graphics2D) graphics;
 			RenderingHints renderHints = new RenderingHints(
 					RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			graphics2D.setRenderingHints(renderHints);
 			if (clicked || forceUpdate) {
 				clicked = false;
 				clearWindow();
 				if (gameVisible) {
 					drawGrid();
 					drawInfo();
 					drawPieces();
 				}
 				if (clientServerVisible) {
 					drawClientServer();
 				}
 				if (clientScreenVisible) {
 					drawClientScreen();
 				}
 				if (serverScreenVisible) {
 					drawServerScreen();
 				}
 				if (localScreenVisible) {
 					drawLocalScreen();
 				}
 				drawButtons();
 				processClick(xClick, yClick);
 
 			}
 			drawTime();
 			if (advWithVisible) {
 				drawFlashingBox(180, yMax - 40, 38, 240);
 				ticked = false;
 			}
 			
 
 		} catch (Exception e) {
 			System.out.println("Error: " + e.getMessage());
 		} finally {
 
 		}
 		graphics.dispose();
 		bufferStrat.show();
 		Toolkit.getDefaultToolkit().sync();
 		forceUpdate = false;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	//Client should be set as the white player
 	
 	public ServerSocket create(){
 		
 		for(int i = 1024; i < 2048; i++){
 			try{
 				return new ServerSocket(i);
 			}
 			catch(IOException e){
 				continue;
 			}
 		}
 		return null;
 	}
 	
 	//serverMode(human);
 
 	//clientMode(human,host,port);
 
 	public String getPlayerInput(){
 		System.out.print("Enter a move ");
 		String playerInput = "";
 		Scanner scan2 = new Scanner(System.in);			//The server is white and a human is playing
 		playerInput = scan2.nextLine();					//TODO update this code to work with the gamewindow
 		return playerInput;
 	}
 	
 	public void serverMode(boolean human){
 		
 		xBoardDim = tempRows;
 		yBoardDim = tempColumns;
 		calculateDimensions();
 		if (radius > xGridMin) {
 			xGridMin = (radius) + 10;
 			yGridMin = (radius) + 40;
 			calculateDimensions();
 		}
		int temp = tempRows;
 		tempRows = tempColumns;
		tempColumns = temp;
		game = new Fanorona(yBoardDim,xBoardDim);
 		
 		long startTime = 0;
 		long endTime = 0;
 
 		Piece.Type clientPlayer;
 		ServerSocket sock = null;
 
 		try{
 			sock = new ServerSocket(port);
 		}
 		catch(Exception e){
 			System.out.println("Port not available");
 		}
 		System.out.println("Listening on "+sock.getLocalPort());
 		
 		Socket client = null;
 		PrintWriter out = null;
 		BufferedReader in = null;
 		try{
 			client = sock.accept();
 			out = new PrintWriter(client.getOutputStream(), true);		//socket related stuff
 			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
 		}
 		catch(IOException e){
 			System.out.println("Accept failed");
 			System.exit(1);
 		}
 		
 		out.println("WELCOME");			//server sends the client a welcome
 		out.println("INFO "+yBoardDim+" "+xBoardDim+" "+clientT+" "+responseTime);   //send the info command
 		
		game = new Fanorona(yBoardDim,xBoardDim);
 		
 		Piece.Type serverPlayer;
 		if(clientT == 'B'){
 			clientPlayer = Piece.Type.BLACK;
 			serverPlayer = Piece.Type.WHITE;			//initialize game and determine which side controls which pieces
 		}
 		else{
 			clientPlayer = Piece.Type.WHITE;
 			serverPlayer = Piece.Type.BLACK;
 		}
 
 		String playerInput = "";
 		boolean cont = true;
 		
 		try {
 			while((playerInput = in.readLine()) != null && cont){			//loops until it reads a null or cont == false
 																				//cont is set to false when an illegal move occurs
 				if(playerInput.equals("READY")){			//client sends ready, so begin game
 					out.println("BEGIN");
 					
 					if(serverPlayer == Piece.Type.WHITE && !human){
 
 						String move;					//if the server is white and the AI is playing...
 						TimedMoveGet tmg = new TimedMoveGet(game.copyGame(), 0, 0, serverPlayer);
 						Thread t = new Thread(tmg);
 						t.run();
 						try {
 							t.join();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						move = tmg.bestMove;			//tmg.bestMove is updated anytime a better move is found
 						game.move(move);
 						out.println(move);				//do the move and send it to the client
 
 					}
 					else if(serverPlayer == Piece.Type.WHITE && human){
 						
 						String input = getPlayerInput();
 						input = game.convertToInternalMove(input);
 						
 						game.move(input);
 						out.println(input);
 						
 					}
 					continue;		//go back to the beginning of the while loop and wait for more input
 				}
 				if(playerInput.equals("OK")){
 					startTime = System.currentTimeMillis();			//client has acknowledged getting a move, start timer
 					continue;
 				}
 				if(playerInput.equals("TIME")){
 					System.out.println("Time exceeded");		//Client reports that the server took too long to respond
 					out.println("WINNER");
 					break;
 				}
 				if(playerInput.equals("ILLEGAL")){
 					System.out.println("Illegal move");			//client reports that the server attempted an illegal move
 					out.println("WINNER");
 					break;
 				}
 				
 				System.out.println("input = " + playerInput);	
 				int index = playerInput.indexOf(' ');
 				String command = "";
 				try{
 					command = playerInput.substring(0,index);		//Gets the command from the player input
 				}
 				catch(Exception e){
 					command = playerInput;
 				}
 				
 				if(command.equals("A") || command.equals("W") || command.equals("S") || command.equals("P")){	//A move command
 					endTime = System.currentTimeMillis();
 					
 					if(endTime-startTime > responseTime && responseTime != 0 && game.numberOfTurns != 0){
 						out.println("TIME");
 						out.println("LOSER");			//Client exceeded time limit on a move that wasnt the first		
 						break;
 					}
 					
 					
 					out.println("OK");					//Server responds ok, it received a move
 					long time3 = System.currentTimeMillis();
 					if(game.capturingMoveAvailable() && !game.isPossibleCapturingMove(playerInput)){
 						out.println("ILLEGAL");
 						out.println("LOSER");		//If the move is illegal, notify the client
 						System.out.println("WINNER");
 						break;
 					}
 					else if(!game.capturingMoveAvailable() && !game.isPossibleNonCapturingMove(playerInput)){
 						out.println("ILLEGAL");
 						out.println("LOSER");
 						System.out.println("WINNER");
 						break;
 					}
 					
 					game.move(playerInput);
 					//game.prettyprint();
 					forceUpdate = true;
 					updateScreen();
 					int val = game.checkEndGame();		//returns an int which represents the possible game states
 					if(val == 1){
 																			//white win
 						if(clientPlayer == Piece.Type.WHITE){
 							out.println("WINNER");
 							System.out.println("LOSER");
 							break;
 						}
 						else{
 							out.println("LOSER");
 							System.out.println("WINNER");
 							break;
 						}
 					}
 					else if(val == -1){
 																			//black win
 						if(clientPlayer == Piece.Type.WHITE){
 							out.println("LOSER");
 							System.out.println("WINNER");
 							break;
 						}
 						else{
 							out.println("WINNER");
 							System.out.println("LOSER");
 							break;
 						}
 					}
 					else if(val == 2){
 																			//max turns
 						out.println("TIE");
 						System.out.println("TIE");
 						break;
 					}
 					else{											//The game is not over
 						if(!human){											
 
 							String move;
 							TimedMoveGet tmg = new TimedMoveGet(game.copyGame(), time3, responseTime, serverPlayer);
 							Thread t = new Thread(tmg);
 							t.run();										//Get a move from the AI with a time limit of responseTime
 							try {											//time3 is the start time
 								t.join();
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 							move = tmg.bestMove;
 							game.move(move);
 							out.println(move);
 
 						}
 						else{
 							String input = getPlayerInput();
 							input = game.convertToInternalMove(input);
 							
 							game.move(input);
 							out.println(input);
 						}
 					}
 					
 					val = game.checkEndGame();					//perform an endgame check again
 					if(val == 1){
 						//white win
 						if(clientPlayer == Piece.Type.WHITE){
 							out.println("WINNER");
 							System.out.println("LOSER");
 							break;
 						}
 						else{
 							out.println("LOSER");
 							System.out.println("WINNER");
 							break;
 						}
 					}
 					else if(val == -1){
 						//black win
 						if(clientPlayer == Piece.Type.WHITE){
 							out.println("LOSER");
 							System.out.println("WINNER");
 							break;
 						}
 						else{
 							out.println("WINNER");
 							System.out.println("LOSER");
 							break;
 						}
 					}
 					else if(val == 2){
 						//max turns
 						out.println("TIE");
 						System.out.println("TIE");
 						break;
 					}
 					//Approach move
 				}
 				else{									//The command was not recognized
 					out.println("ILLEGAL");
 					cont = false;
 				}
 			}
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			sock.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void clientMode(boolean human,String host, int port){
 		
 		//Running as a client
 		
 		long startTime = 0;
 		long endTime = 0;
 		
 		game = null;
 		Piece.Type clientPlayer = null;
 		Piece.Type serverPlayer = null;
 		
 		int responseTime = 0;
 		
 		Socket tSock = null;
 		BufferedReader in = null;
 		PrintWriter out = null;
 		
 		boolean cont = true;
 		while(cont){
 			try {
 				tSock = new Socket(host, port);					//Socket related
 				out = new PrintWriter(tSock.getOutputStream(), true);
 				in = new BufferedReader(new InputStreamReader(tSock.getInputStream()));
 				cont = false;
 			}
 			catch (UnknownHostException e) {
 			}
 			catch (IOException e) {
 			}
 			catch (Exception e){
 
 			}
 		}
 		System.out.println("client - got connection");
 		
 		String response = "";
 		try {
 			while((response = in.readLine()) != null){
 				System.out.println("from server: "+response+".");
 				if(response.equals("WELCOME")){						//server sends a welcome, nothing to do here
 					continue;
 				}
 				if(response.equals("OK")){
 					startTime = System.currentTimeMillis();			//server received the move, start timer
 					continue;
 				}
 				if(response.equals("ILLEGAL")){
 					break;											//server reports that an illegal move was attempted
 				}
 				if(response.equals("LOSER")){						
 					System.out.println("Lost");
 					break;
 				}													//Server reporting win/loss condition
 				if(response.equals("WINNER")){
 					System.out.println("won");
 					break;
 				}
 				if(response.equals("TIE")){							//TODO display end game conditions on the gamewindow
 					System.out.println("Tie");
 					break;
 				}
 				if(response.equals("TIME")){
 					System.out.println("Time limit exceeded");		//Server reporting time limit exceeded, continue. A LOSER command is coming next.
 					continue;
 				}
 				
 				int index = response.indexOf(' ');
 				String command = response;
 				if(index == -1){
 					command = response;								//Retrieve the command from the player input
 				}
 				else{
 					command = response.substring(0,index);
 				}
 				System.out.println("Command: "+command);
 				
 				if(command.equals("INFO")){
 				
 					String cmd = response;
 					cmd = cmd.substring(index+1);
 					index = cmd.indexOf(' ');
 					tempRows = Integer.parseInt(cmd.substring(0,index));
 					cmd = cmd.substring(index+1);
 					index = cmd.indexOf(' ');
 					tempColumns = Integer.parseInt(cmd.substring(0,index));			//Get the various parameters passed by the server
 					cmd = cmd.substring(index+1);
 					index = cmd.indexOf(' ');
 					char startType = cmd.charAt(index-1);
 					cmd = cmd.substring(index+1);
 					int timeRestriction = Integer.parseInt(cmd);
 					//System.out.println("rows: "+rows+" columns: "+columns);
 					
 					xBoardDim = tempRows;
 					yBoardDim = tempColumns;
					int temp = tempRows;
 					tempRows = tempColumns;
					tempColumns = temp;
 					
 					calculateDimensions();
 					if (radius > xGridMin) {
 						xGridMin = (radius) + 10;
 						yGridMin = (radius) + 40;
 						calculateDimensions();
 					}
 					
					game = new Fanorona(yBoardDim,xBoardDim);
 					if(startType == 'W'){
 						clientPlayer = Piece.Type.WHITE;
 					}																	//Determine which side the client plays on
 					else if(startType == 'B'){
 						clientPlayer = Piece.Type.BLACK;
 					}
 					else{
 						System.out.println("Type error");
 						System.exit(1);
 					}
 					if(clientPlayer == Piece.Type.WHITE){
 						serverPlayer = Piece.Type.BLACK;
 					}
 					else{
 						serverPlayer = Piece.Type.WHITE;
 					}
 						responseTime = timeRestriction;
 						out.println("READY");												//Let server know the client is done setting up the game
 					}
 				else if(command.equals("BEGIN")){
 													//Start game
 					//game.prettyprint();
 					forceUpdate = true;
 					updateScreen();
 					if(clientPlayer == Piece.Type.WHITE && !human){
 						String move;
 						TimedMoveGet tmg = new TimedMoveGet(game.copyGame(), 0, 0, clientPlayer);		//get an AI move with no time restriction
 						Thread t = new Thread(tmg);
 						t.run();
 						try {
 							t.join();
 						}
 						catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						move = tmg.bestMove;
 						game.move(move);
 						out.println(move);
 					}
 					else if(clientPlayer == Piece.Type.WHITE && human){
 					
 						String input = getPlayerInput();
 						input = game.convertToInternalMove(input);
 						
 						game.move(input);
 						out.println(input);
 						
 					}
 					continue;
 				}
 				else if(command.equals("A") || command.equals("W") || command.equals("S") || command.equals("P")){
 				
 					endTime = System.currentTimeMillis();
 					if(endTime - startTime > responseTime && responseTime != 0 && game.numberOfTurns != 0){
 						out.println("TIME");						//report time limit exceeded
 						continue;
 					}
 					
 					out.println("OK");								//acknowledge move received
 					startTime = System.currentTimeMillis();
 					
 					if(game.capturingMoveAvailable() && !game.isPossibleCapturingMove(response)){
 						out.println("ILLEGAL");
 						continue;
 					}
 					else if(!game.capturingMoveAvailable() && !game.isPossibleNonCapturingMove(response)){
 						out.println("ILLEGAL");
 						continue;
 					}
 					game.move(response);
 					//game.prettyprint();
 					forceUpdate = true;
 					updateScreen();
 					if(!human){
 						String move;
 						long tempTime = (long) responseTime;
 						TimedMoveGet tmg = new TimedMoveGet(game.copyGame(), startTime, tempTime, clientPlayer);		//Get AI move move with time limit
 						Thread t = new Thread(tmg);
 						t.run();
 						
 						try {
 							t.join();
 						}
 						catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						move = tmg.bestMove;
 						System.out.println("Move: "+move);
 						game.move(move);
 						//game.prettyprint();
 						forceUpdate = true;
 						updateScreen();
 						out.println(move);
 						}
 					else{
 						String input = getPlayerInput();
 						input = game.convertToInternalMove(input);
 					
 						game.move(input);
 						out.println(input);
 					}
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();			//the end of a giant try/catch which started right before the while loop
 		}
 		
 		out.close();
 		try {
 			in.close();					//clean up the buffers and sockets
 			tSock.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 }
