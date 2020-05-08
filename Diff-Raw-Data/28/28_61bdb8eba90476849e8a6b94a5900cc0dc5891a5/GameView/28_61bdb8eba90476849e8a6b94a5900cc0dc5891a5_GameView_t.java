 package edu.mharper.tp2;
 
 import java.awt.BasicStroke;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 public class GameView extends Canvas implements MouseListener {
 	
 	public Point prevSelection;
 	public Point advanceCaptureChoice; //For advance/withdraw capture choice
 	public Point withdrawCaptureChoice; //For advance/withdraw capture choice
 	public Point captureMovePoint; //For advance/withdraw capture choice
 	public ArrayList<Point> prevMoves; //For chain captures
 	public Point tileSelection;
 	public static GameManager gameManager;
 	
 	public GameView() {
 		setPreferredSize(new Dimension(Main.windowWidth, Main.windowHeight));
 		
 		prevSelection = null;
 		advanceCaptureChoice = null;
 		withdrawCaptureChoice = null;
 		prevMoves = new ArrayList<Point>();
 		gameManager = new GameManager();
 		
 		addMouseListener(this);
 		gameManager.startGame();
 	}
 	
 	public void newGame() {
 		gameManager.genBoard();
 		repaint();
 	}
 	
 	public void saveGame() {
 		gameManager.saveGame();
 	}
 	
 	public void loadGame(String fileName) {
 		gameManager.loadGame(fileName);
 	}
 	
 	public void paint(Graphics g) {	
 		drawBackground(g);
 		//drawSpaces(g);
 		drawLines(g);
 		drawTileSelection(g);
 		drawPieces(g);
 		drawSelection(g);
 		
 		//If player has to choose moves, display these choices
 		//If there is a chain, display possible chain moves
 		//Else, draw moves as normal			
 		if(advanceCaptureChoice != null && withdrawCaptureChoice != null)
 			drawCaptureChoices(g);
 		else if(!prevMoves.isEmpty())
 			drawValidChainMoves(g);
 		else
 			drawValidMoves(g);
 	}
 	
 	private void drawValidMoves(Graphics g) {
 		if(prevSelection == null)
 			return;
 		
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(Color.green);
 		g2.setStroke(new BasicStroke(5));
 		
 		int x = prevSelection.getX() + Main.pieceSize / 2; //* Main.tileSize + Main.tileSize / 2;
 		int y = prevSelection.getY() + Main.pieceSize / 2; //* Main.tileSize + Main.tileSize / 2;
 		
 		//Point on the board (in units of tiles)
 		int pieceX = coordToTile(prevSelection.getX());
 		int pieceY = coordToTile(prevSelection.getY());
 		Point piecePoint = new Point(pieceX, pieceY);
 		GamePiece selectedPiece = gameManager.getBoard().getPiece(piecePoint);
 			
 		ArrayList<Point> validMoves = gameManager.getValidMoves(selectedPiece);
 		for (int i = 0; i < validMoves.size(); i++) {
 			// Draw point from prevSelection to its valid moves
 			g2.drawLine(x, y, validMoves.get(i).getX() * Main.tileSize + Main.tileSize / 2, validMoves.get(i).getY() * Main.tileSize + Main.tileSize / 2);
 		}
 	}
 	
 	private void drawCaptureChoices(Graphics g)
 	{
 		Graphics2D g2 = (Graphics2D) g;
 		
 		g2.setColor(Color.magenta);
 			
 		//Draw circles showing choices
 		int advanceX = advanceCaptureChoice.getX() * Main.tileSize + Main.pieceSize / 2;
 		int advanceY = advanceCaptureChoice.getY() * Main.tileSize + Main.pieceSize / 2;
 	
 		int withdrawX = withdrawCaptureChoice.getX() * Main.tileSize + Main.pieceSize / 2;
 		int withdrawY = withdrawCaptureChoice.getY() * Main.tileSize + Main.pieceSize / 2;
 				
 		g2.fillOval(advanceX, advanceY, Main.pieceSize, Main.pieceSize);
 		g2.fillOval(withdrawX, withdrawY, Main.pieceSize, Main.pieceSize);
 	}
 	
 	private void drawValidChainMoves(Graphics g)
 	{
 		Graphics2D g2 = (Graphics2D) g;
 		
 		g2.setColor(Color.red);
 			
 		//Point on the board (in units of tiles)
 		int pieceX = coordToTile(prevSelection.getX());
 		int pieceY = coordToTile(prevSelection.getY());
 		Point piecePoint = new Point(pieceX, pieceY);
 		GamePiece selectedPiece = gameManager.getBoard().getPiece(piecePoint);
 		
 		ArrayList<Point> validChainMoves = gameManager.getValidChainMoves(selectedPiece, prevMoves);
 		for(Point move : validChainMoves)
 		{
 			int drawX = (move.getX() * Main.tileSize) + (int)(Main.tileSize * 0.4);
 			int drawY = (move.getY() * Main.tileSize) + (int)(Main.tileSize * 0.4);
 			int drawW = (int)(Main.tileSize * 0.2);
 			int drawH = (int)(Main.tileSize * 0.2);
 			g2.fillOval(drawX, drawY, drawW, drawH);
 		}
 	}
 	
 	private void drawBackground(Graphics g) {
 		g.setColor(Color.lightGray);
 		g.fillRect(0, 0, Main.windowWidth, Main.windowHeight);
 	}
 	
 	private void drawSpaces(Graphics g) {
 		Dimension panelSize = getParent().getSize();
 		double horizontalSpacing = panelSize.getWidth() / Main.horizontalSpaces;
 		double verticalSpacing = panelSize.getHeight() / Main.verticalSpaces;
 		
 		g.setColor(Color.white);
 		for (int i = 0; i < Main.horizontalSpaces; i++) {
 			for (int j = 0; j < Main.verticalSpaces; j++) {
 				g.fillOval((int) (i * horizontalSpacing), (int) (j * verticalSpacing), (int) horizontalSpacing, (int) verticalSpacing);
 			}
 		}
 	}
 	
 	private void drawLines(Graphics g) {
 		int spacing = Main.tileSize / 2;
 		
 		// Set up drawing properties
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(Color.blue);
 		g2.setStroke(new BasicStroke(5));
 		
 		// Draw vertical lines
 		for (int i = 0; i < Main.horizontalSpaces; i++) {
 			//g2.drawLine(i * Main.tileSize + spacing, spacing, i * Main.tileSize + spacing, getParent().getHeight() - spacing);
 			g2.drawLine(i * Main.tileSize + spacing, spacing, i * Main.tileSize + spacing, Main.tileSize * Main.verticalSpaces - spacing);
 		}
 		
 		// Draw horizontal lines
 		for (int i = 0; i < Main.verticalSpaces; i++) {
 			g2.drawLine(spacing, i * Main.tileSize + spacing, Main.tileSize * Main.horizontalSpaces - spacing, i * Main.tileSize + spacing);
 		}
 		
 		// Draw diagonals
		int drawFactor = (Main.horizontalSpaces + Main.verticalSpaces / 2) % 2;
 		for (int i = 0; i < Main.horizontalSpaces; i++) {
 			for (int j = 0; j < Main.verticalSpaces; j++) {
				Point testPoint = new Point(i,j);
				if (gameManager.getBoard().canMoveDiagonal(testPoint)) {
 					// Draw down right
 					if (j != Main.verticalSpaces - 1 && i != Main.horizontalSpaces - 1) {
 						g2.drawLine(i * Main.tileSize + Main.tileSize / 2, j * Main.tileSize + Main.tileSize / 2, 
 							(i + 1) * Main.tileSize + Main.tileSize / 2, (j + 1) * Main.tileSize + Main.tileSize / 2);
 					}
 					// Draw down left
 					if (i != 0 && j != Main.verticalSpaces - 1)
 						g2.drawLine(i * Main.tileSize + Main.tileSize / 2, j * Main.tileSize + Main.tileSize / 2, 
 							(i - 1) * Main.tileSize + Main.tileSize / 2, (j + 1) * Main.tileSize + Main.tileSize / 2);
 				}
				else if (gameManager.getBoard().canMoveDiagonal(testPoint)) {
 					g2.drawLine(i * Main.tileSize + Main.tileSize / 2, j * Main.tileSize + Main.tileSize / 2, 
 						(i - 1) * Main.tileSize + Main.tileSize / 2, (j + 1) * Main.tileSize + Main.tileSize / 2);
 					g2.drawLine(i * Main.tileSize + Main.tileSize / 2, j * Main.tileSize + Main.tileSize / 2, 
 						(i + 1) * Main.tileSize + Main.tileSize / 2, (j + 1) * Main.tileSize + Main.tileSize / 2);
 					
 				}
 			}
 		}
 	}
 	
 	//Draw game pieces over board	
 	public void drawPieces(Graphics g) {
 		ArrayList<GamePiece> pieces = gameManager.getBoard().getPiecesList();
 		for (GamePiece piece : pieces) {
 			if (piece != null) {
 				int x = piece.getColumn();
 				int y = piece.getRow();
 				Color color = piece.getColor();
 				
 				int xCoord = Main.tileSize * x + Main.pieceSize / 2;
 				int yCoord = Main.tileSize * y + Main.pieceSize / 2;
 				
 				//If piece is sacrificed, display in an different color
 				if(piece.equals(gameManager.getSacrificedPiece()))
 					g.setColor(Color.gray);
 				else
 					g.setColor(color);
 				
 				g.fillOval(xCoord, yCoord, Main.pieceSize, Main.pieceSize);
 			}
 		}
 	}
 	
 	private void drawSelection(Graphics g) {
 		if(!prevMoves.isEmpty())
 			return;
 		
 		g.setColor(Color.green);
 		if (prevSelection != null) {
 			g.fillOval(prevSelection.getX(), prevSelection.getY(), Main.pieceSize, Main.pieceSize);
 		}
 		//prevSelection = null;
 	}
 	
 	private void drawTileSelection(Graphics g) {
 		if (tileSelection != null) {
 			g.setColor(Color.green);
 			g.fillOval(tileSelection.getX(), tileSelection.getY(), Main.tileSize, Main.tileSize);
 		}
 		//tileSelection = null;
 	}
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 		
 		int xTile = coordToTile(x);
 		int yTile = coordToTile(y);
 		
 		tileSelection = null;
 		
 		// A piece has been selected
 		if (isPiecePresent(xTile, yTile)) {
 			//System.out.println("Piece present at clicked location");
 			int xCoord = xTile * Main.tileSize + Main.pieceSize / 2;
 			int yCoord = yTile * Main.tileSize + Main.pieceSize / 2;
 			
 			//int pieceX = coordToTile(xCoord);
 			//int pieceY = coordToTile(yCoord);
 			Point piecePoint = new Point(xTile, yTile);
 			
 			// Check if player is choosing piece to capture in an advance/withdraw capture
 			if (advanceCaptureChoice != null && withdrawCaptureChoice != null)
 			{
 				int prevX = coordToTile(prevSelection.getX());
 				int prevY = coordToTile(prevSelection.getY());
 				Point prevPoint = new Point(prevX, prevY);
 				GamePiece movingPiece = gameManager.getBoard().getPiece(prevPoint);
 				
 				//Make sure piece player chose is a viable piece to capture
 				if(advanceCaptureChoice.equals(piecePoint))
 				{
 					gameManager.advanceCapturePieces(movingPiece, captureMovePoint);
 				}
 				else if(withdrawCaptureChoice.equals(piecePoint))
 				{
 					gameManager.withdrawCapturePieces(movingPiece, captureMovePoint);
 				}
 				else
 				{
 					//Player did not choose either of the pieces that could be captured
 					advanceCaptureChoice = null;
 					withdrawCaptureChoice = null;
 					prevSelection = new Point(x,y);
 					prevMoves.clear();
 					return;
 				}
 				
 				prevMoves.add(prevPoint);
 				gameManager.movePiece(movingPiece, captureMovePoint);
 					
 				//Check if chain moves are possible; if so, add to chain
 				if(gameManager.getValidChainMoves(movingPiece, prevMoves).isEmpty())
 				{
 					endTurnAndUpdate();
 					return;
 				}
 				else
 				{
 					advanceCaptureChoice = null;
 					withdrawCaptureChoice = null;
 					Point newPoint = movingPiece.getPoint();
 					prevSelection = new Point(tileToCoord(newPoint.getX()), tileToCoord(newPoint.getY()));
 				}
 			}
 			//If same piece selected twice, count as a sacrifice move
 			else if(prevSelection != null &&
 					coordToTile(prevSelection.getX()) == xTile &&
 					coordToTile(prevSelection.getY()) == yTile)
 			{
 				GamePiece selectedPiece = gameManager.getBoard().getPiece(piecePoint);
 				
 				gameManager.sacrificePiece(selectedPiece);
 				endTurnAndUpdate();
 				return;
 			}
 			else
 			{
 				//Highlight piece clicked as selected piece
 				GamePiece selectedPiece = gameManager.getBoard().getPiece(piecePoint);
 				
 				if(selectedPiece.getColor().equals(gameManager.getCurrentPlayer()))
 					prevSelection = new Point(xCoord, yCoord);
 			}
 		}
 		else {
 			//If piece already selected and empty tile selected after, a move of some kind will be made
 			if(prevSelection != null) {
 				int pieceX = coordToTile(prevSelection.getX());
 				int pieceY = coordToTile(prevSelection.getY());
 				Point piecePoint = new Point(pieceX, pieceY);
 				
 				GamePiece selectedPiece = gameManager.getBoard().getPiece(piecePoint);
 				
 				Point movePoint = new Point(xTile, yTile);
 				
 				boolean moveValid;
 				
 				//Check for a chain move
 				if(!prevMoves.isEmpty())
 				{
 					moveValid = gameManager.isValidChainMove(selectedPiece, movePoint, prevMoves);
 				}
 				else
 				{
 					moveValid = gameManager.isValidMove(selectedPiece, movePoint);
 				}
 				
 				if(moveValid)
 				{
 					//If move could be an advance or withdraw, display the possible options
 					if(gameManager.isAdvanceAndWithdrawCaptureMove(selectedPiece, movePoint))
 					{
 						int startX = selectedPiece.getPoint().getX();
 						int startY = selectedPiece.getPoint().getY();
 						
 						int deltaX = movePoint.getX() - startX;
 						int deltaY = movePoint.getY() - startY;
 						advanceCaptureChoice = new Point(movePoint.getX() + deltaX, movePoint.getY() + deltaY);
 						withdrawCaptureChoice = new Point(startX - deltaX, startY - deltaY);
 						captureMovePoint = movePoint;
 					}
 						
 					//Else, move and capture pieces accordingly
 					else
 					{
 						if(gameManager.isAdvanceCaptureMove(selectedPiece, movePoint))
 							gameManager.advanceCapturePieces(selectedPiece, movePoint);
 						else if(gameManager.isWithdrawCaptureMove(selectedPiece, movePoint))
 							gameManager.withdrawCapturePieces(selectedPiece, movePoint);
 						else
 							//Paika move
 						{
 							gameManager.movePiece(selectedPiece, movePoint);
 							endTurnAndUpdate();
 							return;
 						}
 						
 						prevMoves.add(selectedPiece.getPoint());
 						gameManager.movePiece(selectedPiece, movePoint);
 						
 						//Check if chain moves are possible; if so, add to chain
 						if(gameManager.getValidChainMoves(selectedPiece, prevMoves).isEmpty())
 						{
 							//prevSelection = null;
 							endTurnAndUpdate();
 							return;
 						}
 						else
 						{
 							prevSelection = new Point(x,y);
 							advanceCaptureChoice = null;
 							withdrawCaptureChoice = null;
 						}
 					}
 				}
 			}
 			else {
 				// If piece not selected, highlight the tile
 				int xCoord = xTile * Main.tileSize;
 				int yCoord = yTile * Main.tileSize;
 				tileSelection = new Point(xCoord, yCoord);
 				//repaint();
 			}
 		}
 		repaint();
 	}
 	
 	public void endTurnAndUpdate()
 	{
 		gameManager.endTurn();
 		//Clear tracking variables
 		prevSelection = null;
 		prevMoves.clear();
 		advanceCaptureChoice = null;
 		withdrawCaptureChoice = null;
 		captureMovePoint = null;
 		
 		// Reset timer
 		View.infoView.resetTime();
 		View.infoView.updateColors();
 		View.infoView.updateTurns();
 		
 		if (gameManager.getTurnsLeft() < 1 || View.infoView.white == 0 || View.infoView.black == 0) {
 			View.gameOver();
 		}
 		repaint();
 	}
 	
 	boolean isPiecePresent(int x, int y) {
 		ArrayList<GamePiece> pieces = gameManager.getBoard().getPiecesList();
 		for (GamePiece piece : pieces) {
 			if (piece != null && piece.getColumn() == x && piece.getRow() == y)
 				return true;
 		}
 		return false;
 	}
 	
 	int coordToTile(int offset) {
 		return offset / Main.tileSize;
 	}
 	
 	int tileToCoord(int offset) {
 		return offset * Main.tileSize + (Main.tileSize / 2);
 	}
 }
