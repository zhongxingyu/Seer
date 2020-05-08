 package frontend;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 
 import backend.*;
 import backend.event.*;
 
 // TODO stop extending Jtogglebutton
 @SuppressWarnings("serial")
 public class MineSpot extends JComponent implements 
 	MouseListener, ClickedSpotEventListener, GameEndListener {
 
 	public static final int SIZE = 40;
 	public static final Color HIGHLIGHT = Color.CYAN;
 
 	private Spot spot;
 	private Minesweeper game;
 	private boolean enabled;
 	private boolean highlight;
 	
 	public MineSpot(Spot s, Minesweeper m) {
 		setSpot(s);
 		setGame(m);
 		addMouseListener(this);
 		spot.addEventListener(this);
 		game.addEventListener(this);
 		enabled = true;
 		highlight = false;
 	}
 
 	private void setGame(Minesweeper m) {
 		game = m;
 	}
 
 	public void setSpot(Spot s) {
 		spot = s;
 	}
 
 	public void paint(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  
                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
 
 		if (spot.isOpen()) {
 			if(spot.isBomb()){
 				g2.setColor(Color.BLACK);
 				g2.fill3DRect(0, 0, MineSpot.SIZE, MineSpot.SIZE, true);
 			}
 			else{
 				g2.setColor(Color.blue);
 				switch (spot.getState()) {
 				case 0:
 					return;
 				case 1:
 					g2.setColor(Color.BLUE); break;
 				case 2:
 					g2.setColor(Color.GREEN); break;
 				case 3:
 					g2.setColor(Color.RED); break;
 				case 4:
 					g2.setColor(Color.MAGENTA); break;
 				case 5:
 					g2.setColor(Color.CYAN); break;
 				case 6:
 					g2.setColor(Color.ORANGE); break;
 				case 7:
 					g2.setColor(Color.PINK); break;
 				case 8:
 					g2.setColor(Color.YELLOW); break;
 				}
 				String s = "" + spot.getState();
 				
 				//The following block is Copyright 2003 Fred Swartz MIT License
 				//http://leepoint.net/notes-java/GUI-appearance/fonts/18font.html
 				{
 					// Find the size of string s in font f in the current Graphics context g.
 					FontMetrics fm   = g2.getFontMetrics(g2.getFont());
 					java.awt.geom.Rectangle2D rect = fm.getStringBounds(s, g);
 	
 					int textHeight = (int)(rect.getHeight()); 
 					int textWidth  = (int)(rect.getWidth());
 					int panelHeight= this.getHeight();
 					int panelWidth = this.getWidth();
 	
 					// Center text horizontally and vertically
 					int x = (panelWidth  - textWidth)  / 2;
 					int y = (panelHeight - textHeight) / 2  + fm.getAscent();
 	
 					g.drawString(s, x, y);  // Draw the string.
 				}
 			}
 		}
 		else{
 			if(highlight){
 				g2.setColor(HIGHLIGHT);
 			}
 			else if(spot.isFlagged()){
 				g2.setColor(Color.YELLOW);
 				if(!game.isActive() && !spot.isBomb())
 					g2.setColor(Color.PINK);
 			}
 			else
 				g2.setColor(Color.GRAY);
 			g2.fill3DRect(0, 0, MineSpot.SIZE, MineSpot.SIZE, true);
 
 		}
 	}
 
 	public Spot getSpot() {
 		return spot;
 	}
 
 
 	/**
 	 * Event Listener for the Spot's opening event
 	 */
 	@Override
 	public void handleEvent(ClickedSpotEvent e) {
 		repaint();
 		if(e.getStatus() == ClickedSpotEvent.CLICK)
 			setEnabled(false);
 	}
 
 	@Override
 	public void handleEvent(GameEndEvent e) {
 		setEnabled(false);
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		highlight = true;
 		repaint();
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		highlight = false;
 		repaint();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		switch(e.getButton()){
 		case MouseEvent.BUTTON1: System.out.println("1"); break;
 		case MouseEvent.BUTTON3: System.out.println("3"); break;
 		}
 		if(SwingUtilities.isLeftMouseButton(e)){
 			if(!spot.isFlagged()){
 				game.open(spot.loc);
 				// TODO dual click?
 				if(e.getButton() == MouseEvent.BUTTON3_DOWN_MASK){
 					ArrayList<Spot> n = game.getGrid().getNeighbors(spot.loc);
 					for(Spot s : n){
 						if(!s.isFlagged())
 							game.open(s.loc);
 					}
 				}
 			}
 		}
		else if (SwingUtilities.isRightMouseButton(e) && game.isEnabled()){
 			if(!spot.isOpen())
 				spot.toggleFlag();
 		}
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {}
 }
