 package edu.wheaton.simulator.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import edu.wheaton.simulator.gui.screen.EditEntityScreen;
 
 public class IconGridPanel extends GridPanel {
 
 	private static final long serialVersionUID = 8466121263560126226L;
 
 	private ScreenManager sm;
 
 	private int width;
 
 	private int height;
 
 	private int gridDimension;
 
 	private int pixelWidth;
 
 	private int pixelHeight;
 
 	private int squareSize;
 
 	private boolean[][] icon;
 
 	public IconGridPanel(final SimulatorFacade gm) {
 		super(gm);
 		sm = Gui.getScreenManager();
 		gridDimension = 7;
 		icon = new boolean[gridDimension][gridDimension];
 		for (int i = 0; i < gridDimension; i++){
 			for( int j = 0; j < gridDimension; j++){
 				icon[i][j] = false;
 			}
 		}
 		this.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent me) {
 				width = getWidth();
 				height = getHeight();
 				pixelWidth = width / gridDimension;
 				pixelHeight = height / gridDimension;
 				squareSize = Math.min(pixelWidth, pixelHeight);
 				int x = me.getX();
 				int y = me.getY();
 				int xIndex = x/squareSize;
 				int yIndex = y/squareSize;
				if(xIndex > 0 && xIndex < 7 && yIndex > 0 && yIndex < 7){
 					icon[xIndex][yIndex] = !icon[xIndex][yIndex];
 				}
 				repaint();
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {}
 
 		});
 	}
 
 	@Override
 	public void paint(Graphics g) {
 
 		width = getWidth();
 		height = getHeight();
 		pixelWidth = width / gridDimension;
 		pixelHeight = height / gridDimension;
 		squareSize = Math.min(pixelWidth, pixelHeight);
 		g.setColor(Color.BLACK);
 		for (int i = 0; i < gridDimension; i++) {
 			for (int j = 0; j < gridDimension; j++) {
 				g.drawRect(squareSize * i, squareSize * j, 
 						squareSize, squareSize);
 			}
 		}
 		clearAgents(g);
 		agentPaint(g);
 	}
 
 	@Override
 	public void agentPaint(Graphics g){
 
 		width = getWidth();
 		height = getHeight();
 		pixelWidth = width / gridDimension;
 		pixelHeight = height / gridDimension;
 		squareSize = Math.min(pixelWidth, pixelHeight);
 
 		Color color = ((EditEntityScreen)sm.getScreen("Edit Entities")).getColor();
 		g.setColor(color);
 		for (int i = 0; i < gridDimension; i++) {
 			for (int j = 0; j < gridDimension; j++) {
 				if(icon[i][j] == true){
 					g.fillRect(squareSize * i + 1, squareSize * j + 1, 
 							squareSize - 1, squareSize - 1);
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public void clearAgents(Graphics g) {
 
 		width = getWidth();
 		height = getHeight();
 		pixelWidth = width / gridDimension;
 		pixelHeight = height / gridDimension;
 		squareSize = Math.min(pixelWidth, pixelHeight);
 
 		squareSize = Math.min(pixelWidth, pixelHeight) - 1;
 		g.setColor(Color.WHITE);
 		for (int x = 0; x < gridDimension; x++) {
 			for (int y = 0; y < gridDimension; y++) {
 				g.fillRect(squareSize * x + (x + 1), squareSize * y + (y + 1), squareSize, squareSize);
 			}
 		}
 	}
 
 	public void setIcon(boolean[][] icon){
 		this.icon = icon;
 	}
 }
 
