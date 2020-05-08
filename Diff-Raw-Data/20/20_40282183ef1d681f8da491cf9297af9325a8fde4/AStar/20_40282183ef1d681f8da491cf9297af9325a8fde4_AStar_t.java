 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.Timer;
 
 
 public class AStar extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private final static JFrame frame = new JFrame("A* Search Algorithm");
 	
 	public static class AStarPanel extends JPanel {
 		
 		private static final long serialVersionUID = 1L;
 		private static LinkedList<Tile> path = new LinkedList<Tile>(),
 				neighbors = new LinkedList<Tile>();
 		PriorityQueue<Tile> queue = new PriorityQueue<Tile>();
 		private Tile[][] tiles;
 		private Tile startTile = null, endTile = null, selectedTile, bestTile;
 		private final Color defaultOutline = new Color(204, 0, 0), defaultFill = new Color(240, 49, 49),
 				startOutline = new Color(7, 157, 208), startFill = new Color(36, 173, 222),
 				endOutline = new Color(102, 153, 0), endFill = new Color(138, 189, 0),
 				selectedOutline = new Color(255, 138, 0), selectedFill = new Color(255, 174, 24);
 		private Timer timer;
		private final int DELAY = 200;
 		private boolean isRunning = false, isCtrlDown;
 		
 		/**
 		 * Creates the grid used for path calculations
 		 */
 		public AStarPanel() {
 			tiles = new Tile[8][8];
 			for(int y = 0; y < tiles.length; y++) {
 				for(int x = 0; x < tiles[0].length; x++) {
 					tiles[x][y] = new Tile(x * 60, y * 60, 60, 60);
 				}
 			}
 			
 			setBackground(Color.WHITE);
 			addMouseListener(new MouseAction());
 			addKeyListener(new KeyboardAction());
 			setFocusable(true);
			timer = new Timer(DELAY, new Update());
 			timer.start();
 		}
 		
 		/**
 		 * Initializes the path finder by setting values for start & end tile sand
 		 * adds the start tile to the queue
 		 */
 		private void run() {
 			startTile.setG(0);
 			startTile.setF(endTile);
 			endTile.setH(0);
 			
 			// A priority queue keeps tile with lowest F value on top
 			queue.add(startTile);	// Begin by adding the start tile
 			findShortestPath();	// Calculate shortest path
 		}
 		
 		/**
 		 * Continuously called by the Timer to update the path and display progress.
 		 * This can be condensed to a single while loop for most practical uses.
 		 */
 		private void findShortestPath() {
 			if(queue.contains(endTile)) {
 				selectedTile = null;
 				
 				// Creates the path by tracing back from the end tile
 				path.add(endTile);
 				while(path.peek().getPrev() != null) {
 					path.addFirst(path.peek().getPrev());
 				}
 				
 				// Highlights the path
 				for(Tile tile : path) {
 					if(tile != startTile && tile != endTile) {
 						tile.setOutline(selectedOutline);
 						tile.setFill(selectedFill);
 					}
 				}
 				isRunning = false;
 			} else {
 				// Continue until end is found.  Can result in infinite loop if 
 				// the end tile is not accessible since the problem is NP-hard.
 				updatePath();
 			}
 		}
 		
 		/**
 		 * Executes a step in the calculation of the path. When a tile is examined, its
 		 * neighbors are added to a list and each neighbor is checked to see if moving
 		 * to it from the current tile will result in a more efficient path than one
 		 * that has already been established.
 		 */
 		private void updatePath() {
 			if(neighbors.isEmpty()) {
 				bestTile = queue.poll();			// Grab the best tile and remove from queue
 				selectedTile = bestTile;			// Highlights the selected tile in GUI
 				neighbors = getNeighbors(bestTile);	// Find the tile's neighbors
 			}
 			
 			int stepCost = bestTile.getG() + 1;	// Set the G cost of travelling to neighbor
 			
 			Tile neighbor = neighbors.pop();	// Check a neighbor...
 			if(!neighbor.isObstacle() && stepCost < neighbor.getG()) {	// If new G is lower:
 				neighbor.setG(stepCost);		// Update new G cost
 				neighbor.setPrev(bestTile);		// Update previous tile to reflect best path
 				neighbor.setF(endTile);			// Re-calculate F cost
 				queue.add(neighbor);			// Add neighbor to queue
 			} else {
 				updatePath();	// Check another neighbor if there is no change. This is 
 								// only done to make the animation smoother.
 			}
 			
 		}
 		
 		/**
 		 * Adds all adjacent neighbors to list
 		 */
 		private LinkedList<Tile> getNeighbors(Tile tile) {
 			LinkedList<Tile> neighbors = new LinkedList<Tile>();
 			int x = tile.getX() / tile.getWidth();
 			int y = tile.getY() / tile.getHeight();
 			
 			if(x - 1 >= 0) {	// These if-statements check for bounds of array
 				neighbors.add(tiles[x - 1][y]);
 			}
 			if(y - 1 >= 0) {
 				neighbors.add(tiles[x][y - 1]);
 			}
 			if(x + 1 < tiles[0].length) {
 				neighbors.add(tiles[x + 1][y]);
 			}
 			if(y + 1 < tiles.length) {
 				neighbors.add(tiles[x][y + 1]);
 			}
 			
 			return neighbors;
 		}
 
 		@Override
 		protected void paintComponent(final Graphics gr) {
 			super.paintComponent(gr);
 			Graphics2D g = (Graphics2D)gr;
 			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 	                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);	// Fancy text is the best text
 			
 			for(Tile[] row : tiles) {	// Draws tiles in the GUI
 				for(Tile tile : row) {
 					if(tile == selectedTile) {
 						tile.draw(g, true);
 					} else {
 						tile.draw(g, false);
 					}
 				}
 			}
 
 			g.setColor(Color.BLACK);
 			g.drawString("Left Click: Start tile     Ctrl+Click: Obstacle     Right Click: End tile", 18, 493);
 		}
 		
 		public class Update implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(isRunning && path.isEmpty()) {
 					findShortestPath();
 				}
 				repaint();
 			}
 		}
 		
 		// Controls selection of start end tiles. Tiles are referenced [x][y].
 		// X origin is on the left and Y origin is at the top of the grid.
 		private class MouseAction extends MouseAdapter {
 			@Override
 			public void mouseClicked(MouseEvent e) {
				Tile tile = tiles[e.getX() / 60][e.getY() / 60];
 				if(e.getButton() == MouseEvent.BUTTON1 && isCtrlDown && !isRunning) {
 					if(tile != startTile && tile != endTile) tile.toggleObstacle();
				} else if(e.getButton() == MouseEvent.BUTTON1 && !isRunning && tile != endTile) {
 					if(!path.isEmpty()) {
 						resetTiles();
 					} else if(startTile != null) {
 						startTile.setOutline(defaultOutline);
 						startTile.setFill(defaultFill);
 					}
					startTile = tile;
 					startTile.setOutline(startOutline);
 					startTile.setFill(startFill);
				} else if(e.getButton() == MouseEvent.BUTTON3 && !isRunning && tile != startTile) {
 					if(!path.isEmpty()) {
 						resetTiles();
 					} else if(endTile != null) {
 						endTile.setOutline(defaultOutline);
 						endTile.setFill(defaultFill);
 					}
					endTile = tile;
 					endTile.setOutline(endOutline);
 					endTile.setFill(endFill);
 				}
 				
 				repaint();
 				if(!isRunning && startTile != null && endTile != null && path.isEmpty()) {
 					run();					
 					isRunning = true;
 					timer.restart();
 				}
 			}
 			
 			/**
 			 * Resets all of the tile values for calculating a new path.
 			 */
 			private void resetTiles() {
 				isRunning = false;
 				startTile = endTile = selectedTile = null;
 				path.clear();
 				queue.clear();
 				neighbors.clear();
 				for(Tile[] row : tiles) {
 					for(Tile tile : row) {
 						tile.reset();
 					}
 				}
 			}
 		}
 		
 		private class KeyboardAction extends KeyAdapter {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if(e.getKeyCode() == KeyEvent.VK_CONTROL) isCtrlDown = true;
 			}
 			
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if(e.getKeyCode() == KeyEvent.VK_CONTROL) isCtrlDown = false;
 			}
 		}
 	}
 	
 	public static void main(String[] args) {
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		AStarPanel panel = new AStarPanel();
 		frame.add(panel);
 		// width + 7, height + 45
 		frame.setSize(487, 525);
 		frame.setResizable(false);
 		frame.setVisible(true);
 	}
 	
 	public static void close() {
 		frame.dispose();
 	}
 }
 
