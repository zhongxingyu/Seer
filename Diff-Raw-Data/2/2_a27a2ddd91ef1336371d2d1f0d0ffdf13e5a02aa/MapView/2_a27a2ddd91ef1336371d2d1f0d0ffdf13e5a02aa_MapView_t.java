 package editor;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.TexturePaint;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.filechooser.FileFilter;
 import main.ResourceManager;
 
 /**
  * MapViewer.java<p>
  * 
  * This class is used for viewing and editing map files.<p>
  * Part of the MapEditor.
  * 
  */
 public class MapView extends JPanel implements MouseListener, MouseMotionListener {
 
 	private static final long serialVersionUID = 1986396425980276084L;
 	
 	/** ArrayList to hold all of the Squares in the map */
 	public static ArrayList<Square> squares;
 	
 	/** Holds initial values of square for TileSelection.PAINTMODE_SQUARE */
 	private int squareX, squareY;
 	
 	TexturePaint paint;
 	
 	BufferedImage im;
 	
 	/** 
 	 * Constructor to create our MapView frame. We are implementing JPanel so 
 	 * that we can draw to the screen. Create a JFrame to hold our JPanel and
 	 * add necessary Event Listeners and initialise variables.
 	 */
 	public MapView() {
 
 		// Call our super constructor, layout=null, doubleBuffered=true
 		super(null, true);
 
 		JFrame f = new JFrame("MapViewer");
 		
 		f.add(this);
 		f.setBounds(new Rectangle(Editor.xMapView,Editor.yMapView,Editor.wMapView,Editor.hMapView));
 		f.setResizable(true);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBackground(Color.white);
 		f.setVisible(true);
 		
 		f.addComponentListener(new ComponentAdapter() {
 			
 			public void componentResized(ComponentEvent e) {
 				repaint();
 			}
 		});
 		
 		setPreferredSize(new Dimension(Editor.levelWidth,Editor.levelHeight));
 		setMinimumSize(f.getSize());
 		
 		JScrollPane scroller = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		scroller.setMaximumSize(f.getSize());
 		scroller.setBounds(0, 0, Editor.wMapView,Editor.hMapView);
 		scroller.setBackground(Color.white);
 		f.add(scroller);
 		
 		scroller.setViewportView(this);
 		
 		addMouseListener(this);
 		addMouseMotionListener(this);
 		
 		squares = new ArrayList<Square>();
 		
 		try {
 			im = ImageIO.read(ResourceManager.getResourceAsStream("../res/img/tileset.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Our method to draw to our JPanel.<p>
 	 * - Clear our screen<br>
 	 * - Draw our squares<br>
 	 * - Draw our grid
 	 */
 	public void paintComponent(Graphics g) {
 		
 		Graphics2D g2 = (Graphics2D)g;
 		
 		drawSquares(g2);
 		
 		drawGrid(g2);
 	}
 
 	/**
 	 * Draws our grid to the screen for aid of alignment and size.
 	 * 
 	 * @param g Graphics object
 	 */
 	public void drawGrid(Graphics2D g) {
 
 		g.setColor(Color.black);
 		
 		// Draw vertical lines
 		for (int i = 0; i <= Editor.levelWidth/Editor.gridSize; i++) {
 			
 			if (i % Editor.xCol == 0) {
 				g.setStroke(new BasicStroke(1.5f));
 			} else {
 				g.setStroke(new BasicStroke(0.2f));
 			}
 			
 			g.drawLine(i*Editor.gridSize, 0, i*Editor.gridSize, Editor.levelHeight);
 		}
 		// Draw horizontal lines
 		for (int i = 0; i <= Editor.levelHeight/Editor.gridSize; i++) {
 
 			if (i % Editor.xCol == 0) {
 				g.setStroke(new BasicStroke(1.5f));
 			} else {
 				g.setStroke(new BasicStroke(0.2f));
 			}
 			
 			g.drawLine(0, i*Editor.gridSize, Editor.levelWidth, i*Editor.gridSize);
 		}
 	}
 
 	/**
 	 * Draw the squares to the screen.
 	 * 
 	 * @param g Graphics object
 	 */
 	private void drawSquares(Graphics2D g) {
 		
 		for (Square s : squares) {
 			
			paint = new TexturePaint(im.getSubimage((s.getSymbol()%16)*16, (int)(s.getSymbol()/16)*16, 16, 16), new Rectangle(0, 0, 16, 16));
 			
 			g.setPaint(paint);
 			
 			g.fillRect(s.getX()*Editor.gridSize, s.getY()*Editor.gridSize, Editor.gridSize, Editor.gridSize);
 		}
 	}
 
 	/**
 	 * Load a map from a file and update our ArrayList.
 	 * 
 	 * TODO: Find a way to load multiple maps at once
 	 * 
 	 * @throws IOException
 	 */
 	public static void loadMap() throws IOException {
 		
 		File f;
 		String path;
 		
 		// Require user confirmation to clear the map
 		if (JOptionPane.showConfirmDialog(new JFrame(), 
 				"Loading a map will delete the current map. Continue?", 
 				"Load Map", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
 			return;
 		} else {
 				// Browse file to dialog to find file
 				JFileChooser c = new JFileChooser();
 				c.setDialogTitle("Choose a map file to load");
 				int v = c.showOpenDialog(null);
 				if (v== JFileChooser.APPROVE_OPTION) {
 					path = c.getSelectedFile().getAbsolutePath();
 				} else {
 					return;
 				}
 		}
 		
 		try {
 			f = new File(path);
 			if (!f.exists())
 				throw new FileNotFoundException();
 		} catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(new JFrame(), "Could not find the specified file");
 			return;
 		} 
 		
 		Scanner s = new Scanner(f);
 		
 		squares.clear();
 		
 		Tile t;
 		
 		while (s.hasNext()) {
 			
 			for (int i = 0; i < Editor.yRow; i++) {
 				
 				String[] strs = s.nextLine().split(",");
 				
 				for (int j = 0; j < Editor.xCol; j++) {
 					t = getTile(Integer.parseInt(strs[j]));
 					squares.add(new Square(j, i, t));
 				}
 			}
 		}
 		
 		s.close();
 		
 	}
 	
 	/**
 	 * Return Tile object that is referenced by symbol.
 	 * 
 	 * @param symbol The character that references the required Tile
 	 * @return 	null if the Tile associated with symbol does not exist.<br>
 	 * 		The Tile object referenced by symbol.
 	 */
 	private static Tile getTile(int symbol) {
 		
 		for (Tile t : TileSelection.tiles) {
 			if (t.getSymbol() == symbol) {
 				return t;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Save the current map to a file.
 	 * 
 	 * @throws IOException
 	 */
 	public static void saveMap() throws IOException {
 		
 		if (squares.size() < Editor.xCol*Editor.yRow) {
 			if (JOptionPane.showConfirmDialog(new JFrame(), 
 					"The Map looks like it isn't full.\n" +
 					"Would you like to fill it with empty " +
 					"squares and continue?") == JOptionPane.NO_OPTION) {
 				return;
 			}
 		}
 		
 		fillEmptySquares();
 		
 		try {
 			writeMapsToFile();
 		} catch (NullPointerException e) {
 			
 		}
 				
 	}
 	
 	/**
 	 * Write the squares to a file so that it can be accessed externally.
 	 * 
 	 * @param file Output file for the map
 	 * @throws IOException
 	 */
 	private static void writeMapsToFile() throws IOException {
 		
 		BufferedWriter w = null;
 		Square tmp;
 		String filename;
 		JFileChooser c = new JFileChooser();
 		
 		c.setFileFilter(new FileFilter() {
 			
 			String description = "Map files (*.map)";
 			
 			@Override
 			public String getDescription() {
 				return description;
 			}
 			
 			@Override
 			public boolean accept(File f) {
 				if (f==null) return false;
 				if (f.isDirectory()) return true;
 				if (f.getName().endsWith(".map")) return true;
 				return false;
 			}
 		});
 		
 		for (int i = 0; i < Editor.levelHeight/Editor.gridSize; i+=32) {
 			for (int j = 0; j < Editor.levelWidth/Editor.gridSize; j+=32) {
 				
 				c.setDialogTitle("Choose save location for map (" + j + ", " + i + ")");
 
 				if (c.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
 					filename = c.getSelectedFile().getAbsolutePath();
 					
 					if (!filename.substring(filename.length()-4, filename.length()).equals(".map")) {
 						filename += ".map";
 					}
 				} else {
 					return;
 				}
 				
 				w = new BufferedWriter(new FileWriter(new File(filename)));
 				
 				for (int y = i; y < i+32; y++) {
 					for (int x = j; x < j+32; x++) {
 						tmp = getSquare(x, y);
 						w.write(tmp.getSymbol() + ",");
 					}
 					w.write(System.getProperty("line.separator"));
 				}
 				
 				w.close();
 			}
 		}
 		
 		w.close();
 	}
 	
 	/**
 	 * Fill the undefined squares with an empty square.
 	 * 
 	 */
 	private static void fillEmptySquares() {
 		
 		for (int i = 0; i <= Editor.levelHeight/Editor.gridSize; i++) {
 			for (int j = 0; j <= Editor.levelWidth/Editor.gridSize; j++) {
 				if (getSquare(j, i) == null) {
 					squares.add(new Square(j, i, TileSelection.tiles.get(0)));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Returns the square at location (x,y).
 	 * 
 	 * @param x The x coordinate of the square
 	 * @param y The y coordinate of the square
 	 * @return 	Square object at (x,y) otherwise<br>
 	 * 		null if square does not exist at (x,y)
 	 */
 	private static Square getSquare(int x, int y) {
 		
 		Square sq = new Square(x, y);
 		
 		for (Square s : squares) {
 			if (s.equals(sq)) {
 				return s;
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Overrides MouseListener.mouseReleased(MouseEvent e).<p>
 	 * Handles PAINTMODE_SINGLE
 	 * 
 	 */
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		
 		if (TileSelection.getPaintMode() == TileSelection.PAINTMODE_SINGLE) {
 			
 			float divX = (float)e.getX()/(float)Editor.gridSize;
 			float divY = (float)e.getY()/(float)Editor.gridSize;
 
 			int x1 = (int) Math.floor(divX);
 			int y1 = (int) Math.floor(divY);
 
 			Square square = new Square(x1, y1, TileSelection.tile);
 
 			for (Square s : squares) {
 				if (s.equals(square)) {
 					squares.remove(square);
 					repaint();
 					return;
 				}
 			}
 			
 			squares.add(square);
 		} 
 		
 		repaint();
 	}
 	
 	/**
 	 * Overrides MouseListener.mousePressed(MouseEvent e).<p>
 	 * Handles returning the x and y coordinates of the mouse 
 	 * for PAINTMODE_SQUARE
 	 * 
 	 */
 	@Override
 	public void mousePressed(MouseEvent e) { 
 		
 		if (TileSelection.getPaintMode() == TileSelection.PAINTMODE_SQUARE) {
 			
 			float divX = (float)e.getX()/(float)Editor.gridSize;
 			float divY = (float)e.getY()/(float)Editor.gridSize;
 
 			squareX = (int) Math.floor(divX);
 			squareY = (int) Math.floor(divY);
 
 		}
 		
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent arg0) { }
 	
 	@Override
 	public void mouseEntered(MouseEvent arg0) { }
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {  }
 
 	@Override
 	public void mouseMoved(MouseEvent e) { }
 	
 	/**
 	 * Overrides MouseMotionListener.mouseDragged(MouseEvent e).<p>
 	 * Handles the painting for PAINTMODE_CONSTANT and PAINTMODE_SQUARE
 	 * 
 	 */
 	@Override
 	public void mouseDragged(MouseEvent e) { 
 		
 		if (TileSelection.getPaintMode() == TileSelection.PAINTMODE_CONSTANT) {
 			
 			float divX = (float)e.getX()/(float)Editor.gridSize;
 			float divY = (float)e.getY()/(float)Editor.gridSize;
 
 			int x1 = (int) Math.floor(divX);
 			int y1 = (int) Math.floor(divY);
 			
 			Square square = new Square(x1, y1, TileSelection.tile);
 			
 			squares.remove(square);
 			squares.add(square);
 			
 			//repaint();
 		}
 		
 		/**	TODO: Improve this method to retract the square and consequently delete
 		 * 	the drawn squares from the screen and array
 		 */
 		if (TileSelection.getPaintMode() == TileSelection.PAINTMODE_SQUARE) {
 			
 			float divX = (float)e.getX()/(float)Editor.gridSize;
 			float divY = (float)e.getY()/(float)Editor.gridSize;
 
 			int x1 = (int) Math.floor(divX);
 			int y1 = (int) Math.floor(divY);
 			
 			for (int j = Math.min(squareY, y1); j <= Math.max(squareY, y1); j++) {
 				for (int i = Math.min(squareX, x1); i <= Math.max(squareX, x1); i++) {
 				
 					Square square = new Square(i, j, TileSelection.tile);
 					
 					squares.remove(square);
 					squares.add(square);
 					
 				}
 			}
 			
 			//repaint();
 			
 		}
 		
 	}
 }
