 package edu.mharper.tp2;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 public class View extends Canvas implements ActionListener {
 	
 	JFrame frame;
 	JPanel panel;
 	JMenuBar menuBar;
 	JMenu fileMenu;
 	JMenuItem newGameMenuItem;
 	JMenuItem exitMenuItem;
 	
 	private ArrayList<GamePiece> pieces;
 	
 	public View() {
 		pieces = new ArrayList<GamePiece>();
 		
 		frame = new JFrame(Main.gameTitle);
 		menuBar = new JMenuBar();
 		panel = new JPanel(new BorderLayout());
 		fileMenu = new JMenu("File");
 		newGameMenuItem = new JMenuItem("New game");
 		exitMenuItem = new JMenuItem("Exit");
 		
 		// Set up window
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		frame.setPreferredSize(new Dimension(Main.windowWidth, Main.windowHeight));
 		
 		// Set up menu
 		newGameMenuItem.addActionListener(this);
 		exitMenuItem.addActionListener(this);
 		fileMenu.add(newGameMenuItem);
 		fileMenu.add(exitMenuItem);
 		menuBar.add(fileMenu);
 		
 		// Add the items to the window
 		panel.add(this);
 		frame.setJMenuBar(menuBar);
 		frame.add(panel);
 		
 		
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 	public void paint(Graphics g) {
 		drawBackground(g);
 		drawSpaces(g);
 		drawLines(g);
 		
 		//TODO: change to pass in appropriate board state
 		drawPieces(g);
 		//drawPieces(g, new GameBoard());
 	}
 	
 	private void drawBackground(Graphics g) {
 		g.setColor(Color.lightGray);
 		g.fillRect(0, 0, Main.windowWidth, Main.windowHeight);
 	}
 	
 	private void drawSpaces(Graphics g) {
 		Dimension panelSize = getParent().getSize();
 		System.out.println(panelSize);
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
 			g2.drawLine(i * Main.tileSize + spacing, spacing, i * Main.tileSize + spacing, getParent().getHeight() - spacing);
 		}
 		
 		// Draw horizontal lines
 		for (int i = 0; i < Main.verticalSpaces; i++) {
 			g2.drawLine(spacing, i * Main.tileSize + spacing, getParent().getWidth() - spacing, i * Main.tileSize + spacing);
 		}
 		
 		// Draw right diagonals
 		for (int i = 0; i < Main.horizontalSpaces; i++) {
 			// Draw every other space diagonal
 			if (i % 2 == 0 && i + 4 < Main.horizontalSpaces) {
 				int xStart = i * Main.tileSize + spacing;
 				int xEnd = (i + 4) * Main.tileSize + spacing;
 				int yStart = spacing;
 				int yEnd = getParent().getHeight() - spacing;
 				g2.drawLine(xStart, yStart, xEnd, yEnd);
 			}
 		}
 		// Draw shorter right diagonal
 		g2.drawLine(getParent().getWidth() - (2 * Main.tileSize + spacing) , spacing, getParent().getWidth() - spacing, Main.tileSize * 2 + spacing);
 		g2.drawLine(spacing, 2 * Main.tileSize + spacing, 2 * Main.tileSize + spacing, getParent().getHeight() - spacing);
 		
 		// Draw left diagonals
 		for (int i = Main.horizontalSpaces; i >= 0; i--) {
 			// Draw every other space diagonal
 			if (i % 2 == 0 && i - 4 >= 0) {
 				int xStart = i * Main.tileSize + spacing;
 				int xEnd = (i - 4) * Main.tileSize + spacing;
 				int yStart = spacing;
 				int yEnd = getParent().getHeight() - spacing;
 				g2.drawLine(xStart, yStart, xEnd, yEnd);
 			}
 		}
 		// Draw shorter left diagonal
 		g2.drawLine(2 * Main.tileSize + spacing, spacing, spacing, 2 * Main.tileSize + spacing);
 		g2.drawLine(getParent().getWidth() - spacing, 2 * Main.tileSize + spacing, getParent().getWidth() - 2 * Main.tileSize - spacing, getParent().getHeight() - spacing);
 	}
 	
 	public void updatePieces(GamePiece[][] gamePieces) {
 		pieces.clear();
 		
 		for (int i = 0; i < Main.verticalSpaces; i++) {
 			for (int j = 0; j < Main.horizontalSpaces; j++) {
				//System.out.println("Updating with " + gamePieces[i][j]);
				GamePiece piece = gamePieces[i][j];
				
				if (piece != null) {
					System.out.println("Updating " + piece.getColor() + " piece");
					pieces.add(piece);
				}
					
 			}
 		}
 		this.repaint();
 	}
 	//Draw game pieces over board
 	
 	public void drawPieces(Graphics g) {
 		for (GamePiece piece : pieces) {
 			int x = piece.getColumn();
 			int y = piece.getRow();
 			Color color = piece.getColor();
 			
 			int xCoord = Main.tileSize * x + Main.pieceSize / 2;
 			int yCoord = Main.tileSize * y + Main.pieceSize / 2;
 			g.setColor(color);
 			
 			g.fillOval(xCoord, yCoord, Main.pieceSize, Main.pieceSize);
 		}
 	}
 	/*public void drawPieces(Graphics g, GameBoard board)
 	{
 		BufferedImage blackPieceImg = null;
 		BufferedImage redPieceImg = null;
 		
 		try
 		{
 			//TODO: Create res directory for image files
 			blackPieceImg = ImageIO.read(new File("bin/edu/mharper/tp2/blackpiece.png"));
 			redPieceImg = ImageIO.read(new File("bin/edu/mharper/tp2/redpiece.png"));
 		} 
 		catch(IOException e)
 		{
 			return;
 		}
 		
 		for(int r = 0; r < board.getNumRows(); r++)		
 		{
 			for(int c = 0; c < board.getNumCols(); c++)
 			{
 				//Draw individual piece
 				GamePiece piece = board.getPiece(r,c);
 				if(piece == null)
 					continue;
 				
 				int drawX = c * Main.tileSize;
 				int drawY = r * Main.tileSize;
 				
 				if(piece.getColor() == GamePiece.Color.Black)
 					g.drawImage(blackPieceImg, drawX, drawY, null);
 				else
 					g.drawImage(redPieceImg, drawX, drawY, null);
 		
 			}
 		}
 	}*/
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if (event.getActionCommand().equals("New game")) {
 			// New game
 			// Tell the game logic to reset the board
 			this.update(getGraphics());
 		}
 		else if (event.getActionCommand().equals("Exit")) {
 			System.exit(0);
 		}
 		
 	}
 	
 	
 }
